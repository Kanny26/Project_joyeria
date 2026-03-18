<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, model.Categoria, model.Administrador, model.Material, model.Subcategoria, model.MetodoPago" %>
<%@ page import="java.net.URLEncoder" %>

<%
    /*
     * Seguridad: se verifica que haya un admin en sesión antes de mostrar cualquier contenido.
     * session.getAttribute("admin") retorna null si la sesión no tiene ese atributo.
     * sendRedirect envía al navegador a la página de login y el return detiene el resto del JSP.
     */
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }

    /*
     * El parámetro "tab" determina cuál pestaña mostrar activa al cargar la página.
     * Los servlets lo incluyen en el sendRedirect después de cada operación exitosa,
     * por ejemplo: CategoriaServlet?msg=creado&tab=categorias
     */
    String tabActivo = request.getParameter("tab");
    if (tabActivo == null) tabActivo = "categorias";

    /*
     * Estos datos los pone el servlet correspondiente con request.setAttribute()
     * antes de hacer forward a este JSP.
     * Si el servlet usa sendRedirect en lugar de forward, estos atributos no llegan
     * porque sendRedirect inicia una petición nueva donde los atributos se pierden.
     * En ese caso, los mensajes se pasan como parámetros en la URL (msg, tipo, etc.)
     */
    List<Categoria>   categorias   = (List<Categoria>)   request.getAttribute("categorias");
    List<Material>    materiales   = (List<Material>)    request.getAttribute("materiales");
    List<Subcategoria> subcategorias = (List<Subcategoria>) request.getAttribute("subcategorias");
    List<MetodoPago>  metodosPago  = (List<MetodoPago>)  request.getAttribute("metodosPago");

    /*
     * Sistema de mensajes:
     * - msg: viene como parámetro en la URL cuando se usa sendRedirect desde el servlet.
     *   Valores posibles: "creado", "actualizado", "eliminado"
     * - error: viene como atributo del request cuando se usa forward desde el servlet (en errores).
     * - tipo, accion, nombre: parámetros adicionales para personalizar el mensaje de éxito.
     */
    String msg    = request.getParameter("msg");
    String tipo   = request.getParameter("tipo");
    String accion = request.getParameter("accion");
    String nombre = request.getParameter("nombre");

    // Atributo de error: lo pone el servlet cuando hace forward después de una excepción.
    String errorAtributo = (String) request.getAttribute("error");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Gestionar Catálogo | AAC27</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/org-categorias.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/animate.css/4.1.1/animate.min.css"/>
    <%-- SweetAlert2: librería para mostrar alertas visuales amigables en lugar de alert() nativo --%>
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head>
<body>

<%--
    BLOQUE DE MENSAJES DE ÉXITO
    Se activa cuando el servlet redirige con ?msg=creado/actualizado/eliminado en la URL.
    Esto ocurre después de un sendRedirect, por lo que los datos llegan como parámetros GET.

    El flujo es:
    1. Usuario envía formulario → Servlet procesa → sendRedirect con ?msg=creado&tipo=categorias&nombre=Collares
    2. Navegador hace GET a esta página → JSP lee los parámetros → muestra SweetAlert
