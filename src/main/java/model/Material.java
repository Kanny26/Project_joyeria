package model;

/**
 * Clase modelo Material
 * Representa el material asociado a un producto dentro del sistema AAC27
 */
public class Material {

    // Identificador único del material
    private int materialId;

    // Nombre del material
    private String nombre;

    /**
     * Constructor vacío
     * Necesario para instanciación desde DAOs y JSP
     */
    public Material() {
    }

    /* ===============================
       GETTERS
       =============================== */

    // Retorna el id del material
    public int getMaterialId() {
        return materialId;
    }

    // Retorna el nombre del material
    public String getNombre() {
        return nombre;
    }

    /* ===============================
       SETTERS
       =============================== */

    // Asigna el id del material
    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    // Asigna el nombre del material
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}

