package model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Venta {
    private int ventaId;
    private int usuarioId;          // Vendedor
    private int usuarioClienteId;   // Cliente
    private String vendedorNombre;
    private String clienteNombre;
    private String telefonoCliente;
    private Date fechaEmision;
    private BigDecimal total;
    private String metodoPago;      // efectivo, tarjeta
    private String estado;          // pendiente, confirmado, rechazado
    private String modalidad;       // contado, anticipo (calculado)
    private BigDecimal montoAnticipo;
    private BigDecimal saldoPendiente;
    private List<DetalleVenta> detalles;

    public Venta() {}

    public Venta(int usuarioId, int usuarioClienteId, Date fechaEmision, 
                 BigDecimal total, String metodoPago) {
        this.usuarioId = usuarioId;
        this.usuarioClienteId = usuarioClienteId;
        this.fechaEmision = fechaEmision;
        this.total = total;
        this.metodoPago = metodoPago;
        this.estado = "pendiente";
    }

    // Getters y Setters
    public int getVentaId() { return ventaId; }
    public void setVentaId(int ventaId) { this.ventaId = ventaId; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public int getUsuarioClienteId() { return usuarioClienteId; }
    public void setUsuarioClienteId(int usuarioClienteId) { this.usuarioClienteId = usuarioClienteId; }
    public String getVendedorNombre() { return vendedorNombre; }
    public void setVendedorNombre(String vendedorNombre) { this.vendedorNombre = vendedorNombre; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getTelefonoCliente() { return telefonoCliente; }
    public void setTelefonoCliente(String telefonoCliente) { this.telefonoCliente = telefonoCliente; }
    public Date getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(Date fechaEmision) { this.fechaEmision = fechaEmision; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }
    public BigDecimal getMontoAnticipo() { return montoAnticipo; }
    public void setMontoAnticipo(BigDecimal montoAnticipo) { this.montoAnticipo = montoAnticipo; }
    public BigDecimal getSaldoPendiente() { return saldoPendiente; }
    public void setSaldoPendiente(BigDecimal saldoPendiente) { this.saldoPendiente = saldoPendiente; }
    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }
}