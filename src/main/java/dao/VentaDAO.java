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
 * Maneja todas las operaciones de base de datos relacionadas con ventas:
 * insertar, listar, buscar, calcular pagos y manejar devoluciones de stock.
 */
public class VentaDAO {

    /*
     * Consulta base que se reutiliza en varios métodos.
     * Usa subconsultas en lugar de JOINs directos para evitar que aparezcan
     * registros duplicados cuando una venta tiene múltiples detalles o pagos.
     * Cada subconsulta trae solo el primer registro relevante (LIMIT 1).
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

    /** Retorna todas las ventas del sistema, ordenadas de la más reciente a la más antigua. */
    public List<Venta> listarVentas() throws Exception {
        List<Venta> lista = new ArrayList<>();
        String sql = SQL_BASE + " ORDER BY v.fecha_emision DESC, v.venta_id DESC";

        Connection con = null;
        try {
            con = ConexionDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Venta v = mapearVenta(rs);
                    // Para cada venta, se cargan sus líneas de producto usando la misma conexión
                    v.setDetalles(listarDetalles(v.getVentaId(), con));
                    // Se determina si la venta fue de contado o anticipo consultando Pago_Venta
                    calcularAnticipoSaldo(v, con);
                    lista.add(v);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error SQL en listarVentas: " + e.getMessage());
            throw e;
        } finally {
            // La conexión se cierra en el finally para garantizar que siempre se cierre,
            // incluso si ocurre un error dentro del try.
            if (con != null && !con.isClosed()) con.close();
        }
        return lista;
    }

