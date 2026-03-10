package controller;

import dao.ProductoDAO;
import dao.CategoriaDAO;
import dao.MaterialDAO;
import dao.SubcategoriaDAO;
import model.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.List;

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

    @Override
    public void init() {
        productoDAO     = new ProductoDAO();
        categoriaDAO    = new CategoriaDAO();
        materialDAO     = new MaterialDAO();
        subcategoriaDAO = new SubcategoriaDAO();
    }

    // ════════════════════════════════════════════════════════════════════
    // GET
    // ════════════════════════════════════════════════════════════════════
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!estaAutenticado(request, response)) return;

        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }

        try {
            switch (action) {
                case "nuevo"           -> mostrarFormularioNuevo(request, response);
                case "ver"             -> verProducto(request, response);
                case "editar"          -> mostrarFormularioEditar(request, response);
                case "confirmarEliminar" -> confirmarEliminar(request, response);
                default -> response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            }
        } catch (Exception e) {
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/Administrador/mensajesexito.html").forward(request, response);
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
        if (action == null || action.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }

        try {
            switch (action) {
                case "guardar"       -> guardarProducto(request, response);
                case "actualizar"    -> actualizarProducto(request, response);
                case "eliminar"      -> eliminarProductoPost(request, response);
                case "ajustarStock"  -> ajustarStock(request, response);   // ■■ NUEVO ■■
                default -> response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            }
        } catch (Exception e) {
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/Administrador/mensajesexito.html").forward(request, response);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // Guardar producto nuevo
    // ■■ Stock siempre se guarda en 0 — no se lee del formulario ■■
    // ════════════════════════════════════════════════════════════════════
    private void guardarProducto(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        HttpSession session = request.getSession();
        Administrador admin = (Administrador) session.getAttribute("admin");

        Producto p = construirProductoDesdeRequest(request);

        if (p.getImagenData() == null || p.getImagenData().length == 0) {
            reenviarFormularioNuevo(request, response, p, "Selecciona una imagen para el producto.");
            return;
        }

        String error = validarProducto(p, false);   // false = es creación (no valida stock)
        if (error != null) {
            reenviarFormularioNuevo(request, response, p, error);
            return;
        }

        productoDAO.guardar(p, admin.getId(), admin.getId());
        response.sendRedirect(request.getContextPath() + "/CategoriaServlet?id=" + p.getCategoriaId());
    }

    // ════════════════════════════════════════════════════════════════════
    // Actualizar producto
    // ■■ El stock NO se modifica aquí — solo se cambia por ajustarStock ■■
    // ════════════════════════════════════════════════════════════════════
    private void actualizarProducto(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String idStr = request.getParameter("productoId");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }

        try {
            Producto p = construirProductoDesdeRequest(request);
            p.setProductoId(Integer.parseInt(idStr));

            String error = validarProducto(p, true);   // true = es edición
            if (error != null) {
                request.setAttribute("error", error);
                request.setAttribute("producto", p);
                request.setAttribute("materiales", materialDAO.listarMateriales());
                request.setAttribute("subcategorias", subcategoriaDAO.listarTodas());
                request.getRequestDispatcher("/Administrador/editar.jsp").forward(request, response);
                return;
            }

            HttpSession session = request.getSession();
            Administrador admin = (Administrador) session.getAttribute("admin");
            productoDAO.actualizar(p, admin.getId());
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet?id=" + p.getCategoriaId());

        } catch (Exception e) {
            request.setAttribute("error", "Error al actualizar: " + e.getMessage());
            request.getRequestDispatcher("/Administrador/mensajesexito.html").forward(request, response);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // ■■ AJUSTE MANUAL DE STOCK — responde JSON ■■
    // ════════════════════════════════════════════════════════════════════
    private void ajustarStock(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // 1. Validar Sesión
            HttpSession session = request.getSession(false);
            Administrador admin = (session != null) ? (Administrador) session.getAttribute("admin") : null;
            if (admin == null) {
                out.write("{\"ok\":false,\"error\":\"Sesión expirada. Inicie sesión nuevamente.\"}");
                return;
            }

            // 2. Capturar parámetros
            int productoId = Integer.parseInt(request.getParameter("productoId"));
            int nuevoStock = Integer.parseInt(request.getParameter("nuevoStock"));
            int cantidad   = Integer.parseInt(request.getParameter("cantidad"));
            String tipo    = request.getParameter("tipo"); // "entrada" o "salida"
            String motivo  = request.getParameter("motivo");

            // 3. Validación de Lógica de Negocio (El "Techo" de stock)
            if (tipo.equals("entrada")) {
                int maxComprado = productoDAO.obtenerTotalEntradasPorCompras(productoId);
                if (nuevoStock > maxComprado) {
                    out.write("{\"ok\":false,\"error\":\"Inconsistencia: El stock propuesto (" + nuevoStock + 
                              ") excede el total de unidades compradas a proveedores (" + maxComprado + ").\"}");
                    return;
                }
            }

            if (nuevoStock < 0) throw new IllegalArgumentException("El stock no puede ser negativo.");

            // 4. Ejecutar cambios
            productoDAO.actualizarStock(productoId, nuevoStock);
            productoDAO.registrarMovimiento(productoId, admin.getId(), tipo, cantidad, "Ajuste manual: " + motivo.trim());

            out.write("{\"ok\":true}");

        } catch (Exception e) {
            out.write("{\"ok\":false,\"error\":\"Error en el servidor: " + e.getMessage() + "\"}");
        }
    }
    // ════════════════════════════════════════════════════════════════════
    // Eliminar producto (lógico)
    // ════════════════════════════════════════════════════════════════════
    private void eliminarProductoPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Producto producto = productoDAO.obtenerPorId(id);
            if (producto != null) {
                HttpSession session = request.getSession();
                Administrador admin = (Administrador) session.getAttribute("admin");
                productoDAO.eliminar(id, admin.getId());
                request.setAttribute("producto", producto);
                request.getRequestDispatcher("/Administrador/eliminado.jsp").forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            }
        } catch (Exception e) {
            request.setAttribute("error", "Error al eliminar: " + e.getMessage());
            request.getRequestDispatcher("/Administrador/mensajesexito.html").forward(request, response);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // Helpers de formulario
    // ════════════════════════════════════════════════════════════════════
    private Producto construirProductoDesdeRequest(HttpServletRequest request)
            throws IOException, ServletException {
        Producto p = new Producto();
        p.setNombre(request.getParameter("nombre"));
        p.setDescripcion(request.getParameter("descripcion"));
        // ■■ Stock no se lee aquí para creación (se fuerza 0 en el DAO)
        // Para edición el campo viene como hidden con el valor actual (readonly en UI)
        p.setStock(parsearInt(request.getParameter("stock"), 0));
        p.setPrecioUnitario(parsearBigDecimal(request.getParameter("precioUnitario"), BigDecimal.ZERO));
        p.setPrecioVenta(parsearBigDecimal(request.getParameter("precioVenta"), BigDecimal.ZERO));
        p.setCategoriaId(parsearInt(request.getParameter("categoriaId"), 0));
        p.setMaterialId(parsearInt(request.getParameter("materialId"), 0));
        p.setSubcategoriaId(parsearInt(request.getParameter("subcategoriaId"), 0));

        Part filePart = request.getPart("imagen");
        if (filePart != null && filePart.getSize() > 0) {
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            p.setImagen(fileName);
            p.setImagenData(filePart.getInputStream().readAllBytes());
            p.setImagenTipo(filePart.getContentType());
        } else {
            p.setImagen(request.getParameter("imagenActual"));
            p.setImagenData(null);
            p.setImagenTipo(null);
        }
        return p;
    }

    private int parsearInt(String valor, int def) {
        if (valor == null || valor.trim().isEmpty()) return def;
        try { return Integer.parseInt(valor.trim()); }
        catch (NumberFormatException e) { return def; }
    }

    private BigDecimal parsearBigDecimal(String valor, BigDecimal def) {
        if (valor == null || valor.trim().isEmpty()) return def;
        try { return new BigDecimal(valor.trim()); }
        catch (NumberFormatException e) { return def; }
    }

    // ■■ esEdicion=true omite validación de stock (campo readonly en editar) ■■
    private String validarProducto(Producto p, boolean esEdicion) {
        if (p.getNombre() == null || p.getNombre().trim().isEmpty())
            return "El nombre del producto es obligatorio.";
        if (p.getDescripcion() == null || p.getDescripcion().trim().isEmpty())
            return "La descripción es obligatoria.";
        if (!esEdicion && p.getStock() < 0)
            return "El stock no puede ser negativo.";
        if (p.getPrecioUnitario() == null || p.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0)
            return "El precio de costo debe ser mayor a 0.";
        if (p.getPrecioVenta() == null || p.getPrecioVenta().compareTo(BigDecimal.ZERO) <= 0)
            return "El precio de venta debe ser mayor a 0.";
        if (p.getPrecioVenta().compareTo(p.getPrecioUnitario()) < 0)
            return "El precio de venta no puede ser menor al precio de costo.";
        if (p.getCategoriaId() <= 0)
            return "Debe seleccionar una categoría válida.";
        if (p.getMaterialId() <= 0)
            return "Debe seleccionar un material válido.";
        return null;
    }

    private void reenviarFormularioNuevo(HttpServletRequest request, HttpServletResponse response,
                                          Producto p, String error) throws ServletException, IOException {
        try {
            Categoria categoria = categoriaDAO.obtenerPorId(p.getCategoriaId());
            request.setAttribute("error", error);
            request.setAttribute("producto", p);
            request.setAttribute("materiales", materialDAO.listarMateriales());
            request.setAttribute("subcategorias", subcategoriaDAO.listarTodas());
            request.setAttribute("categoria", categoria);
            request.getRequestDispatcher("/Administrador/agregar_producto.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/Administrador/mensajesexito.html").forward(request, response);
        }
    }

    private void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String catIdStr = request.getParameter("categoria");
            if (catIdStr == null || !catIdStr.matches("\\d+")) {
                response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
                return;
            }
            int catId = Integer.parseInt(catIdStr);
            Categoria categoria = categoriaDAO.obtenerPorId(catId);
            if (categoria == null) {
                response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
                return;
            }
            request.setAttribute("categoria", categoria);
            request.setAttribute("materiales", materialDAO.listarMateriales());
            request.setAttribute("subcategorias", subcategoriaDAO.listarTodas());
            request.getRequestDispatcher("/Administrador/agregar_producto.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Error al cargar formulario: " + e.getMessage());
            request.getRequestDispatcher("/Administrador/mensajesexito.html").forward(request, response);
        }
    }

    private void verProducto(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Producto producto = obtenerProductoPorParam(request, response);
            if (producto == null) return;
            request.setAttribute("producto", producto);
            request.getRequestDispatcher("/Administrador/ver-producto.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/Administrador/mensajesexito.html").forward(request, response);
        }
    }

    private void mostrarFormularioEditar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Producto producto = obtenerProductoPorParam(request, response);
            if (producto == null) return;
            request.setAttribute("producto", producto);
            request.setAttribute("materiales", materialDAO.listarMateriales());
            request.setAttribute("subcategorias", subcategoriaDAO.listarTodas());
            request.getRequestDispatcher("/Administrador/editar.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/Administrador/mensajesexito.html").forward(request, response);
        }
    }

    private void confirmarEliminar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Producto producto = obtenerProductoPorParam(request, response);
            if (producto == null) return;
            request.setAttribute("producto", producto);
            request.getRequestDispatcher("/Administrador/eliminar.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/Administrador/mensajesexito.html").forward(request, response);
        }
    }

    private boolean estaAutenticado(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
            return false;
        }
        return true;
    }

    private Producto obtenerProductoPorParam(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return null;
        }
        try {
            Producto p = productoDAO.obtenerPorId(Integer.parseInt(idStr));
            if (p == null) response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return p;
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return null;
        }
    }
}
