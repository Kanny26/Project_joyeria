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
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Editar Producto - AAC27</title>
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
    <a href="<%= request.getContextPath() %>/CategoriaServlet?id=<%= producto.getCategoriaId() %>" 
       class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<div class="page-header">
    <h1 class="product-title">Editar Producto</h1>
</div>

<% if (error != null) { %>
    <div class="alert-server-edit">
        <i class="fa-solid fa-circle-exclamation"></i> <%= error %>
    </div>
<% } %>

<main class="product-page">
    <form action="<%= request.getContextPath() %>/ProductoServlet" method="post"
          enctype="multipart/form-data" class="full-width">

        <input type="hidden" name="action" value="actualizar">
        <input type="hidden" name="productoId" value="<%= producto.getProductoId() %>">
        <input type="hidden" name="imagenActual" value="<%= producto.getImagen() != null ? producto.getImagen() : "" %>">
        <input type="hidden" name="categoriaId" value="<%= producto.getCategoriaId() %>">
        <input type="hidden" name="stock" value="<%= producto.getStock() %>">

        <section class="product-card">
            <!-- IZQUIERDA: imagen -->
            <div class="product-image">
                <div class="product-image__circle">
                    <img id="preview"
                         src="<%= request.getContextPath() %>/imagen-producto/<%= producto.getProductoId() %>"
                         alt="Vista previa">
                </div>
                <div class="custom-file-upload">
                    <label for="imagenInput" class="btn-upload">
                        <i class="fa-solid fa-cloud-arrow-up"></i> Cambiar Imagen
                    </label>
                    <input type="file" name="imagen" id="imagenInput"
                           accept="image/*" onchange="handleImageChange(this)">
                    <span id="file-name" class="file-name">Ningún archivo seleccionado</span>
                </div>
            </div>

            <!-- DERECHA: detalles -->
            <div class="product-details">
                <div class="product-row">
                    <label class="product-label">Nombre *</label>
                    <input class="product-value" type="text" name="nombre"
                           value="<%= producto.getNombre() %>" required>
                </div>

                <div class="product-row">
                    <label class="product-label">Precio de Costo *</label>
                    <input class="product-value" type="number" step="0.01" min="0.01"
                           name="precioUnitario" value="<%= producto.getPrecioUnitario() %>" required>
                </div>

                <div class="product-row">
                    <label class="product-label">Precio de Venta *</label>
                    <input class="product-value" type="number" step="0.01" min="0.01"
                           name="precioVenta" value="<%= producto.getPrecioVenta() %>" required>
                </div>

                <!-- MATERIAL (CORREGIDO: usar getMaterialId()) -->
                <div class="product-row">
                    <label class="product-label">Material *</label>
                    <select class="product-value" name="materialId" required>
                        <% for (Material m : materiales) { 
                            boolean selected = (producto.getMaterialId() == m.getMaterialId());
                        %>
                            <option value="<%= m.getMaterialId() %>" <%= selected ? "selected" : "" %>>
                                <%= m.getNombre() %>
                            </option>
                        <% } %>
                    </select>
                </div>

                <div class="product-row">
                    <label class="product-label">Descripción *</label>
                    <textarea class="product-value" name="descripcion" rows="4"><%= producto.getDescripcion() %></textarea>
                </div>

                <div class="product-actions">
                    <button type="submit" class="btn-primary">
                        <i class="fa-solid fa-floppy-disk"></i> Guardar Cambios
                    </button>
                    <button type="button" class="btn-danger"
                            onclick="window.history.back()">
                        <i class="fa-solid fa-xmark"></i> Cancelar
                    </button>
                </div>
            </div>
        </section>
    </form>
</main>

<script>
function handleImageChange(input) {
    const preview = document.getElementById('preview');
    const fileNameDisplay = document.getElementById('file-name');
    if (input.files && input.files[0]) {
        fileNameDisplay.textContent = input.files[0].name;
        const reader = new FileReader();
        reader.onload = (e) => preview.src = e.target.result;
        reader.readAsDataURL(input.files[0]);
    }
}
</script>

</body>
</html>