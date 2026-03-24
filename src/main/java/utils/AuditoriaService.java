package utils;

import config.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

/**
 * Servicio centralizado para registros de auditoría.
 * Cumple con RF38: Registro de Actividades (Logs).
 */
public class AuditoriaService {

    /**
     * Registra una acción en la tabla Auditoria_Log.
     * @param conn conexión a la BD (para usar en transacciones)
     * @param usuarioId ID del usuario que realizó la acción
     * @param accion tipo de acción (CREAR, EDITAR, ELIMINAR, etc.)
     * @param entidad nombre de la entidad afectada (Usuario, Venta, etc.)
     * @param entidadId ID de la entidad afectada
     * @param datosAnteriores datos antes del cambio (JSON o texto)
     * @param datosNuevos datos después del cambio (JSON o texto)
     * @throws Exception si falla el registro
     */
    public static void registrarAccion(Connection conn, int usuarioId, String accion, 
                                       String entidad, int entidadId, 
                                       String datosAnteriores, String datosNuevos) throws Exception {
        String sql = """
            INSERT INTO Auditoria_Log 
            (usuario_id, accion, entidad, entidad_id, datos_anteriores, datos_nuevos, fecha_hora)
            VALUES (?, ?, ?, ?, ?, ?, NOW())
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

    /**
     * Registra una acción con conexión automática (fuera de transacción).
     */
    public static void registrarAccion(int usuarioId, String accion, String entidad, 
                                       int entidadId, String datosAnteriores, String datosNuevos) {
        try (Connection conn = ConexionDB.getConnection()) {
            registrarAccion(conn, usuarioId, accion, entidad, entidadId, datosAnteriores, datosNuevos);
        } catch (Exception e) {
            System.err.println("Error al registrar auditoría: " + e.getMessage());
            // No lanzar excepción para no interrumpir el flujo principal
        }
    }

    /**
     * Registra inicio de sesión.
     */
    public static void registrarLogin(int usuarioId, String ip) {
        registrarAccion(usuarioId, "LOGIN", "Usuario", usuarioId, null, "IP: " + ip);
    }

    /**
     * Registra cierre de sesión.
     */
    public static void registrarLogout(int usuarioId) {
        registrarAccion(usuarioId, "LOGOUT", "Usuario", usuarioId, null, null);
    }
}