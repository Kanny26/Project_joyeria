package controller;

import dao.ProductoDAO;
import dao.CategoriaDAO;
import dao.MaterialDAO;
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

    private ProductoDAO  productoDAO;
    private CategoriaDAO categoriaDAO;
    private MaterialDAO  materialDAO;

    @Override
    public void init() {
        productoDAO  = new ProductoDAO();
        categoriaDAO = new CategoriaDAO();
        materialDAO  = new MaterialDAO();
    }

    /* ═══════════════════════════════════════════════
       GET
    ═══════════════════════════════════════════════ */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Administrador admin = (Administrador) request.getSession().getAttribute("admin");
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }

        switch (action) {
            case "nuevo":
                mostrarFormularioNuevo(request, response);
                break;
            case "ver":
                verProducto(request, response);
                break;
            case "editar":
                mostrarFormularioEditar(request, response);
                break;
            case "confirmarEliminar":
                confirmarEliminar(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
        }
    }

    /* ═══════════════════════════════════════════════
       POST
    ═══════════════════════════════════════════════ */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Administrador admin = (Administrador) request.getSession().getAttribute("admin");
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }

        switch (action) {
            case "guardar":
                guardarProducto(request, response);
                break;
            case "actualizar":
                actualizarProducto(request, response);
                break;
            case "eliminar":
                eliminarProductoPost(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
        }
    }

    /* ═══════════════════════════════════════════════
       ACCIONES GET
    ═══════════════════════════════════════════════ */

    private void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String catIdStr = request.getParameter("categoria");
        if (catIdStr == null || !catIdStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }

        Categoria categoria = categoriaDAO.obtenerPorId(Integer.parseInt(catIdStr));
        if (categoria == null) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }

        request.setAttribute("categoria", categoria);
        request.setAttribute("materiales", materialDAO.listarMateriales());
        request.getRequestDispatcher("/Administrador/agregar_producto.jsp")
               .forward(request, response);
    }

    private void verProducto(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }

        Producto producto = productoDAO.obtenerPorId(Integer.parseInt(idStr));
        if (producto == null) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }

        request.setAttribute("producto", producto);
        request.getRequestDispatcher("/Administrador/ver-producto.jsp")
               .forward(request, response);
    }

    private void mostrarFormularioEditar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }

        Producto producto = productoDAO.obtenerPorId(Integer.parseInt(idStr));
        if (producto == null) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }

        request.setAttribute("producto", producto);
        request.setAttribute("materiales", materialDAO.listarMateriales());
        request.getRequestDispatcher("/Administrador/editar.jsp")
               .forward(request, response);
    }

    private void confirmarEliminar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }

        Producto producto = productoDAO.obtenerPorId(Integer.parseInt(idStr));
        if (producto == null) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }

        request.setAttribute("producto", producto);
        request.getRequestDispatcher("/Administrador/eliminar.jsp")
               .forward(request, response);
    }

    /* ═══════════════════════════════════════════════
       ACCIONES POST
    ═══════════════════════════════════════════════ */

    private void guardarProducto(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        Administrador admin = (Administrador) request.getSession().getAttribute("admin");
        Producto p = construirProductoDesdeRequest(request);

        String error = validarProducto(p);
        if (error != null) {
            Categoria categoria = categoriaDAO.obtenerPorId(p.getCategoria().getCategoriaId());
            request.setAttribute("error", error);
            request.setAttribute("producto", p);
            request.setAttribute("materiales", materialDAO.listarMateriales());
            request.setAttribute("categoria", categoria);
            request.getRequestDispatcher("/Administrador/agregar_producto.jsp")
                   .forward(request, response);
            return;
        }

        productoDAO.guardar(p, admin.getId());
        response.sendRedirect(request.getContextPath()
                + "/CategoriaServlet?id=" + p.getCategoria().getCategoriaId());
    }

    private void actualizarProducto(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

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
            request.setAttribute("producto", p);
            request.setAttribute("materiales", materialDAO.listarMateriales());
            request.getRequestDispatcher("/Administrador/editar.jsp")
                   .forward(request, response);
            return;
        }

        productoDAO.actualizar(p);
        response.sendRedirect(request.getContextPath()
                + "/CategoriaServlet?id=" + p.getCategoria().getCategoriaId());
    }

    private void eliminarProductoPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }

        Producto producto = productoDAO.obtenerPorId(Integer.parseInt(idStr));
        if (producto != null) {
            productoDAO.eliminar(producto.getProductoId());
            request.setAttribute("producto", producto);
            request.getRequestDispatcher("/Administrador/eliminado.jsp")
                   .forward(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
        }
    }

    /* ═══════════════════════════════════════════════
       CONSTRUCCIÓN DEL PRODUCTO
    ═══════════════════════════════════════════════ */

    private Producto construirProductoDesdeRequest(HttpServletRequest request)
            throws IOException, ServletException {

        Producto p = new Producto();
        p.setNombre(request.getParameter("nombre"));
        p.setDescripcion(request.getParameter("descripcion"));

        String stockStr       = request.getParameter("stock");
        String precioUnitStr  = request.getParameter("precioUnitario");
        String precioVentaStr = request.getParameter("precioVenta");

        p.setStock(stockStr != null && !stockStr.isEmpty()
                ? Integer.parseInt(stockStr) : 0);
        p.setPrecioUnitario(precioUnitStr != null && !precioUnitStr.isEmpty()
                ? new BigDecimal(precioUnitStr) : BigDecimal.ZERO);
        p.setPrecioVenta(precioVentaStr != null && !precioVentaStr.isEmpty()
                ? new BigDecimal(precioVentaStr) : BigDecimal.ZERO);

        Categoria c = new Categoria();
        String catIdStr = request.getParameter("categoriaId");
        c.setCategoriaId(catIdStr != null && !catIdStr.isEmpty()
                ? Integer.parseInt(catIdStr) : 0);
        p.setCategoria(c);

        Material m = new Material();
        String matIdStr = request.getParameter("materialId");
        m.setMaterialId(matIdStr != null && !matIdStr.isEmpty()
                ? Integer.parseInt(matIdStr) : 0);
        p.setMaterial(m);

        /* ── Imagen: se guarda como BLOB en la BD ──────────────────────
           - Los bytes van a imagen_data  → cualquier PC la ve
           - El nombre va  a imagen       → para mostrar en formularios
           - El tipo MIME va a imagen_tipo → para servirla correctamente
        ────────────────────────────────────────────────────────────── */
        Part filePart = request.getPart("imagen");

        if (filePart != null && filePart.getSize() > 0) {

            String fileName = Paths.get(filePart.getSubmittedFileName())
                                   .getFileName()
                                   .toString();

            byte[] bytes = filePart.getInputStream().readAllBytes();

            p.setImagen(fileName);
            p.setImagenData(bytes);
            p.setImagenTipo(filePart.getContentType());

        } else {
            // Sin imagen nueva → conservar nombre actual (bytes ya están en BD)
            p.setImagen(request.getParameter("imagenActual"));
            p.setImagenData(null);  // null = no actualizar bytes en BD
            p.setImagenTipo(null);
        }

        return p;
    }

    /* ═══════════════════════════════════════════════
       VALIDACIÓN
    ═══════════════════════════════════════════════ */

    private String validarProducto(Producto p) {

        if (p.getNombre() == null || p.getNombre().trim().isEmpty())
            return "El nombre del producto es obligatorio.";

        if (p.getDescripcion() == null || p.getDescripcion().trim().isEmpty())
            return "La descripción es obligatoria.";

        if (p.getStock() < 0)
            return "El stock no puede ser negativo.";

        if (p.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0)
            return "El precio unitario debe ser mayor a 0.";

        if (p.getPrecioVenta().compareTo(p.getPrecioUnitario()) < 0)
            return "El precio de venta no puede ser menor al precio unitario.";

        if (p.getCategoria() == null || p.getCategoria().getCategoriaId() <= 0)
            return "Debe seleccionar una categoría válida.";

        if (p.getMaterial() == null || p.getMaterial().getMaterialId() <= 0)
            return "Debe seleccionar un material válido.";

        return null;
    }
}