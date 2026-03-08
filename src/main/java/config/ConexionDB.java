package config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Gestiona la conexión a la base de datos MySQL.
 * Carga la configuración desde un archivo properties o usa valores por defecto.
 */
public class ConexionDB {
    private static String URL;
    private static String USER;
    private static String PASS;

    // Bloque estático: se ejecuta una sola vez al cargar la clase.
    // Carga las credenciales desde db.properties si está disponible,
    // sino usa valores por defecto para entorno de desarrollo local.
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

    // Método público para obtener una nueva conexión a la base de datos
    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}