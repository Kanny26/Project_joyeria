package dao;

import config.ConexionDB;
import model.Material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO encargado de la gestión de materiales.
 *
 * Funcionalidad principal:
 * - Listar materiales disponibles en el sistema
 */
public class MaterialDAO {

    /**
     * Obtiene la lista de todos los materiales registrados.
     *
     * @return lista de materiales
     */
    public List<Material> listarMateriales() {

        List<Material> lista = new ArrayList<>();
        String sql = "SELECT material_id, nombre FROM Material";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
