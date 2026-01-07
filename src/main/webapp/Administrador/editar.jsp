<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="model.Producto, java.util.List, model.Material" %>

<%
    Producto producto = (Producto) request.getAttribute("producto");
    List<Material> materiales = (List<Material>) request.getAttribute("materiales");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Editar producto</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
</head>

<body>

<h2>Editar producto</h2>

<form action="<%=request.getContextPath()%>/ProductoServlet" method="post">
    <input type="hidden" name="action" value="actualizar">
    <input type="hidden" name="productoId" value="<%= producto.getProductoId() %>">
    <input type="hidden" name="categoriaId" value="<%= producto.getCategoria().getCategoriaId() %>">

    <label>Nombre</label>
    <input type="text" name="nombre" value="<%= producto.getNombre() %>" required>

    <label>Precio</label>
    <input type="number" step="0.01" name="precioUnitario"
           value="<%= producto.getPrecioUnitario() %>" required>

    <label>Stock</label>
    <input type="number" name="stock" value="<%= producto.getStock() %>" required>

    <label>Material</label>
    <select name="materialId" required>
        <% for (Material m : materiales) { %>
            <option value="<%= m.getMaterialId() %>"
                <%= producto.getMaterial().getMaterialId() == m.getMaterialId() ? "selected" : "" %>>
                <%= m.getNombre() %>
            </option>
        <% } %>
    </select>

    <label>Descripción</label>
    <textarea name="descripcion"><%= producto.getDescripcion() %></textarea>

    <button type="submit">Actualizar</button>
</form>

</body>
</html>
