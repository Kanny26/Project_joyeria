package dao;

import config.ConexionDB;
import model.Material;
import model.Proveedor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestión de proveedores del sistema de joyería.
 * Esta clase administra la relación comercial con proveedores, incluyendo sus datos maestros
 * (nombre, documento, fechas, estado), contactos (teléfonos, correos) y materiales que suministran.
 * Implementa operaciones CRUD completas con gestión transaccional para garantizar consistencia
 * entre datos principales y tablas relacionadas. Todas las operaciones críticas registran auditoría
 * para trazabilidad de cambios. Utiliza ConexionDB para obtención de conexiones y PreparedStatement
 * para prevenir SQL Injection y optimizar ejecución de consultas.
 */
public class ProveedorDAO {

    // ==================== CONSULTAS ====================

    /**
     * Devuelve todos los proveedores ordenados por fecha de registro (más recientes primero).
     * Para cada proveedor, carga adicionalmente sus teléfonos, correos y materiales asociados
     * mediante consultas separadas. Si alguna carga secundaria falla, se asigna lista vacía
     * en lugar de interrumpir todo el listado, garantizando resiliencia en la presentación.
     *
     * Consulta SQL principal: SELECT simple sobre tabla Proveedor
     * Tabla involucrada: Proveedor (entidad maestra de relaciones comerciales)
     * Columnas seleccionadas:
     *   - proveedor_id: PK autoincremental, identificador único del proveedor
     *   - nombre: razón social o nombre comercial del proveedor
     *   - documento: número de identificación fiscal (RUC, NIT, etc.)
     *   - fecha_registro: fecha de creación del registro en el sistema
     *   - fecha_inicio: fecha de inicio de la relación comercial
     *   - estado: flag booleano para activación/desactivación lógica
     *   - minimo_compra: monto mínimo requerido para realizar pedidos
     * Ordenamiento: ORDER BY fecha_registro DESC para mostrar proveedores nuevos primero
     *
     * Flujo de datos adicional por proveedor:
     *   - obtenerTelefonos(id): consulta tabla Telefono_Proveedor por FK
     *   - obtenerCorreos(id): consulta tabla Correo_Proveedor por FK
     *   - obtenerMateriales(id): consulta Proveedor_Material + Material mediante JOIN
     *
     * Manejo de errores: captura excepciones en cargas secundarias para no fallar el listado completo
     *
     * @return lista de objetos Proveedor con datos principales y relacionados, o lista vacía si hay error crítico
     */
    public List<Proveedor> listarProveedores() {
        List<Proveedor> lista = new ArrayList<>();
        // Consulta SQL: SELECT de campos principales de tabla Proveedor
        // Sin condiciones WHERE: recupera todos los proveedores registrados
        // ORDER BY fecha_registro DESC: presenta proveedores más recientes primero para mejor UX
        String sql = """
            SELECT proveedor_id, nombre, documento, fecha_registro, fecha_inicio, estado, minimo_compra 
            FROM Proveedor ORDER BY fecha_registro DESC
            """;
        // try-with-resources: garantiza cierre automático de Connection, PreparedStatement y ResultSet
        // previniendo fugas de recursos incluso si ocurre excepción durante iteración
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            // Itera sobre cada fila del resultado para mapearla a objeto Proveedor
            while (rs.next()) {
                // Mapea campos principales mediante método helper privado
                Proveedor p = mapearProveedor(rs);
                int id = p.getProveedorId();
                // Carga datos relacionados con manejo resiliente de errores:
                // Si alguna consulta secundaria falla, asigna lista vacía para no romper el flujo
                try { p.setTelefonos(obtenerTelefonos(id)); } catch (Exception e) { p.setTelefonos(new ArrayList<>()); }
                try { p.setCorreos(obtenerCorreos(id)); } catch (Exception e) { p.setCorreos(new ArrayList<>()); }
                try { p.setMateriales(obtenerMateriales(id)); } catch (Exception e) { p.setMateriales(new ArrayList<>()); }
                // Agrega proveedor completamente poblado a lista de retorno
                lista.add(p);
            }
        } catch (Exception e) {
            // Registra error crítico en consola para diagnóstico en producción
            System.err.println("ERROR CRÍTICO al listar proveedores: " + e.getMessage());
            e.printStackTrace();
        }
        // Retorna lista poblada; si hubo error o no hay datos, retorna lista vacía (nunca null)
        return lista;
    }

    /**
     * Busca y retorna un proveedor específico por su identificador único.
     * Método utilizado para cargar datos completos de un proveedor para edición
     * o visualización de detalle en la interfaz.
     *
     * Consulta SQL: SELECT con filtro WHERE por clave primaria
     * Tabla involucrada: Proveedor (alias 'p' para claridad en consultas complejas)
     * Condición: WHERE p.proveedor_id = ? (búsqueda por PK, eficiente con índice)
     * Parámetro: id (Integer) → bindado al primer placeholder (?) mediante PreparedStatement
     * Columnas seleccionadas: mismas que listarProveedores (campos principales de la entidad)
     *
     * Flujo de datos adicional tras recuperar proveedor:
     *   - obtenerTelefonos(), obtenerCorreos(), obtenerMateriales(): cargan listas relacionadas
     *   - Si proveedor no existe (rs.next() es false), retorna null para que el servlet redirija
     *
     * @param id identificador único {@code proveedor_id} a buscar
     * @return objeto Proveedor completamente poblado con datos principales y relacionados,
     *         o {@code null} si no se encuentra el registro o ocurre error
     */
    public Proveedor obtenerPorId(Integer id) {
        // SQL: SELECT filtrado por clave primaria para recuperación eficiente de un solo registro
        // Alias 'p' para Proveedor mejora legibilidad en consultas con JOINs futuros
        // WHERE p.proveedor_id = ?: condición parametrizada para prevenir SQL Injection
        String sql = """
            SELECT p.proveedor_id, p.nombre, p.documento, p.fecha_registro, p.fecha_inicio, p.estado, p.minimo_compra 
            FROM Proveedor p WHERE p.proveedor_id = ?
            """;
        // try-with-resources para gestión automática de Connection y PreparedStatement
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Bind del parámetro: asigna el valor de id al primer placeholder (?) de la consulta
            stmt.setInt(1, id);
            // Ejecuta consulta y obtiene ResultSet con máximo 1 fila (por PK única)
            try (ResultSet rs = stmt.executeQuery()) {
                // next() retorna true solo si existe al menos una fila en el resultado
                if (rs.next()) {
                    // Mapea campos principales mediante método helper privado
                    Proveedor p = mapearProveedor(rs);
                    // Carga datos relacionados (teléfonos, correos, materiales) mediante consultas separadas
                    p.setTelefonos(obtenerTelefonos(p.getProveedorId()));
                    p.setCorreos(obtenerCorreos(p.getProveedorId()));
                    p.setMateriales(obtenerMateriales(p.getProveedorId()));
                    // Retorna proveedor completamente poblado
                    return p;
                }
            }
        } catch (Exception e) {
            // Registra error en consola para diagnóstico sin propagar excepción
            System.err.println("Error al obtener proveedor: " + e.getMessage());
            e.printStackTrace();
        }
        // Retorna null si no se encontró el proveedor o ocurrió error no capturado
        return null;
    }

    /**
     * Busca proveedores según un término de búsqueda y un tipo de filtro.
     * El parámetro "filtro" determina en qué campo buscar:
     *   - "nombre": busca coincidencias parciales en el nombre del proveedor (LIKE %term%)
     *   - "materiales": busca proveedores que suministren materiales cuyo nombre coincida
     *   - "todos" (default): busca en nombre O materiales mediante operador OR
     *
     * Consultas SQL según filtro:
     *
     * Caso "nombre":
     *   Tabla: Proveedor (alias 'p')
     *   Condición: WHERE p.nombre LIKE ? (búsqueda textual con comodines %)
     *   DISTINCT: evita duplicados si hubiera JOINs futuros
     *
     * Caso "materiales":
     *   Tablas: Proveedor (p) + Proveedor_Material (pm) + Material (m)
     *   JOINs: INNER JOIN para retornar solo proveedores con materiales que coincidan
     *   Condición: WHERE m.nombre LIKE ? (filtra por nombre de material)
     *   DISTINCT: evita duplicados si un proveedor tiene múltiples materiales coincidentes
     *
     * Caso "todos":
     *   Tablas: mismas que caso "materiales"
     *   JOINs: LEFT JOIN para conservar proveedores sin materiales si su nombre coincide
     *   Condición: WHERE p.nombre LIKE ? OR m.nombre LIKE ? (búsqueda en ambos campos)
     *   Parámetros: se binda el mismo término dos veces (para cada placeholder del OR)
     *
     * Ordenamiento común: ORDER BY p.fecha_registro DESC (más recientes primero)
     * Parámetro de búsqueda: se envuelve con % para permitir coincidencias parciales (LIKE)
     *
     * @param q texto de búsqueda a encontrar en nombre o materiales
     * @param filtro criterio de búsqueda: {@code nombre}, {@code materiales} u otro valor para "todos"
     * @return lista de objetos Proveedor que coinciden con los criterios, o lista vacía si no hay resultados
     * @throws Exception si falla la consulta por errores de conexión, sintaxis SQL o acceso a datos
     */
    public List<Proveedor> buscar(String q, String filtro) throws Exception {
        String sql;
        // Construye consulta SQL dinámica según criterio de filtro seleccionado
        switch (filtro) {
            case "nombre":
                // Búsqueda simple en tabla Proveedor por campo nombre
                // LIKE con % permite coincidencias parciales (ej: "oro" encuentra "Oro Fino SA")
                sql = """
                    SELECT DISTINCT p.proveedor_id, p.nombre, p.documento, p.fecha_registro, 
                           p.fecha_inicio, p.estado, p.minimo_compra 
                    FROM Proveedor p 
                    WHERE p.nombre LIKE ?
                    ORDER BY p.fecha_registro DESC
                    """;
                break;
            case "materiales":
                // Búsqueda mediante JOIN con tablas de relación Proveedor_Material y Material
                // INNER JOIN: solo retorna proveedores que tengan al menos un material coincidente
                // DISTINCT: evita duplicados si un proveedor tiene múltiples materiales que coinciden
                sql = """
                    SELECT DISTINCT p.proveedor_id, p.nombre, p.documento, p.fecha_registro, 
                           p.fecha_inicio, p.estado, p.minimo_compra 
                    FROM Proveedor p 
                    JOIN Proveedor_Material pm ON p.proveedor_id = pm.proveedor_id 
                    JOIN Material m ON pm.material_id = m.material_id 
                    WHERE m.nombre LIKE ?
                    ORDER BY p.fecha_registro DESC
                    """;
                break;
            default: // todos
                // Búsqueda combinada: nombre del proveedor O nombre de sus materiales
                // LEFT JOIN: conserva proveedores sin materiales si su nombre coincide con la búsqueda
                // OR en WHERE: permite coincidencia en cualquiera de los dos campos
                // Se requieren dos parámetros idénticos para los dos placeholders del OR
                sql = """
                    SELECT DISTINCT p.proveedor_id, p.nombre, p.documento, p.fecha_registro, 
                           p.fecha_inicio, p.estado, p.minimo_compra 
                    FROM Proveedor p 
                    LEFT JOIN Proveedor_Material pm ON p.proveedor_id = pm.proveedor_id 
                    LEFT JOIN Material m ON pm.material_id = m.material_id 
                    WHERE p.nombre LIKE ? OR m.nombre LIKE ?
                    ORDER BY p.fecha_registro DESC
                    """;
                break;
        }

        List<Proveedor> lista = new ArrayList<>();
        // try-with-resources para gestión automática de Connection y PreparedStatement
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Prepara parámetro de búsqueda con comodines % para coincidencias parciales (LIKE)
            String param = "%" + q + "%";
            ps.setString(1, param);
            // Solo cuando el filtro es "todos" se necesita un segundo parámetro (para el OR del SQL)
            if ("todos".equals(filtro)) ps.setString(2, param);
            // Ejecuta consulta y obtiene ResultSet con proveedores que coinciden con los criterios
            try (ResultSet rs = ps.executeQuery()) {
                // Itera sobre resultados mapeando cada fila a objeto Proveedor
                while (rs.next()) {
                    Proveedor p = mapearProveedor(rs);
                    int id = p.getProveedorId();
                    // Carga datos relacionados con manejo resiliente de errores
                    try { p.setTelefonos(obtenerTelefonos(id)); } catch (Exception e) { p.setTelefonos(new ArrayList<>()); }
                    try { p.setCorreos(obtenerCorreos(id)); }    catch (Exception e) { p.setCorreos(new ArrayList<>()); }
                    try { p.setMateriales(obtenerMateriales(id)); } catch (Exception e) { p.setMateriales(new ArrayList<>()); }
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            // Registra stack trace para diagnóstico de errores SQL en producción
            e.printStackTrace();
        }
        // Retorna lista poblada; si no hay coincidencias o hay error, retorna lista vacía (nunca null)
        return lista;
    }

    /**
     * Verifica si ya existe un proveedor con ese documento en la base de datos.
     * Método de validación utilizado al registrar un nuevo proveedor para prevenir
     * duplicados de identificación fiscal (RUC, NIT, etc.) que violarían integridad de negocio.
     *
     * Consulta SQL: SELECT COUNT con filtro por documento
     * Tabla involucrada: Proveedor
     * Condición: WHERE documento = ? (búsqueda exacta por campo único de negocio)
     * Función de agregación: COUNT(*) retorna número de registros que coinciden
     * Lógica de retorno: si COUNT > 0 → true (ya existe), si COUNT = 0 → false (disponible)
     *
     * @param documento número de identificación fiscal a validar (ej: RUC, NIT, CIF)
     * @return {@code true} si ya existe un proveedor con ese documento, {@code false} si está disponible
     */
    public boolean existeDocumento(String documento) {
        // SQL: Consulta de conteo para validar unicidad de documento antes de insertar
        // Tabla: Proveedor
        // WHERE documento = ?: filtro parametrizado para búsqueda exacta y prevención de SQL Injection
        // COUNT(*): retorna 0 si no hay coincidencias, 1 o más si el documento ya existe
        String sql = "SELECT COUNT(*) FROM Proveedor WHERE documento = ?";
        // try-with-resources para gestión automática de recursos de base de datos
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Bind del parámetro: asigna documento al primer placeholder (?) de la consulta
            stmt.setString(1, documento);
            // Ejecuta consulta de conteo y obtiene ResultSet con una sola fila y columna
            try (ResultSet rs = stmt.executeQuery()) {
                // next() avanza a la única fila esperada del resultado de COUNT
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            // Registra error en consola para diagnóstico sin interrumpir flujo del llamador
            System.err.println("Error al verificar existencia de documento: " + e.getMessage());
        }
        // Retorna false por defecto si no se pudo obtener resultado o hubo error no capturado
        return false;
    }

    /**
     * Verifica si el documento ya está usado por OTRO proveedor diferente al que se está editando.
     * La condición "proveedor_id <> ?" excluye al proveedor actual de la validación,
     * permitiendo que conserve su propio documento sin error de duplicado durante edición.
     *
     * Consulta SQL: SELECT COUNT con dos condiciones
     * Tabla involucrada: Proveedor
     * Condiciones aplicadas:
     *   - WHERE documento = ?: busca coincidencias exactas del documento
     *   - AND proveedor_id <> ?: excluye el registro del proveedor que se está editando
     * Propósito: validar unicidad de documento ignorando el propio registro en contexto de actualización
     *
     * @param documento número de identificación fiscal a comprobar
     * @param proveedorIdActual ID del proveedor que se está editando (excluido del conteo de duplicados)
     * @return {@code true} si otro proveedor distinto ya usa ese documento, {@code false} si está disponible
     */
    public boolean existeDocumentoParaOtro(String documento, int proveedorIdActual) {
        // SQL: Consulta de conteo para validar unicidad de documento excluyendo proveedor actual
        // Tabla: Proveedor
        // WHERE documento = ?: filtra por documento específico
        // AND proveedor_id <> ?: excluye el registro del proveedor que se está editando
        // Esto permite que un proveedor mantenga su propio documento al guardar cambios sin falsos positivos
        String sql = "SELECT COUNT(*) FROM Proveedor WHERE documento = ? AND proveedor_id <> ?";
        // try-with-resources para gestión automática de Connection y PreparedStatement
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Bind parámetros en orden: 1=documento a validar, 2=ID del proveedor actual a excluir
            stmt.setString(1, documento);
            stmt.setInt(2, proveedorIdActual);
            // Ejecuta consulta de conteo y evalúa resultado
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            // Registra error en consola para diagnóstico sin propagar excepción
            System.err.println("Error al verificar documento para otro: " + e.getMessage());
        }
        // Retorna false por defecto si no se pudo obtener resultado o hubo error
        return false;
    }

    // ==================== GUARDAR (INSERT) ====================

    /**
     * Guarda un nuevo proveedor junto con todos sus datos relacionados:
     * teléfonos, correos, materiales y un registro de auditoría.
     *
     * Usa una transacción (setAutoCommit false) para que, si alguna inserción falla,
     * se revierta todo y no queden datos incompletos en la base de datos.
     *
     * RETURN_GENERATED_KEYS permite obtener el ID autoincremental que MySQL asigna
     * al nuevo proveedor, necesario para insertar los registros relacionados.
     *
     * Las listas se insertan en lote (addBatch/executeBatch) para mayor eficiencia.
     * Los correos se convierten a minúsculas para evitar duplicados por capitalización.
     *
     * @param p datos del proveedor a insertar (nombre, documento, fecha_inicio, estado, minimo_compra)
     * @param telefonos lista de teléfonos a asociar (puede ser vacía o null)
     * @param correos lista de correos electrónicos a asociar (se normalizan a minúsculas)
     * @param materialesIds lista de IDs de materiales que suministra este proveedor
     * @param usuarioId ID del usuario que origina la acción para registro de auditoría
     * @return {@code true} si la transacción completó exitosamente todas las inserciones,
     *         {@code false} si ocurrió error o no se pudo obtener conexión
     */
    public boolean guardar(Proveedor p, List<String> telefonos, List<String> correos, List<Integer> materialesIds, int usuarioId) {
        Connection conn = null;
        try {
            // Obtiene conexión manual para controlar explícitamente la transacción
            conn = ConexionDB.getConnection();
            if (conn == null) return false;
            // Desactiva auto-commit para iniciar transacción manual atómica
            conn.setAutoCommit(false);

            // SQL: INSERT para crear nuevo registro en tabla Proveedor
            // Tabla destino: Proveedor (entidad maestra de relaciones comerciales)
            // Columnas insertadas:
            //   - nombre: razón social o nombre comercial del proveedor
            //   - documento: número de identificación fiscal (campo único de negocio)
            //   - fecha_registro: CURDATE() para fecha automática del servidor de BD
            //   - fecha_inicio: fecha de inicio de relación comercial (proporcionada por usuario)
            //   - estado: flag booleano para activación inicial (generalmente true)
            //   - minimo_compra: monto mínimo para pedidos (con fallback a 0.0 si es null)
            // Valores parametrizados (?, ?, ?, ?, ?, ?) para seguridad y reutilización de plan
            // Configuración RETURN_GENERATED_KEYS: recupera proveedor_id autogenerado tras INSERT
            String sqlProveedor = """
                INSERT INTO Proveedor(nombre, documento, fecha_registro, fecha_inicio, estado, minimo_compra) 
                VALUES(?, ?, CURDATE(), ?, ?, ?)
                """;

            int idGenerado = 0;
            // PreparedStatement con configuración para retorno de claves generadas (PK autogenerada)
            try (PreparedStatement stmt = conn.prepareStatement(sqlProveedor, Statement.RETURN_GENERATED_KEYS)) {
                // Bind parámetros en orden definido en la consulta SQL:
                // 1=nombre, 2=documento, 3=fecha_inicio, 4=estado, 5=minimo_compra (con fallback)
                stmt.setString(1, p.getNombre());
                stmt.setString(2, p.getDocumento());
                stmt.setString(3, p.getFechaInicio());
                stmt.setBoolean(4, p.isEstado());
                stmt.setDouble(5, p.getMinimoCompra() != null ? p.getMinimoCompra() : 0.0);
                // Ejecuta INSERT y verifica que se afectó al menos una fila
                int filas = stmt.executeUpdate();
                if (filas == 0) throw new SQLException("No se pudo insertar el proveedor.");
                // Recupera ResultSet con claves generadas (proveedor_id autogenerado)
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        idGenerado = keys.getInt(1);
                        p.setProveedorId(idGenerado);
                    } else {
                        throw new SQLException("No se obtuvo el ID del proveedor generado.");
                    }
                }
            }

            // Insertar teléfonos — se ignoran los vacíos para no guardar entradas en blanco
            // SQL: INSERT en tabla Telefono_Proveedor ejecutado en batch para eficiencia
            // Tabla destino: Telefono_Proveedor (relación uno-a-muchos con Proveedor)
            // Columnas: telefono (VARCHAR), proveedor_id (FK hacia Proveedor)
            // Estrategia batch: addBatch() por cada teléfono válido + executeBatch() único
            if (telefonos != null && !telefonos.isEmpty()) {
                String sqlTel = "INSERT INTO Telefono_Proveedor(telefono, proveedor_id) VALUES(?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlTel)) {
                    for (String tel : telefonos) {
                        if (tel != null && !tel.trim().isEmpty()) {
                            stmt.setString(1, tel.trim());
                            stmt.setInt(2, idGenerado);
                            stmt.addBatch();
                        }
                    }
                    // Ejecuta todos los INSERTs del batch en una sola operación a la BD
                    stmt.executeBatch();
                }
            }

            // Insertar correos — se normalizan a minúsculas antes de guardar
            // SQL: INSERT en tabla Correo_Proveedor ejecutado en batch
            // Tabla destino: Correo_Proveedor (relación uno-a-muchos con Proveedor)
            // Columnas: email (VARCHAR, único en negocio), proveedor_id (FK)
            // Normalización: toLowerCase() para evitar duplicados por diferencias de capitalización
            if (correos != null && !correos.isEmpty()) {
                String sqlCorreo = "INSERT INTO Correo_Proveedor(email, proveedor_id) VALUES(?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlCorreo)) {
                    for (String correo : correos) {
                        if (correo != null && !correo.trim().isEmpty()) {
                            stmt.setString(1, correo.trim().toLowerCase());
                            stmt.setInt(2, idGenerado);
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            }

            // Insertar materiales que suministra este proveedor
            // SQL: INSERT en tabla Proveedor_Material (tabla intermedia de relación muchos-a-muchos)
            // Tabla destino: Proveedor_Material (vincula Proveedor con Material)
            // Columnas: proveedor_id (FK), material_id (FK)
            // Propósito: definir qué materiales puede suministrar cada proveedor para filtros y validaciones
            if (materialesIds != null && !materialesIds.isEmpty()) {
                String sqlMat = "INSERT INTO Proveedor_Material(proveedor_id, material_id) VALUES(?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlMat)) {
                    for (Integer matId : materialesIds) {
                        if (matId != null) {
                            stmt.setInt(1, idGenerado);
                            stmt.setInt(2, matId);
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            }

            // RF38: Registro de auditoría — deja constancia de quién creó el proveedor
            // Llama a método helper con conexión activa para que el registro quede en misma transacción
            registrarAuditoria(conn, usuarioId, "CREAR", "Proveedor", idGenerado, null, p.getNombre());

            // Confirma la transacción: hace permanentes todos los cambios si todo fue exitoso
            conn.commit();
            System.out.println("Proveedor guardado con éxito. ID: " + idGenerado);
            return true;
        } catch (Exception e) {
            // En caso de error en cualquier paso, registra mensaje detallado para diagnóstico
            System.err.println("ERROR AL GUARDAR PROVEEDOR: " + e.getMessage());
            e.printStackTrace();
            // Si algo falló, se deshacen todos los cambios para no dejar datos a medias
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            // Bloque finally: garantiza restauración de auto-commit y cierre de conexión
            // incluso si ocurre excepción, previniendo fugas de recursos
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // ==================== ACTUALIZAR (UPDATE) ====================

    /**
     * Actualiza los datos editables de un proveedor: estado, mínimo de compra,
     * teléfonos, correos y materiales.
     *
     * IMPORTANTE: nombre, documento y fechaInicio NO se actualizan desde aquí
     * porque son campos inmutables según las reglas del negocio (RF11).
     *
     * La estrategia para teléfonos, correos y materiales es "eliminar todo e insertar de nuevo"
     * (DELETE + INSERT), lo que simplifica el manejo de cambios en las listas.
     * Esto funciona correctamente dentro de la transacción.
     *
     * @param p proveedor con ID y campos editables actualizados (estado, minimo_compra)
     * @param telefonos lista nueva de teléfonos a asociar (reemplaza completamente la anterior)
     * @param correos lista nueva de correos a asociar (se normalizan a minúsculas)
     * @param materialesIds lista nueva de IDs de materiales que suministra
     * @param usuarioId ID del usuario que realiza la actualización para registro de auditoría
     * @return {@code true} si la transacción completó exitosamente todas las actualizaciones,
     *         {@code false} si ocurrió error en cualquier paso
     */
    public boolean actualizar(Proveedor p, List<String> telefonos, List<String> correos, List<Integer> materialesIds, int usuarioId) {
        Connection conn = null;
        try {
            // Obtiene conexión manual para controlar explícitamente la transacción
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false);

            // Capturar nombre anterior para dejarlo registrado en la auditoría
            // Consulta auxiliar: SELECT nombre FROM Proveedor WHERE proveedor_id = ?
            // Propósito: almacenar valor previo para trazabilidad en log de auditoría
            String nombreAnterior = "N/A";
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT nombre FROM Proveedor WHERE proveedor_id = ?")) {
                ps.setInt(1, p.getProveedorId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) nombreAnterior = rs.getString("nombre");
                }
            }

            // SQL: UPDATE para modificar campos editables de proveedor existente
            // Tabla destino: Proveedor
            // Columnas modificadas: estado (boolean), minimo_compra (decimal)
            // Condición: WHERE proveedor_id = ? (actualiza solo el registro específico)
            // Nota: nombre, documento y fecha_inicio NO se actualizan por regla de negocio (inmutables)
            String sqlProveedor = """
                UPDATE Proveedor 
                SET estado = ?, minimo_compra = ? 
                WHERE proveedor_id = ?
                """;
            try (PreparedStatement stmt = conn.prepareStatement(sqlProveedor)) {
                // Bind parámetros en orden: 1=estado, 2=minimo_compra (con fallback), 3=proveedor_id
                stmt.setBoolean(1, p.isEstado());
                stmt.setDouble(2, p.getMinimoCompra() != null ? p.getMinimoCompra() : 0.0);
                stmt.setInt(3, p.getProveedorId());
                stmt.executeUpdate();
            }

            // Reemplazar teléfonos: estrategia DELETE + INSERT para simplificar sincronización
            // Paso 1: DELETE FROM Telefono_Proveedor WHERE proveedor_id = ?
            // Elimina todos los teléfonos anteriores para este proveedor
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM Telefono_Proveedor WHERE proveedor_id = ?")) {
                stmt.setInt(1, p.getProveedorId()); stmt.executeUpdate();
            }
            // Paso 2: INSERT en batch con los nuevos teléfonos (ignorando vacíos)
            if (telefonos != null && !telefonos.isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO Telefono_Proveedor(telefono, proveedor_id) VALUES(?, ?)")) {
                    for (String tel : telefonos) {
                        if (tel != null && !tel.trim().isEmpty()) {
                            stmt.setString(1, tel.trim());
                            stmt.setInt(2, p.getProveedorId());
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            }

            // Reemplazar correos de la misma forma (DELETE + INSERT batch)
            // Normalización: toLowerCase() para evitar duplicados por capitalización
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM Correo_Proveedor WHERE proveedor_id = ?")) {
                stmt.setInt(1, p.getProveedorId()); stmt.executeUpdate();
            }
            if (correos != null && !correos.isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO Correo_Proveedor(email, proveedor_id) VALUES(?, ?)")) {
                    for (String correo : correos) {
                        if (correo != null && !correo.trim().isEmpty()) {
                            stmt.setString(1, correo.trim().toLowerCase());
                            stmt.setInt(2, p.getProveedorId());
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            }

            // Reemplazar materiales de la misma forma (DELETE + INSERT batch)
            // Tabla intermedia Proveedor_Material: relación muchos-a-muchos entre Proveedor y Material
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM Proveedor_Material WHERE proveedor_id = ?")) {
                stmt.setInt(1, p.getProveedorId()); stmt.executeUpdate();
            }
            if (materialesIds != null && !materialesIds.isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO Proveedor_Material(proveedor_id, material_id) VALUES(?, ?)")) {
                    for (Integer matId : materialesIds) {
                        if (matId != null) {
                            stmt.setInt(1, p.getProveedorId());
                            stmt.setInt(2, matId);
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            }

            // Registra auditoría con nombre anterior y nuevo para trazabilidad completa del cambio
            registrarAuditoria(conn, usuarioId, "EDITAR", "Proveedor", p.getProveedorId(), nombreAnterior, p.getNombre());
            // Confirma transacción: hace permanentes todos los cambios si todo fue exitoso
            conn.commit();
            System.out.println("Proveedor actualizado con éxito. ID: " + p.getProveedorId());
            return true;
        } catch (Exception e) {
            // En caso de error, registra mensaje detallado para diagnóstico
            System.err.println("Error al actualizar proveedor: " + e.getMessage());
            e.printStackTrace();
            // Revierte todos los cambios si ocurre error en cualquier paso
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            return false;
        } finally {
            // Garantiza restauración de auto-commit y cierre de conexión incluso con excepción
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    /**
     * Verifica si un número de teléfono ya está registrado en CUALQUIER proveedor.
     * Se usa al crear un nuevo proveedor para evitar duplicados globales de contacto.
     *
     * Consulta SQL: SELECT COUNT con filtro exacto por teléfono
     * Tabla involucrada: Telefono_Proveedor (tabla de contactos de proveedores)
     * Condición: WHERE telefono = ? (búsqueda exacta, case-sensitive según collation de BD)
     * Pre-procesamiento: trim() para eliminar espacios accidentales antes de comparar
     * Lógica de retorno: si COUNT > 0 → true (ya existe), si COUNT = 0 → false (disponible)
     *
     * @param telefono número telefónico a validar (se aplica trim() en la consulta)
     * @return {@code true} si el teléfono ya está registrado en algún proveedor, {@code false} si está disponible
     */
    public boolean existeTelefonoProveedor(String telefono) {
        // SQL: Consulta de conteo para validar unicidad global de teléfono
        // Tabla: Telefono_Proveedor
        // WHERE telefono = ?: filtro parametrizado para búsqueda exacta y prevención de SQL Injection
        // COUNT(*): retorna número de registros que coinciden con el teléfono
        String sql = "SELECT COUNT(*) FROM Telefono_Proveedor WHERE telefono = ?";
        // try-with-resources para gestión automática de recursos de base de datos
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Bind parámetro con trim() para normalizar entrada y evitar falsos negativos por espacios
            ps.setString(1, telefono.trim());
            // Ejecuta consulta de conteo y evalúa resultado
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (Exception e) { e.printStackTrace(); }
        // Retorna false por defecto si no se pudo obtener resultado o hubo error
        return false;
    }

    /**
     * Verifica si un teléfono ya lo usa OTRO proveedor distinto al que se está editando.
     * Así el proveedor actual puede conservar su propio teléfono sin error de duplicado.
     *
     * Consulta SQL: SELECT COUNT con dos condiciones
     * Tabla involucrada: Telefono_Proveedor
     * Condiciones aplicadas:
     *   - WHERE telefono = ?: busca coincidencias exactas del número
     *   - AND proveedor_id != ?: excluye el proveedor que se está editando
     * Propósito: validar unicidad de teléfono ignorando el propio registro en contexto de actualización
     *
     * @param telefono número telefónico a comprobar (se aplica trim() en la consulta)
     * @param proveedorId ID del proveedor actual que se está editando (excluido del conteo)
     * @return {@code true} si otro proveedor distinto ya usa ese teléfono, {@code false} si está disponible
     */
    public boolean existeTelefonoParaOtro(String telefono, int proveedorId) {
        // SQL: Consulta de conteo para validar unicidad de teléfono excluyendo proveedor actual
        // Tabla: Telefono_Proveedor
        // WHERE telefono = ?: filtra por número específico
        // AND proveedor_id != ?: excluye registros del proveedor que se está editando
        String sql = "SELECT COUNT(*) FROM Telefono_Proveedor WHERE telefono = ? AND proveedor_id != ?";
        // try-with-resources para gestión automática de Connection y PreparedStatement
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Bind parámetros: 1=telefono con trim(), 2=proveedorId a excluir
            ps.setString(1, telefono.trim());
            ps.setInt(2, proveedorId);
            // Ejecuta consulta y evalúa resultado
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Verifica si un correo ya está registrado en CUALQUIER proveedor.
     * El correo se normaliza a minúsculas antes de comparar para evitar falsos negativos
     * por diferencias de capitalización (ej: "Correo@Example.com" vs "correo@example.com").
     *
     * Consulta SQL: SELECT COUNT con filtro exacto por email
     * Tabla involucrada: Correo_Proveedor
     * Condición: WHERE email = ? (búsqueda exacta, case-insensitive por normalización)
     * Pre-procesamiento: trim().toLowerCase() para normalizar entrada antes de comparar
     *
     * @param correo dirección de email a validar
     * @return {@code true} si el correo ya existe en algún proveedor, {@code false} si está disponible
     */
    public boolean existeCorreoProveedor(String correo) {
        // SQL: Consulta de conteo para validar unicidad global de correo electrónico
        // Tabla: Correo_Proveedor
        // WHERE email = ?: filtro parametrizado para búsqueda exacta
        // Normalización: toLowerCase() para evitar duplicados por diferencias de mayúsculas/minúsculas
        String sql = "SELECT COUNT(*) FROM Correo_Proveedor WHERE email = ?";
        // try-with-resources para gestión automática de recursos
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Bind parámetro con normalización completa: trim() + toLowerCase()
            ps.setString(1, correo.trim().toLowerCase());
            // Ejecuta consulta y evalúa resultado
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Verifica si un correo ya lo usa OTRO proveedor diferente al que se está editando.
     *
     * Consulta SQL: SELECT COUNT con dos condiciones
     * Tabla involucrada: Correo_Proveedor
     * Condiciones aplicadas:
     *   - WHERE email = ?: busca coincidencias exactas del correo (normalizado a minúsculas)
     *   - AND proveedor_id != ?: excluye el proveedor que se está editando
     * Propósito: validar unicidad de correo ignorando el propio registro en contexto de actualización
     *
     * @param correo dirección de email a comprobar
     * @param proveedorId ID del proveedor actual que se está editando (excluido del conteo)
     * @return {@code true} si otro proveedor distinto ya usa ese correo, {@code false} si está disponible
     */
    public boolean existeCorreoParaOtroProveedor(String correo, int proveedorId) {
        // SQL: Consulta de conteo para validar unicidad de correo excluyendo proveedor actual
        // Tabla: Correo_Proveedor
        // WHERE email = ?: filtra por correo específico (normalizado)
        // AND proveedor_id != ?: excluye registros del proveedor que se está editando
        String sql = "SELECT COUNT(*) FROM Correo_Proveedor WHERE email = ? AND proveedor_id != ?";
        // try-with-resources para gestión automática de recursos
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Bind parámetros: 1=correo normalizado, 2=proveedorId a excluir
            ps.setString(1, correo.trim().toLowerCase());
            ps.setInt(2, proveedorId);
            // Ejecuta consulta y evalúa resultado
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ==================== ESTADO Y ELIMINACIÓN ====================

    /**
     * Cambia el estado activo/inactivo de un proveedor sin eliminarlo físicamente.
     * Se usa desde el botón toggle de la lista de proveedores para activar/desactivar
     * relaciones comerciales sin perder historial de datos.
     *
     * Consulta SQL: UPDATE simple con filtro por clave primaria
     * Tabla afectada: Proveedor
     * Columna modificada: estado (boolean)
     * Condición: WHERE proveedor_id = ? (actualiza solo el registro específico)
     * Retorno: true si se actualizó al menos una fila, false si no se encontró el ID
     *
     * @param id identificador único {@code proveedor_id} del proveedor a modificar
     * @param estado nuevo valor booleano: true para activar, false para desactivar
     * @return {@code true} si se actualizó exitosamente una fila, {@code false} si no existía el registro
     */
    public boolean actualizarEstado(Integer id, Boolean estado) {
        // SQL: UPDATE para cambiar flag de estado en tabla Proveedor
        // Tabla destino: Proveedor
        // SET estado = ?: asigna nuevo valor booleano al campo de activación
        // WHERE proveedor_id = ?: filtra por PK para actualizar solo registro específico
        String sql = "UPDATE Proveedor SET estado = ? WHERE proveedor_id = ?";
        // try-with-resources para gestión automática de Connection y PreparedStatement
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Bind parámetros: 1=nuevo estado (boolean), 2=ID del proveedor a modificar
            stmt.setBoolean(1, estado);
            stmt.setInt(2, id);
            // Ejecuta UPDATE y retorna true si se modificó al menos una fila
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            // Registra error en consola para diagnóstico sin propagar excepción
            System.err.println("Error al actualizar estado: " + e.getMessage());
            return false;
        }
    }

    /**
     * RF13: Eliminación lógica — marca el proveedor como inactivo (estado = 0).
     * El registro no se borra físicamente de la base de datos, solo se desactiva.
     * Esto permite mantener el historial de compras, contactos y auditoría,
     * además de posibilitar reactivación futura si la relación comercial se reanuda.
     * Registra la operación en auditoría con la acción "ELIMINAR" para trazabilidad.
     *
     * Consulta SQL: UPDATE para cambio de estado (no DELETE físico)
     * Tabla afectada: Proveedor
     * Operación: SET estado = 0 (desactivación lógica)
     * Condición: WHERE proveedor_id = ? (afecta solo el registro específico)
     * Transacción: incluye registro de auditoría en misma transacción para consistencia
     *
     * @param id {@code proveedor_id} del proveedor a desactivar lógicamente
     * @param usuarioId ID del usuario que origina el registro de auditoría
     * @return {@code true} si la transacción completó exitosamente (UPDATE + auditoría),
     *         {@code false} si ocurrió error en cualquier paso
     */
    public boolean eliminar(Integer id, int usuarioId) {
        Connection conn = null;
        try {
            // Obtiene conexión manual para controlar explícitamente la transacción
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false);
            
            // Obtener nombre para dejarlo en el registro de auditoría
            // Consulta auxiliar: obtiene proveedor completo para extraer nombre legible
            // Si falla la consulta, usa fallback "ID:{id}" para no interrumpir eliminación
            String nombre = "ID:" + id;
            try {
                Proveedor p = obtenerPorId(id);
                if(p != null) nombre = p.getNombre();
            } catch(Exception e) {}

            // SQL: UPDATE para desactivación lógica (no eliminación física)
            // Tabla afectada: Proveedor
            // SET estado = 0: marca registro como inactivo sin borrar datos históricos
            // WHERE proveedor_id = ?: filtra por PK para afectar solo registro específico
            String sql = "UPDATE Proveedor SET estado = 0 WHERE proveedor_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

            // Registra auditoría con acción "ELIMINAR" y valor "ELIMINADO_LOGICO" para claridad
            // Esto permite distinguir en logs entre eliminación lógica y física (si existiera)
            registrarAuditoria(conn, usuarioId, "ELIMINAR", "Proveedor", id, nombre, "ELIMINADO_LOGICO");

            // Confirma transacción: hace permanentes UPDATE + registro de auditoría
            conn.commit();
            return true;
        } catch (Exception e) {
            // En caso de error, revierte todos los cambios para mantener consistencia
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) {} }
            return false;
        } finally {
            // Garantiza restauración de auto-commit y cierre de conexión incluso con excepción
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {} }
        }
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================

    /**
     * Convierte una fila del ResultSet en un objeto Proveedor.
     * Método helper privado para centralizar lógica de mapeo y evitar duplicación
     * de código entre listarProveedores(), obtenerPorId() y buscar().
     *
     * Flujo de mapeo de datos:
     *   1. Crea nueva instancia de Proveedor
     *   2. Mapea campos primitivos directamente desde ResultSet:
     *      - getInt() para proveedor_id (INT)
     *      - getString() para nombre, documento, fechas (VARCHAR/DATE)
     *      - getBoolean() para estado (BOOLEAN/TINYINT)
     *   3. Manejo especial para minimo_compra: lee como String primero para controlar
     *      posibles valores no numéricos o NULL, convirtiendo a Double con fallback a 0.0
     *      en caso de NumberFormatException o valor null
     *
     * Consideraciones técnicas:
     *   - No maneja excepciones SQL internamente; las propaga para que el llamador decida
     *   - Asume que el ResultSet está posicionado en una fila válida (rs.next() ya fue llamado)
     *   - Los nombres de columna en rs.getXXX() deben coincidir exactamente con alias en SQL
     *
     * @param rs fila actual del {@link ResultSet} con estructura de consultas de Proveedor
     * @return objeto {@link Proveedor} con campos principales poblados desde la fila
     * @throws SQLException si falta alguna columna esperada o hay error de conversión de tipos JDBC
     */
    private Proveedor mapearProveedor(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        // Mapea campos primitivos desde ResultSet usando getters tipo-safe de JDBC
        p.setProveedorId(rs.getInt("proveedor_id"));
        p.setNombre(rs.getString("nombre"));
        p.setDocumento(rs.getString("documento"));
        p.setFechaRegistro(rs.getString("fecha_registro"));
        p.setFechaInicio(rs.getString("fecha_inicio"));
        p.setEstado(rs.getBoolean("estado"));
        // Manejo especial para minimo_compra: lee como String para controlar errores de conversión
        String minStr = rs.getString("minimo_compra");
        // Si el valor no es un número válido o es null, se asigna 0.0 como valor seguro por defecto
        try { p.setMinimoCompra(minStr != null ? Double.parseDouble(minStr) : 0.0); } 
        catch (NumberFormatException e) { p.setMinimoCompra(0.0); }
        return p;
    }

    /**
     * Obtiene la lista de teléfonos asociados a un proveedor específico.
     *
     * Consulta SQL: SELECT simple sobre tabla Telefono_Proveedor
     * Tabla involucrada: Telefono_Proveedor (relación uno-a-muchos con Proveedor)
     * Condición: WHERE proveedor_id = ? (filtra contactos del proveedor especificado)
     * Columna seleccionada: telefono (VARCHAR)
     * Retorno: lista de Strings con números telefónicos, o lista vacía si no hay contactos
     *
     * @param proveedorId ID del proveedor para filtrar sus teléfonos asociados
     * @return lista de números de teléfono (puede estar vacía si no hay registros o hay error)
     */
    private List<String> obtenerTelefonos(Integer proveedorId) {
        List<String> lista = new ArrayList<>();
        // SQL: SELECT de teléfonos para un proveedor específico
        // Tabla: Telefono_Proveedor
        // WHERE proveedor_id = ?: filtro parametrizado por FK hacia Proveedor
        String sql = "SELECT telefono FROM Telefono_Proveedor WHERE proveedor_id = ?";
        // try-with-resources para gestión automática de recursos
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, proveedorId);
            try (ResultSet rs = stmt.executeQuery()) {
                // Itera sobre resultados agregando cada teléfono a la lista
                while (rs.next()) lista.add(rs.getString("telefono"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        // Retorna lista poblada; si no hay datos o hay error, retorna lista vacía (nunna null)
        return lista;
    }

    /**
     * Obtiene la lista de correos electrónicos asociados a un proveedor específico.
     *
     * Consulta SQL: SELECT simple sobre tabla Correo_Proveedor
     * Tabla involucrada: Correo_Proveedor (relación uno-a-muchos con Proveedor)
     * Condición: WHERE proveedor_id = ? (filtra contactos del proveedor especificado)
     * Columna seleccionada: email (VARCHAR)
     * Retorno: lista de Strings con direcciones de email, o lista vacía si no hay registros
     *
     * @param proveedorId ID del proveedor para filtrar sus correos asociados
     * @return lista de direcciones de email (puede estar vacía si no hay registros o hay error)
     */
    private List<String> obtenerCorreos(Integer proveedorId) {
        List<String> lista = new ArrayList<>();
        // SQL: SELECT de correos para un proveedor específico
        // Tabla: Correo_Proveedor
        // WHERE proveedor_id = ?: filtro parametrizado por FK hacia Proveedor
        String sql = "SELECT email FROM Correo_Proveedor WHERE proveedor_id = ?";
        // try-with-resources para gestión automática de recursos
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, proveedorId);
            try (ResultSet rs = stmt.executeQuery()) {
                // Itera sobre resultados agregando cada email a la lista
                while (rs.next()) lista.add(rs.getString("email"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    /**
     * Obtiene la lista de materiales que suministra un proveedor específico.
     *
     * Consulta SQL: SELECT con INNER JOIN entre Material y Proveedor_Material
     * Tablas involucradas:
     *   - Material (alias 'm'): tabla maestra con definiciones de materiales
     *   - Proveedor_Material (alias 'pm'): tabla intermedia que define relaciones válidas
     * Tipo de JOIN: INNER JOIN para retornar solo materiales con relación explícita
     * Condición del JOIN: m.material_id = pm.material_id (relación por clave foránea)
     * Condición WHERE: pm.proveedor_id = ? (filtra por proveedor específico)
     * Columnas seleccionadas:
     *   - m.material_id: PK del material para identificación técnica
     *   - m.nombre: nombre legible para presentación en UI
     * Retorno: lista de objetos Material, o lista vacía si no hay relaciones o hay error
     *
     * @param proveedorId ID del proveedor para filtrar materiales que suministra
     * @return lista de objetos Material vinculados (puede estar vacía si no hay relaciones o hay error)
     */
    private List<Material> obtenerMateriales(Integer proveedorId) {
        List<Material> lista = new ArrayList<>();
        // SQL: Consulta con JOIN para recuperar materiales asociados a un proveedor
        // Tabla principal: Material (m) → contiene datos legibles de cada material
        // Tabla de relación: Proveedor_Material (pm) → define qué proveedores suministran cada material
        // INNER JOIN: garantiza que solo se retornen materiales con relación explícita en tabla intermedia
        // WHERE pm.proveedor_id = ?: filtra por el proveedor solicitado (parámetro bindado)
        String sql = """
            SELECT m.material_id, m.nombre FROM Material m
            INNER JOIN Proveedor_Material pm ON m.material_id = pm.material_id
            WHERE pm.proveedor_id = ?
            """;
        // try-with-resources para gestión automática de recursos
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, proveedorId);
            try (ResultSet rs = stmt.executeQuery()) {
                // Itera sobre resultados mapeando cada fila a objeto Material
                while (rs.next()) {
                    Material m = new Material();
                    m.setMaterialId(rs.getInt("material_id"));
                    m.setNombre(rs.getString("nombre"));
                    lista.add(m);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    /**
     * Elimina todos los teléfonos asociados a un proveedor específico.
     * Método auxiliar privado usado dentro de transacciones de actualización.
     *
     * Consulta SQL: DELETE en tabla Telefono_Proveedor con condición por FK
     * Tabla afectada: Telefono_Proveedor
     * Condición: WHERE proveedor_id = ? (elimina solo contactos del proveedor especificado)
     * Propósito: limpiar registros anteriores antes de insertar nueva lista de teléfonos
     *
     * @param proveedorId ID del proveedor cuyos teléfonos se eliminarán
     * @param conn conexión activa dentro de transacción en curso (para consistencia atómica)
     * @throws SQLException si falla el DELETE por errores de conexión o constraints de BD
     */
    private void eliminarTelefonos(Integer proveedorId, Connection conn) throws SQLException {
        // SQL: DELETE para remover todos los teléfonos de un proveedor específico
        // Tabla afectada: Telefono_Proveedor
        // WHERE proveedor_id = ?: condición parametrizada para afectar solo registros del proveedor
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Telefono_Proveedor WHERE proveedor_id = ?")) {
            stmt.setInt(1, proveedorId);
            stmt.executeUpdate();
        }
    }

    /**
     * Elimina todos los correos asociados a un proveedor específico.
     * Método auxiliar privado usado dentro de transacciones de actualización.
     *
     * Consulta SQL: DELETE en tabla Correo_Proveedor con condición por FK
     * Tabla afectada: Correo_Proveedor
     * Condición: WHERE proveedor_id = ? (elimina solo contactos del proveedor especificado)
     * Propósito: limpiar registros anteriores antes de insertar nueva lista de correos
     *
     * @param proveedorId ID del proveedor cuyos correos se eliminarán
     * @param conn conexión activa dentro de transacción en curso (para consistencia atómica)
     * @throws SQLException si falla el DELETE por errores de conexión o constraints de BD
     */
    private void eliminarCorreos(Integer proveedorId, Connection conn) throws SQLException {
        // SQL: DELETE para remover todos los correos de un proveedor específico
        // Tabla afectada: Correo_Proveedor
        // WHERE proveedor_id = ?: condición parametrizada para afectar solo registros del proveedor
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Correo_Proveedor WHERE proveedor_id = ?")) {
            stmt.setInt(1, proveedorId);
            stmt.executeUpdate();
        }
    }

    /**
     * Elimina todas las relaciones de materiales asociadas a un proveedor específico.
     * Método auxiliar privado usado dentro de transacciones de actualización.
     *
     * Consulta SQL: DELETE en tabla Proveedor_Material con condición por FK
     * Tabla afectada: Proveedor_Material (tabla intermedia de relación muchos-a-muchos)
     * Condición: WHERE proveedor_id = ? (elimina solo relaciones del proveedor especificado)
     * Propósito: limpiar asociaciones anteriores antes de insertar nueva lista de materiales
     *
     * @param proveedorId ID del proveedor cuyas relaciones de materiales se eliminarán
     * @param conn conexión activa dentro de transacción en curso (para consistencia atómica)
     * @throws SQLException si falla el DELETE por errores de conexión o constraints de BD
     */
    private void eliminarMateriales(Integer proveedorId, Connection conn) throws SQLException {
        // SQL: DELETE para remover todas las relaciones de materiales de un proveedor
        // Tabla afectada: Proveedor_Material (tabla intermedia Proveedor↔Material)
        // WHERE proveedor_id = ?: condición parametrizada para afectar solo relaciones del proveedor
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Proveedor_Material WHERE proveedor_id = ?")) {
            stmt.setInt(1, proveedorId);
            stmt.executeUpdate();
        }
    }

    /**
     * Registra una acción en la tabla de auditoría para trazabilidad de operaciones.
     * Los datos anteriores y nuevos se guardan en formato JSON simple para facilitar
     * lectura humana y parsing posterior si se requiere análisis programático.
     * Las comillas en los valores se escapan para evitar JSON inválido.
     * Esta función recibe la conexión activa para que el registro quede dentro de
     * la misma transacción que la operación principal, garantizando atomicidad.
     *
     * Consulta SQL: INSERT en tabla Auditoria_Log
     * Tabla destino: Auditoria_Log (bitácora centralizada de eventos del sistema)
     * Columnas insertadas:
     *   - usuario_id: FK hacia Usuario para trazabilidad de quién ejecutó la acción
     *   - accion: tipo de operación ("CREAR", "EDITAR", "ELIMINAR", etc.)
     *   - entidad: nombre lógico de la entidad afectada ("Proveedor", "Producto", etc.)
     *   - entidad_id: ID del registro específico modificado para referencia directa
     *   - datos_anteriores: JSON simple {"valor": "..."} con estado previo (nullable)
     *   - datos_nuevos: JSON simple {"valor": "..."} con estado posterior (nullable)
     *   - fecha_hora: NOW() para timestamp automático del servidor de BD
     * Pre-procesamiento: replace("\"", "\\\"") para escapar comillas en valores y mantener JSON válido
     *
     * @param conn conexión abierta dentro de transacción en curso (para consistencia atómica)
     * @param usuarioId ID del usuario que ejecuta la acción para trazabilidad
     * @param accion texto descriptivo de la operación ("CREAR", "EDITAR", "ELIMINAR", etc.)
     * @param entidad nombre lógico de la entidad afectada ("Proveedor", "Producto", etc.)
     * @param entidadId ID del registro específico modificado para referencia directa
     * @param datosAnteriores JSON simple con valor anterior o {@code null} si no aplica
     * @param datosNuevos JSON simple con valor nuevo o {@code null} si no aplica
     * @throws SQLException si falla el INSERT por errores de conexión, constraints o sintaxis SQL
     */
    private void registrarAuditoria(Connection conn, int usuarioId, String accion, String entidad, 
            int entidadId, String datosAnteriores, String datosNuevos) throws SQLException {
        // SQL: INSERT para registrar evento de auditoría en tabla centralizada
        // Tabla destino: Auditoria_Log (bitácora de trazabilidad de operaciones del sistema)
        // Columnas: usuario_id (FK), accion, entidad, entidad_id, datos_anteriores, datos_nuevos, fecha_hora
        // Valores parametrizados (?, ?, ?, ?, ?, ?) para seguridad y reutilización de plan
        // NOW(): función de BD para timestamp automático de creación del registro
        String sql = """
			INSERT INTO Auditoria_Log(usuario_id, accion, entidad, entidad_id, datos_anteriores, datos_nuevos, fecha_hora)
			VALUES(?, ?, ?, ?, ?, ?, NOW())
			""";
        // PreparedStatement para ejecutar INSERT con parámetros seguros
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Bind parámetros en orden definido en la consulta SQL:
            // 1=usuario_id, 2=accion, 3=entidad, 4=entidad_id
            stmt.setInt(1, usuarioId);
            stmt.setString(2, accion);
            stmt.setString(3, entidad);
            stmt.setInt(4, entidadId);
            // Parámetro 5: datos_anteriores como JSON simple, con escape de comillas para validez JSON
            // Si es null, se envía NULL SQL para campo nullable en esquema de BD
            stmt.setString(5, datosAnteriores != null 
			? "{\"valor\": \"" + datosAnteriores.replace("\"", "\\\"") + "\"}" 
			: null);
            // Parámetro 6: datos_nuevos como JSON simple, con mismo tratamiento de escape
            stmt.setString(6, datosNuevos != null 
			? "{\"valor\": \"" + datosNuevos.replace("\"", "\\\"") + "\"}" 
			: null);
            // Ejecuta INSERT para persistir registro de auditoría; se espera 1 fila afectada
            stmt.executeUpdate();
        }
    }
}