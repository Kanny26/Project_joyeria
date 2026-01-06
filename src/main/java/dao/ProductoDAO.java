package dao;

import config.ConexionDB;
import model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> listarPorCategoria(int categoriaId) {

        List<Producto> lista = new ArrayList<>();

        String sql = """
            SELECT p.producto_id, p.nombre, p.descripcion, p.stock,
                   p.precio_unitario, p.imagen,
                   c.categoria_id, c.nombre AS categoria_nombre,
                   m.material_id, m.nombre AS material_nombre
            FROM Producto p
            JOIN Categoria c ON p.categoria_id = c.categoria_id
            JOIN Material m ON p.material_id = m.material_id
            WHERE p.categoria_id = ?
        """;

        try (
            Connection con = ConexionDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, categoriaId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Producto p = new Producto();

                p.setProductoId(rs.getInt("producto_id"));
                p.setNombre(rs.getString("nombre"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setStock(rs.getInt("stock"));
                p.setPrecioUnitario(rs.getDouble("precio_unitario"));
                p.setImagen(rs.getString("imagen"));

                Categoria c = new Categoria();
                c.setCategoriaId(rs.getInt("categoria_id"));
                c.setNombre(rs.getString("categoria_nombre"));
                p.setCategoria(c);

                Material m = new Material();
                m.setMaterialId(rs.getInt("material_id"));
                m.setNombre(rs.getString("material_nombre"));
                p.setMaterial(m);

                lista.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }
}

