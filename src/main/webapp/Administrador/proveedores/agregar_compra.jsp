<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
Object admin = session.getAttribute("admin");
if (admin == null) {
    response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
    return;
}
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nueva Compra</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/proveedores/listar.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="${pageContext.request.contextPath}/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="${pageContext.request.contextPath}/ProveedorServlet?action=verCompras&id=${usuarioId}"
       class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
        </span>
    </a>
</nav>

<main class="prov-page">
    <div class="form-card">
        <div class="form-card__title">
            <i class="fa-solid fa-cart-plus"></i>
            Nueva Compra
        </div>
        
        <c:if test="${not empty error}">
            <div class="alert alert--error">${error}</div>
        </c:if>
        
        <form action="${pageContext.request.contextPath}/CompraServlet" method="post" id="formCompra">
            <input type="hidden" name="action" value="guardarCompra">
            <input type="hidden" name="usuarioId" value="${usuarioId}">
            
            <div class="form-row">
                <div class="form-group">
                    <label><i class="fa-regular fa-calendar"></i> Fecha de compra</label>
                    <input type="date" name="fechaCompra" id="fechaCompra" required>
                </div>
                <div class="form-group">
                    <label><i class="fa-solid fa-truck"></i> Fecha de entrega esperada</label>
                    <input type="date" name="fechaEntrega" id="fechaEntrega" required>
                </div>
            </div>
            
            <div class="section-title">
                <i class="fa-solid fa-boxes-stacked"></i>
                Productos
                <span class="row-count" id="rowCount">0</span>
            </div>
            
            <table class="productos-table">
                <thead>
                    <tr>
                        <th style="width:35%">Producto</th>
                        <th style="width:18%">Precio unitario</th>
                        <th style="width:12%">Cantidad</th>
                        <th style="width:18%">Subtotal</th>
                        <th style="width:10%"></th>
                    </tr>
                </thead>
                <tbody id="tbodyProductos">
                    <tr id="emptyRow">
                        <td colspan="5">
                            <div class="empty-rows">
                                <i class="fa-solid fa-plus-circle"></i>
                                Presiona "Agregar producto" para comenzar
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
            
            <div class="bottom-bar">
                <button type="button" class="btn-add-row" id="btnAgregarProducto">
                    <i class="fa-solid fa-plus"></i> Agregar producto
                </button>
                <div class="total-display">
                    Total: <span id="totalDisplay">$0.00</span>
                    <input type="hidden" name="total" id="inputTotal" value="0">
                </div>
            </div>
            
            <div class="form-actions">
                <a href="${pageContext.request.contextPath}/ProveedorServlet?action=verCompras&id=${usuarioId}"
                   class="btn-cancel">
                    <i class="fa-solid fa-xmark"></i> Cancelar
                </a>
                <button type="submit" class="btn-save">
                    <i class="fa-solid fa-floppy-disk"></i> Guardar compra
                </button>
            </div>
        </form>
    </div>
</main>

<!-- MODAL SELECCIÓN PRODUCTOS -->
<div class="modal-overlay" id="modalSeleccion">
    <div class="modal-content">
        <div class="modal-header">
            <h3><i class="fa-solid fa-box-open"></i> Seleccionar Producto</h3>
            <button class="modal-close" id="btnCerrarModal">&times;</button>
        </div>
        <div class="modal-body">
            <div class="modal-breadcrumb" id="modalBreadcrumb" style="display:none;">
                <span class="modal-breadcrumb__item" id="btnVolverCategorias">
                    <i class="fa-solid fa-layer-group"></i> Categorías
                </span>
                <span class="modal-breadcrumb__separator">/</span>
                <span class="modal-breadcrumb__item active">
                    <i class="fa-solid fa-tag"></i> <span id="nombreCategoriaSeleccionada"></span>
                </span>
            </div>
            
            <button class="btn-volver-categorias" id="btnVolverCategoriasVisible" style="display:none;">
                <i class="fa-solid fa-arrow-left"></i> Volver a categorías
            </button>
            
            <div id="modalContent">
                <!-- Contenido dinámico cargado vía AJAX -->
            </div>
        </div>
    </div>
