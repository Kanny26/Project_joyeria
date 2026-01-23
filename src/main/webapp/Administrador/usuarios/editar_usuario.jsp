<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Usuario" %>
<%
    Usuario usuario = (Usuario) request.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/UsuarioServlet");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar Usuario</title>
    
    <!-- Fuentes e íconos -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    
    <!-- CSS base y específico -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/editar.css">
</head>
<body>

    <!-- Navbar igual al de producto -->
    <nav class="navbar-admin"> 
        <div class="navbar-admin__catalogo"> 
            <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin"> 
        </div> 
        <h1 class="navbar-admin__title">AAC27</h1> 
        <a href="<%= request.getContextPath() %>/UsuarioServlet">
            <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i> 
        </a>
    </nav>

    <main class="editar-producto">
        <h1 class="editar-producto__titulo">Editar usuario</h1>
        
        <!-- Formulario POST con contexto correcto -->
        <form class="editar-producto__detalles" 
              action="<%= request.getContextPath() %>/UsuarioServlet" 
              method="post">
              
            <input type="hidden" name="accion" value="editar">
            <input type="hidden" name="id" value="<%= usuario.getUsuarioId() %>">

            <div class="editar-producto__contenido">
                <!-- Sin imagen (usuarios no tienen foto), pero podrías dejar un placeholder o icono -->
                <div class="editar-producto__imagen">
                    <i class="fa-solid fa-user fa-5x" style="color: #ccc;"></i>
                    <!-- Opcional: si más adelante añades foto de perfil -->
                </div>

                <!-- Campos del formulario -->
                <div class="editar-producto__campos">

				    <label>Nombre</label>
				    <input type="text" name="nombre" 
				           value="<%= usuario.getNombre() != null ? usuario.getNombre() : "" %>" 
				           class="editar-producto__input-texto" required>
				
				    <label>Correo electrónico</label>
				    <input type="email" name="correo" 
				           value="<%= usuario.getCorreo() != null ? usuario.getCorreo() : "" %>" 
				           class="editar-producto__input-texto" required>
				
				    <label>Teléfono</label>
				    <input type="tel" name="telefono" 
				           value="<%= usuario.getTelefono() != null ? usuario.getTelefono() : "" %>" 
				           class="editar-producto__input-texto" required>
				
				    <label>Rol</label>
				    <select name="rol" class="editar-producto__input-select" required>
				        <option value="Administrador" <%= "Administrador".equals(usuario.getRol()) ? "selected" : "" %>>Administrador</option>
				        <option value="Vendedor" <%= "Vendedor".equals(usuario.getRol()) ? "selected" : "" %>>Vendedor</option>
				        <option value="Proveedor" <%= "Proveedor".equals(usuario.getRol()) ? "selected" : "" %>>Proveedor</option>
				        <option value="Cliente" <%= "Cliente".equals(usuario.getRol()) ? "selected" : "" %>>Cliente</option>
				    </select>
				
				    <label>Estado</label>
				    <select name="estado" class="editar-producto__input-select">
				        <option value="Activo" <%= usuario.isEstado() ? "selected" : "" %>>Activo</option>
				        <option value="Inactivo" <%= !usuario.isEstado() ? "selected" : "" %>>Inactivo</option>
				    </select>
				
				    <label>Observaciones de desempeño</label>
				    <textarea name="observaciones" class="editar-producto__input-area" rows="3"><%= "" %></textarea>
				
				    <!-- Acciones: Guardar y Cancelar -->
				    <div class="editar-producto__acciones">
				        <button type="submit" class="boton guardar">
				            <i class="fa-solid fa-floppy-disk"></i> Guardar
				        </button>
				        <a href="<%= request.getContextPath() %>/UsuarioServlet" class="boton cancelar">
				            <i class="fa-solid fa-xmark"></i> Cancelar
				        </a>
				    </div>
				</div>

            </div>
        </form>
    </main>
</body>
</html>