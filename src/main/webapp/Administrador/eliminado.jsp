<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Producto" %>

<%
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
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Producto eliminado</title>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css" />
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/eliminado.css" />
</head>
<body>
<nav class="navbar-admin"> 
    <div class="navbar-admin__catalogo"> 
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin"> 
    </div> 
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%=request.getContextPath()%>/ProductoServlet">
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i> 
    </a> 
</nav>

<main class="eliminado">
    <h1 class="eliminado__titulo">Producto eliminado</h1>

    <div class="eliminado__contenedor">
    	<div class="eliminar-producto__imagen">
	        <img src="<%=request.getContextPath()%>/imagenes/<%= producto.getImagen() %>" 
	             alt="<%= producto.getNombre() %>" />
         </div>

        <div class="eliminado__mensaje">
            <h2>¡El producto "<%= producto.getNombre() %>" ya no se encuentra disponible!</h2>

            <a href="<%=request.getContextPath()%>/ProductoServlet"
               class="eliminado__boton-volver">
                Gestionar productos
            </a>
        </div>
    </div>
</main>
</body>
</html>