--%>
<%
    String tituloAlerta = "";
    String textoAlerta  = "";
    String iconoAlerta  = "success";
    boolean mostrarExito = (msg != null && !msg.isEmpty() &&
                           ("creado".equals(msg) || "actualizado".equals(msg) || "eliminado".equals(msg)));

    if (mostrarExito) {
        // Determinar el texto del tipo de elemento según el parámetro "tipo"
        String nombreTipo = "Elemento";
        String articulo   = "el";
        if ("categorias".equals(tipo))    { nombreTipo = "Categoría";       articulo = "La"; }
        else if ("subcategorias".equals(tipo)) { nombreTipo = "Subcategoría"; articulo = "La"; }
        else if ("materiales".equals(tipo))    { nombreTipo = "Material";     articulo = "El"; }
        else if ("metodosPago".equals(tipo))   { nombreTipo = "Método de pago"; articulo = "El"; }

        // Construir título y texto del mensaje según la acción realizada
        String accionTexto = (accion != null) ? accion : msg;
        if ("creado".equals(accionTexto) || "creado".equals(msg)) {
            tituloAlerta = nombreTipo + " creado correctamente";
            textoAlerta  = (nombre != null && !nombre.isEmpty())
                ? articulo + " " + nombreTipo.toLowerCase() + " <strong>\"" + nombre + "\"</strong> fue agregado al sistema."
                : "El registro fue creado exitosamente.";
        } else if ("actualizado".equals(accionTexto) || "actualizado".equals(msg)) {
            tituloAlerta = nombreTipo + " actualizado correctamente";
            textoAlerta  = (nombre != null && !nombre.isEmpty())
                ? articulo + " " + nombreTipo.toLowerCase() + " <strong>\"" + nombre + "\"</strong> fue modificado."
                : "El registro fue actualizado exitosamente.";
        } else if ("eliminado".equals(accionTexto) || "eliminado".equals(msg)) {
            tituloAlerta = nombreTipo + " eliminado";
            textoAlerta  = (nombre != null && !nombre.isEmpty())
                ? articulo + " " + nombreTipo.toLowerCase() + " <strong>\"" + nombre + "\"</strong> fue eliminado del sistema."
                : "El registro fue eliminado exitosamente.";
        }
    }
%>

<% if (mostrarExito) { %>
<script>
    document.addEventListener('DOMContentLoaded', function() {
        Swal.fire({
            title: '<%= tituloAlerta %>',
            html: '<%= textoAlerta %>',
            icon: '<%= iconoAlerta %>',
            confirmButtonColor: '#ff85a2',
            confirmButtonText: 'Aceptar',
            timer: 4000,
            timerProgressBar: true,
            showClass: { popup: 'animate__animated animate__fadeInDown' },
            hideClass: { popup: 'animate__animated animate__fadeOutUp' },
            background: '#fff5f7',
            iconColor: '#ff85a2'
        });
    });
</script>
<% } %>

<%--
    BLOQUE DE MENSAJES DE ERROR
    Se activa cuando el servlet captura una excepción y hace forward (no redirect).
    Con forward, los atributos del request se conservan y llegan al JSP.
    Se muestra tanto el atributo "error" del request como un parámetro "error" en la URL (si existiera).
