package dao;

import model.Compra;
import model.DetalleCompra;
import config.ConexionDB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * registra abastecimiento, impacto en inventario y pagos asociados.
 * El enfoque transaccional garantiza consistencia: o se guarda todo el proceso de compra, o no se guarda nada.
 */
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
    /**
     * Persiste una compra completa en una sola transacción (cabecera, detalle, inventario, pago y crédito si aplica).
     *
     * @param compra datos de la compra con detalles y banderas de crédito
     * @return {@code true} si se hizo commit
     * @throws Exception si falla algún paso (se hace rollback)
     */
    public boolean insertarConTransaccion(Compra compra) throws Exception {
        Connection con = null;
        try {
            con = ConexionDB.getConnection();
            con.setAutoCommit(false);

            int compraId = insertarCompra(con, compra);
            compra.setCompraId(compraId);

            insertarDetalles(con, compraId, compra.getDetalles());

            // En negocio necesitamos trazabilidad de quién registró la entrada de inventario;
            // por eso se propaga usuarioId hacia Inventario_Movimiento.
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

    /**
     * Actualiza stock y precio de costo por cada línea y registra movimiento de inventario.
     *
     * @param con conexión activa (misma transacción)
     * @param detalles líneas de la compra
     * @param referencia texto para {@code Inventario_Movimiento}
     * @param usuarioId usuario para el movimiento o {@code null}
     * @throws SQLException si un UPDATE no afecta filas u otro error SQL
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

    /**
     * @param con conexión activa
     * @param c datos de cabecera (proveedor, fechas)
     * @return {@code compra_id} generado
     * @throws SQLException si no se obtienen claves
     */
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

    /**
     * @param con conexión activa
     * @param compraId ID de la cabecera
     * @param detalles líneas de producto
     * @throws SQLException si falla el batch
     */
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

    /**
     * @param con conexión activa
     * @param compraId compra asociada
     * @param metodoPagoId método de pago
     * @param monto importe del pago
     * @param estado {@code confirmado} o {@code pendiente} según el caso
     * @throws SQLException si falla el insert
     */
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

    /**
     * @param con conexión activa
     * @param compraId compra asociada
     * @param montoTotal monto total del crédito
     * @param saldoPendiente saldo restante
     * @param fechaInicio inicio del crédito
     * @param fechaVencimiento vencimiento
     * @param estado estado del crédito en la BD
     * @return {@code credito_id} generado
     * @throws SQLException si no se obtienen claves
     */
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

    /**
     * @param con conexión activa
     * @param creditoId crédito al que pertenece el abono
     * @param metodoPagoId método de pago del abono
     * @param montoAbono importe
     * @param estado estado del abono
     * @throws SQLException si falla el insert
     */
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

    /**
     * Busca una compra por su ID incluyendo detalles, pago y crédito si aplica.
     *
     * @param compraId identificador de la compra
     * @return objeto {@link Compra} cargado o {@code null}
     * @throws Exception si falla la consulta
     */
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

    /**
     * Retorna los productos detallados de una compra.
     *
     * @param con conexión activa
     * @param compraId ID de compra
     * @return lista de líneas
     * @throws SQLException si falla la consulta
     */
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

    /**
     * Retorna todas las compras de un proveedor ordenadas por fecha descendente.
     *
     * @param proveedorId identificador del proveedor
     * @return lista de compras con detalles cargados
     * @throws Exception si falla la consulta
     */
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

    /**
     * Elimina una compra. Los detalles y pagos se eliminan por CASCADE en la BD.
     *
     * @param compraId identificador de la compra
     * @return {@code true} si se borró una fila
     * @throws Exception si falla el delete
     */
    public boolean eliminarConTransaccion(int compraId) throws Exception {
        String sql = "DELETE FROM Compra WHERE compra_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, compraId);
            return ps.executeUpdate() > 0;
        }
    }
}