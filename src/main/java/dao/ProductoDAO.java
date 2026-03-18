package dao;

import config.ConexionDB;
import model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // ─────────────────────────────────────────────────────────────────────────
    // SELECT BASE
    // Se usa GROUP_CONCAT para obtener:
    //   subcategoria_nombres → "Compromiso, Aniversario, Uso Diario"  (para mostrar)
    //   subcategoria_ids_str → "2,6,12"                               (para preseleccionar checkboxes)
    // ─────────────────────────────────────────────────────────────────────────
    private static final String SELECT_BASE = """
        SELECT
            p.producto_id, p.codigo, p.nombre, p.descripcion, p.stock,
            p.precio_unitario, p.precio_venta, p.imagen, p.imagen_data,
            p.imagen_tipo, p.fecha_registro, p.material_id, p.categoria_id,
            p.proveedor_id, p.estado,
            m.nombre    AS material_nombre,
            c.nombre    AS categoria_nombre,
            prov.nombre AS proveedor_nombre,
            (SELECT GROUP_CONCAT(s.nombre ORDER BY s.nombre SEPARATOR ', ')
             FROM Producto_Subcategoria ps
             INNER JOIN Subcategoria s ON s.subcategoria_id = ps.subcategoria_id
             WHERE ps.producto_id = p.producto_id
            ) AS subcategoria_nombres,
            (SELECT GROUP_CONCAT(ps2.subcategoria_id ORDER BY ps2.subcategoria_id SEPARATOR ',')
             FROM Producto_Subcategoria ps2
             WHERE ps2.producto_id = p.producto_id
            ) AS subcategoria_ids_str
        FROM Producto p
        INNER JOIN Material  m    ON m.material_id     = p.material_id
        INNER JOIN Categoria c    ON c.categoria_id    = p.categoria_id
        INNER JOIN Proveedor prov ON prov.proveedor_id = p.proveedor_id
        """;

    // ─────────────────────────────────────────────────────────────────────────
    // LISTAR POR CATEGORÍA
    // ─────────────────────────────────────────────────────────────────────────
    public List<Producto> listarPorCategoria(int categoriaId) throws Exception {
        List<Producto> lista = new ArrayList<>();
        String sql = SELECT_BASE
            + " WHERE p.categoria_id = ? AND p.estado = 1 ORDER BY p.codigo ASC";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearProducto(rs));
            }
        }
        return lista;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LISTAR DISPONIBLES (para ventas — sin BLOB, sin subcategorías)
    // ─────────────────────────────────────────────────────────────────────────
    public List<Producto> listarProductosDisponibles() throws Exception {
        List<Producto> lista = new ArrayList<>();
        String sql = """
            SELECT p.producto_id, p.codigo, p.nombre, p.descripcion,
                   p.stock, p.precio_venta, p.imagen,
                   m.nombre AS material_nombre,
                   c.nombre AS categoria_nombre
            FROM Producto p
            INNER JOIN Material  m ON m.material_id  = p.material_id
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

    // ─────────────────────────────────────────────────────────────────────────
    // OBTENER POR ID
    // ─────────────────────────────────────────────────────────────────────────
    public Producto obtenerPorId(int id) throws Exception {
        String sql = SELECT_BASE + " WHERE p.producto_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearProducto(rs);
            }
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OBTENER CON STOCK (para validación en ventas — sin BLOB)
    // ─────────────────────────────────────────────────────────────────────────
    public Producto obtenerProductoConStock(int productoId) throws Exception {
        String sql = """
            SELECT p.producto_id, p.codigo, p.nombre, p.descripcion, p.stock,
                   p.precio_unitario, p.precio_venta, p.imagen,
                   m.nombre AS material_nombre, c.nombre AS categoria_nombre
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

    // ─────────────────────────────────────────────────────────────────────────
    // GUARDAR NUEVO PRODUCTO
    // CAMBIO: después del INSERT en Producto, inserta en Producto_Subcategoria
    // todos los IDs de subcategoría que vengan en p.getSubcategoriaIds().
    // Todo dentro de la misma transacción.
    // ─────────────────────────────────────────────────────────────────────────
    public void guardar(Producto p, int usuarioId) throws Exception {
        if (p.getPrecioVenta().compareTo(p.getPrecioUnitario()) < 0)
            throw new Exception("El precio de venta no puede ser menor al precio de costo.");
        if (p.getDescripcion() != null && p.getDescripcion().length() > 500)
            throw new Exception("La descripción no puede superar los 500 caracteres.");
        if (p.getProveedorId() <= 0)
            throw new Exception("Debes seleccionar un proveedor.");

        p.setStock(0);

        String sqlProducto = """
            INSERT INTO Producto
                (codigo, nombre, descripcion, stock, precio_unitario, precio_venta,
                 fecha_registro, material_id, categoria_id, proveedor_id,
                 imagen, imagen_data, imagen_tipo, estado)
            VALUES (?, ?, ?, 0, ?, ?, CURDATE(), ?, ?, ?, ?, ?, ?, 1)
            """;

        String sqlSubcat = """
            INSERT INTO Producto_Subcategoria (producto_id, subcategoria_id)
            VALUES (?, ?)
            """;

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                p.setCodigo(generarCodigo(con, p.getCategoriaId()));

                // 1. Insertar el producto
                try (PreparedStatement ps = con.prepareStatement(
                        sqlProducto, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, p.getCodigo());
                    ps.setString(2, p.getNombre());
                    ps.setString(3, p.getDescripcion());
                    ps.setBigDecimal(4, p.getPrecioUnitario());
                    ps.setBigDecimal(5, p.getPrecioVenta());
                    ps.setInt(6, p.getMaterialId());
                    ps.setInt(7, p.getCategoriaId());
                    ps.setInt(8, p.getProveedorId());
                    ps.setString(9, p.getImagen());
                    if (p.getImagenData() != null && p.getImagenData().length > 0) {
                        ps.setBytes(10, p.getImagenData());
                        ps.setString(11, p.getImagenTipo());
                    } else {
                        ps.setNull(10, Types.BLOB);
                        ps.setNull(11, Types.VARCHAR);
                    }
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) p.setProductoId(keys.getInt(1));
                    }
                }

                // 2. Insertar subcategorías en Producto_Subcategoria
                if (p.getSubcategoriaIds() != null && !p.getSubcategoriaIds().isEmpty()) {
                    try (PreparedStatement ps = con.prepareStatement(sqlSubcat)) {
                        for (int subcatId : p.getSubcategoriaIds()) {
                            if (subcatId > 0) {
                                ps.setInt(1, p.getProductoId());
                                ps.setInt(2, subcatId);
                                ps.addBatch();
                            }
                        }
                        ps.executeBatch();
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

    // ─────────────────────────────────────────────────────────────────────────
    // ACTUALIZAR PRODUCTO
    // CAMBIO: borra las subcategorías anteriores y reinserta las nuevas,
    // todo dentro de la misma transacción.
    // ─────────────────────────────────────────────────────────────────────────
    public void actualizar(Producto p, int usuarioId) throws Exception {
        if (p.getPrecioVenta().compareTo(p.getPrecioUnitario()) < 0)
            throw new Exception("El precio de venta no puede ser menor al precio de costo.");
        if (p.getDescripcion() != null && p.getDescripcion().length() > 500)
            throw new Exception("La descripción no puede superar los 500 caracteres.");
        if (p.getProveedorId() <= 0)
            throw new Exception("Debes seleccionar un proveedor.");

        boolean nuevaImg = p.getImagenData() != null && p.getImagenData().length > 0;

        String sqlProducto = nuevaImg ? """
            UPDATE Producto
            SET nombre=?, descripcion=?, precio_unitario=?, precio_venta=?,
                material_id=?, categoria_id=?, proveedor_id=?,
                imagen=?, imagen_data=?, imagen_tipo=?
            WHERE producto_id=?
            """ : """
            UPDATE Producto
            SET nombre=?, descripcion=?, precio_unitario=?, precio_venta=?,
                material_id=?, categoria_id=?, proveedor_id=?, imagen=?
            WHERE producto_id=?
            """;

        String sqlBorrarSubcat  = "DELETE FROM Producto_Subcategoria WHERE producto_id = ?";
        String sqlInsertarSubcat = """
            INSERT INTO Producto_Subcategoria (producto_id, subcategoria_id)
            VALUES (?, ?)
            """;

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Actualizar campos del producto
                try (PreparedStatement ps = con.prepareStatement(sqlProducto)) {
                    ps.setString(1, p.getNombre());
                    ps.setString(2, p.getDescripcion());
                    ps.setBigDecimal(3, p.getPrecioUnitario());
                    ps.setBigDecimal(4, p.getPrecioVenta());
                    ps.setInt(5, p.getMaterialId());
                    ps.setInt(6, p.getCategoriaId());
                    ps.setInt(7, p.getProveedorId());
                    ps.setString(8, p.getImagen());
                    int idx = 9;
                    if (nuevaImg) {
                        ps.setBytes(idx++, p.getImagenData());
                        ps.setString(idx++, p.getImagenTipo());
                    }
                    ps.setInt(idx, p.getProductoId());
                    ps.executeUpdate();
                }

                // 2. Borrar subcategorías anteriores
                try (PreparedStatement ps = con.prepareStatement(sqlBorrarSubcat)) {
                    ps.setInt(1, p.getProductoId());
                    ps.executeUpdate();
                }

                // 3. Reinsertar nuevas subcategorías
                if (p.getSubcategoriaIds() != null && !p.getSubcategoriaIds().isEmpty()) {
                    try (PreparedStatement ps = con.prepareStatement(sqlInsertarSubcat)) {
                        for (int subcatId : p.getSubcategoriaIds()) {
                            if (subcatId > 0) {
                                ps.setInt(1, p.getProductoId());
                                ps.setInt(2, subcatId);
                                ps.addBatch();
                            }
                        }
                        ps.executeBatch();
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

    // ─────────────────────────────────────────────────────────────────────────
    // AJUSTE MANUAL DE STOCK
    // ─────────────────────────────────────────────────────────────────────────
    public void actualizarStock(int productoId, int nuevoStock) throws Exception {
        String sql = "UPDATE Producto SET stock = ? WHERE producto_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevoStock);
            ps.setInt(2, productoId);
            ps.executeUpdate();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REGISTRAR MOVIMIENTO DE INVENTARIO
    // ─────────────────────────────────────────────────────────────────────────
    public void registrarMovimiento(int productoId, int usuarioId,
                                    String tipo, int cantidad,
                                    String referencia) throws Exception {
        String sql = """
            INSERT INTO Inventario_Movimiento
                (producto_id, usuario_id, tipo, cantidad, fecha, referencia)
            VALUES (?, ?, ?, ?, NOW(), ?)
            """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            ps.setInt(2, usuarioId);
            ps.setString(3, tipo);
            ps.setInt(4, cantidad);
            ps.setString(5, referencia);
            ps.executeUpdate();
        }
    }

    public int obtenerTotalEntradasPorCompras(int productoId) throws Exception {
        String sql = "SELECT COALESCE(SUM(cantidad), 0) FROM detalle_compra WHERE producto_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ELIMINACIÓN LÓGICA
    // ─────────────────────────────────────────────────────────────────────────
    public void eliminar(int id, int usuarioId) throws Exception {
        String sqlEstado     = "UPDATE Producto SET estado = 0 WHERE producto_id = ?";
        String sqlInventario = """
            INSERT INTO Inventario_Movimiento
                (producto_id, usuario_id, tipo, cantidad, fecha, referencia)
            VALUES (?, ?, 'salida', ?, NOW(), ?)
            """;
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                int stockActual = obtenerStockActual(con, id);
                try (PreparedStatement ps = con.prepareStatement(sqlEstado)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                if (stockActual > 0) {
                    try (PreparedStatement ps = con.prepareStatement(sqlInventario)) {
                        ps.setInt(1, id);
                        ps.setInt(2, usuarioId);
                        ps.setInt(3, stockActual);
                        ps.setString(4, "ELIMINACION-PROD-" + id);
                        ps.executeUpdate();
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

    public boolean tieneProductosActivos(int categoriaId) throws Exception {
        String sql = "SELECT COUNT(*) FROM Producto WHERE categoria_id = ? AND estado = 1";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BÚSQUEDA GLOBAL
    // ─────────────────────────────────────────────────────────────────────────
    public List<Producto> buscarGlobal(String termino, String filtro) throws Exception {
        List<Producto> lista = new ArrayList<>();
        String sql = buildSearchSql(false, filtro);
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bindParams(ps, 1, termino, filtro, -1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearProducto(rs));
            }
        }
        return lista;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BÚSQUEDA EN CATEGORÍA
    // ─────────────────────────────────────────────────────────────────────────
    public List<Producto> buscarEnCategoria(int categoriaId, String termino,
                                             String filtro) throws Exception {
        List<Producto> lista = new ArrayList<>();
        String sql = buildSearchSql(true, filtro);
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bindParams(ps, 1, termino, filtro, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearProducto(rs));
            }
        }
        return lista;
    }

    private String buildSearchSql(boolean porCategoria, String filtro) {
        // CAMBIO: para filtro "subcategoria" se busca en la subconsulta EXISTS,
        // ya que p.subcategoria_id ya no existe.
        String base = """
            SELECT
                p.producto_id, p.codigo, p.nombre, p.descripcion, p.stock,
                p.precio_unitario, p.precio_venta, p.imagen,
                NULL AS imagen_data,
                p.imagen_tipo, p.fecha_registro, p.material_id, p.categoria_id,
                p.proveedor_id, p.estado,
                m.nombre    AS material_nombre,
                c.nombre    AS categoria_nombre,
                prov.nombre AS proveedor_nombre,
                (SELECT GROUP_CONCAT(s.nombre ORDER BY s.nombre SEPARATOR ', ')
                 FROM Producto_Subcategoria ps
                 INNER JOIN Subcategoria s ON s.subcategoria_id = ps.subcategoria_id
                 WHERE ps.producto_id = p.producto_id
                ) AS subcategoria_nombres,
                (SELECT GROUP_CONCAT(ps2.subcategoria_id ORDER BY ps2.subcategoria_id SEPARATOR ',')
                 FROM Producto_Subcategoria ps2
                 WHERE ps2.producto_id = p.producto_id
                ) AS subcategoria_ids_str
            FROM Producto p
            INNER JOIN Material  m    ON m.material_id     = p.material_id
            INNER JOIN Categoria c    ON c.categoria_id    = p.categoria_id
            INNER JOIN Proveedor prov ON prov.proveedor_id = p.proveedor_id
            WHERE\s
            """;

        StringBuilder sb = new StringBuilder(base);
        if (porCategoria) sb.append(" p.categoria_id = ? AND (");
        else              sb.append(" (");

        switch (filtro != null ? filtro : "todos") {
            case "nombre"   -> sb.append("p.nombre LIKE ? OR p.codigo LIKE ?");
            case "material" -> sb.append("m.nombre LIKE ?");
            case "stock"    -> sb.append("CAST(p.stock AS CHAR) LIKE ?");
            // CAMBIO: búsqueda en subcategorías via EXISTS sobre Producto_Subcategoria
            case "subcategoria" -> sb.append("""
                EXISTS (
                    SELECT 1 FROM Producto_Subcategoria ps
                    INNER JOIN Subcategoria s ON s.subcategoria_id = ps.subcategoria_id
                    WHERE ps.producto_id = p.producto_id AND s.nombre LIKE ?
                )""");
            default -> sb.append("""
                p.nombre LIKE ? OR p.codigo LIKE ? OR p.descripcion LIKE ?
                OR m.nombre LIKE ? OR CAST(p.stock AS CHAR) LIKE ?
                OR EXISTS (
                    SELECT 1 FROM Producto_Subcategoria ps
                    INNER JOIN Subcategoria s ON s.subcategoria_id = ps.subcategoria_id
                    WHERE ps.producto_id = p.producto_id AND s.nombre LIKE ?
                )""");
        }
        sb.append(") AND p.estado = 1 ORDER BY p.nombre");
        return sb.toString();
    }

    private void bindParams(PreparedStatement ps, int start, String termino,
                            String filtro, int categoriaId) throws SQLException {
        int i = start;
        if (categoriaId > 0) ps.setInt(i++, categoriaId);
        String like = "%" + termino + "%";
        switch (filtro != null ? filtro : "todos") {
            case "nombre"       -> { ps.setString(i++, like); ps.setString(i, like); }
            case "material"     -> ps.setString(i, like);
            case "stock"        -> ps.setString(i, like);
            case "subcategoria" -> ps.setString(i, like);
            default -> {
                ps.setString(i++, like); ps.setString(i++, like); ps.setString(i++, like);
                ps.setString(i++, like); ps.setString(i++, like); ps.setString(i, like);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MAPEO ResultSet → Producto
    // CAMBIO: se leen subcategoria_nombres y subcategoria_ids_str en lugar
    // de subcategoria_id y subcategoria_nombre.
    // ─────────────────────────────────────────────────────────────────────────
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
        p.setProveedorId(rs.getInt("proveedor_id"));

        p.setMaterialNombre(rs.getString("material_nombre"));
        p.setCategoriaNombre(rs.getString("categoria_nombre"));
        p.setProveedorNombre(rs.getString("proveedor_nombre"));

        // Nombres concatenados para mostrar en vistas: "Compromiso, Aniversario"
        p.setSubcategoriaNombre(rs.getString("subcategoria_nombres"));

        // IDs como lista: "2,6,12" → [2, 6, 12]  (para preseleccionar en formularios)
        String idsStr = rs.getString("subcategoria_ids_str");
        if (idsStr != null && !idsStr.isBlank()) {
            List<Integer> ids = new ArrayList<>();
            for (String part : idsStr.split(",")) {
                try { ids.add(Integer.parseInt(part.trim())); }
                catch (NumberFormatException ignored) {}
            }
            p.setSubcategoriaIds(ids);
        }

        return p;
    }

    private int obtenerStockActual(Connection con, int productoId) throws Exception {
        String sql = "SELECT stock FROM Producto WHERE producto_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("stock") : 0;
            }
        }
    }

    private String generarCodigo(Connection con, int categoriaId) throws SQLException {
        String prefijo = "PRD";
        String sqlPrefijo = "SELECT UPPER(LEFT(nombre, 3)) AS p FROM Categoria WHERE categoria_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlPrefijo)) {
            ps.setInt(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString("p") != null) prefijo = rs.getString("p");
            }
        }
        int siguiente = 1;
        String sqlUltimo = """
            SELECT codigo FROM Producto
            WHERE categoria_id = ? AND estado = 1
            ORDER BY producto_id DESC LIMIT 1
            """;
        try (PreparedStatement ps = con.prepareStatement(sqlUltimo)) {
            ps.setInt(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString("codigo") != null) {
                    String numStr = rs.getString("codigo").replaceAll("[^0-9]", "");
                    if (!numStr.isEmpty()) {
                        try { siguiente = Integer.parseInt(numStr) + 1; }
                        catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
        return prefijo + String.format("%02d", siguiente);
    }
}