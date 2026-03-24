package dao;

import config.ConexionDB;
import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * transforma datos operativos de joyería en indicadores de decisión
 * (ingresos, ventas, pendientes y alertas) para administración y vendedores.
 */
public class DashboardDAO {

    // ══════════════════════════════════════════════════════
    //  STATS ADMIN
    // ══════════════════════════════════════════════════════

    /**
     * Suma de todos los pagos con estado {@code confirmado} del mes calendario actual.
     *
     * @return total de ingresos o cero si falla o no hay datos
     */
    public BigDecimal getIngresosMes() {
        String sql = """
            SELECT COALESCE(SUM(pv.monto), 0)
            FROM Pago_Venta pv
            JOIN Venta v ON v.venta_id = pv.venta_id
            WHERE pv.estado = 'confirmado'
              AND MONTH(v.fecha_emision) = MONTH(CURDATE())
              AND YEAR(v.fecha_emision)  = YEAR(CURDATE())
            """;
        try (Connection c = ConexionDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        } catch (Exception e) { e.printStackTrace(); return BigDecimal.ZERO; }
    }

    /**
     * Total facturado (suma de líneas de detalle) en ventas del mes actual.
     *
     * @return suma de subtotales o cero
     */
    public BigDecimal getVentasMes() {
        String sql = """
            SELECT COALESCE(SUM(dv.precio_unitario * dv.cantidad), 0)
            FROM Detalle_Venta dv
            JOIN Venta v ON v.venta_id = dv.venta_id
            WHERE MONTH(v.fecha_emision) = MONTH(CURDATE())
              AND YEAR(v.fecha_emision)  = YEAR(CURDATE())
            """;
        try (Connection c = ConexionDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        } catch (Exception e) { e.printStackTrace(); return BigDecimal.ZERO; }
    }

