package dao;

import config.ConexionDB;
import model.Material;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MaterialDAO {

    public List<Material> listarMateriales() {
        List<Material> lista = new ArrayList<>();
        String sql = "SELECT material_id, nombre FROM Material ORDER BY nombre ASC";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Material m = new Material();
                m.setMaterialId(rs.getInt("material_id"));
                m.setNombre(rs.getString("nombre"));
                lista.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    // ■■ RF15: Validar si el material tiene productos activos ■■
    public boolean tieneProductosActivos(int materialId) throws Exception {
        String sql = "SELECT COUNT(*) FROM Producto WHERE material_id = ? AND estado = 1";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, materialId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public boolean guardar(Material m) throws Exception {
        String sql = "INSERT INTO Material (nombre) VALUES (?)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.getNombre());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Material m) throws Exception {
        String sql = "UPDATE Material SET nombre = ? WHERE material_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.getNombre());
            ps.setInt(2, m.getMaterialId());
            return ps.executeUpdate() > 0;
        }
    }

    // ■■ RF15: Eliminación con validación ■■
    public boolean eliminar(int id) throws Exception {
        if (tieneProductosActivos(id)) {
            throw new Exception("No se puede eliminar: hay productos activos usando este material.");
        }
        String sql = "DELETE FROM Material WHERE material_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}