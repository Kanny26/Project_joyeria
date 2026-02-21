package dao;

import model.CasoPostVenta;
import config.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CasoPostVentaDAO {

    // ─────────────────────────────────────────────
    //  LISTAR TODOS LOS CASOS
    // ─────────────────────────────────────────────
    public List<CasoPostVenta> listarCasos() throws Exception {
        List<CasoPostVenta> lista = new ArrayList<>();
        String sql = """
            SELECT cp.caso_id, cp.venta_id, cp.tipo, cp.cantidad,
                   cp.motivo, cp.fecha, cp.estado,
                   uv.nombre AS vendedor,
                   uc.nombre AS cliente
            FROM Caso_Postventa_Cliente cp
            JOIN venta_factura vf ON vf.venta_id = cp.venta_id
            JOIN Usuario uv ON uv.usuario_id = vf.usuario_id
            JOIN Usuario uc ON uc.usuario_id = vf.usuario_cliente_id
            ORDER BY cp.fecha DESC
        """;

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearCaso(rs));
            }
        }
        return lista;
    }

    // ─────────────────────────────────────────────
    //  OBTENER UN CASO POR ID
    // ─────────────────────────────────────────────
    public CasoPostVenta obtenerPorId(int casoId) throws Exception {
        String sql = """
            SELECT cp.caso_id, cp.venta_id, cp.tipo, cp.cantidad,
                   cp.motivo, cp.fecha, cp.estado,
                   uv.nombre AS vendedor,
                   uc.nombre AS cliente,
                   ec.observacion
            FROM Caso_Postventa_Cliente cp
            JOIN venta_factura vf ON vf.venta_id = cp.venta_id
            JOIN Usuario uv ON uv.usuario_id = vf.usuario_id
            JOIN Usuario uc ON uc.usuario_id = vf.usuario_cliente_id
            LEFT JOIN Estado_Caso_Cliente ec ON ec.caso_id = cp.caso_id
            WHERE cp.caso_id = ?
            ORDER BY ec.fecha DESC
            LIMIT 1
        """;

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, casoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CasoPostVenta caso = mapearCaso(rs);
                    caso.setObservacion(rs.getString("observacion"));
                    return caso;
                }
            }
        }
        return null;
    }

    // ─────────────────────────────────────────────
    //  ACTUALIZAR ESTADO DEL CASO
    // ─────────────────────────────────────────────
    public boolean actualizarEstado(int casoId, String nuevoEstado, String observacion) throws Exception {
        String sqlCaso   = "UPDATE Caso_Postventa_Cliente SET estado = ? WHERE caso_id = ?";
        String sqlEstado = "INSERT INTO Estado_Caso_Cliente (caso_id, estado, fecha, observacion) VALUES (?,?,NOW(),?)";

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlCaso)) {
                    ps.setString(1, nuevoEstado);
                    ps.setInt(2, casoId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(sqlEstado)) {
                    ps.setInt(1, casoId);
                    ps.setString(2, nuevoEstado);
                    ps.setString(3, observacion);
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
    //  CONTADORES
    // ─────────────────────────────────────────────
    public int contarCasos() throws Exception {
        return contar("SELECT COUNT(*) FROM Caso_Postventa_Cliente");
    }

    public int contarPendientes() throws Exception {
        return contar("SELECT COUNT(*) FROM Caso_Postventa_Cliente WHERE estado = 'en_proceso'");
    }

    // ─────────────────────────────────────────────
    //  AUXILIARES
    // ─────────────────────────────────────────────
    private CasoPostVenta mapearCaso(ResultSet rs) throws SQLException {
        CasoPostVenta c = new CasoPostVenta();
        c.setCasoId(rs.getInt("caso_id"));
        c.setVentaId(rs.getInt("venta_id"));
        c.setTipo(rs.getString("tipo"));
        c.setCantidad(rs.getInt("cantidad"));
        c.setMotivo(rs.getString("motivo"));
        c.setFecha(rs.getDate("fecha"));
        c.setEstado(rs.getString("estado"));
        c.setVendedorNombre(rs.getString("vendedor"));
        c.setClienteNombre(rs.getString("cliente"));
        return c;
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