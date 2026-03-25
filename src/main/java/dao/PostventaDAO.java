package dao;

import config.ConexionDB;
import model.CasoPostventa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO encargado de gestionar todas las operaciones relacionadas con los casos
 * de postventa en la base de datos. Esta clase centraliza la consulta, registro
 * y actualización de casos de postventa (devoluciones, cambios, garantías),
 * integrando información de ventas, clientes, vendedores, productos e historial
 * de observaciones para proporcionar una vista completa en la interfaz de administración.
 * Utiliza ConexionDB para gestión de conexiones y PreparedStatement para prevenir
 * SQL Injection y optimizar la ejecución de consultas complejas con JOINs y subconsultas.
 */
public class PostventaDAO {

    /**
     * Consulta base reutilizable que une Caso_Postventa, Venta, Usuario, Cliente,
     * Producto (mediante subconsultas) e Historial (última observación).
     *
     * Consulta SQL: SELECT complejo con múltiples LEFT JOINs y subconsultas correlacionadas
     * Tablas principales involucradas:
     *   - Caso_Postventa (alias 'cp'): tabla central con datos del caso de postventa
     *   - Venta (alias 'v'): LEFT JOIN para obtener datos de la venta original asociada
     *   - Usuario (alias 'uv'): LEFT JOIN para obtener nombre del vendedor que realizó la venta
     *   - Cliente (alias 'cl'): LEFT JOIN para obtener nombre del cliente afectado
     *   - Historial_Caso_Postventa (alias 'h'): LEFT JOIN con subconsulta para obtener la última observación
     *
     * Subconsultas para producto (ya que Producto no tiene relación directa con Caso_Postventa):
     *   - (SELECT dv2.producto_id FROM Detalle_Venta dv2 WHERE dv2.venta_id = v.venta_id LIMIT 1)
     *     Obtiene el ID del primer producto asociado a la venta (asume venta con un solo producto o toma el primero)
     *   - (SELECT p2.nombre FROM Detalle_Venta dv2 LEFT JOIN Producto p2 ON p2.producto_id = dv2.producto_id
     *      WHERE dv2.venta_id = v.venta_id LIMIT 1)
     *     Obtiene el nombre legible del producto mediante JOIN con tabla Producto
     *
     * Lógica para última observación del historial:
     *   LEFT JOIN Historial_Caso_Postventa h ON h.historial_id = (
     *       SELECT MAX(h2.historial_id) FROM Historial_Caso_Postventa h2 WHERE h2.caso_id = cp.caso_id
     *   )
     *   Esta subconsulta correlacionada obtiene el historial_id máximo (más reciente) para cada caso,
     *   permitiendo mostrar solo la última observación registrada sin duplicar filas del caso principal.
     *
     * Columnas seleccionadas:
     *   - cp.*: campos básicos del caso (caso_id, venta_id, tipo, cantidad, motivo, fecha, estado)
     *   - v.usuario_id AS vendedor_id: ID del vendedor que procesó la venta original
     *   - uv.nombre AS vendedor_nombre: nombre legible del vendedor para presentación en UI
     *   - v.cliente_id, cl.nombre AS cliente_nombre: datos del cliente afectado por el caso
     *   - producto_id, producto_nombre: datos del producto involucrado (obtenidos vía subconsultas)
     *   - h.observacion AS observacion: texto de la última observación del historial del caso
     *
     * Nota: Todos los JOIN son LEFT JOIN para conservar casos incluso si faltan datos relacionados
     * (ej: venta eliminada, usuario inactivo, sin historial aún), evitando pérdida de registros en listados.
     */
    private static final String SQL_BASE = """
        SELECT 
            cp.caso_id,
            cp.venta_id,
            cp.tipo,
            cp.cantidad,
            cp.motivo,
            cp.fecha,
            cp.estado,

            -- ID del vendedor (usuario que realizó la venta)
            v.usuario_id AS vendedor_id,

            -- Nombre del vendedor
            uv.nombre AS vendedor_nombre,

            -- ID del cliente
            v.cliente_id,

            -- Nombre del cliente
            cl.nombre AS cliente_nombre,

            -- Subconsulta para obtener el ID del producto asociado a la venta
            (SELECT dv2.producto_id FROM Detalle_Venta dv2 WHERE dv2.venta_id = v.venta_id LIMIT 1) AS producto_id,

            -- Subconsulta para obtener el nombre del producto
            (SELECT p2.nombre FROM Detalle_Venta dv2 
             LEFT JOIN Producto p2 ON p2.producto_id = dv2.producto_id
             WHERE dv2.venta_id = v.venta_id LIMIT 1) AS producto_nombre,

            -- Última observación del historial del caso
            h.observacion AS observacion

        FROM Caso_Postventa cp

        -- Relación con la venta
        LEFT JOIN Venta v ON v.venta_id = cp.venta_id

        -- Relación con el usuario (vendedor)
        LEFT JOIN Usuario uv ON uv.usuario_id = v.usuario_id

        -- Relación con el cliente
        LEFT JOIN Cliente cl ON cl.cliente_id = v.cliente_id

        -- Obtener el último registro del historial del caso
        LEFT JOIN Historial_Caso_Postventa h
            ON h.historial_id = (
                SELECT MAX(h2.historial_id)
                FROM Historial_Caso_Postventa h2
                WHERE h2.caso_id = cp.caso_id
            )
        """;

