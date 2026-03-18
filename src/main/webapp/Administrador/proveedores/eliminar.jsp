<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="model.Proveedor, model.Administrador" %>
<%
    /* Seguridad: si no hay sesión activa de admin, redirige al login */
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }

    /*
     * El proveedor llega como atributo del request via forward del servlet.
     * Si no existe (alguien accede directo a esta URL), se redirige al listado.
     */
    Proveedor proveedor = (Proveedor) request.getAttribute("proveedor");
    if (proveedor == null) {
        response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Desactivar Proveedor - AAC27</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/producto.css">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head>
<body>
<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/ProveedorServlet?action=listar" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<div class="page-header">
    <h1 class="product-title" style="color: #dc2626;">Desactivar Proveedor</h1>
</div>

<main class="product-page">
    <section class="product-card">
        <div class="product-image">
            <div class="product-image__circle" style="background:#fee2e2; display:flex; align-items:center; justify-content:center;">
                <i class="fa-solid fa-user-slash" style="font-size: 4rem; color: #dc2626;"></i>
            </div>
        </div>

        <%-- Advertencia con el nombre del proveedor para que el usuario confirme qué está desactivando --%>
        <div class="product-details" style="justify-content: center;">
            <div class="alert-box">
                <i class="fa-solid fa-triangle-exclamation"></i>
                <p>
                    <strong>Advertencia:</strong> El proveedor
                    <b><%= proveedor.getNombre() %></b>
                    (<%= proveedor.getDocumento() != null ? proveedor.getDocumento() : "sin documento" %>)
                    será marcado como <b>inactivo</b>.
                    Podrá reactivarlo desde el listado de proveedores.
                </p>
            </div>

            <div class="product-actions" style="margin-top: 30px;">
                <%--
                    El formulario envía action=eliminar al servlet, que ejecuta la eliminación lógica.
                    El botón submit está deshabilitado inicialmente; solo se habilita si el usuario
                    confirma la acción en el diálogo de SweetAlert.
                --%>
                <form id="formEliminar" action="<%= request.getContextPath() %>/ProveedorServlet"
                      method="post" style="flex: 1;">
                    <input type="hidden" name="action" value="eliminar">
                    <input type="hidden" name="id"     value="<%= proveedor.getProveedorId() %>">
                    <button type="button" id="btnDesactivar" class="btn-danger" style="width: 100%;"
                            onclick="confirmarDesactivacion()">
                        <i class="fa-solid fa-user-slash"></i> Desactivar Proveedor
                    </button>
                </form>
                <button type="button" class="btn-primary" style="flex: 1;"
                        onclick="window.history.back()">
                    <i class="fa-solid fa-xmark"></i> Cancelar
                </button>
            </div>
        </div>
    </section>
</main>

<script>
/*
 * Muestra un diálogo de confirmación antes de enviar el formulario de desactivación.
 * Esto evita que el usuario desactive un proveedor por error con un solo clic.
 * Solo si confirma se ejecuta el submit real del formulario.
 */
function confirmarDesactivacion() {
    Swal.fire({
        title: '¿Desactivar este proveedor?',
        html: '<p>El proveedor <strong><%= proveedor.getNombre().replace("'", "\\'") %></strong> ' +
              'quedará como <b>inactivo</b>.<br>Sus compras y datos se conservarán.<br>' +
              'Puedes reactivarlo en cualquier momento desde el listado.</p>',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#dc2626',
        cancelButtonColor: '#6b7280',
        confirmButtonText: 'Sí, desactivar',
        cancelButtonText: 'No, cancelar'
    }).then(function(result) {
        if (result.isConfirmed) {
            /* Mostrar estado de carga para que el usuario sepa que se está procesando */
            Swal.fire({
                title: 'Desactivando...',
                text: 'Por favor espera un momento.',
                allowOutsideClick: false,
                didOpen: function() {
                    Swal.showLoading();
                }
            });
            document.getElementById('formEliminar').submit();
        }
    });
}
</script>
</body>
</html>
