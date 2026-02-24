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
    fileSizeThreshold = 1024 * 1024,      // 1 MB
    maxFileSize       = 1024 * 1024 * 5,  // 5 MB
    maxRequestSize    = 1024 * 1024 * 10  // 10 MB
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
                default                -> response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            }
        } catch (Exception e) {
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    /* ═══════════════════════════════════════════════
       POST
    ═══════════════════════════════════════════════ */
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
                case "guardar"    -> guardarProducto(request, response);
                case "actualizar" -> actualizarProducto(request, response);
                case "eliminar"   -> eliminarProductoPost(request, response);
                default           -> response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            }
        } catch (Exception e) {
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    /* ═══════════════════════════════════════════════
       AUTENTICACIÓN
    ═══════════════════════════════════════════════ */
    private boolean estaAutenticado(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
            return false;
        }
        return true;
    }

    /* ═══════════════════════════════════════════════
       ACCIONES GET
    ═══════════════════════════════════════════════ */
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
            request.getRequestDispatcher("/Administrador/agregar_producto.jsp")
                   .forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Error al cargar formulario: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    private void verProducto(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Producto producto = obtenerProductoPorParam(request, response);
            if (producto == null) return;
            request.setAttribute("producto", producto);
            request.getRequestDispatcher("/Administrador/ver-producto.jsp")
                   .forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    private void mostrarFormularioEditar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Producto producto = obtenerProductoPorParam(request, response);
            if (producto == null) return;
            request.setAttribute("producto", producto);
            request.setAttribute("materiales", materialDAO.listarMateriales());
            request.getRequestDispatcher("/Administrador/editar.jsp")
                   .forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    private void confirmarEliminar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Producto producto = obtenerProductoPorParam(request, response);
            if (producto == null) return;
            request.setAttribute("producto", producto);
            request.getRequestDispatcher("/Administrador/eliminar.jsp")
                   .forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    private Producto obtenerProductoPorParam(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return null;
        }
        try {
            int id = Integer.parseInt(idStr);
            Producto p = productoDAO.obtenerPorId(id);
            if (p == null) {
                response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            }
            return p;
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return null;
        }
    }

    /* ═══════════════════════════════════════════════
       ACCIONES POST
    ═══════════════════════════════════════════════ */
    private void guardarProducto(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        HttpSession session = request.getSession();
        Administrador admin = (Administrador) session.getAttribute("admin");
        
        Producto p = construirProductoDesdeRequest(request);

        // Validar imagen obligatoria al crear
        if (p.getImagenData() == null || p.getImagenData().length == 0) {
            reenviarFormularioNuevo(request, response, p, "Selecciona una imagen para el producto.");
            return;
        }

        String error = validarProducto(p);
        if (error != null) {
            reenviarFormularioNuevo(request, response, p, error);
            return;
        }

        // Usar getUsuarioId() del modelo Usuario
        productoDAO.guardar(p, admin.getId());
        response.sendRedirect(request.getContextPath()
                + "/CategoriaServlet?id=" + p.getCategoriaId());
    }

    private void reenviarFormularioNuevo(HttpServletRequest request, HttpServletResponse response,
                                          Producto p, String error)
            throws ServletException, IOException {
        try {
            Categoria categoria = categoriaDAO.obtenerPorId(p.getCategoriaId());
            request.setAttribute("error", error);
            request.setAttribute("producto", p);
            request.setAttribute("materiales", materialDAO.listarMateriales());
            request.setAttribute("categoria", categoria);
            request.getRequestDispatcher("/Administrador/agregar_producto.jsp")
                   .forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

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
                    + "/CategoriaServlet?id=" + p.getCategoriaId());
        } catch (Exception e) {
            request.setAttribute("error", "Error al actualizar: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

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
                productoDAO.eliminar(id);
                request.setAttribute("producto", producto);
                request.getRequestDispatcher("/Administrador/eliminado.jsp")
                       .forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            }
        } catch (Exception e) {
            request.setAttribute("error", "Error al eliminar: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    /* ═══════════════════════════════════════════════
       CONSTRUCCIÓN DEL PRODUCTO DESDE REQUEST
    ═══════════════════════════════════════════════ */
    private Producto construirProductoDesdeRequest(HttpServletRequest request)
            throws IOException, ServletException {

        Producto p = new Producto();
        p.setNombre(request.getParameter("nombre"));
        p.setDescripcion(request.getParameter("descripcion"));

        // Parseo seguro de números
        p.setStock(parsearInt(request.getParameter("stock"), 0));
        p.setPrecioUnitario(parsearBigDecimal(request.getParameter("precioUnitario"), BigDecimal.ZERO));
        p.setPrecioVenta(parsearBigDecimal(request.getParameter("precioVenta"), BigDecimal.ZERO));

        // Categoría y Material (usando IDs directamente)
        p.setCategoriaId(parsearInt(request.getParameter("categoriaId"), 0));
        p.setMaterialId(parsearInt(request.getParameter("materialId"), 0));

        // Imagen
        Part filePart = request.getPart("imagen");
        if (filePart != null && filePart.getSize() > 0) {
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            byte[] bytes = filePart.getInputStream().readAllBytes();
            p.setImagen(fileName);
            p.setImagenData(bytes);
            p.setImagenTipo(filePart.getContentType());
        } else {
            // Al actualizar: conservar imagen anterior si no se sube nueva
            p.setImagen(request.getParameter("imagenActual"));
            p.setImagenData(null);   // null = no actualizar bytes
            p.setImagenTipo(null);
        }

        return p;
    }

    /* ═══════════════════════════════════════════════
       HELPERS DE PARSEO SEGURO
    ═══════════════════════════════════════════════ */
    private int parsearInt(String valor, int defaultValue) {
        if (valor == null || valor.trim().isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private BigDecimal parsearBigDecimal(String valor, BigDecimal defaultValue) {
        if (valor == null || valor.trim().isEmpty()) return defaultValue;
        try {
            return new BigDecimal(valor.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /* ═══════════════════════════════════════════════
       VALIDACIÓN DE PRODUCTO
    ═══════════════════════════════════════════════ */
    private String validarProducto(Producto p) {
        if (p.getNombre() == null || p.getNombre().trim().isEmpty())
            return "El nombre del producto es obligatorio.";
        if (p.getDescripcion() == null || p.getDescripcion().trim().isEmpty())
            return "La descripción es obligatoria.";
        if (p.getStock() < 0)
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
}