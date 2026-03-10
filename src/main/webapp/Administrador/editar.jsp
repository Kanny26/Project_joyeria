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
                <div class="fs-section">
                    <div class="fs-section-title"><i class="fa-solid fa-tag"></i> Datos del Producto</div>
                    <div class="fs-grid">

                        <div class="fs-group">
                            <label class="fs-label"><i class="fa-solid fa-pen"></i> Nombre *</label>
                            <input type="text" name="nombre" class="fs-input" value="<%= producto.getNombre() %>" required>
                        </div>

                        <div class="fs-group">
                            <label class="fs-label"><i class="fa-solid fa-dollar-sign"></i> Precio de Costo *</label>
                            <input type="number" name="precioUnitario" class="fs-input" step="0.01" min="0.01"
                                   value="<%= producto.getPrecioUnitario() %>" required>
                        </div>

                        <div class="fs-group">
                            <label class="fs-label"><i class="fa-solid fa-tag"></i> Precio de Venta *</label>
                            <input type="number" name="precioVenta" class="fs-input" step="0.01" min="0.01"
                                   value="<%= producto.getPrecioVenta() %>" required>
                        </div>

                        <!-- ■■ STOCK: readonly + botón ajustar ■■ -->
                        <div class="fs-group">
                            <label class="fs-label"><i class="fa-solid fa-boxes-stacked"></i> Stock actual</label>
                            <div style="display:flex; gap:8px; align-items:center;">
                                <input type="number" id="stockDisplay" class="fs-input"
                                       value="<%= producto.getStock() %>"
                                       readonly
                                       style="background:#f3f4f6; color:#6b7280; cursor:not-allowed; flex:1;">
                                <button type="button" onclick="abrirAjusteStock()"
                                        style="padding:10px 14px; background:#7c3aed; color:#fff; border:none;
                                               border-radius:10px; cursor:pointer; font-size:0.82rem; white-space:nowrap;
                                               display:flex; align-items:center; gap:5px; height:42px;">
                                    <i class="fa-solid fa-sliders"></i> Ajustar
                                </button>
                            </div>
                            <span style="font-size:0.72rem; color:#9ca3af; margin-top:3px; display:block;">
                                <i class="fa-solid fa-circle-info"></i>
                                El stock solo cambia por compras a proveedor o ajustes manuales justificados.
                            </span>
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
// Variables globales necesarias
const ctx         = '<%= request.getContextPath() %>';
const productoId  = <%= producto.getProductoId() %>;
let   stockActual = <%= producto.getStock() %>;

// 1. Manejo de Vista Previa de Imagen
function handleImageChange(input) {
    if (input.files && input.files[0]) {
        document.getElementById('file-name').textContent = input.files[0].name;
        const r = new FileReader();
        r.onload = e => document.getElementById('preview').src = e.target.result;
        r.readAsDataURL(input.files[0]);
    }
}

