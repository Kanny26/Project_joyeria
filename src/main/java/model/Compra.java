package model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Modelo Compra — refleja la tabla Compra + datos auxiliares de:
 *   Pago_Compra     (metodoPagoId, total como monto pagado, esCredito)
 *   Credito_Compra  (anticipo, fechaVencimiento, estadoCredito)
 *
 * Estos campos extra NO están en la tabla Compra; los usa el DAO
 * para distribuir la información en las tablas correctas.
 */
public class Compra {

    // ── Tabla Compra ──────────────────────────────────────────────────
    private int    compraId;
    private int    proveedorId;
    private Date   fechaCompra;
    private Date   fechaEntrega;

    // ── Tabla Pago_Compra ─────────────────────────────────────────────
    /** FK a Metodo_Pago */
    private int        metodoPagoId;
    /** Monto total de la compra (suma de subtotales) */
    private BigDecimal total;
    /** true si el pago es a crédito (genera registro en Credito_Compra) */
    private boolean    esCredito;

    // ── Tabla Credito_Compra (solo si esCredito = true) ───────────────
    /** Monto pagado por adelantado al registrar la compra */
    private BigDecimal anticipo       = BigDecimal.ZERO;
    /** Fecha límite para saldar la deuda */
    private Date       fechaVencimiento;
    /** "activo" | "pagado" | "vencido" */
    private String     estadoCredito  = "activo";

    // ── Detalles (Detalle_Compra) ─────────────────────────────────────
    private List<DetalleCompra> detalles;

    // ── Getters / Setters ─────────────────────────────────────────────

    public int getCompraId()                        { return compraId; }
    public void setCompraId(int compraId)           { this.compraId = compraId; }

    public int getProveedorId()                     { return proveedorId; }
    public void setProveedorId(int proveedorId)     { this.proveedorId = proveedorId; }

    public Date getFechaCompra()                    { return fechaCompra; }
    public void setFechaCompra(Date fechaCompra)    { this.fechaCompra = fechaCompra; }

    public Date getFechaEntrega()                   { return fechaEntrega; }
    public void setFechaEntrega(Date fechaEntrega)  { this.fechaEntrega = fechaEntrega; }

    public int getMetodoPagoId()                    { return metodoPagoId; }
    public void setMetodoPagoId(int metodoPagoId)   { this.metodoPagoId = metodoPagoId; }

    public BigDecimal getTotal()                    { return total; }
    public void setTotal(BigDecimal total)          { this.total = total; }

    public boolean isEsCredito()                    { return esCredito; }
    public void setEsCredito(boolean esCredito)     { this.esCredito = esCredito; }

    public BigDecimal getAnticipo()                 { return anticipo; }
    public void setAnticipo(BigDecimal anticipo)    { this.anticipo = anticipo; }

    public Date getFechaVencimiento()               { return fechaVencimiento; }
    public void setFechaVencimiento(Date d)         { this.fechaVencimiento = d; }

    public String getEstadoCredito()                { return estadoCredito; }
    public void setEstadoCredito(String e)          { this.estadoCredito = e; }

    public List<DetalleCompra> getDetalles()                    { return detalles; }
    public void setDetalles(List<DetalleCompra> detalles)       { this.detalles = detalles; }

    // ── Campos calculados (útiles en JSP/vistas) ──────────────────────

    /**
     * Saldo que queda pendiente con el proveedor.
     * = total - anticipo  (0 si es contado o ya está pagado)
     */
    public BigDecimal getSaldoPendiente() {
        if (!esCredito || "pagado".equalsIgnoreCase(estadoCredito)) return BigDecimal.ZERO;
        if (total == null) return BigDecimal.ZERO;
        BigDecimal ant = (anticipo != null) ? anticipo : BigDecimal.ZERO;
        return total.subtract(ant).max(BigDecimal.ZERO);
    }

    /** true si queda algún saldo pendiente con el proveedor */
    public boolean isDeuda() {
        return getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0;
    }
}