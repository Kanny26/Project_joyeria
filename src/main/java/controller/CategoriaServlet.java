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

@WebServlet("/CategoriaServlet")
public class CategoriaServlet extends HttpServlet {

    private CategoriaDAO categoriaDAO;
    private ProductoDAO  productoDAO;

    @Override
    public void init() {
        categoriaDAO = new CategoriaDAO();
        productoDAO  = new ProductoDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        /* ── Validar sesión ── */
        HttpSession session = request.getSession();
        Administrador admin = (Administrador) session.getAttribute("admin");
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
            return;
        }

        /* ── Leer parámetros ── */
        String idStr  = emptyToNull(request.getParameter("id"));
        String query  = emptyToNull(request.getParameter("q"));
        // filtro: "todos" | "nombre" | "material" | "stock"
        String filtro = request.getParameter("filtro");
        if (filtro == null || filtro.isBlank()) filtro = "todos";

        /* ══════════════════════════════════════════════
           CASO 1: BÚSQUEDA GLOBAL (sin categoría)
        ══════════════════════════════════════════════ */
        if (query != null && idStr == null) {
            List<Producto> productos = productoDAO.buscarGlobal(query, filtro);
            request.setAttribute("productos",       productos);
            request.setAttribute("categoria",       null);
            request.setAttribute("terminoBusqueda", query);
            request.setAttribute("filtroActivo",    filtro);
            forward(request, response, "/Administrador/accesorios/categoria.jsp");
            return;
        }

        /* ══════════════════════════════════════════════
           CASO 2: BÚSQUEDA DENTRO DE UNA CATEGORÍA
        ══════════════════════════════════════════════ */
        if (query != null && idStr != null && idStr.matches("\\d+")) {
            int categoriaId = Integer.parseInt(idStr);
            Categoria categoria = categoriaDAO.obtenerPorId(categoriaId);
            if (categoria == null) {
                response.sendRedirect(request.getContextPath() + "/Administrador/org-categorias.jsp");
                return;
            }
            List<Producto> productos = productoDAO.buscarEnCategoria(categoriaId, query, filtro);
            request.setAttribute("productos",       productos);
            request.setAttribute("categoria",       categoria);
            request.setAttribute("terminoBusqueda", query);
            request.setAttribute("filtroActivo",    filtro);
            forward(request, response, "/Administrador/accesorios/categoria.jsp");
            return;
        }

        /* ══════════════════════════════════════════════
           CASO 3: VER PRODUCTOS DE UNA CATEGORÍA
        ══════════════════════════════════════════════ */
        if (idStr != null && idStr.matches("\\d+")) {
            int categoriaId = Integer.parseInt(idStr);
            Categoria categoria = categoriaDAO.obtenerPorId(categoriaId);
            if (categoria == null) {
                response.sendRedirect(request.getContextPath() + "/Administrador/org-categorias.jsp");
                return;
            }
            List<Producto> productos = productoDAO.listarPorCategoria(categoriaId);
            request.setAttribute("productos",    productos);
            request.setAttribute("categoria",    categoria);
            forward(request, response, "/Administrador/accesorios/categoria.jsp");
            return;
        }

        /* ══════════════════════════════════════════════
           CASO 4: LISTADO GENERAL DE CATEGORÍAS
        ══════════════════════════════════════════════ */
        List<Categoria> categorias = categoriaDAO.listarCategorias();
        request.setAttribute("categorias", categorias);
        forward(request, response, "/Administrador/org-categorias.jsp");
    }

    /* ── helpers ── */
    private void forward(HttpServletRequest req, HttpServletResponse res, String path)
            throws ServletException, IOException {
        req.getRequestDispatcher(path).forward(req, res);
    }
    private String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
