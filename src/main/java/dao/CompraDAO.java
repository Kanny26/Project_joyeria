package dao;

import model.Compra;
import model.DetalleCompra;
import config.ConexionDB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de Compra.
 *
 * insertarConTransaccion() escribe en:
 *   1. Compra              (siempre)
 *   2. Detalle_Compra      (siempre, un registro por producto)
 *   3. Pago_Compra         (siempre; monto = total si es contado, monto = anticipo si es crédito)
 *   4. Credito_Compra      (solo si esCredito == true)
 *   5. Abono_Credito       (solo si esCredito == true y anticipo > 0)
 *
 * Todo en una sola transacción: si algo falla se hace rollback completo.
 */
public class CompraDAO {

    // ════════════════════════════════════════════════════════════════════
    // INSERT principal con transacción
    // ════════════════════════════════════════════════════════════════════
    public boolean insertarConTransaccion(Compra compra) throws Exception {

        Connection con = null;
        try {
            con = ConexionDB.getConnection();
            con.setAutoCommit(false);

            /* 1. Insertar en Compra */
            int compraId = insertarCompra(con, compra);
            compra.setCompraId(compraId);

            /* 2. Insertar Detalle_Compra */
            insertarDetalles(con, compraId, compra.getDetalles());

            /* 3. Insertar Pago_Compra
             *    - Si es contado  → monto = total, estado = 'confirmado'
             *    - Si es crédito  → monto = anticipo, estado = 'pendiente'
             *      (el pago queda pendiente hasta confirmar el anticipo)
             */
            BigDecimal montoPago   = compra.isEsCredito() ? compra.getAnticipo() : compra.getTotal();
            String     estadoPago  = compra.isEsCredito() ? "pendiente"          : "confirmado";
            insertarPagoCompra(con, compraId, compra.getMetodoPagoId(), montoPago, estadoPago);

            /* 4. Si es crédito → Credito_Compra */
            if (compra.isEsCredito()) {
                BigDecimal saldoPendiente = compra.getTotal().subtract(compra.getAnticipo())
                                                             .max(BigDecimal.ZERO);
                // Si el admin marcó estadoCredito = 'pagado', forzamos saldo = 0
                if ("pagado".equals(compra.getEstadoCredito())) {
                    saldoPendiente = BigDecimal.ZERO;
                }

                int creditoId = insertarCreditoCompra(
                    con,
                    compraId,
                    compra.getTotal(),
                    saldoPendiente,
                    compra.getFechaCompra(),
                    compra.getFechaVencimiento(),
                    compra.getEstadoCredito()
                );

                /* 5. Abono_Credito con el anticipo (si anticipo > 0) */
                if (compra.getAnticipo().compareTo(BigDecimal.ZERO) > 0) {
                    insertarAbonoCredito(
                        con,
                        creditoId,
                        compra.getMetodoPagoId(),
                        compra.getAnticipo(),
                        "confirmado"   // el anticipo ya se pagó al registrar
                    );
                }
            }

            con.commit();
            return true;

        } catch (Exception e) {
            if (con != null) { try { con.rollback(); } catch (SQLException ignored) {} }
            throw e;
        } finally {
            if (con != null) { try { con.setAutoCommit(true); con.close(); } catch (SQLException ignored) {} }
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // Pasos individuales
    // ════════════════════════════════════════════════════════════════════

    private int insertarCompra(Connection con, Compra c) throws SQLException {
        String sql = "INSERT INTO Compra (proveedor_id, usuario_id, fecha_compra, fecha_entrega) VALUES (?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getProveedorId());
            ps.setInt(2, c.getUsuarioId());
            ps.setDate(3, new java.sql.Date(c.getFechaCompra().getTime()));
            ps.setDate(4, new java.sql.Date(c.getFechaEntrega().getTime()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                throw new SQLException("No se obtuvo el ID de la compra creada");
            }
        }
    }

    private void insertarDetalles(Connection con, int compraId, List<DetalleCompra> detalles)
            throws SQLException {
        String sql = "INSERT INTO Detalle_Compra (compra_id, producto_id, precio_unitario, cantidad) VALUES (?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (DetalleCompra d : detalles) {
                ps.setInt(1, compraId);
                ps.setInt(2, d.getProductoId());
                ps.setBigDecimal(3, d.getPrecioUnitario());
                ps.setInt(4, d.getCantidad());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Inserta en Pago_Compra.
     * Columnas: compra_id, metodo_pago_id, monto, fecha (DEFAULT NOW()), estado
     */
    private void insertarPagoCompra(Connection con, int compraId, int metodoPagoId,
                                    BigDecimal monto, String estado) throws SQLException {
        String sql = "INSERT INTO Pago_Compra (compra_id, metodo_pago_id, monto, estado) VALUES (?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, compraId);
            ps.setInt(2, metodoPagoId);
            ps.setBigDecimal(3, monto);
            ps.setString(4, estado);
            ps.executeUpdate();
        }
    }

    /**
     * Inserta en Credito_Compra.
     * Columnas: compra_id, monto_total, saldo_pendiente, fecha_inicio, fecha_vencimiento, estado
     */
    private int insertarCreditoCompra(Connection con, int compraId, BigDecimal montoTotal,
                                       BigDecimal saldoPendiente, java.util.Date fechaInicio,
                                       java.util.Date fechaVencimiento, String estado) throws SQLException {
        String sql = "INSERT INTO Credito_Compra "
                   + "(compra_id, monto_total, saldo_pendiente, fecha_inicio, fecha_vencimiento, estado) "
                   + "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, compraId);
            ps.setBigDecimal(2, montoTotal);
            ps.setBigDecimal(3, saldoPendiente);
            ps.setDate(4, new java.sql.Date(fechaInicio.getTime()));
            ps.setDate(5, new java.sql.Date(fechaVencimiento.getTime()));
            ps.setString(6, estado);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                throw new SQLException("No se obtuvo el ID del crédito creado");
            }
        }
    }

