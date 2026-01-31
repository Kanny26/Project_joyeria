package controller;

import dao.ProveedorDAO;
import model.Proveedor;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Servlet encargado de la gestión de proveedores.
 * Maneja listado, creación, historial y cambio de estado.
 */
@WebServlet("/Administrador/proveedores/*")
public class ProveedorServlet extends HttpServlet {

    private ProveedorDAO dao = new ProveedorDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();

        // Ruta base o vacía → listar proveedores
        if (path == null || path.equals("/")) {
            listarProveedores(req, resp);
            return;
        }

        switch (path) {
            case "/listar":
                listarProveedores(req, resp);
                break;

            case "/agregar":
                req.getRequestDispatcher("/Administrador/proveedores/agregar.jsp")
                        .forward(req, resp);
                break;

            case "/compras":
                req.getRequestDispatcher("/Administrador/proveedores/compras.jsp")
                        .forward(req, resp);
                break;

            case "/historial":
                req.getRequestDispatcher("/Administrador/proveedores/historial.jsp")
                        .forward(req, resp);
                break;

            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");

        /* =========================
           GUARDAR PROVEEDOR
           ========================= */
        if ("guardar".equals(action)) {

            Proveedor p = new Proveedor();
            p.setNombre(req.getParameter("nombre"));

            // Teléfonos
            String[] telefonos = req.getParameterValues("telefono");
            p.setTelefonos(
                    telefonos != null ? Arrays.asList(telefonos) : Collections.emptyList()
            );

            // Correos
            String[] correos = req.getParameterValues("correo");
            p.setCorreos(
                    correos != null ? Arrays.asList(correos) : Collections.emptyList()
            );

            // Materiales
            String[] materiales = req.getParameterValues("materiales");
            p.setMateriales(
                    materiales != null ? Arrays.asList(materiales) : Collections.emptyList()
            );

            // Fecha de inicio
            String fechaStr = req.getParameter("fechaInicio");
            p.setFechaInicio(
                    fechaStr != null && !fechaStr.isEmpty()
                            ? LocalDate.parse(fechaStr)
                            : LocalDate.now()
            );

            // Estado
            p.setEstado("activo".equals(req.getParameter("estado")));

            if (dao.guardar(p)) {
                resp.sendRedirect(
                        req.getContextPath()
                                + "/Administrador/proveedores/listar?exito=1"
                );
            } else {
                req.setAttribute("error", "Error al guardar el proveedor.");
                req.getRequestDispatcher("/Administrador/proveedores/agregar.jsp")
                        .forward(req, resp);
            }
        }

        /* =========================
           ACTUALIZAR ESTADO
           ========================= */
        else if ("actualizarEstado".equals(action)) {
            try {
                int id = Integer.parseInt(req.getParameter("id"));
                boolean estado = Boolean.parseBoolean(req.getParameter("estado"));
                dao.actualizarEstado(id, estado);
            } catch (NumberFormatException ignored) {
                // Se ignora para no romper el flujo
            }

            resp.sendRedirect(
                    req.getContextPath()
                            + "/Administrador/proveedores/listar.jsp"
            );
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Lista todos los proveedores registrados.
     */
    private void listarProveedores(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<Proveedor> proveedores = dao.listar();
        req.setAttribute("proveedores", proveedores);
        req.getRequestDispatcher("/Administrador/proveedores/listar.jsp")
                .forward(req, resp);
    }
}
