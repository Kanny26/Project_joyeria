package controller;

import config.ConexionDB;
import org.mindrot.jbcrypt.BCrypt;
import services.EmailService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

/**
 * Servlet que maneja el flujo completo de recuperación de contraseña:
 *   POST /recuperar?paso=1  → valida correo, genera token, envía email
 *   POST /recuperar?paso=2  → valida el código de 6 dígitos
 *   POST /recuperar?paso=3  → guarda la nueva contraseña
 */
@WebServlet("/recuperar")
public class RecuperarPasswordServlet extends HttpServlet {

    // ─── GET: mostrar las páginas JSP ───────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String paso = req.getParameter("paso");
        if ("2".equals(paso)) {
            req.getRequestDispatcher("/Recuperar_pass/ing-codigo.jsp").forward(req, resp);
        } else if ("3".equals(paso)) {
            // Solo puede llegar aquí si el código fue validado (token en sesión)
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("recuperar_token") == null) {
                resp.sendRedirect(req.getContextPath() + "/Recuperar_pass/ing-correo.jsp");
                return;
            }
            req.getRequestDispatcher("/Recuperar_pass/nueva-pass.jsp").forward(req, resp);
        } else {
            req.getRequestDispatcher("/Recuperar_pass/ing-correo.jsp").forward(req, resp);
        }
    }

    // ─── POST: lógica de cada paso ───────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String paso = req.getParameter("paso");

        if ("1".equals(paso)) { paso1_enviarCodigo(req, resp); }
        else if ("2".equals(paso)) { paso2_validarCodigo(req, resp); }
        else if ("3".equals(paso)) { paso3_guardarPassword(req, resp); }
        else { resp.sendRedirect(req.getContextPath() + "/Recuperar_pass/ing-correo.jsp"); }
    }

    // ════════════════════════════════════════════════════════════════════════
    // PASO 1 — Validar correo y enviar código
    // ════════════════════════════════════════════════════════════════════════
    private void paso1_enviarCodigo(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String correo = req.getParameter("correo");
        if (correo == null || correo.trim().isEmpty()) {
            reenviar(req, resp, "/Recuperar_pass/ing-correo.jsp", "Debes ingresar tu correo.");
            return;
        }
        correo = correo.trim().toLowerCase();

        // Buscar usuario por correo
        int usuarioId = -1;
        String nombreUsuario = null;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT u.usuario_id, u.nombre FROM Usuario u " +
                "INNER JOIN Correo_Usuario c ON c.usuario_id = u.usuario_id " +
                "WHERE LOWER(c.email) = ? AND u.estado = 1")) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuarioId = rs.getInt("usuario_id");
                    nombreUsuario = rs.getString("nombre");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Siempre mostrar el mismo mensaje (no revelar si el correo existe)
        if (usuarioId == -1) {
            // Guardar en sesión el correo para mostrarlo en paso 2
            HttpSession session = req.getSession();
            session.setAttribute("recuperar_correo", correo);
            resp.sendRedirect(req.getContextPath() + "/recuperar?paso=2");
            return;
        }

        // Generar código de 6 dígitos
        String codigo = String.format("%06d", (int)(Math.random() * 1_000_000));
        String token  = UUID.randomUUID().toString(); // token interno para seguridad

        // Invalidar tokens anteriores del usuario
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE recuperacion_contrasena SET estado = 0 WHERE usuario_id = ?")) {
            ps.setInt(1, usuarioId);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }

        // Guardar token + código en BD (expira en 15 minutos)
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO recuperacion_contrasena (usuario_id, token, fecha_solicitud, fecha_expiracion, estado) " +
                "VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 15 MINUTE), 1)")) {
            ps.setInt(1, usuarioId);
            // Guardamos "codigo:token" como token compuesto
            ps.setString(2, codigo + ":" + token);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            reenviar(req, resp, "/Recuperar_pass/ing-correo.jsp", "Error interno. Intenta de nuevo.");
            return;
        }

        // Enviar correo con el código
        boolean enviado = EmailService.enviarCodigoRecuperacion(correo, nombreUsuario, codigo);
        if (!enviado) {
            System.err.println("⚠ Código generado pero correo no enviado. Código: " + codigo);
        }

        // Guardar en sesión para el paso 2
        HttpSession session = req.getSession();
        session.setAttribute("recuperar_correo", correo);
        session.setAttribute("recuperar_usuario_id", usuarioId);

        resp.sendRedirect(req.getContextPath() + "/recuperar?paso=2");
    }

    // ════════════════════════════════════════════════════════════════════════
    // PASO 2 — Validar el código de 6 dígitos
    // ════════════════════════════════════════════════════════════════════════
    private void paso2_validarCodigo(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("recuperar_usuario_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/Recuperar_pass/ing-correo.jsp");
            return;
        }

        int usuarioId = (int) session.getAttribute("recuperar_usuario_id");
        String codigoIngresado = req.getParameter("codigo");

        if (codigoIngresado == null || codigoIngresado.trim().isEmpty()) {
            req.setAttribute("error", "Debes ingresar el código.");
            req.getRequestDispatcher("/Recuperar_pass/ing-codigo.jsp").forward(req, resp);
            return;
        }
        codigoIngresado = codigoIngresado.trim();

        // Buscar token vigente en BD
        String tokenBD = null;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT token FROM recuperacion_contrasena " +
                "WHERE usuario_id = ? AND estado = 1 AND fecha_expiracion > NOW() " +
                "ORDER BY fecha_solicitud DESC LIMIT 1")) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tokenBD = rs.getString("token");
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (tokenBD == null) {
            req.setAttribute("error", "El código ha expirado. Solicita uno nuevo.");
            req.getRequestDispatcher("/Recuperar_pass/ing-codigo.jsp").forward(req, resp);
            return;
        }

        // El token tiene formato "codigo:uuid"
        String codigoBD = tokenBD.split(":")[0];
        String tokenUUID = tokenBD.split(":")[1];

        if (!codigoBD.equals(codigoIngresado)) {
            req.setAttribute("error", "Código incorrecto. Revisa tu correo e intenta de nuevo.");
            req.getRequestDispatcher("/Recuperar_pass/ing-codigo.jsp").forward(req, resp);
            return;
        }

        // Código correcto → guardar token en sesión para paso 3
        session.setAttribute("recuperar_token", tokenUUID);
        resp.sendRedirect(req.getContextPath() + "/recuperar?paso=3");
    }

    // ════════════════════════════════════════════════════════════════════════
    // PASO 3 — Guardar nueva contraseña
    // ════════════════════════════════════════════════════════════════════════
    private void paso3_guardarPassword(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("recuperar_token") == null) {
            resp.sendRedirect(req.getContextPath() + "/Recuperar_pass/ing-correo.jsp");
            return;
        }

        int usuarioId = (int) session.getAttribute("recuperar_usuario_id");
        String tokenSesion = (String) session.getAttribute("recuperar_token");

        String passNueva   = req.getParameter("passNueva");
        String passConfirm = req.getParameter("passConfirm");

        if (passNueva == null || passNueva.trim().length() < 6) {
            req.setAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            req.getRequestDispatcher("/Recuperar_pass/nueva-pass.jsp").forward(req, resp);
            return;
        }
        if (!passNueva.equals(passConfirm)) {
            req.setAttribute("error", "Las contraseñas no coinciden.");
            req.getRequestDispatcher("/Recuperar_pass/nueva-pass.jsp").forward(req, resp);
            return;
        }

        // Verificar que el token en sesión coincide con BD y sigue vigente
        boolean tokenValido = false;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM recuperacion_contrasena " +
                "WHERE usuario_id = ? AND token LIKE ? AND estado = 1 AND fecha_expiracion > NOW()")) {
            ps.setInt(1, usuarioId);
            ps.setString(2, "%:" + tokenSesion);
            try (ResultSet rs = ps.executeQuery()) {
                tokenValido = rs.next();
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (!tokenValido) {
            req.setAttribute("error", "El enlace expiró. Solicita un nuevo código.");
            req.getRequestDispatcher("/Recuperar_pass/nueva-pass.jsp").forward(req, resp);
            return;
        }

        // Actualizar contraseña
        String nuevoHash = BCrypt.hashpw(passNueva.trim(), BCrypt.gensalt());
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE Usuario SET pass = ?, pass_temporal = 0 WHERE usuario_id = ?")) {
            ps.setString(1, nuevoHash);
            ps.setInt(2, usuarioId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al guardar la contraseña. Intenta de nuevo.");
            req.getRequestDispatcher("/Recuperar_pass/nueva-pass.jsp").forward(req, resp);
            return;
        }

        // Invalidar todos los tokens del usuario
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE recuperacion_contrasena SET estado = 0 WHERE usuario_id = ?")) {
            ps.setInt(1, usuarioId);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }

        // Limpiar sesión de recuperación
        session.removeAttribute("recuperar_correo");
        session.removeAttribute("recuperar_usuario_id");
        session.removeAttribute("recuperar_token");

        // Redirigir al login con mensaje de éxito
        resp.sendRedirect(req.getContextPath() + "/inicio-sesion.jsp?msg=password_actualizado");
    }

    private void reenviar(HttpServletRequest req, HttpServletResponse resp, String vista, String error)
            throws ServletException, IOException {
        req.setAttribute("error", error);
        req.getRequestDispatcher(vista).forward(req, resp);
    }
}
