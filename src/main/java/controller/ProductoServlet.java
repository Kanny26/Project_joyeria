package controller;

import dao.CategoriaDAO;
import dao.MaterialDAO;
import dao.ProductoDAO;
import dao.ProveedorDAO;
import dao.SubcategoriaDAO;
import model.Administrador;
import model.Producto;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para la gestión completa de productos.
 * Permite crear, leer, actualizar y eliminar productos, así como ajustar stock.
 * Soporta subida de imágenes y asignación de múltiples subcategorías.
 * Requiere autenticación de administrador para todas las operaciones.
 */
@WebServlet("/ProductoServlet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize       = 1024 * 1024 * 5,
    maxRequestSize    = 1024 * 1024 * 10
)
public class ProductoServlet extends HttpServlet {

    private ProductoDAO     productoDAO;
    private CategoriaDAO    categoriaDAO;
    private MaterialDAO     materialDAO;
    private SubcategoriaDAO subcategoriaDAO;
    private ProveedorDAO    proveedorDAO;

    /**
     * Inicializa el servlet instanciando todos los DAO necesarios.
     * Este método es llamado automáticamente por el contenedor de servlets
     * cuando el servlet es cargado por primera vez.
     */
    @Override
    public void init() {
        productoDAO     = new ProductoDAO();
        categoriaDAO    = new CategoriaDAO();
        materialDAO     = new MaterialDAO();
        subcategoriaDAO = new SubcategoriaDAO();
        proveedorDAO    = new ProveedorDAO();
    }

    // ══════════════════════════════════════════════════════════
    // GET
    // ══════════════════════════════════════════════════════════
    
    /**
     * Maneja las peticiones GET para operaciones de visualización.
     * Verifica autenticación y redirige según el parámetro action.
     * Acciones soportadas: nuevo, ver, editar, confirmarEliminar.
     *
     * @param request  petición HTTP con parámetro action
     * @param response respuesta HTTP
     * @throws ServletException si ocurre un error en el procesamiento
     * @throws IOException      si ocurre un error de E/S
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!estaAutenticado(request, response)) return;
        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }
        try {
            switch (action) {
                case "nuevo"             -> mostrarFormularioNuevo(request, response);
                case "ver"               -> verProducto(request, response);
                case "editar"            -> mostrarFormularioEditar(request, response);
                case "confirmarEliminar" -> confirmarEliminar(request, response);
                default -> response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
        }
    }

    // POST
    
    /**
     * Maneja las peticiones POST para operaciones de modificación.
     * Verifica autenticación y procesa según el parámetro action.
     * Acciones soportadas: guardar, actualizar, eliminar, ajustarStock.
     *
     * @param request  petición HTTP con parámetro action y datos del formulario
     * @param response respuesta HTTP
     * @throws ServletException si ocurre un error en el procesamiento
     * @throws IOException      si ocurre un error de E/S
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!estaAutenticado(request, response)) return;
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }
        try {
            switch (action) {
                case "guardar"      -> guardarProducto(request, response);
                case "actualizar"   -> actualizarProducto(request, response);
                case "eliminar"     -> eliminarProductoPost(request, response);
                case "ajustarStock" -> ajustarStock(request, response);
                default -> response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
        }
    }

    // GUARDAR NUEVO PRODUCTO
    
    /**
     * Crea un nuevo producto con los datos enviados desde el formulario.
     * Valida los datos, procesa la imagen y guarda en base de datos.
     * En caso de éxito redirige a la vista de categoría con mensaje de confirmación.
     * En caso de error muestra nuevamente el formulario con el mensaje correspondiente.
     *
     * @param request  petición HTTP con los datos del producto
     * @param response respuesta HTTP
     * @throws Exception si ocurre un error durante el proceso
     */
    private void guardarProducto(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Administrador admin = (Administrador) request.getSession().getAttribute("admin");
        Producto p = construirProductoDesdeRequest(request);

        if (p.getImagenData() == null || p.getImagenData().length == 0) {
            reenviarFormularioNuevo(request, response, p, "Selecciona una imagen para el producto.");
            return;
        }
        String error = validarProducto(p);
        if (error != null) {
            reenviarFormularioNuevo(request, response, p, error);
            return;
        }
        try {
            productoDAO.guardar(p, admin.getId());
            response.sendRedirect(request.getContextPath()
                + "/CategoriaServlet?id=" + p.getCategoriaId() + "&msg=create_ok");
        } catch (Exception e) {
            e.printStackTrace();
            reenviarFormularioNuevo(request, response, p,
                "Error al guardar el producto: " + e.getMessage());
        }
    }

    // ACTUALIZAR PRODUCTO
    
