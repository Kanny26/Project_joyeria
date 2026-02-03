package main.model;

/**
 * Clase modelo para la tabla Permiso
 * Representa los permisos que pueden tener los roles en el sistema
 */
public class Permiso {
    
    // Atributos
    private int permisoId;
    private String nombre;
    private String descripcion;
    
    // Constructor vacío
    public Permiso() {
    }
    
    // Constructor con campos principales
    public Permiso(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
    
    // Constructor completo
    public Permiso(int permisoId, String nombre, String descripcion) {
        this.permisoId = permisoId;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
    
    // Getters y Setters
    public int getPermisoId() {
        return permisoId;
    }
    
    public void setPermisoId(int permisoId) {
        this.permisoId = permisoId;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    // Método toString
    @Override
    public String toString() {
        return "Permiso{" +
                "permisoId=" + permisoId +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}