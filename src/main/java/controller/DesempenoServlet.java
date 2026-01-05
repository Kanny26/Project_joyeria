// controller/DesempenoServlet.java
package controller;

import dao.DesempenoDAO;
import model.Desempeno_Vendedor;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/DesempenoServlet")
public class DesempenoServlet extends HttpServlet {
    private DesempenoDAO desempenoDAO;

    @Override
    public void init() {
        desempenoDAO = new DesempenoDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        // Obtener todo el historial de desempeño (con nombre del vendedor)
        List<Desempeno_Vendedor> historial = desempenoDAO.obtenerHistorialCompleto();
        req.setAttribute("historial", historial);
        req.getRequestDispatcher("/Administrador/usuarios/historial.jsp").forward(req, resp);
    }
}