package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo Proveedor — mapea la tabla Proveedor del SQL.
 * Tabla: Proveedor(proveedor_id, nombre, documento, fecha_registro, fecha_inicio, estado)
 *
 * Esta clase representa un proveedor del sistema. Además de los campos
 * básicos, incluye listas de contacto (teléfonos, correos) y relaciones
 * con materiales y productos que este proveedor suministra.
 */
public class Proveedor {
    private Integer proveedorId; // PK real de la tabla Proveedor
    private String nombre;
    private Boolean estado;
    private String documento;
    private String fechaRegistro;
    private String fechaInicio; // 
    private Double minimoCompra; // 

    // Relaciones (cargadas con JOINs o consultas adicionales)
    private List<String> telefonos;
    private List<String> correos;
    private List<Material> materiales;
    private List<Producto> productos;

    // Se inicializan las listas al crear un proveedor para evitar NullPointerException
    // al intentar agregar elementos antes de asignar valores
    public Proveedor() {
        this.telefonos = new ArrayList<>();
        this.correos = new ArrayList<>();
        this.materiales = new ArrayList<>();
        this.productos = new ArrayList<>();
    }

    public Integer getProveedorId() { return proveedorId; }
    public void setProveedorId(Integer proveedorId) { this.proveedorId = proveedorId; }

    // ■■ Alias eliminado para evitar confusión. Usar siempre getProveedorId() ■■
    // public Integer getUsuarioId() { return proveedorId; } // DEPRECATED

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    // Si estado es null (dato incompleto en BD), se asume activo por defecto
    public Boolean isEstado() { return estado != null ? estado : true; }
    public void setEstado(Boolean estado) { this.estado = estado; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }

    public Double getMinimoCompra() { return minimoCompra; }
    public void setMinimoCompra(Double minimoCompra) { this.minimoCompra = minimoCompra; }

    // ■■ Listas de relaciones ■■
    public List<String> getTelefonos() { return telefonos; }
    public void setTelefonos(List<String> telefonos) { this.telefonos = telefonos; }
    public void addTelefono(String telefono) { this.telefonos.add(telefono); }

    public List<String> getCorreos() { return correos; }
    public void setCorreos(List<String> correos) { this.correos = correos; }
    public void addCorreo(String correo) { this.correos.add(correo); }

    public List<Material> getMateriales() { return materiales; }
    public void setMateriales(List<Material> m) { this.materiales = m; }
    public void addMaterial(Material material) { this.materiales.add(material); }

    public List<Producto> getProductos() { return productos; }
    public void setProductos(List<Producto> p) { this.productos = p; }
    public void addProducto(Producto producto) { this.productos.add(producto); }
}
