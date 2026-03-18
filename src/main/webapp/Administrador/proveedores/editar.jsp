<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="model.Proveedor, model.Material, java.util.List"%>
<%
    /* Seguridad: si no hay sesión activa de admin, redirige al login */
    Object admin = session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }

    /*
     * El proveedor llega como atributo del request via forward del servlet.
     * Si no existe (alguien accede directo a esta URL), se redirige al listado.
     */
    Proveedor p = (Proveedor) request.getAttribute("proveedor");
    if (p == null) {
        response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
        return;
    }

    List<Material> materiales = (List<Material>) request.getAttribute("materiales");
    if (materiales == null) materiales = new java.util.ArrayList<>();

    /*
     * errorServidor contiene el mensaje cuando el servlet rechazó el formulario.
     * Ejemplos: teléfono ya registrado, correo ya en uso en otro proveedor.
     * Se muestra en la parte superior y también con SweetAlert al cargar la página.
     */
    String errorServidor = (String) request.getAttribute("error");

    /* Listas de contacto pre-cargadas para llenar los campos del formulario */
    List<String> telefonos = p.getTelefonos() != null ? p.getTelefonos() : new java.util.ArrayList<>();
    List<String> correos   = p.getCorreos()   != null ? p.getCorreos()   : new java.util.ArrayList<>();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar Proveedor - AAC27</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/forms-global.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/producto.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%=request.getContextPath()%>/ProveedorServlet?action=listar" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<main class="form-product-container">

    <h2 class="form-product-container__title">
        <i class="fa-solid fa-pen-to-square" style="color:#7c3aed; margin-right:8px; font-size:1.1rem;"></i>
        Editar Proveedor
    </h2>

    <%-- Mensaje de error del backend visible en la parte superior del formulario --%>
    <% if (errorServidor != null && !errorServidor.isEmpty()) { %>
    <div class="server-error">
        <i class="fa-solid fa-circle-exclamation"></i> <%= errorServidor %>
    </div>
    <% } %>

    <form id="formEditar" class="form-product" method="post"
          action="<%=request.getContextPath()%>/ProveedorServlet" novalidate>

        <input type="hidden" name="action"      value="actualizar">
        <input type="hidden" name="proveedorId" value="<%= p.getProveedorId() %>">
        <input type="hidden" name="fechaInicio" value="<%= p.getFechaInicio() != null ? p.getFechaInicio() : "" %>">
        <input type="hidden" name="documento"   value="<%= p.getDocumento()   != null ? p.getDocumento()   : "" %>">

        <%-- ══════════════════════════════════════════
             SECCIÓN 1: IDENTIDAD
        ══════════════════════════════════════════ --%>
        <div class="form-section">
            <div class="section-sep">
                <i class="fa-solid fa-building"></i> Identidad del Proveedor
            </div>
            <div class="section-grid">

                <%-- NOMBRE — editable, solo letras y números --%>
                <div class="form-group">
                    <label class="form-label" for="nombre">
                        <i class="fa-solid fa-pen"></i> Nombre Comercial *
                    </label>
                    <input id="nombre" type="text" name="nombre" class="form-input"
                           value="<%= p.getNombre() != null ? p.getNombre() : "" %>"
                           placeholder="Ej: Joyería Aurora" maxlength="100" autocomplete="off">
                    <div class="field-msg error" id="err-nombre">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <span id="txt-nombre">Requerido — mínimo 3 caracteres, solo letras y números.</span>
                    </div>
                </div>

                <%-- DOCUMENTO — solo lectura --%>
                <div class="form-group">
                    <label class="form-label">
                        <i class="fa-solid fa-id-card"></i> Documento (NIT / CC)
                    </label>
                    <div class="input-readonly">
                        <i class="fa-solid fa-lock" style="color:#d1d5db; flex-shrink:0;"></i>
                        <%= p.getDocumento() %>
                    </div>
                    <span class="readonly-badge">
                        <i class="fa-solid fa-circle-info"></i> Identificador único — No editable
                    </span>
                </div>

                <%-- FECHA — solo lectura --%>
                <div class="form-group">
                    <label class="form-label">
                        <i class="fa-regular fa-calendar"></i> Fecha de Registro
                    </label>
                    <div class="input-readonly">
                        <i class="fa-regular fa-calendar" style="color:#d1d5db; flex-shrink:0;"></i>
                        <%= p.getFechaInicio() != null ? p.getFechaInicio() : "—" %>
                    </div>
                </div>

            </div>
        </div>

        <%-- ══════════════════════════════════════════
             SECCIÓN 2: CONFIGURACIÓN COMERCIAL
        ══════════════════════════════════════════ --%>
        <div class="form-section">
            <div class="section-sep">
                <i class="fa-solid fa-sliders"></i> Configuración Comercial
            </div>
            <div class="section-grid">
                <div class="form-group">
                    <label class="form-label" for="minimoCompra">
                        <i class="fa-solid fa-dollar-sign"></i> Monto Mínimo de Compra ($)
                    </label>
                    <input id="minimoCompra" type="number" name="minimoCompra"
                           class="form-input" min="0" step="1"
                           placeholder="0"
                           value="<%= p.getMinimoCompra() != null ? p.getMinimoCompra().longValue() : 0 %>">
                    <div class="field-msg error" id="err-minimo">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <span id="txt-minimo">El monto no puede ser negativo.</span>
                    </div>
                </div>
            </div>
        </div>

        <%-- ══════════════════════════════════════════
             SECCIÓN 3: CONTACTO
        ══════════════════════════════════════════ --%>
        <div class="form-section">
            <div class="section-sep">
                <i class="fa-solid fa-address-book"></i> Información de Contacto
            </div>
            <div class="section-grid">

                <%-- Teléfonos --%>
                <div class="form-group">
                    <label class="form-label">
                        <i class="fa-solid fa-phone"></i> Teléfonos *
                    </label>
                    <div id="telefonos-container">
                        <% if (!telefonos.isEmpty()) {
                               for (String tel : telefonos) { %>
                            <div class="dynamic-row">
                                <input type="tel" name="telefono" class="form-input tel-input"
                                       value="<%= tel != null ? tel.trim() : "" %>"
                                       placeholder="Ej: 3001234567" maxlength="15"
                                       inputmode="numeric">
                                <button type="button" class="btn-remove-dyn"
                                        onclick="quitarFila(this,'telefonos-container')" title="Quitar">
                                    <i class="fa-solid fa-minus"></i>
                                </button>
                            </div>
                        <%     }
                           } else { %>
                            <div class="dynamic-row">
                                <input type="tel" name="telefono" class="form-input tel-input"
                                       placeholder="Ej: 3001234567" maxlength="15"
                                       inputmode="numeric">
                            </div>
                        <% } %>
                    </div>
                    <div class="field-msg error" id="err-telefonos">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <span id="txt-tel">Solo dígitos, entre 7 y 15 caracteres.</span>
                    </div>
                    <button type="button" class="btn-add-dyn" onclick="agregarTelefono()">
                        <i class="fa-solid fa-plus"></i> Añadir Teléfono
                    </button>
                </div>

                <%-- Correos --%>
                <div class="form-group">
                    <label class="form-label">
                        <i class="fa-solid fa-envelope"></i> Correos Electrónicos *
                    </label>
                    <div id="correos-container">
                        <% if (!correos.isEmpty()) {
                               for (String correo : correos) { %>
                            <div class="dynamic-row">
                                <input type="email" name="correo" class="form-input email-input"
                                       value="<%= correo != null ? correo.trim() : "" %>"
                                       placeholder="correo@ejemplo.com">
                                <button type="button" class="btn-remove-dyn"
                                        onclick="quitarFila(this,'correos-container')" title="Quitar">
                                    <i class="fa-solid fa-minus"></i>
                                </button>
                            </div>
                        <%     }
                           } else { %>
                            <div class="dynamic-row">
                                <input type="email" name="correo" class="form-input email-input"
                                       placeholder="correo@ejemplo.com">
                            </div>
                        <% } %>
                    </div>
                    <div class="field-msg error" id="err-correos">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <span id="txt-correo">Formato inválido. Debe contener @ y dominio (.com, .co, etc.).</span>
                    </div>
                    <button type="button" class="btn-add-dyn" onclick="agregarCorreo()">
                        <i class="fa-solid fa-plus"></i> Añadir Correo
                    </button>
                </div>

            </div>
        </div>

        <%-- ══════════════════════════════════════════
             SECCIÓN 4: MATERIALES
        ══════════════════════════════════════════ --%>
        <div class="form-section">
            <div class="section-sep">
                <i class="fa-solid fa-cubes"></i> Catálogo de Materiales *
            </div>
            <% if (materiales.isEmpty()) { %>
                <p style="color:#9ca3af; font-size:0.85rem;">No hay materiales registrados en el sistema.</p>
            <% } else { %>
            <div class="materiales-grid">
                <% for (Material m : materiales) {
                       boolean sel = false;
                       if (p.getMateriales() != null) {
                           for (Material pm : p.getMateriales()) {
                               if (pm.getMaterialId() != null && pm.getMaterialId().equals(m.getMaterialId())) {
                                   sel = true; break;
                               }
                           }
                       }
                %>
                <label class="material-label">
                    <input type="checkbox" name="materiales"
                           value="<%= m.getMaterialId() %>"
                           class="material-check" <%= sel ? "checked" : "" %>>
                    <%= m.getNombre() %>
                </label>
                <% } %>
            </div>
            <div class="materiales-bloque-error" id="err-materiales">
                <i class="fa-solid fa-circle-exclamation"></i>
                Debes seleccionar al menos un material.
            </div>
            <% } %>
        </div>

        <%-- ══════════════════════════════════════════
             SECCIÓN 5: ESTADO
        ══════════════════════════════════════════ --%>
        <div class="form-section">
            <div class="section-sep">
                <i class="fa-solid fa-toggle-on"></i> Estado Operativo
            </div>
            <div class="estado-group">
                <label class="estado-option">
                    <input type="radio" name="estado" value="activo"
                           id="radioActivo" <%= p.isEstado() ? "checked" : "" %>>
                    <i class="fa-solid fa-circle-check" style="color:#16a34a;"></i> Activo
                </label>
                <label class="estado-option">
                    <input type="radio" name="estado" value="inactivo"
                           id="radioInactivo" <%= !p.isEstado() ? "checked" : "" %>>
                    <i class="fa-solid fa-circle-xmark" style="color:#dc2626;"></i> Inactivo
                </label>
            </div>
            <p class="estado-hint">
                <i class="fa-solid fa-circle-info" style="margin-top:2px; color:#9177a8;"></i>
                Marcar como <strong>Inactivo</strong> impedirá que aparezca al registrar nuevas compras.
                Podrás reactivarlo en cualquier momento.
            </p>
        </div>

        <div class="form-actions">
            <button type="submit" class="btn-guardar">
                <i class="fa-solid fa-floppy-disk"></i> Guardar Cambios
            </button>
            <button type="button" class="btn-cancelar"
                    onclick="window.location.href='<%=request.getContextPath()%>/ProveedorServlet?action=listar'">
                <i class="fa-solid fa-xmark"></i> Cancelar
            </button>
        </div>
    </form>
