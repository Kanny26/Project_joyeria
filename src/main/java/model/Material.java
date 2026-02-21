// model/Material.java
package model;

public class Material {
    private Integer materialId;
    private String nombre;
    
    public Material() {}
    
    public Material(Integer materialId, String nombre) {
        this.materialId = materialId;
        this.nombre = nombre;
    }
    
    public Integer getMaterialId() { return materialId; }
    public void setMaterialId(Integer materialId) { this.materialId = materialId; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}