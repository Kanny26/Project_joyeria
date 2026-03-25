package dao;

import config.ConexionDB;
import model.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la gestión de clientes en el sistema de joyería.
 * 
 * Proporciona operaciones CRUD sobre las tablas relacionadas con clientes:
 *   - Cliente: Información principal del cliente
 *   - Telefono_Cliente: Teléfonos de contacto asociados al cliente
 *   - Correo_Cliente: Correos electrónicos asociados al cliente
 * 
 * Este DAO es utilizado principalmente por los controladores durante el proceso
 * de registro de ventas, donde se requiere obtener o crear clientes rápidamente.
 * 
 * Implementa patrones como "obtener o crear" (get-or-create) para optimizar
 * el flujo de registro de ventas en el punto de venta.
 */
public class ClienteDAO {

    /**
     * Obtiene un cliente existente por nombre o lo crea junto con sus datos de contacto.
     * 
     * Este método implementa la lógica de negocio que busca primero si el cliente
     * ya existe en el sistema para evitar duplicados. Si no existe, lo crea junto
     * con sus teléfonos y correos en una transacción atómica.
     * 
     * El nombre se utiliza como clave lógica de búsqueda porque en el contexto
     * de joyería, los clientes frecuentemente son buscados por su nombre durante
     * el registro de ventas en el punto de venta.
     * 
     * @param nombre Nombre del cliente. Es obligatorio y se usa como identificador
     *               lógico para la búsqueda inicial.
     * @param telefono Número de teléfono del cliente. Es opcional; si es null o está
     *                 en blanco, no se inserta en Telefono_Cliente.
     * @param email Dirección de correo electrónico. Es opcional; si es null o está
     *              en blanco, no se inserta en Correo_Cliente.
     * @return El ID del cliente (cliente_id) ya sea existente o recién creado.
     * @throws Exception Si ocurre algún error durante la transacción de creación
     *                   del cliente. Las excepciones pueden incluir:
     *                   - SQLException: Problemas con la base de datos
     *                   - NullPointerException: Si nombre es null
     *                   La transacción se revierte automáticamente en caso de error.
     */
    public int registrarOObtenerCliente(String nombre, String telefono, String email) throws Exception {
        /*
         * Se obtiene una conexión a la base de datos usando el pool de conexiones
         * configurado en ConexionDB. El try-with-resources garantiza que la conexión
         * se cierre automáticamente al finalizar el bloque.
         */
        try (Connection con = ConexionDB.getConnection()) {

            // PASO 1: Buscar cliente existente por nombre exacto
            /*
             * Consulta SQL para verificar si el cliente ya está registrado.
             * 
             * Tabla involucrada:
             *   - Cliente: Tabla principal de clientes
             * 
             * Condiciones:
             *   - c.nombre = ? : Comparación exacta con el nombre proporcionado
             * 
             * LIMIT 1: Optimización que detiene la búsqueda al encontrar el primer
             *          resultado, ya que el nombre debería ser único en el sistema.
             * 
             * Esta consulta evita crear duplicados cuando el mismo cliente realiza
             * múltiples compras en diferentes momentos.
             */
            String sqlBuscar = """
                SELECT c.cliente_id
                FROM Cliente c
                WHERE c.nombre = ?
                LIMIT 1
                """;
            try (PreparedStatement ps = con.prepareStatement(sqlBuscar)) {
                /*
                 * El parámetro se establece con setString para prevenir inyección SQL.
                 * El nombre es tratado como dato, no como código ejecutable.
                 */
                ps.setString(1, nombre);
                try (ResultSet rs = ps.executeQuery()) {
                    /*
                     * Si se encuentra el cliente, se retorna inmediatamente su ID.
                     * No se necesita crear un nuevo registro.
                     */
                    if (rs.next()) return rs.getInt("cliente_id");
                }
            }

            // PASO 2: El cliente no existe, proceder a crearlo con transacción
            /*
             * Se desactiva el autocommit para manejar la transacción manualmente.
             * Esto garantiza que todas las operaciones (insert en Cliente,
             * Telefono_Cliente y Correo_Cliente) se ejecuten como una unidad atómica.
             * Si falla alguna, todas se revierten.
             */
            con.setAutoCommit(false);
            try {
                int clienteId;
                
                // 2.1 Insertar registro principal en tabla Cliente
                /*
                 * Consulta SQL para insertar un nuevo cliente.
                 * 
                 * Tabla: Cliente
                 * Campos insertados:
                 *   - nombre: Nombre del cliente (obligatorio)
                 *   - fecha_registro: Fecha actual usando función CURDATE() de MySQL
                 *   - estado: Valor 1 (activo) por defecto
                 * 
                 * Se utiliza Statement.RETURN_GENERATED_KEYS para obtener el ID
                 * autogenerado por la base de datos.
                 */
                String sqlCliente = """
                    INSERT INTO Cliente (nombre, fecha_registro, estado)
                    VALUES (?, CURDATE(), 1)
                    """;
                try (PreparedStatement ps = con.prepareStatement(sqlCliente, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, nombre);
                    ps.executeUpdate();
                    
                    /*
                     * Recupera el ID generado automáticamente por la base de datos.
                     * Es fundamental para asociar los datos de contacto.
                     */
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("No se generó cliente_id");
                        clienteId = rs.getInt(1);
                    }
                }

                // 2.2 Insertar teléfono si fue proporcionado
                /*
                 * Consulta SQL para insertar teléfono del cliente.
                 * 
                 * Tabla: Telefono_Cliente
                 * Campos insertados:
                 *   - telefono: Número de teléfono proporcionado
                 *   - cliente_id: ID del cliente recién creado (FK)
                 * 
                 * Solo se ejecuta si el teléfono no es null ni está en blanco,
                 * permitiendo que sea un campo opcional en el formulario.
                 */
                if (telefono != null && !telefono.isBlank()) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO Telefono_Cliente (telefono, cliente_id) VALUES (?, ?)")) {
                        ps.setString(1, telefono);
                        ps.setInt(2, clienteId);
                        ps.executeUpdate();
                    }
                }

