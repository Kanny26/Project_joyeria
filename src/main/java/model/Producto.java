package model;

import java.math.BigDecimal;
import java.util.Date;

public class Producto {
    private int productoId;
    private String codigo;              // Ej: ANI01
    private String nombre;
    private String descripcion;
    private int stock;
    private BigDecimal precioUnitario;  // Precio de costo
    private BigDecimal precioVenta;     // Precio de venta al público
    private Date fechaRegistro;
    private String imagen;              // Nombre del archivo
    private byte[] imagenData;          // Bytes de la imagen (BLOB)
    private String imagenTipo;          // MIME type: image/jpeg, image/png
    
    // IDs de relaciones
    private int materialId;
    private int categoriaId;
    private int usuarioProveedorId;
    
    // Nombres para vistas (sin cargar objetos completos)
    private String materialNombre;
    private String categoriaNombre;
    private String proveedorNombre;

    public Producto() {}

    // Getters y Setters
    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public byte[] getImagenData() { return imagenData; }
    public void setImagenData(byte[] imagenData) { this.imagenData = imagenData; }

    public String getImagenTipo() { return imagenTipo; }
    public void setImagenTipo(String imagenTipo) { this.imagenTipo = imagenTipo; }

    public int getMaterialId() { return materialId; }
    public void setMaterialId(int materialId) { this.materialId = materialId; }

    public int getCategoriaId() { return categoriaId; }
    public void setCategoriaId(int categoriaId) { this.categoriaId = categoriaId; }

    public int getUsuarioProveedorId() { return usuarioProveedorId; }
    public void setUsuarioProveedorId(int usuarioProveedorId) { this.usuarioProveedorId = usuarioProveedorId; }

    public String getMaterialNombre() { return materialNombre; }
    public void setMaterialNombre(String materialNombre) { this.materialNombre = materialNombre; }

    public String getCategoriaNombre() { return categoriaNombre; }
    public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }

    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }

    // Helper para vistas
    public String getNombreCompleto() {
        return codigo + " - " + nombre;
    }
}