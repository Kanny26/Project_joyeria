<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, model.Material, model.Administrador, model.Categoria, model.Producto" %>
<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }
    List<Material> materiales = (List<Material>) request.getAttribute("materiales");
    Categoria categoria       = (Categoria)      request.getAttribute("categoria");
    Producto pRecuperado      = (Producto)        request.getAttribute("producto");

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
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/agregar_producto.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%=request.getContextPath()%>/CategoriaServlet?id=<%= categoria.getCategoriaId() %>">
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
    </a>
</nav>

<main class="form-product-container">
    <h2 class="form-product-container__title">
        Nuevo producto — <%= categoria.getNombre() %>
    </h2>

    <% if (error != null) { %>
        <div class="alert-server">
            <i class="fa-solid fa-circle-exclamation"></i> <%= error %>
        </div>
    <% } %>

    <form id="formProducto" class="form-product" method="post"
          action="<%=request.getContextPath()%>/ProductoServlet"
          enctype="multipart/form-data"
          novalidate>

        <input type="hidden" name="action"      value="guardar">
        <input type="hidden" name="categoriaId" value="<%= categoria.getCategoriaId() %>">

        <div class="form-product__row">

            <div class="form-product__group">
                <label class="form-product__label" for="nombre">Nombre *</label>
                <input id="nombre" type="text" name="nombre"
                       class="form-product__input"
                       value="<%= pRecuperado != null ? pRecuperado.getNombre() : "" %>">
            </div>

            <div class="form-product__group">
                <label class="form-product__label" for="precioUnitario">Precio de costo *</label>
                <input id="precioUnitario" type="number" name="precioUnitario"
                       class="form-product__input" step="0.01" min="0.01"
                       value="<%= pRecuperado != null ? pRecuperado.getPrecioUnitario() : "" %>">
            </div>

            <div class="form-product__group">
                <label class="form-product__label" for="precioVenta">Precio de venta *</label>
                <input id="precioVenta" type="number" name="precioVenta"
                       class="form-product__input" step="0.01" min="0.01"
                       value="<%= pRecuperado != null ? pRecuperado.getPrecioVenta() : "" %>">
            </div>

            <div class="form-product__group">
                <label class="form-product__label" for="stock">Stock *</label>
                <input id="stock" type="number" name="stock"
                       class="form-product__input" min="0"
                       value="<%= pRecuperado != null ? pRecuperado.getStock() : "" %>">
            </div>

            <div class="form-product__group">
                <label class="form-product__label" for="materialId">Material *</label>
                <select id="materialId" name="materialId" class="form-product__input">
                    <option value="">Seleccione material</option>
                    <% for (Material m : materiales) {
                        boolean isSelected = (pRecuperado != null
                                && pRecuperado.getMaterial() != null
                                && pRecuperado.getMaterial().getMaterialId() == m.getMaterialId());
                    %>
                        <option value="<%= m.getMaterialId() %>" <%= isSelected ? "selected" : "" %>>
                            <%= m.getNombre() %>
                        </option>
                    <% } %>
                </select>
            </div>

            <div class="form-product__group">
                <label class="form-product__label" for="imagen">Imagen del producto *</label>
                <input id="imagen" type="file" name="imagen"
                       class="form-product__input" accept="image/*"
                       onchange="previsualizarImagen(this)">
                <div class="img-preview-wrap" id="previewWrap">
                    <img id="imgPreview" src="#" alt="Vista previa">
                    <span id="imgNombre"></span>
                </div>
            </div>

        </div>

        <div class="form-product__group">
            <label class="form-product__label" for="descripcion">Descripción *</label>
            <textarea id="descripcion" name="descripcion"
                      class="form-product__input" rows="4"><%= pRecuperado != null ? pRecuperado.getDescripcion() : "" %></textarea>
        </div>

        <div class="form-product__actions">
            <button type="submit" class="form-product__btn">
                <i class="fa-solid fa-floppy-disk"></i> Guardar producto
            </button>
            <button type="button" class="btn-danger"
                    onclick="window.location.href='<%=request.getContextPath()%>/CategoriaServlet?id=<%= categoria.getCategoriaId() %>'">
                <i class="fa-solid fa-xmark"></i> Cancelar
            </button>
        </div>
    </form>
