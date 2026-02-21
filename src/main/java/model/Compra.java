package model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Compra {
    private int compraId;
    private int proveedorId;
    private String proveedorNombre;   // JOIN con Usuario
    private Date fechaCompra;
    private Date fechaEntrega;
    private BigDecimal total;
    private List<DetalleCompra> detalles;

    public Compra() {}

    // ── Getters y Setters ──
    public int getCompraId()                  { return compraId; }
    public void setCompraId(int v)            { this.compraId = v; }

    public int getProveedorId()               { return proveedorId; }
    public void setProveedorId(int v)         { this.proveedorId = v; }

    public String getProveedorNombre()        { return proveedorNombre; }
    public void setProveedorNombre(String v)  { this.proveedorNombre = v; }

    public Date getFechaCompra()              { return fechaCompra; }
    public void setFechaCompra(Date v)        { this.fechaCompra = v; }

    public Date getFechaEntrega()             { return fechaEntrega; }
    public void setFechaEntrega(Date v)       { this.fechaEntrega = v; }

    public BigDecimal getTotal()              { return total; }
    public void setTotal(BigDecimal v)        { this.total = v; }

    public List<DetalleCompra> getDetalles()           { return detalles; }
    public void setDetalles(List<DetalleCompra> v)     { this.detalles = v; }
}