package dao;

import model.Venta;
import model.DetalleVenta;
import util.Conexion;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO {

    // ─────────────────────────────────────────────
    //  LISTAR TODAS LAS VENTAS (con vendedor, cliente y método de pago)
    // ─────────────────────────────────────────────
    public List<Venta> listarVentas() throws SQLException {
        List<Venta> lista = new ArrayList<>();
        String sql = """
            SELECT vf.venta_id,
                   vf.usuario_id,
                   vf.usuario_cliente_id,
                   uv.nombre  AS vendedor,
                   uc.nombre  AS cliente,
                   vf.fecha_emision,
                   vf.total,
                   mp.metodo  AS metodo_pago,
                   mp.estado
            FROM venta_factura vf
            JOIN Usuario uv ON uv.usuario_id = vf.usuario_id
            JOIN Usuario uc ON uc.usuario_id = vf.usuario_cliente_id
            LEFT JOIN Metodo_pago mp ON mp.venta_id = vf.venta_id
            ORDER BY vf.fecha_emision DESC
        """;

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Venta v = mapearVenta(rs);
                lista.add(v);
            }
        }
        return lista;
    }

    // ─────────────────────────────────────────────
    //  OBTENER UNA VENTA POR ID (con sus detalles)
    // ─────────────────────────────────────────────
    public Venta obtenerPorId(int ventaId) throws SQLException {
        String sql = """
            SELECT vf.venta_id,
                   vf.usuario_id,
                   vf.usuario_cliente_id,
                   uv.nombre  AS vendedor,
                   uc.nombre  AS cliente,
                   vf.fecha_emision,
                   vf.total,
                   mp.metodo  AS metodo_pago,
                   mp.estado
            FROM venta_factura vf
            JOIN Usuario uv ON uv.usuario_id = vf.usuario_id
            JOIN Usuario uc ON uc.usuario_id = vf.usuario_cliente_id
            LEFT JOIN Metodo_pago mp ON mp.venta_id = vf.venta_id
            WHERE vf.venta_id = ?
        """;

        Venta venta = null;
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    venta = mapearVenta(rs);
                    venta.setDetalles(listarDetalles(ventaId, con));
                }
            }
        }
        return venta;
    }

    // ─────────────────────────────────────────────
    //  INSERTAR VENTA + DETALLES (transacción)
    // ─────────────────────────────────────────────
    public boolean insertar(Venta venta) throws SQLException {
        String sqlVenta  = "INSERT INTO venta_factura (usuario_id, usuario_cliente_id, fecha_emision, total) VALUES (?,?,?,?)";
        String sqlDetalle= "INSERT INTO Detalle_Venta (venta_id, producto_id, cantidad, precio_unitario, subtotal) VALUES (?,?,?,?,?)";
        String sqlPago   = "INSERT INTO Metodo_pago (venta_id, monto, metodo, fecha, estado) VALUES (?,?,?,?,?)";

        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                // 1. Insertar cabecera
                int idGenerado;
                try (PreparedStatement ps = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, venta.getUsuarioId());
                    ps.setInt(2, venta.getUsuarioClienteId());
                    ps.setDate(3, new java.sql.Date(venta.getFechaEmision().getTime()));
                    ps.setBigDecimal(4, venta.getTotal());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        idGenerado = rs.getInt(1);
                    }
                }

                // 2. Insertar detalles
                if (venta.getDetalles() != null) {
                    try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                        for (DetalleVenta d : venta.getDetalles()) {
                            ps.setInt(1, idGenerado);
                            ps.setInt(2, d.getProductoId());
                            ps.setInt(3, d.getCantidad());
                            ps.setBigDecimal(4, d.getPrecioUnitario());
                            ps.setBigDecimal(5, d.getSubtotal());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }

                // 3. Insertar método de pago
                try (PreparedStatement ps = con.prepareStatement(sqlPago)) {
                    ps.setInt(1, idGenerado);
                    ps.setBigDecimal(2, venta.getTotal());
                    ps.setString(3, venta.getMetodoPago());
                    ps.setDate(4, new java.sql.Date(venta.getFechaEmision().getTime()));
                    ps.setString(5, venta.getEstado() != null ? venta.getEstado() : "pendiente");
                    ps.executeUpdate();
                }

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ─────────────────────────────────────────────
    //  ACTUALIZAR ESTADO DE PAGO
    // ─────────────────────────────────────────────
    public boolean actualizarEstado(int ventaId, String nuevoEstado) throws SQLException {
        String sql = "UPDATE Metodo_pago SET estado = ? WHERE venta_id = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, ventaId);
            return ps.executeUpdate() > 0;
        }
    }

    // ─────────────────────────────────────────────
    //  ELIMINAR VENTA (y en cascada sus detalles/pagos)
    // ─────────────────────────────────────────────
    public boolean eliminar(int ventaId) throws SQLException {
        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                ejecutar(con, "DELETE FROM Metodo_pago    WHERE venta_id = ?", ventaId);
                ejecutar(con, "DELETE FROM Detalle_Venta  WHERE venta_id = ?", ventaId);
                ejecutar(con, "DELETE FROM venta_factura  WHERE venta_id = ?", ventaId);
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ─────────────────────────────────────────────
    //  CONTADORES PARA EL DASHBOARD
    // ─────────────────────────────────────────────
    public int contarVentas() throws SQLException {
        return contar("SELECT COUNT(*) FROM venta_factura");
    }

    public int contarPendientes() throws SQLException {
        return contar("SELECT COUNT(*) FROM Metodo_pago WHERE estado = 'pendiente'");
    }

    public int contarPorMetodo(String metodo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Metodo_pago WHERE metodo = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, metodo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    // ─────────────────────────────────────────────
    //  MÉTODOS PRIVADOS AUXILIARES
    // ─────────────────────────────────────────────
    private Venta mapearVenta(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setVentaId(rs.getInt("venta_id"));
        v.setUsuarioId(rs.getInt("usuario_id"));
        v.setUsuarioClienteId(rs.getInt("usuario_cliente_id"));
        v.setVendedorNombre(rs.getString("vendedor"));
        v.setClienteNombre(rs.getString("cliente"));
        v.setFechaEmision(rs.getDate("fecha_emision"));
        v.setTotal(rs.getBigDecimal("total"));
        v.setMetodoPago(rs.getString("metodo_pago"));
        v.setEstado(rs.getString("estado"));
        return v;
    }

    private List<DetalleVenta> listarDetalles(int ventaId, Connection con) throws SQLException {
        String sql = """
            SELECT dv.detalle_venta_id, dv.venta_id, dv.producto_id,
                   p.nombre AS producto_nombre,
                   dv.cantidad, dv.precio_unitario, dv.subtotal
            FROM Detalle_Venta dv
            JOIN Producto p ON p.producto_id = dv.producto_id
            WHERE dv.venta_id = ?
        """;
        List<DetalleVenta> lista = new ArrayList<>();
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
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    private int contar(String sql) throws SQLException {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private void ejecutar(Connection con, String sql, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}