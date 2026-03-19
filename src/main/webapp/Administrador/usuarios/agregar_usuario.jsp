<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Object adminSesion = session.getAttribute("admin");
    Object superAdminSesion = session.getAttribute("superadmin");
    if (adminSesion == null && superAdminSesion == null) {
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
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/usuarios/agregar_usuario.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/form-global.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
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

        <%-- SECCIÓN: Datos personales --%>
        <div class="fs-section">
            <div class="fs-section-title">
                <i class="fa-solid fa-user"></i> Datos Personales
            </div>
            <div class="fs-grid">

                <%-- CAMPO: Nombre (usado como usuario de login) --%>
                <div class="fs-group">
                    <label class="fs-label" for="nombre">
                        <i class="fa-solid fa-user"></i> Usuario *
                    </label>
                    <input id="nombre" type="text" name="nombre" class="fs-input"
                           placeholder="Solo letras, máx. 10 caracteres" maxlength="10" required
                           autocomplete="off">
                    <div class="field-msg field-msg--error" id="err-nombre">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <span id="txt-nombre">El usuario solo puede contener letras y máximo 10 caracteres.</span>
                    </div>
                    <div class="field-msg field-msg--ok" id="ok-nombre">
                        <i class="fa-solid fa-circle-check"></i>
                        <span>Usuario válido.</span>
                    </div>
                </div>

                <%-- CAMPO: Documento --%>
                <div class="fs-group">
                    <label class="fs-label" for="documento">
                        <i class="fa-solid fa-id-card"></i> Documento *
                    </label>
                    <input id="documento" type="text" name="documento" class="fs-input"
                           placeholder="Mínimo 10 números" maxlength="20" inputmode="numeric" required>
                    <div class="field-msg field-msg--error" id="err-documento">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <span id="txt-documento">El documento solo puede contener números y debe tener mínimo 10 dígitos.</span>
                    </div>
                    <div class="field-msg field-msg--ok" id="ok-documento">
                        <i class="fa-solid fa-circle-check"></i>
                        <span>Documento válido.</span>
                    </div>
                </div>

                <%-- CAMPO: Correo electrónico --%>
                <div class="fs-group">
                    <label class="fs-label" for="correo">
                        <i class="fa-solid fa-envelope"></i> Correo electrónico *
                    </label>
                    <input id="correo" type="email" name="correo" class="fs-input"
                           placeholder="ejemplo@dominio.com" required autocomplete="off">
                    <div class="field-msg field-msg--error" id="err-correo">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <span id="txt-correo">El correo ingresado no es válido. Usa el formato: usuario@dominio.com</span>
                    </div>
                    <div class="field-msg field-msg--ok" id="ok-correo">
                        <i class="fa-solid fa-circle-check"></i>
                        <span>Correo válido.</span>
                    </div>
                </div>

                <%-- CAMPO: Teléfono --%>
                <div class="fs-group">
                    <label class="fs-label" for="telefono">
                        <i class="fa-solid fa-phone"></i> Teléfono *
                    </label>
                    <input id="telefono" type="tel" name="telefono" class="fs-input"
                           placeholder="Debe iniciar con 3" maxlength="15" inputmode="numeric" required>
                    <div class="field-msg field-msg--error" id="err-telefono">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <span id="txt-telefono">El teléfono debe iniciar con 3 y tener entre 10 y 15 dígitos. Solo números.</span>
                    </div>
                    <div class="field-msg field-msg--ok" id="ok-telefono">
                        <i class="fa-solid fa-circle-check"></i>
                        <span>Teléfono válido.</span>
                    </div>
                </div>

                <%-- CAMPO: Fecha de registro (opcional) --%>
                <div class="fs-group">
                    <label class="fs-label" for="fechaRegistro">
                        <i class="fa-regular fa-calendar"></i> Fecha de Registro
                    </label>
                    <input id="fechaRegistro" type="date" name="fechaRegistro" class="fs-input">
                    <div class="field-msg field-msg--error" id="err-fecha">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <span id="txt-fecha">La fecha no puede ser posterior a hoy.</span>
                    </div>
                </div>

            </div>
        </div>

        <%-- SECCIÓN: Rol y estado --%>
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
                    <div class="fs-hint">
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
                            <i class="fa-solid fa-circle-check fs-icon--success"></i> Activo
                        </label>
                        <label class="fs-radio-chip">
                            <input type="radio" name="estado" value="Inactivo">
                            <i class="fa-solid fa-circle-xmark fs-icon--danger"></i> Inactivo
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
// =====================================================================
// CONSTANTES Y PATRONES
// Estos patrones deben mantenerse sincronizados con los del backend (UsuarioServlet.java).
// El frontend valida para dar retroalimentación inmediata al usuario,
// pero el backend siempre valida de nuevo antes de guardar.
// =====================================================================

const PASSWORDS = { vendedor: 'VendedorAA27', administrador: 'AdminAA27' };
const LABELS    = { vendedor: 'Vendedor',      administrador: 'Administrador' };

// Solo letras (con o sin tilde), sin espacios, sin números, máximo 10 caracteres
const REGEX_NOMBRE   = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ]{1,10}$/;

