<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, model.Material, model.Administrador, model.Categoria, model.Producto, model.Subcategoria, model.Proveedor" %>
<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) { response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp"); return; }

    List<Material>     materiales    = (List<Material>)     request.getAttribute("materiales");
    List<Subcategoria> subcategorias = (List<Subcategoria>) request.getAttribute("subcategorias");
    List<Proveedor>    proveedores   = (List<Proveedor>)    request.getAttribute("proveedores");
    Categoria          categoria     = (Categoria)          request.getAttribute("categoria");
    Producto           pRec          = (Producto)           request.getAttribute("producto");

    if (materiales == null || categoria == null) {
        response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
        return;
    }

    String error = (String) request.getAttribute("error");

    // IDs de subcategorías preseleccionadas (cuando hay error y se reenvía el formulario)
    java.util.List<Integer> subcatSeleccionadas = (pRec != null && pRec.getSubcategoriaIds() != null)
        ? pRec.getSubcategoriaIds()
        : new java.util.ArrayList<>();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nuevo Producto - AAC27</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/forms-global.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/producto.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <style>
        /* ── Checkboxes de subcategoría ── */
        .subcat-grid {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            padding: 10px 0 4px;
        }
        .subcat-chip {
            display: flex;
            align-items: center;
            gap: 6px;
            background: #f3f0f9;
            border: 1.5px solid #d1c4e9;
            border-radius: 20px;
            padding: 5px 14px;
            cursor: pointer;
            font-size: 0.82rem;
            color: #4b3f72;
            transition: background 0.15s, border-color 0.15s;
            user-select: none;
        }
        .subcat-chip input[type="checkbox"] {
            accent-color: #9177a8;
            width: 15px;
            height: 15px;
            cursor: pointer;
        }
        .subcat-chip:has(input:checked) {
            background: #e8e0f5;
            border-color: #9177a8;
            font-weight: 600;
        }
        .subcat-hint {
            font-size: 0.75rem;
            color: #6b7280;
            margin-top: 4px;
        }
    </style>
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%=request.getContextPath()%>/CategoriaServlet?id=<%= categoria.getCategoriaId() %>"
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
        <i class="fa-solid fa-plus-circle"></i> Nuevo Producto — <%= categoria.getNombre() %>
    </h2>

    <% if (error != null) { %>
    <div class="fs-alert-error">
        <i class="fa-solid fa-circle-exclamation"></i> <%= error %>
    </div>
    <% } %>

    <form id="formProducto" class="fs-form" method="post"
          action="<%=request.getContextPath()%>/ProductoServlet"
          enctype="multipart/form-data" novalidate>

        <input type="hidden" name="action"      value="guardar">
        <input type="hidden" name="categoriaId" value="<%= categoria.getCategoriaId() %>">

        <!-- ── SECCIÓN 1: Información del Producto ── -->
        <div class="fs-section">
            <div class="fs-section-title">
                <i class="fa-solid fa-tag"></i> Información del Producto
            </div>
            <div class="fs-grid fs-grid--2">

                <!-- Nombre -->
                <div class="fs-group">
                    <label class="fs-label" for="nombre">
                        <i class="fa-solid fa-pen"></i> Nombre *
                    </label>
                    <div class="fs-input-wrap">
                        <input id="nombre" type="text" name="nombre" class="fs-input"
                               value="<%= pRec != null ? pRec.getNombre() : "" %>"
                               placeholder="Ej: Anillo 360 Oro" autocomplete="off">
                        <div class="fs-bubble" id="err-nombre">
                            <span class="fs-bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                            <span>Solo letras y números (sin caracteres especiales).</span>
                        </div>
                    </div>
                </div>

                <!-- Material -->
                <div class="fs-group">
                    <label class="fs-label" for="materialId">
                        <i class="fa-solid fa-gem"></i> Material *
                    </label>
                    <div class="fs-input-wrap">
                        <select id="materialId" name="materialId" class="fs-input">
                            <option value="">Seleccione material</option>
                            <% for (Material m : materiales) {
                                boolean sel = pRec != null && pRec.getMaterialId() == m.getMaterialId(); %>
                            <option value="<%= m.getMaterialId() %>" <%= sel ? "selected" : "" %>>
                                <%= m.getNombre() %>
                            </option>
                            <% } %>
                        </select>
                        <div class="fs-bubble" id="err-materialId">
                            <span class="fs-bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                            <span>Selecciona un material.</span>
                        </div>
                    </div>
                </div>

                <!-- Proveedor -->
                <div class="fs-group">
                    <label class="fs-label" for="proveedorId">
                        <i class="fa-solid fa-truck"></i> Proveedor *
                    </label>
                    <div class="fs-input-wrap">
                        <select id="proveedorId" name="proveedorId" class="fs-input">
                            <option value="">Seleccione proveedor</option>
                            <% if (proveedores != null) {
                                for (Proveedor prov : proveedores) {
                                    boolean sel = pRec != null && pRec.getProveedorId() == prov.getProveedorId(); %>
                            <option value="<%= prov.getProveedorId() %>" <%= sel ? "selected" : "" %>>
                                <%= prov.getNombre() %>
                            </option>
                            <% } } %>
                        </select>
                        <div class="fs-bubble" id="err-proveedorId">
                            <span class="fs-bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                            <span>Selecciona un proveedor.</span>
                        </div>
                    </div>
                </div>

                <!-- Precio de Costo -->
                <div class="fs-group">
                    <label class="fs-label" for="precioUnitario">
                        <i class="fa-solid fa-dollar-sign"></i> Precio de Costo *
                    </label>
                    <div class="fs-input-wrap">
                        <input id="precioUnitario" type="number" name="precioUnitario" class="fs-input"
                               step="0.01" min="0.01" placeholder="0.00"
                               value="<%= pRec != null ? pRec.getPrecioUnitario() : "" %>">
                        <div class="fs-bubble" id="err-precioUnitario">
                            <span class="fs-bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                            <span>Debe ser un precio mayor a 0.</span>
                        </div>
                    </div>
                </div>

                <!-- Precio de Venta -->
                <div class="fs-group">
                    <label class="fs-label" for="precioVenta">
                        <i class="fa-solid fa-hand-holding-dollar"></i> Precio de Venta *
                    </label>
                    <div class="fs-input-wrap">
                        <input id="precioVenta" type="number" name="precioVenta" class="fs-input"
                               step="0.01" min="0.01" placeholder="Calculado automáticamente"
                               value="<%= pRec != null ? pRec.getPrecioVenta() : "" %>">
                        <div class="fs-bubble" id="err-precioVenta">
                            <span class="fs-bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                            <span>Mínimo esperado: (Costo x 2) + $5,000.</span>
                        </div>
                    </div>
                    <small style="color:#6b7280; font-size:0.8rem; margin-top:4px; display:block;">
                        Margen sugerido: Doble del costo + $5,000.
                    </small>
                </div>

            </div>

            <!-- ── SUBCATEGORÍAS: checkboxes tipo chip ── -->
            <% if (subcategorias != null && !subcategorias.isEmpty()) { %>
            <div class="fs-group" style="margin-top: 16px;">
                <label class="fs-label">
                    <i class="fa-solid fa-layer-group"></i> Subcategorías
                    <span style="font-weight:400; color:#6b7280;">(opcional — selecciona una o varias)</span>
                </label>
                <div class="subcat-grid" id="subcatGrid">
                    <% for (Subcategoria sc : subcategorias) {
                        boolean checked = subcatSeleccionadas.contains(sc.getSubcategoriaId()); %>
                    <label class="subcat-chip">
                        <%-- CAMBIO CLAVE: name="subcategoriaIds" (plural) permite enviar múltiples valores --%>
                        <input type="checkbox"
                               name="subcategoriaIds"
                               value="<%= sc.getSubcategoriaId() %>"
                               <%= checked ? "checked" : "" %>>
                        <%= sc.getNombre() %>
                    </label>
                    <% } %>
                </div>
                <p class="subcat-hint">
                    <i class="fa-solid fa-circle-info"></i>
                    Solo se muestran las subcategorías válidas para la categoría
                    <strong><%= categoria.getNombre() %></strong>.
                </p>
            </div>
            <% } %>

        </div>

        <!-- ── SECCIÓN 2: Descripción e Imagen ── -->
        <div class="fs-section">
            <div class="fs-section-title">
                <i class="fa-solid fa-image"></i> Descripción e Imagen
            </div>
            <div class="fs-grid">

                <!-- Descripción -->
                <div class="fs-group fs-group--full">
                    <label class="fs-label" for="descripcion">
                        <i class="fa-solid fa-align-left"></i> Descripción *
                        <span class="fs-char-counter" id="charCounter">0 / 500</span>
                    </label>
                    <div class="fs-input-wrap">
                        <textarea id="descripcion" name="descripcion" class="fs-input" rows="3"
                                  maxlength="500"
                                  placeholder="Mínimo 10 caracteres, máximo 500..."><%= pRec != null ? pRec.getDescripcion() : "" %></textarea>
                        <div class="fs-bubble" id="err-descripcion">
                            <span class="fs-bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                            <span>Descripción obligatoria (mín. 10 caracteres).</span>
                        </div>
                    </div>
                </div>

                <!-- Imagen -->
                <div class="fs-group">
                    <label class="fs-label" for="imagen">
                        <i class="fa-solid fa-cloud-arrow-up"></i> Imagen del Producto *
                    </label>
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

        <!-- ── INFO: Stock ── -->
        <div class="fs-section" style="background:#f0fdf4; border-left:4px solid #22c55e;">
            <div class="fs-section-title" style="color:#15803d;">
                <i class="fa-solid fa-boxes-stacked"></i> Stock
            </div>
            <p style="color:#166534; font-size:0.9rem; margin:0;">
                <i class="fa-solid fa-circle-info"></i>
                El stock inicial es <strong>0</strong>. Se incrementará automáticamente
                cuando registres una compra que incluya este producto.
            </p>
        </div>

        <div class="fs-actions">
            <button type="submit" class="fs-btn-save">
                <i class="fa-solid fa-floppy-disk"></i> Guardar Producto
            </button>
            <button type="button" class="fs-btn-cancel"
                    onclick="window.location.href='<%=request.getContextPath()%>/CategoriaServlet?id=<%= categoria.getCategoriaId() %>'">
                <i class="fa-solid fa-xmark"></i> Cancelar
            </button>
        </div>
    </form>
</main>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
// subcategoriaId ya no está en la lista de campos obligatorios
const campos = ['nombre', 'precioUnitario', 'precioVenta', 'materialId', 'proveedorId', 'imagen', 'descripcion'];

const inputCosto = document.getElementById('precioUnitario');
const inputVenta = document.getElementById('precioVenta');

inputCosto.addEventListener('input', () => {
    const costo = parseFloat(inputCosto.value);
    if (!isNaN(costo) && costo > 0) {
        const sugerido = (costo * 2) + 5000;
        if (inputVenta.value === '') {
            inputVenta.value = sugerido.toFixed(2);
            ocultarErr('precioVenta');
        }
    }
});

const textareaDesc = document.getElementById('descripcion');
const charCounter  = document.getElementById('charCounter');
function actualizarContador() {
    charCounter.textContent = textareaDesc.value.length + ' / 500';
}
textareaDesc.addEventListener('input', actualizarContador);
actualizarContador();

function esValido(id) {
    const el = document.getElementById(id);
    if (!el) return true;
    const valor = el.value.trim();
    switch (id) {
        case 'nombre': {
            return valor !== '' && /^[a-zA-Z0-9\sñÑáéíóúÁÉÍÓÚ]+$/.test(valor);
        }
        case 'precioUnitario': {
            const v = parseFloat(valor);
            return !isNaN(v) && v > 0;
        }
        case 'precioVenta': {
            const venta = parseFloat(valor);
            const costo = parseFloat(inputCosto.value) || 0;
            return !isNaN(venta) && venta >= (costo * 2) + 5000;
        }
        case 'materialId':
        case 'proveedorId':
            return valor !== '';
        case 'imagen':
            return el.files && el.files.length > 0;
        case 'descripcion':
            return valor.length >= 10 && valor.length <= 500;
        default:
            return true;
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
    if (!el) return;
    ['input', 'change'].forEach(ev => el.addEventListener(ev, () => {
        if (esValido(id)) ocultarErr(id);
    }));
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
    campos.forEach(id => {
        if (!esValido(id)) { mostrarErr(id); errores++; }
        else                ocultarErr(id);
    });

    if (errores > 0) {
        Swal.fire({
            icon: 'warning',
            title: 'Revisa el formulario',
            text: 'Hay campos con errores. Completa todos los campos obligatorios y verifica que el precio de venta sea al menos el doble del costo más $5,000.',
            confirmButtonColor: '#9177a8'
        });
        return;
    }

    const form = this;
    Swal.fire({
        title: '¿Registrar producto?',
        text: 'Se añadirá a la categoría <%= categoria.getNombre() %>.',
        icon: 'question',
        showCancelButton:   true,
        confirmButtonColor: '#9177a8',
        cancelButtonColor:  '#6b7280',
        confirmButtonText:  'Sí, guardar',
        cancelButtonText:   'Revisar'
    }).then(r => {
        if (r.isConfirmed) {
            Swal.fire({ title: 'Guardando...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
            form.submit();
        }
    });
});
</script>
</body>
</html>
