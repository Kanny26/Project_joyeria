<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="model.Proveedor, java.util.List"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Postventa</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/Administrador/ventas/postventa.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

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
        <h2 class="titulo__encabezado">Devolución, Cambios y Reclamos</h2>

        <section class="postventa-cards__contenedor">
            <c:forEach var="caso" items="${casos}">
                <article class="postventa-cards__card">
                    <h3 class="postventa-cards__caso">Caso #<fmt:formatNumber value="${caso.casoId}" pattern="000"/></h3>
                    <p><strong>Fecha:</strong>
                        <fmt:formatDate value="${caso.fecha}" pattern="yyyy-MM-dd"/>
                    </p>
                    <p><strong>Vendedor:</strong> ${caso.vendedorNombre}</p>
                    <p><strong>Cliente:</strong>  ${caso.clienteNombre}</p>
                    <p><strong>Tipo:</strong>     ${caso.tipo}</p>
                    <p><strong>Motivo:</strong>   ${caso.motivo}</p>
                    <p>
                        <strong>Estado:</strong>
                        <form method="post"
                              action="${pageContext.request.contextPath}/Administrador/ventas/caso"
                              id="form-caso-${caso.casoId}"
                              style="display:inline">
                            <input type="hidden" name="casoId" value="${caso.casoId}">
                            <input type="hidden" name="observacion" value="">
                            <select class="estado" name="estado"
                                    onchange="document.getElementById('form-caso-${caso.casoId}').submit()">
                                <option value="en_proceso" ${caso.estado == 'en_proceso' ? 'selected' : ''}>En proceso</option>
                                <option value="aprobado"   ${caso.estado == 'aprobado'   ? 'selected' : ''}>Aprobado</option>
                                <option value="cancelado"  ${caso.estado == 'cancelado'  ? 'selected' : ''}>Cancelado</option>
                            </select>
                        </form>
                    </p>
                    <div class="postventa-cards__acciones">
                        <a href="${pageContext.request.contextPath}/Administrador/ventas/caso?id=${caso.casoId}">
                            <i class="fas fa-eye"></i>
                        </a>
                    </div>
                </article>
            </c:forEach>

            <c:if test="${empty casos}">
                <p style="text-align:center;">No hay casos postventa registrados.</p>
            </c:if>
        </section>

        <div class="contadores">
            <div class="contador-card">
                <h2>Total postventa</h2>
                <h3 class="contador-card__numero">${totalCasos}</h3>
            </div>
            <div class="contador-card">
                <h2>En proceso</h2>
                <h3 class="contador-card__numero">${casosPendientes}</h3>
            </div>
        </div>
    </main>

</body>
</html>
