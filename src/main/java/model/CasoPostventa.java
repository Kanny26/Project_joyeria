package model;

import java.util.Date;
import java.util.List;

public class CasoPostventa {
    private int casoId;
    private int ventaId;
    private int productoId;
    
    private String clienteNombre;
    private String vendedorNombre;
    private String productoNombre;
    
    private String tipo; // cambio | devolucion | reclamo
    private int cantidad;
    private String motivo;
    private Date fecha;
    private String estado; // en_proceso | aprobado | cancelado
    
    private List<EstadoCasoCliente> historialEstados;

    public CasoPostventa() {}

    // Getters y Setters
    public int getCasoId() { return casoId; }
    public void setCasoId(int casoId) { this.casoId = casoId; }
    
    public int getVentaId() { return ventaId; }
    public void setVentaId(int ventaId) { this.ventaId = ventaId; }
    
    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }
    
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String n) { this.clienteNombre = n; }
    
    public String getVendedorNombre() { return vendedorNombre; }
    public void setVendedorNombre(String n) { this.vendedorNombre = n; }
    
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String n) { this.productoNombre = n; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public List<EstadoCasoCliente> getHistorialEstados() { return historialEstados; }
    public void setHistorialEstados(List<EstadoCasoCliente> h) { this.historialEstados = h; }
    
    // Helpers para JSP
    public String getTipoLabel() {
        if (tipo == null) return "";
        return switch (tipo) {
            case "cambio" -> "Cambio";
            case "devolucion" -> "Devolución";
            case "reclamo" -> "Reclamo";
            default -> tipo;
        };
    }
    
    public String getEstadoLabel() {
        if (estado == null) return "";
        return switch (estado) {
            case "en_proceso" -> "En proceso";
            case "aprobado" -> "Aprobado";
            case "cancelado" -> "Cancelado";
            default -> estado;
        };
    }
}