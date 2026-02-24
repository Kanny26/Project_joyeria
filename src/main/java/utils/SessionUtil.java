package utils;

import model.Usuario;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class SessionUtil {

    public boolean validarSesionVendedor(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return false;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (!usuario.isActivo()) {
            session.invalidate();
            resp.sendRedirect(req.getContextPath() + "/login.jsp?error=sesion_inactiva");
            return false;
        }

        if (!"vendedor".equals(usuario.getRol())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado: rol no autorizado");
            return false;
        }

        session.setAttribute("ultimoAcceso", System.currentTimeMillis());
        return true;
    }

    public void cerrarSesion(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/login.jsp?logout=1");
    }
}