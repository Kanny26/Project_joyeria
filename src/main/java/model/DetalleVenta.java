package model;

import java.math.BigDecimal;

/**
 * Representa una línea dentro de una venta: qué producto se vendió,
 * en qué cantidad y a qué precio.
 * Corresponde a la tabla Detalle_Venta en la base de datos.
 */
public class DetalleVenta {

    private int detalleVentaId;
    private int ventaId;
    private int productoId;
    private String productoNombre;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    // Se guarda el stock actual del producto para poder mostrar disponibilidad en la vista
    // y validar que no se pida más de lo que hay.
    private int stockDisponible;

    public DetalleVenta() {}

    /**
     * Constructor de conveniencia que se usa al armar el detalle antes de guardar la venta.
     * Calcula el subtotal automáticamente multiplicando precio × cantidad.
     */
    public DetalleVenta(int productoId, String productoNombre,
                        int cantidad, BigDecimal precioUnitario, int stockDisponible) {
        this.productoId      = productoId;
        this.productoNombre  = productoNombre;
        this.cantidad        = cantidad;
        this.precioUnitario  = precioUnitario;
        this.stockDisponible = stockDisponible;
        // El subtotal se precalcula aquí para no tener que recalcularlo después
        this.subtotal        = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    // ── Getters y Setters ──────────────────────────────────────────────────────
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
