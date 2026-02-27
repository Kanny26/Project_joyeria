<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Usuario" %>
<%
    Usuario vendedor = (Usuario) session.getAttribute("vendedor");
    if (vendedor == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registrar Venta | AAC27</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">

</head>
<body>


<nav class="navbar-vendedor">
    <div class="navbar-vendedor__catalogo">
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/Seller.png" alt="Admin">
        <h2>Volver al inicio</h2>
    </div>
    <h1 class="navbar-vendedor__title">AAC27</h1>

    <div class="navbar-vendedor__usuario">
        <i class="fas fa-user"></i>
        <span><%= vendedor.getNombre() %></span>
    </div>

    <a href="<%=request.getContextPath()%>/index.jsp">
        <i class="fa-solid fa-house-chimney navbar-vendedor__home-icon"></i>
    </a>
</nav>

<main class="page-main">

    <div class="page-title">
        <i class="fa-solid fa-cash-register"></i>
        Nueva Venta
    </div>

    <% String error = (String) request.getAttribute("error");
       if (error != null) { %>
    <div class="alert-error"><i class="fa-solid fa-circle-exclamation"></i> <%= error %></div>
    <% } %>

    <%-- Formulario apunta al VentaVendedorServlet --%>
    <form action="<%=request.getContextPath()%>/Vendedor/ventas/registrar" method="POST" id="formVenta">

        <!-- ── CLIENTE ─────────────────────────────────── -->
        <div class="card">
            <div class="card__title"><i class="fa-solid fa-user-tag"></i> Información del Cliente</div>
            <div class="form-row">
                <div class="form-group">
                    <label><i class="fa-solid fa-user"></i> Nombre del Cliente</label>
                    <input type="text" name="clienteNombre" class="form-control" placeholder="Nombre completo" required>
                </div>
                <div class="form-group">
                    <label><i class="fa-solid fa-phone"></i> Teléfono</label>
                    <input type="text" name="clienteTelefono" class="form-control" placeholder="Ej: 300 123 4567">
                </div>
                <div class="form-group">
                    <label><i class="fa-regular fa-calendar-check"></i> Fecha de Venta</label>
                    <input type="date" name="fechaVenta" id="fechaVenta" class="form-control" required>
                </div>
            </div>
        </div>

        <!-- ── MÉTODO DE PAGO ──────────────────────────── -->
        <div class="card">
            <div class="card__title"><i class="fa-solid fa-credit-card"></i> Método de Pago</div>

            <div class="pago-grid">
                <label class="pago-card">
                    <input type="radio" name="metodoPago" value="efectivo" required>
                    <div class="pago-card__label">
                        <i class="fa-solid fa-money-bill-wave"></i>
                        <div>
                            <span>Efectivo</span>
                            <small>Pago en mano</small>
                        </div>
                    </div>
                </label>
                <label class="pago-card">
                    <input type="radio" name="metodoPago" value="tarjeta">
                    <div class="pago-card__label">
                        <i class="fa-solid fa-mobile-screen"></i>
                        <div>
                            <span>Transferencia</span>
                            <small>Nequi · Bancolombia · Daviplata</small>
                        </div>
                    </div>
                </label>
            </div>

            <!-- MODALIDAD -->
            <div style="margin-top: 20px;">
                <div class="card__title" style="margin-bottom: 12px;"><i class="fa-solid fa-layer-group"></i> Modalidad</div>
                <div class="modalidad-grid">
                    <label class="pago-card">
                        <input type="radio" name="modalidad" value="contado" checked id="radioContado">
                        <div class="pago-card__label">
                            <i class="fa-solid fa-check-circle"></i>
                            <div>
                                <span>Contado</span>
                                <small>Pago total inmediato</small>
                            </div>
                        </div>
                    </label>
                    <label class="pago-card">
                        <input type="radio" name="modalidad" value="anticipo" id="radioAnticipo">
                        <div class="pago-card__label">
                            <i class="fa-solid fa-hourglass-half"></i>
                            <div>
                                <span>Con Anticipo</span>
                                <small>Pago parcial + saldo pendiente</small>
                            </div>
                        </div>
                    </label>
                </div>

                <div class="anticipo-box" id="anticipoBox">
                    <div class="form-group">
                        <label><i class="fa-solid fa-coins"></i> Monto del Anticipo</label>
                        <input type="number" name="montoAnticipo" id="montoAnticipo" class="form-control"
                               placeholder="$0.00" min="1" step="0.01">
                    </div>
                </div>
            </div>
        </div>

        <!-- ── PRODUCTOS ───────────────────────────────── -->
        <div class="card">
            <div class="card__title">
                <i class="fa-solid fa-boxes-stacked"></i> Productos
                <span class="row-count" id="rowCount">0</span>
            </div>

            <table class="productos-table">
                <thead>
                    <tr>
                        <th style="width:38%">Producto</th>
                        <th style="width:12%">Cant.</th>
                        <th style="width:22%">Precio Unit.</th>
                        <th style="width:22%">Subtotal</th>
                        <th style="width:6%"></th>
                    </tr>
                </thead>
                <tbody id="tbodyProductos">
                    <tr id="emptyRow">
                        <td colspan="5">
                            <div class="empty-rows">
                                <i class="fa-solid fa-plus-circle"></i>
                                Presiona "Agregar Producto" para comenzar
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>

            <div class="bottom-bar">
                <button type="button" class="btn-add-row" id="btnAgregarProducto">
                    <i class="fa-solid fa-magnifying-glass-plus"></i> Agregar Producto
                </button>
                <div class="total-display">
                    Total: <span id="totalDisplay">$0.00</span>
                    <input type="hidden" name="totalVenta" id="inputTotal" value="0">
                </div>
            </div>
        </div>

        <!-- ── ACCIONES ────────────────────────────────── -->
        <div class="form-actions">
            <button type="reset" class="btn-cancel" onclick="limpiarFormulario()">
                <i class="fa-solid fa-rotate-left"></i> Limpiar
            </button>
            <button type="submit" class="btn-save">
                <i class="fa-solid fa-check-double"></i> Finalizar Venta
            </button>
        </div>
    </form>
</main>

<!-- ══════════════════════════════════════════════════════════
     MODAL SELECCIÓN DE PRODUCTOS
     ══════════════════════════════════════════════════════════ -->
<div class="modal-overlay" id="modalSeleccion">
    <div class="modal-content">

        <div class="modal-header">
            <h3><i class="fa-solid fa-box-open"></i> Seleccionar Producto</h3>
            <button class="modal-close" id="btnCerrarModal">&times;</button>
        </div>

        <div class="modal-toolbar">
            <button class="btn-volver" id="btnVolver">
                <i class="fa-solid fa-arrow-left"></i> Categorías
            </button>
            <div class="breadcrumb" id="breadcrumb">
                <span style="color:var(--text-muted)"><i class="fa-solid fa-layer-group"></i> Categorías</span>
                <span class="sep">/</span>
                <span class="active" id="nombreCat"></span>
            </div>
        </div>

        <div class="modal-body" id="modalBody">
            <div class="modal-loader">
                <i class="fa-solid fa-circle-notch fa-spin"></i>
                Cargando...
            </div>
        </div>
    </div>
</div>

<!-- ══════════════════════════════════════════════════════════
     JAVASCRIPT
     ══════════════════════════════════════════════════════════ -->
<script>
const ctx = '<%=request.getContextPath()%>';
let rowIndex = 0;

// ── INICIALIZACIÓN ────────────────────────────────────────
document.addEventListener('DOMContentLoaded', function () {
    // Fecha de hoy por defecto
    document.getElementById('fechaVenta').value = new Date().toISOString().split('T')[0];

    // Botón agregar
    document.getElementById('btnAgregarProducto').addEventListener('click', abrirModal);

    // Cerrar modal
    document.getElementById('btnCerrarModal').addEventListener('click', cerrarModal);
    document.getElementById('modalSeleccion').addEventListener('click', function (e) {
        if (e.target === this) cerrarModal();
    });
    document.addEventListener('keydown', function (e) { if (e.key === 'Escape') cerrarModal(); });

    // Volver a categorías
    document.getElementById('btnVolver').addEventListener('click', mostrarCategorias);

    // Mostrar/ocultar caja de anticipo
    document.querySelectorAll('input[name="modalidad"]').forEach(function (r) {
        r.addEventListener('change', function () {
            const box = document.getElementById('anticipoBox');
            const inp = document.getElementById('montoAnticipo');
            if (this.value === 'anticipo') {
                box.classList.add('visible');
                inp.required = true;
            } else {
                box.classList.remove('visible');
                inp.required = false;
            }
        });
    });

    // Validación submit
    document.getElementById('formVenta').addEventListener('submit', function (e) {
        if (!document.querySelectorAll('.fila-producto').length) {
            e.preventDefault();
            alert('Debes agregar al menos un producto a la venta.');
        }
    });
});

// ── MODAL ─────────────────────────────────────────────────
function abrirModal() {
    document.getElementById('modalSeleccion').classList.add('active');
    mostrarCategorias();
}
function cerrarModal() {
    document.getElementById('modalSeleccion').classList.remove('active');
}

// ── CATEGORÍAS ────────────────────────────────────────────
function mostrarCategorias() {
    document.getElementById('btnVolver').classList.remove('visible');
    document.getElementById('breadcrumb').classList.remove('visible');

    const body = document.getElementById('modalBody');
    body.innerHTML = '<div class="modal-loader"><i class="fa-solid fa-circle-notch fa-spin"></i><span>Cargando categorías...</span></div>';

    fetch(ctx + '/CompraServlet?action=obtenerCategorias')
        .then(function (r) { return r.json(); })
        .then(function (cats) {
            if (!cats || cats.length === 0) {
                body.innerHTML = '<div class="sin-resultados"><i class="fa-solid fa-triangle-exclamation"></i><span>No hay categorías disponibles</span></div>';
                return;
            }
            let html = '<div class="categorias-grid">';
            cats.forEach(function (c) {
                const icono = c.icono
                    ? '<img src="' + ctx + '/assets/Imagenes/iconos/' + c.icono + '" alt="' + esc(c.nombre) + '">'
                    : '<i class="fa-solid fa-box"></i>';
                html += '<div class="categoria-card" data-id="' + c.id + '" data-nombre="' + esc(c.nombre) + '">'
                      + '<div class="categoria-card__icon">' + icono + '</div>'
                      + '<div class="categoria-card__nombre">' + esc(c.nombre) + '</div>'
                      + '</div>';
            });
            html += '</div>';
            body.innerHTML = html;
            body.querySelectorAll('.categoria-card').forEach(function (card) {
                card.addEventListener('click', function () {
                    seleccionarCategoria(this.dataset.id, this.dataset.nombre);
                });
            });
        })
        .catch(function () {
            body.innerHTML = '<div class="sin-resultados"><i class="fa-solid fa-triangle-exclamation"></i><span>Error al cargar categorías</span></div>';
        });
}

// ── PRODUCTOS POR CATEGORÍA ───────────────────────────────
function seleccionarCategoria(catId, catNombre) {
    document.getElementById('btnVolver').classList.add('visible');
    document.getElementById('breadcrumb').classList.add('visible');
    document.getElementById('nombreCat').textContent = catNombre;

    const body = document.getElementById('modalBody');
    body.innerHTML = '<div class="modal-loader"><i class="fa-solid fa-circle-notch fa-spin"></i><span>Cargando productos...</span></div>';

    fetch(ctx + '/CompraServlet?action=obtenerProductosPorCategoria&categoriaId=' + catId)
        .then(function (r) { return r.json(); })
        .then(function (prods) {
            if (!prods || prods.length === 0) {
                body.innerHTML = '<div class="sin-resultados"><i class="fa-solid fa-triangle-exclamation"></i><span>Sin productos en esta categoría</span></div>';
                return;
            }
            let html = '<div class="productos-grid">';
            prods.forEach(function (p) {
                const sinStock = p.stock <= 0 ? 'sin-stock' : '';
                const badgeClass = p.stock <= 5 ? 'bajo' : '';
                const imgHtml = p.imagen
                    ? '<img src="' + ctx + '/imagen-producto/' + p.id + '" alt="' + esc(p.nombre) + '">'
                    : '<i class="fa-solid fa-gem"></i>';
                html += '<div class="producto-card ' + sinStock + '" '
                      + 'data-id="' + p.id + '" data-nombre="' + esc(p.nombre) + '" '
                      + 'data-precio="' + p.precioUnitario + '" data-stock="' + p.stock + '">'
                      + '<span class="producto-card__stock-badge ' + badgeClass + '">' + p.stock + ' disp.</span>'
                      + '<div class="producto-card__img">' + imgHtml + '</div>'
                      + '<div class="producto-card__nombre">' + esc(p.nombre) + '</div>'
                      + '<div class="producto-card__codigo">' + (p.codigo || '') + '</div>'
                      + '<div class="producto-card__precio">$' + fmt(p.precioUnitario) + '</div>'
                      + '</div>';
            });
            html += '</div>';
            body.innerHTML = html;
            body.querySelectorAll('.producto-card').forEach(function (card) {
                card.addEventListener('click', function () {
                    agregarFila(
                        this.dataset.id,
                        this.dataset.nombre,
                        parseFloat(this.dataset.precio),
                        parseInt(this.dataset.stock)
                    );
                    cerrarModal();
                });
            });
        })
        .catch(function () {
            body.innerHTML = '<div class="sin-resultados"><i class="fa-solid fa-triangle-exclamation"></i><span>Error al cargar productos</span></div>';
        });
}

// ── AGREGAR FILA ──────────────────────────────────────────
function agregarFila(id, nombre, precio, stock) {
    const emptyRow = document.getElementById('emptyRow');
    if (emptyRow) emptyRow.style.display = 'none';

    const i = rowIndex++;
    const tbody = document.getElementById('tbodyProductos');
    const tr = document.createElement('tr');
    tr.className = 'fila-producto';
    tr.dataset.index = i;

    tr.innerHTML =
        '<td>' +
            '<input type="text" value="' + esc(nombre) + '" readonly class="input-table" style="width:100%">' +
            '<input type="hidden" name="productoId" value="' + id + '">' +
        '</td>' +
        '<td>' +
            '<input type="number" name="cantidad" value="1" min="1" max="' + stock + '" ' +
                   'class="input-table input-cant" data-idx="' + i + '" required style="width:70px">' +
        '</td>' +
        '<td>' +
            '<input type="number" name="precioVenta" value="' + precio + '" ' +
                   'step="0.01" min="0" class="input-table input-precio" data-idx="' + i + '" required style="width:110px">' +
        '</td>' +
        '<td class="subtotal-cell" id="sub_' + i + '">$0.00</td>' +
        '<td>' +
            '<button type="button" class="btn-remove" title="Eliminar">' +
                '<i class="fa-solid fa-xmark"></i>' +
            '</button>' +
        '</td>';

    tbody.appendChild(tr);

    tr.querySelector('.input-cant').addEventListener('input', function () { recalcular(i); });
    tr.querySelector('.input-precio').addEventListener('input', function () { recalcular(i); });
    tr.querySelector('.btn-remove').addEventListener('click', function () {
        tr.remove();
        actualizarContador();
        calcularTotal();
        if (!document.querySelectorAll('.fila-producto').length) {
            document.getElementById('emptyRow').style.display = '';
        }
    });

    actualizarContador();
    recalcular(i);
}

// ── CÁLCULOS ──────────────────────────────────────────────
function recalcular(i) {
    const precio = parseFloat(document.querySelector('.input-precio[data-idx="' + i + '"]').value) || 0;
    const cant   = parseInt(document.querySelector('.input-cant[data-idx="' + i + '"]').value) || 0;
    const sub    = precio * cant;
    const cell   = document.getElementById('sub_' + i);
    if (cell) cell.textContent = '$' + fmt(sub);
    calcularTotal();
}

function calcularTotal() {
    let total = 0;
    document.querySelectorAll('.subtotal-cell').forEach(function (c) {
        total += parseFloat(c.textContent.replace('$', '').replace(/\./g, '').replace(',', '.')) || 0;
    });
    document.getElementById('totalDisplay').textContent = '$' + fmt(total);
    document.getElementById('inputTotal').value = total.toFixed(2);
}

function actualizarContador() {
    document.getElementById('rowCount').textContent = document.querySelectorAll('.fila-producto').length;
}

function limpiarFormulario() {
    document.getElementById('tbodyProductos').querySelectorAll('.fila-producto').forEach(function (r) { r.remove(); });
    document.getElementById('emptyRow').style.display = '';
    actualizarContador();
    calcularTotal();
    document.getElementById('anticipoBox').classList.remove('visible');
}

// ── UTILS ─────────────────────────────────────────────────
function fmt(n) {
    return Number(n).toLocaleString('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
function esc(t) {
    const d = document.createElement('div');
    d.textContent = t;
    return d.innerHTML;
}
</script>
</body>
</html>
