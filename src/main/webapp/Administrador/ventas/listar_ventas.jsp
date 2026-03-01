<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%-- Sustituye 'com.tu.modelo.Venta' por la ruta real de tu clase --%>
<%@ page import="model.Venta" %> 
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ventas | AAC27 Admin</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/ventas.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<%
    // Configuración de formatos para moneda y fecha
    NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    
    // Recuperar datos del request
    List<Venta> ventas = (List<Venta>) request.getAttribute("ventas");
    Object totalVentas = request.getAttribute("totalVentas");
    Object totalPendientes = request.getAttribute("totalPendientes");
    
    // Capturar parámetros de búsqueda para mantener los filtros en el formulario
    String tipoBusqueda = request.getParameter("tipo");
    String queryBusqueda = request.getParameter("q");
    String fInicio = request.getParameter("fechaInicio");
    String fFin = request.getParameter("fechaFin");
%>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/Administrador/admin-principal.jsp">
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
    </a>
</nav>

<main class="prov-page">
    <div class="list-card">
        <div class="list-card__header">
            <h2><i class="fa-solid fa-receipt"></i> Todas las Ventas</h2>
            <div class="stats-row">
                <span class="stat-chip">
                    <i class="fa-solid fa-file-invoice-dollar"></i>
                    Total ventas: <strong><%= (totalVentas != null) ? totalVentas : 0 %></strong>
                </span>
                <span class="stat-chip stat-chip--warning">
                    <i class="fa-solid fa-clock"></i>
                    Con saldo pendiente: <strong><%= (totalPendientes != null) ? totalPendientes : 0 %></strong>
                </span>
            </div>
        </div>

        <%-- ── FILTROS ── --%>
        <form action="<%= request.getContextPath() %>/Administrador/ventas/buscar" method="GET" class="filtros-form">
            <div class="filtros-row">
                <select name="tipo">
                    <option value="">-- Buscar por --</option>
                    <option value="id" <%= "id".equals(tipoBusqueda) ? "selected" : "" %>>N° Venta</option>
                    <option value="cliente" <%= "cliente".equals(tipoBusqueda) ? "selected" : "" %>>Cliente</option>
                    <option value="vendedor" <%= "vendedor".equals(tipoBusqueda) ? "selected" : "" %>>Vendedor</option>
                    <option value="estado" <%= "estado".equals(tipoBusqueda) ? "selected" : "" %>>Estado</option>
                </select>
                <input type="text" name="q" value="<%= (queryBusqueda != null) ? queryBusqueda : "" %>" placeholder="Término de búsqueda...">
                <input type="date" name="fechaInicio" value="<%= (fInicio != null) ? fInicio : "" %>" title="Desde">
                <input type="date" name="fechaFin" value="<%= (fFin != null) ? fFin : "" %>" title="Hasta">
                <button type="submit" class="btn-save btn-save--sm">
                    <i class="fa-solid fa-magnifying-glass"></i> Buscar
                </button>
                <a href="<%= request.getContextPath() %>/Administrador/ventas/listar" class="btn-cancel btn-cancel--sm">
                    <i class="fa-solid fa-rotate-left"></i> Limpiar
                </a>
            </div>
        </form>

        <%-- ── TABLA ── --%>
        <% if (ventas == null || ventas.isEmpty()) { %>
            <div class="empty-state">
                <i class="fa-solid fa-inbox"></i>
                <p>No se encontraron ventas con los filtros aplicados.</p>
            </div>
        <% } else { %>
            <div class="table-wrapper">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Vendedor</th>
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
                        <% for (Venta v : ventas) { %>
                            <tr>
                                <td><span class="badge badge--id">#<%= v.getVentaId() %></span></td>
                                <td><%= v.getVendedorNombre() %></td>
                                <td><%= v.getClienteNombre() %></td>
                                <td><%= (v.getFechaEmision() != null) ? sdf.format(v.getFechaEmision()) : "---" %></td>
                                <td class="monto">
                                    <%= moneda.format(v.getTotal()) %>
                                </td>
                                <td>
                                    <% if ("anticipo".equals(v.getModalidad())) { %>
                                        <span class="badge badge--warning">Anticipo</span>
                                    <% } else { %>
                                        <span class="badge badge--info">Contado</span>
                                    <% } %>
                                </td>
                                <td>
                                    <%= "efectivo".equals(v.getMetodoPago()) ? "💵 Efectivo" : "💳 Tarjeta" %>
                                </td>
                                <td>
                                    <% if ("anticipo".equals(v.getModalidad()) && v.getSaldoPendiente() != null && v.getSaldoPendiente().compareTo(java.math.BigDecimal.ZERO) > 0) { %>
                                        <span class="badge badge--danger">
                                            Saldo: <%= moneda.format(v.getSaldoPendiente()) %>
                                        </span>
                                    <% } else if ("confirmado".equals(v.getEstado())) { %>
                                        <span class="badge badge--success">Pagado</span>
                                    <% } else { %>
                                        <span class="badge badge--warning">Pendiente</span>
                                    <% } %>
                                </td>
                                <td class="acciones">
                                    <a href="<%= request.getContextPath() %>/Administrador/ventas/ver?id=<%= v.getVentaId() %>"
                                       class="btn-icon" title="Ver detalle">
                                        <i class="fa-solid fa-eye"></i>
                                    </a>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        <% } %>
    </div>
</main>
</body>
</html>