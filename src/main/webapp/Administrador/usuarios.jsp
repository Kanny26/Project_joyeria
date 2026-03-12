<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, model.Usuario, java.text.SimpleDateFormat" %>
<%
	Object adminSesion = session.getAttribute("admin");
	Object superAdminSesion = session.getAttribute("superadmin");
	if (adminSesion == null && superAdminSesion == null) {
	    response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
	    return;
	}

    List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");

    // Si se accede directo al JSP sin pasar por el servlet, redirigir correctamente
    if (usuarios == null) {
        response.sendRedirect(request.getContextPath() + "/UsuarioServlet?accion=listar");
        return;
    }

    Integer totalUsuariosObj   = (Integer) request.getAttribute("totalUsuarios");
    Integer usuariosActivosObj = (Integer) request.getAttribute("usuariosActivos");
    int totalUsuarios   = (totalUsuariosObj   != null) ? totalUsuariosObj   : usuarios.size();
    int usuariosActivos = (usuariosActivosObj != null) ? usuariosActivosObj : 0;
    int usuariosInactivos = totalUsuarios - usuariosActivos;

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Usuarios | AAC27 Admin</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/usuarios/listar_usuario.css">
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

    <%-- ── ENCABEZADO ── --%>
    <div class="page-header">
        <div class="page-header__left">
            <div class="page-header__icon"><i class="fa-solid fa-users"></i></div>
            <div>
                <h1>Usuarios</h1>
                <p><%= totalUsuarios %> usuario<%= totalUsuarios != 1 ? "s" : "" %> registrado<%= totalUsuarios != 1 ? "s" : "" %></p>
            </div>
        </div>
        <div class="page-header__right">
            <div class="stat-pills">
                <span class="pill pill--success"><i class="fa-solid fa-circle-check"></i> <%= usuariosActivos %> activo<%= usuariosActivos != 1 ? "s" : "" %></span>
                <span class="pill pill--danger"><i class="fa-solid fa-circle-xmark"></i> <%= usuariosInactivos %> inactivo<%= usuariosInactivos != 1 ? "s" : "" %></span>
            </div>
            <a href="<%= request.getContextPath() %>/UsuarioServlet?accion=nuevo" class="btn-agregar">
                <i class="fa-solid fa-user-plus"></i> Agregar usuario
            </a>
        </div>
    </div>

    <%-- ── MENSAJES ── --%>
    <%
        String msg = (String) request.getAttribute("msg");
        if (msg == null) msg = request.getParameter("msg");
    %>
    <% if ("creado".equals(msg)) { %>
        <div class="alerta alerta--success"><i class="fa-solid fa-circle-check"></i> Usuario creado exitosamente.</div>
    <% } else if ("actualizado".equals(msg)) { %>
        <div class="alerta alerta--success"><i class="fa-solid fa-circle-check"></i> Usuario actualizado correctamente.</div>
    <% } %>

    <%-- ── BARRA DE BÚSQUEDA Y FILTROS ── --%>
    <div class="filtros-bar">
        <div class="search-wrap">
            <i class="fa-solid fa-magnifying-glass"></i>
            <input type="text" id="buscador" placeholder="Buscar por nombre, correo, rol...">
        </div>
        <div class="filter-btns">
            <button class="filter-btn active" data-filtro="todos">Todos</button>
            <button class="filter-btn" data-filtro="activo">Activos</button>
            <button class="filter-btn" data-filtro="inactivo">Inactivos</button>
            <button class="filter-btn" data-filtro="administrador">Administradores</button>
            <button class="filter-btn" data-filtro="vendedor">Vendedores</button>
        </div>
    </div>

    <%-- ── GRID DE CARDS ── --%>
    <% if (usuarios.isEmpty()) { %>
        <div class="empty-state">
            <div class="empty-state__icon"><i class="fa-solid fa-users-slash"></i></div>
            <h3>Sin usuarios registrados</h3>
            <p>Empieza agregando el primer usuario del sistema.</p>
            <a href="<%= request.getContextPath() %>/UsuarioServlet?accion=nuevo" class="btn-agregar btn-agregar--centered">
                <i class="fa-solid fa-user-plus"></i> Agregar primer usuario
            </a>
        </div>
    <% } else { %>

        <div class="usuarios-grid" id="usuariosGrid">
        <%
            for (Usuario u : usuarios) {
                String rolRaw    = (u.getRol() != null) ? u.getRol() : "";
                String rolSlug   = rolRaw.toLowerCase().trim();
                String estadoSlug = u.isEstado() ? "activo" : "inactivo";
                String nombreRaw  = (u.getNombre() != null) ? u.getNombre() : "";
                String iniciales  = nombreRaw.trim().length() >= 2
                    ? nombreRaw.trim().substring(0, 2).toUpperCase()
                    : (nombreRaw.trim().length() == 1 ? nombreRaw.trim().toUpperCase() : "?");
        %>
            <div class="usuario-card"
                 data-nombre="<%= nombreRaw.toLowerCase() %>"
                 data-correo="<%= (u.getCorreo() != null ? u.getCorreo().toLowerCase() : "") %>"
                 data-rol="<%= rolSlug %>"
                 data-estado="<%= estadoSlug %>">

                <%-- Avatar + punto de estado --%>
                <div class="usuario-card__avatar-wrap">
                    <div class="usuario-card__avatar <%= rolSlug.contains("admin") ? "avatar--admin" : rolSlug.contains("vendedor") ? "avatar--vendedor" : "avatar--otro" %>">
                        <%= iniciales %>
                    </div>
                    <span class="estado-dot estado-dot--<%= estadoSlug %>" title="<%= u.isEstado() ? "Activo" : "Inactivo" %>"></span>
                </div>

                <%-- Nombre y rol --%>
                <div class="usuario-card__identity">
                    <h3 class="usuario-card__nombre"><%= nombreRaw.isEmpty() ? "Sin nombre" : nombreRaw %></h3>
                    <span class="rol-badge <%= rolSlug.contains("admin") ? "rol-badge--admin" : rolSlug.contains("vendedor") ? "rol-badge--vendedor" : "rol-badge--otro" %>">
                        <% if (rolSlug.contains("admin")) { %>
                            <i class="fa-solid fa-shield-halved"></i>
                        <% } else if (rolSlug.contains("vendedor")) { %>
                            <i class="fa-solid fa-tag"></i>
                        <% } else { %>
                            <i class="fa-solid fa-circle-user"></i>
                        <% } %>
                        <%= rolRaw.isEmpty() ? "Sin rol" : rolRaw %>
                    </span>
                </div>

                <%-- Info de contacto --%>
                <div class="usuario-card__info">
                    <% if (u.getCorreo() != null && !u.getCorreo().isBlank()) { %>
                    <div class="info-row">
                        <i class="fa-solid fa-envelope"></i>
                        <span><%= u.getCorreo() %></span>
                    </div>
                    <% } %>
                    <% if (u.getTelefono() != null && !u.getTelefono().isBlank()) { %>
                    <div class="info-row">
                        <i class="fa-solid fa-phone"></i>
                        <span><%= u.getTelefono() %></span>
                    </div>
                    <% } %>
                    <% if (u.getFechaCreacion() != null) { %>
                    <div class="info-row">
                        <i class="fa-solid fa-calendar-days"></i>
                        <span>Desde <%= sdf.format(u.getFechaCreacion()) %></span>
                    </div>
                    <% } %>
                </div>

                <%-- Footer: estado + botón editar --%>
                <div class="usuario-card__footer">
                    <span class="estado-pill estado-pill--<%= estadoSlug %>">
                        <%= u.isEstado() ? "Activo" : "Inactivo" %>
                    </span>
                    <a href="<%= request.getContextPath() %>/UsuarioServlet?accion=editar&id=<%= u.getUsuarioId() %>"
                       class="btn-editar">
                        <i class="fa-solid fa-pen-to-square"></i> Editar
                    </a>
                </div>

            </div>
        <% } %>
        </div><%-- fin usuarios-grid --%>

        <p class="sin-resultados" id="sinResultados" style="display:none;">
            <i class="fa-solid fa-magnifying-glass"></i> No se encontraron usuarios con ese criterio.
        </p>

    <% } %>

