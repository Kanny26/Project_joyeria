package dao;

import config.ConexionDB;
import model.Categoria;
import model.Material;
import model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO encargado de la gestión de productos.
 * 
 * Responsabilidades:
 * - Consultar productos (por categoría, búsqueda global, por ID)
 * - Registrar, actualizar y eliminar productos
 * - Generar códigos automáticos por categoría
 * - Centralizar el mapeo ResultSet → Producto
 */
public class ProductoDAO {

    /* ===============================
       LISTAR POR CATEGORÍA
       =============================== */

    /**
     * Obtiene todos los productos pertenecientes a una categoría específica.
     *
     * @param categoriaId ID de la categoría
     * @return lista de productos asociados
     */
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

    /* ===============================
       BÚSQUEDA GLOBAL
       =============================== */

    /**
     * Realiza una búsqueda global de productos usando un término libre.
     * Busca en nombre, código, descripción, categoría y material.
     *
     * @param termino texto a buscar
     * @return lista de productos coincidentes
     */
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

            for (int i = 1; i <= 5; i++) {
                ps.setString(i, likeTerm);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapearProducto(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    /* ===============================
       BÚSQUEDA EN CATEGORÍA
       =============================== */

    /**
     * Busca productos dentro de una categoría específica.
     *
     * @param categoriaId ID de la categoría
     * @param termino texto a buscar
     * @return lista de productos coincidentes
     */
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

            while (rs.next()) {
                lista.add(mapearProducto(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    /* ===============================
       OBTENER POR ID
       =============================== */

    /**
     * Obtiene un producto por su ID.
     *
     * @param id ID del producto
     * @return producto encontrado o null
     */
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
       GUARDAR PRODUCTO
       =============================== */

    /**
     * Registra un nuevo producto en la base de datos.
     * El código del producto se genera automáticamente según la categoría.
     *
     * @param p producto a guardar
     * @param proveedorId ID del proveedor
     */
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

    /**
     * Actualiza los datos principales de un producto existente.
     *
     * @param p producto con datos actualizados
     */
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

    /**
     * Elimina un producto por su ID.
     *
     * @param id ID del producto
     */
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

    /**
     * Genera un código único de producto basado en la categoría.
     * Ejemplo: ANI01, ANI02, etc.
     */
    private String generarCodigoProducto(Connection con, int categoriaId) throws SQLException {

        String prefijo = "";

        String prefijoSql = "SELECT UPPER(LEFT(nombre, 3)) FROM categoria WHERE categoria_id = ?";

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

    /**
     * Convierte un ResultSet en un objeto Producto completamente armado.
     */
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
        p.setFechaRegistro(rs.getDate("fecha_registro").toLocalDate());

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
