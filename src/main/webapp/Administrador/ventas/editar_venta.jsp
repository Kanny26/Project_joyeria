<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="model.Venta" %>
<%
    Object adminSesion = session.getAttribute("admin");
    if (adminSesion == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }
    List<Venta> ventas = (List<Venta>) request.getAttribute("ventas");
    Object totalVentas = request.getAttribute("totalVentas");
    Object pendientes  = request.getAttribute("pendientes");
    Object pagoEfectivo      = request.getAttribute("pagoEfectivo");
    Object pagoTransferencia = request.getAttribute("pagoTransferencia");
    NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar Ventas | AAC27</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/ventas/editar_venta.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/AdminVentaServlet?action=listar">
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
    </a>
</nav>

<main class="titulo">
    <h2 class="titulo__encabezado">Editar Ventas</h2>

    <section class="Ventas-editar__tabla-contenedor">
        <table class="Ventas-editar__tabla">
            <thead>
                <tr>
                    <th>Factura</th>
                    <th>Fecha</th>
                    <th>Vendedor</th>
                    <th>Cliente</th>
                    <th>Total</th>
                    <th>Método de pago</th>
                    <th>Estado</th>
                    <th>Guardar</th>
                </tr>
            </thead>
            <tbody>
                <% if (ventas != null && !ventas.isEmpty()) {
                    for (Venta v : ventas) { %>
                <tr>
                    <td data-label="Factura"><%= v.getVentaId() %></td>
                    <td data-label="Fecha">
                        <%= (v.getFechaEmision() != null) ? sdf.format(v.getFechaEmision()) : "" %>
                    </td>
                    <td data-label="Vendedor"><%= v.getVendedorNombre() %></td>
                    <td data-label="Cliente"><%= v.getClienteNombre() %></td>
                    <td data-label="Total"><%= moneda.format(v.getTotal()) %></td>
                    <td data-label="Método"><%= v.getMetodoPago() %></td>
                    <td data-label="Estado">
                        <% if ("confirmado".equals(v.getEstado())) { %>
                            <span class="estado estado--activo">Pagado</span>
                        <% } else { %>
                            <form method="post"
                                  action="<%= request.getContextPath() %>/AdminVentaServlet"
                                  id="form-<%= v.getVentaId() %>">
                                <input type="hidden" name="action" value="editarEstado">
                                <input type="hidden" name="ventaId" value="<%= v.getVentaId() %>">
                                <select name="estado" class="estado"
                                        onchange="document.getElementById('form-<%= v.getVentaId() %>').submit()">
                                    <option value="pendiente"  <%= "pendiente".equals(v.getEstado())  ? "selected" : "" %>>Pendiente</option>
                                    <option value="confirmado" <%= "confirmado".equals(v.getEstado()) ? "selected" : "" %>>Pagado</option>
                                    <option value="rechazado"  <%= "rechazado".equals(v.getEstado())  ? "selected" : "" %>>Rechazado</option>
                                </select>
                            </form>
                        <% } %>
                    </td>
                    <td data-label="Guardar">
                        <% if (!"confirmado".equals(v.getEstado())) { %>
                            <button onclick="document.getElementById('form-<%= v.getVentaId() %>').submit()"
                                    class="btn-guardar">
                                <i class="fa-solid fa-floppy-disk"></i>
                            </button>
                        <% } %>
                    </td>
                </tr>
                <% } } else { %>
                <tr>
                    <td colspan="8" style="text-align:center;">No hay ventas registradas.</td>
                </tr>
                <% } %>
            </tbody>
        </table>
    </section>

    <div class="contadores">
        <div class="contador-card">
            <h2>Total Ventas</h2>
            <h3 class="contador-card__numero"><%= totalVentas != null ? totalVentas : 0 %></h3>
        </div>
        <div class="contador-card">
            <h2>Pagos pendientes</h2>
            <h3 class="contador-card__numero"><%= pendientes != null ? pendientes : 0 %></h3>
        </div>
        <div class="contador-card">
            <h2>Pagos en efectivo</h2>
            <h3 class="contador-card__numero"><%= pagoEfectivo != null ? pagoEfectivo : 0 %></h3>
        </div>
        <div class="contador-card">
            <h2>Pagos por transferencia</h2>
            <h3 class="contador-card__numero"><%= pagoTransferencia != null ? pagoTransferencia : 0 %></h3>
        </div>
    </div>
</main>
</body>
</html>
