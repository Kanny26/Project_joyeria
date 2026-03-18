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

    // init() se ejecuta una sola vez al cargar el servlet.
    @Override
    public void init() {
        materialDAO = new MaterialDAO();
    }

    // Carga la lista de materiales y hace forward al JSP unificado con tab activo.
    // El parámetro tab=materiales le indica a org-categorias.jsp cuál tab mostrar abierto.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("materiales", materialDAO.listarMateriales());
            request.getRequestDispatcher("/Administrador/org-categorias.jsp?tab=materiales")
                    .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "No se pudieron cargar los materiales. Intenta de nuevo.");
            request.getRequestDispatcher("/Administrador/org-categorias.jsp")
                    .forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // El parámetro "action" viene del campo oculto en el formulario JSP.
        String action = request.getParameter("action");
        try {
            if ("guardar".equals(action)) {
                String nombre = request.getParameter("nombre");
                // Validación: el nombre es obligatorio antes de intentar guardar.
                if (nombre == null || nombre.trim().isEmpty())
                    throw new Exception("El nombre del material es obligatorio.");

                Material m = new Material();
                m.setNombre(nombre.trim());
                materialDAO.guardar(m);
                // sendRedirect evita reenvío del formulario al refrescar la página.
                // msg=creado le indica al JSP que muestre el mensaje de éxito correspondiente.
                response.sendRedirect(request.getContextPath() + "/MaterialServlet?msg=creado");

            } else if ("actualizar".equals(action)) {
                String idStr  = request.getParameter("id");
                String nombre = request.getParameter("nombre");
                // Validación: se necesitan ambos campos para actualizar correctamente.
                if (idStr == null || nombre == null || nombre.trim().isEmpty())
                    throw new Exception("Datos inválidos para actualizar.");

                Material m = new Material();
                // parseInt convierte el String "id" a entero; falla si no es numérico.
                m.setMaterialId(Integer.parseInt(idStr));
                m.setNombre(nombre.trim());
                materialDAO.actualizar(m);
                response.sendRedirect(request.getContextPath() + "/MaterialServlet?msg=actualizado");

            } else if ("eliminar".equals(action)) {
                String idStr = request.getParameter("id");
                if (idStr == null) throw new Exception("No se recibió el ID para eliminar.");

                materialDAO.eliminar(Integer.parseInt(idStr));
                response.sendRedirect(request.getContextPath() + "/MaterialServlet?msg=eliminado");

            } else {
                response.sendRedirect(request.getContextPath() + "/MaterialServlet");
            }

        } catch (Exception e) {
            // Con forward se puede pasar el mensaje de error como atributo del request.
            // Con sendRedirect no sería posible porque inicia una petición completamente nueva.
            request.setAttribute("error", e.getMessage());
            try {
                request.setAttribute("materiales", materialDAO.listarMateriales());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            request.getRequestDispatcher("/Administrador/org-categorias.jsp?tab=materiales")
                    .forward(request, response);
        }
    }
}
