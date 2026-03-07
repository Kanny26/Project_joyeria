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
import utils.PDFGenerator;

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
    
    private static final long serialVersionUID = 1L;
    
    private VentaDAO ventaDAO;
    private ProductoDAO productoDAO;
    private ClienteDAO clienteDAO;
    private PostventaDAO postventaDAO;
    private CategoriaDAO categoriaDAO;

    @Override
    public void init() throws ServletException {
        ventaDAO = new VentaDAO();
        productoDAO = new ProductoDAO();
        clienteDAO = new ClienteDAO();
        postventaDAO = new PostventaDAO();
        categoriaDAO = new CategoriaDAO();
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // GET
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!estaAutenticado(req, resp)) return;

        String action = req.getParameter("action");
        if (action == null) action = "";

        try {
            switch (action) {
                case "nueva" -> mostrarFormularioNueva(req, resp);
                case "verVenta" -> verVenta(req, resp);
                case "misVentas" -> listarMisVentas(req, resp);
                case "registrarPostventa" -> mostrarFormularioPostventa(req, resp);
                case "misCasos" -> listarMisCasos(req, resp);
                case "obtenerCategorias" -> obtenerCategoriasJSON(resp);
                case "obtenerProductosPorCategoria" -> obtenerProductosPorCategoriaJSON(req, resp);
                case "descargarFactura" -> descargarFacturaPDF(req, resp);
                default -> resp.sendRedirect(req.getContextPath() + "/VentaVendedorServlet?action=misVentas");
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Error: " + e.getMessage());
            req.getRequestDispatcher("/vendedor/ventas_realizadas.jsp").forward(req, resp);
        }
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // POST
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!estaAutenticado(req, resp)) return;
        req.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null) action = "";

        try {
            switch (action) {
                case "guardarVenta" -> guardarVenta(req, resp);
                case "abonar" -> procesarAbono(req, resp);
                case "guardarPostventa" -> guardarPostventa(req, resp);
                default -> resp.sendRedirect(req.getContextPath() + "/VentaVendedorServlet?action=misVentas");
            }
        } catch (Exception e) {
            req.setAttribute("error", "Error: " + e.getMessage());
            req.getRequestDispatcher("/vendedor/mensajesexito.jsp").forward(req, resp);
        }
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // JSON — MODAL DE SELECCIÓN DE PRODUCTOS
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private void obtenerCategoriasJSON(HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        
        try {
            List<Categoria> categorias = categoriaDAO.listarCategorias();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < categorias.size(); i++) {
                Categoria c = categorias.get(i);
                json.append("{\"id\":").append(c.getCategoriaId())
                    .append(",\"nombre\":\"").append(escapeJson(c.getNombre()))
                    .append("\",\"icono\":\"").append(escapeJson(c.getIcono() != null ? c.getIcono() : ""))
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

    private void obtenerProductosPorCategoriaJSON(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        
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
                    .append(",\"imagen\":\"").append(escapeJson(p.getImagen() != null ? p.getImagen() : ""))
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

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // VISTAS GET
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private void mostrarFormularioNueva(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("categorias", categoriaDAO.listarCategorias());
        req.getRequestDispatcher("/vendedor/registrar_venta.jsp").forward(req, resp);
    }

    private void listarMisVentas(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Usuario vendedor = getVendedor(req);
        if (vendedor == null) {
            resp.sendRedirect(req.getContextPath() + "/inicio-sesion.jsp");
            return;
        }
        List<Venta> ventas = ventaDAO.listarPorVendedor(vendedor.getUsuarioId());
        req.setAttribute("ventas", ventas);
        req.getRequestDispatcher("/vendedor/ventas_realizadas.jsp").forward(req, resp);
    }

    // ■■ MÉTODO AUXILIAR: Validar propiedad con primitivos int ■■
    private boolean esVentaDelVendedor(Venta venta, Usuario vendedor) {
        if (venta == null || vendedor == null) return false;
        return venta.getUsuarioId() == vendedor.getUsuarioId();
    }

    private void verVenta(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int id = parseId(req.getParameter("id"));
        if (id <= 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de venta inválido");
            return;
        }
        
        Venta venta = ventaDAO.obtenerPorId(id);
        Usuario vendedor = getVendedor(req);
        
        // ■■ RF23: Validación estricta de propiedad (primitivos int) ■■
        if (!esVentaDelVendedor(venta, vendedor)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado: No es tu venta");
            return;
        }
        
        req.setAttribute("venta", venta);
        req.setAttribute("detalles", venta.getDetalles());
        
        if ("true".equals(req.getParameter("imprimir"))) {
            req.setAttribute("imprimir", true);
        }
        
        req.getRequestDispatcher("/vendedor/ver_venta.jsp").forward(req, resp);
    }

    private void mostrarFormularioPostventa(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int ventaId = parseId(req.getParameter("ventaId"));
        if (ventaId <= 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de venta inválido");
            return;
        }
        
        Venta venta = ventaDAO.obtenerPorId(ventaId);
        Usuario vendedor = getVendedor(req);
        
        // ■■ Validación de propiedad (primitivos int) ■■
        if (!esVentaDelVendedor(venta, vendedor)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado");
            return;
        }
        
        req.setAttribute("venta", venta);
        req.setAttribute("detalles", venta.getDetalles());
        req.getRequestDispatcher("/vendedor/registrar_postventa.jsp").forward(req, resp);
    }

    private void listarMisCasos(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Usuario vendedor = getVendedor(req);
        if (vendedor == null) {
            resp.sendRedirect(req.getContextPath() + "/inicio-sesion.jsp");
            return;
        }
        // ■■ CORREGIDO: Listar solo casos del vendedor autenticado ■■
        List<CasoPostventa> casos = postventaDAO.listarPorVendedor(vendedor.getUsuarioId());
        req.setAttribute("casos", casos);
        req.getRequestDispatcher("/vendedor/casos_postventa.jsp").forward(req, resp);
    }

    // ■■ RF21: Método para descargar factura en PDF ■■
    private void descargarFacturaPDF(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int ventaId = parseId(req.getParameter("id"));
        if (ventaId <= 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de venta inválido");
            return;
        }
        
        Venta venta = ventaDAO.obtenerPorId(ventaId);
        Usuario vendedor = getVendedor(req);
        
        // ■■ Validación de propiedad (primitivos int) ■■
        if (!esVentaDelVendedor(venta, vendedor)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado");
            return;
        }
        
        try {
            // ■■ Generar PDF con PDFGenerator ■■
            byte[] pdfBytes = PDFGenerator.generarFacturaPDF(venta);
            
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "attachment; filename=\"Factura_" + ventaId + ".pdf\"");
            resp.setContentLength(pdfBytes.length);
            resp.getOutputStream().write(pdfBytes);
            resp.getOutputStream().flush();
            
        } catch (Exception e) {
            // ■■ Fallback: Redirigir a vista de impresión ■■
            req.setAttribute("venta", venta);
            req.setAttribute("detalles", venta.getDetalles());
            req.setAttribute("imprimir", true);
            req.getRequestDispatcher("/vendedor/ver_venta.jsp").forward(req, resp);
        }
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // LÓGICA POST: Guardar venta
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private void guardarVenta(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String nombreCliente = req.getParameter("clienteNombre");
        String telefonoCliente = req.getParameter("clienteTelefono");
        String emailCliente = req.getParameter("clienteEmail");
        String fechaStr = req.getParameter("fechaVenta");
        String metodoPago = req.getParameter("metodoPago");
        String modalidad = req.getParameter("modalidad");

        // Validaciones básicas
        if (nombreCliente == null || nombreCliente.isBlank()) {
            reenviarConError(req, resp, "El nombre del cliente es obligatorio.", "/vendedor/registrar_venta.jsp");
            return;
        }
        if (metodoPago == null || metodoPago.isBlank()) {
            reenviarConError(req, resp, "El método de pago es obligatorio.", "/vendedor/registrar_venta.jsp");
            return;
        }
        if (fechaStr == null || fechaStr.isBlank()) {
            reenviarConError(req, resp, "La fecha de venta es obligatoria.", "/vendedor/registrar_venta.jsp");
            return;
        }

        // ■■ Obtener arrays de productos ■■
        String[] productoIds = req.getParameterValues("productoId");
        String[] cantidades = req.getParameterValues("cantidad");
        String[] precios = req.getParameterValues("precioUnitario");

        if (productoIds == null || productoIds.length == 0) {
            reenviarConError(req, resp, "Debes agregar al menos un producto.", "/vendedor/registrar_venta.jsp");
            return;
        }

        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        // ■■ VALIDAR STOCK PARA CADA PRODUCTO INDIVIDUALMENTE ■■
        for (int i = 0; i < productoIds.length; i++) {
            if (productoIds[i] == null || productoIds[i].trim().isEmpty()) continue;
            
            try {
                int prodId = Integer.parseInt(productoIds[i].trim());
                int cant = Integer.parseInt(cantidades[i].trim());
                BigDecimal precio = new BigDecimal(precios[i].trim());

                // ■■ CORRECCIÓN: Obtener producto completo con stock ■■
                Producto prod = productoDAO.obtenerProductoConStock(prodId);
                
                if (prod == null) {
                    reenviarConError(req, resp, "Producto no encontrado: " + prodId, "/vendedor/registrar_venta.jsp");
                    return;
                }
                
                // ■■ RF16: Validar que haya stock suficiente ■■
                if (prod.getStock() < cant) {
                    reenviarConError(req, resp, 
                        "Stock insuficiente para: " + prod.getNombre() + 
                        ". Disponible: " + prod.getStock() + ", Solicitado: " + cant, 
                        "/vendedor/registrar_venta.jsp");
                    return;
                }

                DetalleVenta detalle = new DetalleVenta(prodId, prod.getNombre(), cant, precio, cant);
                detalles.add(detalle);
                total = total.add(detalle.getSubtotal());
                
            } catch (NumberFormatException e) {
                reenviarConError(req, resp, "Formato de número inválido en los datos del producto.", "/vendedor/registrar_venta.jsp");
                return;
            }
        }

        if (detalles.isEmpty()) {
            reenviarConError(req, resp, "No hay productos válidos en la venta.", "/vendedor/registrar_venta.jsp");
            return;
        }

        // Validaciones de anticipo
        BigDecimal montoAnticipo = null;
        BigDecimal saldoPendiente = null;
        
        if ("anticipo".equals(modalidad)) {
            String anticipoStr = req.getParameter("montoAnticipo");
            if (anticipoStr == null || anticipoStr.isBlank()) {
                reenviarConError(req, resp, "Ingresa el monto del anticipo.", "/vendedor/registrar_venta.jsp");
                return;
            }
            montoAnticipo = new BigDecimal(anticipoStr);
            if (montoAnticipo.compareTo(BigDecimal.ZERO) <= 0 || montoAnticipo.compareTo(total) >= 0) {
                reenviarConError(req, resp, "El anticipo debe ser mayor a 0 y menor al total.", "/vendedor/registrar_venta.jsp");
                return;
            }
            saldoPendiente = total.subtract(montoAnticipo);
        } else {
            saldoPendiente = BigDecimal.ZERO;
        }

        // Registrar cliente y venta
        int clienteId = clienteDAO.registrarOObtenerCliente(nombreCliente, telefonoCliente, emailCliente);
        Date fechaEmision = new SimpleDateFormat("yyyy-MM-dd").parse(fechaStr);
        Usuario vendedor = getVendedor(req);
        
        if (vendedor == null) {
            reenviarConError(req, resp, "Sesión expirada. Inicia sesión nuevamente.", "/vendedor/registrar_venta.jsp");
            return;
        }

        Venta venta = new Venta(vendedor.getUsuarioId(), clienteId, fechaEmision, total, metodoPago);
        venta.setDetalles(detalles);
        venta.setModalidad(modalidad);
        venta.setSaldoPendiente(saldoPendiente);

        int ventaIdGenerado = ventaDAO.insertar(venta, detalles, modalidad, montoAnticipo, saldoPendiente, vendedor.getUsuarioId());

        if (ventaIdGenerado > 0) {
            req.setAttribute("mensaje", "Venta #" + ventaIdGenerado + " registrada exitosamente.");
            req.setAttribute("venta", ventaDAO.obtenerPorId(ventaIdGenerado));
            req.getRequestDispatcher("/vendedor/venta_confirmada.jsp").forward(req, resp);
        } else {
            throw new Exception("No se pudo guardar la venta en la base de datos.");
        }
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // LÓGICA POST: Abonar saldo
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private void procesarAbono(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int ventaId = parseId(req.getParameter("ventaId"));
        String montoStr = req.getParameter("montoAbono");
        
        if (ventaId <= 0 || montoStr == null || montoStr.isBlank()) {
            reenviarConError(req, resp, "Datos de abono inválidos.", "/vendedor/ver_venta.jsp");
            return;
        }
        
        BigDecimal monto = new BigDecimal(montoStr);
        Venta venta = ventaDAO.obtenerPorId(ventaId);
        Usuario vendedor = getVendedor(req);
        
        // ■■ Validación con primitivos int ■■
        if (!esVentaDelVendedor(venta, vendedor)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (venta.getSaldoPendiente() == null || monto.compareTo(venta.getSaldoPendiente()) > 0) {
            reenviarConError(req, resp, "El monto no puede superar el saldo pendiente ($" + venta.getSaldoPendiente() + ").", "/vendedor/ver_venta.jsp");
            return;
        }

        ventaDAO.abonarSaldo(ventaId, monto);
        resp.sendRedirect(req.getContextPath() + "/VentaVendedorServlet?action=verVenta&id=" + ventaId + "&exito=abono");
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // LÓGICA POST: Guardar caso postventa
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private void guardarPostventa(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int ventaId = parseId(req.getParameter("ventaId"));
        String tipo = req.getParameter("tipo");
        String cantidadStr = req.getParameter("cantidad");
        String motivo = req.getParameter("motivo");

        if (ventaId <= 0 || cantidadStr == null || cantidadStr.isBlank()) {
            reenviarConError(req, resp, "Datos del caso postventa inválidos.", "/vendedor/registrar_postventa.jsp");
            return;
        }

        Venta venta = ventaDAO.obtenerPorId(ventaId);
        Usuario vendedor = getVendedor(req);
        
        // ■■ Validación con primitivos int ■■
        if (!esVentaDelVendedor(venta, vendedor)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (!Arrays.asList("cambio", "devolucion", "reclamo").contains(tipo)) {
            reenviarConError(req, resp, "Tipo de caso inválido.", "/vendedor/registrar_postventa.jsp");
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            reenviarConError(req, resp, "La cantidad debe ser un número válido mayor a 0.", "/vendedor/registrar_postventa.jsp");
            return;
        }

        CasoPostventa caso = new CasoPostventa();
        caso.setVentaId(ventaId);
        caso.setTipo(tipo);
        caso.setCantidad(cantidad);
        caso.setMotivo(motivo != null ? motivo : "");
        caso.setFecha(new Date());

        int casoId = postventaDAO.registrar(caso);

        if (casoId > 0) {
            req.setAttribute("mensaje", "Caso #" + casoId + " registrado. Queda en revisión.");
            req.setAttribute("caso", postventaDAO.obtenerPorId(casoId));
            req.getRequestDispatcher("/vendedor/postventa_confirmada.jsp").forward(req, resp);
        } else {
            throw new Exception("No se pudo registrar el caso postventa.");
        }
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // AUXILIARES
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private boolean estaAutenticado(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("vendedor") == null) {
            String action = req.getParameter("action");
            // ■■ Permitir JSON para autocomplete sin redirigir ■■
            if ("obtenerCategorias".equals(action) || "obtenerProductosPorCategoria".equals(action)) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"error\":\"No autenticado\"}");
                return false;
            }
            resp.sendRedirect(req.getContextPath() + "/inicio-sesion.jsp");
            return false;
        }
        return true;
    }

    private Usuario getVendedor(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        return (Usuario) session.getAttribute("vendedor");
    }

    private int parseId(String param) {
        if (param == null || !param.matches("\\d+")) return -1;
        try {
            return Integer.parseInt(param);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void reenviarConError(HttpServletRequest req, HttpServletResponse resp, String mensaje, String vista) throws ServletException, IOException {
        req.setAttribute("error", mensaje);
        // Recargar datos necesarios para el formulario
        if (vista.contains("registrar_venta")) {
            req.setAttribute("categorias", categoriaDAO.listarCategorias());
        }
        req.getRequestDispatcher(vista).forward(req, resp);
    }

    // ■■ CORREGIDO: Escape JSON más robusto ■■
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("/", "\\/")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    @Override
    public void destroy() {
        // Liberar recursos si es necesario
        ventaDAO = null;
        productoDAO = null;
        clienteDAO = null;
        postventaDAO = null;
        categoriaDAO = null;
    }
}