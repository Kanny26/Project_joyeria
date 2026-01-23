package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Proveedor {
    private int usuarioId;
    private String nombre;
    private List<String> telefonos;
    private List<String> correos;
    private List<String> materiales;
    private List<Producto> productos;
    private LocalDate fechaInicio;
    private boolean estado;

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<String> getTelefonos() { return telefonos; }
    public void setTelefonos(List<String> telefonos) { this.telefonos = telefonos; }

    public List<String> getCorreos() { return correos; }
    public void setCorreos(List<String> correos) { this.correos = correos; }

    public List<String> getMateriales() { return materiales; }
    public void setMateriales(List<String> materiales) { this.materiales = materiales; }

    public List<Producto> getProductos() { return productos; }
    public void setProductos(List<Producto> productos) { this.productos = productos; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }
}