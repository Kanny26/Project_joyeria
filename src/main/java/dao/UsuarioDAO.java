package dao;

import config.ConexionDB;
import model.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import services.EmailService;
import services.PasswordGeneratorService;

import java.sql.*;
import java.util.*;

/**
 * ==========================================================
 * DAO DE USUARIO
 * ==========================================================
 * Gestiona toda la lógica de acceso a datos relacionada con usuarios.
 * 
 * Funcionalidades:
 * - Validación de correos
 * - Registro de usuarios (con rol, teléfono y correo)
 * - Listado con filtros dinámicos
 * - Edición de usuarios
 * - Auditoría de acciones
 * - Consultas auxiliares
 * ==========================================================
 */
public class UsuarioDAO {

    /**
     * Verifica si un correo ya existe en la base de datos.
     */
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

    /**
     * Verifica si un correo existe para otro usuario diferente.
     * Usado en validaciones durante edición.
     */
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

    // ==================== AGREGAR USUARIO ====================

    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * Incluye:
     * - Validación de correo
     * - Generación y encriptación de contraseña
     * - Inserción en múltiples tablas
     * - Envío de correo con credenciales
     * - Registro en auditoría
     * 
     * Manejado con transacciones.
     */
    public boolean agregarUsuario(Usuario usuario) {

        if (usuario.getCorreo() != null && !usuario.getCorreo().trim().isEmpty()) {
            if (existeCorreo(usuario.getCorreo())) {
                System.err.println("ERROR: Correo ya existe: " + usuario.getCorreo());
                return false;
            }
        }

        String sqlUsuario = """
            INSERT INTO Usuario(nombre, pass, estado, fecha_creacion) 
            VALUES(?,?,?, NOW())
            """;
        String sqlTelefono = "INSERT INTO Telefono_Usuario(telefono, usuario_id) VALUES(?,?)";
        String sqlCorreo   = "INSERT INTO Correo_Usuario(email, usuario_id) VALUES(?,?)";

        // Generación de contraseña
        String contrasenaTextoPlano = PasswordGeneratorService.obtenerContrasena(
            usuario.getRol(),
            usuario.getContrasena()
        );
        usuario.setContrasena(contrasenaTextoPlano);

        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false);
            int usuarioId;

