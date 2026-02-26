package controller;

import dao.PostventaDAO;
import dao.VentaDAO;
import model.CasoPostventa;
import model.Usuario;
import model.Venta;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Servlet para el Administrador - módulo de ventas y postventa.
 *  GET  /Administrador/ventas/listar      → todas las ventas
 *  GET  /Administrador/ventas/ver         → detalle de una venta
 *  GET  /Administrador/ventas/buscar      → filtros de búsqueda
 *  GET  /Administrador/postventa/listar   → todos los casos postventa
 *  GET  /Administrador/postventa/ver      → detalle de un caso
 *  POST /Administrador/postventa/estado   → actualizar estado de caso
 */
@WebServlet(urlPatterns = {
    "/Administrador/ventas/listar",
    "/Administrador/ventas/ver",
    "/Administrador/ventas/buscar",
    "/Administrador/postventa/listar",
    "/Administrador/postventa/ver",
    "/Administrador/postventa/estado"
})
public class AdminVentaServlet extends HttpServlet {

    private VentaDAO     ventaDAO;
    private PostventaDAO postventaDAO;

    @Override
    public void init() {
        ventaDAO     = new VentaDAO();
        postventaDAO = new PostventaDAO();
    }

    // ═══════════════════════════════════════════════════════════
    // GET
    // ═══════════════════════════════════════════════════════════
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!validarSesionAdmin(req, resp)) return;
        String ruta = req.getServletPath();

        try {
            switch (ruta) {

                case "/Administrador/ventas/listar": {
                    List<Venta> ventas = ventaDAO.listarVentas();
                    req.setAttribute("ventas", ventas);
                    req.setAttribute("totalVentas",    ventas.size());
                    req.setAttribute("totalPendientes", ventaDAO.contarPendientes());
                    req.getRequestDispatcher("/WEB-INF/views/Administrador/listar_ventas.jsp")
                       .forward(req, resp);
                    break;
                }

                case "/Administrador/ventas/ver": {
                    int id = parseId(req.getParameter("id"));
                    Venta v = ventaDAO.obtenerPorId(id);
                    if (v == null) {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Venta no encontrada");
                        return;
                    }
                    req.setAttribute("venta", v);
                    req.getRequestDispatcher("/WEB-INF/views/Administrador/ver_venta_admin.jsp")
                       .forward(req, resp);
                    break;
                }

                case "/Administrador/ventas/buscar": {
                    String criterio    = req.getParameter("q");
                    String tipo        = req.getParameter("tipo");
                    String fechaIniStr = req.getParameter("fechaInicio");
                    String fechaFinStr = req.getParameter("fechaFin");

                    Date fechaInicio = parseFecha(fechaIniStr);
                    Date fechaFin    = parseFecha(fechaFinStr);

                    List<Venta> resultado = ventaDAO.buscarVentas(criterio, tipo, fechaInicio, fechaFin, 0);
                    req.setAttribute("ventas", resultado);
                    req.setAttribute("criterio", criterio);
                    req.setAttribute("tipo", tipo);
                    req.getRequestDispatcher("/WEB-INF/views/Administrador/listar_ventas.jsp")
                       .forward(req, resp);
                    break;
                }

                case "/Administrador/postventa/listar": {
                    List<CasoPostventa> casos = postventaDAO.listarTodos();
                    req.setAttribute("casos", casos);
                    req.getRequestDispatcher("/WEB-INF/views/Administrador/listar_postventa.jsp")
                       .forward(req, resp);
                    break;
                }

                case "/Administrador/postventa/ver": {
                    int casoId = parseId(req.getParameter("id"));
                    CasoPostventa caso = postventaDAO.obtenerPorId(casoId);
                    if (caso == null) {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Caso no encontrado");
                        return;
                    }
                    req.setAttribute("caso", caso);
                    req.getRequestDispatcher("/WEB-INF/views/Administrador/ver_caso_postventa.jsp")
                       .forward(req, resp);
                    break;
                }

                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            manejarError(req, resp, e);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // POST
    // ═══════════════════════════════════════════════════════════
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        if (!validarSesionAdmin(req, resp)) return;
        String ruta = req.getServletPath();

        try {
            if ("/Administrador/postventa/estado".equals(ruta)) {
                int    casoId       = parseId(req.getParameter("casoId"));
                String nuevoEstado  = req.getParameter("estado");
                String observacion  = req.getParameter("observacion");

                if (!java.util.Arrays.asList("en_proceso", "aprobado", "cancelado").contains(nuevoEstado)) {
                    reenviarConError(req, resp, "Estado inválido.",
                            "/Administrador/postventa/ver?id=" + casoId);
                    return;
                }
                postventaDAO.actualizarEstado(casoId, nuevoEstado, observacion);
                resp.sendRedirect(req.getContextPath() + "/Administrador/postventa/ver?id=" + casoId + "&exito=1");
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            manejarError(req, resp, e);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // AUXILIARES
    // ═══════════════════════════════════════════════════════════
    private boolean validarSesionAdmin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return false;
        }
        Usuario u = (Usuario) session.getAttribute("usuario");
        if (!"administrador".equals(u.getRol())) {
            try { resp.sendError(HttpServletResponse.SC_FORBIDDEN); } catch (Exception ignored) {}
            return false;
        }
        return true;
    }

    private int parseId(String param) {
        if (param == null || !param.matches("\\d+")) return -1;
        return Integer.parseInt(param);
    }

    private Date parseFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.isBlank()) return null;
        try { return new SimpleDateFormat("yyyy-MM-dd").parse(fechaStr); }
        catch (Exception e) { return null; }
    }

    private void reenviarConError(HttpServletRequest req, HttpServletResponse resp,
                                  String msg, String vista) throws ServletException, IOException {
        req.setAttribute("error", msg);
        req.getRequestDispatcher(vista).forward(req, resp);
    }

    private void manejarError(HttpServletRequest req, HttpServletResponse resp, Exception e)
            throws ServletException, IOException {
        e.printStackTrace();
        req.setAttribute("error", e.getMessage());
        req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
    }
}
