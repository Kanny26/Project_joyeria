package dao;

import config.ConexionDB;
import model.Proveedor;
import model.Producto;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProveedorDAO {

    public boolean guardar(Proveedor p) {
        if (p.getNombre() == null || p.getNombre().trim().isEmpty()) return false;
        if (p.getTelefonos() == null || p.getTelefonos().isEmpty()) return false;
        if (p.getCorreos() == null || p.getCorreos().isEmpty()) return false;

        Connection con = null;
        try {
            con = ConexionDB.getConnection();
            con.setAutoCommit(false);

            // 1. Insertar en Usuario
            String sqlUsuario = "INSERT INTO Usuario (nombre, pass, estado, fecha_creacion, fecha_inicio) VALUES (?, ?, ?, NOW(), ?)";
            PreparedStatement psUsuario = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS);
            psUsuario.setString(1, p.getNombre());
            psUsuario.setString(2, ""); // Sin contraseña
            psUsuario.setBoolean(3, p.isEstado());
            psUsuario.setDate(4, Date.valueOf(p.getFechaInicio()));
            psUsuario.executeUpdate();

            ResultSet rs = psUsuario.getGeneratedKeys();
            if (!rs.next()) {
                con.rollback();
                return false;
            }
            int userId = rs.getInt(1);

            // 2. Insertar rol
            insertarRol(con, userId, "proveedor");

            // 3. Insertar teléfonos
            for (String tel : p.getTelefonos()) {
                if (tel != null && !tel.trim().isEmpty()) {
                    insertarTelefono(con, userId, tel.trim());
                }
            }

            // 4. Insertar correos
            for (String email : p.getCorreos()) {
                if (email != null && !email.trim().isEmpty()) {
                    insertarCorreo(con, userId, email.trim());
                }
            }

            // 5. Insertar materiales
            if (p.getMateriales() != null) {
                for (String matNombre : p.getMateriales()) {
                    if (matNombre != null && !matNombre.trim().isEmpty()) {
                        Integer matId = obtenerMaterialId(con, matNombre.trim());
                        if (matId != null) {
                            vincularMaterial(con, userId, matId);
                        }
                    }
                }
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void insertarRol(Connection con, int userId, String cargo) throws SQLException {
        String sql = "INSERT INTO Rol (cargo, usuario_id, nombre) VALUES (?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, cargo);
        ps.setInt(2, userId);
        ps.setString(3, cargo + "_" + userId);
        ps.executeUpdate();
    }

    private void insertarTelefono(Connection con, int userId, String telefono) throws SQLException {
        String sql = "INSERT INTO Telefono_Usuario (telefono, usuario_id) VALUES (?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, telefono);
        ps.setInt(2, userId);
        ps.executeUpdate();
    }

    private void insertarCorreo(Connection con, int userId, String email) throws SQLException {
        String sql = "INSERT INTO Correo_Usuario (email, usuario_id) VALUES (?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, email);
        ps.setInt(2, userId);
        ps.executeUpdate();
    }

    private Integer obtenerMaterialId(Connection con, String nombre) throws SQLException {
        String sql = "SELECT material_id FROM Material WHERE nombre = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, nombre);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("material_id");
        }
        return null;
    }

    private void vincularMaterial(Connection con, int userId, int matId) throws SQLException {
        String sql = "INSERT INTO Usuario_Material (usuario_id, material_id) VALUES (?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, matId);
        ps.executeUpdate();
    }

    public List<Proveedor> listar() {
        List<Proveedor> lista = new ArrayList<>();
        String sql = """
            SELECT u.usuario_id, u.nombre, u.estado, u.fecha_inicio
            FROM Usuario u
            JOIN Rol r ON u.usuario_id = r.usuario_id
            WHERE r.cargo = 'proveedor'
            ORDER BY u.nombre
            """;

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Proveedor p = new Proveedor();
                p.setUsuarioId(rs.getInt("usuario_id"));
                p.setNombre(rs.getString("nombre"));
                p.setEstado(rs.getBoolean("estado"));
                
                // ✅ Manejo seguro de fecha_inicio nula
                Date fechaInicioSql = rs.getDate("fecha_inicio");
                p.setFechaInicio(fechaInicioSql != null ? fechaInicioSql.toLocalDate() : null);

                p.setTelefonos(obtenerTelefonos(con, p.getUsuarioId()));
                p.setCorreos(obtenerCorreos(con, p.getUsuarioId()));
                p.setMateriales(obtenerMateriales(con, p.getUsuarioId()));
                p.setProductos(obtenerProductos(con, p.getUsuarioId()));

                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private List<String> obtenerTelefonos(Connection con, int userId) throws SQLException {
        List<String> tels = new ArrayList<>();
        String sql = "SELECT telefono FROM Telefono_Usuario WHERE usuario_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) tels.add(rs.getString("telefono"));
        }
        return tels;
    }

    private List<String> obtenerCorreos(Connection con, int userId) throws SQLException {
        List<String> emails = new ArrayList<>();
        String sql = "SELECT email FROM Correo_Usuario WHERE usuario_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) emails.add(rs.getString("email"));
        }
        return emails;
    }

    private List<String> obtenerMateriales(Connection con, int userId) throws SQLException {
        List<String> mats = new ArrayList<>();
        String sql = """
            SELECT m.nombre
            FROM Usuario_Material um
            JOIN Material m ON um.material_id = m.material_id
            WHERE um.usuario_id = ?
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) mats.add(rs.getString("nombre"));
        }
        return mats;
    }

    private List<Producto> obtenerProductos(Connection con, int proveedorId) throws SQLException {
        List<Producto> prods = new ArrayList<>();
        String sql = """
            SELECT p.producto_id, p.nombre, p.descripcion, p.stock, p.precio_unitario,
                   p.fecha_registro, m.nombre AS material
            FROM Producto p
            JOIN Material m ON p.material_id = m.material_id
            WHERE p.usuario_proveedor_id = ?
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, proveedorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Producto pr = new Producto();
                pr.setProductoId(rs.getInt("producto_id"));
                pr.setNombre(rs.getString("nombre"));
                pr.setDescripcion(rs.getString("descripcion"));
                pr.setStock(rs.getInt("stock"));
                pr.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                
                // ✅ Manejo seguro de fecha_registro nula
                Date fechaRegistroSql = rs.getDate("fecha_registro");
                pr.setFechaRegistro(fechaRegistroSql != null ? fechaRegistroSql.toLocalDate() : null);
                
                pr.setMaterialNombre(rs.getString("material"));
                pr.setProveedorId(proveedorId);
                prods.add(pr);
            }
        }
        return prods;
    }

    public boolean actualizarEstado(int usuarioId, boolean estado) {
        String sql = "UPDATE Usuario SET estado = ? WHERE usuario_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, estado);
            ps.setInt(2, usuarioId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}