</main>

<script>
/* ══════════════════════════════════════════════════════════
   Los mensajes de error NO existen en el HTML.
   JS los CREA al detectar el error y los ELIMINA al corregir.
══════════════════════════════════════════════════════════ */

/* Mensajes por campo */
const mensajes = {
    nombre:         'El nombre es obligatorio.',
    precioUnitario: 'Ingresa un precio de costo mayor a 0.',
    precioVenta:    'El precio de venta debe ser mayor a 0 y no menor al precio de costo.',
    stock:          'El stock es obligatorio y no puede ser negativo.',
    materialId:     'Selecciona un material.',
    imagen:         'Selecciona una imagen para el producto.',
    descripcion:    'La descripción es obligatoria.'
};

/* Reglas de validación */
function esValido(id) {
    switch (id) {
        case 'nombre':
            return document.getElementById('nombre').value.trim() !== '';
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
        case 'materialId':
            return document.getElementById('materialId').value !== '';
        case 'imagen': {
            const img = document.getElementById('imagen');
            return img.files && img.files.length > 0;
        }
        case 'descripcion':
            return document.getElementById('descripcion').value.trim() !== '';
        default:
            return true;
    }
}

/* Crear alerta de error debajo del campo (solo si no existe ya) */
function mostrarError(id) {
    const campo = document.getElementById(id);
    campo.classList.add('invalid');

    // Si ya existe el mensaje, no lo duplicamos
    const existente = campo.parentElement.querySelector('.field-error-msg');
    if (existente) return;

    const msg = document.createElement('span');
    msg.className = 'field-error-msg';
    msg.innerHTML = '<i class="fa-solid fa-circle-exclamation"></i> ' + mensajes[id];

    // Insertar después del campo (o después del previewWrap si es imagen)
    const referencia = id === 'imagen'
        ? document.getElementById('previewWrap')
        : campo;
    referencia.insertAdjacentElement('afterend', msg);
}

/* Eliminar alerta y quitar borde rojo */
function quitarError(id) {
    const campo = document.getElementById(id);
    campo.classList.remove('invalid');

    const msg = campo.parentElement.querySelector('.field-error-msg');
    if (msg) msg.remove();
}

/* Al corregir un campo que ya tenía error → quitarlo */
Object.keys(mensajes).forEach(id => {
    const el = document.getElementById(id);
    if (!el) return;

    const limpiarSiOk = () => {
        if (esValido(id)) quitarError(id);
    };

    el.addEventListener('input',  limpiarSiOk);
    el.addEventListener('change', limpiarSiOk);
});

/* Preview de imagen */
function previsualizarImagen(input) {
    const wrap    = document.getElementById('previewWrap');
    const preview = document.getElementById('imgPreview');
    const nombre  = document.getElementById('imgNombre');

    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = e => {
            preview.src = e.target.result;
            nombre.textContent = input.files[0].name;
            wrap.classList.add('visible');
        };
        reader.readAsDataURL(input.files[0]);
        quitarError('imagen');
    }
}

/* Al enviar: validar todo y mostrar errores solo de los campos incompletos */
document.getElementById('formProducto').addEventListener('submit', function(e) {
    let hayError = false;

    Object.keys(mensajes).forEach(id => {
        if (esValido(id)) {
            quitarError(id);
        } else {
            mostrarError(id);
            hayError = true;
        }
    });

    if (hayError) {
        e.preventDefault();
        const primerInvalido = this.querySelector('.invalid');
        if (primerInvalido) {
            primerInvalido.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
    }
});
</script>

</body>
</html>
