<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="model.Administrador" %>
<%
    // No declarar session, usar el implícito
    Administrador admin = null;
    if (session != null) {
        admin = (Administrador) session.getAttribute("admin");
    }

    // Si no hay admin logueado, redirigir al login
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Home admin</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css" />
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/admin-principal.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin">
        <h2>Volver al inicio</h2>
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>

    <div class="navbar-admin__usuario">
        <i class="fas fa-user"></i>
        <span><%= admin.getNombre() %></span>
    </div>

    <a href="<%=request.getContextPath()%>/index.jsp">
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
    </a>
</nav>

<main class="panel-admin__tarjetas">
    <h2 class="panel-admin__tarjetas-titulo">
        ¡Bienvenido, <%= admin.getNombre() %>! Haz clic en lo que deseas gestionar hoy.
    </h2>
    <section class="panel-admin__tarjetas-contenedor">
        <div class="panel-admin__tarjeta">
            <div class="panel-admin__tarjeta-circulo">
                <a href="<%=request.getContextPath()%>/CategoriaServlet">
                    <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/catalogar.png" alt="Administrar catalogo">
                </a>
            </div>
            <div class="panel-admin__tarjeta-h3"><h3>Categorías</h3></div>
        </div>

        <div class="panel-admin__tarjeta">
            <div class="panel-admin__tarjeta-circulo">
                <a href="<%=request.getContextPath()%>/Administrador/proveedores.jsp">
                    <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/gestionar_proveedores.png" alt="Gestionar proveedores">
                </a>
            </div>
            <div class="panel-admin__tarjeta-h3"><h3>Proveedores</h3></div>
        </div>

        <div class="panel-admin__tarjeta">
            <div class="panel-admin__tarjeta-circulo">
                <a href="<%=request.getContextPath()%>/Administrador/ventas.jsp">
                    <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/ventas.png" alt="Seguimiento ventas">
                </a>
            </div>
            <div class="panel-admin__tarjeta-h3"><h3>Ventas</h3></div>
        </div>

        <div class="panel-admin__tarjeta">
            <div class="panel-admin__tarjeta-circulo">
                <a href="<%=request.getContextPath()%>/Administrador/usuarios.jsp">
                    <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/Usuarios.png" alt="Gestionar usuarios">
                </a>
            </div>
            <div class="panel-admin__tarjeta-h3"><h3>Usuarios</h3></div>
        </div>
    </section>
</main>

</body>
</html>

