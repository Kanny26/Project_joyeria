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
 * Data Access Object (DAO) para la autenticación de usuarios en el sistema de joyería.
 * Proporciona métodos para validar credenciales contra las tablas de usuarios y roles,
 * implementando seguridad mediante BCrypt para el almacenamiento de contraseñas.
 * 
 * Este DAO es fundamental para el módulo administrativo, ya que controla el acceso
 * a las funcionalidades según el rol del usuario autenticado.
 */
public class AuthDAO {
    private static final Logger LOGGER = Logger.getLogger(AuthDAO.class.getName());

    /**
     * Valida las credenciales de un usuario verificando que exista, esté activo
     * y que la contraseña proporcionada coincida con el hash almacenado en base de datos.
     * 
     * El método realiza una consulta que obtiene información del usuario junto con su rol
     * en una sola operación, optimizando el acceso a datos durante el proceso de login.
     * 
     * @param nombre El nombre de usuario a validar. Debe coincidir exactamente con el
     *               campo 'nombre' en la tabla Usuario.
     * @param password La contraseña en texto plano proporcionada por el usuario.
     *                 Se comparará mediante BCrypt contra el hash almacenado.
     * @return Un Map que contiene los datos del usuario autenticado con las claves:
     *         "id" (Integer), "nombre" (String), "rol" (String). 
     *         Retorna null si las credenciales son inválidas, el usuario está inactivo,
     *         o ocurre un error durante el proceso.
     */
    public Map<String, Object> validar(String nombre, String password) {
        /*
         * Consulta SQL que obtiene todos los datos necesarios para la autenticación en una sola ejecución.
         * 
         * Tablas involucradas:
         *   - Usuario: Tabla principal con datos de cuenta, incluye el hash de la contraseña y estado.
         *   - Usuario_Rol: Tabla puente que asigna roles a usuarios (relación muchos a muchos).
         *   - Rol: Tabla de catálogo con los diferentes roles del sistema (Administrador, Vendedor, etc.).
         * 
         * Condiciones:
         *   - u.nombre = ? : Filtra por el nombre de usuario exacto proporcionado en el login.
         *   - u.estado = 1 : Solo permite acceso si el usuario está activo en el sistema.
         * 
         * JOINs:
         *   - INNER JOIN Usuario_Rol: Garantiza que el usuario tenga al menos un rol asignado.
         *   - INNER JOIN Rol: Trae el nombre del rol para saber qué permisos tendrá el usuario.
         * 
         * Relación con la lógica del método:
         *   - Esta consulta resuelve en un solo viaje a la base de datos la validación de existencia,
         *     estado activo, obtención del hash de contraseña y el rol asignado.
         */
        String sql = """
            SELECT u.usuario_id, u.nombre, u.pass, r.cargo 
            FROM Usuario u 
            INNER JOIN Usuario_Rol ur ON ur.usuario_id = u.usuario_id 
            INNER JOIN Rol r ON r.rol_id = ur.rol_id 
            WHERE u.nombre = ? AND u.estado = 1
        """;

        /*
         * Uso de try-with-resources para asegurar el cierre automático de:
         *   - Connection: Conexión a la base de datos.
         *   - PreparedStatement: Previene inyección SQL al parametrizar la consulta.
         * 
         * El bloque maneja excepciones de conexión y consultas de forma segura.
         */
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            /*
             * Asigna el parámetro de la consulta (nombre de usuario) al PreparedStatement.
             * Esto evita concatenación de strings y protege contra SQL Injection.
             */
            ps.setString(1, nombre);
            
            /*
             * Ejecuta la consulta SELECT que devuelve:
             *   - usuario_id: Identificador único del usuario.
             *   - nombre: Nombre de usuario para confirmación.
             *   - pass: Hash BCrypt de la contraseña almacenado.
             *   - cargo: Nombre del rol (Ej: ADMIN, VENDEDOR, etc.).
             */
            ResultSet rs = ps.executeQuery();

            /*
             * Si el ResultSet tiene al menos un registro, significa que:
             *   1. El usuario existe con ese nombre exacto.
             *   2. El usuario tiene al menos un rol asignado.
             *   3. El usuario está activo (estado = 1).
             */
            if (rs.next()) {
                /*
                 * Obtiene el hash de la contraseña almacenado en la base de datos.
                 * Nunca se almacena ni se recupera la contraseña en texto plano,
                 * cumpliendo con buenas prácticas de seguridad.
                 */
                String passBD = rs.getString("pass");

                /*
                 * BCrypt.checkpw realiza la comparación segura:
                 *   - Toma la contraseña en texto plano ingresada por el usuario.
                 *   - La compara con el hash almacenado usando el algoritmo BCrypt.
                 *   - La comparación es resistente a ataques de timing.
                 */
                if (BCrypt.checkpw(password, passBD)) {
                    /*
                     * Autenticación exitosa: se construye un Map con los datos necesarios
                     * para la sesión del usuario en el sistema de joyería.
                     */
                    Map<String, Object> datos = new HashMap<>();
                    datos.put("id", rs.getInt("usuario_id"));
                    datos.put("nombre", rs.getString("nombre"));
                    datos.put("rol", rs.getString("cargo"));
                    return datos;
                }
            }
        } catch (Exception e) {
            /*
             * Manejo de excepciones que pueden ocurrir:
             *   - Problemas de conexión con la base de datos.
             *   - Errores de sintaxis en la consulta SQL (aunque la aplicación está probada).
             *   - Timeouts de red o problemas de infraestructura.
             * 
             * Se registra el error a nivel de LOGGER para diagnóstico técnico,
             * pero no se expone información sensible al usuario (se retorna null).
             * Esto evita que atacantes puedan obtener detalles sobre la estructura
             * de la base de datos o del sistema.
             */
            LOGGER.log(Level.SEVERE, "Error en autenticación", e);
        }

        /*
         * Retorna null en dos escenarios:
         *   1. El usuario no existe, está inactivo, o no tiene rol asignado.
         *   2. La contraseña no coincide con el hash almacenado.
         * 
         * Esta estrategia es intencional: no diferenciar entre "usuario no existe"
         * y "contraseña incorrecta" para prevenir enumeración de usuarios,
         * que es una técnica común en ataques de fuerza bruta.
         */
        return null;
    }
}