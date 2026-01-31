<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, model.Material" %>
<%@ page import="model.Administrador, model.Categoria" %>

<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }

    List<Material> materiales = (List<Material>) request.getAttribute("materiales");
    Categoria categoria = (Categoria) request.getAttribute("categoria");
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
    <title>Agregar producto</title>

    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/agregar_producto.css">
</head>

<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin">
        <h2>Volver al inicio</h2>
    </div>

    <h1 class="navbar-admin__title">AAC27</h1>

    <div class="navbar-admin__usuario">
        <span><%= admin.getNombre() %></span>
    </div>

    <a href="<%=request.getContextPath()%>/index.jsp">
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
    </a>
</nav>

<main class="form-product-container">

    <h2 class="form-product-container__title">Nuevo producto</h2>

    <!-- 🔴 enctype necesario para imagen -->
    <form class="form-product"
          method="post"
          action="<%=request.getContextPath()%>/ProductoServlet"
          enctype="multipart/form-data">

        <input type="hidden" name="action" value="guardar">
        <input type="hidden" name="categoriaId" value="<%= categoria.getCategoriaId() %>">

        <!-- Mostrar categoría -->
        <div class="form-product__group">
            <label class="form-product__label">Categoría</label>
            <input type="text"
                   class="form-product__input"
                   value="<%= categoria.getNombre() %>"
                   disabled>
        </div>

        <div class="form-product__row">

            <div class="form-product__group">
                <label class="form-product__label">Nombre</label>
                <input type="text"
                       name="nombre"
                       class="form-product__input"
                       placeholder="Nombre del producto"
                       required>
            </div>

            <!-- 🔴 CORREGIDO: precioUnitario -->
            <div class="form-product__group">
                <label class="form-product__label">Precio unitario</label>
                <input type="number"
                       name="precioUnitario"
                       class="form-product__input"
                       placeholder="Precio"
                       min="0"
                       step="0.01"
                       required>
            </div>

			<div class="form-product__group">
			    <label class="form-product__label">Precio de venta</label>
			    <input type="number"
			           name="precioVenta"
			           class="form-product__input"
			           placeholder="Precio de venta"
			           min="0"
			           step="0.01"
			           required>
			</div>
						
            <div class="form-product__group">
                <label class="form-product__label">Stock</label>
                <input type="number"
                       name="stock"
                       class="form-product__input"
                       placeholder="Stock"
                       min="0"
                       required>
            </div>

            <div class="form-product__group">
                <label class="form-product__label">Material</label>
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
                <label class="form-product__label">Imagen del producto</label>
                <input type="file"
                       name="imagen"
                       class="form-product__input"
                       accept="/imagenes/*"
                       required>
            </div>

        </div>

        <div class="form-product__group">
            <label class="form-product__label">Descripción</label>
            <textarea name="descripcion"
                      class="form-product__input"
                      placeholder="Descripción del producto"></textarea>
        </div>

        <button type="submit" class="form-product__btn">
            Guardar producto
        </button>

    </form>

</main>

</body>
</html>
