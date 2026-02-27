<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="model.Venta" %>
<%@ page import="model.DetalleVenta" %>
<%
    Object vendedorSesion = session.getAttribute("vendedor");
    if (vendedorSesion == null) {
        response.sendRedirect(request.getContextPath() + "/vendedor/inicio-sesion.jsp");
        return;
    }
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

        <% if (error != null && !error.isEmpty()) { %>
            <div class="prov-alert prov-alert--error">
                <i class="fa-solid fa-circle-exclamation"></i> <%= error %>
            </div>
        <% } %>

        <div class="form-card__title">
            <i class="fa-solid fa-rotate-left"></i> Registrar Caso Postventa
        </div>

        <% if (venta != null) { %>
            <%-- Resumen de la venta --%>
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

            <%-- Productos de la venta --%>
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
            <form action="<%= request.getContextPath() %>/VentaVendedorServlet" method="POST">
                <input type="hidden" name="action" value="guardarPostventa">
                <input type="hidden" name="ventaId" value="<%= venta.getVentaId() %>">

                <div class="section-title"><i class="fa-solid fa-clipboard-list"></i> Datos del caso</div>

                <div class="form-row">
                    <div class="form-group">
                        <label><i class="fa-solid fa-tag"></i> Tipo de caso *</label>
                        <select name="tipo" required>
                            <option value="">-- Selecciona --</option>
                            <option value="cambio">🔄 Cambio de producto</option>
                            <option value="devolucion">↩️ Devolución</option>
                            <option value="reclamo">⚠️ Reclamo</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label><i class="fa-solid fa-hashtag"></i> Cantidad afectada *</label>
                        <input type="number" name="cantidad" min="1" required placeholder="Ej: 1">
                    </div>
                </div>

                <div class="form-group">
                    <label><i class="fa-solid fa-pen-to-square"></i> Motivo / Descripción *</label>
                    <textarea name="motivo" rows="4" required
                              placeholder="Describe detalladamente el motivo del caso..."
                              style="padding:.65rem .9rem;border:1.5px solid #e5e7eb;border-radius:10px;font-size:.9rem;color:#1e1b4b;outline:none;font-family:inherit;resize:vertical;width:100%;"></textarea>
                </div>

                <div class="form-actions">
                    <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=verVenta&id=<%= venta.getVentaId() %>"
                       class="btn-cancel">
                        <i class="fa-solid fa-xmark"></i> Cancelar
                    </a>
                    <button type="submit" class="btn-save">
                        <i class="fa-solid fa-paper-plane"></i> Registrar Caso
                    </button>
                </div>
            </form>
        <% } else { %>
            <div class="prov-alert prov-alert--error">No se encontró información de la venta.</div>
        <% } %>
    </div>
</main>
</body>
</html>
