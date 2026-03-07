package dao;

import config.ConexionDB;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO para registrar todas las acciones en tabla de auditoría
 * Tabla: Auditoria_Log
 */
public class AuditoriaDAO {
    
    /**
     * Registrar una acción en auditoría
     * @param usuarioId ID del usuario que realiza la acción
     * @param accion Tipo de acción (LOGIN, VENTA_CREADA, PRODUCTO_EDITADO, etc)
     * @param entidad Tipo de entidad afectada (Usuario, Producto, Venta, etc)
     * @param entidadId ID de la entidad (opcional)
     * @param datosAnteriores Datos antes del cambio (optional)
     * @param datosNuevos Datos después del cambio (optional)
     * @param direccionIp Dirección IP del cliente
     * @return true si se registró exitosamente
     * @throws Exception 
     */
    public static boolean registrarAccion(
            int usuarioId,
            String accion,
            String entidad,
            Integer entidadId,
            JSONObject datosAnteriores,
            JSONObject datosNuevos,
            String direccionIp) throws Exception {
        
        // Validaciones básicas
        if (usuarioId < 0 || accion == null || accion.isEmpty()) {
            System.err.println("Parámetros inválidos en auditoria");
            return false;
        }
        
        String sql = "INSERT INTO Auditoria_Log " +
                    "(usuario_id, accion, entidad, entidad_id, datos_anteriores, datos_nuevos, direccion_ip) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection con = ConexionDB.getConnection();
        	PreparedStatement ps = con.prepareStatement(sql))  {
            ps.setInt(1, usuarioId);
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
                ps.setNull(5, java.sql.Types.VARCHAR);
            }
            
            if (datosNuevos != null) {
                ps.setString(6, datosNuevos.toString());
            } else {
                ps.setNull(6, java.sql.Types.VARCHAR);
            }
            
            ps.setString(7, direccionIp);
            
            int filasInsertadas = ps.executeUpdate();
            return filasInsertadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Error registrando auditoría: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Registrar login exitoso
     * @throws Exception 
     */
    public static boolean registrarLoginExitoso(int usuarioId, String usuario, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("usuario", usuario);
        datos.put("timestamp", System.currentTimeMillis());
        
        return registrarAccion(usuarioId, "LOGIN_EXITOSO", "Usuario", usuarioId, null, datos, ip);
    }
    
    /**
     * Registrar intento de login fallido
     * @throws Exception 
     */
    public static boolean registrarLoginFallido(String usuario, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("usuario", usuario);
        datos.put("timestamp", System.currentTimeMillis());
        
        return registrarAccion(0, "LOGIN_FALLIDO", "Usuario", null, null, datos, ip);
    }
    
    /**
     * Registrar creación de producto
     * @throws Exception 
     */
    public static boolean registrarProductoCreado(int usuarioId, int productoId, 
                                                   String nombreProducto, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("producto_id", productoId);
        datos.put("nombre", nombreProducto);
        
        return registrarAccion(usuarioId, "PRODUCTO_CREADO", "Producto", productoId, null, datos, ip);
    }
    
    /**
     * Registrar edición de producto
     * @throws Exception 
     */
    public static boolean registrarProductoEditado(int usuarioId, int productoId,
                                                    JSONObject datosAnteriores,
                                                    JSONObject datosNuevos, String ip) throws Exception {
        return registrarAccion(usuarioId, "PRODUCTO_EDITADO", "Producto", productoId, 
                            datosAnteriores, datosNuevos, ip);
    }
    
    /**
     * Registrar venta creada
     * @throws Exception 
     */
    public static boolean registrarVentaCreada(int usuarioId, int ventaId, 
                                               int clienteId, double montoTotal, String ip) throws Exception {
        JSONObject datos = new JSONObject();
        datos.put("venta_id", ventaId);
        datos.put("cliente_id", clienteId);
        datos.put("monto_total", montoTotal);
        
        return registrarAccion(usuarioId, "VENTA_CREADA", "Venta", ventaId, null, datos, ip);
    }
    
    /**
     * Registrar cambio de contraseña
     * @throws Exception 
     */
    public static boolean registrarCambioPassword(int usuarioId, String ip) throws Exception {
        return registrarAccion(usuarioId, "PASSWORD_CAMBIADA", "Usuario", usuarioId, null, null, ip);
    }
}