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
 * Aquí validamos credenciales contra tablas de usuario/rol, permitiendo ingreso solo a cuentas
 * activas y con contraseña correcta (hash BCrypt), para proteger el módulo administrativo de joyería.
 */
public class AuthDAO {
    private static final Logger LOGGER = Logger.getLogger(AuthDAO.class.getName());

    /**
     * Verifica si el nombre de usuario y la contraseña son correctos.
     * Retorna un mapa con id, nombre y rol si la autenticación es exitosa,
     * o null si las credenciales son incorrectas o el usuario está inactivo.
     *
     * @param nombre nombre de usuario en la tabla {@code Usuario}
     * @param password contraseña en texto plano (se compara con BCrypt al hash almacenado)
     * @return mapa con claves {@code id}, {@code nombre}, {@code rol} o {@code null} si no hay acceso
     */
    public Map<String, Object> validar(String nombre, String password) {
        // Esta consulta resuelve la validación de acceso en negocio: identifica al usuario de joyería
        // y trae su rol para decidir qué módulos puede ver después del login.
        // INNER JOIN Usuario_Rol + Rol vincula cuenta y cargo; WHERE u.nombre filtra al usuario
        // digitado y WHERE u.estado = 1 evita acceso de cuentas desactivadas.
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

                // Comparamos contraseña digitada vs hash guardado: nunca se lee ni se guarda texto plano.
                // Así se controla el riesgo de exposición de credenciales ante fuga de base de datos.
                if (BCrypt.checkpw(password, passBD)) {
                    Map<String, Object> datos = new HashMap<>();
                    datos.put("id", rs.getInt("usuario_id"));
                    datos.put("nombre", rs.getString("nombre"));
                    datos.put("rol", rs.getString("cargo"));
                    return datos;
                }
            }
        } catch (Exception e) {
            // El catch controla fallas de conexión/consulta y garantiza respuesta segura al negocio:
            // se registra diagnóstico técnico en servidor, sin revelar detalles sensibles al usuario final.
            LOGGER.log(Level.SEVERE, "Error en autenticación", e);
        }

        // Retornamos null cuando no hay autenticación válida: esto evita decir si falló usuario o clave,
        // reduciendo el riesgo de enumeración de cuentas en intentos maliciosos.
        return null;
    }
}
