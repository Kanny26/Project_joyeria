<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.text.SimpleDateFormat" %>
<%@ page import="model.CasoPostventa, model.Administrador" %>
<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
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
    
    // Determinamos si el formulario debe estar visible o bloqueado
    boolean resuelto = !"en_proceso".equals(est);
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión Caso #<%= caso.getCasoId() %> | AAC27</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/ventas/casos_postventa.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <style>
        .pv-editable-section { display: <%= resuelto ? "none" : "block" %>; }
        .pv-resolved-view { display: <%= resuelto ? "block" : "none" %>; }
        .char-counter { font-size: 0.8rem; text-align: right; color: #666; margin-top: 5px; }
        .pv-btn-edit { background-color: #f39c12; color: white; border: none; padding: 10px 20px; border-radius: 8px; cursor: pointer; transition: 0.3s; }
        .pv-btn-edit:hover { background-color: #e67e22; }
    </style>
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

    <% if (exito != null) { %>
        <div class="pv-alert pv-alert--ok"><i class="fa-solid fa-circle-check"></i> <%= exito %></div>
    <% } %>
    <% if (error != null) { %>
        <div class="pv-alert pv-alert--err"><i class="fa-solid fa-circle-xmark"></i> <%= error %></div>
    <% } %>

    <div class="pv-card">
        <div class="pv-header-top">
            <div class="pv-header-id">
                <div class="pv-header-icon"><i class="fa-solid fa-rotate-left"></i></div>
                <div>
                    <div class="pv-header-titulo">Caso Postventa #<%= caso.getCasoId() %></div>
                    <div class="pv-header-sub">Gestión de caso registrado</div>
                </div>
            </div>

            <div class="pv-header-badges">
                <span class="pv-badge pv-badge--ok">
                    <i class="fa-regular fa-calendar"></i> <%= caso.getFecha() != null ? sdf.format(caso.getFecha()) : "—" %>
                </span>

                <%-- Badge de Tipo --%>
                <% String t = caso.getTipo(); %>
                <span class="pv-badge <%= "cambio".equals(t)?"pv-badge--blue":"devolucion".equals(t)?"pv-badge--warn":"pv-badge--danger" %>">
                    <i class="fa-solid <%= "cambio".equals(t)?"fa-arrows-rotate":"devolucion".equals(t)?"fa-rotate-left":"fa-triangle-exclamation" %>"></i>
                    <%= t.substring(0,1).toUpperCase() + t.substring(1) %>
                </span>

                <%-- Badge de Estado --%>
                <span id="badge-estado" class="pv-badge <%= "aprobado".equals(est)?"pv-badge--ok":"cancelado".equals(est)?"pv-badge--danger":"pv-badge--warn" %>">
                    <i class="fa-solid <%= "aprobado".equals(est)?"fa-circle-check":"cancelado".equals(est)?"fa-ban":"fa-clock" %>"></i>
                    <%= est.replace("_", " ") %>
                </span>
            </div>
        </div>

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
            <div class="pv-stat pv-stat--full">
                <div class="pv-stat__lbl"><i class="fa-solid fa-comment-dots"></i> Motivo original</div>
                <div class="pv-stat__val"><%= (caso.getMotivo() != null && !caso.getMotivo().isBlank()) ? caso.getMotivo() : "Sin motivo registrado" %></div>
            </div>
        </div>
    </div>

    <%-- ══ SECCIÓN DE GESTIÓN ══ --%>
    <div class="pv-card">
        <div class="pv-section">
            <i class="fa-solid fa-gear"></i> Gestión y Decisión Final
        </div>

        <%-- Vista de resultado cuando ya está resuelto --%>
        <div id="view-resolved" class="pv-resolved-view">
            <div class="pv-group">
                <label class="pv-label">Observaciones registradas:</label>
                <div class="pv-stat__val" style="background: #f9f9f9; padding: 15px; border-radius: 8px; border: 1px solid #eee;">
                    <%= (caso.getObservacion() != null) ? caso.getObservacion() : "Sin observaciones." %>
                </div>
            </div>
            <div class="pv-actions">
                <a href="<%= request.getContextPath() %>/Administrador/postventa/listar" class="pv-btn-back">
                    <i class="fa-solid fa-arrow-left"></i> Volver al listado
                </a>
                <button type="button" class="pv-btn-edit" onclick="habilitarEdicion()">
                    <i class="fa-solid fa-pen-to-square"></i> Editar decisión
                </button>
            </div>
        </div>

        <%-- Formulario de Edición/Gestión --%>
        <div id="form-gestion" class="pv-editable-section">
            <% if ("devolucion".equals(caso.getTipo())) { %>
            <div class="pv-aviso">
                <i class="fa-solid fa-triangle-exclamation"></i>
                <span><strong>Aviso de Stock:</strong> Si aprueba, el stock retornará al inventario.</span>
            </div>
            <% } %>

            <form method="post" action="<%= request.getContextPath() %>/Administrador/postventa/gestionar">
                <input type="hidden" name="casoId" value="<%= caso.getCasoId() %>">
                <input type="hidden" name="esEdicion" value="<%= resuelto %>">

                <div class="pv-group">
                    <label class="pv-label">Seleccionar estado</label>
                    <select name="nuevoEstado" class="pv-input" required>
                        <option value="aprobado" <%= "aprobado".equals(est)?"selected":"" %>>✔ Aprobar</option>
                        <option value="cancelado" <%= "cancelado".equals(est)?"selected":"" %>>✖ Cancelar</option>
                        <option value="en_proceso" <%= "en_proceso".equals(est)?"selected":"" %>>⌛ Mantener en proceso</option>
                    </select>
                </div>

                <div class="pv-group">
                    <label class="pv-label">Observaciones (Máx. 255 caracteres)</label>
                    <textarea name="observacion" id="obsText" rows="3" class="pv-input" 
                              maxlength="255" required placeholder="Explique el motivo de la aprobación o rechazo..."><%= caso.getObservacion() != null ? caso.getObservacion() : "" %></textarea>
                    <div class="char-counter"><span id="charCount">0</span>/255</div>
                </div>

                <div class="pv-actions">
                    <button type="button" class="pv-btn-back" 
					        onclick="window.location.href='<%= request.getContextPath() %>/Administrador/postventa/listar'">
					    Cancelar
					</button>
                    <button type="submit" class="pv-btn-save">
                        <i class="fa-solid fa-floppy-disk"></i> 
                        <%= resuelto ? "Actualizar decisión" : "Guardar decisión" %>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
</main>

<script>
    // Contador de caracteres en tiempo real
    const textarea = document.getElementById('obsText');
    const charCount = document.getElementById('charCount');

    textarea.addEventListener('input', () => {
        const length = textarea.value.length;
        charCount.textContent = length;
        if (length >= 250) charCount.style.color = 'red';
        else charCount.style.color = '#666';
    });

    // Inicializar contador si hay texto previo
    charCount.textContent = textarea.value.length;

    function habilitarEdicion() {
        document.getElementById('view-resolved').style.display = 'none';
        document.getElementById('form-gestion').style.display = 'block';
    }
</script>
</body>
</html>