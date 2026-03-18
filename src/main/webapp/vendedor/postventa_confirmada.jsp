<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.CasoPostventa" %>
<%
    /*
     * Control de sesión.
     */
    Object vendedorSesion = session.getAttribute("vendedor");
    if (vendedorSesion == null) {
        response.sendRedirect(request.getContextPath() + "/vendedor/inicio-sesion.jsp");
        return;
    }

    /*
     * El servlet pone estos atributos en el request antes de hacer forward a esta página:
     *   - "mensaje": texto descriptivo del resultado de la operación
     *   - "caso": el objeto CasoPostventa recién guardado, para mostrar el resumen
     */
    String mensaje = (String) request.getAttribute("mensaje");
    CasoPostventa caso = (CasoPostventa) request.getAttribute("caso");
    if (mensaje == null) mensaje = "Caso registrado correctamente";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Caso Registrado | AAC27</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Vendedor/registrar_venta.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/Seller.png" alt="Vendedor">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
</nav>

<main class="prov-page">
    <div class="form-card confirmacion-card" style="text-align:center;">
        <%-- Ícono de éxito --%>
        <div style="font-size:64px;color:#7c3aed;margin-bottom:1rem;">
            <i class="fa-solid fa-clipboard-check"></i>
        </div>
        <h2 style="font-size:1.4rem;font-weight:800;color:#1e1b4b;margin-bottom:.5rem;"><%= mensaje %></h2>
        <p style="color:#6b7280;font-size:14px;margin-bottom:1.5rem;">
            El caso fue enviado y será revisado por el administrador. Puedes ver su estado en "Mis casos postventa".
        </p>

        <%-- Resumen del caso registrado (solo si el objeto está disponible) --%>
        <% if (caso != null) { %>
        <div class="info-grid" style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;margin-bottom:1.5rem;">
            <div class="info-item" style="background:#f5f3ff;border-radius:12px;padding:1rem;">
                <div style="font-size:11px;font-weight:700;color:#7c3aed;text-transform:uppercase;margin-bottom:4px;">Caso #</div>
                <div style="font-size:1.2rem;font-weight:800;color:#1e1b4b;"><%= caso.getCasoId() %></div>
            </div>
            <div class="info-item" style="background:#f5f3ff;border-radius:12px;padding:1rem;">
                <div style="font-size:11px;font-weight:700;color:#7c3aed;text-transform:uppercase;margin-bottom:4px;">Tipo</div>
                <div style="font-size:1rem;font-weight:700;color:#1e1b4b;">
                    <% if ("cambio".equals(caso.getTipo())) { %>🔄 Cambio
                    <% } else if ("devolucion".equals(caso.getTipo())) { %>↩️ Devolución
                    <% } else { %>⚠️ Reclamo<% } %>
                </div>
            </div>
            <div class="info-item" style="background:#fef9c3;border-radius:12px;padding:1rem;">
                <div style="font-size:11px;font-weight:700;color:#ca8a04;text-transform:uppercase;margin-bottom:4px;">Estado inicial</div>
                <div style="font-size:1rem;font-weight:700;color:#92400e;">🕐 En proceso</div>
            </div>
            <div class="info-item" style="background:#f5f3ff;border-radius:12px;padding:1rem;">
                <div style="font-size:11px;font-weight:700;color:#7c3aed;text-transform:uppercase;margin-bottom:4px;">Venta relacionada</div>
                <div style="font-size:1rem;font-weight:700;color:#1e1b4b;">#<%= caso.getVentaId() %></div>
            </div>
            <% if (caso.getCantidad() > 0) { %>
            <div class="info-item" style="background:#f5f3ff;border-radius:12px;padding:1rem;grid-column:span 2;">
                <div style="font-size:11px;font-weight:700;color:#7c3aed;text-transform:uppercase;margin-bottom:4px;">Cantidad afectada</div>
                <div style="font-size:1rem;font-weight:700;color:#1e1b4b;"><%= caso.getCantidad() %> unidad(es)</div>
            </div>
            <% } %>
        </div>
        <% } %>

        <div class="form-actions" style="justify-content:center;gap:1rem;">
            <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=misCasos" class="btn-save">
                <i class="fa-solid fa-list"></i> Ver mis casos
            </a>
            <a href="<%= request.getContextPath() %>/VentaVendedorServlet?action=misVentas" class="btn-cancel">
                <i class="fa-solid fa-receipt"></i> Mis ventas
            </a>
        </div>
    </div>
</main>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
// Muestra un toast de éxito al cargar la página
window.addEventListener('load', function() {
    Swal.fire({
        toast: true,
        position: 'top-end',
        icon: 'success',
        title: 'Caso postventa registrado',
        showConfirmButton: false,
        timer: 3000,
        timerProgressBar: true
    });
});
</script>
</body>
</html>
