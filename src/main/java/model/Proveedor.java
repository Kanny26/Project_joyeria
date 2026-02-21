package model;

import java.util.ArrayList;
import java.util.List;

public class Proveedor {
    private Integer usuarioId;
    private String nombre;
    private String pass;
    private Boolean estado;
    private String documento;
    private String fechaRegistro;
    private String fechaInicio;
    private Double minimoCompra;
    
    // Relaciones
    private List<String> telefonos;
    private List<String> correos;
    private List<Material> materiales;
    private List<Producto> productos;

    public Proveedor() {
        this.telefonos = new ArrayList<>();
        this.correos = new ArrayList<>();
        this.materiales = new ArrayList<>();
        this.productos = new ArrayList<>();
    }

    // Getters y Setters
    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }

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

    public List<String> getTelefonos() { return telefonos; }
    public void setTelefonos(List<String> telefonos) { this.telefonos = telefonos; }
    public void addTelefono(String telefono) { this.telefonos.add(telefono); }

    public List<String> getCorreos() { return correos; }
    public void setCorreos(List<String> correos) { this.correos = correos; }
    public void addCorreo(String correo) { this.correos.add(correo); }

    public List<Material> getMateriales() { return materiales; }
    public void setMateriales(List<Material> materiales) { this.materiales = materiales; }
    public void addMaterial(Material material) { this.materiales.add(material); }

    public List<Producto> getProductos() { return productos; }
    public void setProductos(List<Producto> productos) { this.productos = productos; }
    public void addProducto(Producto producto) { this.productos.add(producto); }
}