--%>
<% if (errorAtributo != null && !errorAtributo.isEmpty()) { %>
<script>
    document.addEventListener('DOMContentLoaded', function() {
        Swal.fire({
            title: 'Algo salió mal',
            html: '<%= errorAtributo.replace("'", "\\'") %>',
            icon: 'error',
            confirmButtonColor: '#ff85a2',
            confirmButtonText: 'Entendido',
            background: '#fff5f7',
            iconColor: '#e74c3c'
        });
    });
</script>
<% } %>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%=request.getContextPath()%>/Administrador/admin-principal.jsp" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<main class="catalogo-main">
    <div class="catalogo-header">
        <h1 class="catalogo-header__title">Gestión de Catálogo</h1>
    </div>

    <%-- Tabs: cada botón llama a cambiarTab(), que navega al servlet correspondiente --%>
    <div class="tabs-container">
        <button class="tab-pill <%= "categorias".equals(tabActivo) ? "active" : "" %>" onclick="cambiarTab('categorias', '<%=request.getContextPath()%>/CategoriaServlet')">
            <i class="fa-solid fa-layer-group"></i> <span>Categorías</span>
        </button>
        <button class="tab-pill <%= "subcategorias".equals(tabActivo) ? "active" : "" %>" onclick="cambiarTab('subcategorias', '<%=request.getContextPath()%>/SubcategoriaServlet')">
            <i class="fa-solid fa-tags"></i> <span>Subcategorías</span>
        </button>
        <button class="tab-pill <%= "materiales".equals(tabActivo) ? "active" : "" %>" onclick="cambiarTab('materiales', '<%=request.getContextPath()%>/MaterialServlet')">
            <i class="fa-solid fa-gem"></i> <span>Materiales</span>
        </button>
        <button class="tab-pill <%= "metodosPago".equals(tabActivo) ? "active" : "" %>" onclick="cambiarTab('metodosPago', '<%=request.getContextPath()%>/MetodoPagoServlet')">
            <i class="fa-solid fa-credit-card"></i> <span>Métodos de pago</span>
        </button>
    </div>

    <!-- TAB CATEGORÍAS -->
    <div id="tab-categorias" class="tab-panel <%= "categorias".equals(tabActivo) ? "active" : "" %>">
        <button type="button" class="cards__boton-agregar" onclick="abrirModal('modalCategoria')">
            <i class="fa-solid fa-plus"></i> <span>Nueva Categoría</span>
        </button>
        <div class="circles-grid">
            <% if (categorias != null && !categorias.isEmpty()) {
                for (Categoria c : categorias) { %>
                <div class="circle-card">
                    <div class="circle-card__wrapper" onclick="window.location.href='<%=request.getContextPath()%>/CategoriaServlet?id=<%= c.getCategoriaId() %>'">
                        <div class="circle-card__icon-container">
                            <img class="circle-card__icon" src="<%=request.getContextPath()%>/assets/Imagenes/iconos/<%= c.getIcono() %>" alt="<%= c.getNombre() %>">
                        </div>
                        <div class="circle-card__label"><span><%= c.getNombre() %></span></div>
                    </div>
                    <div class="circle-card__actions">
                        <%-- data-* atributos permiten pasar datos al JavaScript sin campos ocultos --%>
                        <button class="action-btn edit"
                                data-id="<%= c.getCategoriaId() %>"
                                data-nombre="<%= c.getNombre().replace("\"", "&quot;") %>"
                                data-icono="<%= c.getIcono() %>"
                                onclick="event.stopPropagation(); prepararEdicion(this, 'categoria')">
                            <i class="fa-solid fa-pen"></i>
                        </button>
                        <button class="action-btn delete"
                                data-id="<%= c.getCategoriaId() %>"
                                data-nombre="<%= c.getNombre().replace("\"", "&quot;") %>"
                                onclick="event.stopPropagation(); prepararEliminacion(this, 'categoria')">
                            <i class="fa-solid fa-trash"></i>
                        </button>
                    </div>
                </div>
            <% } } else { %>
                <div class="empty-state-circle">
                    <i class="fa-solid fa-layer-group"></i>
                    <p>No hay categorías disponibles</p>
                </div>
            <% } %>
        </div>
    </div>

    <!-- TAB SUBCATEGORÍAS -->
    <div id="tab-subcategorias" class="tab-panel <%= "subcategorias".equals(tabActivo) ? "active" : "" %>">
        <button type="button" class="cards__boton-agregar" onclick="abrirModal('modalSubcategoria')">
            <i class="fa-solid fa-plus"></i> <span>Nueva Subcategoría</span>
        </button>
        <div class="rects-grid">
            <% if (subcategorias != null && !subcategorias.isEmpty()) {
                for (Subcategoria s : subcategorias) { %>
                <div class="rect-card">
                    <div class="rect-card__box rect-card__box--subcategoria">
                        <div class="rect-card__content">
                            <div class="rect-card__icon"><i class="fa-solid fa-tag"></i></div>
                            <div class="rect-card__label"><%= s.getNombre() %></div>
                        </div>
                        <div class="rect-card__actions">
                            <button class="action-btn edit"
                                    data-id="<%= s.getSubcategoriaId() %>"
                                    data-nombre="<%= s.getNombre().replace("\"", "&quot;") %>"
                                    onclick="prepararEdicion(this, 'subcategoria')">
                                <i class="fa-solid fa-pen"></i>
                            </button>
                            <button class="action-btn delete"
                                    data-id="<%= s.getSubcategoriaId() %>"
                                    data-nombre="<%= s.getNombre().replace("\"", "&quot;") %>"
                                    onclick="prepararEliminacion(this, 'subcategoria')">
                                <i class="fa-solid fa-trash"></i>
                            </button>
                        </div>
                    </div>
                </div>
            <% } } else { %>
                <div class="empty-state-rect">
                    <i class="fa-solid fa-tags"></i>
                    <p>No hay subcategorías disponibles</p>
                </div>
            <% } %>
        </div>
    </div>

    <!-- TAB MATERIALES -->
    <div id="tab-materiales" class="tab-panel <%= "materiales".equals(tabActivo) ? "active" : "" %>">
        <button type="button" class="cards__boton-agregar" onclick="abrirModal('modalMaterial')">
            <i class="fa-solid fa-plus"></i> <span>Nuevo Material</span>
        </button>
        <div class="rects-grid">
            <% if (materiales != null && !materiales.isEmpty()) {
                for (Material m : materiales) { %>
                <div class="rect-card">
                    <div class="rect-card__box rect-card__box--material">
                        <div class="rect-card__content">
                            <div class="rect-card__icon"><i class="fa-solid fa-cube"></i></div>
                            <div class="rect-card__label"><%= m.getNombre() %></div>
                        </div>
                        <div class="rect-card__actions">
                            <button class="action-btn edit"
                                    data-id="<%= m.getMaterialId() %>"
                                    data-nombre="<%= m.getNombre().replace("\"", "&quot;") %>"
                                    onclick="prepararEdicion(this, 'material')">
                                <i class="fa-solid fa-pen"></i>
                            </button>
                            <button class="action-btn delete"
                                    data-id="<%= m.getMaterialId() %>"
                                    data-nombre="<%= m.getNombre().replace("\"", "&quot;") %>"
                                    onclick="prepararEliminacion(this, 'material')">
                                <i class="fa-solid fa-trash"></i>
                            </button>
                        </div>
                    </div>
                </div>
            <% } } else { %>
                <div class="empty-state-rect">
                    <i class="fa-solid fa-cubes"></i>
                    <p>No hay materiales disponibles</p>
                </div>
            <% } %>
        </div>
    </div>

    <!-- TAB MÉTODOS DE PAGO -->
    <div id="tab-metodosPago" class="tab-panel <%= "metodosPago".equals(tabActivo) ? "active" : "" %>">
        <button type="button" class="cards__boton-agregar" onclick="abrirModal('modalMetodoPago')">
            <i class="fa-solid fa-plus"></i> <span>Nuevo Método</span>
        </button>
        <div class="rects-grid">
            <% if (metodosPago != null && !metodosPago.isEmpty()) {
                for (MetodoPago mp : metodosPago) { %>
                <div class="rect-card">
                    <div class="rect-card__box rect-card__box--metodopago">
                        <div class="rect-card__content">
                            <div class="rect-card__icon"><i class="fa-solid fa-credit-card"></i></div>
                            <div class="rect-card__label"><%= mp.getNombre() %></div>
                        </div>
                        <div class="rect-card__actions">
                            <button class="action-btn edit"
                                    data-id="<%= mp.getMetodoPagoId() %>"
                                    data-nombre="<%= mp.getNombre().replace("\"", "&quot;") %>"
                                    onclick="prepararEdicion(this, 'metodoPago')">
                                <i class="fa-solid fa-pen"></i>
                            </button>
                            <button class="action-btn delete"
                                    data-id="<%= mp.getMetodoPagoId() %>"
                                    data-nombre="<%= mp.getNombre().replace("\"", "&quot;") %>"
                                    onclick="prepararEliminacion(this, 'metodoPago')">
                                <i class="fa-solid fa-trash"></i>
                            </button>
                        </div>
                    </div>
                </div>
            <% } } else { %>
                <div class="empty-state-rect">
                    <i class="fa-solid fa-money-bill-wave"></i>
                    <p>No hay métodos de pago disponibles</p>
                </div>
            <% } %>
        </div>
    </div>
