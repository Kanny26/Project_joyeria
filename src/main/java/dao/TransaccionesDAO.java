package dao;

import config.ConexionDB;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Base class para operaciones transaccionales atómicas
 */
public abstract class TransaccionesDAO {
    
    /**
     * Interfaz funcional para operaciones transaccionales
     */
    @FunctionalInterface
    public interface OperacionTransaccional {
        boolean ejecutar(Connection conexion) throws SQLException;
    }
    
    /**
     * Ejecutar operación dentro de una transacción
     * @param operacion Lambda con operaciones a ejecutar
     * @return true si tuvo éxito
     */
    protected static boolean ejecutarTransaccion(OperacionTransaccional operacion) {
        Connection conexion = null;
        try {
            // CORRECCIÓN: Ahora llama a getConnection() como está en tu clase config.ConexionDB
            // Se usa un try-catch interno o se lanza Exception porque tu método dice "throws Exception"
            try {
                conexion = ConexionDB.getConnection();
            } catch (Exception e) {
                System.err.println("Error obteniendo conexión: " + e.getMessage());
                return false;
            }

            conexion.setAutoCommit(false); // INICIO TRANSACCIÓN
            
            // Ejecutar operación
            boolean resultado = operacion.ejecutar(conexion);
            
            if (resultado) {
                conexion.commit(); // CONFIRMAR
                System.out.println("Transacción completada exitosamente");
                return true;
            } else {
                conexion.rollback(); // REVERTIR
                System.out.println("Transacción revertida por error de lógica");
                return false;
            }
            
        } catch (SQLException e) {
            try {
                if (conexion != null) {
                    conexion.rollback(); // REVERTIR EN EXCEPCIÓN
                    System.err.println("Transacción revertida por excepción SQL: " + e.getMessage());
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error crítico en rollback: " + rollbackEx.getMessage());
            }
            return false;
            
        } finally {
            try {
                if (conexion != null) {
                    // Buena práctica: devolver el autocommit a true antes de cerrar
                    conexion.setAutoCommit(true);
                    conexion.close();
                }
            } catch (SQLException e) {
                System.err.println("Error cerrando conexión: " + e.getMessage());
            }
        }
    }
}