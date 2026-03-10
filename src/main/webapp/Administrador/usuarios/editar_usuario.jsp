<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="model.Usuario"%>
<%
    Object adm = session.getAttribute("admin");
    if (adm == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }

    Usuario usuario = (Usuario) request.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/UsuarioServlet");
        return;
    }

    // Funciona tanto si el admin en sesión es model.Administrador como model.Usuario
    boolean esMismoUsuario = false;
    if (adm instanceof model.Administrador) {
        model.Administrador adminLogueado = (model.Administrador) adm;
        esMismoUsuario = (adminLogueado.getId() == usuario.getUsuarioId());
    } else if (adm instanceof model.Usuario) {
        model.Usuario adminLogueado = (model.Usuario) adm;
        esMismoUsuario = (adminLogueado.getUsuarioId() == usuario.getUsuarioId());
    }

    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
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

    <form id="formEditar" class="fs-form" method="post"
          action="<%= request.getContextPath() %>/UsuarioServlet">
        <input type="hidden" name="accion" value="editar">
        <input type="hidden" name="id" value="<%= usuario.getUsuarioId() %>">

        <!-- SECCIÓN: Datos no editables (RF08) -->
        <div class="fs-section">
            <div class="fs-section-title"><i class="fa-solid fa-lock"></i></div>
            <div class="fs-grid">
                <div class="fs-group">
                    <label class="fs-label"><i class="fa-solid fa-id-card"></i> Documento</label>
                    <div class="fs-readonly">
                        <i class="fa-solid fa-lock" style="color:#d1d5db;font-size:0.8rem;"></i>
                        <%= usuario.getDocumento() != null ? usuario.getDocumento() : "Sin documento" %>
                    </div>
                    <span class="fs-readonly-badge"><i class="fa-solid fa-circle-info"></i> Campo inmutable</span>
                </div>
                <div class="fs-group">
                    <label class="fs-label"><i class="fa-solid fa-envelope"></i> Correo Electrónico</label>
                    <div class="fs-readonly">
                        <i class="fa-solid fa-lock" style="color:#d1d5db;font-size:0.8rem;"></i>
                        <%= usuario.getCorreo() != null ? usuario.getCorreo() : "" %>
                    </div>
                    <span class="fs-readonly-badge"><i class="fa-solid fa-circle-info"></i> Campo inmutable</span>
                    <%-- El correo se muestra como div readonly, así que lo enviamos como hidden --%>
                    <input type="hidden" name="correo" value="<%= usuario.getCorreo() != null ? usuario.getCorreo() : "" %>">
                </div>
            </div>
        </div>

        <!-- SECCIÓN: Datos editables -->
        <div class="fs-section">
            <div class="fs-section-title"><i class="fa-solid fa-sliders"></i> Datos Editables</div>
            <div class="fs-grid">
                <div class="fs-group">
                    <label class="fs-label" for="nombre"><i class="fa-solid fa-user"></i> Nombre *</label>
                    <input id="nombre" type="text" name="nombre" class="fs-input" required
                           value="<%= usuario.getNombre() != null ? usuario.getNombre() : "" %>">
                </div>
                <div class="fs-group">
                    <label class="fs-label" for="telefono"><i class="fa-solid fa-phone"></i> Teléfono *</label>
                    <input id="telefono" type="tel" name="telefono" class="fs-input" required
                           value="<%= usuario.getTelefono() != null ? usuario.getTelefono() : "" %>">
                </div>
                <div class="fs-group">
				    <label class="fs-label" for="rol"><i class="fa-solid fa-user-gear"></i> Rol *</label>
				    
				    <% if (esMismoUsuario) { %>
				        <div class="fs-readonly">
				            <i class="fa-solid fa-lock" style="color:#d1d5db;font-size:0.8rem;"></i>
				            <%= usuario.getRol() %>
				        </div>
				        <span class="fs-readonly-badge"><i class="fa-solid fa-circle-info"></i> No puedes cambiar tu propio rol</span>
				        <input type="hidden" name="rol" value="<%= usuario.getRol() %>">
				    <% } else { %>
				        <select id="rol" name="rol" class="fs-input" required>
				            <option value="Administrador" <%= "Administrador".equals(usuario.getRol()) ? "selected" : "" %>>Administrador</option>
				            <option value="Vendedor"      <%= "Vendedor".equals(usuario.getRol())      ? "selected" : "" %>>Vendedor</option>
				        </select>
				    <% } %>
				</div>
            </div>
        </div>

        <!-- SECCIÓN: Estado -->
		<div class="fs-section">
		    <div class="fs-section-title"><i class="fa-solid fa-toggle-on"></i> Estado del Usuario</div>
		    <div class="fs-radio-group">
		        <% if (esMismoUsuario) { %>
		            <div class="fs-readonly">
		                <i class="fa-solid fa-lock" style="color:#d1d5db;font-size:0.8rem;"></i>
		                Activo
		            </div>
		            <span class="fs-readonly-badge"><i class="fa-solid fa-circle-info"></i> No puedes inactivarte a ti mismo</span>
		            <input type="hidden" name="estado" value="Activo">
		        <% } else { %>
		            <label class="fs-radio-chip">
		                <input type="radio" name="estado" value="Activo" <%= usuario.isEstado() ? "checked" : "" %>>
		                <i class="fa-solid fa-circle-check" style="color:#16a34a;"></i> Activo
		            </label>
		            <label class="fs-radio-chip">
		                <input type="radio" name="estado" value="Inactivo" <%= !usuario.isEstado() ? "checked" : "" %>>
		                <i class="fa-solid fa-circle-xmark" style="color:#dc2626;"></i> Inactivo
		            </label>
		        <% } %>
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
    const nombre = document.getElementById('nombre').value.trim();
    const tel    = document.getElementById('telefono').value.trim();
    if (!nombre) { Swal.fire({ icon:'warning', title:'Campo requerido', text:'El nombre no puede estar vacío.' }); return; }
    if (!tel)    { Swal.fire({ icon:'warning', title:'Campo requerido', text:'El teléfono no puede estar vacío.' }); return; }
    const form = this;
    Swal.fire({
        title:'¿Guardar cambios?', text:'Se actualizará la información del usuario.',
        icon:'question', showCancelButton:true,
        confirmButtonColor:'#7c3aed', cancelButtonColor:'#6b7280',
        confirmButtonText:'Sí, guardar', cancelButtonText:'Revisar'
    }).then(r => {
        if (r.isConfirmed) { Swal.fire({ title:'Guardando...', allowOutsideClick:false, didOpen:()=>Swal.showLoading() }); form.submit(); }
    });
});
<% if (esMismoUsuario) { %>
document.addEventListener('DOMContentLoaded', function() {
    Swal.fire({
        icon: 'info',
        title: 'Restricción de seguridad',
        html: 'No puedes cambiar <strong>tu propio rol</strong> ni <strong>inactivarte</strong> a ti mismo.<br>Estos campos son de solo lectura para tu cuenta.',
        confirmButtonColor: '#7c3aed',
        confirmButtonText: 'Entendido'
    });
});
<% } %>
</script>
</body>
</html>
