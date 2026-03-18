package model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Representa un producto del catálogo.
 * CAMBIO: se reemplazó subcategoriaId (int único) por subcategoriaIds (List<Integer>)
 * y subcategoriaNombres (List<String>), porque un producto puede tener varias subcategorías.
 * Se mantiene subcategoriaNombre como String para retrocompatibilidad con vistas
 * que solo necesiten mostrar las subcategorías como texto separado por comas.
 */
public class Producto {
    private int        productoId;
    private String     codigo;
    private String     nombre;
    private String     descripcion;
    private int        stock;
    private BigDecimal precioUnitario;
    private BigDecimal precioVenta;
    private Date       fechaRegistro;
    private String     imagen;
    private byte[]     imagenData;
    private String     imagenTipo;
    private boolean    estado;

    // IDs de relaciones simples (1:1)
    private int materialId;
    private int categoriaId;
    private int proveedorId;

    // CAMBIO: subcategorías como lista (muchos a muchos)
    // subcategoriaIds: IDs seleccionados en el formulario / cargados desde BD
    // subcategoriaNombre: string concatenado para mostrar en vistas de solo lectura
    private List<Integer> subcategoriaIds    = new ArrayList<>();
    private String        subcategoriaNombre;   // "Compromiso, Aniversario, Uso Diario"

    // Nombres para vistas (joins)
    private String materialNombre;
    private String categoriaNombre;
    private String proveedorNombre;

    public Producto() {}

    // ── IDs y campos simples ──────────────────────────────────
    public int    getProductoId()  { return productoId; }
    public void   setProductoId(int productoId) { this.productoId = productoId; }

    public String getCodigo()      { return codigo; }
    public void   setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre()      { return nombre; }
    public void   setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void   setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int    getStock()       { return stock; }
    public void   setStock(int stock) { this.stock = stock; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void       setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void       setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    public Date   getFechaRegistro() { return fechaRegistro; }
    public void   setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getImagen()      { return imagen; }
    public void   setImagen(String imagen) { this.imagen = imagen; }

    public byte[] getImagenData()  { return imagenData; }
    public void   setImagenData(byte[] imagenData) { this.imagenData = imagenData; }

    public String getImagenTipo()  { return imagenTipo; }
    public void   setImagenTipo(String imagenTipo) { this.imagenTipo = imagenTipo; }

    public boolean isEstado()      { return estado; }
    public void    setEstado(boolean estado) { this.estado = estado; }

    public int getMaterialId()     { return materialId; }
    public void setMaterialId(int materialId) { this.materialId = materialId; }

    public int getCategoriaId()    { return categoriaId; }
    public void setCategoriaId(int categoriaId) { this.categoriaId = categoriaId; }

    public int getProveedorId()    { return proveedorId; }
    public void setProveedorId(int proveedorId) { this.proveedorId = proveedorId; }

    // ── Subcategorías (lista) ─────────────────────────────────
    public List<Integer> getSubcategoriaIds() { return subcategoriaIds; }
    public void          setSubcategoriaIds(List<Integer> subcategoriaIds) {
        this.subcategoriaIds = subcategoriaIds != null ? subcategoriaIds : new ArrayList<>();
    }

    // Nombre concatenado para mostrar en JSP: "Compromiso, Aniversario"
    public String getSubcategoriaNombre() { return subcategoriaNombre; }
    public void   setSubcategoriaNombre(String subcategoriaNombre) {
        this.subcategoriaNombre = subcategoriaNombre;
    }

    // ── Nombres (joins) ───────────────────────────────────────
    public String getMaterialNombre()  { return materialNombre; }
    public void   setMaterialNombre(String materialNombre) { this.materialNombre = materialNombre; }

    public String getCategoriaNombre() { return categoriaNombre; }
    public void   setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }

    public String getProveedorNombre() { return proveedorNombre; }
    public void   setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }

    // Helper para vistas
    public String getNombreCompleto() { return codigo + " - " + nombre; }
}