<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Administrador, config.ConexionDB, dao.DashboardDAO, java.math.BigDecimal, java.util.List, java.util.Map" %>
<%
    Administrador admin = null;
    if (session != null) {
        admin = (Administrador) session.getAttribute("admin");
    }
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }

    boolean esTemporal = false;
    try (java.sql.Connection _c = ConexionDB.getConnection();
         java.sql.PreparedStatement _ps = _c.prepareStatement(
             "SELECT pass_temporal FROM Usuario WHERE usuario_id = ?")) {
        _ps.setInt(1, admin.getId());
        java.sql.ResultSet _rs = _ps.executeQuery();
        if (_rs.next()) esTemporal = _rs.getInt("pass_temporal") == 1;
    } catch (Exception _ex) { }

    java.time.LocalDate hoy = java.time.LocalDate.now();
    java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter
        .ofPattern("d 'de' MMMM',' yyyy", new java.util.Locale("es","ES"));
    String fechaHoy = hoy.format(fmt);

    // ── STATS REALES ──
    DashboardDAO dashDAO = new DashboardDAO();
    BigDecimal ingresosMes  = dashDAO.getIngresosMes();
    BigDecimal ventasMes    = dashDAO.getVentasMes();
    int proveedoresActivos  = dashDAO.getProveedoresActivos();
    int totalUsuarios       = dashDAO.getTotalUsuarios();

    // ── NOTIFICACIONES REALES ──
    List<Map<String, String>> notificaciones = dashDAO.getNotificacionesAdmin();

    // Rol de la sesión (para mostrar/ocultar enlace de auditoría)
    String rolSesion = (String) session.getAttribute("rol");
    boolean verAuditoria = "superadministrador".equals(rolSesion) || "administrador".equals(rolSesion);

    // Formatear moneda colombiana
    java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("es","CO"));
    String ingresosFmt  = "$" + nf.format(ingresosMes.setScale(0, java.math.RoundingMode.HALF_UP));
    String ventasFmt    = "$" + nf.format(ventasMes.setScale(0, java.math.RoundingMode.HALF_UP));
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard Admin | AAC27</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/Administrador/admin-principal.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
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

