package dao;

import model.Compra;
import model.DetalleCompra;
import config.ConexionDB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestión de compras de abastecimiento con impacto en inventario y pagos asociados.
 * Esta clase centraliza el proceso completo de registro de compras, garantizando consistencia
 * transaccional: o se persisten todos los componentes (cabecera, detalles, movimientos de
 * inventario, pagos y créditos), o se revierte completamente la operación mediante rollback.
 * El diseño transaccional asegura integridad de datos en operaciones críticas de negocio
 * como actualización de stock, registro de costos y gestión de créditos a proveedores.
 * Utiliza ConexionDB para obtención de conexiones y sigue buenas prácticas de JDBC con
 * PreparedStatement para prevenir SQL Injection y optimizar ejecución de consultas.
 */
public class CompraDAO {

    /*
     * Guarda una compra completa usando una transacción.
     * Si cualquier paso falla, se hace rollback y no queda nada guardado a medias.
     *
     * Flujo:
     *   1. Insertar cabecera en Compra
     *   2. Insertar filas en Detalle_Compra
     *   3. Actualizar stock en Producto + registrar en Inventario_Movimiento
     *   4. Insertar pago en Pago_Compra
     *   5. (si crédito) Insertar en Credito_Compra
     *   6. (si crédito y anticipo > 0) Insertar abono inicial en Abono_Credito
     */
    /**
     * Persiste una compra completa en una sola transacción atómica que incluye:
     * cabecera de compra, líneas de detalle, actualización de inventario (stock y precio),
     * registro de pago, y gestión de crédito con abonos si aplica.
     *
     * Flujo transaccional detallado:
     *   1. Insertar registro en tabla Compra y recuperar el compra_id generado
     *   2. Insertar múltiples registros en Detalle_Compra mediante batch para eficiencia
     *   3. Actualizar stock y precio_unitario en Producto + registrar entrada en Inventario_Movimiento
     *   4. Insertar registro de pago en Pago_Compra con estado según modalidad (contado/crédito)
     *   5. Si es crédito: insertar registro en Credito_Compra con saldos y fechas
     *   6. Si hay anticipo: insertar primer abono en Abono_Credito
     *
     * Gestión de transacción: se desactiva auto-commit al inicio, y se realiza commit solo si
     * todos los pasos completan exitosamente. Cualquier excepción desencadena rollback para
     * mantener consistencia de datos. Finalmente se restaura auto-commit y se cierra la conexión.
     *
     * @param compra objeto Compra con todos los datos necesarios: proveedor, fechas, detalles,
     *               método de pago, banderas de crédito, montos y usuario responsable
     * @return {@code true} si se completó exitosamente el commit de la transacción completa
     * @throws Exception si falla cualquier paso del proceso; se propaga la excepción original
     *                   tras ejecutar rollback para que la capa superior decida el manejo
     */
    public boolean insertarConTransaccion(Compra compra) throws Exception {
        Connection con = null;
        try {
            // Obtiene conexión manual para controlar explícitamente la transacción
            con = ConexionDB.getConnection();
            // Desactiva auto-commit para iniciar transacción manual atómica
            con.setAutoCommit(false);

            // Paso 1: Inserta cabecera de compra y recupera el ID generado por la BD
            int compraId = insertarCompra(con, compra);
            compra.setCompraId(compraId);

            // Paso 2: Inserta todas las líneas de detalle mediante batch para eficiencia
            insertarDetalles(con, compraId, compra.getDetalles());

            // En negocio necesitamos trazabilidad de quién registró la entrada de inventario;
            // por eso se propaga usuarioId hacia Inventario_Movimiento.
            // Paso 3: Actualiza stock y precio en Producto + registra movimiento de inventario
            registrarEntradaInventario(con, compra.getDetalles(),
                    "Compra #" + compraId, compra.getUsuarioId());

            // Paso 4: Determina monto y estado del pago según modalidad (contado o crédito)
            // Si es crédito: se registra solo el anticipo como pago inicial con estado pendiente
            // Si es contado: se registra el total completo con estado confirmado
            BigDecimal montoPago  = compra.isEsCredito() ? compra.getAnticipo() : compra.getTotal();
            String     estadoPago = compra.isEsCredito() ? "pendiente" : "confirmado";
            insertarPagoCompra(con, compraId, compra.getMetodoPagoId(), montoPago, estadoPago);

            // Pasos 5 y 6: Gestión de crédito solo si la compra fue a crédito
            if (compra.isEsCredito()) {
                // Calcula saldo pendiente: si estado es "pagado" saldo es 0, sino total menos anticipo
                BigDecimal saldoPendiente = "pagado".equals(compra.getEstadoCredito())
                        ? BigDecimal.ZERO
                        : compra.getTotal().subtract(compra.getAnticipo()).max(BigDecimal.ZERO);

                // Paso 5: Inserta registro de crédito con montos, fechas y estado
                int creditoId = insertarCreditoCompra(con, compraId,
                        compra.getTotal(), saldoPendiente,
                        compra.getFechaCompra(), compra.getFechaVencimiento(),
                        compra.getEstadoCredito());

                // Paso 6: Si hay anticipo mayor a cero, registra el primer abono al crédito
                if (compra.getAnticipo().compareTo(BigDecimal.ZERO) > 0) {
                    insertarAbonoCredito(con, creditoId,
                            compra.getMetodoPagoId(), compra.getAnticipo(), "confirmado");
                }
            }

            // Commit final: confirma permanentemente todos los cambios de la transacción
            con.commit();
            return true;

        } catch (Exception e) {
            // En caso de error en cualquier paso, ejecuta rollback para revertir todos los cambios
            // y mantener consistencia de datos en la base de datos
            if (con != null) {
                try { con.rollback(); } catch (SQLException ignored) {}
            }
            // Propaga la excepción original para manejo en capa superior
            throw e;
        } finally {
            // Bloque finally: garantiza restauración de auto-commit y cierre de conexión
            // incluso si ocurre excepción, previniendo fugas de recursos
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Actualiza stock, precio de costo y registra movimiento de inventario para cada línea
     * de la compra. Este método ejecuta tres operaciones por cada detalle:
     *   1. UPDATE Producto: incrementa stock en la cantidad comprada
     *   2. UPDATE Producto: actualiza precio_unitario con el costo de esta compra
     *   3. INSERT Inventario_Movimiento: registra entrada de inventario con trazabilidad
     *
     * Consulta SQL 1 - Actualización de stock:
     *   Tabla: Producto
     *   Operación: UPDATE con incremento acumulativo (stock = stock + ?)
     *   Condición: WHERE producto_id = ? (actualiza solo el producto específico)
     *   Propósito: reflejar en inventario la entrada de mercancía de esta compra
     *
     * Consulta SQL 2 - Actualización de precio de costo:
     *   Tabla: Producto
     *   Operación: UPDATE que sobreescribe precio_unitario con nuevo costo de compra
     *   Condición: WHERE producto_id = ? (actualiza solo el producto específico)
     *   Propósito: mantener actualizado el costo base para cálculo de márgenes y valuación
     *
     * Consulta SQL 3 - Registro de movimiento de inventario:
     *   Tabla: Inventario_Movimiento
     *   Operación: INSERT para crear registro histórico de entrada de inventario
     *   Columnas insertadas:
     *     - producto_id: FK hacia Producto para identificar el artículo afectado
     *     - usuario_id: FK hacia Usuario para trazabilidad de quién registró (nullable)
     *     - tipo: valor fijo 'entrada' para clasificar el movimiento
     *     - cantidad: número de unidades que ingresaron al inventario
     *     - fecha: NOW() para timestamp automático del servidor de BD
     *     - referencia: texto descriptivo (ej: "Compra #123") para contexto del movimiento
     *
     * @param con conexión activa de base de datos dentro de transacción en curso
     * @param detalles lista de objetos DetalleCompra con productos, cantidades y precios
     * @param referencia texto descriptivo para identificar el origen del movimiento en historial
     * @param usuarioId ID del usuario que registra la operación, o {@code null} si es anónimo
     * @throws SQLException si algún UPDATE no afecta filas (producto no existe) o error SQL
     */
    private void registrarEntradaInventario(Connection con,
                                             List<DetalleCompra> detalles,
                                             String referencia,
                                             Integer usuarioId) throws SQLException {
        // UPDATE sin "AND estado = 1" para que funcione con cualquier producto
        // SQL 1: Actualiza stock acumulativo en tabla Producto
        // Tabla: Producto (entidad maestra de inventario)
        // Operación: stock = stock + ? (incremento basado en cantidad comprada)
        // WHERE producto_id = ?: filtra por clave primaria para actualizar registro específico
        // Parámetros: 1=cantidad a sumar, 2=ID del producto
        String sqlStock = """
            UPDATE Producto SET stock = stock + ? WHERE producto_id = ?
            """;

        // SQL 2: Inserta registro histórico en tabla Inventario_Movimiento
        // Tabla: Inventario_Movimiento (bitácora de cambios de inventario)
        // Columnas insertadas:
        //   - producto_id: FK para vincular movimiento con producto específico
        //   - usuario_id: FK nullable para trazabilidad de responsable (puede ser NULL)
        //   - tipo: valor fijo 'entrada' para clasificar dirección del flujo de inventario
        //   - cantidad: unidades que ingresaron (positivo para entradas)
        //   - fecha: NOW() función de BD para timestamp automático del servidor
        //   - referencia: texto libre para contexto humano (ej: número de compra)
        String sqlMovimiento = """
            INSERT INTO Inventario_Movimiento
                (producto_id, usuario_id, tipo, cantidad, fecha, referencia)
            VALUES (?, ?, 'entrada', ?, NOW(), ?)
            """;

        // UPDATE de precio de costo: sobreescribe con el precio de la compra nueva
        // SQL 3: Actualiza precio_unitario en tabla Producto con costo más reciente
        // Tabla: Producto
        // Operación: SET precio_unitario = ? (sobrescribe valor anterior con nuevo costo)
        // WHERE producto_id = ?: filtra por PK para actualizar solo producto específico
        // Propósito: mantener costo actualizado para cálculos de margen y valuación de inventario
        String sqlPrecio = """
            UPDATE Producto SET precio_unitario = ? WHERE producto_id = ?
            """;

        // try-with-resources con múltiples PreparedStatement para gestión automática de recursos
        // Cada statement se prepara una vez y se reutiliza en el bucle para eficiencia
        try (PreparedStatement psS = con.prepareStatement(sqlStock);
             PreparedStatement psM = con.prepareStatement(sqlMovimiento);
             PreparedStatement psP = con.prepareStatement(sqlPrecio)) {

            // Itera sobre cada línea de detalle de la compra para procesar inventario
            for (DetalleCompra d : detalles) {

                // Paso 1: actualizar stock del producto con cantidad comprada
                // Bind parámetros para SQL de stock: 1=cantidad, 2=producto_id
                psS.setInt(1, d.getCantidad());
                psS.setInt(2, d.getProductoId());
                // Ejecuta UPDATE y captura número de filas afectadas para validación
                int filasActualizadas = psS.executeUpdate();

                // Validación de integridad: si no se actualizó ninguna fila, el producto no existe
                if (filasActualizadas == 0) {
                    throw new SQLException(
                        "No se pudo actualizar el stock del producto con ID "
                        + d.getProductoId() + ". Verifica que el producto exista en el sistema."
                    );
                }

                // Paso 2: actualizar precio de costo con el precio de esta compra
                // Bind parámetros para SQL de precio: 1=nuevo precio, 2=producto_id
                psP.setBigDecimal(1, d.getPrecioUnitario());
                psP.setInt(2, d.getProductoId());
                // Ejecuta UPDATE de precio; no se valida filas porque WHERE por PK garantiza 1 o 0
                psP.executeUpdate();

                // Paso 3: registrar movimiento de inventario para trazabilidad histórica
                // Bind parámetros para SQL de movimiento:
                //   1=producto_id, 2=usuario_id (o NULL), 3=cantidad, 4=referencia
                psM.setInt(1, d.getProductoId());
                // Manejo de usuario_id nullable: si es null se usa setNull con tipo SQL apropiado
                if (usuarioId != null) {
                    psM.setInt(2, usuarioId);
                } else {
                    psM.setNull(2, Types.INTEGER);
                }
                psM.setInt(3, d.getCantidad());
                psM.setString(4, referencia);
                // Ejecuta INSERT para crear registro histórico del movimiento de inventario
                psM.executeUpdate();
            }
        }
    }

    // ── Paso 1: cabecera de compra ─────────────────────────────────────────────

    /**
     * Inserta el registro de cabecera de compra en la base de datos y recupera el ID generado.
     *
     * Consulta SQL: INSERT en tabla Compra con retorno de claves generadas
     * Tabla destino: Compra (cabecera de órdenes de abastecimiento)
     * Columnas insertadas:
     *   - proveedor_id: FK hacia tabla Proveedor para identificar origen de la compra
     *   - fecha_compra: fecha en que se realizó el pedido (DATE)
     *   - fecha_entrega: fecha estimada o real de recepción de mercancía (DATE)
     * Configuración: Statement.RETURN_GENERATED_KEYS para recuperar compra_id autogenerado
     * Flujo de datos: tras executeUpdate(), se obtiene ResultSet con claves generadas
     * y se extrae el primer valor (compra_id) para usar en pasos subsiguientes de la transacción
     *
     * @param con conexión activa dentro de transacción en curso
     * @param c objeto Compra con proveedor_id, fecha_compra y fecha_entrega poblados
     * @return valor de {@code compra_id} generado automáticamente por la base de datos
     * @throws SQLException si no se pueden recuperar las claves generadas tras el INSERT
     */
    private int insertarCompra(Connection con, Compra c) throws SQLException {
        // SQL: INSERT para crear nueva cabecera de compra en tabla Compra
        // Tabla destino: Compra (entidad principal de órdenes de abastecimiento)
        // Columnas: proveedor_id (FK), fecha_compra (DATE), fecha_entrega (DATE)
        // Valores parametrizados (?, ?, ?) para seguridad y reutilización de plan de ejecución
        // Configuración RETURN_GENERATED_KEYS: permite recuperar compra_id autogenerado tras INSERT
        String sql = """
            INSERT INTO Compra (proveedor_id, fecha_compra, fecha_entrega)
            VALUES (?, ?, ?)
            """;
        // PreparedStatement con configuración para retorno de claves generadas (PK autogenerada)
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Bind parámetro 1: asigna proveedor_id al primer placeholder
            ps.setInt(1, c.getProveedorId());
            // Bind parámetro 2: convierte java.util.Date a java.sql.Date para fecha_compra
            ps.setDate(2, new java.sql.Date(c.getFechaCompra().getTime()));
            // Bind parámetro 3: convierte java.util.Date a java.sql.Date para fecha_entrega
            ps.setDate(3, new java.sql.Date(c.getFechaEntrega().getTime()));
            // Ejecuta INSERT; el número de filas afectadas se ignora porque se espera 1
            ps.executeUpdate();
            // Recupera ResultSet con claves generadas por la BD (compra_id autogenerado)
            try (ResultSet rs = ps.getGeneratedKeys()) {
                // next() avanza a la primera fila del resultado de claves generadas
                if (rs.next()) return rs.getInt(1);
                // Si no hay claves generadas, lanza excepción para detener transacción
                throw new SQLException("No se obtuvo el ID de la compra creada.");
            }
        }
    }

    // ── Paso 2: líneas de detalle ──────────────────────────────────────────────

    /**
     * Inserta múltiples líneas de detalle de compra mediante batch para eficiencia.
     *
     * Consulta SQL: INSERT en tabla Detalle_Compra ejecutado en lote (batch)
     * Tabla destino: Detalle_Compra (líneas de productos dentro de una compra)
     * Columnas insertadas por cada fila:
     *   - compra_id: FK hacia Compra para vincular detalle con cabecera
     *   - producto_id: FK hacia Producto para identificar artículo comprado
     *   - precio_unitario: costo unitario acordado con proveedor (DECIMAL)
     *   - cantidad: número de unidades adquiridas de este producto (INT)
     * Estrategia batch: se añade cada conjunto de parámetros con addBatch() y se ejecuta
     * una sola vez con executeBatch() para reducir round-trips a la base de datos
     *
     * @param con conexión activa dentro de transacción en curso
     * @param compraId ID de la cabecera de compra ya insertada (FK para detalles)
     * @param detalles lista de objetos DetalleCompra con productos, precios y cantidades
     * @throws SQLException si falla la ejecución del batch por errores de BD o constraints
     */
    private void insertarDetalles(Connection con, int compraId,
                                   List<DetalleCompra> detalles) throws SQLException {
        // SQL: INSERT para crear líneas de detalle en tabla Detalle_Compra
        // Tabla destino: Detalle_Compra (relación muchos-a-uno con Compra)
        // Columnas: compra_id (FK), producto_id (FK), precio_unitario (DECIMAL), cantidad (INT)
        // Valores parametrizados (?, ?, ?, ?) para seguridad y eficiencia en ejecución batch
        String sql = """
            INSERT INTO Detalle_Compra (compra_id, producto_id, precio_unitario, cantidad)
            VALUES (?, ?, ?, ?)
            """;
        // PreparedStatement reutilizable para ejecutar múltiples INSERTs en batch
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            // Itera sobre cada línea de detalle para preparar parámetros y añadir al batch
            for (DetalleCompra d : detalles) {
                // Bind parámetros para cada fila del batch:
                // 1=compra_id (mismo para todas las líneas), 2=producto_id, 3=precio, 4=cantidad
                ps.setInt(1, compraId);
                ps.setInt(2, d.getProductoId());
                ps.setBigDecimal(3, d.getPrecioUnitario());
                ps.setInt(4, d.getCantidad());
                // Añade conjunto actual de parámetros al batch para ejecución diferida
                ps.addBatch();
            }
            // Ejecuta todos los INSERTs del batch en una sola operación a la BD
            // Esto mejora rendimiento al reducir comunicaciones red/BD vs ejecutar uno por uno
            ps.executeBatch();
        }
    }

