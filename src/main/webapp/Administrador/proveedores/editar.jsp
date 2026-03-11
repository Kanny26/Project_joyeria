<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="model.Proveedor, model.Material, java.util.List"%>
<%
    Object admin = session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }

    Proveedor p = (Proveedor) request.getAttribute("proveedor");
    if (p == null) {
        response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
        return;
    }

    List<Material> materiales = (List<Material>) request.getAttribute("materiales");
    if (materiales == null) materiales = new java.util.ArrayList<>();

    String errorServidor = (String) request.getAttribute("error");

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
    <style>
        .form-product-container { max-width: 960px; margin: 40px auto; padding: 0 32px 80px; }
        .form-product {
            background: #fff; border-radius: 20px; padding: 40px 44px;
            box-shadow: 0 8px 32px rgba(124,58,237,0.08); border: 1px solid #ede9fe;
            display: flex; flex-direction: column;
        }
        .form-section {
            background: #faf8ff; border: 1px solid #ede9fe;
            border-radius: 14px; padding: 28px 30px 24px; margin-bottom: 28px;
        }
        .section-sep {
            font-size: 0.73rem; font-weight: 700; text-transform: uppercase;
            letter-spacing: 0.09em; color: #7c3aed; border-bottom: 2px solid #ede9fe;
            padding-bottom: 10px; margin-bottom: 22px; display: flex; align-items: center; gap: 8px;
        }
        .section-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 24px 28px; }
        .form-group   { display: flex; flex-direction: column; gap: 6px; }
        .form-label   {
            font-size: 0.78rem; font-weight: 700; color: #7c3aed;
            text-transform: uppercase; letter-spacing: 0.6px;
            display: flex; align-items: center; gap: 6px;
        }

        /* ── Campo editable ── */
        .form-input {
            padding: 11px 14px; border-radius: 10px; border: 1.5px solid #e2d9f3;
            font-size: 0.93rem; transition: all 0.2s; width: 100%;
            box-sizing: border-box; font-family: inherit; color: #1e1b4b;
        }
        .form-input:focus  { border-color: #7c3aed; box-shadow: 0 0 0 3px rgba(124,58,237,0.12); outline: none; }
        .form-input.is-invalid { border-color: #ef4444 !important; background: #fef2f2; }
        .form-input.is-valid   { border-color: #22c55e !important; background: #f0fdf4; }

        /* ── Campo readonly ── */
        .input-readonly {
            padding: 11px 14px; border-radius: 10px; border: 1.5px solid #e5e7eb;
            background: #f3f4f6; color: #6b7280; font-size: 0.93rem;
            display: flex; align-items: center; gap: 8px;
            width: 100%; box-sizing: border-box;
        }
        .readonly-badge {
            display: inline-flex; align-items: center; gap: 4px; font-size: 0.70rem;
            color: #9ca3af; background: #f9fafb; border: 1px solid #e5e7eb;
            border-radius: 20px; padding: 3px 10px; margin-top: 2px; width: fit-content;
        }

        /* ── Mensajes de error / éxito ── */
        .field-msg {
            font-size: 0.72rem; display: none;
            align-items: center; gap: 4px; margin-top: 2px;
        }
        .field-msg.error   { color: #dc2626; }
        .field-msg.success { color: #16a34a; }
        .field-msg.visible { display: flex; }

        /* ── Filas dinámicas ── */
        .dynamic-row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; }
        .btn-remove-dyn {
            width: 36px; height: 36px; border-radius: 9px; border: none;
            background: #fee2e2; color: #dc2626; cursor: pointer; flex-shrink: 0;
            display: flex; align-items: center; justify-content: center; transition: background 0.2s;
        }
        .btn-remove-dyn:hover { background: #fca5a5; }
        .btn-add-dyn {
            display: inline-flex; align-items: center; gap: 6px; padding: 7px 16px;
            border-radius: 9px; border: 1.5px dashed #7c3aed; background: transparent;
            color: #7c3aed; font-size: 0.82rem; font-weight: 600;
            cursor: pointer; margin-top: 6px; transition: all 0.2s; font-family: inherit;
        }
        .btn-add-dyn:hover { background: #f5f3ff; border-style: solid; }

        /* ── Materiales ── */
        .materiales-grid {
            display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
            gap: 10px; background: #fff; padding: 18px;
            border-radius: 10px; border: 1.5px solid #e2d9f3;
        }
        .material-label {
            display: flex; align-items: center; gap: 8px; cursor: pointer;
            font-size: 0.88rem; color: #374151; padding: 8px 12px;
            border-radius: 8px; border: 1px solid #e5e7eb; background: #faf8ff;
            transition: all 0.15s; user-select: none;
        }
        .material-label:hover { background: #ede9fe; border-color: #c4b5fd; }
        .material-label input[type="checkbox"] { accent-color: #7c3aed; }
        .materiales-bloque-error {
            display: none; align-items: center; gap: 6px; font-size: 0.78rem;
            color: #dc2626; margin-top: 10px; padding: 8px 12px;
            background: #fef2f2; border-radius: 8px; border: 1px solid #fecaca;
        }
        .materiales-bloque-error.visible { display: flex; }

        /* ── Estado ── */
        .estado-group { display: flex; gap: 14px; flex-wrap: wrap; }
        .estado-option {
            display: flex; align-items: center; gap: 8px; cursor: pointer;
            font-size: 0.9rem; padding: 10px 20px; border-radius: 10px;
            border: 1.5px solid #e5e7eb; background: #fff; transition: all 0.2s;
        }
        .estado-option:has(input:checked) { border-color: #7c3aed; background: #f5f3ff; color: #5b21b6; font-weight: 600; }
        .estado-hint { font-size: 0.78rem; color: #6b7280; margin-top: 10px; display: flex; align-items: flex-start; gap: 6px; }

        /* ── Acciones ── */
        .form-actions {
            display: flex; gap: 14px; margin-top: 36px; padding-top: 28px;
            border-top: 1.5px solid #ede9fe; justify-content: flex-end;
        }
        .btn-guardar {
            display: inline-flex; align-items: center; gap: 8px; padding: 12px 28px;
            border-radius: 12px; border: none;
            background: linear-gradient(135deg, #7c3aed, #a78bfa);
            color: #fff; font-weight: 700; font-size: 0.95rem;
            cursor: pointer; font-family: inherit;
            box-shadow: 0 4px 14px rgba(124,58,237,0.3); transition: transform 0.15s, box-shadow 0.15s;
        }
        .btn-guardar:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(124,58,237,0.4); }
        .btn-cancelar {
            display: inline-flex; align-items: center; gap: 8px; padding: 12px 24px;
            border-radius: 12px; border: none; background: #f3f4f6; color: #374151;
            font-weight: 700; font-size: 0.95rem; cursor: pointer; font-family: inherit;
            transition: background 0.15s;
        }
        .btn-cancelar:hover { background: #e5e7eb; }

        /* ── Error servidor ── */
        .server-error {
            background: #fef2f2; border: 1px solid #fecaca; border-left: 4px solid #ef4444;
            border-radius: 10px; padding: 14px 18px; margin-bottom: 24px;
            color: #991b1b; font-size: 0.9rem; display: flex; align-items: center; gap: 10px;
        }
    </style>
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
</script>
</body>
</html>
