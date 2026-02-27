<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    Object vendedor = session.getAttribute("vendedor");
    if (vendedor == null) {
        response.sendRedirect(request.getContextPath() + "/vendedor/inicio-sesion.jsp");
        return;
    }
    model.Usuario u = (model.Usuario) vendedor;
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel Vendedor | AAC27</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css" />
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Vendedor/vendedor-principal.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/Seller.png" alt="Vendedor">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <%-- Saludo al vendedor --%>
    <span style="font-size:14px;font-weight:600;color:#fff;opacity:0.85;">
        <i class="fa-solid fa-user-circle"></i> <%= u.getNombre() %>
    </span>
    <a href="<%= request.getContextPath() %>/index.jsp" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-right-from-bracket"></i>
            <span class="navbar-admin__home-text">Cerrar sesión</span>
        </span>
    </a>
</nav>

<main class="panel-vendedor__tarjetas">
    <h2 class="panel-vendedor__tarjetas-titulo">Haz clic en lo que deseas gestionar hoy.</h2>

    <section class="panel-vendedor__tarjetas-contenedor">

        <%-- Registrar venta --%>
        <div class="panel-vendedor__tarjeta">
            <div class="icono-boton__circulo">
                <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=nueva">
                    <img class="icono-boton__img"
                         src="<%= request.getContextPath() %>/assets/Imagenes/iconos/gestionar_proveedores.png"
                         alt="Registrar venta">
                </a>
            </div>
            <div class="panel-vendedor__tarjeta-h3">
                <div class="panel-vendedor__tarjeta-h3-izquierda"></div>
                <div class="panel-vendedor__tarjeta-h3-derecha"></div>
                <h3 class="icono-boton__titulo">Registrar venta</h3>
            </div>
        </div>

        <%-- Mis ventas --%>
        <div class="panel-vendedor__tarjeta">
            <div class="icono-boton__circulo">
                <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=misVentas">
                    <img class="icono-boton__img"
                         src="<%= request.getContextPath() %>/assets/Imagenes/iconos/ventas.png"
                         alt="Mis ventas">
                </a>
            </div>
            <div class="panel-vendedor__tarjeta-h3">
                <div class="panel-vendedor__tarjeta-h3-izquierda"></div>
                <div class="panel-vendedor__tarjeta-h3-derecha"></div>
                <h3 class="icono-boton__titulo">Mis ventas</h3>
            </div>
        </div>

        <%-- Mis casos postventa --%>
        <div class="panel-vendedor__tarjeta">
            <div class="icono-boton__circulo">
                <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=misCasos">
                    <img class="icono-boton__img"
                         src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png"
                         alt="Casos postventa">
                </a>
            </div>
            <div class="panel-vendedor__tarjeta-h3">
                <div class="panel-vendedor__tarjeta-h3-izquierda"></div>
                <div class="panel-vendedor__tarjeta-h3-derecha"></div>
                <h3 class="icono-boton__titulo">Mis casos postventa</h3>
            </div>
        </div>

    </section>
</main>

</body>
</html>
