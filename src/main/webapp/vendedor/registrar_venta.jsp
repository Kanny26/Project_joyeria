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

   <form action="<%= request.getContextPath() %>/VentaVendedorServlet?action=guardarVenta" method="post" id="formVenta">
        <input type="hidden" name="action" value="guardarVenta">

        <%-- Datos del cliente --%>
        <div class="form-card" style="margin-bottom:1.5rem;">
            <div class="form-card__title"><i class="fa-solid fa-user"></i> Datos del Cliente</div>
            <div class="form-row">
                <div class="form-group">
                    <label><i class="fa-solid fa-user-pen"></i> Nombre del cliente *</label>
                    <input type="text" name="clienteNombre" required placeholder="Nombre completo" maxlength="100">
                </div>
                <div class="form-group">
                    <label><i class="fa-solid fa-phone"></i> Teléfono</label>
                    <input type="tel" name="clienteTelefono" placeholder="Ej: 3001234567" maxlength="20">
                </div>
                <div class="form-group">
                    <label><i class="fa-solid fa-envelope"></i> Email</label>
                    <input type="email" name="clienteEmail" placeholder="correo@ejemplo.com" maxlength="100">
                </div>
            </div>
        </div>

        <%-- Datos de la venta --%>
        <div class="form-card" style="margin-bottom:1.5rem;">
            <div class="form-card__title"><i class="fa-solid fa-receipt"></i> Datos de la Venta</div>
            <div class="form-row">
                <div class="form-group">
                    <label><i class="fa-solid fa-calendar-day"></i> Fecha de venta *</label>
                    <input type="date" name="fechaVenta" id="fechaVenta" required>
                </div>
                
               <div class="form-row">
                <div class="form-group">
                    <label><i class="fa-solid fa-wallet"></i> Método de pago</label>
                    <select name="metodoPago" id="metodoPagoId" required>
                        <option value="">-- Selecciona un método --</option>
                        <%
                            java.util.List<model.MetodoPago> metodosPagoList = (java.util.List<model.MetodoPago>) request.getAttribute("metodosPago");
                            if (metodosPagoList != null) {
                                for (model.MetodoPago mp : metodosPagoList) { %>
                                    <option value="<%= mp.getMetodoPagoId() %>"><%= mp.getNombre() %></option>
                        <%      }
                            }
                        %>
                    </select>
                </div>
                <div class="form-group">
                    <label><i class="fa-solid fa-hand-holding-dollar"></i> Tipo de pago</label>
                    <select name="tipoPago" id="tipoPago" onchange="toggleCredito(this.value)">
                        <option value="CONTADO">Contado</option>
                        <option value="CREDITO">Crédito</option>
                    </select>
                </div>
            </div>

            <!-- Campos de crédito (ocultos por defecto) -->
            <div id="seccionCredito" style="display:none;">
                <div class="form-row">
                    <div class="form-group">
                        <label><i class="fa-regular fa-calendar-xmark"></i> Fecha límite de pago</label>
                        <input type="date" name="fechaVencimiento" id="fechaVencimiento">
                    </div>
                    <div class="form-group">
                        <label><i class="fa-solid fa-money-bill-wave"></i> Anticipo (opcional)</label>
                        <input type="number" name="anticipo" id="anticipo" min="0" step="0.01" placeholder="0.00">
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

        <%-- Carrito de productos --%>
        <div class="form-card" style="margin-bottom:1.5rem;">
            <div class="form-card__title" style="display:flex;justify-content:space-between;align-items:center;">
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
            <div style="margin-top:1rem;padding:.75rem 1rem;background:#f0fdf4;border-radius:10px;display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:700;color:#166534;">Total</span>
                <span id="totalDisplay">$0.00</span>
            </div>
        </div>

        <div id="inputsCarrito"></div>

        <div class="form-actions">
            <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=misVentas" class="btn-cancel">
                <i class="fa-solid fa-xmark"></i> Cancelar
            </a>
            <button type="submit" class="btn-save">
                <i class="fa-solid fa-floppy-disk"></i> Guardar Venta
            </button>
        </div>
    </form>
