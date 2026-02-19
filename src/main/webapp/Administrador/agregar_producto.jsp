<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, model.Material, model.Administrador, model.Categoria" %>
<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }
    List<Material> materiales = (List<Material>) request.getAttribute("materiales");
    Categoria categoria       = (Categoria)      request.getAttribute("categoria");

    if (materiales == null || categoria == null) {
        response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
        return;
    }

    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Agregar producto</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/agregar_producto.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <div class="navbar-admin__usuario">
        <span><%= admin.getNombre() %></span>
    </div>
    <a href="<%=request.getContextPath()%>/CategoriaServlet?id=<%= categoria.getCategoriaId() %>">
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
    </a>
</nav>

<main class="form-product-container">
    <h2 class="form-product-container__title">Nuevo producto — <%= categoria.getNombre() %></h2>

    <% if (error != null) { %>
        <div class="alert alert-danger">
            <i class="fa-solid fa-circle-exclamation"></i> <%= error %>
        </div>
    <% } %>

    <form class="form-product"
          method="post"
          action="<%=request.getContextPath()%>/ProductoServlet"
          enctype="multipart/form-data">

        <input type="hidden" name="action"      value="guardar">
        <input type="hidden" name="categoriaId" value="<%= categoria.getCategoriaId() %>">

        <div class="form-product__group">
            <label class="form-product__label">Categoría</label>
            <input type="text"
                   class="form-product__input"
                   value="<%= categoria.getNombre() %>"
                   disabled>
        </div>

        <div class="form-product__row">

            <div class="form-product__group">
                <label class="form-product__label">Nombre *</label>
                <input type="text"
                       name="nombre"
                       class="form-product__input"
                       placeholder="Nombre del producto"
                       required>
            </div>

            <div class="form-product__group">
                <label class="form-product__label">Precio de costo *</label>
                <input type="number"
                       name="precioUnitario"
                       class="form-product__input"
                       placeholder="0.00"
                       min="0.01"
                       step="0.01"
                       required>
            </div>

            <div class="form-product__group">
                <label class="form-product__label">Precio de venta *</label>
                <input type="number"
                       name="precioVenta"
                       class="form-product__input"
                       placeholder="0.00"
                       min="0.01"
                       step="0.01"
                       required>
            </div>

            <div class="form-product__group">
                <label class="form-product__label">Stock *</label>
                <input type="number"
                       name="stock"
                       class="form-product__input"
                       placeholder="0"
                       min="0"
                       required>
            </div>

            <div class="form-product__group">
                <label class="form-product__label">Material *</label>
                <select name="materialId"
                        class="form-product__input"
                        required>
                    <option value="">Seleccione material</option>
                    <% for (Material m : materiales) { %>
                        <option value="<%= m.getMaterialId() %>">
                            <%= m.getNombre() %>
                        </option>
                    <% } %>
                </select>
            </div>

            <div class="form-product__group">
                <label class="form-product__label">Imagen del producto *</label>
                <!-- ✅ CORRECTO: accept="image/*" en lugar de "/imagenes/*" -->
                <input type="file"
                       name="imagen"
                       class="form-product__input"
                       accept="image/*"
                       required>
            </div>

        </div>

        <div class="form-product__group">
            <label class="form-product__label">Descripción</label>
            <textarea name="descripcion"
                      class="form-product__input"
                      rows="4"
                      placeholder="Descripción del producto"></textarea>
        </div>

        <div class="form-product__actions">
            <button type="submit" class="form-product__btn">
                <i class="fa-solid fa-floppy-disk"></i> Guardar producto
            </button>
            <button type="button" class="btn-danger"
                onclick="window.location.href='<%=request.getContextPath()%>/CategoriaServlet?id=<%= categoria.getCategoriaId() %>'">
                <i class="fa-solid fa-xmark"></i> Cancelar
            </button>
        </div>

    </form>
</main>
</body>
</html>