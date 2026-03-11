<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="model.Producto, model.Administrador, java.util.List, model.Material, model.Subcategoria, model.Proveedor" %>
<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) { response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp"); return; }

    Producto           producto      = (Producto)           request.getAttribute("producto");
    List<Material>     materiales    = (List<Material>)     request.getAttribute("materiales");
    List<Subcategoria> subcategorias = (List<Subcategoria>) request.getAttribute("subcategorias");
    List<Proveedor>    proveedores   = (List<Proveedor>)    request.getAttribute("proveedores");

    if (producto == null || materiales == null) {
        response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
        return;
    }
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar Producto - AAC27</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/forms-global.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/producto.css">
    <style>
        .campo-protegido {
            background: #f1f0f7 !important;
            color: #4b5563 !important;
            cursor: not-allowed !important;
            font-weight: 600 !important;
        }
        .campo-protegido-info {
            font-size: .72rem;
            color: #6b7280;
            margin-top: 4px;
            display: flex;
            align-items: center;
            gap: 5px;
        }
        .campo-protegido-info i { color: #9177a8; }
    </style>
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

<main class="fs-container">
    <h2 class="fs-page-title">
        <i class="fa-solid fa-pen-to-square"></i> Editar Producto
    </h2>

    <% if (error != null) { %>
    <div class="fs-alert-error">
        <i class="fa-solid fa-circle-exclamation"></i> <%= error %>
    </div>
    <% } %>

    <% if (producto.getProveedorId() <= 0) { %>
    <div class="fs-alert-error" style="background:#fff3cd; border-color:#ffc107; color:#856404;">
        <i class="fa-solid fa-triangle-exclamation"></i>
        Este producto no tiene proveedor asignado. Debes seleccionar uno para poder guardarlo.
    </div>
    <% } %>

    <form id="formEditar" class="fs-form" method="post"
          action="<%= request.getContextPath() %>/ProductoServlet"
          enctype="multipart/form-data">

        <input type="hidden" name="action"        value="actualizar">
        <input type="hidden" name="productoId"    value="<%= producto.getProductoId() %>">
        <input type="hidden" name="imagenActual"  value="<%= producto.getImagen() != null ? producto.getImagen() : "" %>">
        <input type="hidden" name="categoriaId"   value="<%= producto.getCategoriaId() %>">
        <%-- Enviamos precio costo real como hidden para que el servlet lo reciba aunque el visible sea readonly --%>
        <input type="hidden" name="precioUnitario" id="precioUnitarioHidden" value="<%= producto.getPrecioUnitario() %>">

        <div class="fs-product-layout">

            <!-- ── Columna imagen ── -->
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
                <input type="file" name="imagen" id="imagenInput" accept="image/*"
                       style="display:none" onchange="handleImageChange(this)">
                <span id="file-name" class="fs-file-name">Sin cambios</span>
            </div>

            <!-- ── Columna datos ── -->
            <div>
                <div class="fs-section">
                    <div class="fs-section-title">
                        <i class="fa-solid fa-tag"></i> Datos del Producto
                    </div>
                    <div class="fs-grid">

                        <!-- Nombre -->
                        <div class="fs-group">
                            <label class="fs-label">
                                <i class="fa-solid fa-pen"></i> Nombre *
                            </label>
                            <input type="text" name="nombre" id="nombre" class="fs-input"
                                   value="<%= producto.getNombre() %>"
                                   pattern="^[a-zA-Z0-9\sñÑáéíóúÁÉÍÓÚ]+$"
                                   title="Solo se permiten letras y números" required>
                        </div>

                        <!-- Precio Costo — PROTEGIDO (actualizado vía Compras) -->
                        <div class="fs-group">
                            <label class="fs-label">
                                <i class="fa-solid fa-dollar-sign"></i> Precio de Costo
                            </label>
                            <input type="number" class="fs-input campo-protegido"
                                   value="<%= producto.getPrecioUnitario() %>"
                                   readonly tabindex="-1">
                            <span class="campo-protegido-info">
                                <i class="fa-solid fa-lock"></i>
                                Se actualiza automáticamente al registrar una Compra.
                            </span>
                        </div>

                        <!-- Precio Venta -->
                        <div class="fs-group">
                            <label class="fs-label">
                                <i class="fa-solid fa-tag"></i> Precio de Venta *
                            </label>
                            <input type="number" name="precioVenta" id="precioVenta"
                                   class="fs-input" step="0.01" min="0.01"
                                   value="<%= producto.getPrecioVenta() %>" required>
                        </div>

                        <!-- Stock — PROTEGIDO (actualizado vía Ventas o Compras) -->
                        <div class="fs-group">
                            <label class="fs-label">
                                <i class="fa-solid fa-boxes-stacked"></i> Stock actual
                            </label>
                            <input type="number" class="fs-input campo-protegido"
                                   value="<%= producto.getStock() %>"
                                   readonly tabindex="-1">
                            <span class="campo-protegido-info">
                                <i class="fa-solid fa-lock"></i>
                                Se actualiza vía Ventas (descuenta) o Compras (aumenta).
                            </span>
                        </div>

                        <!-- Material -->
                        <div class="fs-group">
                            <label class="fs-label">
                                <i class="fa-solid fa-gem"></i> Material *
                            </label>
                            <select name="materialId" class="fs-input" required>
                                <% for (Material m : materiales) { %>
                                <option value="<%= m.getMaterialId() %>"
                                    <%= producto.getMaterialId() == m.getMaterialId() ? "selected" : "" %>>
                                    <%= m.getNombre() %>
                                </option>
                                <% } %>
                            </select>
                        </div>

                        <!-- Subcategoría -->
                        <div class="fs-group">
                            <label class="fs-label">
                                <i class="fa-solid fa-layer-group"></i> Subcategoría
                            </label>
                            <select name="subcategoriaId" class="fs-input">
                                <option value="">— Sin subcategoría —</option>
                                <% if (subcategorias != null) {
                                    for (Subcategoria s : subcategorias) { %>
                                <option value="<%= s.getSubcategoriaId() %>"
                                    <%= producto.getSubcategoriaId() == s.getSubcategoriaId() ? "selected" : "" %>>
                                    <%= s.getNombre() %>
                                </option>
                                <%  }
                                   } %>
                            </select>
                        </div>

                        <!-- Proveedor -->
                        <div class="fs-group fs-group--full">
                            <label class="fs-label">
                                <i class="fa-solid fa-truck" style="color:#7c3aed;"></i> Proveedor *
                            </label>
                            <select name="proveedorId" id="proveedorId" class="fs-input" required>
                                <option value="">— Selecciona un proveedor —</option>
                                <% if (proveedores != null) {
                                    for (Proveedor prov : proveedores) {
                                        boolean seleccionado = producto.getProveedorId() == prov.getProveedorId();
                                %>
                                <option value="<%= prov.getProveedorId() %>"
                                    <%= seleccionado ? "selected" : "" %>
                                    <%= !prov.isEstado() ? "style=\"color:#9ca3af;\"" : "" %>>
                                    <%= prov.getNombre() %><%= !prov.isEstado() ? " (Inactivo)" : "" %>
                                </option>
                                <%  }
                                   } %>
                            </select>
                            <span class="campo-protegido-info">
                                <i class="fa-solid fa-circle-info"></i>
                                Solo aparecerán los productos de este proveedor al registrar compras.
                            </span>
                        </div>

                        <!-- Descripción -->
                        <div class="fs-group fs-group--full">
                            <label class="fs-label">
                                <i class="fa-solid fa-align-left"></i> Descripción *
                            </label>
                            <textarea name="descripcion" id="descripcion" class="fs-input"
                                      rows="3" minlength="10" maxlength="500"
                                      required><%= producto.getDescripcion() != null ? producto.getDescripcion() : "" %></textarea>
                            <small id="charCount" style="color:#9ca3af; font-size:.7rem;">
                                Escribe mínimo 10 caracteres.
                            </small>
                        </div>

                    </div>
                </div>

                <div class="fs-actions" style="margin-top:20px; padding-top:20px;">
                    <button type="submit" class="fs-btn-save">
                        <i class="fa-solid fa-floppy-disk"></i> Guardar Cambios
                    </button>
                    <button type="button" class="fs-btn-cancel" onclick="window.history.back()">
                        <i class="fa-solid fa-xmark"></i> Cancelar
                    </button>
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
        const reader = new FileReader();
        reader.onload = e => document.getElementById('preview').src = e.target.result;
        reader.readAsDataURL(input.files[0]);
    }
}

