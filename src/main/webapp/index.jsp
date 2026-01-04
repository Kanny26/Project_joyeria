<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bienvenido - AAC27</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/styles.css">
</head>
<body>
<div class="splash">
    <div class="splash__tarjeta">
        <h1 class="splash__titulo">¡Bienvenido a AAC27!</h1>

        <p class="splash__descripcion">
            Administra tus ventas, inventario y clientes de forma eficiente y elegante.
            Optimiza tu negocio de joyería con herramientas modernas y fáciles de usar.
        </p>

        <p class="splash__texto">Selecciona tu rol para continuar:</p>

        <div class="splash__grupo-botones">
            <a href="<%=request.getContextPath()%>/Administrador/inicio-sesion.jsp" class="splash__boton splash__boton--hover">
                <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Administrador" class="splash__boton-icono">
                Administrador
            </a>

            <a href="<%=request.getContextPath()%>/Vendedor/inicio-sesion.jsp" class="splash__boton splash__boton--hover">
                <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/Seller.png" alt="Vendedor" class="splash__boton-icono">
                Vendedor
            </a>
        </div>
    </div>

    <div class="splash__decoracion"></div>
</div>
</body>
</html>
