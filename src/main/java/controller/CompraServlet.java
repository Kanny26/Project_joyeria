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
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@WebServlet("/CompraServlet")
@MultipartConfig
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
                case "obtenerCategorias"            -> obtenerCategoriasJSON(request, response);
                case "obtenerProductosPorCategoria" -> obtenerProductosPorCategoriaJSON(request, response);
                default -> response.sendRedirect(
                        request.getContextPath() + "/ProveedorServlet?action=listar");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        if (action == null) action = "";

        try {
            switch (action) {
                case "guardarCompra" -> guardarCompra(request, response);
                default -> response.sendRedirect(
                        request.getContextPath() + "/ProveedorServlet?action=listar");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Devolver error en JSON para que SweetAlert lo muestre en el JSP
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"ok\":false,\"error\":\"" + esc(e.getMessage()) + "\"}");
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // Formulario nueva compra
    // ════════════════════════════════════════════════════════════════════
    private void mostrarFormularioNueva(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String proveedorIdStr = request.getParameter("id");
        
        // Validación de ID
        if (proveedorIdStr == null || !proveedorIdStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }

        try {
            List<MetodoPago> metodos = metodoPagoDAO.listarTodos();
            // Asegúrate de que la lista no sea nula para evitar errores en el JSP
            if (metodos == null) metodos = new ArrayList<>(); 
            request.setAttribute("metodosPago", metodos);
        } catch (Exception e) {
            System.err.println("Error al cargar métodos de pago: " + e.getMessage());
            e.printStackTrace();
        }

        request.setAttribute("proveedorId", proveedorIdStr);
        
        // VERIFICA ESTA RUTA: Debe coincidir exactamente con la ubicación de tu archivo
        request.getRequestDispatcher("/Administrador/proveedores/agregar_compra.jsp")
               .forward(request, response);
    }

    // ════════════════════════════════════════════════════════════════════
    // Guardar compra — responde JSON para que el JSP maneje el resultado
    // con SweetAlert2 sin recargas bruscas
    // ════════════════════════════════════════════════════════════════════
    private void guardarCompra(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        response.setContentType("application/json;charset=UTF-8");

        // ── Proveedor ────────────────────────────────────────────────
        String proveedorIdStr = request.getParameter("proveedorId");
        if (proveedorIdStr == null || !proveedorIdStr.matches("\\d+"))
            throw new IllegalArgumentException("ID de proveedor inválido.");
        int proveedorId = Integer.parseInt(proveedorIdStr);

        // ── Fechas ───────────────────────────────────────────────────
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

        String fcStr = request.getParameter("fechaCompra");
        String feStr = request.getParameter("fechaEntrega");
        if (fcStr == null || fcStr.isBlank())
            throw new IllegalArgumentException("La fecha de compra es obligatoria.");
        if (feStr == null || feStr.isBlank())
            throw new IllegalArgumentException("La fecha de entrega es obligatoria.");

        Date fechaCompra  = fmt.parse(fcStr.trim());
        Date fechaEntrega = fmt.parse(feStr.trim());

        if (fechaEntrega.before(fechaCompra))
            throw new IllegalArgumentException(
                    "La fecha de entrega no puede ser anterior a la fecha de compra.");

        // ── Método de pago ───────────────────────────────────────────
        String mpStr = request.getParameter("metodoPagoId");
        if (mpStr == null || mpStr.isBlank() || mpStr.equals("0"))
            throw new IllegalArgumentException("Debes seleccionar un método de pago.");
        int metodoPagoId = Integer.parseInt(mpStr);

        // ── Tipo de pago ─────────────────────────────────────────────
        String  tipoPago  = request.getParameter("tipoPago");
        boolean esCredito = "CREDITO".equals(tipoPago);

        // ── Campos de crédito ────────────────────────────────────────
        Date       fechaVencimiento = null;
        BigDecimal anticipo         = BigDecimal.ZERO;
        String     estadoCredito    = "pagado";

        if (esCredito) {
            String fvStr = request.getParameter("fechaVencimiento");
            if (fvStr == null || fvStr.isBlank())
                throw new IllegalArgumentException(
                        "Debes indicar la fecha límite de pago para compras a crédito.");
            fechaVencimiento = fmt.parse(fvStr.trim());
            if (fechaVencimiento.before(fechaCompra))
                throw new IllegalArgumentException(
                        "La fecha de pago no puede ser anterior a la fecha de compra.");

            String antStr = request.getParameter("anticipo");
            if (antStr != null && !antStr.isBlank()) {
                try   { anticipo = new BigDecimal(antStr.trim()); }
                catch (NumberFormatException ignored) { anticipo = BigDecimal.ZERO; }
            }
            String ecStr = request.getParameter("estadoCredito");
            estadoCredito = "pagado".equals(ecStr) ? "pagado" : "activo";
        }

        // ── Productos ────────────────────────────────────────────────
        String[] productosIds = request.getParameterValues("productoId");
        String[] precios      = request.getParameterValues("precioUnitario");
        String[] cantidades   = request.getParameterValues("cantidad");

        if (productosIds == null || productosIds.length == 0)
            throw new IllegalArgumentException("Debes agregar al menos un producto a la compra.");

        List<DetalleCompra> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < productosIds.length; i++) {
            if (productosIds[i] == null || productosIds[i].isBlank()) continue;

            int productoId;
            BigDecimal precioUnitario;
            int cantidad;

            try {
                productoId     = Integer.parseInt(productosIds[i].trim());
                precioUnitario = new BigDecimal(precios[i].trim());
                cantidad       = Integer.parseInt(cantidades[i].trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Datos inválidos en la fila " + (i + 1) + ".");
            }

            if (productoId <= 0)
                throw new IllegalArgumentException("Producto inválido en la fila " + (i + 1) + ".");
            if (precioUnitario.compareTo(BigDecimal.ZERO) <= 0)
                throw new IllegalArgumentException(
                        "El precio unitario debe ser mayor a 0 en la fila " + (i + 1) + ".");
            if (cantidad <= 0)
                throw new IllegalArgumentException(
                        "La cantidad debe ser mayor a 0 en la fila " + (i + 1) + ".");

            DetalleCompra d = new DetalleCompra();
            d.setProductoId(productoId);
            d.setPrecioUnitario(precioUnitario);
            d.setCantidad(cantidad);
            d.setSubtotal(precioUnitario.multiply(new BigDecimal(cantidad)));
            total = total.add(d.getSubtotal());
            detalles.add(d);
        }

        if (detalles.isEmpty())
            throw new IllegalArgumentException("No hay productos válidos en la compra.");

        if (anticipo.compareTo(total) > 0)
            throw new IllegalArgumentException(
                    "El anticipo ($" + anticipo.toPlainString()
                    + ") no puede ser mayor al total de la compra ($" + total.toPlainString() + ").");

        // ── Construir objeto Compra ───────────────────────────────────
        Compra compra = new Compra();
        compra.setProveedorId(proveedorId);
        compra.setFechaCompra(fechaCompra);
        compra.setFechaEntrega(fechaEntrega);
        compra.setDetalles(detalles);
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
                compra.setProveedorId(((Administrador) adminObj).getId());
        }

        // ── Persistir ────────────────────────────────────────────────
        boolean exito = compraDAO.insertarConTransaccion(compra);
        if (exito) {
            response.getWriter().write("{\"ok\":true,\"proveedorId\":" + proveedorId + "}");
        } else {
            throw new Exception("No se pudo guardar la compra. Inténtalo de nuevo.");
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
    // JSON: categorías activas
    // ════════════════════════════════════════════════════════════════════
    private void obtenerCategoriasJSON(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        try {
            List<Categoria> categorias = categoriaDAO.listarCategorias();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < categorias.size(); i++) {
                Categoria c = categorias.get(i);
                json.append("{\"id\":").append(c.getCategoriaId())
                    .append(",\"nombre\":\"").append(esc(c.getNombre()))
                    .append("\",\"icono\":\"")
                    .append(c.getIcono() != null ? esc(c.getIcono()) : "")
                    .append("\"}");
                if (i < categorias.size() - 1) json.append(",");
            }
            json.append("]");
            response.getWriter().write(json.toString());
        } catch (Exception e) {
            response.getWriter().write("[]");
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // JSON: productos de una categoría FILTRADOS por proveedor
    // ── Solo muestra los productos cuyo proveedor_id == proveedorId
    // ════════════════════════════════════════════════════════════════════
    private void obtenerProductosPorCategoriaJSON(HttpServletRequest request,
                                                   HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        String categoriaIdStr  = request.getParameter("categoriaId");
        String proveedorIdStr  = request.getParameter("proveedorId");

        if (categoriaIdStr == null || !categoriaIdStr.matches("\\d+")) {
            response.getWriter().write("[]");
            return;
        }

        // proveedorId para filtrar (si no viene, no filtra y muestra todos)
        int proveedorId = 0;
        if (proveedorIdStr != null && proveedorIdStr.matches("\\d+"))
            proveedorId = Integer.parseInt(proveedorIdStr);

        try {
            List<Producto> todos = productoDAO.listarPorCategoria(
                    Integer.parseInt(categoriaIdStr));

            // ── Filtrar: solo los productos asignados a este proveedor ──
            List<Producto> filtrados = new ArrayList<>();
            for (Producto p : todos) {
                if (proveedorId == 0 || p.getProveedorId() == proveedorId) {
                    filtrados.add(p);
                }
            }

            if (filtrados.isEmpty()) {
                response.getWriter().write("[]");
                return;
            }

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < filtrados.size(); i++) {
                Producto p = filtrados.get(i);
                json.append("{")
                    .append("\"id\":").append(p.getProductoId())
                    .append(",\"nombre\":\"").append(esc(p.getNombre())).append("\"")
                    .append(",\"codigo\":\"").append(esc(p.getCodigo())).append("\"")
                    .append(",\"stock\":").append(p.getStock())
                    .append(",\"precioUnitario\":").append(p.getPrecioUnitario())
                    .append(",\"imagen\":\"")
                    .append(p.getImagen() != null ? esc(p.getImagen()) : "")
                    .append("\"")
                    .append("}");
                if (i < filtrados.size() - 1) json.append(",");
            }
            json.append("]");
            response.getWriter().write(json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("[]");
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // Helpers
    // ════════════════════════════════════════════════════════════════════
    private boolean estaAutenticado(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("admin") == null) {
            String accept = req.getHeader("Accept");
            if (accept != null && accept.contains("application/json")) {
                res.setContentType("application/json;charset=UTF-8");
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("{\"error\":\"sesion_expirada\"}");
            } else {
                res.sendRedirect(req.getContextPath() + "/inicio-sesion.jsp");
            }
            return false;
        }
        return true;
    }

    private String esc(String t) {
        if (t == null) return "";
        return t.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}