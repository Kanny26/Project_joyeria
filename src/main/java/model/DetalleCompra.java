package model;

import java.math.BigDecimal;

public class DetalleCompra {

    private int        detalleCompraId;
    private int        compraId;
    private int        productoId;
    private String     productoNombre;
    private BigDecimal precioUnitario;
    private int        cantidad;
    private BigDecimal subtotal;

    public DetalleCompra() {}

    // ── Getters & Setters ──────────────────────────
    public int getDetalleCompraId()                       { return detalleCompraId; }
    public void setDetalleCompraId(int detalleCompraId)   { this.detalleCompraId = detalleCompraId; }

    public int getCompraId()                  { return compraId; }
    public void setCompraId(int compraId)     { this.compraId = compraId; }

    public int getProductoId()                { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public String getProductoNombre()                   { return productoNombre; }
    public void setProductoNombre(String productoNombre){ this.productoNombre = productoNombre; }

    public BigDecimal getPrecioUnitario()                     { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario)  { this.precioUnitario = precioUnitario; }

    public int getCantidad()              { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public BigDecimal getSubtotal()                 { return subtotal; }
    public void setSubtotal(BigDecimal subtotal)    { this.subtotal = subtotal; }
}