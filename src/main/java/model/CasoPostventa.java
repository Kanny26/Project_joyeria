package model;

import java.util.Date;
import java.util.List;

/**
 * Caso de postventa registrado por el vendedor.
 * Mapea a Caso_Postventa_Cliente + Estado_Caso_Cliente en BD.
 */
public class CasoPostventa {

    private int casoId;
    private int ventaId;

    // Datos enriquecidos via JOIN
    private String clienteNombre;
    private String vendedorNombre;
    private String productoNombre;

    private String tipo;     // cambio | devolucion | reclamo
    private int cantidad;
    private String motivo;
    private Date fecha;
    private String estado;   // en_proceso | aprobado | cancelado

    // Historial de estados
    private List<EstadoCasoCliente> historialEstados;

    // ── Constructores ──────────────────────────────────────────
    public CasoPostventa() {}

    // ── Getters / Setters ──────────────────────────────────────
    public int getCasoId()                             { return casoId; }
    public void setCasoId(int casoId)                  { this.casoId = casoId; }

    public int getVentaId()                            { return ventaId; }
    public void setVentaId(int ventaId)                { this.ventaId = ventaId; }

    public String getClienteNombre()                   { return clienteNombre; }
    public void setClienteNombre(String n)             { this.clienteNombre = n; }

    public String getVendedorNombre()                  { return vendedorNombre; }
    public void setVendedorNombre(String n)            { this.vendedorNombre = n; }

    public String getProductoNombre()                  { return productoNombre; }
    public void setProductoNombre(String n)            { this.productoNombre = n; }

    public String getTipo()                            { return tipo; }
    public void setTipo(String tipo)                   { this.tipo = tipo; }

    public int getCantidad()                           { return cantidad; }
    public void setCantidad(int cantidad)              { this.cantidad = cantidad; }

    public String getMotivo()                          { return motivo; }
    public void setMotivo(String motivo)               { this.motivo = motivo; }

    public Date getFecha()                             { return fecha; }
    public void setFecha(Date fecha)                   { this.fecha = fecha; }

    public String getEstado()                          { return estado; }
    public void setEstado(String estado)               { this.estado = estado; }

    public List<EstadoCasoCliente> getHistorialEstados()          { return historialEstados; }
    public void setHistorialEstados(List<EstadoCasoCliente> h)    { this.historialEstados = h; }

    // ── Helper badges para JSP ─────────────────────────────────
    public String getTipoLabel() {
        if (tipo == null) return "";
        return switch (tipo) {
            case "cambio"      -> "Cambio";
            case "devolucion"  -> "Devolución";
            case "reclamo"     -> "Reclamo";
            default            -> tipo;
        };
    }

    public String getEstadoLabel() {
        if (estado == null) return "";
        return switch (estado) {
            case "en_proceso" -> "En proceso";
            case "aprobado"   -> "Aprobado";
            case "cancelado"  -> "Cancelado";
            default           -> estado;
        };
    }
}
