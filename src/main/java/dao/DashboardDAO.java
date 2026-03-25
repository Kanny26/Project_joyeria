package dao;

import config.ConexionDB;
import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object (DAO) especializado en la generación de indicadores y métricas
 * para el panel de control (Dashboard) del sistema de joyería.
 * 
 * Transforma datos operativos de las tablas de ventas, pagos, clientes y proveedores
 * en indicadores clave de rendimiento (KPIs) y alertas de gestión, proporcionando
 * visibilidad tanto a nivel administrativo como por vendedor individual.
 * 
 * Este DAO alimenta las vistas de análisis y seguimiento comercial, permitiendo
 * la toma de decisiones basada en datos en tiempo real.
 */
public class DashboardDAO {

    // ══════════════════════════════════════════════════════
    //  STATS ADMIN - MÉTRICAS DE ALTA GERENCIA
    // ══════════════════════════════════════════════════════
    // Estas métricas ofrecen una visión global del negocio para administradores,
    // incluyendo ingresos consolidados, volumen de ventas, estado de proveedores
    // y cantidad de usuarios activos en el sistema.

    /**
     * Calcula el total de ingresos efectivamente percibidos en el mes calendario actual.
     * 
     * Esta métrica considera únicamente pagos en estado 'confirmado', representando
     * el dinero real que ha entrado a caja durante el período.
     * 
     * @return Total de ingresos como BigDecimal, o BigDecimal.ZERO si no hay datos
     *         o ocurre un error en la consulta.
     */
    public BigDecimal getIngresosMes() {
        /*
         * Consulta SQL que suma los montos de pagos confirmados asociados a ventas del mes actual.
         * 
         * Tablas involucradas:
         *   - Pago_Venta (pv): Registro de pagos realizados, incluye monto y estado
         *   - Venta (v): Encabezado de venta, proporciona la fecha de emisión
         * 
         * Tipo de JOIN: INNER JOIN - Solo considera pagos que tienen una venta asociada,
         *               evitando registros huérfanos.
         * 
         * Condiciones de filtro:
         *   - pv.estado = 'confirmado': Solo pagos que han sido verificados y aprobados
         *   - MONTH(v.fecha_emision) = MONTH(CURDATE()): Ventas emitidas en el mes actual
         *   - YEAR(v.fecha_emision) = YEAR(CURDATE()): Ventas del año actual
         * 
         * COALESCE: Función que retorna 0 cuando SUM devuelve NULL (sin registros),
         *           evitando valores nulos en los reportes.
         */
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
     * Calcula el total facturado (valor bruto de ventas) en el mes calendario actual.
     * 
     * Diferencia clave respecto a getIngresosMes():
     *   - Facturación: Suma de productos vendidos (precio × cantidad), independientemente
     *     de si ya fueron pagados o no.
     *   - Ingresos: Solo pagos confirmados (dinero efectivamente recibido).
     * 
     * La diferencia entre ambos valores representa el saldo pendiente de cobro
     * en el período.
     * 
     * @return Suma total de los subtotales de todas las ventas del mes, o
     *         BigDecimal.ZERO si no hay datos o ocurre error.
     */
    public BigDecimal getVentasMes() {
        /*
         * Consulta SQL que suma el valor total de todas las líneas de detalle de ventas del mes.
         * 
         * Tablas involucradas:
         *   - Detalle_Venta (dv): Líneas individuales de productos vendidos, contiene
         *                         precio_unitario y cantidad
         *   - Venta (v): Encabezado de venta con fecha de emisión
         * 
         * Cálculo: precio_unitario * cantidad = subtotal por producto vendido
         * SUM: Acumula todos los subtotales de todas las ventas del período
         * 
         * COALESCE: Asegura que el resultado sea 0 en lugar de NULL cuando no hay ventas.
         */
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

    /**
     * Obtiene la cantidad de proveedores actualmente activos en el sistema.
     * 
     * @return Número de proveedores con estado = 1 (activo), o 0 si ocurre error.
     */
    public int getProveedoresActivos() {
        /*
         * Consulta simple de conteo sobre la tabla Proveedor.
         * 
         * Tabla: Proveedor
         * Condición: estado = 1 (activo)
         * 
         * Útil para evaluar la salud de la cadena de suministro y
         * la disponibilidad de alternativas de abastecimiento.
         */
        String sql = "SELECT COUNT(*) FROM Proveedor WHERE estado = 1";
        try (Connection c = ConexionDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    /**
     * Obtiene el total de usuarios registrados en el sistema.
     * 
     * Esta métrica incluye tanto usuarios activos como inactivos,
     * proporcionando una visión completa del alcance del sistema.
     * 
     * @return Cantidad total de registros en la tabla Usuario, o 0 si ocurre error.
     */
    public int getTotalUsuarios() {
        /*
         * Consulta simple de conteo de todos los usuarios.
         * 
         * Tabla: Usuario
         * Sin condiciones de filtro para obtener el total absoluto.
         */
        String sql = "SELECT COUNT(*) FROM Usuario";
        try (Connection c = ConexionDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    // ══════════════════════════════════════════════════════
    //  STATS VENDEDOR - MÉTRICAS INDIVIDUALES DE DESEMPEÑO
    // ══════════════════════════════════════════════════════
    // Estas métricas permiten a cada vendedor visualizar su propio rendimiento
    // comercial, incluyendo número de ventas, ingresos generados, casos pendientes
    // y ticket promedio.

    /**
     * Cuenta el número de ventas realizadas por un vendedor específico en el mes actual.
     * 
     * @param usuarioId Identificador único del usuario (vendedor) en el sistema
     * @return Número de ventas distintas realizadas por el vendedor en el mes,
     *         o 0 si no hay datos o ocurre error.
     */
    public int getVentasMesVendedor(int usuarioId) {
        /*
         * Consulta SQL que cuenta ventas únicas del vendedor en el período actual.
         * 
         * Tabla: Venta
         * 
         * Condiciones:
         *   - v.usuario_id = ?: Filtra por el ID del vendedor (parámetro)
         *   - MONTH(v.fecha_emision) = MONTH(CURDATE()): Mes actual
         *   - YEAR(v.fecha_emision) = YEAR(CURDATE()): Año actual
         * 
         * COUNT(DISTINCT v.venta_id): Asegura contar cada venta una sola vez,
         *                              incluso si tiene múltiples pagos o detalles.
         * 
         * Uso de PreparedStatement: Parametrización para prevenir SQL Injection
         * y permitir reutilización eficiente de la consulta.
         */
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
     * Calcula los ingresos efectivamente percibidos por un vendedor en el mes actual.
     * 
     * Diferencia de getVentasMesVendedor: Esta métrica suma solo los pagos confirmados,
     * representando el dinero que el vendedor ha ayudado a recaudar.
     * 
     * @param usuarioId ID del vendedor
     * @return Total de pagos confirmados de las ventas del vendedor en el mes,
     *         o BigDecimal.ZERO si no hay datos.
     */
    public BigDecimal getIngresosMesVendedor(int usuarioId) {
        /*
         * Consulta SQL que suma pagos confirmados de las ventas de un vendedor.
         * 
         * Tablas involucradas:
         *   - Pago_Venta (pv): Pagos realizados con su estado y monto
         *   - Venta (v): Ventas asociadas, que contienen el usuario_id del vendedor
         * 
         * JOIN: Relaciona cada pago con su venta para poder filtrar por vendedor
         * 
         * Condiciones:
         *   - v.usuario_id = ?: Solo ventas del vendedor especificado
         *   - pv.estado = 'confirmado': Solo pagos que ya fueron aprobados
         *   - Filtros de mes y año: Limitado al período actual
         */
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
     * Cuenta los casos abiertos (pagos pendientes) asociados a un vendedor.
     * 
     * Este indicador es clave para la gestión de cobranza: representa ventas
     * realizadas por el vendedor que aún no han sido pagadas completamente.
     * Un número alto puede indicar necesidad de seguimiento con clientes.
     * 
     * @param usuarioId ID del vendedor
     * @return Número de registros de pago en estado 'pendiente' para las ventas del vendedor
     */
    public int getCasosAbiertosVendedor(int usuarioId) {
        /*
         * Consulta SQL que cuenta pagos pendientes del vendedor.
         * 
         * Tablas involucradas:
         *   - Pago_Venta (pv): Pagos que pueden estar pendientes o confirmados
         *   - Venta (v): Ventas que vinculan el pago con el vendedor
         * 
         * Nota importante: No hay filtro de fecha, ya que los casos abiertos
         * pueden ser de cualquier período (deuda histórica que requiere atención).
         * 
         * Condiciones:
         *   - v.usuario_id = ?: Solo ventas del vendedor
         *   - pv.estado = 'pendiente': Solo pagos no confirmados
         */
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
     * Calcula el ticket promedio (valor medio por venta) de un vendedor en el mes actual.
     * 
     * Fórmula: Suma total facturada / Número total de ventas
     * 
     * Este indicador permite evaluar la efectividad comercial del vendedor:
     *   - Ticket alto: Buen manejo de upselling y productos premium
     *   - Ticket bajo: Posible foco en productos de bajo valor o promociones
     * 
     * @param usuarioId ID del vendedor
     * @return Promedio de venta como BigDecimal, o 0 si no hay ventas en el período.
     */
    public BigDecimal getPromedioVentaVendedor(int usuarioId) {
        /*
         * Consulta SQL que calcula el ticket promedio por venta.
         * 
         * Tablas involucradas:
         *   - Detalle_Venta (dv): Líneas de detalle con productos y cantidades
         *   - Venta (v): Encabezado de venta para filtrar por vendedor y fecha
         * 
         * Componentes del cálculo:
         *   1. SUM(dv.precio_unitario * dv.cantidad): Total facturado por el vendedor
         *   2. COUNT(DISTINCT v.venta_id): Número de ventas únicas realizadas
         *   3. División: Total facturado ÷ Número de ventas
         * 
         * Manejo de casos especiales:
         *   - NULLIF(..., 0): Si no hay ventas, la división se convierte en NULL
         *   - COALESCE(..., 0): Si el resultado es NULL, retorna 0
         * 
         * Esto evita errores de división por cero y resultados nulos en los reportes.
         */
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
     * Genera la lista de notificaciones proactivas para el panel del administrador.
     * 
     * Las notificaciones alertan sobre aspectos críticos que requieren atención:
     *   1. Ventas con saldo pendiente → Riesgo de cartera morosa
     *   2. Proveedores inactivos → Posible afectación en abastecimiento
     *   3. Nuevos usuarios registrados hoy → Actividad y crecimiento del sistema
     *   4. Ventas del día → Volumen de operación diario
     * 
     * Solo se muestran notificaciones con conteo > 0 para mantener el panel limpio
     * y enfocado en lo relevante.
     * 
     * @return Lista de mapas, cada mapa contiene:
     *         - "tipo": Clasificación visual (rose, amber, lavender)
     *         - "icono": Clase CSS de Font Awesome para el ícono
     *         - "texto": Mensaje descriptivo con el conteo dinámico
     */
    public List<Map<String, String>> getNotificacionesAdmin() {
        List<Map<String, String>> lista = new ArrayList<>();

        // 1. Ventas con pago pendiente (saldo por cobrar)
        /*
         * Alerta de gestión de cartera: Identifica el número total de pagos
         * pendientes en el sistema, sin importar el vendedor.
         * Importante para el área de cobranza y tesorería.
         */
        agregarNotif(lista,
            contarSQL("SELECT COUNT(*) FROM Pago_Venta WHERE estado = 'pendiente'"),
            "rose", "fas fa-undo-alt",
            n -> n + " venta(s) con saldo pendiente de cobro.");

        // 2. Proveedores inactivos
        /*
         * Alerta de cadena de suministro: Proveedores marcados como inactivos
         * que podrían necesitar revisión o reactivación.
         * Color amber: Atención media, requiere revisión pero no es urgente.
         */
        agregarNotif(lista,
            contarSQL("SELECT COUNT(*) FROM Proveedor WHERE estado = 0"),
            "amber", "fas fa-truck",
            n -> n + " proveedor(es) inactivo(s) en el sistema.");

        // 3. Nuevos usuarios creados hoy
        /*
         * Alerta de crecimiento: Usuarios registrados en el día de hoy.
         * Función DATE(fecha_creacion) = CURDATE(): Compara solo la fecha
         * ignorando la hora para incluir todos los registros del día.
         * Color lavender: Indicador de actividad positiva.
         */
        agregarNotif(lista,
            contarSQL("SELECT COUNT(*) FROM Usuario WHERE DATE(fecha_creacion) = CURDATE()"),
            "lavender", "fas fa-user-plus",
            n -> n + " usuario(s) registrado(s) hoy.");

        // 4. Ventas registradas hoy
        /*
         * Alerta de actividad comercial: Número de ventas realizadas en el día.
         * Indicador de volumen de operación y ritmo de negocio.
         */
        agregarNotif(lista,
            contarSQL("SELECT COUNT(*) FROM Venta WHERE DATE(fecha_emision) = CURDATE()"),
            "amber", "fas fa-receipt",
            n -> n + " venta(s) registrada(s) hoy.");

        return lista;
    }

    /**
     * Genera la lista de notificaciones personalizadas para un vendedor específico.
     * 
     * Las notificaciones ayudan al vendedor a autogestionar su desempeño:
     *   1. Pagos pendientes → Acciones de cobranza requeridas
     *   2. Ventas de hoy → Feedback inmediato de actividad diaria
     *   3. Ventas del mes → Visión de progreso hacia metas comerciales
     * 
     * @param usuarioId ID del vendedor para filtrar notificaciones personalizadas
     * @return Lista de mapas con notificaciones relevantes para el vendedor
     */
    public List<Map<String, String>> getNotificacionesVendedor(int usuarioId) {
        List<Map<String, String>> lista = new ArrayList<>();

        // 1. Pagos pendientes del vendedor
        /*
         * Notificación de cobranza personalizada: Cuenta los pagos pendientes
         * exclusivamente de las ventas realizadas por este vendedor.
         * Ayuda a priorizar seguimiento con sus clientes.
         */
        agregarNotifParam(lista,
            contarSQLParam(
                "SELECT COUNT(*) FROM Pago_Venta pv JOIN Venta v ON v.venta_id = pv.venta_id WHERE v.usuario_id = ? AND pv.estado = 'pendiente'",
                usuarioId),
            "rose", "fas fa-receipt",
            n -> n + " venta(s) tuya(s) con pago pendiente.");

        // 2. Ventas realizadas hoy por el vendedor
        /*
         * Feedback diario: Muestra cuántas ventas ha registrado el vendedor
         * en el día actual, permitiendo autoevaluación y ajuste de estrategia.
         */
        agregarNotifParam(lista,
            contarSQLParam(
                "SELECT COUNT(*) FROM Venta WHERE usuario_id = ? AND DATE(fecha_emision) = CURDATE()",
                usuarioId),
            "lavender", "fas fa-check-circle",
            n -> "Registraste " + n + " venta(s) hoy.");

        // 3. Ventas del mes
        /*
         * Progreso mensual: Total de ventas acumuladas en el mes actual,
         * motivando al vendedor a alcanzar o superar sus metas comerciales.
         */
        agregarNotifParam(lista,
            contarSQLParam(
                "SELECT COUNT(*) FROM Venta WHERE usuario_id = ? AND MONTH(fecha_emision)=MONTH(CURDATE()) AND YEAR(fecha_emision)=YEAR(CURDATE())",
                usuarioId),
            "amber", "fas fa-chart-line",
            n -> "Llevas " + n + " venta(s) este mes.");

        return lista;
    }

    /**
     * Método utilitario para ejecutar consultas COUNT sin parámetros.
     * 
     * Encapsula la lógica de conexión, ejecución y manejo de excepciones
     * para consultas que no requieren parametrización, reduciendo código duplicado.
     * 
     * @param sql Consulta SQL que debe retornar un único valor entero (COUNT)
     * @return Valor entero obtenido del primer campo del ResultSet, o 0 si no hay
     *         resultados o ocurre una excepción.
     */
    private int contarSQL(String sql) {
        try (Connection c = ConexionDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    /**
     * Método utilitario para ejecutar consultas COUNT con un parámetro entero.
     * 
     * Versión parametrizada de contarSQL, útil para consultas que requieren
     * filtrar por ID de usuario u otros valores dinámicos.
     * 
     * @param sql Consulta SQL con un placeholder '?' para el parámetro
     * @param param Valor entero a asignar al placeholder (ej: usuario_id)
     * @return Valor entero obtenido del COUNT, o 0 si no hay resultados o error
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
     * Interfaz funcional para construir mensajes dinámicos de notificaciones.
     * 
     * Permite personalizar el texto de la notificación según el valor del contador,
     * facilitando la internacionalización y adaptación a diferentes contextos.
     */
    @FunctionalInterface
    interface MensajeBuilder { 
        /**
         * Construye el mensaje completo a partir del conteo numérico.
         * 
         * @param n Valor numérico del contador (número de elementos a notificar)
         * @return Mensaje formateado listo para mostrar en la interfaz
         */
        String build(int n); 
    }

    /**
     * Agrega una notificación a la lista solo si el contador es positivo.
     * 
     * Este método evita mostrar notificaciones vacías (count = 0) que no aportan
     * información útil y saturan visualmente el panel de control.
     * 
     * @param lista Lista destino donde se almacenarán las notificaciones
     * @param count Valor del contador (si es 0 o negativo, no se agrega)
     * @param tipo Estilo visual de la notificación (rose, amber, lavender)
     * @param icono Clase CSS de Font Awesome para el ícono (ej: fas fa-receipt)
     * @param msg Constructor funcional que genera el mensaje personalizado
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
     * Alias de agregarNotif para mantener consistencia en llamadas con parámetros.
     * 
     * Este método existe para hacer explícito en el código cuando se está
     * utilizando una consulta parametrizada, mejorando la legibilidad.
     * 
     * @see #agregarNotif(List, int, String, String, MensajeBuilder)
     */
    private void agregarNotifParam(List<Map<String, String>> lista, int count,
                                    String tipo, String icono, MensajeBuilder msg) {
        agregarNotif(lista, count, tipo, icono, msg);
    }
}