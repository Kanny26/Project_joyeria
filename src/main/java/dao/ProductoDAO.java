package dao;

import config.ConexionDB;
import model.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    /* ═══════════════════════════════════════════════
       LISTAR POR CATEGORÍA
    ═══════════════════════════════════════════════ */
    public List<Producto> listarPorCategoria(int categoriaId) throws Exception {
        List<Producto> lista = new ArrayList<>();
        String sql = """
            SELECT p.producto_id, p.codigo, p.nombre, p.descripcion, p.stock,
                   p.precio_unitario, p.precio_venta, p.imagen,
                   p.imagen_data, p.imagen_tipo, p.fecha_registro,
                   p.material_id, p.categoria_id, p.usuario_proveedor_id,
                   m.nombre AS material_nombre,
                   c.nombre AS categoria_nombre,
                   u.nombre AS proveedor_nombre
            FROM Producto p
            INNER JOIN Material  m ON m.material_id  = p.material_id
            INNER JOIN Categoria c ON c.categoria_id = p.categoria_id
            INNER JOIN Usuario   u ON u.usuario_id   = p.usuario_proveedor_id
            WHERE p.categoria_id = ?
            ORDER BY p.nombre
            """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProducto(rs));
                }
            }
        }
        return lista;
    }

    /* ═══════════════════════════════════════════════
       LISTAR PRODUCTOS DISPONIBLES (para módulo de ventas)
    ═══════════════════════════════════════════════ */
    public List<Producto> listarProductosDisponibles() throws Exception {
        List<Producto> lista = new ArrayList<>();
        String sql = """
            SELECT p.producto_id, p.codigo, p.nombre, p.descripcion, p.stock,
                   p.precio_venta, p.imagen,
                   m.nombre AS material_nombre,
                   c.nombre AS categoria_nombre
            FROM Producto p
            INNER JOIN Material  m ON m.material_id  = p.material_id
            INNER JOIN Categoria c ON c.categoria_id = p.categoria_id
            WHERE p.stock > 0
            ORDER BY p.nombre
            """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Producto prod = new Producto();
                prod.setProductoId(rs.getInt("producto_id"));
                prod.setCodigo(rs.getString("codigo"));
                prod.setNombre(rs.getString("nombre"));
                prod.setStock(rs.getInt("stock"));
                prod.setPrecioVenta(rs.getBigDecimal("precio_venta"));
                prod.setImagen(rs.getString("imagen"));
                prod.setCategoriaNombre(rs.getString("categoria_nombre"));
                prod.setMaterialNombre(rs.getString("material_nombre"));
                lista.add(prod);
            }
        }
        return lista;
    }

    /* ═══════════════════════════════════════════════
       OBTENER PRODUCTO CON STOCK (para validación en ventas)
    ═══════════════════════════════════════════════ */
    public Producto obtenerProductoConStock(int productoId) throws Exception {
        String sql = """
            SELECT p.producto_id, p.codigo, p.nombre, p.descripcion, p.stock,
                   p.precio_unitario, p.precio_venta, p.imagen,
                   m.nombre AS material_nombre,
                   c.nombre AS categoria_nombre
            FROM Producto p
            INNER JOIN Material  m ON m.material_id  = p.material_id
            INNER JOIN Categoria c ON c.categoria_id = p.categoria_id
            WHERE p.producto_id = ?
            """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Producto prod = new Producto();
                    prod.setProductoId(rs.getInt("producto_id"));
                    prod.setCodigo(rs.getString("codigo"));
                    prod.setNombre(rs.getString("nombre"));
                    prod.setStock(rs.getInt("stock"));
                    prod.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    prod.setPrecioVenta(rs.getBigDecimal("precio_venta"));
                    prod.setImagen(rs.getString("imagen"));
                    prod.setCategoriaNombre(rs.getString("categoria_nombre"));
                    prod.setMaterialNombre(rs.getString("material_nombre"));
                    return prod;
                }
            }
        }
        return null;
    }

    /* ═══════════════════════════════════════════════
       OBTENER POR ID (con todos los datos)
    ═══════════════════════════════════════════════ */
    public Producto obtenerPorId(int id) throws Exception {
        String sql = """
            SELECT p.producto_id, p.codigo, p.nombre, p.descripcion, p.stock,
                   p.precio_unitario, p.precio_venta, p.imagen,
                   p.imagen_data, p.imagen_tipo, p.fecha_registro,
                   p.material_id, p.categoria_id, p.usuario_proveedor_id,
                   m.nombre AS material_nombre,
                   c.nombre AS categoria_nombre,
                   u.nombre AS proveedor_nombre
            FROM Producto p
            INNER JOIN Material  m ON m.material_id  = p.material_id
            INNER JOIN Categoria c ON c.categoria_id = p.categoria_id
            INNER JOIN Usuario   u ON u.usuario_id   = p.usuario_proveedor_id
            WHERE p.producto_id = ?
            """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }
        }
        return null;
    }

    /* ═══════════════════════════════════════════════
       GUARDAR NUEVO PRODUCTO
    ═══════════════════════════════════════════════ */
    public void guardar(Producto p, int proveedorId) throws Exception {
        String sql = """
            INSERT INTO Producto
              (codigo, nombre, descripcion, stock, precio_unitario, precio_venta,
               fecha_registro, material_id, categoria_id, usuario_proveedor_id,
               imagen, imagen_data, imagen_tipo)
            VALUES (?, ?, ?, ?, ?, ?, CURDATE(), ?, ?, ?, ?, ?, ?)
            """;
        try (Connection con = ConexionDB.getConnection()) {
            // Generar código único
            String codigo = generarCodigo(con, p.getCategoriaId());
            p.setCodigo(codigo);
            
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, p.getCodigo());
                ps.setString(2, p.getNombre());
                ps.setString(3, p.getDescripcion());
                ps.setInt(4, p.getStock());
                ps.setBigDecimal(5, p.getPrecioUnitario());
                ps.setBigDecimal(6, p.getPrecioVenta());
                ps.setInt(7, p.getMaterialId());
                ps.setInt(8, p.getCategoriaId());
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
        }
    }

    /* ═══════════════════════════════════════════════
       ACTUALIZAR PRODUCTO
    ═══════════════════════════════════════════════ */
    public void actualizar(Producto p) throws Exception {
        boolean nuevaImg = p.getImagenData() != null && p.getImagenData().length > 0;
        
        String sql = nuevaImg
            ? """
                UPDATE Producto SET nombre=?, descripcion=?, stock=?,
                    precio_unitario=?, precio_venta=?, material_id=?,
                    categoria_id=?, imagen=?, imagen_data=?, imagen_tipo=?
                WHERE producto_id=?
              """
            : """
                UPDATE Producto SET nombre=?, descripcion=?, stock=?,
                    precio_unitario=?, precio_venta=?, material_id=?,
                    categoria_id=?, imagen=?
                WHERE producto_id=?
              """;
              
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setInt(3, p.getStock());
            ps.setBigDecimal(4, p.getPrecioUnitario());
            ps.setBigDecimal(5, p.getPrecioVenta());
            ps.setInt(6, p.getMaterialId());
            ps.setInt(7, p.getCategoriaId());
            ps.setString(8, p.getImagen());
            
            int paramIndex = 9;
            if (nuevaImg) {
                ps.setBytes(paramIndex++, p.getImagenData());
                ps.setString(paramIndex++, p.getImagenTipo());
            }
            ps.setInt(paramIndex, p.getProductoId());
            
            ps.executeUpdate();
        }
    }

    /* ═══════════════════════════════════════════════
       ELIMINAR PRODUCTO (lógico: stock = 0)
    ═══════════════════════════════════════════════ */
    public void eliminar(int id) throws Exception {
        String sql = "UPDATE Producto SET stock = 0 WHERE producto_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /* ═══════════════════════════════════════════════
       BÚSQUEDA GLOBAL
    ═══════════════════════════════════════════════ */
    public List<Producto> buscarGlobal(String termino, String filtro) throws Exception {
        List<Producto> lista = new ArrayList<>();
        String sql = buildSearchSql(false, filtro);
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bindParams(ps, 1, termino, filtro, -1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProducto(rs));
                }
            }
        }
        return lista;
    }

    /* ═══════════════════════════════════════════════
       BÚSQUEDA EN CATEGORÍA
    ═══════════════════════════════════════════════ */
    public List<Producto> buscarEnCategoria(int categoriaId, String termino, String filtro) throws Exception {
        List<Producto> lista = new ArrayList<>();
        String sql = buildSearchSql(true, filtro);
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bindParams(ps, 1, termino, filtro, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProducto(rs));
                }
            }
        }
        return lista;
    }

    /* ═══════════════════════════════════════════════
       MÉTODOS PRIVADOS AUXILIARES
    ═══════════════════════════════════════════════ */
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
        if (f != null) p.setFechaRegistro(f);
        
        // IDs de relaciones
        p.setMaterialId(rs.getInt("material_id"));
        p.setCategoriaId(rs.getInt("categoria_id"));
        if (rs.getObject("usuario_proveedor_id") != null) {
            p.setUsuarioProveedorId(rs.getInt("usuario_proveedor_id"));
        }
        
        // Nombres para vistas
        p.setMaterialNombre(rs.getString("material_nombre"));
        p.setCategoriaNombre(rs.getString("categoria_nombre"));
        if (rs.getObject("proveedor_nombre") != null) {
            p.setProveedorNombre(rs.getString("proveedor_nombre"));
        }
        
        return p;
    }

    private String buildSearchSql(boolean porCategoria, String filtro) {
        String base = """
            SELECT p.producto_id, p.codigo, p.nombre, p.descripcion, p.stock,
                   p.precio_unitario, p.precio_venta, p.imagen,
                   p.material_id, p.categoria_id, p.usuario_proveedor_id,
                   m.nombre AS material_nombre,
                   c.nombre AS categoria_nombre,
                   u.nombre AS proveedor_nombre
            FROM Producto p
            INNER JOIN Material  m ON m.material_id  = p.material_id
            INNER JOIN Categoria c ON c.categoria_id = p.categoria_id
            INNER JOIN Usuario   u ON u.usuario_id   = p.usuario_proveedor_id
            WHERE
            """;
        StringBuilder sb = new StringBuilder(base);
        
        if (porCategoria) {
            sb.append(" p.categoria_id = ? AND (");
        } else {
            sb.append(" (");
        }
        
        switch (filtro != null ? filtro : "todos") {
            case "nombre" -> sb.append("p.nombre LIKE ? OR p.codigo LIKE ?");
            case "material" -> sb.append("m.nombre LIKE ?");
            case "stock" -> sb.append("CAST(p.stock AS CHAR) LIKE ?");
            default -> sb.append(
                "p.nombre LIKE ? OR p.codigo LIKE ? OR p.descripcion LIKE ? " +
                "OR m.nombre LIKE ? OR CAST(p.stock AS CHAR) LIKE ?");
        }
        sb.append(") ORDER BY p.nombre");
        return sb.toString();
    }

    private void bindParams(PreparedStatement ps, int start,
                           String termino, String filtro,
                           int categoriaId) throws SQLException {
        int i = start;
        if (categoriaId > 0) ps.setInt(i++, categoriaId);
        
        String like = "%" + termino + "%";
        switch (filtro != null ? filtro : "todos") {
            case "nombre" -> {
                ps.setString(i++, like);
                ps.setString(i++, like);
            }
            case "material" -> ps.setString(i++, like);
            case "stock" -> ps.setString(i++, like);
            default -> {
                ps.setString(i++, like); // nombre
                ps.setString(i++, like); // codigo
                ps.setString(i++, like); // descripcion
                ps.setString(i++, like); // material
                ps.setString(i++, like); // stock
            }
        }
    }

    private String generarCodigo(Connection con, int categoriaId) throws SQLException {
        String prefijo = "PRD";
        // Obtener prefijo de categoría (primeras 3 letras)
        String sqlPrefijo = "SELECT UPPER(LEFT(nombre, 3)) AS p FROM Categoria WHERE categoria_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlPrefijo)) {
            ps.setInt(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString("p") != null) {
                    prefijo = rs.getString("p");
                }
            }
        }
        
        // Obtener siguiente número
        int siguiente = 1;
        String sqlCount = "SELECT codigo FROM Producto WHERE categoria_id = ? ORDER BY producto_id DESC LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sqlCount)) {
            ps.setInt(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString("codigo") != null) {
                    String ultimo = rs.getString("codigo");
                    try {
                        // Extraer número después del prefijo
                        String numStr = ultimo.replaceAll("[^0-9]", "");
                        if (!numStr.isEmpty()) {
                            siguiente = Integer.parseInt(numStr) + 1;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return prefijo + String.format("%02d", siguiente);
    }
}