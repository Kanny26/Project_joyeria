package controller;

import dao.UsuarioDAO;
import model.Usuario;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/loginSeller")
public class LoginSellerServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nombre = request.getParameter("usuario");
        String pass = request.getParameter("password");

        UsuarioDAO dao = new UsuarioDAO();
        Usuario seller = dao.validar(nombre, pass);

        if (seller != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute("vendedor", seller);
            session.setAttribute("rol", seller.getRol());

            response.sendRedirect(
                request.getContextPath() + "/vendedor/vendedor_principal.jsp"
            );
        } else {
            response.sendRedirect(
                request.getContextPath() + "/vendedor/inicio-sesion.jsp?error=true"
            );
        }
    }
}
