<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, model.Producto, model.Categoria, model.Administrador" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }

    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
    Categoria categoria = (Categoria) request.getAttribute("categoria");
    
    DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= categoria.getNombre() %></title>

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

    <a href="<%=request.getContextPath()%>/CategoriaServlet">
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
    </a>
</nav>

<main class="titulo">
    <h2 class="titulo__encabezado"><%= categoria.getNombre() %></h2>

    <div class="cards__barra-superior">

        <!-- Botón Agregar Producto -->
        <a href="<%=request.getContextPath()%>/ProductoServlet?action=nuevo&categoria=<%= categoria.getCategoriaId() %>"
           class="cards__boton-agregar">
            <i class="fa-solid fa-plus"></i> Agregar Producto
        </a>

        <form action="<%=request.getContextPath()%>/CategoriaServlet" method="get" class="cards__busqueda">
		    <% if (categoria != null) { %>
		        <input type="hidden" name="id" value="<%= categoria.getCategoriaId() %>">
		    <% } %>
		    <input type="text"
		           name="q"
		           class="cards__busqueda-input"
		           placeholder="Buscar en todos los productos..."
		           value="<%= request.getParameter("q") != null ? request.getParameter("q") : "" %>">
		    <i class="fa-solid fa-magnifying-glass cards__busqueda-icono"></i>
		</form>
		
    </div>

    <section class="cards__contenedor">

        <% if (productos != null && !productos.isEmpty()) {
            for (Producto p : productos) {
                String img = (p.getImagen() == null || p.getImagen().isEmpty())
                        ? "default.jpg"
                        : p.getImagen();
        %>

        <div class="cards__contenedor-content">

            <a href="<%=request.getContextPath()%>/ProductoServlet?action=ver&id=<%= p.getProductoId() %>">
                <img src="<%=request.getContextPath()%>/imagenes/<%= img %>" alt="Producto">
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
			    <span class="product__value"><%= categoria.getNombre() %></span>
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
			    <span class="product__value"><%= p.getStock() %></span>
			</h4>
			
			<h4 class="product__date">
			    <span class="product__label">En stock desde:</span>
			    <span class="product__value"><%= p.getFechaRegistro().format(formato) %></span>
			</h4>


            <div class="iconos">
                <a href="<%=request.getContextPath()%>/ProductoServlet?action=ver&id=<%= p.getProductoId() %>">
                    <i class="fas fa-eye icon-right"></i>
                </a>
                <a href="<%=request.getContextPath()%>/ProductoServlet?action=editar&id=<%= p.getProductoId() %>">
                    <i class="fa-solid fa-pen-to-square"></i>
                </a>
                <a href="<%=request.getContextPath()%>/ProductoServlet?action=confirmarEliminar&id=<%= p.getProductoId() %>">
                    <i class="fa-solid fa-trash"></i>
                </a>
            </div>
        </div>

        <% } } else { %>
            <p>No hay productos en esta categoría</p>
        <% } %>

    </section>
</main>

</body>
</html>
