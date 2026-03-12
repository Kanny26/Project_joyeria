<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="model.Material, java.util.List"%>
<%
    if (session.getAttribute("admin") == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }
    List<Material> materiales = (List<Material>) request.getAttribute("materiales");
    if (materiales == null) materiales = java.util.Collections.emptyList();
    String errorServidor = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Agregar Proveedor - AAC27</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/forms-global.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/producto.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <style>
        .fs-input.is-invalid { border-color: #ef4444 !important; background: #fef2f2 !important; }
        .field-error { font-size: 0.72rem; color: #dc2626; display: none; align-items: center; gap: 4px; margin-top: 3px; font-weight: 500; }
        .field-error.visible { display: flex; }
        .fs-check-grid.invalid-border { border-color: #ef4444 !important; background: #fef2f2; }
        .materiales-error { font-size: 0.78rem; color: #dc2626; display: none; align-items: center; gap: 5px; margin-top: 8px; padding: 8px 12px; background: #fef2f2; border-radius: 8px; border: 1px solid #fecaca; }
        .materiales-error.visible { display: flex; }
        .fs-dyn-row { display: flex; gap: 8px; align-items: center; margin-bottom: 10px; }
        .fs-btn-remove { width: 36px; height: 36px; border-radius: 9px; border: none; background: #fee2e2; color: #dc2626; cursor: pointer; display: flex; align-items: center; justify-content: center; }
        .fs-btn-add { display: inline-flex; align-items: center; gap: 6px; padding: 7px 16px; border-radius: 9px; border: 1.5px dashed #7c3aed; background: transparent; color: #7c3aed; font-size: 0.82rem; font-weight: 600; cursor: pointer; margin-top: 6px; }
        .server-error { background:#fef2f2; color:#dc2626; padding:12px 16px; border:1px solid #fecaca; border-radius:8px; margin-bottom:20px; display:flex; align-items:center; gap:8px; font-weight:500; }
    </style>
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo"><img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin"></div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%=request.getContextPath()%>/ProveedorServlet?action=listar" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap"><i class="fa-solid fa-arrow-left"></i> Volver</span>
    </a>
</nav>

<main class="fs-container">
    <h2 class="fs-page-title"><i class="fa-solid fa-truck-ramp-box" style="color: #7c3aed;"></i> Registrar Nuevo Proveedor</h2>

    <% if (errorServidor != null && !errorServidor.isEmpty()) { %>
        <div class="server-error">
            <i class="fa-solid fa-circle-exclamation"></i>
            <%= errorServidor %>
        </div>
    <% } %>

    <!-- ✅ FORMULARIO CORREGIDO -->
    <form id="formProveedor" class="fs-form" method="post" action="<%=request.getContextPath()%>/ProveedorServlet">
        <input type="hidden" name="action" value="guardar">
        <input type="hidden" name="estado" value="activo"> <!-- ✅ CAMPO ESENCIAL AGREGADO -->

        <div class="fs-section">
            <div class="fs-section-title"><i class="fa-solid fa-building"></i> Identificación Comercial</div>
            <div class="fs-grid">
                <div class="fs-group">
                    <label class="fs-label" for="nombre">Nombre *</label>
                    <input id="nombre" type="text" name="nombre" class="fs-input" placeholder="Ej: Joyeria Aurora" required 
                           value="<%= request.getAttribute("proveedor") != null ? ((model.Proveedor)request.getAttribute("proveedor")).getNombre() : "" %>">
                    <div class="field-error" id="err-nombre"><i class="fa-solid fa-circle-exclamation"></i> Solo letras y números (mín. 3).</div>
                </div>

                <div class="fs-group">
                    <label class="fs-label" for="documento">Documento / NIT (Solo números) *</label>
                    <input id="documento" type="text" name="documento" class="fs-input" maxlength="20" required 
                           oninput="this.value = this.value.replace(/[^0-9]/g, '')"
                           value="<%= request.getAttribute("proveedor") != null ? ((model.Proveedor)request.getAttribute("proveedor")).getDocumento() : "" %>">
                    <div class="field-error" id="err-documento"><i class="fa-solid fa-circle-exclamation"></i> Mínimo 5 dígitos numéricos.</div>
                </div>

                <div class="fs-group">
                    <label class="fs-label" for="fechaInicio">Fecha de Inicio *</label>
                    <input id="fechaInicio" type="date" name="fechaInicio" class="fs-input" required
                           value="<%= request.getAttribute("proveedor") != null ? ((model.Proveedor)request.getAttribute("proveedor")).getFechaInicio() : "" %>">
                    <div class="field-error" id="err-fechaInicio"><i class="fa-solid fa-circle-exclamation"></i> La fecha no puede ser futura.</div>
                </div>

                <div class="fs-group">
                    <label class="fs-label" for="minimoCompra">Monto Mínimo Compra ($)</label>
                    <input id="minimoCompra" type="number" name="minimoCompra" class="fs-input" min="0" step="0.01" placeholder="0.00"
                           value="<%= request.getAttribute("proveedor") != null ? ((model.Proveedor)request.getAttribute("proveedor")).getMinimoCompra() : "" %>">
                    <div class="field-error" id="err-minimo"><i class="fa-solid fa-circle-exclamation"></i> El monto no puede ser negativo.</div>
                </div>
            </div>
        </div>

        <div class="fs-section">
            <div class="fs-section-title"><i class="fa-solid fa-address-book"></i> Canales de Contacto</div>
            <div class="fs-grid">
                <div class="fs-group">
                    <label class="fs-label">Teléfonos (Solo números) *</label>
                    <div id="telefonos-container">
                        <% 
                            model.Proveedor provEdit = (model.Proveedor) request.getAttribute("proveedor");
                            List<String> telefonos = (provEdit != null && provEdit.getTelefonos() != null) ? provEdit.getTelefonos() : java.util.Arrays.asList("");
                            for (int i = 0; i < telefonos.size(); i++) { 
                                String tel = telefonos.get(i) != null ? telefonos.get(i) : "";
                        %>
                        <div class="fs-dyn-row">
                            <input type="text" name="telefono" class="fs-input tel-input" maxlength="15" 
                                   placeholder="3001234567" value="<%= tel %>" 
                                   oninput="this.value = this.value.replace(/[^0-9]/g, '')">
                            <% if (i > 0) { %>
                            <button type="button" class="fs-btn-remove" onclick="this.parentElement.remove(); validarTelefonos();">
                                <i class="fa-solid fa-minus"></i>
                            </button>
                            <% } %>
                        </div>
                        <% } %>
                    </div>
                    <div class="field-error" id="err-telefonos"><i class="fa-solid fa-circle-exclamation"></i> Entre 7 y 15 dígitos.</div>
                    <button type="button" class="fs-btn-add" onclick="agregarTelefono()"><i class="fa-solid fa-plus"></i> Añadir otro</button>
                </div>

                <div class="fs-group">
                    <label class="fs-label">Correos Electrónicos *</label>
                    <div id="correos-container">
                        <% 
                            List<String> correos = (provEdit != null && provEdit.getCorreos() != null) ? provEdit.getCorreos() : java.util.Arrays.asList("");
                            for (int i = 0; i < correos.size(); i++) { 
                                String cor = correos.get(i) != null ? correos.get(i) : "";
                        %>
                        <div class="fs-dyn-row">
                            <input type="email" name="correo" class="fs-input email-input" 
                                   placeholder="ejemplo@correo.com" value="<%= cor %>">
                            <% if (i > 0) { %>
                            <button type="button" class="fs-btn-remove" onclick="this.parentElement.remove(); validarCorreos();">
                                <i class="fa-solid fa-minus"></i>
                            </button>
                            <% } %>
                        </div>
                        <% } %>
                    </div>
                    <div class="field-error" id="err-correos"><i class="fa-solid fa-circle-exclamation"></i> Formato de correo inválido (@).</div>
                    <button type="button" class="fs-btn-add" onclick="agregarCorreo()"><i class="fa-solid fa-plus"></i> Añadir otro</button>
                </div>
            </div>
        </div>

        <div class="fs-section">
            <div class="fs-section-title"><i class="fa-solid fa-cubes"></i> Materiales Suministrados *</div>
            <div class="fs-check-grid" id="container-materiales">
                <% 
                    List<Integer> materialesSeleccionados = new java.util.ArrayList<>();
                    if (provEdit != null && provEdit.getMateriales() != null) {
                        for (model.Material mSel : provEdit.getMateriales()) {
                            materialesSeleccionados.add(mSel.getMaterialId());
                        }
                    }
                    for (Material m : materiales) { 
                        boolean seleccionado = materialesSeleccionados.contains(m.getMaterialId());
                %>
                <label class="fs-check-label">
                    <input type="checkbox" name="materiales" value="<%= m.getMaterialId() %>" 
                           class="material-check" <%= seleccionado ? "checked" : "" %>>
                    <%= m.getNombre() %>
                </label>
                <% } %>
            </div>
            <div class="materiales-error" id="err-materiales"><i class="fa-solid fa-circle-exclamation"></i> Selecciona al menos un material.</div>
        </div>

        <div class="fs-actions">
            <button type="submit" class="fs-btn-save">Registrar Proveedor</button>
            <button type="button" class="fs-btn-cancel" onclick="history.back()">Cancelar</button>
        </div>
    </form>
</main>

<!-- ✅ CDN CORREGIDO: sin espacios al final -->
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const NAME_REGEX  = /^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\s]+$/;

const toggleError = (id, show) => {
    const el = document.getElementById(id);
    if(el) el.style.display = show ? 'flex' : 'none';
};

// --- VALIDACIONES INDIVIDUALES ---
function validarNombre() {
    const nom = document.getElementById('nombre');
    const ok = nom.value.trim().length >= 3 && NAME_REGEX.test(nom.value) && !/^\d+$/.test(nom.value);
    nom.classList.toggle('is-invalid', !ok);
    toggleError('err-nombre', !ok);
    return ok;
}

function validarDocumento() {
    const doc = document.getElementById('documento');
    const ok = /^\d{5,}$/.test(doc.value);
    doc.classList.toggle('is-invalid', !ok);
    toggleError('err-documento', !ok);
    return ok;
}

function validarFecha() {
    const fec = document.getElementById('fechaInicio');
    const hoy = new Date().toISOString().split('T')[0];
    const ok = fec.value !== "" && fec.value <= hoy;
    fec.classList.toggle('is-invalid', !ok);
    toggleError('err-fechaInicio', !ok);
    return ok;
}

function validarMinimo() {
    const min = document.getElementById('minimoCompra');
    const ok = min.value === "" || parseFloat(min.value) >= 0;
    min.classList.toggle('is-invalid', !ok);
    toggleError('err-minimo', !ok);
    return ok;
}

function validarTelefonos() {
    const tels = document.querySelectorAll('.tel-input');
    let allOk = true;
    let hasOne = false;
    tels.forEach(t => {
        if (t.value.trim() !== "") {
            hasOne = true;
            const ok = /^\d{7,15}$/.test(t.value);
            t.classList.toggle('is-invalid', !ok);
            if(!ok) allOk = false;
        }
    });
    const finalOk = hasOne && allOk;
    toggleError('err-telefonos', !finalOk);
    return finalOk;
}

function validarCorreos() {
    const mails = document.querySelectorAll('.email-input');
    let allOk = true;
    let hasOne = false;
    mails.forEach(m => {
        if (m.value.trim() !== "") {
            hasOne = true;
            const ok = EMAIL_REGEX.test(m.value);
            m.classList.toggle('is-invalid', !ok);
            if(!ok) allOk = false;
        }
    });
    const finalOk = hasOne && allOk;
    toggleError('err-correos', !finalOk);
    return finalOk;
}

function validarMateriales() {
    const matOk = document.querySelectorAll('.material-check:checked').length > 0;
    document.getElementById('container-materiales').classList.toggle('invalid-border', !matOk);
    toggleError('err-materiales', !matOk);
    return matOk;
}

// --- EVENTOS EN TIEMPO REAL ---
document.getElementById('nombre').addEventListener('input', validarNombre);
document.getElementById('documento').addEventListener('input', validarDocumento);
document.getElementById('fechaInicio').addEventListener('change', validarFecha);
document.getElementById('minimoCompra').addEventListener('input', validarMinimo);
document.getElementById('telefonos-container').addEventListener('input', (e) => { 
    if(e.target.classList.contains('tel-input')) validarTelefonos(); 
});
document.getElementById('correos-container').addEventListener('input', (e) => { 
    if(e.target.classList.contains('email-input')) validarCorreos(); 
});
document.querySelectorAll('.material-check').forEach(check => check.addEventListener('change', validarMateriales));

// --- DINÁMICOS ---
function agregarTelefono() {
    const div = document.createElement('div');
    div.className = 'fs-dyn-row';
    div.innerHTML = `<input type="text" name="telefono" class="fs-input tel-input" maxlength="15" 
                     oninput="this.value=this.value.replace(/[^0-9]/g,'')" placeholder="3001234567">
                     <button type="button" class="fs-btn-remove" onclick="this.parentElement.remove(); validarTelefonos();">
                        <i class="fa-solid fa-minus"></i>
                     </button>`;
    document.getElementById('telefonos-container').appendChild(div);
}

function agregarCorreo() {
    const div = document.createElement('div');
    div.className = 'fs-dyn-row';
    div.innerHTML = `<input type="email" name="correo" class="fs-input email-input" placeholder="ejemplo@correo.com">
                     <button type="button" class="fs-btn-remove" onclick="this.parentElement.remove(); validarCorreos();">
                        <i class="fa-solid fa-minus"></i>
                     </button>`;
    document.getElementById('correos-container').appendChild(div);
}

// --- SUBMIT ---
document.getElementById('formProveedor').addEventListener('submit', function(e) {
    e.preventDefault();
    
    const checks = [
        validarNombre(), 
        validarDocumento(), 
        validarFecha(), 
        validarMinimo(), 
        validarTelefonos(), 
        validarCorreos(), 
        validarMateriales()
    ];
    
    if (checks.every(res => res === true)) {
        Swal.fire({
            title: '¿Confirmar registro?',
            text: 'Verifica que todos los datos sean correctos',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#7c3aed',
            cancelButtonColor: '#6b7280',
            confirmButtonText: 'Sí, registrar',
            cancelButtonText: 'Cancelar'
        }).then((r) => { 
            if (r.isConfirmed) {
                e.target.submit(); // ✅ Submit real
            } 
        });
    } else {
        const err = document.querySelector('.is-invalid, .invalid-border');
        if(err) err.scrollIntoView({ behavior: 'smooth', block: 'center' });
        Swal.fire({
            title: 'Campos inválidos', 
            text: 'Revisa los datos marcados en rojo.', 
            icon: 'error',
            confirmButtonColor: '#7c3aed'
        });
    }
});

// ✅ Validar al cargar
document.addEventListener('DOMContentLoaded', function() {
    validarNombre();
    validarDocumento();
    validarFecha();
    validarMinimo();
    validarTelefonos();
    validarCorreos();
    validarMateriales();
});
</script>
</body>
</html>