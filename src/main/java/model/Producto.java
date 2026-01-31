package model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Clase modelo Producto
 * Representa un producto del inventario con su información comercial y relacional
 */
public class Producto {

    // Identificador único del producto
    private int productoId;

    // Código interno del producto
    private String codigo;

    // Nombre del producto
    private String nombre;

    // Descripción del producto
    private String descripcion;

    // Cantidad disponible en inventario
    private int stock;

    // Precio de compra o costo unitario
    private BigDecimal precioUnitario;

    // Precio de venta al público
    private BigDecimal precioVenta;

    // Ruta o nombre de la imagen del producto
    private String imagen;

    // Categoría asociada al producto
    private Categoria categoria;

    // Material asociado (uso avanzado u opcional)
    private Material material;

    // Nombre del material usado para listados simples
    private String materialNombre;

    // Fecha de registro del producto
    private LocalDate fechaRegistro;

    // Identificador del proveedor asociado
    private int proveedorId;

    /**
     * Constructor vacío
     * Requerido para DAOs, JSP y frameworks
     */
    public Producto() {
    }

    /* ===============================
       GETTERS
       =============================== */

    // Retorna el id del producto
    public int getProductoId() {
        return productoId;
    }

    // Alias del id para compatibilidad con JSP
    public int getId() {
        return productoId;
    }

    // Retorna el código del producto
    public String getCodigo() {
        return codigo;
    }

    // Retorna el nombre del producto
    public String getNombre() {
        return nombre;
    }

    // Retorna la descripción del producto
    public String getDescripcion() {
        return descripcion;
    }

    // Retorna el stock disponible
    public int getStock() {
        return stock;
    }

    // Retorna el precio unitario
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    // Retorna el precio de venta
    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    // Retorna la imagen del producto
    public String getImagen() {
        return imagen;
    }

    // Retorna la categoría asociada
    public Categoria getCategoria() {
        return categoria;
    }

    // Retorna el material asociado
    public Material getMaterial() {
        return material;
    }

    // Retorna el nombre del material
    // Prioriza el objeto Material si existe
    public String getMaterialNombre() {
        if (material != null) {
            return material.getNombre();
        }
        return materialNombre;
    }

    // Retorna la fecha de registro
    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    // Retorna el id del proveedor
    public int getProveedorId() {
        return proveedorId;
    }

    /* ===============================
       SETTERS
       =============================== */

    // Asigna el id del producto
    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

    // Asigna el código del producto
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    // Asigna el nombre del producto
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Asigna la descripción del producto
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Asigna el stock
    public void setStock(int stock) {
        this.stock = stock;
    }

    // Asigna el precio unitario
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    // Asigna el precio de venta
    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    // Asigna la imagen del producto
    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    // Asigna la categoría
    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    // Asigna el material
    public void setMaterial(Material material) {
        this.material = material;
    }

    // Asigna el nombre del material
    public void setMaterialNombre(String materialNombre) {
        this.materialNombre = materialNombre;
    }

    // Asigna la fecha de registro
    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    // Asigna el id del proveedor
    public void setProveedorId(int proveedorId) {
        this.proveedorId = proveedorId;
    }
}

