package controller;

import dao.ClienteDAO;
import dao.PostventaDAO;
import dao.ProductoDAO;
import dao.VentaDAO;
import model.CasoPostventa;
import model.DetalleVenta;
import model.Producto;
import model.Usuario;
import model.Venta;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Servlet para el Vendedor:
 *  GET  /Vendedor/ventas/registrar        → formulario nueva venta
 *  POST /Vendedor/ventas/registrar        → procesar nueva venta
 *  GET  /Vendedor/ventas/mis-ventas       → listado propio
 *  GET  /Vendedor/ventas/ver              → detalle de una venta
 *  POST /Vendedor/ventas/abonar           → abonar saldo pendiente
 *  GET  /Vendedor/postventa/registrar     → formulario caso postventa
 *  POST /Vendedor/postventa/registrar     → guardar caso postventa
 *  GET  /Vendedor/postventa/mis-casos     → listar mis casos postventa
 */
@WebServlet(urlPatterns = {
    "/Vendedor/ventas/registrar",
    "/Vendedor/ventas/mis-ventas",
    "/Vendedor/ventas/ver",
    "/Vendedor/ventas/abonar",
    "/Vendedor/postventa/registrar",
    "/Vendedor/postventa/mis-casos"
})
public class VentaVendedorServlet extends HttpServlet {

    private VentaDAO     ventaDAO;
    private ProductoDAO  productoDAO;
    private ClienteDAO   clienteDAO;
    private PostventaDAO postventaDAO;

    @Override
    public void init() {
        ventaDAO     = new VentaDAO();
        productoDAO  = new ProductoDAO();
        clienteDAO   = new ClienteDAO();
        postventaDAO = new PostventaDAO();
    }

