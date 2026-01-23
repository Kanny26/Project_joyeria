<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, model.Categoria, model.Administrador, model.Material, model.Producto" %>

<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }

    List<Categoria> categorias = (List<Categoria>) request.getAttribute("categorias");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Gestionar Categorías</title>

    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/org-categorias.css">
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
    <h2 class="titulo__encabezado">Gestionar Categorías</h2>

    <section class="catalogo-admin__contenedor">

        <% if (categorias != null && !categorias.isEmpty()) {
            for (Categoria c : categorias) { %>

            <article class="catalogo-admin__contenedor-item">
			    <a href="<%=request.getContextPath()%>/CategoriaServlet?id=<%= c.getCategoriaId() %>">"
			       class="catalogo-admin__tarjeta">
				<div class=catalogo-admin__icono>
					<img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/<%= c.getIcono() %>"
			             alt="<%= c.getNombre() %>">
			
				</div>
			      
			        <h3 class="catalogo-admin__titulo-tarjeta">
			            <%= c.getNombre() %>
			        </h3>
			    </a>
			</article>


        <% } } else { %>
            <p>No hay categorías registradas</p>
        <% } %>

    </section>
</main>

</body>
</html>

