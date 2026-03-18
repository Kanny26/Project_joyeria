<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="model.Venta" %>
<%@ page import="model.DetalleVenta" %>
<%@ page import="model.Usuario" %>
<%
    /*
     * Control de sesión.
     */
    Object vendedorSesion = session.getAttribute("vendedor");
    if (vendedorSesion == null) {
        response.sendRedirect(request.getContextPath() + "/vendedor/inicio-sesion.jsp");
        return;
    }

    /*
     * La venta llega como atributo del request cuando el servlet hace forward.
     * El parámetro "exito" llega en la URL cuando hay una redirección tras una operación exitosa.
     * Por ejemplo, tras registrar un abono: ?exito=abono
     */
    Venta venta          = (Venta) request.getAttribute("venta");
    String exito         = request.getParameter("exito");
    String mensajeError  = (String) request.getAttribute("error");
    NumberFormat moneda  = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Detalle Venta | AAC27</title>
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
    <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=misVentas" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Mis ventas</span>
        </span>
    </a>
</nav>

<main class="prov-page">

    <%-- Si la venta no existe o no se pudo cargar --%>
    <% if (venta == null) { %>
        <div class="prov-alert prov-alert--error">
            <i class="fa-solid fa-circle-exclamation"></i>
            No se encontró la venta solicitada.
        </div>
        <div style="margin-top:1rem;">
            <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=misVentas" class="btn-cancel">
                <i class="fa-solid fa-arrow-left"></i> Volver a mis ventas
            </a>
        </div>
    <% } else { %>

    <%-- Mensajes de retroalimentación --%>
    <% if (mensajeError != null && !mensajeError.isEmpty()) { %>
        <div class="prov-alert prov-alert--error" id="alertaMensaje">
            <i class="fa-solid fa-circle-exclamation"></i> <%= mensajeError %>
        </div>
    <% } %>

    <%--
        El parámetro "exito" llega en la URL tras sendRedirect.
        "abono" indica que se acaba de registrar un abono exitosamente.
    --%>
    <% if ("abono".equals(exito)) { %>
        <div class="prov-alert prov-alert--success" id="alertaMensaje">
            <i class="fa-solid fa-circle-check"></i> El abono fue registrado correctamente.
        </div>
    <% } else if (exito != null && !exito.isEmpty()) { %>
        <div class="prov-alert prov-alert--success" id="alertaMensaje">
            <i class="fa-solid fa-circle-check"></i> Operación realizada con éxito.
        </div>
    <% } %>

    <div class="form-card">
        <div class="form-card__title">
            <i class="fa-solid fa-receipt"></i> Venta #<%= venta.getVentaId() %>
        </div>

        <%-- Información general de la venta --%>
        <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:1rem;margin-bottom:1.5rem;padding:1rem;background:#f5f3ff;border-radius:12px;">
            <div>
                <div style="font-size:11px;font-weight:700;color:#7c3aed;text-transform:uppercase;">Cliente</div>
                <div style="font-weight:800;color:#1e1b4b;"><%= venta.getClienteNombre() %></div>
            </div>
            <div>
                <div style="font-size:11px;font-weight:700;color:#7c3aed;text-transform:uppercase;">Fecha</div>
                <div style="font-weight:700;color:#1e1b4b;">
                    <%= (venta.getFechaEmision() != null) ? sdf.format(venta.getFechaEmision()) : "—" %>
                </div>
            </div>
            <div>
                <div style="font-size:11px;font-weight:700;color:#7c3aed;text-transform:uppercase;">Método de pago</div>
                <div style="font-weight:700;color:#1e1b4b;"><%= venta.getMetodoPago() %></div>
            </div>
            <div>
                <div style="font-size:11px;font-weight:700;color:#7c3aed;text-transform:uppercase;">Modalidad</div>
                <div style="font-weight:700;color:#1e1b4b;">
                    <%= "anticipo".equals(venta.getModalidad()) ? "Anticipo" : "Contado" %>
                </div>
            </div>
            <div>
                <div style="font-size:11px;font-weight:700;color:#059669;text-transform:uppercase;">Total</div>
                <div style="font-weight:800;font-size:1.2rem;color:#059669;"><%= moneda.format(venta.getTotal()) %></div>
            </div>
            <% if ("anticipo".equals(venta.getModalidad()) && venta.getMontoAnticipo() != null) { %>
            <div>
                <div style="font-size:11px;font-weight:700;color:#ca8a04;text-transform:uppercase;">Anticipo cobrado</div>
                <div style="font-weight:800;color:#92400e;"><%= moneda.format(venta.getMontoAnticipo()) %></div>
            </div>
            <% } %>
            <% if ("anticipo".equals(venta.getModalidad()) && venta.getSaldoPendiente() != null
                   && venta.getSaldoPendiente().compareTo(java.math.BigDecimal.ZERO) > 0) { %>
            <div style="background:#fee2e2;border-radius:8px;padding:.5rem;">
                <div style="font-size:11px;font-weight:700;color:#dc2626;text-transform:uppercase;">Saldo pendiente</div>
                <div style="font-weight:800;color:#991b1b;"><%= moneda.format(venta.getSaldoPendiente()) %></div>
            </div>
            <% } %>
        </div>

        <%-- Detalle de productos --%>
        <div class="section-title"><i class="fa-solid fa-boxes-stacked"></i> Productos vendidos</div>
        <table class="productos-table" style="margin-bottom:1.5rem;">
            <thead>
                <tr>
                    <th>Producto</th>
                    <th>Precio unit.</th>
                    <th>Cantidad</th>
                    <th>Subtotal</th>
                </tr>
            </thead>
            <tbody>
                <%
                    List<DetalleVenta> detalles = venta.getDetalles();
                    if (detalles != null) {
                        for (DetalleVenta d : detalles) {
                %>
                <tr>
                    <td style="font-weight:600;"><%= d.getProductoNombre() %></td>
                    <td><%= moneda.format(d.getPrecioUnitario()) %></td>
                    <td><%= d.getCantidad() %></td>
                    <td style="font-weight:700;color:#059669;"><%= moneda.format(d.getSubtotal()) %></td>
                </tr>
                <% } } %>
            </tbody>
        </table>

        <%-- Formulario de abono: solo se muestra si hay saldo pendiente --%>
        <% if ("anticipo".equals(venta.getModalidad()) && venta.getSaldoPendiente() != null
               && venta.getSaldoPendiente().compareTo(java.math.BigDecimal.ZERO) > 0) { %>
        <div id="abonar" class="section-title"><i class="fa-solid fa-hand-holding-dollar"></i> Registrar Abono</div>
        <form id="formAbono" action="<%= request.getContextPath() %>/VentaVendedorServlet" method="POST"
              style="display:flex;gap:1rem;align-items:flex-end;flex-wrap:wrap;margin-bottom:1.5rem;" novalidate>
            <input type="hidden" name="action" value="abonar">
            <input type="hidden" name="ventaId" value="<%= venta.getVentaId() %>">
            <div class="form-group">
                <label><i class="fa-solid fa-dollar-sign"></i> Monto a abonar</label>
                <input type="number" id="montoAbono" name="montoAbono" step="0.01" min="0.01"
                       max="<%= venta.getSaldoPendiente() %>"
                       placeholder="0.00" required style="width:200px;">
            </div>
            <%-- El botón llama a la función JS que muestra confirmación antes de enviar --%>
            <button type="button" class="btn-save" onclick="confirmarAbono()">
                <i class="fa-solid fa-check"></i> Registrar abono
            </button>
        </form>
        <% } %>

        <%-- Acciones principales --%>
        <div class="form-actions">
            <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=misVentas" class="btn-cancel">
                <i class="fa-solid fa-arrow-left"></i> Volver
            </a>
            <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=descargarFactura&id=<%= venta.getVentaId() %>"
               class="btn-save" style="background-color:#7c3aed;">
                <i class="fa-solid fa-file-pdf"></i> Descargar Factura
            </a>
            <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=registrarPostventa&ventaId=<%= venta.getVentaId() %>"
               class="btn-save" id="btnPostventa">
                <i class="fa-solid fa-rotate-left"></i> Registrar Postventa
            </a>
        </div>
    </div>
    <% } %>
