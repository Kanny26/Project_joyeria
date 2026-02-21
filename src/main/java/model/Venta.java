package model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Venta {
    private int ventaId;
    private int usuarioId;          // vendedor
    private int usuarioClienteId;   // cliente
    private String vendedorNombre;
    private String clienteNombre;
    private Date fechaEmision;
    private BigDecimal total;
    private String metodoPago;      // efectivo, tarjeta, transferencia
    private String estado;          // pendiente, confirmado, rechazado
    private List<DetalleVenta> detalles;

    // Constructor vacío
    public Venta() {}

    // Constructor completo
    public Venta(int ventaId, int usuarioId, int usuarioClienteId,
                 String vendedorNombre, String clienteNombre,
                 Date fechaEmision, BigDecimal total,
                 String metodoPago, String estado) {
        this.ventaId = ventaId;
        this.usuarioId = usuarioId;
        this.usuarioClienteId = usuarioClienteId;
        this.vendedorNombre = vendedorNombre;
        this.clienteNombre = clienteNombre;
        this.fechaEmision = fechaEmision;
        this.total = total;
        this.metodoPago = metodoPago;
        this.estado = estado;
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

    public Date getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(Date fechaEmision) { this.fechaEmision = fechaEmision; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }
}