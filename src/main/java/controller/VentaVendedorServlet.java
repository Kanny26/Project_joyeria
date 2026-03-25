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
 * Todas las operaciones requieren autenticación válida de vendedor y validan
 * que el usuario solo pueda acceder a sus propios registros.
 *
 * @see VentaDAO
 * @see ProductoDAO
 * @see PostventaDAO
 * @see PDFGenerator
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

    /**
     * Inicializa el servlet creando las instancias de todos los DAOs necesarios.
     * Este método se ejecuta una única vez cuando el servlet es cargado por el contenedor,
     * permitiendo reutilizar las instancias en todas las peticiones posteriores.
     *
     * @throws ServletException si el contenedor no puede completar la inicialización
     */
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

    /**
     * Procesa las solicitudes HTTP GET para el módulo de ventas del vendedor.
     *
     * Según el parámetro {@code action}, ejecuta una de las siguientes operaciones:
     * - "nueva": muestra el formulario para registrar una nueva venta
     * - "verVenta": muestra el detalle de una venta específica con control de acceso
     * - "misVentas": lista todas las ventas registradas por el vendedor en sesión
     * - "registrarPostventa": muestra el formulario para crear un caso postventa
     * - "misCasos": lista todos los casos postventa del vendedor
     * - "obtenerCategorias": devuelve categorías en JSON para carga dinámica
     * - "obtenerProductosPorCategoria": devuelve productos filtrados por categoría en JSON
     * - "descargarFactura": genera y envía la factura de una venta en formato PDF
     *
     * Si no se especifica una acción válida o ocurre un error, redirige al listado de ventas.
     *
     * @param req  la petición HTTP que contiene el parámetro de acción y otros datos
     * @param resp la respuesta HTTP utilizada para forwards, redirecciones o respuestas JSON/PDF
     * @throws ServletException si ocurre un error durante el procesamiento del servlet
     * @throws IOException      si ocurre un error de entrada/salida al enviar la respuesta
     */
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

    /**
     * Procesa las solicitudes HTTP POST para ejecutar operaciones de modificación en ventas y postventa.
     *
     * Según el valor del parámetro {@code action}, ejecuta una de las siguientes operaciones:
     * - "guardarVenta": registra una nueva venta validando stock, reglas de crédito y datos del cliente
     * - "abonar": procesa un abono a una venta con saldo pendiente
     * - "guardarPostventa": registra un nuevo caso postventa (cambio, devolución o reclamo)
     *
     * Configura la codificación UTF-8 para manejar correctamente caracteres especiales.
     * En caso de error, realiza forward a la vista con el mensaje de error para su visualización.
     *
     * @param req  la petición HTTP con {@code action} y datos del formulario
     * @param resp la respuesta HTTP utilizada para redirecciones o forwards con errores
     * @throws ServletException si ocurre un error durante el procesamiento del servlet
     * @throws IOException      si ocurre un error de entrada/salida al enviar la respuesta
     */
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
     * Devuelve la lista de categorías disponibles en formato JSON.
     *
     * Este endpoint es consumido por JavaScript del formulario de nueva venta
     * para poblar dinámicamente el selector de categorías sin recargar la página.
     * Configura cabeceras anti-caché para evitar que el navegador almacene respuestas obsoletas.
     *
     * @param resp la respuesta HTTP configurada con contenido JSON y codificación UTF-8
     * @throws IOException si ocurre un error al escribir la respuesta JSON
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
     * Devuelve los productos de una categoría específica en formato JSON.
     *
     * El JavaScript del formulario llama este endpoint al seleccionar una categoría
     * para cargar dinámicamente los productos disponibles. Valida que el parámetro
     * {@code categoriaId} sea numérico antes de consultar la base de datos.
     *
     * @param req  la petición HTTP que contiene el parámetro {@code categoriaId}
     * @param resp la respuesta HTTP configurada con contenido JSON y codificación UTF-8
     * @throws IOException si ocurre un error al escribir la respuesta JSON
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

    /**
     * Prepara los datos necesarios y muestra el formulario para registrar una nueva venta.
     *
     * Carga las listas de categorías y métodos de pago disponibles para populatear
     * los selectores del formulario antes de transferir el control a la vista JSP.
     *
     * @param req  la petición HTTP recibida del vendedor
     * @param resp la respuesta HTTP utilizada para forwards a la vista del formulario
     * @throws ServletException si ocurre un error al despachar la vista
     * @throws IOException      si ocurre un error de entrada/salida
     */
    private void mostrarFormularioNueva(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        cargarAtributosFormulario(req);
        req.getRequestDispatcher("/vendedor/registrar_venta.jsp").forward(req, resp);
    }

    /**
     * Carga las listas auxiliares necesarias para el formulario de nueva venta.
     *
     * Recupera desde la base de datos:
     * - Categorías de productos para el selector dinámico
     * - Métodos de pago disponibles para la transacción
     *
     * Este método se utiliza tanto al abrir el formulario inicialmente como al
     * reenviarlo tras un error de validación, para conservar los datos cargados.
     *
     * @param req la petición HTTP a la que se le agregarán los atributos con las listas
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

    /**
     * Carga y muestra todas las ventas registradas por el vendedor autenticado en sesión.
     *
     * Recupera el objeto Usuario del vendedor desde la sesión y consulta la base de datos
     * para obtener únicamente las ventas asociadas a su ID. Los resultados se pasan a la vista
     * mediante atributos de la petición.
     *
     * @param req  la petición HTTP para acceder a la sesión del vendedor
     * @param resp la respuesta HTTP utilizada para forwards a la vista de listado
     * @throws Exception si ocurre un error al consultar la base de datos
     */
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
     * Verifica que una venta pertenezca al vendedor que solicita acceder a ella.
     *
     * Este método implementa un control de acceso a nivel de aplicación para prevenir
     * que un vendedor pueda visualizar ventas de otros usuarios manipulando parámetros en la URL.
     *
     * @param venta    el objeto Venta a verificar
     * @param vendedor el objeto Usuario del vendedor en sesión
     * @return true si la venta existe, el vendedor existe y sus IDs coinciden; false en caso contrario
     */
    private boolean esVentaDelVendedor(Venta venta, Usuario vendedor) {
        if (venta == null || vendedor == null) return false;
        return venta.getUsuarioId() == vendedor.getUsuarioId();
    }

    /**
     * Muestra el detalle completo de una venta específica con control de acceso.
     *
     * Valida que el ID de la venta sea válido y que pertenezca al vendedor autenticado.
     * Si el parámetro {@code imprimir} está presente, marca la vista para mostrar
     * una versión optimizada para impresión.
     *
     * @param req  la petición HTTP que contiene el parámetro {@code id} de la venta
     * @param resp la respuesta HTTP utilizada para forwards o errores HTTP
     * @throws Exception si ocurre un error al consultar la base de datos
     */
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

    /**
     * Carga los datos de una venta y muestra el formulario para registrar un caso postventa.
     *
     * Valida que el ID de la venta sea válido y que pertenezca al vendedor autenticado
     * antes de permitir el registro de un caso postventa asociado.
     *
     * @param req  la petición HTTP que contiene el parámetro {@code ventaId}
     * @param resp la respuesta HTTP utilizada para forwards o errores HTTP
     * @throws Exception si ocurre un error al consultar la base de datos
     */
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
     * Carga y muestra todos los casos postventa registrados por el vendedor en sesión.
     *
     * Recupera el objeto Usuario del vendedor y consulta la base de datos para obtener
     * únicamente los casos asociados a su ID. El atributo {@code casos} se pasa a la vista
     * con el nombre exacto esperado por el JSP.
     *
     * @param req  la petición HTTP para acceder a la sesión del vendedor
     * @param resp la respuesta HTTP utilizada para forwards a la vista de listado
     * @throws Exception si ocurre un error al consultar la base de datos
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

    /**
     * Genera y envía la factura de una venta en formato PDF para descarga.
     *
     * Valida que la venta exista y pertenezca al vendedor autenticado antes de generar
     * el documento. Configura las cabeceras HTTP apropiadas para forzar la descarga
     * del archivo PDF con un nombre descriptivo.
     *
     * Si falla la generación del PDF, muestra la vista de detalle de venta con la opción
     * de impresión alternativa como respaldo.
     *
     * @param req  la petición HTTP que contiene el parámetro {@code id} de la venta
     * @param resp la respuesta HTTP configurada para enviar el contenido PDF o forwards de respaldo
     * @throws Exception si ocurre un error al consultar la base de datos o generar el PDF
     */
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
     * Procesa el formulario de nueva venta con validación exhaustiva de datos y reglas de negocio.
     *
     * Flujo de procesamiento:
     * 1. Valida campos obligatorios del cliente y de la transacción
     * 2. Construye la lista de detalles de venta desde arrays paralelos del formulario
     * 3. Verifica stock disponible para cada producto antes de permitir la venta
     * 4. Aplica reglas de negocio: crédito solo para compras > $250.000, anticipo válido
     * 5. Registra o recupera el cliente en la base de datos
     * 6. Persiste la venta con sus detalles, modalidad y saldos en una transacción
     * 7. Redirige a la vista de confirmación con el ID de venta generado
     *
     * En caso de error de validación, reenvía al formulario original conservando
     * los datos ingresados y mostrando el mensaje de error correspondiente.
     *
     * @param req  la petición HTTP que contiene los datos del formulario de nueva venta
     * @param resp la respuesta HTTP utilizada para forwards con errores o redirecciones de éxito
     * @throws Exception si ocurre un error de validación, persistencia o formato de datos
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
     * Registra un abono parcial al saldo pendiente de una venta a crédito.
     *
     * Valida que los datos del abono sean correctos, que la venta pertenezca
     * al vendedor autenticado y que el monto no supere el saldo pendiente actual.
     * Tras persistir el abono, redirige con un parámetro de éxito para evitar
     * que el usuario procese el mismo abono múltiples veces al refrescar la página.
     *
     * @param req  la petición HTTP que contiene {@code ventaId} y {@code montoAbono}
     * @param resp la respuesta HTTP utilizada para forwards con errores o redirecciones de éxito
     * @throws Exception si ocurre un error de validación, persistencia o formato de datos
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
     * Procesa el formulario de nuevo caso postventa con validación de datos y reglas de negocio.
     *
     * Flujo de procesamiento:
     * 1. Valida que los parámetros obligatorios estén presentes y sean válidos
     * 2. Verifica que la venta exista y pertenezca al vendedor autenticado
     * 3. Valida que el tipo de caso sea uno de los permitidos: cambio, devolución o reclamo
     * 4. Construye el objeto CasoPostventa con los datos del formulario
     * 5. Persiste el caso en la base de datos y redirige a la vista de confirmación
     *
     * En caso de error, reenvía al formulario original con el mensaje de error correspondiente.
     *
     * @param req  la petición HTTP que contiene los datos del formulario de postventa
     * @param resp la respuesta HTTP utilizada para forwards con errores o redirecciones de éxito
     * @throws Exception si ocurre un error de validación, persistencia o formato de datos
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
     * Verifica que exista una sesión activa con un vendedor autenticado.
     *
     * Para endpoints JSON, responde con error HTTP 401 en formato JSON en lugar
     * de redirigir, ya que el JavaScript del cliente no puede seguir redirecciones HTML.
     * Para peticiones normales, redirige a la página de inicio de sesión.
     *
     * @param req  la petición HTTP para acceder a la sesión
     * @param resp la respuesta HTTP utilizada para redirigir o devolver error JSON
     * @return true si la sesión contiene un vendedor válido, false en caso contrario
     * @throws IOException si ocurre un error al escribir la respuesta o realizar la redirección
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

    /**
     * Obtiene el objeto Usuario del vendedor desde la sesión HTTP activa.
     *
     * @param req la petición HTTP para acceder a la sesión
     * @return el objeto Usuario del vendedor si la sesión es válida, null en caso contrario
     */
    private Usuario getVendedor(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        return (Usuario) session.getAttribute("vendedor");
    }

    /**
     * Convierte un parámetro de tipo String a entero de forma segura.
     *
     * Valida que el valor contenga únicamente dígitos antes de intentar la conversión.
     * Retorna -1 como valor indicador de error si el parámetro es nulo, vacío o no numérico.
     *
     * @param param el valor de parámetro a convertir
     * @return el valor entero si la conversión es exitosa, -1 si el parámetro es inválido
     */
    private int parseId(String param) {
        if (param == null || !param.matches("\\d+")) return -1;
        try { return Integer.parseInt(param); } catch (NumberFormatException e) { return -1; }
    }

    /**
     * Reenvía al usuario al formulario de origen con un mensaje de error descriptivo.
     *
     * Si el formulario destino es el de registrar venta, recarga adicionalmente las listas
     * de categorías y métodos de pago para que los selectores no queden vacíos tras el error.
     *
     * @param req     la petición HTTP a la que se le agregará el atributo de error
     * @param resp    la respuesta HTTP utilizada para forwards a la vista del formulario
     * @param mensaje el texto del mensaje de error a mostrar al usuario
     * @param vista   la ruta del JSP al cual se transferirá el control
     * @throws ServletException si ocurre un error al despachar la vista
     * @throws IOException      si ocurre un error de entrada/salida
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
     * Escapa caracteres especiales para incluir texto de forma segura dentro de una cadena JSON.
     *
     * Previene que caracteres como comillas, barras inversas, saltos de línea o tabuladores
     * en los datos de entrada rompan la estructura del JSON generado. Este método debe aplicarse
     * a cualquier valor de texto que se inserte dinámicamente en una respuesta JSON.
     *
     * @param text la cadena de texto a escapar
     * @return la cadena con los caracteres especiales reemplazados por sus secuencias de escape JSON,
     *         o una cadena vacía si el valor de entrada es null
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

    /**
     * Libera las referencias a los DAOs cuando el contenedor destruye el servlet.
     *
     * Este método es llamado automáticamente por el contenedor de servlets durante
     * el proceso de shutdown, permitiendo una liberación ordenada de recursos.
     */
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