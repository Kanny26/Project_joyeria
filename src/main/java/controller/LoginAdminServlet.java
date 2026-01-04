package controller;

import dao.AdministradorDAO;
import model.Administrador;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginAdminServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nombre = request.getParameter("usuario");
        String pass = request.getParameter("password");

        AdministradorDAO dao = new AdministradorDAO();
        Administrador admin = dao.validar(nombre, pass);

        if (admin != null) {
            HttpSession session = request.getSession();
            session.setAttribute("admin", admin);

            // Redirige a admin-principal.jsp
            response.sendRedirect(request.getContextPath() + "/Administrador/admin-principal.jsp");
        } else {
            response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp?error=true");
        }
    }
}

