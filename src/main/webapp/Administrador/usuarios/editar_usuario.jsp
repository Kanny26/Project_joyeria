<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="model.Usuario"%>
<%
    Object adminSesion = session.getAttribute("admin");
    Object superAdminSesion = session.getAttribute("superadmin");
    if (adminSesion == null && superAdminSesion == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }

    // El servlet carga el usuario desde la BD y lo pasa como atributo
    Usuario usuario = (Usuario) request.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/UsuarioServlet");
        return;
    }

    // El backend puede enviar un mensaje de error si rechazó los datos
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar Usuario - AAC27</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/usuarios/editar.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/form-global.css">
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
    <h2 class="fs-page-title"><i class="fa-solid fa-user-pen"></i> Editar Usuario</h2>

    <form id="formEditar" class="fs-form" method="post" action="<%= request.getContextPath() %>/UsuarioServlet">
        <input type="hidden" name="accion" value="editar">
        <input type="hidden" name="id" value="<%= usuario.getUsuarioId() %>">

        <%-- SECCIÓN: Información editable --%>
        <div class="fs-section">
            <div class="fs-section-title"><i class="fa-solid fa-pen-to-square"></i> Información Editable</div>
            <div class="fs-grid">

                <%-- CAMPO: Nombre (usuario de login) --%>
                <div class="fs-group">
                    <label class="fs-label" for="nombre">
                        <i class="fa-solid fa-user"></i> Usuario *
                    </label>
                    <%-- Se precarga el valor actual del usuario para que el formulario lo muestre --%>
                    <input id="nombre" type="text" name="nombre" class="fs-input"
                           maxlength="10" required autocomplete="off"
                           value="<%= usuario.getNombre() != null ? usuario.getNombre() : "" %>">
                    <div class="field-msg field-msg--error" id="err-nombre">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <span id="txt-nombre">El usuario solo puede contener letras y máximo 10 caracteres.</span>
                    </div>
                    <div class="field-msg field-msg--ok" id="ok-nombre">
                        <i class="fa-solid fa-circle-check"></i>
                        <span>Usuario válido.</span>
                    </div>
                </div>

                <%-- CAMPO: Correo electrónico --%>
                <div class="fs-group">
                    <label class="fs-label" for="correo">
                        <i class="fa-solid fa-envelope"></i> Correo Electrónico *
                    </label>
                    <input id="correo" type="email" name="correo" class="fs-input"
                           required autocomplete="off"
                           value="<%= usuario.getCorreo() != null ? usuario.getCorreo() : "" %>">
                    <div class="field-msg field-msg--error" id="err-correo">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <span id="txt-correo">El correo ingresado no es válido. Usa el formato: usuario@dominio.com</span>
                    </div>
                    <div class="field-msg field-msg--ok" id="ok-correo">
                        <i class="fa-solid fa-circle-check"></i>
                        <span>Correo válido.</span>
                    </div>
                </div>

                <%-- CAMPO: Teléfono (opcional en edición) --%>
                <div class="fs-group">
                    <label class="fs-label" for="telefono">
                        <i class="fa-solid fa-phone"></i> Teléfono
                    </label>
                    <input id="telefono" type="tel" name="telefono" class="fs-input"
                           maxlength="15" inputmode="numeric"
                           value="<%= usuario.getTelefono() != null ? usuario.getTelefono() : "" %>">
                    <div class="field-msg field-msg--error" id="err-telefono">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <span id="txt-telefono">El teléfono debe iniciar con 3 y tener entre 10 y 15 dígitos. Solo números.</span>
                    </div>
                    <div class="field-msg field-msg--ok" id="ok-telefono">
                        <i class="fa-solid fa-circle-check"></i>
                        <span>Teléfono válido.</span>
                    </div>
                </div>

                <%-- CAMPO: Rol --%>
                <div class="fs-group">
                    <label class="fs-label" for="rol">
                        <i class="fa-solid fa-user-gear"></i> Rol *
                    </label>
                    <%-- equalsIgnoreCase permite comparar sin importar si el rol viene en mayúsculas o minúsculas --%>
                    <select id="rol" name="rol" class="fs-input" required>
                        <option value="Administrador" <%= "Administrador".equalsIgnoreCase(usuario.getRol()) ? "selected" : "" %>>Administrador</option>
                        <option value="Vendedor"      <%= "Vendedor".equalsIgnoreCase(usuario.getRol())      ? "selected" : "" %>>Vendedor</option>
                    </select>
                </div>

            </div>
        </div>

        <%-- SECCIÓN: Estado del usuario --%>
        <div class="fs-section">
            <div class="fs-section-title"><i class="fa-solid fa-toggle-on"></i> Estado</div>
            <div class="fs-radio-group">
                <%-- Se marca el radio que corresponde al estado actual del usuario --%>
                <label class="fs-radio-chip">
                    <input type="radio" name="estado" value="Activo" <%= usuario.isEstado() ? "checked" : "" %>>
                    <i class="fa-solid fa-circle-check fs-icon--success"></i> Activo
                </label>
                <label class="fs-radio-chip">
                    <input type="radio" name="estado" value="Inactivo" <%= !usuario.isEstado() ? "checked" : "" %>>
                    <i class="fa-solid fa-circle-xmark fs-icon--danger"></i> Inactivo
                </label>
            </div>
        </div>

        <div class="fs-actions">
            <button type="submit" class="fs-btn-save" id="btnGuardar">
                <i class="fa-solid fa-floppy-disk"></i> Guardar Cambios
            </button>
            <a href="<%= request.getContextPath() %>/UsuarioServlet" class="fs-btn-cancel">
                <i class="fa-solid fa-xmark"></i> Cancelar
            </a>
        </div>
    </form>
