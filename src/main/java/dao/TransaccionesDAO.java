package dao;

import config.ConexionDB;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Clase base abstracta que proporciona infraestructura para operaciones
 * transaccionales atómicas en el sistema de joyería.
 * 
 * Esta clase encapsula el patrón de manejo de transacciones, asegurando que
 * las operaciones que involucran múltiples tablas se ejecuten de manera
 * atómica: todas las operaciones se completan con éxito o ninguna se aplica.
 * 
 * Principales responsabilidades:
 *   - Obtener conexiones de base de datos de forma controlada
 *   - Manejar el ciclo de vida de transacciones (begin, commit, rollback)
 *   - Gestionar errores de forma consistente
 *   - Asegurar el cierre adecuado de recursos
 * 
 * Patrón de diseño: Template Method + Strategy
 *   - La estructura de la transacción está fija en este método
 *   - La lógica específica se inyecta mediante la interfaz funcional
 *     OperacionTransaccional (Strategy Pattern)
 * 
 * Uso típico:
 *   boolean exito = TransaccionesDAO.ejecutarTransaccion(conexion -> {
 *       // Operaciones de base de datos aquí
 *       new ProductoDAO().guardar(producto, conexion);
 *       new InventarioDAO().registrarMovimiento(movimiento, conexion);
 *       return true; // Si todo salió bien
 *   });
 */
public abstract class TransaccionesDAO {
    
    /**
     * Interfaz funcional que define la operación a ejecutar dentro de una transacción.
     * 
     * Permite que el código cliente proporcione la lógica específica de negocio
     * mientras que esta clase se encarga del manejo transaccional.
     * 
     * Se utiliza como un bloque de código que recibe una conexión activa
     * y realiza operaciones de base de datos dentro de una transacción.
     */
    @FunctionalInterface
    public interface OperacionTransaccional {
        /**
         * Ejecuta la lógica de negocio dentro de la transacción.
         * 
         * @param conexion Conexión activa con autocommit desactivado.
         *                 Todas las operaciones dentro de este método usarán
         *                 esta misma conexión, compartiendo la transacción.
         * @return true si la operación fue exitosa y se debe hacer commit,
         *         false si hubo un error de lógica que requiere rollback.
         * @throws SQLException Si ocurre un error de base de datos que también
         *                      provocará rollback de la transacción.
         */
        boolean ejecutar(Connection conexion) throws SQLException;
    }
    
