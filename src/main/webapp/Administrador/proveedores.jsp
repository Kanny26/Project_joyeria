<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.Proveedor, model.Material, model.Administrador" %>
<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }

    List<Proveedor> proveedores  = (List<Proveedor>) request.getAttribute("proveedores");
    String          termino      = (String)          request.getAttribute("terminoBusqueda");
    String          filtroActivo = (String)          request.getAttribute("filtroActivo");

    if (termino      == null) termino      = "";
    if (filtroActivo == null) filtroActivo = "todos";

    int totalProveedores = proveedores != null ? proveedores.size() : 0;
    int totalActivos     = 0;
    if (proveedores != null) {
        for (Proveedor p : proveedores) {
            if (Boolean.TRUE.equals(p.isEstado())) totalActivos++;
        }
    }

    String msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Proveedores — AAC27</title>
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
    <a href="<%=request.getContextPath()%>/Administrador/admin-principal.jsp" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<main class="prov-page">

    <h2 class="prov-page__titulo">Gestión de Proveedores</h2>

    <%-- ══ CONTADORES ══ --%>
    <div class="prov-header-stats">
        <div class="prov-stat-card">
            <span class="prov-stat-card__label">Total Proveedores</span>
            <span class="prov-stat-card__num"><%= totalProveedores %></span>
        </div>
        <div class="prov-stat-card">
            <span class="prov-stat-card__label">Proveedores Activos</span>
            <span class="prov-stat-card__num"><%= totalActivos %></span>
        </div>
    </div>

    <%-- ══ ALERTAS ══ --%>
    <% if ("creado".equals(msg)) { %>
        <div class="prov-alert prov-alert--success"><i class="fa-solid fa-circle-check"></i> Proveedor creado exitosamente.</div>
    <% } else if ("actualizado".equals(msg)) { %>
        <div class="prov-alert prov-alert--success"><i class="fa-solid fa-circle-check"></i> Proveedor actualizado correctamente.</div>
    <% } else if ("eliminado".equals(msg)) { %>
        <div class="prov-alert prov-alert--success"><i class="fa-solid fa-circle-check"></i> Proveedor eliminado correctamente.</div>
    <% } %>

    <%-- ══ BARRA DE FILTROS — idéntica al diseño de productos ══ --%>
    <div class="filtros-bar">

        <div class="search-wrap" id="searchWrap">
            <i class="fa-solid fa-magnifying-glass"></i>
            <input type="text" id="buscadorTexto"
                   placeholder="Buscar por nombre o material..."
                   value="<%= !"material".equals(filtroActivo) ? termino : "" %>">
        </div>

        <div class="search-wrap search-wrap--select" id="wrapMaterial" style="display:none;">
            <i class="fa-solid fa-gem"></i>
            <select id="selectMaterial" class="search-select">
                <option value="">— Seleccionar material —</option>
                <%
                    java.util.Set<String> materialesUnicos = new java.util.LinkedHashSet<>();
                    if (proveedores != null) {
                        for (Proveedor pv : proveedores) {
                            if (pv.getMateriales() != null) {
                                for (Material mat : pv.getMateriales()) {
                                    if (mat != null && mat.getNombre() != null && !mat.getNombre().isEmpty()) {
                                        materialesUnicos.add(mat.getNombre());
                                    }
                                }
                            }
                        }
                    }
                    for (String matNombre : materialesUnicos) {
                %>
                    <option value="<%= matNombre %>"
                        <%= matNombre.equalsIgnoreCase(termino) && "material".equals(filtroActivo) ? "selected" : "" %>>
                        <%= matNombre %>
                    </option>
                <% } %>
            </select>
        </div>

        <form id="formBusqueda" action="<%=request.getContextPath()%>/ProveedorServlet" method="get" style="display:none;">
            <input type="hidden" name="action" value="listar">
            <input type="hidden" name="filtro" id="hiddenFiltro" value="<%= filtroActivo %>">
            <input type="hidden" name="q"      id="hiddenQ"      value="<%= termino %>">
        </form>

        <div class="filter-btns">
            <a href="<%=request.getContextPath()%>/ProveedorServlet?action=nuevo" class="btn-agregar-prov">
                <i class="fa-solid fa-plus"></i> Agregar Proveedor
            </a>
            <button class="filter-btn <%= "todos".equals(filtroActivo)    ? "active" : "" %>" data-filtro="todos">Todos</button>
            <button class="filter-btn <%= "nombre".equals(filtroActivo)   ? "active" : "" %>" data-filtro="nombre"><i class="fa-solid fa-user"></i> Nombre</button>
            <button class="filter-btn <%= "material".equals(filtroActivo) ? "active" : "" %>" data-filtro="material"><i class="fa-solid fa-gem"></i> Material</button>
        </div>
    </div>

    <% if (!termino.isEmpty()) { %>
    <div class="prov-busqueda-info">
        <i class="fa-solid fa-circle-info"></i>
        <span>
            <strong><%= totalProveedores %></strong> resultado<%= totalProveedores != 1 ? "s" : "" %>
            para &ldquo;<em><%= termino %></em>&rdquo;
            <% if (!"todos".equals(filtroActivo)) { %>
                &nbsp;·&nbsp; filtrado por
                <span class="prov-badge-filtro"><%= "nombre".equals(filtroActivo) ? "nombre" : "material" %></span>
            <% } %>
        </span>
        <a href="<%=request.getContextPath()%>/ProveedorServlet?action=listar" class="resultado-limpiar">
            <i class="fa-solid fa-xmark"></i> Limpiar
        </a>
    </div>
    <% } %>

    <%-- ══ GRID ══ --%>
    <% if (proveedores == null || proveedores.isEmpty()) { %>
    <div class="prov-empty">
        <div class="prov-empty__icon"><i class="fa-solid fa-store-slash"></i></div>
        <p class="prov-empty__texto">
            <% if (!termino.isEmpty()) { %>
                No se encontraron proveedores para &ldquo;<strong><%= termino %></strong>&rdquo;.
            <% } else { %>
                Aún no hay proveedores registrados.
            <% } %>
        </p>
    </div>
    <% } else { %>
    <div class="prov-grid">
        <% for (Proveedor p : proveedores) {
               boolean activo = Boolean.TRUE.equals(p.isEstado());
        %>
        <div class="prov-card <%= activo ? "" : "prov-card--inactivo" %>">

            <%-- ── Cabecera ── --%>
            <div class="prov-card__header">
                <div class="prov-card__avatar"><i class="fa-solid fa-store"></i></div>
                <div class="prov-card__header-info">
                    <div class="prov-card__nombre"><%= p.getNombre() %></div>
                    <% if (p.getDocumento() != null && !p.getDocumento().isEmpty()) { %>
                    <div class="prov-card__doc">
                        <i class="fa-solid fa-id-card"></i> <%= p.getDocumento() %>
                    </div>
                    <% } %>
                </div>
                <div class="form-estado">
                    <form action="<%=request.getContextPath()%>/ProveedorServlet" method="post" style="display:inline;">
                        <input type="hidden" name="action"       value="toggleEstado">
                        <input type="hidden" name="id"           value="<%= p.getProveedorId() %>">
                        <input type="hidden" name="estadoActual" value="<%= activo ? "1" : "0" %>">
                        <button type="submit" class="estado-badge <%= activo ? "estado-activo" : "estado-inactivo" %>">
                            <span class="estado-badge__dot"></span>
                            <%= activo ? "Activo" : "Inactivo" %>
                            <i class="fa-solid <%= activo ? "fa-toggle-on" : "fa-toggle-off" %> estado-badge__icon"></i>
                        </button>
                    </form>
                </div>
            </div>

            <%-- ── Cuerpo ── --%>
            <div class="prov-card__body">

                <%-- Teléfonos --%>
                <% if (p.getTelefonos() != null && !p.getTelefonos().isEmpty()) { %>
                <div class="prov-card__fila">
                    <div class="prov-card__etiqueta"><i class="fa-solid fa-phone"></i> Contacto</div>
                    <div class="prov-card__valor">
                        <% for (String tel : p.getTelefonos()) {
                               if (tel != null && !tel.isEmpty()) { %>
                            <span class="prov-dato"><%= tel %></span>
                        <%     } } %>
                    </div>
                </div>
                <% } %>

                <%-- Correos --%>
                <% if (p.getCorreos() != null && !p.getCorreos().isEmpty()) { %>
                <div class="prov-card__fila">
                    <div class="prov-card__etiqueta"><i class="fa-solid fa-envelope"></i> Correo</div>
                    <div class="prov-card__valor">
                        <% for (String correo : p.getCorreos()) {
                               if (correo != null && !correo.isEmpty()) { %>
                            <a class="prov-link" href="mailto:<%= correo %>"><%= correo %></a>
                        <%     } } %>
                    </div>
                </div>
                <% } %>

                <%-- Materiales — solo texto, sin rectángulos de color --%>
                <% if (p.getMateriales() != null && !p.getMateriales().isEmpty()) { %>
                <div class="prov-card__fila">
                    <div class="prov-card__etiqueta"><i class="fa-solid fa-gem"></i> Materiales</div>
                    <div class="prov-card__valor">
                        <%
                            StringBuilder mats = new StringBuilder();
                            for (Material mat : p.getMateriales()) {
                                if (mat != null && mat.getNombre() != null) {
                                    if (mats.length() > 0) mats.append(", ");
                                    mats.append(mat.getNombre());
                                }
                            }
                        %>
                        <span class="prov-dato"><%= mats.toString() %></span>
                    </div>
                </div>
                <% } %>

                <%-- Fecha registro + pedido mínimo --%>
                <div class="prov-card__duo">
                    <div class="prov-card__duo-item">
                        <div class="prov-card__etiqueta"><i class="fa-solid fa-calendar-plus"></i> Registro</div>
                        <div class="prov-card__valor prov-card__valor--dato">
                            <%= p.getFechaRegistro() != null ? p.getFechaRegistro() : "—" %>
                        </div>
                    </div>
                    <% if (p.getMinimoCompra() != null) { %>
                    <div class="prov-card__duo-item">
                        <div class="prov-card__etiqueta"><i class="fa-solid fa-boxes-stacked"></i> Pedido mín.</div>
                        <div class="prov-card__valor prov-card__valor--precio">
                            $<%= String.format("%,.0f", p.getMinimoCompra()) %>
                        </div>
                    </div>
                    <% } %>
                </div>
            </div>

            <%-- ── Pie: solo Compras y Editar (sin Eliminar) ── --%>
            <div class="prov-card__footer">
                <a href="<%=request.getContextPath()%>/ProveedorServlet?action=verCompras&id=<%= p.getProveedorId() %>"
                   class="prov-card__accion prov-card__accion--compras">
                    <i class="fa-solid fa-file-invoice-dollar"></i> Compras
                </a>
                <a href="<%=request.getContextPath()%>/ProveedorServlet?action=editar&id=<%= p.getProveedorId() %>"
                   class="prov-card__accion prov-card__accion--editar">
                    <i class="fa-solid fa-pen-to-square"></i> Editar
                </a>
            </div>

        </div>
        <% } %>
    </div>
    <% } %>

