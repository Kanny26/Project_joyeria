package controller;

import config.ConexionDB;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/CambiarPasswordServlet")
public class CambiarPasswordServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.getWriter().write("{\"ok\":false,\"msg\":\"Sesión expirada.\"}");
            return;
        }

        // ── 1. Determinar el usuario_id con seguridad ──────────────────────
        int usuarioId = -1;
        Object adminObj    = session.getAttribute("admin");
        Object vendedorObj = session.getAttribute("vendedor");

        // Priorizamos al Vendedor si estamos en un flujo de vendedor, 
        // o verificamos cuál de los dos NO es nulo de forma más estricta.
        if (vendedorObj != null && vendedorObj instanceof model.Usuario) {
            usuarioId = ((model.Usuario) vendedorObj).getUsuarioId();
            System.out.println("DEBUG: Identificado como VENDEDOR. ID: " + usuarioId);
        } else if (adminObj != null && adminObj instanceof model.Administrador) {
            usuarioId = ((model.Administrador) adminObj).getId();
            System.out.println("DEBUG: Identificado como ADMIN. ID: " + usuarioId);
        }

        // ── 2. Captura de parámetros ────────────────────────────────────────
        String passActual  = request.getParameter("passActual");
        String passNueva   = request.getParameter("passNueva");
        String passConfirm = request.getParameter("passConfirm");

        // ── 3. Validaciones de negocio ──────────────────────────────────────
        if (isBlank(passActual) || isBlank(passNueva) || isBlank(passConfirm)) {
            response.getWriter().write("{\"ok\":false,\"msg\":\"Todos los campos son obligatorios.\"}");
            return;
        }
        if (!passNueva.equals(passConfirm)) {
            response.getWriter().write("{\"ok\":false,\"msg\":\"La nueva contraseña no coincide con la confirmación.\"}");
            return;
        }
        if (passNueva.trim().length() < 6) {
            response.getWriter().write("{\"ok\":false,\"msg\":\"La nueva contraseña debe tener al menos 6 caracteres.\"}");
            return;
        }
        if (passNueva.trim().equals(passActual.trim())) {
            response.getWriter().write("{\"ok\":false,\"msg\":\"La nueva contraseña debe ser distinta a la actual.\"}");
            return;
        }

        // ── 4. Verificar contraseña actual contra BD ────────────────────────
        String hashBD = null;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT pass FROM Usuario WHERE usuario_id = ? AND estado = 1")) {
            
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    response.getWriter().write("{\"ok\":false,\"msg\":\"Usuario no encontrado.\"}");
                    return;
                }
                hashBD = rs.getString("pass");
            }

            // Comparación con BCrypt
            boolean coincide = BCrypt.checkpw(passActual.trim(), hashBD);
            System.out.println("DEBUG: ¿Contraseña actual coincide?: " + coincide);

            if (!coincide) {
                response.getWriter().write("{\"ok\":false,\"msg\":\"La contraseña actual es incorrecta.\"}");
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"ok\":false,\"msg\":\"Error al verificar credenciales.\"}");
            return;
        }

        // ── 5. Actualizar a la nueva contraseña ─────────────────────────────
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE Usuario SET pass = ?, pass_temporal = 0 WHERE usuario_id = ?")) {
            
            String nuevoHash = BCrypt.hashpw(passNueva.trim(), BCrypt.gensalt());
            ps.setString(1, nuevoHash);
            ps.setInt(2, usuarioId);
            
            int filasAfec = ps.executeUpdate();

            if (filasAfec > 0) {
                // Importante: Actualizar la sesión para que el modal ya no salga
                session.setAttribute("passTemporal", false);
                System.out.println("DEBUG: Contraseña actualizada con éxito para ID: " + usuarioId);
                response.getWriter().write("{\"ok\":true,\"msg\":\"Contraseña actualizada correctamente.\"}");
            } else {
                response.getWriter().write("{\"ok\":false,\"msg\":\"No se pudo actualizar la contraseña.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"ok\":false,\"msg\":\"Error interno al guardar nueva contraseña.\"}");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}