package dao;

import model.Venta;
import model.DetalleVenta;
import config.ConexionDB;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO {

    /* ═══════════════════════════════════════════════
       LISTAR TODAS LAS VENTAS (Admin) - RF10
    ═══════════════════════════════════════════════ */
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

    /* ═══════════════════════════════════════════════
       LISTAR VENTAS POR VENDEDOR - RF20
    ═══════════════════════════════════════════════ */
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

    /* ═══════════════════════════════════════════════
       OBTENER VENTA POR ID - RF11
    ═══════════════════════════════════════════════ */
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
                    Venta venta = mapearVenta(rs);
                    venta.setDetalles(listarDetalles(ventaId, con));
                    calcularAnticipoSaldo(venta, con);
                    return venta;
                }
            }
        }
        return null;
    }

    /* ═══════════════════════════════════════════════
       INSERTAR VENTA + DETALLES (transacción) - RF19+RF25
    ═══════════════════════════════════════════════ */
    public int insertar(Venta venta, List<DetalleVenta> detalles, String modalidad, 
                        BigDecimal montoAnticipo, BigDecimal saldoPendiente) throws Exception {
        
        String sqlVenta = "INSERT INTO venta_factura (usuario_id, usuario_cliente_id, fecha_emision, total) VALUES (?,?,?,?)";
        String sqlDetalle = "INSERT INTO Detalle_Venta (venta_id, producto_id, cantidad, precio_unitario, subtotal) VALUES (?,?,?,?,?)";
        String sqlMetodoPago = "INSERT INTO Metodo_pago (venta_id, monto, metodo, fecha, estado) VALUES (?,?,?,?,?)";
        String sqlPagoVenta = "INSERT INTO Pago_Venta (venta_id, metodo_pago_id, monto, fecha, estado) VALUES (?,?,?,?,?)";
        String sqlInventario = "INSERT INTO Inventario_Movimiento (producto_id, tipo, estado, cantidad, fecha, referencia) VALUES (?,?,?,?,?,?)";

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            int ventaIdGenerado = -1;

            try {
                // 1. Validar stock antes de insertar (RF25)
                for (DetalleVenta d : detalles) {
                    if (!validarStock(con, d.getProductoId(), d.getCantidad())) {
                        throw new SQLException("Stock insuficiente para producto ID: " + d.getProductoId());
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
                        if (rs.next()) ventaIdGenerado = rs.getInt(1);
                    }
                }

                if (ventaIdGenerado == -1) throw new SQLException("Error al generar ID de venta");

                // 3. Insertar detalles de venta
                try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                    for (DetalleVenta d : detalles) {
                        ps.setInt(1, ventaIdGenerado);
                        ps.setInt(2, d.getProductoId());
                        ps.setInt(3, d.getCantidad());
                        ps.setBigDecimal(4, d.getPrecioUnitario());
                        ps.setBigDecimal(5, d.getSubtotal());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // 4. Registrar método de pago principal
                try (PreparedStatement ps = con.prepareStatement(sqlMetodoPago)) {
                    ps.setInt(1, ventaIdGenerado);
                    ps.setBigDecimal(2, venta.getTotal());
                    ps.setString(3, venta.getMetodoPago());
                    ps.setDate(4, new java.sql.Date(venta.getFechaEmision().getTime()));
                    ps.setString(5, "pendiente");
                    ps.executeUpdate();
                }

                // 5. Gestionar pagos (anticipo o contado) - RF35
                int metodoPagoId = obtenerMetodoPagoId(con, venta.getMetodoPago());
                
                if ("anticipo".equals(modalidad) && montoAnticipo != null) {
                    // Registro del anticipo (confirmado)
                    try (PreparedStatement ps = con.prepareStatement(sqlPagoVenta)) {
                        ps.setInt(1, ventaIdGenerado);
                        ps.setInt(2, metodoPagoId);
                        ps.setBigDecimal(3, montoAnticipo);
                        ps.setTimestamp(4, new Timestamp(venta.getFechaEmision().getTime()));
                        ps.setString(5, "confirmado");
                        ps.executeUpdate();
                    }

                    // Registro del saldo pendiente
                    if (saldoPendiente != null && saldoPendiente.compareTo(BigDecimal.ZERO) > 0) {
                        try (PreparedStatement ps = con.prepareStatement(sqlPagoVenta)) {
                            ps.setInt(1, ventaIdGenerado);
                            ps.setInt(2, metodoPagoId);
                            ps.setBigDecimal(3, saldoPendiente);
                            ps.setTimestamp(4, new Timestamp(venta.getFechaEmision().getTime()));
                            ps.setString(5, "pendiente");
                            ps.executeUpdate();
                        }
                    }
                } else {
                    // Pago completo (confirmado)
                    try (PreparedStatement ps = con.prepareStatement(sqlPagoVenta)) {
                        ps.setInt(1, ventaIdGenerado);
                        ps.setInt(2, metodoPagoId);
                        ps.setBigDecimal(3, venta.getTotal());
                        ps.setTimestamp(4, new Timestamp(venta.getFechaEmision().getTime()));
                        ps.setString(5, "confirmado");
                        ps.executeUpdate();
                    }
                }

                // 6. Actualizar inventario (RF25) - descuento de stock
                try (PreparedStatement ps = con.prepareStatement(sqlInventario)) {
                    for (DetalleVenta d : detalles) {
                        ps.setInt(1, d.getProductoId());
                        ps.setString(2, "salida");
                        ps.setString(3, "activo");
                        ps.setInt(4, d.getCantidad());
                        ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                        ps.setString(6, "VENTA-" + ventaIdGenerado);
                        ps.addBatch();

                        // Actualizar stock en Producto
                        actualizarStock(con, d.getProductoId(), -d.getCantidad());
                    }
                    ps.executeBatch();
                }

                con.commit();
                return ventaIdGenerado;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /* ═══════════════════════════════════════════════
       ACTUALIZAR ESTADO DE PAGO - RF35
    ═══════════════════════════════════════════════ */
    public boolean actualizarPagoVenta(int ventaId, BigDecimal montoAbono) throws Exception {
        String sqlUpdateSaldo = "UPDATE Pago_Venta SET estado = 'confirmado', monto = monto + ? WHERE venta_id = ? AND estado = 'pendiente'";
        String sqlVerificarPagado = "SELECT SUM(CASE WHEN estado = 'confirmado' THEN monto ELSE 0 END) as pagado, vf.total FROM Pago_Venta pv JOIN venta_factura vf ON vf.venta_id = pv.venta_id WHERE pv.venta_id = ? GROUP BY vf.total";
        String sqlUpdateMetodoPago = "UPDATE Metodo_pago SET estado = 'confirmado' WHERE venta_id = ?";

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);

            try {
                // Actualizar pago pendiente
                try (PreparedStatement ps = con.prepareStatement(sqlUpdateSaldo)) {
                    ps.setBigDecimal(1, montoAbono);
                    ps.setInt(2, ventaId);
                    ps.executeUpdate();
                }

                // Verificar si está completamente pagado
                try (PreparedStatement ps = con.prepareStatement(sqlVerificarPagado)) {
                    ps.setInt(1, ventaId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            BigDecimal pagado = rs.getBigDecimal("pagado");
                            BigDecimal total = rs.getBigDecimal("total");
                            if (pagado != null && total != null && pagado.compareTo(total) >= 0) {
                                try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdateMetodoPago)) {
                                    psUpdate.setInt(1, ventaId);
                                    psUpdate.executeUpdate();
                                }
                            }
                        }
                    }
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /* ═══════════════════════════════════════════════
       BUSCAR VENTAS CON FILTROS - RF23
    ═══════════════════════════════════════════════ */
    public List<Venta> buscarVentas(String criterio, String tipoBusqueda, 
                                   Date fechaInicio, Date fechaFin, int vendedorId) throws Exception {
        List<Venta> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT vf.venta_id, vf.usuario_id, vf.usuario_cliente_id,
                   uv.nombre AS vendedor, uc.nombre AS cliente,
                   vf.fecha_emision, vf.total, mp.metodo, mp.estado
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

        if (criterio != null && !criterio.isEmpty() && tipoBusqueda != null) {
            switch (tipoBusqueda) {
                case "numero_venta":
                    sql.append(" AND vf.venta_id = ?");
                    params.add(Integer.parseInt(criterio));
                    break;
                case "cliente":
                    sql.append(" AND uc.nombre LIKE ?");
                    params.add("%" + criterio + "%");
                    break;
                case "vendedor":
                    sql.append(" AND uv.nombre LIKE ?");
                    params.add("%" + criterio + "%");
                    break;
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

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

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

    /* ═══════════════════════════════════════════════
       CONTADORES PARA DASHBOARD
    ═══════════════════════════════════════════════ */
    public int contarVentas() throws Exception {
        return contar("SELECT COUNT(*) FROM venta_factura");
    }

    public int contarVentasPorVendedor(int vendedorId) throws Exception {
        String sql = "SELECT COUNT(*) FROM venta_factura WHERE usuario_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, vendedorId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int contarPendientes() throws Exception {
        return contar("SELECT COUNT(*) FROM Metodo_pago WHERE estado = 'pendiente'");
    }

    public int contarPorMetodo(String metodo) throws Exception {
        String sql = "SELECT COUNT(*) FROM Metodo_pago WHERE metodo = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, metodo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /* ═══════════════════════════════════════════════
       MÉTODOS PRIVADOS AUXILIARES
    ═══════════════════════════════════════════════ */
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
                SUM(CASE WHEN estado = 'confirmado' THEN monto ELSE 0 END) as pagado,
                SUM(CASE WHEN estado = 'pendiente' THEN monto ELSE 0 END) as pendiente
            FROM Pago_Venta WHERE venta_id = ?
            """;
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, venta.getVentaId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal pagado = rs.getBigDecimal("pagado");
                    BigDecimal pendiente = rs.getBigDecimal("pendiente");
                    
                    if (pagado != null && pendiente != null && pendiente.compareTo(BigDecimal.ZERO) > 0) {
                        venta.setModalidad("anticipo");
                        venta.setMontoAnticipo(pagado);
                        venta.setSaldoPendiente(pendiente);
                    } else {
                        venta.setModalidad("contado");
                        venta.setMontoAnticipo(null);
                        venta.setSaldoPendiente(null);
                    }
                }
            }
        }
    }

    private boolean validarStock(Connection con, int productoId, int cantidad) throws SQLException {
        String sql = "SELECT stock FROM Producto WHERE producto_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("stock") >= cantidad;
                }
            }
        }
        return false;
    }

    private void actualizarStock(Connection con, int productoId, int cantidad) throws SQLException {
        String sql = "UPDATE Producto SET stock = stock + ? WHERE producto_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cantidad); // cantidad negativa para restar
            ps.setInt(2, productoId);
            ps.executeUpdate();
        }
    }

    private int obtenerMetodoPagoId(Connection con, String metodo) throws SQLException {
        String sql = "SELECT metodo_pago_id FROM Metodo_pago WHERE metodo = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, metodo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("metodo_pago_id");
                }
            }
        }
        return 1; // Default
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