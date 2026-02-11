package main.model;

/**
 * Clase modelo para la tabla Detalle_Venta
 * Representa cada producto vendido en una venta
 */
public class DetalleVenta {
    
    // Atributos
    private int detalleVentaId;
    private int ventaId;
    private int productoId;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
    
    // Objeto relacionado (para joins)
    private Producto producto;
    
    // Constructor vacío
    public DetalleVenta() {
    }
    
    // Constructor con campos principales
    public DetalleVenta(int ventaId, int productoId, int cantidad, double precioUnitario) {
        this.ventaId = ventaId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = cantidad * precioUnitario;
    }
    
    // Constructor completo
    public DetalleVenta(int detalleVentaId, int ventaId, int productoId, 
                       int cantidad, double precioUnitario, double subtotal) {
        this.detalleVentaId = detalleVentaId;
        this.ventaId = ventaId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }
    
    // Getters y Setters
    public int getDetalleVentaId() {
        return detalleVentaId;
    }
    
    public void setDetalleVentaId(int detalleVentaId) {
        this.detalleVentaId = detalleVentaId;
    }
    
    public int getVentaId() {
        return ventaId;
    }
    
    public void setVentaId(int ventaId) {
        this.ventaId = ventaId;
    }
    
    public int getProductoId() {
        return productoId;
    }
    
    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }
    
    public int getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        calcularSubtotal();
    }
    
    public double getPrecioUnitario() {
        return precioUnitario;
    }
    
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }
    
    public double getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
    
    public Producto getProducto() {
        return producto;
    }
    
    public void setProducto(Producto producto) {
        this.producto = producto;
    }
    
    // Métodos auxiliares
    
    /**
     * Calcula el subtotal automáticamente
     */
    private void calcularSubtotal() {
        this.subtotal = this.cantidad * this.precioUnitario;
    }
    
    // Método toString
    @Override
    public String toString() {
        return "DetalleVenta{" +
                "detalleVentaId=" + detalleVentaId +
                ", ventaId=" + ventaId +
                ", productoId=" + productoId +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", subtotal=" + subtotal +
                '}';
    }
}