</main>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
/* ================================================================
   CONSTANTES
================================================================ */
const estadoInicialActivo = <%= p.isEstado() %>;

// Nombre: letras (con tildes), números y espacios — sin caracteres especiales
const NOMBRE_REGEX = /^[a-zA-Z0-9\sáéíóúÁÉÍÓÚñÑüÜ]{3,100}$/;
// Teléfono: solo dígitos, 7-15 caracteres
const TEL_REGEX    = /^[0-9]{7,15}$/;
// Email básico
const EMAIL_REGEX  = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

/* ================================================================
   HELPERS
================================================================ */
function showErr(id, msg) {
    const el = document.getElementById(id);
    if (!el) return;
    if (msg) {
        const span = el.querySelector('span') || el;
        const txtId = 'txt-' + id.replace('err-', '');
        const txt = document.getElementById(txtId);
        if (txt) txt.textContent = msg;
    }
    el.classList.add('visible');
}
function hideErr(id) {
    const el = document.getElementById(id);
    if (el) el.classList.remove('visible');
}
function markInput(el, state) {
    // state: 'error' | 'ok' | 'neutral'
    el.classList.remove('is-invalid', 'is-valid');
    if (state === 'error') el.classList.add('is-invalid');
    if (state === 'ok')    el.classList.add('is-valid');
}

