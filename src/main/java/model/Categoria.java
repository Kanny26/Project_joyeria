package model;

import java.util.List;
import java.util.ArrayList;

/**
 * Representa una categoría del catálogo (ej: Collares, Anillos, Pulseras).
 * CAMBIO: se eliminó subcategoriaId directo. La categoría ya no tiene
 * una sola subcategoría fija — la relación Categoria ↔ Subcategoria
 * es muchos a muchos y se gestiona en Categoria_Subcategoria.
 */
public class Categoria {
    private int    categoriaId;
    private String nombre;
    private String icono;

    // Lista de subcategorías DISPONIBLES para esta categoría
    // (viene de Categoria_Subcategoria, se usa para poblar el select del formulario)
    private List<Subcategoria> subcategoriasDisponibles = new ArrayList<>();

    public Categoria() {}

    public int    getCategoriaId()  { return categoriaId; }
    public void   setCategoriaId(int categoriaId) { this.categoriaId = categoriaId; }

    public String getNombre()       { return nombre; }
    public void   setNombre(String nombre) { this.nombre = nombre; }

    public String getIcono()        { return icono; }
    public void   setIcono(String icono) { this.icono = icono; }

    public List<Subcategoria> getSubcategoriasDisponibles() { return subcategoriasDisponibles; }
    public void setSubcategoriasDisponibles(List<Subcategoria> lista) { this.subcategoriasDisponibles = lista; }
}