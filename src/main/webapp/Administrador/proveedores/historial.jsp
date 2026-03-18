<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Proveedor, java.util.List" %>
<%
    /*
     * Seguridad: si no hay sesión activa de admin, redirige al login.
     * Este archivo estaba usando JSTL (<c:forEach>) sin importar la librería,
     * lo cual generaba un error. Se corrigió usando scriptlets Java estándar.
     */
    Object admin = session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }

    /* Lista de proveedores enviada desde el servlet via request.setAttribute */
    List<Proveedor> listaProveedores = (List<Proveedor>) request.getAttribute("listaProveedores");
    if (listaProveedores == null) listaProveedores = java.util.Collections.emptyList();
%>

<!DOCTYPE html> 
<html lang="es"> 
<head> 
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Historial proveedores</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/proveedores/historial.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>

<body>

    <nav class="navbar-admin"> 
        <div class="navbar-admin__catalogo"> 
            <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin">
        </div> 

        <h1 class="navbar-admin__title">AAC27</h1>
        <%-- Corregido: enlace al servlet en lugar de a un archivo .html que no existe --%>
        <a href="<%=request.getContextPath()%>/ProveedorServlet?action=listar" class="navbar-admin__home-link">
            <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
        </a> 
    </nav>

    <main class="titulo">
        <h2 class="titulo__encabezado">Historial proveedores</h2>
        <section class="Proveedores-listar__tabla-contenedor">
            <table class="Proveedores-listar__tabla">
                <thead>
                    <tr>
                        <th>Codigo</th>
                        <th>Nombre</th>
                        <th>Observaciones</th>
                    </tr>
                </thead>
                <tbody>
                    <%-- Iteración con scriptlets Java en lugar de JSTL para no requerir librerías adicionales --%>
                    <% for (Proveedor proveedor : listaProveedores) { %>
                        <tr>
                            <td data-label="Codigo"><%= proveedor.getProveedorId() %></td>
                            <td data-label="Nombre"><%= proveedor.getNombre() %></td>
                            <td data-label="Observaciones">
                                <textarea name="descripcion" class="editar-producto__input-area" rows="4"></textarea>
                            </td>
                        </tr>
                    <% } %>
                    <% if (listaProveedores.isEmpty()) { %>
                        <tr>
                            <td colspan="3" style="text-align:center; padding: 20px; color: #9ca3af;">
                                No hay registros en el historial.
                            </td>
                        </tr>
                    <% } %>
                </tbody> 
            </table> 
        </section> 
    </main> 
</body> 
</html>
