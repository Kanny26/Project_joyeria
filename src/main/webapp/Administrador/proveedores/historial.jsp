<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Object admin = session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }
%>

<!DOCTYPE html> 
<html lang="es"> 
<head> 
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Historial proveedores</title>

    <!-- Estilos -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/Administrador/proveedores/historial.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>

<body>

    <!-- Navbar -->
    <nav class="navbar-admin"> 
        <div class="navbar-admin__catalogo"> 
            <img src="${pageContext.request.contextPath}/assets/Imagenes/iconos/admin.png" alt="Admin">
        </div> 

        <h1 class="navbar-admin__title">AAC27</h1>
        <a href="${pageContext.request.contextPath}/pages/Administrador/proveedores/proveedores.html">
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
                    <!-- Ejemplo de cómo podrías iterar sobre una lista de proveedores -->
                    <c:forEach var="proveedor" items="${listaProveedores}">
                        <tr>
                            <td data-label="Codigo">${proveedor.codigo}</td>
                            <td data-label="Nombre">${proveedor.nombre}</td>
                            <td data-label="Observaciones">
                                <textarea name="descripcion" class="editar-producto__input-area" rows="4">${proveedor.observaciones}</textarea>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody> 
            </table> 
        </section> 
    </main> 
</body> 
</html>