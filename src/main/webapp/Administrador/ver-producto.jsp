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
        response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
        return;
    }
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Ver producto - <%= producto.getNombre() %></title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
</head>
<body>

<nav class="navbar-admin">
    <h1>AAC27</h1>
</nav>

<main class="ver-producto">
    <img src="<%=request.getContextPath()%>/imagenes/<%= 
        (producto.getImagen() != null && !producto.getImagen().trim().isEmpty()) ? 
        producto.getImagen() : "default.jpg" 
    %>" alt="<%= producto.getNombre() %>">

    <h2><%= producto.getNombre() %></h2>
    <p><strong>Categoría:</strong> <%= producto.getCategoria().getNombre() %></p>
    <p><strong>Material:</strong> <%= producto.getMaterial().getNombre() %></p>
    <p><strong>Precio unitario:</strong> $<%= producto.getPrecioUnitario() %></p>
    <p><strong>Precio venta:</strong> $<%= producto.getPrecioVenta() %></p>
    <p><strong>Stock:</strong> <%= producto.getStock() %></p>
    <p><strong>Descripción:</strong> <%= producto.getDescripcion() != null ? producto.getDescripcion() : "Sin descripción" %></p>

    <div style="margin-top: 20px;">
        <a href="<%=request.getContextPath()%>/ProductoServlet?action=editar&id=<%= producto.getProductoId() %>"
           class="btn-editar">Editar</a>
        <a href="<%=request.getContextPath()%>/CategoriaServlet?id=<%= producto.getCategoria().getCategoriaId() %>"
           class="btn-volver">Volver</a>
    </div>
</main>

</body>
</html>
