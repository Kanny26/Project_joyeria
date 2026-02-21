<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="model.Proveedor, java.util.List"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Listar Ventas</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/Administrador/ventas/listar_ventas.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

    <!-- Navbar -->
    <nav class="navbar-admin">
        <div class="navbar-admin__catalogo">
            <img src="${pageContext.request.contextPath}/assets/Imagenes/iconos/admin.png" alt="Admin">
        </div>
        <h1 class="navbar-admin__title">AAC27</h1>
        <a href="${pageContext.request.contextPath}/Administrador/ventas.jsp">
            <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
        </a>
    </nav>

    <main class="titulo">
        <h2 class="titulo__encabezado">Listar Ventas</h2>

        <section class="Ventas-listar__tabla-contenedor">
            <table class="Ventas-listar__tabla">
                <thead>
                    <tr>
                        <th>Factura</th>
                        <th>Fecha</th>
                        <th>Vendedor</th>
                        <th>Cliente</th>
                        <th>Total venta</th>
                        <th>Método de pago</th>
                        <th>Estado</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="v" items="${ventas}">
                        <tr>
                            <td data-label="Factura">${v.ventaId}</td>
                            <td data-label="Fecha">
                                <fmt:formatDate value="${v.fechaEmision}" pattern="yyyy-MM-dd"/>
                            </td>
                            <td data-label="Vendedor">${v.vendedorNombre}</td>
                            <td data-label="Cliente">${v.clienteNombre}</td>
                            <td data-label="Total venta">
                                <fmt:formatNumber value="${v.total}" type="currency" currencySymbol="$"/>
                            </td>
                            <td data-label="Método de pago">${v.metodoPago}</td>
                            <td data-label="Estado">
                                <span class="estado <c:choose>
                                    <c:when test="${v.estado == 'confirmado'}">estado--activo</c:when>
                                    <c:when test="${v.estado == 'rechazado'}">estado--rechazado</c:when>
                                    <c:otherwise>estado--inactivo</c:otherwise>
                                </c:choose>">
                                    <c:choose>
                                        <c:when test="${v.estado == 'confirmado'}">Pagado</c:when>
                                        <c:when test="${v.estado == 'rechazado'}">Rechazado</c:when>
                                        <c:otherwise>Pendiente</c:otherwise>
                                    </c:choose>
                                </span>
                            </td>
                            <td data-label="Acciones" class="Ventas-listar__acciones">
                                <div class="iconos">
                                    <a href="${pageContext.request.contextPath}/Administrador/ventas/ver?id=${v.ventaId}">
                                        <i class="fas fa-eye icon-right"></i>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/Administrador/ventas/editar?id=${v.ventaId}">
                                        <i class="fa-solid fa-pen-to-square"></i>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/Administrador/ventas/ver?id=${v.ventaId}&accion=pdf" download>
                                        <i class="fa-solid fa-download"></i>
                                    </a>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty ventas}">
                        <tr>
                            <td colspan="8" style="text-align:center;">No hay ventas registradas.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </section>

        <div class="contadores">
            <div class="contador-card">
                <h2>Total Ventas</h2>
                <h3 class="contador-card__numero">${totalVentas}</h3>
            </div>
            <div class="contador-card">
                <h2>Pagos pendientes</h2>
                <h3 class="contador-card__numero">${pendientes}</h3>
            </div>
            <div class="contador-card">
                <h2>Pagos en efectivo</h2>
                <h3 class="contador-card__numero">${pagoEfectivo}</h3>
            </div>
            <div class="contador-card">
                <h2>Pagos por transferencia</h2>
                <h3 class="contador-card__numero">${pagoTransferencia}</h3>
            </div>
        </div>
    </main>

</body>
</html>
