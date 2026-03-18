package model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Venta {

    private int ventaId;
    private int usuarioId;
    private int usuarioClienteId;

    private String vendedorNombre;
    private String clienteNombre;
    private String telefonoCliente;
    private String emailCliente;

    private Date fechaEmision;
    private BigDecimal total;
    private String metodoPago;
    private String estado;
    private String modalidad;
    private BigDecimal montoAnticipo;
    private BigDecimal saldoPendiente;

    private List<DetalleVenta> detalles;
    private List<CasoPostventa> casosPostventa; // NUEVO

    public Venta() {}

    public Venta(int usuarioId, int usuarioClienteId, Date fechaEmision,
                 BigDecimal total, String metodoPago) {
        this.usuarioId        = usuarioId;
        this.usuarioClienteId = usuarioClienteId;
        this.fechaEmision     = fechaEmision;
        this.total            = total;
        this.metodoPago       = metodoPago;
        this.estado           = "pendiente";
    }

    public int getVentaId()                            { return ventaId; }
    public void setVentaId(int ventaId)                { this.ventaId = ventaId; }

    public int getUsuarioId()                          { return usuarioId; }
    public void setUsuarioId(int usuarioId)            { this.usuarioId = usuarioId; }

    public int getUsuarioClienteId()                   { return usuarioClienteId; }
    public void setUsuarioClienteId(int id)            { this.usuarioClienteId = id; }

    public String getVendedorNombre()                  { return vendedorNombre; }
    public void setVendedorNombre(String n)            { this.vendedorNombre = n; }

    public String getClienteNombre()                   { return clienteNombre; }
    public void setClienteNombre(String n)             { this.clienteNombre = n; }

    public String getTelefonoCliente()                 { return telefonoCliente; }
    public void setTelefonoCliente(String t)           { this.telefonoCliente = t; }

    public String getEmailCliente()                    { return emailCliente; }
    public void setEmailCliente(String e)              { this.emailCliente = e; }

    public Date getFechaEmision()                      { return fechaEmision; }
    public void setFechaEmision(Date d)                { this.fechaEmision = d; }

    public BigDecimal getTotal()                       { return total; }
    public void setTotal(BigDecimal total)             { this.total = total; }

    public String getMetodoPago()                      { return metodoPago; }
    public void setMetodoPago(String metodoPago)       { this.metodoPago = metodoPago; }

    public String getEstado()                          { return estado; }
    public void setEstado(String estado)               { this.estado = estado; }

    public String getModalidad()                       { return modalidad; }
    public void setModalidad(String modalidad)         { this.modalidad = modalidad; }

    public BigDecimal getMontoAnticipo()               { return montoAnticipo; }
    public void setMontoAnticipo(BigDecimal m)         { this.montoAnticipo = m; }

    public BigDecimal getSaldoPendiente()              { return saldoPendiente; }
    public void setSaldoPendiente(BigDecimal s)        { this.saldoPendiente = s; }

    public List<DetalleVenta> getDetalles()            { return detalles; }
    public void setDetalles(List<DetalleVenta> d)      { this.detalles = d; }

    public List<CasoPostventa> getCasosPostventa()              { return casosPostventa; }
    public void setCasosPostventa(List<CasoPostventa> casos)    { this.casosPostventa = casos; }

    public boolean isAnticipo() {
        return "anticipo".equals(modalidad);
    }

    public boolean isPagadoCompleto() {
        return saldoPendiente == null || saldoPendiente.compareTo(BigDecimal.ZERO) == 0;
    }

    public String getEstadoPago() {
        if (isAnticipo() && !isPagadoCompleto()) return "Con saldo pendiente";
        if ("confirmado".equals(estado))          return "Pagado";
        return "Pendiente";
    }
}