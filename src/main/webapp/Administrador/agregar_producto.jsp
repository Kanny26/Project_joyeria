<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, model.Material, model.Administrador, model.Categoria, model.Producto" %>
<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }
    List<Material> materiales = (List<Material>) request.getAttribute("materiales");
    Categoria categoria = (Categoria) request.getAttribute("categoria");
    Producto pRecuperado = (Producto) request.getAttribute("producto");

    if (materiales == null || categoria == null) {
        response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
        return;
    }
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Agregar producto - AAC27</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/producto.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
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

<main class="form-product-container">
    <h2 class="form-product-container__title">
        Nuevo producto &mdash; <%= categoria.getNombre() %>
    </h2>

    <% if (error != null) { %>
        <div class="alert-server">
            <i class="fa-solid fa-circle-exclamation"></i> <%= error %>
        </div>
    <% } %>

    <form id="formProducto" class="form-product" method="post"
          action="<%=request.getContextPath()%>/ProductoServlet"
          enctype="multipart/form-data" novalidate>

        <input type="hidden" name="action" value="guardar">
        <input type="hidden" name="categoriaId" value="<%= categoria.getCategoriaId() %>">

        <div class="form-product__row">

            <!-- NOMBRE -->
            <div class="form-product__group">
                <label class="form-product__label" for="nombre">Nombre *</label>
                <div class="input-wrap">
                    <input id="nombre" type="text" name="nombre"
                           class="form-product__input"
                           value="<%= pRecuperado != null ? pRecuperado.getNombre() : "" %>"
                           autocomplete="off">
                    <div class="bubble-error" id="err-nombre">
                        <span class="bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                        <span>El nombre es obligatorio.</span>
                    </div>
                </div>
            </div>

            <!-- PRECIO COSTO -->
            <div class="form-product__group">
                <label class="form-product__label" for="precioUnitario">Precio de costo *</label>
                <div class="input-wrap">
                    <input id="precioUnitario" type="number" name="precioUnitario"
                           class="form-product__input" step="0.01" min="0.01"
                           value="<%= pRecuperado != null ? pRecuperado.getPrecioUnitario() : "" %>">
                    <div class="bubble-error" id="err-precioUnitario">
                        <span class="bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                        <span>Ingresa un precio de costo mayor a 0.</span>
                    </div>
                </div>
            </div>

            <!-- PRECIO VENTA -->
            <div class="form-product__group">
                <label class="form-product__label" for="precioVenta">Precio de venta *</label>
                <div class="input-wrap">
                    <input id="precioVenta" type="number" name="precioVenta"
                           class="form-product__input" step="0.01" min="0.01"
                           value="<%= pRecuperado != null ? pRecuperado.getPrecioVenta() : "" %>">
                    <div class="bubble-error" id="err-precioVenta">
                        <span class="bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                        <span>El precio de venta debe ser mayor a 0 y no menor al precio de costo.</span>
                    </div>
                </div>
            </div>

            <!-- STOCK -->
            <div class="form-product__group">
                <label class="form-product__label" for="stock">Stock *</label>
                <div class="input-wrap">
                    <input id="stock" type="number" name="stock"
                           class="form-product__input" min="0"
                           value="<%= pRecuperado != null ? pRecuperado.getStock() : "" %>">
                    <div class="bubble-error" id="err-stock">
                        <span class="bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                        <span>El stock es obligatorio y no puede ser negativo.</span>
                    </div>
                </div>
            </div>

            <!-- MATERIAL (CORREGIDO: comparar IDs como int) -->
            <div class="form-product__group">
                <label class="form-product__label" for="materialId">Material *</label>
                <div class="input-wrap">
                    <select id="materialId" name="materialId" class="form-product__input">
                        <option value="">Seleccione material</option>
                        <% for (Material m : materiales) {
                            boolean isSelected = (pRecuperado != null 
                                && pRecuperado.getMaterialId() == m.getMaterialId());
                        %>
                            <option value="<%= m.getMaterialId() %>" <%= isSelected ? "selected" : "" %>>
                                <%= m.getNombre() %>
                            </option>
                        <% } %>
                    </select>
                    <div class="bubble-error" id="err-materialId">
                        <span class="bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                        <span>Selecciona un material.</span>
                    </div>
                </div>
            </div>

            <!-- IMAGEN -->
            <div class="form-product__group">
                <label class="form-product__label" for="imagen">Imagen del producto *</label>
                <div class="input-wrap">
                    <input id="imagen" type="file" name="imagen"
                           class="form-product__input" accept="image/*"
                           onchange="previsualizarImagen(this)">
                    <div class="bubble-error" id="err-imagen">
                        <span class="bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                        <span>Selecciona una imagen para el producto.</span>
                    </div>
                </div>
                <div class="img-preview-wrap" id="previewWrap">
                    <img id="imgPreview" src="#" alt="Vista previa">
                    <span id="imgNombre"></span>
                </div>
            </div>

        </div>

        <!-- DESCRIPCIÓN -->
        <div class="form-product__group">
            <label class="form-product__label" for="descripcion">Descripción *</label>
            <div class="input-wrap">
                <textarea id="descripcion" name="descripcion"
                          class="form-product__input" rows="4"><%= pRecuperado != null ? pRecuperado.getDescripcion() : "" %></textarea>
                <div class="bubble-error" id="err-descripcion">
                    <span class="bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                    <span>La descripción es obligatoria.</span>
                </div>
            </div>
        </div>

        <div class="form-product__actions">
            <button type="submit" class="btn-guardar">
                <i class="fa-solid fa-floppy-disk"></i> Guardar producto
            </button>
            <button type="button" class="btn-cancelar"
                    onclick="window.location.href='<%=request.getContextPath()%>/CategoriaServlet?id=<%= categoria.getCategoriaId() %>'">
                <i class="fa-solid fa-xmark"></i> Cancelar
            </button>
        </div>
    </form>
