package dao;
import config.ConexionDB;
import model.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.*;

public class UsuarioDAO {

    public boolean agregarUsuario(Usuario usuario) {
        String sqlUsuario = """
            INSERT INTO Usuario (nombre, pass, estado, fecha_creacion, documento, fecha_registro)
            VALUES (?, ?, ?, NOW(), ?, ?)
            """;
        String sqlRol      = "INSERT INTO Rol (cargo, usuario_id) VALUES (?, ?)";
        String sqlTelefono = "INSERT INTO Telefono_Usuario (telefono, usuario_id) VALUES (?, ?)";
        String sqlCorreo   = "INSERT INTO Correo_Usuario (email, usuario_id) VALUES (?, ?)";

        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false);
            int usuarioId;

            try (PreparedStatement ps = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, usuario.getNombre());
                ps.setString(2, BCrypt.hashpw(usuario.getContrasena(), BCrypt.gensalt()));
                ps.setBoolean(3, usuario.isEstado());
                ps.setString(4, usuario.getDocumento());
                if (usuario.getFechaRegistro() != null)
                    ps.setDate(5, new java.sql.Date(usuario.getFechaRegistro().getTime()));
                else
                    ps.setNull(5, Types.DATE);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                usuarioId = rs.getInt(1);
            }

            try (PreparedStatement psRol = conn.prepareStatement(sqlRol)) {
                    psRol.setString(1, "vendedor");
                    psRol.setInt(2, usuarioId);
                    psRol.executeUpdate();
                }

            if (usuario.getTelefono() != null) {
                try (PreparedStatement psTel = conn.prepareStatement(sqlTelefono)) {
                    for (String tel : usuario.getTelefono().split(",")) {
                        psTel.setString(1, tel.trim());
                        psTel.setInt(2, usuarioId);
                        psTel.executeUpdate();
                    }
                }
            }

            if (usuario.getCorreo() != null) {
                try (PreparedStatement psCorreo = conn.prepareStatement(sqlCorreo)) {
                    psCorreo.setString(1, usuario.getCorreo());
                    psCorreo.setInt(2, usuarioId);
                    psCorreo.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) { // ✅
            e.printStackTrace();
            return false;
        }
    }

    public Usuario validar(String nombre, String password) {
        String sql = """
            SELECT u.usuario_id, u.nombre, u.pass, u.estado, r.cargo
            FROM Usuario u
            INNER JOIN Rol r ON u.usuario_id = r.usuario_id
            WHERE u.nombre = ?
              AND u.estado = 1
              AND r.cargo = 'vendedor'
            """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String passBD = rs.getString("pass");
                if (BCrypt.checkpw(password, passBD)) {
                    Usuario u = new Usuario();
                    u.setUsuarioId(rs.getInt("usuario_id"));
                    u.setNombre(rs.getString("nombre"));
                    u.setRol(rs.getString("cargo"));
                    u.setEstado(rs.getBoolean("estado"));
                    return u;
                }
            }
        } catch (Exception e) { // ✅
            e.printStackTrace();
        }
        return null;
    }

    public List<Usuario> listarUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        String sql = """
            SELECT u.usuario_id, u.nombre, u.estado, u.documento, u.fecha_creacion,
                   r.cargo,
                   GROUP_CONCAT(DISTINCT t.telefono) AS telefonos,
                   GROUP_CONCAT(DISTINCT c.email) AS correos
            FROM Usuario u
            LEFT JOIN Rol r ON u.usuario_id = r.usuario_id
            LEFT JOIN Telefono_Usuario t ON u.usuario_id = t.usuario_id
            LEFT JOIN Correo_Usuario c ON u.usuario_id = c.usuario_id
            GROUP BY u.usuario_id, u.nombre, u.estado, u.documento, u.fecha_creacion, r.cargo
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setUsuarioId(rs.getInt("usuario_id"));
                u.setNombre(rs.getString("nombre"));
                u.setEstado(rs.getBoolean("estado"));
                u.setDocumento(rs.getString("documento"));
                u.setFechaCreacion(rs.getDate("fecha_creacion"));
                u.setRol(rs.getString("cargo"));
                u.setTelefono(rs.getString("telefonos"));
                u.setCorreo(rs.getString("correos"));
                lista.add(u);
            }
        } catch (Exception e) { // ✅
            e.printStackTrace();
        }
        return lista;
    }

    public List<Map<String, Object>> obtenerHistorialUsuariosConDesempeno() {
        List<Map<String, Object>> historial = new ArrayList<>();
        String sql = """
            SELECT u.usuario_id, u.nombre,
                   d.ventas_totales, d.comision_porcentaje,
                   d.comision_ganada, d.periodo
            FROM Usuario u
            LEFT JOIN Desempeno_Vendedor d ON u.usuario_id = d.usuario_id
            ORDER BY u.usuario_id, d.periodo DESC
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("usuarioId",          rs.getInt("usuario_id"));
                fila.put("nombre",             rs.getString("nombre"));
                fila.put("ventasTotales",      rs.getBigDecimal("ventas_totales"));
                fila.put("comisionPorcentaje", rs.getBigDecimal("comision_porcentaje"));
                fila.put("comisionGanada",     rs.getBigDecimal("comision_ganada"));
                fila.put("periodo",            rs.getDate("periodo"));
                historial.add(fila);
            }
        } catch (Exception e) { // ✅
            e.printStackTrace();
        }
        return historial;
    }

    public Usuario obtenerUsuarioPorId(int id) {
        String sql = """
            SELECT u.usuario_id, u.nombre, u.estado, u.documento,
                   GROUP_CONCAT(t.telefono) AS telefonos,
                   GROUP_CONCAT(c.email) AS correos
            FROM Usuario u
            LEFT JOIN Telefono_Usuario t ON u.usuario_id = t.usuario_id
            LEFT JOIN Correo_Usuario c ON u.usuario_id = c.usuario_id
            WHERE u.usuario_id = ?
            GROUP BY u.usuario_id
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Usuario u = new Usuario();
                u.setUsuarioId(id);
                u.setNombre(rs.getString("nombre"));
                u.setEstado(rs.getBoolean("estado"));
                u.setDocumento(rs.getString("documento"));
                u.setTelefono(rs.getString("telefonos"));
                u.setCorreo(rs.getString("correos"));
                return u;
            }
        } catch (Exception e) { // ✅
            e.printStackTrace();
        }
        return null;
    }

    public boolean editarUsuario(Usuario usuario) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Usuario SET nombre=?, estado=? WHERE usuario_id=?")) {
                ps.setString(1, usuario.getNombre());
                ps.setBoolean(2, usuario.isEstado());
                ps.setInt(3, usuario.getUsuarioId());
                ps.executeUpdate();
            }

            try (PreparedStatement psCheck = conn.prepareStatement(
                    "SELECT * FROM Rol WHERE usuario_id=?")) {
                psCheck.setInt(1, usuario.getUsuarioId());
                ResultSet rs = psCheck.executeQuery();
                if (rs.next()) {
                    String[] partes = rs.getString("nombre").split("_");
                    String numero = (partes.length > 1) ? partes[1] : "1";
                    try (PreparedStatement psUpdate = conn.prepareStatement(
                            "UPDATE Rol SET cargo=?, nombre=? WHERE usuario_id=?")) {
                        psUpdate.setString(1, usuario.getRol());
                        psUpdate.setInt(3, usuario.getUsuarioId());
                        psUpdate.executeUpdate();
                    }
                } else {
                    int nextNumber = 1;
                    try (PreparedStatement psCount = conn.prepareStatement(
                            "SELECT COUNT(*) FROM Rol WHERE cargo=?")) {
                        psCount.setString(1, usuario.getRol());
                        ResultSet rsCount = psCount.executeQuery();
                        if (rsCount.next()) nextNumber = rsCount.getInt(1) + 1;
                    }
                    try (PreparedStatement psInsert = conn.prepareStatement(
                            "INSERT INTO Rol (cargo, usuario_id) VALUES (?, ?)")) {
                        psInsert.setString(1, usuario.getRol());
                        psInsert.setInt(2, usuario.getUsuarioId());
                        psInsert.setString(3, usuario.getRol().toLowerCase() + "_" + nextNumber);
                        psInsert.executeUpdate();
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Telefono_Usuario WHERE usuario_id=?")) {
                ps.setInt(1, usuario.getUsuarioId()); ps.executeUpdate();
            }
            if (usuario.getTelefono() != null && !usuario.getTelefono().trim().isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Telefono_Usuario (telefono, usuario_id) VALUES (?, ?)")) {
                    ps.setString(1, usuario.getTelefono().trim());
                    ps.setInt(2, usuario.getUsuarioId());
                    ps.executeUpdate();
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Correo_Usuario WHERE usuario_id=?")) {
                ps.setInt(1, usuario.getUsuarioId()); ps.executeUpdate();
            }
            if (usuario.getCorreo() != null && !usuario.getCorreo().trim().isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Correo_Usuario (email, usuario_id) VALUES (?, ?)")) {
                    ps.setString(1, usuario.getCorreo().trim());
                    ps.setInt(2, usuario.getUsuarioId());
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
                e.printStackTrace();
                try {
                    if (conn != null) conn.rollback();
                } catch (Exception ignored) {}
                return false;
        } finally {
            try {
                if (conn != null) { conn.setAutoCommit(true); conn.close(); }
            } catch (Exception ignored) {}
        }
    }

    public int contarUsuarios() {
        try (Connection c = ConexionDB.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM Usuario");
             ResultSet rs = ps.executeQuery()) {
            rs.next(); return rs.getInt(1);
        } catch (Exception e) { return 0; }
    }

    public int contarUsuariosActivos() {
        try (Connection c = ConexionDB.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM Usuario WHERE estado=1");
             ResultSet rs = ps.executeQuery()) {
            rs.next(); return rs.getInt(1);
        } catch (Exception e) { return 0; }
    }
}