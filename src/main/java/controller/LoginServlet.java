package controller;

import dao.AuthDAO;
import model.Administrador;
import model.Usuario;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@WebServlet("/loginUnificado")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String nombre = request.getParameter("usuario");
        String pass = request.getParameter("password");

        // Validación básica
        if (nombre == null || nombre.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp?error=campos");
            return;
        }

        AuthDAO authDAO = new AuthDAO();
        Map<String, Object> datos = authDAO.validar(nombre, pass);

        if (datos == null) {
            // Mensaje genérico por seguridad (RF02)
            request.setAttribute("error", "Usuario o contraseña incorrectos");
            request.getRequestDispatcher("/inicio-sesion.jsp").forward(request, response);
            return;
        }

        // ■■ PREVENCIÓN DE FIJACIÓN DE SESIÓN ■■
        // Invalidar sesión anterior antes de crear una nueva autenticada
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(900); // 15 minutos (RF04)

        // Cabeceras de seguridad para evitar caché en páginas sensibles
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        String rol = (String) datos.get("rol");

        switch (rol) {
            case "administrador": {
                Administrador admin = new Administrador();
                admin.setId((int) datos.get("id"));
                admin.setNombre((String) datos.get("nombre"));
                session.setAttribute("admin", admin);
                session.setAttribute("rol", "administrador");
                response.sendRedirect(request.getContextPath() + "/Administrador/admin-principal.jsp");
                break;
            }
            case "vendedor": {
                Usuario vendedor = new Usuario();
                vendedor.setUsuarioId((int) datos.get("id"));
                vendedor.setNombre((String) datos.get("nombre"));
                vendedor.setRol(rol);
                session.setAttribute("vendedor", vendedor);
                session.setAttribute("rol", "vendedor");
                response.sendRedirect(request.getContextPath() + "/vendedor/vendedor_principal.jsp");
                break;
            }
            default:
                request.setAttribute("error", "Rol no autorizado");
                request.getRequestDispatcher("/inicio-sesion.jsp").forward(request, response);
        }
    }
}