</main>

<script>
const campos = ['nombre','precioUnitario','precioVenta','stock','materialId','imagen','descripcion'];

function esValido(id) {
    switch (id) {
        case 'nombre': return document.getElementById('nombre').value.trim() !== '';
        case 'precioUnitario': {
            const v = parseFloat(document.getElementById('precioUnitario').value);
            return !isNaN(v) && v > 0;
        }
        case 'precioVenta': {
            const costo = parseFloat(document.getElementById('precioUnitario').value) || 0;
            const venta = parseFloat(document.getElementById('precioVenta').value);
            return !isNaN(venta) && venta > 0 && venta >= costo;
        }
        case 'stock': {
            const s = document.getElementById('stock').value;
            return s !== '' && parseInt(s) >= 0;
        }
        case 'materialId': return document.getElementById('materialId').value !== '';
        case 'imagen': return document.getElementById('imagen').files?.length > 0;
        case 'descripcion': return document.getElementById('descripcion').value.trim() !== '';
        default: return true;
    }
}

function mostrarBurbuja(id) {
    document.getElementById(id).classList.add('invalid');
    document.getElementById('err-' + id).classList.add('visible');
}
function ocultarBurbuja(id) {
    document.getElementById(id).classList.remove('invalid');
    document.getElementById('err-' + id).classList.remove('visible');
}

campos.forEach(id => {
    const el = document.getElementById(id);
    if (!el) return;
    const check = () => { if (esValido(id)) ocultarBurbuja(id); };
    el.addEventListener('input', check);
    el.addEventListener('change', check);
});

function previsualizarImagen(input) {
    const wrap = document.getElementById('previewWrap');
    const preview = document.getElementById('imgPreview');
    const nombre = document.getElementById('imgNombre');
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = e => {
            preview.src = e.target.result;
            nombre.textContent = input.files[0].name;
            wrap.classList.add('visible');
        };
        reader.readAsDataURL(input.files[0]);
        ocultarBurbuja('imagen');
    }
}

document.getElementById('formProducto').addEventListener('submit', function(e) {
    campos.forEach(id => ocultarBurbuja(id));
    for (const id of campos) {
        if (!esValido(id)) {
            e.preventDefault();
            mostrarBurbuja(id);
            document.getElementById(id).scrollIntoView({ behavior: 'smooth', block: 'center' });
            document.getElementById(id).focus();
            break;
        }
    }
});
</script>

</body>
</html>