<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Object admin = session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8"/>
    <title>¡Éxito!</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/mensajesexito.css"/>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
<div class="mensaje-exito">
    <aside class="mensaje-exito__panel-izquierdo">
        <div class="mensaje-exito__caja-logo">
            <img class="mensaje-exito__imagen-logo" src="<%=request.getContextPath()%>/assets/Imagenes/Logo.png" alt="Logo">
        </div>
    </aside>
    <main class="mensaje-exito__panel-derecho">
        <section class="mensaje-exito__caja-mensaje">
            <h1>¡Proveedor guardado con éxito!</h1>
            <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/pulgar-arriba.png" alt="Éxito" class="icon__pulgar-arriba">
            <div class="registro-proveedor__link">
                <p><a href="<%=request.getContextPath()%>/Administrador/proveedores/listar.jsp">Ver listado de proveedores</a></p>
            </div>
        </section>
    </main>
</div>
</body>
</html>