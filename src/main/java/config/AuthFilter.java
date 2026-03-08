package config;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Filtro de autenticación: protege las rutas de vendedor y administrador.
 * Verifica que el usuario tenga una sesión activa con el rol correspondiente.
 */
@WebFilter(urlPatterns = {"/vendedor/*", "/Administrador/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Configura cabeceras para evitar que el navegador almacene en caché páginas protegidas.
        // Esto previene que se muestren datos sensibles al usar el botón "Atrás".
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        HttpSession session = req.getSession(false);
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();

        // Permite acceso sin validación a las páginas de login para evitar bucles de redirección.
        if (uri.contains("inicio-sesion.jsp") || uri.contains("login")) {
            chain.doFilter(request, response);
            return;
        }

        // Valida que el usuario tenga sesión activa y rol de vendedor para acceder a /vendedor/*
        if (uri.startsWith(contextPath + "/vendedor/")) {
            if (session == null || session.getAttribute("vendedor") == null) {
                resp.sendRedirect(contextPath + "/inicio-sesion.jsp");
                return;
            }
        }

        // Valida que el usuario tenga sesión activa y rol de administrador para acceder a /Administrador/*
        if (uri.startsWith(contextPath + "/Administrador/")) {
            if (session == null || session.getAttribute("admin") == null) {
                resp.sendRedirect(contextPath + "/inicio-sesion.jsp");
                return;
            }
        }

        // Si pasó todas las validaciones, continúa con la petición al recurso solicitado.
        chain.doFilter(request, response);
    }
}