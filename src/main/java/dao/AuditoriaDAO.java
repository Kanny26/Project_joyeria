package dao;

import config.ConexionDB;
import org.json.JSONObject;

import java.sql.*;
import java.util.*;

/**
 * Registra todas las acciones importantes del sistema en la tabla Auditoria_Log.
 * Permite rastrear quién hizo qué y cuándo, útil para revisiones y seguridad.
 */
public class AuditoriaDAO {

    /**
     * Método central que inserta un registro de auditoría.
     *
     * @param usuarioId       ID del usuario (0 = anónimo, ej: LOGIN_FALLIDO)
     * @param accion          Tipo de acción, ej: "LOGIN_EXITOSO", "PRODUCTO_CREADO"
     * @param entidad         Entidad afectada, ej: "Usuario", "Producto"
     * @param entidadId       ID del registro afectado (null si no aplica)
     * @param datosAnteriores Estado antes del cambio (null si no aplica)
     * @param datosNuevos     Estado después del cambio (null si no aplica)
     * @param direccionIp     IP del cliente
     * @return true si se guardó correctamente
     */
    public static boolean registrarAccion(
            int usuarioId,
            String accion,
            String entidad,
            Integer entidadId,
            JSONObject datosAnteriores,
            JSONObject datosNuevos,
            String direccionIp) throws Exception {

        // usuarioId == 0 es válido para acciones anónimas (ej: LOGIN_FALLIDO)
        if (usuarioId < 0 || accion == null || accion.isEmpty()) {
            System.err.println("Parámetros inválidos en auditoria");
            return false;
        }

        String sql = "INSERT INTO Auditoria_Log " +
                    "(usuario_id, accion, entidad, entidad_id, datos_anteriores, datos_nuevos, direccion_ip) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // usuarioId == 0 → NULL en la FK (usuario anónimo)
            if (usuarioId == 0) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, usuarioId);
            }

            ps.setString(2, accion);
            ps.setString(3, entidad);

            if (entidadId != null) {
                ps.setInt(4, entidadId);
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }

            if (datosAnteriores != null) {
                ps.setString(5, datosAnteriores.toString());
            } else {
                ps.setNull(5, java.sql.Types.NULL);
            }

            if (datosNuevos != null) {
                ps.setString(6, datosNuevos.toString());
            } else {
                ps.setNull(6, java.sql.Types.NULL);
            }

            ps.setString(7, direccionIp);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error registrando auditoría: " + e.getMessage());
            return false;
        }
    }

    // ==================== CONSULTA PRINCIPAL ====================

    /**
     * Retorna todos los registros de auditoría ordenados del más reciente al más antiguo.
     * Incluye el nombre del usuario que realizó la acción (o "Anónimo" si no aplica).
     * Solo debe llamarse desde roles: superadministrador y administrador.
     *
     * Cada mapa contiene:
     *   log_id, usuario_nombre, accion, entidad, entidad_id,
     *   datos_anteriores, datos_nuevos, direccion_ip, fecha_hora
     * @throws Exception 
     */
    public List<Map<String, Object>> listarLogs() throws Exception {
        List<Map<String, Object>> lista = new ArrayList<>();

        String sql = """
                SELECT
                    al.log_id,
                    COALESCE(u.nombre, 'Anónimo')  AS usuario_nombre,
                    al.accion,
                    al.entidad,
                    al.entidad_id,
                    al.datos_anteriores,
                    al.datos_nuevos,
                    al.direccion_ip,
                    al.fecha_hora
                FROM Auditoria_Log al
                LEFT JOIN Usuario u ON al.usuario_id = u.usuario_id
                ORDER BY al.fecha_hora DESC
                """;

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("log_id",           rs.getInt("log_id"));
                fila.put("usuario_nombre",   rs.getString("usuario_nombre"));
                fila.put("accion",           rs.getString("accion"));
                fila.put("entidad",          rs.getString("entidad"));
                fila.put("entidad_id",       rs.getObject("entidad_id"));       // puede ser null
                fila.put("datos_anteriores", rs.getString("datos_anteriores")); // JSON string o null
                fila.put("datos_nuevos",     rs.getString("datos_nuevos"));     // JSON string o null
                fila.put("direccion_ip",     rs.getString("direccion_ip"));
                fila.put("fecha_hora",       rs.getTimestamp("fecha_hora"));
                lista.add(fila);
            }

        } catch (SQLException e) {
            System.err.println("Error listando auditoría: " + e.getMessage());
        }

        return lista;
    }

    // ==================== ACCIONES ESPECÍFICAS ====================

    public static boolean registrarLoginExitoso(int usuarioId, String usuario, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("usuario", usuario);
        datos.put("timestamp", System.currentTimeMillis());
        return registrarAccion(usuarioId, "LOGIN_EXITOSO", "Usuario", usuarioId, null, datos, ip);
    }

    public static boolean registrarLoginFallido(String usuario, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("usuario", usuario);
        datos.put("timestamp", System.currentTimeMillis());
        return registrarAccion(0, "LOGIN_FALLIDO", "Usuario", null, null, datos, ip);
    }

    public static boolean registrarProductoCreado(int usuarioId, int productoId,
                                                   String nombreProducto, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("producto_id", productoId);
        datos.put("nombre", nombreProducto);
        return registrarAccion(usuarioId, "PRODUCTO_CREADO", "Producto", productoId, null, datos, ip);
    }

    public static boolean registrarProductoEditado(int usuarioId, int productoId,
                                                    JSONObject datosAnteriores,
                                                    JSONObject datosNuevos, String ip) throws Exception {
        return registrarAccion(usuarioId, "PRODUCTO_EDITADO", "Producto", productoId,
                            datosAnteriores, datosNuevos, ip);
    }

    public static boolean registrarVentaCreada(int usuarioId, int ventaId,
                                               int clienteId, double montoTotal, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("venta_id", ventaId);
        datos.put("cliente_id", clienteId);
        datos.put("monto_total", montoTotal);
        return registrarAccion(usuarioId, "VENTA_CREADA", "Venta", ventaId, null, datos, ip);
    }

    public static boolean registrarCambioPassword(int usuarioId, String ip) throws Exception {
        return registrarAccion(usuarioId, "PASSWORD_CAMBIADA", "Usuario", usuarioId, null, null, ip);
    }
}