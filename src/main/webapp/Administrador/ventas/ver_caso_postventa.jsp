<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%-- Ajusta estas importaciones a tus clases reales --%>
<%@ page import="com.tu.modelo.Venta" %>
<%@ page import="com.tu.modelo.DetalleVenta" %>

<%
    // Recuperar el objeto venta del request
    Venta venta = (Venta) request.getAttribute("venta");
    
    // Configuración de formatos
    NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Venta #<%= (venta != null) ? venta.getVentaId() : "" %> | Admin</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/ventas.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/Administrador/ventas/listar">
        <i class="fa-solid fa-arrow-left navbar-admin__home-icon"></i>
    </a>
</nav>

<main class="prov-page">
    <div class="form-card">
        <% if (venta != null) { %>
            <div class="form-card__title">
                <i class="fa-solid fa-receipt"></i> Detalle de Venta #<%= venta.getVentaId() %>
            </div>

            <div class="info-grid">
                <div class="info-item">
                    <span class="info-label">Vendedor</span>
                    <span class="info-value"><%= venta.getVendedorNombre() %></span>
                </div>
                <div class="info-item">
                    <span class="info-label">Cliente</span>
                    <span class="info-value"><%= venta.getClienteNombre() %></span>
                </div>
                
                <% if (venta.getTelefonoCliente() != null && !venta.getTelefonoCliente().isEmpty()) { %>
                    <div class="info-item">
                        <span class="info-label">Teléfono cliente</span>
                        <span class="info-value"><%= venta.getTelefonoCliente() %></span>
                    </div>
                <% } %>

                <div class="info-item">
                    <span class="info-label">Fecha</span>
                    <span class="info-value">
                        <%= (venta.getFechaEmision() != null) ? sdf.format(venta.getFechaEmision()) : "" %>
                    </span>
                </div>
                <div class="info-item">
                    <span class="info-label">Método de pago</span>
                    <span class="info-value">
                        <%= "efectivo".equals(venta.getMetodoPago()) ? "💵 Efectivo" : "💳 Tarjeta" %>
                    </span>
                </div>
                <div class="info-item">
                    <span class="info-label">Modalidad</span>
                    <span class="info-value">
                        <% if ("anticipo".equals(venta.getModalidad())) { %>
                            <span class="badge badge--warning">Anticipo (dos cuotas)</span>
                        <% } else { %>
                            <span class="badge badge--info">Contado</span>
                        <% } %>
                    </span>
                </div>
            </div>

            <% if ("anticipo".equals(venta.getModalidad())) { %>
                <div class="pago-resumen">
                    <div class="pago-resumen__item pago-resumen__item--ok">
                        <span class="label">Anticipo pagado</span>
                        <span class="valor">
                            <%= moneda.format(venta.getMontoAnticipo()) %>
                        </span>
                    </div>
                    <div class="pago-resumen__item pago-resumen__item--<%= (venta.getSaldoPendiente() != null && venta.getSaldoPendiente() > 0) ? "pending" : "ok" %>">
                        <span class="label">Saldo pendiente</span>
                        <span class="valor">
                            <% if (venta.getSaldoPendiente() != null && venta.getSaldoPendiente() > 0) { %>
                                <%= moneda.format(venta.getSaldoPendiente()) %>
                            <% } else { %>
                                $0.00 ✅
                            <% } %>
                        </span>
                    </div>
                    <div class="pago-resumen__item">
                        <span class="label">Total</span>
                        <span class="valor total-principal">
                            <%= moneda.format(venta.getTotal()) %>
                        </span>
                    </div>
                </div>
            <% } %>

            <div class="section-title">
                <i class="fa-solid fa-boxes-stacked"></i> Productos vendidos
            </div>
            <table class="productos-table">
                <thead>
                    <tr>
                        <th>Producto</th>
                        <th>Cantidad</th>
                        <th>Precio unit.</th>
                        <th>Subtotal</th>
                    </tr>
                </thead>
                <tbody>
                    <% 
                        List<DetalleVenta> detalles = venta.getDetalles();
                        if (detalles != null) {
                            for (DetalleVenta d : detalles) { 
                    %>
                        <tr>
                            <td><%= d.getProductoNombre() %></td>
                            <td><%= d.getCantidad() %></td>
                            <td><%= moneda.format(d.getPrecioUnitario()) %></td>
                            <td><%= moneda.format(d.getSubtotal()) %></td>
                        </tr>
                    <% 
                            } 
                        } 
                    %>
                </tbody>
                <tfoot>
                    <tr class="total-row">
                        <td colspan="3" class="text-right"><strong>Total</strong></td>
                        <td><strong><%= moneda.format(venta.getTotal()) %></strong></td>
                    </tr>
                </tfoot>
            </table>
        <% } else { %>
            <div class="alert alert--error">No se encontró la información de la venta.</div>
        <% } %>

        <div class="form-actions">
            <a href="<%= request.getContextPath() %>/Administrador/ventas/listar" class="btn-cancel">
                <i class="fa-solid fa-arrow-left"></i> Volver al listado
            </a>
        </div>
    </div>
</main>
</body>
</html>