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
 * MetodoPagoDAO; reenvía a /Administrador/org-categorias.jsp (pestaña métodos de pago).
 */
@WebServlet("/MetodoPagoServlet")
public class MetodoPagoServlet extends HttpServlet {

    private MetodoPagoDAO metodoPagoDAO;

    /**
     * Inicializa el servlet e instancia el DAO de métodos de pago.
     */
    @Override
    public void init() {
        metodoPagoDAO = new MetodoPagoDAO();
    }

    /**
     * Carga el listado de métodos de pago y hace forward al JSP unificado con {@code tab=metodosPago}.
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
