package dao;

import config.ConexionDB;
import model.DetalleVenta;
import model.Venta;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Acceso a datos para el módulo de Ventas.
 *
 * Modalidades soportadas:
 *   contado  → un solo registro en Pago_Venta (confirmado) por el total
 *   anticipo → dos registros: anticipo (confirmado) + saldo (pendiente)
 *
 * Métodos de pago: efectivo | tarjeta
 */
public class VentaDAO {

    // ═══════════════════════════════════════════════════════════
    // LISTAR TODAS LAS VENTAS (Administrador)
    // ═══════════════════════════════════════════════════════════
    public List<Venta> listarVentas() throws Exception {
        List<Venta> lista = new ArrayList<>();
        String sql = """
            SELECT vf.venta_id, vf.usuario_id, vf.usuario_cliente_id,
                   uv.nombre AS vendedor, uc.nombre AS cliente,
                   tu.telefono AS telefono_cliente,
                   vf.fecha_emision, vf.total,
                   mp.metodo AS metodo_pago, mp.estado
            FROM venta_factura vf
            JOIN Usuario uv ON uv.usuario_id = vf.usuario_id
            JOIN Usuario uc ON uc.usuario_id = vf.usuario_cliente_id
            LEFT JOIN Telefono_Usuario tu ON tu.usuario_id = vf.usuario_cliente_id
            LEFT JOIN Metodo_pago mp ON mp.venta_id = vf.venta_id
            ORDER BY vf.fecha_emision DESC
            """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Venta v = mapearVenta(rs);
                v.setDetalles(listarDetalles(v.getVentaId(), con));
                calcularAnticipoSaldo(v, con);
                lista.add(v);
            }
        }
        return lista;
    }

    // ═══════════════════════════════════════════════════════════
    // LISTAR VENTAS POR VENDEDOR
    // ═══════════════════════════════════════════════════════════
    public List<Venta> listarPorVendedor(int vendedorId) throws Exception {
        List<Venta> lista = new ArrayList<>();
        String sql = """
            SELECT vf.venta_id, vf.usuario_id, vf.usuario_cliente_id,
                   uv.nombre AS vendedor, uc.nombre AS cliente,
                   tu.telefono AS telefono_cliente,
                   vf.fecha_emision, vf.total,
                   mp.metodo AS metodo_pago, mp.estado
            FROM venta_factura vf
            JOIN Usuario uv ON uv.usuario_id = vf.usuario_id
            JOIN Usuario uc ON uc.usuario_id = vf.usuario_cliente_id
            LEFT JOIN Telefono_Usuario tu ON tu.usuario_id = vf.usuario_cliente_id
            LEFT JOIN Metodo_pago mp ON mp.venta_id = vf.venta_id
            WHERE vf.usuario_id = ?
            ORDER BY vf.fecha_emision DESC
            """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, vendedorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Venta v = mapearVenta(rs);
                    v.setDetalles(listarDetalles(v.getVentaId(), con));
                    calcularAnticipoSaldo(v, con);
                    lista.add(v);
                }
            }
        }
        return lista;
    }

    // ═══════════════════════════════════════════════════════════
    // OBTENER VENTA POR ID
    // ═══════════════════════════════════════════════════════════
    public Venta obtenerPorId(int ventaId) throws Exception {
        String sql = """
            SELECT vf.venta_id, vf.usuario_id, vf.usuario_cliente_id,
                   uv.nombre AS vendedor, uc.nombre AS cliente,
                   tu.telefono AS telefono_cliente,
                   vf.fecha_emision, vf.total,
                   mp.metodo AS metodo_pago, mp.estado
            FROM venta_factura vf
            JOIN Usuario uv ON uv.usuario_id = vf.usuario_id
            JOIN Usuario uc ON uc.usuario_id = vf.usuario_cliente_id
            LEFT JOIN Telefono_Usuario tu ON tu.usuario_id = vf.usuario_cliente_id
            LEFT JOIN Metodo_pago mp ON mp.venta_id = vf.venta_id
            WHERE vf.venta_id = ?
            """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Venta v = mapearVenta(rs);
                    v.setDetalles(listarDetalles(ventaId, con));
                    calcularAnticipoSaldo(v, con);
                    return v;
                }
            }
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════
    // INSERTAR VENTA + DETALLES (transacción completa)
    // modalidad: "contado" | "anticipo"
    // ═══════════════════════════════════════════════════════════
    public int insertar(Venta venta, List<DetalleVenta> detalles,
                        String modalidad, BigDecimal montoAnticipo,
                        BigDecimal saldoPendiente) throws Exception {

        final String sqlVenta     = "INSERT INTO venta_factura (usuario_id, usuario_cliente_id, fecha_emision, total) VALUES (?,?,?,?)";
        final String sqlDetalle   = "INSERT INTO Detalle_Venta (venta_id, producto_id, cantidad, precio_unitario, subtotal) VALUES (?,?,?,?,?)";
        final String sqlMetPago   = "INSERT INTO Metodo_pago (venta_id, monto, metodo, fecha, estado) VALUES (?,?,?,?,?)";
        final String sqlPagoVenta = "INSERT INTO Pago_Venta (venta_id, metodo_pago_id, monto, fecha, estado) VALUES (?,?,?,?,?)";
        final String sqlInventario= "INSERT INTO Inventario_Movimiento (producto_id, tipo, estado, cantidad, fecha, referencia) VALUES (?,?,?,?,?,?)";

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            int ventaId = -1;
            try {
                // 1. Validar stock
                for (DetalleVenta d : detalles) {
                    if (!validarStock(con, d.getProductoId(), d.getCantidad())) {
                        throw new SQLException("Stock insuficiente para: " + d.getProductoNombre());
                    }
                }

                // 2. Insertar cabecera de venta
                try (PreparedStatement ps = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, venta.getUsuarioId());
                    ps.setInt(2, venta.getUsuarioClienteId());
                    ps.setDate(3, new java.sql.Date(venta.getFechaEmision().getTime()));
                    ps.setBigDecimal(4, venta.getTotal());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) ventaId = rs.getInt(1);
                    }
                }
                if (ventaId == -1) throw new SQLException("No se generó el ID de venta");

                // 3. Insertar detalles
                try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                    for (DetalleVenta d : detalles) {
                        ps.setInt(1, ventaId);
                        ps.setInt(2, d.getProductoId());
                        ps.setInt(3, d.getCantidad());
                        ps.setBigDecimal(4, d.getPrecioUnitario());
                        ps.setBigDecimal(5, d.getSubtotal());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // 4. Registrar en Metodo_pago (cabecera de pago)
                int metodoPagoId;
                try (PreparedStatement ps = con.prepareStatement(sqlMetPago, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, ventaId);
                    ps.setBigDecimal(2, venta.getTotal());
                    ps.setString(3, venta.getMetodoPago());
                    ps.setDate(4, new java.sql.Date(venta.getFechaEmision().getTime()));
                    // estado de la cabecera según modalidad
                    ps.setString(5, "anticipo".equals(modalidad) ? "pendiente" : "confirmado");
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("No se generó metodo_pago_id");
                        metodoPagoId = rs.getInt(1);
                    }
                }

                // 5. Registrar en Pago_Venta según modalidad
                try (PreparedStatement ps = con.prepareStatement(sqlPagoVenta)) {
                    if ("anticipo".equals(modalidad) && montoAnticipo != null) {
                        // Primera cuota: anticipo (confirmado)
                        ps.setInt(1, ventaId);
                        ps.setInt(2, metodoPagoId);
                        ps.setBigDecimal(3, montoAnticipo);
                        ps.setTimestamp(4, new Timestamp(venta.getFechaEmision().getTime()));
                        ps.setString(5, "confirmado");
                        ps.executeUpdate();

                        // Segunda cuota: saldo pendiente
                        if (saldoPendiente != null && saldoPendiente.compareTo(BigDecimal.ZERO) > 0) {
                            ps.setInt(1, ventaId);
                            ps.setInt(2, metodoPagoId);
                            ps.setBigDecimal(3, saldoPendiente);
                            ps.setTimestamp(4, new Timestamp(venta.getFechaEmision().getTime()));
                            ps.setString(5, "pendiente");
                            ps.executeUpdate();
                        }
                    } else {
                        // Contado: pago completo confirmado
                        ps.setInt(1, ventaId);
                        ps.setInt(2, metodoPagoId);
                        ps.setBigDecimal(3, venta.getTotal());
                        ps.setTimestamp(4, new Timestamp(venta.getFechaEmision().getTime()));
                        ps.setString(5, "confirmado");
                        ps.executeUpdate();
                    }
                }

                // 6. Actualizar inventario + stock
                try (PreparedStatement ps = con.prepareStatement(sqlInventario)) {
                    for (DetalleVenta d : detalles) {
                        ps.setInt(1, d.getProductoId());
                        ps.setString(2, "salida");
                        ps.setString(3, "activo");
                        ps.setInt(4, d.getCantidad());
                        ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                        ps.setString(6, "VENTA-" + ventaId);
                        ps.addBatch();
                        actualizarStock(con, d.getProductoId(), -d.getCantidad());
                    }
                    ps.executeBatch();
                }

                con.commit();
                return ventaId;

            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ABONAR SALDO PENDIENTE
    // ═══════════════════════════════════════════════════════════
    public boolean abonarSaldo(int ventaId, BigDecimal montoAbono) throws Exception {
        // Actualiza el registro pendiente de Pago_Venta
        final String sqlAbono = """
            UPDATE Pago_Venta
            SET monto = ?, estado = 'confirmado'
            WHERE venta_id = ? AND estado = 'pendiente'
            LIMIT 1
            """;
        final String sqlVerificar = """
            SELECT SUM(CASE WHEN estado='confirmado' THEN monto ELSE 0 END) AS pagado,
                   vf.total
            FROM Pago_Venta pv
            JOIN venta_factura vf ON vf.venta_id = pv.venta_id
            WHERE pv.venta_id = ?
            GROUP BY vf.total
            """;
        final String sqlConfirmarMetodo = "UPDATE Metodo_pago SET estado='confirmado' WHERE venta_id=?";

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlAbono)) {
                    ps.setBigDecimal(1, montoAbono);
                    ps.setInt(2, ventaId);
                    ps.executeUpdate();
                }
                // Si pagado >= total → confirmar Metodo_pago
                try (PreparedStatement ps = con.prepareStatement(sqlVerificar)) {
                    ps.setInt(1, ventaId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            BigDecimal pagado = rs.getBigDecimal("pagado");
                            BigDecimal total  = rs.getBigDecimal("total");
                            if (pagado != null && total != null && pagado.compareTo(total) >= 0) {
                                try (PreparedStatement ps2 = con.prepareStatement(sqlConfirmarMetodo)) {
                                    ps2.setInt(1, ventaId);
                                    ps2.executeUpdate();
                                }
                            }
                        }
                    }
                }
                con.commit();
                return true;
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BUSCAR VENTAS CON FILTROS
    // ═══════════════════════════════════════════════════════════
    public List<Venta> buscarVentas(String criterio, String tipoBusqueda,
                                    Date fechaInicio, Date fechaFin,
                                    int vendedorId) throws Exception {
        List<Venta> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT vf.venta_id, vf.usuario_id, vf.usuario_cliente_id,
                   uv.nombre AS vendedor, uc.nombre AS cliente,
                   tu.telefono AS telefono_cliente,
                   vf.fecha_emision, vf.total,
                   mp.metodo AS metodo_pago, mp.estado
            FROM venta_factura vf
            JOIN Usuario uv ON uv.usuario_id = vf.usuario_id
            JOIN Usuario uc ON uc.usuario_id = vf.usuario_cliente_id
            LEFT JOIN Telefono_Usuario tu ON tu.usuario_id = vf.usuario_cliente_id
            LEFT JOIN Metodo_pago mp ON mp.venta_id = vf.venta_id
            WHERE 1=1
            """);
        List<Object> params = new ArrayList<>();

        if (vendedorId > 0) {
            sql.append(" AND vf.usuario_id = ?");
            params.add(vendedorId);
        }
        if (criterio != null && !criterio.isBlank() && tipoBusqueda != null) {
            switch (tipoBusqueda) {
                case "id"       -> { sql.append(" AND vf.venta_id = ?");       params.add(Integer.parseInt(criterio)); }
                case "cliente"  -> { sql.append(" AND uc.nombre LIKE ?");      params.add("%" + criterio + "%"); }
                case "vendedor" -> { sql.append(" AND uv.nombre LIKE ?");      params.add("%" + criterio + "%"); }
                case "estado"   -> { sql.append(" AND mp.estado = ?");         params.add(criterio); }
            }
        }
        if (fechaInicio != null) {
            sql.append(" AND vf.fecha_emision >= ?");
            params.add(new java.sql.Date(fechaInicio.getTime()));
        }
        if (fechaFin != null) {
            sql.append(" AND vf.fecha_emision <= ?");
            params.add(new java.sql.Date(fechaFin.getTime()));
        }
        sql.append(" ORDER BY vf.fecha_emision DESC");

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Venta v = mapearVenta(rs);
                    v.setDetalles(listarDetalles(v.getVentaId(), con));
                    calcularAnticipoSaldo(v, con);
                    lista.add(v);
                }
            }
        }
        return lista;
    }

    // ═══════════════════════════════════════════════════════════
    // CONTADORES PARA DASHBOARD
    // ═══════════════════════════════════════════════════════════
    public int contarVentas()              throws Exception { return contar("SELECT COUNT(*) FROM venta_factura"); }
    public int contarPendientes()          throws Exception { return contar("SELECT COUNT(*) FROM Metodo_pago WHERE estado='pendiente'"); }
    public int contarVentasPorVendedor(int vendedorId) throws Exception {
        String sql = "SELECT COUNT(*) FROM venta_factura WHERE usuario_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, vendedorId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTODOS PRIVADOS AUXILIARES
    // ═══════════════════════════════════════════════════════════
    private Venta mapearVenta(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setVentaId(rs.getInt("venta_id"));
        v.setUsuarioId(rs.getInt("usuario_id"));
        v.setUsuarioClienteId(rs.getInt("usuario_cliente_id"));
        v.setVendedorNombre(rs.getString("vendedor"));
        v.setClienteNombre(rs.getString("cliente"));
        v.setTelefonoCliente(rs.getString("telefono_cliente"));
        v.setFechaEmision(rs.getDate("fecha_emision"));
        v.setTotal(rs.getBigDecimal("total"));
        v.setMetodoPago(rs.getString("metodo_pago"));
        v.setEstado(rs.getString("estado"));
        return v;
    }

    private List<DetalleVenta> listarDetalles(int ventaId, Connection con) throws SQLException {
        List<DetalleVenta> lista = new ArrayList<>();
        String sql = """
            SELECT dv.detalle_venta_id, dv.venta_id, dv.producto_id,
                   p.nombre AS producto_nombre, p.stock,
                   dv.cantidad, dv.precio_unitario, dv.subtotal
            FROM Detalle_Venta dv
            JOIN Producto p ON p.producto_id = dv.producto_id
            WHERE dv.venta_id = ?
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleVenta d = new DetalleVenta();
                    d.setDetalleVentaId(rs.getInt("detalle_venta_id"));
                    d.setVentaId(rs.getInt("venta_id"));
                    d.setProductoId(rs.getInt("producto_id"));
                    d.setProductoNombre(rs.getString("producto_nombre"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    d.setSubtotal(rs.getBigDecimal("subtotal"));
                    d.setStockDisponible(rs.getInt("stock"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    private void calcularAnticipoSaldo(Venta venta, Connection con) throws SQLException {
        String sql = """
            SELECT
                SUM(CASE WHEN estado='confirmado' THEN monto ELSE 0 END) AS pagado,
                SUM(CASE WHEN estado='pendiente'  THEN monto ELSE 0 END) AS pendiente
            FROM Pago_Venta WHERE venta_id = ?
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, venta.getVentaId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal pendiente = rs.getBigDecimal("pendiente");
                    BigDecimal pagado    = rs.getBigDecimal("pagado");
                    if (pendiente != null && pendiente.compareTo(BigDecimal.ZERO) > 0) {
                        venta.setModalidad("anticipo");
                        venta.setMontoAnticipo(pagado);
                        venta.setSaldoPendiente(pendiente);
                    } else {
                        venta.setModalidad("contado");
                    }
                }
            }
        }
    }

    private boolean validarStock(Connection con, int productoId, int cantidad) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT stock FROM Producto WHERE producto_id=?")) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("stock") >= cantidad;
            }
        }
    }

    private void actualizarStock(Connection con, int productoId, int delta) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE Producto SET stock = stock + ? WHERE producto_id=?")) {
            ps.setInt(1, delta);
            ps.setInt(2, productoId);
            ps.executeUpdate();
        }
    }

    private int contar(String sql) throws Exception {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
