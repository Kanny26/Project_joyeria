package dao;

import config.ConexionDB;
import model.MetodoPago;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la gestión de métodos de pago en el sistema de joyería.
 * 
 * Administra las opciones de cobro y abono disponibles en los módulos de ventas y compras,
 * proporcionando operaciones CRUD completas sobre la tabla Metodo_Pago.
 * 
 * Los métodos de pago son catálogos esenciales que definen cómo los clientes pueden
 * cancelar sus compras (efectivo, tarjeta, transferencia, etc.) y cómo la empresa
 * paga a sus proveedores.
 * 
 * Este DAO es utilizado por los controladores de configuración y por los formularios
 * de venta/compra que requieren listar las opciones de pago disponibles.
 */
public class MetodoPagoDAO {

    /**
     * Recupera todos los métodos de pago registrados en el sistema.
     * 
     * La lista se ordena alfabéticamente por nombre para facilitar la navegación
     * en los formularios de selección (combobox, listas desplegables).
     * 
     * @return Lista de objetos MetodoPago con todos los registros de la tabla.
     *         Puede estar vacía si no hay métodos de pago configurados.
     * @throws Exception Si ocurre un error de conexión o ejecución de la consulta.
     */
    public List<MetodoPago> listarTodos() throws Exception {
        List<MetodoPago> lista = new ArrayList<>();
        
        /*
         * Consulta SQL que obtiene todos los métodos de pago.
         * 
         * Tabla: Metodo_Pago
         * Campos seleccionados:
         *   - metodo_pago_id: Identificador único del método de pago
         *   - nombre: Nombre descriptivo del método (Efectivo, Tarjeta, etc.)
         * 
         * ORDER BY nombre ASC: Ordenación alfabética para presentación consistente
         * en interfaces de usuario.
         */
        String sql = "SELECT metodo_pago_id, nombre FROM Metodo_Pago ORDER BY nombre ASC";

        /*
         * El bloque try-with-resources garantiza el cierre automático de recursos:
         *   - Connection: Conexión a la base de datos obtenida del pool
         *   - PreparedStatement: Consulta preparada (aunque no tiene parámetros)
         *   - ResultSet: Resultados de la consulta
         * 
         * Todos se cierran automáticamente al salir del bloque, incluso si ocurre
         * una excepción, previniendo fugas de recursos.
         */
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            /*
             * Itera sobre cada fila del ResultSet construyendo objetos MetodoPago.
             * Cada iteración agrega un nuevo método de pago a la lista de resultados.
             */
            while (rs.next()) {
                MetodoPago mp = new MetodoPago();
                mp.setMetodoPagoId(rs.getInt("metodo_pago_id"));
                mp.setNombre(rs.getString("nombre"));
                lista.add(mp);
            }
        }
        return lista;
    }

    /**
     * Busca un método de pago específico por su identificador único.
     * 
     * Este método es útil cuando se tiene el ID de un método de pago almacenado
     * en una venta o compra y se necesita recuperar sus detalles para mostrarlos
     * en la interfaz de usuario.
     * 
     * @param id Identificador único del método de pago (metodo_pago_id)
     * @return Objeto MetodoPago con los datos del registro encontrado,
     *         o null si no existe un método de pago con ese ID.
     * @throws Exception Si ocurre un error de conexión o ejecución de la consulta.
     */
    public MetodoPago obtenerPorId(int id) throws Exception {
        MetodoPago mp = null;
        
        /*
         * Consulta SQL parametrizada para buscar un método de pago por su ID.
         * 
         * Tabla: Metodo_Pago
         * 
         * Condición: metodo_pago_id = ? : Filtra por el identificador único
         * 
         * Uso de PreparedStatement con parámetro para prevenir SQL Injection,
         * aunque el ID generalmente viene de fuentes confiables (selección de combo).
         */
        String sql = "SELECT metodo_pago_id, nombre FROM Metodo_Pago WHERE metodo_pago_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            /*
             * Asigna el ID proporcionado al placeholder '?' de la consulta.
             * Esto permite reutilizar la misma estructura de consulta con diferentes IDs.
             */
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                /*
                 * Si se encuentra un registro (rs.next() true), se construye el objeto
                 * con los datos recuperados. Si no existe, mp permanece null.
                 */
                if (rs.next()) {
                    mp = new MetodoPago();
                    mp.setMetodoPagoId(rs.getInt("metodo_pago_id"));
                    mp.setNombre(rs.getString("nombre"));
                }
            }
        }
        return mp;
    }

    /**
     * Inserta un nuevo método de pago en la base de datos.
     * 
     * Este método se utiliza en el módulo de administración para agregar nuevas
     * formas de pago que la empresa acepta (ej: nuevos procesadores de pago,
     * criptomonedas, etc.).
     * 
     * @param mp Objeto MetodoPago que contiene el nombre del método a guardar.
     *           El ID se genera automáticamente por la base de datos.
     * @return true si se insertó al menos un registro (operación exitosa),
     *         false si no se insertó ninguna fila (caso improbable con datos válidos).
     * @throws Exception Si falla la inserción por problemas de conexión,
     *                   violación de restricciones, o datos inválidos.
     */
    public boolean guardar(MetodoPago mp) throws Exception {
        /*
         * Consulta SQL de inserción en la tabla Metodo_Pago.
         * 
         * Tabla: Metodo_Pago
         * Campo insertado:
         *   - nombre: Nombre descriptivo del método de pago (ej: "Tarjeta Débito")
         * 
         * Nota: metodo_pago_id es auto-incremental y no se especifica aquí.
         */
        String sql = "INSERT INTO Metodo_Pago (nombre) VALUES (?)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            /*
             * trim() elimina espacios en blanco al inicio y al final del nombre,
             * evitando que se guarden strings con espacios accidentales.
             * Ejemplo: "  Efectivo  " se guarda como "Efectivo"
             */
            ps.setString(1, mp.getNombre().trim());
            
            /*
             * executeUpdate() retorna el número de filas afectadas.
             * En un INSERT exitoso, siempre debe ser 1.
             * Se retorna true si se afectó al menos una fila.
             */
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza el nombre de un método de pago existente.
     * 
     * Permite modificar la descripción de un método de pago cuando cambian
     * las condiciones comerciales o se requiere una nomenclatura más clara.
     * 
     * @param mp Objeto MetodoPago que debe contener:
     *           - metodo_pago_id: Identificador del registro a actualizar
     *           - nombre: Nuevo nombre que reemplazará al existente
     * @return true si se actualizó al menos un registro (el ID existe),
     *         false si no se encontró el registro a actualizar.
     * @throws Exception Si falla la actualización por problemas de conexión
     *                   o datos inválidos.
     */
    public boolean actualizar(MetodoPago mp) throws Exception {
        /*
         * Consulta SQL de actualización.
         * 
         * Tabla: Metodo_Pago
         * Campos actualizados:
         *   - nombre: Se establece al nuevo valor proporcionado
         * 
         * Condición: WHERE metodo_pago_id = ? : Identifica el registro específico
         * a modificar mediante su ID único.
         */
        String sql = "UPDATE Metodo_Pago SET nombre = ? WHERE metodo_pago_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            /*
             * Se asignan los parámetros en el orden de los placeholders '?':
             *   1. Nuevo nombre (trim para limpiar espacios)
             *   2. ID del método de pago a actualizar
             */
            ps.setString(1, mp.getNombre().trim());
            ps.setInt(2, mp.getMetodoPagoId());
            
            /*
             * executeUpdate() retorna el número de filas afectadas.
             * Si el ID existe, actualiza 1 fila y retorna true.
             * Si el ID no existe, actualiza 0 filas y retorna false.
             */
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Elimina un método de pago del sistema por su identificador.
     * 
     * PRECAUCIÓN: Este método puede fallar si el método de pago está siendo utilizado
     * en alguna venta o compra registrada en el sistema, debido a la restricción
     * de clave foránea definida en la base de datos.
     * 
     * La integridad referencial protege la consistencia de los datos históricos,
     * evitando que se eliminen métodos de pago que ya fueron utilizados en transacciones.
     * 
     * @param id Identificador único del método de pago a eliminar (metodo_pago_id)
     * @return true si se eliminó al menos un registro (el ID existía y no tenía
     *         dependencias), false si no se encontró el registro.
     * @throws Exception Si falla la eliminación por problemas de conexión,
     *                   o si existe una restricción de clave foránea (SQLException
     *                   con código de violación de integridad referencial).
     */
    public boolean eliminar(int id) throws Exception {
        /*
         * Consulta SQL de eliminación.
         * 
         * Tabla: Metodo_Pago
         * 
         * Condición: WHERE metodo_pago_id = ? : Identifica el registro a eliminar
         * 
         * NOTA DE INTEGRIDAD: Si este ID está referenciado en las tablas:
         *   - Venta (como método de pago utilizado)
         *   - Compra (como método de pago utilizado)
         * La base de datos lanzará una excepción de violación de clave foránea,
         * previniendo la eliminación y manteniendo la consistencia histórica.
         */
        String sql = "DELETE FROM Metodo_Pago WHERE metodo_pago_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            /*
             * Asigna el ID del método de pago a eliminar al placeholder.
             */
            ps.setInt(1, id);
            
            /*
             * executeUpdate() retorna el número de filas eliminadas.
             * Si el ID existe y no tiene dependencias, elimina 1 fila y retorna true.
             * Si el ID no existe, elimina 0 filas y retorna false.
             * 
             * Si existen dependencias, se lanza SQLException y debe ser manejada
             * por el controlador para mostrar un mensaje amigable al usuario.
             */
            return ps.executeUpdate() > 0;
        }
    }
}