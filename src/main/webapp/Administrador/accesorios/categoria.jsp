<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, model.Producto, model.Categoria, model.Administrador" %>

<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }

    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
    Categoria categoria = (Categoria) request.getAttribute("categoria");
    if (categoria == null) {
        out.println("<p>Error: categoría no encontrada</p>");
        return;
    }
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title><%= categoria.getNombre() %></title>

    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/gest-productos.css">
</head>

<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin">
        <h2>Volver al inicio</h2>
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>

    <div class="navbar-admin__usuario">
        <i class="fas fa-user"></i>
        <span><%= admin.getNombre() %></span>
    </div>

    <a href="<%=request.getContextPath()%>/index.jsp">
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
    </a>
</nav>

<main class="titulo">
    <h2 class="titulo__encabezado"><%= categoria.getNombre() %></h2>

    <div class="cards__barra-superior">
        <a href="<%=request.getContextPath()%>/ProductoServlet?action=nuevo&categoria=<%= categoria.getId() %>"
           class="cards__boton-agregar">
            + Agregar Producto
        </a>
    </div>

    <section class="cards__contenedor">

		<% if (productos == null) { %>
		    <p>Error: no se recibieron productos</p>
		
		<% } else if (productos.isEmpty()) { %>
		    <p>No hay productos en esta categoría</p>
		
		<% } else {
		    for (Producto p : productos) { %>
		
		    <div class="cards__contenedor-content">
		        <img src="<%=request.getContextPath()%>/<%= p.getImagen() %>">
		
		        <h3><%= p.getNombre() %></h3>
		        <h4><%= p.getMaterial().getNombre() %></h4>
		        <h4>$<%= p.getPrecioUnitario() %></h4>
		        <h4>Stock: <%= p.getStock() %></h4>
		
		        <div class="iconos">
		            <a href="<%=request.getContextPath()%>/ProductoServlet?action=ver&id=<%= p.getProductoId() %>">👁</a>
		            <a href="<%=request.getContextPath()%>/ProductoServlet?action=editar&id=<%= p.getProductoId() %>">✏</a>
		            <a href="<%=request.getContextPath()%>/ProductoServlet?action=eliminar&id=<%= p.getProductoId() %>"
		               onclick="return confirm('¿Eliminar producto?')">🗑</a>
		        </div>
		    </div>
		
		<% } } %>
		
		</section>

</main>

</body>
</html>