<!-- ══════════ MAIN ══════════ -->
<main class="dash-main">

    <!-- FILA 1: hero + notificaciones -->
    <div class="dash-row dash-row--top">

        <!-- HERO CARD -->
        <div class="hero-card">
            <div class="hero-card__body">
                <h2 class="hero-card__title">¡Hola, <%= admin.getNombre() %>!</h2>
                <p class="hero-card__sub">¿Qué gestionamos hoy?</p>
                <div class="hero-icons">
                    <a href="<%=request.getContextPath()%>/CategoriaServlet" class="hero-icon-item">
                        <div class="icono-boton__circulo">
                            <img class="icono-boton__img" src="<%=request.getContextPath()%>/assets/Imagenes/iconos/catalogar.png" alt="Categorías">
                        </div>
                        <span class="icono-boton__titulo">Categorías</span>
                    </a>
                    <a href="<%=request.getContextPath()%>/ProveedorServlet?accion=listar" class="hero-icon-item">
                        <div class="icono-boton__circulo">
                            <img class="icono-boton__img" src="<%=request.getContextPath()%>/assets/Imagenes/iconos/gestionar_proveedores.png" alt="Proveedores">
                        </div>
                        <span class="icono-boton__titulo">Proveedores</span>
                    </a>
                    <a href="<%=request.getContextPath()%>/Administrador/ventas.jsp" class="hero-icon-item">
                        <div class="icono-boton__circulo">
                            <img class="icono-boton__img" src="<%=request.getContextPath()%>/assets/Imagenes/iconos/ventas.png" alt="Ventas">
                        </div>
                        <span class="icono-boton__titulo">Ventas</span>
                    </a>
                    <a href="<%=request.getContextPath()%>/Administrador/usuarios.jsp" class="hero-icon-item">
                        <div class="icono-boton__circulo">
                            <img class="icono-boton__img" src="<%=request.getContextPath()%>/assets/Imagenes/iconos/Usuarios.png" alt="Usuarios">
                        </div>
                        <span class="icono-boton__titulo">Usuarios</span>
                    </a>
                    <a href="<%=request.getContextPath()%>/Administrador/ayuda.jsp" class="hero-icon-item">
                        <div class="icono-boton__circulo">
                            <img class="icono-boton__img" src="<%=request.getContextPath()%>/assets/Imagenes/iconos/preguntas.png" alt="Ayuda">
                        </div>
                        <span class="icono-boton__titulo">Preguntas</span>
                    </a>
                </div>
            </div>
            <!-- fecha flotante -->
            <span class="hero-card__date"><i class="far fa-calendar-alt"></i> <%= fechaHoy %></span>
        </div>

        <!-- ══ NOTIFICACIONES ══ -->
        <div class="notif-card">
            <div class="notif-card__header">
                <span class="notif-card__title"><i class="far fa-bell"></i> Notificaciones</span>
                <% if (verAuditoria) { %>
                <a href="<%=request.getContextPath()%>/AuditoriaServlet" class="notif-card__all">
                    <i class="fas fa-shield-alt"></i> Ver auditoría
                </a>
                <% } else { %>
                <a href="#" class="notif-card__all">Ver todo</a>
                <% } %>
            </div>

            <%-- Acceso directo al log de auditoría (solo superadmin y admin) --%>
            <% if (verAuditoria) { %>
            <a href="<%=request.getContextPath()%>/AuditoriaServlet" style="text-decoration:none; color:inherit;">
                <div class="notif-item notif-item--lavender" style="cursor:pointer;">
                    <div class="notif-item__ico"><i class="fas fa-shield-alt"></i></div>
                    <p class="notif-item__txt">Log de auditoría — revisa todos los movimientos del sistema.</p>
                    <button class="notif-item__btn"><i class="fas fa-chevron-right"></i></button>
                </div>
            </a>
            <% } %>

            <%-- Notificaciones dinámicas del dashboard --%>
            <%
            if (notificaciones.isEmpty()) {
            %>
            <div class="notif-item notif-item--lavender">
                <div class="notif-item__ico"><i class="fas fa-check-circle"></i></div>
                <p class="notif-item__txt">Todo al día, sin alertas pendientes.</p>
                <button class="notif-item__btn"><i class="fas fa-chevron-right"></i></button>
            </div>
            <%
            } else {
                for (Map<String, String> notif : notificaciones) {
            %>
            <div class="notif-item notif-item--<%= notif.get("tipo") %>">
                <div class="notif-item__ico"><i class="<%= notif.get("icono") %>"></i></div>
                <p class="notif-item__txt"><%= notif.get("texto") %></p>
                <button class="notif-item__btn"><i class="fas fa-chevron-right"></i></button>
            </div>
            <%
                }
            }
            %>
        </div>
        <!-- ══ FIN NOTIFICACIONES ══ -->

    </div>

    <!-- FILA 2: stats -->
    <div class="dash-row dash-row--stats">
        <div class="stat-card">
            <div class="stat-card__ico stat-card__ico--rose"><i class="fas fa-wallet"></i></div>
            <p class="stat-card__lbl">Ingresos del mes</p>
            <p class="stat-card__val"><%= ingresosFmt %></p>
        </div>
        <div class="stat-card">
            <div class="stat-card__ico stat-card__ico--lavender"><i class="fas fa-file-invoice-dollar"></i></div>
            <p class="stat-card__lbl">Ventas del mes</p>
            <p class="stat-card__val"><%= ventasFmt %></p>
        </div>
        <div class="stat-card">
            <div class="stat-card__ico stat-card__ico--amber"><i class="fas fa-truck"></i></div>
            <p class="stat-card__lbl">Proveedores activos</p>
            <p class="stat-card__val"><%= proveedoresActivos %></p>
        </div>
        <div class="stat-card">
            <div class="stat-card__ico stat-card__ico--mint"><i class="fas fa-users"></i></div>
            <p class="stat-card__lbl">Usuarios registrados</p>
            <p class="stat-card__val"><%= totalUsuarios %></p>
        </div>
    </div>

