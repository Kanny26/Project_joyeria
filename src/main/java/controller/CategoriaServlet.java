package controller;

import dao.CategoriaDAO;
import dao.ProductoDAO;
import model.Categoria;
import model.Producto;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/CategoriaServlet")
public class CategoriaServlet extends HttpServlet {

    private CategoriaDAO categoriaDAO;

    @Override
    public void init() {
        categoriaDAO = new CategoriaDAO();
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idCategoriaStr = request.getParameter("id");

        // 👉 LISTAR CATEGORÍAS
        if (idCategoriaStr == null) {
            List<Categoria> categorias = categoriaDAO.listarCategorias();
            request.setAttribute("categorias", categorias);
            request.getRequestDispatcher("/Administrador/org-categorias.jsp")
                   .forward(request, response);
            return;
        }

        // 👉 VER PRODUCTOS POR CATEGORÍA
        int idCategoria = Integer.parseInt(idCategoriaStr);

        Categoria categoria = categoriaDAO.obtenerPorId(idCategoria);
        ProductoDAO productoDAO = new ProductoDAO();
        List<Producto> productos = productoDAO.listarPorCategoria(idCategoria);

        request.setAttribute("categoria", categoria);
        request.setAttribute("productos", productos);

        request.getRequestDispatcher("/Administrador/accesorios/categoria.jsp")
               .forward(request, response);
    }


}