</main>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
// Oculta alertas automáticamente después de 5 segundos
(function() {
    var alerta = document.getElementById('alertaMensaje');
    if (alerta) {
        setTimeout(function() {
            alerta.style.transition = 'opacity 0.5s';
            alerta.style.opacity = '0';
            setTimeout(function() { alerta.style.display = 'none'; }, 500);
        }, 5000);
    }
})();

/**
 * Muestra un diálogo de confirmación antes de registrar el abono.
 * Valida que se haya ingresado un monto válido antes de mostrar la confirmación.
 */
function confirmarAbono() {
    var montoInput = document.getElementById('montoAbono');
    var monto      = parseFloat(montoInput.value);
    var saldoMax   = parseFloat(montoInput.getAttribute('max'));

    if (!monto || monto <= 0) {
        Swal.fire({
            title: 'Monto inválido',
            text: 'Debes ingresar un monto mayor a $0 para el abono.',
            icon: 'warning',
            confirmButtonText: 'Entendido',
            confirmButtonColor: '#7c3aed'
        });
        return;
    }
    if (monto > saldoMax) {
        Swal.fire({
            title: 'Monto excede el saldo',
            html: 'El abono no puede ser mayor al saldo pendiente de <b>$' + saldoMax.toLocaleString('es-CO') + '</b>.',
            icon: 'warning',
            confirmButtonText: 'Entendido',
            confirmButtonColor: '#7c3aed'
        });
        return;
    }

    Swal.fire({
        title: '¿Registrar este abono?',
        html: 'Monto del abono: <b>$' + monto.toLocaleString('es-CO', {minimumFractionDigits:2}) + '</b><br>' +
              'Saldo restante: <b>$' + (saldoMax - monto).toLocaleString('es-CO', {minimumFractionDigits:2}) + '</b>',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Sí, registrar',
        cancelButtonText: 'Cancelar',
        confirmButtonColor: '#059669',
        cancelButtonColor: '#6b7280'
    }).then(function(result) {
        if (result.isConfirmed) {
            // Muestra estado de carga mientras se procesa
            Swal.fire({
                title: 'Registrando abono...',
                text: 'Por favor espera.',
                allowOutsideClick: false,
                didOpen: function() { Swal.showLoading(); }
            });
            document.getElementById('formAbono').submit();
        }
    });
}
</script>
</body>
</html>