</main>
<div class="modal-overlay" id="modalSeleccion">
    <div class="modal-box"> <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem; border-bottom:1px solid #e2e8f0; padding-bottom:10px;">
            <h3 style="font-weight:800; color:#1e1b4b; font-size:1.1rem; margin:0;">
                <i class="fa-solid fa-box-open"></i> Seleccionar Producto
            </h3>
            <button type="button" onclick="cerrarModal()" style="background:none; border:none; font-size:1.3rem; cursor:pointer; color:#64748b;">
                <i class="fa-solid fa-xmark"></i>
            </button>
        </div>
        
        <div class="modal-body">
            <div class="modal-breadcrumb" id="modalBreadcrumb" style="display:none; margin-bottom:1rem;">
                <span class="modal-breadcrumb__item" onclick="mostrarCategorias()" style="cursor:pointer; color:#7c3aed;">
                    <i class="fa-solid fa-layer-group"></i> Categorías
                </span>
                <span class="modal-breadcrumb__separator"> / </span>
                <span class="modal-breadcrumb__item active">
                    <i class="fa-solid fa-tag"></i> <span id="nombreCategoriaSeleccionada"></span>
                </span>
            </div>
            
            <button class="btn-volver-categorias" id="btnVolverCategoriasVisible" onclick="mostrarCategorias()" style="display:none; margin-bottom:1rem;">
                <i class="fa-solid fa-arrow-left"></i> Volver a categorías
            </button>
            
            <div id="modalContent">
                </div>

            <div id="addQtySection" style="display:none; margin-top:1rem; padding:1rem; background:#f5f3ff; border-radius:12px; border:1px solid #c4b5fd;">
                <div style="font-weight:700; color:#1e1b4b; margin-bottom:4px;" id="prodSelNombre"></div>
                <div style="font-size:.82rem; color:#64748b; margin-bottom:.75rem;" id="prodSelInfo"></div>
                <div style="display:flex; gap:1rem; align-items:flex-end; flex-wrap:wrap;">
                    <div>
                        <label style="font-size:.82rem; font-weight:600; display:block; margin-bottom:4px;">Cantidad *</label>
                        <input type="number" id="cantidadInput" min="1" value="1"
                               style="width:90px; padding:.4rem .6rem; border:1.5px solid #c4b5fd; border-radius:8px; font-weight:700;">
                    </div>
                    <button type="button" class="btn-save" onclick="confirmarAgregarAlCarrito()"
                            style="padding:.45rem 1.2rem; font-size:.85rem;">
                        <i class="fa-solid fa-cart-plus"></i> Agregar
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script type="text/javascript">
// Variables de estado
const AppVenta = {
    carrito: {},
    productoSeleccionado: null,
    ctx: '<%= request.getContextPath() %>'
};

document.addEventListener('DOMContentLoaded', function() {
    const f = document.getElementById('fechaVenta');
    if(f) f.value = new Date().toISOString().split('T')[0];
});

// --- FUNCIONES DEL MODAL ---
function abrirModal() {
    document.getElementById('modalSeleccion').classList.add('active');
    mostrarCategorias();
}

function cerrarModal() {
    document.getElementById('modalSeleccion').classList.remove('active');
    AppVenta.productoSeleccionado = null;
    document.getElementById('addQtySection').style.display = 'none';
}

