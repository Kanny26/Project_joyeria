package controller;

import dao.ProveedorDAO;
import model.Proveedor;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@WebServlet("/ProveedorServlet") // Mapeo simple y claro
public class ProveedorServlet extends HttpServlet {

    private ProveedorDAO dao = new ProveedorDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Leemos el parámetro 'accion' de la URL
        String accion = req.getParameter("accion");

        if (accion == null || accion.equals("listar")) {
            listarProveedores(req, resp);
        } else if (accion.equals("agregar")) {
            // Enviamos al formulario físico
            req.getRequestDispatcher("/Administrador/proveedores/agregar.jsp").forward(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");

        if ("guardar".equals(action)) {
            Proveedor p = new Proveedor();
            p.setNombre(req.getParameter("nombre"));
            // ... (resto de tus sets de Proveedor) ...
            
            // Asumiendo que procesas los datos correctamente:
            if (dao.guardar(p)) {
                // Redirigir de vuelta al servlet para ver la lista actualizada
                resp.sendRedirect(req.getContextPath() + "/ProveedorServlet?accion=listar&exito=1");
            } else {
                req.setAttribute("error", "Error al guardar");
                req.getRequestDispatcher("/Administrador/proveedores/agregar.jsp").forward(req, resp);
            }
        }
    }

    private void listarProveedores(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Proveedor> proveedores = dao.listar();
        req.setAttribute("proveedores", proveedores);
        // Despachamos al archivo JSP real que muestra la tabla
        req.getRequestDispatcher("/Administrador/proveedores/listar.jsp").forward(req, resp);
    }
}