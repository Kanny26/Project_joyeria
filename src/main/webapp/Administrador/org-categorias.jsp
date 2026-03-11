<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, model.Categoria, model.Administrador, model.Material, model.Subcategoria, model.MetodoPago" %>

<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }

    String tabActivo = request.getParameter("tab");
    if (tabActivo == null) tabActivo = "categorias";

    List<Categoria> categorias = (List<Categoria>) request.getAttribute("categorias");
    List<Material> materiales = (List<Material>) request.getAttribute("materiales");
    List<Subcategoria> subcategorias = (List<Subcategoria>) request.getAttribute("subcategorias");
    List<MetodoPago> metodosPago = (List<MetodoPago>) request.getAttribute("metodosPago");

    String msg = request.getParameter("msg");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Gestionar Catálogo | AAC27</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/org-categorias.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head>
<body>

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

    <% if (msg != null) { %>
        <div class="alert-message alert-success">
            <i class="fa-solid fa-circle-check"></i>
            <span><%= "creado".equals(msg) ? "¡Registro creado!" : "actualizado".equals(msg) ? "¡Actualizado correctamente!" : "¡Eliminado!" %></span>
        </div>
    <% } %>

    <div id="tab-categorias" class="tab-panel <%= "categorias".equals(tabActivo) ? "active" : "" %>">
        <button type="button" class="cards__boton-agregar" onclick="abrirModal('modalCategoria')">
            <i class="fa-solid fa-plus"></i> <span>Nueva Categoría</span>
        </button>
        <div class="circles-grid">
            <% if (categorias != null) { for (Categoria c : categorias) { %>
                <div class="circle-card">
                    <div class="circle-card__wrapper" onclick="window.location.href='<%=request.getContextPath()%>/CategoriaServlet?id=<%= c.getCategoriaId() %>'">
                        <div class="circle-card__icon-container">
                            <img class="circle-card__icon" src="<%=request.getContextPath()%>/assets/Imagenes/iconos/<%= c.getIcono() %>">
                        </div>
                        <div class="circle-card__label"><span><%= c.getNombre() %></span></div>
                    </div>
                    <div class="circle-card__actions">
                        <button class="action-btn edit" data-id="<%= c.getCategoriaId() %>" data-nombre="<%= c.getNombre().replace("\"", "&quot;") %>" data-icono="<%= c.getIcono() %>" onclick="event.stopPropagation(); prepararEdicion(this, 'categoria')">
                            <i class="fa-solid fa-pen"></i>
                        </button>
                        <button class="action-btn delete" data-id="<%= c.getCategoriaId() %>" data-nombre="<%= c.getNombre().replace("\"", "&quot;") %>" onclick="event.stopPropagation(); prepararEliminacion(this, 'categoria')">
                            <i class="fa-solid fa-trash"></i>
                        </button>
                    </div>
                </div>
            <% } } %>
        </div>
    </div>

    <div id="tab-subcategorias" class="tab-panel <%= "subcategorias".equals(tabActivo) ? "active" : "" %>">
        <button type="button" class="cards__boton-agregar" onclick="abrirModal('modalSubcategoria')">
            <i class="fa-solid fa-plus"></i> <span>Nueva Subcategoría</span>
        </button>
        <div class="rects-grid">
            <% if (subcategorias != null) { for (Subcategoria s : subcategorias) { %>
                <div class="rect-card">
                    <div class="rect-card__box rect-card__box--subcategoria">
                        <div class="rect-card__label"><%= s.getNombre() %></div>
                        <div class="rect-card__actions">
                            <button class="action-btn edit" data-id="<%= s.getSubcategoriaId() %>" data-nombre="<%= s.getNombre().replace("\"", "&quot;") %>" onclick="prepararEdicion(this, 'subcategoria')">
                                <i class="fa-solid fa-pen"></i>
                            </button>
                            <button class="action-btn delete" data-id="<%= s.getSubcategoriaId() %>" data-nombre="<%= s.getNombre().replace("\"", "&quot;") %>" onclick="prepararEliminacion(this, 'subcategoria')">
                                <i class="fa-solid fa-trash"></i>
                            </button>
                        </div>
                    </div>
                </div>
            <% } } %>
        </div>
    </div>

    <div id="tab-materiales" class="tab-panel <%= "materiales".equals(tabActivo) ? "active" : "" %>">
        <button type="button" class="cards__boton-agregar" onclick="abrirModal('modalMaterial')">
            <i class="fa-solid fa-plus"></i> <span>Nuevo Material</span>
        </button>
        <div class="rects-grid">
            <% if (materiales != null) { for (Material m : materiales) { %>
                <div class="rect-card">
                    <div class="rect-card__box rect-card__box--material">
                        <div class="rect-card__label"><%= m.getNombre() %></div>
                        <div class="rect-card__actions">
                            <button class="action-btn edit" data-id="<%= m.getMaterialId() %>" data-nombre="<%= m.getNombre().replace("\"", "&quot;") %>" onclick="prepararEdicion(this, 'material')">
                                <i class="fa-solid fa-pen"></i>
                            </button>
                            <button class="action-btn delete" data-id="<%= m.getMaterialId() %>" data-nombre="<%= m.getNombre().replace("\"", "&quot;") %>" onclick="prepararEliminacion(this, 'material')">
                                <i class="fa-solid fa-trash"></i>
                            </button>
                        </div>
                    </div>
                </div>
            <% } } %>
        </div>
    </div>

    <div id="tab-metodosPago" class="tab-panel <%= "metodosPago".equals(tabActivo) ? "active" : "" %>">
        <button type="button" class="cards__boton-agregar" onclick="abrirModal('modalMetodoPago')">
            <i class="fa-solid fa-plus"></i> <span>Nuevo Método</span>
        </button>
        <div class="rects-grid">
            <% if (metodosPago != null) { for (MetodoPago mp : metodosPago) { %>
                <div class="rect-card">
                    <div class="rect-card__box rect-card__box--metodopago">
                        <div class="rect-card__label"><%= mp.getNombre() %></div>
                        <div class="rect-card__actions">
                            <button class="action-btn edit" data-id="<%= mp.getMetodoPagoId() %>" data-nombre="<%= mp.getNombre().replace("\"", "&quot;") %>" onclick="prepararEdicion(this, 'metodoPago')">
                                <i class="fa-solid fa-pen"></i>
                            </button>
                            <button class="action-btn delete" data-id="<%= mp.getMetodoPagoId() %>" data-nombre="<%= mp.getNombre().replace("\"", "&quot;") %>" onclick="prepararEliminacion(this, 'metodoPago')">
                                <i class="fa-solid fa-trash"></i>
                            </button>
                        </div>
                    </div>
                </div>
            <% } } %>
        </div>
    </div>
