<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.text.SimpleDateFormat, java.text.NumberFormat, java.util.Locale" %>
<%@ page import="model.CasoPostventa, model.Venta, model.DetalleVenta" %>
<%
    Object adminSesion = session.getAttribute("admin");
    if (adminSesion == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }
    CasoPostventa caso = (CasoPostventa) request.getAttribute("caso");
    if (caso == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/postventa/listar");
        return;
    }

    String exito = (String) request.getAttribute("exito");
    String error  = (String) request.getAttribute("error");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    String est = caso.getEstado() != null ? caso.getEstado() : "en_proceso";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Caso Postventa #<%= caso.getCasoId() %> | Admin AAC27</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/ventas/casos_postventa.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">

</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/Administrador/postventa/listar" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<main class="prov-page">
<div class="pv-wrap">

    <%-- Alertas --%>
    <% if (exito != null) { %>
        <div class="pv-alert pv-alert--ok">
            <i class="fa-solid fa-circle-check"></i> <%= exito %>
        </div>
    <% } %>
    <% if (error != null) { %>
        <div class="pv-alert pv-alert--err">
            <i class="fa-solid fa-circle-xmark"></i> <%= error %>
        </div>
    <% } %>

    <%-- ══ CARD META (encabezado igual a ver_venta) ══ --%>
    <div class="pv-card">

        <div class="pv-header-top">
            <%-- Ícono + título --%>
            <div class="pv-header-id">
                <div class="pv-header-icon">
                    <i class="fa-solid fa-rotate-left"></i>
                </div>
                <div>
                    <div class="pv-header-titulo">Caso Postventa #<%= caso.getCasoId() %></div>
                    <div class="pv-header-sub">Gestión de caso registrado</div>
                </div>
            </div>

            <%-- Badges: fecha, tipo, estado --%>
            <div class="pv-header-badges">
                <span class="pv-badge pv-badge--ok">
                    <i class="fa-regular fa-calendar"></i>
                    <%= caso.getFecha() != null ? sdf.format(caso.getFecha()) : "—" %>
                </span>

                <% if ("cambio".equals(caso.getTipo())) { %>
                    <span class="pv-badge pv-badge--blue">
                        <i class="fa-solid fa-arrows-rotate"></i> Cambio
                    </span>
                <% } else if ("devolucion".equals(caso.getTipo())) { %>
                    <span class="pv-badge pv-badge--warn">
                        <i class="fa-solid fa-rotate-left"></i> Devolución
                    </span>
                <% } else { %>
                    <span class="pv-badge pv-badge--danger">
                        <i class="fa-solid fa-triangle-exclamation"></i> Reclamo
                    </span>
                <% } %>

                <% if ("aprobado".equals(est)) { %>
                    <span class="pv-badge pv-badge--ok">
                        <i class="fa-solid fa-circle-check"></i> Aprobado
                    </span>
                <% } else if ("cancelado".equals(est)) { %>
                    <span class="pv-badge pv-badge--danger">
                        <i class="fa-solid fa-ban"></i> Cancelado
                    </span>
                <% } else { %>
                    <span class="pv-badge pv-badge--warn">
                        <i class="fa-solid fa-clock"></i> En proceso
                    </span>
                <% } %>
            </div>
        </div>

        <%-- Stats grid --%>
        <div class="pv-stats-grid">
            <div class="pv-stat">
                <div class="pv-stat__lbl"><i class="fa-solid fa-hashtag"></i> ID</div>
                <div class="pv-stat__val">#<%= caso.getCasoId() %></div>
            </div>
            <div class="pv-stat">
                <div class="pv-stat__lbl"><i class="fa-solid fa-user-tie"></i> Vendedor</div>
                <div class="pv-stat__val"><%= caso.getVendedorNombre() != null ? caso.getVendedorNombre() : "—" %></div>
            </div>
            <div class="pv-stat">
                <div class="pv-stat__lbl"><i class="fa-solid fa-user"></i> Cliente</div>
                <div class="pv-stat__val"><%= caso.getClienteNombre() != null ? caso.getClienteNombre() : "—" %></div>
            </div>
            <div class="pv-stat">
                <div class="pv-stat__lbl"><i class="fa-solid fa-receipt"></i> Venta asociada</div>
                <div class="pv-stat__val">#<%= caso.getVentaId() %></div>
            </div>
            <div class="pv-stat">
                <div class="pv-stat__lbl"><i class="fa-solid fa-gem"></i> Producto</div>
                <div class="pv-stat__val"><%= caso.getProductoNombre() != null ? caso.getProductoNombre() : "—" %></div>
            </div>
            <div class="pv-stat">
                <div class="pv-stat__lbl"><i class="fa-solid fa-cubes"></i> Cantidad</div>
                <div class="pv-stat__val"><%= caso.getCantidad() %> uds.</div>
            </div>
            <% if (caso.getMotivo() != null && !caso.getMotivo().isBlank()) { %>
            <div class="pv-stat pv-stat--full">
                <div class="pv-stat__lbl"><i class="fa-solid fa-comment-dots"></i> Motivo</div>
                <div class="pv-stat__val"><%= caso.getMotivo() %></div>
            </div>
            <% } %>
        </div>
    </div>

    <%-- ══ CARD GESTIÓN (solo si está en proceso) ══ --%>
    <% if ("en_proceso".equals(est)) { %>
    <div class="pv-card">

        <div class="pv-section">
            <i class="fa-solid fa-gear"></i> Gestionar caso
        </div>

        <% if ("devolucion".equals(caso.getTipo())) { %>
        <div class="pv-aviso">
            <i class="fa-solid fa-triangle-exclamation"></i>
            <span><strong>Atención:</strong> Al aprobar esta devolución, el stock del producto se incrementará automáticamente.</span>
        </div>
        <% } %>

        <form method="post" action="<%= request.getContextPath() %>/Administrador/postventa/gestionar">
            <input type="hidden" name="casoId" value="<%= caso.getCasoId() %>">

            <div class="pv-group">
                <label class="pv-label">Nuevo estado</label>
                <select name="nuevoEstado" class="pv-input" required>
                    <option value="">-- Seleccionar --</option>
                    <option value="aprobado">✔ Aprobar</option>
                    <option value="cancelado">✖ Cancelar</option>
                </select>
            </div>

            <div class="pv-group">
                <label class="pv-label">Observaciones</label>
                <textarea name="observacion" rows="3" class="pv-input"
                          placeholder="Agrega observaciones sobre esta decisión..."></textarea>
            </div>

            <div class="pv-actions">
                <a href="<%= request.getContextPath() %>/Administrador/postventa/listar" class="pv-btn-back">
                    <i class="fa-solid fa-arrow-left"></i> Volver
                </a>
                <button type="submit" class="pv-btn-save">
                    <i class="fa-solid fa-floppy-disk"></i> Guardar decisión
                </button>
            </div>
        </form>

    </div>
    <% } else { %>
    <%-- Solo botón volver si ya está resuelto --%>
    <div class="pv-actions" style="padding-top:0; border-top:none; margin-top:0;">
        <a href="<%= request.getContextPath() %>/Administrador/postventa/listar" class="pv-btn-back">
            <i class="fa-solid fa-arrow-left"></i> Volver al listado
        </a>
    </div>
    <% } %>

</div>
</main>
</body>
</html>
