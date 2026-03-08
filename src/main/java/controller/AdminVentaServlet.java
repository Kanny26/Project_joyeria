package controller;

import dao.PostventaDAO;
import dao.VentaDAO;
import model.Administrador;
import model.CasoPostventa;
import model.Venta;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet principal para el módulo de administración de ventas y postventas.
 * Maneja las rutas GET/POST para listar, buscar, ver detalles y actualizar estados.
 */
@WebServlet(urlPatterns = {
    "/Administrador/ventas/listar", 
    "/Administrador/ventas/ver", 
    "/Administrador/ventas/buscar",
    "/Administrador/postventa/listar", 
    "/Administrador/postventa/ver", 
    "/Administrador/postventa/estado",
    "/Administrador/postventa/gestionar"
})
public class AdminVentaServlet extends HttpServlet {

    private VentaDAO ventaDAO;
    private PostventaDAO postventaDAO;

    // Inicializa los DAOs una sola vez cuando se carga el servlet
    @Override
    public void init() {
        ventaDAO = new VentaDAO();
        postventaDAO = new PostventaDAO();
        System.out.println("✅ AdminVentaServlet inicializado");
    }

    // Maneja las peticiones GET: listar, ver detalles y búsquedas
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Ruta solicitada: " + req.getServletPath());
        
        if (!validarSesionAdmin(req, resp)) return;
        String ruta = req.getServletPath();

        try {
            // Caso: Listar todas las ventas con estadísticas
            switch (ruta) {
                case "/Administrador/ventas/listar": {
                    System.out.println("Ejecutando: listar ventas");
                    
                    List<Venta> ventas = ventaDAO.listarVentas();
                    
                    System.out.println("Resultado DAO: ventas.size() = " + (ventas != null ? ventas.size() : "NULL"));
                    
                    req.setAttribute("ventas", ventas != null ? ventas : new ArrayList<>());
                    req.setAttribute("totalVentas", ventas != null ? ventas.size() : 0);
                    req.setAttribute("totalPendientes", ventaDAO.contarPendientes());
                    
                    String vista = "/Administrador/ventas/listar_ventas.jsp";
                    System.out.println("🔄 Forwarding a: " + vista);
                    
                    req.getRequestDispatcher(vista).forward(req, resp);
                    break;
                }

                // Caso: Listar todos los casos de postventa
                case "/Administrador/postventa/listar": {
                    System.out.println("Ejecutando: listar postventa");
                    
                    List<CasoPostventa> casos = postventaDAO.listarTodos();
                    
                    System.out.println("Resultado DAO: casos.size() = " + (casos != null ? casos.size() : "NULL"));
                    
                    req.setAttribute("casos", casos != null ? casos : new ArrayList<>());
                    
                    String vista = "/Administrador/ventas/listar_postventa.jsp";
                    System.out.println("Forwarding a: " + vista);
                    
                    req.getRequestDispatcher(vista).forward(req, resp);
                    break;
                }
                    
                // Caso: Ver detalle de una venta específica por ID
                case "/Administrador/ventas/ver": {
                    int id = parseId(req.getParameter("id"));
                    System.out.println("Buscando venta ID: " + id);
                    
                    Venta v = ventaDAO.obtenerPorId(id);
                    if (v == null) {
                        System.out.println("Venta no encontrada");
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Venta no encontrada");
                        return;
                    }
                    req.setAttribute("venta", v);
                    req.getRequestDispatcher("/Administrador/ventas/ver_venta.jsp").forward(req, resp);
                    break;
                }
                
                // Caso: Búsqueda avanzada de ventas por criterio, tipo y rango de fechas
                case "/Administrador/ventas/buscar": {
                    String criterio    = req.getParameter("q");
                    String tipo        = req.getParameter("tipo");
                    String fechaIniStr = req.getParameter("fechaInicio");
                    String fechaFinStr = req.getParameter("fechaFin");
                    
                    System.out.println("Búsqueda: tipo=" + tipo + ", q=" + criterio);
                    
                    java.util.Date fechaInicio = parseFecha(fechaIniStr);
                    java.util.Date fechaFin    = parseFecha(fechaFinStr);
                    
                    List<Venta> resultado = ventaDAO.buscarVentas(criterio, tipo, fechaInicio, fechaFin, 0);
                    
                    System.out.println("Resultados búsqueda: " + (resultado != null ? resultado.size() : "NULL"));
                    
                    req.setAttribute("ventas", resultado != null ? resultado : new java.util.ArrayList<>());
                    req.setAttribute("criterio", criterio);
                    req.setAttribute("tipo", tipo);
                    req.setAttribute("fechaInicio", fechaIniStr);
                    req.setAttribute("fechaFin", fechaFinStr);
                    
                    req.getRequestDispatcher("/Administrador/ventas/listar_ventas.jsp").forward(req, resp);
                    break;
                }
                
                // Caso: Ver detalle de un caso de postventa por ID
                case "/Administrador/postventa/ver": {
                    int casoId = parseId(req.getParameter("id"));
                    System.out.println("Buscando caso postventa ID: " + casoId);
                    
                    CasoPostventa caso = postventaDAO.obtenerPorId(casoId);
                    if (caso == null) {
                        System.out.println("Caso no encontrado");
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Caso no encontrado");
                        return;
                    }
                    req.setAttribute("caso", caso);
                    req.getRequestDispatcher("/Administrador/ventas/ver_caso_postventa.jsp").forward(req, resp);
                    break;
                }
                
                // Ruta no reconocida
                default:
                    System.out.println("Ruta no manejada: " + ruta);
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            // Captura cualquier error inesperado y redirige a página principal con mensaje
            System.err.println("ERROR CRÍTICO en AdminVentaServlet:");
            e.printStackTrace();
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/Administrador/admin-principal.jsp").forward(req, resp);
        }
    }

