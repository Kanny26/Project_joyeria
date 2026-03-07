package controller;

import dao.ProveedorDAO;
import dao.CompraDAO;
import dao.MaterialDAO;
import dao.CategoriaDAO;
import dao.ProductoDAO;
import model.Proveedor;
import model.Compra;
import model.Categoria;
import model.Producto;
import model.Administrador;
import dao.MetodoPagoDAO;
import model.MetodoPago;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WebServlet("/ProveedorServlet")
public class ProveedorServlet extends HttpServlet {
    private ProveedorDAO proveedorDAO;
    private CompraDAO compraDAO;
    private MaterialDAO materialDAO;
    private CategoriaDAO categoriaDAO;
    private ProductoDAO productoDAO;
    private MetodoPagoDAO metodoPagoDAO;

    @Override
    public void init() {
        proveedorDAO = new ProveedorDAO();
        compraDAO = new CompraDAO();
        materialDAO = new MaterialDAO();
        categoriaDAO = new CategoriaDAO();
        productoDAO = new ProductoDAO();
        metodoPagoDAO = new MetodoPagoDAO();
    }

    // ==================== GET ====================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        if (!estaAutenticado(request, response)) return;

        String action = request.getParameter("action");
        try {
            switch (action != null ? action : "listar") {
                case "listar" -> listarProveedores(request, response);
                case "verificarDocumento" -> verificarDocumento(request, response);
                case "buscar" -> buscarProveedor(request, response);
                case "nuevo" -> mostrarFormularioNuevo(request, response);
                case "editar" -> mostrarFormularioEditar(request, response);
                case "actualizarEstado" -> actualizarEstado(request, response);
                case "confirmarEliminar" -> confirmarEliminarProveedor(request, response);
                case "verCompras" -> verCompras(request, response);
                case "nuevaCompra" -> mostrarFormularioCompra(request, response);
                default -> response.sendRedirect(request.getContextPath() + "/Administrador/admin-principal.jsp");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            listarProveedores(request, response);
        }
    }

    // ==================== POST ====================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        if (!estaAutenticado(request, response)) return;

        String action = request.getParameter("action");
        try {
            switch (action != null ? action : "") {
                case "guardar" -> guardarProveedor(request, response);
                case "actualizar" -> actualizarProveedor(request, response);
                case "eliminar" -> eliminarProveedorPost(request, response);
                case "actualizarEstado" -> actualizarEstado(request, response);
                default -> response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error al procesar: " + e.getMessage());
            listarProveedores(request, response);
        }
    }

    // ==================== MÉTODOS GET ====================
    private void listarProveedores(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Proveedor> proveedores = proveedorDAO.listarProveedores();
        String msg = request.getParameter("msg");
        if (msg != null) request.setAttribute("msg", msg);
        request.setAttribute("proveedores", proveedores);
        request.setAttribute("totalProveedores", proveedores.size());
        request.setAttribute("activos", proveedores.stream().filter(Proveedor::isEstado).count());
        request.setAttribute("filtroActivo", "nombre");
        request.getRequestDispatcher("/Administrador/proveedores.jsp").forward(request, response);
    }

