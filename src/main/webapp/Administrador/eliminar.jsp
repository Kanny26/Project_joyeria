<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Producto" %>

<%
    Producto producto = (Producto) request.getAttribute("producto");
    if (producto == null) {
        response.sendRedirect(request.getContextPath() + "/ProductoServlet");
        return;
    }
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Eliminar producto</title>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css" />
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/eliminar.css" />
</head>
<body>

<nav class="navbar-admin"> 
    <div class="navbar-admin__catalogo"> 
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin"> 
    </div> 
    <h1 class="navbar-admin__title">AAC27</h1> 
    <a href="<%=request.getContextPath()%>/ProductoServlet"> 
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i> 
    </a>
</nav>

<main class="eliminar-producto">
    <h1 class="eliminar-producto__titulo">Eliminar producto</h1>

    <section class="eliminar-producto__card">

        <div class="eliminar-producto__vista">
            <div class="eliminar-producto__imagen">
                <img src="<%=request.getContextPath()%>/imagenes/<%= producto.getImagen() %>" alt="<%= producto.getNombre() %>" />

            </div>

            <div class="eliminar-producto__acciones"> 

                <button class="boton editar-imagen"> 
                    <a href="<%=request.getContextPath()%>/ProductoServlet?action=editar&id=<%= producto.getProductoId() %>">
                        <i class="fa-solid fa-pen"></i> Editar producto 
                    </a>
                </button> 

                <button class="boton ver-imagen">
                    <a href="<%=request.getContextPath()%>/ProductoServlet?action=ver&id=<%= producto.getProductoId() %>">
                        <i class="fa-solid fa-eye"></i> Ver producto 
                    </a>
                </button> 

            </div>
        </div>

        <div class="eliminar-producto__info">

            <div class="eliminar-producto__detalle">
                <h3>Detalles del producto</h3>

                <div class="eliminar-producto__item">
                    <h2>Nombre: </h2>
                    <span><%= producto.getNombre() %></span>
                </div>

                <div class="eliminar-producto__item">
                    <h2>Categoría: </h2>
                    <span><%= producto.getCategoria().getNombre() %></span>
                </div>

                <div class="eliminar-producto__item">
                    <h2>Precio: </h2>
                    <span>$<%= producto.getPrecioVenta() %></span>
                </div>

                <div class="eliminar-producto__item">
                    <h2>Clasificación: </h2>
                    <span>4.8 <i class="fa-solid fa-star"></i></span>
                </div>
            </div>

            <div class="eliminar-producto__advertencia">
                <i class="fa-solid fa-exclamation-triangle"></i>
                <div>
                    <h1>¡Advertencia!</h1>
                    <p>
                        ¿Estás seguro de que deseas eliminar este producto?
                        Esta acción no se puede deshacer.
                    </p>
                </div>
            </div>

            <div class="eliminar-producto__acciones">

                <form action="<%=request.getContextPath()%>/ProductoServlet" method="post">
                    <input type="hidden" name="action" value="eliminar">
                    <input type="hidden" name="id" value="<%= producto.getProductoId() %>">

                    <button type="submit" class="boton-confirmar eliminar">  
                        <i class="fa-solid fa-trash-can"></i> Eliminar producto  
                    </button>
                </form>

                <button class="boton cancelar">  
                    <a href="<%=request.getContextPath()%>/ProductoServlet?action=ver&id=<%= producto.getProductoId() %>">
                        <i class="fa-solid fa-xmark"></i> Cancelar  
                    </a>
                </button>

            </div>

        </div>
    </section>
</main>

</body>
</html>

