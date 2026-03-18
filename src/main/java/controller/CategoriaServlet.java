package controller;

import dao.CategoriaDAO;
import dao.MaterialDAO;
import dao.MetodoPagoDAO;
import dao.ProductoDAO;
import dao.SubcategoriaDAO;
import model.Categoria;
import model.Producto;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/CategoriaServlet")
// @MultipartConfig es necesario para recibir archivos en formularios (enctype="multipart/form-data").
// Define límites de tamaño para evitar que suban archivos demasiado grandes:
//   fileSizeThreshold: archivos menores a 2MB se guardan en memoria, los mayores van a disco temporalmente.
//   maxFileSize: tamaño máximo por archivo (10MB).
//   maxRequestSize: tamaño máximo de toda la petición (50MB).
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize       = 1024 * 1024 * 10,
    maxRequestSize    = 1024 * 1024 * 50
)
public class CategoriaServlet extends HttpServlet {

    private CategoriaDAO    categoriaDAO;
    private ProductoDAO     productoDAO;
    private SubcategoriaDAO subcategoriaDAO;
    private MaterialDAO     materialDAO;
    private MetodoPagoDAO   metodoPagoDAO;

    // init() se ejecuta una sola vez cuando el servidor carga el servlet.
    // Se instancian los DAOs aquí para no crearlos en cada petición.
    @Override
    public void init() {
        categoriaDAO    = new CategoriaDAO();
        productoDAO     = new ProductoDAO();
        subcategoriaDAO = new SubcategoriaDAO();
        materialDAO     = new MaterialDAO();
        metodoPagoDAO   = new MetodoPagoDAO();
    }

    // Carga todas las listas necesarias para org-categorias.jsp (gestión del catálogo completo).
    // Se llama tanto al cargar la página normalmente como al volver de un error.
    private void cargarListasGestion(HttpServletRequest request) throws Exception {
        request.setAttribute("categorias",    categoriaDAO.listarCategorias());
        request.setAttribute("subcategorias", subcategoriaDAO.listarTodas());
        request.setAttribute("materiales",    materialDAO.listarMateriales());
        request.setAttribute("metodosPago",   metodoPagoDAO.listarTodos());
    }

    // Carga solo materiales y subcategorías, usados en los selects de filtro de categoria.jsp.
    private void cargarFiltros(HttpServletRequest request) throws Exception {
        request.setAttribute("materiales",    materialDAO.listarMateriales());
        request.setAttribute("subcategorias", subcategoriaDAO.listarTodas());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Seguridad: verifica que haya una sesión activa con un admin autenticado.
        // getSession(false) no crea sesión nueva si no existe, evitando consumo innecesario de memoria.
        // Si no hay sesión válida, sendRedirect envía al navegador a la página de login y corta el flujo.
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
            return;
        }

        // emptyToNull convierte cadenas vacías o solo espacios en null,
        // simplificando las condiciones que vienen a continuación.
        String idStr  = emptyToNull(request.getParameter("id"));
        String query  = emptyToNull(request.getParameter("q"));
        String filtro = request.getParameter("filtro");
        // isBlank() es similar a isEmpty() pero también detecta cadenas con solo espacios.
        if (filtro == null || filtro.isBlank()) filtro = "todos";

        try {
            // CASO 1: búsqueda global sin estar dentro de ninguna categoría específica.
            // Llega cuando el usuario escribe en el buscador desde la vista de categorías.
            if (query != null && idStr == null) {
                request.setAttribute("productos",       productoDAO.buscarGlobal(query, filtro));
                request.setAttribute("categoria",       null);
                request.setAttribute("terminoBusqueda", query);
                request.setAttribute("filtroActivo",    filtro);
                cargarFiltros(request);
                // forward transfiere la petición al JSP sin cambiar la URL en el navegador.
                // Los atributos puestos con setAttribute son accesibles en el JSP via request.getAttribute().
                forward(request, response, "/Administrador/accesorios/categoria.jsp");
                return;
            }

            // CASO 2: búsqueda dentro de una categoría específica.
            // matches("\\d+") valida que el parámetro id sea un número entero positivo
            // para evitar errores al convertirlo con parseInt.
            if (query != null && idStr != null && idStr.matches("\\d+")) {
                int catId = Integer.parseInt(idStr);
                Categoria categoria = categoriaDAO.obtenerPorId(catId);
                // Si el ID no corresponde a ninguna categoría existente, se redirige al listado general.
                if (categoria == null) {
                    response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
                    return;
                }
                request.setAttribute("productos",       productoDAO.buscarEnCategoria(catId, query, filtro));
                request.setAttribute("categoria",       categoria);
                request.setAttribute("terminoBusqueda", query);
                request.setAttribute("filtroActivo",    filtro);
                cargarFiltros(request);
                forward(request, response, "/Administrador/accesorios/categoria.jsp");
                return;
            }

            // CASO 3: ver todos los productos de una categoría, sin término de búsqueda.
            if (idStr != null && idStr.matches("\\d+")) {
                int catId = Integer.parseInt(idStr);
                Categoria categoria = categoriaDAO.obtenerPorId(catId);
                if (categoria == null) {
                    response.sendRedirect(request.getContextPath() + "/CategoriaServlet");
                    return;
                }
                request.setAttribute("productos", productoDAO.listarPorCategoria(catId));
                request.setAttribute("categoria", categoria);
                cargarFiltros(request);
                forward(request, response, "/Administrador/accesorios/categoria.jsp");
                return;
            }

            // CASO 4: listado general de categorías (página principal del módulo).
            cargarListasGestion(request);
            forward(request, response, "/Administrador/org-categorias.jsp");

        } catch (Exception e) {
            e.printStackTrace();
            // Se pasa el mensaje de error como atributo para que el JSP lo muestre al usuario.
            request.setAttribute("error", "Ocurrió un error al cargar la página. Intenta de nuevo.");
            try { cargarListasGestion(request); } catch (Exception ignored) {}
            forward(request, response, "/Administrador/org-categorias.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Misma verificación de sesión que en doGet. Se repite porque doPost es un punto
        // de entrada independiente y también debe estar protegido.
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
            return;
        }

        // El parámetro "action" determina qué operación ejecutar.
        // Los formularios del JSP envían este campo oculto con el valor correspondiente.
        String action = request.getParameter("action");
        try {
            if      ("guardar".equals(action))    guardarCategoria(request, response);
            else if ("actualizar".equals(action)) actualizarCategoria(request, response);
            else if ("eliminar".equals(action))   eliminarCategoria(request, response);
            else response.sendRedirect(request.getContextPath() + "/CategoriaServlet?tab=categorias");
        } catch (Exception e) {
            e.printStackTrace();
            // El error se pasa al JSP como atributo de request (no como parámetro URL)
            // porque usamos forward, que mantiene la misma petición.
            request.setAttribute("error", e.getMessage());
            try { cargarListasGestion(request); } catch (Exception ignored) {}
            forward(request, response, "/Administrador/org-categorias.jsp");
        }
    }

