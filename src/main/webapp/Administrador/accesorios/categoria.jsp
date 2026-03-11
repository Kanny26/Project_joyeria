<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, model.Producto, model.Categoria, model.Administrador, model.Material, model.Subcategoria" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }

    List<Producto>     productos     = (List<Producto>)     request.getAttribute("productos");
    Categoria          categoria     = (Categoria)          request.getAttribute("categoria");
    List<Material>     materiales    = (List<Material>)     request.getAttribute("materiales");
    List<Subcategoria> subcategorias = (List<Subcategoria>) request.getAttribute("subcategorias");

    String termino      = (String) request.getAttribute("terminoBusqueda");
    String filtroActivo = (String) request.getAttribute("filtroActivo");

    if (termino      == null) termino      = "";
    if (filtroActivo == null) filtroActivo = "todos";

    int totalProductos = productos != null ? productos.size() : 0;
    int stockBajo = 0, sinStock = 0;
    if (productos != null) {
        for (Producto p : productos) {
            if (p.getStock() == 0)      sinStock++;
            else if (p.getStock() <= 3) stockBajo++;
        }
    }

    SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= categoria != null ? categoria.getNombre() : "Búsqueda" %> - AAC27</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/gest-productos.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%=request.getContextPath()%>/CategoriaServlet" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<main class="prod-page">

    <%-- ══ ENCABEZADO ══ --%>
    <div class="page-header">
        <div class="page-header__left">
            <div class="page-header__icon"><i class="fa-solid fa-gem"></i></div>
            <div>
                <h1><%= categoria != null ? categoria.getNombre() : "Búsqueda de productos" %></h1>
                <p><%= totalProductos %> producto<%= totalProductos != 1 ? "s" : "" %> <%= termino.isEmpty() ? "en esta categoría" : "encontrados" %></p>
            </div>
        </div>
        <div class="page-header__right">
            <div class="stat-pills">
                <span class="pill pill--warning">
                    <i class="fa-solid fa-triangle-exclamation"></i> <%= stockBajo %> stock bajo
                </span>
                <span class="pill pill--danger">
                    <i class="fa-solid fa-circle-xmark"></i> <%= sinStock %> sin stock
                </span>
            </div>
            <% if (categoria != null) { %>
            <a href="<%=request.getContextPath()%>/ProductoServlet?action=nuevo&categoria=<%= categoria.getCategoriaId() %>"
               class="btn-agregar">
                <i class="fa-solid fa-plus"></i> Agregar Producto
            </a>
            <% } %>
        </div>
    </div>

    <%-- ══ MENSAJES ══ --%>
    <%
        String msg = request.getParameter("msg");
    %>
    <% if ("creado".equals(msg)) { %>
        <div class="alerta alerta--success"><i class="fa-solid fa-circle-check"></i> Producto creado exitosamente.</div>
    <% } else if ("actualizado".equals(msg)) { %>
        <div class="alerta alerta--success"><i class="fa-solid fa-circle-check"></i> Producto actualizado correctamente.</div>
    <% } else if ("eliminado".equals(msg)) { %>
        <div class="alerta alerta--success"><i class="fa-solid fa-circle-check"></i> Producto eliminado correctamente.</div>
    <% } %>

    <%-- ══ BARRA DE BÚSQUEDA Y FILTROS ══ --%>
    <div class="filtros-bar">

        <div class="search-wrap" id="searchWrap">
            <i class="fa-solid fa-magnifying-glass"></i>
            <input type="text" id="buscadorTexto"
                   placeholder="Buscar por nombre, código..."
                   value="<%= ("nombre".equals(filtroActivo) || "todos".equals(filtroActivo)) ? termino : "" %>">
        </div>

        <div class="search-wrap search-wrap--select" id="wrapMaterial" style="display:none;">
            <i class="fa-solid fa-gem"></i>
            <select id="selectMaterial" class="search-select">
                <option value="">— Seleccionar material —</option>
                <% if (materiales != null) { for (Material mat : materiales) { %>
                    <option value="<%= mat.getNombre() %>"
                        <%= mat.getNombre().equalsIgnoreCase(termino) && "material".equals(filtroActivo) ? "selected" : "" %>>
                        <%= mat.getNombre() %>
                    </option>
                <% } } %>
            </select>
        </div>

        <div class="search-wrap search-wrap--select" id="wrapSubcategoria" style="display:none;">
            <i class="fa-solid fa-tags"></i>
            <select id="selectSubcategoria" class="search-select">
                <option value="">— Seleccionar subcategoría —</option>
                <% if (subcategorias != null) { for (Subcategoria sub : subcategorias) { %>
                    <option value="<%= sub.getNombre() %>"
                        <%= sub.getNombre().equalsIgnoreCase(termino) && "subcategoria".equals(filtroActivo) ? "selected" : "" %>>
                        <%= sub.getNombre() %>
                    </option>
                <% } } %>
            </select>
        </div>

        <div class="search-wrap" id="wrapStock" style="display:none;">
            <i class="fa-solid fa-boxes-stacked"></i>
            <input type="number" id="inputStock" min="0"
                   placeholder="Stock máximo (ej: 5)"
                   value="<%= "stock".equals(filtroActivo) ? termino : "" %>">
        </div>

        <%-- Form oculto para enviar búsqueda --%>
        <form id="formBusqueda" action="<%=request.getContextPath()%>/CategoriaServlet" method="get" style="display:none;">
            <% if (categoria != null) { %>
            <input type="hidden" name="id" value="<%= categoria.getCategoriaId() %>">
            <% } %>
            <input type="hidden" name="filtro" id="hiddenFiltro" value="<%= filtroActivo %>">
            <input type="hidden" name="q"      id="hiddenQ"      value="<%= termino %>">
        </form>

        <div class="filter-btns">
            <button class="filter-btn <%= "todos".equals(filtroActivo) ? "active" : "" %>"         data-filtro="todos">Todos</button>
            <button class="filter-btn <%= "nombre".equals(filtroActivo) ? "active" : "" %>"        data-filtro="nombre"><i class="fa-solid fa-font"></i> Nombre</button>
            <button class="filter-btn <%= "material".equals(filtroActivo) ? "active" : "" %>"      data-filtro="material"><i class="fa-solid fa-gem"></i> Material</button>
            <button class="filter-btn <%= "subcategoria".equals(filtroActivo) ? "active" : "" %>"  data-filtro="subcategoria"><i class="fa-solid fa-tags"></i> Subcategoría</button>
            <button class="filter-btn <%= "stock".equals(filtroActivo) ? "active" : "" %>"         data-filtro="stock"><i class="fa-solid fa-boxes-stacked"></i> Stock</button>
        </div>
    </div>

    <%-- Info de resultado --%>
    <% if (!termino.isEmpty()) { %>
    <div class="resultado-info">
        <i class="fa-solid fa-circle-info"></i>
        <span>
            <strong><%= totalProductos %></strong> resultado<%= totalProductos != 1 ? "s" : "" %>
            para &ldquo;<em><%= termino %></em>&rdquo;
            <% if (!"todos".equals(filtroActivo)) { %>
                &nbsp;·&nbsp; filtrado por
                <span class="resultado-badge">
                    <%= "nombre".equals(filtroActivo) ? "nombre"
                      : "material".equals(filtroActivo) ? "material"
                      : "subcategoria".equals(filtroActivo) ? "subcategoría"
                      : "stock" %>
                </span>
            <% } %>
        </span>
        <a href="<%=request.getContextPath()%>/CategoriaServlet<%= categoria != null ? "?id=" + categoria.getCategoriaId() : "" %>"
           class="resultado-limpiar">
            <i class="fa-solid fa-xmark"></i> Limpiar
        </a>
    </div>
    <% } %>

    <%-- ══ GRID DE PRODUCTOS ══ --%>
    <% if (productos == null || productos.isEmpty()) { %>
    <div class="empty-state">
        <div class="empty-state__icon"><i class="fa-solid fa-box-open"></i></div>
        <h3><%= !termino.isEmpty() ? "Sin resultados" : "Sin productos" %></h3>
        <p>
            <% if (!termino.isEmpty()) { %>
                No se encontraron productos para &ldquo;<strong><%= termino %></strong>&rdquo;.
            <% } else { %>
                No hay productos en esta categoría todavía.
            <% } %>
        </p>
    </div>
    <% } else { %>
    <section class="cards__contenedor">
        <% for (Producto p : productos) { %>
        <div class="cards__contenedor-content">
            <a href="<%=request.getContextPath()%>/ProductoServlet?action=ver&id=<%= p.getProductoId() %>">
                <img src="<%=request.getContextPath()%>/imagen-producto/<%= p.getProductoId() %>"
                     alt="<%= p.getNombre() %>"
                     onerror="this.src='<%=request.getContextPath()%>/assets/Imagenes/default.png'">
            </a>
            <h3 class="product__code"><span class="product__label">Código:</span> <span class="product__value"><%= p.getCodigo() %></span></h3>
            <h3 class="product__name"><span class="product__value"><%= p.getNombre() %></span></h3>
            <h4 class="product__category"><span class="product__label">Categoría:</span> <span class="product__value"><%= categoria != null ? categoria.getNombre() : (p.getCategoriaNombre() != null ? p.getCategoriaNombre() : "Sin categoría") %></span></h4>
            <h4 class="product__subcategory"><span class="product__label">Subcategoría:</span> <span class="product__value"><%= p.getSubcategoriaNombre() != null && !p.getSubcategoriaNombre().isEmpty() ? p.getSubcategoriaNombre() : "—" %></span></h4>
            <h4 class="product__material"><span class="product__label">Material:</span> <span class="product__value"><%= p.getMaterialNombre() != null ? p.getMaterialNombre() : "Sin material" %></span></h4>
            <h4 class="product__cost"><span class="product__label">Precio de costo:</span> <span class="product__value">$<%= String.format("%,.0f", p.getPrecioUnitario()) %></span></h4>
            <h4 class="product__price"><span class="product__label">Precio venta:</span> <span class="product__value">$<%= String.format("%,.0f", p.getPrecioVenta()) %></span></h4>
            <h4 class="product__stock">
                <span class="product__label">Stock:</span>
                <span class="product__value <%= p.getStock() == 0 ? "stock-cero" : p.getStock() <= 3 ? "stock-bajo" : "" %>">
                    <%= p.getStock() %>
                    <% if (p.getStock() == 0) { %><i class="fa-solid fa-circle-xmark" title="Sin stock"></i>
                    <% } else if (p.getStock() <= 3) { %><i class="fa-solid fa-triangle-exclamation" title="Stock bajo"></i><% } %>
                </span>
            </h4>
            <h4 class="product__date"><span class="product__label">En stock desde:</span> <span class="product__value"><%= p.getFechaRegistro() != null ? formato.format(p.getFechaRegistro()) : "N/A" %></span></h4>
            <div class="iconos">
                <a href="<%=request.getContextPath()%>/ProductoServlet?action=ver&id=<%= p.getProductoId() %>" title="Ver"><i class="fas fa-eye"></i></a>
                <a href="<%=request.getContextPath()%>/ProductoServlet?action=editar&id=<%= p.getProductoId() %>" title="Editar"><i class="fa-solid fa-pen-to-square"></i></a>
                <a href="<%=request.getContextPath()%>/ProductoServlet?action=confirmarEliminar&id=<%= p.getProductoId() %>" title="Eliminar"><i class="fa-solid fa-trash"></i></a>
            </div>
        </div>
        <% } %>
    </section>
    <% } %>