/* ================================================================
   VALIDADORES
================================================================ */

// ── Nombre ──────────────────────────────────────────────────────
function validarNombre() {
    const el = document.getElementById('nombre');
    const v  = el.value.trim();

    if (v.length === 0) {
        markInput(el, 'error');
        showErr('err-nombre', 'El nombre es obligatorio.');
        return false;
    }
    if (v.length < 3) {
        markInput(el, 'error');
        showErr('err-nombre', 'Mínimo 3 caracteres.');
        return false;
    }
    if (!NOMBRE_REGEX.test(v)) {
        markInput(el, 'error');
        showErr('err-nombre', 'Solo se permiten letras, números y espacios (sin caracteres especiales).');
        return false;
    }
    markInput(el, 'ok');
    hideErr('err-nombre');
    return true;
}

// ── Monto mínimo ────────────────────────────────────────────────
function validarMinimo() {
    const el = document.getElementById('minimoCompra');
    const v  = el.value;

    if (v !== '' && (isNaN(parseFloat(v)) || parseFloat(v) < 0)) {
        markInput(el, 'error');
        showErr('err-minimo', 'El monto no puede ser negativo.');
        return false;
    }
    // Bloquear decimales negativos ingresados manualmente
    if (v.startsWith('-')) {
        el.value = '0';
    }
    markInput(el, v === '' ? 'neutral' : 'ok');
    hideErr('err-minimo');
    return true;
}

