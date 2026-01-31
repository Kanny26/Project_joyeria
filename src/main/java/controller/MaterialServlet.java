package controller;

import dao.MaterialDAO;
import model.Material;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Servlet encargado de gestionar la visualización de los materiales
 * disponibles en el sistema.
 *
 * Ruta:
 *  - /MaterialServlet
 *
 * Función principal:
 *  - Obtener la lista de materiales desde la capa DAO
 *  - Enviarla a la vista JSP correspondiente para su administración
 */
@WebServlet("/MaterialServlet")
public class MaterialServlet extends HttpServlet {

    /**
     * DAO encargado del acceso a datos relacionados con Material.
     */
    private MaterialDAO materialDAO;

    /**
     * Inicializa el servlet y crea la instancia del DAO.
     * Se ejecuta una sola vez al cargar el servlet.
     */
    @Override
    public void init() {
        materialDAO = new MaterialDAO();
    }

    /**
     * Maneja las peticiones GET.
     * Obtiene todos los materiales registrados y los envía a la vista.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Obtener la lista de materiales desde la base de datos
        List<Material> materiales = materialDAO.listarMateriales();

        // Enviar la lista a la vista
        request.setAttribute("materiales", materiales);

        // Redirigir a la página de organización de materiales
        request.getRequestDispatcher("/Administrador/org-materiales.jsp")
               .forward(request, response);
    }
}
