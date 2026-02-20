<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, model.Producto, model.Categoria, model.Administrador" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }

    List<Producto> productos    = (List<Producto>) request.getAttribute("productos");
    Categoria      categoria    = (Categoria)      request.getAttribute("categoria");
    String         termino      = (String)         request.getAttribute("terminoBusqueda");
    String         filtroActivo = (String)         request.getAttribute("filtroActivo");

    if (termino      == null) termino      = "";
    if (filtroActivo == null) filtroActivo = "todos";

    DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= categoria != null ? categoria.getNombre() : "Búsqueda" %> - AAC27</title>

    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/gest-productos.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    
    <a href="<%=request.getContextPath()%>/CategoriaServlet"
	   class="navbar-admin__home-link">
	    <span class="navbar-admin__home-icon-wrap">
	        <i class="fa-solid fa-arrow-left"></i>
		    <span class="navbar-admin__home-text">Volver atras</span>
		    <i class="fa-solid fa-house-chimney"></i>
	    </span>
	</a>
</nav>

<main class="titulo">
    <h2 class="titulo__encabezado">
        <%= categoria != null ? categoria.getNombre() : "Resultados de búsqueda" %>
    </h2>

    <div class="cards__barra-superior">

        <!-- Botón Agregar Producto -->
        <% if (categoria != null) { %>
        <a href="<%=request.getContextPath()%>/ProductoServlet?action=nuevo&categoria=<%= categoria.getCategoriaId() %>"
           class="cards__boton-agregar">
            <i class="fa-solid fa-plus"></i> Agregar Producto
        </a>
        <% } %>

        <!-- Barra de búsqueda con selector de filtro -->
        <form action="<%=request.getContextPath()%>/CategoriaServlet"
              method="get"
              class="cards__busqueda"
              id="formBusqueda">

            <% if (categoria != null) { %>
                <input type="hidden" name="id" value="<%= categoria.getCategoriaId() %>">
            <% } %>

            <!-- Selector de campo a filtrar -->
            <select name="filtro"
                    id="filtroSelect"
                    class="cards__busqueda-filtro"
                    onchange="actualizarPlaceholder(this)">
                <option value="todos"    <%= "todos".equals(filtroActivo)    ? "selected" : "" %>>Todos</option>
                <option value="nombre"   <%= "nombre".equals(filtroActivo)   ? "selected" : "" %>>Nombre</option>
                <option value="material" <%= "material".equals(filtroActivo) ? "selected" : "" %>>Material</option>
                <option value="stock"    <%= "stock".equals(filtroActivo)    ? "selected" : "" %>>Stock</option>
            </select>

            <!-- Separador visual -->
            <span class="cards__busqueda-sep"></span>

            <!-- Input de texto -->
            <input type="text"
                   name="q"
                   id="searchInput"
                   class="cards__busqueda-input"
                   placeholder="Buscar productos..."
                   value="<%= termino %>"
                   autocomplete="off">

            <!-- Icono lupa -->
            <i class="fa-solid fa-magnifying-glass cards__busqueda-icono"></i>

            <!-- Botón limpiar (solo si hay búsqueda activa) -->
            <% if (!termino.isEmpty()) { %>
                <a href="<%=request.getContextPath()%>/CategoriaServlet<%= categoria != null ? "?id=" + categoria.getCategoriaId() : "" %>"
                   class="cards__busqueda-clear"
                   title="Limpiar búsqueda">
                    <i class="fa-solid fa-xmark"></i>
                </a>
            <% } %>
        </form>
    </div>

    <!-- Info de búsqueda activa -->
    <% if (!termino.isEmpty()) { %>
        <div class="cards__busqueda-info">
            <i class="fa-solid fa-circle-info"></i>
            <span>
                <strong><%= productos != null ? productos.size() : 0 %></strong>
                resultado<%= (productos == null || productos.size() != 1) ? "s" : "" %>
                para &ldquo;<em><%= termino %></em>&rdquo;
                <% if (!"todos".equals(filtroActivo)) { %>
                    &nbsp;&mdash;&nbsp; filtrado por
                    <span class="cards__busqueda-badge">
                        <%= "nombre".equals(filtroActivo) ? "nombre"
                          : "material".equals(filtroActivo) ? "material"
                          : "stock" %>
                    </span>
                <% } %>
            </span>
        </div>
    <% } %>

    <!-- Grid de productos -->
    <section class="cards__contenedor">

        <% if (productos != null && !productos.isEmpty()) {
            for (Producto p : productos) { %>

        <div class="cards__contenedor-content">

            <a href="<%=request.getContextPath()%>/ProductoServlet?action=ver&id=<%= p.getProductoId() %>">
                <img src="<%=request.getContextPath()%>/imagen-producto/<%= p.getProductoId() %>"
                     alt="<%= p.getNombre() %>"
                     onerror="this.src='<%=request.getContextPath()%>/assets/Imagenes/default.png'">
            </a>

            <h3 class="product__code">
                <span class="product__label">Código:</span>
                <span class="product__value"><%= p.getCodigo() %></span>
            </h3>

            <h3 class="product__name">
                <span class="product__value"><%= p.getNombre() %></span>
            </h3>

            <h4 class="product__category">
                <span class="product__label">Categoría:</span>
                <span class="product__value"><%= categoria != null ? categoria.getNombre() : p.getCategoria().getNombre() %></span>
            </h4>

            <h4 class="product__material">
                <span class="product__label">Material:</span>
                <span class="product__value"><%= p.getMaterial().getNombre() %></span>
            </h4>

            <h4 class="product__cost">
                <span class="product__label">Precio de costo:</span>
                <span class="product__value">$<%= String.format("%,.0f", p.getPrecioUnitario()) %></span>
            </h4>

            <h4 class="product__price">
                <span class="product__label">Precio venta:</span>
                <span class="product__value">$<%= String.format("%,.0f", p.getPrecioVenta()) %></span>
            </h4>

            <h4 class="product__stock">
                <span class="product__label">Stock:</span>
                <span class="product__value <%= p.getStock() <= 3 ? "stock-bajo" : "" %>">
                    <%= p.getStock() %>
                    <% if (p.getStock() <= 3) { %>
                        <i class="fa-solid fa-triangle-exclamation" title="Stock bajo"></i>
                    <% } %>
                </span>
            </h4>

            <h4 class="product__date">
                <span class="product__label">En stock desde:</span>
                <span class="product__value"><%= p.getFechaRegistro().format(formato) %></span>
            </h4>

            <div class="iconos">
                <a href="<%=request.getContextPath()%>/ProductoServlet?action=ver&id=<%= p.getProductoId() %>"
                   title="Ver producto">
                    <i class="fas fa-eye icon-right"></i>
                </a>
                <a href="<%=request.getContextPath()%>/ProductoServlet?action=editar&id=<%= p.getProductoId() %>"
                   title="Editar producto">
                    <i class="fa-solid fa-pen-to-square"></i>
                </a>
                <a href="<%=request.getContextPath()%>/ProductoServlet?action=confirmarEliminar&id=<%= p.getProductoId() %>"
                   title="Eliminar producto">
                    <i class="fa-solid fa-trash"></i>
                </a>
            </div>
        </div>

        <% } } else { %>
            <div class="cards__vacio">
                <i class="fa-solid fa-box-open"></i>
                <p>
                    <% if (!termino.isEmpty()) { %>
                        No se encontraron productos para &ldquo;<strong><%= termino %></strong>&rdquo;.
                    <% } else { %>
                        No hay productos en esta categoría todavía.
                    <% } %>
            </div>
        <% } %>

    </section>
</main>

<script>
const placeholders = {
    todos:    'Buscar por nombre, material, stock...',
    nombre:   'Ej: anillo, collar, topito...',
    material: 'Ej: acero inoxidable, plata...',
    stock:    'Ej: 5, 10, 0...'
};

function actualizarPlaceholder(sel) {
    document.getElementById('searchInput').placeholder =
        placeholders[sel.value] || 'Buscar productos...';
}

// Aplicar placeholder correcto al cargar
actualizarPlaceholder(document.getElementById('filtroSelect'));

// Enviar con Enter
document.getElementById('searchInput').addEventListener('keydown', function(e) {
    if (e.key === 'Enter') {
        e.preventDefault();
        document.getElementById('formBusqueda').submit();
    }
});
</script>

</body>
</html>
