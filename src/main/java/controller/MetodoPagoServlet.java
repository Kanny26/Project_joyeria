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

/**
 * Servlet encargado de gestionar los métodos de pago disponibles en el sistema.
 *
 * Proporciona operaciones CRUD para métodos de pago y coordina su visualización
 * en la vista unificada de organización de categorías. Las operaciones de modificación
 * se realizan mediante POST con redirección tras éxito para evitar reenvíos de formulario.
 *
 * @see MetodoPagoDAO
 * @see MetodoPago
 */
@WebServlet("/MetodoPagoServlet")
public class MetodoPagoServlet extends HttpServlet {

    private MetodoPagoDAO metodoPagoDAO;

    /**
     * Inicializa el servlet creando la instancia del DAO para métodos de pago.
     * Este método se ejecuta una única vez cuando el servlet es cargado por el contenedor,
     * permitiendo reutilizar la instancia del DAO en todas las peticiones posteriores.
     */
    @Override
    public void init() {
        metodoPagoDAO = new MetodoPagoDAO();
    }

    /**
     * Procesa las solicitudes HTTP GET para listar los métodos de pago registrados.
     *
     * Recupera la lista completa desde la base de datos, la almacena como atributo
     * de la petición y transfiere el control a la vista unificada de administración,
     * indicando que se debe mostrar la pestaña de métodos de pago.
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
            List<MetodoPago> metodosPago = metodoPagoDAO.listarTodos();
            request.setAttribute("metodosPago", metodosPago);
            request.getRequestDispatcher("/Administrador/org-categorias.jsp?tab=metodosPago")
                    .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "No se pudieron cargar los métodos de pago. Intenta de nuevo.");
            request.getRequestDispatcher("/Administrador/org-categorias.jsp")
                    .forward(request, response);
        }
    }

    /**
     * Procesa las solicitudes HTTP POST para ejecutar operaciones de modificación sobre métodos de pago.
     *
     * Según el valor del parámetro {@code action}, ejecuta una de las siguientes operaciones:
     * - "guardar": crea un nuevo método de pago validando que el nombre no esté vacío
     * - "actualizar": modifica un método existente validando ID y nombre
     * - "eliminar": remueve un método de pago validando que se proporcione su ID
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
        // El parámetro "action" viene del campo oculto en el formulario y define la operación.
        String action = request.getParameter("action");
        try {
            if ("guardar".equals(action)) {
                String nombre = request.getParameter("nombre");
                // Validación: el nombre es obligatorio antes de guardar.
                if (nombre == null || nombre.trim().isEmpty())
                    throw new Exception("El nombre del método de pago es obligatorio.");

                MetodoPago mp = new MetodoPago();
                mp.setNombre(nombre.trim());
                metodoPagoDAO.guardar(mp);
                // sendRedirect evita que al refrescar la página se reenvíe el formulario.
                response.sendRedirect(request.getContextPath() + "/MetodoPagoServlet?msg=creado");

            } else if ("actualizar".equals(action)) {
                String idStr  = request.getParameter("id");
                String nombre = request.getParameter("nombre");
                // Validación: se necesitan id y nombre para actualizar.
                if (idStr == null || nombre == null || nombre.trim().isEmpty())
                    throw new Exception("Datos inválidos para actualizar.");

                MetodoPago mp = new MetodoPago();
                // parseInt convierte el String del parámetro a int; lanza excepción si no es numérico.
                mp.setMetodoPagoId(Integer.parseInt(idStr));
                mp.setNombre(nombre.trim());
                metodoPagoDAO.actualizar(mp);
                response.sendRedirect(request.getContextPath() + "/MetodoPagoServlet?msg=actualizado");

            } else if ("eliminar".equals(action)) {
                String idStr = request.getParameter("id");
                if (idStr == null) throw new Exception("No se recibió el ID para eliminar.");

                metodoPagoDAO.eliminar(Integer.parseInt(idStr));
                response.sendRedirect(request.getContextPath() + "/MetodoPagoServlet?msg=eliminado");

            } else {
                response.sendRedirect(request.getContextPath() + "/MetodoPagoServlet");
            }

        } catch (Exception e) {
            // forward mantiene los atributos del request, permitiendo pasar el error al JSP.
            // Con sendRedirect la petición termina y se pierde el atributo "error".
            request.setAttribute("error", e.getMessage());
            try {
                request.setAttribute("metodosPago", metodoPagoDAO.listarTodos());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            request.getRequestDispatcher("/Administrador/org-categorias.jsp?tab=metodosPago")
                    .forward(request, response);
        }
    }
}