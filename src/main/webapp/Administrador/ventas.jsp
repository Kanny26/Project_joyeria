<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ventas - AAC27</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/Administrador/ventas.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>

<body>

    <nav class="navbar-admin"> 
        <div class="navbar-admin__catalogo"> 
            <img src="${pageContext.request.contextPath}/assets/Imagenes/iconos/admin.png" alt="Admin">
        </div> 
        <h1 class="navbar-admin__title">AAC27</h1> 
        <a href="${pageContext.request.contextPath}/Administrador/admin-principal.jsp">
            <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i> 
        </a>
    </nav>

    <main class="titulo">
        <h2 class="titulo__encabezado">Gestionar Ventas</h2>

        <section class="iconos-contenedor">

            <article class="iconos-item">
                <a href="${pageContext.request.contextPath}/Administrador/ventas/listar_ventas.jsp" class="icono-boton">
                    <div class="icono-boton__circulo">
                        <img class="icono-boton__img"
                             src="${pageContext.request.contextPath}/assets/Imagenes/iconos/listar_ventas.png"
                             alt="Listar ventas">
                    </div>
                    <h3 class="icono-boton__titulo">Listar ventas</h3>
                </a>
            </article>

            <article class="iconos-item">
                <a href="${pageContext.request.contextPath}/Administrador/ventas/postventa.jsp" class="icono-boton">
                    <div class="icono-boton__circulo">
                        <img class="icono-boton__img"
                             src="${pageContext.request.contextPath}/assets/Imagenes/iconos/postventa.png"
                             alt="Postventa">
                    </div>
                    <h3 class="icono-boton__titulo">Postventa</h3>
                </a>
            </article>

        </section>
    </main>

</body>
</html>