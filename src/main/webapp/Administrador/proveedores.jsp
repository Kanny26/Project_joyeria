<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Verificar si el administrador está logueado
    HttpSession sesion = request.getSession(false);
    if (sesion == null || sesion.getAttribute("admin") == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Proveedores - AAC27</title>

    <!-- Estilos -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/proveedores.css">
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
        <h2 class="titulo__encabezado">Gestionar Proveedores</h2>

        <section class="iconos-contenedor">

            <article class="iconos-item">
                <!-- CAMBIO AQUÍ: Apuntamos al Servlet con el parámetro accion -->
                <a href="<%= request.getContextPath() %>/ProveedorServlet?accion=listar" class="icono-boton">
                    <div class="icono-boton__circulo">
                        <img class="icono-boton__img"
                             src="<%= request.getContextPath() %>/assets/Imagenes/iconos/listar_proveedores.png">
                    </div>
                    <h3 class="icono-boton__titulo">Listar proveedores</h3>
                </a>
            </article>
        
            <article class="iconos-item">
                <!-- CAMBIO AQUÍ: Apuntamos al Servlet con el parámetro accion -->
                <a href="<%= request.getContextPath() %>/ProveedorServlet?accion=agregar" class="icono-boton">
                    <div class="icono-boton__circulo">
                        <img class="icono-boton__img"
                             src="<%= request.getContextPath() %>/assets/Imagenes/iconos/agregar_proveedores.png">
                    </div>
                    <h3 class="icono-boton__titulo">Agregar proveedores</h3>
                </a>
            </article>
        
        </section>
    </main>

</body>
</html>