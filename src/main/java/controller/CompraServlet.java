package controller;

import dao.CompraDAO;
import model.Compra;
import model.Proveedor;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

@WebServlet("/CompraServlet")
public class CompraServlet extends HttpServlet {

    private CompraDAO compraDAO;

    @Override
    public void init() {
        compraDAO = new CompraDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("nueva".equals(action)) {

            String usuarioId = request.getParameter("usuarioId");
            request.setAttribute("usuarioId", usuarioId);

            request.getRequestDispatcher("/Administrador/proveedores/agregar_compra.jsp")
                   .forward(request, response);
        }
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        try {
            if ("guardar".equals(action)) {
                guardarCompra(request, response);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void guardarCompra(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        int proveedorId = Integer.parseInt(request.getParameter("usuarioId"));

        Compra compra = new Compra();
        compra.setProveedorId(proveedorId);
        compra.setFechaCompra(request.getParameter("fechaCompra"));

        String[] productos = request.getParameterValues("productoId");
        String[] precios = request.getParameterValues("precioUnitario");
        String[] cantidades = request.getParameterValues("cantidad");

        List<DetalleCompra> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < productos.length; i++) {

            DetalleCompra d = new DetalleCompra();
            d.setProductoId(Integer.parseInt(productos[i]));
            d.setPrecioUnitario(new BigDecimal(precios[i]));
            d.setCantidad(Integer.parseInt(cantidades[i]));

            BigDecimal subtotal =
                    d.getPrecioUnitario()
                            .multiply(new BigDecimal(d.getCantidad()));

            d.setSubtotal(subtotal);

            total = total.add(subtotal);

            detalles.add(d);
        }

        compra.setTotal(total);
        compra.setDetalles(detalles);

        compraDAO.insertar(compra);

        response.sendRedirect(
                request.getContextPath()
                + "/ProveedorServlet?action=verCompras&id=" + proveedorId
        );
    }