</main>

<!-- MODAL CATEGORÍA -->
<div id="modalCategoria" class="modal-overlay">
    <div class="modal-container">
        <div class="modal-header">
            <div class="modal-header__icon"><i class="fa-solid fa-layer-group"></i></div>
            <h3 class="modal-header__title" id="modalCategoriaTitle">Nueva Categoría</h3>
            <button type="button" class="modal-close" onclick="cerrarModal('modalCategoria')">
                <i class="fa-solid fa-times"></i>
            </button>
        </div>
        <%--
            enctype="multipart/form-data" es obligatorio para enviar archivos (el icono).
            Sin esto, el servlet no puede leer el archivo con request.getPart().
            Los campos ocultos (action, id, icono) se llenan dinámicamente desde JavaScript
            según si se está creando o editando una categoría.
        --%>
        <form id="formCategoria" action="<%=request.getContextPath()%>/CategoriaServlet" method="post" enctype="multipart/form-data"
              onsubmit="mostrarCargando(this, 'Guardando categoría...')">
            <input type="hidden" name="action" id="catAction" value="guardar">
            <input type="hidden" name="id"     id="catId">
            <input type="hidden" name="icono"  id="catIcono">
            <input type="hidden" name="tipo"   value="categorias">
            <div class="modal-body">
                <div class="form-group">
                    <label class="form-label">Nombre de la categoría</label>
                    <input type="text" name="nombre" id="catNombre" class="form-input" required
                           placeholder="Ej: Collares, Anillos, Pulseras...">
                </div>
                <div class="form-group" id="fieldIcono">
                    <label class="form-label">Icono de la categoría</label>
                    <input type="file" name="archivoIcono" class="form-input-file" accept="image/*">
                    <small style="display:block; color:#8b7aa8; margin-top:5px;">Formatos permitidos: JPG, PNG, GIF</small>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn-modal-cancel" onclick="cerrarModal('modalCategoria')">Cancelar</button>
                <button type="submit" class="btn-modal-save">
                    <i class="fa-solid fa-save"></i> Guardar
                </button>
            </div>
        </form>
    </div>
