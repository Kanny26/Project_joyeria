<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.MetodoPago, java.util.List" %>
<%
    /* Seguridad: si no hay sesión activa de admin, redirige al login */
    Object admin = session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }

    /*
     * proveedorId puede llegar como atributo del request (via forward desde el servlet)
     * o como parámetro URL (via enlace directo). Se toma el primero que no sea nulo.
     */
    String proveedorId = (String) request.getAttribute("proveedorId");
    if (proveedorId == null) proveedorId = request.getParameter("id");
    if (proveedorId == null) proveedorId = "";

    /* Lista de métodos de pago cargada por el servlet */
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
            <input type="hidden" name="action"      value="guardarCompra">
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
const CTX          = '<%=request.getContextPath()%>';
const PROVEEDOR_ID = '<%=proveedorId%>';
let rowIndex = 0;

/*
 * subtotalesPorFila guarda el valor numérico exacto de cada subtotal.
 * Es el mapa clave del fix: en lugar de leer y parsear el texto del <td>
 * (que tiene formato colombiano con puntos de miles y coma decimal),
 * guardamos el número limpio aquí cada vez que se calcula.
 * Así recalcularTodo() solo suma números, sin riesgo de error de parseo.
 */
const subtotalesPorFila = {};

document.addEventListener('DOMContentLoaded', function () {
    const hoy = new Date().toISOString().split('T')[0];
    const fC  = document.getElementById('fechaCompra');
    const fE  = document.getElementById('fechaEntrega');
    const fV  = document.getElementById('fechaVencimiento');

    fC.max   = hoy;
    fC.value = hoy;
    fE.value = hoy;
    fE.min   = hoy;
    fV.min   = hoy;

    fC.addEventListener('change', function () {
        if (this.value > hoy) {
            this.value = hoy;
            alerta('Fecha inválida', 'La fecha de factura no puede ser futura.');
        }
        const base = this.value;
        fE.min = base;
        fV.min = base;
        if (fE.value < base) fE.value = base;
        if (fV.value && fV.value < base) fV.value = base;
    });

    document.getElementById('btnAgregarProducto').addEventListener('click', abrirModal);
    document.getElementById('btnCerrarModal').addEventListener('click', cerrarModal);
    document.getElementById('btnVolver').addEventListener('click', mostrarCategorias);
    document.getElementById('btnGuardar').addEventListener('click', validarYEnviar);
});

// ── Modal ──────────────────────────────────────────────────────────────────

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
                let ico = c.icono
                    ? '<img src="' + CTX + '/assets/Imagenes/iconos/' + c.icono + '" alt="">'
                    : '<i class="fa-solid fa-layer-group"></i>';
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

    fetch(CTX + '/CompraServlet?action=obtenerProductosPorCategoria&categoriaId='
          + categoriaId + '&proveedorId=' + PROVEEDOR_ID)
        .then(r => r.json())
        .then(prods => {
            if (!prods || prods.length === 0) {
                content.innerHTML = '<div class="sin-resultados">Sin productos en esta categoría para este proveedor.</div>';
                return;
            }
            let html = '<div class="productos-grid">';
            prods.forEach(p => {
                let imgHtml    = p.imagen
                    ? '<img src="' + CTX + '/imagen-producto/' + p.id + '">'
                    : '<i class="fa-solid fa-box"></i>';
                let stockClass = p.stock === 0 ? 'stock-badge--cero'
                               : p.stock <= 3  ? 'stock-badge--bajo'
                               : 'stock-badge--ok';

                /*
                 * Se pasa p.precioUnitario como número limpio al llamar agregarFila().
                 * Nunca se formatea aquí para evitar que el parseo posterior falle.
                 */
                html += '<div class="producto-card" onclick="agregarFila('
                      + p.id + ', \'' + esc(p.nombre) + '\', '
                      + p.precioUnitario + ', ' + p.stock + ')">'
                      + '<div class="producto-card__img">' + imgHtml + '</div>'
                      + '<div class="producto-card__nombre">' + esc(p.nombre) + '</div>'
                      + '<div class="producto-card__precio">$' + fmt(p.precioUnitario) + '</div>'
                      + '<div class="producto-card__stock-badge ' + stockClass + '">Stock: ' + p.stock + '</div>'
                      + '</div>';
            });
            content.innerHTML = html + '</div>';
        });
}

