package dao;

import config.ConexionDB;
import model.Material;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaterialDAO {

    public List<Material> listarMateriales() {

        List<Material> lista = new ArrayList<>();
        String sql = "SELECT material_id, nombre FROM Material";

        try (
            Connection con = ConexionDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                Material m = new Material();
                m.setMaterialId(rs.getInt("material_id"));
                m.setNombre(rs.getString("nombre"));
                lista.add(m);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }
}
