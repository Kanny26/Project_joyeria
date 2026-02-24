package dao;
import config.ConexionDB;
import model.Compra;
import model.DetalleCompra;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompraDAO {

    // ═══════════════════════════════════════════════════════════════
    //  MÉTODO PRINCIPAL: INSERTAR COMPRA CON TRANSACCIÓN COMPLETA
    // ═══════════════════════════════════════════════════════════════
    public boolean insertarConTransaccion(Compra compra) throws Exception {
        Connection con = null;
        try {
            con = ConexionDB.getConnection();
            con.setAutoCommit(false);
            int compraId = insertarCompra(con, compra);
            compra.setCompraId(compraId);
            if (compra.getDetalles() != null && !compra.getDetalles().isEmpty()) {
                for (DetalleCompra detalle : compra.getDetalles()) {
                    insertarDetalle(con, compraId, detalle);
                    actualizarStockProducto(con, detalle.getProductoId(), detalle.getCantidad(), "entrada");
                    registrarMovimientoInventario(con, detalle.getProductoId(), detalle.getCantidad(), "entrada", compraId);
                }
            }
            con.commit();
            return true;
        } catch (Exception e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  LISTAR COMPRAS DE UN PROVEEDOR
    // ═══════════════════════════════════════════════════════════════
    public List<Compra> listarPorProveedor(int proveedorId) throws Exception {
        List<Compra> lista = new ArrayList<>();
        String sql = """
            SELECT c.compra_id, c.usuario_proveedor_id, c.fecha_compra,
                   c.fecha_entrega, c.total
            FROM Compra c
            WHERE c.usuario_proveedor_id = ?
            ORDER BY c.fecha_compra DESC
            """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, proveedorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Compra c = mapearCompra(rs);
                    c.setDetalles(listarDetalles(c.getCompraId(), con));
                    lista.add(c);
                }
            }
        }
        return lista;
    }

    // ═══════════════════════════════════════════════════════════════
    //  OBTENER COMPRA POR ID
    // ═══════════════════════════════════════════════════════════════
    public Compra obtenerPorId(int compraId) throws Exception {
        String sql = """
            SELECT c.compra_id, c.usuario_proveedor_id, c.fecha_compra,
                   c.fecha_entrega, c.total
            FROM Compra c
            WHERE c.compra_id = ?
            """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, compraId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Compra c = mapearCompra(rs);
                    c.setDetalles(listarDetalles(compraId, con));
                    return c;
                }
            }
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════
    //  ELIMINAR COMPRA CON TRANSACCIÓN (REVERTIR STOCK)
    // ═══════════════════════════════════════════════════════════════
    public boolean eliminarConTransaccion(int compraId) throws Exception {
        Connection con = null;
        try {
            con = ConexionDB.getConnection();
            con.setAutoCommit(false);
            List<DetalleCompra> detalles = listarDetalles(compraId, con);
            ejecutar(con, "DELETE FROM Detalle_Compra WHERE compra_id = ?", compraId);
            for (DetalleCompra d : detalles) {
                actualizarStockProducto(con, d.getProductoId(), d.getCantidad(), "salida");
                marcarMovimientoInactivo(con, compraId, d.getProductoId());
            }
            ejecutar(con, "DELETE FROM Compra WHERE compra_id = ?", compraId);
            con.commit();
            return true;
        } catch (Exception e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  CONTADORES PARA DASHBOARD
    // ═══════════════════════════════════════════════════════════════
    public int contarComprasPorProveedor(int proveedorId) throws Exception {
        String sql = "SELECT COUNT(*) FROM Compra WHERE usuario_proveedor_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, proveedorId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public java.math.BigDecimal totalGastadoPorProveedor(int proveedorId) throws Exception {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM Compra WHERE usuario_proveedor_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, proveedorId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  MÉTODOS PRIVADOS
    // ═══════════════════════════════════════════════════════════════
    private int insertarCompra(Connection con, Compra compra) throws SQLException {
        String sql = """
            INSERT INTO Compra (usuario_proveedor_id, fecha_compra, fecha_entrega, total)
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, compra.getProveedorId());
            ps.setDate(2, new java.sql.Date(compra.getFechaCompra().getTime()));
            ps.setDate(3, new java.sql.Date(compra.getFechaEntrega().getTime()));
            ps.setBigDecimal(4, compra.getTotal());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            throw new SQLException("No se pudo obtener el ID de la compra generada");
        }
    }

    private void insertarDetalle(Connection con, int compraId, DetalleCompra detalle) throws SQLException {
        String sql = """
            INSERT INTO Detalle_Compra (compra_id, producto_id, precio_unitario, cantidad, subtotal)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, compraId);
            ps.setInt(2, detalle.getProductoId());
            ps.setBigDecimal(3, detalle.getPrecioUnitario());
            ps.setInt(4, detalle.getCantidad());
            ps.setBigDecimal(5, detalle.getSubtotal());
            ps.executeUpdate();
        }
    }

    private void actualizarStockProducto(Connection con, int productoId, int cantidad, String tipo) throws SQLException {
        String sql = "UPDATE Producto SET stock = stock + ? WHERE producto_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int ajuste = "entrada".equals(tipo) ? cantidad : -cantidad;
            ps.setInt(1, ajuste);
            ps.setInt(2, productoId);
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("Producto no encontrado para actualizar stock: ID " + productoId);
            }
        }
    }

    private void registrarMovimientoInventario(Connection con, int productoId, int cantidad,
                                                String tipo, int referenciaId) throws SQLException {
        String sql = """
            INSERT INTO Inventario_Movimiento (producto_id, tipo, estado, cantidad, fecha, referencia)
            VALUES (?, ?, 'activo', ?, NOW(), ?)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            ps.setString(2, tipo);
            ps.setInt(3, cantidad);
            ps.setString(4, "COMPRA-" + referenciaId);
            ps.executeUpdate();
        }
    }

    private void marcarMovimientoInactivo(Connection con, int compraId, int productoId) throws SQLException {
        String sql = """
            UPDATE Inventario_Movimiento
            SET estado = 'inactivo'
            WHERE producto_id = ? AND referencia = ?
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            ps.setString(2, "COMPRA-" + compraId);
            ps.executeUpdate();
        }
    }

    private Compra mapearCompra(ResultSet rs) throws SQLException {
        Compra c = new Compra();
        c.setCompraId(rs.getInt("compra_id"));
        c.setProveedorId(rs.getInt("usuario_proveedor_id"));
        c.setFechaCompra(rs.getDate("fecha_compra"));
        c.setFechaEntrega(rs.getDate("fecha_entrega"));
        c.setTotal(rs.getBigDecimal("total"));
        return c;
    }

    // ═══════════════════════════════════════════════════════════════
    //  CORRECCIÓN PRINCIPAL: LEFT JOIN + COALESCE para evitar NPE
    // ═══════════════════════════════════════════════════════════════
    private List<DetalleCompra> listarDetalles(int compraId, Connection con) throws SQLException {
        String sql = """
            SELECT dc.detalle_compra_id, dc.compra_id, dc.producto_id,
                   COALESCE(p.nombre, 'Producto eliminado') AS producto_nombre,
                   dc.precio_unitario, dc.cantidad, dc.subtotal
            FROM Detalle_Compra dc
            LEFT JOIN Producto p ON p.producto_id = dc.producto_id
            WHERE dc.compra_id = ?
            """;
        List<DetalleCompra> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, compraId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleCompra d = new DetalleCompra();
                    d.setDetalleCompraId(rs.getInt("detalle_compra_id"));
                    d.setCompraId(rs.getInt("compra_id"));
                    d.setProductoId(rs.getInt("producto_id"));
                    d.setProductoNombre(rs.getString("producto_nombre"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setSubtotal(rs.getBigDecimal("subtotal"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    private void ejecutar(Connection con, String sql, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}