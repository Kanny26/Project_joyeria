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
    <title>Proveedores</title>

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

        <section class="proveedores-admin__contenedor">
            <article class="proveedores-admin__contenedor-item">
                <a href="<%= request.getContextPath() %>/Administrador/proveedores/listar"
                   class="proveedores-admin__tarjeta">
                    <div class="proveedores-admin__icono">
                        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/listar_proveedores.png" alt="Listar proveedores">
                    </div>
                    <h3 class="proveedores-admin__titulo-tarjeta">Listar proveedores</h3>
                </a>
            </article>

            <article class="proveedores-admin__contenedor-item">
                <a href="<%= request.getContextPath() %>/Administrador/proveedores/agregar"
                   class="proveedores-admin__tarjeta">
                    <div class="proveedores-admin__icono">
                        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/agregar_proveedores.png" alt="Agregar proveedores">
                    </div>
                    <h3 class="proveedores-admin__titulo-tarjeta">Agregar proveedores</h3>
                </a>
            </article>

            <article class="proveedores-admin__contenedor-item">
                <a href="<%= request.getContextPath() %>/Administrador/proveedores/compras"
                   class="proveedores-admin__tarjeta">
                    <div class="proveedores-admin__icono">
                        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/resumen.png" alt="Compras realizadas">
                    </div>
                    <h3 class="proveedores-admin__titulo-tarjeta">Compras realizadas</h3>
                </a>
            </article>

            <article class="proveedores-admin__contenedor-item">
                <a href="<%= request.getContextPath() %>/Administrador/proveedores/historial"
                   class="proveedores-admin__tarjeta">
                    <div class="proveedores-admin__icono">
                        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/historial.png" alt="Historial">
                    </div>
                    <h3 class="proveedores-admin__titulo-tarjeta">Historial</h3>
                </a>
            </article>
        </section>
    </main>
</body>
</html>