package dao;

import config.ConexionDB;
import model.Compra;
import model.DetalleCompra;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompraDAO {

    // ─────────────────────────────────────────────
    //  LISTAR COMPRAS DE UN PROVEEDOR
    // ─────────────────────────────────────────────
    public List<Compra> listarPorProveedor(int proveedorId) throws Exception {
        List<Compra> lista = new ArrayList<>();
        String sql = """
            SELECT c.compra_id,
                   c.usuario_proveedor_id,
                   c.fecha_compra,
                   c.fecha_entrega,
                   c.total
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

    // ─────────────────────────────────────────────
    //  OBTENER UNA COMPRA POR ID
    // ─────────────────────────────────────────────
    public Compra obtenerPorId(int compraId) throws Exception {
        String sql = """
            SELECT c.compra_id,
                   c.usuario_proveedor_id,
                   c.fecha_compra,
                   c.fecha_entrega,
                   c.total
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

    // ─────────────────────────────────────────────
    //  INSERTAR COMPRA + DETALLES (transacción)
    // ─────────────────────────────────────────────
    public boolean insertar(Compra compra) throws Exception {
        String sqlCompra  = "INSERT INTO Compra (usuario_proveedor_id, fecha_compra, fecha_entrega, total) VALUES (?,?,?,?)";
        String sqlDetalle = "INSERT INTO Detalle_Compra (compra_id, producto_id, precio_unitario, cantidad, subtotal) VALUES (?,?,?,?,?)";

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                int idGenerado;
                try (PreparedStatement ps = con.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, compra.getProveedorId());
                    ps.setDate(2, new java.sql.Date(compra.getFechaCompra().getTime()));
                    ps.setDate(3, new java.sql.Date(compra.getFechaEntrega().getTime()));
                    ps.setBigDecimal(4, compra.getTotal());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        idGenerado = rs.getInt(1);
                    }
                }

                if (compra.getDetalles() != null) {
                    try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                        for (DetalleCompra d : compra.getDetalles()) {
                            ps.setInt(1, idGenerado);
                            ps.setInt(2, d.getProductoId());
                            ps.setBigDecimal(3, d.getPrecioUnitario());
                            ps.setInt(4, d.getCantidad());
                            ps.setBigDecimal(5, d.getSubtotal());
                            ps.addBatch();
                        }
                        ps.executeBatch();
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

    
    // ─────────────────────────────────────────────
    //  ELIMINAR COMPRA (detalles primero)
    // ─────────────────────────────────────────────
    public boolean eliminar(int compraId) throws Exception {
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                ejecutar(con, "DELETE FROM Detalle_Compra WHERE compra_id = ?", compraId);
                ejecutar(con, "DELETE FROM Compra          WHERE compra_id = ?", compraId);
                con.commit();
                return true;
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ─────────────────────────────────────────────
    //  CONTADORES DEL DASHBOARD (por proveedor)
    // ─────────────────────────────────────────────
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

    public int contarProductosPorProveedor(int proveedorId) throws Exception {
        String sql = """
            SELECT COALESCE(SUM(dc.cantidad), 0)
            FROM Detalle_Compra dc
            JOIN Compra c ON c.compra_id = dc.compra_id
            WHERE c.usuario_proveedor_id = ?
            """;
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

    // ─────────────────────────────────────────────
    //  AUXILIARES PRIVADOS
    // ─────────────────────────────────────────────
    private Compra mapearCompra(ResultSet rs) throws SQLException {
        Compra c = new Compra();
        c.setCompraId(rs.getInt("compra_id"));
        c.setProveedorId(rs.getInt("usuario_proveedor_id"));
        c.setFechaCompra(rs.getDate("fecha_compra"));
        c.setFechaEntrega(rs.getDate("fecha_entrega"));
        c.setTotal(rs.getBigDecimal("total"));
        return c;
    }

    private List<DetalleCompra> listarDetalles(int compraId, Connection con) throws SQLException {
        String sql = """
            SELECT dc.detalle_compra_id, dc.compra_id, dc.producto_id,
                   p.nombre AS producto_nombre,
                   dc.precio_unitario, dc.cantidad, dc.subtotal
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
                    d.setCompraId(rs.getInt("detalle_compra_id"));
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