                // 2.3 Insertar correo si fue proporcionado
                /*
                 * Consulta SQL para insertar correo electrónico del cliente.
                 * 
                 * Tabla: Correo_Cliente
                 * Campos insertados:
                 *   - email: Dirección de correo proporcionada
                 *   - cliente_id: ID del cliente recién creado (FK)
                 * 
                 * Al igual que el teléfono, el correo es opcional para el registro.
                 */
                if (email != null && !email.isBlank()) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO Correo_Cliente (email, cliente_id) VALUES (?, ?)")) {
                        ps.setString(1, email);
                        ps.setInt(2, clienteId);
                        ps.executeUpdate();
                    }
                }

                /*
                 * Confirma todas las operaciones realizadas.
                 * Solo llega a este punto si todas las inserciones fueron exitosas.
                 */
                con.commit();
                return clienteId;

            } catch (Exception e) {
                /*
                 * En caso de cualquier excepción durante la creación del cliente,
                 * se revierte toda la transacción para mantener la integridad
                 * referencial de la base de datos.
                 * 
                 * La excepción se relanza para que el controlador pueda manejarla
                 * y mostrar un mensaje apropiado al usuario.
                 */
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Lista todos los clientes registrados en el sistema junto con sus datos de contacto.
     * 
     * Este método recupera información combinada de las tablas Cliente,
     * Telefono_Cliente y Correo_Cliente en una sola consulta optimizada,
     * utilizando GROUP_CONCAT para agregar múltiples teléfonos y correos
     * en campos de texto concatenados.
     * 
     * La lista resultante está ordenada alfabéticamente por nombre del cliente
     * para facilitar la navegación en las interfaces de usuario.
     * 
     * @return Lista de objetos Cliente con todos sus datos. Si ocurre un error,
     *         retorna una lista vacía (no null) para evitar NullPointerException
     *         en las vistas.
     */
    public List<Cliente> listarClientes() {
        List<Cliente> lista = new ArrayList<>();
        
        /*
         * Consulta SQL principal que obtiene todos los clientes con sus contactos.
         * 
         * Tablas involucradas:
         *   - Cliente (c): Tabla principal con datos básicos del cliente
         *   - Telefono_Cliente (tc): Tabla de teléfonos (relación uno a muchos)
         *   - Correo_Cliente (cc): Tabla de correos (relación uno a muchos)
         * 
         * LEFT JOIN: Se utiliza LEFT JOIN en lugar de INNER JOIN para incluir
         *            clientes que no tienen teléfono o correo registrado.
         *            Esto es común en clientes nuevos o datos opcionales.
         * 
         * GROUP_CONCAT: Función de agregación de MySQL que concatena múltiples
         *               valores de teléfonos/correos en un solo string,
         *               separados por comas por defecto.
         * 
         * DISTINCT: Evita duplicados en la concatenación cuando un cliente tiene
         *           múltiples registros en las tablas de contacto.
         * 
         * GROUP BY: Agrupa los resultados por cliente_id para que GROUP_CONCAT
         *           funcione correctamente. Se incluyen todos los campos de Cliente
         *           para cumplir con el estándar SQL de GROUP BY.
         * 
         * ORDER BY: Ordena los resultados alfabéticamente por nombre para facilitar
         *           la visualización en listados.
         */
        String sql = """
            SELECT c.cliente_id, c.nombre, c.documento, c.estado, c.fecha_registro,
                   GROUP_CONCAT(DISTINCT tc.telefono) AS telefonos,
                   GROUP_CONCAT(DISTINCT cc.email)    AS correos
            FROM Cliente c
            LEFT JOIN Telefono_Cliente tc ON tc.cliente_id = c.cliente_id
            LEFT JOIN Correo_Cliente   cc ON cc.cliente_id = c.cliente_id
            GROUP BY c.cliente_id, c.nombre, c.documento, c.estado, c.fecha_registro
            ORDER BY c.nombre
            """;
        
        /*
         * Bloque try-with-resources que maneja automáticamente el cierre de:
         *   - Connection: Conexión a la base de datos
         *   - PreparedStatement: Consulta parametrizada
         *   - ResultSet: Resultados de la consulta
         */
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            /*
             * Itera sobre cada registro del ResultSet construyendo objetos Cliente.
             * Cada fila contiene la información combinada de un cliente con
             * sus teléfonos y correos ya concatenados.
             */
            while (rs.next()) {
                Cliente c = new Cliente();
                
                /*
                 * Mapeo de campos de la base de datos a los atributos del modelo.
                 * Los nombres de las columnas deben coincidir con los alias
                 * definidos en la consulta SQL.
                 */
                c.setClienteId(rs.getInt("cliente_id"));
                c.setNombre(rs.getString("nombre"));
                c.setDocumento(rs.getString("documento"));
                c.setEstado(rs.getBoolean("estado"));
                c.setFechaRegistro(rs.getString("fecha_registro"));
                
                /*
                 * Los campos 'telefonos' y 'correos' son cadenas concatenadas
                 * que pueden contener múltiples valores separados por comas,
                 * o null si el cliente no tiene datos de contacto.
                 */
                c.setTelefonos(rs.getString("telefonos"));
                c.setCorreos(rs.getString("correos"));
                
                lista.add(c);
            }
        } catch (Exception e) {
            /*
             * Manejo de errores de conexión o consulta.
             * Se imprime el stack trace para debugging en desarrollo.
             * En producción, esto debería reemplazarse con un logger
             * adecuado para no exponer detalles sensibles.
             * 
             * Se retorna lista vacía para que la interfaz de usuario
             * pueda mostrar un mensaje amigable en lugar de una excepción.
             */
            e.printStackTrace();
        }
        return lista;
    }
}