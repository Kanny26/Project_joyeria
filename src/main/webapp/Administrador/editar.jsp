<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="model.Producto, java.util.List, model.Material" %>

<%
    Producto producto = (Producto) request.getAttribute("producto");
    List<Material> materiales = (List<Material>) request.getAttribute("materiales");

    if (producto == null || materiales == null) {
        response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
        return;
    }
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Editar producto - <%= producto.getNombre() %></title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
</head>
<body>

<h2>Editar producto: <%= producto.getNombre() %></h2>

<form action="<%=request.getContextPath()%>/ProductoServlet" method="post" enctype="multipart/form-data">
    <input type="hidden" name="action" value="actualizar">
    <input type="hidden" name="productoId" value="<%= producto.getProductoId() %>">
    <input type="hidden" name="categoriaId" value="<%= producto.getCategoria().getCategoriaId() %>">
    <input type="hidden" name="imagenActual" value="<%= producto.getImagen() != null ? producto.getImagen() : "" %>">

    <label for="imagen">Imagen (dejar vacío para mantener actual):</label>
    <input type="file" id="imagen" name="imagen" accept="image/*">

    <label for="nombre">Nombre:</label>
    <input type="text" id="nombre" name="nombre" value="<%= producto.getNombre() %>" required>

    <label for="precioUnitario">Precio unitario:</label>
    <input type="number" step="0.01" id="precioUnitario" name="precioUnitario"
           value="<%= producto.getPrecioUnitario() %>" required>

    <label for="precioVenta">Precio venta:</label>
    <input type="number" step="0.01" id="precioVenta" name="precioVenta"
           value="<%= producto.getPrecioVenta() %>" required>

    <label for="stock">Stock:</label>
    <input type="number" id="stock" name="stock" value="<%= producto.getStock() %>" required>

    <label for="materialId">Material:</label>
    <select id="materialId" name="materialId" required>
        <% for (Material m : materiales) { %>
            <option value="<%= m.getMaterialId() %>"
                <%= producto.getMaterial().getMaterialId() == m.getMaterialId() ? "selected" : "" %>>
                <%= m.getNombre() %>
            </option>
        <% } %>
    </select>

    <label for="descripcion">Descripción:</label>
    <textarea id="descripcion" name="descripcion"><%= producto.getDescripcion() != null ? producto.getDescripcion() : "" %></textarea>

    <button type="submit">Actualizar</button>
    <a href="<%=request.getContextPath()%>/ProductoServlet?action=ver&id=<%= producto.getProductoId() %>">Cancelar</a>
</form>

</body>
</html>