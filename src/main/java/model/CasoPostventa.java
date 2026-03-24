package model;

import java.util.Date;
import java.util.List;

/**
 * Caso de postventa (cambio, devolución o reclamo) vinculado a venta y producto.
 * Los DAO consultan y actualizan casos e historial; los controladores gestionan el flujo y las vistas de gestión.
 */
public class CasoPostventa {
    private int casoId;
    private int ventaId;
    private int productoId;
    
    private String clienteNombre;
    private String vendedorNombre;
    private String productoNombre;
    
    private String tipo; // cambio | devolucion | reclamo
    private int cantidad;
    private String motivo; // El motivo inicial del cliente
    private String observacion; // La respuesta/observación del administrador (NUEVO)
    private Date fecha;
    private String estado; // en_proceso | aprobado | cancelado
    
    // Si usas una clase para el historial, asegúrate que se llame igual
    private List<EstadoCasoCliente> historialEstados;

    /** Constructor vacío; usado por DAO y vistas. */
    public CasoPostventa() {}

    // --- GETTERS Y SETTERS ---
    /** @return identificador del caso */
    public int getCasoId() { return casoId; }
    /** @param casoId identificador del caso */
    public void setCasoId(int casoId) { this.casoId = casoId; }
    
    /** @return identificador de la venta */
    public int getVentaId() { return ventaId; }
    /** @param ventaId identificador de la venta */
    public void setVentaId(int ventaId) { this.ventaId = ventaId; }
    
    /** @return identificador del producto */
    public int getProductoId() { return productoId; }
    /** @param productoId identificador del producto */
    public void setProductoId(int productoId) { this.productoId = productoId; }
    
    /** @return nombre del cliente (vista) */
    public String getClienteNombre() { return clienteNombre; }
    /** @param n nombre del cliente */
    public void setClienteNombre(String n) { this.clienteNombre = n; }
    
    /** @return nombre del vendedor (vista) */
    public String getVendedorNombre() { return vendedorNombre; }
    /** @param n nombre del vendedor */
    public void setVendedorNombre(String n) { this.vendedorNombre = n; }
    
    /** @return nombre del producto (vista) */
    public String getProductoNombre() { return productoNombre; }
    /** @param n nombre del producto */
    public void setProductoNombre(String n) { this.productoNombre = n; }
    
    /** @return tipo de caso (código interno) */
    public String getTipo() { return tipo; }
    /** @param tipo tipo de caso */
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    /** @return cantidad afectada */
    public int getCantidad() { return cantidad; }
    /** @param cantidad cantidad */
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    
    /** @return motivo declarado por el cliente */
    public String getMotivo() { return motivo; }
    /** @param motivo motivo del cliente */
    public void setMotivo(String motivo) { this.motivo = motivo; }

    /** @return observación o respuesta del administrador */
    public String getObservacion() { return observacion; }
    /** @param observacion observación del administrador */
    public void setObservacion(String observacion) { this.observacion = observacion; }
    
    /** @return fecha del caso o última actualización */
    public Date getFecha() { return fecha; }
    /** @param fecha fecha del registro */
    public void setFecha(Date fecha) { this.fecha = fecha; }
    
    /** @return estado del caso */
    public String getEstado() { return estado; }
    /** @param estado estado del caso */
    public void setEstado(String estado) { this.estado = estado; }
    
    /** @return historial de cambios de estado */
    public List<EstadoCasoCliente> getHistorialEstados() { return historialEstados; }
    /** @param h lista de estados del historial */
    public void setHistorialEstados(List<EstadoCasoCliente> h) { this.historialEstados = h; }
    
    // --- HELPERS PARA JSP (Lógica de presentación) ---
    
    /**
     * Etiqueta legible del tipo de caso para la vista.
     *
     * @return texto para mostrar en UI
     */
    public String getTipoLabel() {
        if (tipo == null) return "No definido";
        return switch (tipo) {
            case "cambio" -> "Cambio";
            case "devolucion" -> "Devolución";
            case "reclamo" -> "Reclamo";
            default -> tipo;
        };
    }
    
    /**
     * Etiqueta legible del estado para la vista.
     *
     * @return texto para mostrar en UI
     */
    public String getEstadoLabel() {
        if (estado == null) return "En proceso";
        return switch (estado) {
            case "en_proceso" -> "En proceso";
            case "aprobado" -> "Aprobado";
            case "cancelado" -> "Cancelado";
            default -> estado.replace("_", " ");
        };
    }

    /**
     * Clase CSS sugerida según el estado (badges en JSP).
     *
     * @return nombre de clase CSS
     */
    public String getEstadoClass() {
        if (estado == null) return "pv-badge--warn";
        return switch (estado) {
            case "aprobado" -> "pv-badge--ok";
            case "cancelado" -> "pv-badge--danger";
            default -> "pv-badge--warn";
        };
    }
}
