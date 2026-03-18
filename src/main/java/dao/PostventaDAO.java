package dao;

import config.ConexionDB;
import model.CasoPostventa;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostventaDAO {

    private static final String SQL_BASE = """
        SELECT 
            cp.caso_id,
            cp.venta_id,
            cp.tipo,
            cp.cantidad,
            cp.motivo,
            cp.fecha,
            cp.estado,
            v.usuario_id AS vendedor_id,
            uv.nombre AS vendedor_nombre,
            v.cliente_id,
            cl.nombre AS cliente_nombre,
            (SELECT dv2.producto_id FROM Detalle_Venta dv2 WHERE dv2.venta_id = v.venta_id LIMIT 1) AS producto_id,
            (SELECT p2.nombre FROM Detalle_Venta dv2 
             LEFT JOIN Producto p2 ON p2.producto_id = dv2.producto_id
             WHERE dv2.venta_id = v.venta_id LIMIT 1) AS producto_nombre,
            h.observacion AS observacion
        FROM Caso_Postventa cp
        LEFT JOIN Venta v ON v.venta_id = cp.venta_id
        LEFT JOIN Usuario uv ON uv.usuario_id = v.usuario_id
        LEFT JOIN Cliente cl ON cl.cliente_id = v.cliente_id
        LEFT JOIN Historial_Caso_Postventa h
            ON h.historial_id = (
                SELECT MAX(h2.historial_id)
                FROM Historial_Caso_Postventa h2
                WHERE h2.caso_id = cp.caso_id
            )
        """;

    public List<CasoPostventa> listarPorVendedor(int vendedorId) throws Exception {
        List<CasoPostventa> lista = new ArrayList<>();
        String sql = SQL_BASE + " WHERE v.usuario_id = ? ORDER BY cp.fecha DESC, cp.caso_id DESC";
        try (Connection con = ConexionDB.getConnection()) {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, vendedorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCaso(rs));
            }
            ps.close();
        }
        return lista;
    }

    public List<CasoPostventa> listarTodos() throws Exception {
        List<CasoPostventa> lista = new ArrayList<>();
        String sql = SQL_BASE + " ORDER BY cp.fecha DESC, cp.caso_id DESC";
        try (Connection con = ConexionDB.getConnection()) {
            PreparedStatement ps = con.prepareStatement(sql);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCaso(rs));
            }
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error SQL en listarTodos (PostventaDAO): " + e.getMessage());
            throw e;
        }
        return lista;
    }

    // Método con SQL propio para evitar ambigüedad de aliases
    public List<CasoPostventa> listarPorVenta(int ventaId) throws Exception {
        List<CasoPostventa> lista = new ArrayList<>();
        String sql = """
            SELECT 
                cp.caso_id,
                cp.venta_id,
                cp.tipo,
                cp.cantidad,
                cp.motivo,
                cp.fecha,
                cp.estado,
                uv.nombre AS vendedor_nombre,
                cl.nombre AS cliente_nombre,
                (SELECT dv2.producto_id FROM Detalle_Venta dv2 
                 WHERE dv2.venta_id = cp.venta_id LIMIT 1) AS producto_id,
                (SELECT p2.nombre FROM Detalle_Venta dv2 
                 LEFT JOIN Producto p2 ON p2.producto_id = dv2.producto_id
                 WHERE dv2.venta_id = cp.venta_id LIMIT 1) AS producto_nombre,
                h.observacion AS observacion
            FROM Caso_Postventa cp
            LEFT JOIN Venta ven ON ven.venta_id = cp.venta_id
            LEFT JOIN Usuario uv ON uv.usuario_id = ven.usuario_id
            LEFT JOIN Cliente cl ON cl.cliente_id = ven.cliente_id
            LEFT JOIN Historial_Caso_Postventa h
                ON h.historial_id = (
                    SELECT MAX(h2.historial_id)
                    FROM Historial_Caso_Postventa h2
                    WHERE h2.caso_id = cp.caso_id
                )
            WHERE cp.venta_id = ?
            ORDER BY cp.fecha DESC, cp.caso_id DESC
            """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCaso(rs));
            }
        }
        return lista;
    }

    public CasoPostventa obtenerPorId(int casoId) throws Exception {
        String sql = SQL_BASE + " WHERE cp.caso_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, casoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearCaso(rs);
            }
        }
        return null;
    }

    public int registrar(CasoPostventa caso) throws Exception {
        String sql = "INSERT INTO Caso_Postventa(venta_id, tipo, cantidad, motivo, fecha, estado) VALUES(?, ?, ?, ?, NOW(), 'en_proceso')";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, caso.getVentaId());
            ps.setString(2, caso.getTipo());
            ps.setInt(3, caso.getCantidad());
            ps.setString(4, caso.getMotivo() != null ? caso.getMotivo() : "");
            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
            return -1;
        } catch (SQLException e) {
            System.err.println("Error al registrar caso postventa: " + e.getMessage());
            throw e;
        }
    }

    public boolean actualizarEstado(int casoId, String nuevoEstado, String observacion, int usuarioId) throws Exception {
        final String sqlUpdate    = "UPDATE Caso_Postventa SET estado = ? WHERE caso_id = ?";
        final String sqlHistorial = "INSERT INTO Historial_Caso_Postventa(caso_id, estado, observacion, usuario_id, fecha) VALUES(?,?,?,?,NOW())";

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                    ps.setString(1, nuevoEstado);
                    ps.setInt(2, casoId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(sqlHistorial)) {
                    ps.setInt(1, casoId);
                    ps.setString(2, nuevoEstado);
                    ps.setString(3, observacion != null ? observacion : "");
                    ps.setInt(4, usuarioId);
                    ps.executeUpdate();
                }
                CasoPostventa caso = obtenerPorId(casoId);
                if (caso != null && "devolucion".equals(caso.getTipo()) && "aprobado".equals(nuevoEstado)) {
                    new VentaDAO().retornarStockDevolucion(
                        caso.getVentaId(), caso.getProductoId(), caso.getCantidad(), usuarioId
                    );
                }
                con.commit();
                return true;
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    private CasoPostventa mapearCaso(ResultSet rs) throws SQLException {
        CasoPostventa c = new CasoPostventa();
        c.setCasoId(rs.getInt("caso_id"));
        c.setVentaId(rs.getInt("venta_id"));
        c.setTipo(rs.getString("tipo"));
        c.setCantidad(rs.getInt("cantidad"));
        c.setMotivo(rs.getString("motivo"));
        c.setFecha(rs.getDate("fecha"));
        c.setEstado(rs.getString("estado"));
        c.setVendedorNombre(rs.getString("vendedor_nombre"));
        c.setClienteNombre(rs.getString("cliente_nombre"));
        c.setObservacion(rs.getString("observacion"));
        int productoId = 0;
        if (rs.getObject("producto_id") != null) productoId = rs.getInt("producto_id");
        c.setProductoId(productoId);
        c.setProductoNombre(rs.getString("producto_nombre"));
        return c;
    }
}