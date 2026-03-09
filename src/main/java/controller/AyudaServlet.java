package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import services.EmailService;

@WebServlet("/AyudaServlet")
public class AyudaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Obtener los parámetros del formulario
        String nombreAdmin = request.getParameter("nombreAdmin");
        String asunto = request.getParameter("asunto");
        String mensaje = request.getParameter("mensaje");

        // 2. Llamar al método que agregamos en EmailService
        boolean enviado = EmailService.enviarConsultaSoporte(nombreAdmin, asunto, mensaje);

        // 3. Redirigir con una respuesta
        if (enviado) {
            response.sendRedirect(request.getContextPath() + "/Administrador/ayuda.jsp?status=success");
        } else {
            response.sendRedirect(request.getContextPath() + "/Administrador/ayuda.jsp?status=error");
        }
    }
}