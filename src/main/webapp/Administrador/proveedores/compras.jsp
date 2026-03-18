<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Proveedor, model.Compra, model.DetalleCompra, java.util.List, java.text.SimpleDateFormat, java.math.BigDecimal" %>
<%
    /* Seguridad: si no hay sesión activa de admin, redirige al login */
    Object admin = session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }

    Proveedor proveedor   = (Proveedor) request.getAttribute("proveedor");
    List<Compra> compras  = (List<Compra>) request.getAttribute("listaCompras");
    if (compras == null) compras = java.util.Collections.emptyList();

    /* Estadísticas calculadas en el servlet y enviadas como atributos */
    Integer totalCompras   = (Integer)    request.getAttribute("totalCompras");
    Integer totalProductos = (Integer)    request.getAttribute("totalProductos");
    BigDecimal totalGasto  = (BigDecimal) request.getAttribute("totalGasto");
    if (totalCompras   == null) totalCompras   = 0;
    if (totalProductos == null) totalProductos = 0;
    if (totalGasto     == null) totalGasto     = BigDecimal.ZERO;

    /*
     * El parámetro "msg" llega desde sendRedirect después de una operación.
     * Valores esperados: "creado" (nueva compra), "eliminado" (compra eliminada).
     */
    String msg = request.getParameter("msg");

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Compras — <%= proveedor != null ? proveedor.getNombre() : "" %></title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/proveedores/listar.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%=request.getContextPath()%>/ProveedorServlet?action=listar" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<main class="prov-page">

    <!-- Encabezado con el nombre del proveedor -->
    <div class="compras-header">
        <div class="compras-header__avatar">
            <i class="fa-solid fa-building"></i>
        </div>
        <div class="compras-header__info">
            <h2>Compras a <%= proveedor != null ? proveedor.getNombre() : "" %></h2>
            <p>Historial completo de órdenes de compra</p>
        </div>
    </div>

    <!-- Estadísticas de compras calculadas en el servidor -->
    <div class="stat-grid">
        <div class="stat-card">
            <span class="stat-card__label"><i class="fa-solid fa-receipt"></i> Total compras</span>
            <span class="stat-card__value"><%= totalCompras %></span>
        </div>
        <div class="stat-card">
            <span class="stat-card__label"><i class="fa-solid fa-boxes-stacked"></i> Productos recibidos</span>
            <span class="stat-card__value"><%= totalProductos %></span>
        </div>
        <div class="stat-card">
            <span class="stat-card__label"><i class="fa-solid fa-dollar-sign"></i> Total gastado</span>
            <span class="stat-card__value stat-card__value--money">
                $<%= String.format("%,.0f", totalGasto) %>
            </span>
        </div>
    </div>

    <!-- Botón para registrar una nueva compra a este proveedor -->
    <div class="toolbar">
        <a href="<%=request.getContextPath()%>/CompraServlet?action=nueva&proveedorId=<%= proveedor.getProveedorId()%>"
           class="btn-nueva-compra">
            <i class="fa-solid fa-plus"></i> Nueva compra
        </a>
    </div>

    <!-- Grid de tarjetas de compras -->
    <% if (compras.isEmpty()) { %>
        <div class="prov-empty">
            <i class="fa-solid fa-box-open prov-empty__icon"></i>
            <p class="prov-empty__texto">Este proveedor aún no tiene compras registradas.</p>
        </div>
    <% } else { %>
        <div class="compras-grid">
            <% for (Compra compra : compras) { %>
                <div class="compra-card">

                    <div class="compra-card__head">
                        <div class="compra-card__icon">
                            <i class="fa-solid fa-cart-shopping"></i>
                        </div>
                        <div>
                            <div class="compra-card__id">Compra #<%= compra.getCompraId() %></div>
                            <div class="compra-card__date">
                                <i class="fa-regular fa-calendar"></i>
                                <%= compra.getFechaCompra() != null ? sdf.format(compra.getFechaCompra()) : "—" %>
                            </div>
                        </div>
                    </div>

                    <div class="compra-card__body">
                        <div class="compra-card__row">
                            <span class="compra-card__key"><i class="fa-solid fa-truck"></i> Entrega</span>
                            <span class="compra-card__val">
                                <%= compra.getFechaEntrega() != null ? sdf.format(compra.getFechaEntrega()) : "—" %>
                            </span>
                        </div>

                        <div class="compra-card__row">
                            <span class="compra-card__key"><i class="fa-solid fa-box"></i> Productos</span>
                            <div class="compra-card__tags">
                                <%--
                                    Se muestran máximo 3 productos para no sobrecargar la tarjeta.
                                    Si hay más de 3, se muestra un badge con la cantidad restante.
                                --%>
                                <% if (compra.getDetalles() != null) {
                                    int idx = 0;
                                    for (DetalleCompra d : compra.getDetalles()) {
                                        if (idx >= 3) break; %>
                                        <span class="tag"><%= d.getProductoNombre() %> ×<%= d.getCantidad() %></span>
                                <%      idx++;
                                    }
                                    if (compra.getDetalles().size() > 3) { %>
                                        <span class="tag tag--more">+<%= compra.getDetalles().size() - 3 %> más</span>
                                <%  }
                                } %>
                            </div>
                        </div>

                        <div class="compra-card__row">
                            <span class="compra-card__key"><i class="fa-solid fa-dollar-sign"></i> Total</span>
                            <span class="compra-card__val compra-card__val--money">
                                $<%= String.format("%,.2f", compra.getTotal()) %>
                            </span>
                        </div>
                    </div>

                    <div class="compra-card__foot">
                        <a href="<%=request.getContextPath()%>/CompraServlet?action=detalle&id=<%= compra.getCompraId() %>&proveedorId=<%= proveedor != null ? proveedor.getProveedorId() : "" %>"
                           class="btn-detalle">
                            <i class="fa-solid fa-eye"></i> Ver detalle
                        </a>
                    </div>

                </div>
            <% } %>
        </div>
    <% } %>

</main>

<script>
/*
 * Alertas de resultado según el parámetro "msg" recibido del servidor.
 * "creado"    → se muestra después de registrar una nueva compra exitosamente.
 * "eliminado" → se muestra después de eliminar una compra desde el detalle.
 */
(function() {
    var msg = '<%= msg != null ? msg : "" %>';
    if (msg === 'creado') {
        Swal.fire({
            icon: 'success',
            title: '¡Compra registrada!',
            text: 'La orden de compra fue guardada correctamente.',
            confirmButtonColor: '#7c3aed',
            timer: 3000,
            timerProgressBar: true
        });
    } else if (msg === 'eliminado') {
        Swal.fire({
            icon: 'success',
            title: 'Compra eliminada',
            text: 'La orden de compra fue eliminada del historial.',
            confirmButtonColor: '#7c3aed',
            timer: 3000,
            timerProgressBar: true
        });
    }
})();
</script>

</body>
</html>
