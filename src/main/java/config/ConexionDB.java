package config;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConexionDB {

    // ← Cambia solo estos tres valores en cada PC
    private static final String URL  = "jdbc:mysql://localhost:3306/gestor_abbyac27";
    private static final String USER = "root";
    private static final String PASS = "MYSQLKey2025";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Driver MySQL no encontrado: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}