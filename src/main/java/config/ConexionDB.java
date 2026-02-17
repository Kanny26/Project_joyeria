package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Clase encargada de gestionar la conexión a la base de datos MySQL.
    Proporciona un método estático para obtener una conexión activa que será utilizada por los DAOs del proyecto.
 **/
public class ConexionDB {

    /** URL de conexión a la base de datos */
    private static final String URL =
    "jdbc:mysql://localhost:3306/gestor_abbyac27?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    /** Usuario de la base de datos */
    private static final String USER = "stephany_user";

    /** Contraseña del usuario de la base de datos */
    private static final String PASSWORD = "stephanymb";

    /**
     * Obtiene una conexión activa a la base de datos.
     *
     * @return Connection activa si la conexión es exitosa, null en caso de error.
     */
    public static Connection getConnection() {
        try {
            // Carga del driver JDBC de MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Creación de la conexión
            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("Conexión exitosa a gestor_abbyac27");
            return con;

        } catch (ClassNotFoundException | SQLException e) {
            // Manejo de errores de conexión o carga del driver
            System.out.println("Error de conexión: " + e.getMessage());
            return null;
        }
    }
}

