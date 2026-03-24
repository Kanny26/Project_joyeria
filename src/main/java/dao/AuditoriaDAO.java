package dao;

import config.ConexionDB;
import org.json.JSONObject;

import java.sql.*;
import java.util.*;

/**
 * Registra todas las acciones importantes del sistema en la tabla Auditoria_Log.
 * Permite rastrear quién hizo qué y cuándo, útil para revisiones y seguridad.
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

        if (usuarioId < 0 || accion == null || accion.isEmpty()) {
            System.err.println("Parámetros inválidos en auditoria");
            return false;
        }

        String sql = "INSERT INTO Auditoria_Log " +
                    "(usuario_id, accion, entidad, entidad_id, datos_anteriores, datos_nuevos, direccion_ip) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (usuarioId == 0) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, usuarioId);
            }

            ps.setString(2, accion);
            ps.setString(3, entidad);

            if (entidadId != null) {
                ps.setInt(4, entidadId);
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }

            if (datosAnteriores != null) {
                ps.setString(5, datosAnteriores.toString());
            } else {
                ps.setNull(5, java.sql.Types.NULL);
            }

            if (datosNuevos != null) {
                ps.setString(6, datosNuevos.toString());
            } else {
                ps.setNull(6, java.sql.Types.NULL);
            }

            ps.setString(7, direccionIp);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error registrando auditoría: " + e.getMessage());
            return false;
        }
    }

    // ==================== CONSULTA PRINCIPAL ====================

    public List<Map<String, Object>> listarLogs() throws Exception {
        List<Map<String, Object>> lista = new ArrayList<>();

        String sql = """
                SELECT
                    al.log_id,
                    COALESCE(u.nombre, 'Anónimo')  AS usuario_nombre,
                    al.accion,
                    al.entidad,
                    al.entidad_id,
                    al.datos_anteriores,
                    al.datos_nuevos,
                    al.direccion_ip,
                    al.fecha_hora
                FROM Auditoria_Log al
                LEFT JOIN Usuario u ON al.usuario_id = u.usuario_id
                ORDER BY al.fecha_hora DESC
                """;

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("log_id",           rs.getInt("log_id"));
                fila.put("usuario_nombre",   rs.getString("usuario_nombre"));
                fila.put("accion",           rs.getString("accion"));
                fila.put("entidad",          rs.getString("entidad"));
                fila.put("entidad_id",       rs.getObject("entidad_id"));
                fila.put("datos_anteriores", rs.getString("datos_anteriores"));
                fila.put("datos_nuevos",     rs.getString("datos_nuevos"));
                fila.put("direccion_ip",     rs.getString("direccion_ip"));
                fila.put("fecha_hora",       rs.getTimestamp("fecha_hora"));
                lista.add(fila);
            }

        } catch (SQLException e) {
            System.err.println("Error listando auditoría: " + e.getMessage());
        }

        return lista;
    }

    // ==================== ACCIONES DE AUTENTICACIÓN ====================

    public static boolean registrarLoginExitoso(int usuarioId, String usuario, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("usuario", usuario);
        datos.put("timestamp", System.currentTimeMillis());
        return registrarAccion(usuarioId, "LOGIN_EXITOSO", "Usuario", usuarioId, null, datos, ip);
    }

    public static boolean registrarLoginFallido(String usuario, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("usuario", usuario);
        datos.put("timestamp", System.currentTimeMillis());
        return registrarAccion(0, "LOGIN_FALLIDO", "Usuario", null, null, datos, ip);
    }

    public static boolean registrarCambioPassword(int usuarioId, String ip) throws Exception {
        return registrarAccion(usuarioId, "PASSWORD_CAMBIADA", "Usuario", usuarioId, null, null, ip);
    }

    // ==================== ACCIONES DE USUARIOS ====================

    public static boolean registrarUsuarioCreado(int usuarioId, String nombreUsuario, String rol, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("usuario_id", usuarioId);
        datos.put("nombre", nombreUsuario);
        datos.put("rol", rol);
        return registrarAccion(usuarioId, "USUARIO_CREADO", "Usuario", usuarioId, null, datos, ip);
    }

    public static boolean registrarUsuarioEditado(int usuarioId, int usuarioEditadoId, String nombreUsuario,
                                                   JSONObject datosAnteriores, JSONObject datosNuevos, String ip) throws Exception {
        return registrarAccion(usuarioId, "USUARIO_EDITADO", "Usuario", usuarioEditadoId,
                            datosAnteriores, datosNuevos, ip);
    }

    public static boolean registrarUsuarioEliminado(int usuarioId, int usuarioEliminadoId, String nombreUsuario, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("usuario_id", usuarioEliminadoId);
        datos.put("nombre", nombreUsuario);
        return registrarAccion(usuarioId, "USUARIO_ELIMINADO", "Usuario", usuarioEliminadoId, null, datos, ip);
    }

    // ==================== ACCIONES DE PRODUCTOS ====================

    public static boolean registrarProductoCreado(int usuarioId, int productoId,
                                                   String nombreProducto, double precio, int stock, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("producto_id", productoId);
        datos.put("nombre", nombreProducto);
        datos.put("precio", precio);
        datos.put("stock", stock);
        return registrarAccion(usuarioId, "PRODUCTO_CREADO", "Producto", productoId, null, datos, ip);
    }

    public static boolean registrarProductoEditado(int usuarioId, int productoId,
                                                    JSONObject datosAnteriores,
                                                    JSONObject datosNuevos, String ip) throws Exception {
        return registrarAccion(usuarioId, "PRODUCTO_EDITADO", "Producto", productoId,
                            datosAnteriores, datosNuevos, ip);
    }

    public static boolean registrarProductoEliminado(int usuarioId, int productoId, String nombreProducto, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("producto_id", productoId);
        datos.put("nombre", nombreProducto);
        return registrarAccion(usuarioId, "PRODUCTO_ELIMINADO", "Producto", productoId, null, datos, ip);
    }

    // ==================== ACCIONES DE CATEGORÍAS ====================

    public static boolean registrarCategoriaCreada(int usuarioId, int categoriaId, String nombreCategoria, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("categoria_id", categoriaId);
        datos.put("nombre", nombreCategoria);
        return registrarAccion(usuarioId, "CATEGORIA_CREADA", "Categoria", categoriaId, null, datos, ip);
    }

    public static boolean registrarCategoriaEditada(int usuarioId, int categoriaId,
                                                     JSONObject datosAnteriores, JSONObject datosNuevos, String ip) throws Exception {
        return registrarAccion(usuarioId, "CATEGORIA_EDITADA", "Categoria", categoriaId,
                            datosAnteriores, datosNuevos, ip);
    }

    public static boolean registrarCategoriaEliminada(int usuarioId, int categoriaId, String nombreCategoria, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("categoria_id", categoriaId);
        datos.put("nombre", nombreCategoria);
        return registrarAccion(usuarioId, "CATEGORIA_ELIMINADA", "Categoria", categoriaId, null, datos, ip);
    }

    // ==================== ACCIONES DE PROVEEDORES ====================

    public static boolean registrarProveedorCreado(int usuarioId, int proveedorId, String nombreProveedor, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("proveedor_id", proveedorId);
        datos.put("nombre", nombreProveedor);
        return registrarAccion(usuarioId, "PROVEEDOR_CREADO", "Proveedor", proveedorId, null, datos, ip);
    }

    public static boolean registrarProveedorEditado(int usuarioId, int proveedorId,
                                                     JSONObject datosAnteriores, JSONObject datosNuevos, String ip) throws Exception {
        return registrarAccion(usuarioId, "PROVEEDOR_EDITADO", "Proveedor", proveedorId,
                            datosAnteriores, datosNuevos, ip);
    }

    public static boolean registrarProveedorEliminado(int usuarioId, int proveedorId, String nombreProveedor, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("proveedor_id", proveedorId);
        datos.put("nombre", nombreProveedor);
        return registrarAccion(usuarioId, "PROVEEDOR_ELIMINADO", "Proveedor", proveedorId, null, datos, ip);
    }

    // ==================== ACCIONES DE VENTAS ====================

    public static boolean registrarVentaCreada(int usuarioId, int ventaId,
                                               int clienteId, double montoTotal, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("venta_id", ventaId);
        datos.put("cliente_id", clienteId);
        datos.put("monto_total", montoTotal);
        return registrarAccion(usuarioId, "VENTA_CREADA", "Venta", ventaId, null, datos, ip);
    }

    public static boolean registrarVentaAnulada(int usuarioId, int ventaId, String motivo, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("venta_id", ventaId);
        datos.put("motivo", motivo);
        return registrarAccion(usuarioId, "VENTA_ANULADA", "Venta", ventaId, null, datos, ip);
    }

    // ==================== ACCIONES DE CONFIGURACIÓN ====================

    public static boolean registrarConfiguracionCambiada(int usuarioId, String configKey,
                                                          String valorAnterior, String valorNuevo, String ip) throws Exception {
        JSONObject anteriores = new JSONObject();
        anteriores.put(configKey, valorAnterior);
        JSONObject nuevos = new JSONObject();
        nuevos.put(configKey, valorNuevo);
        return registrarAccion(usuarioId, "CONFIGURACION_CAMBIADA", "Configuracion", null,
                            anteriores, nuevos, ip);
    }

    public static boolean registrarBackupRealizado(int usuarioId, String nombreBackup, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("backup", nombreBackup);
        return registrarAccion(usuarioId, "BACKUP_REALIZADO", "Sistema", null, null, datos, ip);
    }
}