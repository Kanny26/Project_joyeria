package main.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Clase para probar la conexión a la base de datos
 */
public class testconnection {
    
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("PRUEBA DE CONEXIÓN A BASE DE DATOS");
        System.out.println("=================================\n");
        
        DatabaseConfig dbConfig = DatabaseConfig.getInstance();
        
        try {
            // Probar conexión
            Connection conn = dbConfig.getConnection();
            
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ CONEXIÓN EXITOSA\n");
                
                // Hacer una consulta simple para verificar
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM Usuario");
                
                if (rs.next()) {
                    int totalUsuarios = rs.getInt("total");
                    System.out.println("📊 Total de usuarios en BD: " + totalUsuarios);
                }
                
                rs.close();
                stmt.close();
                
                System.out.println("\n✓ Base de datos 'gestor_abbyac27' funcionando correctamente");
                
            } else {
                System.out.println("✗ ERROR: No se pudo establecer la conexión");
            }
            
        } catch (Exception e) {
            System.err.println("✗ ERROR AL CONECTAR:");
            System.err.println("   " + e.getMessage());
            e.printStackTrace();
        } finally {
            dbConfig.closeConnection();
        }
        
        System.out.println("\n=================================");
        System.out.println("FIN DE LA PRUEBA");
        System.out.println("=================================");
    }
}