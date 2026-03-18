<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="model.Venta" %>
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
    Usuario usuario = (Usuario) vendedorSesion;

    /*
     * El servlet pone la lista de ventas en el request con el nombre "ventas".
     * El parámetro "exito" viene en la URL cuando se redirige tras un abono exitoso.
     */
    List<Venta> ventas  = (List<Venta>) request.getAttribute("ventas");
    String exito        = request.getParameter("exito");
    String mensajeError = (String) request.getAttribute("error");

    NumberFormat moneda  = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
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
    <a href="<%=request.getContextPath()%>/vendedor/vendedor_principal.jsp" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<main class="prov-page">
    <h1 class="prov-page__titulo">Mis Ventas</h1>

    <%-- Mensajes de retroalimentación --%>
    <% if (mensajeError != null && !mensajeError.isEmpty()) { %>
        <div class="prov-alert prov-alert--error" id="alertaMensaje">
            <i class="fa-solid fa-circle-exclamation"></i> <%= mensajeError %>
        </div>
    <% } %>

    <%--
        El parámetro "exito" viene en la URL cuando se redirige tras una operación exitosa
        (como un abono). Se detecta aquí para mostrar el mensaje apropiado.
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

    <%-- Estadísticas rápidas del vendedor --%>
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

    <%-- Barra de herramientas: buscador y acceso a casos postventa --%>
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

    <% if (ventas == null || ventas.isEmpty()) { %>
        <div class="prov-empty">
            <div class="prov-empty__icon"><i class="fa-solid fa-inbox"></i></div>
            <p class="prov-empty__texto">Aún no has registrado ninguna venta.</p>
            <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=nueva"
               class="btn-save" style="margin-top:1rem;">
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
                            <%-- Botón de postventa con confirmación de JS antes de navegar --%>
                            <button onclick="irAPostventa(<%= v.getVentaId() %>)"
                               class="btn-cancel" style="padding:.4rem .8rem;font-size:.78rem;" title="Registrar postventa">
                                <i class="fa-solid fa-rotate-left"></i>
                            </button>
                        </td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
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

// Filtra las filas de la tabla según lo que se escribe en el buscador
function filtrar() {
    var q = document.getElementById('buscador').value.toLowerCase();
    document.querySelectorAll('#tablaVentas tbody tr.fila-tabla').forEach(function(row) {
        row.style.display = row.textContent.toLowerCase().includes(q) ? '' : 'none';
    });
}

// Muestra una confirmación antes de ir al formulario de postventa
function irAPostventa(ventaId) {
    Swal.fire({
        title: '¿Registrar caso postventa?',
        text: 'Vas a crear un caso de postventa para la venta #' + ventaId + '.',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Continuar',
        cancelButtonText: 'Cancelar',
        confirmButtonColor: '#7c3aed',
        cancelButtonColor: '#6b7280'
    }).then(function(result) {
        if (result.isConfirmed) {
            window.location.href = '<%= request.getContextPath() %>/VentaVendedorServlet?action=registrarPostventa&ventaId=' + ventaId;
        }
    });
}
</script>
</body>
</html>
