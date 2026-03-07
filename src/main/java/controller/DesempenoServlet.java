package controller;

import dao.DesempenoDAO;
import model.Desempeno_Vendedor;
import model.Administrador;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@WebServlet("/DesempenoServlet")
public class DesempenoServlet extends HttpServlet {
    
    private DesempenoDAO desempenoDAO;

    @Override
    public void init() {
        desempenoDAO = new DesempenoDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // ■■ RNF03: Validar sesión de administrador ■■
        if (!validarSesionAdmin(request, response)) {
            return;
        }
        
        String accion = request.getParameter("accion");
        if (accion == null) accion = "listar";
        
        try {
            switch (accion) {
                case "listar":
                    listarDesempeno(request, response);
                    break;
                case "calcular":
                    calcularDesempeno(request, response);
                    break;
                case "exportar":
                    exportarReporte(request, response);
                    break;
                default:
                    listarDesempeno(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/Administrador/usuarios/historial.jsp")
                    .forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // ■■ RNF03: Validar sesión de administrador ■■
        if (!validarSesionAdmin(request, response)) {
            return;
        }
        
        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");
        
        try {
            switch (accion) {
                case "actualizar":
                    actualizarDesempeno(request, response);
                    break;
                case "generar":
                    generarDesempenoMensual(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/DesempenoServlet?accion=listar");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error al procesar: " + e.getMessage());
            try {
				listarDesempeno(request, response);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // LISTAR HISTORIAL CON FILTROS
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private void listarDesempeno(HttpServletRequest request, HttpServletResponse response) 
            throws Exception {
        
        String fechaInicioStr = request.getParameter("fechaInicio");
        String fechaFinStr = request.getParameter("fechaFin");
        
        Date fechaInicio = null;
        Date fechaFin = null;
        
        if (fechaInicioStr != null && !fechaInicioStr.isEmpty()) {
            fechaInicio = new SimpleDateFormat("yyyy-MM-dd").parse(fechaInicioStr);
        }
        if (fechaFinStr != null && !fechaFinStr.isEmpty()) {
            fechaFin = new SimpleDateFormat("yyyy-MM-dd").parse(fechaFinStr);
        }
        
        // ■■ RF36: Obtener historial con filtros de fecha ■■
        List<Desempeno_Vendedor> historial = desempenoDAO.obtenerHistorialCompleto(fechaInicio, fechaFin);
        
        request.setAttribute("historial", historial);
        request.setAttribute("fechaInicio", fechaInicioStr);
        request.setAttribute("fechaFin", fechaFinStr);
        
        // ■■ Calcular totales para dashboard ■■
        BigDecimal totalVentas = historial.stream()
            .map(Desempeno_Vendedor::getVentasTotales)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        BigDecimal totalComisiones = historial.stream()
            .map(Desempeno_Vendedor::getComisionGanada)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        request.setAttribute("totalVentas", totalVentas);
        request.setAttribute("totalComisiones", totalComisiones);
        
        request.getRequestDispatcher("/Administrador/usuarios/historial.jsp")
            .forward(request, response);
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // CALCULAR DESEMPEÑO REAL DESDE VENTAS (RF36)
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private void calcularDesempeno(HttpServletRequest request, HttpServletResponse response) 
            throws Exception {
        
        int usuarioId = Integer.parseInt(request.getParameter("usuarioId"));
        String fechaInicioStr = request.getParameter("fechaInicio");
        String fechaFinStr = request.getParameter("fechaFin");
        
        Date fechaInicio = new SimpleDateFormat("yyyy-MM-dd").parse(fechaInicioStr);
        Date fechaFin = new SimpleDateFormat("yyyy-MM-dd").parse(fechaFinStr);
        
        // ■■ RF36: Calcular métricas reales desde tabla Venta ■■
        Desempeno_Vendedor desempenoReal = desempenoDAO.calcularDesempenoReal(usuarioId, fechaInicio, fechaFin);
        
        if (desempenoReal != null) {
            request.setAttribute("desempenoCalculado", desempenoReal);
            request.setAttribute("mensaje", "Desempeño calculado exitosamente desde ventas reales.");
        } else {
            request.setAttribute("error", "No se encontraron ventas para el período seleccionado.");
        }
        
        listarDesempeno(request, response);
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // ACTUALIZAR OBSERVACIONES CON AUDITORÍA (RF38)
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private void actualizarDesempeno(HttpServletRequest request, HttpServletResponse response) 
            throws Exception {
        
        HttpSession session = request.getSession();
        Administrador admin = (Administrador) session.getAttribute("admin");
        
        int desempenoId = Integer.parseInt(request.getParameter("desempenoId"));
        String observaciones = request.getParameter("observaciones");
        
        Desempeno_Vendedor existente = desempenoDAO.obtenerUltimoDesempenoPorUsuario(
            Integer.parseInt(request.getParameter("usuarioId"))
        );
        
        if (existente != null) {
            existente.setDesempenoId(desempenoId);
            existente.setObservaciones(observaciones);
            
            // ■■ RF38: Pasar admin ID para auditoría ■■
            boolean exito = desempenoDAO.actualizarDesempeno(existente, admin.getId());
            
            if (exito) {
                request.setAttribute("mensaje", "Desempeño actualizado correctamente.");
            } else {
                request.setAttribute("error", "No se pudo actualizar el desempeño.");
            }
        }
        
        listarDesempeno(request, response);
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // GENERAR DESEMPEÑO MENSUAL AUTOMÁTICO
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private void generarDesempenoMensual(HttpServletRequest request, HttpServletResponse response) 
            throws Exception {
        
        HttpSession session = request.getSession();
        Administrador admin = (Administrador) session.getAttribute("admin");
        
        String mesStr = request.getParameter("mes"); // formato: yyyy-MM
        Date fechaInicio = new SimpleDateFormat("yyyy-MM-dd").parse(mesStr + "-01");
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(fechaInicio);
        cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
        Date fechaFin = cal.getTime();
        
        // ■■ Obtener todos los vendedores ■■
        String sqlVendedores = """
            SELECT u.usuario_id, u.nombre 
            FROM Usuario u 
            INNER JOIN Usuario_Rol ur ON ur.usuario_id = u.usuario_id 
            INNER JOIN Rol r ON r.rol_id = ur.rol_id 
            WHERE r.cargo = 'vendedor' AND u.estado = 1
            """;
        
        try (Connection conn = config.ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlVendedores);
             ResultSet rs = ps.executeQuery()) {
            
            int creados = 0;
            while (rs.next()) {
                int vendedorId = rs.getInt("usuario_id");
                
                // ■■ RF36: Calcular desde ventas reales ■■
                Desempeno_Vendedor desempeno = desempenoDAO.calcularDesempenoReal(
                    vendedorId, fechaInicio, fechaFin
                );
                
                if (desempeno != null) {
                    // ■■ RF38: Pasar admin ID para auditoría ■■
                    desempenoDAO.insertarDesempeno(desempeno, admin.getId());
                    creados++;
                }
            }
            
            request.setAttribute("mensaje", "Se generaron " + creados + " registros de desempeño para " + mesStr);
        }
        
        listarDesempeno(request, response);
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // EXPORTAR REPORTE (RF35)
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private void exportarReporte(HttpServletRequest request, HttpServletResponse response) 
            throws Exception {
        // Implementación para exportar a PDF/Excel según RF35
        response.setContentType("text/plain");
        response.getWriter().write("Funcionalidad de exportación - Pendiente de implementar PDFGenerator");
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // VALIDAR SESIÓN DE ADMINISTRADOR
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private boolean validarSesionAdmin(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession(false);
        
        // ■■ RNF03: Validar que sea administrador ■■
        if (session == null || session.getAttribute("admin") == null) {
            response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp");
            return false;
        }
        
        return true;
    }
}