</div>

<script>
const ctx = '<%=request.getContextPath()%>';
let rowIndex = 0;
let categoriaActual = null;

// ==================== INICIALIZACIÓN ====================
document.addEventListener('DOMContentLoaded', function() {
    inicializarFechas();
    configurarEventosModal();
    configurarValidacionFormulario();
});

function inicializarFechas() {
    const hoy = new Date().toISOString().split('T')[0];
    const fechaCompra = document.getElementById('fechaCompra');
    const fechaEntrega = document.getElementById('fechaEntrega');
    
    if (fechaCompra) fechaCompra.value = hoy;
    if (fechaEntrega) {
        fechaEntrega.min = hoy;
        if (fechaCompra) {
            fechaCompra.addEventListener('change', function() {
                fechaEntrega.min = this.value;
            });
        }
    }
}

// ==================== EVENTOS MODAL ====================
function configurarEventosModal() {
    const btnAgregar = document.getElementById('btnAgregarProducto');
    const btnCerrar = document.getElementById('btnCerrarModal');
    const modal = document.getElementById('modalSeleccion');
    const btnVolver = document.getElementById('btnVolverCategorias');
    const btnVolverVisible = document.getElementById('btnVolverCategoriasVisible');
    
    if (btnAgregar) btnAgregar.addEventListener('click', abrirModal);
    if (btnCerrar) btnCerrar.addEventListener('click', cerrarModal);
    if (btnVolver) btnVolver.addEventListener('click', mostrarCategorias);
    if (btnVolverVisible) btnVolverVisible.addEventListener('click', mostrarCategorias);
    
    if (modal) {
        modal.addEventListener('click', function(e) {
            if (e.target === modal) cerrarModal();
        });
    }
    
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') cerrarModal();
    });
}

function abrirModal() {
    const modal = document.getElementById('modalSeleccion');
    if (modal) {
        modal.classList.add('active');
        mostrarCategorias();
    }
}

function cerrarModal() {
    const modal = document.getElementById('modalSeleccion');
    if (modal) modal.classList.remove('active');
    categoriaActual = null;
}

// ==================== CARGAR CATEGORÍAS ====================
function mostrarCategorias() {
    categoriaActual = null;
    
    const btnVolver = document.getElementById('btnVolverCategoriasVisible');
    const breadcrumb = document.getElementById('modalBreadcrumb');
    if (btnVolver) btnVolver.style.display = 'none';
    if (breadcrumb) breadcrumb.style.display = 'none';
    
    const content = document.getElementById('modalContent');
    if (!content) return;
    
    content.innerHTML = '<div class="sin-resultados"><i class="fa-solid fa-circle-notch fa-spin"></i><br>Cargando categorías...</div>';
    
    fetch(ctx + '/CompraServlet?action=obtenerCategorias')
        .then(res => res.json())
        .then(categorias => {
            if (!categorias || categorias.length === 0) {
                content.innerHTML = '<div class="sin-resultados"><i class="fa-solid fa-triangle-exclamation"></i><br>No hay categorías disponibles</div>';
                return;
            }
            
            let html = '<div class="categorias-grid">';
            categorias.forEach(c => {
                const iconoHtml = c.icono 
                    ? '<img src="' + ctx + '/assets/Imagenes/iconos/' + c.icono + '" alt="' + c.nombre + '">'
                    : '<i class="fa-solid fa-box"></i>';
                
                html += '<div class="categoria-card" data-id="' + c.id + '" data-nombre="' + escapeHtml(c.nombre) + '">' +
                        '<div class="categoria-card__icon">' + iconoHtml + '</div>' +
                        '<div class="categoria-card__nombre">' + escapeHtml(c.nombre) + '</div>' +
                        '</div>';
            });
            html += '</div>';
            content.innerHTML = html;
            
            // Agregar eventos a las tarjetas
            content.querySelectorAll('.categoria-card').forEach(card => {
                card.addEventListener('click', function() {
                    const id = this.dataset.id;
                    const nombre = this.dataset.nombre;
                    seleccionarCategoria(id, nombre);
                });
            });
        })
        .catch(err => {
            console.error(err);
            content.innerHTML = '<div class="sin-resultados"><i class="fa-solid fa-triangle-exclamation"></i><br>Error al cargar categorías</div>';
        });
}

