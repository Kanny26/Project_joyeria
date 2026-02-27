<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.CasoPostventa" %> 
<%@ page import="java.text.SimpleDateFormat" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Postventa | AAC27 Admin</title>
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
    <a href="<%= request.getContextPath() %>/Administrador/admin-principal.jsp">
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
    </a>
</nav>

<main class="prov-page">
    <div class="list-card">
        <div class="list-card__header">
            <h2><i class="fa-solid fa-rotate-left"></i> Casos Postventa</h2>
        </div>

        <div class="filtros-row">
            <input type="text" id="buscador" placeholder="Buscar por cliente, vendedor, tipo..." oninput="filtrar()">
            <select id="filtroEstado" onchange="filtrar()">
                <option value="">Todos los estados</option>
                <option value="en_proceso">En proceso</option>
                <option value="aprobado">Aprobado</option>
                <option value="cancelado">Cancelado</option>
            </select>
        </div>

        <%
            List<Caso> casos = (List<Caso>) request.getAttribute("casos");
            if (casos == null || casos.isEmpty()) {
        %>
            <div class="empty-state">
                <i class="fa-solid fa-inbox"></i>
                <p>No hay casos postventa registrados.</p>
            </div>
        <%
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        %>
            <div class="table-wrapper">
                <table class="data-table" id="tablaCasos">
                    <thead>
                        <tr>
                            <th>#Caso</th>
                            <th>#Venta</th>
                            <th>Vendedor</th>
                            <th>Cliente</th>
                            <th>Tipo</th>
                            <th>Cant.</th>
                            <th>Fecha</th>
                            <th>Estado</th>
                            <th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Caso c : casos) { %>
                            <tr class="fila-caso" data-estado="<%= c.getEstado() %>">
                                <td><span class="badge badge--id">#<%= c.getCasoId() %></span></td>
                                <td>#<%= c.getVentaId() %></td>
                                <td><%= c.getVendedorNombre() %></td>
                                <td><%= c.getClienteNombre() %></td>
                                <td>
                                    <% if ("cambio".equals(c.getTipo())) { %>
                                        <span class="badge badge--info">🔄 Cambio</span>
                                    <% } else if ("devolucion".equals(c.getTipo())) { %>
                                        <span class="badge badge--warning">↩️ Devolución</span>
                                    <% } else { %>
                                        <span class="badge badge--danger">⚠️ Reclamo</span>
                                    <% } %>
                                </td>
                                <td><%= c.getCantidad() %></td>
                                <td><%= (c.getFecha() != null) ? sdf.format(c.getFecha()) : "" %></td>
                                <td>
                                    <% if ("aprobado".equals(c.getEstado())) { %>
                                        <span class="badge badge--success">✅ Aprobado</span>
                                    <% } else if ("cancelado".equals(c.getEstado())) { %>
                                        <span class="badge badge--danger">❌ Cancelado</span>
                                    <% } else { %>
                                        <span class="badge badge--warning">🕐 En proceso</span>
                                    <% } %>
                                </td>
                                <td class="acciones">
                                    <a href="<%= request.getContextPath() %>/Administrador/postventa/ver?id=<%= c.getCasoId() %>"
                                       class="btn-icon" title="Ver y gestionar">
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

<script>
function filtrar() {
    const q      = document.getElementById('buscador').value.toLowerCase();
    const estado = document.getElementById('filtroEstado').value;
    document.querySelectorAll('.fila-caso').forEach(row => {
        const textoOk  = row.textContent.toLowerCase().includes(q);
        const estadoOk = !estado || row.dataset.estado === estado;
        row.style.display = (textoOk && estadoOk) ? '' : 'none';
    });
}
</script>
</body>
</html>