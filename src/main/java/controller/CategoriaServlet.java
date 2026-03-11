package controller;

import dao.CategoriaDAO;
import dao.MaterialDAO;
import dao.MetodoPagoDAO;
import dao.ProductoDAO;
import dao.SubcategoriaDAO;
import model.Categoria;
import model.Producto;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/CategoriaServlet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize       = 1024 * 1024 * 10,
    maxRequestSize    = 1024 * 1024 * 50
)
public class CategoriaServlet extends HttpServlet {

    private CategoriaDAO    categoriaDAO;
    private ProductoDAO     productoDAO;
    private SubcategoriaDAO subcategoriaDAO;
    private MaterialDAO     materialDAO;
    private MetodoPagoDAO   metodoPagoDAO;

    @Override
    public void init() {
        categoriaDAO    = new CategoriaDAO();
        productoDAO     = new ProductoDAO();
        subcategoriaDAO = new SubcategoriaDAO();
        materialDAO     = new MaterialDAO();
        metodoPagoDAO   = new MetodoPagoDAO();
    }

    /** Listas para org-categorias.jsp (gestión de catálogo) */
    private void cargarListasGestion(HttpServletRequest request) throws Exception {
        request.setAttribute("categorias",    categoriaDAO.listarCategorias());
        request.setAttribute("subcategorias", subcategoriaDAO.listarTodas());
        request.setAttribute("materiales",    materialDAO.listarMateriales());
        request.setAttribute("metodosPago",   metodoPagoDAO.listarTodos());
    }

    /** Materiales y subcategorías para los selects de filtros en categoria.jsp */
    private void cargarFiltros(HttpServletRequest request) throws Exception {
        request.setAttribute("materiales",    materialDAO.listarMateriales());
        request.setAttribute("subcategorias", subcategoriaDAO.listarTodas());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
            return;
        }

        String idStr  = emptyToNull(request.getParameter("id"));
        String query  = emptyToNull(request.getParameter("q"));
        String filtro = request.getParameter("filtro");
        if (filtro == null || filtro.isBlank()) filtro = "todos";

        try {
            // CASO 1: búsqueda global (sin categoría)
            if (query != null && idStr == null) {
                // usa buscarGlobal(termino, filtro) — firma exacta del DAO
                request.setAttribute("productos",       productoDAO.buscarGlobal(query, filtro));
                request.setAttribute("categoria",       null);
                request.setAttribute("terminoBusqueda", query);
                request.setAttribute("filtroActivo",    filtro);
                cargarFiltros(request);
                forward(request, response, "/Administrador/accesorios/categoria.jsp");
                return;
            }

            // CASO 2: búsqueda dentro de una categoría
            if (query != null && idStr != null && idStr.matches("\\d+")) {
                int catId = Integer.parseInt(idStr);
                Categoria categoria = categoriaDAO.obtenerPorId(catId);
                if (categoria == null) {
                    response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
                    return;
                }
                // usa buscarEnCategoria(categoriaId, termino, filtro) — firma exacta del DAO
                request.setAttribute("productos",       productoDAO.buscarEnCategoria(catId, query, filtro));
                request.setAttribute("categoria",       categoria);
                request.setAttribute("terminoBusqueda", query);
                request.setAttribute("filtroActivo",    filtro);
                cargarFiltros(request);
                forward(request, response, "/Administrador/accesorios/categoria.jsp");
                return;
            }

            // CASO 3: ver todos los productos de una categoría
            if (idStr != null && idStr.matches("\\d+")) {
                int catId = Integer.parseInt(idStr);
                Categoria categoria = categoriaDAO.obtenerPorId(catId);
                if (categoria == null) {
                    response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
                    return;
                }
                request.setAttribute("productos", productoDAO.listarPorCategoria(catId));
                request.setAttribute("categoria", categoria);
                cargarFiltros(request);
                forward(request, response, "/Administrador/accesorios/categoria.jsp");
                return;
            }

            // CASO 4: listado general de categorías (org-categorias.jsp)
            cargarListasGestion(request);
            forward(request, response, "/Administrador/org-categorias.jsp");

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            try { cargarListasGestion(request); } catch (Exception ignored) {}
            forward(request, response, "/Administrador/org-categorias.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
            return;
        }

        String action = request.getParameter("action");
        try {
            if      ("guardar".equals(action))    guardarCategoria(request, response);
            else if ("actualizar".equals(action)) actualizarCategoria(request, response);
            else if ("eliminar".equals(action))   eliminarCategoria(request, response);
            else response.sendRedirect(request.getContextPath() + "/CategoriaServlet?tab=categorias");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", e.getMessage());
            try { cargarListasGestion(request); } catch (Exception ignored) {}
            forward(request, response, "/Administrador/org-categorias.jsp");
        }
    }

    private void guardarCategoria(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String nombre = request.getParameter("nombre");
        javax.servlet.http.Part filePart = request.getPart("archivoIcono");
        String fileName = filePart.getSubmittedFileName();
        String uploadPath = getServletContext().getRealPath("") + "assets/Imagenes/iconos";
        java.io.File uploadDir = new java.io.File(uploadPath);
        if (!uploadDir.exists()) uploadDir.mkdirs();
        filePart.write(uploadPath + java.io.File.separator + fileName);
        Categoria c = new Categoria();
        c.setNombre(nombre.trim());
        c.setIcono(fileName);
        categoriaDAO.guardar(c);
        response.sendRedirect(request.getContextPath() + "/CategoriaServlet?msg=creado&tab=categorias");
    }

    private void actualizarCategoria(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String idStr  = request.getParameter("id");
        String nombre = request.getParameter("nombre");
        String icono  = request.getParameter("icono");
        if (idStr == null || nombre == null || nombre.trim().isEmpty()) throw new Exception("Datos inválidos.");
        Categoria c = new Categoria();
        c.setCategoriaId(Integer.parseInt(idStr));
        c.setNombre(nombre.trim());
        c.setIcono(icono != null ? icono.trim() : "default.png");
        categoriaDAO.actualizar(c);
        response.sendRedirect(request.getContextPath() + "/CategoriaServlet?msg=actualizado&tab=categorias");
    }

    private void eliminarCategoria(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String idStr = request.getParameter("id");
        if (idStr == null) throw new Exception("ID no proporcionado.");
        categoriaDAO.eliminar(Integer.parseInt(idStr));
        response.sendRedirect(request.getContextPath() + "/CategoriaServlet?msg=eliminado&tab=categorias");
    }

    private void forward(HttpServletRequest req, HttpServletResponse res, String path)
            throws ServletException, IOException {
        req.getRequestDispatcher(path).forward(req, res);
    }

    private String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}