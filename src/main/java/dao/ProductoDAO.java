package dao;

import config.ConexionDB;
import model.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    private static final String SELECT_BASE = """
        SELECT p.producto_id, p.codigo, p.nombre, p.descripcion, p.stock, p.precio_unitario, 
               p.precio_venta, p.imagen, p.imagen_data, p.imagen_tipo, p.fecha_registro, 
               p.material_id, p.categoria_id, p.subcategoria_id, p.proveedor_id, p.estado,
               m.nombre AS material_nombre, c.nombre AS categoria_nombre, 
               s.nombre AS subcategoria_nombre, prov.nombre AS proveedor_nombre 
        FROM Producto p 
        INNER JOIN Material m ON m.material_id = p.material_id 
        INNER JOIN Categoria c ON c.categoria_id = p.categoria_id 
        INNER JOIN Proveedor prov ON prov.proveedor_id = p.proveedor_id 
        LEFT JOIN Subcategoria s ON s.subcategoria_id = p.subcategoria_id
        """;

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // LISTAR POR CATEGORÍA
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public List<Producto> listarPorCategoria(int categoriaId) throws Exception {
        List<Producto> lista = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE p.categoria_id = ? AND p.estado = 1 ORDER BY p.codigo ASC";
        
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

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // LISTAR DISPONIBLES (stock > 0, sin BLOB, para ventas)
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public List<Producto> listarProductosDisponibles() throws Exception {
        List<Producto> lista = new ArrayList<>();
        String sql = """
            SELECT p.producto_id, p.codigo, p.nombre, p.descripcion, p.stock, p.precio_venta, 
                   p.imagen, m.nombre AS material_nombre, c.nombre AS categoria_nombre 
            FROM Producto p 
            INNER JOIN Material m ON m.material_id = p.material_id 
            INNER JOIN Categoria c ON c.categoria_id = p.categoria_id 
            WHERE p.stock > 0 AND p.estado = 1 
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
                prod.setDescripcion(rs.getString("descripcion"));
                prod.setStock(rs.getInt("stock"));
                prod.setPrecioVenta(rs.getBigDecimal("precio_venta"));
                prod.setImagen(rs.getString("imagen"));
                prod.setMaterialNombre(rs.getString("material_nombre"));
                prod.setCategoriaNombre(rs.getString("categoria_nombre"));
                lista.add(prod);
            }
        }
        return lista;
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // OBTENER POR ID (completo, con imagen_data)
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public Producto obtenerPorId(int id) throws Exception {
        String sql = SELECT_BASE + " WHERE p.producto_id = ?";
        
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

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // OBTENER CON STOCK (para validación en ventas, sin BLOB)
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public Producto obtenerProductoConStock(int productoId) throws Exception {
        String sql = """
            SELECT p.producto_id, p.codigo, p.nombre, p.descripcion, p.stock, 
                   p.precio_unitario, p.precio_venta, p.imagen, 
                   m.nombre AS material_nombre, c.nombre AS categoria_nombre
            FROM Producto p 
            INNER JOIN Material m ON m.material_id = p.material_id 
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
                    prod.setDescripcion(rs.getString("descripcion"));
                    prod.setStock(rs.getInt("stock"));
                    prod.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    prod.setPrecioVenta(rs.getBigDecimal("precio_venta"));
                    prod.setImagen(rs.getString("imagen"));
                    prod.setMaterialNombre(rs.getString("material_nombre"));
                    prod.setCategoriaNombre(rs.getString("categoria_nombre"));
                    return prod;
                }
            }
        }
        return null;
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // GUARDAR NUEVO PRODUCTO
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public void guardar(Producto p, int usuarioId, int proveedorId) throws Exception {
        // ■■ VALIDACIÓN RF13: Precio venta ≥ precio costo ■■
        if (p.getPrecioVenta().compareTo(p.getPrecioUnitario()) < 0) {
            throw new Exception("El precio de venta no puede ser menor al precio de costo.");
        }

        String sql = """
            INSERT INTO Producto (codigo, nombre, descripcion, stock, precio_unitario, 
                    precio_venta, fecha_registro, material_id, categoria_id, subcategoria_id, 
                    proveedor_id, imagen, imagen_data, imagen_tipo, estado) 
            VALUES (?, ?, ?, ?, ?, ?, CURDATE(), ?, ?, ?, ?, ?, ?, ?, 1)
            """;
        
        String sqlInventario = """
            INSERT INTO Inventario_Movimiento (producto_id, usuario_id, tipo, cantidad, fecha, referencia) 
            VALUES (?, ?, 'entrada', ?, NOW(), ?)
            """;

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            
            try {
                p.setCodigo(generarCodigo(con, p.getCategoriaId()));
                
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, p.getCodigo());
                    ps.setString(2, p.getNombre());
                    ps.setString(3, p.getDescripcion());
                    ps.setInt(4, p.getStock());
                    ps.setBigDecimal(5, p.getPrecioUnitario());
                    ps.setBigDecimal(6, p.getPrecioVenta());
                    ps.setInt(7, p.getMaterialId());
                    ps.setInt(8, p.getCategoriaId());
                    
                    if (p.getSubcategoriaId() > 0) {
                        ps.setInt(9, p.getSubcategoriaId());
                    } else {
                        ps.setNull(9, Types.INTEGER);
                    }
                    
                    ps.setInt(10, proveedorId);
                    ps.setString(11, p.getImagen());
                    
                    if (p.getImagenData() != null && p.getImagenData().length > 0) {
                        ps.setBytes(12, p.getImagenData());
                        ps.setString(13, p.getImagenTipo());
                    } else {
                        ps.setNull(12, Types.BLOB);
                        ps.setNull(13, Types.VARCHAR);
                    }
                    
                    ps.executeUpdate();
                    
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            int productoId = keys.getInt(1);
                            p.setProductoId(productoId);
                            
                            // ■■ RF16: Registrar movimiento de inventario ■■
                            try (PreparedStatement psInv = con.prepareStatement(sqlInventario)) {
                                psInv.setInt(1, productoId);
                                psInv.setInt(2, usuarioId);
                                psInv.setInt(3, p.getStock());
                                psInv.setString(4, "CREACION-PROD-" + productoId);
                                psInv.executeUpdate();
                            }
                        }
                    }
                }
                
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // ACTUALIZAR PRODUCTO
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public void actualizar(Producto p, int usuarioId) throws Exception {
        // ■■ VALIDACIÓN RF13: Precio venta ≥ precio costo ■■
        if (p.getPrecioVenta().compareTo(p.getPrecioUnitario()) < 0) {
            throw new Exception("El precio de venta no puede ser menor al precio de costo.");
        }

        // ■■ Obtener stock anterior para registro en inventario ■■
        int stockAnterior = obtenerStockActual(p.getProductoId());
        int diferenciaStock = p.getStock() - stockAnterior;

        boolean nuevaImg = p.getImagenData() != null && p.getImagenData().length > 0;
        
        String sql = nuevaImg ? """
            UPDATE Producto SET nombre=?, descripcion=?, stock=?, precio_unitario=?, 
                    precio_venta=?, material_id=?, categoria_id=?, subcategoria_id=?, 
                    imagen=?, imagen_data=?, imagen_tipo=? 
            WHERE producto_id=?
            """ : """
            UPDATE Producto SET nombre=?, descripcion=?, stock=?, precio_unitario=?, 
                    precio_venta=?, material_id=?, categoria_id=?, subcategoria_id=?, imagen=? 
            WHERE producto_id=?
            """;

        String sqlInventario = """
            INSERT INTO Inventario_Movimiento (producto_id, usuario_id, tipo, cantidad, fecha, referencia) 
            VALUES (?, ?, ?, ?, NOW(), ?)
            """;

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            
            try {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, p.getNombre());
                    ps.setString(2, p.getDescripcion());
                    ps.setInt(3, p.getStock());
                    ps.setBigDecimal(4, p.getPrecioUnitario());
                    ps.setBigDecimal(5, p.getPrecioVenta());
                    ps.setInt(6, p.getMaterialId());
                    ps.setInt(7, p.getCategoriaId());
                    
                    if (p.getSubcategoriaId() > 0) {
                        ps.setInt(8, p.getSubcategoriaId());
                    } else {
                        ps.setNull(8, Types.INTEGER);
                    }
                    
                    ps.setString(9, p.getImagen());
                    
                    int idx = 10;
                    if (nuevaImg) {
                        ps.setBytes(idx++, p.getImagenData());
                        ps.setString(idx++, p.getImagenTipo());
                    }
                    ps.setInt(idx, p.getProductoId());
                    
                    ps.executeUpdate();
                }
                
                // ■■ RF16: Registrar movimiento de inventario si hay cambio de stock ■■
                if (diferenciaStock != 0) {
                    try (PreparedStatement psInv = con.prepareStatement(sqlInventario)) {
                        psInv.setInt(1, p.getProductoId());
                        psInv.setInt(2, usuarioId);
                        psInv.setString(3, diferenciaStock > 0 ? "entrada" : "salida");
                        psInv.setInt(4, Math.abs(diferenciaStock));
                        psInv.setString(5, "ACTUALIZACION-PROD-" + p.getProductoId());
                        psInv.executeUpdate();
                    }
                }
                
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // ■■ RF13: Eliminación lógica (estado = false) ■■
    public void eliminar(int id, int usuarioId) throws Exception {
        String sql = "UPDATE Producto SET estado = 0 WHERE producto_id = ?";
        
        String sqlInventario = """
            INSERT INTO Inventario_Movimiento (producto_id, usuario_id, tipo, cantidad, fecha, referencia) 
            VALUES (?, ?, 'salida', ?, NOW(), ?)
            """;

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            
            try {
                // Obtener stock actual antes de eliminar
                int stockActual = obtenerStockActual(id);
                
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                
                // ■■ RF16: Registrar movimiento de inventario ■■
                if (stockActual > 0) {
                    try (PreparedStatement psInv = con.prepareStatement(sqlInventario)) {
                        psInv.setInt(1, id);
                        psInv.setInt(2, usuarioId);
                        psInv.setInt(3, stockActual);
                        psInv.setString(4, "ELIMINACION-PROD-" + id);
                        psInv.executeUpdate();
                    }
                }
                
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // AUXILIAR: obtener stock actual de un producto
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private int obtenerStockActual(int productoId) throws Exception {
        String sql = "SELECT stock FROM Producto WHERE producto_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("stock") : 0;
            }
        }
    }

    // ■■ RF14: Validar si categoría tiene productos activos ■■
    public boolean tieneProductosActivos(int categoriaId) throws Exception {
        String sql = "SELECT COUNT(*) FROM Producto WHERE categoria_id = ? AND estado = 1";
        
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // BÚSQUEDA GLOBAL (sin filtro de categoría)
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
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

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // BÚSQUEDA DENTRO DE UNA CATEGORÍA
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
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

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // AUXILIAR: construir SQL de búsqueda dinámico
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private String buildSearchSql(boolean porCategoria, String filtro) {
        String base = """
            SELECT p.producto_id, p.codigo, p.nombre, p.descripcion, p.stock, 
                   p.precio_unitario, p.precio_venta, p.imagen, NULL AS imagen_data, 
                   p.imagen_tipo, p.fecha_registro, p.material_id, p.categoria_id, 
                   p.subcategoria_id, p.proveedor_id, p.estado,
                   m.nombre AS material_nombre, c.nombre AS categoria_nombre, 
                   s.nombre AS subcategoria_nombre, prov.nombre AS proveedor_nombre 
            FROM Producto p 
            INNER JOIN Material m ON m.material_id = p.material_id 
            INNER JOIN Categoria c ON c.categoria_id = p.categoria_id 
            INNER JOIN Proveedor prov ON prov.proveedor_id = p.proveedor_id 
            LEFT JOIN Subcategoria s ON s.subcategoria_id = p.subcategoria_id 
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
            case "subcategoria" -> sb.append("s.nombre LIKE ?");
            default -> sb.append(
                "p.nombre LIKE ? OR p.codigo LIKE ? OR p.descripcion LIKE ? " +
                "OR m.nombre LIKE ? OR CAST(p.stock AS CHAR) LIKE ? OR s.nombre LIKE ?"
            );
        }
        
        sb.append(") AND p.estado = 1 ORDER BY p.nombre");
        return sb.toString();
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // AUXILIAR: bindear parámetros de búsqueda
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private void bindParams(PreparedStatement ps, int start, String termino, String filtro, int categoriaId) throws SQLException {
        int i = start;
        if (categoriaId > 0) {
            ps.setInt(i++, categoriaId);
        }
        
        String like = "%" + termino + "%";
        
        switch (filtro != null ? filtro : "todos") {
            case "nombre" -> {
                ps.setString(i++, like);
                ps.setString(i++, like);
            }
            case "material" -> ps.setString(i++, like);
            case "stock" -> ps.setString(i++, like);
            case "subcategoria" -> ps.setString(i++, like);
            default -> {
                ps.setString(i++, like); // nombre
                ps.setString(i++, like); // codigo
                ps.setString(i++, like); // descripcion
                ps.setString(i++, like); // material
                ps.setString(i++, like); // stock
                ps.setString(i++, like); // subcategoria
            }
        }
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // AUXILIAR: mapear ResultSet → Producto
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
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
        p.setEstado(rs.getBoolean("estado"));
        
        Date f = rs.getDate("fecha_registro");
        if (f != null) p.setFechaRegistro(f);
        
        p.setMaterialId(rs.getInt("material_id"));
        p.setCategoriaId(rs.getInt("categoria_id"));
        p.setSubcategoriaId(rs.getInt("subcategoria_id"));
        
        if (rs.getObject("proveedor_id") != null) {
            p.setProveedorId(rs.getInt("proveedor_id"));
        }
        
        p.setMaterialNombre(rs.getString("material_nombre"));
        p.setCategoriaNombre(rs.getString("categoria_nombre"));
        p.setSubcategoriaNombre(rs.getString("subcategoria_nombre"));
        
        if (rs.getObject("proveedor_nombre") != null) {
            p.setProveedorNombre(rs.getString("proveedor_nombre"));
        }
        
        return p;
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // AUXILIAR: generar código automático por categoría
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private String generarCodigo(Connection con, int categoriaId) throws SQLException {
        String prefijo = "PRD";
        
        String sqlPrefijo = "SELECT UPPER(LEFT(nombre, 3)) AS p FROM Categoria WHERE categoria_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlPrefijo)) {
            ps.setInt(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString("p") != null) {
                    prefijo = rs.getString("p");
                }
            }
        }
        
        int siguiente = 1;
        String sqlUltimo = """
            SELECT codigo FROM Producto WHERE categoria_id = ? AND estado = 1 
            ORDER BY producto_id DESC LIMIT 1
            """;
        
        try (PreparedStatement ps = con.prepareStatement(sqlUltimo)) {
            ps.setInt(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString("codigo") != null) {
                    String numStr = rs.getString("codigo").replaceAll("[^0-9]", "");
                    if (!numStr.isEmpty()) {
                        try {
                            siguiente = Integer.parseInt(numStr) + 1;
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
        
        return prefijo + String.format("%02d", siguiente);
    }
}