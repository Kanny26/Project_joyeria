<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="model.Venta" %>
<%@ page import="model.DetalleVenta" %>
<%
    /*
     * Control de sesión: si el vendedor no está logueado, redirige al login.
     */
    Object vendedorSesion = session.getAttribute("vendedor");
    if (vendedorSesion == null) {
        response.sendRedirect(request.getContextPath() + "/vendedor/inicio-sesion.jsp");
        return;
    }

    /*
     * La venta llega desde el servlet cuando el vendedor hace clic en
     * "Registrar Postventa" desde la vista de una venta.
     * Si no llega (acceso directo al JSP), se mostrará un mensaje de error.
     */
    Venta venta = (Venta) request.getAttribute("venta");
    String error = (String) request.getAttribute("error");
    NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registrar Postventa | AAC27</title>
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
    <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=verVenta&id=<%= (venta != null) ? venta.getVentaId() : "" %>"
       class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver a la venta</span>
        </span>
    </a>
</nav>

<main class="prov-page">
    <div class="form-card">

        <%-- Mensaje de error del servidor (validación backend) --%>
        <% if (error != null && !error.isEmpty()) { %>
            <div class="prov-alert prov-alert--error" id="alertaError">
                <i class="fa-solid fa-circle-exclamation"></i> <%= error %>
            </div>
        <% } %>

        <div class="form-card__title">
            <i class="fa-solid fa-rotate-left"></i> Registrar Caso Postventa
        </div>

        <% if (venta != null) { %>
            <%-- Resumen de la venta relacionada --%>
            <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:1rem;margin-bottom:1.5rem;padding:1rem;background:#f5f3ff;border-radius:12px;">
                <div>
                    <div style="font-size:11px;font-weight:700;color:#7c3aed;text-transform:uppercase;">Venta #</div>
                    <div style="font-weight:800;color:#1e1b4b;"><%= venta.getVentaId() %></div>
                </div>
                <div>
                    <div style="font-size:11px;font-weight:700;color:#7c3aed;text-transform:uppercase;">Cliente</div>
                    <div style="font-weight:700;color:#1e1b4b;"><%= venta.getClienteNombre() %></div>
                </div>
                <div>
                    <div style="font-size:11px;font-weight:700;color:#7c3aed;text-transform:uppercase;">Fecha</div>
                    <div style="font-weight:700;color:#1e1b4b;">
                        <%= (venta.getFechaEmision() != null) ? sdf.format(venta.getFechaEmision()) : "" %>
                    </div>
                </div>
                <div>
                    <div style="font-size:11px;font-weight:700;color:#7c3aed;text-transform:uppercase;">Total</div>
                    <div style="font-weight:800;color:#059669;"><%= moneda.format(venta.getTotal()) %></div>
                </div>
            </div>

            <%-- Tabla de productos de la venta para referencia del vendedor --%>
            <div class="section-title"><i class="fa-solid fa-boxes-stacked"></i> Productos de la venta</div>
            <table class="productos-table" style="margin-bottom:1.5rem;">
                <thead>
                    <tr>
                        <th>Producto</th>
                        <th>Cantidad</th>
                        <th>Precio unit.</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        List<DetalleVenta> detalles = venta.getDetalles();
                        if (detalles != null) {
                            for (DetalleVenta d : detalles) {
                    %>
                        <tr>
                            <td><%= d.getProductoNombre() %></td>
                            <td><%= d.getCantidad() %></td>
                            <td><%= moneda.format(d.getPrecioUnitario()) %></td>
                        </tr>
                    <% } } %>
                </tbody>
            </table>

            <%-- Formulario del caso --%>
            <form id="formPostventa" action="<%= request.getContextPath() %>/VentaVendedorServlet" method="POST" novalidate>
                <input type="hidden" name="action" value="guardarPostventa">
                <input type="hidden" name="ventaId" value="<%= venta.getVentaId() %>">

                <div class="section-title"><i class="fa-solid fa-clipboard-list"></i> Datos del caso</div>

                <div class="form-row">
                    <div class="form-group">
                        <label><i class="fa-solid fa-tag"></i> Tipo de caso *</label>
                        <select name="tipo" id="tipoCaso" required>
                            <option value="">-- Selecciona un tipo --</option>
                            <option value="cambio">🔄 Cambio de producto</option>
                            <option value="devolucion">↩️ Devolución</option>
                            <option value="reclamo">⚠️ Reclamo</option>
                        </select>
                        <div id="msgTipo" style="font-size:.75rem;color:#ef4444;margin-top:3px;min-height:16px;"></div>
                    </div>
                    <div class="form-group">
                        <label><i class="fa-solid fa-hashtag"></i> Cantidad afectada *</label>
                        <input type="number" name="cantidad" id="cantidadCaso" min="1" required placeholder="Ej: 1">
                        <div id="msgCantidad" style="font-size:.75rem;color:#ef4444;margin-top:3px;min-height:16px;"></div>
                    </div>
                </div>

                <div class="form-group">
                    <label><i class="fa-solid fa-pen-to-square"></i> Motivo / Descripción *</label>
                    <textarea name="motivo" id="motivoCaso" rows="4" required
                              placeholder="Describe detalladamente el motivo del caso..."
                              style="padding:.65rem .9rem;border:1.5px solid #e5e7eb;border-radius:10px;font-size:.9rem;color:#1e1b4b;outline:none;font-family:inherit;resize:vertical;width:100%;"></textarea>
                    <div id="msgMotivo" style="font-size:.75rem;color:#ef4444;margin-top:3px;min-height:16px;"></div>
                </div>

                <div class="form-actions">
                    <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=verVenta&id=<%= venta.getVentaId() %>"
                       class="btn-cancel">
                        <i class="fa-solid fa-xmark"></i> Cancelar
                    </a>
                    <%-- El botón llama a la función JS que muestra confirmación antes de enviar --%>
                    <button type="button" class="btn-save" onclick="confirmarEnvioPostventa()">
                        <i class="fa-solid fa-paper-plane"></i> Registrar Caso
                    </button>
                </div>
            </form>
        <% } else { %>
            <div class="prov-alert prov-alert--error">
                <i class="fa-solid fa-circle-exclamation"></i>
                No se encontró la información de la venta. Por favor, regresa e intenta nuevamente.
            </div>
            <div style="margin-top:1rem;">
                <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=misVentas" class="btn-cancel">
                    <i class="fa-solid fa-arrow-left"></i> Volver a mis ventas
                </a>
            </div>
        <% } %>
    </div>
