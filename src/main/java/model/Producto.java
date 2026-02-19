package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Producto {

    private int        productoId;
    private String     codigo;
    private String     nombre;
    private String     descripcion;
    private int        stock;
    private BigDecimal precioUnitario;
    private BigDecimal precioVenta;
    private String     imagen;        // nombre del archivo
    private byte[]     imagenData;    // bytes almacenados en BD
    private String     imagenTipo;    // ej: "image/jpeg"
    private Categoria  categoria;
    private Material   material;
    private String     materialNombre;
    private LocalDate  fechaRegistro;
    private int        proveedorId;

    public Producto() {}

    /* ── GETTERS ── */
    public int        getProductoId()    { return productoId; }
    public int        getId()            { return productoId; }
    public String     getCodigo()        { return codigo; }
    public String     getNombre()        { return nombre; }
    public String     getDescripcion()   { return descripcion; }
    public int        getStock()         { return stock; }
    public BigDecimal getPrecioUnitario(){ return precioUnitario; }
    public BigDecimal getPrecioVenta()   { return precioVenta; }
    public String     getImagen()        { return imagen; }
    public byte[]     getImagenData()    { return imagenData; }
    public String     getImagenTipo()    { return imagenTipo; }
    public Categoria  getCategoria()     { return categoria; }
    public Material   getMaterial()      { return material; }
    public LocalDate  getFechaRegistro() { return fechaRegistro; }
    public int        getProveedorId()   { return proveedorId; }

    public String getMaterialNombre() {
        if (material != null) return material.getNombre();
        return materialNombre;
    }

    /* ── SETTERS ── */
    public void setProductoId(int productoId)          { this.productoId = productoId; }
    public void setCodigo(String codigo)               { this.codigo = codigo; }
    public void setNombre(String nombre)               { this.nombre = nombre; }
    public void setDescripcion(String descripcion)     { this.descripcion = descripcion; }
    public void setStock(int stock)                    { this.stock = stock; }
    public void setPrecioUnitario(BigDecimal v)        { this.precioUnitario = v; }
    public void setPrecioVenta(BigDecimal v)           { this.precioVenta = v; }
    public void setImagen(String imagen)               { this.imagen = imagen; }
    public void setImagenData(byte[] imagenData)       { this.imagenData = imagenData; }
    public void setImagenTipo(String imagenTipo)       { this.imagenTipo = imagenTipo; }
    public void setCategoria(Categoria categoria)      { this.categoria = categoria; }
    public void setMaterial(Material material)         { this.material = material; }
    public void setMaterialNombre(String v)            { this.materialNombre = v; }
    public void setFechaRegistro(LocalDate v)          { this.fechaRegistro = v; }
    public void setProveedorId(int proveedorId)        { this.proveedorId = proveedorId; }
}