// ==================== CARGAR PRODUCTOS POR CATEGORÍA ====================
function seleccionarCategoria(categoriaId, categoriaNombre) {
    categoriaActual = { id: categoriaId, nombre: categoriaNombre };
    
    const btnVolver = document.getElementById('btnVolverCategoriasVisible');
    const breadcrumb = document.getElementById('modalBreadcrumb');
    const nombreCat = document.getElementById('nombreCategoriaSeleccionada');
    
    if (btnVolver) btnVolver.style.display = 'inline-flex';
    if (breadcrumb) breadcrumb.style.display = 'flex';
    if (nombreCat) nombreCat.textContent = categoriaNombre;
    
    const content = document.getElementById('modalContent');
    if (!content) return;
    
    content.innerHTML = '<div class="sin-resultados"><i class="fa-solid fa-circle-notch fa-spin"></i><br>Cargando productos...</div>';
    
    fetch(ctx + '/CompraServlet?action=obtenerProductosPorCategoria&categoriaId=' + categoriaId)
        .then(res => res.json())
        .then(productos => {
            if (!productos || productos.length === 0) {
                content.innerHTML = '<div class="sin-resultados"><i class="fa-solid fa-triangle-exclamation"></i><br>No hay productos en esta categoría</div>';
                return;
            }
            
            let html = '<div class="productos-grid">';
            productos.forEach(p => {
                const stockClase = p.stock <= 5 ? 'bajo' : '';
                const imagenHtml = p.imagen
                ? '<img src="' + ctx + '/imagen-producto/' + p.id + '" alt="' + escapeHtml(p.nombre) + '">'
                : '<i class="fa-solid fa-image"></i>';
                
                html += '<div class="producto-card" data-id="' + p.id + '" data-nombre="' + escapeHtml(p.nombre) + 
                        '" data-precio="' + p.precioUnitario + '" data-stock="' + p.stock + '">' +
                        '<span class="producto-card__stock ' + stockClase + '">' + p.stock + ' disp.</span>' +
                        '<div class="producto-card__img">' + imagenHtml + '</div>' +
                        '<div class="producto-card__nombre">' + escapeHtml(p.nombre) + '</div>' +
                        '<div class="producto-card__codigo">' + p.codigo + '</div>' +
                        '<div class="producto-card__precio">$' + formatMoney(p.precioUnitario) + '</div>' +
                        '</div>';
            });
            html += '</div>';
            content.innerHTML = html;
            
            // Agregar eventos a las tarjetas de producto
            content.querySelectorAll('.producto-card').forEach(card => {
                card.addEventListener('click', function() {
                    const id = this.dataset.id;
                    const nombre = this.dataset.nombre;
                    const precio = parseFloat(this.dataset.precio);
                    const stock = parseInt(this.dataset.stock);
                    agregarProductoAlFormulario(id, nombre, precio, stock);
                    cerrarModal();
                });
            });
        })
        .catch(err => {
            console.error(err);
            content.innerHTML = '<div class="sin-resultados"><i class="fa-solid fa-triangle-exclamation"></i><br>Error al cargar productos</div>';
        });
}

