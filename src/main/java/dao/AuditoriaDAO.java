package dao;

import config.ConexionDB;
import org.json.JSONObject;

import java.sql.*;
import java.util.*;

/**
 * DAO para operaciones de lectura e inserción en la tabla Auditoria_Log de AAC27.
 * Esta clase centraliza el registro de eventos de auditoría del sistema, permitiendo
 * trazabilidad de acciones como logins, creación/edición de entidades y operaciones críticas.
 * Los métodos estáticos permiten su uso directo desde controladores y otros DAO sin
 * necesidad de instanciar la clase. Utiliza ConexionDB para gestión de conexiones y
 * JSONObject para serializar estados anteriores y nuevos de las entidades auditadas.
 */
public class AuditoriaDAO {

    /**
     * Método central que inserta un registro de auditoría en la base de datos.
     * Este método es el punto único de entrada para registrar cualquier evento
     * auditable en el sistema, garantizando consistencia en el formato de los logs.
     *
     * @param usuarioId ID del usuario que realiza la acción (0 = anónimo, ej: LOGIN_FALLIDO)
     * @param accion Tipo de acción realizada, ej: "LOGIN_EXITOSO", "PRODUCTO_CREADO"
     * @param entidad Nombre de la entidad afectada, ej: "Usuario", "Producto", "Venta"
     * @param entidadId ID del registro específico afectado (null si la acción no apunta a un registro)
     * @param datosAnteriores Estado de la entidad antes del cambio en formato JSON (null si no aplica)
     * @param datosNuevos Estado de la entidad después del cambio en formato JSON (null si no aplica)
     * @param direccionIp Dirección IP del cliente desde donde se originó la operación
     * @return true si el registro se insertó correctamente en la base de datos, false en caso contrario
     * @throws Exception si ocurre un error inesperado durante el proceso de registro
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
        // Consulta SQL: INSERT en tabla Auditoria_Log
        // Tabla destino: Auditoria_Log (almacena todos los eventos de auditoría del sistema)
        // Columnas insertadas:
        //   - usuario_id: FK hacia tabla Usuario (nullable para acciones anónimas)
        //   - accion: tipo de evento de negocio registrado
        //   - entidad: nombre lógico de la entidad afectada por la acción
        //   - entidad_id: ID del registro específico modificado (nullable)
        //   - datos_anteriores: JSON con el estado previo de la entidad (nullable)
        //   - datos_nuevos: JSON con el estado posterior de la entidad (nullable)
        //   - direccion_ip: dirección IP de origen para trazabilidad de seguridad
        // Valores parametrizados (?) para prevenir SQL Injection y permitir reutilización del plan de ejecución
        String sql = "INSERT INTO Auditoria_Log " +
                    // Lista exacta de columnas destino en la tabla de auditoría.
                    "(usuario_id, accion, entidad, entidad_id, datos_anteriores, datos_nuevos, direccion_ip) " +
                    // Valores parametrizados para evitar SQL Injection y reutilizar el plan de ejecución.
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        // try-with-resources: garantiza el cierre automático de Connection y PreparedStatement
        // incluso si ocurre una excepción, previniendo fugas de recursos de base de datos
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

    // CONSULTA PRINCIPAL 

    /**
     * Retorna todos los registros de auditoría ordenados del más reciente al más antiguo.
     * Incluye el nombre del usuario que realizó la acción (o "Anónimo" si no aplica)
     * mediante un LEFT JOIN con la tabla Usuario.
     * Solo debe llamarse desde roles: superadministrador y administrador.
     *
     * Cada mapa en la lista representa una fila del log y contiene las siguientes claves:
     *   - log_id: identificador único del evento de auditoría
     *   - usuario_nombre: nombre del usuario o "Anónimo" si no hay usuario asociado
     *   - accion: tipo de acción realizada (LOGIN_EXITOSO, PRODUCTO_CREADO, etc.)
     *   - entidad: nombre de la entidad afectada (Usuario, Producto, Venta, etc.)
     *   - entidad_id: ID del registro específico afectado (puede ser null)
     *   - datos_anteriores: JSON con el estado previo de la entidad (puede ser null)
     *   - datos_nuevos: JSON con el estado posterior de la entidad (puede ser null)
     *   - direccion_ip: dirección IP de origen de la operación
     *   - fecha_hora: timestamp exacto de cuando ocurrió el evento
     *
     * @return lista de mapas con los campos del log de auditoría, ordenados por fecha descendente
     * @throws Exception si falla la consulta (los errores SQL se registran en consola y se retorna lista vacía)
     */
    public List<Map<String, Object>> listarLogs() throws Exception {
        // Lista donde cada elemento representa una fila del log de auditoría.
        List<Map<String, Object>> lista = new ArrayList<>();

        // Consulta SQL: SELECT con JOIN para recuperar logs de auditoría con información de usuario
        // Tablas involucradas:
        //   - Auditoria_Log (alias 'al'): tabla principal con todos los eventos de auditoría
        //   - Usuario (alias 'u'): tabla de usuarios para obtener el nombre legible
        // Tipo de JOIN: LEFT JOIN para conservar registros anónimos (usuario_id NULL en Auditoria_Log)
        // Condiciones del JOIN: al.usuario_id = u.usuario_id (relación FK hacia Usuario)
        // Columnas seleccionadas:
        //   - al.log_id: PK del evento de auditoría
        //   - COALESCE(u.nombre, 'Anónimo') AS usuario_nombre: nombre del usuario o valor por defecto
        //   - al.accion: tipo de evento registrado
        //   - al.entidad: entidad afectada por la acción
        //   - al.entidad_id: ID del registro específico modificado
        //   - al.datos_anteriores: JSON con estado previo (para auditoría de cambios)
        //   - al.datos_nuevos: JSON con estado posterior (para auditoría de cambios)
        //   - al.direccion_ip: IP de origen para trazabilidad de seguridad
        //   - al.fecha_hora: timestamp del evento para ordenamiento cronológico
        // Ordenamiento: ORDER BY al.fecha_hora DESC (más recientes primero para UX en pantallas de admin)
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

        // try-with-resources: abre y cierra automáticamente Connection, PreparedStatement y ResultSet
        // Esto garantiza que los recursos de base de datos se liberen incluso si ocurre una excepción
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Recorremos cada fila del resultado para transformarla a Map<String, Object>.
            while (rs.next()) {
                // LinkedHashMap mantiene el orden de inserción, útil para preservar orden de columnas en UI
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

    // ACCIONES ESPECÍFICAS 

    /**
     * Registra un evento de login exitoso en el log de auditoría.
     * Método de conveniencia que delega en registrarAccion con parámetros preconfigurados
     * para el caso específico de autenticación correcta.
     *
     * @param usuarioId ID del usuario que inició sesión exitosamente
     * @param usuario nombre de usuario (texto libre almacenado en JSON para legibilidad del log)
     * @param ip dirección IP del cliente desde donde se realizó el login
     * @return resultado de {@link #registrarAccion(int, String, String, Integer, JSONObject, JSONObject, String)}
     *         true si se registró correctamente, false en caso contrario
     * @throws Exception si falla el registro por errores de base de datos o conexión
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
     * Registra un evento de login fallido en el log de auditoría.
     * Método de conveniencia para rastrear intentos de acceso no autorizados.
     * Se registra con usuarioId = 0 (anónimo) ya que no hay usuario autenticado.
     *
     * @param usuario nombre de usuario intentado en el login fallido
     * @param ip dirección IP del cliente desde donde se originó el intento
     * @return {@code true} si se insertó el log correctamente, false en caso contrario
     * @throws Exception si falla el insert por errores de base de datos o conexión
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
     * Registra la creación de un nuevo producto en el log de auditoría.
     * Método de conveniencia que encapsula los parámetros específicos para eventos
     * de creación de productos, facilitando la consistencia en el registro.
     *
     * @param usuarioId ID del usuario que realizó la creación del producto
     * @param productoId ID técnico del nuevo producto creado (PK en tabla Producto)
     * @param nombreProducto nombre legible del producto para facilitar identificación en logs
     * @param ip dirección IP del cliente desde donde se originó la operación
     * @return {@code true} si se registró el evento correctamente, false en caso contrario
     * @throws Exception si falla el registro por errores de base de datos o conexión
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
     * Registra la edición de un producto existente en el log de auditoría.
     * Incluye ambos estados (antes y después) para permitir auditoría completa del cambio.
     *
     * @param usuarioId ID del usuario que realizó la edición
     * @param productoId ID del producto que fue modificado
     * @param datosAnteriores JSON con el estado del producto antes de la edición
     * @param datosNuevos JSON con el estado del producto después de la edición
     * @param ip dirección IP del cliente desde donde se originó la operación
     * @return {@code true} si se registró el evento correctamente, false en caso contrario
     * @throws Exception si falla el registro por errores de base de datos o conexión
     */
    public static boolean registrarProductoEditado(int usuarioId, int productoId,
                                                    JSONObject datosAnteriores,
                                                    JSONObject datosNuevos, String ip) throws Exception {
        // Inserta evento de edición incluyendo estado antes y después para auditoría completa.
        return registrarAccion(usuarioId, "PRODUCTO_EDITADO", "Producto", productoId,
                            datosAnteriores, datosNuevos, ip);
    }

    /**
     * Registra la creación de una nueva venta en el log de auditoría.
     * Método de conveniencia para eventos de registro de ventas, incluyendo
     * datos clave para trazabilidad financiera y operativa.
     *
     * @param usuarioId ID del vendedor/usuario que registró la venta
     * @param ventaId ID técnico de la venta creada (PK en tabla Venta)
     * @param clienteId ID del cliente asociado a la venta
     * @param montoTotal monto total de la operación para auditoría financiera
     * @param ip dirección IP del cliente desde donde se originó la operación
     * @return {@code true} si se registró el evento correctamente, false en caso contrario
     * @throws Exception si falla el registro por errores de base de datos o conexión
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
     * Registra un cambio de contraseña de usuario en el log de auditoría.
     * Método de conveniencia para eventos de seguridad críticos.
     * No almacena la contraseña ni datos sensibles, solo la acción y metadatos de contexto.
     *
     * @param usuarioId ID del usuario que cambió su contraseña
     * @param ip dirección IP del cliente desde donde se originó el cambio
     * @return {@code true} si se registró el evento correctamente, false en caso contrario
     * @throws Exception si falla el registro por errores de base de datos o conexión
     */
    public static boolean registrarCambioPassword(int usuarioId, String ip) throws Exception {
        // Registra cambio de contraseña sin exponer datos sensibles.
        // Solo se guarda la acción, usuario y metadatos de contexto (IP).
        return registrarAccion(usuarioId, "PASSWORD_CAMBIADA", "Usuario", usuarioId, null, null, ip);
    }
}