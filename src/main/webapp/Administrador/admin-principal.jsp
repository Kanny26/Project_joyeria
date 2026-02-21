<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="model.Administrador" %>
<%
    // No declarar session, usar el implícito
    Administrador admin = null;
    if (session != null) {
        admin = (Administrador) session.getAttribute("admin");
    }

    // Si no hay admin logueado, redirigir al login
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Home admin</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/Administrador/admin-principal.css">

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
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

<main class="panel-admin__tarjetas">
    <h2 class="panel-admin__tarjetas-titulo">
        ¡Bienvenido, <%= admin.getNombre() %>! Haz clic en lo que deseas gestionar hoy.
    </h2>
    <section class="iconos-contenedor">
	
	    <article class="iconos-item">
	        <a href="<%=request.getContextPath()%>/CategoriaServlet" class="icono-boton">
	            <div class="icono-boton__circulo">
	                <img class="icono-boton__img"
	                     src="<%=request.getContextPath()%>/assets/Imagenes/iconos/catalogar.png"
	                     alt="Administrar catálogo">
	            </div>
	            <h3 class="icono-boton__titulo">Categorías</h3>
	        </a>
	    </article>
	
	    <article class="iconos-item">
	        <a href="<%=request.getContextPath()%>/ProveedorServlet?accion=listar" class="icono-boton">
	            <div class="icono-boton__circulo">
	                <img class="icono-boton__img"
	                     src="<%=request.getContextPath()%>/assets/Imagenes/iconos/gestionar_proveedores.png"
	                     alt="Proveedores">
	            </div>
	            <h3 class="icono-boton__titulo">Proveedores</h3>
	        </a>
	    </article>
	
	    <article class="iconos-item">
	        <a href="<%=request.getContextPath()%>/Administrador/ventas.jsp" class="icono-boton">
	            <div class="icono-boton__circulo">
	                <img class="icono-boton__img"
	                     src="<%=request.getContextPath()%>/assets/Imagenes/iconos/ventas.png"
	                     alt="Ventas">
	            </div>
	            <h3 class="icono-boton__titulo">Ventas</h3>
	        </a>
	    </article>
	
	    <article class="iconos-item">
	        <a href="<%=request.getContextPath()%>/Administrador/usuarios.jsp" class="icono-boton">
	            <div class="icono-boton__circulo">
	                <img class="icono-boton__img"
	                     src="<%=request.getContextPath()%>/assets/Imagenes/iconos/Usuarios.png"
	                     alt="Usuarios">
	            </div>
	            <h3 class="icono-boton__titulo">Usuarios</h3>
	        </a>
	    </article>
	
	</section>

</main>

</body>
</html>

