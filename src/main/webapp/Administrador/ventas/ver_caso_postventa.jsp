<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="model.CasoPostventa" %>
<%@ page import="model.Venta" %>
<%@ page import="model.DetalleVenta" %>
<%
    Object adminSesion = session.getAttribute("admin");
    if (adminSesion == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }
    CasoPostventa caso = (CasoPostventa) request.getAttribute("caso");
    Venta venta         = (Venta) request.getAttribute("venta");
    String exito        = (String) request.getAttribute("exito");
    String error        = (String) request.getAttribute("error");
    NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Caso Postventa #<%= (caso != null) ? caso.getCasoId() : "" %> | Admin</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/ventas.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/AdminVentaServlet?action=listarPostventa">
        <i class="fa-solid fa-arrow-left navbar-admin__home-icon"></i>
    </a>
</nav>

<main class="prov-page">
<% if (caso == null) { %>
    <div class="alert alert--error">No se encontró el caso postventa.</div>
<% } else { %>

    <% if (exito != null) { %>
        <div class="alert alert--success"><i class="fa-solid fa-circle-check"></i> <%= exito %></div>
    <% } %>
    <% if (error != null) { %>
        <div class="alert alert--error"><i class="fa-solid fa-circle-xmark"></i> <%= error %></div>
    <% } %>

    <div class="form-card">
        <div class="form-card__title">
            <i class="fa-solid fa-rotate-left"></i>
            Caso Postventa #<%= caso.getCasoId() %>
            &nbsp;
            <%
                String est = caso.getEstado() != null ? caso.getEstado() : "en_proceso";
                if ("aprobado".equals(est)) { %><span class="badge badge--success">✅ Aprobado</span><%
                } else if ("cancelado".equals(est)) { %><span class="badge badge--danger">❌ Cancelado</span><%
                } else { %><span class="badge badge--warning">🕐 En proceso</span><% } %>
        </div>

        <%-- Datos del caso --%>
        <div class="info-grid">
            <div class="info-item">
                <span class="info-label">Vendedor</span>
                <span class="info-value"><%= caso.getVendedorNombre() %></span>
            </div>
            <div class="info-item">
                <span class="info-label">Cliente</span>
                <span class="info-value"><%= caso.getClienteNombre() %></span>
            </div>
            <div class="info-item">
                <span class="info-label">Venta asociada</span>
                <span class="info-value">#<%= caso.getVentaId() %></span>
            </div>
            <div class="info-item">
                <span class="info-label">Producto</span>
                <span class="info-value"><%= caso.getProductoNombre() != null ? caso.getProductoNombre() : "—" %></span>
            </div>
            <div class="info-item">
                <span class="info-label">Tipo</span>
                <span class="info-value">
                    <% if ("cambio".equals(caso.getTipo())) { %>
                        <span class="badge badge--info">🔄 Cambio</span>
                    <% } else if ("devolucion".equals(caso.getTipo())) { %>
                        <span class="badge badge--warning">↩️ Devolución</span>
                    <% } else { %>
                        <span class="badge badge--danger">⚠️ Reclamo</span>
                    <% } %>
                </span>
            </div>
            <div class="info-item">
                <span class="info-label">Cantidad</span>
                <span class="info-value"><%= caso.getCantidad() %></span>
            </div>
            <div class="info-item">
                <span class="info-label">Fecha</span>
                <span class="info-value"><%= caso.getFecha() != null ? sdf.format(caso.getFecha()) : "—" %></span>
            </div>
            <% if (caso.getMotivo() != null && !caso.getMotivo().isBlank()) { %>
            <div class="info-item" style="grid-column:1/-1;">
                <span class="info-label">Motivo</span>
                <span class="info-value"><%= caso.getMotivo() %></span>
            </div>
            <% } %>
        </div>

        <%-- Venta asociada --%>
        <% if (venta != null) { %>
        <div class="section-title"><i class="fa-solid fa-receipt"></i> Venta asociada</div>
        <div class="info-grid">
            <div class="info-item">
                <span class="info-label">Total</span>
                <span class="info-value"><%= moneda.format(venta.getTotal()) %></span>
            </div>
            <div class="info-item">
                <span class="info-label">Método</span>
                <span class="info-value"><%= "efectivo".equals(venta.getMetodoPago()) ? "💵 Efectivo" : "💳 Transferencia" %></span>
            </div>
            <div class="info-item">
                <span class="info-label">Fecha venta</span>
                <span class="info-value"><%= venta.getFechaEmision() != null ? sdf.format(venta.getFechaEmision()) : "—" %></span>
            </div>
        </div>

        <% List<DetalleVenta> detalles = venta.getDetalles();
           if (detalles != null && !detalles.isEmpty()) { %>
        <table class="productos-table">
            <thead>
                <tr><th>Producto</th><th>Cant.</th><th>Precio unit.</th><th>Subtotal</th></tr>
            </thead>
            <tbody>
                <% for (DetalleVenta d : detalles) { %>
                <tr>
                    <td><%= d.getProductoNombre() %></td>
                    <td><%= d.getCantidad() %></td>
                    <td><%= moneda.format(d.getPrecioUnitario()) %></td>
                    <td><%= moneda.format(d.getSubtotal()) %></td>
                </tr>
                <% } %>
            </tbody>
        </table>
        <% } %>
        <% } %>

        <%-- Gestión del caso (solo si está en proceso) --%>
        <% if ("en_proceso".equals(est) || est == null) { %>
        <div class="section-title"><i class="fa-solid fa-gear"></i> Gestionar caso</div>
        <form method="post" action="<%= request.getContextPath() %>/AdminVentaServlet">
            <input type="hidden" name="action" value="gestionarCaso">
            <input type="hidden" name="casoId" value="<%= caso.getCasoId() %>">
            <div class="form-group">
                <label class="form-label">Nuevo estado</label>
                <select name="nuevoEstado" class="form-control" required>
                    <option value="">-- Seleccionar --</option>
                    <option value="aprobado">✅ Aprobar</option>
                    <option value="cancelado">❌ Cancelar</option>
                </select>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn-save">
                    <i class="fa-solid fa-floppy-disk"></i> Guardar decisión
                </button>
                <a href="<%= request.getContextPath() %>/AdminVentaServlet?action=listarPostventa" class="btn-cancel">
                    Volver
                </a>
            </div>
        </form>
        <% } else { %>
        <div class="form-actions">
            <a href="<%= request.getContextPath() %>/AdminVentaServlet?action=listarPostventa" class="btn-cancel">
                <i class="fa-solid fa-arrow-left"></i> Volver al listado
            </a>
        </div>
        <% } %>
    </div>
<% } %>
</main>
</body>
</html>
