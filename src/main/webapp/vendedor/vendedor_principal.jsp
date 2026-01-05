<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="model.Usuario" %>

<%
    Usuario vendedor = (Usuario) session.getAttribute("vendedor");
    if (vendedor == null) {
        response.sendRedirect(request.getContextPath() + "/vendedor/inicio-sesion.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Home vendedor</title>

    <!-- Rutas con contextPath -->
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Vendedor/vendedor-principal.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/Seller.png" alt="Vendedor">
        <h2>Panel del vendedor</h2>
    </div>

    <h1 class="navbar-admin__title">AAC27</h1>

    <a href="<%=request.getContextPath()%>/index.html">
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
    </a>
</nav>

<main class="panel-vendedor__tarjetas">
    <h2 class="panel-vendedor__tarjetas-titulo">
        Bienvenido, <%= vendedor.getNombre() %>.  
        Haz clic en lo que deseas gestionar hoy.
    </h2>

    <section class="panel-vendedor__tarjetas-contenedor">

        <div class="panel-vendedor__tarjeta">
            <div class="panel-vendedor__tarjeta-circulo">
                <a href="<%=request.getContextPath()%>/vendedor/registrar_venta.jsp">
                    <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/gestionar_proveedores.png"
                         alt="Registrar venta">
                </a>
            </div>
            <div class="panel-vendedor__tarjeta-h3">
                <div class="panel-vendedor__tarjeta-h3-izquierda"></div>
                <div class="panel-vendedor__tarjeta-h3-derecha"></div>
                <h3>Registrar venta</h3>
            </div>
        </div>

        <div class="panel-vendedor__tarjeta">
            <div class="panel-vendedor__tarjeta-circulo">
                <a href="<%=request.getContextPath()%>/vendedor/informe_ventas.jsp">
                    <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/ventas.png"
                         alt="Informe ventas">
                </a>
            </div>
            <div class="panel-vendedor__tarjeta-h3">
                <div class="panel-vendedor__tarjeta-h3-izquierda"></div>
                <div class="panel-vendedor__tarjeta-h3-derecha"></div>
                <h3>Informe ventas</h3>
            </div>
        </div>

    </section>
</main>

</body>
</html>