</main>

<script>
(function () {
    var buscador    = document.getElementById('buscador');
    var grid        = document.getElementById('usuariosGrid');
    var sinRes      = document.getElementById('sinResultados');
    var filtroActivo = 'todos';

    function aplicar() {
        if (!grid) return;
        var q = buscador ? buscador.value.toLowerCase() : '';
        var visibles = 0;

        grid.querySelectorAll('.usuario-card').forEach(function (card) {
            var nombreOk = !q || card.dataset.nombre.includes(q) || card.dataset.correo.includes(q) || card.dataset.rol.includes(q);
            var filtroOk = true;
            if (filtroActivo === 'activo')         filtroOk = card.dataset.estado === 'activo';
            else if (filtroActivo === 'inactivo')  filtroOk = card.dataset.estado === 'inactivo';
            else if (filtroActivo === 'administrador') filtroOk = card.dataset.rol.includes('admin');
            else if (filtroActivo === 'vendedor')  filtroOk = card.dataset.rol.includes('vendedor');

            var mostrar = nombreOk && filtroOk;
            card.style.display = mostrar ? '' : 'none';
            if (mostrar) visibles++;
        });

        if (sinRes) sinRes.style.display = visibles === 0 ? 'flex' : 'none';
    }

    if (buscador) buscador.addEventListener('input', aplicar);

    document.querySelectorAll('.filter-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            document.querySelectorAll('.filter-btn').forEach(function (b) { b.classList.remove('active'); });
            btn.classList.add('active');
            filtroActivo = btn.dataset.filtro;
            aplicar();
        });
    });
}());
</script>
</body>
</html>
