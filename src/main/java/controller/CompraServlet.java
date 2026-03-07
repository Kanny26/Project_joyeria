package controller;

import dao.CompraDAO;
import dao.ProductoDAO;
import dao.CategoriaDAO;
import dao.MetodoPagoDAO;
import model.Administrador;
import model.Compra;
import model.DetalleCompra;
import model.Producto;
import model.Categoria;
import model.MetodoPago;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@WebServlet("/CompraServlet")
public class CompraServlet extends HttpServlet {

    private CompraDAO     compraDAO;
    private ProductoDAO   productoDAO;
    private CategoriaDAO  categoriaDAO;
    private MetodoPagoDAO metodoPagoDAO;

    @Override
    public void init() throws ServletException {
        compraDAO     = new CompraDAO();
        productoDAO   = new ProductoDAO();
        categoriaDAO  = new CategoriaDAO();
        metodoPagoDAO = new MetodoPagoDAO();
    }

    // ════════════════════════════════════════════════════════════════════
    // GET
    // ════════════════════════════════════════════════════════════════════
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!estaAutenticado(request, response)) return;
        String action = request.getParameter("action");
        if (action == null) action = "";

        try {
            switch (action) {
                case "nueva"                        -> mostrarFormularioNueva(request, response);
                case "detalle"                      -> verDetalle(request, response);
                case "eliminar"                     -> eliminarCompra(request, response);
                case "obtenerCategorias"            -> obtenerCategoriasJSON(response);
                case "obtenerProductosPorCategoria" -> obtenerProductosPorCategoriaJSON(request, response);
                default -> response.sendRedirect(
                        request.getContextPath() + "/ProveedorServlet?action=listar");
            }
        } catch (Exception e) {
            request.setAttribute("error", "Error: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // POST
    // ════════════════════════════════════════════════════════════════════
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!estaAutenticado(request, response)) return;
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        try {
            switch (action) {
                case "guardarCompra" -> guardarCompra(request, response);
                default -> response.sendRedirect(
                        request.getContextPath() + "/ProveedorServlet?action=listar");
            }
        } catch (Exception e) {
            request.setAttribute("error", "Error al guardar compra: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // Formulario nueva compra — carga también la lista de métodos de pago
    // ════════════════════════════════════════════════════════════════════
    private void mostrarFormularioNueva(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String usuarioIdStr = request.getParameter("usuarioId");
        if (usuarioIdStr == null || !usuarioIdStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }

        // Cargar métodos de pago para el <select> del JSP
        try {
            List<MetodoPago> metodos = metodoPagoDAO.listarTodos();
            request.setAttribute("metodosPago", metodos);
        } catch (Exception ignored) { }

        request.setAttribute("usuarioId", usuarioIdStr);
        request.getRequestDispatcher("/Administrador/proveedores/agregar_compra.jsp")
               .forward(request, response);
    }

    // ════════════════════════════════════════════════════════════════════
    // Guardar compra — inserta en Compra, Detalle_Compra, Pago_Compra
    // y opcionalmente en Credito_Compra + Abono_Credito
    // ════════════════════════════════════════════════════════════════════
    private void guardarCompra(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        // ── Proveedor ────────────────────────────────────────────────
        String usuarioIdStr = request.getParameter("usuarioId");
        if (usuarioIdStr == null || !usuarioIdStr.matches("\\d+"))
            throw new IllegalArgumentException("ID de proveedor inválido");
        int proveedorId = Integer.parseInt(usuarioIdStr);

        // ── Fechas base ──────────────────────────────────────────────
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date fechaCompra  = fmt.parse(request.getParameter("fechaCompra"));
        Date fechaEntrega = fmt.parse(request.getParameter("fechaEntrega"));
        if (fechaEntrega.before(fechaCompra))
            throw new IllegalArgumentException(
                    "La fecha de entrega no puede ser anterior a la fecha de compra");

        // ── Método de pago → Pago_Compra.metodo_pago_id ─────────────
        String mpStr = request.getParameter("metodoPagoId");
        if (mpStr == null || !mpStr.matches("\\d+") || mpStr.equals("0"))
            throw new IllegalArgumentException("Debes seleccionar un método de pago");
        int metodoPagoId = Integer.parseInt(mpStr);

        // ── Tipo de pago ─────────────────────────────────────────────
        String tipoPago = request.getParameter("tipoPago");
        boolean esCredito = "CREDITO".equals(tipoPago);

        // ── Campos de crédito (solo si esCredito) ────────────────────
        Date       fechaVencimiento = null;
        BigDecimal anticipo         = BigDecimal.ZERO;
        String     estadoCredito    = "pagado";

        if (esCredito) {
            // Fecha vencimiento (obligatoria)
            String fvStr = request.getParameter("fechaVencimiento");
            if (fvStr == null || fvStr.trim().isEmpty())
                throw new IllegalArgumentException(
                        "Debes indicar la fecha límite de pago para compras a crédito");
            fechaVencimiento = fmt.parse(fvStr.trim());
            if (fechaVencimiento.before(fechaCompra))
                throw new IllegalArgumentException(
                        "La fecha de pago no puede ser anterior a la fecha de compra");

            // Anticipo
            String antStr = request.getParameter("anticipo");
            if (antStr != null && !antStr.trim().isEmpty()) {
                try { anticipo = new BigDecimal(antStr.trim()); }
                catch (NumberFormatException ignored) { anticipo = BigDecimal.ZERO; }
            }

            // Estado del crédito
            String ecStr = request.getParameter("estadoCredito");
            estadoCredito = "pagado".equals(ecStr) ? "pagado" : "activo";
        }

        // ── Productos ─────────────────────────────────────────────────
        String[] productosIds = request.getParameterValues("productoId");
        String[] precios      = request.getParameterValues("precioUnitario");
        String[] cantidades   = request.getParameterValues("cantidad");

        if (productosIds == null || productosIds.length == 0)
            throw new IllegalArgumentException("Debes agregar al menos un producto a la compra");

        List<DetalleCompra> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < productosIds.length; i++) {
            if (productosIds[i] == null || productosIds[i].trim().isEmpty()) continue;

            int        productoId     = Integer.parseInt(productosIds[i].trim());
            BigDecimal precioUnitario = new BigDecimal(precios[i].trim());
            int        cantidad       = Integer.parseInt(cantidades[i].trim());

            if (productoId <= 0 || precioUnitario.compareTo(BigDecimal.ZERO) <= 0 || cantidad <= 0)
                throw new IllegalArgumentException("Datos inválidos en la fila " + (i + 1));

            DetalleCompra d = new DetalleCompra();
            d.setProductoId(productoId);
            d.setPrecioUnitario(precioUnitario);
            d.setCantidad(cantidad);
            d.setSubtotal(precioUnitario.multiply(new BigDecimal(cantidad)));
            total = total.add(d.getSubtotal());
            detalles.add(d);
        }

        if (detalles.isEmpty())
            throw new IllegalArgumentException("No hay productos válidos en la compra");

        // Validar anticipo <= total
        if (anticipo.compareTo(total) > 0)
            throw new IllegalArgumentException(
                    "El anticipo no puede ser mayor al total de la compra");

        // ── Construir objeto Compra ───────────────────────────────────
        Compra compra = new Compra();
        compra.setProveedorId(proveedorId);
        compra.setFechaCompra(fechaCompra);
        compra.setFechaEntrega(fechaEntrega);
        compra.setDetalles(detalles);

        // Campos de pago que se pasarán al DAO
        compra.setMetodoPagoId(metodoPagoId);
        compra.setEsCredito(esCredito);
        compra.setTotal(total);
        compra.setAnticipo(anticipo);
        compra.setFechaVencimiento(fechaVencimiento);
        compra.setEstadoCredito(estadoCredito);

        // ── Admin en sesión ───────────────────────────────────────────
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object adminObj = session.getAttribute("admin");
            if (adminObj instanceof Administrador)
                compra.setUsuarioId(((Administrador) adminObj).getId());
        }

        // ── Persistir (transacción en el DAO) ────────────────────────
        boolean exito = compraDAO.insertarConTransaccion(compra);
        if (exito) {
            response.sendRedirect(request.getContextPath()
                + "/ProveedorServlet?action=verCompras&id=" + proveedorId + "&msg=creado");
        } else {
            throw new Exception("No se pudo guardar la compra");
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // Ver detalle
    // ════════════════════════════════════════════════════════════════════
    private void verDetalle(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String idStr          = request.getParameter("id");
        String proveedorIdStr = request.getParameter("proveedorId");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }
        Compra compra = compraDAO.obtenerPorId(Integer.parseInt(idStr));
        if (compra == null) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }
        request.setAttribute("compra",      compra);
        request.setAttribute("proveedorId", proveedorIdStr);
        request.getRequestDispatcher("/Administrador/proveedores/detalle_compra.jsp")
               .forward(request, response);
    }

    // ════════════════════════════════════════════════════════════════════
    // Eliminar compra
    // ════════════════════════════════════════════════════════════════════
    private void eliminarCompra(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String idStr          = request.getParameter("id");
        String proveedorIdStr = request.getParameter("proveedorId");
        if (idStr != null && idStr.matches("\\d+"))
            compraDAO.eliminarConTransaccion(Integer.parseInt(idStr));
        response.sendRedirect(request.getContextPath()
            + "/ProveedorServlet?action=verCompras&id=" + proveedorIdStr + "&msg=eliminado");
    }

    // ════════════════════════════════════════════════════════════════════
    // JSON: categorías
    // ════════════════════════════════════════════════════════════════════
    private void obtenerCategoriasJSON(HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try {
            List<Categoria> categorias = categoriaDAO.listarCategorias();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < categorias.size(); i++) {
                Categoria c = categorias.get(i);
                json.append("{\"id\":").append(c.getCategoriaId())
                    .append(",\"nombre\":\"").append(esc(c.getNombre()))
                    .append("\",\"icono\":\"").append(c.getIcono() != null ? c.getIcono() : "")
                    .append("\"}");
                if (i < categorias.size() - 1) json.append(",");
            }
            json.append("]");
            response.getWriter().write(json.toString());
        } catch (Exception e) { response.getWriter().write("[]"); }
    }

    // ════════════════════════════════════════════════════════════════════
    // JSON: productos por categoría
    // ════════════════════════════════════════════════════════════════════
    private void obtenerProductosPorCategoriaJSON(HttpServletRequest request,
                                                   HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        String categoriaIdStr = request.getParameter("categoriaId");
        if (categoriaIdStr == null || !categoriaIdStr.matches("\\d+")) {
            response.getWriter().write("[]"); return;
        }
        try {
            List<Producto> productos = productoDAO.listarPorCategoria(Integer.parseInt(categoriaIdStr));
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < productos.size(); i++) {
                Producto p = productos.get(i);
                json.append("{\"id\":").append(p.getProductoId())
                    .append(",\"nombre\":\"").append(esc(p.getNombre()))
                    .append("\",\"codigo\":\"").append(p.getCodigo())
                    .append("\",\"stock\":").append(p.getStock())
                    .append(",\"precioUnitario\":").append(p.getPrecioUnitario())
                    .append(",\"imagen\":\"").append(p.getImagen() != null ? p.getImagen() : "")
                    .append("\"}");
                if (i < productos.size() - 1) json.append(",");
            }
            json.append("]");
            response.getWriter().write(json.toString());
        } catch (Exception e) { response.getWriter().write("[]"); }
    }

    // ════════════════════════════════════════════════════════════════════
    // Helpers
    // ════════════════════════════════════════════════════════════════════
    private boolean estaAutenticado(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("admin") == null) {
            res.sendRedirect(req.getContextPath() + "/inicio-sesion.jsp");
            return false;
        }
        return true;
    }

    private String esc(String t) {
        if (t == null) return "";
        return t.replace("\\","\\\\").replace("\"","\\\"")
                .replace("\n","\\n").replace("\r","\\r").replace("\t","\\t");
    }
}