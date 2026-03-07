<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.text.SimpleDateFormat, java.text.NumberFormat, java.util.Locale" %>
<%@ page import="model.CasoPostventa, model.Venta, model.DetalleVenta" %>
<%
    Object adminSesion = session.getAttribute("admin");
    if (adminSesion == null) {
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
        return;
    }
    CasoPostventa caso = (CasoPostventa) request.getAttribute("caso");
    Venta venta = (Venta) request.getAttribute("venta");
    String exito = (String) request.getAttribute("exito");
    String error = (String) request.getAttribute("error");
    NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Caso Postventa #<%= caso != null ? caso.getCasoId() : "" %> | Admin</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/forms-global.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/ventas/ver_caso_postventa.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
    <nav class="navbar-admin">
        <div class="navbar-admin__catalogo">
            <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
        </div>
        <h1 class="navbar-admin__title">AAC27</h1>
        <a href="<%= request.getContextPath() %>/Administrador/postventa/listar">
            <i class="fa-solid fa-arrow-left navbar-admin__home-icon"></i>
        </a>
    </nav>

    <main class="prov-page">
        <% if (caso == null) { %>
            <div class="alert alert--error">No se encontró el caso postventa.</div>
        <% } else { %>
            <% if (exito != null) { %>
                <div class="alert alert--success">
                    <i class="fa-solid fa-circle-check"></i> <%= exito %>
                </div>
            <% } %>
            <% if (error != null) { %>
                <div class="alert alert--error">
                    <i class="fa-solid fa-circle-xmark"></i> <%= error %>
                </div>
            <% } %>

            <div class="form-card">
                <div class="form-card__title">
                    <i class="fa-solid fa-rotate-left"></i> 
                    Caso Postventa #<%= caso.getCasoId() %> &nbsp;
                    <% 
                        String est = caso.getEstado() != null ? caso.getEstado() : "en_proceso";
                        if ("aprobado".equals(est)) { 
                    %>
                        <span class="badge badge--success">■ Aprobado</span>
                    <% } else if ("cancelado".equals(est)) { %>
                        <span class="badge badge--danger">■ Cancelado</span>
                    <% } else { %>
                        <span class="badge badge--warning">■ En proceso</span>
                    <% } %>
                </div>

                <!-- Datos del caso -->
                <div class="info-grid">
                    <div class="info-item">
                        <span class="info-label">Vendedor</span>
                        <span class="info-value"><%= caso.getVendedorNombre() %></span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Cliente</span>
                        <span class="info-value"><%= caso.getClienteNombre() %></span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Venta asociada</span>
                        <span class="info-value">#<%= caso.getVentaId() %></span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Producto</span>
                        <span class="info-value"><%= caso.getProductoNombre() != null ? caso.getProductoNombre() : "—" %></span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Tipo</span>
                        <span class="info-value">
                            <% if ("cambio".equals(caso.getTipo())) { %>
                                <span class="badge badge--info">■ Cambio</span>
                            <% } else if ("devolucion".equals(caso.getTipo())) { %>
                                <span class="badge badge--warning">■■ Devolución</span>
                            <% } else { %>
                                <span class="badge badge--danger">■■ Reclamo</span>
                            <% } %>
                        </span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Cantidad</span>
                        <span class="info-value"><%= caso.getCantidad() %></span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Fecha</span>
                        <span class="info-value"><%= caso.getFecha() != null ? sdf.format(caso.getFecha()) : "—" %></span>
                    </div>
                    <% if (caso.getMotivo() != null && !caso.getMotivo().isBlank()) { %>
                        <div class="info-item" style="grid-column: 1 / -1;">
                            <span class="info-label">Motivo</span>
                            <span class="info-value"><%= caso.getMotivo() %></span>
                        </div>
                    <% } %>
                </div>

                <!-- Gestión del caso (solo si está en proceso) -->
                <% if ("en_proceso".equals(est) || est == null) { %>
                    <div class="section-title">
                        <i class="fa-solid fa-gear"></i> Gestionar caso
                    </div>
                    <form method="post" action="<%= request.getContextPath() %>/Administrador/postventa/gestionar">
                        <input type="hidden" name="casoId" value="<%= caso.getCasoId() %>">
                        
                        <div class="form-group">
                            <label class="form-label">Nuevo estado</label>
                            <select name="nuevoEstado" class="form-control" required>
                                <option value="">-- Seleccionar --</option>
                                <option value="aprobado">■ Aprobar</option>
                                <option value="cancelado">■ Cancelar</option>
                            </select>
                        </div>
                        
                        <div class="form-group">
                            <label class="form-label">Observaciones</label>
                            <textarea name="observacion" rows="3" class="form-control" 
                                      placeholder="Agrega observaciones sobre esta decisión..."></textarea>
                        </div>
                        
                        <!-- ■■ RF31: Advertencia para devoluciones ■■ -->
                        <% if ("devolucion".equals(caso.getTipo())) { %>
                            <div style="background:#fef3c7;border-left:4px solid #f59e0b;padding:12px;margin:16px 0;border-radius:4px;">
                                <i class="fa-solid fa-triangle-exclamation" style="color:#92400e;"></i>
                                <strong style="color:#92400e;">Atención:</strong>
                                <span style="color:#92400e;">Al aprobar esta devolución, el stock del producto se incrementará automáticamente.</span>
                            </div>
                        <% } %>
                        
                        <div class="form-actions">
                            <button type="submit" class="btn-save">
                                <i class="fa-solid fa-floppy-disk"></i> Guardar decisión
                            </button>
                            <a href="<%= request.getContextPath() %>/Administrador/postventa/listar" class="btn-cancel">
                                Volver
                            </a>
                        </div>
                    </form>
                <% } else { %>
                    <div class="form-actions">
                        <a href="<%= request.getContextPath() %>/Administrador/postventa/listar" class="btn-cancel">
                            <i class="fa-solid fa-arrow-left"></i> Volver al listado
                        </a>
                    </div>
                <% } %>
            </div>
        <% } %>
    </main>
</body>
</html>