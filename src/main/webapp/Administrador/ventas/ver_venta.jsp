<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ver Venta</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/Administrador/ventas/ver_ventas.css">
</head>
<body>

    <nav class="navbar-admin">
        <div class="navbar-admin__catalogo">
            <img src="${pageContext.request.contextPath}/assets/Imagenes/iconos/admin.png" alt="Admin">
        </div>
        <h1 class="navbar-admin__title">AAC27</h1>
        <a href="${pageContext.request.contextPath}/Administrador/ventas/listar">
            <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
        </a>
    </nav>

    <c:if test="${not empty venta}">
    <div class="factura">

        <!-- ENCABEZADO -->
        <header class="factura__header">
            <div class="factura__logo">
                <h1>Abby.accesorios</h1>
                <p class="slogan">Tu lugar favorito</p>
            </div>
            <div class="factura__info">
                <p><strong>No. Factura:</strong> ${venta.ventaId}</p>
                <p><strong>Fecha:</strong>
                    <fmt:formatDate value="${venta.fechaEmision}" pattern="dd/MM/yyyy"/>
                </p>
                <p><strong>Método de pago:</strong> ${venta.metodoPago}</p>
            </div>
        </header>

        <div class="linea-div"></div>

        <!-- CLIENTE -->
        <section class="factura__cliente">
            <p class="label">Factura para:</p>
            <h2 class="cliente-nombre">${venta.clienteNombre}</h2>
            <p>Vendedor: ${venta.vendedorNombre}</p>
            <span class="estado <c:choose>
                <c:when test="${venta.estado == 'confirmado'}">estado--pagado</c:when>
                <c:when test="${venta.estado == 'rechazado'}">estado--rechazado</c:when>
                <c:otherwise>estado--pendiente</c:otherwise>
            </c:choose>">
                <c:choose>
                    <c:when test="${venta.estado == 'confirmado'}">Pagado</c:when>
                    <c:when test="${venta.estado == 'rechazado'}">Rechazado</c:when>
                    <c:otherwise>Pendiente</c:otherwise>
                </c:choose>
            </span>
        </section>

        <!-- TABLA DE PRODUCTOS -->
        <table class="tabla">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Descripción</th>
                    <th>Precio Unitario</th>
                    <th>Cant.</th>
                    <th>Total</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="d" items="${venta.detalles}" varStatus="s">
                    <tr>
                        <td>${s.count}</td>
                        <td>${d.productoNombre}</td>
                        <td><fmt:formatNumber value="${d.precioUnitario}" type="currency" currencySymbol="$"/></td>
                        <td>${d.cantidad}</td>
                        <td><fmt:formatNumber value="${d.subtotal}" type="currency" currencySymbol="$"/></td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>

        <!-- TOTALES -->
        <div class="totales">
            <p class="total-final">
                <span>Total:</span>
                <fmt:formatNumber value="${venta.total}" type="currency" currencySymbol="$"/>
            </p>
        </div>

        <!-- CONDICIONES -->
        <section class="condiciones">
            <h3>Términos y condiciones</h3>
            <p>Gracias por su compra. Esta factura corresponde a los servicios prestados
               y debe conservarse como comprobante.</p>
        </section>

    </div>
    </c:if>

    <c:if test="${empty venta}">
        <div class="titulo">
            <p style="text-align:center; margin-top:2rem;">No se encontró la venta solicitada.</p>
            <a href="${pageContext.request.contextPath}/Administrador/ventas/listar">Volver al listado</a>
        </div>
    </c:if>

</body>
</html>