    /** Busca una venta por su ID. Retorna null si no existe. */
    public Venta obtenerPorId(int ventaId) throws Exception {
        String sql = SQL_BASE + " WHERE v.venta_id = ? ORDER BY v.venta_id DESC";
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

    /** Retorna todas las ventas registradas por un vendedor específico. */
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
     * Cuenta cuántas ventas tienen pagos en estado "pendiente" o no tienen ningún pago.
     * Se usa para mostrar el total de ventas pendientes en el dashboard del administrador.
     */
    public int contarPendientes() throws Exception {
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
     * Busca ventas filtrando por criterio de texto, tipo de búsqueda y/o rango de fechas.
     * Se construye la consulta dinámicamente según los parámetros que lleguen con valor.
     *
     * isBlank() retorna true si el String es null, vacío o contiene solo espacios.
     */
    public List<Venta> buscarVentas(String criterio, String tipoBusqueda, Date fechaInicio, Date fechaFin, int vendedorId) throws Exception {
        List<Venta> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SQL_BASE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // Si se pasa un vendedorId mayor a 0, se filtra solo sus ventas
        if (vendedorId > 0) {
            sql.append(" AND v.usuario_id = ?");
            params.add(vendedorId);
        }

        // Solo se aplica el filtro de texto si hay criterio Y tipo de búsqueda definidos
        if (criterio != null && !criterio.isBlank() && tipoBusqueda != null) {
            switch (tipoBusqueda) {
                case "id" -> {
                    sql.append(" AND v.venta_id = ?");
                    params.add(Integer.parseInt(criterio));
                }
                case "cliente" -> {
                    // LIKE con % permite buscar texto parcial en el nombre del cliente
                    sql.append(" AND cl.nombre LIKE ?");
                    params.add("%" + criterio + "%");
                }
                case "vendedor" -> {
                    sql.append(" AND uv.nombre LIKE ?");
                    params.add("%" + criterio + "%");
                }
                case "estado" -> {
                    sql.append(" AND COALESCE((SELECT pv.estado FROM Pago_Venta pv WHERE pv.venta_id = v.venta_id LIMIT 1), 'pendiente') = ?");
                    params.add(criterio);
                }
            }
        }

        // Filtros de rango de fechas: se aplican solo si se proporcionaron fechas válidas
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
            // Se asignan los parámetros en el orden en que fueron añadidos a la lista
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

    /** Convierte una fila del ResultSet en un objeto Venta. */
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
     * Obtiene los productos (detalles) de una venta específica.
     * Recibe la conexión como parámetro para reutilizarla y evitar abrir una nueva.
     */
    private List<DetalleVenta> listarDetalles(int ventaId, Connection con) throws SQLException {
        List<DetalleVenta> lista = new ArrayList<>();
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
     * Consulta los registros de Pago_Venta para determinar si la venta fue de contado
     * o anticipo, y cuánto saldo queda pendiente.
     * Si hay un pago pendiente mayor a 0, la venta es de tipo "anticipo".
     */
    private void calcularAnticipoSaldo(Venta venta, Connection con) throws SQLException {
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

    /**
     * Guarda una venta completa en la base de datos, incluyendo sus detalles,
     * el registro de pago y el movimiento de inventario.
     * Usa una transacción para que todo se guarde junto o nada se guarde si falla algo.
     */
    public int insertar(Venta venta, List<DetalleVenta> detalles, String modalidad,
                        BigDecimal montoAnticipo, BigDecimal saldoPendiente,
                        int usuarioIdAuditoria) throws Exception {
        final String sqlVenta      = "INSERT INTO Venta(usuario_id, cliente_id, fecha_emision) VALUES(?,?,?)";
        final String sqlDetalle    = "INSERT INTO Detalle_Venta(venta_id, producto_id, cantidad, precio_unitario) VALUES(?,?,?,?)";
        final String sqlGetMetodo  = "SELECT metodo_pago_id FROM Metodo_Pago WHERE nombre = ?";
        final String sqlPagoVenta  = "INSERT INTO Pago_Venta(venta_id, metodo_pago_id, monto, fecha, estado) VALUES(?,?,?,?,?)";
        final String sqlInventario = "INSERT INTO Inventario_Movimiento(producto_id, usuario_id, tipo, cantidad, fecha, referencia) VALUES(?,?,?,?,?,?)";

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            int ventaId = -1;
            try {
                // Validación previa: verifica stock de cada producto antes de registrar nada
                for (DetalleVenta d : detalles) {
                    if (!validarStock(con, d.getProductoId(), d.getCantidad())) {
                        throw new SQLException("Stock insuficiente para: " + d.getProductoNombre());
                    }
                }

                // Insertar el registro principal de la venta y obtener el ID generado
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

                // Insertar todos los detalles en lote (batch) para mayor eficiencia
                try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                    for (DetalleVenta d : detalles) {
                        ps.setInt(1, ventaId);
                        ps.setInt(2, d.getProductoId());
                        ps.setInt(3, d.getCantidad());
                        ps.setBigDecimal(4, d.getPrecioUnitario());
                        ps.addBatch(); // Acumula los inserts
                    }
                    ps.executeBatch(); // Ejecuta todos de una vez
                }

                /*
                 * El formulario envía el ID del método de pago (un número).
                 * Se intenta convertir directamente. Si por alguna razón llega
                 * el nombre en texto, se busca en la base de datos como respaldo.
                 */
                int metodoPagoId;
                try {
                    metodoPagoId = Integer.parseInt(venta.getMetodoPago());
                } catch (NumberFormatException e) {
                    try (PreparedStatement ps = con.prepareStatement(sqlGetMetodo)) {
                        ps.setString(1, venta.getMetodoPago());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Método de pago no encontrado: " + venta.getMetodoPago());
                            metodoPagoId = rs.getInt("metodo_pago_id");
                        }
                    }
                }

                // Registrar el pago según la modalidad
                try (PreparedStatement ps = con.prepareStatement(sqlPagoVenta)) {
                    if ("anticipo".equals(modalidad) && montoAnticipo != null) {
                        // Para anticipo: dos registros — el anticipo (confirmado) y el saldo (pendiente)
                        ps.setInt(1, ventaId); ps.setInt(2, metodoPagoId);
                        ps.setBigDecimal(3, montoAnticipo);
                        ps.setTimestamp(4, new Timestamp(venta.getFechaEmision().getTime()));
                        ps.setString(5, "confirmado");
                        ps.executeUpdate();
                        if (saldoPendiente != null && saldoPendiente.compareTo(BigDecimal.ZERO) > 0) {
                            ps.setInt(1, ventaId); ps.setInt(2, metodoPagoId);
                            ps.setBigDecimal(3, saldoPendiente);
                            ps.setTimestamp(4, new Timestamp(venta.getFechaEmision().getTime()));
                            ps.setString(5, "pendiente");
                            ps.executeUpdate();
                        }
                    } else {
                        // Para contado: un solo registro marcado como confirmado
                        ps.setInt(1, ventaId); ps.setInt(2, metodoPagoId);
                        ps.setBigDecimal(3, venta.getTotal());
                        ps.setTimestamp(4, new Timestamp(venta.getFechaEmision().getTime()));
                        ps.setString(5, "confirmado");
                        ps.executeUpdate();
                    }
                }

                // Registrar el movimiento de inventario y descontar stock para cada producto
                try (PreparedStatement ps = con.prepareStatement(sqlInventario)) {
                    for (DetalleVenta d : detalles) {
                        ps.setInt(1, d.getProductoId());
                        ps.setInt(2, venta.getUsuarioId());
                        ps.setString(3, "salida"); // tipo "salida" porque los productos salen del inventario
                        ps.setInt(4, d.getCantidad());
                        ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                        ps.setString(6, "VENTA-" + ventaId); // referencia para trazabilidad
                        ps.addBatch();
                        actualizarStock(con, d.getProductoId(), -d.getCantidad()); // descuenta el stock
                    }
                    ps.executeBatch();
                }

                registrarAuditoria(con, usuarioIdAuditoria, "CREAR", "Venta", ventaId, null,
                        "{\"descripcion\": \"Venta #" + ventaId + " Total: " + venta.getTotal() + "\"}");

                con.commit();
                return ventaId;

            } catch (Exception e) {
                con.rollback(); // Si algo falla, revierte todos los cambios de esta transacción
                throw e;
            }
        }
    }

    /**
     * Devuelve el stock de un producto al inventario cuando se aprueba una devolución.
     * También registra el movimiento en Inventario_Movimiento para auditoría.
     */
    public boolean retornarStockDevolucion(int ventaId, int productoId, int cantidad, int usuarioIdAuditoria) throws Exception {
        String sqlInventario = "INSERT INTO Inventario_Movimiento(producto_id, usuario_id, tipo, cantidad, fecha, referencia) VALUES(?,?,?,?,?,?)";
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                actualizarStock(con, productoId, cantidad); // suma el stock de vuelta
                try (PreparedStatement ps = con.prepareStatement(sqlInventario)) {
                    ps.setInt(1, productoId);
                    ps.setInt(2, usuarioIdAuditoria);
                    ps.setString(3, "entrada"); // tipo "entrada" porque el producto vuelve al inventario
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
     * Aplica un abono al saldo pendiente de una venta a crédito.
     * Marca el registro de Pago_Venta pendiente como confirmado con el nuevo monto.
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

    /** Verifica si un producto tiene suficiente stock para la cantidad solicitada. */
    private boolean validarStock(Connection con, int productoId, int cantidad) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT stock FROM Producto WHERE producto_id = ?")) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("stock") >= cantidad;
            }
        }
    }

    /**
     * Suma o resta unidades del stock de un producto.
     * Se usa delta positivo para entradas (devoluciones) y negativo para salidas (ventas).
     */
    private void actualizarStock(Connection con, int productoId, int delta) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE Producto SET stock = stock + ? WHERE producto_id = ?")) {
            ps.setInt(1, delta);
            ps.setInt(2, productoId);
            ps.executeUpdate();
        }
    }

    /** Registra una entrada en la tabla de auditoría con los datos del cambio realizado. */
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
            stmt.setString(5, datosAnteriores);
            stmt.setString(6, datosNuevos);
            stmt.executeUpdate();
        }
    }
}
