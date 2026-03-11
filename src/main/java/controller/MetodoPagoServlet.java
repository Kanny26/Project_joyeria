package controller;

import dao.MetodoPagoDAO;
import model.MetodoPago;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/MetodoPagoServlet")
public class MetodoPagoServlet extends HttpServlet {
    
    private MetodoPagoDAO metodoPagoDAO;
    
    @Override
    public void init() {
        metodoPagoDAO = new MetodoPagoDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // ✅ Usar la instancia del DAO, no llamar estáticamente
            List<MetodoPago> metodosPago = metodoPagoDAO.listarTodos();
            request.setAttribute("metodosPago", metodosPago);
            request.getRequestDispatcher("/Administrador/org-categorias.jsp?tab=metodosPago")
                    .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error al cargar métodos de pago: " + e.getMessage());
            request.getRequestDispatcher("/Administrador/org-categorias.jsp")
                    .forward(request, response);
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
                
                MetodoPago mp = new MetodoPago();
                mp.setNombre(nombre.trim());
                metodoPagoDAO.guardar(mp);
                response.sendRedirect(request.getContextPath() + "/MetodoPagoServlet?msg=creado");
                
            } else if ("actualizar".equals(action)) {
                String idStr = request.getParameter("id");
                String nombre = request.getParameter("nombre");
                if (idStr == null || nombre == null || nombre.trim().isEmpty()) 
                    throw new Exception("Datos inválidos");
                
                MetodoPago mp = new MetodoPago();
                mp.setMetodoPagoId(Integer.parseInt(idStr));
                mp.setNombre(nombre.trim());
                metodoPagoDAO.actualizar(mp);
                response.sendRedirect(request.getContextPath() + "/MetodoPagoServlet?msg=actualizado");
                
            } else if ("eliminar".equals(action)) {
                String idStr = request.getParameter("id");
                if (idStr == null) throw new Exception("ID no proporcionado");
                
                metodoPagoDAO.eliminar(Integer.parseInt(idStr));
                response.sendRedirect(request.getContextPath() + "/MetodoPagoServlet?msg=eliminado");
            } else {
                response.sendRedirect(request.getContextPath() + "/MetodoPagoServlet");
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            try {
                // ✅ También aquí usar la instancia
                request.setAttribute("metodosPago", metodoPagoDAO.listarTodos());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            request.getRequestDispatcher("/Administrador/org-categorias.jsp?tab=metodosPago")
                    .forward(request, response);
        }
    }
}