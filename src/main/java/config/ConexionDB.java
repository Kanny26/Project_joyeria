package config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Gestiona la conexión a la base de datos MySQL.
 * Esta clase proporciona una conexión única configurada mediante un archivo de propiedades
 * o valores predeterminados para desarrollo local. Implementa un patrón de carga estática
 * que garantiza que la configuración esté disponible antes de cualquier intento de conexión.
 */
public class ConexionDB {
    private static String URL;
    private static String USER;
    private static String PASS;

    /**
     * Bloque estático de inicialización que se ejecuta una sola vez al cargar la clase.
     * Carga las credenciales desde db.properties si el archivo está disponible en el classpath.
     * En caso de no encontrar el archivo, utiliza valores por defecto para entorno de desarrollo local.
     * También registra el driver JDBC de MySQL para permitir las conexiones.
     * 
     * @throws ExceptionInInitializerError si ocurre algún error durante la inicialización,
     *         incluyendo problemas al cargar el archivo de propiedades o al registrar el driver.
     */
    static {
        try {
            Properties prop = new Properties();
            InputStream input = ConexionDB.class.getClassLoader().getResourceAsStream("db.properties");
            if (input != null) {
                prop.load(input);
                URL = prop.getProperty("db.url");
                USER = prop.getProperty("db.user");
                PASS = prop.getProperty("db.password");
            } else {
                // Valores de respaldo si no se encuentra el archivo de configuración
                URL = "jdbc:mysql://localhost:3306/gestor_abbyac27";
                USER = "root";
                PASS = ""; 
            }
            input.close();
            // Registra el driver de MySQL para poder establecer conexiones
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            // Si falla la inicialización, se lanza un error que detiene la aplicación
            throw new ExceptionInInitializerError("Error al cargar configuración de BD: " + e.getMessage());
        }
    }

    /**
     * Obtiene una nueva conexión activa a la base de datos utilizando la configuración cargada.
     * 
     * @return una conexión activa a la base de datos MySQL
     * @throws Exception si ocurre un error al establecer la conexión, incluyendo problemas
     *         de red, credenciales incorrectas o driver no disponible
     */
    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}