// Formato estándar de correo electrónico
const REGEX_CORREO   = /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/;

// Solo dígitos, inicia con 3, entre 10 y 15 caracteres
const REGEX_TELEFONO = /^3[0-9]{9,14}$/;

// Solo dígitos, mínimo 10
const REGEX_DOCUMENTO = /^[0-9]{10,20}$/;

// Toast: notificación pequeña no intrusiva en la esquina superior
const Toast = Swal.mixin({
    toast: true,
    position: 'top-end',
    showConfirmButton: false,
    timer: 3000,
    timerProgressBar: true
});

// Bloquear fechas futuras en el selector de fecha
const hoy = new Date().toISOString().split('T')[0];
document.getElementById('fechaRegistro').setAttribute('max', hoy);


// =====================================================================
// HELPERS VISUALES
// =====================================================================

// Marca el campo como válido, inválido, o limpia el estado visual
function mark(el, state) {
    el.classList.remove('is-invalid', 'is-valid');
    if (state === 'error') el.classList.add('is-invalid');
    if (state === 'ok')    el.classList.add('is-valid');
}

// Muestra el mensaje de error debajo del campo y actualiza su texto si se indica
function showErr(errId, txtId, msg) {
    const errEl = document.getElementById(errId);
    if (errEl) errEl.classList.add('visible');
    if (txtId && msg) {
        const txtEl = document.getElementById(txtId);
        if (txtEl) txtEl.textContent = msg;
    }
    // Oculta el mensaje de OK si estaba visible
    const okId = errId.replace('err-', 'ok-');
    const okEl = document.getElementById(okId);
    if (okEl) okEl.classList.remove('visible');
}

// Oculta el mensaje de error y muestra el de OK para ese campo
function showOk(errId) {
    const errEl = document.getElementById(errId);
    if (errEl) errEl.classList.remove('visible');
    const okId = errId.replace('err-', 'ok-');
    const okEl = document.getElementById(okId);
    if (okEl) okEl.classList.add('visible');
}


// =====================================================================
// FUNCIONES DE VALIDACIÓN INDIVIDUALES
// Cada función valida un campo específico, marca el estado visual
// y retorna true (válido) o false (inválido).
// =====================================================================

