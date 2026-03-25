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

/**
 * Servlet encargado de gestionar el módulo completo de proveedores en el sistema AAC27.
 *
 * Proporciona funcionalidades para:
 * - Listado, búsqueda y filtrado de proveedores
 * - Registro, edición y eliminación lógica de proveedores
 * - Gestión de estados activos/inactivos
 * - Visualización del historial de compras por proveedor
 * - Registro de nuevas compras asociadas a un proveedor
 *
 * Todas las operaciones requieren autenticación de administrador y validan
 * la integridad de los datos antes de persistir cambios en la base de datos.
 *
 * @see ProveedorDAO
 * @see CompraDAO
 * @see Proveedor
 * @see Compra
 */
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
     * Procesa las solicitudes HTTP GET para el módulo de proveedores.
     *
     * Según el parámetro {@code action}, ejecuta una de las siguientes operaciones:
     * - "listar": muestra el listado completo de proveedores
     * - "verificarDocumento": valida unicidad de documento vía AJAX (JSON)
     * - "buscar": filtra proveedores por término de búsqueda y criterio
     * - "nuevo": muestra el formulario para registrar un nuevo proveedor
     * - "editar": muestra el formulario de edición con datos existentes
     * - "actualizarEstado": cambia el estado activo/inactivo de un proveedor
     * - "confirmarEliminar": muestra vista de confirmación antes de eliminar
     * - "verCompras": muestra el historial de compras de un proveedor específico
     * - "nuevaCompra": prepara el formulario para registrar una nueva compra
     *
     * Si no se especifica una acción válida o ocurre un error, redirige al panel principal.
     *
     * @param request  la petición HTTP que contiene el parámetro de acción y filtros
     * @param response la respuesta HTTP utilizada para forwards o redirecciones
     * @throws ServletException si ocurre un error durante el procesamiento del servlet
     * @throws IOException      si ocurre un error de entrada/salida al enviar la respuesta
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
     * Procesa las solicitudes HTTP POST para ejecutar operaciones de modificación sobre proveedores.
     *
     * Según el valor del parámetro {@code action}, ejecuta una de las siguientes operaciones:
     * - "guardar": registra un nuevo proveedor validando datos obligatorios y unicidad
     * - "actualizar": modifica un proveedor existente preservando campos inmutables
     * - "actualizarEstado": cambia el estado activo/inactivo desde el listado
     *
     * En caso de éxito, redirige con parámetros de confirmación para evitar reenvíos.
     * En caso de error, reenvía al listado con el mensaje de error para visualización.
     *
     * @param request  la petición HTTP con {@code action} y datos del formulario
     * @param response la respuesta HTTP utilizada para redirecciones o forwards con errores
     * @throws ServletException si ocurre un error durante el procesamiento del servlet
     * @throws IOException      si ocurre un error de entrada/salida al enviar la respuesta
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
     * Carga el listado completo de proveedores y prepara los atributos para la vista.
     *
     * Calcula estadísticas como total de proveedores y cantidad de activos.
     * Carga la lista de materiales disponibles para los filtros del JSP.
     * Si existe el parámetro {@code msg} en la URL, lo reenvía como atributo
     * para que la vista pueda mostrar alertas de confirmación tras operaciones exitosas.
     *
     * @param request  la petición HTTP que puede contener el parámetro {@code msg}
     * @param response la respuesta HTTP utilizada para forwards a la vista JSP
     * @throws ServletException si ocurre un error al despachar la vista
     * @throws IOException      si ocurre un error de entrada/salida
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
     * Verifica si un documento de identidad ya existe registrado en la base de datos.
     *
     * Este endpoint es consumido vía AJAX desde el formulario de registro/edición
     * para validar en tiempo real la unicidad del documento. Si se proporciona
     * un {@code idActual} válido (edición), excluye al proveedor actual de la validación.
     *
     * @param request  la petición HTTP que contiene {@code documento} y opcionalmente {@code idActual}
     * @param response la respuesta HTTP configurada para devolver JSON con el resultado de la validación
     * @throws IOException si ocurre un error al escribir la respuesta JSON
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
     * Ejecuta una búsqueda de proveedores según el término y criterio de filtro proporcionados.
     *
     * Si el término de búsqueda está vacío, devuelve el listado completo sin filtrar.
     * Los resultados, el término activo y el filtro aplicado se pasan a la vista
     * para mantener el estado de la búsqueda en la interfaz.
     *
     * @param request  la petición HTTP que contiene los parámetros {@code q} y {@code filtro}
     * @param response la respuesta HTTP utilizada para forwards a la vista de listado
     * @throws ServletException si ocurre un error al despachar la vista
     * @throws IOException      si ocurre un error de entrada/salida
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
     * Prepara y muestra el formulario para registrar un nuevo proveedor.
     *
     * Carga la lista completa de materiales disponibles para populatear
     * los checkboxes de selección múltiple en el formulario.
     *
     * @param request  la petición HTTP recibida
     * @param response la respuesta HTTP utilizada para forwards al JSP del formulario
     * @throws ServletException si ocurre un error al despachar la vista
     * @throws IOException      si ocurre un error de entrada/salida
     */
    private void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("materiales", materialDAO.listarMateriales());
        request.getRequestDispatcher("/Administrador/proveedores/agregar.jsp").forward(request, response);
    }

    /**
     * Prepara y muestra el formulario de edición con los datos del proveedor seleccionado.
     *
     * Valida que el ID recibido sea numérico antes de consultar la base de datos.
     * Si el proveedor no existe o el ID es inválido, redirige al listado para evitar errores.
     *
     * @param request  la petición HTTP que contiene el parámetro {@code id} del proveedor
     * @param response la respuesta HTTP utilizada para forwards o redirecciones según validación
     * @throws ServletException si ocurre un error al despachar la vista
     * @throws IOException      si ocurre un error de entrada/salida
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
     * Actualiza el estado activo/inactivo de un proveedor registrado.
     *
     * Valida que el ID y el nuevo estado sean proporcionados y que el ID sea numérico.
     * Tras actualizar, recarga el listado completo para reflejar los cambios en la interfaz.
     *
     * @param request  la petición HTTP que contiene {@code id} y {@code estado}
     * @param response la respuesta HTTP utilizada para forwards al listado actualizado
     * @throws IOException      si ocurre un error de entrada/salida
     * @throws ServletException si ocurre un error al procesar la vista
     */
    private void actualizarEstado(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String idStr     = request.getParameter("id");
        String estadoStr = request.getParameter("estado");
        if (idStr != null && estadoStr != null && idStr.matches("\\d+")) {
            proveedorDAO.actualizarEstado(Integer.parseInt(idStr), Boolean.parseBoolean(estadoStr));
        }
        listarProveedores(request, response);
    }
    // ==================== MÉTODOS POST ====================

    /**
     * Procesa el registro de un nuevo proveedor con validación exhaustiva de datos.
     *
     * Flujo de validación:
     * 1. Construye el objeto Proveedor desde los parámetros del formulario
     * 2. Valida campos obligatorios y unicidad del documento
     * 3. Verifica que teléfonos y correos no estén registrados en otros proveedores
     * 4. Si todo es válido, persiste en base de datos y redirige con confirmación
     * 5. Si hay error, reenvía al formulario conservando los datos ingresados
     *
     * Implementa el patrón POST-Redirect-GET para evitar reenvíos accidentales.
     *
     * @param request  la petición HTTP que contiene los datos del formulario de nuevo proveedor
     * @param response la respuesta HTTP utilizada para redirecciones o forwards con errores
     * @throws ServletException si ocurre un error durante el procesamiento
     * @throws IOException      si ocurre un error de entrada/salida
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

        



        // Validar campos obligatorios y unicidad del documento
        String error = validarProveedor(p, true);
        if (error != null) {

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

                // sendRedirect evita que al refrescar el navegador se reenvíe el formulario (patrón POST-Redirect-GET)
                // El parámetro msg=creado es recibido por el listado para mostrar la alerta de éxito
                response.sendRedirect(request.getContextPath() + "/ProveedorServlet?action=listar&msg=creado");
            } else {

                reenviarFormProveedor(request, response, "Error: El documento ya existe o hubo un fallo en la base de datos.", p, "/Administrador/proveedores/agregar.jsp");
            }
        } catch (Exception e) {

            e.printStackTrace();
            reenviarFormProveedor(request, response, "Error interno: " + e.getMessage(), p, "/Administrador/proveedores/agregar.jsp");
        }
    }

    /**
     * Procesa la actualización de un proveedor existente preservando campos inmutables.
     *
     * Los campos nombre, documento y fechaInicio se toman del registro original
     * en base de datos para prevenir modificaciones maliciosas vía manipulación del HTML.
     *
     * La validación de teléfonos y correos excluye al proveedor actual mediante
     * métodos "ParaOtro", permitiendo conservar sus propios datos de contacto.
     *
     * @param request  la petición HTTP que contiene los datos actualizados del proveedor
     * @param response la respuesta HTTP utilizada para redirecciones o forwards con errores
     * @throws ServletException si ocurre un error durante el procesamiento
     * @throws IOException      si ocurre un error de entrada/salida
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
     * Reenvía al usuario al formulario con los datos ingresados y un mensaje de error.
     *
     * Reconstruye las listas de teléfonos, correos y materiales seleccionados desde
     * los parámetros del request para pre-llenar los campos del formulario.
     * Esto mejora la experiencia de usuario al evitar tener que volver a ingresar
     * toda la información cuando ocurre un error de validación.
     *
     * @param request   la petición HTTP que contiene los datos del formulario original
     * @param response  la respuesta HTTP utilizada para forwards a la vista del formulario
     * @param error     el mensaje de error a mostrar al usuario
     * @param p         el objeto Proveedor con los datos ingresados para pre-llenar campos
     * @param vista     la ruta del JSP al cual se transferirá el control
     * @throws ServletException si ocurre un error al despachar la vista
     * @throws IOException      si ocurre un error de entrada/salida
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
     * Ejecuta la eliminación lógica de un proveedor marcándolo como inactivo.
     *
     * La eliminación es lógica (no física) para preservar la integridad histórica
     * de compras y otros registros relacionados. Requiere autenticación de administrador
     * para registrar quién realizó la acción.
     *
     * @param request  la petición HTTP que contiene el parámetro {@code id} del proveedor
     * @param response la respuesta HTTP utilizada para redirecciones con confirmación
     * @throws ServletException si ocurre un error durante el procesamiento
     * @throws IOException      si ocurre un error de entrada/salida
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
     * Construye un objeto Proveedor populated con los datos recibidos desde el formulario.
     *
     * Maneja valores opcionales y convierte tipos de datos:
     * - fechaInicio: null si está vacía para evitar strings vacíos en la BD
     * - minimoCompra: 0.0 por defecto si el valor no es numérico o está vacío
     * - estado: true por defecto si no se especifica o es "activo"
     *
     * @param request la petición HTTP que contiene los parámetros del formulario
     * @return un objeto Proveedor con los datos convertidos y validados básicos
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
     * Valida que los campos obligatorios de un proveedor estén presentes y sean válidos.
     *
     * Si {@code esNuevo} es true, también verifica que el documento no esté
     * ya registrado en la base de datos para garantizar unicidad.
     *
     * @param p      el objeto Proveedor a validar
     * @param esNuevo indica si se trata de un registro nuevo (requiere validación de unicidad)
     * @return el mensaje de error descriptivo si la validación falla, o null si todo es válido
     */
    private String validarProveedor(Proveedor p, boolean esNuevo) {
        if (p.getNombre()     == null || p.getNombre().trim().isEmpty())     return "El nombre es obligatorio.";
        if (p.getDocumento()  == null || p.getDocumento().trim().isEmpty())  return "El documento es obligatorio.";
        if (p.getFechaInicio()== null || p.getFechaInicio().isEmpty())       return "La fecha de inicio es obligatoria.";
        if (esNuevo && proveedorDAO.existeDocumento(p.getDocumento()))       return "Ya existe un proveedor con ese documento.";
        return null;
    }

    /**
     * Verifica que exista una sesión activa con un administrador autenticado.
     *
     * Si no hay sesión válida, redirige al usuario a la página de inicio de sesión
     * y retorna false para que el método llamador pueda detener su ejecución.
     *
     * @param request  la petición HTTP para acceder a la sesión
     * @param response la respuesta HTTP utilizada para redirigir si no hay autenticación
     * @return true si la sesión contiene un administrador válido, false en caso contrario
     * @throws IOException si ocurre un error al realizar la redirección
     */
    private boolean estaAutenticado(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getSession().getAttribute("admin") == null) {
            response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
            return false;
        }
        return true;
    }

    /**
     * Carga y muestra el historial completo de compras de un proveedor específico.
     *
     * Calcula estadísticas agregadas:
     * - totalGasto: suma de los totales de todas las compras
     * - totalProductos: suma de cantidades de todos los detalles de compra
     *
     * @param request  la petición HTTP que contiene el parámetro {@code id} del proveedor
     * @param response la respuesta HTTP utilizada para forwards a la vista de historial
     * @throws Exception si ocurre un error al consultar la base de datos o calcular estadísticas
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
     * Prepara y muestra el formulario para registrar una nueva compra a un proveedor.
     *
     * Carga las listas auxiliares necesarias:
     * - categorías de productos para filtrar en el selector dinámico
     * - métodos de pago disponibles para la transacción
     *
     * @param request  la petición HTTP que contiene el parámetro {@code id} del proveedor
     * @param response la respuesta HTTP utilizada para forwards o redirecciones según validación
     * @throws Exception si ocurre un error al consultar las listas auxiliares desde los DAO
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