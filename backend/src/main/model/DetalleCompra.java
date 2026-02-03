package main.model;

/**
 * Clase modelo para la tabla Detalle_Compra
 * Representa cada producto comprado a un proveedor
 */
public class DetalleCompra {
    
    // Atributos
    private int detalleCompraId;
    private int compraId;
    private int productoId;
    private double precioUnitario;
    private int cantidad;
    private double subtotal;
    
    // Objeto relacionado (para joins)
    private Producto producto;
    
    // Constructor vacío
    public DetalleCompra() {
    }
    
    // Constructor con campos principales
    public DetalleCompra(int compraId, int productoId, double precioUnitario, int cantidad) {
        this.compraId = compraId;
        this.productoId = productoId;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.subtotal = cantidad * precioUnitario;
    }
    
    // Constructor completo
    public DetalleCompra(int detalleCompraId, int compraId, int productoId, 
                        double precioUnitario, int cantidad, double subtotal) {
        this.detalleCompraId = detalleCompraId;
        this.compraId = compraId;
        this.productoId = productoId;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }
    
    // Getters y Setters
    public int getDetalleCompraId() {
        return detalleCompraId;
    }
    
    public void setDetalleCompraId(int detalleCompraId) {
        this.detalleCompraId = detalleCompraId;
    }
    
    public int getCompraId() {
        return compraId;
    }
    
    public void setCompraId(int compraId) {
        this.compraId = compraId;
    }
    
    public int getProductoId() {
        return productoId;
    }
    
    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }
    
    public double getPrecioUnitario() {
        return precioUnitario;
    }
    
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }
    
    public int getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
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
        return "DetalleCompra{" +
                "detalleCompraId=" + detalleCompraId +
                ", compraId=" + compraId +
                ", productoId=" + productoId +
                ", precioUnitario=" + precioUnitario +
                ", cantidad=" + cantidad +
                ", subtotal=" + subtotal +
                '}';
    }
}