            // Inserción en Usuario
            try (PreparedStatement ps = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, usuario.getNombre());
                ps.setString(2, BCrypt.hashpw(contrasenaTextoPlano, BCrypt.gensalt()));
                ps.setBoolean(3, usuario.isEstado());
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                usuarioId = rs.getInt(1);
            }

            // Validación y asignación de rol
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

            // Teléfonos múltiples
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

            // Correo
            if (usuario.getCorreo() != null && !usuario.getCorreo().trim().isEmpty()) {
                try (PreparedStatement psCorreo = conn.prepareStatement(sqlCorreo)) {
                    psCorreo.setString(1, usuario.getCorreo().trim().toLowerCase());
                    psCorreo.setInt(2, usuarioId);
                    psCorreo.executeUpdate();
                }
            }

            conn.commit();

            // Envío de correo
            if (usuario.getCorreo() != null && !usuario.getCorreo().trim().isEmpty()) {
                EmailService.enviarCredenciales(
                    usuario.getCorreo().trim(),
                    usuario.getNombre(),
                    cargo,
                    contrasenaTextoPlano
                );
            }

            // Auditoría
            try {
                String datosNuevosJson = "{\"nombre\": " + org.json.JSONObject.quote(usuario.getNombre())
                        + ", \"rol\": " + org.json.JSONObject.quote(cargo) + "}";
                registrarAuditoria(conn, usuarioId, "CREAR", "Usuario", usuarioId, null, datosNuevosJson);
            } catch (Exception ignored) {}

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== LISTAR ====================

    /**
     * Lista usuarios con filtros opcionales por rol y estado.
     */
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

    // ==================== EDITAR ====================

    /**
     * Edita un usuario existente y registra auditoría.
     */
    public boolean editarUsuario(Usuario usuario, int usuarioQueEditaId) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false);

            Usuario anterior = obtenerUsuarioPorId(usuario.getUsuarioId());

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Usuario SET nombre=?, estado=? WHERE usuario_id=?")) {
                ps.setString(1, usuario.getNombre());
                ps.setBoolean(2, usuario.isEstado());
                ps.setInt(3, usuario.getUsuarioId());
                ps.executeUpdate();
            }

            conn.commit();

            return true;

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            return false;
        }
    }

    // ==================== AUXILIARES ====================

    /**
     * Obtiene un usuario por ID con información relacionada.
     */
    public Usuario obtenerUsuarioPorId(int id) {
        String sql = """
            SELECT u.usuario_id, u.nombre, u.estado, u.fecha_creacion, r.cargo,
                   GROUP_CONCAT(DISTINCT t.telefono) AS telefonos,
                   GROUP_CONCAT(DISTINCT c.email)    AS correos
            FROM Usuario u
            LEFT JOIN Usuario_Rol ur ON ur.usuario_id = u.usuario_id
            LEFT JOIN Rol r          ON r.rol_id = ur.rol_id
            LEFT JOIN Telefono_Usuario t ON u.usuario_id = t.usuario_id
            LEFT JOIN Correo_Usuario c   ON u.usuario_id = c.usuario_id
            WHERE u.usuario_id = ?
            GROUP BY u.usuario_id, u.nombre, u.estado, u.fecha_creacion, r.cargo
            """;

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setUsuarioId(rs.getInt("usuario_id"));
                    u.setNombre(rs.getString("nombre"));
                    u.setEstado(rs.getBoolean("estado"));
                    u.setFechaCreacion(rs.getDate("fecha_creacion"));
                    u.setRol(rs.getString("cargo"));
                    u.setTelefono(rs.getString("telefonos"));
                    u.setCorreo(rs.getString("correos"));
                    return u;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }/**
     * Cuenta total de usuarios.
     */
    public int contarUsuarios() {
        String sql = "SELECT COUNT(*) FROM Usuario";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Cuenta usuarios activos.
     */
    public int contarUsuariosActivos() {
        String sql = "SELECT COUNT(*) FROM Usuario WHERE estado = 1";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Registra acciones en auditoría.
     */
    private void registrarAuditoria(Connection conn, int usuarioId, String accion, String entidad,
                                    int entidadId, String datosAnteriores, String datosNuevos) throws SQLException {
        String sql = """
            INSERT INTO Auditoria(usuario_id, accion, entidad, entidad_id, datos_anteriores, datos_nuevos, fecha)
            VALUES (?, ?, ?, ?, ?, ?, NOW())
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setString(2, accion);
            ps.setString(3, entidad);
            ps.setInt(4, entidadId);
            ps.setString(5, datosAnteriores);
            ps.setString(6, datosNuevos);
            ps.executeUpdate();
        }
    }

    /**
     * Valida si un texto es JSON.
     */
    private boolean esJsonValido(String texto) {
        if (texto == null || texto.trim().isEmpty()) return false;
        try {
            new org.json.JSONObject(texto);
            return true;
        } catch (Exception e) {
            try {
                new org.json.JSONArray(texto);
                return true;
            } catch (Exception e2) {
                return false;
            }
        }
    }

    /**
     * Obtiene historial de usuarios con su desempeño.
     */
    public List<Map<String, Object>> obtenerHistorialUsuariosConDesempeno() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = """
            SELECT u.usuario_id, u.nombre, u.estado, u.fecha_creacion, r.cargo,
                   a.accion, a.entidad, a.fecha AS fecha_auditoria,
                   a.datos_anteriores, a.datos_nuevos
            FROM Usuario u
            LEFT JOIN Usuario_Rol ur  ON ur.usuario_id = u.usuario_id
            LEFT JOIN Rol r           ON r.rol_id = ur.rol_id
            LEFT JOIN Auditoria a     ON a.usuario_id = u.usuario_id
            ORDER BY a.fecha DESC
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("usuario_id",       rs.getInt("usuario_id"));
                fila.put("nombre",           rs.getString("nombre"));
                fila.put("estado",           rs.getBoolean("estado"));
                fila.put("fecha_creacion",   rs.getDate("fecha_creacion"));
                fila.put("cargo",            rs.getString("cargo"));
                fila.put("accion",           rs.getString("accion"));
                fila.put("entidad",          rs.getString("entidad"));
                fila.put("fecha_auditoria",  rs.getTimestamp("fecha_auditoria"));
                fila.put("datos_anteriores", rs.getString("datos_anteriores"));
                fila.put("datos_nuevos",     rs.getString("datos_nuevos"));
                lista.add(fila);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}