package dao;

import config.ConexionDB;
import model.Proveedor;
import model.Material;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
public class ProveedorDAO {

    // ==================== CONSULTAS ====================
    
    public List<Proveedor> listarProveedores() {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT u.*, r.cargo " +
                     "FROM Usuario u " +
                     "INNER JOIN Rol r ON u.usuario_id = r.usuario_id " +
                     "WHERE r.cargo = 'proveedor' " +
                     "ORDER BY u.fecha_registro DESC";
        
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Proveedor p = mapearProveedor(rs);
                p.setTelefonos(obtenerTelefonos(p.getUsuarioId()));
                p.setCorreos(obtenerCorreos(p.getUsuarioId()));
                p.setMateriales(obtenerMateriales(p.getUsuarioId()));
                lista.add(p);
            }
        } catch (Exception e) {
            System.err.println("Error al listar proveedores: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    public Proveedor obtenerPorId(Integer id) {
        String sql = "SELECT u.*, r.cargo " +
                     "FROM Usuario u " +
                     "INNER JOIN Rol r ON u.usuario_id = r.usuario_id " +
                     "WHERE u.usuario_id = ? AND r.cargo = 'proveedor'";
        
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Proveedor p = mapearProveedor(rs);
                    p.setTelefonos(obtenerTelefonos(p.getUsuarioId()));
                    p.setCorreos(obtenerCorreos(p.getUsuarioId()));
                    p.setMateriales(obtenerMateriales(p.getUsuarioId()));
                    return p;
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener proveedor: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

        /**
     * Búsqueda filtrada por campo específico (nombre o documento).
     */
    public List<Proveedor> buscar(String criterio, String filtro) {
        List<Proveedor> lista = new ArrayList<>();

        String campo;
        switch (filtro != null ? filtro : "nombre") {
            case "documento": campo = "u.documento"; break;
            default:          campo = "u.nombre";    break;
        }

        String sql = "SELECT u.*, r.cargo " +
                     "FROM Usuario u " +
                     "INNER JOIN Rol r ON u.usuario_id = r.usuario_id " +
                     "WHERE r.cargo = 'proveedor' " +
                     "AND " + campo + " LIKE ? " +
                     "ORDER BY u.fecha_registro DESC";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + criterio + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Proveedor p = mapearProveedor(rs);
                    p.setTelefonos(obtenerTelefonos(p.getUsuarioId()));
                    p.setCorreos(obtenerCorreos(p.getUsuarioId()));
                    p.setMateriales(obtenerMateriales(p.getUsuarioId()));
                    lista.add(p);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al buscar proveedores: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    // ==================== GUARDAR (INSERT) ====================
    
    public boolean guardar(Proveedor p, List<String> telefonos, List<String> correos, List<Integer> materialesIds) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Insertar en Usuario
            String sqlUsuario = "INSERT INTO Usuario (nombre, pass, estado, fecha_creacion, documento, fecha_registro, fecha_inicio, minimo_compra) " +
                               "VALUES (?, ?, ?, NOW(), ?, CURDATE(), ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, p.getNombre());
                stmt.setString(2, p.getPass() != null ? p.getPass() : "NO_LOGIN");
                stmt.setBoolean(3, p.isEstado());
                stmt.setString(4, p.getDocumento());
                stmt.setString(5, p.getFechaInicio());
                stmt.setDouble(6, p.getMinimoCompra() != null ? p.getMinimoCompra() : 0.0);
                stmt.executeUpdate();
                
                // Obtener ID generado
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        p.setUsuarioId(keys.getInt(1));
                    }
                }
            }

            // 2. Insertar rol proveedor
            String sqlRol = "INSERT INTO Rol (cargo, usuario_id, nombre) VALUES ('proveedor', ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlRol)) {
                stmt.setInt(1, p.getUsuarioId());
                stmt.setString(2, p.getNombre());
                stmt.executeUpdate();
            }

            // 3. Insertar teléfonos
            if (telefonos != null && !telefonos.isEmpty()) {
                String sqlTel = "INSERT INTO Telefono_Usuario (telefono, usuario_id) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlTel)) {
                    for (String tel : telefonos) {
                        if (tel != null && !tel.trim().isEmpty()) {
                            stmt.setString(1, tel.trim());
                            stmt.setInt(2, p.getUsuarioId());
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            }

            // 4. Insertar correos
            if (correos != null && !correos.isEmpty()) {
                String sqlCorreo = "INSERT INTO Correo_Usuario (email, usuario_id) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlCorreo)) {
                    for (String correo : correos) {
                        if (correo != null && !correo.trim().isEmpty()) {
                            stmt.setString(1, correo.trim().toLowerCase());
                            stmt.setInt(2, p.getUsuarioId());
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            }

            // 5. Vincular materiales (RF34)
            if (materialesIds != null && !materialesIds.isEmpty()) {
                String sqlMat = "INSERT INTO Usuario_Material (usuario_id, material_id) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlMat)) {
                    for (Integer matId : materialesIds) {
                        stmt.setInt(1, p.getUsuarioId());
                        stmt.setInt(2, matId);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            }

            conn.commit(); // Confirmar transacción
            return true;
            
        } catch (Exception e) {
            System.err.println("Error al guardar proveedor: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { 
                    conn.setAutoCommit(true); 
                    conn.close(); 
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // ==================== ACTUALIZAR (UPDATE) ====================
    
    public boolean actualizar(Proveedor p, List<String> telefonos, List<String> correos, List<Integer> materialesIds) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false);

            // 1. Actualizar Usuario (EXCLUYENDO fecha_inicio - RF08)
            String sqlUsuario = "UPDATE Usuario SET nombre=?, pass=?, estado=?, documento=?, minimo_compra=? WHERE usuario_id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlUsuario)) {
                stmt.setString(1, p.getNombre());
                stmt.setString(2, p.getPass() != null && !p.getPass().isEmpty() ? p.getPass() : obtenerPassActual(p.getUsuarioId()));
                stmt.setBoolean(3, p.isEstado());
                stmt.setString(4, p.getDocumento());
                stmt.setDouble(5, p.getMinimoCompra() != null ? p.getMinimoCompra() : 0.0);
                stmt.setInt(6, p.getUsuarioId());
                stmt.executeUpdate();
            }

            // 2. Actualizar nombre en Rol
            String sqlRol = "UPDATE Rol SET nombre=? WHERE usuario_id=? AND cargo='proveedor'";
            try (PreparedStatement stmt = conn.prepareStatement(sqlRol)) {
                stmt.setString(1, p.getNombre());
                stmt.setInt(2, p.getUsuarioId());
                stmt.executeUpdate();
            }

            // 3. Reemplazar teléfonos
            eliminarTelefonos(p.getUsuarioId(), conn);
            if (telefonos != null && !telefonos.isEmpty()) {
                String sqlTel = "INSERT INTO Telefono_Usuario (telefono, usuario_id) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlTel)) {
                    for (String tel : telefonos) {
                        if (tel != null && !tel.trim().isEmpty()) {
                            stmt.setString(1, tel.trim());
                            stmt.setInt(2, p.getUsuarioId());
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            }

            // 4. Reemplazar correos
            eliminarCorreos(p.getUsuarioId(), conn);
            if (correos != null && !correos.isEmpty()) {
                String sqlCorreo = "INSERT INTO Correo_Usuario (email, usuario_id) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlCorreo)) {
                    for (String correo : correos) {
                        if (correo != null && !correo.trim().isEmpty()) {
                            stmt.setString(1, correo.trim().toLowerCase());
                            stmt.setInt(2, p.getUsuarioId());
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            }

            // 5. Reemplazar materiales
            eliminarMateriales(p.getUsuarioId(), conn);
            if (materialesIds != null && !materialesIds.isEmpty()) {
                String sqlMat = "INSERT INTO Usuario_Material (usuario_id, material_id) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlMat)) {
                    for (Integer matId : materialesIds) {
                        stmt.setInt(1, p.getUsuarioId());
                        stmt.setInt(2, matId);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            }

            conn.commit();
            return true;
            
        } catch (Exception e) {
            System.err.println("Error al actualizar proveedor: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { 
                    conn.setAutoCommit(true); 
                    conn.close(); 
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // ==================== ESTADO Y ELIMINACIÓN ====================
    
    public boolean actualizarEstado(Integer id, Boolean estado) {
        String sql = "UPDATE Usuario SET estado=? WHERE usuario_id=?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, estado);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error al actualizar estado: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(Integer id) {
        // Soft delete: cambiar estado a inactivo
        return actualizarEstado(id, false);
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================
    
    private Proveedor mapearProveedor(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setUsuarioId(rs.getInt("usuario_id"));
        p.setNombre(rs.getString("nombre"));
        p.setPass(rs.getString("pass"));
        p.setEstado(rs.getBoolean("estado"));
        p.setDocumento(rs.getString("documento"));
        p.setFechaRegistro(rs.getString("fecha_registro"));
        p.setFechaInicio(rs.getString("fecha_inicio"));
        p.setMinimoCompra(rs.getDouble("minimo_compra"));
        return p;
    }

    private List<String> obtenerTelefonos(Integer usuarioId) {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT telefono FROM Telefono_Usuario WHERE usuario_id=?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(rs.getString("telefono"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    private List<String> obtenerCorreos(Integer usuarioId) {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT email FROM Correo_Usuario WHERE usuario_id=?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(rs.getString("email"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    private List<Material> obtenerMateriales(Integer usuarioId) {
        List<Material> lista = new ArrayList<>();
        String sql = "SELECT m.material_id, m.nombre FROM Material m " +
                     "INNER JOIN Usuario_Material um ON m.material_id = um.material_id " +
                     "WHERE um.usuario_id=?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
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

    private void eliminarTelefonos(Integer usuarioId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Telefono_Usuario WHERE usuario_id=?")) {
            stmt.setInt(1, usuarioId);
            stmt.executeUpdate();
        }
    }

    private void eliminarCorreos(Integer usuarioId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Correo_Usuario WHERE usuario_id=?")) {
            stmt.setInt(1, usuarioId);
            stmt.executeUpdate();
        }
    }

    private void eliminarMateriales(Integer usuarioId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Usuario_Material WHERE usuario_id=?")) {
            stmt.setInt(1, usuarioId);
            stmt.executeUpdate();
        }
    }

    private String obtenerPassActual(Integer usuarioId) {
        String sql = "SELECT pass FROM Usuario WHERE usuario_id=?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("pass");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "NO_LOGIN";
    }
}