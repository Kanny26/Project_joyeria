<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, model.Desempeno_Vendedor" %>
<%
    List<Desempeno_Vendedor> historial = (List<Desempeno_Vendedor>) request.getAttribute("historial");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Historial de Desempeño</title>

    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/usuarios/historial.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<nav class="navbar-admin"> 
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/UsuarioServlet">
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
    </a>
</nav>

<main class="titulo">
    <h2 class="titulo__encabezado">Historial de Desempeño</h2>
    
    <section class="usuarios-listar__tabla-contenedor">
        <table class="usuarios-listar__tabla">
            <thead>
                <tr>
                    <th>Código</th>
                    <th>Nombre</th>
                    <th>Ventas</th>
                    <th>Comisión</th>
                    <th>Observaciones</th>
                </tr>
            </thead>
            <tbody>
                <%
                if (historial != null && !historial.isEmpty()) {
                    for (Desempeno_Vendedor d : historial) {
                %>
                <tr>
                    <td data-label="Código"><%= d.getDesempenoId() %></td>
                    <td data-label="Usuario"><%= d.getNombre() %></td>
                    <td data-label="Ventas">$<%= d.getVentasTotales() != null ? d.getVentasTotales() : "0" %></td>
                    <td data-label="Comisión">$<%= d.getComisionGanada() != null ? d.getComisionGanada() : "0" %></td>
                    <td data-label="Observaciones">
                        <%= d.getObservaciones() != null ? d.getObservaciones() : "Sin observaciones" %>
                    </td>
                </tr>
                <%
                    }
                } else {
                %>
                <tr>
                    <td colspan="5">No hay registros de desempeño</td>
                </tr>
                <%
                }
                %>
            </tbody>
        </table>
    </section>
</main>

</body>
</html>