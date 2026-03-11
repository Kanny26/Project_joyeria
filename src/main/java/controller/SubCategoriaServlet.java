package controller;

import dao.SubcategoriaDAO;
import model.Subcategoria;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/SubcategoriaServlet")
public class SubCategoriaServlet extends HttpServlet {
    
    private SubcategoriaDAO subcategoriaDAO;
    
    @Override
    public void init() {
        subcategoriaDAO = new SubcategoriaDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            request.setAttribute("subcategorias", subcategoriaDAO.listarTodas());
            request.getRequestDispatcher("/Administrador/org-categorias.jsp?tab=subcategorias")
                    .forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Error al cargar subcategorías: " + e.getMessage());
            request.getRequestDispatcher("/Administrador/org-categorias.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            if ("guardar".equals(action)) {
                String nombre = request.getParameter("nombre");
                if (nombre == null || nombre.trim().isEmpty()) 
                    throw new Exception("Nombre obligatorio");
                
                Subcategoria s = new Subcategoria();
                s.setNombre(nombre.trim());
                subcategoriaDAO.guardar(s);
                response.sendRedirect(request.getContextPath() + "/SubcategoriaServlet?msg=creado");
                
            } else if ("actualizar".equals(action)) {
                String idStr = request.getParameter("id");
                String nombre = request.getParameter("nombre");
                if (idStr == null || nombre == null || nombre.trim().isEmpty()) 
                    throw new Exception("Datos inválidos");
                
                Subcategoria s = new Subcategoria();
                s.setSubcategoriaId(Integer.parseInt(idStr));
                s.setNombre(nombre.trim());
                subcategoriaDAO.actualizar(s);
                response.sendRedirect(request.getContextPath() + "/SubcategoriaServlet?msg=actualizado");
                
            } else if ("eliminar".equals(action)) {
                String idStr = request.getParameter("id");
                if (idStr == null) throw new Exception("ID no proporcionado");
                
                subcategoriaDAO.eliminar(Integer.parseInt(idStr));
                response.sendRedirect(request.getContextPath() + "/SubcategoriaServlet?msg=eliminado");
            } else {
                response.sendRedirect(request.getContextPath() + "/SubcategoriaServlet");
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("subcategorias", subcategoriaDAO.listarTodas());
            request.getRequestDispatcher("/Administrador/org-categorias.jsp?tab=subcategorias")
                    .forward(request, response);
        }
    }
}