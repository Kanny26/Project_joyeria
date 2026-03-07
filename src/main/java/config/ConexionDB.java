package config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class ConexionDB {
    private static String URL;
    private static String USER;
    private static String PASS;

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
                URL = "jdbc:mysql://localhost:3306/gestor_abbyac27";
                USER = "root";
                PASS = ""; 
            }
            input.close();
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Error al cargar configuración de BD: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}