package dao;

import config.ConexionDB;
import model.Subcategoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubcategoriaDAO {

    /** Retorna todas las subcategorías ordenadas por nombre */
    public List<Subcategoria> listarTodas() {
        List<Subcategoria> lista = new ArrayList<>();
        String sql = "SELECT subcategoria_id, nombre FROM Subcategoria ORDER BY nombre";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Subcategoria(
                    rs.getInt("subcategoria_id"),
                    rs.getString("nombre")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    /** Obtiene una subcategoría por su ID */
    public Subcategoria obtenerPorId(int id) {
        String sql = "SELECT subcategoria_id, nombre FROM Subcategoria WHERE subcategoria_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Subcategoria(
                    rs.getInt("subcategoria_id"),
                    rs.getString("nombre")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}