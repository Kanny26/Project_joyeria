<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="model.Producto, model.Administrador" %>
<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }
    Producto producto = (Producto) request.getAttribute("producto");
    if (producto == null) {
        response.sendRedirect(request.getContextPath() + "/ProductoServlet");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Ver Producto - AAC27</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/producto.css">
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/CategoriaServlet?id=<%= producto.getCategoria().getCategoriaId() %>" class="navbar-admin__home-link">
	   
	    <span class="navbar-admin__home-icon-wrap">
	        <i class="fa-solid fa-arrow-left"></i>
		    <span class="navbar-admin__home-text">Volver atras</span>
		    <i class="fa-solid fa-house-chimney"></i>
	    </span>
    </a>
</nav>

<div class="page-header">
    <h1 class="product-title">Detalles del Producto</h1>
</div>

<main class="product-page">
    <section class="product-card">
        <div class="product-image">
            <div class="product-image__circle">
                <img src="<%= request.getContextPath() %>/imagen-producto/<%= producto.getProductoId() %>" alt="<%= producto.getNombre() %>">
            </div>
        </div>
        <div class="product-details">
            <div class="product-row">
                <span class="product-label">Nombre</span>
                <span class="product-info-text"><%= producto.getNombre() %></span>
            </div>
            <div class="product-row">
                <span class="product-label">Categoría</span>
                <span class="product-info-text"><%= producto.getCategoria().getNombre() %></span>
            </div>
            <div class="product-row">
                <span class="product-label">Precio de Venta</span>
                <span class="product-value price">$<%= String.format("%,.0f", producto.getPrecioVenta()) %></span>
            </div>
            <div class="product-row">
                <span class="product-label">Descripción</span>
                <p class="product-info-text"><%= producto.getDescripcion() %></p>
            </div>
            <div class="product-actions">
                <a href="<%= request.getContextPath() %>/ProductoServlet?action=editar&id=<%= producto.getProductoId() %>" class="btn-primary no-underline">
                    <i class="fa-solid fa-pen"></i> Editar Producto
                </a>
                <a href="<%= request.getContextPath() %>/ProductoServlet?action=confirmarEliminar&id=<%= producto.getProductoId() %>" class="btn-danger no-underline">
                    <i class="fa-solid fa-trash-can"></i> Eliminar
                </a>
            </div>
        </div>
    </section>
</main>
</body>
</html>