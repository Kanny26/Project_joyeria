package model;

public class Subcategoria {

    private int    subcategoriaId;
    private String nombre;

    public Subcategoria() {}

    public Subcategoria(int subcategoriaId, String nombre) {
        this.subcategoriaId = subcategoriaId;
        this.nombre         = nombre;
    }

    public int    getSubcategoriaId()              { return subcategoriaId; }
    public void   setSubcategoriaId(int id)        { this.subcategoriaId = id; }

    public String getNombre()                      { return nombre; }
    public void   setNombre(String nombre)         { this.nombre = nombre; }

    @Override
    public String toString() {
        return "Subcategoria{id=" + subcategoriaId + ", nombre='" + nombre + "'}";
    }
}