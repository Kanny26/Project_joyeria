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