    private void verificarDocumento(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String doc = request.getParameter("documento");
        String idActualStr = request.getParameter("idActual");
        boolean existe;
        if (idActualStr != null && idActualStr.matches("\\d+")) {
            existe = proveedorDAO.existeDocumentoParaOtro(doc, Integer.parseInt(idActualStr));
        } else {
            existe = proveedorDAO.existeDocumento(doc);
        }
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"existe\": " + existe + "}");
    }

    private void buscarProveedor(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String q = request.getParameter("q");
        String filtro = request.getParameter("filtro");
        if (filtro == null || filtro.isEmpty()) filtro = "nombre";
        List<Proveedor> resultados;
        if (q == null || q.trim().isEmpty()) {
            resultados = proveedorDAO.listarProveedores();
            q = "";
        } else {
            resultados = proveedorDAO.buscar(q.trim(), filtro);
        }
        request.setAttribute("proveedores", resultados);
        request.setAttribute("busqueda", q);
        request.setAttribute("filtroActivo", filtro);
        request.setAttribute("totalProveedores", resultados.size());
        request.setAttribute("activos", resultados.stream().filter(Proveedor::isEstado).count());
        request.getRequestDispatcher("/Administrador/proveedores.jsp").forward(request, response);
    }

    private void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("materiales", materialDAO.listarMateriales());
        request.getRequestDispatcher("/Administrador/proveedores/agregar.jsp").forward(request, response);
    }

    private void mostrarFormularioEditar(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }
        Proveedor p = proveedorDAO.obtenerPorId(Integer.parseInt(idStr));
        if (p == null) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }
        request.setAttribute("proveedor", p);
        request.setAttribute("materiales", materialDAO.listarMateriales());
        request.getRequestDispatcher("/Administrador/proveedores/editar.jsp").forward(request, response);
    }

    private void actualizarEstado(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String idStr = request.getParameter("id");
        String estadoStr = request.getParameter("estado");
        if (idStr != null && estadoStr != null && idStr.matches("\\d+")) {
            proveedorDAO.actualizarEstado(Integer.parseInt(idStr), Boolean.parseBoolean(estadoStr));
        }
        listarProveedores(request, response);
    }

    private void confirmarEliminarProveedor(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }
        Proveedor p = proveedorDAO.obtenerPorId(Integer.parseInt(idStr));
        if (p == null) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }
        request.setAttribute("proveedor", p);
        request.getRequestDispatcher("/Administrador/proveedores/eliminar.jsp").forward(request, response);
    }

    // ==================== MÉTODOS POST ====================
    private void guardarProveedor(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Administrador admin = (Administrador) session.getAttribute("admin");
        
        Proveedor p = construirProveedorDesdeRequest(request);
        String[] telefonosArr = request.getParameterValues("telefono");
        String[] correosArr = request.getParameterValues("correo");
        String[] materialesArr = request.getParameterValues("materiales");

        List<String> telefonos = telefonosArr != null ? Arrays.asList(telefonosArr) : new ArrayList<>();
        List<String> correos = correosArr != null ? Arrays.asList(correosArr) : new ArrayList<>();
        List<Integer> materialesIds = new ArrayList<>();
        if (materialesArr != null) {
            for (String m : materialesArr) {
                if (m.matches("\\d+")) materialesIds.add(Integer.parseInt(m));
            }
        }

        String error = validarProveedor(p, true);
        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("proveedor", p);
            request.setAttribute("materiales", materialDAO.listarMateriales());
            request.getRequestDispatcher("/Administrador/proveedores/agregar.jsp").forward(request, response);
            return;
        }

        // ■■ Pasar usuarioId para auditoría ■■
        if (proveedorDAO.guardar(p, telefonos, correos, materialesIds, admin.getId())) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar&msg=creado");
        } else {
            request.setAttribute("error", "Error: El documento ya existe o hubo un fallo en la base de datos.");
            request.setAttribute("materiales", materialDAO.listarMateriales());
            request.getRequestDispatcher("/Administrador/proveedores/agregar.jsp").forward(request, response);
        }
    }

    private void actualizarProveedor(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Administrador admin = (Administrador) session.getAttribute("admin");
        
        String idStr = request.getParameter("proveedorId");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }

        Proveedor p = construirProveedorDesdeRequest(request);
        // ■■ CORREGIDO: setProveedorId() ■■
        p.setProveedorId(Integer.parseInt(idStr));

        // Mantener nombre, documento y fecha_inicio originales (campos no editables)
        Proveedor original = proveedorDAO.obtenerPorId(p.getProveedorId());
        if (original != null) {
            p.setNombre(original.getNombre());
            p.setDocumento(original.getDocumento());
            p.setFechaInicio(original.getFechaInicio());
        }

        String[] telefonosArr = request.getParameterValues("telefono");
        String[] correosArr = request.getParameterValues("correo");
        String[] materialesArr = request.getParameterValues("materiales");

        List<String> telefonos = telefonosArr != null ? Arrays.asList(telefonosArr) : new ArrayList<>();
        List<String> correos = correosArr != null ? Arrays.asList(correosArr) : new ArrayList<>();
        List<Integer> materialesIds = new ArrayList<>();
        if (materialesArr != null) {
            for (String m : materialesArr) {
                if (m.matches("\\d+")) materialesIds.add(Integer.parseInt(m));
            }
        }

        String error = validarProveedor(p, false);
        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("proveedor", p);
            request.setAttribute("materiales", materialDAO.listarMateriales());
            request.getRequestDispatcher("/Administrador/proveedores/editar.jsp").forward(request, response);
            return;
        }

        // ■■ Pasar usuarioId para auditoría ■■
        if (proveedorDAO.actualizar(p, telefonos, correos, materialesIds, admin.getId())) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar&msg=actualizado");
        } else {
            request.setAttribute("error", "Error al actualizar el proveedor.");
            request.setAttribute("proveedor", p);
            request.setAttribute("materiales", materialDAO.listarMateriales());
            request.getRequestDispatcher("/Administrador/proveedores/editar.jsp").forward(request, response);
        }
    }

    private void eliminarProveedorPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Administrador admin = (Administrador) session.getAttribute("admin");

        String idStr = request.getParameter("id");
        if (idStr != null && idStr.matches("\\d+")) {
            // ■■ Pasar usuarioId para auditoría ■■
            boolean exito = proveedorDAO.eliminar(Integer.parseInt(idStr), admin.getId());
            if (exito) {
                response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar&msg=eliminado");
                return;
            }
        }
        response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
    }

    // ==================== AUXILIARES ====================
    private Proveedor construirProveedorDesdeRequest(HttpServletRequest request) {
        Proveedor p = new Proveedor();
        p.setNombre(request.getParameter("nombre"));
        p.setDocumento(request.getParameter("documento"));
        p.setFechaInicio(request.getParameter("fechaInicio"));
        String minimoStr = request.getParameter("minimoCompra");
        p.setMinimoCompra(minimoStr != null && !minimoStr.isEmpty() ? Double.parseDouble(minimoStr) : 0.0);
        p.setEstado("activo".equalsIgnoreCase(request.getParameter("estado")));
        return p;
    }

    private String validarProveedor(Proveedor p, boolean esNuevo) {
        if (p.getNombre() == null || p.getNombre().trim().isEmpty()) return "El nombre es obligatorio.";
        if (p.getDocumento() == null || p.getDocumento().trim().isEmpty()) return "El documento es obligatorio.";
        if (p.getFechaInicio() == null || p.getFechaInicio().isEmpty()) return "La fecha de inicio es obligatoria.";
        if (esNuevo && proveedorDAO.existeDocumento(p.getDocumento())) return "Ya existe un proveedor con ese documento.";
        return null;
    }

    private boolean estaAutenticado(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getSession().getAttribute("admin") == null) {
            response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
            return false;
        }
        return true;
    }
    
    private void verCompras(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String idStr = request.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }
        int proveedorId = Integer.parseInt(idStr);
        Proveedor proveedor = proveedorDAO.obtenerPorId(proveedorId);
        if (proveedor == null) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }
        List<Compra> compras = compraDAO.listarPorProveedor(proveedorId);

        // Calcular estadísticas
        java.math.BigDecimal totalGasto = java.math.BigDecimal.ZERO;
        int totalProductos = 0;
        for (Compra c : compras) {
            if (c.getTotal() != null) totalGasto = totalGasto.add(c.getTotal());
            if (c.getDetalles() != null) {
                for (model.DetalleCompra d : c.getDetalles()) {
                    totalProductos += d.getCantidad();
                }
            }
        }

        request.setAttribute("proveedor", proveedor);
        request.setAttribute("listaCompras", compras);
        request.setAttribute("totalCompras", compras.size());
        request.setAttribute("totalProductos", totalProductos);
        request.setAttribute("totalGasto", totalGasto);
        request.getRequestDispatcher("/Administrador/proveedores/compras.jsp").forward(request, response);
    }
    
    private void mostrarFormularioCompra(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Implementación similar a la original
        String idStr = request.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }
        int proveedorId = Integer.parseInt(idStr);
        Proveedor proveedor = proveedorDAO.obtenerPorId(proveedorId);
        if (proveedor == null) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }
        List<Categoria> categorias = categoriaDAO.listarCategorias();
        List<MetodoPago> metodosPago = metodoPagoDAO.listarTodos();
        request.setAttribute("proveedor", proveedor);
        request.setAttribute("proveedorId", proveedorId);
        request.setAttribute("categorias", categorias);
        request.setAttribute("metodosPago", metodosPago);
        request.getRequestDispatcher("/Administrador/proveedores/agregar_compra.jsp").forward(request, response);
    }
}