// ── Tabla de productos ─────────────────────────────────────────────────────

function agregarFila(productoId, nombre, precio, stockActual) {
    const emptyRow = document.getElementById('emptyRow');
    if (emptyRow) emptyRow.style.display = 'none';

    let i          = rowIndex++;
    let stockClass = stockActual === 0 ? 'stock-cero'
                   : stockActual <= 3  ? 'stock-bajo'
                   : 'stock-ok';

    // Guardar subtotal inicial en el mapa (precio × 1 unidad por defecto)
    subtotalesPorFila[i] = precio * 1;

    let tr = document.createElement('tr');
    tr.className          = 'fila-producto';
    tr.dataset.stockActual = stockActual;
    tr.dataset.idx         = i;

    tr.innerHTML =
        '<td>' + esc(nombre) + '<input type="hidden" name="productoId" value="' + productoId + '"></td>'
      + '<td><span class="stock-info-cell ' + stockClass + '">' + stockActual + '</span></td>'
      + '<td>'
      +     '<input type="number" name="precioUnitario" value="' + precio + '"'
      +     ' step="0.01" min="0.01" class="input-precio" data-idx="' + i + '"'
      +     ' oninput="calcularFila(' + i + ')">'
      + '</td>'
      + '<td>'
      +     '<input type="number" name="cantidad" value="1" min="1"'
      +     ' class="input-cantidad" data-idx="' + i + '"'
      +     ' oninput="actualizarStockFinal(' + i + ')">'
      + '</td>'
      + '<td id="stockFinal_' + i + '" style="font-weight:700; color:#059669;">' + (stockActual + 1) + '</td>'
      + '<td id="sub_' + i + '" style="font-weight:700; color:#059669;">$ ' + fmt(precio) + '</td>'
      + '<td>'
      +     '<button type="button" class="btn-delete" onclick="eliminarFila(this, ' + i + ')">'
      +         '<i class="fa-solid fa-xmark"></i>'
      +     '</button>'
      + '</td>';

    document.getElementById('tbodyProductos').appendChild(tr);
    cerrarModal();
    recalcularTodo();
}

/*
 * Al eliminar una fila se borra también su entrada en subtotalesPorFila
 * para que no siga sumando al total.
 */
function eliminarFila(btn, i) {
    btn.closest('tr').remove();
    delete subtotalesPorFila[i];
    recalcularTodo();
}

/*
 * Actualiza la columna "Stock Final" cuando cambia la cantidad
 * y luego delega en calcularFila para el subtotal.
 */
function actualizarStockFinal(i) {
    const cantInput  = document.querySelector('.input-cantidad[data-idx="' + i + '"]');
    const tr         = cantInput.closest('tr');
    const stockActual = parseInt(tr.dataset.stockActual) || 0;
    let cantidad = parseInt(cantInput.value) || 0;

    if (cantidad < 1) {
        cantInput.value = 1;
        cantidad = 1;
    }
    document.getElementById('stockFinal_' + i).textContent = stockActual + cantidad;
    calcularFila(i);
}

/*
 * CORRECCIÓN PRINCIPAL:
 * Antes: el subtotal se leía del texto del <td> con replace() para convertir
 * el formato colombiano (1.250,00 → 1250.00). Eso fallaba cuando el número
 * tenía separadores de miles, produciendo totales incorrectos.
 *
 * Ahora: el subtotal se calcula directo de los inputs (números puros sin
 * formato), se guarda en subtotalesPorFila[i] como número limpio, y solo
 * al final se formatea para mostrar en pantalla. recalcularTodo() suma
 * los valores del mapa, nunca parsea texto visible.
 */
function calcularFila(i) {
    const precio   = parseFloat(document.querySelector('.input-precio[data-idx="'   + i + '"]').value) || 0;
    const cantidad = parseInt(document.querySelector('.input-cantidad[data-idx="' + i + '"]').value)   || 0;

    const subtotal = precio * cantidad;

    // Guardar el número limpio en el mapa
    subtotalesPorFila[i] = subtotal;

    // Mostrar formateado en la celda (solo para visualización)
    document.getElementById('sub_' + i).textContent = '$ ' + fmt(subtotal);

    recalcularTodo();
}

