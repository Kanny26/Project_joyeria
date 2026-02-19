<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- Importamos las clases necesarias, incluyendo Producto --%>
<%@ page import="model.Proveedor, model.Producto, java.util.List" %>

<%
    // Verificación de sesión
    Object admin = session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }

    // Obtener la lista enviada por el Servlet
    List<Proveedor> proveedores = (List<Proveedor>) request.getAttribute("proveedores");

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
                    
                    <%-- Corregido: Recorrer lista de teléfonos --%>
                    <td data-label="Telefono">
                        <% if(p.getTelefonos() != null) { 
                             for(String tel : p.getTelefonos()) { %>
                                <%= tel %><br>
                        <%   } 
                           } %>
                    </td>

                    <%-- Corregido: Recorrer lista de correos --%>
                    <td data-label="Correo">
                        <% if(p.getCorreos() != null) { 
                             for(String mail : p.getCorreos()) { %>
                                <%= mail %><br>
                        <%   } 
                           } %>
                    </td>

                    <td data-label="Material">
                        <% if(p.getMateriales() != null) {
                             for (String m : p.getMateriales()) { %>
                                <%= m %><br>
                        <%   }
                           } %>
                    </td>

                    <%-- CORRECCIÓN CRÍTICA: Cambiado String por Producto --%>
                    <td data-label="Productos">
                        <% if(p.getProductos() != null) {
                             for (Producto pr : p.getProductos()) { %>
                                <%= pr.getNombre() %><br>
                        <%   }
                           } %>
                    </td>

                    <td data-label="Fecha inicio"><%= p.getFechaInicio() %></td>

                    <td data-label="Estado">
                        <%-- Corregido: El action debe ir al Servlet --%>
                        <form method="post" action="<%=request.getContextPath()%>/ProveedorServlet">
                            <input type="hidden" name="action" value="actualizarEstado">
                            <input type="hidden" name="id" value="<%= p.getUsuarioId() %>">
                            <select name="estado" onchange="this.form.submit()">
                                <option value="true" <%= p.isEstado() ? "selected" : "" %>>Activo</option>
                                <option value="false" <%= !p.isEstado() ? "selected" : "" %>>Inactivo</option>
                            </select>
                        </form>
                    </td>

                    <td data-label="Acciones" class="Proveedores-listar__acciones">
                        <%-- Corregido: El enlace de edición también debe pasar por el Servlet preferiblemente --%>
                        <a href="<%=request.getContextPath()%>/ProveedorServlet?accion=editar&id=<%= p.getUsuarioId() %>">
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
                <% 
                   long activos = proveedores.stream().filter(Proveedor::isEstado).count();
                %>
                <%= activos %>
            </h3>
        </div>
    </div>
</main>

</body>
</html>