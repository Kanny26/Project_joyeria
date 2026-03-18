package model;

/**
 * Representa el material con el que puede estar fabricado un producto.
 * Ejemplos: Oro, Plata, Acero inoxidable, Cobre.
 * Se usa como filtro de búsqueda en la vista de productos por categoría.
 */
public class Material {
    private Integer materialId;
    private String nombre;

    public Material() {}

    // Constructor de conveniencia para inicializar el objeto en una sola línea.
    public Material(Integer materialId, String nombre) {
        this.materialId = materialId;
        this.nombre = nombre;
    }

    // Se usa Integer (objeto) en lugar de int (primitivo) para poder detectar
    // si el ID es null, por ejemplo antes de que el registro sea guardado en BD.
    public Integer getMaterialId() { return materialId; }
    public void setMaterialId(Integer materialId) { this.materialId = materialId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
