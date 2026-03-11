<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Producto" %>

<%
    Producto producto = (Producto) request.getAttribute("producto");
    if (producto == null) {
        response.sendRedirect(request.getContextPath() + "/ProductoServlet");
        return;
    }
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Producto Eliminado - AAC27</title>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css" />
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/eliminado.css" />
</head>
<body>
<nav class="navbar-admin"> 
    <div class="navbar-admin__catalogo"> 
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin Dashboard"> 
    </div> 
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%=request.getContextPath()%>/ProductoServlet" title="Volver al inicio">
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i> 
    </a> 
</nav>

<main class="eliminado">

    <div class="eliminado__contenedor">
        <div class="eliminar-producto__imagen" style="position: relative; overflow: hidden; border-radius: 15px;">
            <div class="eliminado__badge" style="position: absolute; top: 10px; right: 10px; background: #ef4444; color: white; padding: 5px 15px; border-radius: 20px; font-size: 0.8rem; font-weight: bold;">
                ELIMINADO
            </div>
        </div>

        <div class="eliminado__mensaje">
            <span style="color: #64748b; text-transform: uppercase; font-size: 0.8rem; font-weight: 700; letter-spacing: 1px;">Confirmación de salida</span>
            <h2 style="margin-top: 0.5rem; color: #1e293b; line-height: 1.2;">
                El producto <strong style="color: #7c3aed;">"<%= producto.getNombre() %>"</strong> ha sido retirado del inventario exitosamente.
            </h2>
            
            <p style="color: #64748b; margin-bottom: 2rem; font-size: 0.95rem;">
                Esta acción es permanente. Los datos ya no aparecerán en el catálogo público ni en la gestión de stock actual.
            </p>

            <a href="<%=request.getContextPath()%>/ProductoServlet" class="eliminado__boton-volver">
                <i class="fa-solid fa-arrow-left" style="margin-right: 8px;"></i>
                Volver a Gestión de Productos
            </a>
        </div>
    </div>
</main>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
    // Pequeño detalle: Un aviso rápido al cargar que se quite solo
    window.onload = () => {
        const Toast = Swal.mixin({
            toast: true,
            position: 'top-end',
            showConfirmButton: false,
            timer: 3000,
            timerProgressBar: true
        });
        Toast.fire({
            icon: 'success',
            title: 'Inventario actualizado'
        });
    }
</script>
</body>
</html>