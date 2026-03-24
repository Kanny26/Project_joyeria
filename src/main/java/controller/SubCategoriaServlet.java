package controller;

import dao.SubcategoriaDAO;
import model.Subcategoria;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 SubcategoriaDAO; reenvía a /Administrador/org-categorias.jsp (pestaña subcategorías).
 */
@WebServlet("/SubcategoriaServlet")
public class SubCategoriaServlet extends HttpServlet {

    private SubcategoriaDAO subcategoriaDAO;

    /**
     * Inicializa el servlet e instancia el DAO de subcategorías.
     */
    @Override
    public void init() {
        subcategoriaDAO = new SubcategoriaDAO();
    }

    /**
     * Carga el listado de subcategorías y hace forward al JSP unificado con {@code tab=subcategorias}.
     *
     * @param request  petición HTTP
     * @param response respuesta HTTP hacia el cliente
     * @throws ServletException si falla el despacho a la vista
     * @throws IOException      si ocurre un error de E/S
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("subcategorias", subcategoriaDAO.listarTodas());
            // forward mantiene la misma petición: el JSP lee los atributos sin cambiar la URL visible
            request.getRequestDispatcher("/Administrador/org-categorias.jsp?tab=subcategorias")
                    .forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "No se pudieron cargar las subcategorías. Intenta de nuevo.");
            request.getRequestDispatcher("/Administrador/org-categorias.jsp").forward(request, response);
        }
    }

    /**
     * Ejecuta guardar, actualizar o eliminar según {@code action}; ante error hace forward con mensaje.
     *
     * @param request  petición HTTP con {@code action} y datos del formulario
     * @param response respuesta HTTP (redirect en éxito, forward con error)
     * @throws ServletException si falla el despacho a la vista
     * @throws IOException      si ocurre un error de E/S
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // El parámetro "action" viene del campo oculto en el formulario JSP
        // y determina qué operación ejecutar.
        String action = request.getParameter("action");
        try {
            if ("guardar".equals(action)) {
                String nombre = request.getParameter("nombre");
                // Validación: el nombre es obligatorio. Si viene vacío o null, se corta el proceso.
                if (nombre == null || nombre.trim().isEmpty())
                    throw new Exception("El nombre de la subcategoría es obligatorio.");

                Subcategoria s = new Subcategoria();
                s.setNombre(nombre.trim());
                subcategoriaDAO.guardar(s);
                // sendRedirect redirige al navegador a una URL nueva, evitando que al refrescar
                // se reenvíe el formulario. El parámetro msg indica al JSP qué alerta mostrar.
                response.sendRedirect(request.getContextPath() + "/SubcategoriaServlet?msg=creado");

            } else if ("actualizar".equals(action)) {
                String idStr  = request.getParameter("id");
                String nombre = request.getParameter("nombre");
                // Validación: ambos campos son necesarios para actualizar correctamente.
                if (idStr == null || nombre == null || nombre.trim().isEmpty())
                    throw new Exception("Datos inválidos para actualizar.");

                Subcategoria s = new Subcategoria();
                // Integer.parseInt convierte el String del parámetro a int.
                // Si el valor no fuera numérico, lanzaría NumberFormatException (capturada abajo).
                s.setSubcategoriaId(Integer.parseInt(idStr));
                s.setNombre(nombre.trim());
                subcategoriaDAO.actualizar(s);
                response.sendRedirect(request.getContextPath() + "/SubcategoriaServlet?msg=actualizado");

            } else if ("eliminar".equals(action)) {
                String idStr = request.getParameter("id");
                if (idStr == null) throw new Exception("No se recibió el ID para eliminar.");

                subcategoriaDAO.eliminar(Integer.parseInt(idStr));
                response.sendRedirect(request.getContextPath() + "/SubcategoriaServlet?msg=eliminado");

            } else {
                response.sendRedirect(request.getContextPath() + "/SubcategoriaServlet");
            }

        } catch (Exception e) {
            // Si ocurre un error, se hace forward (no redirect) para poder pasar el mensaje
            // como atributo del request. Con sendRedirect se perdería el mensaje.
            request.setAttribute("error", e.getMessage());
            // Se recarga la lista para que el JSP tenga datos que mostrar
            try { request.setAttribute("subcategorias", subcategoriaDAO.listarTodas()); } catch (Exception ignored) {}
            request.getRequestDispatcher("/Administrador/org-categorias.jsp?tab=subcategorias")
                    .forward(request, response);
        }
    }
}
