package main.model;

/**
 * Clase modelo para la tabla Subcategoria
 * Representa las subcategorías de productos
 */
public class Subcategoria {
    
    // Atributos
    private int subcategoriaId;
    private String nombre;
    
    // Constructor vacío
    public Subcategoria() {
    }
    
    // Constructor con nombre
    public Subcategoria(String nombre) {
        this.nombre = nombre;
    }
    
    // Constructor completo
    public Subcategoria(int subcategoriaId, String nombre) {
        this.subcategoriaId = subcategoriaId;
        this.nombre = nombre;
    }
    
    // Getters y Setters
    public int getSubcategoriaId() {
        return subcategoriaId;
    }
    
    public void setSubcategoriaId(int subcategoriaId) {
        this.subcategoriaId = subcategoriaId;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    // Método toString
    @Override
    public String toString() {
        return "Subcategoria{" +
                "subcategoriaId=" + subcategoriaId +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}