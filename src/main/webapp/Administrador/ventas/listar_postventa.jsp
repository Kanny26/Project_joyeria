<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.ArrayList, java.util.LinkedHashMap, java.util.Map, java.text.SimpleDateFormat" %>
<%@ page import="model.CasoPostventa" %>
<%
    Object adminSesion = session.getAttribute("admin");
    if (adminSesion == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }

    List<CasoPostventa> casos = (List<CasoPostventa>) request.getAttribute("casos");
    System.out.println("JSP postventa - casos: " + (casos == null ? "NULL" : casos.size() + " registros"));
    if (casos == null) casos = new ArrayList<>();

    Map<String, List<CasoPostventa>> porVendedor = new LinkedHashMap<>();
    for (CasoPostventa c : casos) {
        String vendedor = (c.getVendedorNombre() != null && !c.getVendedorNombre().isBlank())
                          ? c.getVendedorNombre() : "Sin vendedor";
        porVendedor.computeIfAbsent(vendedor, k -> new ArrayList<>()).add(c);
    }

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    long totalEnProceso = 0, totalAprobados = 0, totalCancelados = 0;
    for (CasoPostventa c : casos) {
        if ("en_proceso".equals(c.getEstado())) totalEnProceso++;
        else if ("aprobado".equals(c.getEstado())) totalAprobados++;
        else if ("cancelado".equals(c.getEstado())) totalCancelados++;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Postventa | AAC27 Admin</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/ventas/postventa.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/Administrador/admin-principal.jsp" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<main class="prov-page">

    <div class="page-header">
        <div class="page-header__title">
            <i class="fa-solid fa-rotate-left"></i>
            <div>
                <h1>Casos Postventa</h1>
                <p><%= casos.size() %> caso<%= casos.size() != 1 ? "s" : "" %> &middot; <%= porVendedor.size() %> vendedor<%= porVendedor.size() != 1 ? "es" : "" %></p>
            </div>
        </div>
        <div class="stat-pills">
            <span class="pill pill--warning"><i class="fa-solid fa-clock"></i> <%= totalEnProceso %> en proceso</span>
            <span class="pill pill--success"><i class="fa-solid fa-check-circle"></i> <%= totalAprobados %> aprobado<%= totalAprobados != 1 ? "s" : "" %></span>
            <span class="pill pill--danger"><i class="fa-solid fa-ban"></i> <%= totalCancelados %> cancelado<%= totalCancelados != 1 ? "s" : "" %></span>
        </div>
    </div>

    <div class="filtros-bar">
        <div class="search-wrap">
            <i class="fa-solid fa-magnifying-glass"></i>
            <input type="text" id="buscador" placeholder="Buscar por cliente, tipo, producto...">
        </div>
        <div class="filter-btns" id="filterBtns">
            <button class="filter-btn active" data-estado="">Todos</button>
            <button class="filter-btn" data-estado="en_proceso">En proceso</button>
            <button class="filter-btn" data-estado="aprobado">Aprobado</button>
            <button class="filter-btn" data-estado="cancelado">Cancelado</button>
        </div>
    </div>

<% if (casos.isEmpty()) { %>
    <div class="empty-state">
        <div class="empty-state__icon"><i class="fa-solid fa-box-open"></i></div>
        <h3>Sin casos postventa</h3>
        <p>Aun no se han registrado casos de cambio, devolucion o reclamo.</p>
    </div>
<% } else { %>

    <div class="vendedor-grupos" id="gruposContainer">

    <% for (Map.Entry<String, List<CasoPostventa>> entry : porVendedor.entrySet()) {
        String vNombre = entry.getKey();
        List<CasoPostventa> casosDel = entry.getValue();
        long enProcesoV = 0;
        for (CasoPostventa cx : casosDel) { if ("en_proceso".equals(cx.getEstado())) enProcesoV++; }
        String iniciales = vNombre.length() >= 2 ? vNombre.substring(0,2).toUpperCase() : vNombre.toUpperCase();
    %>

        <div class="vendedor-grupo" data-vendedor="<%= vNombre.toLowerCase() %>">

            <div class="vendedor-cabecera">
                <div class="vendedor-avatar"><%= iniciales %></div>
                <div class="vendedor-info">
                    <h2 class="vendedor-nombre"><%= vNombre %></h2>
                    <span class="vendedor-meta">
                        <%= casosDel.size() %> caso<%= casosDel.size() != 1 ? "s" : "" %>
                        <% if (enProcesoV > 0) { %>
                            &nbsp;&bull;&nbsp;<span class="alerta-pendiente"><i class="fa-solid fa-clock"></i> <%= enProcesoV %> pendiente<%= enProcesoV != 1 ? "s" : "" %></span>
                        <% } %>
                    </span>
                </div>
                <button class="btn-colapsar" onclick="toggleGrupo(this)" title="Colapsar">
                    <i class="fa-solid fa-chevron-up"></i>
                </button>
            </div>

            <div class="casos-grid">
            <% for (CasoPostventa c : casosDel) {
                String est = (c.getEstado() != null) ? c.getEstado() : "en_proceso";
                String tipo = (c.getTipo() != null) ? c.getTipo() : "";
                String textoFiltro = (c.getClienteNombre() != null ? c.getClienteNombre() : "")
                                   + " " + tipo
                                   + " " + (c.getProductoNombre() != null ? c.getProductoNombre() : "");
            %>

                <div class="caso-card caso-card--<%= est %>"
                     data-estado="<%= est %>"
                     data-texto="<%= textoFiltro.toLowerCase() %>">

                    <div class="caso-card__top">
                        <span class="caso-id">#<%= c.getCasoId() %></span>
                        <% if ("aprobado".equals(est)) { %>
                            <span class="estado-badge estado-badge--success"><i class="fa-solid fa-circle-check"></i> Aprobado</span>
                        <% } else if ("cancelado".equals(est)) { %>
                            <span class="estado-badge estado-badge--danger"><i class="fa-solid fa-ban"></i> Cancelado</span>
                        <% } else { %>
                            <span class="estado-badge estado-badge--warning"><i class="fa-solid fa-clock"></i> En proceso</span>
                        <% } %>
                    </div>

                    <div class="tipo-banda tipo-banda--<%= tipo %>">
                        <% if ("cambio".equals(tipo)) { %>
                            <i class="fa-solid fa-arrows-rotate"></i> Cambio
                        <% } else if ("devolucion".equals(tipo)) { %>
                            <i class="fa-solid fa-rotate-left"></i> Devolucion
                        <% } else { %>
                            <i class="fa-solid fa-triangle-exclamation"></i> Reclamo
                        <% } %>
                    </div>

                    <div class="caso-card__body">
                        <div class="caso-dato">
                            <span class="caso-dato__lbl"><i class="fa-solid fa-user"></i> Cliente</span>
                            <span class="caso-dato__val"><%= c.getClienteNombre() != null ? c.getClienteNombre() : "Sin cliente" %></span>
                        </div>
                        <% if (c.getProductoNombre() != null && !c.getProductoNombre().isBlank()) { %>
                        <div class="caso-dato">
                            <span class="caso-dato__lbl"><i class="fa-solid fa-gem"></i> Producto</span>
                            <span class="caso-dato__val"><%= c.getProductoNombre() %></span>
                        </div>
                        <% } %>
                        <div class="caso-dato caso-dato--row">
                            <div class="mini-dato">
                                <span class="caso-dato__lbl">Venta</span>
                                <span class="caso-dato__val">#<%= c.getVentaId() %></span>
                            </div>
                            <div class="mini-dato">
                                <span class="caso-dato__lbl">Cant.</span>
                                <span class="caso-dato__val"><%= c.getCantidad() %> uds.</span>
                            </div>
                            <div class="mini-dato">
                                <span class="caso-dato__lbl">Fecha</span>
                                <span class="caso-dato__val"><%= c.getFecha() != null ? sdf.format(c.getFecha()) : "---" %></span>
                            </div>
                        </div>
                    </div>

                    <div class="caso-card__footer">
                        <a href="<%= request.getContextPath() %>/Administrador/postventa/ver?id=<%= c.getCasoId() %>"
                           class="btn-ver<%= "en_proceso".equals(est) ? " btn-ver--activo" : "" %>">
                            <i class="fa-solid fa-eye"></i>
                            <%= "en_proceso".equals(est) ? "Ver y gestionar" : "Ver detalle" %>
                        </a>
                    </div>

                </div>

            <% } %>
            </div><%-- casos-grid --%>

        </div><%-- vendedor-grupo --%>

    <% } %>
    </div><%-- vendedor-grupos --%>

<% } %>
</main>

<script>
const buscador = document.getElementById('buscador');
let estadoActivo = '';

function aplicarFiltros() {
    const q = buscador.value.toLowerCase().trim();
    document.querySelectorAll('.vendedor-grupo').forEach(grupo => {
        let visibles = 0;
        grupo.querySelectorAll('.caso-card').forEach(card => {
            const textoOk = !q || card.dataset.texto.includes(q);
            const estadoOk = !estadoActivo || card.dataset.estado === estadoActivo;
            const mostrar = textoOk && estadoOk;
            card.style.display = mostrar ? '' : 'none';
            if (mostrar) visibles++;
        });
        grupo.style.display = visibles > 0 ? '' : 'none';
    });
}

buscador.addEventListener('input', aplicarFiltros);

document.querySelectorAll('.filter-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        estadoActivo = btn.dataset.estado;
        aplicarFiltros();
    });
});

function toggleGrupo(btn) {
    const grid = btn.closest('.vendedor-grupo').querySelector('.casos-grid');
    const icon = btn.querySelector('i');
    const collapsed = grid.classList.toggle('collapsed');
    icon.className = collapsed ? 'fa-solid fa-chevron-down' : 'fa-solid fa-chevron-up';
    btn.title = collapsed ? 'Expandir' : 'Colapsar';
}
</script>
</body>
</html>
