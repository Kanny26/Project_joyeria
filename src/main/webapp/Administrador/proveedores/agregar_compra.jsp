<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.MetodoPago, java.util.List" %>
<%
    Object admin = session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }

    String proveedorId = (String) request.getAttribute("proveedorId");
    if (proveedorId == null) proveedorId = request.getParameter("id");
    if (proveedorId == null) proveedorId = "";

    List<MetodoPago> metodosPagoList = (List<MetodoPago>) request.getAttribute("metodosPago");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nueva Compra — AAC27</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/proveedores/listar.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <style>
        .info-reception { background: #fff9db; border-left: 4px solid #fcc419; padding: 1rem; margin-bottom: 1.5rem; border-radius: 4px; font-size: .9rem; color: #856404; }
        .input-precio, .input-cantidad { border: 1.5px solid #dee2e6; padding: 8px; border-radius: 4px; width: 90%; transition: border-color .2s; }
        .input-precio:focus, .input-cantidad:focus { border-color: #6b48a0; outline: none; }
        .producto-card { border: 1.5px solid #e5e7eb; border-radius: 10px; padding: .8rem; cursor: pointer; transition: border-color .2s, box-shadow .2s; }
        .producto-card:hover { border-color: #9177a8; box-shadow: 0 4px 12px rgba(107,72,160,.15); }
        .producto-card__img { width: 100%; height: 90px; display: flex; align-items: center; justify-content: center; background: #f8f9fa; border-radius: 6px; overflow: hidden; margin-bottom: 8px; }
        .producto-card__img img { width: 100%; height: 100%; object-fit: cover; }
        .producto-card__img i { font-size: 2.2rem; color: #ced4da; }
        .producto-card__nombre { font-size: .82rem; font-weight: 700; color: #1f2937; margin-bottom: 3px; }
        .producto-card__precio { font-size: .8rem; color: #6b48a0; font-weight: 700; }
        .producto-card__stock-badge { display: inline-flex; align-items: center; gap: 4px; font-size: .72rem; font-weight: 700; padding: 2px 7px; border-radius: 20px; margin-top: 4px; }
        .stock-badge--ok { background: #dcfce7; color: #16a34a; }
        .stock-badge--bajo { background: #fef9c3; color: #854d0e; }
        .stock-badge--cero { background: #fee2e2; color: #dc2626; }
        .categoria-card__icon img { width: 45px; height: 45px; object-fit: contain; }
        .sin-resultados { text-align: center; padding: 2rem; color: #9ca3af; }
        .stock-info-cell { font-size: .75rem; color: #6b7280; margin-top: 3px; }
        .stock-info-cell.stock-ok { color: #16a34a; }
        .stock-info-cell.stock-bajo { color: #d97706; }
        .stock-info-cell.stock-cero { color: #dc2626; }
    </style>
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%=request.getContextPath()%>/ProveedorServlet?action=verCompras&id=<%=proveedorId%>" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
        </span>
    </a>
</nav>

<main class="prov-page">
    <div class="form-card">
        <div class="form-card__title">
            <i class="fa-solid fa-file-invoice-dollar"></i> Registrar Nueva Compra
        </div>

        <div class="info-reception">
            <i class="fa-solid fa-circle-info"></i>
            <strong>Importante:</strong> La fecha de factura no puede ser futura. 
            La recepción y el vencimiento deben ser iguales o posteriores a la factura.
        </div>

        <form id="formCompra">
            <input type="hidden" name="action" value="guardarCompra">
            <input type="hidden" name="proveedorId" value="<%=proveedorId%>">

            <div class="section-title"><i class="fa-solid fa-clock"></i> Registro de Recepción</div>
            <div class="form-row">
                <div class="form-group">
                    <label for="fechaCompra"><i class="fa-regular fa-calendar"></i> Fecha Factura *</label>
                    <input type="date" id="fechaCompra" name="fechaCompra" required>
                </div>
                <div class="form-group">
                    <label for="fechaEntrega"><i class="fa-solid fa-truck-ramp-box"></i> Fecha Recepción *</label>
                    <input type="date" id="fechaEntrega" name="fechaEntrega" required>
                </div>
            </div>

            <div class="section-title"><i class="fa-solid fa-receipt"></i> Información de Pago</div>
            <div class="form-row">
                <div class="form-group">
                    <label for="metodoPagoId"><i class="fa-solid fa-wallet"></i> Método *</label>
                    <select id="metodoPagoId" name="metodoPagoId" required>
                        <option value="">-- Seleccione --</option>
                        <% if (metodosPagoList != null) {
                               for (MetodoPago mp : metodosPagoList) { %>
                            <option value="<%=mp.getMetodoPagoId()%>"><%=mp.getNombre()%></option>
                        <%     }
                           } %>
                    </select>
                </div>
                <div class="form-group">
                    <label for="tipoPago"><i class="fa-solid fa-hand-holding-dollar"></i> Condición</label>
                    <select id="tipoPago" name="tipoPago" onchange="toggleCredito(this.value)">
                        <option value="CONTADO">Contado</option>
                        <option value="CREDITO">A Crédito</option>
                    </select>
                </div>
            </div>

            <div id="seccionCredito" style="display:none;">
                <div class="form-row">
                    <div class="form-group">
                        <label for="fechaVencimiento">Vencimiento *</label>
                        <input type="date" id="fechaVencimiento" name="fechaVencimiento">
                    </div>
                    <div class="form-group">
                        <label for="anticipo">Monto Abonado</label>
                        <input type="number" id="anticipo" name="anticipo" min="0" step="0.01" value="0">
                    </div>
                </div>
            </div>

            <div class="section-title">
                <i class="fa-solid fa-list-check"></i> Productos
                <span class="row-count" id="rowCount">0</span>
            </div>
            <table class="productos-table">
                <thead>
                    <tr>
                        <th>Producto</th>
                        <th>Stock Actual</th>
                        <th>Precio Unit.</th>
                        <th>Cant. a Ingresar</th>
                        <th>Stock Final</th>
                        <th>Subtotal</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody id="tbodyProductos">
                    <tr id="emptyRow">
                        <td colspan="7" style="text-align:center; padding:2rem; color:#9ca3af;">
                            <i class="fa-solid fa-plus-circle"></i> Haga clic en "Agregar producto" para comenzar
                        </td>
                    </tr>
                </tbody>
            </table>

            <div class="bottom-bar">
                <button type="button" class="btn-add-row" id="btnAgregarProducto">
                    <i class="fa-solid fa-plus"></i> Agregar producto
                </button>
                <div class="total-display">
                    Total: <span id="totalDisplay">$0,00</span>
                    <input type="hidden" name="total" id="inputTotal" value="0">
                </div>
            </div>

            <div class="form-actions">
                <a href="<%=request.getContextPath()%>/ProveedorServlet?action=verCompras&id=<%=proveedorId%>" class="btn-cancel">Cancelar</a>
                <button type="button" class="btn-save" id="btnGuardar">
                    <i class="fa-solid fa-floppy-disk"></i> Confirmar Recepción
                </button>
            </div>
        </form>
    </div>
</main>

<div class="modal-overlay" id="modalSeleccion">
    <div class="modal-content">
        <div class="modal-header">
            <h3><i class="fa-solid fa-magnifying-glass"></i> Seleccionar Producto</h3>
            <button class="modal-close" id="btnCerrarModal">&times;</button>
        </div>
        <div class="modal-body">
            <button class="btn-volver-categorias" id="btnVolver" style="display:none;">
                <i class="fa-solid fa-arrow-left"></i> Volver a categorías
            </button>
            <div id="modalContent"></div>
        </div>
    </div>
</div>

<script>
const CTX = '<%=request.getContextPath()%>';
const PROVEEDOR_ID = '<%=proveedorId%>';
let rowIndex = 0;

document.addEventListener('DOMContentLoaded', function () {
    const hoy = new Date().toISOString().split('T')[0];
    const fC = document.getElementById('fechaCompra');
    const fE = document.getElementById('fechaEntrega');
    const fV = document.getElementById('fechaVencimiento');

    // Inicialización
    fC.max = hoy;
    fC.value = hoy;
    fE.value = hoy;
    fE.min = hoy;
    fV.min = hoy;

    // Al cambiar fecha de factura, ajustar límites de entrega y vencimiento
    fC.addEventListener('change', function () {
        if (this.value > hoy) {
            this.value = hoy;
            alerta('Fecha inválida', 'La fecha de factura no puede ser futura.');
        }
        
        const nuevaBase = this.value;
        fE.min = nuevaBase;
        fV.min = nuevaBase;

        if (fE.value < nuevaBase) fE.value = nuevaBase;
        if (fV.value && fV.value < nuevaBase) fV.value = nuevaBase;
    });

    document.getElementById('btnAgregarProducto').addEventListener('click', abrirModal);
    document.getElementById('btnCerrarModal').addEventListener('click', cerrarModal);
    document.getElementById('btnVolver').addEventListener('click', mostrarCategorias);
    document.getElementById('btnGuardar').addEventListener('click', validarYEnviar);
});

function abrirModal() {
    document.getElementById('modalSeleccion').classList.add('active');
    mostrarCategorias();
}
function cerrarModal() {
    document.getElementById('modalSeleccion').classList.remove('active');
}

function mostrarCategorias() {
    document.getElementById('btnVolver').style.display = 'none';
    const content = document.getElementById('modalContent');
    content.innerHTML = '<div class="sin-resultados"><i class="fa-solid fa-circle-notch fa-spin"></i> Cargando...</div>';

    fetch(CTX + '/CompraServlet?action=obtenerCategorias')
        .then(r => r.json())
        .then(cats => {
            if (!cats || cats.length === 0) {
                content.innerHTML = '<div class="sin-resultados">No hay categorías disponibles.</div>';
                return;
            }
            let html = '<div class="categorias-grid">';
            cats.forEach(c => {
                let ico = c.icono ? '<img src="' + CTX + '/assets/Imagenes/iconos/' + c.icono + '" alt="">' : '<i class="fa-solid fa-layer-group"></i>';
                html += '<div class="categoria-card" onclick="mostrarProductos(' + c.id + ')">'
                      + '<div class="categoria-card__icon">' + ico + '</div>'
                      + '<div class="categoria-card__nombre">' + esc(c.nombre) + '</div>'
                      + '</div>';
            });
            content.innerHTML = html + '</div>';
        });
}

function mostrarProductos(categoriaId) {
    document.getElementById('btnVolver').style.display = 'block';
    const content = document.getElementById('modalContent');
    content.innerHTML = '<div class="sin-resultados"><i class="fa-solid fa-circle-notch fa-spin"></i> Buscando...</div>';

    fetch(CTX + '/CompraServlet?action=obtenerProductosPorCategoria&categoriaId=' + categoriaId + '&proveedorId=' + PROVEEDOR_ID)
        .then(r => r.json())
        .then(prods => {
            if (!prods || prods.length === 0) {
                content.innerHTML = '<div class="sin-resultados">Sin productos en esta categoría para este proveedor.</div>';
                return;
            }
            let html = '<div class="productos-grid">';
            prods.forEach(p => {
                let imgHtml = p.imagen ? '<img src="' + CTX + '/imagen-producto/' + p.id + '">' : '<i class="fa-solid fa-box"></i>';
                let stockClass = p.stock === 0 ? 'stock-badge--cero' : p.stock <= 3 ? 'stock-badge--bajo' : 'stock-badge--ok';
                
                html += '<div class="producto-card" onclick="agregarFila(' + p.id + ', \'' + esc(p.nombre) + '\', ' + p.precioUnitario + ', ' + p.stock + ')">'
                      + '<div class="producto-card__img">' + imgHtml + '</div>'
                      + '<div class="producto-card__nombre">' + esc(p.nombre) + '</div>'
                      + '<div class="producto-card__precio">$' + fmt(p.precioUnitario) + '</div>'
                      + '<div class="producto-card__stock-badge ' + stockClass + '">Stock: ' + p.stock + '</div>'
                      + '</div>';
            });
            content.innerHTML = html + '</div>';
        });
}

function agregarFila(productoId, nombre, precio, stockActual) {
    const emptyRow = document.getElementById('emptyRow');
    if (emptyRow) emptyRow.style.display = 'none';

    let i = rowIndex++;
    let stockClass = stockActual === 0 ? 'stock-cero' : stockActual <= 3 ? 'stock-bajo' : 'stock-ok';

    let tr = document.createElement('tr');
    tr.className = 'fila-producto';
    tr.dataset.stockActual = stockActual;
    tr.dataset.idx = i;

    tr.innerHTML = `
        <td>\${esc(nombre)}<input type="hidden" name="productoId" value="\${productoId}"></td>
        <td><span class="stock-info-cell \${stockClass}">\${stockActual}</span></td>
        <td><input type="number" name="precioUnitario" value="\${precio}" step="0.01" min="0.01" class="input-precio" data-idx="\${i}" oninput="calcularFila(\${i})"></td>
        <td><input type="number" name="cantidad" value="1" min="1" class="input-cantidad" data-idx="\${i}" oninput="actualizarStockFinal(\${i})"></td>
        <td id="stockFinal_\${i}" style="font-weight:700; color:#059669;">\${stockActual + 1}</td>
        <td id="sub_\${i}" style="font-weight:700; color:#059669;">$ \${fmt(precio)}</td>
        <td><button type="button" class="btn-delete" onclick="this.closest('tr').remove(); recalcularTodo();"><i class="fa-solid fa-xmark"></i></button></td>
    `;

    document.getElementById('tbodyProductos').appendChild(tr);
    cerrarModal();
    recalcularTodo();
}

function actualizarStockFinal(i) {
    const cantInput = document.querySelector('.input-cantidad[data-idx="' + i + '"]');
    const tr = cantInput.closest('tr');
    const stockActual = parseInt(tr.dataset.stockActual) || 0;
    let cantidad = parseInt(cantInput.value) || 0;

    if (cantidad < 1) {
        cantInput.value = 1;
        cantidad = 1;
    }
    document.getElementById('stockFinal_' + i).textContent = stockActual + cantidad;
    calcularFila(i);
}

function calcularFila(i) {
    let p = parseFloat(document.querySelector('.input-precio[data-idx="' + i + '"]').value) || 0;
    let c = parseInt(document.querySelector('.input-cantidad[data-idx="' + i + '"]').value) || 0;
    document.getElementById('sub_' + i).textContent = '$ ' + fmt(p * c);
    recalcularTodo();
}

function recalcularTodo() {
    let total = 0;
    document.querySelectorAll('[id^="sub_"]').forEach(el => {
        total += parseFloat(el.textContent.replace('$ ', '').replace(/\./g, '').replace(',', '.')) || 0;
    });
    document.getElementById('totalDisplay').textContent = '$ ' + fmt(total);
    document.getElementById('inputTotal').value = total.toFixed(2);
    document.getElementById('rowCount').textContent = document.querySelectorAll('.fila-producto').length;
}

function validarYEnviar() {
    const hoy = new Date().toISOString().split('T')[0];
    const fc = document.getElementById('fechaCompra').value;
    const fe = document.getElementById('fechaEntrega').value;
    const tipoPago = document.getElementById('tipoPago').value;
    const fv = document.getElementById('fechaVencimiento').value;

    // Validaciones de Fecha
    if (!fc || fc > hoy) return alerta('Fecha inválida', 'La fecha de factura no puede ser futura.');
    if (!fe || fe < fc) return alerta('Fecha inválida', 'La recepción no puede ser anterior a la factura.');

    // Validaciones de Crédito
    if (tipoPago === 'CREDITO') {
        if (!fv) return alerta('Campo Requerido', 'Debe indicar la fecha de vencimiento para compras a crédito.');
        if (fv < fc) return alerta('Fecha inválida', 'El vencimiento no puede ser anterior a la fecha de factura.');
    }

    if (!document.getElementById('metodoPagoId').value) return alerta('Campo Requerido', 'Seleccione un método de pago.');
    if (document.querySelectorAll('.fila-producto').length === 0) return alerta('Sin productos', 'Agregue al menos un producto.');

    Swal.fire({
        title: '¿Confirmar registro?',
        text: 'Total: ' + document.getElementById('totalDisplay').textContent,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#6b48a0',
        confirmButtonText: 'Sí, registrar'
    }).then(result => {
        if (!result.isConfirmed) return;
        
        Swal.fire({ title: 'Procesando...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
        const formData = new URLSearchParams(new FormData(document.getElementById('formCompra')));

        fetch(CTX + '/CompraServlet', {
            method: 'POST',
            body: formData,
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        })
        .then(r => r.json())
        .then(j => {
            if (j.ok) {
                Swal.fire({ icon: 'success', title: '¡Éxito!', text: 'Compra registrada correctamente.' })
                .then(() => window.location.href = CTX + '/ProveedorServlet?action=verCompras&id=' + PROVEEDOR_ID + '&msg=creado');
            } else {
                Swal.fire({ icon: 'error', title: 'Error', text: j.error });
            }
        });
    });
}

function toggleCredito(v) {
    document.getElementById('seccionCredito').style.display = (v === 'CREDITO' ? '' : 'none');
}
function alerta(t, m) {
    Swal.fire({ icon: 'warning', title: t, text: m, confirmButtonColor: '#6b48a0' });
}
function fmt(n) {
    return parseFloat(n).toLocaleString('es-CO', { minimumFractionDigits: 2 });
}
function esc(t) {
    let d = document.createElement('div');
    d.textContent = t || '';
    return d.innerHTML;
}
</script>
</body>
</html>