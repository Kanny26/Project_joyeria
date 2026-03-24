package controller;

import dao.PostventaDAO;
import dao.VentaDAO;

import model.Administrador;
import model.CasoPostventa;
import model.Venta;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/**
 * Servlet encargado de la gestión de ventas y casos de postventa
 * dentro del módulo de administrador.
 * 
 * Maneja:
 * - Listado de ventas
 * - Búsqueda de ventas
 * - Visualización de ventas
 * - Gestión de casos postventa
 */
@WebServlet(urlPatterns = {
    "/Administrador/ventas/listar",
    "/Administrador/ventas/ver",
    "/Administrador/ventas/buscar",
    "/Administrador/postventa/listar",
    "/Administrador/postventa/ver",
    "/Administrador/postventa/estado",
    "/Administrador/postventa/gestionar"
})
public class AdminVentaServlet extends HttpServlet {

    private VentaDAO ventaDAO;
    private PostventaDAO postventaDAO;

    /**
     * Método init: se ejecuta al iniciar el servlet.
     * Inicializa los DAO.
     */
    @Override
    public void init() {
        ventaDAO     = new VentaDAO();
        postventaDAO = new PostventaDAO();
    }

    /**
     * Método doGet: maneja las solicitudes GET
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // Validar si hay sesión activa de administrador
        if (!validarSesionAdmin(req, resp)) return;

        // Obtener la ruta solicitada
        String ruta = req.getServletPath();

        try {
            switch (ruta) {

                // Obtener todas las ventas
                case "/Administrador/ventas/listar": {

                    // Obtener todas las ventas
                    List<Venta> ventas = ventaDAO.listarVentas();
                    if (ventas == null) ventas = new ArrayList<>();

                    // Para cada venta, cargar sus casos de postventa
                    for (Venta v : ventas) {
                        try {
                            List<CasoPostventa> casos = postventaDAO.listarPorVenta(v.getVentaId());
                            v.setCasosPostventa(casos != null ? casos : new ArrayList<>());
                        } catch (Exception e) {
                            // En caso de error, asigna lista vacía
                            v.setCasosPostventa(new ArrayList<>());
                        }
                    }

                    // Enviar datos a la vista
                    req.setAttribute("ventas", ventas);
                    req.setAttribute("totalVentas", ventas.size());
                    req.setAttribute("totalPendientes", ventaDAO.contarPendientes());

                    // Redirigir a la vista JSP
                    req.getRequestDispatcher("/Administrador/ventas.jsp").forward(req, resp);
                    break;
                }

                // Listar todos los casos de postventa
                case "/Administrador/postventa/listar": {
                    List<CasoPostventa> casos = postventaDAO.listarTodos();

                    req.setAttribute("casos", casos != null ? casos : new ArrayList<>());
                    req.getRequestDispatcher("/Administrador/ventas/listar_postventa.jsp").forward(req, resp);
                    break;
                }

                // Ver detalle de una venta
                case "/Administrador/ventas/ver": {
                    int id = parseId(req.getParameter("id"));

                    Venta v = ventaDAO.obtenerPorId(id);

                    // Validar si la venta existe
                    if (v == null) {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Venta no encontrada");
                        return;
                    }

                    req.setAttribute("venta", v);
                    req.getRequestDispatcher("/Administrador/ventas/ver_venta.jsp").forward(req, resp);
                    break;
                }

                // Buscar ventas por criterio, tipo y rango de fechas
                case "/Administrador/ventas/buscar": {

                    // Obtener parámetros de búsqueda
                    String criterio    = req.getParameter("q");
                    String tipo        = req.getParameter("tipo");
                    String fechaIniStr = req.getParameter("fechaInicio");
                    String fechaFinStr = req.getParameter("fechaFin");

                    // Convertir fechas
                    java.util.Date fechaInicio = parseFecha(fechaIniStr);
                    java.util.Date fechaFin    = parseFecha(fechaFinStr);

                    // Realizar búsqueda
                    List<Venta> resultado = ventaDAO.buscarVentas(criterio, tipo, fechaInicio, fechaFin, 0);

                    // Enviar resultados a la vista
                    req.setAttribute("ventas", resultado != null ? resultado : new ArrayList<>());
                    req.setAttribute("criterio", criterio);
                    req.setAttribute("tipo", tipo);
                    req.setAttribute("fechaInicio", fechaIniStr);
                    req.setAttribute("fechaFin", fechaFinStr);

                    req.getRequestDispatcher("/Administrador/ventas/listar_ventas.jsp").forward(req, resp);
                    break;
                }

                // Ver detalle de un caso postventa
                case "/Administrador/postventa/ver": {
                    int casoId = parseId(req.getParameter("id"));

                    CasoPostventa caso = postventaDAO.obtenerPorId(casoId);

                    if (caso == null) {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Caso no encontrado");
                        return;
                    }

                    req.setAttribute("caso", caso);
                    req.getRequestDispatcher("/Administrador/ventas/ver_caso_postventa.jsp").forward(req, resp);
                    break;
                }

                // Ruta no reconocida
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            e.printStackTrace();

            // Manejo general de errores
            req.setAttribute("error", "Ocurrió un error al procesar la solicitud.");
            req.getRequestDispatcher("/Administrador/admin-principal.jsp").forward(req, resp);
        }
    }

    /**
     * Método doPost: maneja actualizaciones (estado de postventa)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // Configurar codificación
        req.setCharacterEncoding("UTF-8");

        // Validar sesión
        if (!validarSesionAdmin(req, resp)) return;

        // Obtener administrador en sesión
        Administrador admin = (Administrador) req.getSession().getAttribute("admin");
        int adminId = admin != null ? admin.getId() : -1;

        String ruta = req.getServletPath();

        try {
            // Solo para rutas de actualización de estado
            if ("/Administrador/postventa/estado".equals(ruta) || "/Administrador/postventa/gestionar".equals(ruta)) {

                int casoId = parseId(req.getParameter("casoId"));

                // Obtener nuevo estado (según nombre del parámetro)
                String nuevoEstado = req.getParameter("nuevoEstado") != null
                    ? req.getParameter("nuevoEstado")
                    : req.getParameter("estado");

                String observacion = req.getParameter("observacion");

                // Validar estados permitidos
                if (!java.util.Arrays.asList("en_proceso", "aprobado", "cancelado").contains(nuevoEstado)) {
                    req.setAttribute("error", "El estado seleccionado no es válido.");
                    req.getRequestDispatcher("/Administrador/postventa/ver?id=" + casoId).forward(req, resp);
                    return;
                }

                // Obtener caso
                CasoPostventa caso = postventaDAO.obtenerPorId(casoId);

                if (caso == null) {
                    req.setAttribute("error", "No se encontró el caso postventa.");
                    req.getRequestDispatcher("/Administrador/postventa/ver?id=" + casoId).forward(req, resp);
                    return;
                }

                // Actualizar estado en base de datos
                postventaDAO.actualizarEstado(casoId, nuevoEstado, observacion, adminId);

                // Redirigir con mensaje de éxito
                resp.sendRedirect(req.getContextPath() + "/Administrador/postventa/ver?id=" + casoId + "&exito=1");

            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            e.printStackTrace();

            req.setAttribute("error", "No se pudo actualizar el estado del caso.");
            req.getRequestDispatcher("/Administrador/admin-principal.jsp").forward(req, resp);
        }
    }

    /**
     * Valida que exista una sesión activa de administrador
     */
    private boolean validarSesionAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("admin") == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return false;
        }
        return true;
    }

    /**
     * Convierte un parámetro a entero de forma segura
     */
    private int parseId(String param) {
        if (param == null || !param.matches("\\d+")) return -1;
        try { 
            return Integer.parseInt(param); 
        } catch (NumberFormatException e) { 
            return -1; 
        }
    }

    /**
     * Convierte una fecha en formato String (yyyy-MM-dd) a Date
     */
    private java.util.Date parseFecha(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(s);
        } catch (Exception e) {
            return null;
        }
    }
}