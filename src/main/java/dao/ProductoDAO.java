package dao;

import config.ConexionDB;
import model.Categoria;
import model.Material;
import model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    /* ═══════════════════════════════════════════════
       LISTAR POR CATEGORÍA
    ═══════════════════════════════════════════════ */
    public List<Producto> listarPorCategoria(int categoriaId) {
        List<Producto> lista = new ArrayList<>();
        String sql = """
            SELECT p.producto_id, p.codigo, p.nombre, p.descripcion, p.stock,
                   p.precio_unitario, p.precio_venta, p.imagen,
                   p.imagen_data, p.imagen_tipo, p.fecha_registro,
                   c.categoria_id, c.nombre AS categoria_nombre,
                   m.material_id, m.nombre AS material_nombre
            FROM Producto p
            INNER JOIN Categoria c ON p.categoria_id = c.categoria_id
            INNER JOIN Material  m ON p.material_id  = m.material_id
            WHERE p.categoria_id = ?
            ORDER BY p.nombre
        """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoriaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearProducto(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    /* ═══════════════════════════════════════════════
       BÚSQUEDA GLOBAL — filtro: "todos"|"nombre"|"material"|"stock"
    ═══════════════════════════════════════════════ */
    public List<Producto> buscarGlobal(String termino, String filtro) {
        List<Producto> lista = new ArrayList<>();
        String sql = buildSearchSql(false, filtro);
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bindParams(ps, 1, termino, filtro, -1);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearProducto(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    /* ═══════════════════════════════════════════════
       BÚSQUEDA EN CATEGORÍA — filtro: "todos"|"nombre"|"material"|"stock"
    ═══════════════════════════════════════════════ */
    public List<Producto> buscarEnCategoria(int categoriaId, String termino, String filtro) {
        List<Producto> lista = new ArrayList<>();
        String sql = buildSearchSql(true, filtro);
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bindParams(ps, 1, termino, filtro, categoriaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearProducto(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    /** Compatibilidad hacia atrás sin filtro → usa "todos" */
    public List<Producto> buscarGlobal(String termino) {
        return buscarGlobal(termino, "todos");
    }
    public List<Producto> buscarEnCategoria(int categoriaId, String termino) {
        return buscarEnCategoria(categoriaId, termino, "todos");
    }

    /* ═══════════════════════════════════════════════
       OBTENER POR ID
    ═══════════════════════════════════════════════ */
    public Producto obtenerPorId(int id) {
        String sql = """
            SELECT p.producto_id, p.codigo, p.nombre, p.descripcion, p.stock,
                   p.precio_unitario, p.precio_venta, p.imagen,
                   p.imagen_data, p.imagen_tipo, p.fecha_registro,
                   c.categoria_id, c.nombre AS categoria_nombre,
                   m.material_id, m.nombre AS material_nombre
            FROM Producto p
            INNER JOIN Categoria c ON p.categoria_id = c.categoria_id
            INNER JOIN Material  m ON p.material_id  = m.material_id
            WHERE p.producto_id = ?
        """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapearProducto(rs);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /* ═══════════════════════════════════════════════
       GUARDAR
    ═══════════════════════════════════════════════ */
    public void guardar(Producto p, int proveedorId) {
        String sql = """
            INSERT INTO Producto
              (codigo, nombre, descripcion, stock, precio_unitario, precio_venta,
               fecha_registro, material_id, categoria_id, usuario_proveedor_id,
               imagen, imagen_data, imagen_tipo)
            VALUES (?, ?, ?, ?, ?, ?, CURDATE(), ?, ?, ?, ?, ?, ?)
        """;
        try (Connection con = ConexionDB.getConnection()) {
            String codigo = generarCodigo(con, p.getCategoria().getCategoriaId());
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
                if (p.getImagenData() != null && p.getImagenData().length > 0) {
                    ps.setBytes(11, p.getImagenData());
                    ps.setString(12, p.getImagenTipo());
                } else {
                    ps.setNull(11, Types.BLOB);
                    ps.setNull(12, Types.VARCHAR);
                }
                ps.executeUpdate();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    /* ═══════════════════════════════════════════════
       ACTUALIZAR
    ═══════════════════════════════════════════════ */
    public void actualizar(Producto p) {
        boolean nuevaImg = p.getImagenData() != null && p.getImagenData().length > 0;
        String sql = nuevaImg
            ? """
                UPDATE Producto SET nombre=?, descripcion=?, stock=?,
                    precio_unitario=?, precio_venta=?, material_id=?,
                    imagen=?, imagen_data=?, imagen_tipo=?
                WHERE producto_id=?
              """
            : """
                UPDATE Producto SET nombre=?, descripcion=?, stock=?,
                    precio_unitario=?, precio_venta=?, material_id=?, imagen=?
                WHERE producto_id=?
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
            if (nuevaImg) {
                ps.setBytes(8, p.getImagenData());
                ps.setString(9, p.getImagenTipo());
                ps.setInt(10, p.getProductoId());
            } else {
                ps.setInt(8, p.getProductoId());
            }
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /* ═══════════════════════════════════════════════
       ELIMINAR
    ═══════════════════════════════════════════════ */
    public void eliminar(int id) {
        String sql = "DELETE FROM Producto WHERE producto_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /* ═══════════════════════════════════════════════
       HELPERS PRIVADOS
    ═══════════════════════════════════════════════ */

    /**
     * Construye el SQL de búsqueda según filtro y contexto.
     * filtro: "todos" | "nombre" | "material" | "stock"
     */
    private String buildSearchSql(boolean porCategoria, String filtro) {
        String select = """
            SELECT p.producto_id, p.codigo, p.nombre, p.descripcion, p.stock,
                   p.precio_unitario, p.precio_venta, p.imagen,
                   p.imagen_data, p.imagen_tipo, p.fecha_registro,
                   c.categoria_id, c.nombre AS categoria_nombre,
                   m.material_id, m.nombre AS material_nombre
            FROM Producto p
            INNER JOIN Categoria c ON p.categoria_id = c.categoria_id
            INNER JOIN Material  m ON p.material_id  = m.material_id
            WHERE
        """;

        StringBuilder sb = new StringBuilder(select);

        if (porCategoria) sb.append(" p.categoria_id = ? AND (");
        else              sb.append(" (");

        switch (filtro == null ? "todos" : filtro) {
            case "nombre"   -> sb.append("p.nombre LIKE ? OR p.codigo LIKE ?");
            case "material" -> sb.append("m.nombre LIKE ?");
            case "stock"    -> sb.append("CAST(p.stock AS CHAR) LIKE ?");
            default         -> sb.append(
                                "p.nombre LIKE ? OR p.codigo LIKE ? " +
                                "OR p.descripcion LIKE ? " +
                                "OR m.nombre LIKE ? " +
                                "OR CAST(p.stock AS CHAR) LIKE ?");
        }

        sb.append(") ORDER BY p.nombre");
        return sb.toString();
    }

    /**
     * Enlaza los parámetros del PreparedStatement.
     * categoriaId = -1 para búsqueda global.
     */
    private void bindParams(PreparedStatement ps, int start,
                            String termino, String filtro,
                            int categoriaId) throws SQLException {
        int i = start;
        if (categoriaId > 0) ps.setInt(i++, categoriaId);

        String like = "%" + termino + "%";
        switch (filtro == null ? "todos" : filtro) {
            case "nombre" -> {
                ps.setString(i++, like);  // nombre
                ps.setString(i++, like);  // codigo
            }
            case "material" -> ps.setString(i++, like);
            case "stock"    -> ps.setString(i++, like);
            default -> {
                ps.setString(i++, like);  // nombre
                ps.setString(i++, like);  // codigo
                ps.setString(i++, like);  // descripcion
                ps.setString(i++, like);  // material
                ps.setString(i++, like);  // stock
            }
        }
    }

    private String generarCodigo(Connection con, int categoriaId) throws SQLException {
        String prefijo = "PRD";
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT UPPER(LEFT(nombre,3)) AS p FROM Categoria WHERE categoria_id=?")) {
            ps.setInt(1, categoriaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) prefijo = rs.getString("p");
        }
        int siguiente = 1;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT codigo FROM Producto WHERE categoria_id=? ORDER BY producto_id DESC LIMIT 1")) {
            ps.setInt(1, categoriaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                try { siguiente = Integer.parseInt(rs.getString("codigo").substring(3)) + 1; }
                catch (NumberFormatException ignored) {}
            }
        }
        return prefijo + String.format("%02d", siguiente);
    }

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
        p.setImagenData(rs.getBytes("imagen_data"));
        p.setImagenTipo(rs.getString("imagen_tipo"));
        Date f = rs.getDate("fecha_registro");
        if (f != null) p.setFechaRegistro(f.toLocalDate());
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
