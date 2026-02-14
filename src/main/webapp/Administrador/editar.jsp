<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="model.Producto, model.Administrador, java.util.List, model.Material" %>

<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }

    Producto producto = (Producto) request.getAttribute("producto");
    List<Material> materiales = (List<Material>) request.getAttribute("materiales");

    if (producto == null || materiales == null) {
        response.sendRedirect(request.getContextPath() + "/ProductoServlet");
        return;
    }
%>
<%
    String error = (String) request.getAttribute("error");
    if (error != null) {
%>
    <div class="alert alert-danger">
        <%= error %>
    </div>
<%
    }
%>


<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar producto</title>
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
    <a href="<%= request.getContextPath() %>/CategoriaServlet?id=<%= producto.getCategoria().getCategoriaId() %>">
    	<i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
	</a>

</nav>
<main class="product-page">
    <h1 class="product-title">Editar producto</h1>

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

        <form class="product-details"
              action="<%= request.getContextPath() %>/ProductoServlet"
              method="post"
              enctype="multipart/form-data">

            <input type="hidden" name="action" value="actualizar">
            <input type="hidden" name="productoId" value="<%= producto.getProductoId() %>">
            <input type="hidden" name="imagenActual" value="<%= producto.getImagen() != null ? producto.getImagen() : "" %>">
            <input type="hidden" name="categoriaId" value="<%= producto.getCategoria().getCategoriaId() %>">
            <input type="hidden" name="stock" value="<%= producto.getStock() %>">

            <div class="product-row">
                <label class="product-label">Nombre</label>
                <input class="product-value" 
                       type="text" 
                       name="nombre"
                       value="<%= producto.getNombre() %>" 
                       required>
            </div>

            <div class="product-row">
                <label class="product-label">Categoría</label>
                <input class="product-value" 
                       type="text" 
                       value="<%= producto.getCategoria().getNombre() %>" 
                       disabled>
            </div>

            <div class="product-row">
                <label class="product-label">Precio de costo</label>
                <input class="product-value price" 
                       type="number" 
                       step="0.05"
                       name="precioUnitario"
                       value="<%= producto.getPrecioUnitario() != null ? producto.getPrecioUnitario().toString() : "0.00" %>" 
                       required>
            </div>

            <div class="product-row">
                <label class="product-label">Precio de venta</label>
                <input class="product-value price" 
                       type="number" 
                       step="0.05"
                       name="precioVenta"
                       value="<%= producto.getPrecioVenta() != null ? producto.getPrecioVenta().toString() : "0.00" %>" 
                       required>
            </div>

            <div class="product-row">
                <label class="product-label">Material</label>
                <select class="product-value" name="materialId" required>
                    <% for (Material m : materiales) { %>
                        <option value="<%= m.getMaterialId() %>"
                            <%= producto.getMaterial().getMaterialId() == m.getMaterialId() ? "selected" : "" %>>
                            <%= m.getNombre() %>
                        </option>
                    <% } %>
                </select>
            </div>

            <div class="product-row">
                <label class="product-label">Descripción</label>
                <textarea class="product-value" 
                          name="descripcion" 
                          rows="4"><%= producto.getDescripcion() != null ? producto.getDescripcion() : "" %></textarea>
            </div>

            <div class="product-actions">
			    <button type="submit" class="btn-primary">
			        <i class="fa-solid fa-floppy-disk"></i> Guardar
			    </button>
			
			    <button type="button" class="btn-danger"
			        onclick="window.location.href='<%= request.getContextPath() %>/CategoriaServlet?id=<%= producto.getCategoria().getCategoriaId() %>'">
			        <i class="fa-solid fa-xmark"></i> Cancelar
			    </button>
			</div>

        </form>
    </section>
</main>

</body>
</html>
