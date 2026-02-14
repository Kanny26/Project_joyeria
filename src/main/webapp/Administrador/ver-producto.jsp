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
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/producto.css">

</head>
<body>

<nav class="navbar-admin"> 
    <div class="navbar-admin__catalogo"> 
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin"> 
    </div> 

    <h1 class="navbar-admin__title">AAC27</h1> 

    <a href="<%= request.getContextPath() %>/CategoriaServlet?id=<%= producto.getCategoria().getCategoriaId() %>">
    	<i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
	</a>

</nav>

<main class="product-page">
    <h1 class="product-title">Ver producto</h1>

    <section class="product-card">
        <div class="product-image">
            <div class="product-image__circle">
                <img src="<%= request.getContextPath() %>/imagenes/<%= 
                    (producto.getImagen() != null && !producto.getImagen().isEmpty())
                        ? producto.getImagen()
                        : "default.jpg"
                %>" alt="<%= producto.getNombre() %>">
            </div>
        </div>

        <div class="product-details">

            <div class="product-row">
                <span class="product-label">Nombre</span>
                <span class="product-value">
                    <%= producto.getNombre() %>
                </span>
            </div>

            <div class="product-row">
                <span class="product-label">Categoría</span>
                <span class="product-value">
                    <%= producto.getCategoria().getNombre() %>
                </span>
            </div>

            <div class="product-row">
                <span class="product-label">Precio</span>
                <span class="product-value price">
                    $<%= String.format("%,.0f", producto.getPrecioVenta()) %>
                </span>
            </div>

            <div class="product-row">
                <span class="product-label">Descripción</span>
                <span class="product-value">
                    <%= producto.getDescripcion() != null 
                        ? producto.getDescripcion() 
                        : "Sin descripción" %>
                </span>
            </div>

            <div class="product-actions">
                <button type="button" class="btn-primary">
                    <a href="<%= request.getContextPath() %>/ProductoServlet?action=editar&id=<%= producto.getProductoId() %>">
                        <i class="fa-solid fa-pen"></i> Editar
                    </a>
                </button>

                <button type="button" class="btn-danger">
                    <a href="<%= request.getContextPath() %>/ProductoServlet?action=eliminar&id=<%= producto.getProductoId() %>">
                        <i class="fa-solid fa-trash-can"></i> Eliminar
                    </a>
                </button>
            </div>

        </div>
    </section>
</main>


</body>
</html>