// ==================== AGREGAR PRODUCTO AL FORMULARIO ====================
function agregarProductoAlFormulario(productoId, productoNombre, precioUnitario, stock) {
    ocultarEmptyRow();
    
    const i = rowIndex++;
    const tbody = document.getElementById('tbodyProductos');
    if (!tbody) return;
    
    const tr = document.createElement('tr');
    tr.className = 'fila-producto';
    tr.dataset.index = i;
    
    tr.innerHTML = 
        '<td>' +
            '<div class="producto-seleccionado">' +
                '<input type="text" value="' + escapeHtml(productoNombre) + '" readonly class="input-readonly">' +
                '<input type="hidden" name="productoId" value="' + productoId + '">' +
            '</div>' +
        '</td>' +
        '<td>' +
            '<input type="number" name="precioUnitario" value="' + precioUnitario + '" ' +
                   'step="0.01" min="0" class="input-precio" data-index="' + i + '" required>' +
        '</td>' +
        '<td>' +
            '<input type="number" name="cantidad" value="1" min="1" max="' + stock + '" ' +
                   'class="input-cantidad" data-index="' + i + '" required>' +
        '</td>' +
        '<td class="subtotal-cell" id="sub_' + i + '">$0.00</td>' +
        '<td>' +
            '<button type="button" class="btn-remove" data-index="' + i + '" title="Eliminar">' +
                '<i class="fa-solid fa-xmark"></i>' +
            '</button>' +
        '</td>';
    
    tbody.appendChild(tr);
    actualizarContador();
    calcularFila(i);
    
    // Eventos para la nueva fila
    tr.querySelector('.input-precio').addEventListener('input', function() { calcularFila(i); });
    tr.querySelector('.input-cantidad').addEventListener('input', function() { calcularFila(i); });
    tr.querySelector('.btn-remove').addEventListener('click', function() { eliminarFila(this); });
}

// ==================== FUNCIONES AUXILIARES ====================
function ocultarEmptyRow() {
    const e = document.getElementById('emptyRow');
    if (e) e.style.display = 'none';
}

function actualizarContador() {
    const count = document.getElementById('rowCount');
    if (count) {
        count.textContent = document.querySelectorAll('.fila-producto').length;
    }
}

function eliminarFila(btn) {
    const fila = btn.closest('tr');
    if (fila) fila.remove();
    actualizarContador();
    calcularTotal();
    
    if (!document.querySelectorAll('.fila-producto').length) {
        const e = document.getElementById('emptyRow');
        if (e) e.style.display = '';
    }
}

function calcularFila(i) {
    const precio = parseFloat(document.querySelector('.input-precio[data-index="' + i + '"]')?.value) || 0;
    const cantidad = parseInt(document.querySelector('.input-cantidad[data-index="' + i + '"]')?.value) || 0;
    const cell = document.getElementById('sub_' + i);
    
    if (cell) {
        const subtotal = precio * cantidad;
        cell.textContent = '$' + formatMoney(subtotal);
    }
    calcularTotal();
}

function calcularTotal() {
    let total = 0;
    document.querySelectorAll('.subtotal-cell').forEach(c => {
        const texto = c.textContent.replace(/[$,.]/g, s => s === ',' ? '.' : '');
        total += parseFloat(texto) || 0;
    });
    
    const display = document.getElementById('totalDisplay');
    const input = document.getElementById('inputTotal');
    
    if (display) display.textContent = '$' + formatMoney(total);
    if (input) input.value = total.toFixed(2);
}

function formatMoney(amount) {
    return amount.toLocaleString('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function configurarValidacionFormulario() {
    const form = document.getElementById('formCompra');
    if (!form) return;
    
    form.addEventListener('submit', function(e) {
        if (!document.querySelectorAll('.fila-producto').length) {
            e.preventDefault();
            alert('Debes agregar al menos un producto a la compra.');
            return;
        }
        
        let ok = true;
        document.querySelectorAll('.fila-producto').forEach(fila => {
            const prodId = fila.querySelector('input[name="productoId"]').value;
            if (!prodId) {
                ok = false;
                const input = fila.querySelector('input[type="text"]');
                if (input) { 
                    input.style.borderColor = '#ef4444'; 
                    input.focus(); 
                }
            }
        });
        
        if (!ok) {
            e.preventDefault();
            alert('Selecciona un producto válido del listado en cada fila.');
        }
    });
}


</script>
</body>
</html>


