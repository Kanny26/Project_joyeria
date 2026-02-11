package main.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase para gestionar la conexión a la base de datos MySQL
 */
public class DatabaseConfig {
    
    // Configuración de la base de datos
    private static final String URL = "jdbc:mysql://localhost:3306/gestor_abbyac27";
    private static final String USER = "root";
    private static final String PASSWORD = "MYSQLKey2025";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    
    // Instancia única
    private static DatabaseConfig instance;
    private Connection connection;
    
    /**
     * Constructor privado para implementar el patrón Singleton
     */
    private DatabaseConfig() {
        try {
            // Cargar el driver de MySQL
            Class.forName(DRIVER);
            System.out.println("Driver MySQL cargado correctamente");
        } catch (ClassNotFoundException e) {
            System.err.println("Error al cargar el driver de MySQL: " + e.getMessage());
            throw new RuntimeException("No se pudo cargar el driver de MySQL", e);
        }
    }
    
    /**
     * Obtiene la instancia única de DatabaseConfig (Singleton)
     */
    public static DatabaseConfig getInstance() {
        if (instance == null) {
            synchronized (DatabaseConfig.class) {
                if (instance == null) {
                    instance = new DatabaseConfig();
                }
            }
        }
        return instance;
    }
    
    /**
     * Obtiene una conexión activa a la base de datos
     * @return Connection objeto de conexión
     * @throws SQLException si hay error al conectar
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexión a base de datos establecida");
        }
        return connection;
    }
    
    /**
     * Cierra la conexión a la base de datos
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Conexión cerrada correctamente");
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
    
    /**
     * Método para probar la conexión
     */
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("Prueba de conexión exitosa");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error en prueba de conexión: " + e.getMessage());
        }
        return false;
    }
}