</main>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
// =====================================================================
// PATRONES DE VALIDACIÓN (idénticos a los del backend para consistencia)
// =====================================================================

const REGEX_NOMBRE   = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ]{1,10}$/;
const REGEX_CORREO   = /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/;
const REGEX_TELEFONO = /^3[0-9]{9,14}$/;

const Toast = Swal.mixin({
    toast: true,
    position: 'top-end',
    showConfirmButton: false,
    timer: 3000,
    timerProgressBar: true
});


// =====================================================================
// HELPERS VISUALES
// =====================================================================

function mark(el, state) {
    el.classList.remove('is-invalid', 'is-valid');
    if (state === 'error') el.classList.add('is-invalid');
    if (state === 'ok')    el.classList.add('is-valid');
}

function showErr(errId, txtId, msg) {
    const errEl = document.getElementById(errId);
    if (errEl) errEl.classList.add('visible');
    if (txtId && msg) {
        const txtEl = document.getElementById(txtId);
        if (txtEl) txtEl.textContent = msg;
    }
    const okEl = document.getElementById(errId.replace('err-', 'ok-'));
    if (okEl) okEl.classList.remove('visible');
}

function showOk(errId) {
    const errEl = document.getElementById(errId);
    if (errEl) errEl.classList.remove('visible');
    const okEl = document.getElementById(errId.replace('err-', 'ok-'));
    if (okEl) okEl.classList.add('visible');
}


// =====================================================================
// FUNCIONES DE VALIDACIÓN
// =====================================================================

function validarNombre() {
    const el = document.getElementById('nombre');
    const v  = el.value.trim();

    if (v.length === 0) {
        mark(el, 'error');
        showErr('err-nombre', 'txt-nombre', 'El nombre del usuario es obligatorio.');
        return false;
    }
    if (/[0-9]/.test(v)) {
        mark(el, 'error');
        showErr('err-nombre', 'txt-nombre', 'El usuario no puede contener números.');
        return false;
    }
    if (/[\s]/.test(v)) {
        mark(el, 'error');
        showErr('err-nombre', 'txt-nombre', 'El usuario no puede contener espacios.');
        return false;
    }
    if (/[^a-zA-ZáéíóúÁÉÍÓÚñÑ]/.test(v)) {
        mark(el, 'error');
        showErr('err-nombre', 'txt-nombre', 'No se permiten caracteres especiales en el usuario.');
        return false;
    }
    if (v.length > 10) {
        mark(el, 'error');
        showErr('err-nombre', 'txt-nombre', 'El usuario no puede superar los 10 caracteres.');
        return false;
    }
    mark(el, 'ok');
    showOk('err-nombre');
    return true;
}

function validarCorreo() {
    const el = document.getElementById('correo');
    const v  = el.value.trim();

    if (v.length === 0) {
        mark(el, 'error');
        showErr('err-correo', 'txt-correo', 'El correo electrónico es obligatorio.');
        return false;
    }
    if (!REGEX_CORREO.test(v)) {
        mark(el, 'error');
        showErr('err-correo', 'txt-correo', 'El correo ingresado no es válido. Usa el formato: usuario@dominio.com');
        return false;
    }
    mark(el, 'ok');
    showOk('err-correo');
    return true;
}