function validarNombre() {
    const el = document.getElementById('nombre');
    const v  = el.value.trim();

    if (v.length === 0) {
        mark(el, 'error');
        showErr('err-nombre', 'txt-nombre', 'El nombre del usuario es obligatorio.');
        return false;
    }
    // Verificación carácter a carácter para dar mensajes más precisos
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

function validarDocumento() {
    const el = document.getElementById('documento');
    const v  = el.value.trim();

    if (v.length === 0) {
        mark(el, 'error');
        showErr('err-documento', 'txt-documento', 'El documento es obligatorio.');
        return false;
    }
    if (/[^0-9]/.test(v)) {
        mark(el, 'error');
        showErr('err-documento', 'txt-documento', 'No se permiten caracteres especiales ni letras en el documento.');
        return false;
    }
    if (v.length < 10) {
        mark(el, 'error');
        showErr('err-documento', 'txt-documento', 'El documento debe tener mínimo 10 dígitos.');
        return false;
    }
    mark(el, 'ok');
    showOk('err-documento');
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

    if (v.length === 0) {
        mark(el, 'error');
        showErr('err-telefono', 'txt-telefono', 'El teléfono es obligatorio.');
        return false;
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

function validarFecha() {
    const el = document.getElementById('fechaRegistro');
    const v  = el.value;
    // La fecha es opcional; si está vacía no se valida
    if (!v) return true;

    const seleccionada = new Date(v + 'T00:00:00');
    const fechaHoy     = new Date();
    fechaHoy.setHours(0, 0, 0, 0);

    if (seleccionada > fechaHoy) {
        mark(el, 'error');
        showErr('err-fecha', 'txt-fecha', 'La fecha no puede ser posterior a hoy.');
        return false;
    }
    mark(el, 'ok');
    return true;
}


// =====================================================================
// BLOQUEO DE TECLAS
// Impide escribir caracteres no permitidos mientras el usuario escribe,
// para evitar confusión. Esto es un complemento de la validación,
// no un reemplazo (el backend siempre valida de nuevo).
// =====================================================================

// El campo nombre solo acepta letras (con tilde incluida)
document.getElementById('nombre').addEventListener('keypress', function(e) {
    // Permite teclas de control (Enter, Backspace, flechas, etc.)
    if (e.key.length > 1) return;
    if (!/[a-zA-ZáéíóúÁÉÍÓÚñÑ]/.test(e.key)) {
        e.preventDefault();
        Toast.fire({ icon: 'warning', title: 'Solo se permiten letras en este campo.' });
    }
});

// El campo nombre tampoco acepta espacios con la barra espaciadora
document.getElementById('nombre').addEventListener('keydown', function(e) {
    if (e.key === ' ') {
        e.preventDefault();
        Toast.fire({ icon: 'warning', title: 'El usuario no puede contener espacios.' });
    }
});

// Documento y teléfono: solo dígitos
['documento', 'telefono'].forEach(function(id) {
    document.getElementById(id).addEventListener('keypress', function(e) {
        if (e.key.length > 1) return;
        if (!/[0-9]/.test(e.key)) {
            e.preventDefault();
            Toast.fire({ icon: 'warning', title: 'No se permiten caracteres especiales en este campo.' });
        }
    });
});

// Correo: bloquear caracteres claramente inválidos que el usuario podría teclear por error
document.getElementById('correo').addEventListener('keypress', function(e) {
    if (e.key.length > 1) return;
    // Se bloquean caracteres que nunca son válidos en un correo
    if (/[#$%&*()\[\]{}<>\\\/'"`,;!?=+]/.test(e.key)) {
        e.preventDefault();
        Toast.fire({ icon: 'warning', title: 'No se permiten caracteres especiales en el correo.' });
    }
});


// =====================================================================
// VALIDACIÓN EN TIEMPO REAL (al escribir en cada campo)
// =====================================================================

document.getElementById('nombre').addEventListener('input', validarNombre);
document.getElementById('documento').addEventListener('input', validarDocumento);
document.getElementById('correo').addEventListener('input', validarCorreo);
document.getElementById('telefono').addEventListener('input', validarTelefono);
document.getElementById('fechaRegistro').addEventListener('change', validarFecha);


// =====================================================================
// COMPORTAMIENTO DEL SELECTOR DE ROL
// =====================================================================

document.getElementById('rol').addEventListener('change', function() {
    const pass  = PASSWORDS[this.value];
    const label = LABELS[this.value];
    document.getElementById('preview-rol').textContent = pass;
    document.getElementById('contrasenaEnvio').value   = pass;
    Toast.fire({ icon: 'info', title: 'Rol: ' + label + '. Clave a enviar: ' + pass });
});

document.querySelectorAll('input[name="estado"]').forEach(function(radio) {
    radio.addEventListener('change', function() {
        Toast.fire({
            icon:  this.value === 'Activo' ? 'success' : 'warning',
            title: 'Estado seleccionado: ' + this.value
        });
    });
});


// =====================================================================
// ENVÍO DEL FORMULARIO
// Se validan todos los campos antes de confirmar. Si alguno falla,
// se muestra el resumen de errores y no se envía el formulario.
// =====================================================================

document.getElementById('btnGuardar').addEventListener('click', async function() {
    // Ejecutar todas las validaciones y recopilar resultados
    const resultados = [
        validarNombre(),
        validarDocumento(),
        validarCorreo(),
        validarTelefono(),
        validarFecha()
    ];

    const hayErrores = resultados.includes(false);

    if (hayErrores) {
        Swal.fire({
            icon: 'warning',
            title: 'Por favor revisa el formulario',
            text: 'Hay campos con información incorrecta. Corrige los errores marcados en rojo para continuar.',
            confirmButtonColor: '#7c3aed',
            confirmButtonText: 'Entendido'
        });
        // Llevar el scroll al primer campo con error
        const primerError = document.querySelector('.is-invalid');
        if (primerError) primerError.scrollIntoView({ behavior: 'smooth', block: 'center' });
        return;
    }

    // Pedir confirmación antes de crear el usuario
    const confirmacion = await Swal.fire({
        icon: 'question',
        title: '¿Confirmar registro?',
        text: 'Se creará el usuario y se enviarán las credenciales al correo indicado.',
        showCancelButton: true,
        confirmButtonColor: '#7c3aed',
        cancelButtonColor:  '#6b7280',
        confirmButtonText:  'Sí, crear usuario',
        cancelButtonText:   'Cancelar'
    });

    if (confirmacion.isConfirmed) {
        // Deshabilitar el botón para evitar doble envío
        this.disabled = true;
        this.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Creando usuario...';
        Swal.fire({
            title: 'Registrando usuario...',
            text: 'Por favor espera un momento.',
            allowOutsideClick: false,
            didOpen: () => Swal.showLoading()
        });
        document.getElementById('formUsuario').submit();
    }
});


// =====================================================================
// MENSAJE DE ERROR DEL SERVIDOR
// Si el backend rechazó el formulario, se muestra al cargar la página.
// =====================================================================
<% if (errorServidor != null) { %>
    Swal.fire({
        icon: 'error',
        title: 'No se pudo crear el usuario',
        text: '<%= errorServidor.replace("'", "\\'") %>',
        confirmButtonColor: '#7c3aed'
    });
<% } %>
</script>
</body>
</html>