    /**
     * Ejecuta una operación dentro de una transacción de base de datos.
     * 
     * Este método maneja todo el ciclo de vida de la transacción:
     *   1. Obtiene una conexión de base de datos
     *   2. Desactiva autocommit (inicia transacción)
     *   3. Ejecuta la lógica proporcionada por el cliente
     *   4. Si tiene éxito: commit (confirma cambios)
     *   5. Si falla: rollback (revierte cambios)
     *   6. Restaura autocommit y cierra la conexión
     * 
     * Beneficios de este enfoque:
     *   - Centraliza el manejo de transacciones en un solo lugar
     *   - Evita código repetitivo en múltiples DAOs
     *   - Garantiza consistencia en el manejo de errores
     *   - Previene fugas de recursos (conexiones no cerradas)
     *   - Proporciona logging consistente de operaciones transaccionales
     * 
     * @param operacion Objeto lambda que contiene las operaciones de negocio
     *                  a ejecutar dentro de la transacción.
     * @return true si la transacción se completó exitosamente (commit ejecutado),
     *         false si hubo algún error que causó rollback.
     */
    protected static boolean ejecutarTransaccion(OperacionTransaccional operacion) {
        Connection conexion = null;
        try {
            // ─────────────────────────────────────────────────────────────
            // PASO 1: OBTENER CONEXIÓN DE BASE DE DATOS
            // ─────────────────────────────────────────────────────────────
            /*
             * Se intenta obtener una conexión del pool de conexiones
             * configurado en ConexionDB.
             * 
             * Manejo defensivo: Si getConnection() lanza excepción,
             * se captura y se retorna false inmediatamente, sin intentar
             * operaciones que dependan de la conexión.
             */
            try {
                conexion = ConexionDB.getConnection();
            } catch (Exception e) {
                System.err.println("Error obteniendo conexión: " + e.getMessage());
                return false;
            }

            // ─────────────────────────────────────────────────────────────
            // PASO 2: INICIAR TRANSACCIÓN
            // ─────────────────────────────────────────────────────────────
            /*
             * Desactivar autocommit: A partir de este punto, todos los cambios
             * quedarán pendientes hasta que se ejecute commit() o rollback().
             * Esto es el punto de inicio de la transacción.
             */
            conexion.setAutoCommit(false);
            
            // ─────────────────────────────────────────────────────────────
            // PASO 3: EJECUTAR LÓGICA DE NEGOCIO
            // ─────────────────────────────────────────────────────────────
            /*
             * Se invoca la lambda proporcionada por el cliente, pasándole
             * la conexión activa con autocommit desactivado.
             * 
             * La lambda puede contener múltiples operaciones de base de datos
             * (inserts, updates, deletes) que afectarán a diferentes tablas,
             * pero todas compartirán la misma transacción.
             */
            boolean resultado = operacion.ejecutar(conexion);
            
            // ─────────────────────────────────────────────────────────────
            // PASO 4: DECIDIR ENTRE COMMIT O ROLLBACK
            // ─────────────────────────────────────────────────────────────
            if (resultado) {
                /*
                 * La lógica de negocio fue exitosa (retornó true)
                 * Se confirman todos los cambios realizados durante la transacción.
                 * Una vez ejecutado commit(), los cambios son permanentes.
                 */
                conexion.commit();
                System.out.println("Transacción completada exitosamente");
                return true;
            } else {
                /*
                 * La lógica de negocio indicó fallo (retornó false)
                 * Se revierten todos los cambios realizados durante la transacción.
                 * Esto asegura que la base de datos quede en el estado anterior
                 * a la ejecución de la operación.
                 */
                conexion.rollback();
                System.out.println("Transacción revertida por error de lógica");
                return false;
            }
            
        } catch (SQLException e) {
            // ─────────────────────────────────────────────────────────────
            // MANEJO DE EXCEPCIONES DE BASE DE DATOS
            // ─────────────────────────────────────────────────────────────
            /*
             * Si ocurre una excepción SQL durante la ejecución de la lógica
             * de negocio (ej: violación de restricción, error de sintaxis,
             * timeout, deadlock), se ejecuta rollback para deshacer cualquier
             * cambio parcial que se haya realizado antes del error.
             */
            try {
                if (conexion != null) {
                    conexion.rollback();
                    System.err.println("Transacción revertida por excepción SQL: " + e.getMessage());
                }
            } catch (SQLException rollbackEx) {
                /*
                 * Caso crítico: No se pudo ejecutar el rollback.
                 * Esto puede deberse a que la conexión ya está cerrada
                 * o a un problema más grave con la base de datos.
                 * Se registra el error para diagnóstico, pero la transacción
                 * ya está comprometida o inconsistente.
                 */
                System.err.println("Error crítico en rollback: " + rollbackEx.getMessage());
            }
            return false;
            
        } finally {
            // ─────────────────────────────────────────────────────────────
            // PASO 5: LIMPIEZA DE RECURSOS
            // ─────────────────────────────────────────────────────────────
            /*
             * Este bloque se ejecuta siempre, tanto si la transacción fue exitosa
             * como si hubo error. Garantiza que la conexión se restaure a su
             * estado normal (autocommit activado) y se cierre correctamente.
             * 
             * Importancia del autocommit: Las conexiones en pool pueden ser
             * reutilizadas por otras operaciones. Si dejamos autocommit=false,
             * futuras operaciones que usen esta conexión (sin saberlo) estarían
             * dentro de una transacción sin haberla iniciado explícitamente.
             * Por eso es crítico restaurar autocommit=true antes de cerrar.
             */
            try {
                if (conexion != null) {
                    // Restaurar autocommit al valor por defecto (true)
                    conexion.setAutoCommit(true);
                    // Cerrar la conexión para devolverla al pool
                    conexion.close();
                }
            } catch (SQLException e) {
                // Error en limpieza: no crítico pero debe registrarse para monitoreo
                System.err.println("Error cerrando conexión: " + e.getMessage());
            }
        }
    }
}