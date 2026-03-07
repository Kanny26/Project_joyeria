package model;

public class Categoria {
    private int categoriaId;
    private String nombre;
    private String icono;
    private int subcategoriaId; // ■■ CAMPO AGREGADO SEGÚN BD ■■

    public Categoria() {}

    public int getCategoriaId() { return categoriaId; }
    public void setCategoriaId(int categoriaId) { this.categoriaId = categoriaId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }

    // ■■ GETTER Y SETTER PARA SUBCATEGORIA ■■
    public int getSubcategoriaId() { return subcategoriaId; }
    public void setSubcategoriaId(int subcategoriaId) { this.subcategoriaId = subcategoriaId; }
}