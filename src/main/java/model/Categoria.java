package model;

/**
 * Clase modelo Categoria
 * Representa una categoría de productos dentro del sistema AAC27
 * Se utiliza principalmente para clasificación y visualización en JSP
 */
public class Categoria {

    // Identificador único de la categoría
    private int categoriaId;

    // Nombre de la categoría
    private String nombre;

    // Ícono asociado a la categoría (ruta o nombre del recurso tipo imagen)
    private String icono;

    /**
     * Constructor vacío
     * Requerido para instanciación desde JSP y DAOs
     */
    public Categoria() {
    }

    /* ===============================
       GETTERS
       =============================== */

    // Retorna el id real de la categoría
    public int getCategoriaId() {
        return categoriaId;
    }

    // Alias del id para uso en JSP (evita errores de compatibilidad)
    public int getId() {
        return categoriaId;
    }

    // Retorna el nombre de la categoría
    public String getNombre() {
        return nombre;
    }

    // Retorna el ícono de la categoría
    public String getIcono() {
        return icono;
    }

    /* ===============================
       SETTERS
       =============================== */

    // Asigna el id de la categoría
    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }

    // Asigna el nombre de la categoría
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Asigna el ícono de la categoría
    public void setIcono(String icono) {
        this.icono = icono;
    }
}

