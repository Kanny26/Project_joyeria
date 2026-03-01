<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="model.Venta" %>
<%@ page import="model.DetalleVenta" %>
<%
    Object adminSesion = session.getAttribute("admin");
    if (adminSesion == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }
    Venta venta = (Venta) request.getAttribute("venta");
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
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/ventas/ver_ventas.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/AdminVentaServlet?action=listar">
        <i class="fa-solid fa-arrow-left navbar-admin__home-icon"></i>
    </a>
</nav>

<main class="prov-page">
    <% if (venta != null) { %>
    <div class="factura">
        <header class="factura__header">
            <div class="factura__logo">
                <h1>Abby.accesorios</h1>
                <p class="slogan">Tu lugar favorito</p>
            </div>
            <div class="factura__info">
                <p><strong>No. Factura:</strong> <%= venta.getVentaId() %></p>
                <p><strong>Fecha:</strong> <%= (venta.getFechaEmision() != null) ? sdf.format(venta.getFechaEmision()) : "" %></p>
                <p><strong>Método de pago:</strong> <%= venta.getMetodoPago() %></p>
            </div>
        </header>

        <div class="linea-div"></div>

        <section class="factura__cliente">
            <p class="label">Factura para:</p>
            <h2 class="cliente-nombre"><%= venta.getClienteNombre() %></h2>
            <p>Vendedor: <%= venta.getVendedorNombre() %></p>
            <%
                String estadoClass = "estado--pendiente";
                String estadoLabel = "Pendiente";
                if ("confirmado".equals(venta.getEstado())) { estadoClass = "estado--pagado"; estadoLabel = "Pagado"; }
                else if ("rechazado".equals(venta.getEstado())) { estadoClass = "estado--rechazado"; estadoLabel = "Rechazado"; }
            %>
            <span class="estado <%= estadoClass %>"><%= estadoLabel %></span>
        </section>

        <% if ("anticipo".equals(venta.getModalidad()) && venta.getMontoAnticipo() != null) { %>
        <div class="pago-resumen" style="display:flex;gap:1rem;margin:1rem 0;flex-wrap:wrap;">
            <div style="background:#dcfce7;border-radius:10px;padding:.75rem 1rem;">
                <div style="font-size:11px;font-weight:700;color:#16a34a;">Anticipo pagado</div>
                <div style="font-weight:800;color:#059669;"><%= moneda.format(venta.getMontoAnticipo()) %></div>
            </div>
            <% if (venta.getSaldoPendiente() != null && venta.getSaldoPendiente().compareTo(java.math.BigDecimal.ZERO) > 0) { %>
            <div style="background:#fee2e2;border-radius:10px;padding:.75rem 1rem;">
                <div style="font-size:11px;font-weight:700;color:#dc2626;">Saldo pendiente</div>
                <div style="font-weight:800;color:#991b1b;"><%= moneda.format(venta.getSaldoPendiente()) %></div>
            </div>
            <% } %>
        </div>
        <% } %>

        <table class="tabla">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Descripción</th>
                    <th>Precio Unitario</th>
                    <th>Cant.</th>
                    <th>Total</th>
                </tr>
            </thead>
            <tbody>
                <%
                    List<DetalleVenta> detalles = venta.getDetalles();
                    int num = 1;
                    if (detalles != null) {
                        for (DetalleVenta d : detalles) {
                %>
                <tr>
                    <td><%= num++ %></td>
                    <td><%= d.getProductoNombre() %></td>
                    <td><%= moneda.format(d.getPrecioUnitario()) %></td>
                    <td><%= d.getCantidad() %></td>
                    <td><%= moneda.format(d.getSubtotal()) %></td>
                </tr>
                <% } } %>
            </tbody>
        </table>

        <div class="totales">
            <p class="total-final">
                <span>Total:</span> <%= moneda.format(venta.getTotal()) %>
            </p>
        </div>

        <section class="condiciones">
            <h3>Términos y condiciones</h3>
            <p>Gracias por su compra. Esta factura corresponde a los servicios prestados y debe conservarse como comprobante.</p>
        </section>
    </div>
    <% } else { %>
    <div class="titulo">
        <p style="text-align:center;margin-top:2rem;">No se encontró la venta solicitada.</p>
        <a href="<%= request.getContextPath() %>/AdminVentaServlet?action=listar">Volver al listado</a>
    </div>
    <% } %>
</main>
</body>
</html>