    /**
     * Lista los casos de postventa asociados a un vendedor específico, ordenados
     * por fecha descendente para mostrar primero los casos más recientes.
     *
     * Consulta SQL: SQL_BASE + filtro WHERE por vendedor_id + ordenamiento
     * Tablas involucradas: mismas que SQL_BASE (Caso_Postventa, Venta, Usuario, Cliente,
     * Detalle_Venta, Producto, Historial_Caso_Postventa)
     * Condición aplicada: WHERE v.usuario_id = ? (filtra casos del vendedor especificado)
     * Ordenamiento: ORDER BY cp.fecha DESC, cp.caso_id DESC (prioriza fecha, desempata por ID)
     * Parámetro: vendedorId (int) → se binda al primer placeholder (?) mediante PreparedStatement
     *
     * Flujo de datos:
     *   1. Ejecuta consulta parametrizada con filtro por vendedor
     *   2. Itera sobre ResultSet creando objeto CasoPostventa por cada fila
     *   3. Delega mapeo de columnas a método privado mapearCaso() para reutilización
     *   4. Retorna lista poblada o vacía si no hay casos para ese vendedor
     *
     * @param vendedorId ID del usuario vendedor para filtrar casos asociados
     * @return lista de objetos CasoPostventa con datos completos (caso, venta, cliente, producto, última observación)
     * @throws Exception si falla la consulta por errores de conexión, sintaxis SQL o acceso a datos
     */
    public List<CasoPostventa> listarPorVendedor(int vendedorId) throws Exception {

        List<CasoPostventa> lista = new ArrayList<>();

        // Se añade filtro por vendedor y orden descendente
        // SQL: SQL_BASE + WHERE v.usuario_id = ? + ORDER BY fecha/caso_id DESC
        // Filtra solo casos pertenecientes al vendedor especificado para vistas personales
        String sql = SQL_BASE + " WHERE v.usuario_id = ? ORDER BY cp.fecha DESC, cp.caso_id DESC";

        try (Connection con = ConexionDB.getConnection()) {

            PreparedStatement ps = con.prepareStatement(sql);

            // Se asigna el ID del vendedor al primer parámetro de la consulta parametrizada
            ps.setInt(1, vendedorId);

            try (ResultSet rs = ps.executeQuery()) {

                // Se recorren los resultados y se mapean a objetos CasoPostventa
                // Cada llamada a mapearCaso() transforma una fila del ResultSet en un objeto de dominio
                while (rs.next()) lista.add(mapearCaso(rs));
            }

            ps.close();
        }

        return lista;
    }

