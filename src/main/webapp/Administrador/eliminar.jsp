<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="model.Producto, model.Administrador" %>
<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }
    Producto producto = (Producto) request.getAttribute("producto");
    if (producto == null) {
        response.sendRedirect(request.getContextPath() + "/ProductoServlet");
        return;
    }
    // Asumimos que el objeto Producto trae un booleano o count de movimientos
    // Si no lo tienes, puedes simularlo o validarlo en el Servlet
    boolean tieneMovimientos = (request.getAttribute("tieneMovimientos") != null) 
                               ? (Boolean) request.getAttribute("tieneMovimientos") 
                               : false;
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Eliminar Producto - AAC27</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/producto.css">
    <style>
        .danger-zone {
            border: 2px dashed #ef4444;
            padding: 3rem;
            border-radius: 30px;
            background-color: #ffffff;
            text-align: center;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05);
            max-width: 480px;
            width: 100%;
        }
        
        .img-container {
            position: relative;
            display: inline-block;
            margin-bottom: 1.5rem;
        }

        .product-preview-del {
            width: 180px;
            height: 180px;
            border-radius: 50%;
            object-fit: cover;
            border: 6px solid #fef2f2;
            box-shadow: 0 10px 20px rgba(0,0,0,0.1);
        }

        /* Badge de stock o historial */
        .status-badge {
            position: absolute;
            bottom: 10px;
            right: 10px;
            background: #ef4444;
            color: white;
            padding: 5px 12px;
            border-radius: 20px;
            font-size: 11px;
            font-weight: bold;
        }

        .btn-danger-custom {
            background: #dc2626;
            color: white;
            padding: 16px;
            border-radius: 15px;
            border: none;
            font-weight: bold;
            cursor: pointer;
            width: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
            transition: 0.3s;
        }

        .btn-danger-custom:disabled {
            background: #94a3b8;
            cursor: not-allowed;
            transform: none !important;
        }

        .info-lock {
            background: #fff7ed;
            border: 1px solid #ffedd5;
            padding: 15px;
            border-radius: 15px;
            margin-bottom: 20px;
            color: #9a3412;
            font-size: 0.9rem;
            display: flex;
            gap: 10px;
            align-items: center;
            text-align: left;
        }

        @keyframes shake {
            0%, 100% { transform: translateX(0); }
            25% { transform: translateX(-8px); }
            75% { transform: translateX(8px); }
        }
        .shake-active { animation: shake 0.5s ease-in-out; }
    </style>
</head>
<body style="background: #fdf2f2;">

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
</nav>

<main style="display: flex; justify-content: center; align-items: center; min-height: 85vh; padding: 20px;">
    <section id="deleteCard" class="danger-zone">
        
        <div style="color: #dc2626; font-size: 2.5rem; margin-bottom: 15px;">
            <i class="fa-solid fa-triangle-exclamation"></i>
        </div>
        
        <h2 style="color: #111827; margin-bottom: 10px; font-weight: 800;">Eliminar Producto</h2>

        <% if (tieneMovimientos) { %>
            <div class="info-lock">
                <i class="fa-solid fa-lock" style="font-size: 1.2rem;"></i>
                <p>Este producto <b>tiene historial de movimientos</b> (ventas o facturas) y no puede ser eliminado para proteger la integridad de los datos.</p>
            </div>
        <% } %>

        <div class="img-container">
            <img src="<%= request.getContextPath() %>/imagen-producto/<%= producto.getProductoId() %>" 
                 alt="<%= producto.getNombre() %>" 
                 class="product-preview-del"
                 onerror="this.src='<%= request.getContextPath() %>/assets/Imagenes/iconos/joya-default.png';">
            
            <% if (producto.getStock() > 0) { %>
                <div class="status-badge">Stock: <%= producto.getStock() %></div>
            <% } %>
        </div>

        <div style="margin-bottom: 30px;">
            <h3 style="font-size: 1.4rem; color: #111827; margin: 0;"><%= producto.getNombre() %></h3>
            <span style="color: #9ca3af; font-size: 0.85rem;">ID: #<%= producto.getProductoId() %></span>
        </div>

        <button type="button" class="btn-danger-custom" 
                onclick="procesarEliminacion()" 
                <%= tieneMovimientos ? "disabled" : "" %>>
            <i class="fa-solid fa-trash-can"></i> 
            <%= tieneMovimientos ? "Eliminación Bloqueada" : "Confirmar y Eliminar" %>
        </button>
        
        <button style="background:none; border:none; color:#6b7280; margin-top:15px; cursor:pointer; text-decoration:underline;" 
                onclick="window.history.back()">
            Volver atrás
        </button>
    </section>
</main>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
async function procesarEliminacion() {
    const stock = <%= producto.getStock() %>;
    const movimientos = <%= tieneMovimientos %>;
    const card = document.getElementById('deleteCard');

    // 1. Validar Movimientos (Doble seguridad)
    if (movimientos) {
        Swal.fire({
            icon: 'error',
            title: 'Bloqueo de Seguridad',
            text: 'El historial de este producto es necesario para la contabilidad. No es posible eliminarlo.',
            confirmButtonColor: '#94a3b8'
        });
        return;
    }

    // 2. Validar Stock
    if (stock > 0) {
        card.classList.add('shake-active');
        setTimeout(() => card.classList.remove('shake-active'), 500);
        Swal.fire({
            icon: 'warning',
            title: 'Producto con Stock',
            text: `Aún tienes ${stock} unidades. Debes vaciar el inventario antes de desactivarlo, no puedes eliminarlo ya que tiene movimientos de inventario.`,
            confirmButtonColor: '#dc2626'
        });
        return;
    }

    // 3. Confirmación Final
    const confirm = await Swal.fire({
        title: '¿Confirmar borrado definitivo?',
        text: "Esta acción borrará el registro de la base de datos.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#dc2626',
        confirmButtonText: 'Sí, borrar',
        cancelButtonText: 'Cancelar'
    });

    if (confirm.isConfirmed) {
        Swal.fire({ title: 'Eliminando...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });

        const params = new URLSearchParams();
        params.append('action', 'eliminar');
        params.append('id', '<%= producto.getProductoId() %>');

        try {
            await fetch('<%=request.getContextPath()%>/ProductoServlet', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params
            });

            Swal.fire({
                icon: 'success',
                title: '¡Eliminado!',
                text: 'El producto ha sido retirado.',
                confirmButtonColor: '#7c3aed'
            }).then(() => {
                window.location.href = '<%= request.getContextPath() %>/CategoriaServlet?id=<%= producto.getCategoriaId() %>';
            });
        } catch (e) {
            Swal.fire('Error', 'No se pudo completar la acción.', 'error');
        }
    }
}
</script>
</body>
</html>