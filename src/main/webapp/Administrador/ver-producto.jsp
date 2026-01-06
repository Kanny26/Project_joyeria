<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="model.Producto, model.Administrador" %>

<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }

    Producto producto = (Producto) request.getAttribute("producto");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Ver producto</title>

    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
</head>

<body>

<nav class="navbar-admin">
    <h1>AAC27</h1>
</nav>

<main class="ver-producto">

    <img src="<%=request.getContextPath()%>/<%= producto.getImagen() %>">

    <h2><%= producto.getNombre() %></h2>
    <p>Categoría: <%= producto.getCategoria().getNombre() %></p>
    <p>Material: <%= producto.getMaterial().getNombre() %></p>
    <p>Precio: $<%= producto.getPrecio() %></p>
    <p>Stock: <%= producto.getStock() %></p>
    <p><%= producto.getDescripcion() %></p>

    <a href="<%=request.getContextPath()%>/ProductoServlet?action=editar&id=<%= producto.getId() %>">
        Editar
    </a>

</main>

</body>
</html>
