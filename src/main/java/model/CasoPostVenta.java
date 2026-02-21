package model;

import java.util.Date;

public class CasoPostVenta {
    private int    casoId;
    private int    ventaId;
    private String tipo;          // cambio, devolucion, reclamo
    private int    cantidad;
    private String motivo;
    private Date   fecha;
    private String estado;        // en_proceso, aprobado, cancelado
    private String vendedorNombre;
    private String clienteNombre;
    private String observacion;

    public CasoPostVenta() {}

    // Getters y Setters
    public int    getCasoId()           { return casoId; }
    public void   setCasoId(int v)      { this.casoId = v; }

    public int    getVentaId()          { return ventaId; }
    public void   setVentaId(int v)     { this.ventaId = v; }

    public String getTipo()             { return tipo; }
    public void   setTipo(String v)     { this.tipo = v; }

    public int    getCantidad()         { return cantidad; }
    public void   setCantidad(int v)    { this.cantidad = v; }

    public String getMotivo()           { return motivo; }
    public void   setMotivo(String v)   { this.motivo = v; }

    public Date   getFecha()            { return fecha; }
    public void   setFecha(Date v)      { this.fecha = v; }

    public String getEstado()           { return estado; }
    public void   setEstado(String v)   { this.estado = v; }

    public String getVendedorNombre()         { return vendedorNombre; }
    public void   setVendedorNombre(String v) { this.vendedorNombre = v; }

    public String getClienteNombre()          { return clienteNombre; }
    public void   setClienteNombre(String v)  { this.clienteNombre = v; }

    public String getObservacion()            { return observacion; }
    public void   setObservacion(String v)    { this.observacion = v; }
}