</main>

<div id="modalCategoria" class="modal-overlay">
    <div class="modal-container">
        <div class="modal-header"><h3 id="modalCategoriaTitle">Nueva Categoría</h3></div>
        <form id="formCategoria" action="<%=request.getContextPath()%>/CategoriaServlet" method="post" enctype="multipart/form-data">
            <input type="hidden" name="action" id="catAction" value="guardar">
            <input type="hidden" name="id" id="catId">
            <input type="hidden" name="icono" id="catIcono">
            <div class="modal-body">
                <input type="text" name="nombre" id="catNombre" class="form-input" required placeholder="Nombre">
                <div id="fieldIcono"> 
                    <label class="form-label">Icono de la categoría:</label>
                    <input type="file" name="archivoIcono" class="form-input-file"> 
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn-modal-cancel" onclick="cerrarModal('modalCategoria')">Cancelar</button>
                <button type="submit" class="btn-modal-save">Guardar</button>
            </div>
        </form>
    </div>
</div>

<div id="modalSubcategoria" class="modal-overlay">
    <div class="modal-container">
        <div class="modal-header"><h3 id="modalSubcategoriaTitle">Nueva Subcategoría</h3></div>
        <form action="<%=request.getContextPath()%>/SubcategoriaServlet" method="post">
            <input type="hidden" name="action" id="subcatAction" value="guardar">
            <input type="hidden" name="id" id="subcatId">
            <div class="modal-body">
                <input type="text" name="nombre" id="subcatNombre" class="form-input" required placeholder="Nombre de la subcategoría">
            </div>
            <div class="modal-footer">
                <button type="button" class="btn-modal-cancel" onclick="cerrarModal('modalSubcategoria')">Cancelar</button>
                <button type="submit" class="btn-modal-save">Guardar</button>
            </div>
        </form>
    </div>
</div>

<div id="modalMaterial" class="modal-overlay">
    <div class="modal-container">
        <div class="modal-header"><h3 id="modalMaterialTitle">Nuevo Material</h3></div>
        <form action="<%=request.getContextPath()%>/MaterialServlet" method="post">
            <input type="hidden" name="action" id="matAction" value="guardar">
            <input type="hidden" name="id" id="matId">
            <div class="modal-body">
                <input type="text" name="nombre" id="matNombre" class="form-input" required placeholder="Nombre del material">
            </div>
            <div class="modal-footer">
                <button type="button" class="btn-modal-cancel" onclick="cerrarModal('modalMaterial')">Cancelar</button>
                <button type="submit" class="btn-modal-save">Guardar</button>
            </div>
        </form>
    </div>
