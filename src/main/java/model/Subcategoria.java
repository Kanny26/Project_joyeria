package model;

/**
 * Representa una subcategoría del catálogo.
 * Permite clasificar productos dentro de una categoría principal.
 * Ejemplo: Categoría "Collares" → Subcategorías "Dijes", "Collares largos".
 */
public class Subcategoria {

    private int    subcategoriaId;
    private String nombre;

    public Subcategoria() {}

    // Constructor de conveniencia: permite crear el objeto ya inicializado
    // sin necesidad de llamar a los setters por separado.
    public Subcategoria(int subcategoriaId, String nombre) {
        this.subcategoriaId = subcategoriaId;
        this.nombre         = nombre;
    }

    public int    getSubcategoriaId()           { return subcategoriaId; }
    public void   setSubcategoriaId(int id)     { this.subcategoriaId = id; }

    public String getNombre()                   { return nombre; }
    public void   setNombre(String nombre)      { this.nombre = nombre; }

    // toString útil para ver el contenido del objeto en logs o depuración.
    @Override
    public String toString() {
        return "Subcategoria{id=" + subcategoriaId + ", nombre='" + nombre + "'}";
    }
}
