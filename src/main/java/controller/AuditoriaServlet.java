package controller;

import dao.AuditoriaDAO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Muestra el registro de auditoría del sistema.
 *
 * Solo es accesible para usuarios con sesión activa de administrador o superadministrador.
 * Consulta todos los logs de la tabla Auditoria_Log y los pasa a la vista JSP.
 *
 * El log de auditoría registra quién hizo qué y cuándo en el sistema
 * (creación de ventas, cambios de contraseña, edición de usuarios, etc.).
 */
@WebServlet("/AuditoriaServlet")
public class AuditoriaServlet extends HttpServlet {

    private AuditoriaDAO auditoriaDAO;

    @Override
    public void init() {
        auditoriaDAO = new AuditoriaDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        // Verificación de sesión: sin sesión activa no se puede acceder al log de auditoría
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            resp.sendRedirect(req.getContextPath() + "/inicio-sesion.jsp");
            return;
        }

        // El log de auditoría solo está disponible para roles con privilegios de gestión.
        // Se obtiene el rol desde la sesión para verificar el permiso.
        String rol = (String) session.getAttribute("rol");
        if (!"superadministrador".equals(rol) && !"administrador".equals(rol)) {
            resp.sendRedirect(req.getContextPath() + "/inicio-sesion.jsp");
            return;
        }

        // listarLogs() retorna todos los registros ordenados del más reciente al más antiguo.
        // Cada registro es un Map con claves: log_id, usuario_nombre, accion, entidad,
        // entidad_id, datos_anteriores, datos_nuevos, direccion_ip, fecha_hora.
        List<Map<String, Object>> logs = null;
        try {
            logs = auditoriaDAO.listarLogs();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Los logs se pasan como atributo del request para que el JSP los itere con JSTL.
        req.setAttribute("logs", logs);
        req.getRequestDispatcher("/Administrador/auditoria/auditoria_log.jsp").forward(req, resp);
    }
}
