package main.model;

/**
 * Clase modelo para la tabla Categoria
 * Representa las categorías de productos (Anillos, Collares, etc.)
 */
public class Categoria {
    
    // Atributos
    private int categoriaId;
    private String nombre;
    private String icono;
    private Integer subcategoriaId; // Puede ser null
    
    // Constructor vacío
    public Categoria() {
    }
    
    // Constructor con campos principales
    public Categoria(String nombre, String icono) {
        this.nombre = nombre;
        this.icono = icono;
    }
    
    // Constructor completo
    public Categoria(int categoriaId, String nombre, String icono, Integer subcategoriaId) {
        this.categoriaId = categoriaId;
        this.nombre = nombre;
        this.icono = icono;
        this.subcategoriaId = subcategoriaId;
    }
    
    // Getters y Setters
    public int getCategoriaId() {
        return categoriaId;
    }
    
    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getIcono() {
        return icono;
    }
    
    public void setIcono(String icono) {
        this.icono = icono;
    }
    
    public Integer getSubcategoriaId() {
        return subcategoriaId;
    }
    
    public void setSubcategoriaId(Integer subcategoriaId) {
        this.subcategoriaId = subcategoriaId;
    }
    
    // Método toString
    @Override
    public String toString() {
        return "Categoria{" +
                "categoriaId=" + categoriaId +
                ", nombre='" + nombre + '\'' +
                ", icono='" + icono + '\'' +
                '}';
    }
}