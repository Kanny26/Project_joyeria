package model;

import java.time.LocalDate;
import java.util.List;

/**
 * Clase modelo Proveedor
 * Representa un proveedor del sistema con sus datos de contacto y productos asociados
 */
public class Proveedor {

    // Identificador del proveedor (usuario)
    private int usuarioId;

    // Nombre del proveedor
    private String nombre;

    // Lista de teléfonos de contacto
    private List<String> telefonos;

    // Lista de correos electrónicos
    private List<String> correos;

    // Lista de materiales que maneja el proveedor
    private List<String> materiales;

    // Lista de productos suministrados
    private List<Producto> productos;

    // Fecha de inicio de relación comercial
    private LocalDate fechaInicio;

    // Estado del proveedor (activo/inactivo)
    private boolean estado;

    /**
     * Constructor vacío
     * Necesario para DAOs y JSP
     */
    public Proveedor() {
    }

    /* ===============================
       GETTERS
       =============================== */

    // Retorna el id del proveedor
    public int getUsuarioId() {
        return usuarioId;
    }

    // Retorna el nombre del proveedor
    public String getNombre() {
        return nombre;
    }

    // Retorna los teléfonos
    public List<String> getTelefonos() {
        return telefonos;
    }

    // Retorna los correos
    public List<String> getCorreos() {
        return correos;
    }

    // Retorna los materiales
    public List<String> getMateriales() {
        return materiales;
    }

    // Retorna los productos
    public List<Producto> getProductos() {
        return productos;
    }

    // Retorna la fecha de inicio
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    // Retorna el estado del proveedor
    public boolean isEstado() {
        return estado;
    }

    /* ===============================
       SETTERS
       =============================== */

    // Asigna el id del proveedor
    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    // Asigna el nombre del proveedor
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Asigna los teléfonos
    public void setTelefonos(List<String> telefonos) {
        this.telefonos = telefonos;
    }

    // Asigna los correos
    public void setCorreos(List<String> correos) {
        this.correos = correos;
    }

    // Asigna los materiales
    public void setMateriales(List<String> materiales) {
        this.materiales = materiales;
    }

    // Asigna los productos
    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

    // Asigna la fecha de inicio
    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    // Asigna el estado del proveedor
    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