// 2. Guardar Cambios Generales del Formulario
document.getElementById('formEditar').addEventListener('submit', function(e) {
    e.preventDefault();
    const form = this;
    Swal.fire({
        title: '¿Aplicar cambios?',
        text: 'La información del producto será actualizada.',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#7c3aed',
        cancelButtonColor: '#6b7280',
        confirmButtonText: 'Confirmar',
        cancelButtonText: 'Cancelar'
    }).then(r => {
        if (r.isConfirmed) {
            Swal.fire({ title: 'Guardando...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
            form.submit();
        }
    });
});

// 3. Abrir Ventana de Ajuste de Stock
function abrirAjusteStock() {
    Swal.fire({
        title: 'Ajuste de Inventario',
        html: `
            <div style="text-align: left; font-family: sans-serif;">
                <p style="color: #64748b; font-size: 0.9rem;">Modifica el stock físico disponible.</p>
                <div style="background: #f8fafc; border: 1px solid #e2e8f0; padding: 12px; border-radius: 10px; margin-bottom: 15px;">
                    <div style="display: flex; justify-content: space-between;">
                        <span style="color: #475569;">Stock en Sistema:</span>
                        <strong style="color: #1e293b;">${stockActual} uds.</strong>
                    </div>
                </div>
                <label style="font-size: 0.85rem; font-weight: 600; color: #1e293b;">Cantidad Física Real</label>
                <input id="swal-stock" type="number" class="swal2-input" style="width: 100%; margin: 8px 0 15px 0;" value="${stockActual}">
                
                <label style="font-size: 0.85rem; font-weight: 600; color: #1e293b;">Motivo del cambio</label>
                <select id="swal-motivo" class="swal2-select" style="width: 100%; margin: 8px 0 0 0; display: flex;">
                    <option value="" disabled selected>Seleccione una razón...</option>
                    <option value="Inventario físico">Diferencia en Inventario Físico</option>
                    <option value="Producto dañado">Producto Dañado / Merma</option>
                    <option value="Corrección">Error de Digitación</option>
                    <option value="Otro">Otro motivo específico</option>
                </select>
                <div id="swal-otro-wrap" style="display:none; margin-top: 15px;">
                    <input id="swal-otro" type="text" class="swal2-input" style="width: 100%; margin: 0;" placeholder="Describa el motivo...">
                </div>
            </div>
        `,
        showCancelButton: true,
        confirmButtonText: 'Revisar Movimiento',
        confirmButtonColor: '#7c3aed',
        preConfirm: () => {
            const nuevoStock = parseInt(document.getElementById('swal-stock').value);
            const motivo = document.getElementById('swal-motivo').value;
            const otro = document.getElementById('swal-otro').value;

            if (isNaN(nuevoStock) || nuevoStock < 0) return Swal.showValidationMessage('Cantidad inválida');
            if (!motivo) return Swal.showValidationMessage('Seleccione un motivo');
            return { nuevoStock, motivo: motivo === 'Otro' ? otro : motivo };
        },
        didOpen: () => {
            const select = document.getElementById('swal-motivo');
            select.addEventListener('change', () => {
                document.getElementById('swal-otro-wrap').style.display = select.value === 'Otro' ? 'block' : 'none';
            });
        }
    }).then(result => {
        if (result.isConfirmed) confirmarAjusteFinal(result.value);
    });
}

// 4. Segunda Confirmación (Resumen visual)
function confirmarAjusteFinal(datos) {
    const diferencia = datos.nuevoStock - stockActual;
    const esIncremento = diferencia > 0;
    
    Swal.fire({
        title: '¿Confirmar Ajuste?',
        html: `
            <div style="background: #f8fafc; border: 1px dashed #cbd5e1; padding: 15px; border-radius: 10px;">
                <div style="font-size: 1.3rem; font-weight: bold; color: ${esIncremento ? '#059669' : '#dc2626'}">
                    ${esIncremento ? '+' : ''}${diferencia} Unidades
                </div>
                <p style="margin: 0; color: #64748b; font-size: 0.8rem;">MOVIMIENTO DE ${esIncremento ? 'ENTRADA' : 'SALIDA'}</p>
            </div>
            <div style="margin-top: 15px; text-align: left; font-size: 0.9rem; color: #374151;">
                <strong>Nuevo Stock:</strong> ${datos.nuevoStock} uds.<br>
                <strong>Motivo:</strong> ${datos.motivo}
            </div>
        `,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Sí, aplicar cambio',
        confirmButtonColor: '#7c3aed'
    }).then(r => {
        if (r.isConfirmed) enviarAlServidor(datos);
    });
}

// 5. Envío de datos al Servlet (AJAX)
function enviarAlServidor(datos) {
    Swal.fire({ title: 'Procesando...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });

    const diferencia = Math.abs(datos.nuevoStock - stockActual);
    const tipo = (datos.nuevoStock > stockActual) ? 'entrada' : 'salida';

    const params = new URLSearchParams({
        action: 'ajustarStock',
        productoId: productoId,
        nuevoStock: datos.nuevoStock,
        cantidad: diferencia,
        tipo: tipo,
        motivo: datos.motivo
    });

    fetch(ctx + '/ProductoServlet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
    .then(r => r.json())
    .then(data => {
        if (data.ok) {
            stockActual = datos.nuevoStock;
            document.getElementById('stockDisplay').value = stockActual;
            Swal.fire('¡Éxito!', 'El stock ha sido actualizado.', 'success');
        } else {
            // Muestra el error de validación (como el de exceso de compras)
            Swal.fire({
                icon: 'error',
                title: 'No se pudo ajustar',
                html: `<p style="color:#ef4444; font-weight:bold;">${data.error}</p>`
            });
        }
    })
    .catch(() => Swal.fire('Error', 'No se pudo conectar con el servidor', 'error'));
}
</script>
</body>
</html>
