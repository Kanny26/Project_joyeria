package controller;

import dao.ProductoDAO;
import dao.CategoriaDAO;
import dao.MaterialDAO;
import model.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.List;

@WebServlet("/ProductoServlet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 1024 * 1024 * 5,
    maxRequestSize = 1024 * 1024 * 10
)
public class ProductoServlet extends HttpServlet {

    private ProductoDAO productoDAO;
    private CategoriaDAO categoriaDAO;
    private MaterialDAO materialDAO;

    @Override
    public void init() {
        productoDAO = new ProductoDAO();
        categoriaDAO = new CategoriaDAO();
        materialDAO = new MaterialDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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
            case "eliminar":
                eliminarProducto(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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
            default:
                response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
        }
    }

    private void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String catIdStr = request.getParameter("categoria");
        if (catIdStr == null || !catIdStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }
        int categoriaId = Integer.parseInt(catIdStr);
        Categoria categoria = categoriaDAO.obtenerPorId(categoriaId);
        if (categoria == null) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }
        List<Material> materiales = materialDAO.listarMateriales();
        request.setAttribute("categoria", categoria);
        request.setAttribute("materiales", materiales);
        request.getRequestDispatcher("/Administrador/agregar_producto.jsp").forward(request, response);
    }

    private void verProducto(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }
        int id = Integer.parseInt(idStr);
        Producto producto = productoDAO.obtenerPorId(id);
        if (producto == null) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }
        request.setAttribute("producto", producto);
        request.getRequestDispatcher("/Administrador/ver-producto.jsp").forward(request, response);
    }

    private void mostrarFormularioEditar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }
        int id = Integer.parseInt(idStr);
        Producto producto = productoDAO.obtenerPorId(id);
        if (producto == null) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }
        List<Material> materiales = materialDAO.listarMateriales();
        request.setAttribute("producto", producto);
        request.setAttribute("materiales", materiales);
        request.getRequestDispatcher("/Administrador/editar.jsp").forward(request, response);
    }

    private void eliminarProducto(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }
        int id = Integer.parseInt(idStr);
        Producto producto = productoDAO.obtenerPorId(id);
        if (producto != null) {
            int categoriaId = producto.getCategoria().getCategoriaId();
            productoDAO.eliminar(id);
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet?id=" + categoriaId);
        } else {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
        }
    }

    private void guardarProducto(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Producto p = construirProductoDesdeRequest(request);
        HttpSession session = request.getSession();
        Administrador admin = (Administrador) session.getAttribute("admin");
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
            return;
        }
        productoDAO.guardar(p, admin.getId());
        response.sendRedirect(request.getContextPath() + "/CategoriaServlet?id=" + p.getCategoria().getCategoriaId());
    }

    private void actualizarProducto(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Producto p = construirProductoDesdeRequest(request);
        String idStr = request.getParameter("productoId");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }
        p.setProductoId(Integer.parseInt(idStr));
        productoDAO.actualizar(p);
        response.sendRedirect(request.getContextPath() + "/CategoriaServlet?id=" + p.getCategoria().getCategoriaId());
    }

    private Producto construirProductoDesdeRequest(HttpServletRequest request)
            throws IOException, ServletException {
        Producto p = new Producto();
        p.setNombre(request.getParameter("nombre"));
        p.setDescripcion(request.getParameter("descripcion"));
        p.setStock(Integer.parseInt(request.getParameter("stock")));
        p.setPrecioUnitario(new BigDecimal(request.getParameter("precioUnitario")));
        p.setPrecioVenta(new BigDecimal(request.getParameter("precioVenta")));

        Categoria c = new Categoria();
        c.setCategoriaId(Integer.parseInt(request.getParameter("categoriaId")));
        p.setCategoria(c);

        Material m = new Material();
        m.setMaterialId(Integer.parseInt(request.getParameter("materialId")));
        p.setMaterial(m);

        Part filePart = request.getPart("imagen");
        if (filePart != null && filePart.getSize() > 0) {
        	String uploadPath = getServletContext().getRealPath("/imagenes") + File.separator;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdirs();
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            filePart.write(uploadPath + fileName);
            p.setImagen(fileName);
        } else {
            p.setImagen(request.getParameter("imagenActual"));
        }
        return p;
    }
}