    // Maneja las peticiones POST: actualmente solo actualiza estados de postventa
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!validarSesionAdmin(req, resp)) return;

        Administrador admin = (Administrador) req.getSession().getAttribute("admin");
        int adminId = admin != null ? admin.getId() : -1;

        String ruta = req.getServletPath();
        try {
            // Procesa la actualización de estado de un caso de postventa
            if ("/Administrador/postventa/estado".equals(ruta) || "/Administrador/postventa/gestionar".equals(ruta)) {
                int casoId = parseId(req.getParameter("casoId"));
                String nuevoEstado = req.getParameter("nuevoEstado") != null 
                    ? req.getParameter("nuevoEstado") 
                    : req.getParameter("estado");
                String observacion = req.getParameter("observacion");

                // Valida que el nuevo estado sea uno de los permitidos
                if (!java.util.Arrays.asList("en_proceso", "aprobado", "cancelado").contains(nuevoEstado)) {
                    reenviarConError(req, resp, "Estado inválido.", "/Administrador/postventa/ver?id=" + casoId);
                    return;
                }

                CasoPostventa caso = postventaDAO.obtenerPorId(casoId);
                if (caso == null) {
                    reenviarConError(req, resp, "Caso no encontrado.", "/Administrador/postventa/ver?id=" + casoId);
                    return;
                }

                // Actualiza el estado en BD y redirige con mensaje de éxito
                postventaDAO.actualizarEstado(casoId, nuevoEstado, observacion, adminId);
                resp.sendRedirect(req.getContextPath() + "/Administrador/postventa/ver?id=" + casoId + "&exito=1");
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            manejarError(req, resp, e);
        }
    }

    // ========== MÉTODOS AUXILIARES ==========
    
    // Verifica que exista sesión activa de administrador, si no, redirige al login
    private boolean validarSesionAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            System.out.println("⚠️ Sesión de admin no válida");
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return false;
        }
        return true;
    }

    // Convierte un parámetro String a entero, retorna -1 si es nulo o no es número válido
    private int parseId(String param) {
        if (param == null || !param.matches("\\d+")) return -1;
        try { return Integer.parseInt(param); } catch (NumberFormatException e) { return -1; }
    }

    // Parsea una fecha en formato yyyy-MM-dd, retorna null si falla o está vacía
    private java.util.Date parseFecha(String s) {
        if (s == null || s.isBlank()) return null;
        try { 
            return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(s); 
        } catch (Exception e) { 
            System.err.println("Error parseando fecha: " + s);
            return null; 
        }
    }

    // Reenvía a una vista JSP con un mensaje de error en el request
    private void reenviarConError(HttpServletRequest req, HttpServletResponse resp, String msg, String vista)
            throws ServletException, IOException {
        req.setAttribute("error", msg);
        req.getRequestDispatcher(vista).forward(req, resp);
    }

    // Manejo centralizado de errores: registra el error y redirige a página principal
    private void manejarError(HttpServletRequest req, HttpServletResponse resp, Exception e)
            throws ServletException, IOException {
        e.printStackTrace();
        req.setAttribute("error", "Error del servidor: " + e.getMessage());
        req.getRequestDispatcher("/Administrador/admin-principal.jsp").forward(req, resp);
    }
}