    /**
     * Actualiza un producto existente con los datos enviados desde el formulario.
     * Valida los datos, procesa la nueva imagen si se proporciona, y actualiza en base de datos.
     * En caso de éxito redirige a la vista de categoría con mensaje de confirmación.
     * En caso de error muestra nuevamente el formulario con el mensaje correspondiente.
     *
     * @param request  petición HTTP con los datos del producto
     * @param response respuesta HTTP
     * @throws Exception si ocurre un error durante el proceso
     */
    private void actualizarProducto(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String idStr = request.getParameter("productoId");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }
        Producto p = construirProductoDesdeRequest(request);
        p.setProductoId(Integer.parseInt(idStr));

        String error = validarProducto(p);
        if (error != null) {
            request.setAttribute("error", error);
            cargarAtributosFormulario(request, p);
            request.getRequestDispatcher("/Administrador/editar.jsp").forward(request, response);
            return;
        }
        Administrador admin = (Administrador) request.getSession().getAttribute("admin");
        try {
            productoDAO.actualizar(p, admin.getId());
            response.sendRedirect(request.getContextPath()
                + "/CategoriaServlet?id=" + p.getCategoriaId() + "&msg=update_ok");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error al actualizar: " + e.getMessage());
            cargarAtributosFormulario(request, p);
            request.getRequestDispatcher("/Administrador/editar.jsp").forward(request, response);
        }
    }

    // CONSTRUIR Producto desde request
    // CAMBIO: se leen los IDs de subcategoría como array de parámetros
    // (getParameterValues), porque el formulario envía múltiples checkboxes
    // o un select múltiple con el mismo nombre "subcategoriaIds".
    
    /**
     * Construye un objeto Producto a partir de los parámetros recibidos en la petición.
     * Extrae datos básicos, lista de subcategorías y la imagen si se envió.
     *
     * @param request petición HTTP que contiene los datos del producto
     * @return objeto Producto poblado con los datos de la petición
     * @throws IOException      si ocurre un error al leer la imagen
     * @throws ServletException si ocurre un error al procesar la parte del archivo
     */
    private Producto construirProductoDesdeRequest(HttpServletRequest request)
            throws IOException, ServletException {
        Producto p = new Producto();
        p.setNombre(request.getParameter("nombre"));
        p.setDescripcion(request.getParameter("descripcion"));
        p.setStock(parsearInt(request.getParameter("stock"), 0));
        p.setPrecioUnitario(parsearBigDecimal(request.getParameter("precioUnitario"), BigDecimal.ZERO));
        p.setPrecioVenta(parsearBigDecimal(request.getParameter("precioVenta"), BigDecimal.ZERO));
        p.setCategoriaId(parsearInt(request.getParameter("categoriaId"), 0));
        p.setMaterialId(parsearInt(request.getParameter("materialId"), 0));
        p.setProveedorId(parsearInt(request.getParameter("proveedorId"), 0));

        // CAMBIO: subcategorías como lista de IDs desde checkboxes o select múltiple
        // En el JSP los inputs deben llamarse "subcategoriaIds" (puede venir varios)
        String[] subcatParams = request.getParameterValues("subcategoriaIds");
        List<Integer> subcatIds = new ArrayList<>();
        if (subcatParams != null) {
            for (String s : subcatParams) {
                int id = parsearInt(s, 0);
                if (id > 0) subcatIds.add(id);
            }
        }
        p.setSubcategoriaIds(subcatIds);

        Part filePart = request.getPart("imagen");
        if (filePart != null && filePart.getSize() > 0) {
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            p.setImagen(fileName);
            p.setImagenData(filePart.getInputStream().readAllBytes());
            p.setImagenTipo(filePart.getContentType());
        } else {
            p.setImagen(request.getParameter("imagenActual"));
        }
        return p;
    }

    // VALIDACIÓN SERVER-SIDE
    
    /**
     * Valida los datos de un producto antes de guardar o actualizar.
     * Verifica campos obligatorios, relaciones y reglas de negocio de precios.
     *
     * @param p el producto a validar
     * @return null si el producto es válido, o un mensaje de error en caso contrario
     */
    private String validarProducto(Producto p) {
        if (p.getNombre() == null || p.getNombre().isBlank())
            return "El nombre del producto es obligatorio.";
        if (p.getDescripcion() == null || p.getDescripcion().isBlank())
            return "La descripción es obligatoria.";
        if (p.getMaterialId() <= 0)
            return "Debes seleccionar un material.";
        if (p.getProveedorId() <= 0)
            return "Debes seleccionar un proveedor.";
        if (p.getPrecioUnitario() == null || p.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0)
            return "El precio de costo debe ser mayor a 0.";
        if (p.getPrecioVenta() == null || p.getPrecioVenta().compareTo(p.getPrecioUnitario()) < 0)
            return "El precio de venta no puede ser menor al precio de costo.";
        BigDecimal minimo = p.getPrecioUnitario()
                             .multiply(new BigDecimal("2"))
                             .add(new BigDecimal("5000"));
        if (p.getPrecioVenta().compareTo(minimo) < 0)
            return "El precio de venta debe ser al menos el doble del costo + $5,000 "
                   + "(mínimo esperado: $" + minimo.toPlainString() + ").";
        return null;
    }

    // AJUSTE MANUAL DE STOCK
    
    /**
     * Realiza un ajuste manual de stock para un producto.
     * Actualiza la cantidad disponible y registra el movimiento en auditoría.
     * Responde en formato JSON para consumo vía AJAX.
     *
     * @param request  petición HTTP con productoId, nuevoStock, cantidad, tipo y motivo
     * @param response respuesta HTTP en formato JSON con estado de la operación
     * @throws IOException si ocurre un error al escribir la respuesta
     */
    private void ajustarStock(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            HttpSession session = request.getSession(false);
            Administrador admin = (session != null)
                ? (Administrador) session.getAttribute("admin") : null;
            if (admin == null) {
                out.write("{\"ok\":false,\"error\":\"Sesión expirada.\"}");
                return;
            }
            int    productoId = Integer.parseInt(request.getParameter("productoId"));
            int    nuevoStock = Integer.parseInt(request.getParameter("nuevoStock"));
            int    cantidad   = Integer.parseInt(request.getParameter("cantidad"));
            String tipo       = request.getParameter("tipo");
            String motivo     = request.getParameter("motivo");
            if (nuevoStock < 0) throw new IllegalArgumentException("El stock no puede ser negativo.");
            productoDAO.actualizarStock(productoId, nuevoStock);
            productoDAO.registrarMovimiento(
                productoId, admin.getId(), tipo, cantidad, "Ajuste manual: " + motivo.trim());
            out.write("{\"ok\":true}");
        } catch (Exception e) {
            out.write("{\"ok\":false,\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // ELIMINAR (POST)
    
    /**
     * Elimina un producto de forma lógica (soft delete).
     * Marca el producto como eliminado y registra la acción en auditoría.
     *
     * @param request  petición HTTP con el parámetro id del producto a eliminar
     * @param response respuesta HTTP que redirige a la vista de confirmación
     * @throws Exception si ocurre un error durante la eliminación
     */
    private void eliminarProductoPost(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String idStr = request.getParameter("id");
        if (idStr != null && idStr.matches("\\d+")) {
            int id = Integer.parseInt(idStr);
            Administrador admin = (Administrador) request.getSession().getAttribute("admin");
            productoDAO.eliminar(id, admin.getId());
            request.getRequestDispatcher("/Administrador/eliminado.jsp").forward(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
        }
    }

    // HELPERS DE NAVEGACIÓN
    // CAMBIO: mostrarFormularioNuevo y mostrarFormularioEditar ahora cargan
    // las subcategorías disponibles FILTRADAS por la categoría del producto,
    // usando categoriaDAO.obtenerSubcategoriasDisponibles(categoriaId).
    
    /**
     * Muestra el formulario para crear un nuevo producto.
     * Carga la categoría seleccionada, materiales, proveedores y subcategorías filtradas.
     *
     * @param request  petición HTTP con el parámetro categoria
     * @param response respuesta HTTP
     * @throws Exception si ocurre un error al cargar los datos
     */
    private void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String catIdStr = request.getParameter("categoria");
        if (catIdStr == null || !catIdStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }
        int catId = Integer.parseInt(catIdStr);
        request.setAttribute("categoria",    categoriaDAO.obtenerPorId(catId));
        request.setAttribute("materiales",   materialDAO.listarMateriales());
        request.setAttribute("proveedores",  proveedorDAO.listarProveedores());
        // CAMBIO: solo las subcategorías válidas para esta categoría
        request.setAttribute("subcategorias",
            categoriaDAO.obtenerSubcategoriasDisponibles(catId));
        request.getRequestDispatcher("/Administrador/agregar_producto.jsp")
               .forward(request, response);
    }

    /**
     * Muestra los detalles completos de un producto.
     *
     * @param request  petición HTTP con el parámetro id del producto
     * @param response respuesta HTTP
     * @throws Exception si ocurre un error al cargar el producto
     */
    private void verProducto(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Producto p = obtenerProductoPorParam(request, response);
        if (p != null) {
            request.setAttribute("producto", p);
            request.getRequestDispatcher("/Administrador/ver-producto.jsp")
                   .forward(request, response);
        }
    }

    /**
     * Muestra el formulario para editar un producto existente.
     * Carga los datos actuales del producto junto con materiales, proveedores y subcategorías filtradas.
     *
     * @param request  petición HTTP con el parámetro id del producto
     * @param response respuesta HTTP
     * @throws Exception si ocurre un error al cargar los datos
     */
    private void mostrarFormularioEditar(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Producto p = obtenerProductoPorParam(request, response);
        if (p != null) {
            request.setAttribute("producto",     p);
            request.setAttribute("materiales",   materialDAO.listarMateriales());
            request.setAttribute("proveedores",  proveedorDAO.listarProveedores());
            // CAMBIO: subcategorías filtradas por la categoría actual del producto
            request.setAttribute("subcategorias",
                categoriaDAO.obtenerSubcategoriasDisponibles(p.getCategoriaId()));
            request.getRequestDispatcher("/Administrador/editar.jsp").forward(request, response);
        }
    }

    /**
     * Muestra la vista de confirmación antes de eliminar un producto.
     *
     * @param request  petición HTTP con el parámetro id del producto
     * @param response respuesta HTTP
     * @throws Exception si ocurre un error al cargar el producto
     */
    private void confirmarEliminar(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Producto p = obtenerProductoPorParam(request, response);
        if (p != null) {
            request.setAttribute("producto", p);
            request.getRequestDispatcher("/Administrador/eliminar.jsp").forward(request, response);
        }
    }

    /**
     * Reenvía al formulario de nuevo producto con los datos previamente ingresados
     * y un mensaje de error, para que el usuario pueda corregir.
     *
     * @param request  petición HTTP
     * @param response respuesta HTTP
     * @param p        producto con los datos ingresados
     * @param error    mensaje de error a mostrar
     * @throws Exception si ocurre un error al cargar los datos
     */
    private void reenviarFormularioNuevo(HttpServletRequest request, HttpServletResponse response,
                                          Producto p, String error) throws Exception {
        request.setAttribute("error",        error);
        request.setAttribute("producto",     p);
        request.setAttribute("categoria",    categoriaDAO.obtenerPorId(p.getCategoriaId()));
        request.setAttribute("materiales",   materialDAO.listarMateriales());
        request.setAttribute("proveedores",  proveedorDAO.listarProveedores());
        request.setAttribute("subcategorias",
            categoriaDAO.obtenerSubcategoriasDisponibles(p.getCategoriaId()));
        request.getRequestDispatcher("/Administrador/agregar_producto.jsp")
               .forward(request, response);
    }

    /**
     * Helper: carga atributos comunes para los formularios de edición con error.
     *
     * @param request petición HTTP
     * @param p       producto a editar
     * @throws Exception si ocurre un error al cargar los datos
     */
    private void cargarAtributosFormulario(HttpServletRequest request, Producto p)
            throws Exception {
        request.setAttribute("producto",     p);
        request.setAttribute("materiales",   materialDAO.listarMateriales());
        request.setAttribute("proveedores",  proveedorDAO.listarProveedores());
        request.setAttribute("subcategorias",
            categoriaDAO.obtenerSubcategoriasDisponibles(p.getCategoriaId()));
    }

    // AUTENTICACIÓN
    
    /**
     * Verifica si el usuario tiene una sesión activa como administrador.
     * Si no está autenticado, redirige a la página de inicio de sesión.
     *
     * @param request  petición HTTP
     * @param response respuesta HTTP
     * @return true si el usuario está autenticado como administrador, false en caso contrario
     * @throws IOException si ocurre un error al redirigir
     */
    private boolean estaAutenticado(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
            return false;
        }
        return true;
    }

    /**
     * Obtiene un producto por su ID a partir del parámetro de la petición.
     *
     * @param request  petición HTTP con el parámetro id
     * @param response respuesta HTTP
     * @return el producto encontrado, o null si no existe o el ID es inválido
     * @throws Exception si ocurre un error al consultar la base de datos
     */
    private Producto obtenerProductoPorParam(HttpServletRequest request,
                                              HttpServletResponse response) throws Exception {
        String idStr = request.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return null;
        }
        return productoDAO.obtenerPorId(Integer.parseInt(idStr));
    }

    // PARSERS UTILITARIOS
    
    /**
     * Convierte un String a entero de forma segura.
     *
     * @param valor el String a convertir
     * @param def   valor por defecto si la conversión falla
     * @return el valor convertido o el valor por defecto
     */
    private int parsearInt(String valor, int def) {
        if (valor == null || valor.isBlank()) return def;
        try { return Integer.parseInt(valor.trim()); } catch (Exception e) { return def; }
    }

    /**
     * Convierte un String a BigDecimal de forma segura.
     *
     * @param valor el String a convertir
     * @param def   valor por defecto si la conversión falla
     * @return el valor convertido o el valor por defecto
     */
    private BigDecimal parsearBigDecimal(String valor, BigDecimal def) {
        if (valor == null || valor.isBlank()) return def;
        try { return new BigDecimal(valor.trim()); } catch (Exception e) { return def; }
    }
}