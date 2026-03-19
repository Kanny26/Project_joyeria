package model;

import java.util.Date;

/**
 * Representa a un usuario del sistema.
 * Esta clase se usa para transportar los datos del usuario entre
 * la base de datos, los servlets y las vistas JSP.
 */
public class Usuario {

    // Campos que corresponden directamente a columnas de la base de datos
    private int usuarioId;
    private String nombre;
    private String correo;
    private String telefono;
    private String contrasena;
    private String rol;
    private boolean estado; // true = activo, false = inactivo
    private Date fechaCreacion;  // Fecha en que se registró el usuario en el sistema
    private Date fechaRegistro;  // Fecha de registro como cliente
    private Date fechaInicio;    // Fecha de inicio como proveedor
    private double minimoCompra;
    private String observaciones; // Campo opcional para notas internas del usuario

    // Constructor vacío necesario para crear objetos sin datos iniciales
    public Usuario() {}

    // Constructor completo para cuando se necesita inicializar el objeto con todos los datos de una vez
    public Usuario(int usuarioId, String nombre, String correo, String telefono, String contrasena,
                   String rol, boolean estado, Date fechaCreacion, Date fechaRegistro,
                   Date fechaInicio, double minimoCompra, String observaciones) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.contrasena = contrasena;
        this.rol = rol;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fechaRegistro = fechaRegistro;
        this.fechaInicio = fechaInicio;
        this.minimoCompra = minimoCompra;
        this.observaciones = observaciones;
    }

    // Getters y Setters: permiten acceder y modificar los campos privados desde otras clases
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    public double getMinimoCompra() { return minimoCompra; }
    public void setMinimoCompra(double minimoCompra) { this.minimoCompra = minimoCompra; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
