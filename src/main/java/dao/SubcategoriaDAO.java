package dao;

import config.ConexionDB;
import model.Subcategoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la gestión de subcategorías en el sistema de joyería.
 * 
 * Las subcategorías permiten una clasificación más fina de los productos dentro
 * de cada categoría principal (ej: dentro de "Anillos" puede haber subcategorías
 * como "Compromiso", "Aniversario", "Uso Diario").
 * 
 * Este DAO maneja la relación N:M entre productos y subcategorías a través de
 * la tabla puente Producto_Subcategoria, manteniendo la integridad referencial
 * y evitando datos huérfanos mediante ON DELETE CASCADE.
 * 
 * Es utilizado principalmente en:
 *   - Módulo de administración de catálogo (CRUD de subcategorías)
 *   - Formularios de producto (selección múltiple de subcategorías)
 *   - Búsquedas avanzadas (filtro por subcategoría)
 */
public class SubcategoriaDAO {

    /**
     * Recupera todas las subcategorías registradas en el sistema.
     * 
     * La lista se ordena alfabéticamente para facilitar la navegación
     * en formularios y listados desplegables.
     * 
     * @return Lista de objetos Subcategoria con todos los registros.
     *         Puede estar vacía si no hay subcategorías configuradas.
     *         En caso de error, retorna lista vacía y registra el error.
     */
    public List<Subcategoria> listarTodas() {
        List<Subcategoria> lista = new ArrayList<>();
        /*
         * Consulta SQL simple que obtiene todas las subcategorías.
         * 
         * Tabla: Subcategoria
         * Campos seleccionados:
         *   - subcategoria_id: Identificador único de la subcategoría
         *   - nombre: Nombre descriptivo de la subcategoría
         * 
         * ORDER BY nombre ASC: Ordenación alfabética para presentación consistente
         * en interfaces de usuario (combos, listas, etc.).
         */
        String sql = "SELECT subcategoria_id, nombre FROM Subcategoria ORDER BY nombre ASC";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Subcategoria s = new Subcategoria();
                s.setSubcategoriaId(rs.getInt("subcategoria_id"));
                s.setNombre(rs.getString("nombre"));
                lista.add(s);
            }
        } catch (Exception e) {
            /*
             * Manejo defensivo de errores: se registra el error pero no se relanza,
             * retornando lista vacía para que la interfaz pueda mostrar un mensaje
             * amigable sin romper la aplicación.
             * En producción, esto debería usar un logger adecuado.
             */
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Busca una subcategoría específica por su identificador único.
     * 
     * Útil cuando se tiene el ID de una subcategoría almacenada en un producto
     * y se necesitan sus detalles para mostrarlos en la interfaz.
     * 
     * @param id Identificador único de la subcategoría (subcategoria_id)
     * @return Objeto Subcategoria con los datos del registro encontrado,
     *         o null si no existe una subcategoría con ese ID.
     */
    public Subcategoria obtenerPorId(int id) {
        Subcategoria s = null;
        /*
         * Consulta SQL parametrizada para buscar subcategoría por ID.
         * 
         * Tabla: Subcategoria
         * 
         * Condición: WHERE subcategoria_id = ? : Filtra por el identificador único
         * 
         * Uso de PreparedStatement para prevenir SQL Injection y optimizar
         * la ejecución cuando se realiza múltiples veces con diferentes IDs.
         */
        String sql = "SELECT subcategoria_id, nombre FROM Subcategoria WHERE subcategoria_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                /*
                 * Si se encuentra un registro (rs.next() true), se construye el objeto
                 * con los datos recuperados. Si no existe, s permanece null.
                 */
                if (rs.next()) {
                    s = new Subcategoria();
                    s.setSubcategoriaId(rs.getInt("subcategoria_id"));
                    s.setNombre(rs.getString("nombre"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * Verifica si una subcategoría tiene productos activos asociados.
     * 
     * Esta validación es crítica antes de eliminar una subcategoría,
     * para evitar violaciones de integridad referencial o dejar productos
     * sin clasificación adecuada.
     * 
     * CAMBIO IMPORTANTE: Anteriormente se buscaba en Producto.subcategoria_id
     * (columna que ya no existe). Ahora la relación es N:M a través de la
     * tabla puente Producto_Subcategoria, por lo que esta consulta refleja
     * correctamente la nueva estructura de datos.
     * 
     * @param subcategoriaId Identificador de la subcategoría a verificar
     * @return true si existe al menos un producto activo (estado = 1) asociado
     *         a esta subcategoría, false en caso contrario.
     * @throws Exception Si falla la conexión o ejecución de la consulta
     */
    public boolean tieneProductosActivos(int subcategoriaId) throws Exception {
        /*
         * Consulta SQL que verifica la existencia de productos activos asociados.
         * 
         * Tablas involucradas:
         *   - Producto_Subcategoria (ps): Tabla puente que relaciona productos y subcategorías
         *   - Producto (p): Tabla principal de productos, contiene el estado
         * 
         * JOIN: INNER JOIN entre Producto_Subcategoria y Producto para obtener
         *       solo productos que existen y podemos verificar su estado.
         * 
         * Condiciones:
         *   - ps.subcategoria_id = ?: Filtra por la subcategoría específica
         *   - p.estado = 1: Solo considera productos activos (ignora productos dados de baja)
         * 
         * COUNT(*): Cuenta cuántos productos activos tienen esta subcategoría.
         * Si el resultado es > 0, significa que la subcategoría está en uso.
         * 
         * Esta consulta es más precisa que antes, ya que considera solo productos activos,
         * permitiendo eliminar subcategorías que solo estén asociadas a productos inactivos.
         */
        String sql = """
            SELECT COUNT(*)
            FROM Producto_Subcategoria ps
            INNER JOIN Producto p ON p.producto_id = ps.producto_id
            WHERE ps.subcategoria_id = ? AND p.estado = 1
            """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subcategoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Inserta una nueva subcategoría en el sistema.
     * 
     * @param s Objeto Subcategoria que contiene el nombre a guardar.
     *          El ID se genera automáticamente por la base de datos.
     * @return true si se insertó al menos un registro (operación exitosa),
     *         false si no se insertó ninguna fila.
     * @throws Exception Si falla la inserción por problemas de conexión
     *                   o violación de restricciones (ej: nombre duplicado)
     */
    public boolean guardar(Subcategoria s) throws Exception {
        /*
         * Consulta SQL de inserción en tabla Subcategoria.
         * 
         * Tabla: Subcategoria
         * Campo insertado:
         *   - nombre: Nombre descriptivo de la subcategoría
         * 
         * Nota: subcategoria_id es auto-incremental y no se especifica aquí.
         */
        String sql = "INSERT INTO Subcategoria (nombre) VALUES (?)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            /*
             * trim() elimina espacios en blanco al inicio y al final del nombre,
             * evitando que se guarden strings con espacios accidentales.
             * Ejemplo: "  Compromiso  " se guarda como "Compromiso"
             */
            ps.setString(1, s.getNombre().trim());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza el nombre de una subcategoría existente.
     * 
     * @param s Objeto Subcategoria que debe contener:
     *           - subcategoria_id: Identificador del registro a actualizar
     *           - nombre: Nuevo nombre que reemplazará al existente
     * @return true si se actualizó al menos un registro (el ID existe),
     *         false si no se encontró el registro a actualizar.
     * @throws Exception Si falla la actualización por problemas de conexión
     *                   o violación de restricciones.
     */
    public boolean actualizar(Subcategoria s) throws Exception {
        /*
         * Consulta SQL de actualización.
         * 
         * Tabla: Subcategoria
         * Campos actualizados:
         *   - nombre: Se establece al nuevo valor proporcionado
         * 
         * Condición: WHERE subcategoria_id = ? : Identifica el registro específico
         * a modificar mediante su ID único.
         */
        String sql = "UPDATE Subcategoria SET nombre = ? WHERE subcategoria_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getNombre().trim());
            ps.setInt(2, s.getSubcategoriaId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Elimina una subcategoría del sistema.
     * 
     * PRECAUCIÓN: Este método realiza validaciones previas para garantizar
     * que la eliminación no viole la integridad referencial:
     *   1. Verifica si hay productos activos asociados (mediante tieneProductosActivos)
     *   2. Si hay productos activos, lanza excepción informativa
     *   3. Si no hay productos activos, procede con la eliminación
     * 
     * INTEGRIDAD REFERENCIAL:
     *   La tabla Producto_Subcategoria tiene definido ON DELETE CASCADE,
     *   lo que significa que al eliminar una subcategoría, MySQL automáticamente
     *   elimina todas las filas de Producto_Subcategoria que hacían referencia
     *   a ella, sin dejar datos huérfanos.
     * 
     * MANEJO DE EXCEPCIONES:
     *   Se capturan específicamente SQLIntegrityConstraintViolationException
     *   y errores MySQL con código 1451 (foreign key constraint fails) para
     *   proporcionar mensajes de error amigables al usuario.
     * 
     * @param id Identificador único de la subcategoría a eliminar (subcategoria_id)
     * @return true si se eliminó al menos un registro (el ID existía y no tenía
     *         productos activos asociados), false si no se encontró el registro.
     * @throws Exception Si la subcategoría tiene productos activos asociados,
     *                   o si ocurre un error de base de datos no manejado.
     */
    public boolean eliminar(int id) throws Exception {
        /*
         * Validación de negocio: No se permite eliminar subcategorías que
         * estén siendo utilizadas por productos activos en el sistema.
         * Esto previene que productos queden sin clasificación adecuada
         * y mantiene la consistencia de los datos.
         */
        if (tieneProductosActivos(id)) {
            throw new Exception(
                "No se puede eliminar esta subcategoría porque tiene productos activos asociados. "
                + "Primero reasigna o elimina esos productos."
            );
        }
        
        /*
         * Consulta SQL de eliminación.
         * 
         * Tabla: Subcategoria
         * 
         * Condición: WHERE subcategoria_id = ? : Identifica el registro a eliminar
         * 
         * NOTA DE INTEGRIDAD: Gracias a ON DELETE CASCADE en Producto_Subcategoria,
         * las relaciones con productos inactivos se eliminan automáticamente.
         * Solo se verifica productos activos en la validación previa.
         */
        String sql = "DELETE FROM Subcategoria WHERE subcategoria_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            /*
             * Captura específica para violaciones de integridad referencial.
             * Aunque la validación previa debería prevenir esto, puede ocurrir
             * si hay productos inactivos asociados o si la validación falla.
             */
            throw new Exception(
                "No se puede eliminar esta subcategoría porque está siendo usada en el sistema."
            );
        } catch (SQLException e) {
            /*
             * Captura específica para MySQL error 1451:
             * Cannot delete or update a parent row: a foreign key constraint fails
             * Este es el código de error estándar de MySQL para violación de FK.
             */
            if (e.getErrorCode() == 1451) {
                throw new Exception(
                    "No se puede eliminar esta subcategoría porque está siendo usada en el sistema."
                );
            }
            // Si es otro error SQL, se relanza para que el controlador lo maneje
            throw e;
        }
    }
}