</div>

<!-- MODAL SUBCATEGORÍA -->
<div id="modalSubcategoria" class="modal-overlay">
    <div class="modal-container">
        <div class="modal-header">
            <div class="modal-header__icon modal-header__icon--blue"><i class="fa-solid fa-tag"></i></div>
            <h3 class="modal-header__title" id="modalSubcategoriaTitle">Nueva Subcategoría</h3>
            <button type="button" class="modal-close" onclick="cerrarModal('modalSubcategoria')">
                <i class="fa-solid fa-times"></i>
            </button>
        </div>
        <form action="<%=request.getContextPath()%>/SubcategoriaServlet" method="post"
              onsubmit="mostrarCargando(this, 'Guardando subcategoría...')">
            <input type="hidden" name="action" id="subcatAction" value="guardar">
            <input type="hidden" name="id"     id="subcatId">
            <input type="hidden" name="tipo"   value="subcategorias">
            <div class="modal-body">
                <div class="form-group">
                    <label class="form-label">Nombre de la subcategoría</label>
                    <input type="text" name="nombre" id="subcatNombre" class="form-input" required
                           placeholder="Ej: Collares largos, Dijes, Argollas...">
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn-modal-cancel" onclick="cerrarModal('modalSubcategoria')">Cancelar</button>
                <button type="submit" class="btn-modal-save">
                    <i class="fa-solid fa-save"></i> Guardar
                </button>
            </div>
        </form>
    </div>
</div>

<!-- MODAL MATERIAL -->
<div id="modalMaterial" class="modal-overlay">
    <div class="modal-container">
        <div class="modal-header">
            <div class="modal-header__icon modal-header__icon--red"><i class="fa-solid fa-cube"></i></div>
            <h3 class="modal-header__title" id="modalMaterialTitle">Nuevo Material</h3>
            <button type="button" class="modal-close" onclick="cerrarModal('modalMaterial')">
                <i class="fa-solid fa-times"></i>
            </button>
        </div>
        <form action="<%=request.getContextPath()%>/MaterialServlet" method="post"
              onsubmit="mostrarCargando(this, 'Guardando material...')">
            <input type="hidden" name="action" id="matAction" value="guardar">
            <input type="hidden" name="id"     id="matId">
            <input type="hidden" name="tipo"   value="materiales">
            <div class="modal-body">
                <div class="form-group">
                    <label class="form-label">Nombre del material</label>
                    <input type="text" name="nombre" id="matNombre" class="form-input" required
                           placeholder="Ej: Oro, Plata, Acero, Cobre...">
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn-modal-cancel" onclick="cerrarModal('modalMaterial')">Cancelar</button>
                <button type="submit" class="btn-modal-save">
                    <i class="fa-solid fa-save"></i> Guardar
                </button>
            </div>
        </form>
    </div>
</div>

