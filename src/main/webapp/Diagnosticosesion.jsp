<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Usuario" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Diagnóstico Sesión</title>
    <style>
        body { font-family: monospace; padding: 20px; background: #1e1e1e; color: #d4d4d4; }
        .ok   { color: #4ec9b0; }
        .fail { color: #f48771; }
        .warn { color: #dcdcaa; }
        table { border-collapse: collapse; width: 100%; margin-top: 10px; }
        td, th { border: 1px solid #555; padding: 8px 12px; text-align: left; }
        th { background: #333; }
        h2 { color: #569cd6; }
    </style>
</head>
<body>

<h2>🔍 Diagnóstico de Sesión</h2>

<%
    HttpSession sesion = request.getSession(false);

    if (sesion == null) {
%>
    <p class="fail">❌ NO HAY SESIÓN ACTIVA — No has iniciado sesión.</p>
<%
    } else {
        Object usuarioObj = sesion.getAttribute("usuario");

        if (usuarioObj == null) {
%>
    <p class="fail">❌ La sesión existe pero el atributo <strong>"usuario"</strong> es NULL.</p>
    <p class="warn">→ El login no está guardando el usuario en sesión con ese nombre exacto.</p>
<%
        } else {
%>
    <p class="ok">✅ Sesión activa. Objeto usuario encontrado.</p>
    <p class="warn">Clase del objeto: <strong><%= usuarioObj.getClass().getName() %></strong></p>

    <%
        // Intentar castear a model.Usuario
        try {
            model.Usuario u = (model.Usuario) usuarioObj;
    %>
    <p class="ok">✅ Cast a model.Usuario exitoso.</p>

    <h2>📋 Datos del Usuario en Sesión</h2>
    <table>
        <tr><th>Campo</th><th>Valor</th><th>Estado</th></tr>
        <tr>
            <td>usuarioId</td>
            <td><%= u.getUsuarioId() %></td>
            <td class="<%= u.getUsuarioId() > 0 ? "ok" : "fail" %>">
                <%= u.getUsuarioId() > 0 ? "✅ OK" : "❌ es 0 o negativo" %>
            </td>
        </tr>
        <tr>
            <td>nombre</td>
            <td><%= u.getNombre() %></td>
            <td class="<%= u.getNombre() != null ? "ok" : "fail" %>">
                <%= u.getNombre() != null ? "✅ OK" : "❌ NULL" %>
            </td>
        </tr>
        <tr>
            <td>getRol()</td>
            <td><strong><%= u.getRol() %></strong></td>
            <td class="<%= u.getRol() != null ? "ok" : "fail" %>">
                <%= u.getRol() != null ? "✅ Tiene rol" : "❌ NULL — este es el problema" %>
            </td>
        </tr>
        <tr>
            <td>estado</td>
            <td><%= u.isEstado() %></td>
            <td class="<%= u.isEstado() ? "ok" : "fail" %>">
                <%= u.isEstado() ? "✅ Activo" : "❌ Inactivo" %>
            </td>
        </tr>
    </table>

    <h2>🎯 Diagnóstico del Problema</h2>
    <%
        String rol = u.getRol();
        if (rol == null) {
    %>
        <p class="fail">❌ <strong>getRol() retorna NULL</strong></p>
        <p class="warn">→ El login guarda el usuario pero no le asigna el rol desde la tabla <code>Rol</code>.</p>
        <p class="warn">→ Revisa tu LoginServlet: debe hacer un segundo query a la tabla <code>Rol</code> y llamar <code>u.setRol(...)</code></p>
    <%
        } else if (rol.equals("vendedor")) {
    %>
        <p class="ok">✅ El rol es "vendedor" — la sesión está correcta.</p>
        <p class="warn">→ El problema puede estar en otro lugar. Revisa la URL que usas para ingresar.</p>
    <%
        } else if (rol.equals("administrador")) {
    %>
        <p class="fail">⚠️ El rol es "administrador", no "vendedor".</p>
        <p class="warn">→ Estás logueado con una cuenta de administrador, no de vendedor.</p>
    <%
        } else {
    %>
        <p class="warn">⚠️ El rol es "<strong><%= rol %></strong>" — verifica que coincida exactamente con "vendedor" (minúsculas, sin espacios).</p>
    <%
        }
    %>

    <%
        } catch (ClassCastException e) {
    %>
    <p class="fail">❌ Error de cast: el objeto en sesión NO es de tipo <code>model.Usuario</code></p>
    <p class="warn">Clase real: <strong><%= usuarioObj.getClass().getName() %></strong></p>
    <%
        }
    %>
<%
        } // fin else usuarioObj != null
    } // fin else sesion != null
%>

<br><br>
<a href="javascript:history.back()" style="color:#569cd6;">← Volver</a>

</body>
</html>
