<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="model.Material, java.util.List"%>
<%
    Object admin = session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }
    
    List<Material> materiales = (List<Material>) request.getAttribute("materiales");
    if (materiales == null) materiales = java.util.Collections.emptyList();
    
    String error = (String) request.getAttribute("error");
    model.Proveedor p = (model.Proveedor) request.getAttribute("proveedor");
    List<String> telefonos = (List<String>) request.getAttribute("telefonos");
    List<String> correos = (List<String>) request.getAttribute("correos");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Agregar Proveedor - AAC27</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/producto.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%=request.getContextPath()%>/ProveedorServlet?action=listar" class="navbar-admin__home-link">
	   
	    <span class="navbar-admin__home-icon-wrap">
	        <i class="fa-solid fa-arrow-left"></i>
		    <span class="navbar-admin__home-text">Volver atras</span>
		    <i class="fa-solid fa-house-chimney"></i>
	    </span>
    </a>
</nav>

<main class="form-product-container">
    <h2 class="form-product-container__title">Registrar Nuevo Proveedor</h2>
    
    <% if (error != null) { %>
        <div class="alert-error">
            <i class="fa-solid fa-circle-exclamation"></i> <%= error %>
        </div>
    <% } %>
    
    <form id="formProveedor" class="form-product" method="post" 
          action="<%=request.getContextPath()%>/ProveedorServlet" novalidate>
        <input type="hidden" name="action" value="guardar">
        
        <div class="form-product__row">
            <!-- Nombre -->
            <div class="form-product__group">
                <label class="form-product__label" for="nombre">Nombre del Proveedor *</label>
                <div class="input-wrap">
                    <input id="nombre" type="text" name="nombre" class="form-product__input" 
                           required value="<%= p != null && p.getNombre() != null ? p.getNombre() : "" %>">
                    <div class="bubble-error" id="err-nombre">
                        <span class="bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                        <span>El nombre es obligatorio.</span>
                    </div>
                </div>
            </div>
            
            <!-- Documento -->
            <div class="form-product__group">
                <label class="form-product__label" for="documento">Documento de Identidad *</label>
                <div class="input-wrap">
                    <input id="documento" type="text" name="documento" class="form-product__input" 
                           required value="<%= p != null && p.getDocumento() != null ? p.getDocumento() : "" %>">
                    <div class="bubble-error" id="err-documento">
                        <span class="bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                        <span>El documento es obligatorio.</span>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="form-product__row">
            <!-- Fecha Inicio -->
            <div class="form-product__group">
                <label class="form-product__label" for="fechaInicio">Fecha de Inicio *</label>
                <div class="input-wrap">
                    <input id="fechaInicio" type="date" name="fechaInicio" class="form-product__input" 
                           required value="<%= p != null && p.getFechaInicio() != null ? p.getFechaInicio() : "" %>">
                    <div class="bubble-error" id="err-fechaInicio">
                        <span class="bubble-icon"><i class="fa-solid fa-circle-exclamation"></i></span>
                        <span>Selecciona una fecha de inicio.</span>
                    </div>
                </div>
            </div>
            
            <!-- Mínimo Compra (RF38) -->
            <div class="form-product__group">
                <label class="form-product__label" for="minimoCompra">Monto Mínimo de Compra</label>
                <div class="input-wrap">
                    <input id="minimoCompra" type="number" name="minimoCompra" class="form-product__input" 
                           min="0" step="0.01" placeholder="0.00"
                           value="<%= p != null && p.getMinimoCompra() != null ? p.getMinimoCompra() : "" %>">
                    <small class="form-help">Monto mínimo para compras a este proveedor (RF38).</small>
                </div>
            </div>
        </div>
        
        <div class="form-product__row">
            <!-- Teléfonos (múltiples) -->
            <div class="form-product__group">
                <label class="form-product__label">Teléfonos *</label>
                <div id="telefonos-container">
                    <% 
                    if (telefonos != null && !telefonos.isEmpty()) {
                        for (int i = 0; i < telefonos.size(); i++) { 
                    %>
                    <div class="telefono-row">
                        <input type="tel" name="telefono" class="form-product__input" 
                               placeholder="Ej: 3001234567" 
                               value="<%= telefonos.get(i) != null ? telefonos.get(i) : "" %>" required>
                        <% if (i > 0) { %>
                        <button type="button" class="btn-remove" onclick="this.parentElement.remove()">
                            <i class="fa-solid fa-minus"></i>
                        </button>
                        <% } %>
                    </div>
                    <% } } else { %>
                    <div class="telefono-row">
                        <input type="tel" name="telefono" class="form-product__input" 
                               placeholder="Ej: 3001234567" required>
                    </div>
                    <% } %>
                </div>
                <button type="button" class="btn-add" onclick="agregarTelefono()">
                    <i class="fa-solid fa-plus"></i> Agregar teléfono
                </button>
            </div>
            
            <!-- Correos (múltiples) -->
            <div class="form-product__group">
                <label class="form-product__label">Correos Electrónicos *</label>
                <div id="correos-container">
                    <% 
                    if (correos != null && !correos.isEmpty()) {
                        for (int i = 0; i < correos.size(); i++) { 
                    %>
                    <div class="correo-row">
                        <input type="email" name="correo" class="form-product__input" 
                               placeholder="correo@ejemplo.com" 
                               value="<%= correos.get(i) != null ? correos.get(i) : "" %>" required>
                        <% if (i > 0) { %>
                        <button type="button" class="btn-remove" onclick="this.parentElement.remove()">
                            <i class="fa-solid fa-minus"></i>
                        </button>
                        <% } %>
                    </div>
                    <% } } else { %>
                    <div class="correo-row">
                        <input type="email" name="correo" class="form-product__input" 
                               placeholder="correo@ejemplo.com" required>
                    </div>
                    <% } %>
                </div>
                <button type="button" class="btn-add" onclick="agregarCorreo()">
                    <i class="fa-solid fa-plus"></i> Agregar correo
                </button>
            </div>
        </div>
        
        <div class="form-product__row">
            <!-- Materiales (RF34) -->
            <div class="form-product__group" style="grid-column: span 2;">
                <label class="form-product__label">Materiales que Suministra</label>
                <div class="checkbox-grid">
                    <% for (Material m : materiales) { 
                        boolean seleccionado = false;
                        if (p != null && p.getMateriales() != null) {
                            for (Material pm : p.getMateriales()) {
                                if (pm.getMaterialId().equals(m.getMaterialId())) {
                                    seleccionado = true;
                                    break;
                                }
                            }
                        }
                    %>
                    <label class="checkbox-label">
                        <input type="checkbox" name="materiales" value="<%= m.getMaterialId() %>" 
                               <%= seleccionado ? "checked" : "" %>>
                        <span><%= m.getNombre() %></span>
                    </label>
                    <% } %>
                </div>
            </div>
        </div>
        
        <div class="form-product__row">
            <!-- Estado -->
            <div class="form-product__group">
                <label class="form-product__label">Estado *</label>
                <div class="radio-group">
                    <label><input type="radio" name="estado" value="activo" checked> Activo</label>
                    <label><input type="radio" name="estado" value="inactivo"> Inactivo</label>
                </div>
            </div>
        </div>
        
        <div class="form-product__actions">
            <button type="submit" class="btn-guardar">
                <i class="fa-solid fa-floppy-disk"></i> Guardar Proveedor
            </button>
            <button type="button" class="btn-cancelar" 
                    onclick="window.location.href='<%=request.getContextPath()%>/ProveedorServlet?action=listar'">
                <i class="fa-solid fa-xmark"></i> Cancelar
            </button>
        </div>
    </form>
