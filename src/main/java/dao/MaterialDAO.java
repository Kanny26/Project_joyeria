package dao;

import config.ConexionDB;
import model.Material;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestión de materiales del catálogo de joyería.
 * Esta clase permite operaciones CRUD sobre la tabla Material, que define
 * los insumos base de los productos (oro, plata, acero, etc.).
 * Los materiales son datos clave para filtros comerciales en la interfaz
 * y para mantener consistencia en el inventario lógico del sistema.
 * Utiliza ConexionDB para gestión de conexiones y PreparedStatement para
 * prevenir SQL Injection y optimizar la ejecución de consultas.
 */
public class MaterialDAO {

    /**
     * Retorna todos los materiales registrados ordenados alfabéticamente por nombre.
     * Este método se utiliza para poblar listas desplegables y vistas de administración
     * donde se requiere mostrar los materiales disponibles de forma ordenada.
     *
     * Consulta SQL: SELECT simple sobre tabla Material
     * Tabla involucrada: Material (almacena los insumos base de productos de joyería)
     * Columnas seleccionadas:
     *   - material_id: PK de la entidad, identificador único autoincremental
     *   - nombre: nombre legible del material para mostrar en interfaces de usuario
     * Ordenamiento: ORDER BY nombre ASC para garantizar presentación alfabética consistente
     *
     * Manejo de excepciones: captura cualquier error y retorna lista vacía para evitar
     * fallos en la capa de presentación (JSP) al iterar sobre el resultado.
     *
     * @return lista de objetos Material con los datos recuperados, o lista vacía si ocurre un error
     */
    public List<Material> listarMateriales() {
        // Lista que almacenará cada material recuperado de la base de datos
        List<Material> lista = new ArrayList<>();
        // Consulta SQL: SELECT de columnas básicas de la tabla Material
        // Tabla: Material (entidad maestra de insumos para joyería)
        // Sin condiciones WHERE: recupera todos los materiales registrados
        // ORDER BY nombre ASC: garantiza orden alfabético para mejor experiencia de usuario
        String sql = "SELECT material_id, nombre FROM Material ORDER BY nombre ASC";
        // try-with-resources: garantiza cierre automático de Connection, PreparedStatement y ResultSet
        // previniendo fugas de recursos incluso si ocurre una excepción durante la ejecución
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            // Itera sobre cada fila del resultado para mapearla a un objeto Material
            while (rs.next()) {
                // Crea nueva instancia de Material para almacenar los datos de la fila actual
                Material m = new Material();
                // Mapea columna material_id (INT) al setter correspondiente del modelo
                m.setMaterialId(rs.getInt("material_id"));
                // Mapea columna nombre (VARCHAR) al setter del modelo
                m.setNombre(rs.getString("nombre"));
                // Agrega el objeto poblado a la lista de retorno
                lista.add(m);
            }
        } catch (Exception e) {
            // Captura cualquier excepción (SQL o de otro tipo) para no interrumpir el flujo del llamador
            // Imprime stack trace para diagnóstico en logs del servidor
            e.printStackTrace();
        }
        // Retorna la lista poblada; si hubo error o no hay datos, retorna lista vacía (nunca null)
        // Esto garantiza que la capa de presentación pueda iterar sin validar null
        return lista;
    }

    /**
     * Busca y retorna un material específico por su identificador único.
     * Método utilizado cuando se requiere cargar los datos de un material
     * para edición, validación o mostrar detalles en la interfaz.
     *
     * Consulta SQL: SELECT con cláusula WHERE por clave primaria
     * Tabla involucrada: Material
     * Condición: WHERE material_id = ? (búsqueda por PK, eficiente con índice)
     * Parámetro: id (int) → se binda al placeholder ? mediante PreparedStatement
     * Datos recuperados: material_id, nombre (campos básicos de la entidad)
     *
     * @param id valor de {@code material_id} a buscar
     * @return objeto Material con los datos encontrados, o {@code null} si no existe o hay error
     */
    public Material obtenerPorId(int id) {
        // Inicializa referencia a null; se asignará solo si se encuentra el registro
        Material m = null;
        // SQL: SELECT filtrado por clave primaria para recuperación eficiente de un solo registro
        // Tabla: Material
        // WHERE material_id = ?: condición parametrizada para prevenir SQL Injection
        // El parámetro se establece con ps.setInt(1, id) antes de ejecutar
        String sql = "SELECT material_id, nombre FROM Material WHERE material_id = ?";
        // try-with-resources para Connection y PreparedStatement
        // ResultSet se maneja en bloque interno para cierre explícito tras leer el único resultado esperado
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Bind del parámetro: asigna el valor de id al primer placeholder (?) de la consulta
            ps.setInt(1, id);
            // Ejecuta la consulta y obtiene ResultSet con máximo 1 fila (por PK)
            try (ResultSet rs = ps.executeQuery()) {
                // next() retorna true solo si existe al menos una fila en el resultado
                if (rs.next()) {
                    // Crea instancia de Material para mapear los datos recuperados
                    m = new Material();
                    // Mapea cada columna del ResultSet a los setters del modelo
                    m.setMaterialId(rs.getInt("material_id"));
                    m.setNombre(rs.getString("nombre"));
                }
                // Si rs.next() es false, m permanece null (material no encontrado)
            }
        } catch (Exception e) {
            // Captura y registra cualquier error para diagnóstico sin propagar excepción
            e.printStackTrace();
        }
        // Retorna el material encontrado o null si no existe / ocurrió error
        return m;
    }

    /**
     * Inserta un nuevo material en la base de datos.
     * Método utilizado para crear registros de insumos en el catálogo de joyería.
     *
     * Consulta SQL: INSERT en tabla Material
     * Tabla destino: Material (almacena insumos base para productos de joyería)
     * Columnas insertadas:
     *   - nombre: nombre legible del material (VARCHAR, único en esquema para evitar duplicados)
     * Valores parametrizados (?) para prevenir SQL Injection y permitir reutilización del plan
     * La PK material_id se genera automáticamente (AUTO_INCREMENT según motor MySQL)
     * Pre-procesamiento: se aplica trim() al nombre para eliminar espacios accidentales
     *
     * @param m objeto Material con nombre poblado para persistir
     * @return {@code true} si la inserción afectó al menos una fila, {@code false} en caso contrario
     * @throws Exception si falla el INSERT por errores de conexión, constraints de BD o sintaxis SQL
     */
    public boolean guardar(Material m) throws Exception {
        // SQL: INSERT para crear nuevo registro en tabla Material
        // Tabla destino: Material
        // Columnas: nombre (VARCHAR)
        // Valores parametrizados (?) para seguridad y eficiencia en ejecución
        // PK material_id se autogenera, no se incluye en la lista de columnas
        String sql = "INSERT INTO Material (nombre) VALUES (?)";
        // try-with-resources para gestión automática de Connection y PreparedStatement
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Bind parámetro 1: asigna nombre del material con trim() para limpieza de datos
            // trim() elimina espacios en blanco al inicio y fin, evitando inconsistencias visuales
            ps.setString(1, m.getNombre().trim());
            // Ejecuta el INSERT y retorna true si se insertó al menos una fila
            // executeUpdate() retorna el número de filas afectadas (esperado: 1 para INSERT exitoso)
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza el nombre de un material existente identificado por su ID.
     * Método utilizado para editar propiedades de materiales en el catálogo.
     *
     * Consulta SQL: UPDATE en tabla Material con cláusula WHERE por clave primaria
     * Tabla destino: Material
     * Columnas modificadas:
     *   - nombre: nuevo nombre legible del material (VARCHAR)
     * Condición de filtrado: WHERE material_id = ? (actualiza solo el registro con PK específica)
     * Parámetros en orden: 1=nombre, 2=materialId (para el WHERE)
     * Pre-procesamiento: se aplica trim() al nombre para mantener consistencia de datos
     *
     * @param m objeto Material con materialId y nombre poblados para actualizar
     * @return {@code true} si se actualizó al menos una fila (material existente), {@code false} si no se encontró
     * @throws Exception si falla el UPDATE por errores de conexión, constraints o sintaxis SQL
     */
    public boolean actualizar(Material m) throws Exception {
        // SQL: UPDATE para modificar registro existente en tabla Material
        // Tabla destino: Material
        // SET nombre = ?: asigna nuevo valor a la columna modificable
        // WHERE material_id = ?: filtra por PK para actualizar solo el registro específico
        // Parámetros: 1=nombre, 2=materialId (bindados en ese orden)
        String sql = "UPDATE Material SET nombre = ? WHERE material_id = ?";
        // try-with-resources para gestión automática de Connection y PreparedStatement
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Bind parámetro 1: asigna nuevo nombre con trim() para limpieza de datos
            ps.setString(1, m.getNombre().trim());
            // Bind parámetro 2: asigna ID de material al segundo placeholder (WHERE material_id = ?)
            ps.setInt(2, m.getMaterialId());
            // Ejecuta el UPDATE y retorna true si se modificó al menos una fila
            // executeUpdate() retorna número de filas afectadas (esperado: 1 si el material existe)
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Elimina un material por su ID con manejo especializado de restricciones de integridad.
     * Antes de eliminar, la base de datos verifica que no existan productos que referencien
     * este material mediante foreign key constraints. Si hay referencias, se lanza una excepción
     * con mensaje legible para el usuario final.
     *
     * Consulta SQL: DELETE en tabla Material con condición por clave primaria
     * Tabla afectada: Material
     * Condición: WHERE material_id = ? (elimina solo el registro con PK específica)
     * Restricción de integridad: la BD lanza SQLIntegrityConstraintViolationException (código 1451 en MySQL)
     * si existen productos que usan este material, evitando eliminación que rompa consistencia de datos
     * Retorno: true si se eliminó una fila, false si no se encontró el material
     *
     * Manejo de excepciones especializado:
     *   - SQLIntegrityConstraintViolationException: se captura y se relanza con mensaje humano
     *   - SQLException con código 1451: fallback por si el driver no usa la subclase específica
     *   - Otras SQLException: se propagan sin modificar para diagnóstico técnico
     *
     * @param id valor de {@code material_id} del material a eliminar
     * @return {@code true} si se eliminó exitosamente una fila, {@code false} si no existía el registro
     * @throws Exception con mensaje legible si hay productos que referencian el material,
     *                   o propaga el error SQL original para otros casos
     */
    public boolean eliminar(int id) throws Exception {
        // SQL: DELETE para remover registro de tabla Material
        // Tabla afectada: Material
        // WHERE material_id = ?: condición parametrizada para eliminar solo el registro específico
        // Parámetro bindado con ps.setInt(1, id) antes de ejecutar
        String sql = "DELETE FROM Material WHERE material_id = ?";
        // try-with-resources para gestión automática de Connection y PreparedStatement
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Bind del parámetro: asigna el ID de material al primer placeholder (?) de la consulta
            ps.setInt(1, id);
            // Ejecuta el DELETE y retorna true si se eliminó al menos una fila (registro encontrado)
            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            // Error 1451 en MySQL: se intenta eliminar un registro referenciado por otra tabla.
            // Aquí significa que uno o más productos tienen asignado este material.
            // Se captura la excepción específica y se relanza con mensaje legible para el usuario final
            throw new Exception("No se puede eliminar este material porque hay productos que lo usan. Primero reasigna o elimina esos productos.");
        } catch (java.sql.SQLException e) {
            // Fallback por si el driver no lanza SQLIntegrityConstraintViolationException directamente
            // Verifica el código de error SQL (1451 = foreign key constraint violation en MySQL)
            if (e.getErrorCode() == 1451) {
                // Traduce el error técnico a mensaje comprensible para experiencia de usuario
                throw new Exception("No se puede eliminar este material porque hay productos que lo usan. Primero reasigna o elimina esos productos.");
            }
            // Para otros errores SQL no manejados, propaga la excepción original sin modificar
            throw e;
        }
    }
}