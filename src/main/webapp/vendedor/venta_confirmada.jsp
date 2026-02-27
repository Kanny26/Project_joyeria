<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="model.Venta" %>
<%
    Object vendedorSesion = session.getAttribute("vendedor");
    if (vendedorSesion == null) {
        response.sendRedirect(request.getContextPath() + "/vendedor/inicio-sesion.jsp");
        return;
    }
    Venta venta = (Venta) request.getAttribute("venta");
    String mensaje = (String) request.getAttribute("mensaje");
    NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    if (mensaje == null) mensaje = "¡Venta registrada con éxito!";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Venta Confirmada | AAC27</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Vendedor/registrar_venta.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/Seller.png" alt="Vendedor">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
</nav>

<main class="prov-page">
    <div class="form-card confirmacion-card" style="text-align:center;">
        <div style="font-size:72px;color:#22c55e;margin-bottom:1rem;">
            <i class="fa-solid fa-circle-check"></i>
        </div>
        <h2 style="font-size:1.4rem;font-weight:800;color:#1e1b4b;margin-bottom:1.5rem;"><%= mensaje %></h2>

        <% if (venta != null) { %>
        <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:1rem;margin-bottom:1.5rem;">
            <div style="background:#f5f3ff;border-radius:12px;padding:1rem;">
                <div style="font-size:11px;font-weight:700;color:#7c3aed;text-transform:uppercase;margin-bottom:4px;">Cliente</div>
                <div style="font-weight:800;color:#1e1b4b;"><%= venta.getClienteNombre() %></div>
            </div>
            <div style="background:#f5f3ff;border-radius:12px;padding:1rem;">
                <div style="font-size:11px;font-weight:700;color:#7c3aed;text-transform:uppercase;margin-bottom:4px;">Fecha</div>
                <div style="font-weight:700;color:#1e1b4b;">
                    <%= (venta.getFechaEmision() != null) ? sdf.format(venta.getFechaEmision()) : "" %>
                </div>
            </div>
            <div style="background:#dcfce7;border-radius:12px;padding:1rem;">
                <div style="font-size:11px;font-weight:700;color:#16a34a;text-transform:uppercase;margin-bottom:4px;">Total</div>
                <div style="font-weight:800;font-size:1.2rem;color:#059669;"><%= moneda.format(venta.getTotal()) %></div>
            </div>
            <% if ("anticipo".equals(venta.getModalidad())) { %>
            <div style="background:#fef9c3;border-radius:12px;padding:1rem;">
                <div style="font-size:11px;font-weight:700;color:#ca8a04;text-transform:uppercase;margin-bottom:4px;">Anticipo</div>
                <div style="font-weight:800;color:#92400e;"><%= moneda.format(venta.getMontoAnticipo()) %></div>
            </div>
            <div style="background:#fee2e2;border-radius:12px;padding:1rem;">
                <div style="font-size:11px;font-weight:700;color:#dc2626;text-transform:uppercase;margin-bottom:4px;">Saldo pendiente</div>
                <div style="font-weight:800;color:#991b1b;"><%= moneda.format(venta.getSaldoPendiente()) %></div>
            </div>
            <% } %>
        </div>
        <% } %>

        <div class="form-actions" style="justify-content:center;gap:1rem;">
            <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=nueva" class="btn-save">
                <i class="fa-solid fa-plus"></i> Nueva Venta
            </a>
            <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=misVentas" class="btn-cancel">
                <i class="fa-solid fa-list"></i> Ver mis ventas
            </a>
        </div>
    </div>
</main>
</body>
</html>
