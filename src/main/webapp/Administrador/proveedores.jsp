<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="model.Proveedor, java.util.List"%>
<%
    Object admin = session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }

    List<Proveedor> proveedores = (List<Proveedor>) request.getAttribute("proveedores");
    if (proveedores == null) proveedores = java.util.Collections.emptyList();

    String msg = request.getAttribute("msg") != null
             ? (String) request.getAttribute("msg")
             : request.getParameter("msg");

    String busqueda     = (String) request.getAttribute("busqueda");
    String filtroActivo = request.getAttribute("filtroActivo") != null
                          ? (String) request.getAttribute("filtroActivo")
                          : "todos";

    if (busqueda == null) busqueda = "";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Proveedores - AAC27</title>
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
    <a href="<%=request.getContextPath()%>/Administrador/admin-principal.jsp" class="navbar-admin__home-link">
	   
	    <span class="navbar-admin__home-icon-wrap">
	        <i class="fa-solid fa-arrow-left"></i>
		    <span class="navbar-admin__home-text">Volver atras</span>
		    <i class="fa-solid fa-house-chimney"></i>
	    </span>
    </a>
</nav>

<main class="prov-page">
    <h2 class="prov-page__titulo">Gestión de Proveedores</h2>

    
    <!-- ===== CONTADORES ===== -->
    <div class="contadores">
        <div class="contador-card">
            <h2>Total Proveedores</h2>
            <h3 class="contador-card__numero">
                <%= request.getAttribute("totalProveedores") != null
                    ? request.getAttribute("totalProveedores")
                    : proveedores.size() %>
            </h3>
        </div>
        <div class="contador-card">
            <h2>Proveedores Activos</h2>
            <h3 class="contador-card__numero">
                <%= request.getAttribute("activos") != null
                    ? request.getAttribute("activos")
                    : proveedores.stream().filter(Proveedor::isEstado).count() %>
            </h3>
        </div>
    </div>
    <!-- ===== BARRA SUPERIOR ===== -->
    <div class="prov-toolbar">
        <a href="<%=request.getContextPath()%>/ProveedorServlet?action=nuevo"
           class="prov-toolbar__btn-nuevo">
            <i class="fa-solid fa-plus"></i> Agregar Proveedor
        </a>

        <form action="<%=request.getContextPath()%>/ProveedorServlet"
              method="get"
              class="cards__busqueda"
              id="formBusquedaProveedor">
            <input type="hidden" name="action" value="buscar">

            <select name="filtro"
                    id="filtroSelectProveedor"
                    class="cards__busqueda-filtro"
                    onchange="actualizarPlaceholderProveedor(this)">
                <option value="todos"     <%= "todos".equals(filtroActivo)     ? "selected" : "" %>>Todos</option>
                <option value="nombre"    <%= "nombre".equals(filtroActivo)    ? "selected" : "" %>>Nombre</option>
                <option value="documento" <%= "documento".equals(filtroActivo) ? "selected" : "" %>>Documento</option>
            </select>

            <span class="cards__busqueda-sep"></span>

            <input type="text"
                   name="q"
                   id="searchInputProveedor"
                   class="cards__busqueda-input"
                   placeholder="Buscar proveedores..."
                   value="<%= busqueda %>"
                   autocomplete="off">

            <i class="fa-solid fa-magnifying-glass cards__busqueda-icono"></i>

            <% if (!busqueda.isEmpty()) { %>
                <a href="<%=request.getContextPath()%>/ProveedorServlet?action=listar"
                   class="cards__busqueda-clear" title="Limpiar búsqueda">
                    <i class="fa-solid fa-xmark"></i>
                </a>
            <% } %>
        </form>
    </div>

    <!-- ===== INFO BÚSQUEDA ACTIVA ===== -->
    <% if (!busqueda.isEmpty()) { %>
        <div class="prov-busqueda-info">
            <i class="fa-solid fa-circle-info"></i>
            <span>
                <strong><%= proveedores.size() %></strong>
                resultado<%= proveedores.size() != 1 ? "s" : "" %>
                para &ldquo;<em><%= busqueda %></em>&rdquo;
                <% if (!"todos".equals(filtroActivo)) { %>
                    &mdash; filtrado por
                    <span class="prov-badge-filtro">
                        <%= "nombre".equals(filtroActivo) ? "nombre" : "documento" %>
                    </span>
                <% } %>
            </span>
        </div>
    <% } %>

    <% if (proveedores.isEmpty()) { %>
        <div class="prov-empty">
            <i class="fa-solid fa-box-open prov-empty__icon"></i>
            <p class="prov-empty__texto">
                <%= !busqueda.isEmpty()
                    ? "No se encontraron resultados para \"" + busqueda + "\""
                    : "No hay proveedores registrados aún." %>
            </p>
            <% if (busqueda.isEmpty()) { %>
                <a href="<%=request.getContextPath()%>/ProveedorServlet?action=nuevo"
                   class="prov-toolbar__btn-nuevo" style="margin-top:12px;">
                    <i class="fa-solid fa-plus"></i> Agregar el primero
                </a>
            <% } %>
        </div>
    <% } else { %>
        <div class="prov-grid">
            <% for (Proveedor p : proveedores) { %>
            <article class="prov-card <%= !p.isEstado() ? "prov-card--inactivo" : "" %>">

                <!-- Cabecera -->
                <div class="prov-card__header">
                    <div class="prov-card__avatar">
                        <i class="fa-solid fa-building"></i>
                    </div>
                    <div class="prov-card__header-info">
                        <h3 class="prov-card__nombre"><%= p.getNombre() %></h3>
                        <span class="prov-card__doc">
                            <i class="fa-solid fa-id-card"></i>
                            <%= p.getDocumento() != null ? p.getDocumento() : "Sin documento" %>
                        </span>
                    </div>

                    <!-- Badge de estado -->
                    <form method="post"
                          action="<%=request.getContextPath()%>/ProveedorServlet"
                          class="form-estado"
                          id="form-estado-<%= p.getUsuarioId() %>">
                        <input type="hidden" name="action" value="actualizarEstado">
                        <input type="hidden" name="id"     value="<%= p.getUsuarioId() %>">
                        <input type="hidden" name="estado" id="estado-val-<%= p.getUsuarioId() %>"
                               value="<%= p.isEstado() %>">
                        <button type="button"
                                class="estado-badge <%= p.isEstado() ? "estado-activo" : "estado-inactivo" %>"
                                onclick="toggleEstado(<%= p.getUsuarioId() %>, <%= p.isEstado() %>)"
                                title="Clic para cambiar estado">
                            <span class="estado-badge__dot"></span>
                            <span class="estado-badge__label">
                                <%= p.isEstado() ? "Activo" : "Inactivo" %>
                            </span>
                            <i class="fa-solid fa-rotate estado-badge__icon"></i>
                        </button>
                    </form>
                </div>

                <!-- Cuerpo -->
                <div class="prov-card__body">

                    <div class="prov-card__fila">
                        <span class="prov-card__etiqueta">
                            <i class="fa-solid fa-phone"></i> Teléfono(s)
                        </span>
                        <div class="prov-card__valor">
                            <% if (p.getTelefonos() != null && !p.getTelefonos().isEmpty()) {
                                for (String tel : p.getTelefonos()) { %>
                                <span><%= tel %></span>
                            <% } } else { %><span>—</span><% } %>
                        </div>
                    </div>

                    <div class="prov-card__fila">
                        <span class="prov-card__etiqueta">
                            <i class="fa-solid fa-envelope"></i> Correo(s)
                        </span>
                        <div class="prov-card__valor">
                            <% if (p.getCorreos() != null && !p.getCorreos().isEmpty()) {
                                for (String mail : p.getCorreos()) { %>
                                <a href="mailto:<%= mail %>" class="prov-link"><%= mail %></a>
                            <% } } else { %><span>—</span><% } %>
                        </div>
                    </div>

                    <div class="prov-card__fila">
                        <span class="prov-card__etiqueta">
                            <i class="fa-solid fa-gem"></i> Materiales
                        </span>
                        <div class="prov-card__valor prov-card__valor--tags">
                            <% if (p.getMateriales() != null && !p.getMateriales().isEmpty()) {
                                for (model.Material m : p.getMateriales()) { %>
                                <span><%= m.getNombre() %></span>
                            <% } } else { %><span class="prov-muted">—</span><% } %>
                        </div>
                    </div>

                    <div class="prov-card__duo">
                        <div class="prov-card__duo-item">
                            <span class="prov-card__etiqueta">
                                <i class="fa-solid fa-calendar-days"></i> Fecha inicio
                            </span>
                            <span class="prov-card__valor prov-card__valor--dato">
                                <%= p.getFechaInicio() %>
                            </span>
                        </div>
                        <div class="prov-card__duo-item">
                            <span class="prov-card__etiqueta">
                                <i class="fa-solid fa-dollar-sign"></i> Mín. compra
                            </span>
                            <span class="prov-card__valor prov-card__valor--dato prov-card__valor--precio">
                                $<%= String.format("%,.0f", p.getMinimoCompra() != null ? p.getMinimoCompra() : 0) %>
                            </span>
                        </div>
                    </div>
                </div>

                <!-- Pie -->
                <div class="prov-card__footer">
                    
                    <a href="<%=request.getContextPath()%>/ProveedorServlet?action=verCompras&id=<%= p.getUsuarioId() %>"
                       title="Ver producto" class="prov-card__accion prov-card__accion--editar">
                        <i class="fa-solid fa-cart-shopping"></i>Compras
                    </a>
                    <a href="<%=request.getContextPath()%>/ProveedorServlet?action=editar&id=<%= p.getUsuarioId() %>"
                       class="prov-card__accion prov-card__accion--compras">
                        <i class="fa-solid fa-pen-to-square"></i> Editar
                    </a>
                </div>
            </article>
            <% } %>
        </div>
    <% } %>

