package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import services.EmailService;

/**
 * Servlet del módulo de ayuda y soporte técnico.
 *
 * Recibe el formulario de contacto desde ayuda.jsp y envía un correo
 * al equipo de soporte con la consulta del administrador.
 * Redirige de vuelta a ayuda.jsp con ?status=success o ?status=error
 * para que el JSP muestre la alerta correspondiente.
 */
@WebServlet("/AyudaServlet")
public class AyudaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Procesa las solicitudes POST del formulario de contacto de soporte técnico.
     * Extrae los datos del formulario, envía la consulta al equipo de soporte mediante
     * el servicio de correo electrónico y redirige al usuario con un indicador de estado.
     *
     * @param request  la petición HTTP que contiene los parámetros del formulario de ayuda
     * @param response la respuesta HTTP utilizada para redirigir al usuario con el estado del envío
     * @throws ServletException si ocurre un error durante el procesamiento del servlet
     * @throws IOException      si ocurre un error de entrada/salida al enviar la respuesta o el correo
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Leer los campos del formulario de ayuda
        String nombreAdmin = request.getParameter("nombreAdmin");
        String asunto      = request.getParameter("asunto");
        String mensaje     = request.getParameter("mensaje");

        // enviarConsultaSoporte() usa EmailService para enviar el correo al equipo de soporte.
        // Retorna true si el envío fue exitoso, false si ocurrió algún error de conexión SMTP.
        boolean enviado = EmailService.enviarConsultaSoporte(nombreAdmin, asunto, mensaje);

        // Se usa sendRedirect para evitar que al refrescar la página se reenvíe el formulario.
        // El parámetro ?status indica al JSP si mostrar mensaje de éxito o error.
        if (enviado) {
            response.sendRedirect(request.getContextPath() + "/Administrador/ayuda.jsp?status=success");
        } else {
            response.sendRedirect(request.getContextPath() + "/Administrador/ayuda.jsp?status=error");
        }
    }
}