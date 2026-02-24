package controller;

import dao.ProveedorDAO;
import dao.CompraDAO;
import dao.MaterialDAO;
import dao.CategoriaDAO;
import dao.ProductoDAO;
import model.Proveedor;
import model.Administrador;
import model.Compra;
import model.Categoria;
import model.Producto;

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
    private MaterialDAO  materialDAO;
    private CategoriaDAO categoriaDAO;
    private ProductoDAO  productoDAO;

    @Override
    public void init() {
        proveedorDAO = new ProveedorDAO();
        materialDAO  = new MaterialDAO();
        categoriaDAO = new CategoriaDAO();
        productoDAO  = new ProductoDAO();
    }

    // ==================== GET ====================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        
        if (!estaAutenticado(request, response)) return;

        String action = request.getParameter("action");

        try {
            switch (action != null ? action : "listar") {
                case "listar":           listarProveedores(request, response);          break;
                case "buscar":           buscarProveedor(request, response);            break;
                case "nuevo":            mostrarFormularioNuevo(request, response);     break;
                case "editar":           mostrarFormularioEditar(request, response);    break;
                case "actualizarEstado": actualizarEstado(request, response);           break;
                case "confirmarEliminar":confirmarEliminarProveedor(request, response); break;
                case "verCompras":       verCompras(request, response);                 break;
                case "nuevaCompra":      mostrarFormularioCompra(request, response);    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/Administrador/admin-principal.jsp");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            listarProveedores(request, response);
        }
    }

    // ==================== POST ====================

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        
        if (!estaAutenticado(request, response)) return;

        String action = request.getParameter("action");

        try {
            switch (action != null ? action : "") {
                case "guardar":          guardarProveedor(request, response);      break;
                case "actualizar":       actualizarProveedor(request, response);   break;
                case "eliminar":         eliminarProveedorPost(request, response); break;
                case "actualizarEstado": actualizarEstado(request, response);      break;
                default:
                    response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error al procesar: " + e.getMessage());
            listarProveedores(request, response);
        }
    }

    // ==================== MÉTODOS GET ====================

    private void listarProveedores(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<Proveedor> proveedores = proveedorDAO.listarProveedores();
        
        request.setAttribute("proveedores",      proveedores);
        request.setAttribute("totalProveedores", proveedores.size());
        request.setAttribute("activos",          proveedores.stream().filter(Proveedor::isEstado).count());
        request.setAttribute("filtroActivo",     "nombre");
        
        request.getRequestDispatcher("/Administrador/proveedores.jsp")
               .forward(request, response);
    }

    private void buscarProveedor(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String q      = request.getParameter("q");
        String filtro = request.getParameter("filtro");
        if (filtro == null || filtro.isEmpty()) filtro = "nombre";

        List<Proveedor> resultados;
        if (q == null || q.trim().isEmpty()) {
            resultados = proveedorDAO.listarProveedores();
            q = "";
        } else {
            resultados = proveedorDAO.buscar(q.trim(), filtro);
        }

        request.setAttribute("proveedores",      resultados);
        request.setAttribute("busqueda",         q);
        request.setAttribute("filtroActivo",     filtro);
        request.setAttribute("totalProveedores", resultados.size());
        request.setAttribute("activos",          resultados.stream().filter(Proveedor::isEstado).count());
        
        request.getRequestDispatcher("/Administrador/proveedores.jsp")
               .forward(request, response);
    }

    private void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setAttribute("materiales", materialDAO.listarMateriales());
        request.getRequestDispatcher("/Administrador/proveedores/agregar.jsp")
               .forward(request, response);
    }

    private void mostrarFormularioEditar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
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
        
        request.setAttribute("proveedor",  p);
        request.setAttribute("materiales", materialDAO.listarMateriales());
        
        request.getRequestDispatcher("/Administrador/proveedores/editar.jsp")
               .forward(request, response);
    }

    private void actualizarEstado(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        
        String idStr     = request.getParameter("id");
        String estadoStr = request.getParameter("estado");
        
        if (idStr != null && estadoStr != null && idStr.matches("\\d+")) {
            proveedorDAO.actualizarEstado(Integer.parseInt(idStr), Boolean.parseBoolean(estadoStr));
        }
        
        // Forward limpio, sin msg en URL
        List<Proveedor> proveedores = proveedorDAO.listarProveedores();
        request.setAttribute("proveedores",      proveedores);
        request.setAttribute("totalProveedores", proveedores.size());
        request.setAttribute("activos",          proveedores.stream().filter(Proveedor::isEstado).count());
        request.setAttribute("filtroActivo",     "nombre");
        
        request.getRequestDispatcher("/Administrador/proveedores.jsp")
               .forward(request, response);
    }

    private void confirmarEliminarProveedor(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
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
        request.getRequestDispatcher("/Administrador/proveedores/eliminar.jsp")
               .forward(request, response);
    }

    // ==================== MÉTODOS POST ====================

    private void guardarProveedor(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Proveedor p = construirProveedorDesdeRequest(request);

        String[] telefonosArr  = request.getParameterValues("telefono");
        String[] correosArr    = request.getParameterValues("correo");
        String[] materialesArr = request.getParameterValues("materiales");

        List<String>  telefonos     = telefonosArr  != null ? Arrays.asList(telefonosArr)  : new ArrayList<>();
        List<String>  correos       = correosArr    != null ? Arrays.asList(correosArr)    : new ArrayList<>();
        List<Integer> materialesIds = new ArrayList<>();
        
        if (materialesArr != null) {
            for (String m : materialesArr) {
                if (m.matches("\\d+")) {
                    materialesIds.add(Integer.parseInt(m));
                }
            }
        }

        String error = validarProveedor(p, true);
        if (error != null) {
            request.setAttribute("error",     error);
            request.setAttribute("proveedor", p);
            request.setAttribute("materiales", materialDAO.listarMateriales());
            request.setAttribute("telefonos", telefonos);
            request.setAttribute("correos",   correos);
            request.getRequestDispatcher("/Administrador/proveedores/agregar.jsp")
                   .forward(request, response);
            return;
        }

        if (proveedorDAO.guardar(p, telefonos, correos, materialesIds)) {
            List<Proveedor> proveedores = proveedorDAO.listarProveedores();
            request.setAttribute("proveedores",      proveedores);
            request.setAttribute("totalProveedores", proveedores.size());
            request.setAttribute("activos",          proveedores.stream().filter(Proveedor::isEstado).count());
            request.setAttribute("filtroActivo",     "nombre");
            request.setAttribute("msg",              "creado");
            
            request.getRequestDispatcher("/Administrador/proveedores.jsp")
                   .forward(request, response);
        } else {
            request.setAttribute("error", "No se pudo guardar. Verifique que el documento no esté repetido.");
            request.setAttribute("materiales", materialDAO.listarMateriales());
            request.getRequestDispatcher("/Administrador/proveedores/agregar.jsp")
                   .forward(request, response);
        }
    }

    private void actualizarProveedor(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("usuarioId");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }

        Proveedor p = construirProveedorDesdeRequest(request);
        p.setUsuarioId(Integer.parseInt(idStr));

        // RF08: Mantener fecha_inicio original (NO EDITABLE)
        Proveedor original = proveedorDAO.obtenerPorId(p.getUsuarioId());
        if (original != null) {
            p.setFechaInicio(original.getFechaInicio());
        }

        String[] telefonosArr  = request.getParameterValues("telefono");
        String[] correosArr    = request.getParameterValues("correo");
        String[] materialesArr = request.getParameterValues("materiales");

        List<String>  telefonos     = telefonosArr  != null ? Arrays.asList(telefonosArr)  : new ArrayList<>();
        List<String>  correos       = correosArr    != null ? Arrays.asList(correosArr)    : new ArrayList<>();
        List<Integer> materialesIds = new ArrayList<>();
        
        if (materialesArr != null) {
            for (String m : materialesArr) {
                if (m.matches("\\d+")) {
                    materialesIds.add(Integer.parseInt(m));
                }
            }
        }

        String error = validarProveedor(p, false);
        if (error != null) {
            request.setAttribute("error",     error);
            request.setAttribute("proveedor", p);
            request.setAttribute("materiales", materialDAO.listarMateriales());
            request.getRequestDispatcher("/Administrador/proveedores/editar.jsp")
                   .forward(request, response);
            return;
        }

        if (proveedorDAO.actualizar(p, telefonos, correos, materialesIds)) {
            List<Proveedor> proveedores = proveedorDAO.listarProveedores();
            request.setAttribute("proveedores",      proveedores);
            request.setAttribute("totalProveedores", proveedores.size());
            request.setAttribute("activos",          proveedores.stream().filter(Proveedor::isEstado).count());
            request.setAttribute("filtroActivo",     "nombre");
            request.setAttribute("msg",              "actualizado");
            
            request.getRequestDispatcher("/Administrador/proveedores.jsp")
                   .forward(request, response);
        } else {
            request.setAttribute("error", "Error al actualizar el proveedor.");
            request.setAttribute("materiales", materialDAO.listarMateriales());
            request.getRequestDispatcher("/Administrador/proveedores/editar.jsp")
                   .forward(request, response);
        }
    }

    private void eliminarProveedorPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String idStr = request.getParameter("id");
        if (idStr != null && idStr.matches("\\d+")) {
            boolean exito = proveedorDAO.eliminar(Integer.parseInt(idStr));
            if (exito) {
                List<Proveedor> proveedores = proveedorDAO.listarProveedores();
                request.setAttribute("proveedores",      proveedores);
                request.setAttribute("totalProveedores", proveedores.size());
                request.setAttribute("activos",          proveedores.stream().filter(Proveedor::isEstado).count());
                request.setAttribute("filtroActivo",     "nombre");
                request.setAttribute("msg",              "eliminado");
                
                request.getRequestDispatcher("/Administrador/proveedores.jsp")
                       .forward(request, response);
                return;
            }
        }
        // Si falla, redirige sin mensaje
        response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
    }

    // ==================== MÉTODOS DE COMPRAS ====================

    private void verCompras(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String idStr = request.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }
        
        int proveedorId = Integer.parseInt(idStr);

        CompraDAO compraDAO = new CompraDAO();
        Proveedor proveedor = proveedorDAO.obtenerPorId(proveedorId);
        List<Compra> compras = compraDAO.listarPorProveedor(proveedorId);

        request.setAttribute("proveedor", proveedor);
        request.setAttribute("listaCompras", compras);
        request.setAttribute("totalCompras", compraDAO.contarComprasPorProveedor(proveedorId));
        request.setAttribute("totalProductos", compraDAO.contarComprasPorProveedor(proveedorId));
        request.setAttribute("totalGasto", compraDAO.totalGastadoPorProveedor(proveedorId));

        request.getRequestDispatcher("/Administrador/proveedores/compras.jsp")
                .forward(request, response);
    }

    private void mostrarFormularioCompra(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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

        // Cargar categorías para el modal de selección
        List<Categoria> categorias = categoriaDAO.listarCategorias();
        
        request.setAttribute("proveedor", proveedor);
        request.setAttribute("proveedorId", proveedorId);
        request.setAttribute("categorias", categorias);
        
        request.getRequestDispatcher("/Administrador/proveedores/agregar_compra.jsp")
                .forward(request, response);
    }

    // ==================== AUXILIARES ====================

    private Proveedor construirProveedorDesdeRequest(HttpServletRequest request) {
        Proveedor p = new Proveedor();
        p.setNombre(request.getParameter("nombre"));
        p.setDocumento(request.getParameter("documento"));
        p.setFechaInicio(request.getParameter("fechaInicio"));
        
        String minimoStr = request.getParameter("minimoCompra");
        p.setMinimoCompra(minimoStr != null && !minimoStr.isEmpty() 
                ? Double.parseDouble(minimoStr) 
                : 0.0);
        
        p.setEstado("activo".equalsIgnoreCase(request.getParameter("estado")));
        p.setPass("NO_LOGIN"); // Los proveedores no inician sesión
        
        return p;
    }

    private String validarProveedor(Proveedor p, boolean esNuevo) {
        if (p.getNombre() == null || p.getNombre().trim().isEmpty()) {
            return "El nombre es obligatorio.";
        }
        if (p.getDocumento() == null || p.getDocumento().trim().isEmpty()) {
            return "El documento es obligatorio.";
        }
        if (p.getFechaInicio() == null || p.getFechaInicio().isEmpty()) {
            return "La fecha de inicio es obligatoria.";
        }
        return null;
    }

    private boolean estaAutenticado(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        if (request.getSession().getAttribute("admin") == null) {
            response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
            return false;
        }
        return true;
    }
}