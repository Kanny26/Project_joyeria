package controller;

import dao.AdministradorDAO;
import model.Administrador;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet encargado del inicio de sesión de administradores.
 *
 * Valida las credenciales ingresadas y, si son correctas,
 * crea una sesión de administrador.
 */
@WebServlet("/login")
public class LoginAdminServlet extends HttpServlet {

    /**
     * Procesa el inicio de sesión del administrador.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Obtiene las credenciales desde el formulario
        String nombre = request.getParameter("usuario");
        String pass = request.getParameter("password");

        /* ==========================
           VALIDACIONES BÁSICAS
           ========================== */
        if (nombre == null || nombre.trim().isEmpty()
                || pass == null || pass.trim().isEmpty()) {

            // Redirige con error si faltan datos
            response.sendRedirect(
                    request.getContextPath()
                            + "/Administrador/inicio-sesion.jsp?error=campos"
            );
            return;
        }

        // Validación de credenciales contra la base de datos
        AdministradorDAO dao = new AdministradorDAO();
        Administrador admin = dao.validar(nombre, pass);

        if (admin != null) {
            // Credenciales correctas: se crea sesión
            HttpSession session = request.getSession(true);
            session.setAttribute("admin", admin);

            // Redirige al panel principal del administrador
            response.sendRedirect(
                    request.getContextPath()
                            + "/Administrador/admin-principal.jsp"
            );
        } else {
            // Credenciales incorrectas
            response.sendRedirect(
                    request.getContextPath()
                            + "/Administrador/inicio-sesion.jsp?error=true"
            );
        }
    }
}

