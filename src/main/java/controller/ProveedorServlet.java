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

    // Se instancian los DAOs una sola vez al iniciar el servlet, no en cada petición
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

    /**
     * Maneja todas las peticiones GET del módulo de proveedores.
     * El parámetro "action" determina qué operación ejecutar.
     * Si no se envía action, se muestra el listado por defecto.
     * Antes de cualquier acción se verifica que el admin tenga sesión activa.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        // Seguridad: si no hay sesión de admin, redirige al login antes de procesar
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

    /**
     * Maneja todas las peticiones POST: guardar, actualizar y eliminar proveedores,
     * y también el cambio de estado desde el listado.
     * Cualquier excepción no controlada redirige al listado sin perder el flujo.
     */
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

    /**
     * Carga la lista completa de proveedores y los pasa al JSP via atributos de request.
     * El parámetro "msg" llega desde un sendRedirect después de una operación exitosa
     * (ej: ?msg=creado) y se reenvía como atributo para que el JSP lo muestre con SweetAlert.
     */
    private void listarProveedores(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Proveedor> proveedores = proveedorDAO.listarProveedores();
        String msg = request.getParameter("msg");
        if (msg != null) request.setAttribute("msg", msg);
        request.setAttribute("proveedores",      proveedores);
        request.setAttribute("totalProveedores", proveedores.size());
        request.setAttribute("activos",          proveedores.stream().filter(Proveedor::isEstado).count());
        request.setAttribute("filtroActivo",     "todos");
        // Se cargan todos los materiales para el selector del filtro del JSP
        request.setAttribute("todosMateriales",  materialDAO.listarMateriales());
        // forward mantiene los atributos del request; sendRedirect los perdería
        request.getRequestDispatcher("/Administrador/proveedores.jsp").forward(request, response);
    }

    /**
     * Responde en JSON si un documento ya existe en la base de datos.
     * Se llama desde JavaScript (AJAX) al escribir el documento en el formulario.
     *
     * Si viene un idActual válido (edición), verifica solo contra OTROS proveedores.
     * Si no viene (nuevo proveedor), verifica contra todos.
     *
     * matches("\\d+") valida que el ID sea un número entero positivo antes de usarlo.
     * La respuesta tiene el formato: {"existe": true} o {"existe": false}
     */
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

    /**
     * Ejecuta una búsqueda de proveedores según el término y el tipo de filtro enviados.
     * Si el término está vacío, devuelve todos los proveedores sin filtrar.
     * Los resultados y la búsqueda activa se pasan al mismo JSP de listado.
     */
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
        // Se cargan TODOS los materiales (no solo los de los resultados) para que el selector no quede incompleto
        request.setAttribute("todosMateriales",  materialDAO.listarMateriales());
        request.getRequestDispatcher("/Administrador/proveedores.jsp").forward(request, response);
    }

    /**
     * Prepara el formulario para agregar un nuevo proveedor.
     * Carga la lista de materiales disponibles para los checkboxes del formulario.
     */
    private void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("materiales", materialDAO.listarMateriales());
        request.getRequestDispatcher("/Administrador/proveedores/agregar.jsp").forward(request, response);
    }

    /**
     * Prepara el formulario de edición cargando los datos del proveedor a editar.
     *
     * matches("\\d+") valida que el ID recibido sea numérico antes de hacer la consulta,
     * evitando errores o inyecciones si alguien manipula la URL manualmente.
     * Si el proveedor no existe, redirige al listado.
     */
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

    /**
     * Cambia el estado activo/inactivo de un proveedor.
     * Valida que el ID sea numérico antes de procesar.
     * Después del cambio recarga el listado para reflejar el nuevo estado.
     */
    private void actualizarEstado(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String idStr     = request.getParameter("id");
        String estadoStr = request.getParameter("estado");
        if (idStr != null && estadoStr != null && idStr.matches("\\d+")) {
            proveedorDAO.actualizarEstado(Integer.parseInt(idStr), Boolean.parseBoolean(estadoStr));
        }
        listarProveedores(request, response);
    }

    /**
     * Carga los datos del proveedor y hace forward a la página de confirmación de eliminación.
     * Si el ID no es válido o el proveedor no existe, redirige al listado de forma segura.
     */
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

    /**
     * Procesa el formulario de registro de un nuevo proveedor.
     *
     * Flujo:
     * 1. Lee los datos del formulario (campos individuales y listas de teléfonos/correos/materiales).
     * 2. Valida los campos obligatorios y duplicados de documento.
     * 3. Verifica que ningún teléfono ni correo ya exista en otro proveedor.
     * 4. Si todo es válido, guarda en base de datos y redirige al listado con msg=creado.
     * 5. Si hay error, reenvía el formulario conservando los datos ingresados.
     *
     * getParameterValues devuelve un array porque el formulario puede enviar
     * múltiples campos con el mismo nombre (ej: varios inputs name="telefono").
     *
     * matches("\\d+") filtra los IDs de materiales para asegurarse de que son números válidos.
     */
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
                // Solo se procesan valores numéricos para evitar datos corruptos
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

        // Validar campos obligatorios y unicidad del documento
        String error = validarProveedor(p, true);
        if (error != null) {
            System.err.println("❌ Validación fallida: " + error);
            reenviarFormProveedor(request, response, error, p, "/Administrador/proveedores/agregar.jsp"); 
            return;
        }

        // Verificar que ningún teléfono enviado ya pertenezca a otro proveedor
        for (String tel : telefonos) {
            if (tel != null && !tel.trim().isEmpty() && proveedorDAO.existeTelefonoProveedor(tel)) {
                reenviarFormProveedor(request, response, "El teléfono " + tel + " ya está registrado en otro proveedor.", p, "/Administrador/proveedores/agregar.jsp"); 
                return;
            }
        }

        // Verificar que ningún correo enviado ya pertenezca a otro proveedor
        for (String correo : correos) {
            if (correo != null && !correo.trim().isEmpty() && proveedorDAO.existeCorreoProveedor(correo)) {
                reenviarFormProveedor(request, response, "El correo " + correo + " ya está registrado en otro proveedor.", p, "/Administrador/proveedores/agregar.jsp"); 
                return;
            }
        }

        try {
            if (proveedorDAO.guardar(p, telefonos, correos, materialesIds, admin.getId())) {
                System.out.println("✅ Proveedor guardado exitosamente");
                // sendRedirect evita que al refrescar el navegador se reenvíe el formulario (patrón POST-Redirect-GET)
                // El parámetro msg=creado es recibido por el listado para mostrar la alerta de éxito
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

    /**
     * Procesa el formulario de edición de un proveedor existente.
     *
     * IMPORTANTE: nombre, documento y fechaInicio se toman del registro original
     * en base de datos (no del formulario), porque son campos inmutables (RF11).
     * Esto evita que alguien los modifique manipulando el HTML.
     *
     * La validación de teléfonos y correos duplicados excluye al proveedor actual
     * usando los métodos "ParaOtro", lo que permite conservar sus propios datos.
     *
     * Si la actualización es exitosa, redirige al listado con msg=actualizado.
     */
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

        // Tomar nombre, documento y fechaInicio del registro original para no permitir su modificación
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

        // Validar que ningún teléfono pertenezca a OTRO proveedor diferente al actual
        for (String tel : telefonos) {
            if (tel != null && !tel.trim().isEmpty() && proveedorDAO.existeTelefonoParaOtro(tel, proveedorId)) {
                reenviarFormProveedor(request, response, "El teléfono " + tel + " ya está registrado en otro proveedor.", p, "/Administrador/proveedores/editar.jsp"); 
                return;
            }
        }

        // Validar que ningún correo pertenezca a OTRO proveedor diferente al actual
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

    /**
     * Reenvía el formulario con los datos que el usuario ya ingresó más el mensaje de error.
     * Esto evita que el usuario tenga que volver a llenar todo el formulario cuando hay un error.
     *
     * Reconstruye las listas de teléfonos y correos desde el request para pre-llenar los campos.
     * Para los materiales, crea objetos Material con solo el ID para que el JSP pueda marcar
     * los checkboxes correctos como seleccionados.
     *
     * forward (no redirect) es necesario aquí porque se necesita pasar atributos al JSP;
     * con sendRedirect los atributos del request se perderían.
     */
    private void reenviarFormProveedor(HttpServletRequest request, HttpServletResponse response,
            String error, Proveedor p, String vista) throws ServletException, IOException {

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

        // Reconstruir los materiales seleccionados para que el JSP pueda marcar los checkboxes
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

    /**
     * Ejecuta la eliminación lógica del proveedor (lo marca como inactivo).
     * Si el ID no es válido o la operación falla, redirige al listado sin mensaje.
     * Si tiene éxito, redirige con msg=eliminado para mostrar la confirmación al usuario.
     */
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

    /**
     * Construye un objeto Proveedor con los datos enviados desde el formulario.
     *
     * fechaInicio: si viene vacía se guarda como null para que la BD no reciba un string vacío.
     * minimoCompra: se convierte de String a Double; si está vacío se asigna 0.0 por defecto.
     * estado: el formulario envía "activo" o nada; si no viene se asume activo por defecto.
     */
    private Proveedor construirProveedorDesdeRequest(HttpServletRequest request) {
        Proveedor p = new Proveedor();
        p.setNombre(request.getParameter("nombre"));
        p.setDocumento(request.getParameter("documento"));
        
        String fechaInicio = request.getParameter("fechaInicio");
        p.setFechaInicio((fechaInicio != null && !fechaInicio.isEmpty()) ? fechaInicio : null);
        
        String minimoStr = request.getParameter("minimoCompra");
        p.setMinimoCompra(minimoStr != null && !minimoStr.isEmpty() ? Double.parseDouble(minimoStr) : 0.0);
        
        String estadoParam = request.getParameter("estado");
        p.setEstado(estadoParam == null || "activo".equalsIgnoreCase(estadoParam));
        
        return p;
    }

    /**
     * Valida que los campos obligatorios del proveedor estén presentes.
     * Si esNuevo es true, también verifica que el documento no esté ya registrado.
     * Devuelve el mensaje de error como String, o null si todo está bien.
     */
    private String validarProveedor(Proveedor p, boolean esNuevo) {
        if (p.getNombre()     == null || p.getNombre().trim().isEmpty())     return "El nombre es obligatorio.";
        if (p.getDocumento()  == null || p.getDocumento().trim().isEmpty())  return "El documento es obligatorio.";
        if (p.getFechaInicio()== null || p.getFechaInicio().isEmpty())       return "La fecha de inicio es obligatoria.";
        if (esNuevo && proveedorDAO.existeDocumento(p.getDocumento()))       return "Ya existe un proveedor con ese documento.";
        return null;
    }

    /**
     * Verifica que haya una sesión de administrador activa.
     * sendRedirect envía al usuario al login sin mostrar ningún contenido protegido.
     * Devuelve false para que el método que lo llama pueda detener su ejecución con return.
     */
    private boolean estaAutenticado(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getSession().getAttribute("admin") == null) {
            response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
            return false;
        }
        return true;
    }

    /**
     * Carga el historial de compras de un proveedor específico con sus estadísticas.
     * Calcula el total gastado sumando los totales de cada compra,
     * y el total de productos sumando las cantidades de cada detalle.
     */
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

    /**
     * Prepara el formulario para registrar una nueva compra a un proveedor.
     * Carga las categorías de productos y los métodos de pago disponibles.
     * Si el proveedor no existe, redirige al listado de forma segura.
     */
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