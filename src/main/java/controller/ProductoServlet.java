package controller;

import dao.ProductoDAO;
import dao.CategoriaDAO;
import dao.MaterialDAO;
import model.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/ProductoServlet")
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

    /* ===============================
       GET
       =============================== */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null) {
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
                break;
        }
    }

    /* ===============================
       POST
       =============================== */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null) {
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
                break;
        }
    }

    /* ===============================
       MÉTODOS DE APOYO
       =============================== */

    private void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int categoriaId = Integer.parseInt(request.getParameter("categoria"));

        Categoria categoria = categoriaDAO.obtenerPorId(categoriaId);
        List<Material> materiales = materialDAO.listarMateriales();

        request.setAttribute("categoria", categoria);
        request.setAttribute("materiales", materiales);

        request.getRequestDispatcher("/Administrador/agregar_producto.jsp")
               .forward(request, response);
    }

    private void verProducto(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id = Integer.parseInt(request.getParameter("id"));

        Producto producto = productoDAO.obtenerPorId(id);

        if (producto == null) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }

        request.setAttribute("producto", producto);
        request.getRequestDispatcher("/Administrador/productos/ver_producto.jsp")
               .forward(request, response);
    }

    private void mostrarFormularioEditar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id = Integer.parseInt(request.getParameter("id"));

        Producto producto = productoDAO.obtenerPorId(id);
        List<Material> materiales = materialDAO.listarMateriales();

        if (producto == null) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }

        request.setAttribute("producto", producto);
        request.setAttribute("materiales", materiales);

        request.getRequestDispatcher("/Administrador/productos/editar_producto.jsp")
               .forward(request, response);
    }

    private void eliminarProducto(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        int id = Integer.parseInt(request.getParameter("id"));

        Producto producto = productoDAO.obtenerPorId(id);

        if (producto != null) {
            productoDAO.eliminar(id);
            int categoriaId = producto.getCategoria().getCategoriaId();
            response.sendRedirect(
                request.getContextPath() + "/CategoriaServlet?id=" + categoriaId
            );
        } else {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
        }
    }

    private void guardarProducto(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Producto p = construirProductoDesdeRequest(request);

        // proveedor = admin logueado (temporalmente)
        HttpSession session = request.getSession();
        Administrador admin = (Administrador) session.getAttribute("admin");

        productoDAO.guardar(p, admin.getId());

        response.sendRedirect(
            request.getContextPath() + "/CategoriaServlet?id=" +
            p.getCategoria().getCategoriaId()
        );
    }

    private void actualizarProducto(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Producto p = construirProductoDesdeRequest(request);
        p.setProductoId(Integer.parseInt(request.getParameter("productoId")));
        
        Categoria c = new Categoria();
        c.setCategoriaId(Integer.parseInt(request.getParameter("categoriaId")));
        p.setCategoria(c);


        productoDAO.actualizar(p);

        response.sendRedirect(
            request.getContextPath() + "/CategoriaServlet?id=" +
            p.getCategoria().getCategoriaId()
        );
    }

    private Producto construirProductoDesdeRequest(HttpServletRequest request) {

        Producto p = new Producto();
        p.setNombre(request.getParameter("nombre"));
        p.setDescripcion(request.getParameter("descripcion"));
        p.setStock(Integer.parseInt(request.getParameter("stock")));
        p.setPrecioUnitario(
        	    Double.parseDouble(request.getParameter("precioUnitario"))
        	);


        Categoria c = new Categoria();
        c.setCategoriaId(Integer.parseInt(request.getParameter("categoriaId")));
        p.setCategoria(c);

        Material m = new Material();
        m.setMaterialId(Integer.parseInt(request.getParameter("materialId")));
        p.setMaterial(m);

        return p;
    }
}