</main>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
// Oculta la alerta de error del servidor después de 6 segundos
(function() {
    var alerta = document.getElementById('alertaError');
    if (alerta) {
        setTimeout(function() {
            alerta.style.transition = 'opacity 0.5s';
            alerta.style.opacity = '0';
            setTimeout(function() { alerta.style.display = 'none'; }, 500);
        }, 6000);
    }
})();

/**
 * Valida el formulario y muestra un diálogo de confirmación antes de enviar.
 * Si hay errores, los muestra sin enviar el formulario.
 */
function confirmarEnvioPostventa() {
    var tipo     = document.getElementById('tipoCaso').value;
    var cantidad = document.getElementById('cantidadCaso').value;
    var motivo   = document.getElementById('motivoCaso').value.trim();
    var ok       = true;

    // Limpiar mensajes anteriores
    document.getElementById('msgTipo').textContent     = '';
    document.getElementById('msgCantidad').textContent = '';
    document.getElementById('msgMotivo').textContent   = '';

    if (!tipo) {
        document.getElementById('msgTipo').textContent = 'Selecciona el tipo de caso.';
        ok = false;
    }
    if (!cantidad || parseInt(cantidad) < 1) {
        document.getElementById('msgCantidad').textContent = 'Ingresa una cantidad válida (mínimo 1).';
        ok = false;
    }
    if (!motivo) {
        document.getElementById('msgMotivo').textContent = 'El motivo es obligatorio.';
        ok = false;
    }

    if (!ok) {
        // Mostrar resumen de errores con SweetAlert2
        Swal.fire({
            title: 'Revisa el formulario',
            text: 'Hay campos obligatorios sin completar.',
            icon: 'warning',
            confirmButtonText: 'Entendido',
            confirmButtonColor: '#7c3aed'
        });
        return;
    }

    // Obtener el nombre del tipo para el mensaje de confirmación
    var tiposTexto = { cambio: 'Cambio de producto', devolucion: 'Devolución', reclamo: 'Reclamo' };
    var tipoTexto = tiposTexto[tipo] || tipo;

    // Mostrar diálogo de confirmación antes de enviar el formulario
    Swal.fire({
        title: '¿Registrar este caso?',
        html: '<p style="text-align:left;margin:0;">' +
              '<b>Tipo:</b> ' + tipoTexto + '<br>' +
              '<b>Cantidad:</b> ' + cantidad + ' unidad(es)<br>' +
              '<b>Motivo:</b> ' + motivo.substring(0, 80) + (motivo.length > 80 ? '...' : '') +
              '</p><p style="color:#6b7280;font-size:13px;margin-top:8px;">' +
              'El caso quedará en estado "En proceso" hasta que el administrador lo revise.</p>',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: '<i class="fa-solid fa-paper-plane"></i> Sí, registrar',
        cancelButtonText: 'Revisar antes',
        confirmButtonColor: '#7c3aed',
        cancelButtonColor: '#6b7280'
    }).then(function(result) {
        if (result.isConfirmed) {
            // Mostrar estado de carga mientras se procesa
            Swal.fire({
                title: 'Procesando caso...',
                text: 'Por favor espera un momento.',
                allowOutsideClick: false,
                didOpen: function() { Swal.showLoading(); }
            });
            document.getElementById('formPostventa').submit();
        }
    });
}
</script>
</body>
</html>
