<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="model.Usuario"%>
<%
	Object adminSesion = session.getAttribute("admin");
	Object superAdminSesion = session.getAttribute("superadmin");
	if (adminSesion == null && superAdminSesion == null) {
	    response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
	    return;
	}

    // OJO: Aquí recibimos el usuario que el Servlet buscó en la DB
    Usuario usuario = (Usuario) request.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/UsuarioServlet");
        return;
    }

    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar Usuario - AAC27</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/forms-global.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/producto.css">
</head>
<body>
<nav class="navbar-admin">
    <div class="navbar-admin__catalogo"><img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin"></div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/UsuarioServlet" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<main class="fs-container">
    <h2 class="fs-page-title"><i class="fa-solid fa-user-pen"></i> Editar Usuario</h2>

    <% if (error != null) { %>
    <div class="fs-alert-error"><i class="fa-solid fa-circle-exclamation"></i> <%= error %></div>
    <% } %>

    <form id="formEditar" class="fs-form" method="post" action="<%= request.getContextPath() %>/UsuarioServlet">
        <input type="hidden" name="accion" value="editar">
        <input type="hidden" name="id" value="<%= usuario.getUsuarioId() %>">

        <div class="fs-section">
          
        <div class="fs-section">
            <div class="fs-section-title"><i class="fa-solid fa-pen-to-square"></i> Información Editable</div>
            <div class="fs-grid">
                <div class="fs-group">
                    <label class="fs-label" for="nombre"><i class="fa-solid fa-user"></i> Nombre *</label>
                    <input id="nombre" type="text" name="nombre" class="fs-input" required
                           value="<%= usuario.getNombre() != null ? usuario.getNombre() : "" %>">
                </div>
                
                <div class="fs-group">
                    <label class="fs-label" for="correo"><i class="fa-solid fa-envelope"></i> Correo Electrónico *</label>
                    <input id="correo" type="email" name="correo" class="fs-input" required
                           value="<%= usuario.getCorreo() != null ? usuario.getCorreo() : "" %>">
                </div>

                <div class="fs-group">
                    <label class="fs-label" for="telefono"><i class="fa-solid fa-phone"></i> Teléfono *</label>
                    <input id="telefono" type="tel" name="telefono" class="fs-input" required
                           value="<%= usuario.getTelefono() != null ? usuario.getTelefono() : "" %>">
                </div>

                <div class="fs-group">
				    <label class="fs-label" for="rol"><i class="fa-solid fa-user-gear"></i> Rol *</label>
				    <select id="rol" name="rol" class="fs-input" required>
				        <option value="Administrador" <%= "Administrador".equalsIgnoreCase(usuario.getRol()) ? "selected" : "" %>>Administrador</option>
				        <option value="Vendedor"      <%= "Vendedor".equalsIgnoreCase(usuario.getRol())      ? "selected" : "" %>>Vendedor</option>
				    </select>
				</div>
            </div>
        </div>

        <div class="fs-section">
		    <div class="fs-section-title"><i class="fa-solid fa-toggle-on"></i> Estado</div>
		    <div class="fs-radio-group">
		        <label class="fs-radio-chip">
		            <input type="radio" name="estado" value="Activo" <%= usuario.isEstado() ? "checked" : "" %>>
		            <i class="fa-solid fa-circle-check" style="color:#16a34a;"></i> Activo
		        </label>
		        <label class="fs-radio-chip">
		            <input type="radio" name="estado" value="Inactivo" <%= !usuario.isEstado() ? "checked" : "" %>>
		            <i class="fa-solid fa-circle-xmark" style="color:#dc2626;"></i> Inactivo
		        </label>
		    </div>
		</div>

        <div class="fs-actions">
            <button type="submit" class="fs-btn-save"><i class="fa-solid fa-floppy-disk"></i> Guardar Cambios</button>
            <a href="<%= request.getContextPath() %>/UsuarioServlet" class="fs-btn-cancel"><i class="fa-solid fa-xmark"></i> Cancelar</a>
        </div>
    </form>
</main>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
document.getElementById('formEditar').addEventListener('submit', function(e) {
    e.preventDefault();
    Swal.fire({
        title: '¿Guardar cambios?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#7c3aed',
        confirmButtonText: 'Sí, guardar',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            this.submit();
        }
    });
});
</script>
</body>
</html>