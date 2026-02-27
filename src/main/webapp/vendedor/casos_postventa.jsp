<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="model.CasoPostventa" %>
<%
    Object vendedorSesion = session.getAttribute("vendedor");
    if (vendedorSesion == null) {
        response.sendRedirect(request.getContextPath() + "/vendedor/inicio-sesion.jsp");
        return;
    }
    List<CasoPostventa> casos = (List<CasoPostventa>) request.getAttribute("casos");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mis Casos Postventa | AAC27</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Vendedor/registrar_venta.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/Seller.png" alt="Vendedor">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=misVentas" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Mis ventas</span>
        </span>
    </a>
</nav>

<main class="prov-page">
    <h1 class="prov-page__titulo">Mis Casos Postventa</h1>

    <%-- Contadores --%>
    <%
        int totalCasos = casos != null ? casos.size() : 0;
        int enProceso = 0, aprobados = 0, cancelados = 0;
        if (casos != null) {
            for (CasoPostventa c : casos) {
                if ("aprobado".equals(c.getEstado()))       aprobados++;
                else if ("cancelado".equals(c.getEstado())) cancelados++;
                else                                         enProceso++;
            }
        }
    %>
    <div class="contadores">
        <div class="contador-card" style="--color-fondo-contadores:#f5f3ff;--borde-contadores:#7c3aed;--color-fondo-num-contadores:#7c3aed;">
            <div><div style="font-weight:700;color:#1e1b4b;">Total</div><div style="font-size:13px;color:#6b7280;">Registrados</div></div>
            <div class="contador-card__numero"><%= totalCasos %></div>
        </div>
        <div class="contador-card" style="--color-fondo-contadores:#fef9c3;--borde-contadores:#eab308;--color-fondo-num-contadores:#ca8a04;">
            <div><div style="font-weight:700;color:#1e1b4b;">En proceso</div><div style="font-size:13px;color:#6b7280;">Pendientes</div></div>
            <div class="contador-card__numero"><%= enProceso %></div>
        </div>
        <div class="contador-card" style="--color-fondo-contadores:#dcfce7;--borde-contadores:#22c55e;--color-fondo-num-contadores:#16a34a;">
            <div><div style="font-weight:700;color:#1e1b4b;">Aprobados</div><div style="font-size:13px;color:#6b7280;">Resueltos</div></div>
            <div class="contador-card__numero"><%= aprobados %></div>
        </div>
        <div class="contador-card" style="--color-fondo-contadores:#fee2e2;--borde-contadores:#ef4444;--color-fondo-num-contadores:#dc2626;">
            <div><div style="font-weight:700;color:#1e1b4b;">Cancelados</div><div style="font-size:13px;color:#6b7280;">No procedieron</div></div>
            <div class="contador-card__numero"><%= cancelados %></div>
        </div>
    </div>

    <%-- Filtros --%>
    <div class="prov-toolbar">
        <div class="cards__busqueda">
            <select class="cards__busqueda-filtro" id="filtroTipo" onchange="filtrar()">
                <option value="">Todos los tipos</option>
                <option value="cambio">Cambio</option>
                <option value="devolucion">Devolución</option>
                <option value="reclamo">Reclamo</option>
            </select>
            <input type="text" class="cards__busqueda-input" id="buscador"
                   placeholder="Buscar por venta, motivo..."
                   oninput="filtrar()">
            <i class="fa-solid fa-magnifying-glass cards__busqueda-icono"></i>
        </div>
        <select id="filtroEstado" onchange="filtrar()"
                style="border-radius:10px;height:44px;padding:0 14px;border:1px solid #ccc;font-family:inherit;font-size:13px;font-weight:700;">
            <option value="">Todos los estados</option>
            <option value="en_proceso">En proceso</option>
            <option value="aprobado">Aprobado</option>
            <option value="cancelado">Cancelado</option>
        </select>
    </div>

    <% if (casos == null || casos.isEmpty()) { %>
        <div class="prov-empty">
            <div class="prov-empty__icon"><i class="fa-solid fa-inbox"></i></div>
            <p class="prov-empty__texto">Aún no tienes casos postventa registrados.</p>
        </div>
    <% } else { %>
        <div class="prov-grid" id="gridCasos">
            <% for (CasoPostventa c : casos) {
                String est = c.getEstado() != null ? c.getEstado() : "en_proceso";
            %>
            <div class="prov-card caso-card"
                 data-tipo="<%= c.getTipo() %>"
                 data-estado="<%= est %>"
                 data-texto="<%= c.getTipo() %> <%= c.getMotivo() != null ? c.getMotivo().toLowerCase() : "" %> <%= c.getVentaId() %>">

                <div class="prov-card__header">
                    <div class="prov-card__avatar">
                        <% if ("cambio".equals(c.getTipo())) { %>
                            <i class="fa-solid fa-rotate"></i>
                        <% } else if ("devolucion".equals(c.getTipo())) { %>
                            <i class="fa-solid fa-undo"></i>
                        <% } else { %>
                            <i class="fa-solid fa-triangle-exclamation"></i>
                        <% } %>
                    </div>
                    <div class="prov-card__header-info">
                        <div class="prov-card__nombre">Caso #<%= c.getCasoId() %></div>
                        <div style="font-size:12px;color:#6b7280;">Venta #<%= c.getVentaId() %></div>
                    </div>
                    <div class="form-estado">
                        <% if ("aprobado".equals(est)) { %>
                            <span class="estado-badge estado-activo">
                                <span class="estado-badge__dot"></span> Aprobado
                            </span>
                        <% } else if ("cancelado".equals(est)) { %>
                            <span class="estado-badge estado-inactivo">
                                <span class="estado-badge__dot"></span> Cancelado
                            </span>
                        <% } else { %>
                            <span class="estado-badge" style="background:linear-gradient(135deg,#fef9c3,#fde68a);color:#92400e;box-shadow:0 2px 8px rgba(234,179,8,.2);">
                                <span class="estado-badge__dot" style="background:#ca8a04;"></span> En proceso
                            </span>
                        <% } %>
                    </div>
                </div>

                <div class="prov-card__body">
                    <div class="prov-card__fila">
                        <div class="prov-card__etiqueta"><i class="fa-solid fa-tag"></i> Tipo</div>
                        <div class="prov-card__valor prov-card__valor--dato">
                            <% if ("cambio".equals(c.getTipo())) { %>🔄 Cambio de producto
                            <% } else if ("devolucion".equals(c.getTipo())) { %>↩️ Devolución
                            <% } else { %>⚠️ Reclamo<% } %>
                        </div>
                    </div>
                    <div class="prov-card__fila">
                        <div class="prov-card__etiqueta"><i class="fa-solid fa-hashtag"></i> Cantidad</div>
                        <div class="prov-card__valor prov-card__valor--dato"><%= c.getCantidad() %> unidad(es)</div>
                    </div>
                    <% if (c.getMotivo() != null && !c.getMotivo().isBlank()) { %>
                    <div class="prov-card__fila">
                        <div class="prov-card__etiqueta"><i class="fa-solid fa-comment"></i> Motivo</div>
                        <div class="prov-card__valor" style="font-size:13px;color:#4b5563;"><%= c.getMotivo() %></div>
                    </div>
                    <% } %>
                    <% if (c.getObservacion() != null && !c.getObservacion().isBlank()) { %>
                    <div class="prov-card__fila">
                        <div class="prov-card__etiqueta" style="color:#059669;"><i class="fa-solid fa-clipboard-check"></i> Respuesta admin</div>
                        <div class="prov-card__valor" style="font-size:13px;color:#065f46;background:#f0fdf4;padding:8px 10px;border-radius:8px;border-left:3px solid #22c55e;">
                            <%= c.getObservacion() %>
                        </div>
                    </div>
                    <% } %>
                    <div class="prov-card__duo">
                        <div class="prov-card__duo-item">
                            <div class="prov-card__etiqueta"><i class="fa-regular fa-calendar"></i> Fecha</div>
                            <div class="prov-card__valor prov-card__valor--dato">
                                <%= c.getFecha() != null ? sdf.format(c.getFecha()) : "—" %>
                            </div>
                        </div>
                        <div class="prov-card__duo-item">
                            <div class="prov-card__etiqueta"><i class="fa-solid fa-box"></i> Venta</div>
                            <div class="prov-card__valor prov-card__valor--dato">#<%= c.getVentaId() %></div>
                        </div>
                    </div>
                </div>

                <div class="prov-card__footer">
                    <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=verVenta&id=<%= c.getVentaId() %>"
                       class="prov-card__accion prov-card__accion--compras">
                        <i class="fa-solid fa-receipt"></i> Ver venta
                    </a>
                </div>
            </div>
            <% } %>
        </div>
    <% } %>
</main>

<script>
function filtrar() {
    const q      = document.getElementById('buscador').value.toLowerCase();
    const tipo   = document.getElementById('filtroTipo').value;
    const estado = document.getElementById('filtroEstado').value;
    document.querySelectorAll('.caso-card').forEach(card => {
        const ok = card.dataset.texto.includes(q)
                && (!tipo   || card.dataset.tipo   === tipo)
                && (!estado || card.dataset.estado === estado);
        card.style.display = ok ? '' : 'none';
    });
}
</script>
</body>
</html>
