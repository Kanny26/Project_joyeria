<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Proveedor, java.util.List" %>

<%
    Object admin = session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }

    List<Proveedor> proveedores =
        (List<Proveedor>) request.getAttribute("proveedores");

    if (proveedores == null) {
        proveedores = java.util.Collections.emptyList();
    }
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Listar proveedores</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/proveedores/listar.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo"> 
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div> 
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%=request.getContextPath()%>/Administrador/proveedores.jsp">
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
    </a>
</nav>

<main class="titulo">
    <h2 class="titulo__encabezado">Listar proveedores</h2>

    <section class="Proveedores-listar__tabla-contenedor">
        <table class="Proveedores-listar__tabla">
            <thead>
                <tr>
                    <th>Nombre</th>
                    <th>Teléfono</th>
                    <th>Correo</th>
                    <th>Material</th>
                    <th>Productos</th>
                    <th>Fecha inicio</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                </tr>
            </thead>
            <tbody>

            <% for (Proveedor p : proveedores) { %>
                <tr>
                    <td data-label="Nombre"><%= p.getNombre() %></td>
                    <td data-label="Telefono"><%= p.getTelefono() %></td>
                    <td data-label="Correo"><%= p.getCorreo() %></td>

                    <td data-label="Material">
                        <% for (String m : p.getMateriales()) { %>
                            <%= m %><br>
                        <% } %>
                    </td>

                    <td data-label="Productos">
                        <% for (String pr : p.getProductos()) { %>
                            <%= pr %><br>
                        <% } %>
                    </td>

                    <td data-label="Fecha inicio"><%= p.getFechaInicio() %></td>

                    <td data-label="Estado">
                        <form method="post" action="<%=request.getContextPath()%>/Administrador/proveedores">
                            <input type="hidden" name="action" value="actualizarEstado">
                            <input type="hidden" name="id" value="<%= p.getUsuarioId() %>">
                            <select name="estado" onchange="this.form.submit()">
                                <option value="true" <%= p.isEstado() ? "selected" : "" %>>Activo</option>
                                <option value="false" <%= !p.isEstado() ? "selected" : "" %>>Inactivo</option>
                            </select>
                        </form>
                    </td>

                    <td data-label="Acciones" class="Proveedores-listar__acciones">
                        <a href="<%=request.getContextPath()%>/Administrador/proveedores/editar?id=<%= p.getUsuarioId() %>">
                            <i class="fa-solid fa-pen-to-square"></i>
                        </a>
                    </td>
                </tr>
            <% } %>

            </tbody>
        </table>
    </section>

    <div class="contadores">
        <div class="contador-card">
            <h2>Total proveedores</h2>
            <h3 class="contador-card__numero"><%= proveedores.size() %></h3>
        </div>

        <div class="contador-card">
            <h2>Proveedores activos</h2>
            <h3 class="contador-card__numero">
                <%-- proveedores.stream().filter(Proveedor::isEstado).count() --%>
            </h3>
        </div>
    </div>
</main>

</body>
</html>
