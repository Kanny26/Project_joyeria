package joyeria.controllers;

import joyeria.services.UsuarioService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import java.io.IOException;

@WebServlet("/RegistroUsuarioServlet")
public class RegistroUsuarioServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Recibir datos del formulario (HTML)
        String nombre = request.getParameter("nombre");
        String email = request.getParameter("email");
        String pass = request.getParameter("pass");

        // 2. Llamar al service para registrar el usuario
        UsuarioService usuarioService = new UsuarioService();
        boolean registrado = usuarioService.registrarUsuario(nombre, email, pass);

        // 3. Redireccionar según resultado
        if (registrado) {
            response.sendRedirect("mensajesexito.html");
        } else {
            response.sendRedirect("registro.html");
        }
    }
}
