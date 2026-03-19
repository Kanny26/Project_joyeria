<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Se lee el error desde el request (enviado por forward) o desde la URL (enviado por redirect)
    String error = (String) request.getAttribute("error");
    if (error == null) error = request.getParameter("error");

    // "msg" se usa para mostrar confirmaciones, como cuando el usuario acaba de cambiar su contraseña
    String msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Iniciar sesión</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css" />
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/inicio-sesion.css" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" />
</head>
<body>
<div class="inicio-sesion">
    <aside class="inicio-sesion__panel">
        <div class="inicio-sesion__panel-caja">
            <img
                class="inicio-sesion__panel-logo"
                src="<%=request.getContextPath()%>/assets/Imagenes/Logo.png"
                alt="Logo"
            />
        </div>
    </aside>

    <main class="inicio-sesion__contenido">
        <section class="inicio-sesion__formulario">
            <div class="inicio-sesion__formulario-contenido">
                <header>
                    <h1>Iniciar Sesión</h1>
                </header>

                <form action="<%=request.getContextPath()%>/loginUnificado" method="post" id="formLogin">

                    <%-- Mensajes de error: "campos" viene por parámetro URL (redirect), otros por atributo (forward) --%>
                    <% if (error != null && !error.isEmpty()) { %>
                        <p class="login__error">
                            <% if ("campos".equals(error)) { %>
                                Por favor completa todos los campos.
                            <% } else { %>
                                <%= error %>
                            <% } %>
                        </p>
                    <% } %>

                    <%-- Mensaje de éxito cuando el usuario acaba de actualizar su contraseña --%>
                    <% if ("password_actualizado".equals(msg)) { %>
                        <p class="login__exito">
                            <i class="fa-solid fa-circle-check"></i> ¡Contraseña actualizada! Ya puedes iniciar sesión.
                        </p>
                    <% } %>

                    <div class="inicio-sesion__input-grupo">
                        <i class="fas fa-user icon-left"></i>
                        <input
                            type="text"
                            name="usuario"
                            id="usuario"
                            placeholder="Nombre de usuario"
                            required
                        />
                    </div>

                    <div class="inicio-sesion__input-grupo">
                        <i class="fas fa-lock icon-left"></i>
                        <input
                            type="password"
                            name="password"
                            id="password"
                            placeholder="Contraseña"
                            required
                        />
                        <i class="fas fa-eye icon-right" id="togglePassword"></i>
                    </div>

                    <a href="<%=request.getContextPath()%>/recuperar" class="inicio-sesion__link-recuperar">
                        ¿Olvidaste tu contraseña?
                    </a>

                    <button type="submit" class="btn" id="btnIngresar">
                        Iniciar Sesión
                    </button>
                </form>

                <footer class="inicio-sesion__link-registro">
                    <p>Diseñado por Stephany Moreno</p>
                </footer>
            </div>
        </section>
    </main>
</div>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
// Alternar visibilidad de la contraseña al hacer clic en el ícono del ojo
document.getElementById('togglePassword').addEventListener('click', function() {
    const input = document.getElementById('password');
    const icon = this;
    if (input.type === 'password') {
        input.type = 'text';
        icon.classList.replace('fa-eye', 'fa-eye-slash');
    } else {
        input.type = 'password';
        icon.classList.replace('fa-eye-slash', 'fa-eye');
    }
});

// Mostrar indicador de carga al enviar el formulario para que el usuario sepa que está procesando
document.getElementById('formLogin').addEventListener('submit', function() {
    const btn = document.getElementById('btnIngresar');
    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Verificando...';
});
</script>
</body>
</html>
