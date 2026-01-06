<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Agregar producto</title>

    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
</head>

<body>

<nav class="navbar-admin">
    <h1>AAC27</h1>
</nav>

<main class="form-product-container">

    <h2>Nuevo producto</h2>

    <form method="post"
          action="<%=request.getContextPath()%>/ProductoServlet"
          enctype="multipart/form-data">

        <input type="hidden" name="action" value="guardar">
        <input type="hidden" name="categoriaId" value="<%= request.getParameter("categoria") %>">

        <input type="text" name="nombre" placeholder="Nombre" required>
        <textarea name="descripcion" placeholder="Descripción"></textarea>
        <input type="number" name="precio" required>
        <input type="number" name="stock" required>

        <button type="submit">Guardar</button>
    </form>

</main>

</body>
</html>