// ── Teléfonos ───────────────────────────────────────────────────
function validarTelefonos() {
    const inputs = document.querySelectorAll('input[name="telefono"]');
    let ok = true;
    let msg = '';

    inputs.forEach(inp => {
        const v = inp.value.trim();
        let estado = 'ok';

        if (v === '') {
            estado = 'error'; ok = false;
            msg = 'El campo de teléfono no puede estar vacío.';
        } else if (/[^0-9]/.test(v)) {
            estado = 'error'; ok = false;
            msg = 'Solo se permiten dígitos (sin letras, espacios ni símbolos).';
        } else if (v.length < 7) {
            estado = 'error'; ok = false;
            msg = 'El teléfono debe tener al menos 7 dígitos.';
        } else if (v.length > 15) {
            estado = 'error'; ok = false;
            msg = 'El teléfono no puede superar 15 dígitos.';
        }
        markInput(inp, estado);
    });

    if (!ok) showErr('err-telefonos', msg);
    else     hideErr('err-telefonos');
    return ok;
}

// ── Correos ─────────────────────────────────────────────────────
function validarCorreos() {
    const inputs = document.querySelectorAll('input[name="correo"]');
    let ok = true;
    let msg = '';

    inputs.forEach(inp => {
        const v = inp.value.trim();
        let estado = 'ok';

        if (v === '') {
            estado = 'error'; ok = false;
            msg = 'El campo de correo no puede estar vacío.';
        } else if (!v.includes('@')) {
            estado = 'error'; ok = false;
            msg = 'Falta el símbolo @.';
        } else if (v.split('@').length > 2) {
            estado = 'error'; ok = false;
            msg = 'El correo no puede tener más de un @.';
        } else if (!v.includes('.', v.indexOf('@'))) {
            estado = 'error'; ok = false;
            msg = 'Falta el dominio después del @ (ej: .com, .co).';
        } else if (!EMAIL_REGEX.test(v)) {
            estado = 'error'; ok = false;
            msg = 'Formato de correo inválido.';
        }
        markInput(inp, estado);
    });

    if (!ok) showErr('err-correos', msg);
    else     hideErr('err-correos');
    return ok;
}

