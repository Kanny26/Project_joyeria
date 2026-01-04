package model;

import java.util.Date;

public class Usuario {

    // -------------------------
    // Campos de la base de datos
    // -------------------------
    private int usuarioId;
    private String nombre;
    private String correo;        // email
    private String telefono;
    private String contrasena;    // pass
    private String rol;
    private boolean estado;       // true = activo, false = inactivo
    private Date fechaCreacion;   // fecha del registro del usuario
    private String documento;     // documento único
    private Date fechaRegistro;   // fecha de cliente
    private Date fechaInicio;     // fecha de proveedor
    private double minimoCompra;  // mínimo de compra
    private String observaciones; // opcional, para historial o desempeño

    // -------------------------
    // Constructores
    // -------------------------
    public Usuario() {}

    public Usuario(int usuarioId, String nombre, String correo, String telefono, String contrasena,
                   String rol, boolean estado, Date fechaCreacion, String documento,
                   Date fechaRegistro, Date fechaInicio, double minimoCompra, String observaciones) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.contrasena = contrasena;
        this.rol = rol;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.documento = documento;
        this.fechaRegistro = fechaRegistro;
        this.fechaInicio = fechaInicio;
        this.minimoCompra = minimoCompra;
        this.observaciones = observaciones;
    }

    // -------------------------
    // Getters y Setters
    // -------------------------
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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public double getMinimoCompra() {
        return minimoCompra;
    }

    public void setMinimoCompra(double minimoCompra) {
        this.minimoCompra = minimoCompra;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}

