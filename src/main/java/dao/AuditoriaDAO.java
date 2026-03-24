package dao;

import config.ConexionDB;
import org.json.JSONObject;

import java.sql.*;
import java.util.*;

/**
 * Lectura e inserción en la tabla Auditoria_Log de AAC27.
 * Controladores y otros DAO usan los métodos estáticos; ConexionDB para conexión y JSONObject para datos anteriores/nuevos.
 */
public class AuditoriaDAO {

    /**
     * Método central que inserta un registro de auditoría.
     *
     * @param usuarioId       ID del usuario (0 = anónimo, ej: LOGIN_FALLIDO)
     * @param accion          Tipo de acción, ej: "LOGIN_EXITOSO", "PRODUCTO_CREADO"
     * @param entidad         Entidad afectada, ej: "Usuario", "Producto"
     * @param entidadId       ID del registro afectado (null si no aplica)
     * @param datosAnteriores Estado antes del cambio (null si no aplica)
     * @param datosNuevos     Estado después del cambio (null si no aplica)
     * @param direccionIp     IP del cliente
     * @return true si se guardó correctamente
     */
    public static boolean registrarAccion(
            int usuarioId,
            String accion,
            String entidad,
            Integer entidadId,
            JSONObject datosAnteriores,
            JSONObject datosNuevos,
            String direccionIp) throws Exception {

        // usuarioId == 0 es válido para acciones anónimas (ej: LOGIN_FALLIDO)
        if (usuarioId < 0 || accion == null || accion.isEmpty()) {
            System.err.println("Parámetros inválidos en auditoria");
            // Si faltan datos mínimos para registrar la acción, devolvemos false de inmediato.
            // Esto evita ejecutar una inserción inválida en la base de datos.
            return false;
        }

        // Esta sentencia INSERT resuelve una necesidad de negocio clave: dejar evidencia verificable
        // de operaciones (login, creación, edición, ventas) para reconstruir el historial del sistema.
        // La lista de columnas define exactamente qué se audita.
        String sql = "INSERT INTO Auditoria_Log " +
                    // Lista exacta de columnas destino en la tabla de auditoría.
                    "(usuario_id, accion, entidad, entidad_id, datos_anteriores, datos_nuevos, direccion_ip) " +
                    // Valores parametrizados para evitar SQL Injection y reutilizar el plan de ejecución.
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        // try-with-resources abre y cierra automáticamente conexión y statement,
        // evitando fugas de recursos aunque ocurra un error SQL.
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // usuarioId == 0 → NULL en la FK (usuario anónimo)
            if (usuarioId == 0) {
                // Parámetro 1 (usuario_id): se envía NULL para acciones sin usuario autenticado.
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                // Parámetro 1 (usuario_id): id real del usuario autenticado.
                ps.setInt(1, usuarioId);
            }

            // Parámetro 2 (accion): tipo de evento del negocio (ej. LOGIN_EXITOSO, PRODUCTO_EDITADO).
            // PreparedStatement separa datos y SQL, mitigando inyección SQL al no concatenar texto externo.
            ps.setString(2, accion);
            // Parámetro 3 (entidad): nombre lógico de la entidad afectada (Usuario, Producto, Venta, etc.).
            ps.setString(3, entidad);

            if (entidadId != null) {
                // Parámetro 4 (entidad_id): id del registro afectado cuando aplica.
                ps.setInt(4, entidadId);
            } else {
                // Parámetro 4 (entidad_id): NULL cuando la acción no apunta a un registro específico.
                ps.setNull(4, java.sql.Types.INTEGER);
            }

            if (datosAnteriores != null) {
                // Parámetro 5 (datos_anteriores): JSON serializado con el estado previo.
                ps.setString(5, datosAnteriores.toString());
            } else {
                // Parámetro 5 (datos_anteriores): NULL si no existe estado previo para esta acción.
                ps.setNull(5, java.sql.Types.NULL);
            }

            if (datosNuevos != null) {
                // Parámetro 6 (datos_nuevos): JSON serializado con el estado posterior.
                ps.setString(6, datosNuevos.toString());
            } else {
                // Parámetro 6 (datos_nuevos): NULL si no hay datos nuevos asociados.
                ps.setNull(6, java.sql.Types.NULL);
            }

            // Parámetro 7 (direccion_ip): IP del cliente desde donde se originó la operación.
            ps.setString(7, direccionIp);

            // Retornamos true solo si se insertó al menos una fila; así garantizamos al flujo de negocio
            // que la acción sí quedó trazada antes de continuar.
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // Captura errores SQL (conexión, constraints, tipos, etc.) para no romper el flujo llamador.
            System.err.println("Error registrando auditoría: " + e.getMessage());
            // Si hubo error de base de datos, se informa con false.
            return false;
        }
    }

    // ==================== CONSULTA PRINCIPAL ====================

    /**
     * Retorna todos los registros de auditoría ordenados del más reciente al más antiguo.
     * Incluye el nombre del usuario que realizó la acción (o "Anónimo" si no aplica).
     * Solo debe llamarse desde roles: superadministrador y administrador.
     *
     * Cada mapa contiene:
     *   log_id, usuario_nombre, accion, entidad, entidad_id,
     *   datos_anteriores, datos_nuevos, direccion_ip, fecha_hora
     *
     * @return lista de mapas con los campos del log
     * @throws Exception si falla la consulta (errores SQL pueden quedar solo en consola)
     */
    public List<Map<String, Object>> listarLogs() throws Exception {
        // Lista donde cada elemento representa una fila del log de auditoría.
        List<Map<String, Object>> lista = new ArrayList<>();

        // Esta consulta recupera todos los logs de auditoría con el nombre del usuario.
        // Incluye LEFT JOIN para conservar también registros anónimos (sin usuario_id).
        String sql = """
                -- SELECT: columnas que se mostrarán en la vista de auditoría
                SELECT
                    -- Identificador único del evento de auditoría
                    al.log_id,
                    -- Si el usuario no existe o es NULL, mostramos "Anónimo" para mantener consistencia visual
                    COALESCE(u.nombre, 'Anónimo')  AS usuario_nombre,
                    -- Acción realizada (crear, editar, login, etc.)
                    al.accion,
                    -- Entidad sobre la cual se ejecutó la acción
                    al.entidad,
                    -- ID de la entidad afectada (puede ser NULL)
                    al.entidad_id,
                    -- JSON previo al cambio (si aplica)
                    al.datos_anteriores,
                    -- JSON posterior al cambio (si aplica)
                    al.datos_nuevos,
                    -- IP desde la cual se realizó la operación
                    al.direccion_ip,
                    -- Fecha y hora exacta del evento
                    al.fecha_hora
                -- FROM: tabla principal de auditoría
                FROM Auditoria_Log al
                -- LEFT JOIN con Usuario para traer el nombre sin perder logs anónimos
                LEFT JOIN Usuario u ON al.usuario_id = u.usuario_id
                -- ORDER BY descendente para listar primero los eventos más recientes
                ORDER BY al.fecha_hora DESC
                """;

        // Se abre conexión y se ejecuta la consulta.
        // ResultSet se cierra automáticamente al salir del bloque.
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Recorremos cada fila del resultado para transformarla a Map<String, Object>.
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                // Recupera log_id para identificar cada evento en pantalla.
                fila.put("log_id",           rs.getInt("log_id"));
                // Recupera nombre del usuario (ya resuelto con COALESCE en la consulta).
                fila.put("usuario_nombre",   rs.getString("usuario_nombre"));
                // Recupera tipo de acción registrada.
                fila.put("accion",           rs.getString("accion"));
                // Recupera entidad afectada.
                fila.put("entidad",          rs.getString("entidad"));
                // Recupera id de entidad; getObject permite preservar NULL sin convertir a 0.
                fila.put("entidad_id",       rs.getObject("entidad_id"));       // puede ser null
                // Recupera JSON anterior como texto para mostrarlo o parsearlo después.
                fila.put("datos_anteriores", rs.getString("datos_anteriores")); // JSON string o null
                // Recupera JSON nuevo como texto para mostrarlo o parsearlo después.
                fila.put("datos_nuevos",     rs.getString("datos_nuevos"));     // JSON string o null
                // Recupera dirección IP de origen.
                fila.put("direccion_ip",     rs.getString("direccion_ip"));
                // Recupera marca de tiempo completa del evento.
                fila.put("fecha_hora",       rs.getTimestamp("fecha_hora"));
                // Agrega la fila transformada a la lista final.
                lista.add(fila);
            }

        } catch (SQLException e) {
            // Captura errores de consulta para evitar que la vista falle por excepción SQL.
            System.err.println("Error listando auditoría: " + e.getMessage());
        }

        // Devuelve lista completa si hubo éxito; si falló la consulta, devuelve lista vacía.
        return lista;
    }

    // ==================== ACCIONES ESPECÍFICAS ====================

    /**
     * @param usuarioId usuario autenticado
     * @param usuario nombre de usuario (texto libre en JSON)
     * @param ip dirección IP del cliente
     * @return resultado de {@link #registrarAccion(int, String, String, Integer, JSONObject, JSONObject, String)}
     * @throws Exception si falla el registro
     */
    public static boolean registrarLoginExitoso(int usuarioId, String usuario, String ip) throws Exception {
        // Armamos JSON de datos_nuevos con información mínima útil para trazabilidad.
        JSONObject datos = new JSONObject();
        // Usuario autenticado en texto para facilitar lectura del log.
        datos.put("usuario", usuario);
        // Timestamp de aplicación para soporte y correlación de eventos.
        datos.put("timestamp", System.currentTimeMillis());
        // Delega en el método central para insertar el log de login exitoso.
        return registrarAccion(usuarioId, "LOGIN_EXITOSO", "Usuario", usuarioId, null, datos, ip);
    }

    /**
     * @param usuario nombre intentado
     * @param ip IP del cliente
     * @return {@code true} si se insertó el log
     * @throws Exception si falla el insert
     */
    public static boolean registrarLoginFallido(String usuario, String ip) throws Exception {
        // Se registra intento fallido aunque no exista usuario autenticado.
        JSONObject datos = new JSONObject();
        // Guarda el nombre intentado para investigación de accesos inválidos.
        datos.put("usuario", usuario);
        // Marca temporal del intento de autenticación.
        datos.put("timestamp", System.currentTimeMillis());
        // usuarioId = 0 indica acción anónima; entidadId no aplica en este caso.
        return registrarAccion(0, "LOGIN_FALLIDO", "Usuario", null, null, datos, ip);
    }

    /**
     * @param usuarioId usuario que crea el producto
     * @param productoId ID del nuevo producto
     * @param nombreProducto nombre legible
     * @param ip IP del cliente
     * @return {@code true} si se registró
     * @throws Exception si falla
     */
    public static boolean registrarProductoCreado(int usuarioId, int productoId,
                                                   String nombreProducto, String ip) throws Exception {
        // datos_nuevos describe el producto recién creado.
        JSONObject datos = new JSONObject();
        // ID técnico del producto para trazabilidad exacta.
        datos.put("producto_id", productoId);
        // Nombre legible para facilitar auditoría en interfaz.
        datos.put("nombre", nombreProducto);
        // Inserta evento de creación de producto.
        return registrarAccion(usuarioId, "PRODUCTO_CREADO", "Producto", productoId, null, datos, ip);
    }

    /**
     * @param usuarioId usuario que edita
     * @param productoId ID del producto
     * @param datosAnteriores JSON antes del cambio
     * @param datosNuevos JSON después del cambio
     * @param ip IP del cliente
     * @return {@code true} si se registró
     * @throws Exception si falla
     */
    public static boolean registrarProductoEditado(int usuarioId, int productoId,
                                                    JSONObject datosAnteriores,
                                                    JSONObject datosNuevos, String ip) throws Exception {
        // Inserta evento de edición incluyendo estado antes y después para auditoría completa.
        return registrarAccion(usuarioId, "PRODUCTO_EDITADO", "Producto", productoId,
                            datosAnteriores, datosNuevos, ip);
    }

    /**
     * @param usuarioId vendedor que registra la venta
     * @param ventaId ID de la venta
     * @param clienteId cliente asociado
     * @param montoTotal total reportado
     * @param ip IP del cliente
     * @return {@code true} si se registró
     * @throws Exception si falla
     */
    public static boolean registrarVentaCreada(int usuarioId, int ventaId,
                                               int clienteId, double montoTotal, String ip) throws Exception {
        // datos_nuevos resume la operación de venta creada.
        JSONObject datos = new JSONObject();
        // ID de la venta para enlazar rápidamente con el comprobante o detalle.
        datos.put("venta_id", ventaId);
        // ID del cliente asociado a la venta.
        datos.put("cliente_id", clienteId);
        // Monto total registrado en la operación.
        datos.put("monto_total", montoTotal);
        // Inserta evento de creación de venta.
        return registrarAccion(usuarioId, "VENTA_CREADA", "Venta", ventaId, null, datos, ip);
    }

    /**
     * @param usuarioId usuario que cambió la contraseña
     * @param ip IP del cliente
     * @return {@code true} si se registró
     * @throws Exception si falla
     */
    public static boolean registrarCambioPassword(int usuarioId, String ip) throws Exception {
        // Registra cambio de contraseña sin exponer datos sensibles.
        // Solo se guarda la acción, usuario y metadatos de contexto (IP).
        return registrarAccion(usuarioId, "PASSWORD_CAMBIADA", "Usuario", usuarioId, null, null, ip);
    }
}