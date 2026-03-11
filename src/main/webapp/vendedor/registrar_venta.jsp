<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Usuario" %>
<%
    Object vendedorSesion = session.getAttribute("vendedor");
    if (vendedorSesion == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }
    Usuario usuario = (Usuario) vendedorSesion;
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registrar Venta | AAC27</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/forms-global.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Vendedor/registrar_venta.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <style>
        .char-counter { font-size:.75rem; color:#94a3b8; text-align:right; margin-top:2px; transition:color .2s; }
        .char-counter.warn  { color:#f59e0b; }
        .char-counter.limit { color:#ef4444; font-weight:700; }
        input.input-error, select.input-error { border-color:#ef4444 !important; background:#fff5f5; }
        input.input-ok,    select.input-ok    { border-color:#22c55e !important; }
        .field-msg { font-size:.75rem; margin-top:3px; min-height:16px; }
        .field-msg.err { color:#ef4444; }
        .field-msg.ok  { color:#16a34a; }
    </style>
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/Seller.png" alt="Vendedor">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%=request.getContextPath()%>/vendedor/vendedor_principal.jsp" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<main class="prov-page">
    <h1 class="prov-page__titulo"><i class="fa-solid fa-cart-plus"></i> Registrar Nueva Venta</h1>

    <% if (error != null && !error.isEmpty()) { %>
        <div class="prov-alert prov-alert--error">
            <i class="fa-solid fa-circle-exclamation"></i> <%= error %>
        </div>
    <% } %>

    <form action="<%= request.getContextPath() %>/VentaVendedorServlet" method="post"
          id="formVenta" novalidate>
        <input type="hidden" name="action" value="guardarVenta">

        <%-- ══ DATOS DEL CLIENTE ══ --%>
        <div class="form-card" style="margin-bottom:1.5rem;">
            <div class="form-card__title"><i class="fa-solid fa-user"></i> Datos del Cliente</div>
            <div class="form-row">

                <div class="form-group">
                    <label><i class="fa-solid fa-user-pen"></i> Nombre del cliente *</label>
                    <input type="text" id="clienteNombre" name="clienteNombre"
                           placeholder="Solo letras y espacios" maxlength="100"
                           oninput="validarNombre(this); contarChars(this,'cntNombre',100)"
                           onblur="validarNombre(this)">
                    <div class="char-counter" id="cntNombre">0 / 100</div>
                    <div class="field-msg" id="msgNombre"></div>
                </div>

                <div class="form-group">
                    <label><i class="fa-solid fa-phone"></i> Teléfono</label>
                    <input type="tel" id="clienteTelefono" name="clienteTelefono"
                           placeholder="Ej: 3001234567" maxlength="20"
                           oninput="limpiarTelefono(this); contarChars(this,'cntTel',20)"
                           onblur="validarTelefono(this)">
                    <div class="char-counter" id="cntTel">0 / 20</div>
                    <div class="field-msg" id="msgTelefono"></div>
                </div>

                <div class="form-group">
                    <label><i class="fa-solid fa-envelope"></i> Email</label>
                    <input type="email" id="clienteEmail" name="clienteEmail"
                           placeholder="correo@ejemplo.com" maxlength="100"
                           oninput="contarChars(this,'cntEmail',100)"
                           onblur="validarEmail(this)">
                    <div class="char-counter" id="cntEmail">0 / 100</div>
                    <div class="field-msg" id="msgEmail"></div>
                </div>

            </div>
        </div>

        <%-- ══ DATOS DE LA VENTA ══ --%>
        <div class="form-card" style="margin-bottom:1.5rem;">
            <div class="form-card__title"><i class="fa-solid fa-receipt"></i> Datos de la Venta</div>

            <div class="form-row">

                <div class="form-group">
                    <label><i class="fa-solid fa-calendar-day"></i> Fecha de venta *</label>
                    <input type="date" id="fechaVenta" name="fechaVenta"
                           onchange="validarFechaVenta(this)" onblur="validarFechaVenta(this)">
                    <div class="field-msg" id="msgFechaVenta"></div>
                </div>

                <div class="form-group">
                    <label><i class="fa-solid fa-wallet"></i> Método de pago *</label>
                    <select name="metodoPago" id="metodoPagoId"
                            onchange="validarSelect(this,'msgMetodo','Selecciona un método de pago.')">
                        <option value="">-- Selecciona un método --</option>
                        <%
                            java.util.List<model.MetodoPago> metodosPagoList =
                                (java.util.List<model.MetodoPago>) request.getAttribute("metodosPago");
                            if (metodosPagoList != null) {
                                for (model.MetodoPago mp : metodosPagoList) {
                        %>
                                <option value="<%= mp.getMetodoPagoId() %>"><%= mp.getNombre() %></option>
                        <%      }
                            }
                        %>
                    </select>
                    <div class="field-msg" id="msgMetodo"></div>
                </div>

                <div class="form-group">
                    <label><i class="fa-solid fa-hand-holding-dollar"></i> Tipo de pago</label>
                    <select name="tipoPago" id="tipoPago" onchange="toggleCredito(this.value)">
                        <option value="CONTADO">Contado</option>
                        <option value="CREDITO">Crédito</option>
                    </select>
                </div>

            </div>

            <%-- Sección crédito --%>
            <div id="seccionCredito" style="display:none;">
                <div class="form-row">

                    <div class="form-group">
                        <label><i class="fa-regular fa-calendar-xmark"></i> Fecha límite de pago *</label>
                        <input type="date" id="fechaVencimiento" name="fechaVencimiento"
                               onchange="validarFechaVencimiento(this)"
                               onblur="validarFechaVencimiento(this)">
                        <div class="field-msg" id="msgFechaVenc"></div>
                    </div>

                    <div class="form-group">
                        <label><i class="fa-solid fa-money-bill-wave"></i> Anticipo (opcional)</label>
                        <input type="number" id="anticipo" name="anticipo"
                               min="0" step="0.01" placeholder="0.00"
                               oninput="sanitizarNumero(this); validarAnticipo(this)"
                               onblur="validarAnticipo(this)">
                        <div class="field-msg" id="msgAnticipo"></div>
                    </div>

                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label><i class="fa-solid fa-circle-check"></i> Estado del crédito</label>
                        <select name="estadoCredito">
                            <option value="activo">Activo (pendiente de pago)</option>
                            <option value="pagado">Pagado (ya saldado)</option>
                        </select>
                    </div>
                </div>
            </div>

        </div>

        <%-- ══ CARRITO ══ --%>
        <div class="form-card" style="margin-bottom:1.5rem;">
            <div class="form-card__title"
                 style="display:flex;justify-content:space-between;align-items:center;">
                <span><i class="fa-solid fa-boxes-stacked"></i> Productos</span>
                <button type="button" class="btn-save" onclick="abrirModal()"
                        style="padding:.45rem 1rem;font-size:.82rem;">
                    <i class="fa-solid fa-plus"></i> Agregar producto
                </button>
            </div>
            <div id="carritoVacio" style="color:#94a3b8;font-size:.9rem;padding:.5rem 0;">
                No has agregado productos. Haz clic en "Agregar producto".
            </div>
            <div id="carritoLista"></div>
            <div id="msgCarrito" class="field-msg err" style="display:none;">
                Debes agregar al menos un producto.
            </div>
            <div style="margin-top:1rem;padding:.75rem 1rem;background:#f0fdf4;border-radius:10px;
                        display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:700;color:#166534;">Total</span>
                <span id="totalDisplay">$0.00</span>
            </div>
        </div>

        <div id="inputsCarrito"></div>

        <div class="form-actions">
            <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=misVentas"
               class="btn-cancel">
                <i class="fa-solid fa-xmark"></i> Cancelar
            </a>
            <button type="button" class="btn-save" onclick="submitFormulario()">
                <i class="fa-solid fa-floppy-disk"></i> Guardar Venta
            </button>
        </div>

    </form>
</main>

<%-- ══ MODAL selección de productos ══ --%>
<div class="modal-overlay" id="modalSeleccion">
    <div class="modal-box">
        <div style="display:flex;justify-content:space-between;align-items:center;
                    margin-bottom:1rem;border-bottom:1px solid #e2e8f0;padding-bottom:10px;">
            <h3 style="font-weight:800;color:#1e1b4b;font-size:1.1rem;margin:0;">
                <i class="fa-solid fa-box-open"></i> Seleccionar Producto
            </h3>
            <button type="button" onclick="cerrarModal()"
                    style="background:none;border:none;font-size:1.3rem;cursor:pointer;color:#64748b;">
                <i class="fa-solid fa-xmark"></i>
            </button>
        </div>

        <div class="modal-body">
            <div class="modal-breadcrumb" id="modalBreadcrumb"
                 style="display:none;margin-bottom:1rem;">
                <span class="modal-breadcrumb__item" onclick="mostrarCategorias()"
                      style="cursor:pointer;color:#7c3aed;">
                    <i class="fa-solid fa-layer-group"></i> Categorías
                </span>
                <span class="modal-breadcrumb__separator"> / </span>
                <span class="modal-breadcrumb__item active">
                    <i class="fa-solid fa-tag"></i>
                    <span id="nombreCategoriaSeleccionada"></span>
                </span>
            </div>

            <button class="btn-volver-categorias" id="btnVolverCategoriasVisible"
                    onclick="mostrarCategorias()"
                    style="display:none;margin-bottom:1rem;">
                <i class="fa-solid fa-arrow-left"></i> Volver a categorías
            </button>

            <div id="modalContent"></div>

            <div id="addQtySection"
                 style="display:none;margin-top:1rem;padding:1rem;background:#f5f3ff;
                        border-radius:12px;border:1px solid #c4b5fd;">
                <div style="font-weight:700;color:#1e1b4b;margin-bottom:4px;" id="prodSelNombre"></div>
                <div style="font-size:.82rem;color:#64748b;margin-bottom:.5rem;" id="prodSelInfo"></div>
                <div id="editCarritoInfo"
                     style="display:none;font-size:.8rem;color:#7c3aed;margin-bottom:.5rem;font-weight:600;">
                    <i class="fa-solid fa-pen"></i>
                    Ya tienes <span id="cantActualEnCarrito">0</span> en el carrito.
                    Cambia la cantidad total aquí.
                </div>
                <div style="display:flex;gap:1rem;align-items:flex-end;flex-wrap:wrap;">
                    <div>
                        <label style="font-size:.82rem;font-weight:600;display:block;margin-bottom:4px;">
                            Cantidad *
                        </label>
                        <input type="number" id="cantidadInput" min="1" value="1"
                               style="width:90px;padding:.4rem .6rem;
                                      border:1.5px solid #c4b5fd;border-radius:8px;font-weight:700;"
                               oninput="validarCantidadModal(this)">
                        <div class="field-msg" id="msgCantModal" style="max-width:220px;"></div>
                    </div>
                    <button type="button" class="btn-save" onclick="confirmarAgregarAlCarrito()"
                            style="padding:.45rem 1.2rem;font-size:.85rem;">
                        <i class="fa-solid fa-cart-plus"></i> Agregar
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script type="text/javascript">
/* ═══════════════════════════════════════════
   ESTADO GLOBAL
═══════════════════════════════════════════ */
const AppVenta = {
    carrito: {},
    productoSeleccionado: null,
    ctx: '<%= request.getContextPath() %>'
};
const MINIMO_CREDITO = 250000;

/* ═══════════════════════════════════════════
   INIT
═══════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', function () {
    const hoy = hoyISO();
    const fv  = document.getElementById('fechaVenta');
    if (fv) { fv.value = hoy; fv.max = hoy; }   // max = hoy → bloquea fechas futuras
    const fl = document.getElementById('fechaVencimiento');
    if (fl) fl.min = mananaISO();                 // min = mañana → bloquea fechas pasadas
});

/* ── Utilidades fecha ── */
function hoyISO() {
    return new Date().toISOString().split('T')[0];
}
function mananaISO() {
    const d = new Date();
    d.setDate(d.getDate() + 1);
    return d.toISOString().split('T')[0];
}

/* ═══════════════════════════════════════════
   HELPERS DE UI
═══════════════════════════════════════════ */
function contarChars(input, counterId, max) {
    const len = input.value.length;
    const el  = document.getElementById(counterId);
    if (!el) return;
    el.textContent = len + ' / ' + max;
    el.className   = 'char-counter' +
        (len >= max ? ' limit' : len >= max * 0.85 ? ' warn' : '');
}

function setFieldMsg(id, texto, tipo) {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = texto;
    el.className   = 'field-msg ' + (tipo || '');
}

function setInputState(input, estado) {
    input.classList.remove('input-error', 'input-ok');
    if (estado === 'error') input.classList.add('input-error');
    if (estado === 'ok')    input.classList.add('input-ok');
}

/* ═══════════════════════════════════════════
   VALIDACIONES
═══════════════════════════════════════════ */

/* Nombre: solo letras, espacios, guion, punto, apóstrofe. Mín 3 chars. */
function validarNombre(input) {
    const val   = input.value.trim();
    const regex = /^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\s.\-']+$/;
    if (!val) {
        setInputState(input, 'error');
        setFieldMsg('msgNombre', 'El nombre es obligatorio.', 'err');
        return false;
    }
    if (val.length < 3) {
        setInputState(input, 'error');
        setFieldMsg('msgNombre', 'Mínimo 3 caracteres.', 'err');
        return false;
    }
    if (!regex.test(val)) {
        setInputState(input, 'error');
        setFieldMsg('msgNombre', 'Solo letras y espacios (sin números ni símbolos especiales).', 'err');
        return false;
    }
    setInputState(input, 'ok');
    setFieldMsg('msgNombre', '', '');
    return true;
}

/* Teléfono: elimina automáticamente letras; 7–15 dígitos. Opcional. */
function limpiarTelefono(input) {
    const limpio = input.value.replace(/[^\d+\-\s()]/g, '');
    if (input.value !== limpio) input.value = limpio;
    contarChars(input, 'cntTel', 20);
}
function validarTelefono(input) {
    const val = input.value.trim();
    if (!val) { setInputState(input,''); setFieldMsg('msgTelefono','',''); return true; }
    const digits = val.replace(/\D/g,'');
    if (digits.length < 7)  { setInputState(input,'error'); setFieldMsg('msgTelefono','Mínimo 7 dígitos.','err'); return false; }
    if (digits.length > 15) { setInputState(input,'error'); setFieldMsg('msgTelefono','Máximo 15 dígitos.','err'); return false; }
    setInputState(input,'ok'); setFieldMsg('msgTelefono','',''); return true;
}

/* Email: formato básico. Opcional. */
function validarEmail(input) {
    const val = input.value.trim();
    if (!val) { setInputState(input,''); setFieldMsg('msgEmail','',''); return true; }
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
    if (!re.test(val)) { setInputState(input,'error'); setFieldMsg('msgEmail','Formato de email no válido.','err'); return false; }
    setInputState(input,'ok'); setFieldMsg('msgEmail','',''); return true;
}

/* Fecha venta: no futura. Autocorrige si lo intentan mediante JS. */
function validarFechaVenta(input) {
    const val = input.value;
    if (!val) { setInputState(input,'error'); setFieldMsg('msgFechaVenta','La fecha es obligatoria.','err'); return false; }
    if (val > hoyISO()) {
        input.value = hoyISO();
        setInputState(input,'error');
        setFieldMsg('msgFechaVenta','La fecha de venta no puede ser futura. Se corrigió a hoy.','err');
        return false;
    }
    setInputState(input,'ok'); setFieldMsg('msgFechaVenta','',''); return true;
}

/* Fecha límite crédito: debe ser > hoy. */
function validarFechaVencimiento(input) {
    if (document.getElementById('seccionCredito').style.display === 'none') return true;
    const val = input.value;
    if (!val) { setInputState(input,'error'); setFieldMsg('msgFechaVenc','La fecha límite es obligatoria.','err'); return false; }
    if (val <= hoyISO()) { setInputState(input,'error'); setFieldMsg('msgFechaVenc','Debe ser posterior a hoy.','err'); return false; }
    setInputState(input,'ok'); setFieldMsg('msgFechaVenc','',''); return true;
}

/* Sanitiza campos numéricos: bloquea negativos al escribir */
function sanitizarNumero(input) {
    if (parseFloat(input.value) < 0) input.value = '';
}

/* Anticipo: positivo y menor al total. */
function validarAnticipo(input) {
    if (document.getElementById('seccionCredito').style.display === 'none') return true;
    const raw = input.value;
    if (raw === '' || raw === null) { setInputState(input,''); setFieldMsg('msgAnticipo','',''); return true; }
    const val = parseFloat(raw);
    if (isNaN(val) || val < 0) {
        input.value = '';
        setInputState(input,'error'); setFieldMsg('msgAnticipo','El anticipo no puede ser negativo.','err'); return false;
    }
    const total = getTotalCarrito();
    if (total > 0 && val >= total) {
        setInputState(input,'error');
        setFieldMsg('msgAnticipo','Debe ser menor al total ($' + total.toLocaleString('es-CO') + ').','err'); return false;
    }
    setInputState(input,'ok'); setFieldMsg('msgAnticipo','',''); return true;
}

/* Select genérico */
function validarSelect(sel, msgId, msgVacio) {
    if (!sel.value) { setInputState(sel,'error'); setFieldMsg(msgId, msgVacio,'err'); return false; }
    setInputState(sel,'ok'); setFieldMsg(msgId,'',''); return true;
}

/* Cantidad modal: entre 1 y stock del producto */
function validarCantidadModal(input) {
    const p = AppVenta.productoSeleccionado;
    if (!p) return;
    let val = parseInt(input.value);
    if (isNaN(val) || val < 1) { input.value = 1; setFieldMsg('msgCantModal','Mínimo 1 unidad.','err'); return; }
    if (val > p.stock)         { input.value = p.stock; setFieldMsg('msgCantModal','Máximo: ' + p.stock + ' unidades disponibles.','err'); return; }
    setFieldMsg('msgCantModal','','');
}

/* ═══════════════════════════════════════════
   SUBMIT FINAL CON VALIDACIÓN COMPLETA
═══════════════════════════════════════════ */
function submitFormulario() {
    let ok = true;
    const errores = [];

    if (!validarNombre(document.getElementById('clienteNombre')))    { ok=false; errores.push('Nombre del cliente inválido.'); }
    if (!validarTelefono(document.getElementById('clienteTelefono'))) { ok=false; errores.push('Teléfono inválido.'); }
    if (!validarEmail(document.getElementById('clienteEmail')))       { ok=false; errores.push('Email inválido.'); }
    if (!validarFechaVenta(document.getElementById('fechaVenta')))    { ok=false; errores.push('Fecha de venta inválida.'); }
    if (!validarSelect(document.getElementById('metodoPagoId'), 'msgMetodo', 'Selecciona un método de pago.')) {
        ok=false; errores.push('Método de pago no seleccionado.');
    }

    // Carrito
    if (!Object.keys(AppVenta.carrito).length) {
        ok=false;
        errores.push('Debes agregar al menos un producto.');
        document.getElementById('msgCarrito').style.display = 'block';
    } else {
        document.getElementById('msgCarrito').style.display = 'none';
    }

    // Crédito
    if (document.getElementById('tipoPago').value === 'CREDITO') {
        if (!validarFechaVencimiento(document.getElementById('fechaVencimiento'))) { ok=false; errores.push('Fecha límite de pago inválida.'); }
        if (!validarAnticipo(document.getElementById('anticipo')))                 { ok=false; errores.push('Anticipo inválido.'); }
    }

    if (!ok) {
        Swal.fire({
            title: 'Corrige los siguientes campos',
            html: '<ul style="text-align:left;padding-left:1.2rem;margin:0;">' +
                  errores.map(e => '<li style="margin-bottom:4px;">' + e + '</li>').join('') + '</ul>',
            icon: 'warning',
            confirmButtonText: 'Entendido',
            confirmButtonColor: '#7c3aed'
        });
        const first = document.querySelector('.input-error');
        if (first) first.scrollIntoView({ behavior:'smooth', block:'center' });
        return;
    }

    document.getElementById('formVenta').submit();
}

/* ═══════════════════════════════════════════
   MODAL – CATEGORÍAS
═══════════════════════════════════════════ */
function abrirModal() {
    document.getElementById('modalSeleccion').classList.add('active');
    mostrarCategorias();
}
function cerrarModal() {
    document.getElementById('modalSeleccion').classList.remove('active');
    AppVenta.productoSeleccionado = null;
    document.getElementById('addQtySection').style.display = 'none';
    setFieldMsg('msgCantModal','','');
}
function mostrarCategorias() {
    const content = document.getElementById('modalContent');
    document.getElementById('modalBreadcrumb').style.display         = 'none';
    document.getElementById('btnVolverCategoriasVisible').style.display = 'none';
    document.getElementById('addQtySection').style.display            = 'none';
    content.innerHTML = '<div style="text-align:center;padding:2rem;"><i class="fa-solid fa-circle-notch fa-spin"></i> Cargando...</div>';

    fetch(AppVenta.ctx + '/VentaVendedorServlet?action=obtenerCategorias')
        .then(r => r.json())
        .then(cats => {
            if (!cats.length) { content.innerHTML = '<p style="text-align:center;color:#94a3b8;padding:2rem;">Sin categorías.</p>'; return; }
            let html = '<div class="categorias-grid">';
            cats.forEach(c => {
                const ico = c.icono
                    ? '<img src="'+AppVenta.ctx+'/assets/Imagenes/iconos/'+c.icono+'" alt="'+c.nombre+'">'
                    : '<i class="fa-solid fa-box"></i>';
                html += '<div class="categoria-card" onclick="seleccionarCategoria('+c.id+',\''+c.nombre.replace(/'/g,"\\'")+'\')">'+
                        '<div class="categoria-card__icon">'+ico+'</div>'+
                        '<div class="categoria-card__nombre" style="font-weight:600;font-size:.9rem;">'+c.nombre+'</div></div>';
            });
            html += '</div>';
            content.innerHTML = html;
        })
        .catch(() => { content.innerHTML = '<p style="color:#ef4444;text-align:center;">Error al cargar categorías.</p>'; });
}

/* ── Productos por categoría ── */
function seleccionarCategoria(id, nombre) {
    const content = document.getElementById('modalContent');
    document.getElementById('modalBreadcrumb').style.display            = 'flex';
    document.getElementById('btnVolverCategoriasVisible').style.display = 'inline-flex';
    document.getElementById('nombreCategoriaSeleccionada').textContent  = nombre;
    document.getElementById('addQtySection').style.display              = 'none';
    content.innerHTML = '<div style="text-align:center;padding:2rem;">Cargando productos...</div>';

    fetch(AppVenta.ctx + '/VentaVendedorServlet?action=obtenerProductosPorCategoria&categoriaId=' + id)
        .then(r => r.json())
        .then(prods => {
            if (!prods.length) { content.innerHTML = '<p style="color:#94a3b8;text-align:center;padding:2rem;">Sin productos en esta categoría.</p>'; return; }
            let html = '<div class="productos-grid">';
            prods.forEach(p => {
                // Descontar lo que ya está en carrito para mostrar disponibilidad real
                const enCarrito = AppVenta.carrito[p.id] ? AppVenta.carrito[p.id].cantidad : 0;
                const restante  = p.stock - enCarrito;
                const agotado   = restante <= 0;
                const pJson     = JSON.stringify(p).replace(/'/g,"&#39;");
                const imgHtml   = p.imagen
                    ? '<img src="'+AppVenta.ctx+'/imagen-producto/'+p.id+'" alt="'+p.nombre+'">'
                    : '<i class="fa-solid fa-image"></i>';
                html += '<div class="producto-card '+(agotado?'sin-stock':'')+'" '+
                        (!agotado?'onclick="prepararProducto(this)"':'')+' data-p=\''+pJson+'\'>';
                html += '<span class="producto-card__stock '+(restante < 5?'bajo':'')+'">'+
                        restante+' disp.'+(enCarrito?' <em>('+enCarrito+' en carrito)</em>':'')+
                        '</span>';
                html += '<div class="producto-card__img">'+imgHtml+'</div>';
                html += '<div class="producto-card__nombre" style="font-weight:700;font-size:.85rem;">'+p.nombre+'</div>';
                html += '<div class="producto-card__precio" style="color:#7c3aed;font-weight:800;">$'+Number(p.precioUnitario).toLocaleString('es-CO')+'</div>';
                html += '</div>';
            });
            html += '</div>';
            content.innerHTML = html;
        })
        .catch(() => { content.innerHTML = '<p style="color:#ef4444;text-align:center;">Error al cargar productos.</p>'; });
}

/* ── Preparar selección de un producto ── */
function prepararProducto(elemento) {
    const p         = JSON.parse(elemento.getAttribute('data-p'));
    const enCarrito = AppVenta.carrito[p.id] ? AppVenta.carrito[p.id].cantidad : 0;
    AppVenta.productoSeleccionado = p;

    document.getElementById('prodSelNombre').textContent = p.nombre;
    document.getElementById('prodSelInfo').textContent   =
        'Precio: $' + Number(p.precioUnitario).toLocaleString('es-CO') + ' | Stock total: ' + p.stock;

    const editInfo  = document.getElementById('editCarritoInfo');
    const cantInput = document.getElementById('cantidadInput');
    if (enCarrito > 0) {
        editInfo.style.display = 'block';
        document.getElementById('cantActualEnCarrito').textContent = enCarrito;
        cantInput.value = enCarrito; // pre-carga cantidad actual para editar
    } else {
        editInfo.style.display = 'none';
        cantInput.value = 1;
    }
    cantInput.min = 1;
    cantInput.max = p.stock;
    setFieldMsg('msgCantModal','','');

    document.getElementById('addQtySection').style.display = 'block';
    document.getElementById('addQtySection').scrollIntoView({ behavior:'smooth' });
}

/* ── Confirmar agregar/editar en carrito ── */
function confirmarAgregarAlCarrito() {
    const p    = AppVenta.productoSeleccionado;
    const cant = parseInt(document.getElementById('cantidadInput').value);
    if (!p) return;

    if (isNaN(cant) || cant < 1) {
        setFieldMsg('msgCantModal','La cantidad mínima es 1.','err'); return;
    }
    if (cant > p.stock) {
        setFieldMsg('msgCantModal','Stock insuficiente. Máximo: ' + p.stock + ' unidades.','err');
        document.getElementById('cantidadInput').value = p.stock;
        return;
    }

    // Sobreescribe (permite editar la cantidad)
    AppVenta.carrito[p.id] = { nombre: p.nombre, precio: p.precioUnitario, cantidad: cant, stock: p.stock };
    renderCarrito();
    cerrarModal();
    Swal.fire({ toast:true, position:'top-end', icon:'success',
        title: '"' + p.nombre + '" ' + (cant === 1 ? 'agregado' : 'actualizado') + ' en el carrito.',
        showConfirmButton:false, timer:1800 });
}

/* ═══════════════════════════════════════════
   CARRITO – RENDER
═══════════════════════════════════════════ */
function renderCarrito() {
    const lista     = document.getElementById('carritoLista');
    const inputs    = document.getElementById('inputsCarrito');
    const totalDisp = document.getElementById('totalDisplay');
    const vacio     = document.getElementById('carritoVacio');
    let total = 0;

    lista.innerHTML = inputs.innerHTML = '';
    const ids = Object.keys(AppVenta.carrito);

    if (!ids.length) {
        vacio.style.display   = 'block';
        totalDisp.textContent = '$0.00';
        return;
    }
    vacio.style.display = 'none';

    ids.forEach(id => {
        const item     = AppVenta.carrito[id];
        const subtotal = item.precio * item.cantidad;
        total += subtotal;

        const div = document.createElement('div');
        div.className = 'carrito-item';
        div.style     = 'display:flex;justify-content:space-between;align-items:center;' +
                        'padding:10px;border-bottom:1px solid #f1f5f9;';
        div.innerHTML =
            '<div>' +
              '<strong>' + item.nombre + '</strong><br>' +
              '<small>$' + Number(item.precio).toLocaleString('es-CO') +
              ' × ' + item.cantidad +
              ' = <b>$' + subtotal.toLocaleString('es-CO') + '</b></small>' +
            '</div>' +
            '<div style="display:flex;align-items:center;gap:8px;">' +
              '<button type="button" title="Editar cantidad" onclick="editarItemCarrito('+id+')" '+
              'style="color:#7c3aed;background:none;border:1px solid #c4b5fd;border-radius:6px;'+
              'padding:3px 9px;cursor:pointer;font-size:.8rem;">'+
              '<i class="fa-solid fa-pen"></i></button>' +
              '<button type="button" title="Eliminar" onclick="confirmarEliminar('+id+')" '+
              'style="color:#ef4444;background:none;border:none;cursor:pointer;">'+
              '<i class="fa-solid fa-trash"></i></button>' +
            '</div>';
        lista.appendChild(div);

        inputs.innerHTML +=
            '<input type="hidden" name="productoId"     value="' + id + '">' +
            '<input type="hidden" name="cantidad"       value="' + item.cantidad + '">' +
            '<input type="hidden" name="precioUnitario" value="' + item.precio + '">';
    });

    totalDisp.textContent = '$' + total.toLocaleString('es-CO', { minimumFractionDigits:2 });

    // Re-validar anticipo con el nuevo total
    const inputAnticipo = document.getElementById('anticipo');
    if (inputAnticipo && inputAnticipo.value) validarAnticipo(inputAnticipo);

    // Si total cae bajo mínimo crédito → resetear a contado
    const selectTipo = document.getElementById('tipoPago');
    if (selectTipo && selectTipo.value === 'CREDITO' && total < MINIMO_CREDITO) {
        selectTipo.value = 'CONTADO';
        toggleCredito('CONTADO');
        Swal.fire({ toast:true, position:'top-end', icon:'warning',
            title:'Total bajo $250.000. Tipo de pago cambiado a Contado.',
            showConfirmButton:false, timer:2500 });
    }
}

/* ── Editar cantidad directamente desde el carrito ── */
function editarItemCarrito(id) {
    const item = AppVenta.carrito[id];
    if (!item) return;
    Swal.fire({
        title: 'Editar cantidad',
        html: '<p style="font-weight:600;margin-bottom:.4rem;">' + item.nombre + '</p>' +
              '<p style="font-size:.85rem;color:#64748b;margin-bottom:.8rem;">Stock disponible: <strong>' + item.stock + '</strong></p>' +
              '<input id="swal-qty" type="number" min="1" max="' + item.stock + '" value="' + item.cantidad + '" class="swal2-input" style="width:110px;">',
        showCancelButton: true,
        confirmButtonText: 'Guardar',
        cancelButtonText: 'Cancelar',
        confirmButtonColor: '#7c3aed',
        preConfirm: () => {
            const val = parseInt(document.getElementById('swal-qty').value);
            if (isNaN(val) || val < 1)     { Swal.showValidationMessage('Mínimo 1 unidad.'); return false; }
            if (val > item.stock)           { Swal.showValidationMessage('Máximo ' + item.stock + ' unidades.'); return false; }
            return val;
        }
    }).then(r => {
        if (r.isConfirmed) {
            AppVenta.carrito[id].cantidad = r.value;
            renderCarrito();
        }
    });
}

/* ── Confirmar eliminación ── */
function confirmarEliminar(id) {
    Swal.fire({
        title: '¿Eliminar producto?',
        text: '"' + AppVenta.carrito[id].nombre + '" se quitará del carrito.',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Sí, eliminar',
        cancelButtonText: 'No',
        confirmButtonColor: '#ef4444'
    }).then(r => {
        if (r.isConfirmed) { delete AppVenta.carrito[id]; renderCarrito(); }
    });
}

function getTotalCarrito() {
    return Object.values(AppVenta.carrito)
        .reduce((acc, it) => acc + it.precio * it.cantidad, 0);
}

/* ═══════════════════════════════════════════
   CRÉDITO
═══════════════════════════════════════════ */
function toggleCredito(valor) {
    const seccion    = document.getElementById('seccionCredito');
    const fechaVenc  = document.getElementById('fechaVencimiento');
    const selectTipo = document.getElementById('tipoPago');

    if (valor === 'CREDITO') {
        const total = getTotalCarrito();
        if (total < MINIMO_CREDITO) {
            Swal.fire({
                title: '¡Monto Insuficiente!',
                html: 'El crédito solo está disponible para compras mayores a <b>$250.000</b>.<br>' +
                      'Tu total actual es: <b>$' + total.toLocaleString('es-CO') + '</b>',
                icon: 'warning', confirmButtonText:'Entendido', confirmButtonColor:'#3085d6'
            });
            selectTipo.value = 'CONTADO';
            seccion.style.display = 'none';
            if (fechaVenc) { fechaVenc.required = false; fechaVenc.value = ''; }
            return;
        }
        if (fechaVenc) fechaVenc.min = mananaISO(); // garantizar min cada vez
    }

    seccion.style.display = (valor === 'CREDITO') ? 'block' : 'none';
    if (fechaVenc) {
        fechaVenc.required = (valor === 'CREDITO');
        if (valor !== 'CREDITO') {
            fechaVenc.value = '';
            setInputState(fechaVenc, '');
            setFieldMsg('msgFechaVenc', '', '');
        }
    }
}
</script>
</body>
</html>
