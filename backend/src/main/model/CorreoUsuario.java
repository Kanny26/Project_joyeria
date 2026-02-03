package main.model;

/**
 * Clase modelo para la tabla Correo_Usuario
 * Representa los correos electrónicos de un usuario
 */
public class CorreoUsuario {
    
    // Atributos
    private int correoId;
    private String email;
    private int usuarioId;
    
    // Constructor vacío
    public CorreoUsuario() {
    }
    
    // Constructor con campos principales
    public CorreoUsuario(String email, int usuarioId) {
        this.email = email;
        this.usuarioId = usuarioId;
    }
    
    // Constructor completo
    public CorreoUsuario(int correoId, String email, int usuarioId) {
        this.correoId = correoId;
        this.email = email;
        this.usuarioId = usuarioId;
    }
    
    // Getters y Setters
    public int getCorreoId() {
        return correoId;
    }
    
    public void setCorreoId(int correoId) {
        this.correoId = correoId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public int getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    // Método toString
    @Override
    public String toString() {
        return "CorreoUsuario{" +
                "correoId=" + correoId +
                ", email='" + email + '\'' +
                ", usuarioId=" + usuarioId +
                '}';
    }
}