<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="model.Venta" %>
<%@ page import="model.Usuario" %>
<%
    Object vendedorSesion = session.getAttribute("vendedor");
    if (vendedorSesion == null) {
        response.sendRedirect(request.getContextPath() + "/vendedor/inicio-sesion.jsp");
        return;
    }
    Usuario usuario = (Usuario) vendedorSesion;
    List<Venta> ventas = (List<Venta>) request.getAttribute("ventas");
    String exito = request.getParameter("exito");
    NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mis Ventas | AAC27</title>
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
    <span style="font-size:14px;font-weight:600;color:#fff;opacity:.85;">
        <i class="fa-solid fa-user-circle"></i> <%= usuario.getNombre() %>
    </span>
    <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=nueva" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-plus"></i>
            <span class="navbar-admin__home-text">Nueva venta</span>
        </span>
    </a>
</nav>

<main class="prov-page">
    <h1 class="prov-page__titulo">Mis Ventas</h1>

    <%-- Estadísticas rápidas --%>
    <%
        int totalVentas = ventas != null ? ventas.size() : 0;
        int conSaldo = 0;
        java.math.BigDecimal totalIngresado = java.math.BigDecimal.ZERO;
        if (ventas != null) {
            for (Venta v : ventas) {
                if (v.getTotal() != null) totalIngresado = totalIngresado.add(v.getTotal());
                if ("anticipo".equals(v.getModalidad()) && v.getSaldoPendiente() != null
                    && v.getSaldoPendiente().compareTo(java.math.BigDecimal.ZERO) > 0) conSaldo++;
            }
        }
    %>
    <div class="stat-grid">
        <div class="stat-card">
            <div class="stat-card__label">Total ventas</div>
            <div class="stat-card__value"><%= totalVentas %></div>
        </div>
        <div class="stat-card">
            <div class="stat-card__label">Ingresos</div>
            <div class="stat-card__value stat-card__value--money"><%= moneda.format(totalIngresado) %></div>
        </div>
        <div class="stat-card">
            <div class="stat-card__label">Con saldo pendiente</div>
            <div class="stat-card__value" style="color:<%= conSaldo > 0 ? "#ef4444" : "#059669" %>;"><%= conSaldo %></div>
        </div>
    </div>

    <%-- Toolbar --%>
    <div class="prov-toolbar">
        <div class="cards__busqueda">
            <input type="text" class="cards__busqueda-input" id="buscador"
                   placeholder="Buscar por cliente, #venta..."
                   oninput="filtrar()" style="width:320px;">
            <i class="fa-solid fa-magnifying-glass cards__busqueda-icono"></i>
        </div>
        <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=misCasos"
           class="prov-toolbar__btn-nuevo">
            <i class="fa-solid fa-rotate-left"></i> Mis casos postventa
        </a>
    </div>

    <% if (exito != null) { %>
        <div class="prov-alert prov-alert--success">
            <i class="fa-solid fa-circle-check"></i> Operación realizada con éxito.
        </div>
    <% } %>

    <% if (ventas == null || ventas.isEmpty()) { %>
        <div class="prov-empty">
            <div class="prov-empty__icon"><i class="fa-solid fa-inbox"></i></div>
            <p class="prov-empty__texto">Aún no has registrado ventas.</p>
            <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=nueva" class="btn-save" style="margin-top:1rem;">
                Registrar primera venta
            </a>
        </div>
    <% } else { %>
        <div style="width:100%;overflow-x:auto;">
            <table class="productos-table" id="tablaVentas" style="min-width:800px;">
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Cliente</th>
                        <th>Fecha</th>
                        <th>Total</th>
                        <th>Modalidad</th>
                        <th>Método</th>
                        <th>Estado pago</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Venta v : ventas) {
                        java.math.BigDecimal saldo = v.getSaldoPendiente();
                    %>
                    <tr class="fila-tabla">
                        <td><span style="background:#7c3aed;color:#fff;padding:2px 8px;border-radius:20px;font-size:12px;font-weight:700;">#<%= v.getVentaId() %></span></td>
                        <td style="font-weight:600;"><%= v.getClienteNombre() %></td>
                        <td><%= (v.getFechaEmision() != null) ? sdf.format(v.getFechaEmision()) : "" %></td>
                        <td style="font-weight:700;color:#059669;"><%= moneda.format(v.getTotal()) %></td>
                        <td>
                            <% if ("anticipo".equals(v.getModalidad())) { %>
                                <span style="background:#fef9c3;color:#92400e;padding:3px 10px;border-radius:20px;font-size:12px;font-weight:600;">Anticipo</span>
                            <% } else { %>
                                <span style="background:#e0f2fe;color:#0369a1;padding:3px 10px;border-radius:20px;font-size:12px;font-weight:600;">Contado</span>
                            <% } %>
                        </td>
                        <td>
                            <% if ("Efectivo".equalsIgnoreCase(v.getMetodoPago())) { %>
                                💵 Efectivo
                            <% } else { %>
                                💳 Transferencia
                            <% } %>
                        </td>
                        <td>
                            <% if ("anticipo".equals(v.getModalidad()) && saldo != null && saldo.compareTo(java.math.BigDecimal.ZERO) > 0) { %>
                                <span style="background:#fee2e2;color:#991b1b;padding:3px 10px;border-radius:20px;font-size:12px;font-weight:600;">
                                    Saldo: <%= moneda.format(saldo) %>
                                </span>
                            <% } else { %>
                                <span style="background:#dcfce7;color:#166534;padding:3px 10px;border-radius:20px;font-size:12px;font-weight:600;">Pagado</span>
                            <% } %>
                        </td>
                        <td style="display:flex;gap:6px;align-items:center;">
                            <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=verVenta&id=<%= v.getVentaId() %>"
                               class="btn-save" style="padding:.4rem .8rem;font-size:.78rem;" title="Ver detalle">
                                <i class="fa-solid fa-eye"></i>
                            </a>
                            <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=registrarPostventa&ventaId=<%= v.getVentaId() %>"
                               class="btn-cancel" style="padding:.4rem .8rem;font-size:.78rem;" title="Registrar postventa">
                                <i class="fa-solid fa-rotate-left"></i>
                            </a>
                        </td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    <% } %>
</main>

<script>
function filtrar() {
    const q = document.getElementById('buscador').value.toLowerCase();
    document.querySelectorAll('#tablaVentas tbody tr.fila-tabla').forEach(row => {
        row.style.display = row.textContent.toLowerCase().includes(q) ? '' : 'none';
    });
}
</script>
</body>
</html>
