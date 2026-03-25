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

/**
 * Permite al usuario autenticado cambiar su propia contraseña.
 * Responde siempre en JSON porque el formulario es enviado vía AJAX (fetch),
 * lo que permite actualizar la UI sin recargar la página.
 * Funciona tanto para vendedores como para administradores,
 * identificando el tipo de sesión activa para obtener el ID correcto del usuario.
 */
@WebServlet("/CambiarPasswordServlet")
public class CambiarPasswordServlet extends HttpServlet {

    /**
     * Maneja las peticiones POST para el cambio de contraseña.
     * Verifica la sesión activa, valida los datos ingresados,
     * comprueba la contraseña actual contra el hash almacenado,
     * y actualiza con la nueva contraseña si todo es correcto.
     * 
     * @param request objeto HttpServletRequest que contiene los parámetros passActual,
     *                passNueva y passConfirm enviados desde el formulario
     * @param response objeto HttpServletResponse para enviar la respuesta en formato JSON
     * @throws ServletException si ocurre un error en el procesamiento del servlet
     * @throws IOException si ocurre un error de entrada/salida durante el manejo
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        // Todas las respuestas son JSON para que el JavaScript del frontend las procese
        response.setContentType("application/json;charset=UTF-8");

        // Si no hay sesión activa, no se puede saber qué usuario desea cambiar la contraseña
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.getWriter().write("{\"ok\":false,\"msg\":\"Sesión expirada.\"}");
            return;
        }

        /*
         * Se determina el ID del usuario a partir del tipo de sesión activa.
         * Se verifica primero "vendedor" y luego "admin" para evitar confusiones
         * si por algún error ambos atributos existieran en la misma sesión.
         */
        int usuarioId = -1;
        Object adminObj    = session.getAttribute("admin");
        Object vendedorObj = session.getAttribute("vendedor");

        if (vendedorObj instanceof model.Usuario) {
            usuarioId = ((model.Usuario) vendedorObj).getUsuarioId();
        } else if (adminObj instanceof model.Administrador) {
            usuarioId = ((model.Administrador) adminObj).getId();
        }

        String passActual  = request.getParameter("passActual");
        String passNueva   = request.getParameter("passNueva");
        String passConfirm = request.getParameter("passConfirm");

        // Validaciones previas a consultar la base de datos:
        // isBlank() retorna true si la cadena es null, vacía o solo espacios en blanco
        if (isBlank(passActual) || isBlank(passNueva) || isBlank(passConfirm)) {
            response.getWriter().write("{\"ok\":false,\"msg\":\"Todos los campos son obligatorios.\"}");
            return;
        }
        // Las dos contraseñas nuevas deben ser idénticas antes de proceder
        if (!passNueva.equals(passConfirm)) {
            response.getWriter().write("{\"ok\":false,\"msg\":\"La nueva contraseña no coincide con la confirmación.\"}");
            return;
        }
        // Longitud mínima de seguridad
        if (passNueva.trim().length() < 6) {
            response.getWriter().write("{\"ok\":false,\"msg\":\"La nueva contraseña debe tener al menos 6 caracteres.\"}");
            return;
        }
        // No tiene sentido cambiar a la misma contraseña que ya se tenía
        if (passNueva.trim().equals(passActual.trim())) {
            response.getWriter().write("{\"ok\":false,\"msg\":\"La nueva contraseña debe ser distinta a la actual.\"}");
            return;
        }

        /*
         * Paso 1: Verificar que la contraseña actual es correcta.
         * Se consulta el hash almacenado en la BD para el usuario activo (estado = 1).
         * Solo se permite el cambio si el usuario está activo.
         */
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

            // BCrypt.checkpw compara la contraseña en texto plano contra el hash de la BD.
            // BCrypt no permite desencriptar; solo verifica si coinciden.
            if (!BCrypt.checkpw(passActual.trim(), hashBD)) {
                response.getWriter().write("{\"ok\":false,\"msg\":\"La contraseña actual es incorrecta.\"}");
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"ok\":false,\"msg\":\"Error al verificar credenciales.\"}");
            return;
        }

        /*
         * Paso 2: Actualizar con la nueva contraseña.
         * BCrypt.hashpw() genera un hash seguro con una sal aleatoria antes de guardarlo.
         * pass_temporal = 0 indica que ya no es una contraseña temporal,
         * por lo que el modal de cambio obligatorio no volverá a aparecer.
         */
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE Usuario SET pass = ?, pass_temporal = 0 WHERE usuario_id = ?")) {

            String nuevoHash = BCrypt.hashpw(passNueva.trim(), BCrypt.gensalt());
            ps.setString(1, nuevoHash);
            ps.setInt(2, usuarioId);

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                // Se sincroniza el atributo de sesión para que el modal no reaparezca
                // en la misma sesión activa sin necesidad de cerrar y volver a entrar.
                session.setAttribute("passTemporal", false);
                response.getWriter().write("{\"ok\":true,\"msg\":\"Contraseña actualizada correctamente.\"}");
            } else {
                response.getWriter().write("{\"ok\":false,\"msg\":\"No se pudo actualizar la contraseña.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"ok\":false,\"msg\":\"Error interno al guardar nueva contraseña.\"}");
        }
    }

    /**
     * Verifica si una cadena de texto es nula, vacía o contiene solo espacios en blanco.
     * 
     * @param s la cadena de texto a validar
     * @return true si la cadena es null, está vacía o solo contiene espacios en blanco;
     *         false en caso contrario
     */
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}