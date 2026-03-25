package dao;

import config.ConexionDB;
import model.DetalleVenta;
import model.Venta;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object (DAO) para la gestión de ventas en el sistema de joyería.
 * 
 * Coordina todo el proceso de facturación, incluyendo:
 *   - Registro de cabecera de venta (Venta)
 *   - Líneas de detalle (Detalle_Venta)
 *   - Pagos asociados (Pago_Venta)
 *   - Impacto en inventario (Inventario_Movimiento)
 *   - Auditoría de operaciones (Auditoria_Log)
 * 
 * Este DAO es el más crítico del sistema porque garantiza la integridad
 * transaccional de las ventas, asegurando que cada operación de joyería
 * quede registrada de forma completa y trazable.
 * 
 * Transaccionalidad crítica: Las operaciones de venta involucran 5 tablas
 * diferentes y deben ejecutarse como una unidad atómica.
 */
public class VentaDAO {

    /**
     * Consulta base para listados de ventas: una fila por venta con información consolidada.
     * 
     * Esta consulta está diseñada para evitar duplicados debido a relaciones 1:N.
     * Utiliza subconsultas para obtener:
     *   - Total de la venta (suma de detalles)
     *   - Método de pago (primer registro de Pago_Venta)
     *   - Estado del pago (pendiente/confirmado)
     * 
     * Tablas involucradas:
     *   - Venta (v): Cabecera principal
     *   - Usuario (uv): Vendedor que realizó la venta
     *   - Cliente (cl): Cliente que compró
     *   - Telefono_Cliente (tc): Teléfono de contacto (primer registro)
     * 
     * Subconsultas:
     *   - total: Suma de precio_unitario * cantidad de Detalle_Venta
     *   - metodo_pago: Nombre del método de pago del primer pago registrado
     *   - estado: Estado del primer pago ('confirmado' o 'pendiente')
     * 
     * LEFT JOIN con Usuario y Cliente: Permite mostrar "Sin vendedor"/"Sin cliente"
     * cuando la venta no tiene esos datos (soporte para datos históricos).
     */
    private static final String SQL_BASE = """
        SELECT 
            v.venta_id, 
            v.usuario_id, 
            v.cliente_id,
            COALESCE(uv.nombre, 'Sin vendedor') AS vendedor,
            COALESCE(cl.nombre, 'Sin cliente') AS cliente,
            (SELECT tc.telefono FROM Telefono_Cliente tc 
             WHERE tc.cliente_id = v.cliente_id LIMIT 1) AS telefono_cliente,
            v.fecha_emision,
            COALESCE((
                SELECT SUM(dv.precio_unitario * dv.cantidad) 
                FROM Detalle_Venta dv 
                WHERE dv.venta_id = v.venta_id
            ), 0) AS total,
            COALESCE((
                SELECT mp.nombre FROM Pago_Venta pv
                LEFT JOIN Metodo_Pago mp ON mp.metodo_pago_id = pv.metodo_pago_id
                WHERE pv.venta_id = v.venta_id LIMIT 1
            ), 'No especificado') AS metodo_pago,
            COALESCE((
                SELECT pv.estado FROM Pago_Venta pv 
                WHERE pv.venta_id = v.venta_id LIMIT 1
            ), 'pendiente') AS estado
        FROM Venta v
        LEFT JOIN Usuario uv ON uv.usuario_id = v.usuario_id
        LEFT JOIN Cliente cl ON cl.cliente_id = v.cliente_id
        """;

