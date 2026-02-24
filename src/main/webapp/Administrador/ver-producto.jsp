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
    <!-- CORREGIDO: usar getCategoriaId() -->
    <a href="<%= request.getContextPath() %>/CategoriaServlet?id=<%= producto.getCategoriaId() %>" 
       class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
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
                <img src="<%= request.getContextPath() %>/imagen-producto/<%= producto.getProductoId() %>" 
                     alt="<%= producto.getNombre() %>"
                     onerror="this.src='<%=request.getContextPath()%>/assets/Imagenes/default.png'">
            </div>
        </div>
        
        <div class="product-details">
            <div class="product-row">
                <span class="product-label">Código</span>
                <span class="product-info-text"><%= producto.getCodigo() %></span>
            </div>
            
            <div class="product-row">
                <span class="product-label">Nombre</span>
                <span class="product-info-text"><%= producto.getNombre() %></span>
            </div>
            
            <!-- CORREGIDO: usar getCategoriaNombre() -->
            <div class="product-row">
                <span class="product-label">Categoría</span>
                <span class="product-info-text">
                    <%= producto.getCategoriaNombre() != null ? producto.getCategoriaNombre() : "Sin categoría" %>
                </span>
            </div>
            
            <!-- Material -->
            <div class="product-row">
                <span class="product-label">Material</span>
                <span class="product-info-text">
                    <%= producto.getMaterialNombre() != null ? producto.getMaterialNombre() : "Sin material" %>
                </span>
            </div>
            
            <div class="product-row">
                <span class="product-label">Precio de Costo</span>
                <span class="product-value price">$<%= String.format("%,.0f", producto.getPrecioUnitario()) %></span>
            </div>
            
            <div class="product-row">
                <span class="product-label">Precio de Venta</span>
                <span class="product-value price">$<%= String.format("%,.0f", producto.getPrecioVenta()) %></span>
            </div>
            
            <div class="product-row">
                <span class="product-label">Stock</span>
                <span class="product-info-text <%= producto.getStock() <= 3 ? "stock-bajo" : "" %>">
                    <%= producto.getStock() %>
                    <% if (producto.getStock() <= 3) { %>
                        <i class="fa-solid fa-triangle-exclamation" title="Stock bajo"></i>
                    <% } %>
                </span>
            </div>
            
            <div class="product-row">
                <span class="product-label">Descripción</span>
                <p class="product-info-text"><%= producto.getDescripcion() != null ? producto.getDescripcion() : "Sin descripción" %></p>
            </div>
            
            <div class="product-row">
                <span class="product-label">Registrado el</span>
                <span class="product-info-text">
                    <%= producto.getFechaRegistro() != null ? 
                        new java.text.SimpleDateFormat("dd/MM/yyyy").format(producto.getFechaRegistro()) : "N/A" %>
                </span>
            </div>
            
            <div class="product-actions">
                <a href="<%= request.getContextPath() %>/ProductoServlet?action=editar&id=<%= producto.getProductoId() %>" 
                   class="btn-primary no-underline">
                    <i class="fa-solid fa-pen"></i> Editar Producto
                </a>
                <a href="<%= request.getContextPath() %>/ProductoServlet?action=confirmarEliminar&id=<%= producto.getProductoId() %>" 
                   class="btn-danger no-underline">
                    <i class="fa-solid fa-trash-can"></i> Eliminar
                </a>
                <button type="button" class="btn-secondary no-underline" onclick="window.history.back()">
                    <i class="fa-solid fa-xmark"></i> Cancelar
                </button>
            </div>
        </div>
    </section>
</main>

</body>
</html>