const descInput = document.getElementById('descripcion');
const charCount = document.getElementById('charCount');
function actualizarContador() {
    const len = descInput.value.length;
    charCount.textContent = len < 10
        ? `Escribe mínimo 10 caracteres (faltan ${10 - len}).`
        : `${len} / 500 caracteres.`;
    charCount.style.color = len < 10 ? '#ef4444' : '#9ca3af';
}
descInput.addEventListener('input', actualizarContador);
actualizarContador();

document.getElementById('formEditar').addEventListener('submit', function(e) {
    e.preventDefault();

    const nombre    = document.getElementById('nombre').value.trim();
    const costo     = parseFloat(document.getElementById('precioUnitarioHidden').value);
    const venta     = parseFloat(document.getElementById('precioVenta').value);
    const desc      = document.getElementById('descripcion').value.trim();
    const proveedor = document.getElementById('proveedorId').value;

    if (!/^[a-zA-Z0-9\sñÑáéíóúÁÉÍÓÚ]+$/.test(nombre)) {
        Swal.fire('Nombre inválido', 'El nombre solo debe contener letras y números.', 'error');
        return;
    }

    if (!proveedor || proveedor === '') {
        Swal.fire({ icon: 'warning', title: 'Proveedor requerido', text: 'Debes seleccionar un proveedor.', confirmButtonColor: '#7c3aed' });
        document.getElementById('proveedorId').focus();
        return;
    }

    if (costo >= venta) {
        Swal.fire({ icon: 'warning', title: 'Error en Precios', text: 'El precio de venta debe ser mayor al precio de costo.', confirmButtonColor: '#7c3aed' });
        return;
    }

    if (desc.length < 10) {
        Swal.fire({ icon: 'info', title: 'Descripción breve', text: 'Brinde una descripción más detallada (mín. 10 caracteres).', confirmButtonColor: '#7c3aed' });
        return;
    }

    if (/(.)\1{4,}/.test(desc.replace(/\s/g, ''))) {
        Swal.fire({ icon: 'error', title: 'Contenido inválido', text: 'La descripción parece contener texto repetitivo.', confirmButtonColor: '#7c3aed' });
        return;
    }

    Swal.fire({
        title: '¿Confirmar cambios?',
        text: 'Se actualizará la información del producto.',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#7c3aed',
        cancelButtonColor: '#6b7280',
        confirmButtonText: 'Sí, guardar',
        cancelButtonText: 'Revisar'
    }).then(result => {
        if (result.isConfirmed) {
            Swal.fire({ title: 'Procesando...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
            this.submit();
        }
    });
});
</script>
</body>
</html>
