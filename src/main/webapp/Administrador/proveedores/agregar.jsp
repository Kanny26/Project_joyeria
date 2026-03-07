<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="model.Material, java.util.List"%>
<%
    if (session.getAttribute("admin") == null) { response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp"); return; }
    List<Material> materiales = (List<Material>) request.getAttribute("materiales");
    if (materiales == null) materiales = java.util.Collections.emptyList();
    String errorServidor = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Agregar Proveedor - AAC27</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/forms-global.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/producto.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
<nav class="navbar-admin">
    <div class="navbar-admin__catalogo"><img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin"></div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%=request.getContextPath()%>/ProveedorServlet?action=listar" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<main class="fs-container">
    <h2 class="fs-page-title"><i class="fa-solid fa-truck-ramp-box"></i> Registrar Nuevo Proveedor</h2>

    <form id="formProveedor" class="fs-form" method="post"
          action="<%=request.getContextPath()%>/ProveedorServlet" novalidate>
        <input type="hidden" name="action" value="guardar">

        <!-- SECCIÓN: Datos básicos -->
        <div class="fs-section">
            <div class="fs-section-title"><i class="fa-solid fa-building"></i> Datos del Proveedor</div>
            <div class="fs-grid">
                <div class="fs-group">
                    <label class="fs-label" for="nombre"><i class="fa-solid fa-building"></i> Nombre *</label>
                    <div class="fs-input-wrap">
                        <input id="nombre" type="text" name="nombre" class="fs-input" autocomplete="off" required>
                        <div class="fs-bubble" id="err-nombre">
                            <span class="fs-bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                            <span>El nombre es obligatorio.</span>
                        </div>
                    </div>
                </div>
                <div class="fs-group">
                    <label class="fs-label" for="documento"><i class="fa-solid fa-id-card"></i> Documento *</label>
                    <div class="fs-input-wrap">
                        <input id="documento" type="text" name="documento" class="fs-input" autocomplete="off" required>
                        <div class="fs-bubble" id="err-documento">
                            <span class="fs-bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                            <span id="msg-doc-error">Documento inválido (mín. 5 caracteres).</span>
                        </div>
                    </div>
                </div>
                <div class="fs-group">
                    <label class="fs-label" for="fechaInicio"><i class="fa-regular fa-calendar"></i> Fecha de Inicio *</label>
                    <div class="fs-input-wrap">
                        <input id="fechaInicio" type="date" name="fechaInicio" class="fs-input" required>
                        <div class="fs-bubble" id="err-fechaInicio">
                            <span class="fs-bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                            <span>Selecciona una fecha válida.</span>
                        </div>
                    </div>
                </div>
                <div class="fs-group">
                    <label class="fs-label" for="minimoCompra"><i class="fa-solid fa-dollar-sign"></i> Monto Mínimo de Compra</label>
                    <div class="fs-input-wrap">
                        <input id="minimoCompra" type="number" name="minimoCompra" class="fs-input"
                               step="0.01" min="0" placeholder="0.00">
                        <div class="fs-bubble" id="err-minimoCompra">
                            <span class="fs-bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                            <span>El monto debe ser positivo.</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- SECCIÓN: Contacto -->
        <div class="fs-section">
            <div class="fs-section-title"><i class="fa-solid fa-address-book"></i> Información de Contacto</div>
            <div class="fs-grid">
                <div class="fs-group">
                    <label class="fs-label"><i class="fa-solid fa-phone"></i> Teléfonos *</label>
                    <div id="telefonos-container">
                        <div class="fs-dyn-row">
                            <input type="tel" name="telefono" class="fs-input" placeholder="Ej: 3001234567" required>
                        </div>
                    </div>
                    <button type="button" class="fs-btn-add" onclick="agregarTelefono()">
                        <i class="fa-solid fa-plus"></i> Agregar teléfono
                    </button>
                </div>
                <div class="fs-group">
                    <label class="fs-label"><i class="fa-solid fa-envelope"></i> Correos *</label>
                    <div id="correos-container">
                        <div class="fs-dyn-row">
                            <input type="email" name="correo" class="fs-input" placeholder="correo@ejemplo.com" required>
                        </div>
                    </div>
                    <button type="button" class="fs-btn-add" onclick="agregarCorreo()">
                        <i class="fa-solid fa-plus"></i> Agregar correo
                    </button>
                </div>
            </div>
        </div>

        <!-- SECCIÓN: Materiales -->
        <div class="fs-section">
            <div class="fs-section-title"><i class="fa-solid fa-cubes"></i> Materiales que Suministra</div>
            <div class="fs-check-grid">
                <% for (Material m : materiales) { %>
                <label class="fs-check-label">
                    <input type="checkbox" name="materiales" value="<%= m.getMaterialId() %>">
                    <%= m.getNombre() %>
                </label>
                <% } %>
                <% if (materiales.isEmpty()) { %>
                <p style="color:#9ca3af;font-size:0.85rem;grid-column:1/-1;">No hay materiales registrados.</p>
                <% } %>
            </div>
        </div>

        <div class="fs-actions">
            <button type="submit" class="fs-btn-save"><i class="fa-solid fa-floppy-disk"></i> Guardar Proveedor</button>
            <button type="button" class="fs-btn-cancel" onclick="history.back()"><i class="fa-solid fa-xmark"></i> Cancelar</button>
        </div>
    </form>
</main>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
<% if (errorServidor != null && !errorServidor.isEmpty()) { %>
document.addEventListener('DOMContentLoaded', () => Swal.fire({ icon:'error', title:'Error al guardar', text:'<%= errorServidor.replace("'","\\'") %>' }));
<% } %>

function agregarTelefono() {
    const c = document.getElementById('telefonos-container');
    const d = document.createElement('div'); d.className = 'fs-dyn-row';
    d.innerHTML = `<input type="tel" name="telefono" class="fs-input" placeholder="Ej: 3001234567" required>
                   <button type="button" class="fs-btn-remove" onclick="quitarFila(this)"><i class="fa-solid fa-minus"></i></button>`;
    c.appendChild(d); d.querySelector('input').focus();
}
function agregarCorreo() {
    const c = document.getElementById('correos-container');
    const d = document.createElement('div'); d.className = 'fs-dyn-row';
    d.innerHTML = `<input type="email" name="correo" class="fs-input" placeholder="correo@ejemplo.com" required>
                   <button type="button" class="fs-btn-remove" onclick="quitarFila(this)"><i class="fa-solid fa-minus"></i></button>`;
    c.appendChild(d); d.querySelector('input').focus();
}
function quitarFila(btn) {
    const fila = btn.closest('.fs-dyn-row');
    if (fila.parentElement.querySelectorAll('.fs-dyn-row').length <= 1) {
        Swal.fire({ icon:'warning', title:'Requerido', text:'Debe quedar al menos uno.', timer:2000, showConfirmButton:false }); return;
    }
    fila.remove();
}

const reglas = {
    nombre:       v => v.trim().length >= 2,
    documento:    v => /^[0-9A-Za-záéíóúÁÉÍÓÚñÑ.\- ]{5,25}$/.test(v.trim()),
    fechaInicio:  v => v !== '',
    minimoCompra: v => v === '' || parseFloat(v) >= 0
};
function validarCampo(id) {
    const el = document.getElementById(id);
    const ok = reglas[id](el.value);
    el.classList.toggle('invalid', !ok);
    const b = document.getElementById('err-' + id);
    if (b) b.classList.toggle('visible', !ok);
    return ok;
}
Object.keys(reglas).forEach(id => {
    const el = document.getElementById(id);
    if (el) el.addEventListener('blur', () => validarCampo(id));
});

document.getElementById('formProveedor').addEventListener('submit', async function(e) {
    e.preventDefault();
    let ok = Object.keys(reglas).reduce((acc, id) => validarCampo(id) && acc, true);
    if (!ok) { Swal.fire({ icon:'warning', title:'Campos inválidos', text:'Revisa los campos marcados.' }); return; }

    const docInput = document.getElementById('documento');
    const btn = this.querySelector('.fs-btn-save');
    btn.disabled = true;
    try {
        const ctx = '<%=request.getContextPath()%>';
        const res = await fetch(ctx + '/ProveedorServlet?action=verificarDocumento&documento=' + encodeURIComponent(docInput.value.trim()));
        const data = await res.json();
        if (data.existe) {
            docInput.classList.add('invalid');
            document.getElementById('err-documento').classList.add('visible');
            document.getElementById('msg-doc-error').textContent = '¡Este documento ya está registrado!';
            Swal.fire({ icon:'error', title:'Documento duplicado', text:'Ya existe un proveedor con ese documento.' });
            btn.disabled = false; return;
        }
    } catch(err) { btn.disabled = false; }

    const form = this;
    Swal.fire({
        title:'¿Guardar proveedor?', icon:'question', showCancelButton:true,
        confirmButtonColor:'#7c3aed', cancelButtonColor:'#6b7280',
        confirmButtonText:'Sí, guardar', cancelButtonText:'Revisar'
    }).then(r => {
        if (r.isConfirmed) { Swal.fire({ title:'Guardando...', allowOutsideClick:false, didOpen:()=>Swal.showLoading() }); form.submit(); }
        else btn.disabled = false;
    });
});
</script>
</body>
</html>
