<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, model.Venta, model.CasoPostventa, java.text.SimpleDateFormat, java.text.NumberFormat" %>
<%
    Object adminSesion = session.getAttribute("admin");
    if (adminSesion == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }
    List<Venta> ventas = (List<Venta>) request.getAttribute("ventas");
    if (ventas == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/ventas/listar");
        return;
    }
    NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    int totalVentas = ventas.size();
    int conSaldoPendiente = 0;
    for (Venta v : ventas) {
        if (v.getSaldoPendiente() != null && v.getSaldoPendiente().compareTo(java.math.BigDecimal.ZERO) > 0)
            conSaldoPendiente++;
    }
    String msg = (String) request.getAttribute("msg");
    if (msg == null) msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ventas | AAC27 Admin</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: var(--fuente-titulos);
            background: var(--color-fondo-admin);
            min-height: 100vh;
            padding-top: 130px;
            padding-bottom: 80px;
        }
        .prov-page { width: 95%; max-width: 1300px; margin: 0 auto; padding: 28px 0; }

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
        .page-header__left h1 { font-size: 1.75rem; font-weight: 800; color: #3d3d3d; }
        .page-header__left p  { font-size: 0.82rem; color: #999; font-weight: 500; margin-top: 2px; }
        .page-header__right   { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; }
        .stat-pills { display: flex; gap: 8px; flex-wrap: wrap; }
        .pill { display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 50px; font-size: 0.78rem; font-weight: 700; white-space: nowrap; }
        .pill--total  { background: #f0eeff; color: #5b21b6; border: 1px solid #ddd6fe; }
        .pill--danger { background: #fdedec; color: #922b21; border: 1px solid #f5b7b1; }

        /* ALERTAS */
        .alerta { display: flex; align-items: center; gap: 10px; padding: 13px 18px; border-radius: 12px; font-size: 0.88rem; font-weight: 600; margin-bottom: 18px; }
        .alerta--success { background: #e8f8f0; color: #1a7a4a; border: 1px solid #a8e6c3; }
        .alerta--error   { background: #fdedec; color: #922b21; border: 1px solid #f5b7b1; }

        /* FILTROS */
        .filtros-bar {
            display: flex; gap: 14px; align-items: center; flex-wrap: wrap;
            background: rgba(255,255,255,0.7); backdrop-filter: blur(8px);
            border-radius: 14px; padding: 14px 20px; margin-bottom: 26px;
            border: 1px solid rgba(197,194,223,0.4);
        }
        .search-wrap { position: relative; flex: 1; min-width: 220px; }
        .search-wrap i { position: absolute; left: 13px; top: 50%; transform: translateY(-50%); color: #9177a8; font-size: 0.83rem; pointer-events: none; }
        .search-wrap input {
            width: 100%; padding: 10px 14px 10px 38px;
            border: 1.5px solid #ddd; border-radius: 10px; font-size: 0.88rem;
            color: #333; background: white; outline: none; font-family: var(--fuente-titulos);
            transition: border-color 0.2s, box-shadow 0.2s;
        }
        .search-wrap input:focus { border-color: #9177a8; box-shadow: 0 0 0 3px rgba(145,119,168,0.12); }
        .filter-btns { display: flex; gap: 8px; flex-wrap: wrap; }
        .filter-btn {
            padding: 8px 16px; border-radius: 50px; font-size: 0.78rem; font-weight: 700;
            font-family: var(--fuente-titulos); border: 1.5px solid rgba(145,119,168,0.25);
            background: rgba(255,255,255,0.85); color: #777; cursor: pointer;
            transition: all 0.2s ease; white-space: nowrap;
        }
        .filter-btn:hover { border-color: #9177a8; color: #9177a8; }
        .filter-btn.active {
            background: linear-gradient(135deg, #9177a8, #c5c2df); color: white;
            border-color: transparent; box-shadow: 0 3px 10px rgba(145,119,168,0.35);
        }

        /* GRID DE CARDS */
        .ventas-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }

        .venta-card {
            background: white; border-radius: 20px; padding: 20px;
            border: 1px solid rgba(197,194,223,0.45);
            box-shadow: 0 3px 14px rgba(145,119,168,0.08);
            display: flex; flex-direction: column; gap: 14px;
            transition: all 0.25s ease; position: relative; overflow: hidden;
        }
        .venta-card::before {
            content: ''; position: absolute; top: 0; left: 0; right: 0; height: 4px;
            background: linear-gradient(90deg, #e3b7c2, #c5c2df, #9177a8);
        }
        .venta-card:hover { transform: translateY(-5px); box-shadow: 0 12px 28px rgba(145,119,168,0.18); border-color: rgba(145,119,168,0.3); }

        .venta-card__head { display: flex; justify-content: space-between; align-items: flex-start; gap: 10px; }
        .venta-card__id   { font-size: 1.05rem; font-weight: 800; color: #5b21b6; }
        .venta-card__fecha { font-size: 0.75rem; color: #9ca3af; font-weight: 600; display: flex; align-items: center; gap: 4px; }

        .badge-row { display: flex; gap: 6px; flex-wrap: wrap; }
        .badge { display: inline-flex; align-items: center; gap: 4px; padding: 3px 10px; border-radius: 50px; font-size: 0.72rem; font-weight: 700; white-space: nowrap; }
        .badge--efectivo   { background: #f0fdf4; color: #16a34a; border: 1px solid #bbf7d0; }
        .badge--tarjeta    { background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; }
        .badge--contado    { background: #f5f3ff; color: #5b21b6; border: 1px solid #ddd6fe; }
        .badge--anticipo   { background: #fef3c7; color: #92400e; border: 1px solid #fde68a; }
        .badge--confirmado { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
        .badge--pendiente  { background: #fef9c3; color: #854d0e; border: 1px solid #fde68a; }
        .badge--rechazado  { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }

        .venta-card__info {
            display: flex; flex-direction: column; gap: 7px;
            background: rgba(197,194,223,0.1); border-radius: 12px; padding: 12px 14px;
            border: 1px solid rgba(197,194,223,0.25);
        }
        .info-row { display: flex; align-items: center; gap: 9px; font-size: 0.82rem; color: #555; }
        .info-row i { color: #9177a8; font-size: 0.78rem; width: 14px; flex-shrink: 0; }
        .info-row .lbl { color: #9ca3af; font-weight: 500; font-size: 0.72rem; min-width: 58px; }
        .info-row .val { font-weight: 700; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

        .saldo-chip {
            display: flex; align-items: center; gap: 6px;
            background: #fee2e2; color: #991b1b; border: 1px solid #fecaca;
            border-radius: 10px; padding: 7px 12px; font-size: 0.78rem; font-weight: 700;
        }

        .venta-card__total { display: flex; justify-content: space-between; align-items: center; padding-top: 6px; border-top: 1px solid #f5f0ff; }
        .total-lbl { font-size: 0.72rem; color: #9ca3af; font-weight: 700; text-transform: uppercase; letter-spacing: 0.04em; }
        .total-val { font-size: 1.1rem; font-weight: 800; color: #059669; }

        /* POSTVENTA BAR */
        .postventa-bar {
            display: flex; justify-content: space-between; align-items: center;
            background: #fff7ed; border: 1px solid #fed7aa;
            border-radius: 10px; padding: 7px 12px; gap: 8px;
        }
        .pv-badge {
            display: inline-flex; align-items: center; gap: 5px;
            font-size: 0.75rem; font-weight: 700;
        }
        .pv-badge--warn { color: #92400e; }
        .pv-badge--ok   { color: #166534; }
        .btn-postventa {
            display: inline-flex; align-items: center; justify-content: center;
            width: 28px; height: 28px; border-radius: 8px;
            background: #fb923c; color: white; font-size: 0.75rem;
            text-decoration: none; transition: all 0.2s; flex-shrink: 0;
        }
        .btn-postventa:hover { background: #ea580c; transform: translateX(2px); }

        .venta-card__footer { display: flex; justify-content: flex-end; gap: 8px; flex-wrap: wrap; }
        .btn-ver {
            display: inline-flex; align-items: center; gap: 5px; padding: 8px 18px;
            border-radius: 10px; background-color: #ab79cf; color: white;
            font-size: 0.82rem; font-weight: 700; font-family: var(--fuente-titulos);
            text-decoration: none; transition: all 0.2s ease;
            box-shadow: 0 2px 8px rgba(145,119,168,0.3);
        }
        .btn-ver:hover { transform: translateY(-2px); box-shadow: 0 5px 14px rgba(145,119,168,0.45); background: linear-gradient(135deg, #7d63a0, #b0acd6); }
        .btn-ver--postventa {
            background: linear-gradient(135deg, #fb923c, #f97316) !important;
            box-shadow: 0 2px 8px rgba(249,115,22,0.35) !important;
        }
        .btn-ver--postventa:hover {
            background: linear-gradient(135deg, #ea580c, #fb923c) !important;
            box-shadow: 0 5px 14px rgba(249,115,22,0.45) !important;
        }

        /* VACÍO */
        .empty-state { text-align: center; padding: 80px 20px; background: rgba(255,255,255,0.65); border-radius: 20px; border: 1px solid rgba(197,194,223,0.4); display: flex; flex-direction: column; align-items: center; gap: 10px; }
        .empty-state__icon { font-size: 3.5rem; color: #c5c2df; margin-bottom: 8px; }
        .empty-state h3 { font-size: 1.2rem; font-weight: 700; color: #555; }
        .empty-state p  { font-size: 0.9rem; color: #aaa; }
        .sin-resultados { display: flex; align-items: center; justify-content: center; gap: 10px; padding: 50px 20px; color: #bbb; font-size: 0.95rem; font-weight: 600; }

        /* RESPONSIVE */
        @media (max-width: 768px) {
            body { padding-top: 100px; }
            .page-header { flex-direction: column; align-items: flex-start; }
            .page-header__right { width: 100%; justify-content: space-between; }
            .filtros-bar { flex-direction: column; }
            .filter-btns { justify-content: flex-start; }
            .ventas-grid { grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 14px; }
        }
        @media (max-width: 480px) { .ventas-grid { grid-template-columns: 1fr; } }
    </style>
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/Administrador/admin-principal.jsp" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<main class="prov-page">

    <div class="page-header">
        <div class="page-header__left">
            <div class="page-header__icon"><i class="fa-solid fa-receipt"></i></div>
            <div>
                <h1>Todas las Ventas</h1>
                <p><%= totalVentas %> venta<%= totalVentas != 1 ? "s" : "" %> registrada<%= totalVentas != 1 ? "s" : "" %></p>
            </div>
        </div>
        <div class="page-header__right">
            <div class="stat-pills">
                <span class="pill pill--total"><i class="fa-solid fa-file-invoice-dollar"></i> Total ventas: <%= totalVentas %></span>
                <% if (conSaldoPendiente > 0) { %>
                <span class="pill pill--danger"><i class="fa-solid fa-clock"></i> Con saldo pendiente: <%= conSaldoPendiente %></span>
                
                <% } %>
            </div>
        </div>
    </div>

    <% if ("creada".equals(msg)) { %>
        <div class="alerta alerta--success"><i class="fa-solid fa-circle-check"></i> Venta registrada exitosamente.</div>
    <% } else if ("error".equals(msg)) { %>
        <div class="alerta alerta--error"><i class="fa-solid fa-circle-xmark"></i> Ocurrió un error al procesar la venta.</div>
    <% } %>

    <div class="filtros-bar">
        <div class="search-wrap">
            <i class="fa-solid fa-magnifying-glass"></i>
            <input type="text" id="buscador" placeholder="Buscar por cliente, vendedor, ID...">
        </div>
        <div class="filter-btns">
            <button class="filter-btn active" data-filtro="todos">Todos</button>
            <button class="filter-btn" data-filtro="confirmado">Pagadas</button>
            <button class="filter-btn" data-filtro="pendiente">Pendientes</button>
            <button class="filter-btn" data-filtro="efectivo">Efectivo</button>
            <button class="filter-btn" data-filtro="tarjeta">Tarjeta</button>
            <button class="filter-btn" data-filtro="anticipo">Anticipo</button>
            <a href="<%= request.getContextPath() %>/Administrador/postventa/listar" >
		            <span class="filter-btn">Casos postventa</span>
    		</a>
        </div>
    </div>

    <% if (ventas.isEmpty()) { %>
        <div class="empty-state">
            <div class="empty-state__icon"><i class="fa-solid fa-receipt"></i></div>
            <h3>Sin ventas registradas</h3>
            <p>Aún no hay ventas en el sistema.</p>
        </div>
    <% } else { %>

    <div class="ventas-grid" id="ventasGrid">
    <%
        for (Venta v : ventas) {
            String estado    = v.getEstado()     != null ? v.getEstado()                  : "pendiente";
            String metodo    = v.getMetodoPago() != null ? v.getMetodoPago().toLowerCase() : "";
            String modalidad = v.getModalidad()  != null ? v.getModalidad().toLowerCase()  : "contado";
            String cliente   = v.getClienteNombre()  != null ? v.getClienteNombre()  : "—";
            String vendedor  = v.getVendedorNombre() != null ? v.getVendedorNombre() : "—";
            String estadoBadge = "confirmado".equals(estado) ? "badge--confirmado" : "rechazado".equals(estado) ? "badge--rechazado" : "badge--pendiente";
            String estadoIcon  = "confirmado".equals(estado) ? "fa-circle-check"   : "rechazado".equals(estado) ? "fa-ban"           : "fa-clock";
            String estadoLabel = "confirmado".equals(estado) ? "Pagado"            : "rechazado".equals(estado) ? "Rechazado"        : "Pendiente";
            boolean tieneSaldo = v.getSaldoPendiente() != null && v.getSaldoPendiente().compareTo(java.math.BigDecimal.ZERO) > 0;

            // Datos postventa
            List<CasoPostventa> casosV = v.getCasosPostventa();
            int totalCasosV = casosV != null ? casosV.size() : 0;
            int enProcesoV  = 0;
            if (casosV != null) {
                for (CasoPostventa cv : casosV) {
                    if ("en_proceso".equals(cv.getEstado())) enProcesoV++;
                }
            }
            // ID del caso más reciente (índice 0 porque la lista viene ordenada DESC)
            int primerCasoId = (casosV != null && !casosV.isEmpty()) ? casosV.get(0).getCasoId() : -1;
            String tienePostventa = totalCasosV > 0 ? "si" : "no";
    %>
        <div class="venta-card"
             data-estado="<%= estado %>" data-metodo="<%= metodo %>"
             data-modalidad="<%= modalidad %>" data-cliente="<%= cliente.toLowerCase() %>"
             data-vendedor="<%= vendedor.toLowerCase() %>" data-id="<%= v.getVentaId() %>"
             data-postventa="<%= tienePostventa %>">

            <div class="venta-card__head">
                <span class="venta-card__id">Venta #<%= v.getVentaId() %></span>
                <span class="venta-card__fecha">
                    <i class="fa-regular fa-calendar"></i>
                    <%= v.getFechaEmision() != null ? sdf.format(v.getFechaEmision()) : "—" %>
                </span>
            </div>

            <div class="badge-row">
                <span class="badge <%= "tarjeta".equals(metodo) ? "badge--tarjeta" : "badge--efectivo" %>">
                    <%= "tarjeta".equals(metodo) ? "💳 Tarjeta" : "💵 Efectivo" %>
                </span>
                <span class="badge <%= "anticipo".equals(modalidad) ? "badge--anticipo" : "badge--contado" %>">
                    <i class="fa-solid <%= "anticipo".equals(modalidad) ? "fa-hourglass-half" : "fa-money-bill-wave" %>"></i>
                    <%= modalidad.substring(0,1).toUpperCase() + modalidad.substring(1) %>
                </span>
                <span class="badge <%= estadoBadge %>">
                    <i class="fa-solid <%= estadoIcon %>"></i> <%= estadoLabel %>
                </span>
            </div>

            <div class="venta-card__info">
                <div class="info-row">
                    <i class="fa-solid fa-user"></i>
                    <span class="lbl">Cliente</span>
                    <span class="val"><%= cliente %></span>
                </div>
                <div class="info-row">
                    <i class="fa-solid fa-user-tie"></i>
                    <span class="lbl">Vendedor</span>
                    <span class="val"><%= vendedor %></span>
                </div>
            </div>

            <% if (tieneSaldo) { %>
            <div class="saldo-chip">
                <i class="fa-solid fa-triangle-exclamation"></i>
                Saldo pendiente: <%= moneda.format(v.getSaldoPendiente()) %>
            </div>
            <% } %>

            <div class="venta-card__total">
                <span class="total-lbl">Total</span>
                <span class="total-val"><%= moneda.format(v.getTotal() != null ? v.getTotal() : 0) %></span>
            </div>

            <%-- BARRA POSTVENTA: solo aparece si la venta tiene casos --%>
            <% if (totalCasosV > 0) { %>
            <div class="postventa-bar">
                <span class="pv-badge <%= enProcesoV > 0 ? "pv-badge--warn" : "pv-badge--ok" %>">
                    <i class="fa-solid fa-rotate-left"></i>
                    <%= totalCasosV %> caso<%= totalCasosV != 1 ? "s" : "" %> postventa
                    <% if (enProcesoV > 0) { %>&nbsp;· <strong><%= enProcesoV %> en proceso</strong><% } %>
                </span>
                <a href="<%= request.getContextPath() %>/Administrador/postventa/ver?id=<%= primerCasoId %>"
                   class="btn-postventa" title="Ver caso postventa">
                    <i class="fa-solid fa-arrow-right"></i>
                </a>
            </div>
            <% } %>

            <div class="venta-card__footer">
                <a href="<%= request.getContextPath() %>/Administrador/ventas/ver?id=<%= v.getVentaId() %>"
                   class="btn-ver">
                    <i class="fa-solid fa-eye"></i> Ver detalle
                </a>
                <% if (totalCasosV > 0) { %>
                
                <% } %>
            </div>
        </div>
    <% } %>
    </div>

    <p class="sin-resultados" id="sinResultados" style="display:none;">
        <i class="fa-solid fa-magnifying-glass"></i> No se encontraron ventas con ese criterio.
    </p>

    <% } %>
</main>

<script>
(function () {
    var buscador = document.getElementById('buscador');
    var grid     = document.getElementById('ventasGrid');
    var sinRes   = document.getElementById('sinResultados');
    var filtro   = 'todos';

    function aplicar() {
        if (!grid) return;
        var q = buscador ? buscador.value.toLowerCase().trim() : '';
        var n = 0;
        grid.querySelectorAll('.venta-card').forEach(function (c) {
            var okQ = !q || c.dataset.cliente.includes(q) || c.dataset.vendedor.includes(q) || String(c.dataset.id).includes(q);
            var okF = filtro === 'todos'
                || (filtro === 'confirmado'    && c.dataset.estado    === 'confirmado')
                || (filtro === 'pendiente'     && c.dataset.estado    === 'pendiente')
                || (filtro === 'efectivo'      && c.dataset.metodo    === 'efectivo')
                || (filtro === 'tarjeta'       && c.dataset.metodo    === 'tarjeta')
                || (filtro === 'anticipo'      && c.dataset.modalidad === 'anticipo')
                || (filtro === 'con_postventa' && c.dataset.postventa === 'si');
            c.style.display = (okQ && okF) ? '' : 'none';
            if (okQ && okF) n++;
        });
        if (sinRes) sinRes.style.display = n === 0 ? 'flex' : 'none';
    }

    if (buscador) buscador.addEventListener('input', aplicar);
    document.querySelectorAll('.filter-btn').forEach(function (b) {
        b.addEventListener('click', function () {
            document.querySelectorAll('.filter-btn').forEach(function (x) { x.classList.remove('active'); });
            b.classList.add('active');
            filtro = b.dataset.filtro;
            aplicar();
        });
    });
}());
</script>
</body>
</html>