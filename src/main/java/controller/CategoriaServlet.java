package controller;

import dao.CategoriaDAO;
import dao.ProductoDAO;
import model.Categoria;
import model.Producto;
import model.Administrador;

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

    @Override
    public void init() {
        categoriaDAO = new CategoriaDAO();
        productoDAO = new ProductoDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Administrador admin = (Administrador) session.getAttribute("admin");
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
            return;
        }

        String idStr = request.getParameter("id");
        String query = request.getParameter("q");

        query = (query != null) ? query.trim() : "";
        idStr = (idStr != null) ? idStr.trim() : "";

        // Caso 1: Búsqueda global (sin categoría)
        if (!query.isEmpty() && idStr.isEmpty()) {
            List<Producto> productos = productoDAO.buscarGlobal(query);
            request.setAttribute("productos", productos);
            request.setAttribute("categoria", null);
            request.setAttribute("terminoBusqueda", query);
            request.getRequestDispatcher("/Administrador/accesorios/categoria.jsp").forward(request, response);
            return;
        }

        // Caso 2: Búsqueda dentro de una categoría
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
            request.getRequestDispatcher("/Administrador/accesorios/categoria.jsp").forward(request, response);
            return;
        }

        // Caso 3: Ver productos de una categoría (sin búsqueda)
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
            request.getRequestDispatcher("/Administrador/accesorios/categoria.jsp").forward(request, response);
            return;
        }

        // Caso 4: Mostrar lista de categorías
        List<Categoria> categorias = categoriaDAO.listarCategorias();
        request.setAttribute("categorias", categorias);
        // ✅ ¡Esta es la corrección clave!
        request.getRequestDispatcher("/Administrador/org-categorias.jsp").forward(request, response);
    }
}