<!-- MODAL MÉTODO DE PAGO -->
<div id="modalMetodoPago" class="modal-overlay">
    <div class="modal-container">
        <div class="modal-header">
            <div class="modal-header__icon modal-header__icon--green"><i class="fa-solid fa-credit-card"></i></div>
            <h3 class="modal-header__title" id="modalMetodoPagoTitle">Nuevo Método de Pago</h3>
            <button type="button" class="modal-close" onclick="cerrarModal('modalMetodoPago')">
                <i class="fa-solid fa-times"></i>
            </button>
        </div>
        <form action="<%=request.getContextPath()%>/MetodoPagoServlet" method="post"
              onsubmit="mostrarCargando(this, 'Guardando método de pago...')">
            <input type="hidden" name="action" id="mpAction" value="guardar">
            <input type="hidden" name="id"     id="mpId">
            <input type="hidden" name="tipo"   value="metodosPago">
            <div class="modal-body">
                <div class="form-group">
                    <label class="form-label">Nombre del método de pago</label>
                    <input type="text" name="nombre" id="mpNombre" class="form-input" required
                           placeholder="Ej: Efectivo, Tarjeta, Transferencia...">
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn-modal-cancel" onclick="cerrarModal('modalMetodoPago')">Cancelar</button>
                <button type="submit" class="btn-modal-save">
                    <i class="fa-solid fa-save"></i> Guardar
                </button>
            </div>
        </form>
    </div>
</div>

<script>

// Navega al servlet del tab seleccionado. El servlet carga los datos y hace forward de vuelta aquí.
function cambiarTab(tab, url) {
    window.location.href = url + '?tab=' + tab;
}

// Abre un modal, resetea su formulario y restaura los valores por defecto del título y campos.
function abrirModal(id) {
    const modal = document.getElementById(id);
    if (!modal) return;

    const form = modal.querySelector('form');
    if (form) form.reset();

    // Restablecer los campos ocultos y títulos al estado de "crear nuevo"
    if (id === 'modalCategoria') {
        document.getElementById('catAction').value = 'guardar';
        document.getElementById('catId').value     = '';
        document.getElementById('modalCategoriaTitle').innerText = 'Nueva Categoría';
        const fieldIcono = document.getElementById('fieldIcono');
        if (fieldIcono) fieldIcono.style.display = 'block';
    } else if (id === 'modalSubcategoria') {
        document.getElementById('subcatAction').value = 'guardar';
        document.getElementById('subcatId').value     = '';
        document.getElementById('modalSubcategoriaTitle').innerText = 'Nueva Subcategoría';
    } else if (id === 'modalMaterial') {
        document.getElementById('matAction').value = 'guardar';
        document.getElementById('matId').value     = '';
        document.getElementById('modalMaterialTitle').innerText = 'Nuevo Material';
    } else if (id === 'modalMetodoPago') {
        document.getElementById('mpAction').value = 'guardar';
        document.getElementById('mpId').value     = '';
        document.getElementById('modalMetodoPagoTitle').innerText = 'Nuevo Método de Pago';
    }

    modal.classList.add('active');
}

function cerrarModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.remove('active');
}

/*
 * Prepara el modal en modo edición.
 * btn.getAttribute('data-*') recupera los valores que el JSP escribió como atributos HTML
 * al generar cada tarjeta. Esto evita hacer una petición adicional para traer los datos.
 */
function prepararEdicion(btn, entidad) {
    const id     = btn.getAttribute('data-id');
    const nombre = btn.getAttribute('data-nombre');

    if (entidad === 'categoria') {
        const icono = btn.getAttribute('data-icono');
        abrirModal('modalCategoria');
        document.getElementById('catAction').value = 'actualizar';
        document.getElementById('catId').value     = id;
        document.getElementById('catNombre').value = nombre;
        document.getElementById('catIcono').value  = icono;
        // Al editar no se pide nuevo icono; se mantiene el existente enviado como campo oculto
        const fieldIcono = document.getElementById('fieldIcono');
        if (fieldIcono) fieldIcono.style.display = 'none';
        document.getElementById('modalCategoriaTitle').innerText = 'Editar Categoría';

    } else if (entidad === 'subcategoria') {
        abrirModal('modalSubcategoria');
        document.getElementById('subcatAction').value = 'actualizar';
        document.getElementById('subcatId').value     = id;
        document.getElementById('subcatNombre').value = nombre;
        document.getElementById('modalSubcategoriaTitle').innerText = 'Editar Subcategoría';

    } else if (entidad === 'material') {
        abrirModal('modalMaterial');
        document.getElementById('matAction').value = 'actualizar';
        document.getElementById('matId').value     = id;
        document.getElementById('matNombre').value = nombre;
        document.getElementById('modalMaterialTitle').innerText = 'Editar Material';

    } else if (entidad === 'metodoPago') {
        abrirModal('modalMetodoPago');
        document.getElementById('mpAction').value = 'actualizar';
        document.getElementById('mpId').value     = id;
        document.getElementById('mpNombre').value = nombre;
        document.getElementById('modalMetodoPagoTitle').innerText = 'Editar Método de Pago';
    }
}

