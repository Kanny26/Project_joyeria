<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String error = (String) request.getAttribute("error");
    String msg   = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recuperar contraseña - AAC27</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Recuperar_pass/ing-correo.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
<div class="ing-correo">
    <aside class="ing-correo__panel">
        <div class="ing-correo__panel-caja">
            <img class="ing-correo__panel-logo" src="<%= request.getContextPath() %>/assets/Imagenes/Logo.png" alt="Logo">
        </div>
    </aside>

    <main class="ing-correo__main">
        <section class="ing-correo__caja">
            <h1>Recuperar Contraseña</h1>
            <p>Ingresa el correo con el que te registraste y te enviaremos un código de verificación.</p>

            <% if (error != null && !error.isEmpty()) { %>
            <div style="background:#fdedec;border-left:4px solid #e74c3c;border-radius:8px;padding:11px 16px;
                        margin:12px 0;font-size:13px;color:#922b21;text-align:left;width:100%;max-width:290px;">
                <i class="fa-solid fa-circle-exclamation" style="margin-right:7px;"></i><%= error %>
            </div>
            <% } %>

            <form method="post" action="<%= request.getContextPath() %>/recuperar">
                <input type="hidden" name="paso" value="1">
                <div class="ing-correo__input-group">
                    <i class="fas fa-envelope icon-left"></i>
                    <input type="email" name="correo" placeholder="correo@ejemplo.com" required autocomplete="off">
                </div>
                <button type="submit" class="btn" style="margin-top:20px;">
                    Enviar código
                </button>
            </form>

            <a href="<%= request.getContextPath() %>/inicio-sesion.jsp"
               style="display:block;margin-top:16px;font-size:13px;color:#9177a8;text-decoration:none;">
                <i class="fa-solid fa-arrow-left" style="margin-right:5px;"></i> Volver al inicio de sesión
            </a>
        </section>
    </main>
</div>
</body>
</html>
