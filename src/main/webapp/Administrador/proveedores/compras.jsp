<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    Object admin = session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }
    String usuarioId = (String) request.getAttribute("usuarioId");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Compras - ${proveedor.nombre}</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/proveedores/listar.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="${pageContext.request.contextPath}/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>

    <h1 class="navbar-admin__title">AAC27</h1>

    <a href="${pageContext.request.contextPath}/ProveedorServlet?action=listar"
       class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<main class="prov-page">

    <h2 class="prov-page__titulo">
        Compras realizadas a el proveedor — ${proveedor.nombre}
    </h2>

    <!-- CONTADORES -->
    <div class="contadores">
        <div class="contador-card">
            <h2>Total compras</h2>
            <h3 class="contador-card__numero">${totalCompras}</h3>
        </div>

        <div class="contador-card">
            <h2>Total productos</h2>
            <h3 class="contador-card__numero">${totalProductos}</h3>
        </div>

        <div class="contador-card">
            <h2>Total gastado</h2>
            <h3 class="contador-card__numero">
                $<fmt:formatNumber value="${totalGasto}" pattern="#,##0"/>
            </h3>
        </div>
    </div>

    <!-- TOOLBAR -->
    <div class="prov-toolbar">
        <a href="${pageContext.request.contextPath}/CompraServlet?action=nueva&usuarioId=${proveedor.usuarioId}"
		   class="prov-toolbar__btn-nuevo">
		    <i class="fa-solid fa-plus"></i> Nueva compra
		</a>
    </div>

    <!-- GRID -->
    <c:choose>
        <c:when test="${empty listaCompras}">
            <div class="prov-empty">
                <i class="fa-solid fa-box-open prov-empty__icon"></i>
                <p class="prov-empty__texto">
                    Este proveedor aún no tiene compras registradas.
                </p>
            </div>
        </c:when>

        <c:otherwise>
            <div class="prov-grid">

                <c:forEach var="compra" items="${listaCompras}">

                    <article class="prov-card">

                        <!-- HEADER -->
                        <div class="prov-card__header">
                            <div class="prov-card__avatar">
                                <i class="fa-solid fa-cart-shopping"></i>
                            </div>

                            <div class="prov-card__header-info">
                                <h3 class="prov-card__nombre">
                                    Compra #${compra.compraId}
                                </h3>
                                <span class="prov-card__doc">
                                    <i class="fa-solid fa-calendar"></i>
                                    <fmt:formatDate value="${compra.fechaCompra}" pattern="dd/MM/yyyy"/>
                                </span>
                            </div>
                        </div>

                        <!-- BODY -->
                        <div class="prov-card__body">

                            <div class="prov-card__fila">
                                <span class="prov-card__etiqueta">
                                    <i class="fa-solid fa-truck"></i> Fecha entrega
                                </span>
                                <span class="prov-card__valor prov-card__valor--dato">
                                    <fmt:formatDate value="${compra.fechaEntrega}" pattern="dd/MM/yyyy"/>
                                </span>
                            </div>

                            <div class="prov-card__fila">
                                <span class="prov-card__etiqueta">
                                    <i class="fa-solid fa-box"></i> Productos
                                </span>

                                <div class="prov-card__valor prov-card__valor--tags">
                                    <c:forEach var="d" items="${compra.detalles}">
                                        <span class="prov-tag prov-tag--mat">
                                            ${d.productoNombre} x${d.cantidad}
                                        </span>
                                    </c:forEach>
                                </div>
                            </div>

                            <div class="prov-card__fila">
                                <span class="prov-card__etiqueta">
                                    <i class="fa-solid fa-dollar-sign"></i> Total
                                </span>
                                <span class="prov-card__valor prov-card__valor--precio">
                                    $<fmt:formatNumber value="${compra.total}" pattern="#,##0"/>
                                </span>
                            </div>

                        </div>

                        <!-- FOOTER -->
                        <div class="prov-card__footer">
                            <a href="${pageContext.request.contextPath}/CompraServlet?action=eliminar&id=${compra.compraId}&proveedorId=${proveedor.usuarioId}"
                               class="prov-card__accion prov-card__accion--eliminar"
                               onclick="return confirm('¿Eliminar compra?')">
                                <i class="fa-solid fa-trash"></i> Eliminar
                            </a>
                        </div>

                    </article>

                </c:forEach>

            </div>
        </c:otherwise>
    </c:choose>

</main>
</body>
</html>