    // ═══════════════════════════════════════════════════════════
    // GET
    // ═══════════════════════════════════════════════════════════
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!validarSesionVendedor(req, resp)) return;
        Usuario vendedor = (Usuario) req.getSession().getAttribute("usuario");
        String ruta = req.getServletPath();

        try {
            switch (ruta) {

                case "/Vendedor/ventas/registrar":
                    req.getRequestDispatcher("/WEB-INF/views/Vendedor/registrar_venta.jsp")
                       .forward(req, resp);
                    break;

                case "/Vendedor/ventas/mis-ventas":
                    List<Venta> misVentas = ventaDAO.listarPorVendedor(vendedor.getUsuarioId());
                    req.setAttribute("ventas", misVentas);
                    req.getRequestDispatcher("/WEB-INF/views/Vendedor/mis_ventas.jsp")
                       .forward(req, resp);
                    break;

                case "/Vendedor/ventas/ver": {
                    int id = parseId(req.getParameter("id"));
                    Venta venta = ventaDAO.obtenerPorId(id);
                    if (venta == null || venta.getUsuarioId() != vendedor.getUsuarioId()) {
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado");
                        return;
                    }
                    req.setAttribute("venta", venta);
                    req.getRequestDispatcher("/WEB-INF/views/Vendedor/ver_venta.jsp")
                       .forward(req, resp);
                    break;
                }

                case "/Vendedor/postventa/registrar": {
                    // La venta que origina el caso viene como parámetro
                    int ventaId = parseId(req.getParameter("ventaId"));
                    Venta venta = ventaDAO.obtenerPorId(ventaId);
                    if (venta == null || venta.getUsuarioId() != vendedor.getUsuarioId()) {
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado");
                        return;
                    }
                    req.setAttribute("venta", venta);
                    req.getRequestDispatcher("/WEB-INF/views/Vendedor/registrar_postventa.jsp")
                       .forward(req, resp);
                    break;
                }

                case "/Vendedor/postventa/mis-casos":
                    List<CasoPostventa> misCasos = postventaDAO.listarPorVendedor(vendedor.getUsuarioId());
                    req.setAttribute("casos", misCasos);
                    req.getRequestDispatcher("/WEB-INF/views/Vendedor/mis_casos_postventa.jsp")
                       .forward(req, resp);
                    break;

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
        if (!validarSesionVendedor(req, resp)) return;
        Usuario vendedor = (Usuario) req.getSession().getAttribute("usuario");
        String ruta = req.getServletPath();

        try {
            switch (ruta) {
                case "/Vendedor/ventas/registrar":
                    procesarRegistroVenta(req, resp, vendedor);
                    break;
                case "/Vendedor/ventas/abonar":
                    procesarAbono(req, resp, vendedor);
                    break;
                case "/Vendedor/postventa/registrar":
                    procesarRegistroPostventa(req, resp, vendedor);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            manejarError(req, resp, e);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // LÓGICA: Registrar venta
    // ═══════════════════════════════════════════════════════════
    private void procesarRegistroVenta(HttpServletRequest req, HttpServletResponse resp,
                                       Usuario vendedor) throws Exception {

        String nombreCliente   = req.getParameter("clienteNombre");
        String telefonoCliente = req.getParameter("clienteTelefono");
        String emailCliente    = req.getParameter("clienteEmail");
        String fechaStr        = req.getParameter("fechaVenta");
        String metodoPago      = req.getParameter("metodoPago");   // efectivo | tarjeta
        String modalidad       = req.getParameter("modalidad");    // contado | anticipo

        // Validaciones básicas
        if (nombreCliente == null || nombreCliente.isBlank()) {
            reenviarConError(req, resp, "El nombre del cliente es obligatorio.",
                    "/WEB-INF/views/Vendedor/registrar_venta.jsp");
            return;
        }
        if (!Arrays.asList("efectivo", "tarjeta").contains(metodoPago)) {
            reenviarConError(req, resp, "Método de pago inválido.",
                    "/WEB-INF/views/Vendedor/registrar_venta.jsp");
            return;
        }

        // Procesar productos
        String[] productoIds = req.getParameterValues("productoId");
        String[] cantidades  = req.getParameterValues("cantidad");
        String[] precios     = req.getParameterValues("precioVenta");

        if (productoIds == null || productoIds.length == 0) {
            reenviarConError(req, resp, "Debes agregar al menos un producto.",
                    "/WEB-INF/views/Vendedor/registrar_venta.jsp");
            return;
        }

        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < productoIds.length; i++) {
            int prodId = Integer.parseInt(productoIds[i]);
            int cant   = Integer.parseInt(cantidades[i]);
            BigDecimal precio = new BigDecimal(precios[i]);

            Producto prod = productoDAO.obtenerProductoConStock(prodId);
            if (prod == null || prod.getStock() < cant) {
                reenviarConError(req, resp,
                        "Stock insuficiente para: " + (prod != null ? prod.getNombre() : "producto " + prodId),
                        "/WEB-INF/views/Vendedor/registrar_venta.jsp");
                return;
            }

            DetalleVenta detalle = new DetalleVenta(prodId, prod.getNombre(), cant, precio, prod.getStock());
            detalles.add(detalle);
            total = total.add(detalle.getSubtotal());
        }

        // Anticipo
        BigDecimal montoAnticipo  = null;
        BigDecimal saldoPendiente = null;
        if ("anticipo".equals(modalidad)) {
            String anticipoStr = req.getParameter("montoAnticipo");
            if (anticipoStr == null || anticipoStr.isBlank()) {
                reenviarConError(req, resp, "Ingresa el monto del anticipo.",
                        "/WEB-INF/views/Vendedor/registrar_venta.jsp");
                return;
            }
            montoAnticipo = new BigDecimal(anticipoStr);
            if (montoAnticipo.compareTo(BigDecimal.ZERO) <= 0 || montoAnticipo.compareTo(total) >= 0) {
                reenviarConError(req, resp, "El anticipo debe ser mayor a 0 y menor al total.",
                        "/WEB-INF/views/Vendedor/registrar_venta.jsp");
                return;
            }
            saldoPendiente = total.subtract(montoAnticipo);
        }

        // Registrar/obtener cliente
        int clienteId = clienteDAO.registrarOObtenerCliente(nombreCliente, telefonoCliente, emailCliente);

        // Armar venta
        Date fechaEmision = new SimpleDateFormat("yyyy-MM-dd").parse(fechaStr);
        Venta venta = new Venta(vendedor.getUsuarioId(), clienteId, fechaEmision, total, metodoPago);
        venta.setDetalles(detalles);
        venta.setModalidad(modalidad);

        int ventaIdGenerado = ventaDAO.insertar(venta, detalles, modalidad, montoAnticipo, saldoPendiente);

        if (ventaIdGenerado > 0) {
            req.setAttribute("mensaje", "Venta #" + ventaIdGenerado + " registrada exitosamente.");
            req.setAttribute("venta", ventaDAO.obtenerPorId(ventaIdGenerado));
            req.getRequestDispatcher("/WEB-INF/views/Vendedor/venta_confirmada.jsp").forward(req, resp);
        } else {
            throw new Exception("Error al guardar la venta.");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // LÓGICA: Abonar saldo
    // ═══════════════════════════════════════════════════════════
    private void procesarAbono(HttpServletRequest req, HttpServletResponse resp,
                               Usuario vendedor) throws Exception {
        int ventaId         = parseId(req.getParameter("ventaId"));
        BigDecimal monto    = new BigDecimal(req.getParameter("montoAbono"));

        Venta venta = ventaDAO.obtenerPorId(ventaId);
        if (venta == null || venta.getUsuarioId() != vendedor.getUsuarioId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (venta.getSaldoPendiente() == null || monto.compareTo(venta.getSaldoPendiente()) > 0) {
            reenviarConError(req, resp, "El monto no puede superar el saldo pendiente ($" +
                    venta.getSaldoPendiente() + ").", "/Vendedor/ventas/ver?id=" + ventaId);
            return;
        }
        ventaDAO.abonarSaldo(ventaId, monto);
        resp.sendRedirect(req.getContextPath() + "/Vendedor/ventas/ver?id=" + ventaId + "&exito=abono");
    }

    // ═══════════════════════════════════════════════════════════
    // LÓGICA: Registrar caso postventa
    // ═══════════════════════════════════════════════════════════
    private void procesarRegistroPostventa(HttpServletRequest req, HttpServletResponse resp,
                                           Usuario vendedor) throws Exception {
        int    ventaId  = parseId(req.getParameter("ventaId"));
        String tipo     = req.getParameter("tipo");       // cambio | devolucion | reclamo
        int    cantidad = Integer.parseInt(req.getParameter("cantidad"));
        String motivo   = req.getParameter("motivo");

        Venta venta = ventaDAO.obtenerPorId(ventaId);
        if (venta == null || venta.getUsuarioId() != vendedor.getUsuarioId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (!Arrays.asList("cambio", "devolucion", "reclamo").contains(tipo)) {
            reenviarConError(req, resp, "Tipo de caso inválido.",
                    "/Vendedor/postventa/registrar?ventaId=" + ventaId);
            return;
        }

        CasoPostventa caso = new CasoPostventa();
        caso.setVentaId(ventaId);
        caso.setTipo(tipo);
        caso.setCantidad(cantidad);
        caso.setMotivo(motivo);
        caso.setFecha(new Date());

        int casoId = postventaDAO.registrar(caso);

        req.setAttribute("mensaje", "Caso #" + casoId + " registrado. Queda en revisión.");
        req.setAttribute("caso", postventaDAO.obtenerPorId(casoId));
        req.getRequestDispatcher("/WEB-INF/views/Vendedor/postventa_confirmada.jsp").forward(req, resp);
    }

    // ═══════════════════════════════════════════════════════════
    // AUXILIARES
    // ═══════════════════════════════════════════════════════════
    private boolean validarSesionVendedor(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return false;
        }
        Usuario u = (Usuario) session.getAttribute("usuario");
        if (!"vendedor".equals(u.getRol())) {
            try {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso solo para vendedores");
            } catch (Exception ignored) {}
            return false;
        }
        return true;
    }

    private int parseId(String param) {
        if (param == null || !param.matches("\\d+")) return -1;
        return Integer.parseInt(param);
    }

    private void reenviarConError(HttpServletRequest req, HttpServletResponse resp,
                                  String mensaje, String vista) throws ServletException, IOException {
        req.setAttribute("error", mensaje);
        req.getRequestDispatcher(vista).forward(req, resp);
    }

    private void manejarError(HttpServletRequest req, HttpServletResponse resp, Exception e)
            throws ServletException, IOException {
        e.printStackTrace();
        req.setAttribute("error", e.getMessage());
        req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
    }
}
