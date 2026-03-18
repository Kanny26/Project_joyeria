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

    // init() se ejecuta una sola vez al cargar el servlet.
    // El DAO se instancia aquí para reutilizarlo en todas las peticiones.
    @Override
    public void init() {
        subcategoriaDAO = new SubcategoriaDAO();
    }

    // El doGet carga la lista de subcategorías y hace forward al JSP unificado
    // con el parámetro tab=subcategorias para que se active el tab correcto.
    // NOTA: forward no cambia la URL del navegador, pero los atributos del request
    // llegan al JSP y están disponibles con request.getAttribute().
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("subcategorias", subcategoriaDAO.listarTodas());
            request.getRequestDispatcher("/Administrador/org-categorias.jsp?tab=subcategorias")
                    .forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "No se pudieron cargar las subcategorías. Intenta de nuevo.");
            request.getRequestDispatcher("/Administrador/org-categorias.jsp").forward(request, response);
        }
    }

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
