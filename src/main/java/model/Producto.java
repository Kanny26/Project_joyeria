package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class Producto {

    private int productoId;
    private String codigo;
    private String nombre;
    private String descripcion;
    private int stock;
    private BigDecimal precioUnitario;
    private BigDecimal precioVenta;
    private String imagen;
    private Categoria categoria;
    private Material material;
    private LocalDateTime fechaRegistro;


    public Producto() {}

    public int getProductoId() {
        return productoId;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

    public int getId() {
        return productoId;
    }
    
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal  getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal  precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal  getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal  precioVenta) {
        this.precioVenta = precioVenta;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getImagen() {
        return imagen;
    }
    
    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

}

