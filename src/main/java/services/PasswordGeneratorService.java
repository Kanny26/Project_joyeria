package services;

import config.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Genera contraseñas temporales únicas basadas en el rol del usuario.
 *
 * Formato: PREFIJO + número correlativo de 3 dígitos
 *   - administrador → ADM001, ADM002 ...
 *   - vendedor      → VEN001, VEN002 ...
 *   - cliente       → CLI001, CLI002 ...
 *   - proveedor     → PRO001, PRO002 ...
 *   - (otros)       → USR001, USR002 ...
 *
 * El número se calcula contando cuántos usuarios con ese rol ya existen en BD,
 * así siempre es único y correlativo.
 */
public class PasswordGeneratorService {

    /**
     * Retorna la contraseña temporal generada para el rol indicado.
     * Si el admin ya escribió una contraseña manual, se usa esa y no se genera nada.
     *
     * @param rol              cargo del usuario ("vendedor", "administrador", etc.)
     * @param contrasenaManual la que escribió el admin en el form (puede ser null o vacía)
     * @return contraseña a usar (la manual si se proporcionó, o la generada)
     */
    public static String obtenerContrasena(String rol, String contrasenaManual) {
        // Si el admin ya puso una contraseña manual, respetarla
        if (contrasenaManual != null && !contrasenaManual.trim().isEmpty()) {
            return contrasenaManual.trim();
        }
        // Si no, generar contraseña automática por rol
        return generarPorRol(rol);
    }

    /**
     * Genera la contraseña automática según el rol.
     */
    public static String generarPorRol(String rol) {
        String prefijo = obtenerPrefijo(rol);
        int siguiente  = obtenerSiguienteNumero(rol);
        // Formato: PREFIJO + número con ceros a la izquierda (3 dígitos mínimo)
        return String.format("%s%03d", prefijo, siguiente);
    }

    // ── Privados ────────────────────────────────────────────────────────────

    private static String obtenerPrefijo(String rol) {
        if (rol == null) return "USR";
        return switch (rol.trim().toLowerCase()) {
            case "administrador" -> "ADM";
            case "vendedor"      -> "VEN";
            case "cliente"       -> "CLI";
            case "proveedor"     -> "PRO";
            default              -> "USR";
        };
    }

    /**
     * Cuenta cuántos usuarios con ese cargo ya existen en la BD
     * y devuelve el siguiente número correlativo (base 1).
     */
    private static int obtenerSiguienteNumero(String rol) {
        String sql = """
            SELECT COUNT(*) AS total
            FROM Usuario u
            INNER JOIN Usuario_Rol ur ON ur.usuario_id = u.usuario_id
            INNER JOIN Rol r          ON r.rol_id = ur.rol_id
            WHERE r.cargo = ?
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rol != null ? rol.trim().toLowerCase() : "vendedor");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("total") + 1;
            }
        } catch (Exception e) {
            System.err.println("Error al obtener siguiente número para rol: " + e.getMessage());
        }
        return 1; // Fallback seguro
    }
}