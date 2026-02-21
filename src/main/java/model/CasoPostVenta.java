package model;

import java.util.Date;

public class CasoPostventa {
    private int casoId;
    private int ventaId;
    private String tipo;       // cambio, devolucion, reclamo
    private int cantidad;
    private String motivo;
    private Date fecha;
    private String estado;     // en_proceso, aprobado, cancelado
    private String vendedorNombre;
    private String clienteNombre;
    private String observacion;

    public CasoPostventa() {}

    public CasoPostventa(int casoId, int ventaId, String tipo, int cantidad,
                         String motivo, Date fecha, String estado,
                         String vendedorNombre, String clienteNombre) {
        this.casoId = casoId;
        this.ventaId = ventaId;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.fecha = fecha;
        this.estado = estado;
        this.vendedorNombre = vendedorNombre;
        this.clienteNombre = clienteNombre;
    }

    public int getCasoId() { return casoId; }
    public void setCasoId(int casoId) { this.casoId = casoId; }

    public int getVentaId() { return ventaId; }
    public void setVentaId(int ventaId) { this.ventaId = ventaId; }

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

    public String getVendedorNombre() { return vendedorNombre; }
    public void setVendedorNombre(String vendedorNombre) { this.vendedorNombre = vendedorNombre; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}