    /** Proveedores con estado activo */
    public int getProveedoresActivos() {
        String sql = "SELECT COUNT(*) FROM Proveedor WHERE estado = 1";
        try (Connection c = ConexionDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    /**
     * Total de filas en la tabla {@code Usuario}.
     *
     * @return cantidad de usuarios o cero si falla
     */
    public int getTotalUsuarios() {
        String sql = "SELECT COUNT(*) FROM Usuario";
        try (Connection c = ConexionDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    // ══════════════════════════════════════════════════════
    //  STATS VENDEDOR
    // ══════════════════════════════════════════════════════

    /**
     * Ventas distintas del vendedor en el mes actual.
     *
     * @param usuarioId ID del usuario vendedor
     * @return número de ventas o cero si falla
     */
    public int getVentasMesVendedor(int usuarioId) {
        String sql = """
            SELECT COUNT(DISTINCT v.venta_id)
            FROM Venta v
            WHERE v.usuario_id = ?
              AND MONTH(v.fecha_emision) = MONTH(CURDATE())
              AND YEAR(v.fecha_emision)  = YEAR(CURDATE())
            """;
        try (Connection c = ConexionDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    /**
     * Suma de pagos confirmados de las ventas del vendedor en el mes actual.
     *
     * @param usuarioId ID del vendedor
     * @return total de ingresos o cero
     */
    public BigDecimal getIngresosMesVendedor(int usuarioId) {
        String sql = """
            SELECT COALESCE(SUM(pv.monto), 0)
            FROM Pago_Venta pv
            JOIN Venta v ON v.venta_id = pv.venta_id
            WHERE v.usuario_id = ?
              AND pv.estado = 'confirmado'
              AND MONTH(v.fecha_emision) = MONTH(CURDATE())
              AND YEAR(v.fecha_emision)  = YEAR(CURDATE())
            """;
        try (Connection c = ConexionDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        } catch (Exception e) { e.printStackTrace(); return BigDecimal.ZERO; }
    }

    /**
     * Cuenta pagos de venta en estado {@code pendiente} asociados al vendedor (indicador de saldo/casos abiertos).
     *
     * @param usuarioId ID del vendedor
     * @return cantidad de registros o cero
     */
    public int getCasosAbiertosVendedor(int usuarioId) {
        String sql = """
            SELECT COUNT(*)
            FROM Pago_Venta pv
            JOIN Venta v ON v.venta_id = pv.venta_id
            WHERE v.usuario_id = ?
              AND pv.estado = 'pendiente'
            """;
        try (Connection c = ConexionDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    /**
     * Promedio de valor de venta (total detalle / número de ventas) del vendedor en el mes.
     *
     * @param usuarioId ID del vendedor
     * @return promedio o cero si no hay ventas o falla la consulta
     */
    public BigDecimal getPromedioVentaVendedor(int usuarioId) {
        String sql = """
            SELECT COALESCE(
                SUM(dv.precio_unitario * dv.cantidad) / NULLIF(COUNT(DISTINCT v.venta_id), 0),
            0)
            FROM Detalle_Venta dv
            JOIN Venta v ON v.venta_id = dv.venta_id
            WHERE v.usuario_id = ?
              AND MONTH(v.fecha_emision) = MONTH(CURDATE())
              AND YEAR(v.fecha_emision)  = YEAR(CURDATE())
            """;
        try (Connection c = ConexionDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        } catch (Exception e) { e.printStackTrace(); return BigDecimal.ZERO; }
    }

    /**
     * Arma la lista de notificaciones para el administrador (texto, icono y color).
     *
     * @return lista de mapas con claves {@code tipo}, {@code icono}, {@code texto}
     */
    public List<Map<String, String>> getNotificacionesAdmin() {
        List<Map<String, String>> lista = new ArrayList<>();

        // 1. Ventas con pago pendiente (saldo)
        agregarNotif(lista,
            contarSQL("SELECT COUNT(*) FROM Pago_Venta WHERE estado = 'pendiente'"),
            "rose", "fas fa-undo-alt",
            n -> n + " venta(s) con saldo pendiente de cobro.");

        // 2. Proveedores inactivos
        agregarNotif(lista,
            contarSQL("SELECT COUNT(*) FROM Proveedor WHERE estado = 0"),
            "amber", "fas fa-truck",
            n -> n + " proveedor(es) inactivo(s) en el sistema.");

        // 3. Nuevos usuarios creados hoy
        agregarNotif(lista,
            contarSQL("SELECT COUNT(*) FROM Usuario WHERE DATE(fecha_creacion) = CURDATE()"),
            "lavender", "fas fa-user-plus",
            n -> n + " usuario(s) registrado(s) hoy.");

        // 4. Ventas registradas hoy
        agregarNotif(lista,
            contarSQL("SELECT COUNT(*) FROM Venta WHERE DATE(fecha_emision) = CURDATE()"),
            "amber", "fas fa-receipt",
            n -> n + " venta(s) registrada(s) hoy.");

        return lista;
    }

    /**
     * Notificaciones para un vendedor concreto (pendientes, ventas hoy, ventas del mes).
     *
     * @param usuarioId ID del vendedor
     * @return lista de mapas con {@code tipo}, {@code icono}, {@code texto}
     */
    public List<Map<String, String>> getNotificacionesVendedor(int usuarioId) {
        List<Map<String, String>> lista = new ArrayList<>();

        // 1. Pagos pendientes del vendedor
        agregarNotifParam(lista,
            contarSQLParam(
                "SELECT COUNT(*) FROM Pago_Venta pv JOIN Venta v ON v.venta_id = pv.venta_id WHERE v.usuario_id = ? AND pv.estado = 'pendiente'",
                usuarioId),
            "rose", "fas fa-receipt",
            n -> n + " venta(s) tuya(s) con pago pendiente.");

        // 2. Ventas realizadas hoy por el vendedor
        agregarNotifParam(lista,
            contarSQLParam(
                "SELECT COUNT(*) FROM Venta WHERE usuario_id = ? AND DATE(fecha_emision) = CURDATE()",
                usuarioId),
            "lavender", "fas fa-check-circle",
            n -> "Registraste " + n + " venta(s) hoy.");

        // 3. Ventas del mes
        agregarNotifParam(lista,
            contarSQLParam(
                "SELECT COUNT(*) FROM Venta WHERE usuario_id = ? AND MONTH(fecha_emision)=MONTH(CURDATE()) AND YEAR(fecha_emision)=YEAR(CURDATE())",
                usuarioId),
            "amber", "fas fa-chart-line",
            n -> "Llevas " + n + " venta(s) este mes.");

        return lista;
    }

    /**
     * @param sql consulta que devuelve un único entero (COUNT)
     * @return primer valor entero o 0 si falla
     */
    private int contarSQL(String sql) {
        try (Connection c = ConexionDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    /**
     * @param sql consulta con un parámetro entero en el primer {@code ?}
     * @param param valor a enlazar
     * @return resultado del COUNT o 0
     */
    private int contarSQLParam(String sql, int param) {
        try (Connection c = ConexionDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    /**
     * Construye el texto de la notificación a partir del conteo.
     */
    @FunctionalInterface
    interface MensajeBuilder { String build(int n); }

    /**
     * Si el conteo es positivo, añade una entrada a la lista de notificaciones.
     *
     * @param lista destino
     * @param count valor del contador
     * @param tipo estilo (p. ej. amber, rose)
     * @param icono clase Font Awesome
     * @param msg generador del mensaje
     */
    private void agregarNotif(List<Map<String, String>> lista, int count,
                               String tipo, String icono, MensajeBuilder msg) {
        if (count > 0) {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("tipo",   tipo);
            m.put("icono",  icono);
            m.put("texto",  msg.build(count));
            lista.add(m);
        }
    }

    /**
     * Alias de {@link #agregarNotif(List, int, String, String, MensajeBuilder)} para llamadas con SQL parametrizado.
     */
    private void agregarNotifParam(List<Map<String, String>> lista, int count,
                                    String tipo, String icono, MensajeBuilder msg) {
        agregarNotif(lista, count, tipo, icono, msg);
    }
}