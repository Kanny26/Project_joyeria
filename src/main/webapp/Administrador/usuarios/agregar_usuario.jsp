<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    if (session.getAttribute("admin") == null) { 
        response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp"); 
        return; 
    }
    String errorServidor = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Agregar Usuario - AAC27</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/forms-global.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/producto.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%= request.getContextPath() %>/UsuarioServlet" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-arrow-left"></i>
            <span class="navbar-admin__home-text">Volver atrás</span>
            <i class="fa-solid fa-house-chimney"></i>
        </span>
    </a>
</nav>

<main class="fs-container">
    <h2 class="fs-page-title"><i class="fa-solid fa-user-plus"></i> Añadir Usuario</h2>

    <form id="formUsuario" class="fs-form" method="post" action="<%= request.getContextPath() %>/UsuarioServlet">
        <input type="hidden" name="accion" value="agregar">
        <input type="hidden" id="contrasenaEnvio" name="contrasena" value="VendedorAA27">

        <div class="fs-section">
            <div class="fs-section-title"><i class="fa-solid fa-user"></i> Datos Personales</div>
            <div class="fs-grid">
                <div class="fs-group">
                    <label class="fs-label" for="nombre"><i class="fa-solid fa-user"></i> Nombre *</label>
                    <input id="nombre" type="text" name="nombre" class="fs-input" placeholder="Nombre completo" required autocomplete="off">
                </div>
                <div class="fs-group">
                    <label class="fs-label"><i class="fa-solid fa-id-card"></i> Documento <span style="font-size:0.65rem;text-transform:none;color:#9ca3af;">(opcional)</span></label>
                    <input type="text" name="documento" class="fs-input" placeholder="Documento de identidad">
                </div>
                <div class="fs-group">
                    <label class="fs-label" for="correo"><i class="fa-solid fa-envelope"></i> Correo *</label>
                    <input id="correo" type="email" name="correo" class="fs-input" placeholder="correo@ejemplo.com" required autocomplete="off">
                    <span class="fs-readonly-badge"><i class="fa-solid fa-envelope-circle-check"></i> Las credenciales se enviarán aquí</span>
                </div>
                <div class="fs-group">
                    <label class="fs-label" for="telefono"><i class="fa-solid fa-phone"></i> Teléfono *</label>
                    <input id="telefono" type="tel" name="telefono" class="fs-input" placeholder="Número de teléfono" required>
                </div>
                <div class="fs-group">
                    <label class="fs-label"><i class="fa-regular fa-calendar"></i> Fecha de Registro</label>
                    <input type="date" name="fechaRegistro" class="fs-input">
                </div>
            </div>
        </div>

        <div class="fs-section">
            <div class="fs-section-title"><i class="fa-solid fa-shield-halved"></i> Rol y Estado</div>
            <div class="fs-grid">
                <div class="fs-group">
                    <label class="fs-label" for="rol"><i class="fa-solid fa-user-gear"></i> Rol *</label>
                    <select id="rol" name="rol" class="fs-input" required>
                        <option value="vendedor">Vendedor</option>
                        <option value="administrador">Administrador</option>
                    </select>
                    <div class="fs-hint">
					    <i class="fa-solid fa-wand-magic-sparkles"></i>
					    <span>Contraseña a generar: <strong id="preview-rol">VendedorAA27</strong></span>
					</div>
                </div>
                <div class="fs-group">
                    <label class="fs-label"><i class="fa-solid fa-toggle-on"></i> Estado</label>
                    <div class="fs-radio-group">
                        <label class="fs-radio-chip">
                            <input type="radio" name="estado" value="Activo" checked>
                            <i class="fa-solid fa-circle-check" style="color:#16a34a;"></i> Activo
                        </label>
                        <label class="fs-radio-chip">
                            <input type="radio" name="estado" value="Inactivo">
                            <i class="fa-solid fa-circle-xmark" style="color:#dc2626;"></i> Inactivo
                        </label>
                    </div>
                </div>
            </div>
            <div class="fs-hint" style="margin-top:14px;">
                <i class="fa-solid fa-circle-info"></i>
                <span>El sistema genera la contraseña automáticamente y la envía al correo del usuario.</span>
            </div>
        </div>

        <div class="fs-actions">
            <button type="button" class="fs-btn-save" id="btnGuardar"><i class="fa-solid fa-user-plus"></i> Crear Usuario</button>
            <button type="button" class="fs-btn-cancel" onclick="history.back()"><i class="fa-solid fa-xmark"></i> Cancelar</button>
        </div>
    </form>
