package dao;

import config.ConexionDB;
import model.Material;
import model.Proveedor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    // ==================== CONSULTAS ====================
    public List<Proveedor> listarProveedores() {
        List<Proveedor> lista = new ArrayList<>();
        String sql = """
            SELECT proveedor_id, nombre, documento, fecha_registro, fecha_inicio, estado, minimo_compra 
            FROM Proveedor ORDER BY fecha_registro DESC
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Proveedor p = mapearProveedor(rs);
                int id = p.getProveedorId();
                try { p.setTelefonos(obtenerTelefonos(id)); } catch (Exception e) { p.setTelefonos(new ArrayList<>()); }
                try { p.setCorreos(obtenerCorreos(id)); } catch (Exception e) { p.setCorreos(new ArrayList<>()); }
                try { p.setMateriales(obtenerMateriales(id)); } catch (Exception e) { p.setMateriales(new ArrayList<>()); }
                lista.add(p);
            }
        } catch (Exception e) {
            System.err.println("■ ERROR CRÍTICO al listar proveedores: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    public Proveedor obtenerPorId(Integer id) {
        String sql = """
            SELECT p.proveedor_id, p.nombre, p.documento, p.fecha_registro, p.fecha_inicio, p.estado, p.minimo_compra 
            FROM Proveedor p WHERE p.proveedor_id = ?
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Proveedor p = mapearProveedor(rs);
                    p.setTelefonos(obtenerTelefonos(p.getProveedorId()));
                    p.setCorreos(obtenerCorreos(p.getProveedorId()));
                    p.setMateriales(obtenerMateriales(p.getProveedorId()));
                    return p;
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener proveedor: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Proveedor> buscar(String criterio, String filtro) {
        List<Proveedor> lista = new ArrayList<>();
        String campo = "documento".equals(filtro) ? "p.documento" : "p.nombre";
        String sql = "SELECT p.proveedor_id, p.nombre, p.documento, " +
                     "p.fecha_registro, p.fecha_inicio, p.estado, p.minimo_compra " +
                     "FROM Proveedor p WHERE " + campo + " LIKE ? ORDER BY p.fecha_registro DESC";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + criterio + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Proveedor p = mapearProveedor(rs);
                    p.setTelefonos(obtenerTelefonos(p.getProveedorId()));
                    p.setCorreos(obtenerCorreos(p.getProveedorId()));
                    p.setMateriales(obtenerMateriales(p.getProveedorId()));
                    lista.add(p);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al buscar proveedores: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    public boolean existeDocumento(String documento) {
        String sql = "SELECT COUNT(*) FROM Proveedor WHERE documento = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, documento);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            System.err.println("Error al verificar existencia de documento: " + e.getMessage());
        }
        return false;
    }

    public boolean existeDocumentoParaOtro(String documento, int proveedorIdActual) {
        String sql = "SELECT COUNT(*) FROM Proveedor WHERE documento = ? AND proveedor_id <> ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, documento);
            stmt.setInt(2, proveedorIdActual);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            System.err.println("Error al verificar documento para otro: " + e.getMessage());
        }
        return false;
    }

    // ==================== GUARDAR (INSERT) ====================
    public boolean guardar(Proveedor p, List<String> telefonos, List<String> correos, List<Integer> materialesIds, int usuarioId) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            if (conn == null) return false;
            conn.setAutoCommit(false);

            String sqlProveedor = """
                INSERT INTO Proveedor(nombre, documento, fecha_registro, fecha_inicio, estado, minimo_compra) 
                VALUES(?, ?, CURDATE(), ?, ?, ?)
                """;

            int idGenerado = 0;
            try (PreparedStatement stmt = conn.prepareStatement(sqlProveedor, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, p.getNombre());
                stmt.setString(2, p.getDocumento());
                stmt.setString(3, p.getFechaInicio());
                stmt.setBoolean(4, p.isEstado());
                stmt.setDouble(5, p.getMinimoCompra() != null ? p.getMinimoCompra() : 0.0);
                int filas = stmt.executeUpdate();
                if (filas == 0) throw new SQLException("No se pudo insertar el proveedor.");
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        idGenerado = keys.getInt(1);
                        p.setProveedorId(idGenerado);
                    } else {
                        throw new SQLException("No se obtuvo el ID del proveedor generado.");
                    }
                }
            }

            // Teléfonos
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
                    stmt.executeBatch();
                }
            }

            // Correos
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

            // Materiales
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

            // ■■ RF38: Registro de auditoría ■■
            registrarAuditoria(conn, usuarioId, "CREAR", "Proveedor", idGenerado, null, p.getNombre());

            conn.commit();
            System.out.println("■ Proveedor guardado con éxito. ID: " + idGenerado);
            return true;
        } catch (Exception e) {
            System.err.println("■ ERROR AL GUARDAR PROVEEDOR: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // ==================== ACTUALIZAR (UPDATE) ====================
    public boolean actualizar(Proveedor p, List<String> telefonos, List<String> correos, List<Integer> materialesIds, int usuarioId) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false);

            // ■■ Capturar estado anterior para auditoría ■■
            Proveedor anterior = obtenerPorId(p.getProveedorId());
            String nombreAnterior = anterior != null ? anterior.getNombre() : "N/A";

            // ■■ RF11: NO se actualiza fecha_inicio (inmutable) ■■
            String sqlProveedor = """
                UPDATE Proveedor 
                SET nombre = ?, documento = ?, estado = ?, minimo_compra = ? 
                WHERE proveedor_id = ?
                """;
            try (PreparedStatement stmt = conn.prepareStatement(sqlProveedor)) {
                stmt.setString(1, p.getNombre());
                stmt.setString(2, p.getDocumento());
                stmt.setBoolean(3, p.isEstado());
                stmt.setDouble(4, p.getMinimoCompra() != null ? p.getMinimoCompra() : 0.0);
                // ■■ CORREGIDO: getProveedorId() ■■
                stmt.setInt(5, p.getProveedorId());
                stmt.executeUpdate();
            }

            // Reemplazar teléfonos
            eliminarTelefonos(p.getProveedorId(), conn);
            if (telefonos != null && !telefonos.isEmpty()) {
                String sqlTel = "INSERT INTO Telefono_Proveedor(telefono, proveedor_id) VALUES(?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlTel)) {
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

            // Reemplazar correos
            eliminarCorreos(p.getProveedorId(), conn);
            if (correos != null && !correos.isEmpty()) {
                String sqlCorreo = "INSERT INTO Correo_Proveedor(email, proveedor_id) VALUES(?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlCorreo)) {
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

            // Reemplazar materiales
            eliminarMateriales(p.getProveedorId(), conn);
            if (materialesIds != null && !materialesIds.isEmpty()) {
                String sqlMat = "INSERT INTO Proveedor_Material(proveedor_id, material_id) VALUES(?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlMat)) {
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

            // ■■ RF38: Registro de auditoría ■■
            registrarAuditoria(conn, usuarioId, "EDITAR", "Proveedor", p.getProveedorId(), nombreAnterior, p.getNombre());

            conn.commit();
            System.out.println("■ Proveedor actualizado con éxito. ID: " + p.getProveedorId());
            return true;
        } catch (Exception e) {
            System.err.println("■ Error al actualizar proveedor: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // ==================== ESTADO Y ELIMINACIÓN ====================
    public boolean actualizarEstado(Integer id, Boolean estado) {
        String sql = "UPDATE Proveedor SET estado = ? WHERE proveedor_id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, estado);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error al actualizar estado: " + e.getMessage());
            return false;
        }
    }

    // ■■ RF13: Eliminación lógica (estado = false) ■■
    public boolean eliminar(Integer id, int usuarioId) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false);
            
            // Obtener nombre para auditoría
            String nombre = "ID:" + id;
            try {
                Proveedor p = obtenerPorId(id);
                if(p != null) nombre = p.getNombre();
            } catch(Exception e) {}

            String sql = "UPDATE Proveedor SET estado = 0 WHERE proveedor_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

            // ■■ RF38: Registro de auditoría ■■
            registrarAuditoria(conn, usuarioId, "ELIMINAR", "Proveedor", id, nombre, "ELIMINADO_LOGICO");

            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) {} }
            return false;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {} }
        }
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================
    private Proveedor mapearProveedor(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setProveedorId(rs.getInt("proveedor_id"));
        p.setNombre(rs.getString("nombre"));
        p.setDocumento(rs.getString("documento"));
        p.setFechaRegistro(rs.getString("fecha_registro"));
        p.setFechaInicio(rs.getString("fecha_inicio"));
        p.setEstado(rs.getBoolean("estado"));
        String minStr = rs.getString("minimo_compra");
        try { p.setMinimoCompra(minStr != null ? Double.parseDouble(minStr) : 0.0); } 
        catch (NumberFormatException e) { p.setMinimoCompra(0.0); }
        return p;
    }

    private List<String> obtenerTelefonos(Integer proveedorId) {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT telefono FROM Telefono_Proveedor WHERE proveedor_id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, proveedorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(rs.getString("telefono"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    private List<String> obtenerCorreos(Integer proveedorId) {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT email FROM Correo_Proveedor WHERE proveedor_id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, proveedorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(rs.getString("email"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    private List<Material> obtenerMateriales(Integer proveedorId) {
        List<Material> lista = new ArrayList<>();
        String sql = """
            SELECT m.material_id, m.nombre FROM Material m
            INNER JOIN Proveedor_Material pm ON m.material_id = pm.material_id
            WHERE pm.proveedor_id = ?
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, proveedorId);
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

    private void eliminarTelefonos(Integer proveedorId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Telefono_Proveedor WHERE proveedor_id = ?")) {
            stmt.setInt(1, proveedorId);
            stmt.executeUpdate();
        }
    }

    private void eliminarCorreos(Integer proveedorId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Correo_Proveedor WHERE proveedor_id = ?")) {
            stmt.setInt(1, proveedorId);
            stmt.executeUpdate();
        }
    }

    private void eliminarMateriales(Integer proveedorId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Proveedor_Material WHERE proveedor_id = ?")) {
            stmt.setInt(1, proveedorId);
            stmt.executeUpdate();
        }
    }

    // ■■ RF38: Método de auditoría ■■
    private void registrarAuditoria(Connection conn, int usuarioId, String accion, String entidad, 
                                    int entidadId, String datosAnteriores, String datosNuevos) throws SQLException {
        String sql = """
            INSERT INTO Auditoria_Log(usuario_id, accion, entidad, entidad_id, datos_anteriores, datos_nuevos, fecha_hora)
            VALUES(?, ?, ?, ?, ?, ?, NOW())
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, accion);
            stmt.setString(3, entidad);
            stmt.setInt(4, entidadId);
            stmt.setString(5, datosAnteriores);
            stmt.setString(6, datosNuevos);
            stmt.executeUpdate();
        }
    }
}