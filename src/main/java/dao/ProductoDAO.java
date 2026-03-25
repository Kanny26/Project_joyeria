package dao;

import config.ConexionDB;
import model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la gestión de productos en el sistema de joyería.
 * 
 * Concentra todas las operaciones relacionadas con el catálogo de productos,
 * incluyendo gestión de precios, stock, imágenes, subcategorías y movimientos
 * de inventario.
 * 
 * Este DAO es fundamental para los módulos de:
 *   - Catálogo de productos (CRUD completo)
 *   - Punto de venta (consulta de productos disponibles)
 *   - Gestión de inventario (movimientos, stock, búsquedas)
 *   - Reportes y búsquedas avanzadas
 * 
 * Implementa transacciones atómicas para operaciones que involucran múltiples
 * tablas (Producto, Producto_Subcategoria, Inventario_Movimiento).
 */
public class ProductoDAO {

    // ─────────────────────────────────────────────────────────────────────────
    // SELECT BASE
    // Se usa GROUP_CONCAT para obtener:
    //   subcategoria_nombres → "Compromiso, Aniversario, Uso Diario"  (para mostrar)
    //   subcategoria_ids_str → "2,6,12"                               (para preseleccionar checkboxes)
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * SQL base para consultas completas de productos.
     * 
     * Esta consulta recupera datos principales del producto junto con información
     * relacionada de material, categoría y proveedor, además de agregar las
     * subcategorías asociadas mediante GROUP_CONCAT.
     * 
     * Tablas involucradas:
     *   - Producto (p): Tabla principal
     *   - Material (m): Datos del material del producto (oro, plata, etc.)
     *   - Categoria (c): Categoría principal del producto
     *   - Proveedor (prov): Proveedor que suministra el producto
     * 
     * Subconsultas:
     *   1. subcategoria_nombres: Concatena nombres de subcategorías separados por coma
     *   2. subcategoria_ids_str: Concatena IDs de subcategorías para uso en formularios
     * 
     * Uso de GROUP_CONCAT: Permite traer múltiples subcategorías en una sola fila,
     * evitando múltiples consultas o procesamiento adicional en Java.
     */
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
    /**
     * Lista todos los productos activos pertenecientes a una categoría específica.
     * 
     * Esta consulta es utilizada en interfaces de navegación donde se muestran
     * productos agrupados por categoría (Ej: Anillos, Collares, Pulseras).
     * 
     * @param categoriaId Identificador de la categoría a filtrar
     * @return Lista de objetos Producto activos de la categoría, ordenados por código
     * @throws Exception Si falla la conexión o ejecución de la consulta
     */
    public List<Producto> listarPorCategoria(int categoriaId) throws Exception {
        List<Producto> lista = new ArrayList<>();
        /*
         * Consulta que extiende SELECT_BASE añadiendo filtro por categoría y estado.
         * 
         * Condiciones:
         *   - p.categoria_id = ?: Solo productos de la categoría especificada
         *   - p.estado = 1: Solo productos activos
         * 
         * ORDER BY p.codigo ASC: Ordenación por código para facilitar la localización
         */
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
    /**
     * Lista productos disponibles para la venta en el punto de venta.
     * 
     * Optimizada para el módulo de ventas:
     *   - Excluye campos BLOB (imagen_data) para mejorar rendimiento
     *   - No incluye subcategorías (no necesarias para venta)
     *   - Filtra por stock > 0 y estado activo
     * 
     * @return Lista de productos con stock disponible para venta, ordenados por nombre
     * @throws Exception Si falla la conexión o ejecución de la consulta
     */
    public List<Producto> listarProductosDisponibles() throws Exception {
        List<Producto> lista = new ArrayList<>();
        /*
         * Consulta optimizada para el punto de venta.
         * 
         * Tablas involucradas:
         *   - Producto (p): Datos básicos del producto
         *   - Material (m): Nombre del material para mostrar
         *   - Categoria (c): Nombre de la categoría para mostrar
         * 
         * Condiciones:
         *   - p.stock > 0: Solo productos con inventario disponible
         *   - p.estado = 1: Solo productos activos
         * 
         * ORDER BY p.nombre: Orden alfabético para facilitar búsqueda en POS
         */
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
    /**
     * Obtiene un producto completo por su ID, incluyendo datos de joins y subcategorías.
     * 
     * @param id Identificador único del producto (producto_id)
     * @return Objeto Producto con todos sus datos, o null si no existe
     * @throws Exception Si falla la conexión o ejecución de la consulta
     */
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
    /**
     * Carga información básica de un producto para validación en ventas.
     * 
     * Optimizada para el punto de venta:
     *   - No incluye campo BLOB (imagen_data) para mejor rendimiento
     *   - Incluye stock y precios para validaciones de negocio
     * 
     * @param productoId Identificador del producto
     * @return Producto con datos de stock, precios e información básica, o null
     * @throws Exception Si falla la conexión o ejecución de la consulta
     */
    public Producto obtenerProductoConStock(int productoId) throws Exception {
        /*
         * Consulta que recupera datos esenciales para validación de venta.
         * 
         * Campos clave:
         *   - stock: Para verificar disponibilidad
         *   - precio_unitario: Precio de costo (referencia)
         *   - precio_venta: Precio final al cliente
         * 
         * JOIN con Material y Categoria para mostrar información contextual
         * en la interfaz de venta.
         */
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
    /**
     * Inserta un nuevo producto en el sistema con todas sus asociaciones.
     * 
     * Realiza en una sola transacción:
     *   1. Validaciones de negocio (precios, descripción, proveedor)
     *   2. Generación automática de código basado en categoría
     *   3. Inserción en tabla Producto
     *   4. Inserción de subcategorías en Producto_Subcategoria
     * 
     * @param p Objeto Producto con los datos a guardar (incluye lista de IDs de subcategorías)
     * @param usuarioId ID del usuario que realiza la operación (para trazabilidad futura)
     * @throws Exception Si fallan validaciones o la operación en base de datos
     */
    public void guardar(Producto p, int usuarioId) throws Exception {
        /*
         * Validaciones de negocio antes de la operación de base de datos.
         */
        if (p.getPrecioVenta().compareTo(p.getPrecioUnitario()) < 0)
            throw new Exception("El precio de venta no puede ser menor al precio de costo.");
        if (p.getDescripcion() != null && p.getDescripcion().length() > 500)
            throw new Exception("La descripción no puede superar los 500 caracteres.");
        if (p.getProveedorId() <= 0)
            throw new Exception("Debes seleccionar un proveedor.");

        p.setStock(0);  // Producto nuevo inicia con stock 0

        /*
         * Consulta de inserción en tabla Producto.
         * 
         * Tabla: Producto
         * Campos insertados:
         *   - codigo: Generado automáticamente según categoría
         *   - nombre, descripcion: Datos descriptivos
         *   - stock: 0 por defecto
         *   - precios: Unitario (costo) y Venta
         *   - fecha_registro: CURDATE() (fecha actual)
         *   - material_id, categoria_id, proveedor_id: Relaciones FK
         *   - imagen, imagen_data, imagen_tipo: Datos de imagen (opcionales)
         *   - estado: 1 (activo) por defecto
         */
        String sqlProducto = """
            INSERT INTO Producto
                (codigo, nombre, descripcion, stock, precio_unitario, precio_venta,
                 fecha_registro, material_id, categoria_id, proveedor_id,
                 imagen, imagen_data, imagen_tipo, estado)
            VALUES (?, ?, ?, 0, ?, ?, CURDATE(), ?, ?, ?, ?, ?, ?, 1)
            """;

        /*
         * Consulta para asociar subcategorías al producto.
         * Se ejecuta después de tener el producto_id generado.
         */
        String sqlSubcat = """
            INSERT INTO Producto_Subcategoria (producto_id, subcategoria_id)
            VALUES (?, ?)
            """;

        try (Connection con = ConexionDB.getConnection()) {
            // Desactivar autocommit para manejar transacción manual
            con.setAutoCommit(false);
            try {
                // 1. Generar código automático basado en categoría
                p.setCodigo(generarCodigo(con, p.getCategoriaId()));

                // 2. Insertar el producto y obtener su ID generado
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
                    
                    // Manejo de imagen: si hay datos BLOB, se guardan; si no, se pone NULL
                    if (p.getImagenData() != null && p.getImagenData().length > 0) {
                        ps.setBytes(10, p.getImagenData());
                        ps.setString(11, p.getImagenTipo());
                    } else {
                        ps.setNull(10, Types.BLOB);
                        ps.setNull(11, Types.VARCHAR);
                    }
                    
                    ps.executeUpdate();
                    
                    // Recuperar el ID autogenerado
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) p.setProductoId(keys.getInt(1));
                    }
                }

                // 3. Insertar las subcategorías asociadas
                if (p.getSubcategoriaIds() != null && !p.getSubcategoriaIds().isEmpty()) {
                    try (PreparedStatement ps = con.prepareStatement(sqlSubcat)) {
                        for (int subcatId : p.getSubcategoriaIds()) {
                            if (subcatId > 0) {
                                ps.setInt(1, p.getProductoId());
                                ps.setInt(2, subcatId);
                                ps.addBatch();  // Batch para múltiples inserciones
                            }
                        }
                        ps.executeBatch();  // Ejecutar todas las inserciones en lote
                    }
                }

                // Confirmar todas las operaciones
                con.commit();
            } catch (Exception e) {
                // Revertir toda la transacción en caso de error
                con.rollback();
                throw e;
            } finally {
                // Restaurar autocommit al valor original
                con.setAutoCommit(true);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ACTUALIZAR PRODUCTO
    // CAMBIO: borra las subcategorías anteriores y reinserta las nuevas,
    // todo dentro de la misma transacción.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Actualiza un producto existente y reemplaza sus subcategorías asociadas.
     * 
     * Realiza en una sola transacción:
     *   1. Validaciones de negocio
     *   2. Actualización de datos en tabla Producto
     *   3. Eliminación de subcategorías existentes
     *   4. Inserción de nuevas subcategorías
     * 
     * @param p Objeto Producto con ID y datos actualizados
     * @param usuarioId ID del usuario que realiza la operación (para trazabilidad)
     * @throws Exception Si fallan validaciones o la operación en base de datos
     */
    public void actualizar(Producto p, int usuarioId) throws Exception {
        /*
         * Validaciones de negocio antes de la operación.
         */
        if (p.getPrecioVenta().compareTo(p.getPrecioUnitario()) < 0)
            throw new Exception("El precio de venta no puede ser menor al precio de costo.");
        if (p.getDescripcion() != null && p.getDescripcion().length() > 500)
            throw new Exception("La descripción no puede superar los 500 caracteres.");
        if (p.getProveedorId() <= 0)
            throw new Exception("Debes seleccionar un proveedor.");

        boolean nuevaImg = p.getImagenData() != null && p.getImagenData().length > 0;

        /*
         * Consulta de actualización: dos versiones según si hay nueva imagen.
         * 
         * Si hay nueva imagen: Se actualizan todos los campos incluyendo imagen_data
         * Si no hay imagen nueva: Se actualizan solo campos básicos (imagen_data no cambia)
         */
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

        // Consultas para manejo de subcategorías
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

                // 2. Eliminar todas las subcategorías existentes del producto
                try (PreparedStatement ps = con.prepareStatement(sqlBorrarSubcat)) {
                    ps.setInt(1, p.getProductoId());
                    ps.executeUpdate();
                }

                // 3. Insertar las nuevas subcategorías
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
    /**
     * Actualiza el stock de un producto de forma manual (ajuste directo).
     * 
     * Nota: Este método solo actualiza el campo stock, no registra movimiento
     * en Inventario_Movimiento. Para ajustes con trazabilidad, usar
     * registrarMovimiento.
     * 
     * @param productoId Identificador del producto
     * @param nuevoStock Valor absoluto de stock a establecer
     * @throws Exception Si falla la actualización en base de datos
     */
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
    /**
     * Registra un movimiento de inventario con trazabilidad.
     * 
     * Este método se utiliza junto con actualizaciones de stock para mantener
     * un historial completo de todas las operaciones que afectan el inventario.
     * 
     * @param productoId Producto afectado por el movimiento
     * @param usuarioId Usuario que realiza la operación
     * @param tipo Tipo de movimiento (entrada, salida, ajuste)
     * @param cantidad Cantidad de unidades movidas
     * @param referencia Texto descriptivo de la operación (ej: "VENTA-123", "COMPRA-456")
     * @throws Exception Si falla la inserción en base de datos
     */
    public void registrarMovimiento(int productoId, int usuarioId,
                                    String tipo, int cantidad,
                                    String referencia) throws Exception {
        /*
         * Inserción en tabla de movimientos de inventario.
         * 
         * Tabla: Inventario_Movimiento
         * Campos:
         *   - producto_id: Producto afectado
         *   - usuario_id: Responsable de la operación
         *   - tipo: entrada/salida/ajuste
         *   - cantidad: Unidades movidas
         *   - fecha: NOW() (fecha y hora exacta)
         *   - referencia: Identificador de la transacción origen
         */
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

    /**
     * Calcula el total de unidades ingresadas por compras para un producto.
     * 
     * Utilizado para validaciones de inventario y reportes de compras.
     * 
     * @param productoId Identificador del producto
     * @return Suma de cantidades en detalle_compra, o 0 si no hay registros
     * @throws Exception Si falla la consulta
     */
    public int obtenerTotalEntradasPorCompras(int productoId) throws Exception {
        /*
         * Consulta que suma todas las cantidades compradas del producto.
         * 
         * Tabla: detalle_compra
         * COALESCE: Retorna 0 cuando SUM devuelve NULL (sin compras)
         */
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
    /**
     * Realiza eliminación lógica de un producto (cambia estado a inactivo).
     * 
     * Cuando se desactiva un producto con stock, se registra automáticamente
     * una salida de inventario para mantener consistencia.
     * 
     * @param id Producto a desactivar
     * @param usuarioId Usuario que realiza la operación
     * @throws Exception Si falla la transacción
     */
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
                
                // Desactivar producto
                try (PreparedStatement ps = con.prepareStatement(sqlEstado)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                
                // Si tenía stock, registrar salida por desactivación
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

    /**
     * Verifica si una categoría tiene al menos un producto activo asociado.
     * 
     * Utilizado antes de eliminar una categoría para prevenir eliminación
     * si tiene productos asociados (restricción de integridad).
     * 
     * @param categoriaId Identificador de la categoría
     * @return true si existen productos activos en la categoría
     * @throws Exception Si falla la consulta
     */
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
    /**
     * Busca productos en todo el catálogo según término y tipo de filtro.
     * 
     * Soporta búsquedas por:
     *   - nombre: Coincidencia en nombre o código
     *   - material: Búsqueda por nombre de material
     *   - stock: Búsqueda por valor numérico de stock
     *   - subcategoria: Búsqueda por nombre de subcategoría
     *   - todos: Búsqueda en múltiples campos
     * 
     * @param termino Texto a buscar (se usa LIKE con comodines)
     * @param filtro Tipo de filtro (nombre, material, stock, subcategoria, todos)
     * @return Lista de productos que coinciden con la búsqueda
     * @throws Exception Si falla la consulta
     */
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
    /**
     * Busca productos dentro de una categoría específica.
     * 
     * @param categoriaId Categoría donde realizar la búsqueda
     * @param termino Texto a buscar
     * @param filtro Tipo de filtro (nombre, material, stock, subcategoria, todos)
     * @return Lista de productos que coinciden en la categoría
     * @throws Exception Si falla la consulta
     */
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

    /**
     * Construye dinámicamente la consulta SQL de búsqueda.
     * 
     * Soporta diferentes filtros y la opción de filtrar por categoría.
     * La consulta utiliza EXISTS para búsqueda en subcategorías debido
     * a la relación muchos a muchos.
     * 
     * @param porCategoria Si true, añade filtro por categoria_id
     * @param filtro Tipo de filtro (nombre, material, stock, subcategoria, todos)
     * @return Consulta SQL preparada para PreparedStatement
     */
    private String buildSearchSql(boolean porCategoria, String filtro) {
        /*
         * Base de la consulta con SELECT_BASE (incluye subconsultas GROUP_CONCAT)
         * pero adaptada para búsquedas (excluye imagen_data por rendimiento)
         */
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
        
        // Añadir filtro por categoría si es necesario
        if (porCategoria) sb.append(" p.categoria_id = ? AND (");
        else              sb.append(" (");

        /*
         * Construcción dinámica de condiciones según el filtro seleccionado.
         * 
         * Para búsqueda por subcategoría: Se usa EXISTS para buscar en la
         * tabla Producto_Subcategoria, ya que es una relación muchos a muchos.
         */
        switch (filtro != null ? filtro : "todos") {
            case "nombre"   -> sb.append("p.nombre LIKE ? OR p.codigo LIKE ?");
            case "material" -> sb.append("m.nombre LIKE ?");
            case "stock"    -> sb.append("CAST(p.stock AS CHAR) LIKE ?");
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

    /**
     * Asigna parámetros al PreparedStatement según el filtro seleccionado.
     * 
     * @param ps PreparedStatement ya preparado
     * @param start Índice inicial para asignar parámetros
     * @param termino Texto de búsqueda (se convierte a LIKE '%termino%')
     * @param filtro Tipo de filtro (determina cuántos parámetros asignar)
     * @param categoriaId ID de categoría (si es >0, se asigna primero)
     * @throws SQLException Si falla la asignación de parámetros
     */
    private void bindParams(PreparedStatement ps, int start, String termino,
                            String filtro, int categoriaId) throws SQLException {
        int i = start;
        
        // Si hay filtro por categoría, asignarlo como primer parámetro
        if (categoriaId > 0) ps.setInt(i++, categoriaId);
        
        // Construir el patrón LIKE con comodines
        String like = "%" + termino + "%";
        
        /*
         * Asignar parámetros según el tipo de filtro.
         * Cada caso asigna la cantidad correcta de parámetros
         * que coincide con los placeholders generados en buildSearchSql.
         */
        switch (filtro != null ? filtro : "todos") {
            case "nombre"       -> { ps.setString(i++, like); ps.setString(i, like); }
            case "material"     -> ps.setString(i, like);
            case "stock"        -> ps.setString(i, like);
            case "subcategoria" -> ps.setString(i, like);
            default -> {
                // Filtro "todos": 6 placeholders
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
    /**
     * Convierte una fila del ResultSet en un objeto Producto completo.
     * 
     * Este método maneja:
     *   - Datos básicos del producto
     *   - Datos relacionados (material, categoría, proveedor)
     *   - Subcategorías: nombres concatenados para mostrar, IDs concatenados
     *     para formularios
     * 
     * @param rs ResultSet posicionado en la fila a procesar
     * @return Objeto Producto mapeado
     * @throws SQLException Si alguna columna no existe o hay error de lectura
     */
    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        
        // Datos principales
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

        // Fecha de registro (puede ser null)
        Date f = rs.getDate("fecha_registro");
        if (f != null) p.setFechaRegistro(f);

        // IDs de relaciones
        p.setMaterialId(rs.getInt("material_id"));
        p.setCategoriaId(rs.getInt("categoria_id"));
        p.setProveedorId(rs.getInt("proveedor_id"));

        // Nombres de relaciones para mostrar en UI
        p.setMaterialNombre(rs.getString("material_nombre"));
        p.setCategoriaNombre(rs.getString("categoria_nombre"));
        p.setProveedorNombre(rs.getString("proveedor_nombre"));

        // Subcategorías: nombres concatenados para mostrar
        p.setSubcategoriaNombre(rs.getString("subcategoria_nombres"));

        /*
         * Subcategorías: IDs concatenados como string "2,6,12"
         * Convertir a List<Integer> para uso en formularios (preselección)
         */
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

    /**
     * Obtiene el stock actual de un producto usando una conexión ya abierta.
     * 
     * Utilizado dentro de transacciones para evitar abrir conexiones adicionales.
     * 
     * @param con Conexión activa a la base de datos
     * @param productoId Identificador del producto
     * @return Cantidad de stock actual, o 0 si el producto no existe
     * @throws Exception Si falla la consulta
     */
    private int obtenerStockActual(Connection con, int productoId) throws Exception {
        String sql = "SELECT stock FROM Producto WHERE producto_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("stock") : 0;
            }
        }
    }

    /**
     * Genera un código único para un nuevo producto basado en su categoría.
     * 
     * Formato del código: [Prefijo de 3 letras de la categoría][Número correlativo 2 dígitos]
     * Ejemplo: "ANE01" para Anillos, "COL01" para Collares
     * 
     * Lógica:
     *   1. Obtener las primeras 3 letras del nombre de la categoría (en mayúsculas)
     *   2. Buscar el último código usado en esa categoría
     *   3. Extraer el número y sumar 1
     *   4. Formatear con 2 dígitos (01, 02, ..., 99)
     * 
     * @param con Conexión activa (para usar dentro de transacción)
     * @param categoriaId Identificador de la categoría
     * @return Código generado para el nuevo producto
     * @throws SQLException Si falla alguna consulta
     */
    private String generarCodigo(Connection con, int categoriaId) throws SQLException {
        // 1. Obtener prefijo: primeras 3 letras de la categoría
        String prefijo = "PRD";  // Valor por defecto
        String sqlPrefijo = "SELECT UPPER(LEFT(nombre, 3)) AS p FROM Categoria WHERE categoria_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlPrefijo)) {
            ps.setInt(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString("p") != null) prefijo = rs.getString("p");
            }
        }
        
        // 2. Buscar último número usado en esta categoría
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
                    // Extraer solo los números del código
                    String numStr = rs.getString("codigo").replaceAll("[^0-9]", "");
                    if (!numStr.isEmpty()) {
                        try { siguiente = Integer.parseInt(numStr) + 1; }
                        catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
        
        // 3. Formatear con 2 dígitos (01, 02, ..., 99)
        return prefijo + String.format("%02d", siguiente);
    }
}