function mostrarCategorias() {
    const content = document.getElementById('modalContent');
    const breadcrumb = document.getElementById('modalBreadcrumb');
    const btnVolver = document.getElementById('btnVolverCategoriasVisible');
    const qtySection = document.getElementById('addQtySection');

    breadcrumb.style.display = 'none';
    btnVolver.style.display = 'none';
    qtySection.style.display = 'none';
    content.innerHTML = '<div style="text-align:center; padding:2rem;"><i class="fa-solid fa-circle-notch fa-spin"></i> Cargando categorías...</div>';

    fetch(AppVenta.ctx + '/VentaVendedorServlet?action=obtenerCategorias')
        .then(res => res.json())
        .then(categorias => {
            let html = '<div class="categorias-grid">';
            categorias.forEach(c => {
                const iconoHtml = c.icono 
                ? '<img src="' + AppVenta.ctx + '/assets/Imagenes/iconos/' + c.icono + '" alt="' + c.nombre + '">'
                : '<i class="fa-solid fa-box"></i>';

	            html += '<div class="categoria-card" onclick="seleccionarCategoria(' + c.id + ', \'' + c.nombre.replace(/'/g, "\\'") + '\')">';
	            html += '<div class="categoria-card__icon">' + iconoHtml + '</div>';
	            html += '<div class="categoria-card__nombre" style="font-weight:600; font-size:0.9rem;">' + c.nombre + '</div>';
	            html += '</div>';
            });
            html += '</div>';
            content.innerHTML = html;
        });
}

function seleccionarCategoria(id, nombre) {
    const content = document.getElementById('modalContent');
    document.getElementById('modalBreadcrumb').style.display = 'flex';
    document.getElementById('btnVolverCategoriasVisible').style.display = 'inline-flex';
    document.getElementById('nombreCategoriaSeleccionada').textContent = nombre;
    
    content.innerHTML = '<div style="text-align:center; padding:2rem;">Cargando productos...</div>';

    fetch(AppVenta.ctx + '/VentaVendedorServlet?action=obtenerProductosPorCategoria&categoriaId=' + id)
        .then(res => res.json())
        .then(productos => {
            let html = '<div class="productos-grid">';
            productos.forEach(p => {
                const sinStock = p.stock <= 0;
                const pJson = JSON.stringify(p).replace(/'/g, "&#39;");
                const imagenHtml = p.imagen
	                ? '<img src="' + AppVenta.ctx + '/imagen-producto/' + p.id + '" alt="' + p.nombre + '">'
	                : '<i class="fa-solid fa-image"></i>';
	
	            html += '<div class="producto-card ' + (p.stock <= 0 ? 'sin-stock' : '') + '" ' +
	                    (p.stock <= 0 ? '' : 'onclick="prepararProducto(this)"') + ' data-p=\'' + pJson + '\'>';
	            html += '<span class="producto-card__stock ' + (p.stock < 5 ? 'bajo' : '') + '">' + p.stock + ' disp.</span>';
	            html += '<div class="producto-card__img">' + imagenHtml + '</div>';
	            html += '<div class="producto-card__nombre" style="font-weight:700; font-size:0.85rem;">' + p.nombre + '</div>';
	            html += '<div class="producto-card__precio" style="color:#7c3aed; font-weight:800;">$' + Number(p.precioUnitario).toLocaleString('es-CO') + '</div>';
	            html += '</div>';
                
            });
            html += '</div>';
            content.innerHTML = html;
        });
}

function prepararProducto(elemento) {
    const p = JSON.parse(elemento.getAttribute('data-p'));
    AppVenta.productoSeleccionado = p;
    
    document.getElementById('prodSelNombre').textContent = p.nombre;
    document.getElementById('prodSelInfo').textContent = "Precio: $" + Number(p.precioUnitario).toLocaleString('es-CO') + " | Stock: " + p.stock;
    document.getElementById('cantidadInput').max = p.stock;
    document.getElementById('cantidadInput').value = 1;
    document.getElementById('addQtySection').style.display = 'block';
    document.getElementById('addQtySection').scrollIntoView({ behavior: 'smooth' });
}

function confirmarAgregarAlCarrito() {
    const p = AppVenta.productoSeleccionado;
    const cant = parseInt(document.getElementById('cantidadInput').value);

    if (!p || isNaN(cant) || cant <= 0 || cant > p.stock) {
        alert("Cantidad no válida o supera el stock disponible");
        return;
    }

    // Lógica para acumular en el carrito
    if (AppVenta.carrito[p.id]) {
        const nuevaCant = AppVenta.carrito[p.id].cantidad + cant;
        if (nuevaCant > p.stock) {
            alert("No hay suficiente stock para sumar esa cantidad.");
            return;
        }
        AppVenta.carrito[p.id].cantidad = nuevaCant;
    } else {
        AppVenta.carrito[p.id] = { 
            nombre: p.nombre, 
            precio: p.precioUnitario, 
            cantidad: cant, 
            stock: p.stock 
        };
    }

    renderCarrito();
    cerrarModal();
}

function renderCarrito() {
    const lista = document.getElementById('carritoLista');
    const inputs = document.getElementById('inputsCarrito');
    const totalDisp = document.getElementById('totalDisplay');
    const vacio = document.getElementById('carritoVacio');
    let total = 0;

    lista.innerHTML = '';
    inputs.innerHTML = '';

    const ids = Object.keys(AppVenta.carrito);
    if (ids.length === 0) {
        vacio.style.display = 'block';
        totalDisp.textContent = '$0.00';
        return;
    }

    vacio.style.display = 'none';
    ids.forEach(id => {
        const item = AppVenta.carrito[id];
        const subtotal = item.precio * item.cantidad;
        total += subtotal;

        const div = document.createElement('div');
        div.className = 'carrito-item';
        div.style = 'display:flex; justify-content:space-between; align-items:center; padding:10px; border-bottom:1px solid #f1f5f9;';
        div.innerHTML = '<div><strong>' + item.nombre + '</strong><br><small>$' + Number(item.precio).toLocaleString('es-CO') + ' x ' + item.cantidad + '</small></div>' +
                        '<div style="display:flex; align-items:center; gap:10px;"><span style="font-weight:700;">$' + subtotal.toLocaleString('es-CO') + '</span>' +
                        '<button type="button" onclick="removerDelCarrito(' + id + ')" style="color:#ef4444; background:none; border:none; cursor:pointer;"><i class="fa-solid fa-trash"></i></button></div>';
        
        lista.appendChild(div);

        inputs.innerHTML += '<input type="hidden" name="productoId" value="' + id + '">' +
                            '<input type="hidden" name="cantidad" value="' + item.cantidad + '">' +
                            '<input type="hidden" name="precioUnitario" value="' + item.precio + '">';
    });

    totalDisp.textContent = '$' + total.toLocaleString('es-CO', { minimumFractionDigits: 2 });

    // Si el total baja de $250.000 y estaba en crédito, resetear a contado
    const selectTipo = document.getElementById('tipoPago');
    if (selectTipo && selectTipo.value === 'CREDITO' && total < MINIMO_CREDITO) {
        selectTipo.value = 'CONTADO';
        toggleCredito('CONTADO');
        if (ids.length > 0) {
            alert('⚠️ El total bajó de $250.000. Se cambió el tipo de pago a Contado.');
        }
    }
}

function removerDelCarrito(id) {
    delete AppVenta.carrito[id];
    renderCarrito();
}

const MINIMO_CREDITO = 250000;

function getTotalCarrito() {
    let total = 0;
    Object.values(AppVenta.carrito).forEach(item => {
        total += item.precio * item.cantidad;
    });
    return total;
}
function toggleCredito(valor) {
    const seccion = document.getElementById('seccionCredito');
    const fechaVenc = document.getElementById('fechaVencimiento');
    const selectTipo = document.getElementById('tipoPago');

    if (valor === 'CREDITO') {
        const total = getTotalCarrito();
        
        if (total < MINIMO_CREDITO) {
            // Formatear el dinero para la modal
            const totalFormateado = total.toLocaleString('es-CO', { 
                style: 'currency', 
                currency: 'COP' 
            });

            // Reemplazo del alert por SweetAlert2
            Swal.fire({
                title: '¡Monto Insuficiente!',
                html: `El crédito solo está disponible para compras mayores a <b>$250.000</b>.<br><br>Tu total actual es: <b>${totalFormateado}</b>`,
                icon: 'warning',
                confirmButtonText: 'Entendido',
                confirmButtonColor: '#3085d6'
            });

            // Resetear el select y ocultar sección
            selectTipo.value = 'CONTADO';
            seccion.style.display = 'none';
            if (fechaVenc) { 
                fechaVenc.required = false; 
                fechaVenc.value = ''; 
            }
            return;
        }
    }

    // Lógica normal si pasa la validación o si es CONTADO
    seccion.style.display = (valor === 'CREDITO') ? 'block' : 'none';
    if (fechaVenc) {
        fechaVenc.required = (valor === 'CREDITO');
        if (valor !== 'CREDITO') fechaVenc.value = '';
    }
}

//Cargar métodos de pago via AJAX
function cargarMetodosPago() {
 fetch(ctx + '/CompraServlet?action=obtenerCategorias') // reuse AJAX approach
 .catch(() => {});
 
 // Poblar desde los datos del servidor usando JSP scriptlet
 const select = document.getElementById('metodoPagoId');
 if (!select) return;
 // Los métodos de pago vienen del atributo de request "metodosPago"
 // Se generan como <option> desde JSP scriptlet abajo
}

</script>
</body>
</html>
