package config;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Filtro de seguridad que protege todas las rutas del sistema que requieren autenticación.
 *
 * Se aplica automáticamente a cualquier petición que coincida con:
 *   /vendedor/*   → solo usuarios con sesión de tipo "vendedor"
 *   /Administrador/* → solo usuarios con sesión de tipo "admin"
 *
 * Si el usuario no tiene la sesión correcta, se redirige al login.
 * Este filtro actúa ANTES de que el servlet procese la petición.
 */
@WebFilter(urlPatterns = {"/vendedor/*", "/Administrador/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        /*
         * Cabeceras anti-caché: evitan que el navegador guarde en caché las páginas protegidas.
         * Sin esto, un usuario podría presionar "Atrás" después de cerrar sesión
         * y ver datos de la sesión anterior almacenados en caché.
         *   no-store    → no guardar en ningún tipo de caché
         *   must-revalidate → si se almacena, revalidar siempre con el servidor
         *   Expires: 0  → marcar como caducado inmediatamente (para HTTP/1.0)
         */
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        HttpSession session    = req.getSession(false); // false: no crear sesión si no existe
        String      uri        = req.getRequestURI();
        String      contextPath = req.getContextPath();

        /*
         * Las páginas de login y el propio servlet de login no requieren sesión previa.
         * Si no se excluyen aquí, el filtro redirigiría en bucle: el login redirige al login.
         */
        if (uri.contains("inicio-sesion.jsp") || uri.contains("login")) {
            chain.doFilter(request, response);
            return;
        }

        /*
         * Validación de acceso a rutas del vendedor.
         * Se verifica que exista una sesión activa y que tenga el atributo "vendedor".
         * Si falta alguno de los dos, se redirige al login.
         * sendRedirect envía al navegador a una URL nueva; el filtro corta aquí su ejecución.
         */
        if (uri.startsWith(contextPath + "/vendedor/")) {
            if (session == null || session.getAttribute("vendedor") == null) {
                resp.sendRedirect(contextPath + "/inicio-sesion.jsp");
                return;
            }
        }

        /*
         * Validación de acceso a rutas del administrador.
         * Mismo mecanismo que para vendedor, pero verifica el atributo "admin".
         * Tanto el rol "administrador" como "superadministrador" usan este atributo.
         */
        if (uri.startsWith(contextPath + "/Administrador/")) {
            if (session == null || session.getAttribute("admin") == null) {
                resp.sendRedirect(contextPath + "/inicio-sesion.jsp");
                return;
            }
        }

        // Si pasó todas las validaciones, se continúa hacia el recurso solicitado.
        // chain.doFilter pasa la petición al siguiente filtro o al servlet final.
        chain.doFilter(request, response);
    }
}
