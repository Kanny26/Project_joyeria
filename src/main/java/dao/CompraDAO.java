package dao;

import model.Compra;
import model.DetalleCompra;
import config.ConexionDB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompraDAO {

    /*
     * Guarda una compra completa usando una transacción.
     * Si cualquier paso falla, se hace rollback y no queda nada guardado a medias.
     *
     * Flujo:
     *   1. Insertar cabecera en Compra
     *   2. Insertar filas en Detalle_Compra
     *   3. Actualizar stock en Producto + registrar en Inventario_Movimiento
     *   4. Insertar pago en Pago_Compra
     *   5. (si crédito) Insertar en Credito_Compra
     *   6. (si crédito y anticipo > 0) Insertar abono inicial en Abono_Credito
     */
    public boolean insertarConTransaccion(Compra compra) throws Exception {
        Connection con = null;
        try {
            con = ConexionDB.getConnection();
            con.setAutoCommit(false);

            int compraId = insertarCompra(con, compra);
            compra.setCompraId(compraId);

            insertarDetalles(con, compraId, compra.getDetalles());

            // CORRECCIÓN 1: se pasa el adminId desde la compra para registrarlo
            // en Inventario_Movimiento. Si no hay usuario disponible, se puede pasar null.
            registrarEntradaInventario(con, compra.getDetalles(),
                    "Compra #" + compraId, compra.getUsuarioId());

            BigDecimal montoPago  = compra.isEsCredito() ? compra.getAnticipo() : compra.getTotal();
            String     estadoPago = compra.isEsCredito() ? "pendiente" : "confirmado";
            insertarPagoCompra(con, compraId, compra.getMetodoPagoId(), montoPago, estadoPago);

            if (compra.isEsCredito()) {
                BigDecimal saldoPendiente = "pagado".equals(compra.getEstadoCredito())
                        ? BigDecimal.ZERO
                        : compra.getTotal().subtract(compra.getAnticipo()).max(BigDecimal.ZERO);

                int creditoId = insertarCreditoCompra(con, compraId,
                        compra.getTotal(), saldoPendiente,
                        compra.getFechaCompra(), compra.getFechaVencimiento(),
                        compra.getEstadoCredito());

                if (compra.getAnticipo().compareTo(BigDecimal.ZERO) > 0) {
                    insertarAbonoCredito(con, creditoId,
                            compra.getMetodoPagoId(), compra.getAnticipo(), "confirmado");
                }
            }

            con.commit();
            return true;

        } catch (Exception e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /*
     * CORRECCIÓN PRINCIPAL: actualiza el stock e inserta el movimiento de inventario.
     *
     * Bug 1 (causa principal): el UPDATE tenía "AND estado = 1" en el WHERE.
     * Si el producto estaba marcado como inactivo (estado = 0), el UPDATE no afectaba
     * ninguna fila y el stock nunca se actualizaba. Se eliminó esa condición porque
     * al registrar una compra se debe actualizar el stock independientemente del estado.
     *
     * Bug 2: executeBatch() no verifica si algún UPDATE afectó 0 filas.
     * Ahora se valida el resultado de cada UPDATE y se lanza excepción si falla,
     * lo que fuerza el rollback y evita que la compra quede guardada sin stock actualizado.
     *
     * Bug 3: el INSERT de inventario usaba addBatch() pero si fallaba silenciosamente
     * el UPDATE de stock nunca se ejecutaba. Ahora ambas operaciones se hacen fila por fila
     * con executeUpdate() para detectar errores de inmediato.
     *
     * usuarioId puede ser null si no hay usuario de sistema disponible (la columna lo permite).
     */
    private void registrarEntradaInventario(Connection con,
                                             List<DetalleCompra> detalles,
                                             String referencia,
                                             Integer usuarioId) throws SQLException {
        // UPDATE sin "AND estado = 1" para que funcione con cualquier producto
        String sqlStock = """
            UPDATE Producto SET stock = stock + ? WHERE producto_id = ?
            """;

        String sqlMovimiento = """
            INSERT INTO Inventario_Movimiento
                (producto_id, usuario_id, tipo, cantidad, fecha, referencia)
            VALUES (?, ?, 'entrada', ?, NOW(), ?)
            """;

        // UPDATE de precio de costo: sobreescribe con el precio de la compra nueva
        String sqlPrecio = """
            UPDATE Producto SET precio_unitario = ? WHERE producto_id = ?
            """;

        try (PreparedStatement psS = con.prepareStatement(sqlStock);
             PreparedStatement psM = con.prepareStatement(sqlMovimiento);
             PreparedStatement psP = con.prepareStatement(sqlPrecio)) {

            for (DetalleCompra d : detalles) {

                // Paso 1: actualizar stock
                psS.setInt(1, d.getCantidad());
                psS.setInt(2, d.getProductoId());
                int filasActualizadas = psS.executeUpdate();

                if (filasActualizadas == 0) {
                    throw new SQLException(
                        "No se pudo actualizar el stock del producto con ID "
                        + d.getProductoId() + ". Verifica que el producto exista en el sistema."
                    );
                }

                // Paso 2: actualizar precio de costo con el precio de esta compra
                psP.setBigDecimal(1, d.getPrecioUnitario());
                psP.setInt(2, d.getProductoId());
                psP.executeUpdate();

                // Paso 3: registrar movimiento de inventario
                psM.setInt(1, d.getProductoId());
                if (usuarioId != null) {
                    psM.setInt(2, usuarioId);
                } else {
                    psM.setNull(2, Types.INTEGER);
                }
                psM.setInt(3, d.getCantidad());
                psM.setString(4, referencia);
                psM.executeUpdate();
            }
        }
    }

    // ── Paso 1: cabecera de compra ─────────────────────────────────────────────

    private int insertarCompra(Connection con, Compra c) throws SQLException {
        String sql = """
            INSERT INTO Compra (proveedor_id, fecha_compra, fecha_entrega)
            VALUES (?, ?, ?)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getProveedorId());
            ps.setDate(2, new java.sql.Date(c.getFechaCompra().getTime()));
            ps.setDate(3, new java.sql.Date(c.getFechaEntrega().getTime()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                throw new SQLException("No se obtuvo el ID de la compra creada.");
            }
        }
    }

    // ── Paso 2: líneas de detalle ──────────────────────────────────────────────

    private void insertarDetalles(Connection con, int compraId,
                                   List<DetalleCompra> detalles) throws SQLException {
        String sql = """
            INSERT INTO Detalle_Compra (compra_id, producto_id, precio_unitario, cantidad)
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (DetalleCompra d : detalles) {
                ps.setInt(1, compraId);
                ps.setInt(2, d.getProductoId());
                ps.setBigDecimal(3, d.getPrecioUnitario());
                ps.setInt(4, d.getCantidad());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ── Paso 4: registro de pago ───────────────────────────────────────────────

    private void insertarPagoCompra(Connection con, int compraId,
                                     int metodoPagoId, BigDecimal monto,
                                     String estado) throws SQLException {
        String sql = """
            INSERT INTO Pago_Compra (compra_id, metodo_pago_id, monto, estado)
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, compraId);
            ps.setInt(2, metodoPagoId);
            ps.setBigDecimal(3, monto);
            ps.setString(4, estado);
            ps.executeUpdate();
        }
    }

    // ── Paso 5: crédito asociado ───────────────────────────────────────────────

    private int insertarCreditoCompra(Connection con, int compraId,
                                       BigDecimal montoTotal, BigDecimal saldoPendiente,
                                       java.util.Date fechaInicio, java.util.Date fechaVencimiento,
                                       String estado) throws SQLException {
        String sql = """
            INSERT INTO Credito_Compra
                (compra_id, monto_total, saldo_pendiente, fecha_inicio, fecha_vencimiento, estado)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, compraId);
            ps.setBigDecimal(2, montoTotal);
            ps.setBigDecimal(3, saldoPendiente);
            ps.setDate(4, new java.sql.Date(fechaInicio.getTime()));
            ps.setDate(5, new java.sql.Date(fechaVencimiento.getTime()));
            ps.setString(6, estado);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                throw new SQLException("No se obtuvo el ID del crédito creado.");
            }
        }
    }

    // ── Paso 6: abono inicial del crédito ──────────────────────────────────────

    private void insertarAbonoCredito(Connection con, int creditoId,
                                       int metodoPagoId, BigDecimal montoAbono,
                                       String estado) throws SQLException {
        String sql = """
            INSERT INTO Abono_Credito (credito_id, metodo_pago_id, monto_abono, estado)
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, creditoId);
            ps.setInt(2, metodoPagoId);
            ps.setBigDecimal(3, montoAbono);
            ps.setString(4, estado);
            ps.executeUpdate();
        }
    }

    // ── Consultas ──────────────────────────────────────────────────────────────

    /** Busca una compra por su ID incluyendo detalles, pago y crédito si aplica. */
    public Compra obtenerPorId(int compraId) throws Exception {
        String sql = """
            SELECT c.compra_id, c.proveedor_id,
                   c.fecha_compra, c.fecha_entrega,
                   pc.metodo_pago_id, pc.monto AS monto_pago, pc.estado AS estado_pago,
                   cc.credito_id, cc.monto_total, cc.saldo_pendiente,
                   cc.fecha_inicio, cc.fecha_vencimiento, cc.estado AS estado_credito
            FROM Compra c
            LEFT JOIN Pago_Compra    pc ON pc.compra_id = c.compra_id
            LEFT JOIN Credito_Compra cc ON cc.compra_id = c.compra_id
            WHERE c.compra_id = ?
            LIMIT 1
            """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, compraId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Compra compra = new Compra();
                compra.setCompraId(rs.getInt("compra_id"));
                compra.setProveedorId(rs.getInt("proveedor_id"));
                compra.setFechaCompra(rs.getDate("fecha_compra"));
                compra.setFechaEntrega(rs.getDate("fecha_entrega"));
                compra.setMetodoPagoId(rs.getInt("metodo_pago_id"));
                compra.setTotal(rs.getBigDecimal("monto_pago"));

                BigDecimal montoTotal = rs.getBigDecimal("monto_total");
                if (montoTotal != null) {
                    compra.setEsCredito(true);
                    compra.setTotal(montoTotal);
                    BigDecimal saldo = rs.getBigDecimal("saldo_pendiente");
                    compra.setAnticipo(saldo != null ? montoTotal.subtract(saldo) : BigDecimal.ZERO);
                    compra.setFechaVencimiento(rs.getDate("fecha_vencimiento"));
                    compra.setEstadoCredito(rs.getString("estado_credito"));
                }
                compra.setDetalles(obtenerDetalles(con, compraId));
                return compra;
            }
        }
    }

    /** Retorna los productos detallados de una compra. */
    private List<DetalleCompra> obtenerDetalles(Connection con, int compraId) throws SQLException {
        String sql = """
            SELECT dc.detalle_compra_id, dc.producto_id, dc.precio_unitario, dc.cantidad,
                   p.nombre AS nombre_producto
            FROM Detalle_Compra dc
            JOIN Producto p ON p.producto_id = dc.producto_id
            WHERE dc.compra_id = ?
            """;
        List<DetalleCompra> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, compraId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleCompra d = new DetalleCompra();
                    d.setDetalleCompraId(rs.getInt("detalle_compra_id"));
                    d.setProductoId(rs.getInt("producto_id"));
                    d.setProductoNombre(rs.getString("nombre_producto"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setSubtotal(d.getPrecioUnitario()
                            .multiply(new BigDecimal(d.getCantidad())));
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    /** Retorna todas las compras de un proveedor ordenadas por fecha descendente. */
    public List<Compra> listarPorProveedor(int proveedorId) throws Exception {
        String sql = """
            SELECT c.compra_id, c.proveedor_id,
                   c.fecha_compra, c.fecha_entrega,
                   COALESCE(cc.monto_total, pc.monto, 0) AS total_real
            FROM Compra c
            LEFT JOIN Pago_Compra    pc ON pc.compra_id = c.compra_id
            LEFT JOIN Credito_Compra cc ON cc.compra_id = c.compra_id
            WHERE c.proveedor_id = ?
            ORDER BY c.fecha_compra DESC
            """;
        List<Compra> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, proveedorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Compra c = new Compra();
                    c.setCompraId(rs.getInt("compra_id"));
                    c.setProveedorId(rs.getInt("proveedor_id"));
                    c.setFechaCompra(rs.getDate("fecha_compra"));
                    c.setFechaEntrega(rs.getDate("fecha_entrega"));
                    BigDecimal total = rs.getBigDecimal("total_real");
                    c.setTotal(total != null ? total : BigDecimal.ZERO);
                    c.setDetalles(obtenerDetalles(con, c.getCompraId()));
                    lista.add(c);
                }
            }
        }
        return lista;
    }

    /** Elimina una compra. Los detalles y pagos se eliminan por CASCADE en la BD. */
    public boolean eliminarConTransaccion(int compraId) throws Exception {
        String sql = "DELETE FROM Compra WHERE compra_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, compraId);
            return ps.executeUpdate() > 0;
        }
    }
}