</main>

<script>
(function () {
    var filtroActivo      = '<%= filtroActivo %>';
    var searchWrap        = document.getElementById('searchWrap');
    var wrapMaterial      = document.getElementById('wrapMaterial');
    var wrapSubcategoria  = document.getElementById('wrapSubcategoria');
    var wrapStock         = document.getElementById('wrapStock');
    var buscadorTexto     = document.getElementById('buscadorTexto');
    var selectMaterial    = document.getElementById('selectMaterial');
    var selectSubcategoria= document.getElementById('selectSubcategoria');
    var inputStock        = document.getElementById('inputStock');
    var hiddenFiltro      = document.getElementById('hiddenFiltro');
    var hiddenQ           = document.getElementById('hiddenQ');
    var formBusqueda      = document.getElementById('formBusqueda');

    function mostrarInput(filtro) {
        searchWrap.style.display       = (filtro === 'todos' || filtro === 'nombre') ? '' : 'none';
        wrapMaterial.style.display     = filtro === 'material'     ? '' : 'none';
        wrapSubcategoria.style.display = filtro === 'subcategoria' ? '' : 'none';
        wrapStock.style.display        = filtro === 'stock'        ? '' : 'none';
        setTimeout(function () {
            if (filtro === 'todos' || filtro === 'nombre') buscadorTexto.focus();
            else if (filtro === 'material')     selectMaterial.focus();
            else if (filtro === 'subcategoria') selectSubcategoria.focus();
            else if (filtro === 'stock')        inputStock.focus();
        }, 60);
    }

    function enviar(filtro, valor) {
        hiddenFiltro.value = filtro;
        hiddenQ.value      = valor || '';
        formBusqueda.submit();
    }

    mostrarInput(filtroActivo);

    document.querySelectorAll('.filter-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            document.querySelectorAll('.filter-btn').forEach(function (b) { b.classList.remove('active'); });
            btn.classList.add('active');
            filtroActivo = btn.dataset.filtro;
            mostrarInput(filtroActivo);
            if (filtroActivo === 'todos') enviar('todos', '');
        });
    });

    buscadorTexto.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') { e.preventDefault(); enviar(filtroActivo === 'nombre' ? 'nombre' : 'todos', buscadorTexto.value.trim()); }
    });

    inputStock.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') { e.preventDefault(); enviar('stock', inputStock.value.trim()); }
    });

    selectMaterial.addEventListener('change', function () { if (this.value) enviar('material', this.value); });
    selectSubcategoria.addEventListener('change', function () { if (this.value) enviar('subcategoria', this.value); });
}());
</script>
</body>
</html>
