<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, model.CasoPostventa, java.text.SimpleDateFormat" %>
<%
    Object adminSesion = session.getAttribute("admin");
    if (adminSesion == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }
    List<CasoPostventa> casos = (List<CasoPostventa>) request.getAttribute("casos");
    if (casos == null) casos = new ArrayList<>();

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    int totalCasos  = casos.size();
    int enProceso   = 0, aprobados = 0, cancelados = 0;
    for (CasoPostventa c : casos) {
        if ("aprobado".equals(c.getEstado()))       aprobados++;
        else if ("cancelado".equals(c.getEstado())) cancelados++;
        else                                         enProceso++;
    }

    String msg = (String) request.getAttribute("msg");
    if (msg == null) msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Casos Postventa | AAC27 Admin</title>
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
        .pill--total   { background: #f0eeff; color: #5b21b6; border: 1px solid #ddd6fe; }
        .pill--warning { background: #fef3c7; color: #92400e; border: 1px solid #fde68a; }
        .pill--success { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
        .pill--danger  { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }

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

        /* GRID */
        .casos-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }

        .caso-card {
            background: white; border-radius: 20px; padding: 20px;
            border: 1px solid rgba(197,194,223,0.45);
            box-shadow: 0 3px 14px rgba(145,119,168,0.08);
            display: flex; flex-direction: column; gap: 14px;
            transition: all 0.25s ease; position: relative; overflow: hidden;
        }
        .caso-card::before {
            content: ''; position: absolute; top: 0; left: 0; right: 0; height: 4px;
            background: linear-gradient(90deg, #e3b7c2, #c5c2df, #9177a8);
        }
        .caso-card:hover { transform: translateY(-5px); box-shadow: 0 12px 28px rgba(145,119,168,0.18); border-color: rgba(145,119,168,0.3); }

        .caso-card__head { display: flex; justify-content: space-between; align-items: flex-start; gap: 10px; }
        .caso-card__id   { font-size: 1.05rem; font-weight: 800; color: #5b21b6; }
        .caso-card__fecha { font-size: 0.75rem; color: #9ca3af; font-weight: 600; display: flex; align-items: center; gap: 4px; }

        .badge { display: inline-flex; align-items: center; gap: 4px; padding: 3px 10px; border-radius: 50px; font-size: 0.72rem; font-weight: 700; white-space: nowrap; }
        .badge--en-proceso { background: #fef3c7; color: #92400e; border: 1px solid #fde68a; }
        .badge--aprobado   { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
        .badge--cancelado  { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
        .badge--cambio     { background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; }
        .badge--devolucion { background: #f5f3ff; color: #5b21b6; border: 1px solid #ddd6fe; }
        .badge--reclamo    { background: #fff7ed; color: #c2410c; border: 1px solid #fed7aa; }

        .caso-card__info {
            display: flex; flex-direction: column; gap: 7px;
            background: rgba(197,194,223,0.1); border-radius: 12px; padding: 12px 14px;
            border: 1px solid rgba(197,194,223,0.25);
        }
        .info-row { display: flex; align-items: center; gap: 9px; font-size: 0.82rem; color: #555; }
        .info-row i { color: #9177a8; font-size: 0.78rem; width: 14px; flex-shrink: 0; }
        .info-row .lbl { color: #9ca3af; font-weight: 500; font-size: 0.72rem; min-width: 68px; }
        .info-row .val { font-weight: 700; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

        .caso-card__footer { display: flex; justify-content: flex-end; }
        .btn-ver {
            display: inline-flex; align-items: center; gap: 5px; padding: 8px 18px;
            border-radius: 10px; background-color: #ab79cf; color: white;
            font-size: 0.82rem; font-weight: 700; font-family: var(--fuente-titulos);
            text-decoration: none; transition: all 0.2s ease;
            box-shadow: 0 2px 8px rgba(145,119,168,0.3);
        }
        .btn-ver:hover { transform: translateY(-2px); box-shadow: 0 5px 14px rgba(145,119,168,0.45); background: linear-gradient(135deg, #7d63a0, #b0acd6); }

        /* VACÍO */
        .empty-state { text-align: center; padding: 80px 20px; background: rgba(255,255,255,0.65); border-radius: 20px; border: 1px solid rgba(197,194,223,0.4); display: flex; flex-direction: column; align-items: center; gap: 10px; }
        .empty-state__icon { font-size: 3.5rem; color: #c5c2df; margin-bottom: 8px; }
        .empty-state h3 { font-size: 1.2rem; font-weight: 700; color: #555; }
        .empty-state p  { font-size: 0.9rem; color: #aaa; }
        .sin-resultados { display: flex; align-items: center; justify-content: center; gap: 10px; padding: 50px 20px; color: #bbb; font-size: 0.95rem; font-weight: 600; }

        @media (max-width: 768px) {
            body { padding-top: 100px; }
            .page-header { flex-direction: column; align-items: flex-start; }
            .filtros-bar { flex-direction: column; }
            .casos-grid { grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 14px; }
        }
        @media (max-width: 480px) { .casos-grid { grid-template-columns: 1fr; } }
    </style>
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/Administrador/ventas/listar" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver a ventas</span>
        </span>
    </a>
</nav>

<main class="prov-page">

    <div class="page-header">
        <div class="page-header__left">
            <div class="page-header__icon"><i class="fa-solid fa-rotate-left"></i></div>
            <div>
                <h1>Casos Postventa</h1>
                <p><%= totalCasos %> caso<%= totalCasos != 1 ? "s" : "" %> registrado<%= totalCasos != 1 ? "s" : "" %></p>
            </div>
        </div>
        <div class="page-header__right">
            <div class="stat-pills">
                <span class="pill pill--total">
                    <i class="fa-solid fa-list"></i> Total: <%= totalCasos %>
                </span>
                <% if (enProceso > 0) { %>
                <span class="pill pill--warning">
                    <i class="fa-solid fa-clock"></i> En proceso: <%= enProceso %>
                </span>
                <% } %>
                <% if (aprobados > 0) { %>
                <span class="pill pill--success">
                    <i class="fa-solid fa-circle-check"></i> Aprobados: <%= aprobados %>
                </span>
                <% } %>
                <% if (cancelados > 0) { %>
                <span class="pill pill--danger">
                    <i class="fa-solid fa-ban"></i> Cancelados: <%= cancelados %>
                </span>
                <% } %>
            </div>
        </div>
    </div>

    <% if ("actualizado".equals(msg)) { %>
        <div class="alerta alerta--success"><i class="fa-solid fa-circle-check"></i> Estado del caso actualizado correctamente.</div>
    <% } else if ("error".equals(msg)) { %>
        <div class="alerta alerta--error"><i class="fa-solid fa-circle-xmark"></i> Ocurrió un error al procesar la solicitud.</div>
    <% } %>

    <div class="filtros-bar">
        <div class="search-wrap">
            <i class="fa-solid fa-magnifying-glass"></i>
            <input type="text" id="buscador" placeholder="Buscar por cliente, vendedor, caso #...">
        </div>
        <div class="filter-btns">
            <button class="filter-btn active" data-filtro="todos">Todos</button>
            <button class="filter-btn" data-filtro="en_proceso">En proceso</button>
            <button class="filter-btn" data-filtro="aprobado">Aprobados</button>
            <button class="filter-btn" data-filtro="cancelado">Cancelados</button>
            <button class="filter-btn" data-filtro="cambio">Cambios</button>
            <button class="filter-btn" data-filtro="devolucion">Devoluciones</button>
            <button class="filter-btn" data-filtro="reclamo">Reclamos</button>
        </div>
    </div>

    <% if (casos.isEmpty()) { %>
        <div class="empty-state">
            <div class="empty-state__icon"><i class="fa-solid fa-rotate-left"></i></div>
            <h3>Sin casos postventa</h3>
            <p>Aún no hay casos registrados en el sistema.</p>
        </div>
    <% } else { %>

    <div class="casos-grid" id="casosGrid">
    <%
        for (CasoPostventa c : casos) {
            String est      = c.getEstado() != null ? c.getEstado() : "en_proceso";
            String tipo     = c.getTipo()   != null ? c.getTipo()   : "";
            String cliente  = c.getClienteNombre()  != null ? c.getClienteNombre()  : "—";
            String vendedor = c.getVendedorNombre()  != null ? c.getVendedorNombre() : "—";
            String producto = c.getProductoNombre()  != null ? c.getProductoNombre() : "—";

            String estadoBadge = "aprobado".equals(est)  ? "badge--aprobado"
                               : "cancelado".equals(est) ? "badge--cancelado"
                               : "badge--en-proceso";
            String estadoIcon  = "aprobado".equals(est)  ? "fa-circle-check"
                               : "cancelado".equals(est) ? "fa-ban"
                               : "fa-clock";
            String estadoLabel = "aprobado".equals(est)  ? "Aprobado"
                               : "cancelado".equals(est) ? "Cancelado"
                               : "En proceso";

            String tipoBadge = "cambio".equals(tipo)     ? "badge--cambio"
                             : "devolucion".equals(tipo) ? "badge--devolucion"
                             : "badge--reclamo";
            String tipoIcon  = "cambio".equals(tipo)     ? "fa-rotate"
                             : "devolucion".equals(tipo) ? "fa-undo"
                             : "fa-triangle-exclamation";
            String tipoLabel = "cambio".equals(tipo)     ? "Cambio"
                             : "devolucion".equals(tipo) ? "Devolución"
                             : "Reclamo";
    %>
        <div class="caso-card"
             data-estado="<%= est %>"
             data-tipo="<%= tipo %>"
             data-cliente="<%= cliente.toLowerCase() %>"
             data-vendedor="<%= vendedor.toLowerCase() %>"
             data-id="<%= c.getCasoId() %>">

            <div class="caso-card__head">
                <span class="caso-card__id">Caso #<%= c.getCasoId() %></span>
                <span class="caso-card__fecha">
                    <i class="fa-regular fa-calendar"></i>
                    <%= c.getFecha() != null ? sdf.format(c.getFecha()) : "—" %>
                </span>
            </div>

            <div style="display:flex; gap:6px; flex-wrap:wrap;">
                <span class="badge <%= tipoBadge %>">
                    <i class="fa-solid <%= tipoIcon %>"></i> <%= tipoLabel %>
                </span>
                <span class="badge <%= estadoBadge %>">
                    <i class="fa-solid <%= estadoIcon %>"></i> <%= estadoLabel %>
                </span>
            </div>

            <div class="caso-card__info">
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
                <div class="info-row">
                    <i class="fa-solid fa-box"></i>
                    <span class="lbl">Producto</span>
                    <span class="val"><%= producto %></span>
                </div>
                <div class="info-row">
                    <i class="fa-solid fa-receipt"></i>
                    <span class="lbl">Venta</span>
                    <span class="val">#<%= c.getVentaId() %></span>
                </div>
                <div class="info-row">
                    <i class="fa-solid fa-hashtag"></i>
                    <span class="lbl">Cantidad</span>
                    <span class="val"><%= c.getCantidad() %> unidad(es)</span>
                </div>
                <% if (c.getMotivo() != null && !c.getMotivo().isBlank()) { %>
                <div class="info-row" style="align-items:flex-start;">
                    <i class="fa-solid fa-comment" style="margin-top:2px;"></i>
                    <span class="lbl">Motivo</span>
                    <span style="font-size:0.78rem; color:#555; font-weight:500; line-height:1.4;">
                        <%= c.getMotivo().length() > 60 ? c.getMotivo().substring(0, 60) + "…" : c.getMotivo() %>
                    </span>
                </div>
                <% } %>
            </div>

            <div class="caso-card__footer">
                <a href="<%= request.getContextPath() %>/Administrador/postventa/ver?id=<%= c.getCasoId() %>"
                   class="btn-ver">
                    <i class="fa-solid fa-eye"></i> Ver y gestionar
                </a>
            </div>
        </div>
    <% } %>
    </div>

    <p class="sin-resultados" id="sinResultados" style="display:none;">
        <i class="fa-solid fa-magnifying-glass"></i> No se encontraron casos con ese criterio.
    </p>

    <% } %>
</main>

<script>
(function () {
    var buscador = document.getElementById('buscador');
    var grid     = document.getElementById('casosGrid');
    var sinRes   = document.getElementById('sinResultados');
    var filtro   = 'todos';

    function aplicar() {
        if (!grid) return;
        var q = buscador ? buscador.value.toLowerCase().trim() : '';
        var n = 0;
        grid.querySelectorAll('.caso-card').forEach(function (c) {
            var okQ = !q
                || c.dataset.cliente.includes(q)
                || c.dataset.vendedor.includes(q)
                || String(c.dataset.id).includes(q);
            var okF = filtro === 'todos'
                || (filtro === 'en_proceso'  && c.dataset.estado === 'en_proceso')
                || (filtro === 'aprobado'    && c.dataset.estado === 'aprobado')
                || (filtro === 'cancelado'   && c.dataset.estado === 'cancelado')
                || (filtro === 'cambio'      && c.dataset.tipo   === 'cambio')
                || (filtro === 'devolucion'  && c.dataset.tipo   === 'devolucion')
                || (filtro === 'reclamo'     && c.dataset.tipo   === 'reclamo');
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
