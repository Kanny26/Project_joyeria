<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="model.Producto, model.Administrador, java.util.List, model.Material, model.Subcategoria" %>
<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) { response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp"); return; }
    Producto           producto      = (Producto)           request.getAttribute("producto");
    List<Material>     materiales    = (List<Material>)     request.getAttribute("materiales");
    List<Subcategoria> subcategorias = (List<Subcategoria>) request.getAttribute("subcategorias");
    if (producto == null || materiales == null) { response.sendRedirect(request.getContextPath() + "/CategoriaServlet"); return; }
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar Producto - AAC27</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/forms-global.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/producto.css">
</head>
<body>
<nav class="navbar-admin">
    <div class="navbar-admin__catalogo"><img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin"></div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/CategoriaServlet?id=<%= producto.getCategoriaId() %>" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<main class="fs-container">
    <h2 class="fs-page-title"><i class="fa-solid fa-pen-to-square"></i> Editar Producto</h2>

    <% if (error != null) { %>
    <div class="fs-alert-error"><i class="fa-solid fa-circle-exclamation"></i> <%= error %></div>
    <% } %>

    <form id="formEditar" class="fs-form" method="post"
          action="<%= request.getContextPath() %>/ProductoServlet"
          enctype="multipart/form-data">
        <input type="hidden" name="action"       value="actualizar">
        <input type="hidden" name="productoId"   value="<%= producto.getProductoId() %>">
        <input type="hidden" name="imagenActual" value="<%= producto.getImagen() != null ? producto.getImagen() : "" %>">
        <input type="hidden" name="categoriaId"  value="<%= producto.getCategoriaId() %>">

        <!-- Layout: imagen izq + campos der -->
        <div class="fs-product-layout">

            <!-- Columna imagen -->
            <div class="fs-product-img-col">
                <div class="fs-product-img-circle">
                    <img id="preview"
                         src="<%= request.getContextPath() %>/assets/Imagenes/productos/<%= producto.getImagen() %>"
                         alt="Vista previa"
                         onerror="this.onerror=null;this.src='<%= request.getContextPath() %>/imagen-producto/<%= producto.getProductoId() %>';">
                </div>
                <label for="imagenInput" class="fs-upload-btn">
                    <i class="fa-solid fa-cloud-arrow-up"></i> Cambiar imagen
                </label>
                <input type="file" name="imagen" id="imagenInput" accept="image/*" style="display:none"
                       onchange="handleImageChange(this)">
                <span id="file-name" class="fs-file-name">Sin cambios</span>
            </div>

            <!-- Columna campos -->
            <div>
                <!-- Datos básicos -->
                <div class="fs-section">
                    <div class="fs-section-title"><i class="fa-solid fa-tag"></i> Datos del Producto</div>
                    <div class="fs-grid">
                        <div class="fs-group">
                            <label class="fs-label"><i class="fa-solid fa-pen"></i> Nombre *</label>
                            <input type="text" name="nombre" class="fs-input" value="<%= producto.getNombre() %>" required>
                        </div>
                        <div class="fs-group">
                            <label class="fs-label"><i class="fa-solid fa-dollar-sign"></i> Precio de Costo *</label>
                            <input type="number" name="precioUnitario" class="fs-input" step="0.01" min="0.01" value="<%= producto.getPrecioUnitario() %>" required>
                        </div>
                        <div class="fs-group">
                            <label class="fs-label"><i class="fa-solid fa-tag"></i> Precio de Venta *</label>
                            <input type="number" name="precioVenta" class="fs-input" step="0.01" min="0.01" value="<%= producto.getPrecioVenta() %>" required>
                        </div>
                        <div class="fs-group">
                            <label class="fs-label"><i class="fa-solid fa-boxes-stacked"></i> Stock *</label>
                            <input type="number" name="stock" class="fs-input" min="0" value="<%= producto.getStock() %>" required>
                        </div>
                        <div class="fs-group">
                            <label class="fs-label"><i class="fa-solid fa-gem"></i> Material *</label>
                            <select name="materialId" class="fs-input" required>
                                <% for (Material m : materiales) { boolean sel = producto.getMaterialId() == m.getMaterialId(); %>
                                <option value="<%= m.getMaterialId() %>" <%= sel ? "selected" : "" %>><%= m.getNombre() %></option>
                                <% } %>
                            </select>
                        </div>
                        <div class="fs-group">
                            <label class="fs-label"><i class="fa-solid fa-layer-group"></i> Subcategoría</label>
                            <select name="subcategoriaId" class="fs-input">
                                <option value="">— Sin subcategoría —</option>
                                <% if (subcategorias != null) { for (Subcategoria s : subcategorias) { boolean sel = producto.getSubcategoriaId() == s.getSubcategoriaId(); %>
                                <option value="<%= s.getSubcategoriaId() %>" <%= sel ? "selected" : "" %>><%= s.getNombre() %></option>
                                <% } } %>
                            </select>
                        </div>
                        <div class="fs-group fs-group--full">
                            <label class="fs-label"><i class="fa-solid fa-align-left"></i> Descripción *</label>
                            <textarea name="descripcion" class="fs-input" rows="3" required><%= producto.getDescripcion() %></textarea>
                        </div>
                    </div>
                </div>

                <div class="fs-actions" style="margin-top:20px;padding-top:20px;">
                    <button type="submit" class="fs-btn-save"><i class="fa-solid fa-floppy-disk"></i> Guardar Cambios</button>
                    <button type="button" class="fs-btn-cancel" onclick="window.history.back()"><i class="fa-solid fa-xmark"></i> Cancelar</button>
                </div>
            </div>
        </div>
    </form>
</main>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
function handleImageChange(input) {
    if (input.files && input.files[0]) {
        document.getElementById('file-name').textContent = input.files[0].name;
        const r = new FileReader();
        r.onload = e => document.getElementById('preview').src = e.target.result;
        r.readAsDataURL(input.files[0]);
    }
}
document.getElementById('formEditar').addEventListener('submit', function(e) {
    e.preventDefault();
    const form = this;
    Swal.fire({
        title: '¿Aplicar cambios?', text: 'La información del producto será actualizada.',
        icon: 'question', showCancelButton: true,
        confirmButtonColor: '#7c3aed', cancelButtonColor: '#6b7280',
        confirmButtonText: 'Confirmar', cancelButtonText: 'Cancelar'
    }).then(r => {
        if (r.isConfirmed) { Swal.fire({ title:'Guardando...', allowOutsideClick:false, didOpen:()=>Swal.showLoading() }); form.submit(); }
    });
});
</script>
</body>
</html>
