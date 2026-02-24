package controller;

import dao.CompraDAO;
import dao.ProductoDAO;
import dao.CategoriaDAO;
import model.Compra;
import model.DetalleCompra;
import model.Producto;
import model.Categoria;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@WebServlet("/CompraServlet")
public class CompraServlet extends HttpServlet {
    
    private CompraDAO compraDAO;
    private ProductoDAO productoDAO;
    private CategoriaDAO categoriaDAO;
    
    @Override
    public void init() throws ServletException {
        compraDAO = new CompraDAO();
        productoDAO = new ProductoDAO();
        categoriaDAO = new CategoriaDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if (action == null) action = "";
        
        try {
            switch (action) {
                case "nueva" -> mostrarFormularioNueva(request, response);
                case "detalle" -> verDetalle(request, response);
                case "eliminar" -> eliminarCompra(request, response);
                case "obtenerCategorias" -> obtenerCategoriasJSON(response);
                case "obtenerProductosPorCategoria" -> obtenerProductosPorCategoriaJSON(request, response);
                default -> response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            }
        } catch (Exception e) {
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        if (!estaAutenticado(request, response)) return;
        request.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        
        try {
            switch (action) {
                case "guardarCompra" -> guardarCompra(request, response);
                default -> response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            }
        } catch (Exception e) {
            request.setAttribute("error", "Error al guardar compra: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    private void obtenerCategoriasJSON(HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try {
            List<Categoria> categorias = categoriaDAO.listarCategorias();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < categorias.size(); i++) {
                Categoria c = categorias.get(i);
                json.append("{\"id\":").append(c.getCategoriaId())
                    .append(",\"nombre\":\"").append(escapeJson(c.getNombre()))
                    .append("\",\"icono\":\"").append(c.getIcono() != null ? c.getIcono() : "")
                    .append("\"}");
                if (i < categorias.size() - 1) json.append(",");
            }
            json.append("]");
            response.getWriter().write(json.toString());
        } catch (Exception e) {
            response.getWriter().write("[]");
        }
    }
    
    private void obtenerProductosPorCategoriaJSON(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        String categoriaIdStr = request.getParameter("categoriaId");
        if (categoriaIdStr == null || !categoriaIdStr.matches("\\d+")) {
            response.getWriter().write("[]");
            return;
        }
        try {
            int categoriaId = Integer.parseInt(categoriaIdStr);
            List<Producto> productos = productoDAO.listarPorCategoria(categoriaId);
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < productos.size(); i++) {
                Producto p = productos.get(i);
                json.append("{\"id\":").append(p.getProductoId())
                    .append(",\"nombre\":\"").append(escapeJson(p.getNombre()))
                    .append("\",\"codigo\":\"").append(p.getCodigo())
                    .append("\",\"stock\":").append(p.getStock())
                    .append(",\"precioUnitario\":").append(p.getPrecioUnitario())
                    .append(",\"imagen\":\"").append(p.getImagen() != null ? p.getImagen() : "")
                    .append("\"}");
                if (i < productos.size() - 1) json.append(",");
            }
            json.append("]");
            response.getWriter().write(json.toString());
        } catch (Exception e) {
            response.getWriter().write("[]");
        }
    }
    
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    private void mostrarFormularioNueva(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String usuarioIdStr = request.getParameter("usuarioId");
        if (usuarioIdStr == null || !usuarioIdStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }
        request.setAttribute("usuarioId", usuarioIdStr);
        request.getRequestDispatcher("/Administrador/proveedores/agregar_compra.jsp")
                .forward(request, response);
    }
    
    private void guardarCompra(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String usuarioIdStr = request.getParameter("usuarioId");
        if (usuarioIdStr == null || !usuarioIdStr.matches("\\d+")) {
            throw new IllegalArgumentException("ID de proveedor inválido");
        }
        int proveedorId = Integer.parseInt(usuarioIdStr);
        
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date fechaCompra = fmt.parse(request.getParameter("fechaCompra"));
        Date fechaEntrega = fmt.parse(request.getParameter("fechaEntrega"));
        
        if (fechaEntrega.before(fechaCompra)) {
            throw new IllegalArgumentException("La fecha de entrega no puede ser anterior a la fecha de compra");
        }
        
        String[] productosIds = request.getParameterValues("productoId");
        String[] precios = request.getParameterValues("precioUnitario");
        String[] cantidades = request.getParameterValues("cantidad");
        
        if (productosIds == null || productosIds.length == 0) {
            throw new IllegalArgumentException("Debe agregar al menos un producto a la compra");
        }
        
        List<DetalleCompra> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        
        for (int i = 0; i < productosIds.length; i++) {
            if (productosIds[i] == null || productosIds[i].trim().isEmpty()) continue;
            int productoId = Integer.parseInt(productosIds[i].trim());
            BigDecimal precioUnitario = new BigDecimal(precios[i].trim());
            int cantidad = Integer.parseInt(cantidades[i].trim());
            
            if (productoId <= 0 || precioUnitario.compareTo(BigDecimal.ZERO) <= 0 || cantidad <= 0) {
                throw new IllegalArgumentException("Datos inválidos en la fila " + (i + 1));
            }
            
            DetalleCompra d = new DetalleCompra();
            d.setProductoId(productoId);
            d.setPrecioUnitario(precioUnitario);
            d.setCantidad(cantidad);
            d.setSubtotal(precioUnitario.multiply(new BigDecimal(cantidad)));
            total = total.add(d.getSubtotal());
            detalles.add(d);
        }
        
        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("No hay productos válidos en la compra");
        }
        
        Compra compra = new Compra();
        compra.setProveedorId(proveedorId);
        compra.setFechaCompra(fechaCompra);
        compra.setFechaEntrega(fechaEntrega);
        compra.setTotal(total);
        compra.setDetalles(detalles);
        
        boolean exito = compraDAO.insertarConTransaccion(compra);
        if (exito) {
            response.sendRedirect(request.getContextPath() 
                + "/ProveedorServlet?action=verCompras&id=" + proveedorId + "&msg=creado");
        } else {
            throw new Exception("No se pudo guardar la compra");
        }
    }
    
    private void verDetalle(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String idStr = request.getParameter("id");
        String proveedorIdStr = request.getParameter("proveedorId");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }
        Compra compra = compraDAO.obtenerPorId(Integer.parseInt(idStr));
        if (compra == null) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }
        request.setAttribute("compra", compra);
        request.setAttribute("proveedorId", proveedorIdStr);
        request.getRequestDispatcher("/Administrador/proveedores/detalle_compra.jsp")
                .forward(request, response);
    }
    
    private void eliminarCompra(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String idStr = request.getParameter("id");
        String proveedorIdStr = request.getParameter("proveedorId");
        if (idStr != null && idStr.matches("\\d+")) {
            compraDAO.eliminarConTransaccion(Integer.parseInt(idStr));
        }
        response.sendRedirect(request.getContextPath() 
            + "/ProveedorServlet?action=verCompras&id=" + proveedorIdStr + "&msg=eliminado");
    }
    
    private boolean estaAutenticado(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
            return false;
        }
        return true;
    }
}