// ── Materiales ──────────────────────────────────────────────────
function validarMateriales() {
    const checks = document.querySelectorAll('input.material-check');
    if (checks.length === 0) return true;
    const ok = Array.from(checks).some(c => c.checked);
    const el = document.getElementById('err-materiales');
    if (el) el.classList.toggle('visible', !ok);
    return ok;
}

/* ================================================================
   BLOQUEAR CARACTERES NO NUMÉRICOS EN TELÉFONO (en tiempo real)
================================================================ */
document.getElementById('telefonos-container').addEventListener('keypress', function(e) {
    if (e.target.classList.contains('tel-input')) {
        if (!/[0-9]/.test(e.key) && !['Backspace','Delete','Tab','ArrowLeft','ArrowRight'].includes(e.key)) {
            e.preventDefault();
        }
    }
});
document.getElementById('telefonos-container').addEventListener('input', function(e) {
    if (e.target.classList.contains('tel-input')) {
        // Eliminar cualquier carácter no numérico que se haya pegado
        e.target.value = e.target.value.replace(/\D/g, '');
        validarTelefonos();
    }
});

/* ================================================================
   LISTENERS EN TIEMPO REAL
================================================================ */
document.getElementById('nombre').addEventListener('input', validarNombre);
document.getElementById('nombre').addEventListener('blur',  validarNombre);

document.getElementById('minimoCompra').addEventListener('input', validarMinimo);
document.getElementById('minimoCompra').addEventListener('blur',  validarMinimo);
// Impedir tecla '-' en monto
document.getElementById('minimoCompra').addEventListener('keypress', function(e) {
    if (e.key === '-') e.preventDefault();
});

document.getElementById('correos-container').addEventListener('input', function(e) {
    if (e.target.classList.contains('email-input')) validarCorreos();
});
document.getElementById('correos-container').addEventListener('blur', function(e) {
    if (e.target.classList.contains('email-input')) validarCorreos();
}, true);

document.querySelectorAll('.material-check').forEach(c =>
    c.addEventListener('change', validarMateriales)
);

/* ================================================================
   FILAS DINÁMICAS
================================================================ */
function agregarTelefono() {
    const c = document.getElementById('telefonos-container');
    const d = document.createElement('div');
    d.className = 'dynamic-row';
    d.innerHTML = `<input type="tel" name="telefono" class="form-input tel-input"
                          placeholder="Ej: 3001234567" maxlength="15" inputmode="numeric">
                   <button type="button" class="btn-remove-dyn"
                           onclick="quitarFila(this,'telefonos-container')">
                       <i class="fa-solid fa-minus"></i></button>`;
    c.appendChild(d);
    d.querySelector('input').focus();
}

function agregarCorreo() {
    const c = document.getElementById('correos-container');
    const d = document.createElement('div');
    d.className = 'dynamic-row';
    d.innerHTML = `<input type="email" name="correo" class="form-input email-input"
                          placeholder="correo@ejemplo.com">
                   <button type="button" class="btn-remove-dyn"
                           onclick="quitarFila(this,'correos-container')">
                       <i class="fa-solid fa-minus"></i></button>`;
    c.appendChild(d);
    d.querySelector('input').focus();
}

