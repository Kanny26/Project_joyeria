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
    <title>Eliminar Producto - AAC27</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/producto.css">
</head>
<body>


<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/CategoriaServlet?id=<%= producto.getCategoria().getCategoriaId() %>" class="navbar-admin__home-link">
	   
	    <span class="navbar-admin__home-icon-wrap">
	        <i class="fa-solid fa-arrow-left"></i>
		    <span class="navbar-admin__home-text">Volver atras</span>
		    <i class="fa-solid fa-house-chimney"></i>
	    </span>
    </a>
</nav>

<div class="page-header">
    <h1 class="product-title" style="color: #dc2626;">Eliminar Producto</h1>
</div>

<main class="product-page">
    <section class="product-card">

        <!-- ── IZQUIERDA: imagen ── -->
        <div class="product-image">
            <div class="product-image__circle">
                <img src="<%= request.getContextPath() %>/imagen-producto/<%= producto.getProductoId() %>"
                     alt="<%= producto.getNombre() %>">
            </div>
        </div>

        <!-- ── DERECHA: advertencia + acciones ── -->
        <div class="product-details" style="justify-content: center;">

            <div class="alert-box">
                <i class="fa-solid fa-triangle-exclamation"></i>
                <p>
                    <strong>Advertencia:</strong> Esta acción eliminará a
                    <b><%= producto.getNombre() %></b> permanentemente.
                    Esta acción no se puede deshacer.
                </p>
            </div>

            <div class="product-actions" style="margin-top: 30px;">
                <form action="<%=request.getContextPath()%>/ProductoServlet"
                      method="post" style="flex: 1;">
                    <input type="hidden" name="action" value="eliminar">
                    <input type="hidden" name="id"     value="<%= producto.getProductoId() %>">
                    <button type="submit" class="btn-danger" style="width: 100%;">
                        <i class="fa-solid fa-trash-can"></i> Eliminar Definitivamente
                    </button>
                </form>
                <button type="button" class="btn-primary" style="flex: 1;"
                        onclick="window.history.back()">
                    <i class="fa-solid fa-xmark"></i> Cancelar
                </button>
            </div>

        </div><!-- /product-details -->
    </section>
</main>

</body>
</html>
