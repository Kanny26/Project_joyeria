<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="model.Categoria" %>

<%
    Categoria categoria = (Categoria) request.getAttribute("categoria");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Editar Producto</title>

    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/agregar_producto.css">
</head>

<body>

<nav class="navbar-admin">
    <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png">
    <h1 class="navbar-admin__title">AAC27</h1>
</nav>

<main class="form-product-container">
    <h2>Editar Categoría</h2>

	<form action="CategoriaServlet" method="post">
	    <input type="hidden" name="action" value="actualizar">
	    <input type="hidden" name="id" value="<%= categoria.getId() %>">
	
	    <label>Nombre:</label><br>
	    <input type="text" name="nombre" value="<%= categoria.getNombre() %>" required><br><br>
	
	    <label>Estado:</label><br>
	    <select name="estado">
	        <option value="1" <%= categoria.getEstado()==1?"selected":"" %>>Activo</option>
	        <option value="0" <%= categoria.getEstado()==0?"selected":"" %>>Inactivo</option>
	    </select><br><br>
	
	    <button type="submit">Actualizar</button>
	</form>

	<a href="CategoriaServlet">Volver</a>
</main>

</body>
</html>