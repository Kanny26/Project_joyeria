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

/**
 * Servlet encargado de gestionar el registro de compras a proveedores en el sistema AAC27.
 *
 * Proporciona funcionalidades para:
 * - Crear nuevas compras con múltiples productos y detalles
 * - Visualizar el detalle de una compra específica
 * - Obtener listas de categorías y productos en formato JSON para carga dinámica en formularios
 *
 * Las operaciones se realizan mediante peticiones GET y POST, con validación de sesión de administrador
 * y respuestas adaptadas según el tipo de solicitud (HTML o JSON).
 *
 * @see CompraDAO
 * @see ProductoDAO
 * @see CategoriaDAO
 * @see MetodoPagoDAO
 */
@WebServlet("/CompraServlet")
@MultipartConfig
public class CompraServlet extends HttpServlet {

    private CompraDAO     compraDAO;
    private ProductoDAO   productoDAO;
    private CategoriaDAO  categoriaDAO;
    private MetodoPagoDAO metodoPagoDAO;

    /**
     * Inicializa el servlet e instancia los DAO de compra, producto, categoría y método de pago.
     *
     * @throws ServletException si el contenedor no puede completar la inicialización
     */
    @Override
    public void init() throws ServletException {
        compraDAO     = new CompraDAO();
        productoDAO   = new ProductoDAO();
        categoriaDAO  = new CategoriaDAO();
        metodoPagoDAO = new MetodoPagoDAO();
    }

    // ==================== GET ====================

    /**
     * Procesa las solicitudes HTTP GET para gestionar compras.
     *
     * Según el parámetro {@code action}, ejecuta una de las siguientes operaciones:
     * - "nueva": muestra el formulario para registrar una nueva compra
     * - "detalle": muestra el detalle completo de una compra existente
     * - "obtenerCategorias": devuelve la lista de categorías en formato JSON
     * - "obtenerProductosPorCategoria": devuelve productos filtrados por categoría y proveedor en JSON
     *
     * Si no se especifica una acción válida o ocurre un error, redirige al listado de proveedores.
     *
     * @param request  petición HTTP (sesión de administrador requerida)
     * @param response respuesta HTTP (forward, redirect o JSON)
     * @throws ServletException si falla el despacho a la vista
     * @throws IOException      si ocurre un error de E/S
     */
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

    // ==================== POST ====================

