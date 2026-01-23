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
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ver producto</title>

    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">

    <!-- CSS -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/ver-producto.css">
</head>
<body>

<nav class="navbar-admin"> 
    <div class="navbar-admin__catalogo"> 
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin"> 
    </div> 

    <h1 class="navbar-admin__title">AAC27</h1> 

    <a href="<%= request.getContextPath() %>/Administrador/org-categorias.jsp"> 
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i> 
    </a>
</nav>

<main class="ver-producto">
    <h1 class="ver-producto__titulo">Ver producto</h1>

    <div class="ver-producto__card">

        <!-- IMAGEN -->
        <div class="ver-producto__card-imagen">
            <img src="<%= request.getContextPath() %>/imagenes/<%= 
                (producto.getImagen() != null && !producto.getImagen().isEmpty())
                    ? producto.getImagen()
                    : "default.jpg"
            %>" alt="<%= producto.getNombre() %>">
        </div>

        <!-- DETALLES -->
        <div class="ver-producto__card-detalles">

            <div class="ver-producto__card-detalles-fila">
                <span class="ver-producto__card-detalles-titulo">Nombre:</span>
                <span class="ver-producto__card-detalles-nombre">
                    <%= producto.getNombre() %>
                </span>
            </div>

            <div class="ver-producto__card-detalles-fila">
                <span class="ver-producto__card-detalles-titulo">Categoría:</span>
                <span class="ver-producto__card-detalles-categoria">
                    <%= producto.getCategoria().getNombre() %>
                </span>
            </div>

			<div class="ver-producto__card-detalles-fila">
            
                <span class="ver-producto__card-detalles-titulo">Precio de costo:</span>
                <span class="ver-producto__card-detalles-precio">
                    $<%= String.format("%,.0f", producto.getPrecioUnitario()) %>
                </span>
            </div>
			
            <div class="ver-producto__card-detalles-fila">
            
                <span class="ver-producto__card-detalles-titulo">Precio de venta:</span>
                <span class="ver-producto__card-detalles-precio">
                    $<%= String.format("%,.0f", producto.getPrecioVenta()) %>
                </span>
            </div>

            <div class="ver-producto__card-detalles-fila">
                <span class="ver-producto__card-detalles-titulo">Material:</span>
                <span class="ver-producto__card-detalles-material">
                    <%= producto.getMaterial().getNombre() %>
                </span>
            </div>

            <div class="ver-producto__card-detalles-fila">
                <span class="ver-producto__card-detalles-titulo">Descripción:</span>
                <span class="ver-producto__card-detalles-descripcion">
                    <%= producto.getDescripcion() != null 
                        ? producto.getDescripcion() 
                        : "Sin descripción" %>
                </span>
            </div>

            <!-- CALIFICACIÓN (estática por ahora, como en el HTML) -->
            <div class="ver-producto__card-calificaciones">
                <i class="fas fa-star"></i>
                <i class="fas fa-star"></i>
                <i class="fas fa-star"></i>
                <i class="fas fa-star"></i>
                <i class="fas fa-star-half-alt"></i>
                <span>4.8 Reseñas</span>
            </div>

            <!-- ACCIONES -->
            <div class="ver-producto__card-acciones">
                <button class="boton_view_edit edit-image-btn">  
                    <a href="<%= request.getContextPath() %>/ProductoServlet?action=editar&id=<%= producto.getProductoId() %>">
                        <i class="fa-solid fa-pen"></i> Editar producto  
                    </a>
                </button>  

                <button class="boton-confirmar eliminar"> 
                    <a href="<%= request.getContextPath() %>/ProductoServlet?action=eliminar&id=<%= producto.getProductoId() %>">
                        <i class="fa-solid fa-trash-can"></i> Eliminar producto 
                    </a> 
                </button>
            </div>

        </div>
    </div>
</main>

</body>
</html>

