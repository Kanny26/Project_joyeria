package model;

import java.math.BigDecimal;
import java.util.Date;

public class PagoVenta {
    private int pagoVentaId;
    private int ventaId;
    private int metodoPagoId;
    private BigDecimal monto;
    private Date fecha;
    private String estado;  // pendiente, confirmado, rechazado
    private String tipo;    // anticipo, saldo, completo

    public PagoVenta() {}

    public PagoVenta(int ventaId, int metodoPagoId, BigDecimal monto, String tipo) {
        this.ventaId = ventaId;
        this.metodoPagoId = metodoPagoId;
        this.monto = monto;
        this.fecha = new Date();
        this.tipo = tipo;
        this.estado = "pendiente";
    }

    // Getters y Setters
    public int getPagoVentaId() { return pagoVentaId; }
    public void setPagoVentaId(int pagoVentaId) { this.pagoVentaId = pagoVentaId; }

    public int getVentaId() { return ventaId; }
    public void setVentaId(int ventaId) { this.ventaId = ventaId; }

    public int getMetodoPagoId() { return metodoPagoId; }
    public void setMetodoPagoId(int metodoPagoId) { this.metodoPagoId = metodoPagoId; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}