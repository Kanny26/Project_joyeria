<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, model.Material, model.Administrador, model.Categoria, model.Producto, model.Subcategoria" %>
<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) { response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp"); return; }
    List<Material>     materiales    = (List<Material>)     request.getAttribute("materiales");
    List<Subcategoria> subcategorias = (List<Subcategoria>) request.getAttribute("subcategorias");
    Categoria          categoria     = (Categoria)          request.getAttribute("categoria");
    Producto           pRec          = (Producto)           request.getAttribute("producto");
    if (materiales == null || categoria == null) { response.sendRedirect(request.getContextPath() + "/CategoriaServlet"); return; }
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nuevo Producto - AAC27</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/forms-global.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/producto.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
<nav class="navbar-admin">
    <div class="navbar-admin__catalogo"><img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin"></div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%=request.getContextPath()%>/CategoriaServlet?id=<%= categoria.getCategoriaId() %>" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<main class="fs-container">
    <h2 class="fs-page-title"><i class="fa-solid fa-plus-circle"></i> Nuevo Producto — <%= categoria.getNombre() %></h2>

    <% if (error != null) { %>
    <div class="fs-alert-error"><i class="fa-solid fa-circle-exclamation"></i> <%= error %></div>
    <% } %>

    <form id="formProducto" class="fs-form" method="post"
          action="<%=request.getContextPath()%>/ProductoServlet"
          enctype="multipart/form-data" novalidate>
        <input type="hidden" name="action" value="guardar">
        <input type="hidden" name="categoriaId" value="<%= categoria.getCategoriaId() %>">

        <!-- SECCIÓN 1: Información básica -->
        <div class="fs-section">
            <div class="fs-section-title"><i class="fa-solid fa-tag"></i> Información del Producto</div>
            <div class="fs-grid fs-grid--3">
                <div class="fs-group">
                    <label class="fs-label" for="nombre"><i class="fa-solid fa-pen"></i> Nombre *</label>
                    <div class="fs-input-wrap">
                        <input id="nombre" type="text" name="nombre" class="fs-input"
                               value="<%= pRec != null ? pRec.getNombre() : "" %>" autocomplete="off">
                        <div class="fs-bubble" id="err-nombre">
                            <span class="fs-bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                            <span>El nombre es obligatorio.</span>
                        </div>
                    </div>
                </div>
                <div class="fs-group">
                    <label class="fs-label" for="precioUnitario"><i class="fa-solid fa-dollar-sign"></i> Precio de Costo *</label>
                    <div class="fs-input-wrap">
                        <input id="precioUnitario" type="number" name="precioUnitario" class="fs-input"
                               step="0.01" min="0.01" value="<%= pRec != null ? pRec.getPrecioUnitario() : "" %>">
                        <div class="fs-bubble" id="err-precioUnitario">
                            <span class="fs-bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                            <span>Ingresa un precio mayor a 0.</span>
                        </div>
                    </div>
                </div>
                <div class="fs-group">
                    <label class="fs-label" for="precioVenta"><i class="fa-solid fa-tag"></i> Precio de Venta *</label>
                    <div class="fs-input-wrap">
                        <input id="precioVenta" type="number" name="precioVenta" class="fs-input"
                               step="0.01" min="0.01" value="<%= pRec != null ? pRec.getPrecioVenta() : "" %>">
                        <div class="fs-bubble" id="err-precioVenta">
                            <span class="fs-bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                            <span>El precio de venta debe ser ≥ al de costo.</span>
                        </div>
                    </div>
                </div>
                <div class="fs-group">
                    <label class="fs-label" for="stock"><i class="fa-solid fa-boxes-stacked"></i> Stock *</label>
                    <div class="fs-input-wrap">
                        <input id="stock" type="number" name="stock" class="fs-input"
                               min="0" value="<%= pRec != null ? pRec.getStock() : "" %>">
                        <div class="fs-bubble" id="err-stock">
                            <span class="fs-bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                            <span>El stock no puede ser negativo.</span>
                        </div>
                    </div>
                </div>
                <div class="fs-group">
                    <label class="fs-label" for="materialId"><i class="fa-solid fa-gem"></i> Material *</label>
                    <div class="fs-input-wrap">
                        <select id="materialId" name="materialId" class="fs-input">
                            <option value="">Seleccione material</option>
                            <% for (Material m : materiales) { boolean sel = pRec != null && pRec.getMaterialId() == m.getMaterialId(); %>
                            <option value="<%= m.getMaterialId() %>" <%= sel ? "selected" : "" %>><%= m.getNombre() %></option>
                            <% } %>
                        </select>
                        <div class="fs-bubble" id="err-materialId">
                            <span class="fs-bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                            <span>Selecciona un material.</span>
                        </div>
                    </div>
                </div>
                <div class="fs-group">
                    <label class="fs-label" for="subcategoriaId"><i class="fa-solid fa-layer-group"></i> Subcategoría *</label>
                    <div class="fs-input-wrap">
                        <select id="subcategoriaId" name="subcategoriaId" class="fs-input">
                            <option value="">Seleccione subcategoría</option>
                            <% if (subcategorias != null) { for (Subcategoria s : subcategorias) { boolean sel = pRec != null && pRec.getSubcategoriaId() == s.getSubcategoriaId(); %>
                            <option value="<%= s.getSubcategoriaId() %>" <%= sel ? "selected" : "" %>><%= s.getNombre() %></option>
                            <% } } %>
                        </select>
                        <div class="fs-bubble" id="err-subcategoriaId">
                            <span class="fs-bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                            <span>Selecciona una subcategoría.</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- SECCIÓN 2: Descripción e imagen -->
        <div class="fs-section">
            <div class="fs-section-title"><i class="fa-solid fa-image"></i> Descripción e Imagen</div>
            <div class="fs-grid">
                <div class="fs-group fs-group--full">
                    <label class="fs-label" for="descripcion"><i class="fa-solid fa-align-left"></i> Descripción *</label>
                    <div class="fs-input-wrap">
                        <textarea id="descripcion" name="descripcion" class="fs-input" rows="3"><%= pRec != null ? pRec.getDescripcion() : "" %></textarea>
                        <div class="fs-bubble" id="err-descripcion">
                            <span class="fs-bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                            <span>La descripción es obligatoria.</span>
                        </div>
                    </div>
                </div>
                <div class="fs-group">
                    <label class="fs-label" for="imagen"><i class="fa-solid fa-cloud-arrow-up"></i> Imagen del Producto *</label>
                    <div class="fs-input-wrap">
                        <input id="imagen" type="file" name="imagen" class="fs-input"
                               accept="image/*" onchange="previsualizarImagen(this)">
                        <div class="fs-bubble" id="err-imagen">
                            <span class="fs-bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                            <span>Selecciona una imagen.</span>
                        </div>
                    </div>
                    <div class="fs-img-preview" id="previewWrap">
                        <img id="imgPreview" src="#" alt="Vista previa">
                        <span id="imgNombre"></span>
                    </div>
                </div>
            </div>
        </div>

        <div class="fs-actions">
            <button type="submit" class="fs-btn-save"><i class="fa-solid fa-floppy-disk"></i> Guardar Producto</button>
            <button type="button" class="fs-btn-cancel"
                    onclick="window.location.href='<%=request.getContextPath()%>/CategoriaServlet?id=<%= categoria.getCategoriaId() %>'">
                <i class="fa-solid fa-xmark"></i> Cancelar
            </button>
        </div>
    </form>
</main>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
const campos = ['nombre','precioUnitario','precioVenta','stock','materialId','subcategoriaId','imagen','descripcion'];

function esValido(id) {
    const el = document.getElementById(id);
    if (!el) return true;
    switch(id) {
        case 'nombre': return el.value.trim() !== '';
        case 'precioUnitario': return parseFloat(el.value) > 0;
        case 'precioVenta': return parseFloat(el.value) > 0 && parseFloat(el.value) >= (parseFloat(document.getElementById('precioUnitario').value)||0);
        case 'stock': return el.value !== '' && parseInt(el.value) >= 0;
        case 'materialId': return el.value !== '';
        case 'subcategoriaId': return el.value !== '';
        case 'imagen': return el.files && el.files.length > 0;
        case 'descripcion': return el.value.trim() !== '';
        default: return true;
    }
}
function mostrarErr(id) {
    const el = document.getElementById(id); if (!el) return;
    el.classList.add('invalid');
    const b = document.getElementById('err-' + id); if (b) b.classList.add('visible');
}
function ocultarErr(id) {
    const el = document.getElementById(id); if (!el) return;
    el.classList.remove('invalid');
    const b = document.getElementById('err-' + id); if (b) b.classList.remove('visible');
}
campos.forEach(id => {
    const el = document.getElementById(id);
    if (el) ['input','change'].forEach(ev => el.addEventListener(ev, () => { if(esValido(id)) ocultarErr(id); }));
});
function previsualizarImagen(input) {
    if (input.files && input.files[0]) {
        const r = new FileReader();
        r.onload = e => {
            document.getElementById('imgPreview').src = e.target.result;
            document.getElementById('imgNombre').textContent = input.files[0].name;
            document.getElementById('previewWrap').classList.add('visible');
        };
        r.readAsDataURL(input.files[0]);
        ocultarErr('imagen');
    }
}
document.getElementById('formProducto').addEventListener('submit', function(e) {
    e.preventDefault();
    let errores = 0;
    campos.forEach(id => { if (!esValido(id)) { mostrarErr(id); errores++; } else ocultarErr(id); });
    if (errores > 0) { Swal.fire({ icon:'warning', title:'Campos incompletos', text:'Revisa los campos marcados.' }); return; }
    const form = this;
    Swal.fire({
        title: '¿Guardar producto?', text: 'Se añadirá al catálogo de <%= categoria.getNombre() %>.',
        icon: 'question', showCancelButton: true,
        confirmButtonColor: '#7c3aed', cancelButtonColor: '#6b7280',
        confirmButtonText: 'Sí, guardar', cancelButtonText: 'Revisar'
    }).then(r => {
        if (r.isConfirmed) { Swal.fire({ title:'Guardando...', allowOutsideClick:false, didOpen:()=>Swal.showLoading() }); form.submit(); }
    });
});
</script>
</body>
</html>
