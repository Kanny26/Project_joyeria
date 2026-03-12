package dao;

import config.ConexionDB;
import model.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import services.EmailService;
import services.PasswordGeneratorService;

import java.sql.*;
import java.util.*;

public class UsuarioDAO {

    public boolean existeCorreo(String correo) {
        String sql = "SELECT COUNT(*) FROM Correo_Usuario WHERE email = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, correo.toLowerCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existeCorreoParaOtro(String correo, int usuarioIdActual) {
        String sql = "SELECT COUNT(*) FROM Correo_Usuario WHERE email = ? AND usuario_id <> ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, correo.toLowerCase().trim());
            ps.setInt(2, usuarioIdActual);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== AGREGAR USUARIO (RF06) ====================
    public boolean agregarUsuario(Usuario usuario) {

        if (usuario.getCorreo() != null && !usuario.getCorreo().trim().isEmpty()) {
            if (existeCorreo(usuario.getCorreo())) {
                System.err.println("■ ERROR: Correo ya existe: " + usuario.getCorreo());
                return false;
            }
        }

        String sqlUsuario = """
            INSERT INTO Usuario(nombre, pass, estado, fecha_creacion) 
            VALUES(?,?,?, NOW())
            """;
        String sqlTelefono = "INSERT INTO Telefono_Usuario(telefono, usuario_id) VALUES(?,?)";
        String sqlCorreo   = "INSERT INTO Correo_Usuario(email, usuario_id) VALUES(?,?)";

        String contrasenaTextoPlano = PasswordGeneratorService.obtenerContrasena(
            usuario.getRol(),
            usuario.getContrasena()
        );
        usuario.setContrasena(contrasenaTextoPlano);

        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false);
            int usuarioId;

            // 1. Insertar en Usuario
            try (PreparedStatement ps = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, usuario.getNombre());
                ps.setString(2, BCrypt.hashpw(contrasenaTextoPlano, BCrypt.gensalt()));
                ps.setBoolean(3, usuario.isEstado());
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                usuarioId = rs.getInt(1);
            }

            // 2. Asignar rol
            String cargo = (usuario.getRol() != null && !usuario.getRol().trim().isEmpty())
                ? usuario.getRol().trim().toLowerCase() : "vendedor";

            if (!cargo.equals("superadministrador") && !cargo.equals("administrador") && !cargo.equals("vendedor")) {
                throw new SQLException("Rol no válido: " + cargo);
            }

            int rolId;
            try (PreparedStatement psGetRol = conn.prepareStatement("SELECT rol_id FROM Rol WHERE cargo=?")) {
                psGetRol.setString(1, cargo);
                ResultSet rsRol = psGetRol.executeQuery();
                if (!rsRol.next()) throw new SQLException("Rol no encontrado: " + cargo);
                rolId = rsRol.getInt("rol_id");
            }

            try (PreparedStatement psRol = conn.prepareStatement("INSERT INTO Usuario_Rol(usuario_id, rol_id) VALUES(?,?)")) {
                psRol.setInt(1, usuarioId);
                psRol.setInt(2, rolId);
                psRol.executeUpdate();
            }

            // 3. Teléfono
            if (usuario.getTelefono() != null && !usuario.getTelefono().trim().isEmpty()) {
                try (PreparedStatement psTel = conn.prepareStatement(sqlTelefono)) {
                    for (String tel : usuario.getTelefono().split(",")) {
                        if (!tel.trim().isEmpty()) {
                            psTel.setString(1, tel.trim());
                            psTel.setInt(2, usuarioId);
                            psTel.executeUpdate();
                        }
                    }
                }
            }

            // 4. Correo
            if (usuario.getCorreo() != null && !usuario.getCorreo().trim().isEmpty()) {
                try (PreparedStatement psCorreo = conn.prepareStatement(sqlCorreo)) {
                    psCorreo.setString(1, usuario.getCorreo().trim().toLowerCase());
                    psCorreo.setInt(2, usuarioId);
                    psCorreo.executeUpdate();
                }
            }

            conn.commit();
            System.out.println("■ Usuario guardado con ID: " + usuarioId + " | Contraseña: " + contrasenaTextoPlano);