/*
 * Suma todos los subtotales del mapa (números puros) para obtener el total.
 * No parsea ningún texto visible — elimina completamente el bug de formato.
 */
function recalcularTodo() {
    let total = 0;
    Object.values(subtotalesPorFila).forEach(function(v) {
        total += v;
    });

    document.getElementById('totalDisplay').textContent = '$ ' + fmt(total);

    // inputTotal envía el total al servidor; se guarda con 2 decimales sin formato
    document.getElementById('inputTotal').value = total.toFixed(2);

    document.getElementById('rowCount').textContent =
        document.querySelectorAll('.fila-producto').length;
}

// ── Envío del formulario ───────────────────────────────────────────────────

function validarYEnviar() {
    const hoy     = new Date().toISOString().split('T')[0];
    const fc      = document.getElementById('fechaCompra').value;
    const fe      = document.getElementById('fechaEntrega').value;
    const tipoPago = document.getElementById('tipoPago').value;
    const fv      = document.getElementById('fechaVencimiento').value;

    if (!fc || fc > hoy)  return alerta('Fecha inválida',   'La fecha de factura no puede ser futura.');
    if (!fe || fe < fc)   return alerta('Fecha inválida',   'La recepción no puede ser anterior a la factura.');
    if (tipoPago === 'CREDITO') {
        if (!fv)          return alerta('Campo Requerido',  'Debe indicar la fecha de vencimiento para compras a crédito.');
        if (fv < fc)      return alerta('Fecha inválida',   'El vencimiento no puede ser anterior a la fecha de factura.');
    }
    if (!document.getElementById('metodoPagoId').value)
                          return alerta('Campo Requerido',  'Seleccione un método de pago.');
    if (document.querySelectorAll('.fila-producto').length === 0)
                          return alerta('Sin productos',    'Agregue al menos un producto.');

    Swal.fire({
        title: '¿Confirmar registro?',
        text:  'Total: ' + document.getElementById('totalDisplay').textContent,
        icon:  'question',
        showCancelButton:    true,
        confirmButtonColor:  '#6b48a0',
        confirmButtonText:   'Sí, registrar'
    }).then(result => {
        if (!result.isConfirmed) return;

        Swal.fire({ title: 'Procesando...', allowOutsideClick: false,
                    didOpen: () => Swal.showLoading() });

        const formData = new URLSearchParams(new FormData(document.getElementById('formCompra')));

        /*
         * Se envía via fetch (AJAX) para poder mostrar el resultado con SweetAlert
         * sin recargar la página. El servidor responde con JSON.
         */
        fetch(CTX + '/CompraServlet', {
            method:  'POST',
            body:    formData,
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        })
        .then(r => r.json())
        .then(j => {
            if (j.ok) {
                Swal.fire({
                    icon: 'success', title: '¡Compra registrada!',
                    text: 'La orden de compra fue guardada correctamente.',
                    confirmButtonColor: '#7c3aed',
                    timer: 2500, timerProgressBar: true
                }).then(() => window.location.href =
                    CTX + '/ProveedorServlet?action=verCompras&id=' + PROVEEDOR_ID + '&msg=creado');
            } else {
                Swal.fire({ icon: 'error', title: 'No se pudo guardar',
                            text: j.error || 'Ocurrió un error inesperado.',
                            confirmButtonColor: '#7c3aed' });
            }
        });
    });
}

// ── Utilidades ─────────────────────────────────────────────────────────────

function toggleCredito(v) {
    document.getElementById('seccionCredito').style.display = (v === 'CREDITO' ? '' : 'none');
}
function alerta(t, m) {
    Swal.fire({ icon: 'warning', title: t, text: m, confirmButtonColor: '#6b48a0' });
}

/*
 * fmt() solo formatea para mostrar en pantalla.
 * NUNCA se usa su resultado para hacer cálculos posteriores.
 */
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
