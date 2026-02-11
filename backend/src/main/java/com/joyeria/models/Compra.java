package main.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase modelo para la tabla Compra
 * Representa una compra realizada a un proveedor
 */
public class Compra {
    
    // Atributos
    private int compraId;
    private int usuarioProveedorId;
    private LocalDate fechaCompra;
    private LocalDate fechaEntrega;
    private double total;
    
    // Objetos relacionados (para joins)
    private Usuario proveedor;
    private List<DetalleCompra> detalles;
    
    // Constructor vacío
    public Compra() {
        this.fechaCompra = LocalDate.now();
        this.total = 0.0;
        this.detalles = new ArrayList<>();
    }
    
    // Constructor con campos principales
    public Compra(int usuarioProveedorId, LocalDate fechaEntrega, double total) {
        this();
        this.usuarioProveedorId = usuarioProveedorId;
        this.fechaEntrega = fechaEntrega;
        this.total = total;
    }
    
    // Constructor completo
    public Compra(int compraId, int usuarioProveedorId, LocalDate fechaCompra, 
                 LocalDate fechaEntrega, double total) {
        this.compraId = compraId;
        this.usuarioProveedorId = usuarioProveedorId;
        this.fechaCompra = fechaCompra;
        this.fechaEntrega = fechaEntrega;
        this.total = total;
        this.detalles = new ArrayList<>();
    }
    
    // Getters y Setters
    public int getCompraId() {
        return compraId;
    }
    
    public void setCompraId(int compraId) {
        this.compraId = compraId;
    }
    
    public int getUsuarioProveedorId() {
        return usuarioProveedorId;
    }
    
    public void setUsuarioProveedorId(int usuarioProveedorId) {
        this.usuarioProveedorId = usuarioProveedorId;
    }
    
    public LocalDate getFechaCompra() {
        return fechaCompra;
    }
    
    public void setFechaCompra(LocalDate fechaCompra) {
        this.fechaCompra = fechaCompra;
    }
    
    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }
    
    public void setFechaEntrega(LocalDate fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }
    
    public double getTotal() {
        return total;
    }
    
    public void setTotal(double total) {
        this.total = total;
    }
    
    public Usuario getProveedor() {
        return proveedor;
    }
    
    public void setProveedor(Usuario proveedor) {
        this.proveedor = proveedor;
    }
    
    public List<DetalleCompra> getDetalles() {
        return detalles;
    }
    
    public void setDetalles(List<DetalleCompra> detalles) {
        this.detalles = detalles;
    }
    
    // Métodos auxiliares
    
    /**
     * Agrega un detalle a la compra
     */
    public void agregarDetalle(DetalleCompra detalle) {
        this.detalles.add(detalle);
    }
    
    /**
     * Calcula el total de la compra sumando todos los detalles
     */
    public void calcularTotal() {
        this.total = detalles.stream()
                            .mapToDouble(DetalleCompra::getSubtotal)
                            .sum();
    }
    
    /**
     * Verifica si la compra está pendiente de entrega
     */
    public boolean isPendienteEntrega() {
        return LocalDate.now().isBefore(fechaEntrega);
    }
    
    // Método toString
    @Override
    public String toString() {
        return "Compra{" +
                "compraId=" + compraId +
                ", usuarioProveedorId=" + usuarioProveedorId +
                ", fechaCompra=" + fechaCompra +
                ", fechaEntrega=" + fechaEntrega +
                ", total=" + total +
                '}';
    }
}