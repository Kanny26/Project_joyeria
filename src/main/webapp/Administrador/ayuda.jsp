<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Administrador" %>
<%
    Administrador admin = null;
    if (session != null) {
        admin = (Administrador) session.getAttribute("admin");
    }
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ayuda & Configuración | AAC27</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/Administrador/admin-ayuda.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
</head>
<body>

<!-- ══════════ NAVBAR (igual al dashboard) ══════════ -->
<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin">
        <h2>Volver al inicio</h2>
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    </div>
    <a href="<%=request.getContextPath()%>/Administrador/admin-principal.jsp" class="navbar-admin__home-link">
        <span class="navbar-admin__home-icon-wrap">
            <i class="fa-solid fa-right-from-bracket"></i>
            <span class="navbar-admin__home-text">Volver atras</span>
        </span>
    </a>
</nav>

<!-- ══════════ HERO ══════════ -->
<div class="ayuda-hero">
    <div class="ayuda-hero__inner">
        <h1 class="ayuda-hero__title">¿En qué te podemos ayudar,<%= admin.getNombre() %>?</h1>
        
    </div>
</div>

<!-- ══════════ CONTENIDO ══════════ -->
<main class="ayuda-main">

    <!-- ── SECCIÓN: PRIMEROS PASOS ── -->
    <div class="ayuda-section" id="seccion-inicio">
        <p class="ayuda-section__label"><i class="fas fa-rocket"></i> Primeros pasos</p>
        <div class="ayuda-grid">

            <div class="ayuda-card ayuda-card--expand" onclick="toggleCard(this)">
                <div class="ayuda-card__badge">Esencial</div>
                <div class="ayuda-card__head">
                    <div class="ayuda-card__ico ico--lila"><i class="fas fa-gauge-high"></i></div>
                    <div>
                        <p class="ayuda-card__cat">Dashboard</p>
                        <p class="ayuda-card__title">¿Cómo leer el panel principal?</p>
                    </div>
                    <i class="fas fa-chevron-right ayuda-card__arrow"></i>
                </div>
                <div class="ayuda-card__body">
                    <!-- ✏️ EDITA AQUÍ tu contenido -->
                    El panel principal muestra 4 métricas clave en tiempo real: ingresos del mes, ventas totales, proveedores activos y usuarios registrados. Los datos se actualizan cada vez que recargas la página. Las notificaciones al costado derecho te alertan sobre eventos importantes del sistema.
                </div>
            </div>

            <div class="ayuda-card ayuda-card--expand" onclick="toggleCard(this)">
                <div class="ayuda-card__head">
                    <div class="ayuda-card__ico ico--mint"><i class="fas fa-user-shield"></i></div>
                    <div>
                        <p class="ayuda-card__cat">Acceso</p>
                        <p class="ayuda-card__title">Cambiar contraseña de administrador</p>
                    </div>
                    <i class="fas fa-chevron-right ayuda-card__arrow"></i>
                </div>
                <div class="ayuda-card__body">
                    <!-- ✏️ EDITA AQUÍ tu contenido -->
                    Haz clic en el botón flotante de llave (<i class="fas fa-key"></i>) que aparece en la esquina del dashboard. Ingresa tu contraseña actual, luego la nueva (mínimo 6 caracteres). Si tu contraseña es temporal, el sistema te obligará a cambiarla antes de continuar.
                </div>
            </div>

            <div class="ayuda-card ayuda-card--expand" onclick="toggleCard(this)">
                <div class="ayuda-card__head">
                    <div class="ayuda-card__ico ico--sky"><i class="fas fa-bell"></i></div>
                    <div>
                        <p class="ayuda-card__cat">Notificaciones</p>
                        <p class="ayuda-card__title">¿Qué significan las alertas?</p>
                    </div>
                    <i class="fas fa-chevron-right ayuda-card__arrow"></i>
                </div>
                <div class="ayuda-card__body">
                    <!-- ✏️ EDITA AQUÍ tu contenido -->
                    Las notificaciones del panel se generan automáticamente según eventos del sistema: stock bajo de productos, nuevos pedidos pendientes, proveedores vencidos o usuarios sin verificar. Si el panel muestra "Todo al día", no hay alertas pendientes.
                </div>
            </div>

        </div>
    </div>

    <!-- ── SECCIÓN: GESTIÓN ── -->
    <div class="ayuda-section" id="seccion-gestion">
        <p class="ayuda-section__label"><i class="fas fa-sliders"></i> Gestión del sistema</p>
        <div class="ayuda-grid">

            <div class="ayuda-card ayuda-card--expand" onclick="toggleCard(this)">
                <div class="ayuda-card__head">
                    <div class="ayuda-card__ico ico--amber"><i class="fas fa-truck"></i></div>
                    <div>
                        <p class="ayuda-card__cat">Proveedores</p>
                        <p class="ayuda-card__title">Agregar y gestionar proveedores</p>
                    </div>
                    <i class="fas fa-chevron-right ayuda-card__arrow"></i>
                </div>
                <div class="ayuda-card__body">
                    <!-- ✏️ EDITA AQUÍ tu contenido -->
                    Desde el módulo de Proveedores puedes registrar nuevos proveedores, editar su información de contacto y activar o desactivar su estado. Un proveedor inactivo no aparecerá disponible al momento de crear productos asociados.
                </div>
            </div>

            <div class="ayuda-card ayuda-card--expand" onclick="toggleCard(this)">
                <div class="ayuda-card__head">
                    <div class="ayuda-card__ico ico--rose"><i class="fas fa-tags"></i></div>
                    <div>
                        <p class="ayuda-card__cat">Categorías</p>
                        <p class="ayuda-card__title">Crear y editar categorías</p>
                    </div>
                    <i class="fas fa-chevron-right ayuda-card__arrow"></i>
                </div>
                <div class="ayuda-card__body">
                    <!-- ✏️ EDITA AQUÍ tu contenido -->
                    Las categorías organizan el catálogo de productos. Puedes crear nuevas desde el módulo Categorías, asignarles un nombre y descripción. Eliminar una categoría solo es posible si no tiene productos asociados.
                </div>
            </div>

            <div class="ayuda-card ayuda-card--expand" onclick="toggleCard(this)">
                <div class="ayuda-card__head">
                    <div class="ayuda-card__ico ico--lila"><i class="fas fa-users"></i></div>
                    <div>
                        <p class="ayuda-card__cat">Usuarios</p>
                        <p class="ayuda-card__title">Administrar usuarios del sistema</p>
                    </div>
                    <i class="fas fa-chevron-right ayuda-card__arrow"></i>
                </div>
                <div class="ayuda-card__body">
                    <!-- ✏️ EDITA AQUÍ tu contenido -->
                    Desde el módulo de Usuarios puedes ver todos los registros, filtrar por tipo (cliente, admin), restablecer contraseñas temporales y desactivar cuentas. Un usuario desactivado no podrá iniciar sesión.
                </div>
            </div>

            <div class="ayuda-card ayuda-card--expand" onclick="toggleCard(this)">
                <div class="ayuda-card__head">
                    <div class="ayuda-card__ico ico--mint"><i class="fas fa-file-invoice-dollar"></i></div>
                    <div>
                        <p class="ayuda-card__cat">Ventas</p>
                        <p class="ayuda-card__title">Revisar y gestionar ventas</p>
                    </div>
                    <i class="fas fa-chevron-right ayuda-card__arrow"></i>
                </div>
                <div class="ayuda-card__body">
                    <!-- ✏️ EDITA AQUÍ tu contenido -->
                    El módulo de Ventas muestra el historial de transacciones. Puedes filtrar por fecha, estado del pedido o usuario. Los ingresos del dashboard reflejan únicamente ventas confirmadas en el mes en curso.
                </div>
            </div>

        </div>
    </div>

    <!-- ── SECCIÓN: CONFIGURACIÓN ── -->
    <div class="ayuda-section" id="seccion-config">
        <p class="ayuda-section__label"><i class="fas fa-gear"></i> Configuración</p>
        <div class="ayuda-grid">

            <div class="ayuda-card ayuda-card--expand" onclick="toggleCard(this)">
                <div class="ayuda-card__badge ayuda-card__badge--warn">Importante</div>
                <div class="ayuda-card__head">
                    <div class="ayuda-card__ico ico--rose"><i class="fas fa-database"></i></div>
                    <div>
                        <p class="ayuda-card__cat">Base de datos</p>
                        <p class="ayuda-card__title">Conexión y configuración de BD</p>
                    </div>
                    <i class="fas fa-chevron-right ayuda-card__arrow"></i>
                </div>
                <div class="ayuda-card__body">
                    <!-- ✏️ EDITA AQUÍ tu contenido -->
                    La conexión a la base de datos se configura en el archivo <code>config/ConexionDB.java</code>. Modifica los parámetros de host, puerto, nombre de base de datos, usuario y contraseña. Reinicia el servidor después de cualquier cambio para que tome efecto.
                </div>
            </div>

            <div class="ayuda-card ayuda-card--expand" onclick="toggleCard(this)">
                <div class="ayuda-card__badge ayuda-card__badge--ok">Seguridad</div>
                <div class="ayuda-card__head">
                    <div class="ayuda-card__ico ico--sky"><i class="fas fa-shield-halved"></i></div>
                    <div>
                        <p class="ayuda-card__cat">Sesiones</p>
                        <p class="ayuda-card__title">Tiempo de sesión y seguridad</p>
                    </div>
                    <i class="fas fa-chevron-right ayuda-card__arrow"></i>
                </div>
                <div class="ayuda-card__body">
                    <!-- ✏️ EDITA AQUÍ tu contenido -->
                    La sesión de administrador expira automáticamente tras un período de inactividad. Siempre cierra sesión desde el botón "Cerrar sesión" al terminar. No compartas tus credenciales con terceros.
                </div>
            </div>

            <div class="ayuda-card ayuda-card--expand ayuda-card--wide" onclick="toggleCard(this)">
                <div class="ayuda-card__head">
                    <div class="ayuda-card__ico ico--pink"><i class="fas fa-code"></i></div>
                    <div>
                        <p class="ayuda-card__cat">Desarrollo</p>
                        <p class="ayuda-card__title">Estructura del proyecto y convenciones</p>
                    </div>
                    <i class="fas fa-chevron-right ayuda-card__arrow"></i>
                </div>
                <div class="ayuda-card__body">
                    <!-- ✏️ EDITA AQUÍ tu contenido -->
                    El proyecto sigue el patrón MVC. Los modelos están en <code>model/</code>, la lógica de acceso a datos en <code>dao/</code>, los servlets en la raíz y las vistas JSP en <code>Administrador/</code>. Los assets (CSS, imágenes, JS) están en <code>assets/</code>. Sigue esta estructura al agregar nuevas páginas o funcionalidades.
                </div>
            </div>

        </div>
    </div>

    <!-- ── SECCIÓN: PREGUNTAS FRECUENTES ── -->
    <div class="ayuda-section" id="seccion-faq">
        <p class="ayuda-section__label"><i class="fas fa-circle-question"></i> Preguntas frecuentes</p>
        <div class="ayuda-grid">

            <div class="ayuda-card ayuda-card--expand" onclick="toggleCard(this)">
                <div class="ayuda-card__head">
                    <div class="ayuda-card__ico ico--lila"><i class="fas fa-rotate"></i></div>
                    <div>
                        <p class="ayuda-card__cat">FAQ</p>
                        <p class="ayuda-card__title">¿Por qué los datos no se actualizan?</p>
                    </div>
                    <i class="fas fa-chevron-right ayuda-card__arrow"></i>
                </div>
                <div class="ayuda-card__body">
                    <!-- ✏️ EDITA AQUÍ tu contenido -->
                    Los indicadores del dashboard se calculan en cada carga de página. Si ves datos desactualizados, recarga el navegador (F5). Si el problema persiste, verifica la conexión a la base de datos o contacta al equipo técnico.
                </div>
            </div>

            <div class="ayuda-card ayuda-card--expand" onclick="toggleCard(this)">
                <div class="ayuda-card__head">
                    <div class="ayuda-card__ico ico--amber"><i class="fas fa-user-lock"></i></div>
                    <div>
                        <p class="ayuda-card__cat">FAQ</p>
                        <p class="ayuda-card__title">Olvidé mi contraseña, ¿qué hago?</p>
                    </div>
                    <i class="fas fa-chevron-right ayuda-card__arrow"></i>
                </div>
                <div class="ayuda-card__body">
                    <!-- ✏️ EDITA AQUÍ tu contenido -->
                    Si perdiste acceso a tu cuenta, contacta directamente al desarrollador del sistema. Un administrador con acceso a la base de datos puede restablecer la contraseña marcándola como temporal para que puedas ingresar y cambiarla.
                </div>
            </div>

            <div class="ayuda-card ayuda-card--expand" onclick="toggleCard(this)">
                <div class="ayuda-card__head">
                    <div class="ayuda-card__ico ico--mint"><i class="fas fa-triangle-exclamation"></i></div>
                    <div>
                        <p class="ayuda-card__cat">FAQ</p>
                        <p class="ayuda-card__title">Aparece un error 500 en el sistema</p>
                    </div>
                    <i class="fas fa-chevron-right ayuda-card__arrow"></i>
                </div>
                <div class="ayuda-card__body">
                    <!-- ✏️ EDITA AQUÍ tu contenido -->
                    Un error 500 indica un problema interno del servidor. Revisa los logs de Tomcat en la consola de tu servidor. Los errores más comunes son: fallo en la conexión a la BD, NullPointerException en un servlet o un JSP mal formado.
                </div>
            </div>

        </div>
    </div>

    <!-- ── CTA DE CONTACTO ── -->
    <div class="ayuda-section">
    <div class="cta-card">
        <div class="cta-card__txt">
            <h3>¿Tienes una duda técnica?</h3>
            <p>Escribe tu mensaje y llegará al equipo de desarrollo.</p>
        </div>
        
        <form action="<%= request.getContextPath() %>/AyudaServlet" method="POST" style="width: 100%; max-width: 400px; margin-top: 15px;">
            <input type="hidden" name="nombreAdmin" value="<%= admin.getNombre() %>">
            
            <input type="text" name="asunto" placeholder="Asunto (Ej: Error en reporte)" required 
                   style="width: 100%; padding: 10px; margin-bottom: 10px; border-radius: 5px; border: 1px solid #ddd;">
            
            <textarea name="mensaje" placeholder="Describe tu duda aquí..." required 
                      style="width: 100%; padding: 10px; margin-bottom: 10px; border-radius: 5px; border: 1px solid #ddd; height: 80px;"></textarea>
            
            <button type="submit" class="cta-card__btn" style="width: 100%; cursor: pointer;">
                <i class="fas fa-paper-plane"></i> Enviar al Desarrollador
            </button>
        </form>
    </div>