</main>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
<% if (errorServidor != null && !errorServidor.isEmpty()) { %>
document.addEventListener('DOMContentLoaded', () => Swal.fire({ icon:'error', title:'Error', text:'<%= errorServidor.replace("'","\\'") %>' }));
<% } %>

// Mapeo de contraseñas y etiquetas
const PASSWORDS = { 
    vendedor: 'VendedorAA27', 
    administrador: 'AdminAA27' 
};

const LABELS = { 
    vendedor: 'Vendedor', 
    administrador: 'Administrador' 
};

const rolSel  = document.getElementById('rol');
const prevRol = document.getElementById('preview-rol');
const passEnvio = document.getElementById('contrasenaEnvio');

// Actualizar vista previa y el input oculto al cambiar el rol
rolSel.addEventListener('change', function() {
    const pass = PASSWORDS[rolSel.value] || 'N/A';
    prevRol.textContent = pass;
    passEnvio.value = pass;
});

// Validación y envío con SweetAlert2
document.getElementById('btnGuardar').addEventListener('click', function(e) {
    var nombre   = document.getElementById('nombre').value.trim();
    var correo   = document.getElementById('correo').value.trim();
    var telefono = document.getElementById('telefono').value.trim();
    var rolVal   = rolSel.value;

    if (!nombre) {
        Swal.fire({ icon:'warning', title:'Campo requerido', text:'El nombre es obligatorio.' });
        return;
    }
    if (!correo || !/^[^@]+@[^@]+\.[^@]+$/.test(correo)) {
        Swal.fire({ icon:'warning', title:'Correo inválido', text:'Ingresa un correo válido.' });
        return;
    }
    if (!telefono) {
        Swal.fire({ icon:'warning', title:'Campo requerido', text:'El teléfono es obligatorio.' });
        return;
    }

    var passwordFinal = PASSWORDS[rolVal] || "Error";

    Swal.fire({
        icon: 'question',
        title: '¿Confirmar nuevo usuario?',
        html: '<div style="text-align:left;font-size:14px;padding:12px;background:#faf8ff;border-radius:10px;border:1px solid #ede9fe;">' +
              '<p style="margin:6px 0"><strong>Nombre:</strong> ' + nombre + '</p>' +
              '<p style="margin:6px 0"><strong>Correo:</strong> ' + correo + '</p>' +
              '<p style="margin:6px 0"><strong>Teléfono:</strong> ' + telefono + '</p>' +
              '<p style="margin:6px 0"><strong>Rol:</strong> ' + (LABELS[rolVal] || rolVal) + '</p>' +
              '<p style="margin:6px 0"><strong>Contraseña a asignar:</strong> <code style="background:#7c3aed;color:#fff;padding:2px 8px;border-radius:4px;">' + passwordFinal + '</code></p>' +
              '</div>',
        showCancelButton: true,
        confirmButtonText: 'Sí, crear',
        cancelButtonText: 'Revisar',
        confirmButtonColor: '#7c3aed',
        cancelButtonColor: '#6b7280'
    }).then(function(r) {
        if (r.isConfirmed) {
            // Aseguramos el valor final antes de enviar
            passEnvio.value = passwordFinal;
            
            document.getElementById('btnGuardar').disabled = true;
            Swal.fire({
                title: 'Procesando...',
                text: 'Registrando y enviando correo.',
                allowOutsideClick: false,
                didOpen: function() { Swal.showLoading(); }
            });
            document.getElementById('formUsuario').submit();
        }
    });
});
</script>
</body>
</html>