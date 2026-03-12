package controller;

import dao.ProveedorDAO;
import dao.CompraDAO;
import dao.MaterialDAO;
import dao.CategoriaDAO;
import dao.ProductoDAO;
import model.Proveedor;
import model.Compra;
import model.Categoria;
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
        proveedorDAO  = new ProveedorDAO();
        compraDAO     = new CompraDAO();
        materialDAO   = new MaterialDAO();
        categoriaDAO  = new CategoriaDAO();
        productoDAO   = new ProductoDAO();
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
                case "listar"           -> listarProveedores(request, response);
                case "verificarDocumento" -> verificarDocumento(request, response);
                case "buscar"           -> buscarProveedor(request, response);
                case "nuevo"            -> mostrarFormularioNuevo(request, response);
                case "editar"           -> mostrarFormularioEditar(request, response);
                case "actualizarEstado" -> actualizarEstado(request, response);
                case "confirmarEliminar"-> confirmarEliminarProveedor(request, response);
                case "verCompras"       -> verCompras(request, response);
                case "nuevaCompra"      -> mostrarFormularioCompra(request, response);
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
                case "guardar"          -> guardarProveedor(request, response);
                case "actualizar"       -> actualizarProveedor(request, response);
                case "eliminar"         -> eliminarProveedorPost(request, response);
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
        request.setAttribute("proveedores",      proveedores);
        request.setAttribute("totalProveedores", proveedores.size());
        request.setAttribute("activos",          proveedores.stream().filter(Proveedor::isEstado).count());
        request.setAttribute("filtroActivo",     "todos");
        request.getRequestDispatcher("/Administrador/proveedores.jsp").forward(request, response);
    }

    private void verificarDocumento(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String doc        = request.getParameter("documento");
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
        String q      = request.getParameter("q");
        String filtro = request.getParameter("filtro");
        if (filtro == null || filtro.isEmpty()) filtro = "todos";

        List<Proveedor> resultados;
        try {
            if (q == null || q.trim().isEmpty()) {
                resultados = proveedorDAO.listarProveedores();
                q = "";
            } else {
                resultados = proveedorDAO.buscar(q.trim(), filtro);
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultados = proveedorDAO.listarProveedores();
            q = "";
        }

        request.setAttribute("proveedores",      resultados);
        request.setAttribute("busqueda",         q);
        request.setAttribute("filtroActivo",     filtro);
        request.setAttribute("totalProveedores", resultados.size());
        request.setAttribute("activos",          resultados.stream().filter(Proveedor::isEstado).count());
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
        request.setAttribute("proveedor",   p);
        request.setAttribute("materiales",  materialDAO.listarMateriales());
        request.getRequestDispatcher("/Administrador/proveedores/editar.jsp").forward(request, response);
    }

    private void actualizarEstado(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String idStr     = request.getParameter("id");
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

        Proveedor p          = construirProveedorDesdeRequest(request);
        String[] telefonosArr  = request.getParameterValues("telefono");
        String[] correosArr    = request.getParameterValues("correo");
        String[] materialesArr = request.getParameterValues("materiales");

        List<String>  telefonos    = telefonosArr  != null ? Arrays.asList(telefonosArr)  : new ArrayList<>();
        List<String>  correos      = correosArr    != null ? Arrays.asList(correosArr)    : new ArrayList<>();
        List<Integer> materialesIds = new ArrayList<>();
        if (materialesArr != null) {
            for (String m : materialesArr) {
                if (m.matches("\\d+")) materialesIds.add(Integer.parseInt(m));
            }
        }

        // 🔍 DEBUG: Imprimir datos recibidos
        System.out.println("📦 Guardando proveedor: " + p.getNombre());
        System.out.println("   Documento: " + p.getDocumento());
        System.out.println("   Estado: " + p.isEstado());
        System.out.println("   Teléfonos: " + telefonos);
        System.out.println("   Correos: " + correos);
        System.out.println("   Materiales: " + materialesIds);

        String error = validarProveedor(p, true);
        if (error != null) {
            System.err.println("❌ Validación fallida: " + error);
            reenviarFormProveedor(request, response, error, p, "/Administrador/proveedores/agregar.jsp"); 
            return;
        }

        // ■■ Validar duplicados de teléfonos ■■
        for (String tel : telefonos) {
            if (tel != null && !tel.trim().isEmpty() && proveedorDAO.existeTelefonoProveedor(tel)) {
                reenviarFormProveedor(request, response, "El teléfono " + tel + " ya está registrado en otro proveedor.", p, "/Administrador/proveedores/agregar.jsp"); 
                return;
            }
        }

        // ■■ Validar duplicados de correos ■■
        for (String correo : correos) {
            if (correo != null && !correo.trim().isEmpty() && proveedorDAO.existeCorreoProveedor(correo)) {
                reenviarFormProveedor(request, response, "El correo " + correo + " ya está registrado en otro proveedor.", p, "/Administrador/proveedores/agregar.jsp"); 
                return;
            }
        }

        try {
            if (proveedorDAO.guardar(p, telefonos, correos, materialesIds, admin.getId())) {
                System.out.println("✅ Proveedor guardado exitosamente");
                response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar&msg=creado");
            } else {
                System.err.println("❌ proveedorDAO.guardar() retornó false");
                reenviarFormProveedor(request, response, "Error: El documento ya existe o hubo un fallo en la base de datos.", p, "/Administrador/proveedores/agregar.jsp");
            }
        } catch (Exception e) {
            System.err.println("❌ Excepción al guardar proveedor: " + e.getMessage());
            e.printStackTrace();
            reenviarFormProveedor(request, response, "Error interno: " + e.getMessage(), p, "/Administrador/proveedores/agregar.jsp");
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

        int proveedorId = Integer.parseInt(idStr);
        Proveedor p = construirProveedorDesdeRequest(request);
        p.setProveedorId(proveedorId);

        // ■■ Tomar nombre, documento y fechaInicio del original (no del form) ■■
        Proveedor original = proveedorDAO.obtenerPorId(proveedorId);
        if (original == null) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar"); 
            return;
        }
        p.setNombre(original.getNombre());
        p.setDocumento(original.getDocumento());
        p.setFechaInicio(original.getFechaInicio());

        String[] telefonosArr  = request.getParameterValues("telefono");
        String[] correosArr    = request.getParameterValues("correo");
        String[] materialesArr = request.getParameterValues("materiales");

        List<String>  telefonos    = telefonosArr  != null ? Arrays.asList(telefonosArr)  : new ArrayList<>();
        List<String>  correos      = correosArr    != null ? Arrays.asList(correosArr)    : new ArrayList<>();
        List<Integer> materialesIds = new ArrayList<>();
        if (materialesArr != null) {
            for (String m : materialesArr) {
                if (m.matches("\\d+")) materialesIds.add(Integer.parseInt(m));
            }
        }

        // ■■ Validar duplicados de teléfonos para OTRO proveedor ■■
        for (String tel : telefonos) {
            if (tel != null && !tel.trim().isEmpty() && proveedorDAO.existeTelefonoParaOtro(tel, proveedorId)) {
                reenviarFormProveedor(request, response, "El teléfono " + tel + " ya está registrado en otro proveedor.", p, "/Administrador/proveedores/editar.jsp"); 
                return;
            }
        }

        // ■■ Validar duplicados de correos para OTRO proveedor ■■
        for (String correo : correos) {
            if (correo != null && !correo.trim().isEmpty() && proveedorDAO.existeCorreoParaOtroProveedor(correo, proveedorId)) {
                reenviarFormProveedor(request, response, "El correo " + correo + " ya está registrado en otro proveedor.", p, "/Administrador/proveedores/editar.jsp"); 
                return;
            }
        }

        if (proveedorDAO.actualizar(p, telefonos, correos, materialesIds, admin.getId())) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar&msg=actualizado");
        } else {
            reenviarFormProveedor(request, response, "Error al actualizar el proveedor.", p, "/Administrador/proveedores/editar.jsp");
        }
    }

    // ■■ Auxiliar para reenviar con error al formulario ■■
    private void reenviarFormProveedor(HttpServletRequest request, HttpServletResponse response,
            String error, Proveedor p, String vista) throws ServletException, IOException {

        // ■■ Restaurar teléfonos, correos y materiales desde el request ■■
        String[] telefonosArr  = request.getParameterValues("telefono");
        String[] correosArr    = request.getParameterValues("correo");
        String[] materialesArr = request.getParameterValues("materiales");

        List<String> telefonos = new java.util.ArrayList<>();
        if (telefonosArr != null) {
            for (String t : telefonosArr) if (t != null && !t.trim().isEmpty()) telefonos.add(t.trim());
        }
        List<String> correos = new java.util.ArrayList<>();
        if (correosArr != null) {
            for (String c : correosArr) if (c != null && !c.trim().isEmpty()) correos.add(c.trim());
        }
        p.setTelefonos(telefonos);
        p.setCorreos(correos);

        // ■■ Restaurar materiales seleccionados ■■
        List<model.Material> matsSeleccionados = new java.util.ArrayList<>();
        if (materialesArr != null) {
            for (String m : materialesArr) {
                if (m != null && m.matches("\\d+")) {
                    model.Material mat = new model.Material();
                    mat.setMaterialId(Integer.parseInt(m));
                    matsSeleccionados.add(mat);
                }
            }
        }
        p.setMateriales(matsSeleccionados);

        request.setAttribute("error",      error);
        request.setAttribute("proveedor",  p);
        request.setAttribute("materiales", materialDAO.listarMateriales());
        request.getRequestDispatcher(vista).forward(request, response);
    }

    private void eliminarProveedorPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Administrador admin = (Administrador) session.getAttribute("admin");
        String idStr = request.getParameter("id");
        if (idStr != null && idStr.matches("\\d+")) {
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
        
        // ✅ Manejo seguro de fechaInicio
        String fechaInicio = request.getParameter("fechaInicio");
        p.setFechaInicio((fechaInicio != null && !fechaInicio.isEmpty()) ? fechaInicio : null);
        
        String minimoStr = request.getParameter("minimoCompra");
        p.setMinimoCompra(minimoStr != null && !minimoStr.isEmpty() ? Double.parseDouble(minimoStr) : 0.0);
        
        // ✅ Estado: por defecto activo si no se envía
        String estadoParam = request.getParameter("estado");
        p.setEstado(estadoParam == null || "activo".equalsIgnoreCase(estadoParam));
        
        return p;
    }

    private String validarProveedor(Proveedor p, boolean esNuevo) {
        if (p.getNombre()     == null || p.getNombre().trim().isEmpty())     return "El nombre es obligatorio.";
        if (p.getDocumento()  == null || p.getDocumento().trim().isEmpty())  return "El documento es obligatorio.";
        if (p.getFechaInicio()== null || p.getFechaInicio().isEmpty())       return "La fecha de inicio es obligatoria.";
        if (esNuevo && proveedorDAO.existeDocumento(p.getDocumento()))       return "Ya existe un proveedor con ese documento.";
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
        int proveedorId   = Integer.parseInt(idStr);
        Proveedor proveedor = proveedorDAO.obtenerPorId(proveedorId);
        if (proveedor == null) {
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar");
            return;
        }
        List<Compra> compras = compraDAO.listarPorProveedor(proveedorId);
        java.math.BigDecimal totalGasto = java.math.BigDecimal.ZERO;
        int totalProductos = 0;
        for (Compra c : compras) {
            if (c.getTotal() != null) totalGasto = totalGasto.add(c.getTotal());
            if (c.getDetalles() != null) {
                for (model.DetalleCompra d : c.getDetalles()) totalProductos += d.getCantidad();
            }
        }
        request.setAttribute("proveedor",      proveedor);
        request.setAttribute("listaCompras",   compras);
        request.setAttribute("totalCompras",   compras.size());
        request.setAttribute("totalProductos", totalProductos);
        request.setAttribute("totalGasto",     totalGasto);
        request.getRequestDispatcher("/Administrador/proveedores/compras.jsp").forward(request, response);
    }

    private void mostrarFormularioCompra(HttpServletRequest request, HttpServletResponse response) throws Exception {
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

        List<Categoria>  categorias  = categoriaDAO.listarCategorias();
        List<MetodoPago> metodosPago = metodoPagoDAO.listarTodos();

        request.setAttribute("proveedor",   proveedor);
        request.setAttribute("proveedorId", String.valueOf(proveedorId));
        request.setAttribute("categorias",  categorias);
        request.setAttribute("metodosPago", metodosPago);

        request.getRequestDispatcher("/Administrador/proveedores/agregar_compra.jsp")
               .forward(request, response);
    }
}