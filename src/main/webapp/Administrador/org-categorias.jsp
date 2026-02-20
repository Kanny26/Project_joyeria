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
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>

<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    
    <a href="<%=request.getContextPath()%>/Administrador/admin-principal.jsp"
   class="navbar-admin__home-link">
	   
	    <span class="navbar-admin__home-icon-wrap">
	    
	        <i class="fa-solid fa-arrow-left"></i>
	        
		    <span class="navbar-admin__home-text">Volver atras</span>
		    
		    <i class="fa-solid fa-house-chimney"></i>
	    </span>
    </a>
</nav>

<main class="titulo">
    <h2 class="titulo__encabezado">Gestionar Categorías</h2>

    <section class="iconos-contenedor">
		
		<% if (categorias != null && !categorias.isEmpty()) {
		   for (Categoria c : categorias) { %>
		
		    <article class="iconos-item">
		        <a href="<%=request.getContextPath()%>/CategoriaServlet?id=<%= c.getCategoriaId() %>"
		           class="icono-boton">
		
		            <div class="icono-boton__circulo">
		                <img class="icono-boton__img"
		                     src="<%=request.getContextPath()%>/assets/Imagenes/iconos/<%= c.getIcono() %>"
		                     alt="<%= c.getNombre() %>">
		            </div>
		
		            <h3 class="icono-boton__titulo"><%= c.getNombre() %></h3>
		
		        </a>
		    </article>
		
		<% } } else { %>
		    <p>No hay categorías registradas</p>
		<% } %>
		
		</section>

</main>

</body>
</html>