</div>

<div id="modalMetodoPago" class="modal-overlay">
    <div class="modal-container">
        <div class="modal-header"><h3 id="modalMetodoPagoTitle">Nuevo Método</h3></div>
        <form action="<%=request.getContextPath()%>/MetodoPagoServlet" method="post">
            <input type="hidden" name="action" id="mpAction" value="guardar">
            <input type="hidden" name="id" id="mpId">
            <div class="modal-body">
                <input type="text" name="nombre" id="mpNombre" class="form-input" required placeholder="Nombre del método">
            </div>
            <div class="modal-footer">
                <button type="button" class="btn-modal-cancel" onclick="cerrarModal('modalMetodoPago')">Cancelar</button>
                <button type="submit" class="btn-modal-save">Guardar</button>
            </div>
        </form>
    </div>
</div>

<script>
// --- GENERAL ---
function cambiarTab(tab, url) { window.location.href = url + '?tab=' + tab; }

function abrirModal(id) { 
    const modal = document.getElementById(id);
    if(modal) {
        modal.querySelector('form').reset();
        modal.querySelector('[name="action"]').value = 'guardar';
        // Resetear títulos y visibilidad de campos por defecto
        if(id === 'modalCategoria') {
            document.getElementById('modalCategoriaTitle').innerText = 'Nueva Categoría';
            document.getElementById('fieldIcono').style.display = 'block';
        }
        modal.classList.add('active'); 
    }
}

function cerrarModal(id) { 
    const modal = document.getElementById(id);
    if(modal) modal.classList.remove('active'); 
}

// --- LÓGICA DE EDICIÓN ---
function prepararEdicion(btn, entidad) {
    const id = btn.getAttribute('data-id');
    const nombre = btn.getAttribute('data-nombre');

    if (entidad === 'categoria') {
        const icono = btn.getAttribute('data-icono');
        abrirModal('modalCategoria');
        document.getElementById('catAction').value = 'actualizar';
        document.getElementById('catId').value = id;
        document.getElementById('catNombre').value = nombre;
        document.getElementById('catIcono').value = icono;
        document.getElementById('fieldIcono').style.display = 'none'; // Ocultar file al editar
        document.getElementById('modalCategoriaTitle').innerText = 'Editar Categoría';
    } else if (entidad === 'subcategoria') {
        abrirModal('modalSubcategoria');
        document.getElementById('subcatAction').value = 'actualizar';
        document.getElementById('subcatId').value = id;
        document.getElementById('subcatNombre').value = nombre;
        document.getElementById('modalSubcategoriaTitle').innerText = 'Editar Subcategoría';
    } else if (entidad === 'material') {
        abrirModal('modalMaterial');
        document.getElementById('matAction').value = 'actualizar';
        document.getElementById('matId').value = id;
        document.getElementById('matNombre').value = nombre;
        document.getElementById('modalMaterialTitle').innerText = 'Editar Material';
    } else if (entidad === 'metodoPago') {
        abrirModal('modalMetodoPago');
        document.getElementById('mpAction').value = 'actualizar';
        document.getElementById('mpId').value = id;
        document.getElementById('mpNombre').value = nombre;
        document.getElementById('modalMetodoPagoTitle').innerText = 'Editar Método';
    }
}

// --- LÓGICA DE ELIMINACIÓN ---
function prepararEliminacion(btn, entidad) {
    const id = btn.getAttribute('data-id');
    const nombre = btn.getAttribute('data-nombre');
    
    Swal.fire({
        title: '¿Confirmar eliminación?',
        text: "Se eliminará: " + nombre,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#ff85a2', // Color rosita para combinar
        cancelButtonColor: '#8b7aa8',
        cancelButtonText: 'Cancelar',
        confirmButtonText: 'Sí, eliminar'
    }).then((result) => {
        if (result.isConfirmed) {
            const urls = {
                'categoria': '<%=request.getContextPath()%>/CategoriaServlet',
                'subcategoria': '<%=request.getContextPath()%>/SubcategoriaServlet',
                'material': '<%=request.getContextPath()%>/MaterialServlet',
                'metodoPago': '<%=request.getContextPath()%>/MetodoPagoServlet'
            };
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = urls[entidad];
            const actionInp = document.createElement('input');
            actionInp.type = 'hidden'; actionInp.name = 'action'; actionInp.value = 'eliminar';
            const idInp = document.createElement('input');
            idInp.type = 'hidden'; idInp.name = 'id'; idInp.value = id;
            form.appendChild(actionInp); form.appendChild(idInp);
            document.body.appendChild(form);
            form.submit();
        }
    });
}
</script>
</body>
</html>