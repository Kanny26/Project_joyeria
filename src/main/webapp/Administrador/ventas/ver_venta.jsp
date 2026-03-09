<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="model.Venta" %>
<%@ page import="model.DetalleVenta" %>
<%
    Object adminSesion = session.getAttribute("admin");
    if (adminSesion == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }

    Venta venta = (Venta) request.getAttribute("venta");
    if (venta == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/ventas/listar");
        return;
    }

    NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    List<DetalleVenta> detalles = venta.getDetalles();
    if (detalles == null) detalles = java.util.Collections.emptyList();

    int totalUnidades = 0;
    for (DetalleVenta d : detalles) {
        if (d != null) totalUnidades += d.getCantidad();
    }

    // Estado
    String estadoClass = "estado-pill--pendiente";
    String estadoLabel = "Pendiente";
    String estadoIcon  = "fa-clock";
    if ("confirmado".equals(venta.getEstado())) {
        estadoClass = "estado-pill--pagado";
        estadoLabel = "Pagado";
        estadoIcon  = "fa-circle-check";
    } else if ("rechazado".equals(venta.getEstado())) {
        estadoClass = "estado-pill--rechazado";
        estadoLabel = "Rechazado";
        estadoIcon  = "fa-ban";
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Venta #<%= venta.getVentaId() %> | Admin AAC27</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/ventas/ver_ventas.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/Administrador/ventas/listar" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<main class="prov-page">
<div class="detalle-wrapper">

    <%-- ── META ── --%>
    <div class="venta-meta">
        <div class="venta-meta__top">
            <div class="venta-meta__id">
                <div class="venta-meta__icon">
                    <i class="fa-solid fa-file-invoice-dollar"></i>
                </div>
                <div>
                    <div class="venta-meta__titulo">Venta #<%= venta.getVentaId() %></div>
                    <div class="venta-meta__sub">Factura de venta registrada</div>
                </div>
            </div>
            <div class="venta-meta__badges">
                <span class="badge badge--fecha">
                    <i class="fa-regular fa-calendar"></i>
                    <%= venta.getFechaEmision() != null ? sdf.format(venta.getFechaEmision()) : "—" %>
                </span>
                <span class="badge badge--metodo">
                    <%= "efectivo".equals(venta.getMetodoPago()) ? "💵 Efectivo" : "💳 Tarjeta" %>
                </span>
                <span class="badge badge--vendedor">
                    <i class="fa-solid fa-user-tie"></i>
                    <%= venta.getVendedorNombre() != null ? venta.getVendedorNombre() : "—" %>
                </span>
                <span class="estado-pill <%= estadoClass %>">
                    <i class="fa-solid <%= estadoIcon %>"></i>
                    <%= estadoLabel %>
                </span>
            </div>
        </div>

        <%-- Grid stats --%>
        <div class="venta-meta__grid">
            <div class="meta-item">
                <div class="meta-item__label"><i class="fa-solid fa-hashtag"></i> ID</div>
                <div class="meta-item__value">#<%= venta.getVentaId() %></div>
            </div>
            <div class="meta-item">
                <div class="meta-item__label"><i class="fa-solid fa-user"></i> Cliente</div>
                <div class="meta-item__value"><%= venta.getClienteNombre() != null ? venta.getClienteNombre() : "N/A" %></div>
            </div>
            <div class="meta-item">
                <div class="meta-item__label"><i class="fa-solid fa-boxes-stacked"></i> Productos</div>
                <div class="meta-item__value"><%= detalles.size() %> ítems</div>
            </div>
            <div class="meta-item">
                <div class="meta-item__label"><i class="fa-solid fa-cubes"></i> Unidades</div>
                <div class="meta-item__value"><%= totalUnidades %></div>
            </div>
            <div class="meta-item">
                <div class="meta-item__label"><i class="fa-solid fa-dollar-sign"></i> Total</div>
                <div class="meta-item__value meta-item__value--money">
                    <%= moneda.format(venta.getTotal() != null ? venta.getTotal() : 0) %>
                </div>
            </div>
            <div class="meta-item">
                <div class="meta-item__label"><i class="fa-solid fa-credit-card"></i> Modalidad</div>
                <div class="meta-item__value"><%= venta.getModalidad() != null ? venta.getModalidad() : "—" %></div>
            </div>
        </div>
    </div>

    <%-- ── ANTICIPO / SALDO ── --%>
    <% if ("anticipo".equals(venta.getModalidad()) && venta.getMontoAnticipo() != null) { %>
    <div class="pago-resumen">
        <div class="pago-chip pago-chip--anticipo">
            <span class="pago-chip__label">Anticipo pagado</span>
            <span class="pago-chip__value"><%= moneda.format(venta.getMontoAnticipo()) %></span>
        </div>
        <% if (venta.getSaldoPendiente() != null && venta.getSaldoPendiente().compareTo(java.math.BigDecimal.ZERO) > 0) { %>
        <div class="pago-chip pago-chip--saldo">
            <span class="pago-chip__label">Saldo pendiente</span>
            <span class="pago-chip__value"><%= moneda.format(venta.getSaldoPendiente()) %></span>
        </div>
        <% } %>
    </div>
    <% } %>

    <%-- ── TABLA DETALLES ── --%>
    <div class="detalles-card">
        <div class="detalles-card__header">
            <i class="fa-solid fa-list"></i>
            Detalle de productos
            <span class="detalles-count"><%= detalles.size() %></span>
        </div>

        <% if (detalles.isEmpty()) { %>
            <div class="empty-detalle">
                <i class="fa-solid fa-box-open"></i>
                No hay productos en esta venta
            </div>
        <% } else { %>
        <table class="detalles-table">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Producto</th>
                    <th>Precio unitario</th>
                    <th>Cant.</th>
                    <th>Subtotal</th>
                </tr>
            </thead>
            <tbody>
                <% int rowNum = 1;
                   for (DetalleVenta d : detalles) {
                       if (d == null) continue; %>
                <tr>
                    <td style="color:#9ca3af;font-weight:600;width:3rem;"><%= rowNum++ %></td>
                    <td>
                        <div class="prod-name"><%= d.getProductoNombre() != null ? d.getProductoNombre() : "Producto eliminado" %></div>
                        <div class="prod-id">ID #<%= d.getProductoId() %></div>
                    </td>
                    <td class="precio-cell">
                        <%= moneda.format(d.getPrecioUnitario() != null ? d.getPrecioUnitario() : 0) %>
                    </td>
                    <td>
                        <span class="cant-badge">×<%= d.getCantidad() %></span>
                    </td>
                    <td class="subtotal-cell-right">
                        <%= moneda.format(d.getSubtotal() != null ? d.getSubtotal() : 0) %>
                    </td>
                </tr>
                <% } %>
            </tbody>
            <tfoot>
                <tr>
                    <td colspan="4" class="total-label">
                        <i class="fa-solid fa-calculator" style="color:#7c3aed;margin-right:.3rem;"></i>
                        Total general
                    </td>
                    <td class="total-value">
                        <%= moneda.format(venta.getTotal() != null ? venta.getTotal() : 0) %>
                    </td>
                </tr>
            </tfoot>
        </table>
        <% } %>
    </div>

    <%-- ── CONDICIONES ── --%>
    <div class="condiciones-card">
        <h3><i class="fa-solid fa-file-contract"></i> Términos y condiciones</h3>
        <p>Gracias por su compra. Esta factura corresponde a los servicios prestados y debe conservarse como comprobante de pago. Abby.accesorios — Tu lugar favorito.</p>
    </div>

    <%-- ── ACCIONES ── --%>
    <div class="acciones-bar">
        <a href="<%= request.getContextPath() %>/Administrador/ventas/listar" class="btn-volver">
            <i class="fa-solid fa-arrow-left"></i> Volver a ventas
        </a>
        <div class="acciones-group">
            <button class="btn-print" onclick="descargarPDF()">
                <i class="fa-solid fa-file-pdf"></i> Descargar PDF
            </button>
        </div>
    </div>

</div>
</main>

<script src="https://cdnjs.cloudflare.com/ajax/libs/html2pdf.js/0.10.1/html2pdf.bundle.min.js"></script>
<script>
function descargarPDF() {
    const elemento = document.querySelector('.detalle-wrapper');
    const numVenta = "<%= venta.getVentaId() %>";
    const opciones = {
        margin:      [10, 10, 10, 10],
        filename:    `Venta_${numVenta}.pdf`,
        image:       { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2, useCORS: true },
        jsPDF:       { unit: 'mm', format: 'a4', orientation: 'portrait' }
    };
    const acciones = document.querySelector('.acciones-bar');
    acciones.style.display = 'none';
    html2pdf().set(opciones).from(elemento).save().then(() => {
        acciones.style.display = 'flex';
    });
}
</script>

</body>
</html>
