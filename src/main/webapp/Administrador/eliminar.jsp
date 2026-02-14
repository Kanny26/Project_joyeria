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
    <title>Eliminar producto</title>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css" />
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/producto.css">
</head>
<body>

<nav class="navbar-admin"> 
    <div class="navbar-admin__catalogo"> 
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin"> 
    </div> 
    <h1 class="navbar-admin__title">AAC27</h1> 
    <a href="<%= request.getContextPath() %>/CategoriaServlet?id=<%= producto.getCategoria().getCategoriaId() %>">
    	<i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
	</a>

</nav>
<main class="product-page product-page--delete">
    <h1 class="product-title">Eliminar producto</h1>

    <section class="product-card">
        <div class="product-image">
            <div class="product-image__circle">
                <img src="<%=request.getContextPath()%>/imagenes/<%= 
                    (producto.getImagen() != null && !producto.getImagen().isEmpty())
                        ? producto.getImagen()
                        : "default.jpg"
                %>" alt="<%= producto.getNombre() %>">
            </div>
        </div>

        <div class="product-details">

            <p class="product-value">
                ⚠ <strong>Advertencia:</strong> esta acción eliminará el producto
                de forma permanente y no se puede deshacer.
            </p>

            <div class="product-actions">

                <!-- ELIMINAR -->
                <form action="<%=request.getContextPath()%>/ProductoServlet" method="post">
                    <input type="hidden" name="action" value="eliminar">
                    <input type="hidden" name="id" value="<%= producto.getProductoId() %>">

                    <button type="submit" class="btn-danger">
                        <i class="fa-solid fa-trash-can"></i> Eliminar definitivamente
                    </button>
                </form>

                <!-- CANCELAR -->
                <button type="button" class="btn-primary"
                    onclick="window.location.href='<%=request.getContextPath()%>/ProductoServlet?action=ver&id=<%= producto.getProductoId() %>'">
                    <i class="fa-solid fa-xmark"></i> Cancelar
                </button>

            </div>

        </div>
    </section>
</main>


</body>
</html>

