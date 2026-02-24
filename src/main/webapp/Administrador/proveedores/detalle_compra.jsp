<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Compra, model.DetalleCompra, java.util.List, java.text.SimpleDateFormat, java.math.BigDecimal" %>
<%
    Object admin = session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }

    Compra compra      = (Compra) request.getAttribute("compra");
    String proveedorId = (String) request.getAttribute("proveedorId");
    if (proveedorId == null) proveedorId = request.getParameter("proveedorId");

    if (compra == null) {
        response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
        return;
    }

    List<DetalleCompra> detalles = compra.getDetalles();
    if (detalles == null) detalles = java.util.Collections.emptyList();

    // Total unidades — protegido contra nulos
    int totalUnidades = 0;
    for (DetalleCompra d : detalles) {
        if (d != null) totalUnidades += d.getCantidad();
    }

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Detalle Compra #<%= compra.getCompraId() %></title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/proveedores/listar.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%=request.getContextPath()%>/ProveedorServlet?action=verCompras&id=<%= proveedorId %>"
       class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<main class="prov-page">
    <div class="detalle-wrapper">

        <!-- META -->
        <div class="compra-meta">
            <div class="compra-meta__top">
                <div class="compra-meta__id">
                    <div class="compra-meta__icon">
                        <i class="fa-solid fa-receipt"></i>
                    </div>
                    <div>
                        <div class="compra-meta__titulo">Compra #<%= compra.getCompraId() %></div>
                        <div class="compra-meta__sub">Orden de compra registrada</div>
                    </div>
                </div>
                <div class="compra-meta__badges">
                    <span class="badge badge--fecha">
                        <i class="fa-regular fa-calendar"></i>
                        Compra: <%= compra.getFechaCompra() != null ? sdf.format(compra.getFechaCompra()) : "—" %>
                    </span>
                    <span class="badge badge--entrega">
                        <i class="fa-solid fa-truck"></i>
                        Entrega: <%= compra.getFechaEntrega() != null ? sdf.format(compra.getFechaEntrega()) : "—" %>
                    </span>
                </div>
            </div>

            <div class="compra-meta__grid">
                <div class="meta-item">
                    <div class="meta-item__label"><i class="fa-solid fa-hashtag"></i> ID</div>
                    <div class="meta-item__value">#<%= compra.getCompraId() %></div>
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
                        $<%= String.format("%,.2f", compra.getTotal()) %>
                    </div>
                </div>
            </div>
        </div>

        <!-- TABLA DETALLES -->
        <div class="detalles-card">
            <div class="detalles-card__header">
                <i class="fa-solid fa-list"></i>
                Detalle de productos
                <span class="detalles-count"><%= detalles.size() %></span>
            </div>

            <% if (detalles.isEmpty()) { %>
                <div style="padding:2rem;text-align:center;color:#9ca3af;">
                    <i class="fa-solid fa-box-open" style="font-size:2rem;margin-bottom:.5rem;display:block;"></i>
                    No hay productos en esta compra
                </div>
            <% } else { %>
            <table class="detalles-table">
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Producto</th>
                        <th>Precio unitario</th>
                        <th>Cantidad</th>
                        <th>Subtotal</th>
                    </tr>
                </thead>
                <tbody>
                    <% int rowNum = 1;
                       for (DetalleCompra d : detalles) {
                           if (d == null) continue; %>
                        <tr>
                            <td style="color:#9ca3af;font-weight:600;width:3rem;"><%= rowNum++ %></td>
                            <td>
                                <div class="prod-name"><%= d.getProductoNombre() != null ? d.getProductoNombre() : "Producto eliminado" %></div>
                                <div class="prod-id">ID #<%= d.getProductoId() %></div>
                            </td>
                            <td class="precio-cell">
                                $<%= d.getPrecioUnitario() != null ? String.format("%,.2f", d.getPrecioUnitario()) : "0.00" %>
                            </td>
                            <td>
                                <span class="cant-badge">×<%= d.getCantidad() %></span>
                            </td>
                            <td class="subtotal-cell-right">
                                $<%= d.getSubtotal() != null ? String.format("%,.2f", d.getSubtotal()) : "0.00" %>
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
                            $<%= compra.getTotal() != null ? String.format("%,.2f", compra.getTotal()) : "0.00" %>
                        </td>
                    </tr>
                </tfoot>
            </table>
            <% } %>
        </div>

        <!-- ACCIONES -->
        <div class="acciones-bar">
            <a href="<%=request.getContextPath()%>/ProveedorServlet?action=verCompras&id=<%= proveedorId %>"
               class="btn-volver">
                <i class="fa-solid fa-arrow-left"></i> Volver a compras
            </a>
            <div class="acciones-group">
                <button class="btn-print" onclick="window.print()">
                    <i class="fa-solid fa-print"></i> Imprimir
                </button>
                
            </div>
        </div>

    </div>
</main>

</body>
</html>