    /**
     * Crea una nueva categoría con su icono.
     *
     * request.getPart("archivoIcono") recupera el archivo subido del formulario multipart.
     * getSubmittedFileName() obtiene el nombre original del archivo desde el cliente.
     * filePart.write() guarda físicamente el archivo en la carpeta de iconos del servidor.
     *
     * Después de guardar, se usa sendRedirect (en lugar de forward) para evitar que
     * al refrescar la página se reenvíe el formulario. El parámetro msg=creado en la URL
     * le indica al JSP qué mensaje de éxito mostrar.
     */
    private void guardarCategoria(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String nombre = request.getParameter("nombre");
        javax.servlet.http.Part filePart = request.getPart("archivoIcono");
        String fileName = filePart.getSubmittedFileName();
        String uploadPath = getServletContext().getRealPath("") + "assets/Imagenes/iconos";
        java.io.File uploadDir = new java.io.File(uploadPath);
        if (!uploadDir.exists()) uploadDir.mkdirs();
        filePart.write(uploadPath + java.io.File.separator + fileName);
        Categoria c = new Categoria();
        c.setNombre(nombre.trim());
        c.setIcono(fileName);
        categoriaDAO.guardar(c);
        // msg=creado y tab=categorias son leídos por org-categorias.jsp para mostrar
        // el mensaje de éxito correcto y abrir el tab correspondiente.
        response.sendRedirect(request.getContextPath() + "/CategoriaServlet?msg=creado&tab=categorias");
    }

    /**
     * Actualiza el nombre e icono de una categoría existente.
     * Validación: si no llegan id o nombre, se lanza excepción con mensaje claro.
     * Integer.parseInt(idStr) convierte el String del parámetro a int; si no es un número
     * válido lanzaría NumberFormatException, que el catch del doPost captura.
     */
    private void actualizarCategoria(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String idStr  = request.getParameter("id");
        String nombre = request.getParameter("nombre");
        String icono  = request.getParameter("icono");
        if (idStr == null || nombre == null || nombre.trim().isEmpty()) throw new Exception("Datos inválidos.");
        Categoria c = new Categoria();
        c.setCategoriaId(Integer.parseInt(idStr));
        c.setNombre(nombre.trim());
        // Si no viene icono (campo vacío), se asigna la imagen por defecto
        c.setIcono(icono != null ? icono.trim() : "default.png");
        categoriaDAO.actualizar(c);
        response.sendRedirect(request.getContextPath() + "/CategoriaServlet?msg=actualizado&tab=categorias");
    }

    /**
     * Elimina una categoría. El DAO valida internamente que no tenga productos activos
     * antes de ejecutar el DELETE; si los tiene, lanza excepción con mensaje descriptivo.
     */
    private void eliminarCategoria(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String idStr = request.getParameter("id");
        if (idStr == null) throw new Exception("ID no proporcionado.");
        categoriaDAO.eliminar(Integer.parseInt(idStr));
        response.sendRedirect(request.getContextPath() + "/CategoriaServlet?msg=eliminado&tab=categorias");
    }

    // Método auxiliar que encapsula el forward para no repetir la misma línea en cada caso.
    // forward transfiere el control al JSP indicado, manteniendo los atributos del request.
    private void forward(HttpServletRequest req, HttpServletResponse res, String path)
            throws ServletException, IOException {
        req.getRequestDispatcher(path).forward(req, res);
    }

    // Convierte un parámetro vacío o con solo espacios en null.
    // Esto simplifica las condiciones: en lugar de verificar (s == null || s.isEmpty()),
    // basta con verificar (s == null).
    private String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
