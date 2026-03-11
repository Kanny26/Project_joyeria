<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    if (session.getAttribute("admin") == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }
    String errorServidor = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Agregar Usuario - AAC27</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/forms-global.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/producto.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <style>
        .field-msg {
            font-size: 0.72rem; display: none;
            align-items: center; gap: 4px; margin-top: 3px;
        }
        .field-msg.error   { color: #dc2626; }
        .field-msg.visible { display: flex; }
        .fs-input.is-invalid { border-color: #ef4444 !important; background: #fef2f2; }
        .fs-input.is-valid   { border-color: #22c55e !important; background: #f0fdf4; }
    </style>
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/UsuarioServlet" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<main class="fs-container">
    <h2 class="fs-page-title">
        <i class="fa-solid fa-user-plus"></i> Añadir Usuario
    </h2>

    <form id="formUsuario" class="fs-form" method="post"
          action="<%= request.getContextPath() %>/UsuarioServlet" novalidate>
        <input type="hidden" name="accion" value="agregar">
        <input type="hidden" id="contrasenaEnvio" name="contrasena" value="VendedorAA27">

        <div class="fs-section">
            <div class="fs-section-title">
                <i class="fa-solid fa-user"></i> Datos Personales
            </div>
            <div class="fs-grid">
                <div class="fs-group">
                    <label class="fs-label" for="nombre">
                        <i class="fa-solid fa-user"></i> Nombre *
                    </label>
                    <input id="nombre" type="text" name="nombre" class="fs-input"
                           placeholder="Nombre completo" maxlength="80" required>
                    <div class="field-msg error" id="err-nombre">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <span id="txt-nombre">Solo letras y espacios (mín. 3).</span>
                    </div>
                </div>

                <div class="fs-group">
                    <label class="fs-label" for="documento">
                        <i class="fa-solid fa-id-card"></i> Documento *
                    </label>
                    <input id="documento" type="text" name="documento" class="fs-input"
                           placeholder="Mínimo 10 números" maxlength="20" inputmode="numeric" required>
                    <div class="field-msg error" id="err-documento">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <span id="txt-documento">Solo números, mínimo 10 dígitos.</span>
                    </div>
                </div>

                <div class="fs-group">
                    <label class="fs-label" for="correo">
                        <i class="fa-solid fa-envelope"></i> Correo *
                    </label>
                    <input id="correo" type="email" name="correo" class="fs-input"
                           placeholder="correo@ejemplo.com" required>
                    <div class="field-msg error" id="err-correo">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <span id="txt-correo">Formato inválido.</span>
                    </div>
                </div>

                <div class="fs-group">
                    <label class="fs-label" for="telefono">
                        <i class="fa-solid fa-phone"></i> Teléfono *
                    </label>
                    <input id="telefono" type="tel" name="telefono" class="fs-input"
                           placeholder="Debe iniciar con 3" maxlength="15" inputmode="numeric" required>
                    <div class="field-msg error" id="err-telefono">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <span id="txt-telefono">Debe iniciar con 3 y tener al menos 10 dígitos.</span>
                    </div>
                </div>

                <div class="fs-group">
                    <label class="fs-label" for="fechaRegistro">
                        <i class="fa-regular fa-calendar"></i> Fecha de Registro
                    </label>
                    <input id="fechaRegistro" type="date" name="fechaRegistro" class="fs-input">
                    <div class="field-msg error" id="err-fecha">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <span id="txt-fecha">La fecha no puede ser posterior a hoy.</span>
                    </div>
                </div>
            </div>
        </div>

        <div class="fs-section">
            <div class="fs-section-title">
                <i class="fa-solid fa-shield-halved"></i> Rol y Estado
            </div>
            <div class="fs-grid">
                <div class="fs-group">
                    <label class="fs-label" for="rol">
                        <i class="fa-solid fa-user-gear"></i> Rol *
                    </label>
                    <select id="rol" name="rol" class="fs-input" required>
                        <option value="vendedor">Vendedor</option>
                        <option value="administrador">Administrador</option>
                    </select>
                    <div class="fs-hint" style="margin-top:6px;">
                        <i class="fa-solid fa-wand-magic-sparkles"></i>
                        <span>Clave a generar: <strong id="preview-rol">VendedorAA27</strong></span>
                    </div>
                </div>

                <div class="fs-group">
                    <label class="fs-label">
                        <i class="fa-solid fa-toggle-on"></i> Estado
                    </label>
                    <div class="fs-radio-group">
                        <label class="fs-radio-chip">
                            <input type="radio" name="estado" value="Activo" checked>
                            <i class="fa-solid fa-circle-check" style="color:#16a34a;"></i> Activo
                        </label>
                        <label class="fs-radio-chip">
                            <input type="radio" name="estado" value="Inactivo">
                            <i class="fa-solid fa-circle-xmark" style="color:#dc2626;"></i> Inactivo
                        </label>
                    </div>
                </div>
            </div>
        </div>

        <div class="fs-actions">
            <button type="button" class="fs-btn-save" id="btnGuardar">
                <i class="fa-solid fa-user-plus"></i> Crear Usuario
            </button>
            <button type="button" class="fs-btn-cancel" onclick="history.back()">
                <i class="fa-solid fa-xmark"></i> Cancelar
            </button>
        </div>
    </form>
</main>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
/* CONSTANTES */
const PASSWORDS = { vendedor:'VendedorAA27', administrador:'AdminAA27' };
const LABELS    = { vendedor:'Vendedor',      administrador:'Administrador' };
const Toast = Swal.mixin({ toast:true, position:'top-end', showConfirmButton:false, timer:3000, timerProgressBar:true });

// Configurar el límite de fecha en el calendario al cargar
const hoy = new Date().toISOString().split('T')[0];
document.getElementById('fechaRegistro').setAttribute('max', hoy);

/* HELPERS VISUALES */
function mark(el, state) {
    el.classList.remove('is-invalid','is-valid');
    if (state === 'error') el.classList.add('is-invalid');
    if (state === 'ok')    el.classList.add('is-valid');
}
function showErr(errId, txtId, msg) {
    const el = document.getElementById(errId);
    if(el) el.classList.add('visible');
    if (txtId && msg) {
        const txt = document.getElementById(txtId);
        if(txt) txt.textContent = msg;
    }
}
function hideErr(errId) { 
    const el = document.getElementById(errId);
    if(el) el.classList.remove('visible'); 
}

/* VALIDACIONES */
function validarNombre() {
    const el = document.getElementById('nombre'), v = el.value.trim();
    const regexNombre = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]{3,80}$/;
    if (v.length < 3) { mark(el,'error'); showErr('err-nombre','txt-nombre','Mínimo 3 caracteres.'); return false; }
    if (!regexNombre.test(v)) { mark(el,'error'); showErr('err-nombre','txt-nombre','Solo letras y espacios.'); return false; }
    mark(el,'ok'); hideErr('err-nombre'); return true;
}

function validarDocumento() {
    const el = document.getElementById('documento'), v = el.value.trim();
    if (!/^[0-9]+$/.test(v)) { mark(el,'error'); showErr('err-documento','txt-documento','Solo números.'); return false; }
    if (v.length < 10) { mark(el,'error'); showErr('err-documento','txt-documento','Mínimo 10 dígitos.'); return false; }
    mark(el,'ok'); hideErr('err-documento'); return true;
}

function validarCorreo() {
    const el = document.getElementById('correo'), v = el.value.trim();
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
    if (!regex.test(v)) { mark(el,'error'); showErr('err-correo','txt-correo','Correo inválido.'); return false; }
    mark(el,'ok'); hideErr('err-correo'); return true;
}

function validarTelefono() {
    const el = document.getElementById('telefono'), v = el.value.trim();
    if (!v.startsWith('3')) { mark(el,'error'); showErr('err-telefono','txt-telefono','Debe iniciar con 3.'); return false; }
    if (v.length < 10) { mark(el,'error'); showErr('err-telefono','txt-telefono','Mínimo 10 dígitos.'); return false; }
    if (!/^[0-9]+$/.test(v)) { mark(el,'error'); showErr('err-telefono','txt-telefono','Solo números.'); return false; }
    mark(el,'ok'); hideErr('err-telefono'); return true;
}

function validarFecha() {
    const el = document.getElementById('fechaRegistro'), v = el.value;
    if (!v) return true; // Es opcional según el diseño original
    
    const fechaSeleccionada = new Date(v + "T00:00:00");
    const fechaHoy = new Date();
    fechaHoy.setHours(0, 0, 0, 0);

    if (fechaSeleccionada > fechaHoy) { 
        mark(el,'error'); 
        showErr('err-fecha','txt-fecha','La fecha no puede ser posterior a hoy (' + hoy + ').'); 
        return false; 
    }
    mark(el,'ok'); hideErr('err-fecha'); return true;
}

/* BLOQUEO TECLAS */
document.getElementById('nombre').addEventListener('keypress', e => {
    if (!/[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/.test(e.key)) e.preventDefault();
});
[document.getElementById('documento'), document.getElementById('telefono')].forEach(input => {
    input.addEventListener('keypress', e => { if (!/[0-9]/.test(e.key)) e.preventDefault(); });
});

/* NOTIFICACIONES */
const rolSel = document.getElementById('rol');
rolSel.addEventListener('change', function() {
    const pass = PASSWORDS[this.value];
    const label = LABELS[this.value];
    document.getElementById('preview-rol').textContent = pass;
    document.getElementById('contrasenaEnvio').value = pass;
    Toast.fire({ icon:'info', title: 'Rol: ' + label + '. Clave: ' + pass });
});

document.querySelectorAll('input[name="estado"]').forEach(radio => {
    radio.addEventListener('change', function() {
        Toast.fire({ icon: this.value === 'Activo' ? 'success' : 'warning', title: 'Estado: ' + this.value });
    });
});

/* LISTENERS */
document.getElementById('nombre').addEventListener('input', validarNombre);
document.getElementById('documento').addEventListener('input', validarDocumento);
document.getElementById('correo').addEventListener('input', validarCorreo);
document.getElementById('telefono').addEventListener('input', validarTelefono);
document.getElementById('fechaRegistro').addEventListener('change', validarFecha);

document.getElementById('btnGuardar').addEventListener('click', async function() {
    if ([validarNombre(), validarDocumento(), validarCorreo(), validarTelefono(), validarFecha()].includes(false)) {
        Swal.fire({ icon:'warning', title:'Campos inválidos', text:'Revisa los errores en rojo.' });
        return;
    }
    const conf = await Swal.fire({
        icon: 'question',
        title: '¿Confirmar registro?',
        text: 'Se enviará la clave al correo.',
        showCancelButton: true,
        confirmButtonColor: '#7c3aed',
        confirmButtonText: 'Sí, crear'
    });
    if (conf.isConfirmed) {
        this.disabled = true;
        Swal.fire({ title:'Procesando...', allowOutsideClick:false, didOpen:() => Swal.showLoading() });
        document.getElementById('formUsuario').submit();
    }
});

<% if (errorServidor != null) { %>
    Toast.fire({ icon:'error', title: '<%= errorServidor.replace("'", "\\'") %>' });
<% } %>
</script>
</body>
</html>