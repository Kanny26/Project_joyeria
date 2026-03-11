package dao;

import config.ConexionDB;
import model.Material;

import java.sql.*;
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

    public Material obtenerPorId(int id) {
        Material m = null;
        String sql = "SELECT material_id, nombre FROM Material WHERE material_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m = new Material();
                    m.setMaterialId(rs.getInt("material_id"));
                    m.setNombre(rs.getString("nombre"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return m;
    }

    public boolean guardar(Material m) throws Exception {
        String sql = "INSERT INTO Material (nombre) VALUES (?)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.getNombre().trim());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Material m) throws Exception {
        String sql = "UPDATE Material SET nombre = ? WHERE material_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.getNombre().trim());
            ps.setInt(2, m.getMaterialId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws Exception {
        // Validar si hay productos usando este material (opcional, según tu lógica)
        String sql = "DELETE FROM Material WHERE material_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}