    /**
     * Lista todas las ventas con sus detalles y cálculo de anticipo/saldo.
     * 
     * Esta es la consulta principal para el módulo de administración de ventas,
     * mostrando todas las transacciones ordenadas de más reciente a más antigua.
     * 
     * @return Lista de objetos Venta con todos sus detalles enriquecidos
     * @throws Exception Si falla la conexión o ejecución de consultas SQL
     */
    public List<Venta> listarVentas() throws Exception {
        List<Venta> lista = new ArrayList<>();
        // Sin GROUP BY: cada fila ya es una venta única gracias a las subconsultas
        String sql = SQL_BASE + " ORDER BY v.fecha_emision DESC, v.venta_id DESC";
        
        Connection con = null;
        try {
            con = ConexionDB.getConnection();
            
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    Venta v = mapearVenta(rs);
                    // Cargar líneas de detalle para esta venta
                    v.setDetalles(listarDetalles(v.getVentaId(), con));
                    // Calcular pagos: anticipo vs saldo pendiente
                    calcularAnticipoSaldo(v, con);
                    lista.add(v);
                }
                
                System.out.println("DAO - Ventas recuperadas: " + lista.size());
            }
        } catch (SQLException e) {
            System.err.println("ERROR SQL en listarVentas: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            // Asegurar cierre de conexión incluso si hay excepciones
            if (con != null && !con.isClosed()) con.close();
        }
        return lista;
    }

    /**
     * Obtiene una venta específica por su ID con todos sus detalles.
     * 
     * @param ventaId Identificador único de la venta
     * @return Venta completa con detalles y estado de pagos, o null si no existe
     * @throws Exception Si falla la consulta SQL
     */
    public Venta obtenerPorId(int ventaId) throws Exception {
        String sql = SQL_BASE + " WHERE v.venta_id = ? ORDER BY v.venta_id DESC";
        
        System.out.println("DAO - Buscando venta ID: " + ventaId);
        
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Venta v = mapearVenta(rs);
                    v.setDetalles(listarDetalles(ventaId, con));
                    calcularAnticipoSaldo(v, con);
                    return v;
                }
            }
        }
        return null;
    }

    /**
     * Lista las ventas realizadas por un vendedor específico.
     * 
     * Utilizado en el dashboard del vendedor para mostrar su historial
     * de ventas y gestión de cobros.
     * 
     * @param usuarioId ID del usuario vendedor
     * @return Lista de ventas del vendedor, ordenadas por fecha descendente
     * @throws Exception Si falla la consulta
     */
    public List<Venta> listarPorVendedor(int usuarioId) throws Exception {
        List<Venta> lista = new ArrayList<>();
        String sql = SQL_BASE + " WHERE v.usuario_id = ? ORDER BY v.fecha_emision DESC";
        
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Venta v = mapearVenta(rs);
                    v.setDetalles(listarDetalles(v.getVentaId(), con));
                    calcularAnticipoSaldo(v, con);
                    lista.add(v);
                }
            }
        }
        return lista;
    }

    /**
     * Cuenta las ventas que tienen pagos pendientes o ningún pago registrado.
     * 
     * Indicador de gestión para el dashboard de administración,
     * mostrando el volumen de cartera por cobrar.
     * 
     * @return Número de ventas pendientes de cobro
     * @throws Exception Si falla la consulta
     */
    public int contarPendientes() throws Exception {
        /*
         * Consulta que cuenta ventas pendientes bajo dos criterios:
         *   1. Existe al menos un pago en estado 'pendiente'
         *   2. No existe ningún pago registrado (venta sin abonos)
         * 
         * COUNT(DISTINCT v.venta_id): Evita duplicar ventas que tienen
         * múltiples pagos pendientes.
         */
        String sql = """
            SELECT COUNT(DISTINCT v.venta_id)
            FROM Venta v
            WHERE EXISTS (
                SELECT 1 FROM Pago_Venta pv 
                WHERE pv.venta_id = v.venta_id AND pv.estado = 'pendiente'
            ) OR NOT EXISTS (
                SELECT 1 FROM Pago_Venta pv WHERE pv.venta_id = v.venta_id
            )
            """;
        
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Búsqueda avanzada de ventas con múltiples criterios de filtro.
     * 
     * Soporta búsqueda por:
     *   - ID de venta (búsqueda exacta)
     *   - Nombre de cliente (búsqueda parcial con LIKE)
     *   - Nombre de vendedor (búsqueda parcial con LIKE)
     *   - Estado de pago (pendiente/confirmado)
     *   - Rango de fechas
     *   - Vendedor específico
     * 
     * @param criterio Texto a buscar según tipoBusqueda
     * @param tipoBusqueda Tipo de búsqueda (id, cliente, vendedor, estado)
     * @param fechaInicio Fecha inicial del rango (opcional)
     * @param fechaFin Fecha final del rango (opcional)
     * @param vendedorId ID del vendedor a filtrar (opcional, >0 aplica)
     * @return Lista de ventas que cumplen todos los criterios
     * @throws Exception Si falla la consulta o el parseo de ID
     */
    public List<Venta> buscarVentas(String criterio, String tipoBusqueda, Date fechaInicio, Date fechaFin, int vendedorId) throws Exception {
        List<Venta> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SQL_BASE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        /*
         * Construcción dinámica de condiciones según filtros proporcionados.
         * Se usa StringBuilder para evitar concatenaciones ineficientes.
         */
        
        // Filtro por vendedor específico
        if (vendedorId > 0) {
            sql.append(" AND v.usuario_id = ?");
            params.add(vendedorId);
        }
        
        // Filtro por criterio de búsqueda
        if (criterio != null && !criterio.isBlank() && tipoBusqueda != null) {
            switch (tipoBusqueda) {
                case "id" -> {
                    // Búsqueda exacta por ID de venta
                    sql.append(" AND v.venta_id = ?");
                    params.add(Integer.parseInt(criterio));
                }
                case "cliente" -> {
                    // Búsqueda parcial por nombre de cliente (LIKE con %)
                    sql.append(" AND cl.nombre LIKE ?");
                    params.add("%" + criterio + "%");
                }
                case "vendedor" -> {
                    // Búsqueda parcial por nombre de vendedor
                    sql.append(" AND uv.nombre LIKE ?");
                    params.add("%" + criterio + "%");
                }
                case "estado" -> {
                    // Búsqueda por estado de pago usando subconsulta
                    sql.append(" AND COALESCE((SELECT pv.estado FROM Pago_Venta pv WHERE pv.venta_id = v.venta_id LIMIT 1), 'pendiente') = ?");
                    params.add(criterio);
                }
            }
        }
        
        // Filtro por rango de fechas
        if (fechaInicio != null) {
            sql.append(" AND v.fecha_emision >= ?");
            params.add(new java.sql.Date(fechaInicio.getTime()));
        }
        if (fechaFin != null) {
            sql.append(" AND v.fecha_emision <= ?");
            params.add(new java.sql.Date(fechaFin.getTime()));
        }
        
        sql.append(" ORDER BY v.fecha_emision DESC");

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            
            // Asignar todos los parámetros acumulados
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Venta v = mapearVenta(rs);
                    v.setDetalles(listarDetalles(v.getVentaId(), con));
                    calcularAnticipoSaldo(v, con);
                    lista.add(v);
                }
            }
        }
        return lista;
    }

    /**
     * Mapea una fila del ResultSet (SQL_BASE) a un objeto Venta.
     * 
     * Este método solo mapea datos de cabecera; los detalles se cargan
     * por separado para evitar consultas complejas con múltiples joins.
     * 
     * @param rs ResultSet posicionado en la fila a procesar
     * @return Objeto Venta con datos de cabecera
     * @throws SQLException Si alguna columna no existe
     */
    private Venta mapearVenta(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setVentaId(rs.getInt("venta_id"));
        v.setUsuarioId(rs.getInt("usuario_id"));
        v.setUsuarioClienteId(rs.getInt("cliente_id"));
        v.setVendedorNombre(rs.getString("vendedor"));
        v.setClienteNombre(rs.getString("cliente"));
        v.setTelefonoCliente(rs.getString("telefono_cliente"));
        v.setFechaEmision(rs.getDate("fecha_emision"));
        v.setTotal(rs.getBigDecimal("total"));
        v.setMetodoPago(rs.getString("metodo_pago"));
        v.setEstado(rs.getString("estado"));
        return v;
    }

    /**
     * Carga las líneas de detalle (productos) de una venta específica.
     * 
     * Esta consulta recupera cada producto vendido con su cantidad,
     * precio unitario, subtotal y stock actual para validaciones.
     * 
     * @param ventaId ID de la venta cuyos detalles se desean
     * @param con Conexión activa (reutilizada para eficiencia)
     * @return Lista de objetos DetalleVenta con información completa
     * @throws SQLException Si falla la consulta
     */
    private List<DetalleVenta> listarDetalles(int ventaId, Connection con) throws SQLException {
        List<DetalleVenta> lista = new ArrayList<>();
        /*
         * Consulta que obtiene detalles de venta con información de producto.
         * 
         * Tablas:
         *   - Detalle_Venta (dv): Líneas de la venta
         *   - Producto (p): Información del producto (nombre, stock)
         * 
         * Cálculos:
         *   - subtotal = cantidad * precio_unitario (derivado en SQL)
         *   - stock: Stock actual del producto (para validación)
         */
        String sql = """
            SELECT dv.detalle_venta_id, dv.venta_id, dv.producto_id, p.nombre AS producto_nombre,
                   p.stock, dv.cantidad, dv.precio_unitario, dv.cantidad * dv.precio_unitario AS subtotal
            FROM Detalle_Venta dv
            LEFT JOIN Producto p ON p.producto_id = dv.producto_id
            WHERE dv.venta_id = ?
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleVenta d = new DetalleVenta();
                    d.setDetalleVentaId(rs.getInt("detalle_venta_id"));
                    d.setVentaId(rs.getInt("venta_id"));
                    d.setProductoId(rs.getInt("producto_id"));
                    d.setProductoNombre(rs.getString("producto_nombre"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    d.setSubtotal(rs.getBigDecimal("subtotal"));
                    d.setStockDisponible(rs.getInt("stock"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    /**
     * Calcula el monto pagado y pendiente de una venta, determinando su modalidad.
     * 
     * Modalidades:
     *   - contado: Todos los pagos confirmados, sin saldo pendiente
     *   - anticipo: Al menos un pago pendiente, se guarda monto de anticipo y saldo
     * 
     * @param venta Venta a enriquecer con datos de pagos
     * @param con Conexión activa
     * @throws SQLException Si falla la consulta de pagos
     */
    private void calcularAnticipoSaldo(Venta venta, Connection con) throws SQLException {
        /*
         * Consulta que resume pagos de la venta.
         * 
         * SUM con CASE: Calcula total confirmado y total pendiente
         * en una sola pasada sobre la tabla Pago_Venta.
         */
        String sql = """
            SELECT 
                SUM(CASE WHEN estado = 'confirmado' THEN monto ELSE 0 END) AS pagado,
                SUM(CASE WHEN estado = 'pendiente' THEN monto ELSE 0 END) AS pendiente
            FROM Pago_Venta WHERE venta_id = ?
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, venta.getVentaId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal pendiente = rs.getBigDecimal("pendiente");
                    BigDecimal pagado    = rs.getBigDecimal("pagado");
                    if (pendiente != null && pendiente.compareTo(BigDecimal.ZERO) > 0) {
                        venta.setModalidad("anticipo");
                        venta.setMontoAnticipo(pagado);
                        venta.setSaldoPendiente(pendiente);
                    } else {
                        venta.setModalidad("contado");
                    }
                }
            }
        }
    }

    // ========== OPERACIONES DE INSERCIÓN Y MODIFICACIÓN ==========
    // Todas estas operaciones son transaccionales para garantizar integridad.

    /**
     * Registra una venta completa con todos sus componentes.
     * 
     * Esta operación es la más crítica del sistema y ejecuta en una transacción:
     *   1. Validación de stock disponible para todos los productos
     *   2. Inserción en Venta (cabecera)
     *   3. Inserción masiva en Detalle_Venta (líneas)
     *   4. Inserción en Pago_Venta (uno o dos registros según modalidad)
     *   5. Actualización de stock en Producto
     *   6. Registro en Inventario_Movimiento (salidas)
     *   7. Registro en Auditoria_Log
     * 
     * @param venta Cabecera de venta (cliente, vendedor, fecha)
     * @param detalles Lista de productos vendidos con cantidades
     * @param modalidad "contado" o "anticipo"
     * @param montoAnticipo Monto pagado al momento (si es anticipo)
     * @param saldoPendiente Saldo restante por pagar (si es anticipo)
     * @param usuarioIdAuditoria ID del usuario para registro de auditoría
     * @return ID generado de la venta (venta_id)
     * @throws Exception Si falla stock insuficiente o cualquier operación SQL
     */
    public int insertar(Venta venta, List<DetalleVenta> detalles, String modalidad,
                        BigDecimal montoAnticipo, BigDecimal saldoPendiente,
                        int usuarioIdAuditoria) throws Exception {
        // SQL Statements preparados para cada operación
        final String sqlVenta      = "INSERT INTO Venta(usuario_id, cliente_id, fecha_emision) VALUES(?,?,?)";
        final String sqlDetalle    = "INSERT INTO Detalle_Venta(venta_id, producto_id, cantidad, precio_unitario) VALUES(?,?,?,?)";
        final String sqlGetMetodo  = "SELECT metodo_pago_id FROM Metodo_Pago WHERE nombre = ?";
        final String sqlPagoVenta  = "INSERT INTO Pago_Venta(venta_id, metodo_pago_id, monto, fecha, estado) VALUES(?,?,?,?,?)";
        final String sqlInventario = "INSERT INTO Inventario_Movimiento(producto_id, usuario_id, tipo, cantidad, fecha, referencia) VALUES(?,?,?,?,?,?)";

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            int ventaId = -1;
            try {
                // 1. Validación de stock: Verificar disponibilidad de cada producto
                for (DetalleVenta d : detalles) {
                    if (!validarStock(con, d.getProductoId(), d.getCantidad())) {
                        throw new SQLException("Stock insuficiente para: " + d.getProductoNombre());
                    }
                }

                // 2. Insertar cabecera de venta y obtener ID generado
                try (PreparedStatement ps = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, venta.getUsuarioId());
                    ps.setInt(2, venta.getUsuarioClienteId());
                    ps.setDate(3, new java.sql.Date(venta.getFechaEmision().getTime()));
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) ventaId = rs.getInt(1);
                    }
                }
                if (ventaId == -1) throw new SQLException("No se generó el ID de venta");

                // 3. Insertar líneas de detalle (batch para eficiencia)
                try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                    for (DetalleVenta d : detalles) {
                        ps.setInt(1, ventaId);
                        ps.setInt(2, d.getProductoId());
                        ps.setInt(3, d.getCantidad());
                        ps.setBigDecimal(4, d.getPrecioUnitario());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // 4. Determinar método de pago (puede venir como ID o nombre)
                /*
                 * CORRECCIÓN BUG 1: El formulario envía el metodo_pago_id (número),
                 * no el nombre. Se parsea directamente en lugar de buscarlo por nombre.
                 * Esto resuelve un problema donde se intentaba buscar un número
                 * como si fuera un nombre de método de pago.
                 */
                int metodoPagoId;
                try {
                    metodoPagoId = Integer.parseInt(venta.getMetodoPago());
                } catch (NumberFormatException e) {
                    // Fallback: si por alguna razón llega el nombre, buscarlo en BD
                    try (PreparedStatement ps = con.prepareStatement(sqlGetMetodo)) {
                        ps.setString(1, venta.getMetodoPago());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Método de pago no encontrado: " + venta.getMetodoPago());
                            metodoPagoId = rs.getInt("metodo_pago_id");
                        }
                    }
                }

                // 5. Insertar registros de pago (según modalidad)
                try (PreparedStatement ps = con.prepareStatement(sqlPagoVenta)) {
                    if ("anticipo".equals(modalidad) && montoAnticipo != null) {
                        // Anticipo: un pago confirmado + posible saldo pendiente
                        ps.setInt(1, ventaId);
                        ps.setInt(2, metodoPagoId);
                        ps.setBigDecimal(3, montoAnticipo);
                        ps.setTimestamp(4, new Timestamp(venta.getFechaEmision().getTime()));
                        ps.setString(5, "confirmado");
                        ps.executeUpdate();
                        
                        if (saldoPendiente != null && saldoPendiente.compareTo(BigDecimal.ZERO) > 0) {
                            ps.setInt(1, ventaId);
                            ps.setInt(2, metodoPagoId);
                            ps.setBigDecimal(3, saldoPendiente);
                            ps.setTimestamp(4, new Timestamp(venta.getFechaEmision().getTime()));
                            ps.setString(5, "pendiente");
                            ps.executeUpdate();
                        }
                    } else {
                        // Contado: un solo pago confirmado por el total
                        ps.setInt(1, ventaId);
                        ps.setInt(2, metodoPagoId);
                        ps.setBigDecimal(3, venta.getTotal());
                        ps.setTimestamp(4, new Timestamp(venta.getFechaEmision().getTime()));
                        ps.setString(5, "confirmado");
                        ps.executeUpdate();
                    }
                }

                // 6. Registrar movimientos de inventario y actualizar stock
                try (PreparedStatement ps = con.prepareStatement(sqlInventario)) {
                    for (DetalleVenta d : detalles) {
                        ps.setInt(1, d.getProductoId());
                        ps.setInt(2, venta.getUsuarioId());
                        ps.setString(3, "salida");
                        ps.setInt(4, d.getCantidad());
                        ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                        ps.setString(6, "VENTA-" + ventaId);
                        ps.addBatch();
                        actualizarStock(con, d.getProductoId(), -d.getCantidad());
                    }
                    ps.executeBatch();
                }

                // 7. Registrar auditoría de la operación
                registrarAuditoria(con, usuarioIdAuditoria, "CREAR", "Venta", ventaId, null,
                        "{\"descripcion\": \"Venta #" + ventaId + " Total: " + venta.getTotal() + "\"}");
                
                con.commit();
                return ventaId;

            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Registra la devolución de stock por una venta anulada o devolución de producto.
     * 
     * Esta operación aumenta el stock del producto y registra un movimiento
     * de entrada en Inventario_Movimiento.
     * 
     * @param ventaId ID de la venta origen (para referencia)
     * @param productoId Producto cuyo stock se incrementa
     * @param cantidad Unidades a devolver al inventario
     * @param usuarioIdAuditoria Usuario que realiza la operación
     * @return true si la transacción fue exitosa
     * @throws Exception Si falla la transacción
     */
    public boolean retornarStockDevolucion(int ventaId, int productoId, int cantidad, int usuarioIdAuditoria) throws Exception {
        String sqlInventario = "INSERT INTO Inventario_Movimiento(producto_id, usuario_id, tipo, cantidad, fecha, referencia) VALUES(?,?,?,?,?,?)";
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Incrementar stock del producto
                actualizarStock(con, productoId, cantidad);
                
                // Registrar movimiento de entrada
                try (PreparedStatement ps = con.prepareStatement(sqlInventario)) {
                    ps.setInt(1, productoId);
                    ps.setInt(2, usuarioIdAuditoria);
                    ps.setString(3, "entrada");
                    ps.setInt(4, cantidad);
                    ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                    ps.setString(6, "DEVOLUCION-VENTA-" + ventaId);
                    ps.executeUpdate();
                }
                
                registrarAuditoria(con, usuarioIdAuditoria, "DEVOLUCION", "Venta", ventaId, null,
                        "{\"descripcion\": \"Stock retornado Prod#" + productoId + " Cant: " + cantidad + "\"}");
                con.commit();
                return true;
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Confirma un pago pendiente de una venta.
     * 
     * Actualiza un registro de Pago_Venta de estado 'pendiente' a 'confirmado'
     * y establece el monto del abono.
     * 
     * @param ventaId ID de la venta a abonar
     * @param montoAbono Monto que se está pagando
     * @return true si se actualizó al menos un registro
     * @throws Exception Si falla la transacción
     */
    public boolean abonarSaldo(int ventaId, BigDecimal montoAbono) throws Exception {
        final String sqlAbono = "UPDATE Pago_Venta SET monto = ?, estado = 'confirmado' WHERE venta_id = ? AND estado = 'pendiente' LIMIT 1";
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sqlAbono)) {
                ps.setBigDecimal(1, montoAbono);
                ps.setInt(2, ventaId);
                ps.executeUpdate();
                con.commit();
                return true;
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Valida si hay stock suficiente de un producto.
     * 
     * @param con Conexión activa
     * @param productoId Producto a verificar
     * @param cantidad Cantidad requerida
     * @return true si stock >= cantidad solicitada
     * @throws SQLException Si falla la consulta
     */
    private boolean validarStock(Connection con, int productoId, int cantidad) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT stock FROM Producto WHERE producto_id = ?")) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("stock") >= cantidad;
            }
        }
    }

    /**
     * Actualiza el stock de un producto sumando un delta.
     * 
     * @param con Conexión activa
     * @param productoId Producto a actualizar
     * @param delta Cantidad a sumar (positiva o negativa)
     * @throws SQLException Si falla el update
     */
    private void actualizarStock(Connection con, int productoId, int delta) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE Producto SET stock = stock + ? WHERE producto_id = ?")) {
            ps.setInt(1, delta);
            ps.setInt(2, productoId);
            ps.executeUpdate();
        }
    }

    /**
     * Registra una entrada en la tabla de auditoría.
     * 
     * Maneja campos JSON: Si el texto no es JSON válido, lo envuelve
     * en un objeto {"valor": "texto"} para mantener la estructura.
     * 
     * @param conn Conexión activa
     * @param usuarioId Usuario que realiza la acción
     * @param accion Tipo de acción (CREAR, ACTUALIZAR, ELIMINAR, DEVOLUCION)
     * @param entidad Nombre de la entidad afectada (Venta, Producto, etc.)
     * @param entidadId ID del registro afectado
     * @param datosAnteriores JSON con estado anterior (para updates)
     * @param datosNuevos JSON con estado nuevo
     * @throws SQLException Si falla el insert
     */
    private void registrarAuditoria(Connection conn, int usuarioId, String accion, String entidad,
                                    int entidadId, String datosAnteriores, String datosNuevos) throws SQLException {
        String sql = """
            INSERT INTO Auditoria_Log(usuario_id, accion, entidad, entidad_id, datos_anteriores, datos_nuevos, fecha_hora)
            VALUES(?, ?, ?, ?, ?, ?, NOW())
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, accion);
            stmt.setString(3, entidad);
            stmt.setInt(4, entidadId);

            // Las columnas son JSON: validar antes de insertar
            if (datosAnteriores != null && !datosAnteriores.trim().isEmpty()) {
                String ja = esJsonValido(datosAnteriores)
                        ? datosAnteriores
                        : "{\"valor\": " + org.json.JSONObject.quote(datosAnteriores) + "}";
                stmt.setString(5, ja);
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }

            if (datosNuevos != null && !datosNuevos.trim().isEmpty()) {
                String jn = esJsonValido(datosNuevos)
                        ? datosNuevos
                        : "{\"valor\": " + org.json.JSONObject.quote(datosNuevos) + "}";
                stmt.setString(6, jn);
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }

            stmt.executeUpdate();
        }
    }

    /**
     * Verifica si un String es JSON válido (objeto o array).
     * 
     * @param texto Cadena a validar
     * @return true si parsea como JSONObject o JSONArray
     */
    private boolean esJsonValido(String texto) {
        if (texto == null) return false;
        String t = texto.trim();
        try {
            if (t.startsWith("{")) { new org.json.JSONObject(t); return true; }
            if (t.startsWith("[")) { new org.json.JSONArray(t);  return true; }
        } catch (Exception ignored) {}
        return false;
    }
}