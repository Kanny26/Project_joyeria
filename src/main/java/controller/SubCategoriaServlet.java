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
 * Servlet encargado de gestionar las subcategorías del catálogo de productos.
 *
 * Proporciona operaciones CRUD para subcategorías y coordina su visualización
 * en la vista unificada de organización de categorías. Las operaciones de modificación
 * se realizan mediante POST con redirección tras éxito para evitar reenvíos de formulario.
 *
 * @see SubcategoriaDAO
 * @see Subcategoria
 */
@WebServlet("/SubcategoriaServlet")
public class SubCategoriaServlet extends HttpServlet {

    private SubcategoriaDAO subcategoriaDAO;

    /**
     * Inicializa el servlet creando la instancia del DAO para subcategorías.
     * Este método se ejecuta una única vez cuando el servlet es cargado por el contenedor,
     * permitiendo reutilizar la instancia del DAO en todas las peticiones posteriores.
     */
    @Override
    public void init() {
        subcategoriaDAO = new SubcategoriaDAO();
    }

    /**
     * Procesa las solicitudes HTTP GET para listar las subcategorías registradas.
     *
     * Recupera la lista completa desde la base de datos, la almacena como atributo
     * de la petición y transfiere el control a la vista unificada de administración,
     * indicando que se debe mostrar la pestaña de subcategorías.
     *
     * @param request  la petición HTTP recibida del cliente
     * @param response la respuesta HTTP que se enviará al cliente tras procesar la vista
     * @throws ServletException si ocurre un error durante el despacho a la vista JSP
     * @throws IOException      si ocurre un error de entrada/salida al procesar la petición o respuesta
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
     * Procesa las solicitudes HTTP POST para ejecutar operaciones de modificación sobre subcategorías.
     *
     * Según el valor del parámetro {@code action}, ejecuta una de las siguientes operaciones:
     * - "guardar": crea una nueva subcategoría validando que el nombre no esté vacío
     * - "actualizar": modifica una subcategoría existente validando ID y nombre
     * - "eliminar": remueve una subcategoría validando que se proporcione su ID
     *
     * En caso de éxito, redirige con un parámetro de confirmación para evitar reenvíos.
     * En caso de error, realiza forward a la vista con el mensaje de error para su visualización.
     *
     * @param request  la petición HTTP con {@code action} y datos del formulario
     * @param response la respuesta HTTP (redirect en éxito, forward con error)
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