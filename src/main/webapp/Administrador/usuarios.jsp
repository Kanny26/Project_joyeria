<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, model.Usuario" %>
<%
    // Atributos enviados por el servlet
    Integer totalUsuariosObj = (Integer) request.getAttribute("totalUsuarios");
    Integer usuariosActivosObj = (Integer) request.getAttribute("usuariosActivos");
    List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");

    int totalUsuarios = (totalUsuariosObj != null) ? totalUsuariosObj : 0;
    int usuariosActivos = (usuariosActivosObj != null) ? usuariosActivosObj : 0;
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Usuarios</title>

    <!-- Estilos -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/usuarios.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<!-- Navbar -->
<nav class="navbar-admin"> 
    <div class="navbar-admin__catalogo"> 
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div> 
    
    <h1 class="navbar-admin__title">AAC27</h1> 
    <a href="<%= request.getContextPath() %>/Administrador/admin-principal.jsp">
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i> 
    </a>
</nav>

<main class="titulo">
    <h2 class="titulo__encabezado">Gestionar usuarios</h2>

    <section class="iconos-contenedor">
	
	    <!-- Agregar usuario -->
	    <article class="iconos-item">
	        <a href="<%= request.getContextPath() %>/UsuarioServlet?accion=nuevo"
	           class="icono-boton">
	
	            <div class="icono-boton__circulo">
	                <img class="icono-boton__img"
	                     src="<%= request.getContextPath() %>/assets/Imagenes/iconos/agregar-usuario.png"
	                     alt="Agregar usuario">
	            </div>
	
	            <h3 class="icono-boton__titulo">Agregar usuario</h3>
	        </a>
	    </article>
	
	    <!-- Listar usuarios -->
	    <article class="iconos-item">
	        <a href="<%= request.getContextPath() %>/UsuarioServlet?accion=listar"
	           class="icono-boton">
	
	            <div class="icono-boton__circulo">
	                <img class="icono-boton__img"
	                     src="<%= request.getContextPath() %>/assets/Imagenes/iconos/Usuarios.png"
	                     alt="Listar usuarios">
	            </div>
	
	            <h3 class="icono-boton__titulo">Listar usuarios</h3>
	        </a>
	    </article>
	
	    <!-- Historial -->
	    <article class="iconos-item">
	        <a href="<%= request.getContextPath() %>/DesempenoServlet"
	           class="icono-boton">
	
	            <div class="icono-boton__circulo">
	                <img class="icono-boton__img"
	                     src="<%= request.getContextPath() %>/assets/Imagenes/iconos/historial.png"
	                     alt="Historial de usuarios">
	            </div>
	
	            <h3 class="icono-boton__titulo">Historial</h3>
	        </a>
	    </article>
	
	</section>


    <!-- Tabla de usuarios (solo si estamos listando) -->
    <%
        if (usuarios != null && !usuarios.isEmpty()) {
    %>
    <section class="usuarios-admin__tabla">
        <h3>Listado de Usuarios</h3>
        <table border="1">
            <thead>
                <tr>
                    <th>Id</th>
                    <th>Nombre</th>
                    <th>Correo</th>
                    <th>Teléfono</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                </tr>
            </thead>
            <tbody>
                <%
                    for (Usuario u : usuarios) {
                %>
                <tr>
                    <td><%= u.getUsuarioId() %></td>
                    <td><%= u.getNombre() %></td>
                    <td><%= u.getCorreo() != null ? u.getCorreo() : "-" %></td>
                    <td><%= u.getTelefono() != null ? u.getTelefono() : "-" %></td>
                    <td><%= u.isEstado() ? "Activo" : "Inactivo" %></td>
                    <td>
                        <a href="<%= request.getContextPath() %>/UsuarioServlet?accion=editar&id=<%= u.getUsuarioId() %>">Editar</a>
                    </td>
                </tr>
                <%
                    }
                %>
            </tbody>
        </table>
    </section>
    <%
        }
    %>
    
    <!-- Contadores -->
    <section class="contadores">
        <div class="contador-card">
            <p>Total Usuarios: <%= totalUsuarios %></p>
            <p>Usuarios Activos: <%= usuariosActivos %></p>
        </div>
    </section>
</main>

</body>
</html>


