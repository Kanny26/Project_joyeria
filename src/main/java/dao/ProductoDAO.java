package dao;

import config.ConexionDB;
import model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    /* ===============================
       LISTAR POR CATEGORÍA
       =============================== */
    public List<Producto> listarPorCategoria(int categoriaId) {

        List<Producto> lista = new ArrayList<>();

        String sql = """
        	    SELECT p.producto_id, p.nombre, p.descripcion, p.stock,
        	           p.precio_unitario,
        	           c.categoria_id, c.nombre AS categoria_nombre,
        	           m.material_id, m.nombre AS material_nombre
        	    FROM producto p
        	    INNER JOIN categoria c ON p.categoria_id = c.categoria_id
        	    INNER JOIN material m ON p.material_id = m.material_id
        	    WHERE p.categoria_id = ?
        	""";


        try (
            Connection con = ConexionDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, categoriaId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Producto p = mapearProducto(rs);
                lista.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    /* ===============================
       OBTENER POR ID
       =============================== */
    public Producto obtenerPorId(int id) {

        String sql = """
            SELECT p.producto_id, p.nombre, p.descripcion, p.stock,
                   p.precio_unitario,
                   c.categoria_id, c.nombre AS categoria_nombre,
                   m.material_id, m.nombre AS material_nombre
            FROM Producto p
            INNER JOIN Categoria c ON p.categoria_id = c.categoria_id
            INNER JOIN Material m ON p.material_id = m.material_id
            WHERE p.producto_id = ?
        """;

        try (
            Connection con = ConexionDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapearProducto(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /* ===============================
       GUARDAR
       =============================== */
    public void guardar(Producto p, int proveedorId) {

    	String sql = """
    		    INSERT INTO producto
    		    (nombre, descripcion, stock, precio_unitario,
    		     fecha_registro, material_id, categoria_id, usuario_proveedor_id)
    		    VALUES (?, ?, ?, ?, CURDATE(), ?, ?, ?)
    		""";


        try (
            Connection con = ConexionDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setInt(3, p.getStock());
            ps.setDouble(4, p.getPrecioUnitario());
            ps.setInt(5, p.getMaterial().getMaterialId());
            ps.setInt(6, p.getCategoria().getCategoriaId());
            ps.setInt(7, proveedorId);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ===============================
       ACTUALIZAR
       =============================== */
    public void actualizar(Producto p) {

        String sql = """
            UPDATE Producto
            SET nombre = ?, descripcion = ?, stock = ?,
                precio_unitario = ?, material_id = ?
            WHERE producto_id = ?
        """;

        try (
            Connection con = ConexionDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setInt(3, p.getStock());
            ps.setDouble(4, p.getPrecioUnitario());
            ps.setInt(5, p.getMaterial().getMaterialId());
            ps.setInt(6, p.getProductoId());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ===============================
       ELIMINAR
       =============================== */
    public void eliminar(int id) {

    	String sql = "DELETE FROM producto WHERE producto_id = ?";


        try (
            Connection con = ConexionDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ===============================
       MAPEO CENTRALIZADO
       =============================== */
    private Producto mapearProducto(ResultSet rs) throws SQLException {

        Producto p = new Producto();
        p.setProductoId(rs.getInt("producto_id"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setStock(rs.getInt("stock"));
        p.setPrecioUnitario(rs.getDouble("precio_unitario"));

        Categoria c = new Categoria();
        c.setCategoriaId(rs.getInt("categoria_id"));
        c.setNombre(rs.getString("categoria_nombre"));
        p.setCategoria(c);

        Material m = new Material();
        m.setMaterialId(rs.getInt("material_id"));
        m.setNombre(rs.getString("material_nombre"));
        p.setMaterial(m);

        return p;
    }
}
