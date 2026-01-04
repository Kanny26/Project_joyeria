package controller;

import dao.UsuarioDAO;
import model.Usuario;
import config.ConexionDB;

import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/UsuarioServlet")
public class UsuarioServlet extends HttpServlet {
    private UsuarioDAO usuarioDAO;

    @Override
    public void init() {
        try {
            // Usar la conexión de ConexionDB
            usuarioDAO = new UsuarioDAO(ConexionDB.getConnection());
        } catch (Exception e) {
            throw new RuntimeException("Error al iniciar UsuarioServlet", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if (accion == null) accion = "listar";

        switch (accion) {
            case "nuevo":
                request.getRequestDispatcher("Administrador/usuarios/agregar_usuario.jsp").forward(request, response);
                break;

            case "editar":
                int idEditar = Integer.parseInt(request.getParameter("id"));
                Usuario usuarioEditar = usuarioDAO.obtenerUsuarioPorId(idEditar);
                request.setAttribute("usuario", usuarioEditar);
                request.getRequestDispatcher("Administrador/usuarios/editar_usuario.jsp").forward(request, response);
                break;

            case "historial":
                List<Usuario> historial = usuarioDAO.listarUsuarios();
                request.setAttribute("historialUsuarios", historial);
                request.getRequestDispatcher("Administrador/usuarios/historial.jsp").forward(request, response);
                break;

            default: // listar
                List<Usuario> usuarios = usuarioDAO.listarUsuarios();
                request.setAttribute("usuarios", usuarios);
                request.setAttribute("totalUsuarios", usuarioDAO.contarUsuarios());
                request.setAttribute("usuariosActivos", usuarioDAO.contarUsuariosActivos());
                request.getRequestDispatcher("Administrador/usuarios/listar_usuario.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");

        if ("agregar".equals(accion)) {
            // Datos del formulario
            String nombre = request.getParameter("nombre");
            String correo = request.getParameter("correo");
            String telefono = request.getParameter("telefono");
            String contrasena = request.getParameter("contrasena");
            String rol = request.getParameter("rol");
            boolean estado = "Activo".equals(request.getParameter("estado"));

            // Generar contraseña temporal si el campo está vacío
            if (contrasena == null || contrasena.isEmpty()) {
                contrasena = generarContrasenaTemporal(8); // 8 caracteres
            }

            // Crear usuario
            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setCorreo(correo);
            usuario.setTelefono(telefono);
            usuario.setContrasena(contrasena);
            usuario.setRol(rol);
            usuario.setEstado(estado);

            // Guardar usuario en la BD
            boolean exito = usuarioDAO.agregarUsuario(usuario);

            if (exito) {
                // Enviar correo con contraseña temporal
                if (correo != null && !correo.isEmpty()) {
                    enviarCorreoContrasena(correo, contrasena);
                }

                request.setAttribute("mensaje", "¡El usuario ha sido agregado con éxito!");
                request.getRequestDispatcher("Administrador/usuarios/mensaje_exito.jsp").forward(request, response);
            } else {
                request.setAttribute("mensaje", "Error al agregar el usuario.");
                request.getRequestDispatcher("Administrador/usuarios/mensaje_error.jsp").forward(request, response);
            }

        } else if ("editar".equals(accion)) {
            int id = Integer.parseInt(request.getParameter("id"));
            String nombre = request.getParameter("nombre");
            String correo = request.getParameter("correo");
            String telefono = request.getParameter("telefono");
            boolean estado = "Activo".equals(request.getParameter("estado"));

            // Crear usuario para actualizar
            Usuario usuario = new Usuario();
            usuario.setUsuarioId(id);
            usuario.setNombre(nombre);
            usuario.setCorreo(correo);
            usuario.setTelefono(telefono);
            usuario.setEstado(estado);

            // Actualizar usuario
            usuarioDAO.actualizarUsuario(usuario);

            // Enviar mensaje de éxito
            request.setAttribute("mensaje", "¡Los cambios del usuario se guardaron con éxito!");
            request.getRequestDispatcher("Administrador/usuarios/mensaje_exito.jsp").forward(request, response);
        }
    }

    // -------------------------------
    // Métodos auxiliares
    // -------------------------------

    // Generar contraseña temporal aleatoria
    private String generarContrasenaTemporal(int longitud) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < longitud; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // Enviar correo con contraseña
    private void enviarCorreoContrasena(String destinatario, String contrasena) {
        final String remitente = "tuCorreo@dominio.com"; // Cambiar por tu correo real
        final String clave = "tuPasswordCorreo"; // Cambiar por la contraseña de la app o real

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remitente, clave);
            }
        });

        try {
            Message mensaje = new MimeMessage(session);
            mensaje.setFrom(new InternetAddress(remitente));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject("Contraseña temporal para AAC27");
            mensaje.setText("Hola, se ha creado tu usuario.\nTu contraseña temporal es: " + contrasena
                    + "\nTe recomendamos cambiarla al ingresar al sistema.");

            Transport.send(mensaje);
            System.out.println("Correo enviado con éxito a " + destinatario);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
