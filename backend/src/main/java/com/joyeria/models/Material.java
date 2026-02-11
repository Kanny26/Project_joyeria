package main.model;

/**
 * Clase modelo para la tabla Material
 * Representa los materiales de las joyas (oro, plata, acero, etc.)
 */
public class Material {
    
    // Atributos
    private int materialId;
    private String nombre;
    
    // Constructor vacío
    public Material() {
    }
    
    // Constructor con nombre
    public Material(String nombre) {
        this.nombre = nombre;
    }
    
    // Constructor completo
    public Material(int materialId, String nombre) {
        this.materialId = materialId;
        this.nombre = nombre;
    }
    
    // Getters y Setters
    public int getMaterialId() {
        return materialId;
    }
    
    public void setMaterialId(int materialId) {
        this.materialId = materialId;
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
        return "Material{" +
                "materialId=" + materialId +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}