</main>

<script>
// Funciones para campos dinámicos
function agregarTelefono() {
    const container = document.getElementById('telefonos-container');
    const div = document.createElement('div');
    div.className = 'telefono-row';
    div.innerHTML = `
        <input type="tel" name="telefono" class="form-product__input" placeholder="Ej: 3001234567" required>
        <button type="button" class="btn-remove" onclick="this.parentElement.remove()">
            <i class="fa-solid fa-minus"></i>
        </button>
    `;
    container.appendChild(div);
}

function agregarCorreo() {
    const container = document.getElementById('correos-container');
    const div = document.createElement('div');
    div.className = 'correo-row';
    div.innerHTML = `
        <input type="email" name="correo" class="form-product__input" placeholder="correo@ejemplo.com" required>
        <button type="button" class="btn-remove" onclick="this.parentElement.remove()">
            <i class="fa-solid fa-minus"></i>
        </button>
    `;
    container.appendChild(div);
}

// Validación simplificada (SIN password)
const camposRequeridos = ['nombre', 'documento', 'fechaInicio'];

function esValido(id) {
    const el = document.getElementById(id);
    if (!el) return true;
    if (id === 'correo' || el.type === 'email') {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(el.value);
    }
    return el.value.trim() !== '';
}

function mostrarError(id) {
    const wrap = document.getElementById(id)?.closest('.input-wrap');
    if (wrap) {
        wrap.classList.add('invalid');
        const bubble = wrap.querySelector('.bubble-error');
        if (bubble) bubble.classList.add('visible');
    }
}

function ocultarError(id) {
    const wrap = document.getElementById(id)?.closest('.input-wrap');
    if (wrap) {
        wrap.classList.remove('invalid');
        const bubble = wrap.querySelector('.bubble-error');
        if (bubble) bubble.classList.remove('visible');
    }
}

camposRequeridos.forEach(id => {
    const el = document.getElementById(id);
    if (el) {
        el.addEventListener('input', () => { if (esValido(id)) ocultarError(id); });
        el.addEventListener('blur', () => { if (!esValido(id)) mostrarError(id); });
    }
});

document.getElementById('formProveedor').addEventListener('submit', function(e) {
    let formValido = true;
    
    for (const id of camposRequeridos) {
        const el = document.getElementById(id);
        if (el && !esValido(id)) {
            e.preventDefault();
            mostrarError(id);
            if (formValido) { el.focus(); formValido = false; }
        }
    }
    
    // Validar al menos un teléfono y un correo
    const telefonos = document.querySelectorAll('input[name="telefono"]');
    const correos = document.querySelectorAll('input[name="correo"]');
    
    let tieneTelefonoValido = false, tieneCorreoValido = false;
    telefonos.forEach(input => { if (input.value.trim() !== '') tieneTelefonoValido = true; });
    correos.forEach(input => { if (/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(input.value)) tieneCorreoValido = true; });
    
    if (!tieneTelefonoValido || !tieneCorreoValido) {
        e.preventDefault();
        alert('Debe ingresar al menos un teléfono y un correo válidos.');
        formValido = false;
    }
    
    return formValido;
});
</script>
</body>
</html>