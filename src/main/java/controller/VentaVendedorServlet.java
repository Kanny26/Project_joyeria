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
    "/Vendedor/ventas/registrar",
    "/Vendedor/ventas/listar-mis-ventas",
    "/Vendedor/ventas/ver",
    "/Vendedor/ventas/descargar-factura",
    "/Vendedor/ventas/buscar",
    "/Vendedor/ventas/abonar-saldo"
})
@MultipartConfig
public class VentaVendedorServlet extends HttpServlet {

    private VentaDAO ventaDAO;
    private ProductoDAO productoDAO;
    private ClienteDAO clienteDAO;
    private SessionUtil sessionUtil;

    @Override
    public void init() {
        ventaDAO = new VentaDAO();
        productoDAO = new ProductoDAO();
        clienteDAO = new ClienteDAO();
        sessionUtil = new SessionUtil();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {

        String ruta = req.getServletPath();
        HttpSession session = req.getSession(false);

        try {
            // Validar sesión de vendedor
            if (!sessionUtil.validarSesionVendedor(req, resp)) {
                return;
            }

            Usuario vendedor = (Usuario) session.getAttribute("usuario");

            switch (ruta) {
                case "/Vendedor/ventas/registrar":
                    req.setAttribute("productos", productoDAO.listarProductosDisponibles());
                    req.setAttribute("metodosPago", Arrays.asList("efectivo", "tarjeta"));
                    req.getRequestDispatcher("/WEB-INF/views/Vendedor/registrar_venta.jsp")
                       .forward(req, resp);
                    break;

                case "/Vendedor/ventas/listar-mis-ventas":
                    List<Venta> misVentas = ventaDAO.listarPorVendedor(vendedor.getUsuarioId());
                    req.setAttribute("ventas", misVentas);
                    req.setAttribute("totalVentas", misVentas.size());
                    req.getRequestDispatcher("/WEB-INF/views/Vendedor/listar_mis_ventas.jsp")
                       .forward(req, resp);
                    break;

                case "/Vendedor/ventas/ver":
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
                    
                    // Validar que el vendedor solo vea sus propias ventas
                    if (venta.getUsuarioId() != vendedor.getUsuarioId()) {
                        req.setAttribute("error", "No tienes permiso para ver esta venta");
                        req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
                        return;
                    }
                    
                    req.setAttribute("venta", venta);
                    req.getRequestDispatcher("/WEB-INF/views/Vendedor/ver_venta.jsp")
                       .forward(req, resp);
                    break;

                case "/Vendedor/ventas/descargar-factura":
                    int idFactura = Integer.parseInt(req.getParameter("id"));
                    Venta factura = ventaDAO.obtenerPorId(idFactura);
                    
                    if (factura == null || factura.getUsuarioId() != vendedor.getUsuarioId()) {
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado");
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

                case "/Vendedor/ventas/buscar":
                    String criterio = req.getParameter("q");
                    String tipo = req.getParameter("tipo");
                    List<Venta> resultados = ventaDAO.buscarVentas(
                        criterio, tipo, null, null, vendedor.getUsuarioId()
                    );
                    req.setAttribute("ventas", resultados);
                    req.setAttribute("criterio", criterio);
                    req.getRequestDispatcher("/WEB-INF/views/Vendedor/listar_mis_ventas.jsp")
                       .forward(req, resp);
                    break;

                case "/Vendedor/ventas/abonar-saldo":
                    int vId = Integer.parseInt(req.getParameter("ventaId"));
                    Venta vSaldo = ventaDAO.obtenerPorId(vId);
                    if (vSaldo != null && vSaldo.getUsuarioId() == vendedor.getUsuarioId()) {
                        req.setAttribute("venta", vSaldo);
                        req.setAttribute("metodosPago", Arrays.asList("efectivo", "tarjeta"));
                        req.getRequestDispatcher("/WEB-INF/views/Vendedor/abonar_saldo.jsp")
                           .forward(req, resp);
                    } else {
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado");
                    }
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
        HttpSession session = req.getSession(false);

        try {
            if (!sessionUtil.validarSesionVendedor(req, resp)) {
                return;
            }

            Usuario vendedor = (Usuario) session.getAttribute("usuario");

            switch (ruta) {
                case "/Vendedor/ventas/registrar":
                    procesarRegistroVenta(req, resp, vendedor);
                    break;

                case "/Vendedor/ventas/abonar-saldo":
                    procesarAbonoSaldo(req, resp, vendedor);
                    break;

                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            req.setAttribute("error", "Error al procesar: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }

    private void procesarRegistroVenta(HttpServletRequest req, HttpServletResponse resp, Usuario vendedor) 
            throws Exception {

        String nombreCliente = req.getParameter("nombreCliente");
        String telefonoCliente = req.getParameter("telefonoCliente");
        String emailCliente = req.getParameter("emailCliente");
        String fechaStr = req.getParameter("fechaEmision");
        String metodoPago = req.getParameter("metodoPago");
        String modalidad = req.getParameter("modalidad");
        BigDecimal montoAnticipo = null;
        BigDecimal saldoPendiente = null;

        if ("anticipo".equals(modalidad)) {
            montoAnticipo = new BigDecimal(req.getParameter("montoAnticipo"));
        }

        // Validaciones
        Map<String, String> errores = ValidadorVentas.validarRegistroVenta(
            nombreCliente, telefonoCliente, fechaStr, modalidad, montoAnticipo, req
        );

        if (!errores.isEmpty()) {
            req.setAttribute("errores", errores);
            req.setAttribute("productos", productoDAO.listarProductosDisponibles());
            req.getRequestDispatcher("/WEB-INF/views/Vendedor/registrar_venta.jsp").forward(req, resp);
            return;
        }

        // Registrar o obtener cliente
        int clienteId = clienteDAO.registrarOObtenerCliente(nombreCliente, telefonoCliente, emailCliente);

        // Procesar productos
        String[] productoIds = req.getParameterValues("productoId");
        String[] cantidades = req.getParameterValues("cantidad");
        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < productoIds.length; i++) {
            int prodId = Integer.parseInt(productoIds[i]);
            int cant = Integer.parseInt(cantidades[i]);

            Producto prod = productoDAO.obtenerProductoConStock(prodId);
            if (prod == null || prod.getStock() < cant) {
                throw new Exception("Stock insuficiente para: " + prod.getNombre());
            }

            DetalleVenta detalle = new DetalleVenta(
                prodId, prod.getNombre(), cant, prod.getPrecioVenta(), prod.getStock()
            );
            detalles.add(detalle);
            total = total.add(detalle.getSubtotal());
        }

        // Calcular anticipo y saldo
        if ("anticipo".equals(modalidad) && montoAnticipo != null) {
            if (montoAnticipo.compareTo(total) >= 0) {
                throw new Exception("El anticipo no puede ser mayor o igual al total");
            }
            saldoPendiente = total.subtract(montoAnticipo);
        }

        Date fechaEmision = new SimpleDateFormat("yyyy-MM-dd").parse(fechaStr);
        Venta nuevaVenta = new Venta(
            vendedor.getUsuarioId(), clienteId, fechaEmision, total, metodoPago
        );
        nuevaVenta.setDetalles(detalles);

        // Insertar venta con transacción
        int ventaIdGenerado = ventaDAO.insertar(nuevaVenta, detalles, modalidad, montoAnticipo, saldoPendiente);

        if (ventaIdGenerado > 0) {
            req.setAttribute("mensaje", "✅ Venta registrada exitosamente. Factura #" + ventaIdGenerado);
            req.setAttribute("venta", ventaDAO.obtenerPorId(ventaIdGenerado));
            req.getRequestDispatcher("/WEB-INF/views/Vendedor/venta_confirmada.jsp").forward(req, resp);
        } else {
            throw new Exception("Error al registrar la venta");
        }
    }

    private void procesarAbonoSaldo(HttpServletRequest req, HttpServletResponse resp, Usuario vendedor) 
            throws Exception {

        int ventaId = Integer.parseInt(req.getParameter("ventaId"));
        BigDecimal montoAbono = new BigDecimal(req.getParameter("montoAbono"));

        Venta venta = ventaDAO.obtenerPorId(ventaId);
        if (venta == null || venta.getUsuarioId() != vendedor.getUsuarioId()) {
            throw new Exception("Venta no encontrada o sin permisos");
        }
        
        if (venta.getSaldoPendiente() == null || montoAbono.compareTo(venta.getSaldoPendiente()) > 0) {
            throw new Exception("El monto del abono no puede exceder el saldo pendiente");
        }

        boolean exito = ventaDAO.actualizarPagoVenta(ventaId, montoAbono);

        if (exito) {
            resp.sendRedirect(req.getContextPath() + "/Vendedor/ventas/listar-mis-ventas");
        } else {
            throw new Exception("Error al procesar el abono");
        }
    }
}