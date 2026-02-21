package controller;

import dao.VentaDAO;
import dao.CasoPostventaDAO;
import model.Venta;
import model.CasoPostventa;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Controlador principal del módulo de Ventas.
 *
 * Rutas manejadas:
 *  GET  /Administrador/ventas/listar       → listar_ventas.jsp
 *  GET  /Administrador/ventas/ver          → ver_venta.jsp      (?id=X)
 *  GET  /Administrador/ventas/editar       → editar_venta.jsp   (?id=X)
 *  POST /Administrador/ventas/editar       → actualiza estado y redirige
 *  GET  /Administrador/ventas/postventa    → postventa.jsp
 *  GET  /Administrador/ventas/caso         → casos_postventa.jsp (?id=X)
 *  POST /Administrador/ventas/caso         → actualiza estado caso y redirige
 */
@WebServlet(urlPatterns = {
    "/Administrador/ventas/listar",
    "/Administrador/ventas/ver",
    "/Administrador/ventas/editar",
    "/Administrador/ventas/postventa",
    "/Administrador/ventas/caso"
})
public class VentaServlet extends HttpServlet {

    private final VentaDAO ventaDAO           = new VentaDAO();
    private final CasoPostventaDAO casoDAO    = new CasoPostventaDAO();

    // ─────────────────────────────────────────────
    //  GET
    // ─────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String ruta = req.getServletPath();

        try {
            switch (ruta) {

                case "/Administrador/ventas/listar" -> {
                    List<Venta> ventas = ventaDAO.listarVentas();
                    req.setAttribute("ventas",            ventas);
                    req.setAttribute("totalVentas",       ventaDAO.contarVentas());
                    req.setAttribute("pendientes",        ventaDAO.contarPendientes());
                    req.setAttribute("pagoEfectivo",      ventaDAO.contarPorMetodo("efectivo"));
                    req.setAttribute("pagoTransferencia", ventaDAO.contarPorMetodo("tarjeta"));
                    despachar(req, resp, "/WEB-INF/views/Administrador/ventas/listar_ventas.jsp");
                }

                case "/Administrador/ventas/ver" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    Venta venta = ventaDAO.obtenerPorId(id);
                    req.setAttribute("venta", venta);
                    despachar(req, resp, "/WEB-INF/views/Administrador/ventas/ver_venta.jsp");
                }

                case "/Administrador/ventas/editar" -> {
                    List<Venta> ventas = ventaDAO.listarVentas();
                    req.setAttribute("ventas",            ventas);
                    req.setAttribute("totalVentas",       ventaDAO.contarVentas());
                    req.setAttribute("pendientes",        ventaDAO.contarPendientes());
                    req.setAttribute("pagoEfectivo",      ventaDAO.contarPorMetodo("efectivo"));
                    req.setAttribute("pagoTransferencia", ventaDAO.contarPorMetodo("tarjeta"));
                    despachar(req, resp, "/WEB-INF/views/Administrador/ventas/editar_venta.jsp");
                }

                case "/Administrador/ventas/postventa" -> {
                    List<CasoPostventa> casos = casoDAO.listarCasos();
                    req.setAttribute("casos",          casos);
                    req.setAttribute("totalCasos",     casoDAO.contarCasos());
                    req.setAttribute("casosPendientes",casoDAO.contarPendientes());
                    despachar(req, resp, "/WEB-INF/views/Administrador/ventas/postventa.jsp");
                }

                case "/Administrador/ventas/caso" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    CasoPostventa caso = casoDAO.obtenerPorId(id);
                    req.setAttribute("caso", caso);
                    // Para la tabla resumen
                    List<CasoPostventa> casos = casoDAO.listarCasos();
                    req.setAttribute("casos", casos);
                    despachar(req, resp, "/WEB-INF/views/Administrador/ventas/casos_postventa.jsp");
                }

                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (SQLException e) {
            throw new ServletException("Error de base de datos", e);
        }
    }

    // ─────────────────────────────────────────────
    //  POST
    // ─────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String ruta = req.getServletPath();

        try {
            switch (ruta) {

                // Actualizar estado de una venta
                case "/Administrador/ventas/editar" -> {
                    int    ventaId    = Integer.parseInt(req.getParameter("ventaId"));
                    String nuevoEstado = req.getParameter("estado");
                    ventaDAO.actualizarEstado(ventaId, nuevoEstado);
                    resp.sendRedirect(req.getContextPath() + "/Administrador/ventas/listar");
                }

                // Actualizar estado de un caso postventa
                case "/Administrador/ventas/caso" -> {
                    int    casoId      = Integer.parseInt(req.getParameter("casoId"));
                    String nuevoEstado = req.getParameter("estado");
                    String observacion = req.getParameter("observacion");
                    casoDAO.actualizarEstado(casoId, nuevoEstado, observacion);
                    resp.sendRedirect(req.getContextPath() + "/Administrador/ventas/postventa");
                }

                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (SQLException e) {
            throw new ServletException("Error de base de datos", e);
        }
    }

    // ─────────────────────────────────────────────
    //  AUXILIAR
    // ─────────────────────────────────────────────
    private void despachar(HttpServletRequest req, HttpServletResponse resp, String vista)
            throws ServletException, IOException {
        req.getRequestDispatcher(vista).forward(req, resp);
    }
}