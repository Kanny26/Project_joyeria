<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String error  = (String) request.getAttribute("error");
    String correo = (String) session.getAttribute("recuperar_correo");
    // Si no hay sesión activa de recuperación, redirigir al paso 1
    if (session.getAttribute("recuperar_usuario_id") == null && session.getAttribute("recuperar_correo") == null) {
        response.sendRedirect(request.getContextPath() + "/Recuperar_pass/ing-correo.jsp");
        return;
    }
    String correoMask = "";
    if (correo != null && correo.contains("@")) {
        String[] partes = correo.split("@");
        String local = partes[0];
        String visible = local.length() > 2 ? local.substring(0, 2) + "***" : local + "***";
        correoMask = visible + "@" + partes[1];
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Verificar código - AAC27</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Recuperar_pass/ing-codigo.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
<div class="ing-codigo">
    <aside class="ing-codigo__panel">
        <div class="ing-codigo__panel-caja">
            <img class="ing-codigo__panel-logo" src="<%= request.getContextPath() %>/assets/Imagenes/Logo.png" alt="Logo">
        </div>
    </aside>

    <main class="ing-codigo__main">
        <section class="ing-codigo__caja">
            <h1>Revisa tu correo</h1>
            <p>
                Enviamos un código de 6 dígitos a
                <% if (!correoMask.isEmpty()) { %>
                    <strong><%= correoMask %></strong>
                <% } else { %>
                    tu correo registrado
                <% } %>.
                Ingrésalo a continuación.
            </p>

            <% if (error != null && !error.isEmpty()) { %>
            <div style="background:#fdedec;border-left:4px solid #e74c3c;border-radius:8px;padding:11px 16px;
                        margin:12px 0;font-size:13px;color:#922b21;text-align:left;width:100%;max-width:290px;">
                <i class="fa-solid fa-circle-exclamation" style="margin-right:7px;"></i><%= error %>
            </div>
            <% } %>

            <form method="post" action="<%= request.getContextPath() %>/recuperar">
                <input type="hidden" name="paso" value="2">

                <%-- Input de 6 cajas individuales para el código --%>
                <div id="codigoBoxes" style="display:flex;gap:10px;margin:20px 0 8px;justify-content:center;">
                    <input type="text" maxlength="1" class="cod-box" inputmode="numeric" pattern="[0-9]"
                           style="width:44px;height:54px;text-align:center;font-size:22px;font-weight:800;
                                  border:2px solid #c5c2df;border-radius:10px;outline:none;
                                  font-family:monospace;color:#9177a8;transition:border-color .2s;">
                    <input type="text" maxlength="1" class="cod-box" inputmode="numeric" pattern="[0-9]"
                           style="width:44px;height:54px;text-align:center;font-size:22px;font-weight:800;
                                  border:2px solid #c5c2df;border-radius:10px;outline:none;
                                  font-family:monospace;color:#9177a8;transition:border-color .2s;">
                    <input type="text" maxlength="1" class="cod-box" inputmode="numeric" pattern="[0-9]"
                           style="width:44px;height:54px;text-align:center;font-size:22px;font-weight:800;
                                  border:2px solid #c5c2df;border-radius:10px;outline:none;
                                  font-family:monospace;color:#9177a8;transition:border-color .2s;">
                    <input type="text" maxlength="1" class="cod-box" inputmode="numeric" pattern="[0-9]"
                           style="width:44px;height:54px;text-align:center;font-size:22px;font-weight:800;
                                  border:2px solid #c5c2df;border-radius:10px;outline:none;
                                  font-family:monospace;color:#9177a8;transition:border-color .2s;">
                    <input type="text" maxlength="1" class="cod-box" inputmode="numeric" pattern="[0-9]"
                           style="width:44px;height:54px;text-align:center;font-size:22px;font-weight:800;
                                  border:2px solid #c5c2df;border-radius:10px;outline:none;
                                  font-family:monospace;color:#9177a8;transition:border-color .2s;">
                    <input type="text" maxlength="1" class="cod-box" inputmode="numeric" pattern="[0-9]"
                           style="width:44px;height:54px;text-align:center;font-size:22px;font-weight:800;
                                  border:2px solid #c5c2df;border-radius:10px;outline:none;
                                  font-family:monospace;color:#9177a8;transition:border-color .2s;">
                </div>
                <%-- Input hidden que recibe el código ensamblado --%>
                <input type="hidden" name="codigo" id="codigoHidden">

                <p style="font-size:12px;color:#bbb;margin-bottom:16px;">Solo números · Expira en 15 minutos</p>

                <button type="submit" class="btn" id="btnVerificar" disabled style="opacity:.5;cursor:not-allowed;">
                    Verificar código
                </button>
            </form>

            <a href="<%= request.getContextPath() %>/recuperar?paso=1"
               style="display:block;margin-top:16px;font-size:13px;color:#9177a8;text-decoration:none;">
                <i class="fa-solid fa-rotate-left" style="margin-right:5px;"></i> Reenviar código
            </a>
            <a href="<%= request.getContextPath() %>/inicio-sesion.jsp"
               style="display:block;margin-top:8px;font-size:13px;color:#bbb;text-decoration:none;">
                <i class="fa-solid fa-arrow-left" style="margin-right:5px;"></i> Volver al inicio de sesión
            </a>
        </section>
    </main>
</div>

<script>
(function() {
    var boxes   = document.querySelectorAll('.cod-box');
    var hidden  = document.getElementById('codigoHidden');
    var btnVer  = document.getElementById('btnVerificar');

    function actualizarHidden() {
        var val = '';
        boxes.forEach(function(b) { val += b.value; });
        hidden.value = val;
        var completo = val.length === 6;
        btnVer.disabled = !completo;
        btnVer.style.opacity = completo ? '1' : '.5';
        btnVer.style.cursor  = completo ? 'pointer' : 'not-allowed';
    }

    boxes.forEach(function(box, i) {
        box.addEventListener('focus', function() { this.style.borderColor = '#9177a8'; this.select(); });
        box.addEventListener('blur',  function() { this.style.borderColor = '#c5c2df'; });

        box.addEventListener('input', function() {
            // Solo dígitos
            this.value = this.value.replace(/[^0-9]/g, '');
            if (this.value && i < boxes.length - 1) boxes[i + 1].focus();
            actualizarHidden();
        });

        box.addEventListener('keydown', function(e) {
            if (e.key === 'Backspace' && !this.value && i > 0) {
                boxes[i - 1].focus();
                boxes[i - 1].value = '';
                actualizarHidden();
            }
        });

        // Soporte pegar código completo en el primer box
        box.addEventListener('paste', function(e) {
            e.preventDefault();
            var pasted = (e.clipboardData || window.clipboardData).getData('text').replace(/[^0-9]/g, '');
            pasted.split('').slice(0, 6).forEach(function(c, j) {
                if (boxes[j]) boxes[j].value = c;
            });
            actualizarHidden();
            var nextEmpty = Array.from(boxes).findIndex(function(b) { return !b.value; });
            if (nextEmpty !== -1) boxes[nextEmpty].focus(); else boxes[5].focus();
        });
    });

    // Foco automático en el primer box
    boxes[0].focus();
}());
</script>
</body>
</html>
