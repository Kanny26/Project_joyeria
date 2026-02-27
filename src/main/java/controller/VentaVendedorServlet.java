package controller;

import dao.CategoriaDAO;
import dao.ClienteDAO;
import dao.PostventaDAO;
import dao.ProductoDAO;
import dao.VentaDAO;
import model.CasoPostventa;
import model.Categoria;
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

@WebServlet("/VentaVendedorServlet")
public class VentaVendedorServlet extends HttpServlet {

    private VentaDAO     ventaDAO;
    private ProductoDAO  productoDAO;
    private ClienteDAO   clienteDAO;
    private PostventaDAO postventaDAO;
    private CategoriaDAO categoriaDAO;

    @Override
    public void init() throws ServletException {
        ventaDAO     = new VentaDAO();
        productoDAO  = new ProductoDAO();
        clienteDAO   = new ClienteDAO();
        postventaDAO = new PostventaDAO();
        categoriaDAO = new CategoriaDAO();
    }

    // ═══════════════════════════════════════════════════════════
    // GET
    // ═══════════════════════════════════════════════════════════
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!estaAutenticado(req, resp)) return;

        String action = req.getParameter("action");
        if (action == null) action = "";

        try {
            switch (action) {
                case "nueva"                        -> mostrarFormularioNueva(req, resp);
                case "verVenta"                     -> verVenta(req, resp);
                case "misVentas"                    -> listarMisVentas(req, resp);
                case "registrarPostventa"           -> mostrarFormularioPostventa(req, resp);
                case "misCasos"                     -> listarMisCasos(req, resp);
                case "obtenerCategorias"            -> obtenerCategoriasJSON(resp);
                case "obtenerProductosPorCategoria" -> obtenerProductosPorCategoriaJSON(req, resp);
                default -> resp.sendRedirect(req.getContextPath() + "/VentaVendedorServlet?action=misVentas");
            }
        } catch (Exception e) {
            req.setAttribute("error", "Error: " + e.getMessage());
            req.getRequestDispatcher("/vendedor/mensajesexito.jsp").forward(req, resp);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // POST
    // ═══════════════════════════════════════════════════════════
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!estaAutenticado(req, resp)) return;
        req.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null) action = "";

        try {
            switch (action) {
                case "guardarVenta"     -> guardarVenta(req, resp);
                case "abonar"           -> procesarAbono(req, resp);
                case "guardarPostventa" -> guardarPostventa(req, resp);
                default -> resp.sendRedirect(req.getContextPath() + "/VentaVendedorServlet?action=misVentas");
            }
        } catch (Exception e) {
            req.setAttribute("error", "Error: " + e.getMessage());
            req.getRequestDispatcher("/vendedor/mensajeexito.jsp").forward(req, resp);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // JSON — MODAL DE SELECCIÓN DE PRODUCTOS
    // ═══════════════════════════════════════════════════════════

    private void obtenerCategoriasJSON(HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            List<Categoria> categorias = categoriaDAO.listarCategorias();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < categorias.size(); i++) {
                Categoria c = categorias.get(i);
                json.append("{\"id\":").append(c.getCategoriaId())
                    .append(",\"nombre\":\"").append(escapeJson(c.getNombre()))
                    .append("\",\"icono\":\"").append(c.getIcono() != null ? c.getIcono() : "")
                    .append("\"}");
                if (i < categorias.size() - 1) json.append(",");
            }
            json.append("]");
            resp.getWriter().write(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("[]");
        }
    }

    private void obtenerProductosPorCategoriaJSON(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String categoriaIdStr = req.getParameter("categoriaId");
        if (categoriaIdStr == null || !categoriaIdStr.matches("\\d+")) {
            resp.getWriter().write("[]");
            return;
        }
        try {
            int categoriaId = Integer.parseInt(categoriaIdStr);
            List<Producto> productos = productoDAO.listarPorCategoria(categoriaId);
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < productos.size(); i++) {
                Producto p = productos.get(i);
                json.append("{\"id\":").append(p.getProductoId())
                    .append(",\"nombre\":\"").append(escapeJson(p.getNombre()))
                    .append("\",\"codigo\":\"").append(escapeJson(p.getCodigo()))
                    .append("\",\"stock\":").append(p.getStock())
                    .append(",\"precioUnitario\":").append(p.getPrecioUnitario())
                    .append(",\"imagen\":\"").append(p.getImagen() != null ? escapeJson(p.getImagen()) : "")
                    .append("\"}");
                if (i < productos.size() - 1) json.append(",");
            }
            json.append("]");
            resp.getWriter().write(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("[]");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // VISTAS GET
    // ═══════════════════════════════════════════════════════════

    private void mostrarFormularioNueva(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/vendedor/registrar_venta.jsp")
           .forward(req, resp);
    }

    private void listarMisVentas(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        List<Venta> ventas = ventaDAO.listarPorVendedor(getVendedor(req).getUsuarioId());
        req.setAttribute("ventas", ventas);
        req.getRequestDispatcher("/vendedor/ventas_realizadas.jsp")
           .forward(req, resp);
    }

    private void verVenta(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        int id = parseId(req.getParameter("id"));
        Venta venta = ventaDAO.obtenerPorId(id);
        if (venta == null || venta.getUsuarioId() != getVendedor(req).getUsuarioId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado");
            return;
        }
        req.setAttribute("venta", venta);
        req.getRequestDispatcher("/vendedor/ver_venta.jsp")
           .forward(req, resp);
    }

    private void mostrarFormularioPostventa(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        int ventaId = parseId(req.getParameter("ventaId"));
        Venta venta = ventaDAO.obtenerPorId(ventaId);
        if (venta == null || venta.getUsuarioId() != getVendedor(req).getUsuarioId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado");
            return;
        }
        req.setAttribute("venta", venta);
        req.getRequestDispatcher("/vendedor/registrar_postventa.jsp")
           .forward(req, resp);
    }

    private void listarMisCasos(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        List<CasoPostventa> casos = postventaDAO.listarPorVendedor(getVendedor(req).getUsuarioId());
        req.setAttribute("casos", casos);
        req.getRequestDispatcher("/vendedor/casos_postventa.jsp")
           .forward(req, resp);
    }

    // ═══════════════════════════════════════════════════════════
    // LÓGICA POST: Guardar venta
    // ═══════════════════════════════════════════════════════════

    private void guardarVenta(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {

        String nombreCliente   = req.getParameter("clienteNombre");
        String telefonoCliente = req.getParameter("clienteTelefono");
        String emailCliente    = req.getParameter("clienteEmail");
        String fechaStr        = req.getParameter("fechaVenta");
        String metodoPago      = req.getParameter("metodoPago");
        String modalidad       = req.getParameter("modalidad");

        if (nombreCliente == null || nombreCliente.isBlank()) {
            reenviarConError(req, resp, "El nombre del cliente es obligatorio.",
                    "/vendedor/registrar_venta.jsp");
            return;
        }
        if (metodoPago == null || metodoPago.isBlank()) {
            reenviarConError(req, resp, "El método de pago es obligatorio.",
                    "/vendedor/registrar_venta.jsp");
            return;
        }

        String[] productoIds = req.getParameterValues("productoId");
        String[] cantidades  = req.getParameterValues("cantidad");
        String[] precios     = req.getParameterValues("precioUnitario");

        if (productoIds == null || productoIds.length == 0) {
            reenviarConError(req, resp, "Debes agregar al menos un producto.",
                    "/vendedor/registrar_venta.jsp");
            return;
        }

        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < productoIds.length; i++) {
            if (productoIds[i] == null || productoIds[i].trim().isEmpty()) continue;
            int prodId        = Integer.parseInt(productoIds[i].trim());
            int cant          = Integer.parseInt(cantidades[i].trim());
            BigDecimal precio = new BigDecimal(precios[i].trim());

            Producto prod = productoDAO.obtenerProductoConStock(prodId);
            if (prod == null || prod.getStock() < cant) {
                reenviarConError(req, resp,
                        "Stock insuficiente para: " + (prod != null ? prod.getNombre() : "producto " + prodId),
                        "/vendedor/registrar_venta.jsp");
                return;
            }

            DetalleVenta detalle = new DetalleVenta(prodId, prod.getNombre(), cant, precio, prod.getStock());
            detalles.add(detalle);
            total = total.add(detalle.getSubtotal());
        }

        if (detalles.isEmpty()) {
            reenviarConError(req, resp, "No hay productos válidos en la venta.",
                    "/vendedor/registrar_venta.jsp");
            return;
        }

        BigDecimal montoAnticipo  = null;
        BigDecimal saldoPendiente = null;
        if ("anticipo".equals(modalidad)) {
            String anticipoStr = req.getParameter("montoAnticipo");
            if (anticipoStr == null || anticipoStr.isBlank()) {
                reenviarConError(req, resp, "Ingresa el monto del anticipo.",
                        "/vendedor/registrar_venta.jsp");
                return;
            }
            montoAnticipo = new BigDecimal(anticipoStr);
            if (montoAnticipo.compareTo(BigDecimal.ZERO) <= 0 || montoAnticipo.compareTo(total) >= 0) {
                reenviarConError(req, resp, "El anticipo debe ser mayor a 0 y menor al total.",
                        "/vendedor/registrar_venta.jsp");
                return;
            }
            saldoPendiente = total.subtract(montoAnticipo);
        }

        int clienteId = clienteDAO.registrarOObtenerCliente(nombreCliente, telefonoCliente, emailCliente);

        Date fechaEmision = new SimpleDateFormat("yyyy-MM-dd").parse(fechaStr);
        Usuario vendedor  = getVendedor(req);
        Venta venta = new Venta(vendedor.getUsuarioId(), clienteId, fechaEmision, total, metodoPago);
        venta.setDetalles(detalles);
        venta.setModalidad(modalidad);

        int ventaIdGenerado = ventaDAO.insertar(venta, detalles, modalidad, montoAnticipo, saldoPendiente);

        if (ventaIdGenerado > 0) {
            req.setAttribute("mensaje", "Venta #" + ventaIdGenerado + " registrada exitosamente.");
            req.setAttribute("venta", ventaDAO.obtenerPorId(ventaIdGenerado));
            req.getRequestDispatcher("/vendedor/venta_confirmada.jsp").forward(req, resp);
        } else {
            throw new Exception("No se pudo guardar la venta.");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // LÓGICA POST: Abonar saldo
    // ═══════════════════════════════════════════════════════════

    private void procesarAbono(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        int ventaId      = parseId(req.getParameter("ventaId"));
        BigDecimal monto = new BigDecimal(req.getParameter("montoAbono"));

        Venta venta = ventaDAO.obtenerPorId(ventaId);
        if (venta == null || venta.getUsuarioId() != getVendedor(req).getUsuarioId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (venta.getSaldoPendiente() == null || monto.compareTo(venta.getSaldoPendiente()) > 0) {
            reenviarConError(req, resp,
                    "El monto no puede superar el saldo pendiente ($" + venta.getSaldoPendiente() + ").",
                    "/vendedor/ver_venta.jsp");
            return;
        }
        ventaDAO.abonarSaldo(ventaId, monto);
        resp.sendRedirect(req.getContextPath()
                + "/VentaVendedorServlet?action=verVenta&id=" + ventaId + "&exito=abono");
    }

    // ═══════════════════════════════════════════════════════════
    // LÓGICA POST: Guardar caso postventa
    // ═══════════════════════════════════════════════════════════

    private void guardarPostventa(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        int    ventaId  = parseId(req.getParameter("ventaId"));
        String tipo     = req.getParameter("tipo");
        int    cantidad = Integer.parseInt(req.getParameter("cantidad"));
        String motivo   = req.getParameter("motivo");

        Venta venta = ventaDAO.obtenerPorId(ventaId);
        if (venta == null || venta.getUsuarioId() != getVendedor(req).getUsuarioId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (!Arrays.asList("cambio", "devolucion", "reclamo").contains(tipo)) {
            reenviarConError(req, resp, "Tipo de caso inválido.",
                    "/vendedor/registrar_postventa.jsp");
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
        req.getRequestDispatcher("/vendedor/postventa_confirmada.jsp").forward(req, resp);
    }

    // ═══════════════════════════════════════════════════════════
    // AUXILIARES
    // ═══════════════════════════════════════════════════════════

    private boolean estaAutenticado(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("vendedor") == null) {
            resp.sendRedirect(req.getContextPath() + "/inicio-sesion.jsp");
            return false;
        }
        return true;
    }

    private Usuario getVendedor(HttpServletRequest req) {
        return (Usuario) req.getSession().getAttribute("vendedor");
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

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}