    /**
     * Lista todos los casos de postventa del sistema sin filtros, ordenados por
     * fecha descendente para administración global.
     *
     * Consulta SQL: SQL_BASE + ordenamiento sin filtros
     * Tablas involucradas: mismas que SQL_BASE (Caso_Postventa, Venta, Usuario, Cliente,
     * Detalle_Venta, Producto, Historial_Caso_Postventa)
     * Sin condición WHERE: recupera todos los casos registrados en el sistema
     * Ordenamiento: ORDER BY cp.fecha DESC, cp.caso_id DESC (más recientes primero)
     *
     * Manejo de excepciones: captura SQLException específicamente para registrar mensaje
     * de error en consola antes de propagar la excepción, facilitando diagnóstico en producción.
     *
     * @return lista de todos los objetos CasoPostventa registrados, ordenados por fecha descendente
     * @throws Exception si falla la consulta por errores de conexión, sintaxis SQL o acceso a datos
     */
    public List<CasoPostventa> listarTodos() throws Exception {

        List<CasoPostventa> lista = new ArrayList<>();

        // Orden descendente por fecha e ID para presentar casos más recientes primero en vistas de admin
        String sql = SQL_BASE + " ORDER BY cp.fecha DESC, cp.caso_id DESC";

        try (Connection con = ConexionDB.getConnection()) {

            PreparedStatement ps = con.prepareStatement(sql);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) lista.add(mapearCaso(rs));
            }

