package controller;

import dao.CategoriaDAO;
import dao.ProductoDAO;
import dao.SubcategoriaDAO;
import model.Administrador;
import model.Categoria;
import model.Producto;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/CategoriaServlet")
public class CategoriaServlet extends HttpServlet {
    private CategoriaDAO categoriaDAO;
    private ProductoDAO productoDAO;
    private SubcategoriaDAO subcategoriaDAO;

    @Override
    public void init() {
        categoriaDAO = new CategoriaDAO();
        productoDAO = new ProductoDAO();
        subcategoriaDAO = new SubcategoriaDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
            return;
        }

        String idStr = emptyToNull(request.getParameter("id"));
        String query = emptyToNull(request.getParameter("q"));
        String filtro = request.getParameter("filtro");
        if (filtro == null || filtro.isBlank()) filtro = "todos";

        try {
            // CASO 1: BÚSQUEDA GLOBAL
            if (query != null && idStr == null) {
                List<Producto> productos = productoDAO.listarProductosDisponibles();
                request.setAttribute("productos", productos);
                request.setAttribute("categoria", null);
                request.setAttribute("terminoBusqueda", query);
                request.setAttribute("filtroActivo", filtro);
                forward(request, response, "/Administrador/accesorios/categoria.jsp");
                return;
            }

            // CASO 2: BÚSQUEDA EN CATEGORÍA
            if (query != null && idStr != null && idStr.matches("\\d+")) {
                int catId = Integer.parseInt(idStr);
                Categoria categoria = categoriaDAO.obtenerPorId(catId);
                if (categoria == null) {
                    response.sendRedirect(request.getContextPath() + "/Administrador/org-categorias.jsp");
                    return;
                }
                List<Producto> productos = productoDAO.listarPorCategoria(catId);
                request.setAttribute("productos", productos);
                request.setAttribute("categoria", categoria);
                request.setAttribute("terminoBusqueda", query);
                request.setAttribute("filtroActivo", filtro);
                forward(request, response, "/Administrador/accesorios/categoria.jsp");
                return;
            }

            // CASO 3: VER PRODUCTOS DE UNA CATEGORÍA
            if (idStr != null && idStr.matches("\\d+")) {
                int catId = Integer.parseInt(idStr);
                Categoria categoria = categoriaDAO.obtenerPorId(catId);
                if (categoria == null) {
                    response.sendRedirect(request.getContextPath() + "/Administrador/org-categorias.jsp");
                    return;
                }
                List<Producto> productos = productoDAO.listarPorCategoria(catId);
                request.setAttribute("productos", productos);
                request.setAttribute("categoria", categoria);
                forward(request, response, "/Administrador/accesorios/categoria.jsp");
                return;
            }

            // CASO 4: LISTADO GENERAL DE CATEGORÍAS
            List<Categoria> categorias = categoriaDAO.listarCategorias();
            request.setAttribute("categorias", categorias);
            forward(request, response, "/Administrador/org-categorias.jsp");

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            forward(request, response, "/Administrador/org-categorias.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
            return;
        }

        String action = request.getParameter("action");
        try {
            if ("guardar".equals(action)) {
                guardarCategoria(request, response);
            } else if ("actualizar".equals(action)) {
                actualizarCategoria(request, response);
            } else if ("eliminar".equals(action)) {
                eliminarCategoria(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            forward(request, response, "/Administrador/org-categorias.jsp");
        }
    }

    private void guardarCategoria(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String nombre = request.getParameter("nombre");
        String icono = request.getParameter("icono");
        String subcatIdStr = request.getParameter("subcategoriaId");

        if (nombre == null || nombre.trim().isEmpty()) throw new Exception("El nombre es obligatorio.");

        Categoria c = new Categoria();
        c.setNombre(nombre.trim());
        c.setIcono(icono != null ? icono.trim() : "default.png");
        if (subcatIdStr != null && !subcatIdStr.isEmpty()) {
            c.setSubcategoriaId(Integer.parseInt(subcatIdStr));
        }

        categoriaDAO.guardar(c);
        response.sendRedirect(request.getContextPath() + "/CategoriaServlet?msg=creado");
    }

    private void actualizarCategoria(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String idStr = request.getParameter("id");
        String nombre = request.getParameter("nombre");
        String icono = request.getParameter("icono");
        String subcatIdStr = request.getParameter("subcategoriaId");

        if (idStr == null || nombre == null || nombre.trim().isEmpty()) throw new Exception("Datos inválidos.");

        Categoria c = new Categoria();
        c.setCategoriaId(Integer.parseInt(idStr));
        c.setNombre(nombre.trim());
        c.setIcono(icono != null ? icono.trim() : "default.png");
        if (subcatIdStr != null && !subcatIdStr.isEmpty()) {
            c.setSubcategoriaId(Integer.parseInt(subcatIdStr));
        }

        categoriaDAO.actualizar(c);
        response.sendRedirect(request.getContextPath() + "/CategoriaServlet?msg=actualizado");
    }

    private void eliminarCategoria(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String idStr = request.getParameter("id");
        if (idStr == null) throw new Exception("ID no proporcionado.");

        int id = Integer.parseInt(idStr);
        // La validación de productos activos se hace dentro del DAO
        categoriaDAO.eliminar(id);
        response.sendRedirect(request.getContextPath() + "/CategoriaServlet?msg=eliminado");
    }

    private void forward(HttpServletRequest req, HttpServletResponse res, String path) throws ServletException, IOException {
        req.getRequestDispatcher(path).forward(req, res);
    }

    private String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}