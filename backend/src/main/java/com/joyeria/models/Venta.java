package main.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase modelo para la tabla venta_factura
 * Representa una venta realizada en el sistema
 */
public class Venta {
    
    // Atributos
    private int ventaId;
    private int usuarioId;          // ID del vendedor
    private int usuarioClienteId;   // ID del cliente
    private LocalDate fechaEmision;
    private double total;
    
    // Objetos relacionados (para joins)
    private Usuario vendedor;
    private Usuario cliente;
    private List<DetalleVenta> detalles;
    
    // Constructor vacío
    public Venta() {
        this.fechaEmision = LocalDate.now();
        this.total = 0.0;
        this.detalles = new ArrayList<>();
    }
    
    // Constructor con campos principales
    public Venta(int usuarioId, int usuarioClienteId, double total) {
        this();
        this.usuarioId = usuarioId;
        this.usuarioClienteId = usuarioClienteId;
        this.total = total;
    }
    
    // Constructor completo
    public Venta(int ventaId, int usuarioId, int usuarioClienteId, 
                LocalDate fechaEmision, double total) {
        this.ventaId = ventaId;
        this.usuarioId = usuarioId;
        this.usuarioClienteId = usuarioClienteId;
        this.fechaEmision = fechaEmision;
        this.total = total;
        this.detalles = new ArrayList<>();
    }
    
    // Getters y Setters
    public int getVentaId() {
        return ventaId;
    }
    
    public void setVentaId(int ventaId) {
        this.ventaId = ventaId;
    }
    
    public int getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public int getUsuarioClienteId() {
        return usuarioClienteId;
    }
    
    public void setUsuarioClienteId(int usuarioClienteId) {
        this.usuarioClienteId = usuarioClienteId;
    }
    
    public LocalDate getFechaEmision() {
        return fechaEmision;
    }
    
    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }
    
    public double getTotal() {
        return total;
    }
    
    public void setTotal(double total) {
        this.total = total;
    }
    
    public Usuario getVendedor() {
        return vendedor;
    }
    
    public void setVendedor(Usuario vendedor) {
        this.vendedor = vendedor;
    }
    
    public Usuario getCliente() {
        return cliente;
    }
    
    public void setCliente(Usuario cliente) {
        this.cliente = cliente;
    }
    
    public List<DetalleVenta> getDetalles() {
        return detalles;
    }
    
    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }
    
    // Métodos auxiliares
    
    /**
     * Agrega un detalle a la venta
     */
    public void agregarDetalle(DetalleVenta detalle) {
        this.detalles.add(detalle);
    }
    
    /**
     * Calcula el total de la venta sumando todos los detalles
     */
    public void calcularTotal() {
        this.total = detalles.stream()
                            .mapToDouble(DetalleVenta::getSubtotal)
                            .sum();
    }
    
    // Método toString
    @Override
    public String toString() {
        return "Venta{" +
                "ventaId=" + ventaId +
                ", usuarioId=" + usuarioId +
                ", usuarioClienteId=" + usuarioClienteId +
                ", fechaEmision=" + fechaEmision +
                ", total=" + total +
                '}';
    }
}