</div>

</main>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

<script>
    /* ── Función de filtrado que ya tenías ── */
    function filtrarCards(query) {
        const q = query.toLowerCase().trim();
        document.querySelectorAll('.ayuda-card').forEach(card => {
            const txt = card.innerText.toLowerCase();
            card.style.display = (!q || txt.includes(q)) ? '' : 'none';
        });
        document.querySelectorAll('.ayuda-section').forEach(sec => {
            const visible = [...sec.querySelectorAll('.ayuda-card')].some(c => c.style.display !== 'none');
            sec.style.display = visible ? '' : 'none';
        });
    }

    /* ── Función para Toggle de Cards ── */
    function toggleCard(card) {
        if (!card.classList.contains('ayuda-card--expand')) return;
        card.classList.toggle('open');
    }

    /* ── Estado de "Enviando" en el Formulario ── */
    // Esto evita que el admin haga doble clic y sature el EmailService
    const formSoporte = document.querySelector('form[action*="AyudaServlet"]');
    if(formSoporte) {
        formSoporte.addEventListener('submit', function() {
            const btn = this.querySelector('button[type="submit"]');
            btn.innerHTML = '<i class="fas fa-circle-notch fa-spin"></i> Procesando...';
            btn.style.pointerEvents = 'none';
            btn.style.opacity = '0.7';
        });
    }