/*
 * Muestra una confirmación con SweetAlert2 antes de ejecutar la eliminación.
 * Si el usuario confirma, se crea un formulario dinámico con POST y se envía.
 * Se usa POST (no un enlace GET) para seguir las buenas prácticas: las operaciones
 * que modifican datos no deben ejecutarse con GET.
 *
 * Los parámetros "nombre" y "tipo" se incluyen en el formulario para que el servlet
 * los reenvíe en el redirect, permitiendo que este JSP personalice el mensaje de éxito.
 */
function prepararEliminacion(btn, entidad) {
    const id     = btn.getAttribute('data-id');
    const nombre = btn.getAttribute('data-nombre');

    const config = {
        categoria:   { texto: 'categoría',      param: 'categorias',   url: '<%=request.getContextPath()%>/CategoriaServlet',    articulo: 'la' },
        subcategoria:{ texto: 'subcategoría',   param: 'subcategorias', url: '<%=request.getContextPath()%>/SubcategoriaServlet', articulo: 'la' },
        material:    { texto: 'material',        param: 'materiales',   url: '<%=request.getContextPath()%>/MaterialServlet',      articulo: 'el' },
        metodoPago:  { texto: 'método de pago', param: 'metodosPago',  url: '<%=request.getContextPath()%>/MetodoPagoServlet',    articulo: 'el' }
    };

    const c = config[entidad];
    if (!c) return;

    Swal.fire({
        title: '¿Eliminar ' + c.texto + '?',
        html: 'Se eliminará ' + c.articulo + ' ' + c.texto + ':<br><strong style="font-size:1.1rem;">"' + nombre + '"</strong><br><small>Esta acción no se puede deshacer.</small>',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#ff85a2',
        cancelButtonColor: '#8b7aa8',
        cancelButtonText: 'Cancelar',
        confirmButtonText: 'Sí, eliminar',
        reverseButtons: true,
        background: '#fff5f7',
        iconColor: '#ff85a2'
    }).then(function(result) {
        if (result.isConfirmed) {
            // Mostrar indicador de que se está procesando la eliminación
            Swal.fire({
                title: 'Eliminando...',
                text: 'Por favor espera.',
                allowOutsideClick: false,
                didOpen: function() { Swal.showLoading(); }
            });

            // Crear y enviar formulario dinámico con todos los datos necesarios
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = c.url;

            [
                { name: 'action', value: 'eliminar' },
                { name: 'id',     value: id },
                { name: 'nombre', value: nombre },
                { name: 'tipo',   value: c.param },
                { name: 'accion', value: 'eliminado' }
            ].forEach(function(p) {
                const input = document.createElement('input');
                input.type  = 'hidden';
                input.name  = p.name;
                input.value = p.value;
                form.appendChild(input);
            });

            document.body.appendChild(form);
            form.submit();
        }
    });
}

/*
 * Muestra un indicador de "cargando" al enviar un formulario.
 * Se llama desde el evento onsubmit del formulario antes de que la página navegue.
 * El texto describe qué operación se está ejecutando para que el usuario no se pregunte qué pasó.
 */
function mostrarCargando(form, mensaje) {
    // Primero validamos que el formulario sea válido (los campos required estén completos)
    if (!form.checkValidity()) return;

    Swal.fire({
        title: mensaje,
        allowOutsideClick: false,
        didOpen: function() { Swal.showLoading(); }
    });
}

// Cierra cualquier modal abierto al presionar la tecla Escape
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal-overlay.active').forEach(function(modal) {
            modal.classList.remove('active');
        });
    }
});

// Cierra el modal si el usuario hace clic fuera del contenido (en el fondo oscuro)
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.classList.remove('active');
    }
});

</script>

</body>
</html>
