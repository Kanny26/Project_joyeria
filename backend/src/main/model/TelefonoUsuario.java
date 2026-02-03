package main.model;

/**
 * Clase modelo para la tabla Telefono_Usuario
 * Representa los teléfonos de contacto de un usuario
 */
public class TelefonoUsuario {
    
    // Atributos
    private int telefonoId;
    private String telefono;
    private int usuarioId;
    
    // Constructor vacío
    public TelefonoUsuario() {
    }
    
    // Constructor con campos principales
    public TelefonoUsuario(String telefono, int usuarioId) {
        this.telefono = telefono;
        this.usuarioId = usuarioId;
    }
    
    // Constructor completo
    public TelefonoUsuario(int telefonoId, String telefono, int usuarioId) {
        this.telefonoId = telefonoId;
        this.telefono = telefono;
        this.usuarioId = usuarioId;
    }
    
    // Getters y Setters
    public int getTelefonoId() {
        return telefonoId;
    }
    
    public void setTelefonoId(int telefonoId) {
        this.telefonoId = telefonoId;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
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
        return "TelefonoUsuario{" +
                "telefonoId=" + telefonoId +
                ", telefono='" + telefono + '\'' +
                ", usuarioId=" + usuarioId +
                '}';
    }
}