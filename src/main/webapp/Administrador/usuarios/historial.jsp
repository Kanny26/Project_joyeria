<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, model.Usuario" %>
<%
    // Lista de usuarios enviada por el servlet
    List<Usuario> historialUsuarios = (List<Usuario>) request.getAttribute("historialUsuarios");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Historial usuarios</title>

    <!-- Estilos -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/usuarios/historial.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<!-- Navbar -->
<nav class="navbar-admin"> 
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>

    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/Administrador/usuarios.jsp">
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
    </a>
</nav>

<main class="titulo">
    <h2 class="titulo__encabezado">Historial usuarios</h2>
    <section class="usuarios-listar__tabla-contenedor">
        <table class="usuarios-listar__tabla">
            <thead>
                <tr>
                    <th>Codigo</th>
                    <th>Nombre</th>
                </tr>
            </thead>
            <tbody>
                <%
                    if (historialUsuarios != null) {
                        for (Usuario u : historialUsuarios) {
                %>
                <tr>
                    <td data-label="Codigo"><%= u.getUsuarioId() %></td>
                    <td data-label="Nombre"><%= u.getNombre() %></td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="2">No hay usuarios para mostrar</td>
                </tr>
                <%
                    }
                %>
            </tbody>
        </table>
    </section>
</main>

</body>
</html>