    // ── Paso 4: registro de pago ───────────────────────────────────────────────

    /**
     * Inserta el registro de pago asociado a la compra en la base de datos.
     *
     * Consulta SQL: INSERT en tabla Pago_Compra
     * Tabla destino: Pago_Compra (registros de pagos realizados por compras)
     * Columnas insertadas:
     *   - compra_id: FK hacia Compra para vincular pago con orden específica
     *   - metodo_pago_id: FK hacia Metodo_Pago para identificar forma de pago (efectivo, transferencia, etc.)
     *   - monto: importe del pago realizado (DECIMAL, puede ser anticipo o total)
     *   - estado: estado del pago ('confirmado' para contado, 'pendiente' para crédito)
     * Propósito: registrar trazabilidad financiera de la transacción de compra
     *
     * @param con conexión activa dentro de transacción en curso
     * @param compraId ID de la compra a la que pertenece este pago (FK)
     * @param metodoPagoId ID del método de pago utilizado (efectivo, tarjeta, etc.)
     * @param monto importe monetario del pago (anticipo si es crédito, total si es contado)
     * @param estado estado inicial del pago: {@code confirmado} para pagos completos,
     *               {@code pendiente} para anticipos en compras a crédito
     * @throws SQLException si falla el INSERT por errores de conexión o constraints de BD
     */
    private void insertarPagoCompra(Connection con, int compraId,
                                     int metodoPagoId, BigDecimal monto,
                                     String estado) throws SQLException {
        // SQL: INSERT para registrar pago en tabla Pago_Compra
        // Tabla destino: Pago_Compra (bitácora financiera de pagos por compras)
        // Columnas: compra_id (FK), metodo_pago_id (FK), monto (DECIMAL), estado (VARCHAR)
        // Valores parametrizados (?, ?, ?, ?) para seguridad y reutilización de plan
        String sql = """
            INSERT INTO Pago_Compra (compra_id, metodo_pago_id, monto, estado)
            VALUES (?, ?, ?, ?)
            """;
        // PreparedStatement para ejecutar INSERT con parámetros seguros
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            // Bind parámetros en orden: 1=compra_id, 2=metodo_pago_id, 3=monto, 4=estado
            ps.setInt(1, compraId);
            ps.setInt(2, metodoPagoId);
            ps.setBigDecimal(3, monto);
            ps.setString(4, estado);
            // Ejecuta INSERT para persistir registro de pago; se espera 1 fila afectada
            ps.executeUpdate();
        }
    }

    // ── Paso 5: crédito asociado ───────────────────────────────────────────────

    /**
     * Inserta el registro de crédito asociado a una compra a plazos y recupera su ID generado.
     *
     * Consulta SQL: INSERT en tabla Credito_Compra con retorno de claves generadas
     * Tabla destino: Credito_Compra (gestión de créditos otorgados por compras a proveedores)
     * Columnas insertadas:
     *   - compra_id: FK hacia Compra para vincular crédito con orden específica
     *   - monto_total: valor total de la compra que se financia a crédito (DECIMAL)
     *   - saldo_pendiente: monto restante por pagar después de anticipos (DECIMAL)
     *   - fecha_inicio: fecha de inicio del plazo del crédito (DATE)
     *   - fecha_vencimiento: fecha límite para liquidar el crédito (DATE)
     *   - estado: estado actual del crédito ('pendiente', 'pagado', 'vencido', etc.)
     * Configuración: Statement.RETURN_GENERATED_KEYS para recuperar credito_id autogenerado
     * Flujo de datos: tras executeUpdate(), se extrae credito_id del ResultSet para usar
     * en inserción subsiguiente de abonos iniciales si aplica
     *
     * @param con conexión activa dentro de transacción en curso
     * @param compraId ID de la compra asociada a este crédito (FK)
     * @param montoTotal valor total de la compra que se financia mediante crédito
     * @param saldoPendiente monto restante por pagar después de aplicar anticipos iniciales
     * @param fechaInicio fecha de inicio del plazo de pago del crédito
     * @param fechaVencimiento fecha límite contractual para liquidar el saldo pendiente
     * @param estado estado inicial del crédito según política de negocio
     * @return valor de {@code credito_id} generado automáticamente por la base de datos
     * @throws SQLException si no se pueden recuperar las claves generadas tras el INSERT
     */
    private int insertarCreditoCompra(Connection con, int compraId,
                                       BigDecimal montoTotal, BigDecimal saldoPendiente,
                                       java.util.Date fechaInicio, java.util.Date fechaVencimiento,
                                       String estado) throws SQLException {
        // SQL: INSERT para crear registro de crédito en tabla Credito_Compra
        // Tabla destino: Credito_Compra (gestión de financiamiento de compras a proveedores)
        // Columnas: compra_id (FK), monto_total, saldo_pendiente, fechas y estado
        // Valores parametrizados (?, ?, ?, ?, ?, ?) para seguridad y eficiencia
        // Configuración RETURN_GENERATED_KEYS: recupera credito_id autogenerado tras INSERT
        String sql = """
            INSERT INTO Credito_Compra
                (compra_id, monto_total, saldo_pendiente, fecha_inicio, fecha_vencimiento, estado)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        // PreparedStatement con configuración para retorno de claves generadas (PK autogenerada)
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Bind parámetros en orden definido en la consulta SQL:
            // 1=compra_id, 2=monto_total, 3=saldo_pendiente, 4-5=fechas, 6=estado
            ps.setInt(1, compraId);
            ps.setBigDecimal(2, montoTotal);
            ps.setBigDecimal(3, saldoPendiente);
            // Conversión de java.util.Date a java.sql.Date para compatibilidad con JDBC
            ps.setDate(4, new java.sql.Date(fechaInicio.getTime()));
            ps.setDate(5, new java.sql.Date(fechaVencimiento.getTime()));
            ps.setString(6, estado);
            // Ejecuta INSERT para persistir registro de crédito
            ps.executeUpdate();
            // Recupera ResultSet con claves generadas (credito_id autogenerado)
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                // Si no se obtiene clave generada, lanza excepción para detener transacción
                throw new SQLException("No se obtuvo el ID del crédito creado.");
            }
        }
    }

    // ── Paso 6: abono inicial del crédito ──────────────────────────────────────

    /**
     * Inserta el registro de abono inicial asociado a un crédito de compra.
     *
     * Consulta SQL: INSERT en tabla Abono_Credito
     * Tabla destino: Abono_Credito (registros de pagos parciales aplicados a créditos)
     * Columnas insertadas:
     *   - credito_id: FK hacia Credito_Compra para vincular abono con crédito específico
     *   - metodo_pago_id: FK hacia Metodo_Pago para identificar forma de pago del abono
     *   - monto_abono: importe monetario del abono realizado (DECIMAL)
     *   - estado: estado del abono ('confirmado' para pagos efectivos)
     * Propósito: registrar trazabilidad del primer pago (anticipo) aplicado al crédito
     *
     * @param con conexión activa dentro de transacción en curso
     * @param creditoId ID del crédito al que pertenece este abono inicial (FK)
     * @param metodoPagoId ID del método de pago utilizado para el abono
     * @param montoAbono importe monetario del abono inicial (generalmente anticipo)
     * @param estado estado del abono: {@code confirmado} para pagos efectivos
     * @throws SQLException si falla el INSERT por errores de conexión o constraints de BD
     */
    private void insertarAbonoCredito(Connection con, int creditoId,
                                       int metodoPagoId, BigDecimal montoAbono,
                                       String estado) throws SQLException {
        // SQL: INSERT para registrar abono inicial en tabla Abono_Credito
        // Tabla destino: Abono_Credito (bitácora de pagos parciales a créditos)
        // Columnas: credito_id (FK), metodo_pago_id (FK), monto_abono (DECIMAL), estado (VARCHAR)
        // Valores parametrizados (?, ?, ?, ?) para seguridad y reutilización de plan
        String sql = """
            INSERT INTO Abono_Credito (credito_id, metodo_pago_id, monto_abono, estado)
            VALUES (?, ?, ?, ?)
            """;
        // PreparedStatement para ejecutar INSERT con parámetros seguros
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            // Bind parámetros en orden: 1=credito_id, 2=metodo_pago_id, 3=monto_abono, 4=estado
            ps.setInt(1, creditoId);
            ps.setInt(2, metodoPagoId);
            ps.setBigDecimal(3, montoAbono);
            ps.setString(4, estado);
            // Ejecuta INSERT para persistir registro de abono; se espera 1 fila afectada
            ps.executeUpdate();
        }
    }

    // ── Consultas ──────────────────────────────────────────────────────────────

    /**
     * Busca y retorna una compra completa por su ID, incluyendo detalles, pago y crédito si aplica.
     *
     * Consulta SQL principal: SELECT con LEFT JOINs para recuperar cabecera con datos relacionados
     * Tablas involucradas:
     *   - Compra (alias 'c'): tabla principal con datos de cabecera de la orden
     *   - Pago_Compra (alias 'pc'): LEFT JOIN para obtener datos de pago si existen
     *   - Credito_Compra (alias 'cc'): LEFT JOIN para obtener datos de crédito si aplica
     * Tipo de JOIN: LEFT JOIN en ambos casos para conservar registro de compra incluso si
     * no tiene pago registrado o no es a crédito (campos relacionados pueden ser NULL)
     * Columnas seleccionadas:
     *   - c.compra_id, c.proveedor_id, c.fecha_compra, c.fecha_entrega: datos de cabecera
     *   - pc.metodo_pago_id, pc.monto AS monto_pago, pc.estado AS estado_pago: datos de pago
     *   - cc.credito_id, cc.monto_total, cc.saldo_pendiente, cc.fechas, cc.estado AS estado_credito: datos de crédito
     * Condición: WHERE c.compra_id = ? (búsqueda por PK, eficiente con índice)
     * Limitación: LIMIT 1 para garantizar máximo un resultado (búsqueda por clave única)
     *
     * Flujo de mapeo de datos:
     *   1. Recupera campos básicos de cabecera de Compra
     *   2. Si monto_total (de Credito_Compra) no es NULL → compra es a crédito:
     *      - Calcula anticipo como monto_total - saldo_pendiente
     *      - Carga fechas de vencimiento y estado del crédito
     *   3. Si monto_total es NULL → compra es al contado: usa monto_pago como total
     *   4. Llama a obtenerDetalles() para cargar líneas de producto asociadas
     *
     * @param compraId valor de {@code compra_id} a buscar
     * @return objeto {@link Compra} completamente poblado con cabecera, detalles y datos financieros,
     *         o {@code null} si no se encuentra la compra o hay error
     * @throws Exception si falla la consulta por errores de conexión o sintaxis SQL
     */
    public Compra obtenerPorId(int compraId) throws Exception {
        // SQL: SELECT con LEFT JOINs para recuperar compra completa con datos relacionados opcionales
        // Tabla principal: Compra (c) → datos de cabecera de la orden
        // LEFT JOIN Pago_Compra (pc) → datos de pago si existen (puede ser NULL si no hay pago registrado)
        // LEFT JOIN Credito_Compra (cc) → datos de crédito si aplica (NULL si compra es al contado)
        // WHERE c.compra_id = ?: filtro por PK para búsqueda eficiente de registro único
        // LIMIT 1: garantiza máximo un resultado aunque haya duplicados accidentales en JOINs
        String sql = """
            SELECT c.compra_id, c.proveedor_id,
                   c.fecha_compra, c.fecha_entrega,
                   pc.metodo_pago_id, pc.monto AS monto_pago, pc.estado AS estado_pago,
                   cc.credito_id, cc.monto_total, cc.saldo_pendiente,
                   cc.fecha_inicio, cc.fecha_vencimiento, cc.estado AS estado_credito
            FROM Compra c
            LEFT JOIN Pago_Compra    pc ON pc.compra_id = c.compra_id
            LEFT JOIN Credito_Compra cc ON cc.compra_id = c.compra_id
            WHERE c.compra_id = ?
            LIMIT 1
            """;
        // try-with-resources para gestión automática de Connection y PreparedStatement
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Bind parámetro: asigna compraId al primer placeholder (?) de la consulta
            ps.setInt(1, compraId);
            // Ejecuta consulta y obtiene ResultSet con máximo 1 fila (por PK + LIMIT 1)
            try (ResultSet rs = ps.executeQuery()) {
                // Si no hay resultados (compra no encontrada), retorna null inmediatamente
                if (!rs.next()) return null;

                // Crea instancia de Compra para mapear datos recuperados
                Compra compra = new Compra();
                // Mapea campos básicos de cabecera desde tabla Compra
                compra.setCompraId(rs.getInt("compra_id"));
                compra.setProveedorId(rs.getInt("proveedor_id"));
                compra.setFechaCompra(rs.getDate("fecha_compra"));
                compra.setFechaEntrega(rs.getDate("fecha_entrega"));
                // Mapea datos de pago desde Pago_Compra (pueden ser NULL si no hay pago)
                compra.setMetodoPagoId(rs.getInt("metodo_pago_id"));
                compra.setTotal(rs.getBigDecimal("monto_pago"));

                // Lógica de negocio: determina si es compra a crédito según presencia de monto_total
                BigDecimal montoTotal = rs.getBigDecimal("monto_total");
                if (montoTotal != null) {
                    // Si monto_total existe → compra es a crédito: carga datos específicos
                    compra.setEsCredito(true);
                    compra.setTotal(montoTotal);
                    // Calcula anticipo como diferencia entre total y saldo pendiente
                    BigDecimal saldo = rs.getBigDecimal("saldo_pendiente");
                    compra.setAnticipo(saldo != null ? montoTotal.subtract(saldo) : BigDecimal.ZERO);
                    // Carga fechas y estado del crédito desde Credito_Compra
                    compra.setFechaVencimiento(rs.getDate("fecha_vencimiento"));
                    compra.setEstadoCredito(rs.getString("estado_credito"));
                }
                // Carga líneas de detalle mediante consulta separada (relación uno-a-muchos)
                compra.setDetalles(obtenerDetalles(con, compraId));
                // Retorna objeto Compra completamente poblado
                return compra;
            }
        }
    }

    /**
     * Retorna las líneas de detalle de productos para una compra específica.
     *
     * Consulta SQL: SELECT con INNER JOIN para recuperar detalles con nombres de producto
     * Tablas involucradas:
     *   - Detalle_Compra (alias 'dc'): tabla de líneas de compra con cantidades y precios
     *   - Producto (alias 'p'): tabla maestra para obtener nombre legible del producto
     * Tipo de JOIN: INNER JOIN para garantizar que solo se retornen detalles con producto válido
     * Condición del JOIN: p.producto_id = dc.producto_id (relación FK desde detalle hacia producto)
     * Condición WHERE: dc.compra_id = ? (filtra detalles pertenecientes a compra específica)
     * Columnas seleccionadas:
     *   - dc.detalle_compra_id: PK de la línea de detalle para identificación única
     *   - dc.producto_id: FK hacia Producto para vincular con catálogo
     *   - dc.precio_unitario: costo unitario acordado en esta compra
     *   - dc.cantidad: número de unidades adquiridas de este producto
     *   - p.nombre AS nombre_producto: nombre legible desde tabla Producto para presentación en UI
     *
     * Flujo de mapeo:
     *   - Itera sobre ResultSet creando objeto DetalleCompra por cada fila
     *   - Mapea campos directos desde Detalle_Compra
     *   - Mapea nombre_producto desde JOIN con Producto para legibilidad
     *   - Calcula subtotal como precio_unitario * cantidad para conveniencia en UI/reportes
     *
     * @param con conexión activa de base de datos (puede ser misma transacción)
     * @param compraId ID de la compra cuyos detalles se requieren recuperar
     * @return lista de objetos DetalleCompra con datos de producto, precios y cantidades
     * @throws SQLException si falla la consulta por errores de conexión o sintaxis SQL
     */
    private List<DetalleCompra> obtenerDetalles(Connection con, int compraId) throws SQLException {
        // SQL: SELECT con INNER JOIN para recuperar líneas de detalle con nombres de producto
        // Tabla principal: Detalle_Compra (dc) → líneas de compra con precios y cantidades
        // INNER JOIN Producto (p) → para obtener nombre legible del producto desde catálogo
        // Condición JOIN: p.producto_id = dc.producto_id (relación FK para vincular tablas)
        // WHERE dc.compra_id = ?: filtra solo detalles pertenecientes a compra específica
        // Columnas: detalle_compra_id, producto_id, precio_unitario, cantidad, nombre_producto (alias)
        String sql = """
            SELECT dc.detalle_compra_id, dc.producto_id, dc.precio_unitario, dc.cantidad,
                   p.nombre AS nombre_producto
            FROM Detalle_Compra dc
            JOIN Producto p ON p.producto_id = dc.producto_id
            WHERE dc.compra_id = ?
            """;
        // Lista que almacenará cada línea de detalle recuperada de la consulta
        List<DetalleCompra> lista = new ArrayList<>();
        // PreparedStatement para ejecutar consulta parametrizada de forma segura
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            // Bind parámetro: asigna compraId al primer placeholder (?) para filtrar detalles
            ps.setInt(1, compraId);
            // Ejecuta consulta y obtiene ResultSet con todas las líneas de la compra
            try (ResultSet rs = ps.executeQuery()) {
                // Itera sobre cada fila del resultado para mapearla a objeto DetalleCompra
                while (rs.next()) {
                    // Crea nueva instancia de DetalleCompra para la fila actual
                    DetalleCompra d = new DetalleCompra();
                    // Mapea campos directos desde tabla Detalle_Compra
                    d.setDetalleCompraId(rs.getInt("detalle_compra_id"));
                    d.setProductoId(rs.getInt("producto_id"));
                    // Mapea nombre_producto desde JOIN con tabla Producto (legibilidad para UI)
                    d.setProductoNombre(rs.getString("nombre_producto"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    d.setCantidad(rs.getInt("cantidad"));
                    // Calcula subtotal como precio_unitario * cantidad para conveniencia en presentación
                    d.setSubtotal(d.getPrecioUnitario()
                            .multiply(new BigDecimal(d.getCantidad())));
                    // Agrega objeto poblado a lista de retorno
                    lista.add(d);
                }
            }
        }
        // Retorna lista de detalles; si no hay líneas o hay error, retorna lista vacía (nunca null)
        return lista;
    }

    /**
     * Retorna todas las compras asociadas a un proveedor específico, ordenadas por fecha descendente.
     *
     * Consulta SQL: SELECT con LEFT JOINs y COALESCE para calcular total real de cada compra
     * Tablas involucradas:
     *   - Compra (alias 'c'): tabla principal con cabeceras de órdenes de abastecimiento
     *   - Pago_Compra (alias 'pc'): LEFT JOIN para obtener montos de pago si existen
     *   - Credito_Compra (alias 'cc'): LEFT JOIN para obtener montos de crédito si aplica
     * Tipo de JOIN: LEFT JOIN en ambos casos para conservar compras incluso si no tienen
     * registro de pago o no son a crédito (campos relacionados pueden ser NULL)
     * Condición WHERE: c.proveedor_id = ? (filtra compras por proveedor específico)
     * Columnas seleccionadas:
     *   - c.compra_id, c.proveedor_id, c.fecha_compra, c.fecha_entrega: datos de cabecera
     *   - COALESCE(cc.monto_total, pc.monto, 0) AS total_real: lógica para determinar monto total:
     *     • Si hay crédito: usa cc.monto_total (valor financiado completo)
     *     • Si no hay crédito pero hay pago: usa pc.monto (pago al contado)
     *     • Si no hay ninguno: usa 0 como valor por defecto
     * Ordenamiento: ORDER BY c.fecha_compra DESC para mostrar compras más recientes primero
     *
     * Flujo de mapeo:
     *   - Crea objeto Compra por cada fila del ResultSet con datos de cabecera
     *   - Asigna total_real calculado con COALESCE como monto total de la compra
     *   - Llama a obtenerDetalles() para cargar líneas de producto asociadas a cada compra
     *
     * @param proveedorId ID del proveedor para filtrar compras asociadas
     * @return lista de objetos Compra con cabecera y detalles cargados, ordenados por fecha descendente
     * @throws Exception si falla la consulta por errores de conexión o sintaxis SQL
     */
    public List<Compra> listarPorProveedor(int proveedorId) throws Exception {
        // SQL: SELECT con LEFT JOINs y COALESCE para listar compras de proveedor con cálculo de total
        // Tabla principal: Compra (c) → cabeceras de órdenes de abastecimiento
        // LEFT JOIN Pago_Compra (pc) → datos de pago si existen (puede ser NULL)
        // LEFT JOIN Credito_Compra (cc) → datos de crédito si aplica (puede ser NULL)
        // WHERE c.proveedor_id = ?: filtra compras por proveedor específico (parámetro bindado)
        // COALESCE(cc.monto_total, pc.monto, 0) AS total_real:
        //   - Prioriza monto_total de crédito si existe (compra a crédito)
        //   - Si no, usa monto de pago (compra al contado)
        //   - Si ninguno existe, usa 0 como fallback seguro
        // ORDER BY c.fecha_compra DESC: presenta compras más recientes primero para mejor UX
        String sql = """
            SELECT c.compra_id, c.proveedor_id,
                   c.fecha_compra, c.fecha_entrega,
                   COALESCE(cc.monto_total, pc.monto, 0) AS total_real
            FROM Compra c
            LEFT JOIN Pago_Compra    pc ON pc.compra_id = c.compra_id
            LEFT JOIN Credito_Compra cc ON cc.compra_id = c.compra_id
            WHERE c.proveedor_id = ?
            ORDER BY c.fecha_compra DESC
            """;
        // Lista que almacenará cada compra recuperada de la consulta
        List<Compra> lista = new ArrayList<>();
        // try-with-resources para gestión automática de Connection y PreparedStatement
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Bind parámetro: asigna proveedorId al primer placeholder (?) para filtrar por proveedor
            ps.setInt(1, proveedorId);
            // Ejecuta consulta y obtiene ResultSet con todas las compras del proveedor
            try (ResultSet rs = ps.executeQuery()) {
                // Itera sobre cada fila del resultado para mapearla a objeto Compra
                while (rs.next()) {
                    // Crea nueva instancia de Compra para la fila actual
                    Compra c = new Compra();
                    // Mapea campos básicos de cabecera desde tabla Compra
                    c.setCompraId(rs.getInt("compra_id"));
                    c.setProveedorId(rs.getInt("proveedor_id"));
                    c.setFechaCompra(rs.getDate("fecha_compra"));
                    c.setFechaEntrega(rs.getDate("fecha_entrega"));
                    // Recupera total_real calculado con COALESCE en la consulta SQL
                    BigDecimal total = rs.getBigDecimal("total_real");
                    // Asigna total con fallback a cero si es NULL (seguridad ante datos inconsistentes)
                    c.setTotal(total != null ? total : BigDecimal.ZERO);
                    // Carga líneas de detalle mediante consulta separada (relación uno-a-muchos)
                    c.setDetalles(obtenerDetalles(con, c.getCompraId()));
                    // Agrega objeto Compra poblado a lista de retorno
                    lista.add(c);
                }
            }
        }
        // Retorna lista de compras; si no hay datos o hay error, retorna lista vacía (nunca null)
        return lista;
    }

    /**
     * Elimina una compra completa de la base de datos.
     *
     * Consulta SQL: DELETE en tabla Compra con condición por clave primaria
     * Tabla afectada: Compra (cabecera de órdenes de abastecimiento)
     * Condición: WHERE compra_id = ? (elimina solo el registro con PK específica)
     * Comportamiento CASCADE: las tablas Detalle_Compra, Pago_Compra y Credito_Compra
     * tienen constraints ON DELETE CASCADE configurados en la base de datos, por lo que
     * al eliminar la cabecera se eliminan automáticamente todos los registros relacionados
     * sin necesidad de operaciones adicionales en código Java
     * Retorno: true si se eliminó una fila (compra existente), false si no se encontró
     *
     * @param compraId valor de {@code compra_id} de la compra a eliminar
     * @return {@code true} si se eliminó exitosamente una fila, {@code false} si no existía el registro
     * @throws Exception si falla el DELETE por errores de conexión, constraints o permisos de BD
     */
    public boolean eliminarConTransaccion(int compraId) throws Exception {
        // SQL: DELETE para remover registro de cabecera en tabla Compra
        // Tabla afectada: Compra (entidad principal de órdenes de abastecimiento)
        // WHERE compra_id = ?: condición parametrizada para eliminar solo registro específico
        // Comportamiento CASCADE: registros en Detalle_Compra, Pago_Compra y Credito_Compra
        // se eliminan automáticamente por constraints de BD configurados con ON DELETE CASCADE
        String sql = "DELETE FROM Compra WHERE compra_id = ?";
        // try-with-resources para gestión automática de Connection y PreparedStatement
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Bind parámetro: asigna compraId al primer placeholder (?) de la consulta
            ps.setInt(1, compraId);
            // Ejecuta DELETE y retorna true si se eliminó al menos una fila (registro encontrado)
            return ps.executeUpdate() > 0;
        }
    }
}