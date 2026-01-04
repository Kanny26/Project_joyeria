<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Agregado con exito</title>

    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css" />
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/mensajesexito.css" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
    <div class="mensaje-exito">
        <!-- Panel izquierdo -->
        <aside class="mensaje-exito__panel-izquierdo">
            <div class="mensaje-exito__caja-logo">
                <img class="mensaje-exito__imagen-logo" src="<%= request.getContextPath() %>/assets/Imagenes/Logo.png" alt="Logo de la joyería">
            </div>
        </aside>

        <!-- Panel derecho: mensaje de éxito -->
        <main class="mensaje-exito__panel-derecho">
            <section class="mensaje-exito__caja-mensaje">
                <h1>¡Error al agregar el usuario.!</h1>
                <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/pulgar-arriba.png" alt="Pulgar arriba" class="icon__pulgar-arriba">
                <div class="registro-usuario__link">
                    <p>
                        <a href="<%= request.getContextPath() %>/UsuarioServlet?accion=listar">Ver listado de usuarios</a>
                    </p>
                </div>
            </section>
        </main>
    </div>
</body>
</html>