            // 5. Enviar correo
            if (usuario.getCorreo() != null && !usuario.getCorreo().trim().isEmpty()) {
                boolean correoEnviado = EmailService.enviarCredenciales(
                    usuario.getCorreo().trim(),
                    usuario.getNombre(),
                    cargo,
                    contrasenaTextoPlano
                );
                if (!correoEnviado) {
                    System.err.println("■ Usuario creado pero el correo no pudo enviarse a: " + usuario.getCorreo());
                }
            }

            // 6. Auditoría
            try {
                registrarAuditoria(conn, usuarioId, "CREAR", "Usuario", usuarioId, null, usuario.getNombre());
            } catch (Exception eAudit) {
                System.err.println("⚠️ Auditoría no registrada (no crítico): " + eAudit.getMessage());
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== LISTAR CON FILTROS (RF07) ====================
    public List<Usuario> listarUsuarios(String filtroRol, String filtroEstado) {
        List<Usuario> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT u.usuario_id, u.nombre, u.estado, u.fecha_creacion, r.cargo, 
                   GROUP_CONCAT(DISTINCT t.telefono) AS telefonos, 
                   GROUP_CONCAT(DISTINCT c.email) AS correos 
            FROM Usuario u 
            LEFT JOIN Usuario_Rol ur ON ur.usuario_id = u.usuario_id 
            LEFT JOIN Rol r ON r.rol_id = ur.rol_id 
            LEFT JOIN Telefono_Usuario t ON u.usuario_id = t.usuario_id 
            LEFT JOIN Correo_Usuario c ON u.usuario_id = c.usuario_id 
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();

        if (filtroRol != null && !filtroRol.isEmpty() && !"todos".equals(filtroRol)) {
            sql.append(" AND r.cargo = ?");
            params.add(filtroRol.toLowerCase());
        }

        if (filtroEstado != null && !filtroEstado.isEmpty() && !"todos".equals(filtroEstado)) {
            sql.append(" AND u.estado = ?");
            params.add("Activo".equals(filtroEstado) ? 1 : 0);
        }

        sql.append(" GROUP BY u.usuario_id, u.nombre, u.estado, u.fecha_creacion, r.cargo");

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Usuario u = new Usuario();
                    u.setUsuarioId(rs.getInt("usuario_id"));
                    u.setNombre(rs.getString("nombre"));
                    u.setEstado(rs.getBoolean("estado"));
                    u.setFechaCreacion(rs.getDate("fecha_creacion"));
                    u.setRol(rs.getString("cargo"));
                    u.setTelefono(rs.getString("telefonos"));
                    u.setCorreo(rs.getString("correos"));
                    lista.add(u);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    // ==================== EDITAR USUARIO (RF08) ====================
    public boolean editarUsuario(Usuario usuario, int usuarioQueEditaId) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false);

            Usuario anterior = obtenerUsuarioPorId(usuario.getUsuarioId());
            String nombreAnterior = anterior != null ? anterior.getNombre() : "N/A";

            // Actualizar nombre y estado
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Usuario SET nombre=?, estado=? WHERE usuario_id=?")) {
                ps.setString(1, usuario.getNombre());
                ps.setBoolean(2, usuario.isEstado());
                ps.setInt(3, usuario.getUsuarioId());
                ps.executeUpdate();
            }

            // Actualizar rol
            String cargo = usuario.getRol() != null ? usuario.getRol().trim().toLowerCase() : "vendedor";
            if (!cargo.equals("superadministrador") && !cargo.equals("administrador") && !cargo.equals("vendedor")) {
                throw new SQLException("Rol no válido: " + cargo);
            }

            int rolId;
            try (PreparedStatement psGetRol = conn.prepareStatement("SELECT rol_id FROM Rol WHERE cargo=?")) {
                psGetRol.setString(1, cargo);
                ResultSet rsRol = psGetRol.executeQuery();
                if (!rsRol.next()) throw new SQLException("Rol no encontrado: " + cargo);
                rolId = rsRol.getInt("rol_id");
            }

