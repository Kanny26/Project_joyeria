<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, model.Usuario" %>

<%
	List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");
	if (usuarios == null) usuarios = new ArrayList<>();
    Integer totalUsuariosObj = (Integer) request.getAttribute("totalUsuarios");
    Integer usuariosActivosObj = (Integer) request.getAttribute("usuariosActivos");

    int totalUsuarios = (totalUsuariosObj != null) ? totalUsuariosObj : 0;
    int usuariosActivos = (usuariosActivosObj != null) ? usuariosActivosObj : 0;
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Listar usuarios</title>

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
    <a href="<%= request.getContextPath() %>/Administrador/usuarios.jsp">
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
    </a>
</nav>

<main class="Usuarios-listar">
    <h2 class="Usuarios-listar__titulo">Listar Usuarios</h2>

    <section class="Usuarios-listar__tabla-contenedor">
        <table class="Usuarios-listar__tabla">
            <thead>
                <tr>
                    <th>Codigo</th>
                    <th>Nombre</th>
                    <th>Telefono</th>
                    <th>Correo</th>
                    <th>Rol</th>
                    <th>Fecha creación</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                </tr>
            </thead>
            <tbody>
                <%
                    if (usuarios != null && !usuarios.isEmpty()) {
                        for (Usuario u : usuarios) {
                %>
                <tr>
                    <td data-label="Codigo"><%= u.getUsuarioId() %></td>
                    <td data-label="Nombre"><%= u.getNombre() %></td>
                    <td data-label="Telefono"><%= u.getTelefono() != null ? u.getTelefono() : "-" %></td>
                    <td data-label="Correo"><%= u.getCorreo() != null ? u.getCorreo() : "-" %></td>
                    <td data-label="Rol"><%= u.getRol() != null ? u.getRol() : "-" %></td>
                    <td data-label="Fecha creación"><%= u.getFechaCreacion() != null ? u.getFechaCreacion() : "-" %></td>
                    <td data-label="Estado"><%= u.isEstado() ? "Activo" : "Inactivo" %></td>
                    <td data-label="Acciones" class="Usuarios-listar__acciones">
                        <div class="iconos">
                            <a href="<%= request.getContextPath() %>/UsuarioServlet?accion=editar&id=<%= u.getUsuarioId() %>">
                                <i class="fa-solid fa-pen-to-square"></i>
                            </a>
                        </div>
                    </td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="8">No hay usuarios registrados.</td>
                </tr>
                <%
                    }
                %>
            </tbody>
        </table>
    </section>

    <div class="contadores"> 
        <div class="contador-card"> 
            <h2>Total Usuarios</h2> 
            <h3 class="contador-card__numero"><%= totalUsuarios %></h3> 
        </div> 
        <div class="contador-card"> 
            <h2>Usuarios activos</h2> 
            <h3 class="contador-card__numero"><%= usuariosActivos %></h3> 
        </div> 
    </div> 
</main>

</body>
</html>
