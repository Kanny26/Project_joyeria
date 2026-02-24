package controller;

import dao.VentaDAO;
import dao.ProductoDAO;
import dao.ClienteDAO;
import model.Venta;
import model.DetalleVenta;
import model.Producto;
import model.Usuario;
import utils.PDFGenerator;
import utils.ValidadorVentas;
import utils.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet(urlPatterns = {
    "/Administrador/ventas/listar",
    "/Administrador/ventas/ver",
    "/Administrador/ventas/editar",
    "/Administrador/ventas/descargar-factura",
    "/Administrador/ventas/buscar"
})
@MultipartConfig
public class VentaServlet extends HttpServlet {

    private VentaDAO ventaDAO;
    private SessionUtil sessionUtil;

    @Override
    public void init() {
        ventaDAO = new VentaDAO();
        sessionUtil = new SessionUtil();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {

        String ruta = req.getServletPath();
        HttpSession session = req.getSession(false);

        try {
            // Validar sesión de administrador
            if (session == null || session.getAttribute("admin") == null) {
                resp.sendRedirect(req.getContextPath() + "/Administrador/inicio-sesion.jsp");
                return;
            }

            switch (ruta) {
                case "/Administrador/ventas/listar":
                    List<Venta> ventas = ventaDAO.listarVentas();
                    req.setAttribute("ventas", ventas);
                    req.setAttribute("totalVentas", ventaDAO.contarVentas());
                    req.setAttribute("pendientes", ventaDAO.contarPendientes());
                    req.setAttribute("pagoEfectivo", ventaDAO.contarPorMetodo("efectivo"));
                    req.setAttribute("pagoTransferencia", ventaDAO.contarPorMetodo("tarjeta"));
                    req.getRequestDispatcher("/WEB-INF/views/Administrador/ventas/listar_ventas.jsp")
                       .forward(req, resp);
                    break;

                case "/Administrador/ventas/ver":
                    String idParam = req.getParameter("id");
                    if (idParam == null || !idParam.matches("\\d+")) {
                        req.setAttribute("error", "ID de venta inválido");
                        req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
                        return;
                    }
                    
                    int ventaId = Integer.parseInt(idParam);
                    Venta venta = ventaDAO.obtenerPorId(ventaId);
                    
                    if (venta == null) {
                        req.setAttribute("error", "Venta no encontrada");
                        req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
                        return;
                    }
                    
                    req.setAttribute("venta", venta);
                    req.getRequestDispatcher("/WEB-INF/views/Administrador/ventas/ver_venta.jsp")
                       .forward(req, resp);
                    break;

                case "/Administrador/ventas/editar":
                    List<Venta> ventasEdit = ventaDAO.listarVentas();
                    req.setAttribute("ventas", ventasEdit);
                    req.setAttribute("totalVentas", ventaDAO.contarVentas());
                    req.setAttribute("pendientes", ventaDAO.contarPendientes());
                    req.setAttribute("pagoEfectivo", ventaDAO.contarPorMetodo("efectivo"));
                    req.setAttribute("pagoTransferencia", ventaDAO.contarPorMetodo("tarjeta"));
                    req.getRequestDispatcher("/WEB-INF/views/Administrador/ventas/editar_venta.jsp")
                       .forward(req, resp);
                    break;

                case "/Administrador/ventas/descargar-factura":
                    int idFactura = Integer.parseInt(req.getParameter("id"));
                    Venta factura = ventaDAO.obtenerPorId(idFactura);
                    
                    if (factura == null) {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Factura no encontrada");
                        return;
                    }

                    byte[] pdf = PDFGenerator.generarFacturaPDF(factura);
                    resp.setContentType("application/pdf");
                    resp.setHeader("Content-Disposition", 
                        "attachment; filename=factura_" + idFactura + ".pdf");
                    resp.setContentLength(pdf.length);
                    resp.getOutputStream().write(pdf);
                    resp.flushBuffer();
                    return;

                case "/Administrador/ventas/buscar":
                    String criterio = req.getParameter("q");
                    String tipo = req.getParameter("tipo");
                    List<Venta> resultados = ventaDAO.buscarVentas(
                        criterio, tipo, null, null, 0 // 0 = sin filtro de vendedor
                    );
                    req.setAttribute("ventas", resultados);
                    req.setAttribute("criterio", criterio);
                    req.getRequestDispatcher("/WEB-INF/views/Administrador/ventas/listar_ventas.jsp")
                       .forward(req, resp);
                    break;

                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            req.setAttribute("error", "Error: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String ruta = req.getServletPath();

        try {
            if (req.getSession().getAttribute("admin") == null) {
                resp.sendRedirect(req.getContextPath() + "/Administrador/inicio-sesion.jsp");
                return;
            }

            switch (ruta) {
                case "/Administrador/ventas/editar":
                    int ventaId = Integer.parseInt(req.getParameter("ventaId"));
                    String nuevoEstado = req.getParameter("estado");
                    ventaDAO.actualizarPagoVenta(ventaId, null); // Solo actualiza estado
                    resp.sendRedirect(req.getContextPath() + "/Administrador/ventas/listar");
                    break;

                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            req.setAttribute("error", "Error al procesar: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }
}