            try (PreparedStatement psCheck = conn.prepareStatement("SELECT 1 FROM Usuario_Rol WHERE usuario_id=?")) {
                psCheck.setInt(1, usuario.getUsuarioId());
                ResultSet rs = psCheck.executeQuery();
                if (rs.next()) {
                    try (PreparedStatement psUpdate = conn.prepareStatement(
                            "UPDATE Usuario_Rol SET rol_id=? WHERE usuario_id=?")) {
                        psUpdate.setInt(1, rolId);
                        psUpdate.setInt(2, usuario.getUsuarioId());
                        psUpdate.executeUpdate();
                    }
                } else {
                    try (PreparedStatement psInsert = conn.prepareStatement(
                            "INSERT INTO Usuario_Rol(usuario_id, rol_id) VALUES(?,?)")) {
                        psInsert.setInt(1, usuario.getUsuarioId());
                        psInsert.setInt(2, rolId);
                        psInsert.executeUpdate();
                    }
                }
            }

            // Reemplazar teléfono
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Telefono_Usuario WHERE usuario_id=?")) {
                ps.setInt(1, usuario.getUsuarioId());
                ps.executeUpdate();
            }
            if (usuario.getTelefono() != null && !usuario.getTelefono().trim().isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Telefono_Usuario(telefono, usuario_id) VALUES(?,?)")) {
                    ps.setString(1, usuario.getTelefono().trim());
                    ps.setInt(2, usuario.getUsuarioId());
                    ps.executeUpdate();
                }
            }

            // Reemplazar correo
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Correo_Usuario WHERE usuario_id=?")) {
                ps.setInt(1, usuario.getUsuarioId());
                ps.executeUpdate();
            }
            if (usuario.getCorreo() != null && !usuario.getCorreo().trim().isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Correo_Usuario(email, usuario_id) VALUES(?,?)")) {
                    ps.setString(1, usuario.getCorreo().trim().toLowerCase());
                    ps.setInt(2, usuario.getUsuarioId());
                    ps.executeUpdate();
                }
            }

            conn.commit();

            // Auditoría
            try {
                registrarAuditoria(conn, usuarioQueEditaId, "EDITAR", "Usuario",
                        usuario.getUsuarioId(), nombreAnterior, usuario.getNombre());
            } catch (Exception eAudit) {
                System.err.println("⚠️ Auditoría no registrada (no crítico): " + eAudit.getMessage());
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception ignored) {}
        }
    }

    // ==================== AUXILIARES ====================
    public Usuario obtenerUsuarioPorId(int id) {
        String sql = """
                SELECT u.usuario_id, u.nombre, u.estado,
                       GROUP_CONCAT(DISTINCT t.telefono) AS telefonos,
                       GROUP_CONCAT(DISTINCT c.email)    AS correos,
                       r.cargo
                FROM Usuario u
                LEFT JOIN Telefono_Usuario t  ON u.usuario_id = t.usuario_id
                LEFT JOIN Correo_Usuario   c  ON u.usuario_id = c.usuario_id
                LEFT JOIN Usuario_Rol      ur ON u.usuario_id = ur.usuario_id
                LEFT JOIN Rol              r  ON ur.rol_id    = r.rol_id
                WHERE u.usuario_id = ?
                GROUP BY u.usuario_id, u.nombre, u.estado, r.cargo
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
                u.setRol(rs.getString("cargo"));
                u.setTelefono(rs.getString("telefonos"));
                u.setCorreo(rs.getString("correos"));
                return u;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int contarUsuarios() {
        try (Connection c = ConexionDB.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM Usuario");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (Exception e) {
            return 0;
        }
    }

    public int contarUsuariosActivos() {
        try (Connection c = ConexionDB.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM Usuario WHERE estado=1");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (Exception e) {
            return 0;
        }
    }

    private void registrarAuditoria(Connection conn, int usuarioId, String accion, String entidad,
                                    int entidadId, String datosAnteriores, String datosNuevos) throws SQLException {
        String sql = """
            INSERT INTO Auditoria_Log(usuario_id, accion, entidad, entidad_id, datos_anteriores, datos_nuevos, fecha_hora)
            VALUES(?, ?, ?, ?, ?, ?, NOW())
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, accion);
            stmt.setString(3, entidad);
            stmt.setInt(4, entidadId);
            stmt.setString(5, datosAnteriores);
            stmt.setString(6, datosNuevos);
            stmt.executeUpdate();
        }
    }

    public List<Map<String, Object>> obtenerHistorialUsuariosConDesempeno() {
        return null;
    }
}