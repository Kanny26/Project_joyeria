package controller;

import dao.MaterialDAO;
import model.Material;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/MaterialServlet")
public class MaterialServlet extends HttpServlet {
    private MaterialDAO materialDAO;

    @Override
    public void init() {
        materialDAO = new MaterialDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Listar materiales
        request.setAttribute("materiales", materialDAO.listarMateriales());
        request.getRequestDispatcher("/Administrador/org-materiales.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            if ("guardar".equals(action)) {
                String nombre = request.getParameter("nombre");
                if (nombre == null || nombre.trim().isEmpty()) throw new Exception("Nombre obligatorio");
                Material m = new Material();
                m.setNombre(nombre.trim());
                materialDAO.guardar(m);
                response.sendRedirect(request.getContextPath() + "/MaterialServlet?msg=creado");
            } else if ("eliminar".equals(action)) {
                String idStr = request.getParameter("id");
                if (idStr != null) {
                    materialDAO.eliminar(Integer.parseInt(idStr));
                    response.sendRedirect(request.getContextPath() + "/MaterialServlet?msg=eliminado");
                }
            }
            // Actualizar se puede manejar similarmente
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/Administrador/org-materiales.jsp").forward(request, response);
        }
    }
}