<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Casos Postventa</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/Administrador/ventas/casos_postventa.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

    <nav class="navbar-admin">
        <div class="navbar-admin__catalogo">
            <img src="${pageContext.request.contextPath}/assets/Imagenes/iconos/admin.png" alt="Admin">
        </div>
        <h1 class="navbar-admin__title">AAC27</h1>
        <a href="${pageContext.request.contextPath}/Administrador/ventas/postventa">
            <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
        </a>
    </nav>

    <main class="titulo">
        <h2 class="titulo__encabezado">Detalle casos postventas</h2>

        <section class="postventa-listar__contenedor">

            <!-- TABLA RESUMEN DE TODOS LOS CASOS -->
            <table class="reclamo__tabla">
                <thead>
                    <tr>
                        <th>Factura</th>
                        <th>Fecha</th>
                        <th>Estado</th>
                        <th>Caso</th>
                        <th>Cliente</th>
                        <th>Tipo</th>
                        <th>Motivo</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="c" items="${casos}">
                        <tr class="${not empty caso and caso.casoId == c.casoId ? 'fila-activa' : ''}">
                            <td data-label="Factura">${c.ventaId}</td>
                            <td data-label="Fecha">
                                <fmt:formatDate value="${c.fecha}" pattern="dd/MM/yyyy"/>
                            </td>
                            <td data-label="Estado">${c.estado}</td>
                            <td data-label="Caso"><fmt:formatNumber value="${c.casoId}" pattern="000"/></td>
                            <td data-label="Cliente">${c.clienteNombre}</td>
                            <td data-label="Tipo">${c.tipo}</td>
                            <td data-label="Motivo">${c.motivo}</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty casos}">
                        <tr>
                            <td colspan="7" style="text-align:center;">No hay casos registrados.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>

            <!-- DETALLE DEL CASO SELECCIONADO -->
            <c:if test="${not empty caso}">
                <div class="reclamo__observaciones">
                    <h4>Detalle — Caso #<fmt:formatNumber value="${caso.casoId}" pattern="000"/></h4>
                    <p><strong>Vendedor:</strong>  ${caso.vendedorNombre}</p>
                    <p><strong>Cliente:</strong>   ${caso.clienteNombre}</p>
                    <p><strong>Tipo:</strong>      ${caso.tipo}</p>
                    <p><strong>Cantidad:</strong>  ${caso.cantidad}</p>
                    <p><strong>Motivo:</strong>    ${caso.motivo}</p>
                    <p><strong>Estado:</strong>    ${caso.estado}</p>
                    <c:if test="${not empty caso.observacion}">
                        <p><strong>Observación:</strong> ${caso.observacion}</p>
                    </c:if>

                    <!-- Formulario para actualizar el estado con observación -->
                    <form method="post"
                          action="${pageContext.request.contextPath}/Administrador/ventas/caso"
                          class="form-estado">
                        <input type="hidden" name="casoId" value="${caso.casoId}">
                        <label for="estado">Cambiar estado:</label>
                        <select name="estado" id="estado" class="estado">
                            <option value="en_proceso" ${caso.estado == 'en_proceso' ? 'selected' : ''}>En proceso</option>
                            <option value="aprobado"   ${caso.estado == 'aprobado'   ? 'selected' : ''}>Aprobado</option>
                            <option value="cancelado"  ${caso.estado == 'cancelado'  ? 'selected' : ''}>Cancelado</option>
                        </select>
                        <label for="observacion">Observación:</label>
                        <textarea name="observacion" id="observacion" rows="3">${caso.observacion}</textarea>
                        <button type="submit" class="btn-guardar">
                            <i class="fa-solid fa-floppy-disk"></i> Guardar
                        </button>
                    </form>
                </div>
            </c:if>

        </section>
    </main>

</body>
</html>