</main>

<script>
(function () {
    var filtroActivo   = '<%= filtroActivo %>';
    var searchWrap     = document.getElementById('searchWrap');
    var wrapMaterial   = document.getElementById('wrapMaterial');
    var buscadorTexto  = document.getElementById('buscadorTexto');
    var selectMaterial = document.getElementById('selectMaterial');
    var hiddenFiltro   = document.getElementById('hiddenFiltro');
    var hiddenQ        = document.getElementById('hiddenQ');
    var formBusqueda   = document.getElementById('formBusqueda');

    function mostrarInput(filtro) {
        searchWrap.style.display   = (filtro === 'todos' || filtro === 'nombre') ? '' : 'none';
        wrapMaterial.style.display = filtro === 'material' ? '' : 'none';
        setTimeout(function () {
            if (filtro === 'todos' || filtro === 'nombre') buscadorTexto.focus();
            else if (filtro === 'material') selectMaterial.focus();
        }, 60);
    }

    function enviar(filtro, valor) {
        hiddenFiltro.value = filtro;
        hiddenQ.value      = valor || '';
        formBusqueda.submit();
    }

    mostrarInput(filtroActivo);

    document.querySelectorAll('.filter-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            document.querySelectorAll('.filter-btn').forEach(function (b) { b.classList.remove('active'); });
            btn.classList.add('active');
            filtroActivo = btn.dataset.filtro;
            mostrarInput(filtroActivo);
            if (filtroActivo === 'todos') enviar('todos', '');
        });
    });

    buscadorTexto.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            enviar(filtroActivo === 'nombre' ? 'nombre' : 'todos', buscadorTexto.value.trim());
        }
    });

    selectMaterial.addEventListener('change', function () {
        if (this.value) enviar('material', this.value);
    });
}());
</script>
</body>
</html>
