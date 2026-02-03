package main.model;

import java.time.LocalDate;

/**
 * Clase modelo para la tabla Producto
 * Representa los productos (joyas) en el inventario
 */
public class Producto {
    
    // Atributos
    private int productoId;
    private String nombre;
    private String descripcion;
    private int stock;
    private double precioUnitario;  // Precio de compra/costo
    private double precioVenta;      // Precio de venta al cliente
    private LocalDate fechaRegistro;
    private String imagen;
    
    // IDs de relaciones
    private int materialId;
    private int categoriaId;
    private int usuarioProveedorId;
    
    // Objetos relacionados (opcional para joins)
    private Material material;
    private Categoria categoria;
    private Usuario proveedor;
    
    // Constructor vacío
    public Producto() {
        this.fechaRegistro = LocalDate.now();
        this.stock = 0;
    }
    
    // Constructor con campos principales
    public Producto(String nombre, String descripcion, int stock, 
                   double precioUnitario, double precioVenta,
                   int materialId, int categoriaId, int usuarioProveedorId) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.stock = stock;
        this.precioUnitario = precioUnitario;
        this.precioVenta = precioVenta;
        this.materialId = materialId;
        this.categoriaId = categoriaId;
        this.usuarioProveedorId = usuarioProveedorId;
    }
    
    // Constructor completo
    public Producto(int productoId, String nombre, String descripcion, int stock,
                   double precioUnitario, double precioVenta, LocalDate fechaRegistro,
                   String imagen, int materialId, int categoriaId, int usuarioProveedorId) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.stock = stock;
        this.precioUnitario = precioUnitario;
        this.precioVenta = precioVenta;
        this.fechaRegistro = fechaRegistro;
        this.imagen = imagen;
        this.materialId = materialId;
        this.categoriaId = categoriaId;
        this.usuarioProveedorId = usuarioProveedorId;
    }
    
    // Getters y Setters
    public int getProductoId() {
        return productoId;
    }
    
    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public int getStock() {
        return stock;
    }
    
    public void setStock(int stock) {
        this.stock = stock;
    }
    
    public double getPrecioUnitario() {
        return precioUnitario;
    }
    
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
    
    public double getPrecioVenta() {
        return precioVenta;
    }
    
    public void setPrecioVenta(double precioVenta) {
        this.precioVenta = precioVenta;
    }
    
    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }
    
    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    public String getImagen() {
        return imagen;
    }
    
    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
    
    public int getMaterialId() {
        return materialId;
    }
    
    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }
    
    public int getCategoriaId() {
        return categoriaId;
    }
    
    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }
    
    public int getUsuarioProveedorId() {
        return usuarioProveedorId;
    }
    
    public void setUsuarioProveedorId(int usuarioProveedorId) {
        this.usuarioProveedorId = usuarioProveedorId;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public void setMaterial(Material material) {
        this.material = material;
    }
    
    public Categoria getCategoria() {
        return categoria;
    }
    
    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
    
    public Usuario getProveedor() {
        return proveedor;
    }
    
    public void setProveedor(Usuario proveedor) {
        this.proveedor = proveedor;
    }
    
    // Métodos auxiliares
    
    /**
     * Calcula el margen de ganancia en porcentaje
     */
    public double calcularMargenGanancia() {
        if (precioUnitario == 0) return 0;
        return ((precioVenta - precioUnitario) / precioUnitario) * 100;
    }
    
    /**
     * Verifica si el producto tiene stock disponible
     */
    public boolean tieneStock() {
        return stock > 0;
    }
    
    /**
     * Verifica si el stock está bajo (menos de 5 unidades)
     */
    public boolean stockBajo() {
        return stock < 5;
    }
    
    // Método toString
    @Override
    public String toString() {
        return "Producto{" +
                "productoId=" + productoId +
                ", nombre='" + nombre + '\'' +
                ", stock=" + stock +
                ", precioUnitario=" + precioUnitario +
                ", precioVenta=" + precioVenta +
                ", fechaRegistro=" + fechaRegistro +
                '}';
    }
}