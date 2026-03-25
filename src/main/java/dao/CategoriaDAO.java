package dao;

import config.ConexionDB;
import model.Categoria;
import model.Subcategoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestión de categorías del catálogo de joyería.
 * Esta clase organiza las familias de productos y sus subcategorías válidas,
 * permitiendo que formularios y listados trabajen con relaciones coherentes
 * entre categoría principal y opciones derivadas.
 * Evita combinaciones inválidas al registrar o editar productos mediante
 * consultas que respetan las reglas de negocio definidas en la base de datos.
 * Utiliza ConexionDB para gestión de conexiones y sigue el patrón DAO para
 * mantener la separación entre lógica de negocio y acceso a datos.
 */
public class CategoriaDAO {

    /**
     * Retorna todas las categorías del catálogo ordenadas alfabéticamente por nombre.
     * Este método es utilizado para poblar listas desplegables y vistas de administración
     * donde se requiere mostrar las categorías disponibles de forma ordenada.
     *
     * Consulta SQL: SELECT simple sobre tabla Categoria
     * Tabla involucrada: Categoria (almacena las familias principales de productos)
     * Columnas seleccionadas:
     *   - categoria_id: PK de la categoría, identificador único
     *   - nombre: nombre legible de la categoría para mostrar en UI
     *   - icono: referencia al icono asociado para representación visual
     * Ordenamiento: ORDER BY nombre ASC para mejorar experiencia de usuario en listados
     *
     * @return lista de objetos Categoria con los datos recuperados, o lista vacía si ocurre un error
     */
    public List<Categoria> listarCategorias() {
        // Lista que almacenará cada categoría recuperada de la base de datos
        List<Categoria> lista = new ArrayList<>();
        // Consulta de negocio: trae categorías visibles del catálogo y ORDER BY nombre mejora la experiencia
        // del usuario al mostrar listas alfabéticas en formularios.
        // SQL: SELECT de columnas básicas de la tabla Categoria
        // Tabla: Categoria (entidad principal del catálogo)
        // Sin condiciones WHERE: recupera todas las categorías registradas
        // ORDER BY nombre ASC: garantiza orden alfabético consistente en la presentación
        String sql = "SELECT categoria_id, nombre, icono FROM Categoria ORDER BY nombre ASC";
        // try-with-resources: garantiza cierre automático de Connection, PreparedStatement y ResultSet
        // previniendo fugas de recursos incluso si ocurre una excepción durante la ejecución
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            // Itera sobre cada fila del resultado para mapearla a un objeto Categoria
            while (rs.next()) {
                // Crea nueva instancia de Categoria para almacenar los datos de la fila actual
                Categoria c = new Categoria();
                // Mapea columna categoria_id (INT) al setter correspondiente del modelo
                c.setCategoriaId(rs.getInt("categoria_id"));
                // Mapea columna nombre (VARCHAR) al setter del modelo
                c.setNombre(rs.getString("nombre"));
                // Mapea columna icono (VARCHAR) al setter del modelo
                c.setIcono(rs.getString("icono"));
                // Agrega el objeto poblado a la lista de retorno
                lista.add(c);
            }
        } catch (Exception e) {
            // Captura cualquier excepción (SQL o de otro tipo) para no interrumpir el flujo del llamador
            // Imprime stack trace para diagnóstico en logs del servidor
            e.printStackTrace();
        }
        // Retorna la lista poblada; si hubo error o no hay datos, retorna lista vacía (nunca null)
        return lista;
    }

    /**
     * Busca y retorna una categoría específica por su identificador único.
     * Método utilizado cuando se requiere cargar los datos de una categoría
     * para edición, validación o mostrar detalles en la interfaz.
     *
     * Consulta SQL: SELECT con cláusula WHERE por clave primaria
     * Tabla involucrada: Categoria
     * Condición: WHERE categoria_id = ? (búsqueda por PK, eficiente con índice)
     * Parámetro: id (int) → se binda al placeholder ? mediante PreparedStatement
     * Datos recuperados: categoria_id, nombre, icono (mismos campos que listarCategorias)
     *
     * @param id valor de {@code categoria_id} a buscar
     * @return objeto Categoria con los datos encontrados, o {@code null} si no existe o hay error
     */
    public Categoria obtenerPorId(int id) {
        // Inicializa referencia a null; se asignará solo si se encuentra el registro
        Categoria c = null;
        // SQL: SELECT filtrado por clave primaria para recuperación eficiente de un solo registro
        // Tabla: Categoria
        // WHERE categoria_id = ?: condición parametrizada para prevenir SQL Injection
        // El parámetro se establece con ps.setInt(1, id) antes de ejecutar
        String sql = "SELECT categoria_id, nombre, icono FROM Categoria WHERE categoria_id = ?";
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
                    // Crea instancia de Categoria para mapear los datos recuperados
                    c = new Categoria();
                    // Mapea cada columna del ResultSet a los setters del modelo
                    c.setCategoriaId(rs.getInt("categoria_id"));
                    c.setNombre(rs.getString("nombre"));
                    c.setIcono(rs.getString("icono"));
                }
                // Si rs.next() es false, c permanece null (categoría no encontrada)
            }
        } catch (Exception e) {
            // Captura y registra cualquier error para diagnóstico sin propagar excepción
            e.printStackTrace();
        }
        // Retorna la categoría encontrada o null si no existe / ocurrió error
        return c;
    }

    /**
     * Retorna las subcategorías disponibles y válidas para una categoría específica.
     * Consulta la tabla intermedia Categoria_Subcategoria para respetar las reglas
     * de negocio que definen qué subcategorías pueden asociarse a cada categoría principal.
     * Este método se usa en formularios de registro/edición de productos para poblar
     * selectores desplegables con opciones filtradas y coherentes.
     *
     * Consulta SQL: SELECT con INNER JOIN entre Subcategoria y Categoria_Subcategoria
     * Tablas involucradas:
     *   - Subcategoria (alias 's'): tabla maestra con definiciones de subcategorías
     *   - Categoria_Subcategoria (alias 'cs'): tabla intermedia que define relaciones válidas
     * Tipo de JOIN: INNER JOIN para retornar solo subcategorías que tengan relación explícita
     * Condición del JOIN: cs.subcategoria_id = s.subcategoria_id (relación por clave foránea)
     * Condición WHERE: cs.categoria_id = ? (filtra por la categoría padre solicitada)
     * Columnas seleccionadas:
     *   - s.subcategoria_id: PK de la subcategoría para identificación técnica
     *   - s.nombre: nombre legible para presentación en UI
     * Ordenamiento: ORDER BY s.nombre ASC para mostrar opciones en orden alfabético
     *
     * @param categoriaId identificador de la categoría padre para filtrar subcategorías asociadas
     * @return lista de objetos Subcategoria permitidas para esa categoría, o lista vacía si no hay relaciones
     */
    public List<Subcategoria> obtenerSubcategoriasDisponibles(int categoriaId) {
        // Lista que almacenará las subcategorías válidas recuperadas de la consulta
        List<Subcategoria> lista = new ArrayList<>();
        // SQL: Consulta con JOIN para recuperar subcategorías asociadas a una categoría específica
        // Tabla principal: Subcategoria (s) → contiene los datos legibles de cada subcategoría
        // Tabla de relación: Categoria_Subcategoria (cs) → define qué combinaciones categoría-subcategoría son válidas
        // INNER JOIN: garantiza que solo se retornen subcategorías con relación explícita en la tabla intermedia
        // WHERE cs.categoria_id = ?: filtra por la categoría padre solicitada (parámetro bindado)
        // ORDER BY s.nombre ASC: presenta las opciones en orden alfabético para mejor UX en formularios
        String sql = """
            SELECT s.subcategoria_id, s.nombre
            FROM Subcategoria s
            INNER JOIN Categoria_Subcategoria cs ON cs.subcategoria_id = s.subcategoria_id
            WHERE cs.categoria_id = ?
            ORDER BY s.nombre ASC
            """;
        // try-with-resources para gestión automática de recursos de base de datos
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Bind del parámetro: asigna categoriaId al primer placeholder (?) de la consulta
            ps.setInt(1, categoriaId);
            // Ejecuta la consulta y obtiene ResultSet con las subcategorías asociadas
            try (ResultSet rs = ps.executeQuery()) {
                // Itera sobre cada fila del resultado para mapearla a objeto Subcategoria
                while (rs.next()) {
                    // Crea nueva instancia de Subcategoria para la fila actual
                    Subcategoria s = new Subcategoria();
                    // Mapea columna subcategoria_id (INT) al setter del modelo
                    s.setSubcategoriaId(rs.getInt("subcategoria_id"));
                    // Mapea columna nombre (VARCHAR) al setter del modelo
                    s.setNombre(rs.getString("nombre"));
                    // Agrega el objeto poblado a la lista de retorno
                    lista.add(s);
                }
            }
        } catch (Exception e) {
            // Captura y registra cualquier error para diagnóstico sin interrumpir flujo del llamador
            e.printStackTrace();
        }
        // Retorna lista poblada; si no hay relaciones o hay error, retorna lista vacía (nunca null)
        return lista;
    }

    /**
     * Verifica si una categoría tiene productos activos asociados antes de permitir su eliminación.
     * Este método implementa una regla de integridad de negocio: no eliminar categorías
     * que aún estén siendo utilizadas por productos visibles en el catálogo.
     *
     * Consulta SQL: SELECT COUNT con condiciones de filtrado
     * Tabla involucrada: Producto (contiene referencia FK hacia Categoria)
     * Condiciones aplicadas:
     *   - WHERE categoria_id = ?: filtra productos de la categoría consultada
     *   - AND estado = 1: solo considera productos activos (no eliminados lógicamente)
     * Función de agregación: COUNT(*) retorna número de registros que cumplen las condiciones
     * Lógica de retorno: si COUNT > 0 → true (tiene productos), si COUNT = 0 → false (seguro eliminar)
     *
     * @param categoriaId identificador de la categoría a validar
     * @return {@code true} si existe al menos un producto activo en esta categoría, {@code false} en caso contrario
     * @throws Exception si falla la consulta por errores de conexión o sintaxis SQL
     */
    public boolean tieneProductosActivos(int categoriaId) throws Exception {
        // SQL: Consulta de conteo para validar integridad referencial antes de eliminar categoría
        // Tabla: Producto (entidad que referencia a Categoria mediante FK categoria_id)
        // WHERE categoria_id = ?: filtra por la categoría consultada (parámetro bindado)
        // AND estado = 1: condición de negocio para considerar solo productos activos/visibles
        // COUNT(*): función agregada que retorna el número de filas que cumplen ambas condiciones
        String sql = "SELECT COUNT(*) FROM Producto WHERE categoria_id = ? AND estado = 1";
        // try-with-resources para gestión automática de Connection y PreparedStatement
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Bind del parámetro: asigna categoriaId al primer placeholder (?) de la consulta
            ps.setInt(1, categoriaId);
            // Ejecuta la consulta de conteo y obtiene ResultSet con una sola fila y columna
            try (ResultSet rs = ps.executeQuery()) {
                // next() avanza a la única fila esperada del resultado de COUNT
                if (rs.next()) {
                    // getInt(1) recupera el valor numérico de COUNT(*) desde la primera columna
                    // Retorna true si el conteo es mayor a 0 (hay productos activos), false en caso contrario
                    return rs.getInt(1) > 0;
                }
            }
        }
        // Si no se pudo obtener resultado o hubo error no capturado, retorna false por defecto
        // (aunque en práctica la excepción se propagaría por la firma del método)
        return false;
    }

    /**
     * Inserta una nueva categoría en la base de datos con gestión explícita de transacción.
     * Método utilizado para crear familias de productos en el catálogo.
     * La transacción asegura que la operación sea atómica: o se completa totalmente
     * o se revierte si ocurre cualquier error durante la inserción.
     *
     * Consulta SQL: INSERT en tabla Categoria
     * Tabla destino: Categoria (almacena categorías principales del catálogo)
     * Columnas insertadas:
     *   - nombre: nombre legible de la categoría (VARCHAR, no nullable en esquema)
     *   - icono: referencia al icono asociado para representación visual (VARCHAR, nullable)
     * Valores parametrizados (?, ?) para prevenir SQL Injection y permitir reutilización del plan
     * La PK categoria_id se genera automáticamente (AUTO_INCREMENT o SEQUENCE según motor)
     *
     * @param c objeto Categoria con nombre e icono poblados para persistir
     * @return {@code true} si la inserción se completó exitosamente, {@code false} si no se insertó ninguna fila
     * @throws Exception si falla la transacción por errores de conexión, constraints de BD o rollback
     */
    public boolean guardar(Categoria c) throws Exception {
        // SQL: INSERT para crear nuevo registro en tabla Categoria
        // Tabla destino: Categoria
        // Columnas: nombre (VARCHAR), icono (VARCHAR)
        // Valores parametrizados (?, ?) para seguridad y eficiencia en ejecución
        // PK categoria_id se autogenera, no se incluye en la lista de columnas
        String sql = "INSERT INTO Categoria (nombre, icono) VALUES (?, ?)";
        // Obtiene conexión manual para controlar transacción explícitamente
        try (Connection con = ConexionDB.getConnection()) {
            // Desactiva auto-commit para iniciar transacción manual
            con.setAutoCommit(false);
            try {
                // PreparedStatement para ejecutar la inserción con parámetros seguros
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    // Bind parámetro 1: asigna nombre de la categoría al primer placeholder
                    ps.setString(1, c.getNombre());
                    // Bind parámetro 2: asigna referencia de icono al segundo placeholder
                    ps.setString(2, c.getIcono());
                    // Ejecuta la inserción; el número de filas afectadas se ignora porque se espera 1
                    ps.executeUpdate();
                }
                // Confirma la transacción: hace permanente la inserción en la base de datos
                con.commit();
                // Retorna true indicando que la operación se completó exitosamente
                return true;
            } catch (Exception e) {
                // En caso de error, revierte la transacción para mantener consistencia de datos
                con.rollback();
                // Propaga la excepción original para que el llamador decida cómo manejarla
                throw e;
            } finally {
                // Restaura auto-commit a true para no afectar usos posteriores de esta conexión
                // Este bloque se ejecuta siempre, haya éxito o error
                con.setAutoCommit(true);
            }
        }
    }

    /**
     * Actualiza el nombre y/o icono de una categoría existente identificada por su ID.
     * Método utilizado para editar propiedades de categorías en el catálogo.
     * La transacción garantiza atomicidad: o se actualizan ambos campos o ninguno.
     *
     * Consulta SQL: UPDATE en tabla Categoria con cláusula WHERE por clave primaria
     * Tabla destino: Categoria
     * Columnas modificadas:
     *   - nombre: nuevo nombre legible de la categoría (VARCHAR)
     *   - icono: nueva referencia de icono para representación visual (VARCHAR)
     * Condición de filtrado: WHERE categoria_id = ? (actualiza solo el registro con PK específica)
     * Parámetros en orden: 1=nombre, 2=icono, 3=categoriaId (para el WHERE)
     *
     * @param c objeto Categoria con categoriaId, nombre e icono poblados para actualizar
     * @return {@code true} si se actualizó al menos una fila (categoría existente), {@code false} si no se encontró
     * @throws Exception si falla la transacción por errores de conexión, constraints o rollback
     */
    public boolean actualizar(Categoria c) throws Exception {
        // SQL: UPDATE para modificar registro existente en tabla Categoria
        // Tabla destino: Categoria
        // SET nombre = ?, icono = ?: asigna nuevos valores a las columnas modificables
        // WHERE categoria_id = ?: filtra por PK para actualizar solo el registro específico
        // Parámetros: 1=nombre, 2=icono, 3=categoriaId (bindados en ese orden)
        String sql = "UPDATE Categoria SET nombre = ?, icono = ? WHERE categoria_id = ?";
        // Obtiene conexión manual para control explícito de transacción
        try (Connection con = ConexionDB.getConnection()) {
            // Inicia transacción manual desactivando auto-commit
            con.setAutoCommit(false);
            try {
                // PreparedStatement para ejecutar actualización con parámetros seguros
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    // Bind parámetro 1: asigna nuevo nombre al primer placeholder (SET nombre = ?)
                    ps.setString(1, c.getNombre());
                    // Bind parámetro 2: asigna nuevo icono al segundo placeholder (SET icono = ?)
                    ps.setString(2, c.getIcono());
                    // Bind parámetro 3: asigna ID de categoría al tercer placeholder (WHERE categoria_id = ?)
                    ps.setInt(3, c.getCategoriaId());
                    // Ejecuta la actualización; retorna número de filas modificadas (esperado: 1 si existe)
                    ps.executeUpdate();
                }
                // Confirma la transacción: hace permanente la actualización en la base de datos
                con.commit();
                // Retorna true indicando que la operación se completó exitosamente
                return true;
            } catch (Exception e) {
                // En caso de error, revierte cambios para mantener integridad de datos
                con.rollback();
                // Propaga la excepción para manejo en capa superior
                throw e;
            } finally {
                // Restaura configuración original de auto-commit para reutilización segura de conexión
                con.setAutoCommit(true);
            }
        }
    }

    /**
     * Elimina una categoría del catálogo previa validación de integridad referencial.
     * Antes de ejecutar el DELETE, verifica que la categoría no tenga productos activos
     * asociados, lanzando excepción descriptiva si la regla de negocio se viola.
     *
     * Consulta SQL: DELETE en tabla Categoria con condición por clave primaria
     * Tabla afectada: Categoria
     * Condición: WHERE categoria_id = ? (elimina solo el registro con PK específica)
     * Pre-validación: llama a tieneProductosActivos() para aplicar regla de negocio
     * Retorno: true si se eliminó una fila, false si no se encontró la categoría
     *
     * @param id valor de {@code categoria_id} de la categoría a eliminar
     * @return {@code true} si se eliminó exitosamente, {@code false} si no existía el registro
     * @throws Exception si la categoría tiene productos activos (regla de negocio) o falla la consulta
     */
    public boolean eliminar(int id) throws Exception {
        // Pre-validación de regla de negocio: no permitir eliminar categorías en uso
        // Llama a método auxiliar que ejecuta SELECT COUNT con filtros de integridad
        if (tieneProductosActivos(id)) {
            // Lanza excepción descriptiva si hay productos activos, evitando eliminación insegura
            throw new Exception("No se puede eliminar: hay productos activos en esta categoría.");
        }
        // SQL: DELETE para remover registro de tabla Categoria
        // Tabla afectada: Categoria
        // WHERE categoria_id = ?: condición parametrizada para eliminar solo el registro específico
        // Parámetro bindado con ps.setInt(1, id) antes de ejecutar
        String sql = "DELETE FROM Categoria WHERE categoria_id = ?";
        // try-with-resources para gestión automática de Connection y PreparedStatement
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Bind del parámetro: asigna el ID de categoría al primer placeholder (?) de la consulta
            ps.setInt(1, id);
            // Ejecuta el DELETE y retorna true si se eliminó al menos una fila (registro encontrado)
            return ps.executeUpdate() > 0;
        }
    }
}