</main>

<!-- ══════════ MODAL CAMBIAR CONTRASEÑA ══════════ -->
<button onclick="abrirModalPass()" title="Cambiar contraseña" class="fab-key">
    <i class="fas fa-key"></i>
</button>

<div id="overlayPass" class="modal-overlay">
    <div class="modal-box">
        <div class="modal-box__header">
            <div class="modal-box__header-left">
                <div class="modal-box__icon"><i class="fas fa-lock"></i></div>
                <div>
                    <p class="modal-box__title">Cambiar contraseña</p>
                    <p class="modal-box__subtitle">Mantén tu cuenta segura</p>
                </div>
            </div>
            <button id="btnXModal" onclick="cerrarModalPass()" class="modal-box__close">
                <i class="fas fa-times"></i>
            </button>
        </div>
        <div id="avisoTemp" class="modal-box__aviso">
            <i class="fas fa-exclamation-triangle"></i>
            <strong> Contraseña temporal.</strong> Debes cambiarla antes de continuar.
        </div>
        <div class="modal-box__body">
            <div id="msgCambio" class="modal-box__msg"></div>
            <div class="modal-field">
                <label>Contraseña actual</label>
                <div class="mfi">
                    <i class="fas fa-lock mfi__ico"></i>
                    <input id="cpActual" type="password" placeholder="Tu contraseña actual"
                           onfocus="this.parentElement.classList.add('mfi--focus')"
                           onblur="this.parentElement.classList.remove('mfi--focus')"/>
                    <button type="button" onclick="toggleOjo('cpActual','ojoA')"><i id="ojoA" class="fas fa-eye"></i></button>
                </div>
            </div>
            <div class="modal-field">
                <label>Nueva contraseña</label>
                <div class="mfi">
                    <i class="fas fa-key mfi__ico"></i>
                    <input id="cpNueva" type="password" placeholder="Mínimo 6 caracteres"
                           oninput="verFuerza(this.value)"
                           onfocus="this.parentElement.classList.add('mfi--focus')"
                           onblur="this.parentElement.classList.remove('mfi--focus')"/>
                    <button type="button" onclick="toggleOjo('cpNueva','ojoN')"><i id="ojoN" class="fas fa-eye"></i></button>
                </div>
                <div class="fuerza-bar"><div id="barraFuerza"></div></div>
                <span id="textoFuerza" class="fuerza-txt"></span>
            </div>
            <div class="modal-field">
                <label>Confirmar nueva contraseña</label>
                <div class="mfi">
                    <i class="fas fa-check-circle mfi__ico"></i>
                    <input id="cpConfirm" type="password" placeholder="Repite la nueva contraseña"
                           onfocus="this.parentElement.classList.add('mfi--focus')"
                           onblur="this.parentElement.classList.remove('mfi--focus')"/>
                    <button type="button" onclick="toggleOjo('cpConfirm','ojoC')"><i id="ojoC" class="fas fa-eye"></i></button>
                </div>
            </div>
            <div class="modal-box__actions">
                <button id="btnCancelarModal" onclick="cerrarModalPass()" class="btn-cancel">Cancelar</button>
                <button id="btnGuardarPass" onclick="enviarCambio()" class="btn-save">
                    <i class="fas fa-save"></i> Actualizar
                </button>
            </div>
        </div>
    </div>
</div>

