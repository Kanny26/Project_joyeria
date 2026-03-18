<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.CasoPostventa, java.text.SimpleDateFormat" %>
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

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    /*
     * El parámetro "exito=1" llega en la URL cuando el servlet redirige
     * tras actualizar el estado correctamente (sendRedirect con ?exito=1).
     */
    String exito = request.getParameter("exito");
    String error = (String) request.getAttribute("error");

    String est      = caso.getEstado() != null ? caso.getEstado() : "en_proceso";
    String tipo     = caso.getTipo()   != null ? caso.getTipo()   : "";
    String cliente  = caso.getClienteNombre()  != null ? caso.getClienteNombre()  : "—";
    String vendedor = caso.getVendedorNombre()  != null ? caso.getVendedorNombre() : "—";
    String producto = caso.getProductoNombre()  != null ? caso.getProductoNombre() : "—";
    String obs      = caso.getObservacion()     != null ? caso.getObservacion()    : "";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Caso #<%= caso.getCasoId() %> | AAC27 Admin</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: var(--fuente-titulos);
            background: var(--color-fondo-admin);
            min-height: 100vh;
            padding-top: 130px;
            padding-bottom: 80px;
        }
        .prov-page { width: 95%; max-width: 860px; margin: 0 auto; padding: 28px 0; }

        /* ENCABEZADO */
        .page-header {
            display: flex; justify-content: space-between; align-items: center;
            flex-wrap: wrap; gap: 16px;
            background: rgba(255,255,255,0.68); backdrop-filter: blur(12px);
            border-radius: 20px; padding: 22px 28px; margin-bottom: 20px;
            border: 1px solid rgba(197,194,223,0.45);
            box-shadow: 0 4px 22px rgba(145,119,168,0.1);
            position: relative; overflow: hidden;
        }
        .page-header::before {
            content: ''; position: absolute; top: 0; left: 0; right: 0; height: 4px;
            background: linear-gradient(90deg, #e3b7c2, #c5c2df, #9177a8);
        }
        .page-header__left { display: flex; align-items: center; gap: 16px; }
        .page-header__icon {
            width: 54px; height: 54px; border-radius: 14px; flex-shrink: 0;
            background: linear-gradient(135deg, rgba(227,183,194,0.35), rgba(197,194,223,0.35));
            border: 1px solid rgba(197,194,223,0.5);
            display: flex; align-items: center; justify-content: center;
            font-size: 1.5rem; color: #9177a8;
        }
        .page-header__left h1 { font-size: 1.6rem; font-weight: 800; color: #3d3d3d; }
        .page-header__left p  { font-size: 0.82rem; color: #999; font-weight: 500; margin-top: 2px; }

        /* ALERTAS */
        .alerta { display: flex; align-items: center; gap: 10px; padding: 13px 18px; border-radius: 12px; font-size: 0.88rem; font-weight: 600; margin-bottom: 18px; }
        .alerta--success { background: #e8f8f0; color: #1a7a4a; border: 1px solid #a8e6c3; }
        .alerta--error   { background: #fdedec; color: #922b21; border: 1px solid #f5b7b1; }

        /* CARD DETALLE */
        .detalle-card {
            background: white; border-radius: 20px; padding: 28px;
            border: 1px solid rgba(197,194,223,0.45);
            box-shadow: 0 3px 14px rgba(145,119,168,0.08);
            margin-bottom: 20px; position: relative; overflow: hidden;
        }
        .detalle-card::before {
            content: ''; position: absolute; top: 0; left: 0; right: 0; height: 4px;
            background: linear-gradient(90deg, #e3b7c2, #c5c2df, #9177a8);
        }
        .section-title {
            font-size: 0.78rem; font-weight: 700; color: #9177a8;
            text-transform: uppercase; letter-spacing: 0.06em;
            margin-bottom: 14px; display: flex; align-items: center; gap: 7px;
        }
        .info-grid {
            display: grid; grid-template-columns: 1fr 1fr; gap: 14px;
            margin-bottom: 20px;
        }
        .info-item {
            background: rgba(197,194,223,0.1); border-radius: 12px; padding: 14px;
            border: 1px solid rgba(197,194,223,0.25);
        }
        .info-item__lbl { font-size: 0.72rem; color: #9ca3af; font-weight: 700; text-transform: uppercase; margin-bottom: 5px; }
        .info-item__val { font-size: 0.95rem; font-weight: 700; color: #333; }

        /* BADGES */
        .badge { display: inline-flex; align-items: center; gap: 4px; padding: 4px 12px; border-radius: 50px; font-size: 0.78rem; font-weight: 700; }
        .badge--en-proceso { background: #fef3c7; color: #92400e; border: 1px solid #fde68a; }
        .badge--aprobado   { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
        .badge--cancelado  { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
        .badge--cambio     { background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; }
        .badge--devolucion { background: #f5f3ff; color: #5b21b6; border: 1px solid #ddd6fe; }
        .badge--reclamo    { background: #fff7ed; color: #c2410c; border: 1px solid #fed7aa; }

        /* MOTIVO */
        .motivo-box {
            background: rgba(197,194,223,0.1); border-radius: 12px; padding: 14px 16px;
            border: 1px solid rgba(197,194,223,0.25); font-size: 0.88rem;
            color: #555; line-height: 1.6; margin-bottom: 20px;
        }

        /* OBSERVACION ANTERIOR */
        .obs-box {
            background: #f0fdf4; border-radius: 12px; padding: 14px 16px;
            border-left: 4px solid #22c55e; font-size: 0.88rem;
            color: #166534; line-height: 1.6; margin-bottom: 20px;
        }
        .obs-box__lbl { font-size: 0.72rem; font-weight: 700; color: #15803d; text-transform: uppercase; margin-bottom: 4px; }

        /* FORMULARIO DE GESTIÓN */
        .gestion-card {
            background: white; border-radius: 20px; padding: 28px;
            border: 1px solid rgba(197,194,223,0.45);
            box-shadow: 0 3px 14px rgba(145,119,168,0.08);
            position: relative; overflow: hidden;
        }
        .gestion-card::before {
            content: ''; position: absolute; top: 0; left: 0; right: 0; height: 4px;
            background: linear-gradient(90deg, #9177a8, #c5c2df, #e3b7c2);
        }
        .form-group { margin-bottom: 16px; }
        .form-group label {
            display: block; font-size: 0.82rem; font-weight: 700; color: #555;
            margin-bottom: 7px;
        }
        .form-group select,
        .form-group textarea {
            width: 100%; padding: 10px 14px;
            border: 1.5px solid #ddd; border-radius: 10px;
            font-size: 0.88rem; color: #333; background: white;
            font-family: var(--fuente-titulos); outline: none;
            transition: border-color 0.2s, box-shadow 0.2s;
        }
        .form-group select:focus,
        .form-group textarea:focus {
            border-color: #9177a8;
            box-shadow: 0 0 0 3px rgba(145,119,168,0.12);
        }
        .form-group textarea { resize: vertical; min-height: 90px; }

        /* ACCIONES */
        .form-actions { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; margin-top: 20px; }
        .btn-back {
            display: inline-flex; align-items: center; gap: 6px;
            padding: 10px 20px; border-radius: 10px;
            background: rgba(197,194,223,0.25); color: #555;
            font-size: 0.85rem; font-weight: 700;
            font-family: var(--fuente-titulos); text-decoration: none;
            border: 1.5px solid rgba(197,194,223,0.4);
            transition: all 0.2s;
        }
        .btn-back:hover { background: rgba(197,194,223,0.4); color: #333; }
        .btn-save {
            display: inline-flex; align-items: center; gap: 6px;
            padding: 10px 24px; border-radius: 10px;
            background: linear-gradient(135deg, #9177a8, #b0acd6); color: white;
            font-size: 0.85rem; font-weight: 700; font-family: var(--fuente-titulos);
            border: none; cursor: pointer;
            box-shadow: 0 3px 10px rgba(145,119,168,0.35);
            transition: all 0.2s;
        }
        .btn-save:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(145,119,168,0.45); }

        /* Estado no editable */
        .estado-final {
            display: flex; align-items: center; gap: 8px;
            padding: 10px 16px; border-radius: 10px;
            background: rgba(197,194,223,0.15);
            border: 1px solid rgba(197,194,223,0.3);
            font-size: 0.85rem; color: #777; font-weight: 600;
        }

        @media (max-width: 600px) {
            body { padding-top: 100px; }
            .info-grid { grid-template-columns: 1fr; }
            .form-actions { flex-direction: column; align-items: stretch; }
            .btn-back, .btn-save { text-align: center; justify-content: center; }
        }
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
            <span class="navbar-admin__home-text">Volver a casos</span>
        </span>
    </a>
</nav>

<main class="prov-page">

    <div class="page-header">
        <div class="page-header__left">
            <div class="page-header__icon"><i class="fa-solid fa-clipboard-check"></i></div>
            <div>
                <h1>Caso #<%= caso.getCasoId() %></h1>
                <p>Venta #<%= caso.getVentaId() %> &mdash; <%= caso.getFecha() != null ? sdf.format(caso.getFecha()) : "—" %></p>
            </div>
        </div>
    </div>

    <%-- Mensajes de retroalimentación --%>
    <% if ("1".equals(exito)) { %>
        <div class="alerta alerta--success" id="alertaMsg">
            <i class="fa-solid fa-circle-check"></i> Estado actualizado correctamente.
        </div>
    <% } %>
    <% if (error != null && !error.isEmpty()) { %>
        <div class="alerta alerta--error" id="alertaMsg">
            <i class="fa-solid fa-circle-xmark"></i> <%= error %>
        </div>
    <% } %>

    <%-- INFORMACIÓN DEL CASO --%>
    <div class="detalle-card">
        <div class="section-title"><i class="fa-solid fa-circle-info"></i> Información del caso</div>

        <div style="display:flex; gap:8px; flex-wrap:wrap; margin-bottom:18px;">
            <%
                String tipoBadge = "cambio".equals(tipo)     ? "badge--cambio"
                                 : "devolucion".equals(tipo) ? "badge--devolucion"
                                 : "badge--reclamo";
                String tipoIcon  = "cambio".equals(tipo)     ? "fa-rotate"
                                 : "devolucion".equals(tipo) ? "fa-undo"
                                 : "fa-triangle-exclamation";
                String tipoLabel = "cambio".equals(tipo)     ? "Cambio de producto"
                                 : "devolucion".equals(tipo) ? "Devolución"
                                 : "Reclamo";
                String estBadge  = "aprobado".equals(est)   ? "badge--aprobado"
                                 : "cancelado".equals(est)  ? "badge--cancelado"
                                 : "badge--en-proceso";
                String estIcon   = "aprobado".equals(est)   ? "fa-circle-check"
                                 : "cancelado".equals(est)  ? "fa-ban"
                                 : "fa-clock";
                String estLabel  = "aprobado".equals(est)   ? "Aprobado"
                                 : "cancelado".equals(est)  ? "Cancelado"
                                 : "En proceso";
            %>
            <span class="badge <%= tipoBadge %>">
                <i class="fa-solid <%= tipoIcon %>"></i> <%= tipoLabel %>
            </span>
            <span class="badge <%= estBadge %>">
                <i class="fa-solid <%= estIcon %>"></i> <%= estLabel %>
            </span>
        </div>

        <div class="info-grid">
            <div class="info-item">
                <div class="info-item__lbl">Cliente</div>
                <div class="info-item__val"><%= cliente %></div>
            </div>
            <div class="info-item">
                <div class="info-item__lbl">Vendedor</div>
                <div class="info-item__val"><%= vendedor %></div>
            </div>
            <div class="info-item">
                <div class="info-item__lbl">Producto</div>
                <div class="info-item__val"><%= producto %></div>
            </div>
            <div class="info-item">
                <div class="info-item__lbl">Cantidad afectada</div>
                <div class="info-item__val"><%= caso.getCantidad() %> unidad(es)</div>
            </div>
        </div>

        <% if (caso.getMotivo() != null && !caso.getMotivo().isBlank()) { %>
        <div class="section-title"><i class="fa-solid fa-comment"></i> Motivo del vendedor</div>
        <div class="motivo-box"><%= caso.getMotivo() %></div>
        <% } %>

        <%-- Observación previa del admin (si ya fue gestionado antes) --%>
        <% if (!obs.isBlank()) { %>
        <div class="section-title"><i class="fa-solid fa-comment-dots"></i> Última observación registrada</div>
        <div class="obs-box">
            <div class="obs-box__lbl">Respuesta del administrador</div>
            <%= obs %>
        </div>
        <% } %>
    </div>

    <%-- GESTIÓN: solo se muestra si el caso aún no está resuelto --%>
    <% if ("en_proceso".equals(est)) { %>
    <div class="gestion-card">
        <div class="section-title"><i class="fa-solid fa-sliders"></i> Gestionar caso</div>
        <p style="font-size:0.85rem; color:#777; margin-bottom:18px;">
            Selecciona la resolución y escribe una observación para el vendedor.
            Si es una devolución aprobada, el stock se reintegrará automáticamente.
        </p>

        <form id="formGestion"
              action="<%= request.getContextPath() %>/Administrador/postventa/estado"
              method="POST">
            <input type="hidden" name="casoId"      value="<%= caso.getCasoId() %>">
            <input type="hidden" name="nuevoEstado" id="nuevoEstadoInput" value="">

            <div class="form-group">
                <label><i class="fa-solid fa-circle-dot"></i> Nueva resolución *</label>
                <select id="selectEstado" required>
                    <option value="">-- Selecciona una resolución --</option>
                    <option value="aprobado">Aprobar el caso</option>
                    <option value="cancelado">Cancelar el caso (no procede)</option>
                </select>
            </div>

            <div class="form-group">
                <label><i class="fa-solid fa-pen-to-square"></i> Observación para el vendedor</label>
                <textarea name="observacion"
                          placeholder="Explica la decisión tomada, instrucciones a seguir, etc."></textarea>
            </div>

            <div class="form-actions">
                <a href="<%= request.getContextPath() %>/Administrador/postventa/listar"
                   class="btn-back">
                    <i class="fa-solid fa-arrow-left"></i> Volver
                </a>
                <button type="button" class="btn-save" onclick="confirmarGestion()">
                    <i class="fa-solid fa-floppy-disk"></i> Guardar resolución
                </button>
            </div>
        </form>
    </div>
    <% } else { %>
    <%-- Caso ya resuelto: solo muestra botón de volver --%>
    <div class="gestion-card">
        <div class="section-title"><i class="fa-solid fa-lock"></i> Caso cerrado</div>
        <div class="estado-final">
            <i class="fa-solid <%= "aprobado".equals(est) ? "fa-circle-check" : "fa-ban" %>"
               style="color:<%= "aprobado".equals(est) ? "#16a34a" : "#dc2626" %>;"></i>
            Este caso ya fue <%= "aprobado".equals(est) ? "aprobado" : "cancelado" %> y no puede modificarse.
        </div>
        <div class="form-actions" style="margin-top:16px;">
            <a href="<%= request.getContextPath() %>/Administrador/postventa/listar"
               class="btn-back">
                <i class="fa-solid fa-arrow-left"></i> Volver a casos
            </a>
            <a href="<%= request.getContextPath() %>/Administrador/ventas/ver?id=<%= caso.getVentaId() %>"
               class="btn-save" style="text-decoration:none;">
                <i class="fa-solid fa-receipt"></i> Ver venta relacionada
            </a>
        </div>
    </div>
    <% } %>

</main>

<script>
// Oculta la alerta de éxito después de 5 segundos
(function() {
    var alerta = document.getElementById('alertaMsg');
    if (alerta) {
        setTimeout(function() {
            alerta.style.transition = 'opacity 0.5s';
            alerta.style.opacity = '0';
            setTimeout(function() { alerta.style.display = 'none'; }, 500);
        }, 5000);
    }
})();

function confirmarGestion() {
    var estado = document.getElementById('selectEstado').value;
    if (!estado) {
        Swal.fire({
            icon: 'warning',
            title: 'Selecciona una resolución',
            text: 'Debes elegir si el caso se aprueba o se cancela.',
            confirmButtonColor: '#9177a8'
        });
        return;
    }

    var etiqueta = estado === 'aprobado' ? 'Aprobar' : 'Cancelar';
    var icono    = estado === 'aprobado' ? 'success' : 'warning';
    var extra    = estado === 'aprobado' && '<%= tipo %>'.includes('devolucion')
        ? '<br><small style="color:#6b7280;">El stock del producto será reintegrado automáticamente.</small>'
        : '';

    Swal.fire({
        title: etiqueta + ' este caso',
        html: '\u00bfEstás seguro de que deseas <strong>' + etiqueta.toLowerCase()
              + '</strong> el Caso #<%= caso.getCasoId() %>?' + extra,
        icon: icono,
        showCancelButton: true,
        confirmButtonText: 'Sí, ' + etiqueta.toLowerCase(),
        cancelButtonText: 'Revisar antes',
        confirmButtonColor: estado === 'aprobado' ? '#059669' : '#dc2626',
        cancelButtonColor: '#6b7280'
    }).then(function(result) {
        if (result.isConfirmed) {
            document.getElementById('nuevoEstadoInput').value = estado;
            Swal.fire({
                title: 'Guardando...',
                allowOutsideClick: false,
                didOpen: function() { Swal.showLoading(); }
            });
            document.getElementById('formGestion').submit();
        }
    });
}
</script>
</body>
</html>
