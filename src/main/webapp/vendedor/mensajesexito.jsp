<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mensaje de Éxito | AAC27</title>
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
    <div class="form-card" style="text-align:center;max-width:500px;">
        <div style="font-size:72px;color:#22c55e;margin-bottom:1rem;">
            <i class="fa-solid fa-circle-check"></i>
        </div>
        <h2 style="font-size:1.4rem;font-weight:800;color:#1e1b4b;margin-bottom:.5rem;">
            <%
                String msg = (String) request.getAttribute("mensaje");
                out.print(msg != null ? msg : "¡Operación realizada con éxito!");
            %>
        </h2>
        <p style="color:#6b7280;margin-bottom:1.5rem;">La operación se completó correctamente.</p>
		<%-- Obtener el objeto venta que mandó el Servlet --%>
		<%@ page import="model.Venta" %>
		<%
		    Venta v = (Venta) request.getAttribute("venta");
		    if (v != null) {
		%>
		    <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 1.5rem; margin-bottom: 2rem; text-align: left;">
		        <h3 style="color: #1e1b4b; margin-bottom: 1rem; border-bottom: 1px solid #cbd5e1; padding-bottom: 5px;">
		            Resumen de Venta #<%= v.getVentaId() %>
		        </h3>
		        <p><strong>Cliente:</strong> <%= v.getClienteNombre() %></p>
		        <p><strong>Total pagado:</strong> <span style="color: #16a34a; font-weight: 800;">$<%= String.format("%,.2f", v.getTotal()) %></span></p>
		        <p><strong>Método:</strong> <%= v.getMetodoPago() %></p>
		        
		        <%-- Botón para descargar el PDF que configuramos antes --%>
		        <div style="margin-top: 1.5rem; text-align: center;">
		            <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=descargarFactura&id=<%= v.getVentaId() %>" 
		               class="btn-save" style="background-color: #7c3aed;">
		                <i class="fa-solid fa-file-pdf"></i> Descargar Factura PDF
		            </a>
		        </div>
		    </div>
		<% } %>

        <div class="form-actions" style="justify-content:center;gap:1rem;">
            <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=misVentas" class="btn-save">
                <i class="fa-solid fa-receipt"></i> Mis ventas
            </a>
            <a href="<%= request.getContextPath() %>/vendedor/vendedor_principal.jsp" class="btn-cancel">
                <i class="fa-solid fa-house-chimney"></i> Inicio
            </a>
        </div>
    </div>
</main>
</body>
</html>