<script>
var _passTemporal = <%= esTemporal %>;
window.addEventListener('load', function() {
    if (_passTemporal) {
        abrirModalPass();
        document.getElementById('btnXModal').style.display = 'none';
        document.getElementById('btnCancelarModal').style.display = 'none';
        document.getElementById('avisoTemp').style.display = 'flex';
    }
});
function abrirModalPass() {
    document.getElementById('overlayPass').classList.add('active');
    ['cpActual','cpNueva','cpConfirm'].forEach(id => document.getElementById(id).value = '');
    document.getElementById('msgCambio').style.display = 'none';
    document.getElementById('barraFuerza').style.width = '0%';
    document.getElementById('textoFuerza').textContent = '';
}
function cerrarModalPass() {
    if (_passTemporal) return;
    document.getElementById('overlayPass').classList.remove('active');
}
function toggleOjo(inputId, iconId) {
    var inp = document.getElementById(inputId);
    var ico = document.getElementById(iconId);
    inp.type = inp.type === 'password' ? 'text' : 'password';
    ico.className = inp.type === 'password' ? 'fas fa-eye' : 'fas fa-eye-slash';
}
function verFuerza(v) {
    var pts = 0;
    if (v.length >= 6)  pts++;
    if (v.length >= 10) pts++;
    if (/[A-Z]/.test(v) && /[a-z]/.test(v)) pts++;
    if (/[0-9]/.test(v))   pts++;
    if (/[^A-Za-z0-9]/.test(v)) pts++;
    var n = [{w:'0%',c:'#e5e7eb',t:''},{w:'25%',c:'#ef4444',t:'Muy débil'},
             {w:'50%',c:'#f59e0b',t:'Débil'},{w:'65%',c:'#eab308',t:'Regular'},
             {w:'85%',c:'#22c55e',t:'Fuerte'},{w:'100%',c:'#16a34a',t:'Muy fuerte'}][Math.min(pts,5)];
    document.getElementById('barraFuerza').style.cssText = 'width:'+n.w+';background:'+n.c;
    var t = document.getElementById('textoFuerza');
    t.textContent = n.t; t.style.color = n.c;
}
function mostrarMsg(texto, esError) {
    var d = document.getElementById('msgCambio');
    d.style.display = 'block'; d.textContent = texto;
    d.className = 'modal-box__msg ' + (esError ? 'msg--err' : 'msg--ok');
}
function enviarCambio() {
    document.getElementById('msgCambio').style.display = 'none';
    var actual = document.getElementById('cpActual').value.trim();
    var nueva  = document.getElementById('cpNueva').value.trim();
    var conf   = document.getElementById('cpConfirm').value.trim();
    var btn    = document.getElementById('btnGuardarPass');
    if (!actual||!nueva||!conf)   { mostrarMsg('Todos los campos son obligatorios.',true); return; }
    if (nueva !== conf)           { mostrarMsg('Las contraseñas no coinciden.',true); return; }
    if (nueva.length < 6)        { mostrarMsg('Mínimo 6 caracteres.',true); return; }
    if (nueva === actual)         { mostrarMsg('Debe ser diferente a la actual.',true); return; }
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Guardando...';
    var p = new URLSearchParams();
    p.append('passActual',actual); p.append('passNueva',nueva); p.append('passConfirm',conf);
    fetch('<%=request.getContextPath()%>/CambiarPasswordServlet',{method:'POST',
        headers:{'Content-Type':'application/x-www-form-urlencoded'},body:p})
    .then(r=>r.json()).then(data=>{
        mostrarMsg(data.msg,!data.ok);
        if(data.ok){
            _passTemporal=false;
            document.getElementById('btnXModal').style.display='flex';
            document.getElementById('btnCancelarModal').style.display='block';
            document.getElementById('avisoTemp').style.display='none';
            setTimeout(cerrarModalPass,2000);
        }
    }).catch(()=>mostrarMsg('Error de conexión.',true))
    .finally(()=>{btn.disabled=false;btn.innerHTML='<i class="fas fa-save"></i> Actualizar';});
}
document.getElementById('overlayPass').addEventListener('click',function(e){if(e.target===this)cerrarModalPass();});
</script>

</body>
</html>
