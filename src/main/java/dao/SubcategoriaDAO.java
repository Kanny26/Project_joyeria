package dao;

import config.ConexionDB;
import model.Subcategoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubcategoriaDAO {

    public List<Subcategoria> listarTodas() {
        List<Subcategoria> lista = new ArrayList<>();
        String sql = "SELECT subcategoria_id, nombre FROM Subcategoria ORDER BY nombre ASC";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Subcategoria s = new Subcategoria();
                s.setSubcategoriaId(rs.getInt("subcategoria_id"));
                s.setNombre(rs.getString("nombre"));
                lista.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Subcategoria obtenerPorId(int id) {
        Subcategoria s = null;
        String sql = "SELECT subcategoria_id, nombre FROM Subcategoria WHERE subcategoria_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    s = new Subcategoria();
                    s.setSubcategoriaId(rs.getInt("subcategoria_id"));
                    s.setNombre(rs.getString("nombre"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    public boolean guardar(Subcategoria s) throws Exception {
        String sql = "INSERT INTO Subcategoria (nombre) VALUES (?)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getNombre().trim());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Subcategoria s) throws Exception {
        String sql = "UPDATE Subcategoria SET nombre = ? WHERE subcategoria_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getNombre().trim());
            ps.setInt(2, s.getSubcategoriaId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws Exception {
        // Validar si hay categorías usando esta subcategoría (opcional)
        String sql = "DELETE FROM Subcategoria WHERE subcategoria_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}