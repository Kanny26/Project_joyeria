package controller;
import dao.ProductoDAO;
import dao.CategoriaDAO;
import model.Categoria;
import model.Producto;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;


@WebServlet("/ProductoServlet")
public class ProductoServlet extends HttpServlet {

    private ProductoDAO productoDAO;
    private CategoriaDAO categoriaDAO;

    @Override
    public void init() {
        productoDAO = new ProductoDAO();
        categoriaDAO = new CategoriaDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
            return;
        }

        switch (action) {

            case "porCategoria":
                mostrarPorCategoria(request, response);
                break;

            default:
                response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
                break;
        }
    }

    private void mostrarPorCategoria(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int categoriaId = Integer.parseInt(request.getParameter("id"));

        Categoria categoria = categoriaDAO.obtenerPorId(categoriaId);
        List<Producto> productos = productoDAO.listarPorCategoria(categoriaId);

        request.setAttribute("categoria", categoria);
        request.setAttribute("productos", productos);

        request.getRequestDispatcher(
            "/Administrador/accesorios/categoria.jsp"
        ).forward(request, response);
    }
}