            ps.close();

        } catch (SQLException e) {

            // Manejo de error: registra mensaje descriptivo en consola para diagnóstico antes de propagar
            System.err.println("Error SQL en listarTodos (PostventaDAO): " + e.getMessage());
            throw e;
        }

        return lista;
    }

    /**
     * Lista los casos de postventa asociados a una venta específica.
     * Usa una consulta SQL propia para evitar conflictos de alias con SQL_BASE
     * cuando se filtra directamente por venta_id en la cláusula WHERE.
     *
     * Consulta SQL: SELECT personalizado con misma estructura que SQL_BASE pero con alias 'ven' para Venta
     * (evita ambigüedad al usar SQL_BASE que ya referencia 'v' en subconsultas de producto)
     * Tablas involucradas: Caso_Postventa, Venta (alias 'ven'), Usuario, Cliente, Detalle_Venta,
     * Producto, Historial_Caso_Postventa
     * Condición aplicada: WHERE cp.venta_id = ? (filtra casos de una venta específica)
     * Ordenamiento: ORDER BY cp.fecha DESC, cp.caso_id DESC (más recientes primero)
     * Parámetro: ventaId (int) → bindado al primer placeholder (?) mediante PreparedStatement
     *
     * Subconsultas para producto: mismas que SQL_BASE, obteniendo producto_id y nombre desde
     * Detalle_Venta + Producto con LIMIT 1 para manejar ventas con múltiples productos (toma el primero).
     *
     * LEFT JOIN para historial: misma lógica correlacionada con MAX(historial_id) para obtener
     * solo la observación más reciente sin duplicar filas del caso principal.
     *
     * @param ventaId ID de la venta para filtrar casos de postventa asociados
     * @return lista de objetos CasoPostventa relacionados con esa venta, ordenados por fecha descendente
     * @throws Exception si falla la consulta por errores de conexión, sintaxis SQL o acceso a datos
     */
    public List<CasoPostventa> listarPorVenta(int ventaId) throws Exception {

        List<CasoPostventa> lista = new ArrayList<>();

        // Consulta personalizada con alias 'ven' para Venta (evita conflicto con subconsultas que usan 'v')
        // WHERE cp.venta_id = ?: filtra casos pertenecientes a una venta específica
        // ORDER BY: presenta casos más recientes primero para mejor experiencia en detalle de venta
        String sql = """
            SELECT 
                cp.caso_id,
                cp.venta_id,
                cp.tipo,
                cp.cantidad,
                cp.motivo,
                cp.fecha,
                cp.estado,

                -- Nombre del vendedor
                uv.nombre AS vendedor_nombre,

                -- Nombre del cliente
                cl.nombre AS cliente_nombre,

                -- Producto asociado (subconsulta)
                (SELECT dv2.producto_id FROM Detalle_Venta dv2 
                 WHERE dv2.venta_id = cp.venta_id LIMIT 1) AS producto_id,

                -- Nombre del producto
                (SELECT p2.nombre FROM Detalle_Venta dv2 
                 LEFT JOIN Producto p2 ON p2.producto_id = dv2.producto_id
                 WHERE dv2.venta_id = cp.venta_id LIMIT 1) AS producto_nombre,

                -- Última observación del historial
                h.observacion AS observacion

            FROM Caso_Postventa cp

            -- Relación con venta (alias 'ven' para evitar conflicto con subconsultas)
            LEFT JOIN Venta ven ON ven.venta_id = cp.venta_id

            -- Relación con vendedor
            LEFT JOIN Usuario uv ON uv.usuario_id = ven.usuario_id

            -- Relación con cliente
            LEFT JOIN Cliente cl ON cl.cliente_id = ven.cliente_id

            -- Último historial (subconsulta correlacionada con MAX para obtener observación más reciente)
            LEFT JOIN Historial_Caso_Postventa h
                ON h.historial_id = (
                    SELECT MAX(h2.historial_id)
                    FROM Historial_Caso_Postventa h2
                    WHERE h2.caso_id = cp.caso_id
                )

            WHERE cp.venta_id = ?

            ORDER BY cp.fecha DESC, cp.caso_id DESC
            """;

        // try-with-resources para gestión automática de Connection y PreparedStatement
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Bind del parámetro: asigna ventaId al primer placeholder (?) para filtrar por venta
            ps.setInt(1, ventaId);

            try (ResultSet rs = ps.executeQuery()) {

                // Itera sobre resultados mapeando cada fila a objeto CasoPostventa mediante helper
                while (rs.next()) lista.add(mapearCaso(rs));
            }
        }

        return lista;
    }

    /**
     * Obtiene un caso de postventa específico por su identificador único.
     *
     * Consulta SQL: SQL_BASE + filtro WHERE por caso_id
     * Tablas involucradas: mismas que SQL_BASE (Caso_Postventa, Venta, Usuario, Cliente,
     * Detalle_Venta, Producto, Historial_Caso_Postventa)
     * Condición: WHERE cp.caso_id = ? (búsqueda por PK, eficiente con índice)
     * Parámetro: casoId (int) → bindado al primer placeholder (?) mediante PreparedStatement
     * Retorno esperado: máximo 1 fila (por clave primaria única)
     *
     * Flujo de mapeo: si rs.next() es true, delega a mapearCaso() para transformar la fila
     * en objeto CasoPostventa; si no hay resultados, retorna null indicando caso no encontrado.
     *
     * @param casoId valor de {@code caso_id} a buscar
     * @return objeto CasoPostventa completamente poblado con datos relacionados, o {@code null} si no existe
     * @throws Exception si falla la consulta por errores de conexión, sintaxis SQL o acceso a datos
     */
    public CasoPostventa obtenerPorId(int casoId) throws Exception {

        // SQL: SQL_BASE + WHERE cp.caso_id = ? para búsqueda eficiente por clave primaria
        String sql = SQL_BASE + " WHERE cp.caso_id = ?";

        // try-with-resources para gestión automática de recursos de base de datos
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Bind del parámetro: asigna casoId al primer placeholder (?) de la consulta
            ps.setInt(1, casoId);

            try (ResultSet rs = ps.executeQuery()) {

                // next() retorna true solo si se encontró el caso; delega mapeo a método helper
                if (rs.next()) return mapearCaso(rs);
            }
        }

        // Retorna null si no se encontró el caso o hubo error no capturado
        return null;
    }

    /**
     * Registra un nuevo caso de postventa en la base de datos y retorna el ID generado.
     *
     * Consulta SQL: INSERT en tabla Caso_Postventa con retorno de claves generadas
     * Tabla destino: Caso_Postventa (almacena casos de devolución, cambio, garantía, etc.)
     * Columnas insertadas:
     *   - venta_id: FK hacia Venta para vincular caso con orden original
     *   - tipo: categoría del caso ('devolucion', 'cambio', 'garantia', etc.)
     *   - cantidad: número de unidades afectadas por el caso
     *   - motivo: descripción textual del motivo del caso (nullable, se usa "" si es null)
     *   - fecha: NOW() función de BD para timestamp automático de creación
     *   - estado: valor inicial 'en_proceso' para nuevos casos
     * Configuración: Statement.RETURN_GENERATED_KEYS para recuperar caso_id autogenerado
     *
     * Flujo de retorno:
     *   1. Ejecuta INSERT y verifica que se afectó al menos una fila
     *   2. Obtiene ResultSet con claves generadas mediante ps.getGeneratedKeys()
     *   3. Extrae y retorna el primer valor (caso_id) si está disponible
     *   4. Retorna -1 como fallback si no se pudo obtener la clave generada
     *
     * Manejo de excepciones: captura SQLException para registrar mensaje en consola
     * antes de propagar la excepción original, facilitando diagnóstico sin ocultar el error.
     *
     * @param caso objeto CasoPostventa con venta_id, tipo, cantidad y motivo poblados para persistir
     * @return valor de {@code caso_id} generado automáticamente por la base de datos, o -1 si falla la obtención
     * @throws Exception si falla el INSERT por errores de conexión, constraints de BD o sintaxis SQL
     */
    public int registrar(CasoPostventa caso) throws Exception {

        // SQL: INSERT para crear nuevo caso en tabla Caso_Postventa
        // Columnas: venta_id (FK), tipo, cantidad, motivo, fecha (NOW()), estado ('en_proceso')
        // Valores parametrizados (?, ?, ?, ?) para seguridad y reutilización de plan
        // Configuración RETURN_GENERATED_KEYS: recupera caso_id autogenerado tras INSERT
        String sql = "INSERT INTO Caso_Postventa(venta_id, tipo, cantidad, motivo, fecha, estado) VALUES(?, ?, ?, ?, NOW(), 'en_proceso')";

        // try-with-resources con configuración para retorno de claves generadas
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Bind parámetros en orden definido en la consulta:
            // 1=venta_id, 2=tipo, 3=cantidad, 4=motivo (con fallback a "" si es null)
            ps.setInt(1, caso.getVentaId());
            ps.setString(2, caso.getTipo());
            ps.setInt(3, caso.getCantidad());
            ps.setString(4, caso.getMotivo() != null ? caso.getMotivo() : "");

            // Ejecuta INSERT y captura número de filas afectadas (esperado: 1 para éxito)
            int filas = ps.executeUpdate();

            // Obtener ID generado automáticamente si la inserción fue exitosa
            if (filas > 0) {
                // getGeneratedKeys() retorna ResultSet con claves autogeneradas por la BD
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    // next() avanza a la primera fila del resultado de claves
                    if (keys.next()) return keys.getInt(1);
                }
            }

            // Fallback: retorna -1 si no se pudo obtener la clave generada
            return -1;

        } catch (SQLException e) {

            // Registra mensaje de error en consola para diagnóstico antes de propagar
            System.err.println("Error al registrar caso postventa: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Actualiza el estado de un caso de postventa y registra el cambio en el historial
     * dentro de una transacción atómica. Si el caso es una devolución aprobada, adicionalmente
     * ejecuta lógica de negocio para retornar stock al inventario.
     *
     * Consultas SQL involucradas:
     *   1. UPDATE Caso_Postventa SET estado = ? WHERE caso_id = ?
     *      - Tabla: Caso_Postventa
     *      - Operación: actualiza solo el campo estado del caso específico
     *      - Condición: WHERE caso_id = ? (filtra por PK para modificar solo el caso objetivo)
     *
     *   2. INSERT INTO Historial_Caso_Postventa(caso_id, estado, observacion, usuario_id, fecha)
     *      VALUES(?,?,?,?,NOW())
     *      - Tabla: Historial_Caso_Postventa (bitácora de cambios de estado por caso)
     *      - Columnas: caso_id (FK), nuevo estado, observación textual, usuario que realizó el cambio,
     *                  fecha automática con NOW()
     *      - Propósito: mantener trazabilidad completa de evoluciones de estado para auditoría
     *
     * Lógica de negocio condicional:
     *   - Si el caso es de tipo 'devolucion' Y el nuevo estado es 'aprobado':
     *     • Recupera datos completos del caso mediante obtenerPorId()
     *     • Invoca VentaDAO().retornarStockDevolucion() para incrementar inventario
     *       del producto devuelto, manteniendo consistencia entre ventas y stock
     *
     * Gestión transaccional:
     *   - Desactiva auto-commit al inicio para controlar manualmente la transacción
     *   - Ejecuta UPDATE + INSERT + lógica de inventario en secuencia atómica
     *   - Confirma con con.commit() solo si todos los pasos completan exitosamente
     *   - Revierte con con.rollback() si ocurre cualquier excepción, evitando estados inconsistentes
     *   - Finalmente restaura auto-commit a true para reutilización segura de la conexión
     *
     * @param casoId ID del caso de postventa a actualizar
     * @param nuevoEstado nuevo valor de estado para el caso ('aprobado', 'rechazado', 'en_proceso', etc.)
     * @param observación texto descriptivo del cambio para registrar en historial (nullable, se usa "" si es null)
     * @param usuarioId ID del usuario que realiza la actualización para trazabilidad en historial
     * @return {@code true} si se completó exitosamente la transacción completa, {@code false} en caso contrario
     * @throws Exception si falla cualquier paso (UPDATE, INSERT, lógica de inventario); se propaga tras rollback
     */
    public boolean actualizarEstado(int casoId, String nuevoEstado, String observacion, int usuarioId) throws Exception {

        // SQL 1: UPDATE para cambiar estado del caso en tabla Caso_Postventa
        // WHERE caso_id = ?: filtra por PK para actualizar solo el caso específico
        final String sqlUpdate    = "UPDATE Caso_Postventa SET estado = ? WHERE caso_id = ?";
        
        // SQL 2: INSERT para registrar cambio en bitácora de historial
        // Columnas: caso_id (FK), estado, observacion, usuario_id (FK), fecha (NOW())
        final String sqlHistorial = "INSERT INTO Historial_Caso_Postventa(caso_id, estado, observacion, usuario_id, fecha) VALUES(?,?,?,?,NOW())";

        try (Connection con = ConexionDB.getConnection()) {

            // Se inicia una transacción manual desactivando auto-commit
            con.setAutoCommit(false);

            try {

                // Actualizar estado del caso: ejecuta UPDATE parametrizado
                try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                    ps.setString(1, nuevoEstado);
                    ps.setInt(2, casoId);
                    ps.executeUpdate();
                }

                // Registrar en historial: ejecuta INSERT parametrizado para trazabilidad
                try (PreparedStatement ps = con.prepareStatement(sqlHistorial)) {
                    ps.setInt(1, casoId);
                    ps.setString(2, nuevoEstado);
                    ps.setString(3, observacion != null ? observacion : "");
                    ps.setInt(4, usuarioId);
                    ps.executeUpdate();
                }

                // Verificar si es devolución aprobada para ejecutar lógica de retorno de inventario
                CasoPostventa caso = obtenerPorId(casoId);

                if (caso != null && "devolucion".equals(caso.getTipo()) && "aprobado".equals(nuevoEstado)) {

                    // Retornar stock al inventario: llamada a método de VentaDAO para incrementar
                    // stock del producto devuelto, manteniendo consistencia entre ventas e inventario
                    new VentaDAO().retornarStockDevolucion(
                        caso.getVentaId(), caso.getProductoId(), caso.getCantidad(), usuarioId
                    );
                }

                // Confirmar transacción: hace permanentes todos los cambios si todo fue exitoso
                con.commit();

                return true;

            } catch (Exception e) {

                // Revertir cambios si ocurre error en cualquier paso para mantener integridad de datos
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Convierte una fila de ResultSet en un objeto CasoPostventa completamente poblado.
     * Método helper privado para centralizar lógica de mapeo y evitar duplicación de código
     * entre los distintos métodos de consulta que comparten la misma estructura de resultado.
     *
     * Flujo de mapeo de datos:
     *   1. Crea nueva instancia de CasoPostventa
     *   2. Mapea campos básicos desde tabla Caso_Postventa (caso_id, venta_id, tipo, cantidad,
     *      motivo, fecha, estado) usando getters apropiados del ResultSet
     *   3. Mapea datos relacionados obtenidos vía JOINs: vendedor_nombre, cliente_nombre, observacion
     *   4. Maneja producto_id como nullable: verifica rs.getObject() != null antes de rs.getInt()
     *      para evitar conversión automática de NULL a 0, preservando semántica de "sin producto"
     *   5. Mapea producto_nombre directamente (puede ser null si no hay producto asociado)
     *   6. Retorna objeto completamente poblado listo para uso en capa de presentación o negocio
     *
     * Consideraciones técnicas:
     *   - Usa rs.getInt() para campos INT, rs.getString() para VARCHAR, rs.getDate() para DATE
     *   - Manejo explícito de null en producto_id mediante rs.getObject() para distinguir
     *     entre "valor 0" y "sin valor" en la lógica de negocio
     *   - No maneja excepciones SQL internamente; las propaga para que el llamador decida
     *     el manejo adecuado según contexto (listado, detalle, etc.)
     *
     * @param rs ResultSet posicionado en una fila válida con estructura de SQL_BASE o consultas derivadas
     * @return objeto CasoPostventa con todos los campos mapeados desde la fila actual del ResultSet
     * @throws SQLException si falla la extracción de valores por nombres de columna no encontrados
     *                      o errores de conversión de tipos JDBC
     */
    private CasoPostventa mapearCaso(ResultSet rs) throws SQLException {

        CasoPostventa c = new CasoPostventa();

        // Mapea campos básicos desde tabla Caso_Postventa
        c.setCasoId(rs.getInt("caso_id"));
        c.setVentaId(rs.getInt("venta_id"));
        c.setTipo(rs.getString("tipo"));
        c.setCantidad(rs.getInt("cantidad"));
        c.setMotivo(rs.getString("motivo"));
        c.setFecha(rs.getDate("fecha"));
        c.setEstado(rs.getString("estado"));

        // Datos adicionales obtenidos mediante JOINs en la consulta principal
        c.setVendedorNombre(rs.getString("vendedor_nombre"));
        c.setClienteNombre(rs.getString("cliente_nombre"));
        c.setObservacion(rs.getString("observacion"));

        // Manejo de posible null en producto_id: verifica con getObject() antes de getInt()
        // para preservar semántica de "sin producto" vs "producto con ID 0"
        int productoId = 0;
        if (rs.getObject("producto_id") != null) productoId = rs.getInt("producto_id");

        c.setProductoId(productoId);
        c.setProductoNombre(rs.getString("producto_nombre"));

        return c;
    }
}