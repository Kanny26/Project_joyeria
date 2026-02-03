package main.model;

import java.time.LocalDateTime;

/**
 * Clase modelo para la tabla Recuperacion_Contrasena
 * Representa las solicitudes de recuperación de contraseña
 */
public class RecuperacionContrasena {
    
    // Atributos
    private int recuperacionId;
    private int usuarioId;
    private String token;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaExpiracion;
    private boolean estado; // true = activo, false = usado/expirado
    
    // Objeto relacionado (para joins)
    private Usuario usuario;
    
    // Constructor vacío
    public RecuperacionContrasena() {
        this.fechaSolicitud = LocalDateTime.now();
        // Token válido por 24 horas por defecto
        this.fechaExpiracion = LocalDateTime.now().plusHours(24);
        this.estado = true;
    }
    
    // Constructor con campos principales
    public RecuperacionContrasena(int usuarioId, String token) {
        this();
        this.usuarioId = usuarioId;
        this.token = token;
    }
    
    // Constructor completo
    public RecuperacionContrasena(int recuperacionId, int usuarioId, String token,
                                 LocalDateTime fechaSolicitud, LocalDateTime fechaExpiracion, 
                                 boolean estado) {
        this.recuperacionId = recuperacionId;
        this.usuarioId = usuarioId;
        this.token = token;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaExpiracion = fechaExpiracion;
        this.estado = estado;
    }
    
    // Getters y Setters
    public int getRecuperacionId() {
        return recuperacionId;
    }
    
    public void setRecuperacionId(int recuperacionId) {
        this.recuperacionId = recuperacionId;
    }
    
    public int getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }
    
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }
    
    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }
    
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }
    
    public boolean isEstado() {
        return estado;
    }
    
    public void setEstado(boolean estado) {
        this.estado = estado;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    // Métodos auxiliares
    
    /**
     * Verifica si el token ha expirado
     */
    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }
    
    /**
     * Verifica si el token es válido (activo y no expirado)
     */
    public boolean isValido() {
        return estado && !isExpirado();
    }
    
    /**
     * Marca el token como usado
     */
    public void marcarComoUsado() {
        this.estado = false;
    }
    
    // Método toString
    @Override
    public String toString() {
        return "RecuperacionContrasena{" +
                "recuperacionId=" + recuperacionId +
                ", usuarioId=" + usuarioId +
                ", token='" + token + '\'' +
                ", fechaSolicitud=" + fechaSolicitud +
                ", fechaExpiracion=" + fechaExpiracion +
                ", estado=" + estado +
                '}';
    }
}