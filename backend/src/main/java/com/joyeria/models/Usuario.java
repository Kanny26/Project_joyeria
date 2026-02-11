package main.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Clase modelo para la tabla Usuario
 * Representa un usuario del sistema (puede ser vendedor, administrador, proveedor o cliente)
 */
public class Usuario {
    
    // Atributos principales
    private int usuarioId;
    private String nombre;
    private String pass;
    private boolean estado;
    private LocalDateTime fechaCreacion;
    
    // Atributos opcionales según el rol
    private String documento;
    private LocalDate fechaRegistro; // Para clientes
    private LocalDate fechaInicio;   // Para proveedores
    private Double minimoCompra;     // Para proveedores
    
    // Constructor vacío
    public Usuario() {
        this.estado = true; // Por defecto activo
        this.fechaCreacion = LocalDateTime.now();
    }
    
    // Constructor con campos principales
    public Usuario(String nombre, String pass) {
        this();
        this.nombre = nombre;
        this.pass = pass;
    }
    
    // Constructor completo
    public Usuario(int usuarioId, String nombre, String pass, boolean estado, 
                   LocalDateTime fechaCreacion, String documento, 
                   LocalDate fechaRegistro, LocalDate fechaInicio, Double minimoCompra) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.pass = pass;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.documento = documento;
        this.fechaRegistro = fechaRegistro;
        this.fechaInicio = fechaInicio;
        this.minimoCompra = minimoCompra;
    }
    
    // Getters y Setters
    public int getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getPass() {
        return pass;
    }
    
    public void setPass(String pass) {
        this.pass = pass;
    }
    
    public boolean isEstado() {
        return estado;
    }
    
    public void setEstado(boolean estado) {
        this.estado = estado;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public String getDocumento() {
        return documento;
    }
    
    public void setDocumento(String documento) {
        this.documento = documento;
    }
    
    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }
    
    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }
    
    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
    
    public Double getMinimoCompra() {
        return minimoCompra;
    }
    
    public void setMinimoCompra(Double minimoCompra) {
        this.minimoCompra = minimoCompra;
    }
    
    // Método toString para debugging
    @Override
    public String toString() {
        return "Usuario{" +
                "usuarioId=" + usuarioId +
                ", nombre='" + nombre + '\'' +
                ", estado=" + estado +
                ", fechaCreacion=" + fechaCreacion +
                ", documento='" + documento + '\'' +
                '}';
    }
}