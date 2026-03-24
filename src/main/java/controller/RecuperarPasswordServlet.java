package controller;

import config.ConexionDB;
import org.mindrot.jbcrypt.BCrypt;
import services.EmailService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

/**
 * Servlet que maneja el flujo completo de recuperación de contraseña:
 *   POST /recuperar?paso=1  → valida correo, genera código INT, envía email
 *   POST /recuperar?paso=2  → valida el código de 6 dígitos (INT)
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
            // Solo puede llegar aquí si el código fue validado (usuario_id en sesión)
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("recuperar_usuario_id") == null) {
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
    // PASO 1 — Validar correo y enviar código (INT de 6 dígitos)
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

        // Siempre redirigir al paso 2 (no revelar si el correo existe)
        HttpSession session = req.getSession();
        session.setAttribute("recuperar_correo", correo);

        if (usuarioId == -1) {
            // Usuario no encontrado: redirigir igual para no exponer información
            resp.sendRedirect(req.getContextPath() + "/recuperar?paso=2");
            return;
        }

        // Generar código de 6 dígitos como INT (100000 a 999999)
        int codigo = 100000 + (int)(Math.random() * 900000);

        // Invalidar códigos anteriores del usuario
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE Recuperacion_Contrasena SET estado = 0 WHERE usuario_id = ?")) {
            ps.setInt(1, usuarioId);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }

        // Guardar código en BD (expira en 15 minutos)
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Recuperacion_Contrasena " +
                "(usuario_id, codigo_verificacion, fecha_solicitud, fecha_expiracion, estado) " +
                "VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 15 MINUTE), 1)")) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, codigo);  // ← Guardamos como INT, no como String
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            reenviar(req, resp, "/Recuperar_pass/ing-correo.jsp", "Error interno. Intenta de nuevo.");
            return;
        }

        // Enviar correo con el código (formateado para visualización)
        String codigoStr = String.format("%06d", codigo);
        boolean enviado = EmailService.enviarCodigoRecuperacion(correo, nombreUsuario, codigoStr);
        if (!enviado) {
            System.err.println("Código generado pero correo no enviado. Código: " + codigoStr);
        }

        // Guardar en sesión para los siguientes pasos
        session.setAttribute("recuperar_usuario_id", usuarioId);

        resp.sendRedirect(req.getContextPath() + "/recuperar?paso=2");
    }

    // ════════════════════════════════════════════════════════════════════════
    // PASO 2 — Validar el código de 6 dígitos (comparación directa como INT)
    // ════════════════════════════════════════════════════════════════════════
    private void paso2_validarCodigo(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("recuperar_usuario_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/Recuperar_pass/ing-correo.jsp");
            return;
        }

        int usuarioId = (int) session.getAttribute("recuperar_usuario_id");
        String codigoIngresadoStr = req.getParameter("codigo");

        if (codigoIngresadoStr == null || codigoIngresadoStr.trim().isEmpty()) {
            req.setAttribute("error", "Debes ingresar el código.");
            req.getRequestDispatcher("/Recuperar_pass/ing-codigo.jsp").forward(req, resp);
            return;
        }

        // Convertir a entero para comparar con la BD
        int codigoIngresado;
        try {
            codigoIngresado = Integer.parseInt(codigoIngresadoStr.trim());
        } catch (NumberFormatException e) {
            req.setAttribute("error", "Código inválido. Debe ser un número de 6 dígitos.");
            req.getRequestDispatcher("/Recuperar_pass/ing-codigo.jsp").forward(req, resp);
            return;
        }

        // Buscar código vigente en BD (como INT)
        boolean codigoValido = false;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT recuperacion_id FROM Recuperacion_Contrasena " +
                "WHERE usuario_id = ? AND codigo_verificacion = ? " +
                "AND estado = 1 AND fecha_expiracion > NOW() " +
                "ORDER BY fecha_solicitud DESC LIMIT 1")) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, codigoIngresado);  // ← Comparación directa como INT
            try (ResultSet rs = ps.executeQuery()) {
                codigoValido = rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Error de validación. Intenta de nuevo.");
            req.getRequestDispatcher("/Recuperar_pass/ing-codigo.jsp").forward(req, resp);
            return;
        }

        if (!codigoValido) {
            req.setAttribute("error", "Código incorrecto o expirado. Solicita uno nuevo.");
            req.getRequestDispatcher("/Recuperar_pass/ing-codigo.jsp").forward(req, resp);
            return;
        }

        // Código correcto → avanzar al paso 3
        // (No necesitamos guardar token, el usuario_id en sesión es suficiente)
        resp.sendRedirect(req.getContextPath() + "/recuperar?paso=3");
    }

    // ════════════════════════════════════════════════════════════════════════
    // PASO 3 — Guardar nueva contraseña
    // ════════════════════════════════════════════════════════════════════════
    private void paso3_guardarPassword(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("recuperar_usuario_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/Recuperar_pass/ing-correo.jsp");
            return;
        }

        int usuarioId = (int) session.getAttribute("recuperar_usuario_id");

        String passNueva   = req.getParameter("passNueva");
        String passConfirm = req.getParameter("passConfirm");

        // Validaciones de contraseña
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

        // Hashear y actualizar contraseña
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

        // Invalidar todos los códigos de recuperación del usuario
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE Recuperacion_Contrasena SET estado = 0 WHERE usuario_id = ?")) {
            ps.setInt(1, usuarioId);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }

        // Limpiar sesión de recuperación
        session.removeAttribute("recuperar_correo");
        session.removeAttribute("recuperar_usuario_id");

        // Redirigir al login con mensaje de éxito
        resp.sendRedirect(req.getContextPath() + "/inicio-sesion.jsp?msg=password_actualizado");
    }

    // ─── Helper para reenvío con error ───────────────────────────────────────
    private void reenviar(HttpServletRequest req, HttpServletResponse resp, String vista, String error)
            throws ServletException, IOException {
        req.setAttribute("error", error);
        req.getRequestDispatcher(vista).forward(req, resp);
    }
}