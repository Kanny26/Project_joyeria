package model;

import java.math.BigDecimal;

/**
 * Detalle de una línea de venta (Detalle_Venta en BD)
 */
public class DetalleVenta {

    private int detalleVentaId;
    private int ventaId;
    private int productoId;
    private String productoNombre;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private int stockDisponible;   // stock actual del producto (para validaciones en vista)

    // ── Constructores ──────────────────────────────────────────
    public DetalleVenta() {}

    /**
     * Constructor de conveniencia usado al armar la venta antes de persistir.
     */
    public DetalleVenta(int productoId, String productoNombre,
                        int cantidad, BigDecimal precioUnitario, int stockDisponible) {
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.stockDisponible = stockDisponible;
        this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    // ── Getters / Setters ──────────────────────────────────────
    public int getDetalleVentaId()                     { return detalleVentaId; }
    public void setDetalleVentaId(int id)              { this.detalleVentaId = id; }

    public int getVentaId()                            { return ventaId; }
    public void setVentaId(int ventaId)                { this.ventaId = ventaId; }

    public int getProductoId()                         { return productoId; }
    public void setProductoId(int productoId)          { this.productoId = productoId; }

    public String getProductoNombre()                  { return productoNombre; }
    public void setProductoNombre(String n)            { this.productoNombre = n; }

    public int getCantidad()                           { return cantidad; }
    public void setCantidad(int cantidad)              { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario()              { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal p)        { this.precioUnitario = p; }

    public BigDecimal getSubtotal()                    { return subtotal; }
    public void setSubtotal(BigDecimal subtotal)       { this.subtotal = subtotal; }

    public int getStockDisponible()                    { return stockDisponible; }
    public void setStockDisponible(int stock)          { this.stockDisponible = stock; }
}