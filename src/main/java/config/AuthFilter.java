package config;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = {"/vendedor/*", "/Administrador/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 1. CABECERAS ANTI-CACHÉ (Obligatorio en todas las respuestas protegidas)
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        HttpSession session = req.getSession(false);
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();

        // 2. EXCEPCIONES (Login)
        if (uri.contains("inicio-sesion.jsp") || uri.contains("login")) {
            chain.doFilter(request, response);
            return;
        }

        // 3. PROTECCIÓN RUTA VENDEDOR
        if (uri.startsWith(contextPath + "/vendedor/")) {
            if (session == null || session.getAttribute("vendedor") == null) {
                resp.sendRedirect(contextPath + "/inicio-sesion.jsp");
                return;
            }
        }

        // 4. PROTECCIÓN RUTA ADMINISTRADOR
        if (uri.startsWith(contextPath + "/Administrador/")) {
            if (session == null || session.getAttribute("admin") == null) {
                resp.sendRedirect(contextPath + "/inicio-sesion.jsp");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}