    /**
     * Procesa las solicitudes HTTP POST para persistir nuevas compras.
     *
     * Actualmente maneja la acción {@code guardarCompra}, que recibe los datos del formulario
     * multipart, valida la información, construye el objeto Compra con sus detalles y lo persiste
     * mediante una transacción. La respuesta se devuelve en formato JSON para compatibilidad con
     * el flujo AJAX del formulario.
     *
     * En caso de error, se responde con un objeto JSON que contiene el mensaje de error escapado
     * para evitar romper la estructura del JSON.
     *
     * @param request  petición HTTP con {@code UTF-8} y datos del formulario multipart
     * @param response respuesta HTTP JSON ({@code ok}, error escapado)
     * @throws ServletException si falla el despacho en manejo de error genérico
     * @throws IOException      si ocurre un error de E/S
     */
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
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"ok\":false,\"error\":\"" + esc(e.getMessage()) + "\"}");
        }
    }

    /**
     * Prepara y muestra el formulario para registrar una nueva compra.
     *
     * Carga la lista de métodos de pago disponibles y valida que el ID del proveedor
     * sea numérico antes de continuar. Acepta el ID del proveedor mediante los parámetros
     * {@code proveedorId} o {@code id} para mayor flexibilidad en la construcción de enlaces.
     *
     * @param request  la petición HTTP que contiene el ID del proveedor
     * @param response la respuesta HTTP utilizada para redirigir o forwards al JSP del formulario
     * @throws ServletException si ocurre un error al despachar la vista
     * @throws IOException      si ocurre un error de entrada/salida
     */
    private void mostrarFormularioNueva(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * El botón "Nueva compra" en la vista del proveedor puede enviar el ID
         * como "id" o como "proveedorId" según cómo esté construido el enlace.
         * Se intenta con ambos nombres para no depender de uno solo.
         */
        String proveedorIdStr = request.getParameter("proveedorId");
        if (proveedorIdStr == null || proveedorIdStr.isBlank()) {
            proveedorIdStr = request.getParameter("id");
        }

        if (proveedorIdStr == null || !proveedorIdStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }

        try {
            List<MetodoPago> metodos = metodoPagoDAO.listarTodos();
            if (metodos == null) metodos = new ArrayList<>();
            request.setAttribute("metodosPago", metodos);
        } catch (Exception e) {
            System.err.println("Error al cargar métodos de pago: " + e.getMessage());
        }

        request.setAttribute("proveedorId", proveedorIdStr);
        request.getRequestDispatcher("/Administrador/proveedores/agregar_compra.jsp")
               .forward(request, response);
    }

    /**
     * Procesa y guarda una nueva compra con todos sus detalles en una transacción.
     *
     * Valida exhaustivamente los datos recibidos:
     * - ID de proveedor presente y numérico
     * - Fechas en formato yyyy-MM-dd y coherencia temporal (entrega >= compra)
     * - Método de pago seleccionado
     * - Para compras a crédito: fecha de vencimiento obligatoria y anticipo <= total
     * - Al menos un producto válido con precio y cantidad mayores a cero
     *
     * Registra el ID del administrador en sesión para auditoría en la tabla Inventario_Movimiento.
     * Responde en formato JSON: {"ok":true, "proveedorId":X} si la operación fue exitosa.
     *
     * @param request  la petición HTTP que contiene los datos del formulario de compra
     * @param response la respuesta HTTP utilizada para devolver el resultado en formato JSON
     * @throws Exception si ocurre un error de validación, persistencia o formato de datos
     */
    private void guardarCompra(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        response.setContentType("application/json;charset=UTF-8");

        // Obtener el ID del administrador desde la sesión para registrarlo en el inventario
        Administrador admin = (Administrador) request.getSession(false).getAttribute("admin");
        Integer usuarioId = (admin != null) ? admin.getId() : null;

        String proveedorIdStr = request.getParameter("proveedorId");
        if (proveedorIdStr == null || !proveedorIdStr.matches("\\d+"))
            throw new IllegalArgumentException("ID de proveedor inválido.");
        int proveedorId = Integer.parseInt(proveedorIdStr);

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

        String fcStr = request.getParameter("fechaCompra");
        String feStr = request.getParameter("fechaEntrega");
        if (fcStr == null || fcStr.isBlank())
            throw new IllegalArgumentException("La fecha de compra es obligatoria.");
        if (feStr == null || feStr.isBlank())
            throw new IllegalArgumentException("La fecha de entrega es obligatoria.");

        Date fechaCompra  = fmt.parse(fcStr.trim());
        Date fechaEntrega = fmt.parse(feStr.trim());

        // before() compara si una fecha es estrictamente anterior a otra
        if (fechaEntrega.before(fechaCompra))
            throw new IllegalArgumentException(
                    "La fecha de entrega no puede ser anterior a la fecha de compra.");

        String mpStr = request.getParameter("metodoPagoId");
        if (mpStr == null || mpStr.isBlank() || mpStr.equals("0"))
            throw new IllegalArgumentException("Debes seleccionar un método de pago.");
        int metodoPagoId = Integer.parseInt(mpStr);

        String  tipoPago  = request.getParameter("tipoPago");
        boolean esCredito = "CREDITO".equals(tipoPago);

        Date       fechaVencimiento = null;
        BigDecimal anticipo         = BigDecimal.ZERO;
        String     estadoCredito    = "pagado";

        // Los campos de crédito solo se validan si el tipo de pago es CREDITO
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

        // getParameterValues devuelve arrays paralelos: productoId[0] con precio[0] y cantidad[0]
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
        // CORRECCIÓN: se pasa el ID del admin para registrarlo en Inventario_Movimiento
        compra.setUsuarioId(usuarioId);

        boolean exito = compraDAO.insertarConTransaccion(compra);
        if (exito) {
            response.getWriter().write("{\"ok\":true,\"proveedorId\":" + proveedorId + "}");
        } else {
            throw new Exception("No se pudo guardar la compra. Inténtalo de nuevo.");
        }
    }

    /**
     * Carga y muestra el detalle completo de una compra específica.
     *
     * Valida que el ID de la compra sea numérico y que el registro exista en la base de datos.
     * Si la validación falla, redirige al listado de proveedores para evitar errores en la vista.
     *
     * @param request  la petición HTTP que contiene el ID de la compra y del proveedor
     * @param response la respuesta HTTP utilizada para forwards a la vista de detalle
     * @throws Exception si ocurre un error al consultar la base de datos
     */
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

    /**
     * Devuelve la lista de categorías disponibles en formato JSON.
     *
     * Este endpoint es consumido por JavaScript para poblar dinámicamente el selector
     * de categorías en el formulario de nueva compra. En caso de error, devuelve un
     * array vacío para no interrumpir el flujo del cliente.
     *
     * @param request  la petición HTTP (no utiliza parámetros)
     * @param response la respuesta HTTP configurada con contenido JSON y codificación UTF-8
     * @throws IOException si ocurre un error al escribir la respuesta JSON
     */
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

    /**
     * Devuelve los productos de una categoría filtrados por proveedor en formato JSON.
     *
     * Si se proporciona un {@code proveedorId} válido, solo se incluyen productos
     * asociados a ese proveedor. Si el ID es 0 o no se especifica, se retornan todos
     * los productos de la categoría sin filtrar.
     *
     * @param request  la petición HTTP que contiene {@code categoriaId} y opcionalmente {@code proveedorId}
     * @param response la respuesta HTTP configurada con contenido JSON y codificación UTF-8
     * @throws IOException si ocurre un error al escribir la respuesta JSON
     */
    private void obtenerProductosPorCategoriaJSON(HttpServletRequest request,
                                                   HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        String categoriaIdStr = request.getParameter("categoriaId");
        String proveedorIdStr = request.getParameter("proveedorId");

        if (categoriaIdStr == null || !categoriaIdStr.matches("\\d+")) {
            response.getWriter().write("[]");
            return;
        }

        int proveedorId = 0;
        if (proveedorIdStr != null && proveedorIdStr.matches("\\d+"))
            proveedorId = Integer.parseInt(proveedorIdStr);

        try {
            List<Producto> todos = productoDAO.listarPorCategoria(
                    Integer.parseInt(categoriaIdStr));

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

    /**
     * Verifica que exista una sesión activa con un administrador autenticado.
     *
     * Si la petición espera una respuesta JSON (cabecera Accept: application/json),
     * responde con un error HTTP 401 en formato JSON. De lo contrario, redirige
     * al usuario a la página de inicio de sesión.
     *
     * @param req la petición HTTP para acceder a la sesión
     * @param res la respuesta HTTP utilizada para redirigir o devolver error JSON
     * @return true si la sesión es válida y contiene un administrador, false en caso contrario
     * @throws IOException si ocurre un error al escribir la respuesta o realizar la redirección
     */
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

    /**
     * Escapa caracteres especiales para incluir texto de forma segura dentro de una cadena JSON.
     *
     * Previene que caracteres como comillas, saltos de línea o tabuladores en los datos
     * de entrada rompan la estructura del JSON generado. Este método debe aplicarse a
     * cualquier valor de texto que se inserte dinámicamente en una respuesta JSON.
     *
     * @param t la cadena de texto a escapar
     * @return la cadena con los caracteres especiales reemplazados por sus secuencias de escape JSON,
     *         o una cadena vacía si el valor de entrada es null
     */
    private String esc(String t) {
        if (t == null) return "";
        return t.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}