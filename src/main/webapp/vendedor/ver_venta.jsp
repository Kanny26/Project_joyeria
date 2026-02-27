<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="model.Venta" %>
<%@ page import="model.DetalleVenta" %>
<%@ page import="model.Usuario" %>
<%
    Object vendedorSesion = session.getAttribute("vendedor");
    if (vendedorSesion == null) {
        response.sendRedirect(request.getContextPath() + "/vendedor/inicio-sesion.jsp");
        return;
    }
    Venta venta = (Venta) request.getAttribute("venta");
    String exito = request.getParameter("exito");
    NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Detalle Venta | AAC27</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
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
    <% if (venta == null) { %>
        <div class="prov-alert prov-alert--error">No se encontró la venta.</div>
    <% } else { %>

    <% if ("abono".equals(exito)) { %>
        <div class="prov-alert prov-alert--success">
            <i class="fa-solid fa-circle-check"></i> Abono registrado correctamente.
        </div>
    <% } %>

    <div class="form-card">
        <div class="form-card__title">
            <i class="fa-solid fa-receipt"></i> Venta #<%= venta.getVentaId() %>
        </div>

        <%-- Info general --%>
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

        <%-- Abono (solo si hay saldo pendiente) --%>
        <% if ("anticipo".equals(venta.getModalidad()) && venta.getSaldoPendiente() != null
               && venta.getSaldoPendiente().compareTo(java.math.BigDecimal.ZERO) > 0) { %>
        <div id="abonar" class="section-title"><i class="fa-solid fa-hand-holding-dollar"></i> Registrar Abono</div>
        <form action="<%= request.getContextPath() %>/VentaVendedorServlet" method="POST"
              style="display:flex;gap:1rem;align-items:flex-end;flex-wrap:wrap;margin-bottom:1.5rem;">
            <input type="hidden" name="action" value="abonar">
            <input type="hidden" name="ventaId" value="<%= venta.getVentaId() %>">
            <div class="form-group">
                <label><i class="fa-solid fa-dollar-sign"></i> Monto a abonar</label>
                <input type="number" name="montoAbono" step="0.01" min="0.01"
                       max="<%= venta.getSaldoPendiente() %>"
                       placeholder="0.00" required style="width:200px;">
            </div>
            <button type="submit" class="btn-save">
                <i class="fa-solid fa-check"></i> Registrar abono
            </button>
        </form>
        <% } %>

        <%-- Acciones --%>
        <div class="form-actions">
            <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=misVentas" class="btn-cancel">
                <i class="fa-solid fa-arrow-left"></i> Volver
            </a>
            <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=registrarPostventa&ventaId=<%= venta.getVentaId() %>"
               class="btn-save">
                <i class="fa-solid fa-rotate-left"></i> Registrar Postventa
            </a>
        </div>
    </div>
    <% } %>
</main>
</body>
</html>
