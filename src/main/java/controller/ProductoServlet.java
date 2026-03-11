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

    // ══════════════════════════════════════════════════════════
    // POST
    // ══════════════════════════════════════════════════════════
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

    // ══════════════════════════════════════════════════════════
    // GUARDAR NUEVO PRODUCTO
    // ══════════════════════════════════════════════════════════
    private void guardarProducto(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Administrador admin = (Administrador) request.getSession().getAttribute("admin");
        Producto p = construirProductoDesdeRequest(request);

        // Validar imagen obligatoria en creación
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

    // ══════════════════════════════════════════════════════════
    // ACTUALIZAR PRODUCTO
    // ══════════════════════════════════════════════════════════
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
            request.setAttribute("producto", p);
            request.setAttribute("materiales",    materialDAO.listarMateriales());
            request.setAttribute("subcategorias", subcategoriaDAO.listarTodas());
            request.setAttribute("proveedores",   proveedorDAO.listarProveedores());
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
            request.setAttribute("producto", p);
            request.setAttribute("materiales",    materialDAO.listarMateriales());
            request.setAttribute("subcategorias", subcategoriaDAO.listarTodas());
            request.setAttribute("proveedores",   proveedorDAO.listarProveedores());
            request.getRequestDispatcher("/Administrador/editar.jsp").forward(request, response);
        }
    }

    // ══════════════════════════════════════════════════════════
    // CONSTRUIR Producto desde request
    // ══════════════════════════════════════════════════════════
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
        p.setSubcategoriaId(parsearInt(request.getParameter("subcategoriaId"), 0));

        // CORRECCIÓN CLAVE: proveedorId es OBLIGATORIO — viene del select del formulario
        p.setProveedorId(parsearInt(request.getParameter("proveedorId"), 0));

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

    // ══════════════════════════════════════════════════════════
    // VALIDACIÓN SERVER-SIDE
    // ══════════════════════════════════════════════════════════
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

        // Validar margen mínimo: (costo * 2) + 5000
        BigDecimal minimo = p.getPrecioUnitario()
                             .multiply(new BigDecimal("2"))
                             .add(new BigDecimal("5000"));
        if (p.getPrecioVenta().compareTo(minimo) < 0)
            return "El precio de venta debe ser al menos el doble del costo + $5,000 "
                   + "(mínimo esperado: $" + minimo.toPlainString() + ").";

        return null;
    }

    // ══════════════════════════════════════════════════════════
    // AJUSTE MANUAL DE STOCK (AJAX desde ver-producto.jsp)
    // ══════════════════════════════════════════════════════════
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
            int productoId = Integer.parseInt(request.getParameter("productoId"));
            int nuevoStock = Integer.parseInt(request.getParameter("nuevoStock"));
            int cantidad   = Integer.parseInt(request.getParameter("cantidad"));
            String tipo    = request.getParameter("tipo");
            String motivo  = request.getParameter("motivo");

            if (nuevoStock < 0) throw new IllegalArgumentException("El stock no puede ser negativo.");

            productoDAO.actualizarStock(productoId, nuevoStock);
            productoDAO.registrarMovimiento(
                productoId, admin.getId(), tipo, cantidad, "Ajuste manual: " + motivo.trim());

            out.write("{\"ok\":true}");
        } catch (Exception e) {
            out.write("{\"ok\":false,\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // ══════════════════════════════════════════════════════════
    // ELIMINAR (POST)
    // ══════════════════════════════════════════════════════════
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

    // ══════════════════════════════════════════════════════════
    // HELPERS DE NAVEGACIÓN
    // ══════════════════════════════════════════════════════════
    private void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String catIdStr = request.getParameter("categoria");
        if (catIdStr == null || !catIdStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }
        request.setAttribute("categoria",     categoriaDAO.obtenerPorId(Integer.parseInt(catIdStr)));
        request.setAttribute("materiales",    materialDAO.listarMateriales());
        request.setAttribute("subcategorias", subcategoriaDAO.listarTodas());
        request.setAttribute("proveedores",   proveedorDAO.listarProveedores());
        request.getRequestDispatcher("/Administrador/agregar_producto.jsp").forward(request, response);
    }

    private void verProducto(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Producto p = obtenerProductoPorParam(request, response);
        if (p != null) {
            request.setAttribute("producto", p);
            request.getRequestDispatcher("/Administrador/ver-producto.jsp").forward(request, response);
        }
    }

    private void mostrarFormularioEditar(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Producto p = obtenerProductoPorParam(request, response);
        if (p != null) {
            request.setAttribute("producto",      p);
            request.setAttribute("materiales",    materialDAO.listarMateriales());
            request.setAttribute("subcategorias", subcategoriaDAO.listarTodas());
            request.setAttribute("proveedores",   proveedorDAO.listarProveedores());
            request.getRequestDispatcher("/Administrador/editar.jsp").forward(request, response);
        }
    }

    private void confirmarEliminar(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Producto p = obtenerProductoPorParam(request, response);
        if (p != null) {
            request.setAttribute("producto", p);
            request.getRequestDispatcher("/Administrador/eliminar.jsp").forward(request, response);
        }
    }

    private void reenviarFormularioNuevo(HttpServletRequest request, HttpServletResponse response,
                                         Producto p, String error) throws Exception {
        request.setAttribute("error",         error);
        request.setAttribute("producto",      p);
        request.setAttribute("categoria",     categoriaDAO.obtenerPorId(p.getCategoriaId()));
        request.setAttribute("materiales",    materialDAO.listarMateriales());
        request.setAttribute("subcategorias", subcategoriaDAO.listarTodas());
        request.setAttribute("proveedores",   proveedorDAO.listarProveedores());
        request.getRequestDispatcher("/Administrador/agregar_producto.jsp").forward(request, response);
    }

    // ══════════════════════════════════════════════════════════
    // AUTENTICACIÓN
    // ══════════════════════════════════════════════════════════
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
            throws Exception {
        String idStr = request.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return null;
        }
        return productoDAO.obtenerPorId(Integer.parseInt(idStr));
    }

    // ══════════════════════════════════════════════════════════
    // PARSERS UTILITARIOS
    // ══════════════════════════════════════════════════════════
    private int parsearInt(String valor, int def) {
        if (valor == null || valor.isBlank()) return def;
        try { return Integer.parseInt(valor.trim()); } catch (Exception e) { return def; }
    }

    private BigDecimal parsearBigDecimal(String valor, BigDecimal def) {
        if (valor == null || valor.isBlank()) return def;
        try { return new BigDecimal(valor.trim()); } catch (Exception e) { return def; }
    }
}