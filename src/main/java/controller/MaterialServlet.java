package controller;

import dao.MaterialDAO;
import model.Material;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/MaterialServlet")
public class MaterialServlet extends HttpServlet {

    private MaterialDAO materialDAO;

    @Override
    public void init() {
        materialDAO = new MaterialDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Material> materiales = materialDAO.listarMateriales();
        request.setAttribute("materiales", materiales);

        request.getRequestDispatcher("/Administrador/org-materiales.jsp")
               .forward(request, response);
    }
}
