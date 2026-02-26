package dao;

import config.ConexionDB;
import model.CasoPostventa;
import model.EstadoCasoCliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para el módulo de Postventa (cliente/vendedor).
 * Mapea: Caso_Postventa_Cliente + Estado_Caso_Cliente
 */
public class PostventaDAO {

    // ═══════════════════════════════════════════════════════════
    // LISTAR TODOS LOS CASOS (Administrador)
    // ═══════════════════════════════════════════════════════════
    public List<CasoPostventa> listarTodos() throws Exception {
        return ejecutarListado("""
            SELECT cp.caso_id, cp.venta_id, cp.tipo, cp.cantidad, cp.motivo, cp.fecha, cp.estado,
                   uc.nombre AS cliente, uv.nombre AS vendedor,
                   (SELECT p.nombre FROM Detalle_Venta dv
                    JOIN Producto p ON p.producto_id = dv.producto_id
                    WHERE dv.venta_id = cp.venta_id LIMIT 1) AS producto
            FROM Caso_Postventa_Cliente cp
            JOIN venta_factura vf ON vf.venta_id = cp.venta_id
            JOIN Usuario uc ON uc.usuario_id = vf.usuario_cliente_id
            JOIN Usuario uv ON uv.usuario_id = vf.usuario_id
            ORDER BY cp.fecha DESC
            """, null);
    }

    // ═══════════════════════════════════════════════════════════
    // LISTAR CASOS POR VENDEDOR
    // ═══════════════════════════════════════════════════════════
    public List<CasoPostventa> listarPorVendedor(int vendedorId) throws Exception {
        return ejecutarListado("""
            SELECT cp.caso_id, cp.venta_id, cp.tipo, cp.cantidad, cp.motivo, cp.fecha, cp.estado,
                   uc.nombre AS cliente, uv.nombre AS vendedor,
                   (SELECT p.nombre FROM Detalle_Venta dv
                    JOIN Producto p ON p.producto_id = dv.producto_id
                    WHERE dv.venta_id = cp.venta_id LIMIT 1) AS producto
            FROM Caso_Postventa_Cliente cp
            JOIN venta_factura vf ON vf.venta_id = cp.venta_id
            JOIN Usuario uc ON uc.usuario_id = vf.usuario_cliente_id
            JOIN Usuario uv ON uv.usuario_id = vf.usuario_id
            WHERE vf.usuario_id = ?
            ORDER BY cp.fecha DESC
            """, vendedorId);
    }

    // ═══════════════════════════════════════════════════════════
    // OBTENER CASO POR ID
    // ═══════════════════════════════════════════════════════════
    public CasoPostventa obtenerPorId(int casoId) throws Exception {
        String sql = """
            SELECT cp.caso_id, cp.venta_id, cp.tipo, cp.cantidad, cp.motivo, cp.fecha, cp.estado,
                   uc.nombre AS cliente, uv.nombre AS vendedor,
                   (SELECT p.nombre FROM Detalle_Venta dv
                    JOIN Producto p ON p.producto_id = dv.producto_id
                    WHERE dv.venta_id = cp.venta_id LIMIT 1) AS producto
            FROM Caso_Postventa_Cliente cp
            JOIN venta_factura vf ON vf.venta_id = cp.venta_id
            JOIN Usuario uc ON uc.usuario_id = vf.usuario_cliente_id
            JOIN Usuario uv ON uv.usuario_id = vf.usuario_id
            WHERE cp.caso_id = ?
            """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, casoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CasoPostventa caso = mapearCaso(rs);
                    caso.setHistorialEstados(listarHistorial(casoId, con));
                    return caso;
                }
            }
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════
    // REGISTRAR NUEVO CASO POSTVENTA
    // ═══════════════════════════════════════════════════════════
    public int registrar(CasoPostventa caso) throws Exception {
        final String sqlCaso = """
            INSERT INTO Caso_Postventa_Cliente (venta_id, tipo, cantidad, motivo, fecha, estado)
            VALUES (?, ?, ?, ?, ?, 'en_proceso')
            """;
        final String sqlEstado = """
            INSERT INTO Estado_Caso_Cliente (caso_id, estado, fecha, observacion)
            VALUES (?, 'en_proceso', ?, 'Caso registrado')
            """;
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                int casoId;
                try (PreparedStatement ps = con.prepareStatement(sqlCaso, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, caso.getVentaId());
                    ps.setString(2, caso.getTipo());
                    ps.setInt(3, caso.getCantidad());
                    ps.setString(4, caso.getMotivo());
                    ps.setDate(5, new java.sql.Date(caso.getFecha().getTime()));
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("No se generó caso_id");
                        casoId = rs.getInt(1);
                    }
                }
                // Registrar estado inicial
                try (PreparedStatement ps = con.prepareStatement(sqlEstado)) {
                    ps.setInt(1, casoId);
                    ps.setTimestamp(2, new Timestamp(caso.getFecha().getTime()));
                    ps.executeUpdate();
                }
                con.commit();
                return casoId;
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ACTUALIZAR ESTADO DE UN CASO (Administrador)
    // ═══════════════════════════════════════════════════════════
    public boolean actualizarEstado(int casoId, String nuevoEstado, String observacion) throws Exception {
        final String sqlCaso   = "UPDATE Caso_Postventa_Cliente SET estado = ? WHERE caso_id = ?";
        final String sqlHistorial = "INSERT INTO Estado_Caso_Cliente (caso_id, estado, fecha, observacion) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlCaso)) {
                    ps.setString(1, nuevoEstado);
                    ps.setInt(2, casoId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(sqlHistorial)) {
                    ps.setInt(1, casoId);
                    ps.setString(2, nuevoEstado);
                    ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                    ps.setString(4, observacion != null ? observacion : "");
                    ps.executeUpdate();
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
    // PRIVADOS AUXILIARES
    // ═══════════════════════════════════════════════════════════
    private List<CasoPostventa> ejecutarListado(String sql, Integer vendedorId) throws Exception {
        List<CasoPostventa> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (vendedorId != null) ps.setInt(1, vendedorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CasoPostventa c = mapearCaso(rs);
                    c.setHistorialEstados(listarHistorial(c.getCasoId(), con));
                    lista.add(c);
                }
            }
        }
        return lista;
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
        c.setClienteNombre(rs.getString("cliente"));
        c.setVendedorNombre(rs.getString("vendedor"));
        c.setProductoNombre(rs.getString("producto"));
        return c;
    }

    private List<EstadoCasoCliente> listarHistorial(int casoId, Connection con) throws SQLException {
        List<EstadoCasoCliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM Estado_Caso_Cliente WHERE caso_id = ? ORDER BY fecha ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, casoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EstadoCasoCliente e = new EstadoCasoCliente();
                    e.setEstadoId(rs.getInt("estado_id"));
                    e.setCasoId(rs.getInt("caso_id"));
                    e.setEstado(rs.getString("estado"));
                    e.setFecha(rs.getTimestamp("fecha"));
                    e.setObservacion(rs.getString("observacion"));
                    lista.add(e);
                }
            }
        }
        return lista;
    }
}
