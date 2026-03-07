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
    if (materiales == null) materiales = java.util.Collections.emptyList();
    String error = (String) request.getAttribute("error");
    List<String> telefonos = p.getTelefonos();
    List<String> correos   = p.getCorreos();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar Proveedor - AAC27</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/forms-global.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/producto.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <style>
        /* ── Espaciado general del formulario ── */
        .form-product-container {
            max-width: 960px;
            margin: 40px auto;
            padding: 0 32px 80px;
        }
        .form-product {
            background: #fff;
            border-radius: 20px;
            padding: 40px 44px;
            box-shadow: 0 8px 32px rgba(124,58,237,0.08);
            border: 1px solid #ede9fe;
            display: flex;
            flex-direction: column;
            gap: 0; /* controlado por secciones */
        }

        /* ── Sección: bloque con fondo y separación generosa ── */
        .form-section {
            background: #faf8ff;
            border: 1px solid #ede9fe;
            border-radius: 14px;
            padding: 28px 30px 24px;
            margin-bottom: 28px;
        }
        .form-section:last-of-type { margin-bottom: 0; }

        /* ── Encabezado de sección ── */
        .section-sep {
            font-size: 0.73rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.09em;
            color: #7c3aed;
            border-bottom: 2px solid #ede9fe;
            padding-bottom: 10px;
            margin-bottom: 22px;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        /* ── Grid de campos dentro de la sección ── */
        .section-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
            gap: 24px 28px;
        }

        /* ── Grupo de campo individual ── */
        .form-product__group {
            display: flex;
            flex-direction: column;
            gap: 8px;
        }

        /* ── Labels ── */
        .form-product__label {
            font-size: 0.78rem;
            font-weight: 600;
            color: #9ca3af;
            text-transform: uppercase;
            letter-spacing: 0.6px;
        }
        /* Quitamos el * automático del CSS global para este form */
        .form-product__label::after { content: '' !important; }

        /* ── Campos de solo lectura ── */
        .input-readonly-field {
            width: 100%;
            padding: 11px 14px;
            border-radius: 10px;
            border: 1.5px solid #e5e7eb;
            background: #f3f4f6;
            color: #6b7280;
            font-size: 0.93rem;
            font-family: inherit;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .readonly-badge {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            font-size: 0.70rem;
            color: #9ca3af;
            background: #f9fafb;
            border: 1px solid #e5e7eb;
            border-radius: 20px;
            padding: 3px 10px;
            margin-top: 4px;
            width: fit-content;
        }

        /* ── Inputs normales ── */
        .form-product__input {
            padding: 11px 14px;
            border-radius: 10px;
            border: 1.5px solid #e2d9f3;
            font-size: 0.93rem;
            transition: all 0.2s;
        }
        .form-product__input:focus {
            border-color: #7c3aed;
            box-shadow: 0 0 0 3px rgba(124,58,237,0.12);
            outline: none;
        }
        .input-wrap { position: relative; }

        /* ── Filas dinámicas (tel/correo) ── */
        .dynamic-row {
            display: flex;
            gap: 8px;
            align-items: center;
            margin-bottom: 10px;
        }
        .dynamic-row .form-product__input { flex: 1; }
        .btn-remove-dyn {
            width: 36px; height: 36px;
            border-radius: 9px; border: none;
            background: #fee2e2; color: #dc2626;
            cursor: pointer; display: flex;
            align-items: center; justify-content: center;
            font-size: 0.85rem; flex-shrink: 0;
            transition: background 0.2s;
        }
        .btn-remove-dyn:hover { background: #fca5a5; }
        .btn-add-dyn {
            display: inline-flex; align-items: center; gap: 6px;
            padding: 7px 16px; border-radius: 9px;
            border: 1.5px dashed #7c3aed; background: transparent;
            color: #7c3aed; font-size: 0.82rem; font-weight: 600;
            cursor: pointer; margin-top: 6px; transition: all 0.2s;
        }
        .btn-add-dyn:hover { background: #f5f3ff; border-style: solid; }

        /* ── Grid de materiales ── */
        .materiales-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
            gap: 10px;
            background: #fff;
            padding: 18px;
            border-radius: 10px;
            border: 1.5px solid #e2d9f3;
        }
        .material-label {
            display: flex; align-items: center; gap: 8px;
            cursor: pointer; font-size: 0.88rem; color: #374151;
            padding: 8px 12px; border-radius: 8px;
            border: 1px solid #e5e7eb; background: #faf8ff;
            transition: all 0.15s;
        }
        .material-label:hover { background: #ede9fe; border-color: #c4b5fd; }
        .material-label:has(input:checked) {
            background: #ede9fe; border-color: #7c3aed;
            color: #5b21b6; font-weight: 600;
        }
        .material-label input[type="checkbox"] {
            accent-color: #7c3aed; width: 15px; height: 15px; flex-shrink: 0;
        }

        /* ── Estado (radios chip) ── */
        .estado-group { display: flex; gap: 14px; flex-wrap: wrap; }
        .estado-option {
            display: flex; align-items: center; gap: 8px;
            cursor: pointer; font-size: 0.9rem; padding: 10px 20px;
            border-radius: 10px; border: 1.5px solid #e5e7eb;
            background: #fff; transition: all 0.2s;
        }
        .estado-option:has(input:checked) {
            border-color: #7c3aed; background: #f5f3ff;
            color: #5b21b6; font-weight: 600;
            box-shadow: 0 0 0 3px rgba(124,58,237,0.10);
        }
        .estado-option input[type="radio"] { accent-color: #7c3aed; }

        /* ── Botones de acción ── */
        .form-product__actions {
            display: flex; gap: 14px; margin-top: 36px;
            padding-top: 28px; border-top: 1.5px solid #ede9fe;
            justify-content: flex-end; flex-wrap: wrap;
        }

        /* ── Bubble error ── */
        .bubble-error {
            display: none; position: absolute;
            top: calc(100% + 6px); left: 4px; right: 4px; z-index: 100;
            align-items: flex-start; gap: 8px;
            background: #fff; border: 1px solid #fecaca;
            border-left: 3px solid #f87171; border-radius: 8px;
            padding: 10px 13px; box-shadow: 0 4px 12px rgba(0,0,0,0.08);
            font-size: 0.81rem; color: #991b1b; line-height: 1.4;
            pointer-events: none;
        }
        .bubble-error.visible { display: flex; }
        .bubble-icon { color: #f87171; font-size: 1rem; flex-shrink: 0; }
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
        <i class="fa-solid fa-pen-to-square" style="color:#7c3aed;margin-right:8px;font-size:1.1rem;"></i>
        Editar Proveedor
    </h2>

    <form id="formEditar" class="form-product" method="post"
          action="<%=request.getContextPath()%>/ProveedorServlet" novalidate>
        <input type="hidden" name="action"      value="actualizar">
        <input type="hidden" name="proveedorId" value="<%= p.getProveedorId() %>">
        <input type="hidden" name="fechaInicio" value="<%= p.getFechaInicio() != null ? p.getFechaInicio() : "" %>">
        <input type="hidden" name="nombre"      value="<%= p.getNombre() != null ? p.getNombre() : "" %>">
        <input type="hidden" name="documento"   value="<%= p.getDocumento() != null ? p.getDocumento() : "" %>">

        <!-- ══ SECCIÓN 1: Datos no editables ══ -->
        <div class="form-section">
            <div class="section-sep">
                <i class="fa-solid fa-lock"></i> Datos no editables
            </div>
            <div class="section-grid">
                <div class="form-product__group">
                    <label class="form-product__label">
                        <i class="fa-solid fa-building" style="color:#7c3aed;margin-right:4px;"></i>
                        Nombre del Proveedor
                    </label>
                    <div class="input-readonly-field">
                        <i class="fa-solid fa-lock" style="color:#d1d5db;font-size:0.8rem;"></i>
                        <%= p.getNombre() != null ? p.getNombre() : "" %>
                    </div>
                    <span class="readonly-badge"><i class="fa-solid fa-circle-info"></i> Campo no editable</span>
                </div>
                <div class="form-product__group">
                    <label class="form-product__label">
                        <i class="fa-solid fa-id-card" style="color:#7c3aed;margin-right:4px;"></i>
                        Documento de Identidad
                    </label>
                    <div class="input-readonly-field">
                        <i class="fa-solid fa-lock" style="color:#d1d5db;font-size:0.8rem;"></i>
                        <%= p.getDocumento() != null ? p.getDocumento() : "" %>
                    </div>
                    <span class="readonly-badge"><i class="fa-solid fa-circle-info"></i> Campo no editable</span>
                </div>
            </div>
        </div>

        <!-- ══ SECCIÓN 2: Datos editables ══ -->
        <div class="form-section">
            <div class="section-sep">
                <i class="fa-solid fa-sliders"></i> Datos editables
            </div>
            <div class="section-grid">
                <div class="form-product__group">
                    <label class="form-product__label">
                        <i class="fa-regular fa-calendar" style="color:#7c3aed;margin-right:4px;"></i>
                        Fecha de Inicio
                    </label>
                    <div class="input-readonly-field">
                        <i class="fa-solid fa-lock" style="color:#d1d5db;font-size:0.8rem;"></i>
                        <%= p.getFechaInicio() != null ? p.getFechaInicio() : "—" %>
                    </div>
                    <span class="readonly-badge"><i class="fa-solid fa-circle-info"></i> No editable (RF08)</span>
                </div>
                <div class="form-product__group">
                    <label class="form-product__label" for="minimoCompra">
                        <i class="fa-solid fa-dollar-sign" style="color:#7c3aed;margin-right:4px;"></i>
                        Monto Mínimo de Compra
                    </label>
                    <div class="input-wrap">
                        <input id="minimoCompra" type="number" name="minimoCompra"
                               class="form-product__input"
                               min="0" step="0.01" placeholder="0.00"
                               value="<%= p.getMinimoCompra() != null ? p.getMinimoCompra() : "" %>">
                        <div class="bubble-error">
                            <span class="bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                            <span>El monto debe ser un número positivo.</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- ══ SECCIÓN 3: Contacto ══ -->
        <div class="form-section">
            <div class="section-sep">
                <i class="fa-solid fa-address-book"></i> Información de Contacto
            </div>
            <div class="section-grid">
                <div class="form-product__group">
                    <label class="form-product__label">
                        <i class="fa-solid fa-phone" style="color:#7c3aed;margin-right:4px;"></i>
                        Teléfonos *
                    </label>
                    <div id="telefonos-container">
                        <%
                        if (telefonos != null && !telefonos.isEmpty()) {
                            for (String tel : telefonos) {
                        %>
                        <div class="dynamic-row">
                            <input type="tel" name="telefono" class="form-product__input"
                                   placeholder="Ej: 3001234567" value="<%= tel %>">
                            <button type="button" class="btn-remove-dyn" onclick="quitarFila(this)" title="Eliminar">
                                <i class="fa-solid fa-minus"></i>
                            </button>
                        </div>
                        <% } } else { %>
                        <div class="dynamic-row">
                            <input type="tel" name="telefono" class="form-product__input" placeholder="Ej: 3001234567">
                        </div>
                        <% } %>
                    </div>
                    <button type="button" class="btn-add-dyn" onclick="agregarTelefono()">
                        <i class="fa-solid fa-plus"></i> Agregar teléfono
                    </button>
                </div>
                <div class="form-product__group">
                    <label class="form-product__label">
                        <i class="fa-solid fa-envelope" style="color:#7c3aed;margin-right:4px;"></i>
                        Correos Electrónicos *
                    </label>
                    <div id="correos-container">
                        <%
                        if (correos != null && !correos.isEmpty()) {
                            for (String correo : correos) {
                        %>
                        <div class="dynamic-row">
                            <input type="email" name="correo" class="form-product__input"
                                   placeholder="correo@ejemplo.com" value="<%= correo %>">
                            <button type="button" class="btn-remove-dyn" onclick="quitarFila(this)" title="Eliminar">
                                <i class="fa-solid fa-minus"></i>
                            </button>
                        </div>
                        <% } } else { %>
                        <div class="dynamic-row">
                            <input type="email" name="correo" class="form-product__input" placeholder="correo@ejemplo.com">
                        </div>
                        <% } %>
                    </div>
                    <button type="button" class="btn-add-dyn" onclick="agregarCorreo()">
                        <i class="fa-solid fa-plus"></i> Agregar correo
                    </button>
                </div>
            </div>
        </div>

        <!-- ══ SECCIÓN 4: Materiales ══ -->
        <div class="form-section">
            <div class="section-sep">
                <i class="fa-solid fa-cubes"></i> Materiales que Suministra
            </div>
            <div class="materiales-grid">
                <%
                for (Material m : materiales) {
                    boolean sel = false;
                    if (p.getMateriales() != null) {
                        for (Material pm : p.getMateriales()) {
                            if (pm.getMaterialId().equals(m.getMaterialId())) { sel = true; break; }
                        }
                    }
                %>
                <label class="material-label">
                    <input type="checkbox" name="materiales"
                           value="<%= m.getMaterialId() %>" <%= sel ? "checked" : "" %>>
                    <%= m.getNombre() %>
                </label>
                <% } %>
                <% if (materiales.isEmpty()) { %>
                <p style="color:#9ca3af;font-size:0.85rem;grid-column:1/-1;">No hay materiales registrados.</p>
                <% } %>
            </div>
        </div>

        <!-- ══ SECCIÓN 5: Estado ══ -->
        <div class="form-section">
            <div class="section-sep">
                <i class="fa-solid fa-toggle-on"></i> Estado del Proveedor
            </div>
            <div class="estado-group">
                <label class="estado-option">
                    <input type="radio" name="estado" value="activo" <%= p.isEstado() ? "checked" : "" %>>
                    <i class="fa-solid fa-circle-check" style="color:#16a34a;"></i> Activo
                </label>
                <label class="estado-option">
                    <input type="radio" name="estado" value="inactivo" <%= !p.isEstado() ? "checked" : "" %>>
                    <i class="fa-solid fa-circle-xmark" style="color:#dc2626;"></i> Inactivo
                </label>
            </div>
        </div>

        <!-- Botones -->
        <div class="form-product__actions">
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
// Error del servidor
<% if (error != null && !error.isEmpty()) { %>
document.addEventListener('DOMContentLoaded', () => {
    Swal.fire({ icon: 'error', title: 'Error al actualizar',
                text: '<%= error.replace("'", "\\'") %>' });
});
<% } %>

function agregarTelefono() {
    const c = document.getElementById('telefonos-container');
    const d = document.createElement('div');
    d.className = 'dynamic-row';
    d.innerHTML = `<input type="tel" name="telefono" class="form-product__input" placeholder="Ej: 3001234567">
                   <button type="button" class="btn-remove-dyn" onclick="quitarFila(this)" title="Eliminar">
                       <i class="fa-solid fa-minus"></i></button>`;
    c.appendChild(d);
    d.querySelector('input').focus();
}

function agregarCorreo() {
    const c = document.getElementById('correos-container');
    const d = document.createElement('div');
    d.className = 'dynamic-row';
    d.innerHTML = `<input type="email" name="correo" class="form-product__input" placeholder="correo@ejemplo.com">
                   <button type="button" class="btn-remove-dyn" onclick="quitarFila(this)" title="Eliminar">
                       <i class="fa-solid fa-minus"></i></button>`;
    c.appendChild(d);
    d.querySelector('input').focus();
}

function quitarFila(btn) {
    const fila = btn.closest('.dynamic-row');
    const container = fila.parentElement;
    if (container.querySelectorAll('.dynamic-row').length <= 1) {
        Swal.fire({ icon: 'warning', title: 'Requerido',
                    text: 'Debe quedar al menos un teléfono o correo.',
                    timer: 2200, showConfirmButton: false });
        return;
    }
    fila.remove();
}

document.getElementById('formEditar').addEventListener('submit', function(e) {
    e.preventDefault();

    const minimo = document.getElementById('minimoCompra');
    if (minimo.value !== '' && parseFloat(minimo.value) < 0) {
        minimo.classList.add('invalid');
        minimo.closest('.input-wrap').querySelector('.bubble-error').classList.add('visible');
        Swal.fire({ icon: 'error', title: 'Monto inválido',
                    text: 'El mínimo de compra no puede ser negativo.' });
        return;
    }
    minimo.classList.remove('invalid');
    const bErr = minimo.closest('.input-wrap').querySelector('.bubble-error');
    if (bErr) bErr.classList.remove('visible');

    const vacioTel  = [...document.querySelectorAll('input[name="telefono"]')].some(t => t.value.trim() === '');
    const vacioCor  = [...document.querySelectorAll('input[name="correo"]')].some(c => c.value.trim() === '');
    if (vacioTel || vacioCor) {
        Swal.fire({ icon: 'warning', title: 'Campos incompletos',
                    text: 'Asegúrate de llenar todos los teléfonos y correos.' });
        return;
    }

    const form = this;
    Swal.fire({
        title: '¿Guardar cambios?',
        text: 'Se actualizará la información del proveedor.',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#7c3aed',
        cancelButtonColor: '#6b7280',
        confirmButtonText: '<i class="fa-solid fa-floppy-disk"></i> Sí, actualizar',
        cancelButtonText: 'Revisar'
    }).then(result => {
        if (result.isConfirmed) {
            Swal.fire({ title: 'Guardando...', allowOutsideClick: false,
                        didOpen: () => Swal.showLoading() });
            form.submit();
        }
    });
});
</script>
</body>
</html>
