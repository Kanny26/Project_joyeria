<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Verificar que llegó desde el paso 2 correctamente
    if (session.getAttribute("recuperar_token") == null) {
        response.sendRedirect(request.getContextPath() + "/Recuperar_pass/ing-correo.jsp");
        return;
    }
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nueva contraseña - AAC27</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Recuperar_pass/nueva-pass.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
<div class="nueva-contraseña">
    <aside class="nueva-contraseña__panel">
        <div class="nueva-contraseña__panel-caja">
            <img class="nueva-contraseña__panel-logo" src="<%= request.getContextPath() %>/assets/Imagenes/Logo.png" alt="Logo">
        </div>
    </aside>

    <main class="nueva-contraseña__main">
        <section class="nueva-contraseña__form">
            <h1>Nueva contraseña</h1>
            <p>Crea una contraseña segura de al menos 6 caracteres.</p>

            <% if (error != null && !error.isEmpty()) { %>
            <div style="background:#fdedec;border-left:4px solid #e74c3c;border-radius:8px;padding:11px 16px;
                        margin:12px 0;font-size:13px;color:#922b21;text-align:left;width:100%;max-width:290px;">
                <i class="fa-solid fa-circle-exclamation" style="margin-right:7px;"></i><%= error %>
            </div>
            <% } %>

            <form id="formNuevaPass" method="post" action="<%= request.getContextPath() %>/recuperar">
                <input type="hidden" name="paso" value="3">

                <div class="nueva-contraseña__input-group">
                    <i class="fas fa-lock icon-left"></i>
                    <input type="password" name="passNueva" id="passNueva"
                           placeholder="Nueva contraseña" required minlength="6">
                    <i class="fas fa-eye icon-right" id="toggleNueva" style="cursor:pointer;"></i>
                </div>

                <%-- Barra de fortaleza --%>
                <div style="width:100%;max-width:290px;margin:8px 0 4px;">
                    <div style="height:5px;background:#e5e7eb;border-radius:3px;overflow:hidden;">
                        <div id="barraFuerza" style="height:100%;width:0;border-radius:3px;transition:all .3s;"></div>
                    </div>
                    <p id="textoFuerza" style="font-size:11px;color:#bbb;margin:3px 0 0;text-align:right;height:14px;"></p>
                </div>

                <div class="nueva-contraseña__input-group" style="margin-top:10px;">
                    <i class="fas fa-lock icon-left"></i>
                    <input type="password" name="passConfirm" id="passConfirm"
                           placeholder="Confirmar contraseña" required>
                    <i class="fas fa-eye icon-right" id="toggleConfirm" style="cursor:pointer;"></i>
                </div>
                <p id="matchMsg" style="font-size:12px;height:16px;margin:4px 0 14px;"></p>

                <button type="submit" class="btn" id="btnGuardar">
                    Guardar contraseña
                </button>
            </form>
        </section>
    </main>
</div>

<script>
(function() {
    // Toggle visibilidad contraseñas
    function togglePass(inputId, iconId) {
        var input = document.getElementById(inputId);
        var icon  = document.getElementById(iconId);
        icon.addEventListener('click', function() {
            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.replace('fa-eye', 'fa-eye-slash');
            } else {
                input.type = 'password';
                icon.classList.replace('fa-eye-slash', 'fa-eye');
            }
        });
    }
    togglePass('passNueva', 'toggleNueva');
    togglePass('passConfirm', 'toggleConfirm');

    // Barra de fortaleza
    var passNueva = document.getElementById('passNueva');
    var barra     = document.getElementById('barraFuerza');
    var textoF    = document.getElementById('textoFuerza');

    passNueva.addEventListener('input', function() {
        var v = this.value;
        var puntos = 0;
        if (v.length >= 6)  puntos++;
        if (v.length >= 10) puntos++;
        if (/[A-Z]/.test(v)) puntos++;
        if (/[0-9]/.test(v)) puntos++;
        if (/[^A-Za-z0-9]/.test(v)) puntos++;

        var colores = ['#ef4444','#f97316','#eab308','#22c55e','#16a34a'];
        var labels  = ['Muy débil','Débil','Regular','Fuerte','Muy fuerte'];
        var idx = Math.min(puntos - 1, 4);
        if (v.length === 0) { barra.style.width = '0'; textoF.textContent = ''; return; }
        barra.style.width  = ((puntos / 5) * 100) + '%';
        barra.style.background = colores[Math.max(0, idx)];
        textoF.textContent = labels[Math.max(0, idx)];
        textoF.style.color = colores[Math.max(0, idx)];
        validarMatch();
    });

    // Validar coincidencia
    var passConf = document.getElementById('passConfirm');
    var matchMsg = document.getElementById('matchMsg');

    function validarMatch() {
        if (!passConf.value) { matchMsg.textContent = ''; return; }
        if (passNueva.value === passConf.value) {
            matchMsg.textContent = '✓ Las contraseñas coinciden';
            matchMsg.style.color = '#16a34a';
        } else {
            matchMsg.textContent = '✗ No coinciden';
            matchMsg.style.color = '#ef4444';
        }
    }
    passConf.addEventListener('input', validarMatch);

    // Validación antes de enviar
    document.getElementById('formNuevaPass').addEventListener('submit', function(e) {
        if (passNueva.value.length < 6) {
            e.preventDefault();
            alert('La contraseña debe tener al menos 6 caracteres.');
            return;
        }
        if (passNueva.value !== passConf.value) {
            e.preventDefault();
            alert('Las contraseñas no coinciden.');
            return;
        }
    });
}());
</script>
</body>
</html>
