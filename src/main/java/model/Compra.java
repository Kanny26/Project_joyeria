package model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Compra {

    private int         compraId;
    private int         proveedorId;
    private Date        fechaCompra;
    private Date        fechaEntrega;
    private BigDecimal  total;
    private List<DetalleCompra> detalles;

    public Compra() {}

    // ── Getters & Setters ──────────────────────────
    public int getCompraId()                    { return compraId; }
    public void setCompraId(int compraId)       { this.compraId = compraId; }

    public int getProveedorId()                 { return proveedorId; }
    public void setProveedorId(int proveedorId) { this.proveedorId = proveedorId; }

    public Date getFechaCompra()                { return fechaCompra; }
    public void setFechaCompra(Date fechaCompra){ this.fechaCompra = fechaCompra; }

    public Date getFechaEntrega()                   { return fechaEntrega; }
    public void setFechaEntrega(Date fechaEntrega)  { this.fechaEntrega = fechaEntrega; }

    public BigDecimal getTotal()                { return total; }
    public void setTotal(BigDecimal total)      { this.total = total; }

    public List<DetalleCompra> getDetalles()            { return detalles; }
    public void setDetalles(List<DetalleCompra> detalles){ this.detalles = detalles; }
}