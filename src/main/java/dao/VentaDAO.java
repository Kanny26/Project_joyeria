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
 * DAO de ventas: coordina facturación, detalle, pagos, impacto en stock y auditoría.
 * En negocio garantiza que cada venta de joyería quede registrada de forma íntegra y trazable.
 */
public class VentaDAO {

    // Consulta base diseñada para evitar duplicados por relaciones 1:N y devolver una fila por venta.
    /**
     * Consulta base de listado: una fila por venta con totales y método/estado de pago vía subconsultas.
     */    private static final String SQL_BASE = """
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
     * Lista todas las ventas con detalle y cálculo de anticipo/saldo.
     *
     * @return lista ordenada por fecha descendente
     * @throws Exception si falla SQL o cierre de conexión
     */
    public List<Venta> listarVentas() throws Exception {
        List<Venta> lista = new ArrayList<>();
        // Sin GROUP BY: cada fila es una venta única
        String sql = SQL_BASE + " ORDER BY v.fecha_emision DESC, v.venta_id DESC";
        
        Connection con = null;
        try {
            con = ConexionDB.getConnection();
            
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    Venta v = mapearVenta(rs);
                    v.setDetalles(listarDetalles(v.getVentaId(), con));
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
            if (con != null && !con.isClosed()) con.close();
        }
        return lista;
    }

    /**
     * Obtiene una venta por ID con líneas de detalle y modalidad de pago.
     *
     * @param ventaId identificador de venta
     * @return venta o {@code null}
     * @throws Exception si falla la consulta
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
     * Lista ventas de un vendedor concreto.
     *
     * @param usuarioId {@code usuario_id} del vendedor
     * @return ventas con detalles
     * @throws Exception si falla la consulta
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
     * Cuenta ventas con pago pendiente o sin fila en {@code Pago_Venta}.
     *
     * @return número de ventas “pendientes” de cobro según la regla SQL
     * @throws Exception si falla la consulta
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
     * Búsqueda filtrada por vendedor, fechas, criterio y tipo (id, cliente, vendedor, estado).
     *
     * @param criterio texto o valor según {@code tipoBusqueda}
     * @param tipoBusqueda modo de filtro
     * @param fechaInicio límite inferior de fecha de emisión (opcional)
     * @param fechaFin límite superior (opcional)
     * @param vendedorId si es positivo restringe al vendedor
     * @return ventas que cumplen los filtros
     * @throws Exception si falla la consulta o el parseo de ID
     */
    public List<Venta> buscarVentas(String criterio, String tipoBusqueda, Date fechaInicio, Date fechaFin, int vendedorId) throws Exception {
        List<Venta> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SQL_BASE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (vendedorId > 0) {
            sql.append(" AND v.usuario_id = ?");
            params.add(vendedorId);
        }
        if (criterio != null && !criterio.isBlank() && tipoBusqueda != null) {
            switch (tipoBusqueda) {
                case "id" -> {
                    sql.append(" AND v.venta_id = ?");
                    params.add(Integer.parseInt(criterio));
                }
                case "cliente" -> {
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
     * Mapea una fila del {@code SQL_BASE} a {@link Venta}.
     *
     * @param rs fila actual
     * @return modelo sin detalles (se cargan aparte)
     * @throws SQLException si falta alguna columna
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
     * Carga líneas de {@code Detalle_Venta} para una venta.
     *
     * @param ventaId identificador de venta
     * @param con conexión reutilizada
     * @return lista de detalles
     * @throws SQLException si falla la consulta
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
     * Ajusta en el objeto venta si la modalidad es anticipo (pagos confirmados vs pendientes).
     *
     * @param venta venta a enriquecer
     * @param con conexión abierta
     * @throws SQLException si falla el resumen de {@code Pago_Venta}
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

    // ========== INSERTAR (sin cambios) ==========
    /**
     * Registra venta, detalles, pagos ({@code Pago_Venta}), movimientos de inventario y auditoría en una transacción.
     *
     * @param venta cabecera (cliente, vendedor, fecha; método de pago como ID o nombre)
     * @param detalles líneas vendidas
     * @param modalidad {@code anticipo} o contado según flujo
     * @param montoAnticipo primer pago confirmado si aplica
     * @param saldoPendiente monto pendiente si aplica
     * @param usuarioIdAuditoria usuario para {@code Auditoria_Log}
     * @return {@code venta_id} generado
     * @throws Exception si falta stock u otro error SQL
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
                for (DetalleVenta d : detalles) {
                    if (!validarStock(con, d.getProductoId(), d.getCantidad())) {
                        throw new SQLException("Stock insuficiente para: " + d.getProductoNombre());
                    }
                }

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

                // CORRECCIÓN BUG 1: El formulario envía el metodo_pago_id (número),
                // no el nombre. Se parsea directamente en lugar de buscarlo por nombre.
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

                try (PreparedStatement ps = con.prepareStatement(sqlPagoVenta)) {
                    if ("anticipo".equals(modalidad) && montoAnticipo != null) {
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
                        ps.setInt(1, ventaId);
                        ps.setInt(2, metodoPagoId);
                        ps.setBigDecimal(3, venta.getTotal());
                        ps.setTimestamp(4, new Timestamp(venta.getFechaEmision().getTime()));
                        ps.setString(5, "confirmado");
                        ps.executeUpdate();
                    }
                }

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
     * Suma stock y registra movimiento de entrada por devolución aprobada.
     *
     * @param ventaId venta origen
     * @param productoId producto devuelto
     * @param cantidad unidades
     * @param usuarioIdAuditoria usuario para movimiento y auditoría
     * @return {@code true} si hubo commit
     * @throws Exception si falla la transacción
     */
    public boolean retornarStockDevolucion(int ventaId, int productoId, int cantidad, int usuarioIdAuditoria) throws Exception {
        String sqlInventario = "INSERT INTO Inventario_Movimiento(producto_id, usuario_id, tipo, cantidad, fecha, referencia) VALUES(?,?,?,?,?,?)";
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                actualizarStock(con, productoId, cantidad);
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
     * Confirma un pago pendiente actualizando monto y estado en una fila de {@code Pago_Venta}.
     *
     * @param ventaId venta afectada
     * @param montoAbono nuevo monto a registrar como confirmado
     * @return {@code true} si se ejecutó el update
     * @throws Exception si falla la transacción
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
     * Comprueba que el stock disponible en {@code Producto} sea suficiente.
     *
     * @param con conexión activa
     * @param productoId producto
     * @param cantidad cantidad solicitada
     * @return {@code true} si hay stock suficiente
     * @throws SQLException si falla el SELECT
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
     * Aplica un delta al stock del producto ({@code stock = stock + delta}).
     *
     * @param con conexión activa
     * @param productoId producto
     * @param delta cantidad positiva o negativa
     * @throws SQLException si falla el update
     */
    private void actualizarStock(Connection con, int productoId, int delta) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE Producto SET stock = stock + ? WHERE producto_id = ?")) {
            ps.setInt(1, delta);
            ps.setInt(2, productoId);
            ps.executeUpdate();
        }
    }

    /**
     * Inserta en {@code Auditoria_Log} con valores JSON seguros cuando el texto no es JSON válido.
     *
     * @param conn conexión abierta
     * @param usuarioId usuario que ejecuta la acción
     * @param accion tipo de acción
     * @param entidad entidad lógica
     * @param entidadId ID afectado
     * @param datosAnteriores JSON o texto plano
     * @param datosNuevos JSON o texto plano
     * @throws SQLException si falla el insert
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
     * @param texto cadena a validar
     * @return {@code true} si parsea como {@link org.json.JSONObject} o {@link org.json.JSONArray}
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