</script>

<% 
    String status = request.getParameter("status"); 
    if ("success".equals(status)) { 
%>
    <script>
        Swal.fire({
            title: '¡Consulta Enviada!',
            text: 'Tu ticket de soporte ha sido recibido. El desarrollador te contactará pronto.',
            icon: 'success',
            iconColor: '#9177a8',
            confirmButtonText: 'Entendido',
            confirmButtonColor: '#9177a8',
            background: '#ffffff',
            backdrop: `rgba(145, 119, 168, 0.2)`, // Un sutil rastro lila al fondo
            showClass: { popup: 'animate__animated animate__fadeInUp' }
        }).then(() => {
            // Limpiar la URL para que el mensaje no salga de nuevo al recargar (F5)
            const cleanUrl = window.location.protocol + "//" + window.location.host + window.location.pathname;
            window.history.replaceState({path: cleanUrl}, '', cleanUrl);
        });
    </script>
<% 
    } else if ("error".equals(status)) { 
%>
    <script>
        Swal.fire({
            title: 'Error técnico',
            text: 'No pudimos enviar el correo. Por favor, verifica la configuración SMTP.',
            icon: 'error',
            confirmButtonText: 'Reintentar',
            confirmButtonColor: '#e74c3c'
        });
    </script>
<% } %>
</body>
</html>
