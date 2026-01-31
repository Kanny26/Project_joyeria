<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="model.Producto, model.Administrador, java.util.List, model.Material" %>

<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }

    Producto producto = (Producto) request.getAttribute("producto");
    List<Material> materiales = (List<Material>) request.getAttribute("materiales");

    if (producto == null || materiales == null) {
        response.sendRedirect(request.getContextPath() + "/ProductoServlet");
        return;
    }
%>
<%
    String error = (String) request.getAttribute("error");
    if (error != null) {
%>
    <div class="alert alert-danger">
        <%= error %>
    </div>
<%
    }
%>


<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar producto</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/editar.css">
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/Administrador/org-categorias.jsp">
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
    </a>
</nav>

<main class="editar-producto">
    <h1 class="editar-producto__titulo">Editar producto</h1>

    <form class="editar-producto__detalles"
          action="<%= request.getContextPath() %>/ProductoServlet"
          method="post"
          enctype="multipart/form-data">

        <input type="hidden" name="action" value="actualizar">
        <input type="hidden" name="productoId" value="<%= producto.getProductoId() %>">
        <input type="hidden" name="imagenActual" value="<%= producto.getImagen() != null ? producto.getImagen() : "" %>">
        <input type="hidden" name="categoriaId" value="<%= producto.getCategoria().getCategoriaId() %>">
        <input type="hidden" name="stock" value="<%= producto.getStock() %>">

        <div class="editar-producto__contenido">

            <div class="editar-producto__imagen">
                <img src="<%= request.getContextPath() %>/imagenes/<%= 
                        (producto.getImagen() != null && !producto.getImagen().isEmpty())
                                ? producto.getImagen()
                                : "default.jpg"
                %>" alt="<%= producto.getNombre() %>">

                <label for="nuevaImagen" class="editar-producto__label">
                    <i class="fa-solid fa-pen"></i> Cambiar imagen
                </label>
                <input type="file" id="nuevaImagen" name="imagen" accept="image/*" hidden>
            </div>

            <div class="editar-producto__campos">
                <label for="nombre">Nombre del producto</label>
                <input id="nombre" type="text" name="nombre" value="<%= producto.getNombre() %>" class="editar-producto__input-texto" required>

                <label>Categoría</label>
                <input type="text" class="editar-producto__input-texto" value="<%= producto.getCategoria().getNombre() %>" disabled>

                <!-- ✅ CORREGIDO: name="precioUnitario" para precio de costo -->
                <label for="precioCosto">Precio de costo</label>
                <input id="precioCosto" type="number" step="0.05" name="precioUnitario" 
                       value="<%= producto.getPrecioUnitario() != null ? producto.getPrecioUnitario().toString() : "0.00" %>" 
                       class="editar-producto__input-texto" required>

                <!-- ✅ CORREGIDO: name="precioVenta" solo para precio de venta -->
                <label for="precioVenta">Precio de venta</label>
                <input id="precioVenta" type="number" step="0.05" name="precioVenta" 
                       value="<%= producto.getPrecioVenta() != null ? producto.getPrecioVenta().toString() : "0.00" %>" 
                       class="editar-producto__input-texto" required>

                <label for="material">Material</label>
                <select id="material" name="materialId" class="editar-producto__input-select" required>
                    <% for (Material m : materiales) { %>
                        <option value="<%= m.getMaterialId() %>"
                            <%= producto.getMaterial().getMaterialId() == m.getMaterialId() ? "selected" : "" %>>
                            <%= m.getNombre() %>
                        </option>
                    <% } %>
                </select>

                <label for="descripcion">Descripción</label>
                <textarea id="descripcion" name="descripcion" class="editar-producto__input-area" rows="4"><%= producto.getDescripcion() != null ? producto.getDescripcion() : "" %></textarea>

                <div class="editar-producto__acciones">
                    <button type="submit" class="boton boton--principal">
                        <i class="fa-solid fa-floppy-disk"></i> Guardar
                    </button>

                    <a href="<%= request.getContextPath() %>/CategoriaServlet?id=<%= producto.getCategoria().getCategoriaId() %>"
                       class="boton boton--secundario">
                        <i class="fa-solid fa-xmark"></i> Cancelar
                    </a>
                </div>
            </div>
        </div>
    </form>
</main>
</body>
</html>
