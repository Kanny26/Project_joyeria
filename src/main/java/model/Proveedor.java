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
 * Los DAO cargan joins y listas relacionadas; los controladores exponen el proveedor a JSP y formularios.
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
    /** Constructor que inicializa listas vacías; usado por DAO y vistas. */
    public Proveedor() {
        this.telefonos = new ArrayList<>();
        this.correos = new ArrayList<>();
        this.materiales = new ArrayList<>();
        this.productos = new ArrayList<>();
    }

    /** @return identificador del proveedor */
    public Integer getProveedorId() { return proveedorId; }
    /** @param proveedorId identificador del proveedor */
    public void setProveedorId(Integer proveedorId) { this.proveedorId = proveedorId; }

    // Alias eliminado para evitar confusión. Usar siempre getProveedorId()
    // public Integer getUsuarioId() { return proveedorId; } // DEPRECATED

    /** @return nombre del proveedor */
    public String getNombre() { return nombre; }
    /** @param nombre nombre del proveedor */
    public void setNombre(String nombre) { this.nombre = nombre; }

    // Si estado es null (dato incompleto en BD), se asume activo por defecto
    /** @return estado del proveedor (true si activo o null) */
    public Boolean isEstado() { return estado != null ? estado : true; }
    /** @param estado activo o inactivo */
    public void setEstado(Boolean estado) { this.estado = estado; }

    /** @return documento de identificación */
    public String getDocumento() { return documento; }
    /** @param documento número de documento */
    public void setDocumento(String documento) { this.documento = documento; }

    /** @return fecha de registro como cadena */
    public String getFechaRegistro() { return fechaRegistro; }
    /** @param fechaRegistro fecha de registro */
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    /** @return fecha de inicio de relación comercial */
    public String getFechaInicio() { return fechaInicio; }
    /** @param fechaInicio fecha de inicio */
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }

    /** @return mínimo de compra acordado */
    public Double getMinimoCompra() { return minimoCompra; }
    /** @param minimoCompra mínimo de compra */
    public void setMinimoCompra(Double minimoCompra) { this.minimoCompra = minimoCompra; }

    // Listas de relaciones
    /** @return lista de teléfonos de contacto */
    public List<String> getTelefonos() { return telefonos; }
    /** @param telefonos lista de teléfonos */
    public void setTelefonos(List<String> telefonos) { this.telefonos = telefonos; }
    /** @param telefono teléfono a agregar */
    public void addTelefono(String telefono) { this.telefonos.add(telefono); }

    /** @return lista de correos */
    public List<String> getCorreos() { return correos; }
    /** @param correos lista de correos */
    public void setCorreos(List<String> correos) { this.correos = correos; }
    /** @param correo correo a agregar */
    public void addCorreo(String correo) { this.correos.add(correo); }

    /** @return materiales asociados */
    public List<Material> getMateriales() { return materiales; }
    /** @param m lista de materiales */
    public void setMateriales(List<Material> m) { this.materiales = m; }
    /** @param material material a agregar */
    public void addMaterial(Material material) { this.materiales.add(material); }

    /** @return productos suministrados */
    public List<Producto> getProductos() { return productos; }
    /** @param p lista de productos */
    public void setProductos(List<Producto> p) { this.productos = p; }
    /** @param producto producto a agregar */
    public void addProducto(Producto producto) { this.productos.add(producto); }
}