function quitarFila(btn, containerId) {
    const container = document.getElementById(containerId);
    if (container.querySelectorAll('.dynamic-row').length <= 1) {
        Swal.fire({
            icon: 'warning', title: 'Campo requerido',
            text: 'Debe quedar al menos un registro de contacto.',
            timer: 2500, showConfirmButton: false
        });
        return;
    }
    btn.closest('.dynamic-row').remove();
}

/* ================================================================
   SUBMIT
================================================================ */
document.getElementById('formEditar').addEventListener('submit', async function(e) {
    e.preventDefault();

    // Ejecutar todas las validaciones
    const r = [validarNombre(), validarMinimo(), validarTelefonos(), validarCorreos(), validarMateriales()];

    if (r.includes(false)) {
        // Scroll al primer campo en error
        const primerError = document.querySelector('.is-invalid, .materiales-bloque-error.visible');
        if (primerError) primerError.scrollIntoView({ behavior: 'smooth', block: 'center' });
        Swal.fire({
            icon: 'error', title: 'Campos inválidos',
            text: 'Corrige los campos marcados antes de continuar.',
            confirmButtonColor: '#7c3aed'
        });
        return;
    }

    /* ── Detectar cambio de estado ── */
    const cambiandoAInactivo = document.getElementById('radioInactivo').checked && estadoInicialActivo;
    const cambiandoAActivo   = document.getElementById('radioActivo').checked   && !estadoInicialActivo;

    if (cambiandoAInactivo) {
        const conf = await Swal.fire({
            icon: 'warning', title: '¿Desactivar Proveedor?',
            html: `<p>Al marcar como <strong>Inactivo</strong>, este proveedor:</p>
                   <ul style="text-align:left;margin-top:8px;color:#374151;font-size:0.9rem;line-height:1.8">
                     <li>No aparecerá al registrar nuevas compras.</li>
                     <li>Sus productos seguirán activos en el catálogo.</li>
                     <li>Podrás reactivarlo en cualquier momento.</li>
                   </ul>`,
            showCancelButton: true,
            confirmButtonColor: '#dc2626', cancelButtonColor: '#6b7280',
            confirmButtonText: 'Sí, desactivar', cancelButtonText: 'Mantener activo'
        });
        if (!conf.isConfirmed) {
            document.getElementById('radioActivo').checked = true;
            return;
        }
    }

    if (cambiandoAActivo) {
        const conf = await Swal.fire({
            icon: 'info', title: '¿Reactivar Proveedor?',
            text: 'Volverá a aparecer al registrar nuevas compras.',
            showCancelButton: true,
            confirmButtonColor: '#16a34a', cancelButtonColor: '#6b7280',
            confirmButtonText: 'Sí, reactivar', cancelButtonText: 'Cancelar'
        });
        if (!conf.isConfirmed) {
            document.getElementById('radioInactivo').checked = true;
            return;
        }
    }

    /* ── Confirmación final ── */
    const final = await Swal.fire({
        title: '¿Confirmar cambios?',
        text: 'Se actualizará la información del proveedor.',
        icon: 'question', showCancelButton: true,
        confirmButtonColor: '#7c3aed', cancelButtonColor: '#6b7280',
        confirmButtonText: 'Actualizar ahora', cancelButtonText: 'Revisar'
    });

    if (final.isConfirmed) {
        Swal.fire({ title: 'Procesando...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
        this.submit();
    }
});

/*
 * Si el servlet rechazó el formulario con un error, se muestra como alerta de SweetAlert
 * al cargar la página para que el usuario no pase por alto el mensaje.
 * La condición JSP evalúa el error en el servidor; si existe, emite el bloque JS.
 */
<% if (errorServidor != null && !errorServidor.isEmpty()) { %>
window.addEventListener('DOMContentLoaded', function() {
    Swal.fire({
        title: 'No se pudo actualizar',
        text: '<%=errorServidor.replace("'", "\'")%>',
        icon: 'error',
        confirmButtonColor: '#7c3aed'
    });
});
<% } %>
</script>
</body>
</html>
