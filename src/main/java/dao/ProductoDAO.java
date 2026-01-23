package dao;

import config.ConexionDB;
import model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {
	
	// En ProductoDAO.java
	public List<Producto> listarPorCategoria(int categoriaId) {
	    List<Producto> lista = new ArrayList<>();
	    String sql = """
	        SELECT p.producto_id, p.codigo, p.nombre, p.descripcion, p.stock,
	               p.precio_unitario, p.precio_venta, p.imagen, p.fecha_registro,
	               c.categoria_id, c.nombre AS categoria_nombre,
	               m.material_id, m.nombre AS material_nombre
	        FROM producto p
	        INNER JOIN categoria c ON p.categoria_id = c.categoria_id
	        INNER JOIN material m ON p.material_id = m.material_id
	        WHERE p.categoria_id = ?
	        ORDER BY p.nombre
	    """;

	    try (Connection con = ConexionDB.getConnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {
	        ps.setInt(1, categoriaId);
	        ResultSet rs = ps.executeQuery();
	        while (rs.next()) {
	            lista.add(mapearProducto(rs));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return lista;
	}
	
	public List<Producto> buscarGlobal(String termino) {
	    List<Producto> lista = new ArrayList<>();
	    String sql = """
	        SELECT p.producto_id, p.codigo, p.nombre, p.descripcion, p.stock,
	               p.precio_unitario, p.precio_venta, p.imagen, p.fecha_registro,
	               c.categoria_id, c.nombre AS categoria_nombre,
	               m.material_id, m.nombre AS material_nombre
	        FROM producto p
	        INNER JOIN categoria c ON p.categoria_id = c.categoria_id
	        INNER JOIN material m ON p.material_id = m.material_id
	        WHERE p.nombre LIKE ?
	           OR p.codigo LIKE ?
	           OR p.descripcion LIKE ?
	           OR c.nombre LIKE ?
	           OR m.nombre LIKE ?
	        ORDER BY p.nombre
	    """;

	    try (Connection con = ConexionDB.getConnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {
	        String likeTerm = "%" + termino + "%";
	        for (int i = 1; i <= 5; i++) ps.setString(i, likeTerm);
	        ResultSet rs = ps.executeQuery();
	        while (rs.next()) lista.add(mapearProducto(rs));
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return lista;
	}

	// Buscar solo en una categoría
	public List<Producto> buscarEnCategoria(int categoriaId, String termino) {
	    List<Producto> lista = new ArrayList<>();
	    String sql = """
	        SELECT p.producto_id, p.codigo, p.nombre, p.descripcion, p.stock,
	               p.precio_unitario, p.precio_venta, p.imagen, p.fecha_registro,
	               c.categoria_id, c.nombre AS categoria_nombre,
	               m.material_id, m.nombre AS material_nombre
	        FROM producto p
	        INNER JOIN categoria c ON p.categoria_id = c.categoria_id
	        INNER JOIN material m ON p.material_id = m.material_id
	        WHERE p.categoria_id = ?
	          AND (p.nombre LIKE ? OR p.codigo LIKE ? OR p.descripcion LIKE ?)
	        ORDER BY p.nombre
	    """;

	    try (Connection con = ConexionDB.getConnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {
	        ps.setInt(1, categoriaId);
	        String likeTerm = "%" + termino + "%";
	        ps.setString(2, likeTerm);
	        ps.setString(3, likeTerm);
	        ps.setString(4, likeTerm);
	        ResultSet rs = ps.executeQuery();
	        while (rs.next()) lista.add(mapearProducto(rs));
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
            SELECT p.producto_id, p.codigo, p.nombre, p.descripcion, p.stock,
                   p.precio_unitario, p.precio_venta, p.imagen, p.fecha_registro,
                   c.categoria_id, c.nombre AS categoria_nombre,
                   m.material_id, m.nombre AS material_nombre
            FROM producto p
            INNER JOIN categoria c ON p.categoria_id = c.categoria_id
            INNER JOIN material m ON p.material_id = m.material_id
            WHERE p.producto_id = ?
        """;

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

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
       GUARDAR PRODUCTO NUEVO
       =============================== */
    public void guardar(Producto p, int proveedorId) {

        String sql = """
            INSERT INTO producto
            (codigo, nombre, descripcion, stock, precio_unitario, precio_venta,
             fecha_registro, material_id, categoria_id, usuario_proveedor_id, imagen)
            VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?)
        """;

        try (Connection con = ConexionDB.getConnection()) {

            String codigo = generarCodigoProducto(con, p.getCategoria().getCategoriaId());

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, codigo);
                ps.setString(2, p.getNombre());
                ps.setString(3, p.getDescripcion());
                ps.setInt(4, p.getStock());
                ps.setBigDecimal(5, p.getPrecioUnitario());
                ps.setBigDecimal(6, p.getPrecioVenta());
                ps.setInt(7, p.getMaterial().getMaterialId());
                ps.setInt(8, p.getCategoria().getCategoriaId());
                ps.setInt(9, proveedorId);
                ps.setString(10, p.getImagen());

                ps.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ===============================
       ACTUALIZAR PRODUCTO
       =============================== */
    public void actualizar(Producto p) {
        String sql = """
            UPDATE producto
            SET nombre = ?, descripcion = ?, stock = ?, precio_unitario = ?, precio_venta = ?,
                material_id = ?, imagen = ?
            WHERE producto_id = ?
        """;

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setInt(3, p.getStock());
            ps.setBigDecimal(4, p.getPrecioUnitario());
            ps.setBigDecimal(5, p.getPrecioVenta());
            ps.setInt(6, p.getMaterial().getMaterialId());
            ps.setString(7, p.getImagen());
            ps.setInt(8, p.getProductoId());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ===============================
       ELIMINAR PRODUCTO
       =============================== */
    public void eliminar(int id) {

        String sql = "DELETE FROM producto WHERE producto_id = ?";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ===============================
       GENERAR CÓDIGO AUTOMÁTICO
       =============================== */
    private String generarCodigoProducto(Connection con, int categoriaId) throws SQLException {

        String prefijoSql = "SELECT UPPER(LEFT(nombre, 3)) FROM categoria WHERE categoria_id = ?";
        String prefijo = "";

        try (PreparedStatement ps = con.prepareStatement(prefijoSql)) {
            ps.setInt(1, categoriaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                prefijo = rs.getString(1);
            }
        }

        String ultimoSql = """
            SELECT codigo
            FROM producto
            WHERE categoria_id = ?
            ORDER BY producto_id DESC
            LIMIT 1
        """;

        int siguienteNumero = 1;

        try (PreparedStatement ps = con.prepareStatement(ultimoSql)) {
            ps.setInt(1, categoriaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String ultimoCodigo = rs.getString("codigo");
                int numero = Integer.parseInt(ultimoCodigo.substring(3));
                siguienteNumero = numero + 1;
            }
        }

        return prefijo + String.format("%02d", siguienteNumero);
    }

    /* ===============================
       MAPEO CENTRALIZADO
       =============================== */
    private Producto mapearProducto(ResultSet rs) throws SQLException {

        Producto p = new Producto();

        p.setProductoId(rs.getInt("producto_id"));
        p.setCodigo(rs.getString("codigo"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setStock(rs.getInt("stock"));
        p.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        p.setPrecioVenta(rs.getBigDecimal("precio_venta"));
        p.setImagen(rs.getString("imagen"));
        p.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());

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
