package controller;

import dao.CategoriaDAO;
import dao.ClienteDAO;
import dao.MetodoPagoDAO;
import dao.PostventaDAO;
import dao.ProductoDAO;
import dao.VentaDAO;
import model.CasoPostventa;
import model.Categoria;
import model.DetalleVenta;
import model.MetodoPago;
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

/**
 * Servlet principal del módulo de ventas para el rol de vendedor.
 * Maneja todo el flujo de ventas y postventa desde la perspectiva del vendedor:
 *   - Registrar nuevas ventas
 *   - Ver y listar sus propias ventas
 *   - Registrar casos postventa
 *   - Ver sus casos postventa
 *   - Descargar facturas en PDF
 *
 * El parámetro "action" en la URL determina qué operación se ejecuta.
 */
@WebServlet("/VentaVendedorServlet")
public class VentaVendedorServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private VentaDAO ventaDAO;
    private ProductoDAO productoDAO;
    private ClienteDAO clienteDAO;
    private PostventaDAO postventaDAO;
    private CategoriaDAO categoriaDAO;
    private MetodoPagoDAO metodoPagoDAO;

    /** Inicializa todos los DAOs una vez al cargar el servlet. */
    @Override
    public void init() throws ServletException {
        ventaDAO      = new VentaDAO();
        productoDAO   = new ProductoDAO();
        clienteDAO    = new ClienteDAO();
        postventaDAO  = new PostventaDAO();
        categoriaDAO  = new CategoriaDAO();
        metodoPagoDAO = new MetodoPagoDAO();
    }

    // ── Peticiones GET ────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Verifica sesión antes de cualquier operación
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
                case "descargarFactura"             -> descargarFacturaPDF(req, resp);
                // Si la acción no es reconocida, redirige al listado de ventas
                default -> resp.sendRedirect(req.getContextPath() + "/VentaVendedorServlet?action=misVentas");
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Ocurrió un error inesperado. Por favor, intente nuevamente.");
            req.getRequestDispatcher("/vendedor/ventas_realizadas.jsp").forward(req, resp);
        }
    }

    // ── Peticiones POST ───────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!estaAutenticado(req, resp)) return;
        // Necesario para leer caracteres especiales (tildes, ñ) correctamente desde el formulario
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
            e.printStackTrace();
            req.setAttribute("error", "No se pudo completar la operación. Por favor, intente nuevamente.");
            req.getRequestDispatcher("/vendedor/mensajesexito.jsp").forward(req, resp);
        }
    }

    // ── Respuestas JSON (para el formulario dinámico de productos) ────────────

    /**
     * Retorna la lista de categorías en formato JSON.
     * Este endpoint es llamado por el JavaScript del formulario de nueva venta
     * para cargar las categorías sin recargar la página.
     */
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
            resp.getWriter().write("[]"); // Si falla, retorna lista vacía para no romper el JS
        }
    }

    /**
     * Retorna los productos de una categoría en formato JSON.
     * El JavaScript del formulario llama este endpoint al seleccionar una categoría.
     *
     * matches("\\d+") verifica que el parámetro sea un número antes de usarlo.
     */
    private void obtenerProductosPorCategoriaJSON(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        String categoriaIdStr = req.getParameter("categoriaId");

        // Validación: si no viene el parámetro o no es un número, retorna lista vacía
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

    // ── Vistas GET ────────────────────────────────────────────────────────────

    /** Prepara los datos necesarios y muestra el formulario de nueva venta. */
    private void mostrarFormularioNueva(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        cargarAtributosFormulario(req);
        req.getRequestDispatcher("/vendedor/registrar_venta.jsp").forward(req, resp);
    }

    /**
     * Carga categorías y métodos de pago en el request.
     * Se separa en un método propio porque se necesita tanto al abrir el formulario
     * como al regresar a él después de un error de validación.
     */
    private void cargarAtributosFormulario(HttpServletRequest req) {
        try {
            req.setAttribute("categorias", categoriaDAO.listarCategorias());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            req.setAttribute("metodosPago", metodoPagoDAO.listarTodos());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Carga y muestra todas las ventas registradas por el vendedor en sesión. */
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

    /**
     * Verifica que la venta pertenece al vendedor que la solicita.
     * Esto evita que un vendedor pueda ver ventas de otro vendedor manipulando la URL.
     */
    private boolean esVentaDelVendedor(Venta venta, Usuario vendedor) {
        if (venta == null || vendedor == null) return false;
        return venta.getUsuarioId() == vendedor.getUsuarioId();
    }

    /** Muestra el detalle de una venta verificando que pertenezca al vendedor en sesión. */
    private void verVenta(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int id = parseId(req.getParameter("id"));
        if (id <= 0) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de venta inválido"); return; }

        Venta venta      = ventaDAO.obtenerPorId(id);
        Usuario vendedor = getVendedor(req);

        // Control de acceso: solo el vendedor dueño puede ver la venta
        if (!esVentaDelVendedor(venta, vendedor)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "No tienes permiso para ver esta venta");
            return;
        }
        req.setAttribute("venta", venta);
        req.setAttribute("detalles", venta.getDetalles());
        if ("true".equals(req.getParameter("imprimir"))) req.setAttribute("imprimir", true);
        req.getRequestDispatcher("/vendedor/ver_venta.jsp").forward(req, resp);
    }

    /** Carga la venta y muestra el formulario para registrar un caso postventa. */
    private void mostrarFormularioPostventa(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int ventaId      = parseId(req.getParameter("ventaId"));
        if (ventaId <= 0) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de venta inválido"); return; }

        Venta venta      = ventaDAO.obtenerPorId(ventaId);
        Usuario vendedor = getVendedor(req);

        if (!esVentaDelVendedor(venta, vendedor)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "No tienes permiso para registrar postventa en esta venta");
            return;
        }
        req.setAttribute("venta", venta);
        req.setAttribute("detalles", venta.getDetalles());
        req.getRequestDispatcher("/vendedor/registrar_postventa.jsp").forward(req, resp);
    }

    /**
     * Carga y muestra todos los casos postventa del vendedor en sesión.
     * El atributo "casos" que se pone en el request es leído por casos_postventa.jsp.
     */
    private void listarMisCasos(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Usuario vendedor = getVendedor(req);
        if (vendedor == null) {
            resp.sendRedirect(req.getContextPath() + "/inicio-sesion.jsp");
            return;
        }
        List<CasoPostventa> casos = postventaDAO.listarPorVendedor(vendedor.getUsuarioId());
        // El JSP casos_postventa.jsp espera este atributo con exactamente este nombre "casos"
        req.setAttribute("casos", casos);
        req.getRequestDispatcher("/vendedor/casos_postventa.jsp").forward(req, resp);
    }

    /** Genera y envía la factura de una venta en formato PDF. */
    private void descargarFacturaPDF(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int ventaId      = parseId(req.getParameter("id"));
        if (ventaId <= 0) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de venta inválido"); return; }

        Venta venta      = ventaDAO.obtenerPorId(ventaId);
        Usuario vendedor = getVendedor(req);
        if (!esVentaDelVendedor(venta, vendedor)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado");
            return;
        }

        try {
            byte[] pdfBytes = PDFGenerator.generarFacturaPDF(venta);
            resp.setContentType("application/pdf");
            // Content-Disposition: attachment fuerza al navegador a descargar el archivo
            resp.setHeader("Content-Disposition", "attachment; filename=\"Factura_" + ventaId + ".pdf\"");
            resp.setContentLength(pdfBytes.length);
            resp.getOutputStream().write(pdfBytes);
            resp.getOutputStream().flush();
        } catch (Exception e) {
            // Si falla la generación del PDF, muestra la venta en pantalla para que pueda imprimir
            req.setAttribute("venta", venta);
            req.setAttribute("detalles", venta.getDetalles());
            req.setAttribute("imprimir", true);
            req.getRequestDispatcher("/vendedor/ver_venta.jsp").forward(req, resp);
        }
    }

    // ── Guardar venta ─────────────────────────────────────────────────────────

    /**
     * Procesa el formulario de nueva venta:
     * 1. Valida todos los campos del cliente y de la venta.
     * 2. Arma la lista de productos del carrito.
     * 3. Valida reglas de negocio (stock, mínimo para crédito, anticipo válido).
     * 4. Guarda la venta en la base de datos.
     * 5. Redirige a la página de confirmación.
     */
    private void guardarVenta(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String nombreCliente   = req.getParameter("clienteNombre");
        String telefonoCliente = req.getParameter("clienteTelefono");
        String emailCliente    = req.getParameter("clienteEmail");
        String fechaStr        = req.getParameter("fechaVenta");
        String metodoPago      = req.getParameter("metodoPago");
        String tipoPago        = req.getParameter("tipoPago");
        // "CREDITO" se convierte a la modalidad interna "anticipo"
        String modalidad       = "CREDITO".equals(tipoPago) ? "anticipo" : "contado";

        // Validaciones de campos obligatorios
        // isBlank() verifica que no sea null, vacío ni solo espacios
        if (nombreCliente == null || nombreCliente.isBlank()) {
            reenviarConError(req, resp, "El nombre del cliente es obligatorio.", "/vendedor/registrar_venta.jsp"); return;
        }
        if (metodoPago == null || metodoPago.isBlank()) {
            reenviarConError(req, resp, "El método de pago es obligatorio.", "/vendedor/registrar_venta.jsp"); return;
        }
        if (fechaStr == null || fechaStr.isBlank()) {
            reenviarConError(req, resp, "La fecha de venta es obligatoria.", "/vendedor/registrar_venta.jsp"); return;
        }

        // Productos del carrito: llegan como arrays paralelos (mismo índice = mismo producto)
        String[] productoIds = req.getParameterValues("productoId");
        String[] cantidades  = req.getParameterValues("cantidad");
        String[] precios     = req.getParameterValues("precioUnitario");

        if (productoIds == null || productoIds.length == 0) {
            reenviarConError(req, resp, "Debes agregar al menos un producto a la venta.", "/vendedor/registrar_venta.jsp"); return;
        }

        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        // Se construye la lista de detalles validando cada producto
        for (int i = 0; i < productoIds.length; i++) {
            if (productoIds[i] == null || productoIds[i].trim().isEmpty()) continue;
            try {
                int prodId        = Integer.parseInt(productoIds[i].trim());
                int cant          = Integer.parseInt(cantidades[i].trim());
                BigDecimal precio = new BigDecimal(precios[i].trim());
                Producto prod     = productoDAO.obtenerProductoConStock(prodId);

                if (prod == null) {
                    reenviarConError(req, resp, "No se encontró el producto seleccionado.", "/vendedor/registrar_venta.jsp"); return;
                }
                // Validación de stock: no se puede vender más de lo disponible
                if (prod.getStock() < cant) {
                    reenviarConError(req, resp,
                        "Stock insuficiente para " + prod.getNombre() +
                        ". Disponible: " + prod.getStock() + ", Solicitado: " + cant,
                        "/vendedor/registrar_venta.jsp"); return;
                }
                DetalleVenta detalle = new DetalleVenta(prodId, prod.getNombre(), cant, precio, cant);
                detalles.add(detalle);
                total = total.add(detalle.getSubtotal());
            } catch (NumberFormatException e) {
                reenviarConError(req, resp, "Los datos del producto contienen un formato inválido.", "/vendedor/registrar_venta.jsp"); return;
            }
        }

        if (detalles.isEmpty()) {
            reenviarConError(req, resp, "No se encontraron productos válidos en la venta.", "/vendedor/registrar_venta.jsp"); return;
        }

        // Regla de negocio: el crédito solo aplica para compras mayores a $250.000
        if ("anticipo".equals(modalidad) && total.compareTo(new BigDecimal("250000")) <= 0) {
            reenviarConError(req, resp, "El crédito solo está disponible para compras mayores a $250.000.", "/vendedor/registrar_venta.jsp"); return;
        }

        BigDecimal montoAnticipo = null;
        BigDecimal saldoPendiente;

        if ("anticipo".equals(modalidad)) {
            String anticipoStr = req.getParameter("anticipo");
            if (anticipoStr == null || anticipoStr.isBlank()) {
                reenviarConError(req, resp, "Debes ingresar el monto del anticipo.", "/vendedor/registrar_venta.jsp"); return;
            }
            montoAnticipo = new BigDecimal(anticipoStr);
            // El anticipo debe ser mayor a 0 y menor al total (no puede ser pago completo)
            if (montoAnticipo.compareTo(BigDecimal.ZERO) <= 0 || montoAnticipo.compareTo(total) >= 0) {
                reenviarConError(req, resp, "El anticipo debe ser mayor a $0 y menor al total de la venta.", "/vendedor/registrar_venta.jsp"); return;
            }
            saldoPendiente = total.subtract(montoAnticipo);
        } else {
            saldoPendiente = BigDecimal.ZERO;
        }

        // Si el cliente no existe, se crea automáticamente. Si ya existe, se retorna su ID.
        int clienteId    = clienteDAO.registrarOObtenerCliente(nombreCliente, telefonoCliente, emailCliente);
        Date fechaEmision = new SimpleDateFormat("yyyy-MM-dd").parse(fechaStr);
        Usuario vendedor  = getVendedor(req);

        if (vendedor == null) {
            reenviarConError(req, resp, "Tu sesión expiró. Por favor, inicia sesión nuevamente.", "/vendedor/registrar_venta.jsp"); return;
        }

        Venta venta = new Venta(vendedor.getUsuarioId(), clienteId, fechaEmision, total, metodoPago);
        venta.setDetalles(detalles);
        venta.setModalidad(modalidad);
        venta.setSaldoPendiente(saldoPendiente);

        int ventaIdGenerado = ventaDAO.insertar(venta, detalles, modalidad, montoAnticipo, saldoPendiente, vendedor.getUsuarioId());

        if (ventaIdGenerado > 0) {
            req.setAttribute("mensaje", "¡Venta #" + ventaIdGenerado + " registrada correctamente!");
            req.setAttribute("venta", ventaDAO.obtenerPorId(ventaIdGenerado));
            req.getRequestDispatcher("/vendedor/venta_confirmada.jsp").forward(req, resp);
        } else {
            throw new Exception("No se pudo guardar la venta. Intente nuevamente.");
        }
    }

    // ── Procesar abono ────────────────────────────────────────────────────────

    /**
     * Registra un abono a una venta con saldo pendiente.
     * Verifica que el monto no supere el saldo y que la venta pertenezca al vendedor.
     * Tras el abono exitoso, usa sendRedirect con "exito=abono" en la URL para que
     * el JSP de destino muestre el mensaje de éxito sin repetir el POST si se recarga.
     */
    private void procesarAbono(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int ventaId     = parseId(req.getParameter("ventaId"));
        String montoStr = req.getParameter("montoAbono");

        if (ventaId <= 0 || montoStr == null || montoStr.isBlank()) {
            reenviarConError(req, resp, "Los datos del abono no son válidos.", "/vendedor/ver_venta.jsp"); return;
        }

        BigDecimal monto  = new BigDecimal(montoStr);
        Venta venta       = ventaDAO.obtenerPorId(ventaId);
        Usuario vendedor  = getVendedor(req);

        if (!esVentaDelVendedor(venta, vendedor)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        // El abono no puede superar el saldo pendiente actual
        if (venta.getSaldoPendiente() == null || monto.compareTo(venta.getSaldoPendiente()) > 0) {
            reenviarConError(req, resp,
                "El monto del abono no puede superar el saldo pendiente ($" + venta.getSaldoPendiente() + ").",
                "/vendedor/ver_venta.jsp"); return;
        }

        ventaDAO.abonarSaldo(ventaId, monto);
        // sendRedirect evita que al refrescar la página se procese el abono nuevamente
        resp.sendRedirect(req.getContextPath() + "/VentaVendedorServlet?action=verVenta&id=" + ventaId + "&exito=abono");
    }

    // ── Guardar postventa ─────────────────────────────────────────────────────

    /**
     * Procesa el formulario de nuevo caso postventa:
     * 1. Valida los datos del formulario.
     * 2. Verifica que el tipo sea válido (cambio, devolucion, reclamo).
     * 3. Registra el caso en la base de datos.
     * 4. Muestra la página de confirmación con el resumen del caso.
     */
    private void guardarPostventa(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int ventaId        = parseId(req.getParameter("ventaId"));
        String tipo        = req.getParameter("tipo");
        String cantidadStr = req.getParameter("cantidad");
        String motivo      = req.getParameter("motivo");

        if (ventaId <= 0 || cantidadStr == null || cantidadStr.isBlank()) {
            reenviarConError(req, resp, "Los datos del caso no son válidos.", "/vendedor/registrar_postventa.jsp"); return;
        }

        Venta venta      = ventaDAO.obtenerPorId(ventaId);
        Usuario vendedor = getVendedor(req);

        if (!esVentaDelVendedor(venta, vendedor)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Validación del tipo: solo se permiten los tres tipos definidos en el sistema
        if (!Arrays.asList("cambio", "devolucion", "reclamo").contains(tipo)) {
            reenviarConError(req, resp, "El tipo de caso seleccionado no es válido.", "/vendedor/registrar_postventa.jsp"); return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            reenviarConError(req, resp, "La cantidad debe ser un número mayor a 0.", "/vendedor/registrar_postventa.jsp"); return;
        }

        CasoPostventa caso = new CasoPostventa();
        caso.setVentaId(ventaId);
        caso.setTipo(tipo);
        caso.setCantidad(cantidad);
        caso.setMotivo(motivo != null ? motivo : "");
        caso.setFecha(new Date());

        int casoId = postventaDAO.registrar(caso);
        if (casoId > 0) {
            req.setAttribute("mensaje", "Caso #" + casoId + " registrado correctamente. Quedará en revisión por el administrador.");
            req.setAttribute("caso", postventaDAO.obtenerPorId(casoId));
            req.getRequestDispatcher("/vendedor/postventa_confirmada.jsp").forward(req, resp);
        } else {
            throw new Exception("No se pudo registrar el caso postventa. Intente nuevamente.");
        }
    }

    // ── Métodos auxiliares ────────────────────────────────────────────────────

    /**
     * Verifica que el vendedor esté logueado.
     * Las rutas JSON retornan un error 401 en vez de redirigir, porque el JavaScript
     * no puede seguir redirecciones HTML.
     */
    private boolean estaAutenticado(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false); // false: no crea sesión nueva si no existe
        if (session == null || session.getAttribute("vendedor") == null) {
            String action = req.getParameter("action");
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

    /** Obtiene el objeto Usuario del vendedor desde la sesión activa. */
    private Usuario getVendedor(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        return (Usuario) session.getAttribute("vendedor");
    }

    /**
     * Convierte un String a entero de forma segura.
     * matches("\\d+") verifica que solo contenga dígitos antes de convertir.
     * Retorna -1 si es nulo o no es un número válido.
     */
    private int parseId(String param) {
        if (param == null || !param.matches("\\d+")) return -1;
        try { return Integer.parseInt(param); } catch (NumberFormatException e) { return -1; }
    }

    /**
     * Reenvía al formulario de origen con un mensaje de error.
     * Si el formulario destino es el de registrar venta, también carga de nuevo
     * los datos del formulario (categorías y métodos de pago) para que no queden vacíos.
     */
    private void reenviarConError(HttpServletRequest req, HttpServletResponse resp,
                                  String mensaje, String vista)
            throws ServletException, IOException {
        req.setAttribute("error", mensaje);
        if (vista.contains("registrar_venta")) {
            cargarAtributosFormulario(req);
        }
        req.getRequestDispatcher(vista).forward(req, resp);
    }

    /**
     * Escapa caracteres especiales para que el texto sea seguro dentro de un JSON.
     * Evita que comillas, barras inversas u otros caracteres rompan la estructura JSON.
     */
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

    /** Libera los DAOs cuando el servidor destruye el servlet. */
    @Override
    public void destroy() {
        ventaDAO      = null;
        productoDAO   = null;
        clienteDAO    = null;
        postventaDAO  = null;
        categoriaDAO  = null;
        metodoPagoDAO = null;
    }
}
