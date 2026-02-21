package controller;

import dao.VentaDAO;
import dao.CasoPostVentaDAO;      // ← mismo nombre que el DAO real
import model.Venta;
import model.CasoPostVenta;       // ← mismo nombre que el modelo real

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {
    "/Administrador/ventas/listar",
    "/Administrador/ventas/ver",
    "/Administrador/ventas/editar",
    "/Administrador/ventas/postventa",
    "/Administrador/ventas/caso"
})
public class VentaServlet extends HttpServlet {

    private final VentaDAO         ventaDAO = new VentaDAO();
    private final CasoPostVentaDAO casoDAO  = new CasoPostVentaDAO(); // ← instancia correcta

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
                    List<CasoPostVenta> casos = casoDAO.listarCasos(); // ← instancia, no estático
                    req.setAttribute("casos",           casos);
                    req.setAttribute("totalCasos",      casoDAO.contarCasos());
                    req.setAttribute("casosPendientes", casoDAO.contarPendientes());
                    despachar(req, resp, "/WEB-INF/views/Administrador/ventas/postventa.jsp");
                }

                case "/Administrador/ventas/caso" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    CasoPostVenta caso = casoDAO.obtenerPorId(id); // ← tipo correcto
                    req.setAttribute("caso",  caso);
                    req.setAttribute("casos", casoDAO.listarCasos());
                    despachar(req, resp, "/WEB-INF/views/Administrador/ventas/casos_postventa.jsp");
                }

                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            throw new ServletException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String ruta = req.getServletPath();

        try {
            switch (ruta) {

                case "/Administrador/ventas/editar" -> {
                    int    ventaId     = Integer.parseInt(req.getParameter("ventaId"));
                    String nuevoEstado = req.getParameter("estado");
                    ventaDAO.actualizarEstado(ventaId, nuevoEstado);
                    resp.sendRedirect(req.getContextPath() + "/Administrador/ventas/listar");
                }

                case "/Administrador/ventas/caso" -> {
                    int    casoId      = Integer.parseInt(req.getParameter("casoId"));
                    String nuevoEstado = req.getParameter("estado");
                    String observacion = req.getParameter("observacion");
                    casoDAO.actualizarEstado(casoId, nuevoEstado, observacion);
                    resp.sendRedirect(req.getContextPath() + "/Administrador/ventas/postventa");
                }

                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            throw new ServletException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    private void despachar(HttpServletRequest req, HttpServletResponse resp, String vista)
            throws ServletException, IOException {
        req.getRequestDispatcher(vista).forward(req, resp);
    }
}