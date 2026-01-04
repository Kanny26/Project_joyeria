<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Usuario" %>
<%
    Usuario usuario = (Usuario) request.getAttribute("usuario");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Editar Usuario</title>
    <link rel="stylesheet" href="../../assets/css/main.css">
</head>
<body>
    <h2>Editar Usuario</h2>
    <form action="UsuarioServlet" method="post">
        <input type="hidden" name="accion" value="editar">
        <input type="hidden" name="id" value="<%= usuario.getUsuarioId() %>">
        <label>Nombre: <input type="text" name="nombre" value="<%= usuario.getNombre() %>" required></label><br>
        <label>Correo: <input type="email" name="correo" value="<%= usuario.getCorreo() %>" required></label><br>
        <label>Teléfono: <input type="tel" name="telefono" value="<%= usuario.getTelefono() %>" required></label><br>
        <label>Estado:
            <select name="estado">
                <option value="Activo" <%= usuario.isEstado() ? "selected" : "" %>>Activo</option>
                <option value="Inactivo" <%= !usuario.isEstado() ? "selected" : "" %>>Inactivo</option>
            </select>
        </label><br>
        <button type="submit">Guardar Cambios</button>
    </form>
</body>
</html>
html>
