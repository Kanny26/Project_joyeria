<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Auditoría | AAC27</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/forms-global.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/auditoria_log.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/html2pdf.js/0.10.1/html2pdf.bundle.min.js"></script>
</head>
<body>

<!-- ══════════ NAVBAR ══════════ -->
<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin">
        <h2>Volver al inicio</h2>
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%=request.getContextPath()%>/CerrarSesionServlet" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-right-from-bracket"></i>
            <span class="navbar-admin__home-text">Cerrar sesión</span>
        </span>
    </a>
</nav>


<div class="container">
    <!-- Welcome Section -->
    <div class="welcome-section">
        <h2>Registro de Auditoría</h2>
        <p>Historial completo de todas las acciones realizadas en el sistema.</p>
    </div>

    <!-- Stats Cards -->
    <div class="stats-grid">
        <div class="stat-card">
            <h3 id="totalAcciones">0</h3>
            <p>Total de Acciones</p>
        </div>
        <div class="stat-card">
            <h3 id="usuariosActivos">0</h3>
            <p>Usuarios Activos</p>
        </div>
        <div class="stat-card">
            <h3 id="accionesHoy">0</h3>
            <p>Acciones Hoy</p>
        </div>
        <div class="stat-card">
            <h3 id="eventosCriticos">0</h3>
            <p>Eventos Críticos</p>
        </div>
    </div>

    <!-- Filters -->
    <div class="filters-section">
        <div class="filters-header">
            <h3>Filtros de Búsqueda</h3>
        </div>
        <div class="filters-body">
            <div class="filter-group">
                <label>USUARIO</label>
                <input type="text" id="filterUsuario" placeholder="Filtrar por usuario...">
            </div>
            <button class="btn-clear" id="btnClearFilters">
                <i class="fas fa-eraser"></i> Limpiar
            </button>
        </div>
    </div>

    <!-- Table -->
    <div class="table-section">
        <div class="table-header">
            <h3><i class="fas fa-list-ul"></i> Historial de Eventos</h3>
            <div class="table-actions">
                <button class="btn-pdf" id="btnPDF">
                    <i class="fas fa-file-pdf"></i> Exportar PDF
                </button>
                <button class="btn-refresh" id="btnRefresh">
                    <i class="fas fa-sync-alt"></i>
                </button>
            </div>
        </div>
        <div class="table-wrapper" id="pdfContent">
            <table class="audit-table" id="auditTable">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Usuario</th>
                        <th>Acción</th>
                        <th>Entidad</th>
                        <th>ID Entidad</th>
                        <th>Datos Anteriores</th>
                        <th>Datos Nuevos</th>
                        <th>IP</th>
                        <th>Fecha/Hora</th>
                    </tr>
                </thead>
                <tbody>
                    <% 
                        List<Map<String, Object>> logs = (List<Map<String, Object>>) request.getAttribute("logs");
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        
                        if (logs != null && !logs.isEmpty()) {
                            for (Map<String, Object> log : logs) {
                                Integer logId = (Integer) log.get("log_id");
                                String usuarioNombre = (String) log.get("usuario_nombre");
                                String accion = (String) log.get("accion");
                                String entidad = (String) log.get("entidad");
                                Object entidadIdObj = log.get("entidad_id");
                                String entidadId = (entidadIdObj != null) ? entidadIdObj.toString() : "-";
                                String datosAnteriores = (String) log.get("datos_anteriores");
                                String datosNuevos = (String) log.get("datos_nuevos");
                                String direccionIp = (String) log.get("direccion_ip");
                                Object fechaHoraObj = log.get("fecha_hora");
                                String fechaHora = (fechaHoraObj != null) ? sdf.format((Date) fechaHoraObj) : "-";
                                
                                // Escapar caracteres para JSON
                                String datosAnterioresEscapados = "";
                                String datosNuevosEscapados = "";
                                if (datosAnteriores != null && !datosAnteriores.trim().isEmpty() && !"null".equals(datosAnteriores)) {
                                    datosAnterioresEscapados = datosAnteriores.replace("\\", "\\\\").replace("'", "\\'");
                                }
                                if (datosNuevos != null && !datosNuevos.trim().isEmpty() && !"null".equals(datosNuevos)) {
                                    datosNuevosEscapados = datosNuevos.replace("\\", "\\\\").replace("'", "\\'");
                                }
                    %>
                        <tr class="log-row" data-accion="<%= accion != null ? accion : "" %>">
                            <td class="log-id"><%= logId %></td>
                            <td>
                                <div class="usuario-badge">
                                    <i class="fas fa-user-circle"></i>
                                    <span><%= usuarioNombre != null ? usuarioNombre : "Anónimo" %></span>
                                </div>
                            </td>
                            <td>
                                <% if (accion != null) { 
                                    String actionClass = "";
                                    if (accion.contains("CREAR") || accion.contains("EXITOSO") || accion.contains("CREADO")) actionClass = "action-CREAR";
                                    else if (accion.contains("EDITAR") || accion.contains("CAMBIADA")) actionClass = "action-EDITAR";
                                    else if (accion.contains("FALLIDO")) actionClass = "action-LOGIN-FALLIDO";
                                    else actionClass = "action-CREAR";
                                %>
                                    <span class="action-badge <%= actionClass %>">
                                        <% if ("LOGIN_EXITOSO".equals(accion)) { %>
                                            <i class="fas fa-sign-in-alt"></i> Login Exitoso
                                        <% } else if ("LOGIN_FALLIDO".equals(accion)) { %>
                                            <i class="fas fa-times-circle"></i> Login Fallido
                                        <% } else if ("PRODUCTO_CREADO".equals(accion)) { %>
                                            <i class="fas fa-plus-circle"></i> Producto Creado
                                        <% } else if ("PRODUCTO_EDITADO".equals(accion)) { %>
                                            <i class="fas fa-edit"></i> Producto Editado
                                        <% } else if ("VENTA_CREADA".equals(accion)) { %>
                                            <i class="fas fa-shopping-cart"></i> Venta Creada
                                        <% } else if ("PASSWORD_CAMBIADA".equals(accion)) { %>
                                            <i class="fas fa-key"></i> Password Cambiada
                                        <% } else { %>
                                            <%= accion %>
                                        <% } %>
                                    </span>
                                <% } else { %>
                                    <span class="null-data">-</span>
                                <% } %>
                            </td>
                            <td><span class="entidad-badge"><%= entidad != null ? entidad : "-" %></span></td>
                            <td><%= entidadId %></td>
                            <td>
                                <% if (datosAnteriores != null && !datosAnteriores.trim().isEmpty() && !"null".equals(datosAnteriores)) { %>
                                    <button class="btn-view-json" data-json='<%= datosAnterioresEscapados %>'>
                                        <i class="fas fa-eye"></i> Ver
                                    </button>
                                <% } else { %>
                                    <span class="null-data">-</span>
                                <% } %>
                            </td>
                            <td>
                                <% if (datosNuevos != null && !datosNuevos.trim().isEmpty() && !"null".equals(datosNuevos)) { %>
                                    <button class="btn-view-json" data-json='<%= datosNuevosEscapados %>'>
                                        <i class="fas fa-eye"></i> Ver
                                    </button>
                                <% } else { %>
                                    <span class="null-data">-</span>
                                <% } %>
                            </td>
                            <td class="log-ip"><code><%= direccionIp != null ? direccionIp : "-" %></code></td>
                            <td class="log-fecha"><%= fechaHora %></td>
                        </tr>
                    <% 
                            }
                        } else { 
                    %>
                        <tr>
                            <td colspan="9">
                                <div class="empty-state">
                                    <i class="fas fa-inbox"></i>
                                    <p>No hay registros de auditoría disponibles</p>
                                </div>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Modal -->
<div id="jsonModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <h3><i class="fas fa-code"></i> Datos JSON</h3>
            <button class="modal-close">&times;</button>
        </div>
        <div class="modal-body">
            <pre id="jsonContent" class="json-pretty"></pre>
        </div>
    </div>
</div>

<script>
(function() {
    // Update stats
    function updateStats() {
        const rows = document.querySelectorAll('.log-row');
        const totalAcciones = rows.length;
        const usuariosSet = new Set();
        const hoy = new Date().toDateString();
        let accionesHoy = 0;
        let eventosCriticos = 0;
        
        rows.forEach(row => {
            const usuarioSpan = row.querySelector('.usuario-badge span');
            const usuario = usuarioSpan ? usuarioSpan.innerText : '';
            if (usuario && usuario !== 'Anónimo') usuariosSet.add(usuario);
            
            const fechaCell = row.querySelector('.log-fecha');
            if (fechaCell) {
                const fechaStr = fechaCell.innerText;
                if (fechaStr && fechaStr !== '-') {
                    try {
                        const fechaPart = fechaStr.split(' ')[0];
                        const partes = fechaPart.split('/');
                        const fechaRow = new Date(partes[2] + '-' + partes[1] + '-' + partes[0]);
                        if (fechaRow.toDateString() === hoy) accionesHoy++;
                    } catch(e) {}
                }
            }
            
            const accion = row.getAttribute('data-accion');
            if (accion === 'LOGIN_FALLIDO') eventosCriticos++;
        });
        
        document.getElementById('totalAcciones').textContent = totalAcciones;
        document.getElementById('usuariosActivos').textContent = usuariosSet.size;
        document.getElementById('accionesHoy').textContent = accionesHoy;
        document.getElementById('eventosCriticos').textContent = eventosCriticos;
    }
    
    // Filter table
    function filterTable() {
        const filterUsuario = document.getElementById('filterUsuario') ? document.getElementById('filterUsuario').value.toLowerCase() : '';
        const filterAccion = document.getElementById('filterAccion') ? document.getElementById('filterAccion').value : '';
        
        const rows = document.querySelectorAll('.log-row');
        
        rows.forEach(row => {
            let show = true;
            
            const usuarioSpan = row.querySelector('.usuario-badge span');
            const usuario = usuarioSpan ? usuarioSpan.innerText.toLowerCase() : '';
            if (filterUsuario && !usuario.includes(filterUsuario)) show = false;
            
            const accion = row.getAttribute('data-accion') || '';
            if (filterAccion && accion !== filterAccion) show = false;
            
            row.style.display = show ? '' : 'none';
        });
    }
    
    function clearFilters() {
        const filterUsuario = document.getElementById('filterUsuario');
        const filterAccion = document.getElementById('filterAccion');
        if (filterUsuario) filterUsuario.value = '';
        if (filterAccion) filterAccion.value = '';
        filterTable();
    }
    
    // Modal functions
    function showJson(jsonStr) {
        const modal = document.getElementById('jsonModal');
        const content = document.getElementById('jsonContent');
        if (!modal || !content) return;
        
        try {
            const parsed = JSON.parse(jsonStr);
            content.textContent = JSON.stringify(parsed, null, 2);
        } catch(e) {
            content.textContent = jsonStr;
        }
        modal.classList.add('show');
    }
    
    function closeModal() {
        const modal = document.getElementById('jsonModal');
        if (modal) modal.classList.remove('show');
    }
    
    // Export PDF
    function exportToPDF() {
        const originalTitle = document.title;
        document.title = 'Auditoria_AAC27_' + new Date().toLocaleString();
        
        // Ocultar elementos no deseados para el PDF
        const navbar = document.querySelector('.navbar-admin');
        const filtersSection = document.querySelector('.filters-section');
        const statsGrid = document.querySelector('.stats-grid');
        const tableActions = document.querySelector('.table-actions');
        const modalElement = document.getElementById('jsonModal');
        
        if (navbar) navbar.style.display = 'none';
        if (filtersSection) filtersSection.style.display = 'none';
        if (statsGrid) statsGrid.style.display = 'none';
        if (tableActions) tableActions.style.display = 'none';
        if (modalElement) modalElement.style.display = 'none';
        
        // Crear un título para el PDF
        const pdfTitle = document.createElement('div');
        pdfTitle.className = 'pdf-title';
        pdfTitle.innerHTML = '<h1>AAC27 - Registro de Auditoría</h1>' +
            '<p>Fecha de exportación: ' + new Date().toLocaleString() + '</p>' +
            '<hr>';
        document.querySelector('.container').insertBefore(pdfTitle, document.querySelector('.table-section'));
        
        // Configurar opciones para PDF
        const element = document.getElementById('pdfContent');
        const opt = {
            margin: [0.5, 0.5, 0.5, 0.5],
            filename: 'Auditoria_AAC27_' + new Date().toISOString().slice(0,19).replace(/:/g, '-') + '.pdf',
            image: { type: 'jpeg', quality: 0.98 },
            html2canvas: { scale: 2, letterRendering: true },
            jsPDF: { unit: 'in', format: 'a4', orientation: 'landscape' }
        };
        
        // Generar PDF
        html2pdf().set(opt).from(element).save()
            .then(function() {
                // Restaurar elementos
                if (navbar) navbar.style.display = '';
                if (filtersSection) filtersSection.style.display = '';
                if (statsGrid) statsGrid.style.display = '';
                if (tableActions) tableActions.style.display = '';
                if (modalElement) modalElement.style.display = '';
                var pdfTitleElement = document.querySelector('.pdf-title');
                if (pdfTitleElement) pdfTitleElement.remove();
                document.title = originalTitle;
            })
            .catch(function(err) {
                console.error('Error al generar PDF:', err);
                if (navbar) navbar.style.display = '';
                if (filtersSection) filtersSection.style.display = '';
                if (statsGrid) statsGrid.style.display = '';
                if (tableActions) tableActions.style.display = '';
                if (modalElement) modalElement.style.display = '';
                var pdfTitleElement = document.querySelector('.pdf-title');
                if (pdfTitleElement) pdfTitleElement.remove();
                document.title = originalTitle;
                alert('Error al generar el PDF. Por favor, intente nuevamente.');
            });
    }
    
    // Event listeners
    var filterUsuario = document.getElementById('filterUsuario');
    var filterAccion = document.getElementById('filterAccion');
    var btnClear = document.getElementById('btnClearFilters');
    var btnRefresh = document.getElementById('btnRefresh');
    var btnPDF = document.getElementById('btnPDF');
    
    if (filterUsuario) filterUsuario.addEventListener('input', filterTable);
    if (filterAccion) filterAccion.addEventListener('change', filterTable);
    if (btnClear) btnClear.addEventListener('click', clearFilters);
    if (btnRefresh) btnRefresh.addEventListener('click', function() { location.reload(); });
    if (btnPDF) btnPDF.addEventListener('click', exportToPDF);
    
    // JSON buttons delegation
    var tableWrapper = document.querySelector('.table-wrapper');
    if (tableWrapper) {
        tableWrapper.addEventListener('click', function(e) {
            var btn = e.target.closest('.btn-view-json');
            if (btn) {
                e.stopPropagation();
                var json = btn.getAttribute('data-json');
                if (json) showJson(json);
            }
        });
    }
    
    var modalClose = document.querySelector('.modal-close');
    if (modalClose) modalClose.addEventListener('click', closeModal);
    
    var jsonModal = document.getElementById('jsonModal');
    if (jsonModal) jsonModal.addEventListener('click', function(e) {
        if (e.target === this) closeModal();
    });
    
    updateStats();
})();
</script>
</body>
</html>