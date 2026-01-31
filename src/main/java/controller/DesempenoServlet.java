package controller;

import dao.DesempenoDAO;
import model.Desempeno_Vendedor;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Servlet encargado de gestionar la visualización del historial
 * de desempeño de los vendedores.
 *
 * Ruta:
 *  - /DesempenoServlet
 *
 * Función principal:
 *  - Obtener el historial completo de desempeño (incluyendo datos del vendedor)
 *  - Enviar la información a la vista JSP para su visualización administrativa
 */
@WebServlet("/DesempenoServlet")
public class DesempenoServlet extends HttpServlet {

    /**
     * DAO responsable de las operaciones relacionadas
     * con el desempeño de los vendedores.
     */
    private DesempenoDAO desempenoDAO;

    /**
     * Inicializa el servlet y crea la instancia del DAO.
     * Se ejecuta una sola vez al cargar el servlet.
     */
    @Override
    public void init() {
        desempenoDAO = new DesempenoDAO();
    }

    /**
     * Maneja las peticiones GET.
     * Obtiene el historial completo de desempeño de los vendedores
     * y lo envía a la vista correspondiente.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Obtener el historial completo de desempeño (incluye nombre del vendedor)
        List<Desempeno_Vendedor> historial = desempenoDAO.obtenerHistorialCompleto();

        // Enviar el historial a la vista
        request.setAttribute("historial", historial);

        // Redirigir a la página de historial de desempeño
        request.getRequestDispatcher("/Administrador/usuarios/historial.jsp")
               .forward(request, response);
    }
}
