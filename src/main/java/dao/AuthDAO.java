package dao;

import config.ConexionDB;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Maneja la validación de credenciales de usuario para el inicio de sesión.
 * Solo retorna datos si el usuario existe, está activo y la contraseña es correcta.
 */
public class AuthDAO {
    private static final Logger LOGGER = Logger.getLogger(AuthDAO.class.getName());

    /**
     * Verifica si el nombre de usuario y la contraseña son correctos.
     * Retorna un mapa con id, nombre y rol si la autenticación es exitosa,
     * o null si las credenciales son incorrectas o el usuario está inactivo.
     */
    public Map<String, Object> validar(String nombre, String password) {
        // La consulta trae el hash de la contraseña y el rol del usuario.
        // El filtro "estado = 1" impide que usuarios inactivos puedan ingresar.
        String sql = """
            SELECT u.usuario_id, u.nombre, u.pass, r.cargo 
            FROM Usuario u 
            INNER JOIN Usuario_Rol ur ON ur.usuario_id = u.usuario_id 
            INNER JOIN Rol r ON r.rol_id = ur.rol_id 
            WHERE u.nombre = ? AND u.estado = 1
        """;

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String passBD = rs.getString("pass");

                // BCrypt.checkpw compara la contraseña ingresada contra el hash almacenado.
                // Nunca se comparan contraseñas en texto plano; BCrypt lo hace de forma segura.
                if (BCrypt.checkpw(password, passBD)) {
                    Map<String, Object> datos = new HashMap<>();
                    datos.put("id", rs.getInt("usuario_id"));
                    datos.put("nombre", rs.getString("nombre"));
                    datos.put("rol", rs.getString("cargo"));
                    return datos;
                }
            }
        } catch (Exception e) {
            // Se registra el error en el log del servidor sin exponer detalles al usuario
            LOGGER.log(Level.SEVERE, "Error en autenticación", e);
        }

        // Retorna null tanto si el usuario no existe como si la contraseña es incorrecta.
        // Esto es intencional: no se debe indicar cuál de los dos falló (seguridad).
        return null;
    }
}