</main>

<script>
const placeholdersProveedor = {
    todos:     'Buscar por nombre o documento...',
    nombre:    'Ej: Joyeria strawberry...',
    documento: 'Ej: 12345678...'
};

function actualizarPlaceholderProveedor(sel) {
    document.getElementById('searchInputProveedor').placeholder =
        placeholdersProveedor[sel.value] || 'Buscar proveedores...';
}

document.addEventListener('DOMContentLoaded', function () {
    const filtroSelect = document.getElementById('filtroSelectProveedor');
    if (filtroSelect) actualizarPlaceholderProveedor(filtroSelect);

    document.getElementById('searchInputProveedor')
        ?.addEventListener('keydown', e => {
            if (e.key === 'Enter') {
                e.preventDefault();
                document.getElementById('formBusquedaProveedor').submit();
            }
        });

    // Auto-ocultar alertas tras 4s
    setTimeout(() => {
        document.querySelectorAll('.prov-alert').forEach(el => {
            el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            el.style.opacity    = '0';
            el.style.transform  = 'translateY(-8px)';
            setTimeout(() => el.remove(), 600);
        });
    }, 4000);

    // Entrada escalonada de cards
    document.querySelectorAll('.prov-card').forEach((card, i) => {
        card.style.animationDelay = i * 0.07 + 's';
    });
});

function toggleEstado(id, estadoActual) {
    const input = document.getElementById('estado-val-' + id);
    const badge = input.closest('form').querySelector('.estado-badge');
    const icono = badge.querySelector('.estado-badge__icon');

    icono.style.transform  = 'rotate(360deg)';
    icono.style.transition = 'transform 0.4s ease';
    icono.style.opacity    = '1';

    input.value = !estadoActual;
    setTimeout(() => document.getElementById('form-estado-' + id).submit(), 350);
}
</script>
</body>
</html>