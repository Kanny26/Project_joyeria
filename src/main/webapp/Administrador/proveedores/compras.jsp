<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Compras proveedor</title>

    <!-- Estilos -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/Administrador/proveedores/compras.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
    <!-- Navbar -->
    <nav class="navbar-admin">
        <div class="navbar-admin__catalogo"> 
            <img src="${pageContext.request.contextPath}/assets/Imagenes/iconos/admin.png" alt="Admin">
        </div> 

        <h1 class="navbar-admin__title">AAC27</h1>
        <a href="${pageContext.request.contextPath}/pages/Administrador/proveedores/proveedores.html">
            <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
        </a>
    </nav>

    <main class="titulo">
        <h2 class="titulo__encabezado">Compras realizadas al proveedor</h2>

        <section class="Compras-listar__tabla-contenedor">
            <table class="Compras-listar__tabla">
                <thead>
                    <tr>
                        <th>Codigo compra</th>
                        <th>Producto</th>
                        <th>Cantidad</th>
                        <th>Precio unitario</th>
                        <th>Total</th>
                        <th>Fecha</th>
                        <th>Observaciones</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="compra" items="${listaCompras}">
                        <tr>
                            <td data-label="Codigo compra">${compra.codigoCompra}</td>
                            <td data-label="Producto">${compra.producto}</td>
                            <td data-label="Cantidad">${compra.cantidad}</td>
                            <td data-label="Precio unitario">$<fmt:formatNumber value="${compra.precioUnitario}" pattern="#,##0" /></td>
                            <td data-label="Total">$<fmt:formatNumber value="${compra.total}" pattern="#,##0" /></td>
                            <td data-label="Fecha"><fmt:formatDate value="${compra.fecha}" pattern="dd/MM/yyyy" /></td>
                            <td data-label="Observaciones">${compra.observaciones}</td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </section>

        <div class="contadores">
            <div class="contador-card">
                <h2>Total compras</h2>
                <h3 class="contador-card__numero">${totalCompras}</h3>
            </div>
            <div class="contador-card">
                <h2>Total productos comprados</h2>
                <h3 class="contador-card__numero">${totalProductos}</h3>
            </div>
            <div class="contador-card">
                <h2>Total gasto</h2>
                <h3 class="contador-card__numero">$<fmt:formatNumber value="${totalGasto}" pattern="#,##0" /></h3>
            </div>
        </div>
    </main>
</body>
</html>