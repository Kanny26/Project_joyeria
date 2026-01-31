package controller;

import dao.UsuarioDAO;
import model.Usuario;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet encargado del inicio de sesión de vendedores.
 *
 * Valida las credenciales del vendedor y, si son correctas,
 * crea una sesión con su información y rol.
 */
@WebServlet("/loginSeller")
public class LoginSellerServlet extends HttpServlet {

    /**
     * Procesa el inicio de sesión del vendedor.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Obtención de credenciales desde el formulario
        String nombre = request.getParameter("usuario");
        String pass = request.getParameter("password");

        /* ==========================
           VALIDACIONES BÁSICAS
           ========================== */
        if (nombre == null || nombre.trim().isEmpty()
                || pass == null || pass.trim().isEmpty()) {

            // Redirige si los campos están vacíos
            response.sendRedirect(
                    request.getContextPath()
                            + "/vendedor/inicio-sesion.jsp?error=campos"
            );
            return;
        }

        // Validación de credenciales
        UsuarioDAO dao = new UsuarioDAO();
        Usuario seller = dao.validar(nombre, pass);

        if (seller != null) {
            // Credenciales correctas: se crea sesión
            HttpSession session = request.getSession(true);
            session.setAttribute("vendedor", seller);
            session.setAttribute("rol", seller.getRol());

            // Redirige al panel principal del vendedor
            response.sendRedirect(
                    request.getContextPath()
                            + "/vendedor/vendedor_principal.jsp"
            );
        } else {
            // Credenciales incorrectas
            response.sendRedirect(
                    request.getContextPath()
                            + "/vendedor/inicio-sesion.jsp?error=true"
            );
        }
    }
}
