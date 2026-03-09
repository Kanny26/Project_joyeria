<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.Venta" %> 
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>

<%
    // Configuración de formatos
    NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    
    // 🔒 Recuperar atributos con fallback seguro (NUNCA null)
    List<Venta> ventas = (List<Venta>) request.getAttribute("ventas");
    if (ventas == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/ventas/listar");
        return;
    }
    
    Object totalVentasObj = request.getAttribute("totalVentas");
    Object totalPendientesObj = request.getAttribute("totalPendientes");
    
    int totalVentas = (totalVentasObj instanceof Integer) ? (Integer) totalVentasObj : 0;
    int totalPendientes = (totalPendientesObj instanceof Integer) ? (Integer) totalPendientesObj : 0;
    
    // Parámetros de búsqueda
    String tipoBusqueda = (String) request.getAttribute("tipo");
    String queryBusqueda = (String) request.getAttribute("criterio");
    String fInicio = (String) request.getAttribute("fechaInicio");
    String fFin = (String) request.getAttribute("fechaFin");
    
    // Debug final
    System.out.println("✅ JSP cargado - ventas.size(): " + ventas.size());
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ventas | AAC27 Admin</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/ventas/listar_ventas.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%=request.getContextPath()%>/Administrador/ventas.jsp" class="navbar-admin__home-link">
	    <span class="navbar-admin__home-icon-wrap">
	        <i class="fa-solid fa-arrow-left"></i>
		    <span class="navbar-admin__home-text">Volver atrás</span>
		    <i class="fa-solid fa-house-chimney"></i>
	    </span>
    </a>
</nav>

<main class="prov-page">
    <div class="list-card">
        <div class="list-card__header">
            <h2><i class="fa-solid fa-receipt"></i> Todas las Ventas</h2>
            <div class="stats-row">
                <span class="stat-chip">
                    <i class="fa-solid fa-file-invoice-dollar"></i>
                    Total ventas: <strong><%= totalVentas %></strong>
                </span>
                <span class="stat-chip stat-chip--warning">
                    <i class="fa-solid fa-clock"></i>
                    Con saldo pendiente: <strong><%= totalPendientes %></strong>
                </span>
            </div>
        </div>

        <%-- FILTROS --%>
        <form action="<%= request.getContextPath() %>/Administrador/ventas/buscar" method="GET" class="filtros-form">
            <div class="filtros-row">
                <select name="tipo">
                    <option value="">-- Buscar por --</option>
                    <option value="id" <%= "id".equals(tipoBusqueda) ? "selected" : "" %>>N° Venta</option>
                    <option value="cliente" <%= "cliente".equals(tipoBusqueda) ? "selected" : "" %>>Cliente</option>
                    <option value="vendedor" <%= "vendedor".equals(tipoBusqueda) ? "selected" : "" %>>Vendedor</option>
                    <option value="estado" <%= "estado".equals(tipoBusqueda) ? "selected" : "" %>>Estado</option>
                </select>
                <input type="text" name="q" value="<%= queryBusqueda != null ? queryBusqueda : "" %>" placeholder="Término de búsqueda...">
                <input type="date" name="fechaInicio" value="<%= fInicio != null ? fInicio : "" %>" title="Desde">
                <input type="date" name="fechaFin" value="<%= fFin != null ? fFin : "" %>" title="Hasta">
                <button type="submit" class="btn-save btn-save--sm">
                    <i class="fa-solid fa-magnifying-glass"></i> Buscar
                </button>
                <a href="<%= request.getContextPath() %>/Administrador/ventas/listar" class="btn-cancel btn-cancel--sm">
                    <i class="fa-solid fa-rotate-left"></i> Limpiar
                </a>
            </div>
        </form>

        <%-- LISTADO DE VENTAS --%>
        <% if (ventas.isEmpty()) { %>
            <div class="empty-state">
                <i class="fa-solid fa-inbox"></i>
                <p><%= totalVentas == 0 ? "Aún no hay ventas registradas." : "No se encontraron ventas con los filtros aplicados." %></p>
            </div>
        <% } else { %>
            <div class="cards-container">
                <% for (Venta v : ventas) { 
                    String estadoClass = "status--pending";
                    String estadoTexto = "Pendiente";
                    
                    if ("anticipo".equals(v.getModalidad()) && v.getSaldoPendiente() != null && v.getSaldoPendiente().compareTo(java.math.BigDecimal.ZERO) > 0) {
                        estadoClass = "status--danger";
                        estadoTexto = "Saldo: " + moneda.format(v.getSaldoPendiente());
                    } else if ("confirmado".equals(v.getEstado())) {
                        estadoClass = "status--success";
                        estadoTexto = "Pagado";
                    }
                %>
                <div class="venta-card">
                    <div class="venta-card__header">
                        <span class="venta-card__id">Venta #<%= v.getVentaId() %></span>
                        <span class="venta-card__date"><%= v.getFechaEmision() != null ? sdf.format(v.getFechaEmision()) : "---" %></span>
                    </div>
                    
                    <div class="venta-card__body">
                        <div class="venta-card__info">
                            <p class="info-label">Cliente</p>
                            <p class="info-value"><%= v.getClienteNombre() != null ? v.getClienteNombre() : "N/A" %></p>
                        </div>
                        <div class="venta-card__info">
                            <p class="info-label">Vendedor</p>
                            <p class="info-value"><%= v.getVendedorNombre() != null ? v.getVendedorNombre() : "N/A" %></p>
                        </div>
                        <div class="venta-card__total">
                            <p class="info-label">Total Venta</p>
                            <p class="total-amount"><%= moneda.format(v.getTotal() != null ? v.getTotal() : 0) %></p>
                        </div>
                    </div>
            
                    <div class="venta-card__footer">
                        <div class="venta-card__tags">
                            <span class="tag-method">
                                <%= "efectivo".equals(v.getMetodoPago()) ? "💵 Efectivo" : "💳 Tarjeta" %>
                            </span>
                            <span class="status-badge <%= estadoClass %>"><%= estadoTexto %></span>
                        </div>
                        <div class="venta-card__actions">
                            <a href="<%= request.getContextPath() %>/Administrador/ventas/ver?id=<%= v.getVentaId() %>" 
                               class="btn-view" title="Ver Detalle">
                                <i class="fa-solid fa-eye"></i>
                            </a>
                        </div>
                    </div>
                </div>
                <% } %>
            </div>
        <% } %>
    </div>
</main>
</body>
</html>