<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar Ventas</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/Administrador/ventas/editar_venta.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
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

    <main class="titulo">
        <h2 class="titulo__encabezado">Editar Ventas</h2>

        <section class="Ventas-editar__tabla-contenedor">
            <table class="Ventas-editar__tabla">
                <thead>
                    <tr>
                        <th>Factura</th>
                        <th>Fecha</th>
                        <th>Vendedor</th>
                        <th>Cliente</th>
                        <th>Total venta</th>
                        <th>Método de pago</th>
                        <th>Estado</th>
                        <th>Guardar</th>
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
                                <%-- Solo se puede editar si NO está confirmado (pagado) --%>
                                <c:choose>
                                    <c:when test="${v.estado == 'confirmado'}">
                                        <span class="estado estado--activo">Pagado</span>
                                    </c:when>
                                    <c:otherwise>
                                        <form method="post"
                                              action="${pageContext.request.contextPath}/Administrador/ventas/editar"
                                              id="form-${v.ventaId}">
                                            <input type="hidden" name="ventaId" value="${v.ventaId}">
                                            <select name="estado" class="estado"
                                                    onchange="document.getElementById('form-${v.ventaId}').submit()">
                                                <option value="pendiente"  ${v.estado == 'pendiente'  ? 'selected' : ''}>Pendiente</option>
                                                <option value="confirmado" ${v.estado == 'confirmado' ? 'selected' : ''}>Pagado</option>
                                                <option value="rechazado"  ${v.estado == 'rechazado'  ? 'selected' : ''}>Rechazado</option>
                                            </select>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td data-label="Guardar">
                                <c:if test="${v.estado != 'confirmado'}">
                                    <button onclick="document.getElementById('form-${v.ventaId}').submit()"
                                            class="btn-guardar">
                                        <i class="fa-solid fa-floppy-disk"></i>
                                    </button>
                                </c:if>
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