    /**
     * Inserta en Abono_Credito el anticipo inicial.
     * Columnas: credito_id, metodo_pago_id, monto_abono, fecha (DEFAULT NOW()), estado
     */
    private void insertarAbonoCredito(Connection con, int creditoId, int metodoPagoId,
                                       BigDecimal montoAbono, String estado) throws SQLException {
        String sql = "INSERT INTO Abono_Credito (credito_id, metodo_pago_id, monto_abono, estado) VALUES (?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, creditoId);
            ps.setInt(2, metodoPagoId);
            ps.setBigDecimal(3, montoAbono);
            ps.setString(4, estado);
            ps.executeUpdate();
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // Obtener compra por ID (con detalles y crédito si existe)
    // ════════════════════════════════════════════════════════════════════
    public Compra obtenerPorId(int compraId) throws Exception {
        String sql =
            "SELECT c.compra_id, c.proveedor_id, c.usuario_id, c.fecha_compra, c.fecha_entrega, " +
            "       pc.metodo_pago_id, pc.monto AS monto_pago, pc.estado AS estado_pago, " +
            "       cc.credito_id, cc.monto_total, cc.saldo_pendiente, " +
            "       cc.fecha_inicio, cc.fecha_vencimiento, cc.estado AS estado_credito " +
            "FROM Compra c " +
            "LEFT JOIN Pago_Compra  pc ON pc.compra_id  = c.compra_id " +
            "LEFT JOIN Credito_Compra cc ON cc.compra_id = c.compra_id " +
            "WHERE c.compra_id = ? " +
            "LIMIT 1";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, compraId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Compra compra = new Compra();
                compra.setCompraId(rs.getInt("compra_id"));
                compra.setProveedorId(rs.getInt("proveedor_id"));
                compra.setUsuarioId(rs.getInt("usuario_id"));
                compra.setFechaCompra(rs.getDate("fecha_compra"));
                compra.setFechaEntrega(rs.getDate("fecha_entrega"));
                compra.setMetodoPagoId(rs.getInt("metodo_pago_id"));
                compra.setTotal(rs.getBigDecimal("monto_pago"));      // monto registrado en Pago_Compra
                // Crédito (puede ser null si fue contado)
                BigDecimal montoTotal = rs.getBigDecimal("monto_total");
                if (montoTotal != null) {
                    compra.setEsCredito(true);
                    compra.setTotal(montoTotal);
                    compra.setAnticipo(montoTotal.subtract(rs.getBigDecimal("saldo_pendiente")));
                    compra.setFechaVencimiento(rs.getDate("fecha_vencimiento"));
                    compra.setEstadoCredito(rs.getString("estado_credito"));
                }
                compra.setDetalles(obtenerDetalles(con, compraId));
                return compra;
            }
        }
    }

    private List<DetalleCompra> obtenerDetalles(Connection con, int compraId) throws SQLException {
        String sql = "SELECT dc.detalle_compra_id, dc.producto_id, dc.precio_unitario, dc.cantidad, " +
                     "       p.nombre AS nombre_producto " +
                     "FROM Detalle_Compra dc " +
                     "JOIN Producto p ON p.producto_id = dc.producto_id " +
                     "WHERE dc.compra_id = ?";
        List<DetalleCompra> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, compraId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleCompra d = new DetalleCompra();
                    d.setDetalleCompraId(rs.getInt("detalle_compra_id"));
                    d.setProductoId(rs.getInt("producto_id"));
                    d.setProductoNombre(rs.getString("nombre_producto"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setSubtotal(d.getPrecioUnitario().multiply(new BigDecimal(d.getCantidad())));
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    // ════════════════════════════════════════════════════════════════════
    // Eliminar compra (CASCADE borra detalles, pagos, créditos y abonos)
    // ════════════════════════════════════════════════════════════════════
    public boolean eliminarConTransaccion(int compraId) throws Exception {
        // ON DELETE CASCADE está definido en la BD para Detalle_Compra,
        // Pago_Compra, Credito_Compra (y por extensión Abono_Credito).
        // Solo hace falta eliminar el registro padre.
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM Compra WHERE compra_id = ?")) {
            ps.setInt(1, compraId);
            return ps.executeUpdate() > 0;
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // Listar compras por proveedor (con detalles y total real)
    // ════════════════════════════════════════════════════════════════════
    public List<Compra> listarPorProveedor(int proveedorId) throws Exception {
        String sql =
            "SELECT c.compra_id, c.proveedor_id, c.usuario_id, c.fecha_compra, c.fecha_entrega, " +
            "       COALESCE(cc.monto_total, pc.monto, 0) AS total_real " +
            "FROM Compra c " +
            "LEFT JOIN Pago_Compra pc ON pc.compra_id = c.compra_id " +
            "LEFT JOIN Credito_Compra cc ON cc.compra_id = c.compra_id " +
            "WHERE c.proveedor_id = ? " +
            "ORDER BY c.fecha_compra DESC";

        List<Compra> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, proveedorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Compra c = new Compra();
                    c.setCompraId(rs.getInt("compra_id"));
                    c.setProveedorId(rs.getInt("proveedor_id"));
                    c.setUsuarioId(rs.getInt("usuario_id"));
                    c.setFechaCompra(rs.getDate("fecha_compra"));
                    c.setFechaEntrega(rs.getDate("fecha_entrega"));
                    java.math.BigDecimal total = rs.getBigDecimal("total_real");
                    c.setTotal(total != null ? total : java.math.BigDecimal.ZERO);
                    c.setDetalles(obtenerDetalles(con, c.getCompraId()));
                    lista.add(c);
                }
            }
        }
        return lista;
    }
}