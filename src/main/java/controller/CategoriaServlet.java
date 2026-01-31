package controller;

import dao.CategoriaDAO;
import dao.ProductoDAO;
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

/**
 * Servlet encargado de gestionar:
 *  - Listado de categorías
 *  - Visualización de productos por categoría
 *  - Búsqueda global de productos
 *  - Búsqueda de productos dentro de una categoría
 *
 * Ruta:
 *  - /CategoriaServlet
 */
@WebServlet("/CategoriaServlet")
public class CategoriaServlet extends HttpServlet {

    /**
     * DAO para operaciones relacionadas con categorías.
     */
    private CategoriaDAO categoriaDAO;

    /**
     * DAO para operaciones relacionadas con productos.
     */
    private ProductoDAO productoDAO;

    /**
     * Inicializa los DAO al cargar el servlet.
     */
    @Override
    public void init() {
        categoriaDAO = new CategoriaDAO();
        productoDAO = new ProductoDAO();
    }

    /**
     * Maneja las peticiones GET.
     * Controla múltiples flujos según los parámetros recibidos:
     *  - Validación de sesión
     *  - Búsqueda global
     *  - Búsqueda por categoría
     *  - Listado de productos
     *  - Listado de categorías
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        /* ===============================
           VALIDACIÓN DE SESIÓN
           =============================== */
        HttpSession session = request.getSession();
        Administrador admin = (Administrador) session.getAttribute("admin");

        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
            return;
        }

        /* ===============================
           LECTURA Y NORMALIZACIÓN DE PARÁMETROS
           =============================== */
        String idStr = request.getParameter("id");
        String query = request.getParameter("q");

        query = (query != null) ? query.trim() : "";
        idStr = (idStr != null) ? idStr.trim() : "";

        /* ===============================
           CASO 1: BÚSQUEDA GLOBAL (SIN CATEGORÍA)
           =============================== */
        if (!query.isEmpty() && idStr.isEmpty()) {

            List<Producto> productos = productoDAO.buscarGlobal(query);

            request.setAttribute("productos", productos);
            request.setAttribute("categoria", null);
            request.setAttribute("terminoBusqueda", query);

            request.getRequestDispatcher("/Administrador/accesorios/categoria.jsp")
                   .forward(request, response);
            return;
        }

        /* ===============================
           CASO 2: BÚSQUEDA DENTRO DE UNA CATEGORÍA
           =============================== */
        if (!query.isEmpty() && !idStr.isEmpty() && idStr.matches("\\d+")) {

            int categoriaId = Integer.parseInt(idStr);
            Categoria categoria = categoriaDAO.obtenerPorId(categoriaId);

            if (categoria == null) {
                response.sendRedirect(request.getContextPath() + "/Administrador/org-categorias.jsp");
                return;
            }

            List<Producto> productos = productoDAO.buscarEnCategoria(categoriaId, query);

            request.setAttribute("productos", productos);
            request.setAttribute("categoria", categoria);
            request.setAttribute("terminoBusqueda", query);

            request.getRequestDispatcher("/Administrador/accesorios/categoria.jsp")
                   .forward(request, response);
            return;
        }

        /* ===============================
           CASO 3: VER PRODUCTOS DE UNA CATEGORÍA (SIN BÚSQUEDA)
           =============================== */
        if (!idStr.isEmpty() && idStr.matches("\\d+")) {

            int categoriaId = Integer.parseInt(idStr);
            Categoria categoria = categoriaDAO.obtenerPorId(categoriaId);

            if (categoria == null) {
                response.sendRedirect(request.getContextPath() + "/Administrador/org-categorias.jsp");
                return;
            }

            List<Producto> productos = productoDAO.listarPorCategoria(categoriaId);

            request.setAttribute("productos", productos);
            request.setAttribute("categoria", categoria);

            request.getRequestDispatcher("/Administrador/accesorios/categoria.jsp")
                   .forward(request, response);
            return;
        }

        /* ===============================
           CASO 4: LISTADO GENERAL DE CATEGORÍAS
           =============================== */
        List<Categoria> categorias = categoriaDAO.listarCategorias();
        request.setAttribute("categorias", categorias);

        request.getRequestDispatcher("/Administrador/org-categorias.jsp")
               .forward(request, response);
    }
}