function validarTelefono() {
    const el = document.getElementById('telefono');
    const v  = el.value.trim();

    // El teléfono es opcional en edición; si está vacío, es válido
    if (v.length === 0) {
        mark(el, '');
        return true;
    }
    if (/[^0-9]/.test(v)) {
        mark(el, 'error');
        showErr('err-telefono', 'txt-telefono', 'No se permiten caracteres especiales ni letras en el teléfono.');
        return false;
    }
    if (!v.startsWith('3')) {
        mark(el, 'error');
        showErr('err-telefono', 'txt-telefono', 'El teléfono debe iniciar con el número 3.');
        return false;
    }
    if (v.length < 10 || v.length > 15) {
        mark(el, 'error');
        showErr('err-telefono', 'txt-telefono', 'El teléfono debe tener entre 10 y 15 dígitos.');
        return false;
    }
    mark(el, 'ok');
    showOk('err-telefono');
    return true;
}


// =====================================================================
// BLOQUEO DE TECLAS
// =====================================================================

document.getElementById('nombre').addEventListener('keypress', function(e) {
    if (e.key.length > 1) return;
    if (!/[a-zA-ZáéíóúÁÉÍÓÚñÑ]/.test(e.key)) {
        e.preventDefault();
        Toast.fire({ icon: 'warning', title: 'Solo se permiten letras en este campo.' });
    }
});

document.getElementById('nombre').addEventListener('keydown', function(e) {
    if (e.key === ' ') {
        e.preventDefault();
        Toast.fire({ icon: 'warning', title: 'El usuario no puede contener espacios.' });
    }
});

document.getElementById('telefono').addEventListener('keypress', function(e) {
    if (e.key.length > 1) return;
    if (!/[0-9]/.test(e.key)) {
        e.preventDefault();
        Toast.fire({ icon: 'warning', title: 'No se permiten caracteres especiales en el teléfono.' });
    }
});

document.getElementById('correo').addEventListener('keypress', function(e) {
    if (e.key.length > 1) return;
    if (/[#$%&*()\[\]{}<>\\\/'"`,;!?=+]/.test(e.key)) {
        e.preventDefault();
        Toast.fire({ icon: 'warning', title: 'No se permiten caracteres especiales en el correo.' });
    }
});


// =====================================================================
// VALIDACIÓN EN TIEMPO REAL
// =====================================================================

document.getElementById('nombre').addEventListener('input', validarNombre);
document.getElementById('correo').addEventListener('input', validarCorreo);
document.getElementById('telefono').addEventListener('input', validarTelefono);


// =====================================================================
// ENVÍO DEL FORMULARIO CON CONFIRMACIÓN
// =====================================================================

document.getElementById('formEditar').addEventListener('submit', async function(e) {
    e.preventDefault(); // Detiene el envío inmediato para validar primero

    const resultados = [validarNombre(), validarCorreo(), validarTelefono()];
    const hayErrores = resultados.includes(false);

    if (hayErrores) {
        Swal.fire({
            icon: 'warning',
            title: 'Por favor revisa el formulario',
            text: 'Hay campos con información incorrecta. Corrige los errores marcados en rojo para continuar.',
            confirmButtonColor: '#7c3aed',
            confirmButtonText: 'Entendido'
        });
        const primerError = document.querySelector('.is-invalid');
        if (primerError) primerError.scrollIntoView({ behavior: 'smooth', block: 'center' });
        return;
    }

    // Pedir confirmación antes de guardar los cambios
    const confirmacion = await Swal.fire({
        title: '¿Guardar los cambios?',
        text: 'Los datos del usuario serán actualizados.',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#7c3aed',
        cancelButtonColor:  '#6b7280',
        confirmButtonText:  'Sí, guardar',
        cancelButtonText:   'Cancelar'
    });

    if (confirmacion.isConfirmed) {
        const btn = document.getElementById('btnGuardar');
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Guardando...';
        this.submit();
    }
});


// =====================================================================
// MENSAJE DE ERROR DEL SERVIDOR
// Si el backend rechazó los datos, se muestra al cargar la página.
// =====================================================================
<% if (error != null) { %>
    Swal.fire({
        icon: 'error',
        title: 'No se pudo guardar',
        text: '<%= error.replace("'", "\\'") %>',
        confirmButtonColor: '#7c3aed'
    });
<% } %>
</script>
</body>
</html>
