package main.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO (Data Transfer Object) para registro completo de usuario
 * Incluye datos del usuario, rol, teléfonos y correos en una sola clase
 * Útil para operaciones de registro y actualización
 */
public class UsuarioCompleto {
    
    // Datos del usuario
    private int usuarioId;
    private String nombre;
    private String pass;
    private boolean estado;
    private LocalDateTime fechaCreacion;
    private String documento;
    private LocalDate fechaRegistro;  // Para clientes
    private LocalDate fechaInicio;    // Para proveedores
    private Double minimoCompra;      // Para proveedores
    
    // Datos del rol
    private String cargo; // vendedor, administrador, proveedor, cliente
    private String nombreRol;
    
    // Listas de contacto
    private List<String> telefonos;
    private List<String> correos;
    
    // Constructor vacío
    public UsuarioCompleto() {
        this.estado = true;
        this.fechaCreacion = LocalDateTime.now();
        this.telefonos = new ArrayList<>();
        this.correos = new ArrayList<>();
    }
    
    // Constructor para registro básico
    public UsuarioCompleto(String nombre, String pass, String cargo) {
        this();
        this.nombre = nombre;
        this.pass = pass;
        this.cargo = cargo;
        this.nombreRol = nombre; // Por defecto el nombre del rol es el nombre del usuario
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
    
    public String getCargo() {
        return cargo;
    }
    
    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
    
    public String getNombreRol() {
        return nombreRol;
    }
    
    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }
    
    public List<String> getTelefonos() {
        return telefonos;
    }
    
    public void setTelefonos(List<String> telefonos) {
        this.telefonos = telefonos;
    }
    
    public void agregarTelefono(String telefono) {
        this.telefonos.add(telefono);
    }
    
    public List<String> getCorreos() {
        return correos;
    }
    
    public void setCorreos(List<String> correos) {
        this.correos = correos;
    }
    
    public void agregarCorreo(String correo) {
        this.correos.add(correo);
    }
    
    // Métodos de conversión
    
    /**
     * Convierte este DTO a un objeto Usuario
     */
    public Usuario toUsuario() {
        Usuario usuario = new Usuario();
        usuario.setUsuarioId(this.usuarioId);
        usuario.setNombre(this.nombre);
        usuario.setPass(this.pass);
        usuario.setEstado(this.estado);
        usuario.setFechaCreacion(this.fechaCreacion);
        usuario.setDocumento(this.documento);
        usuario.setFechaRegistro(this.fechaRegistro);
        usuario.setFechaInicio(this.fechaInicio);
        usuario.setMinimoCompra(this.minimoCompra);
        return usuario;
    }
    
    /**
     * Convierte este DTO a un objeto Rol
     */
    public Rol toRol() {
        Rol rol = new Rol();
        rol.setCargo(Rol.TipoCargo.fromString(this.cargo));
        rol.setUsuarioId(this.usuarioId);
        rol.setNombre(this.nombreRol);
        return rol;
    }
    
    // Método toString
    @Override
    public String toString() {
        return "UsuarioCompleto{" +
                "usuarioId=" + usuarioId +
                ", nombre='" + nombre + '\'' +
                ", cargo='" + cargo + '\'' +
                ", documento='" + documento + '\'' +
                ", telefonos=" + telefonos +
                ", correos=" + correos +
                '}';
    }
}