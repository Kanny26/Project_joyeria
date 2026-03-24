package services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;

/**
 * Servicio para envío de correos electrónicos.
 * Cumple con RF06 (envío de credenciales) y notificaciones.
 */
public class EmailService {

    /**
     * Envía las credenciales de acceso al usuario.
     * @param destinatario correo del destinatario
     * @param nombreUsuario nombre del usuario
     * @param rol rol asignado
     * @param contrasena contraseña temporal
     * @return boolean true si se envió correctamente
     */
    public static boolean enviarCredenciales(String destinatario, String nombreUsuario, 
                                              String rol, String contrasena) {
        try {
            Properties config = cargarConfiguracion();
            if (config == null) {
                System.err.println("No se encontró email.properties. Correo NO enviado.");
                return false;
            }

            final String remitente = config.getProperty("mail.from");
            final String appPass = config.getProperty("mail.password");

            Properties smtpProps = new Properties();
            smtpProps.put("mail.smtp.auth", "true");
            smtpProps.put("mail.smtp.starttls.enable", "true");
            smtpProps.put("mail.smtp.host", config.getProperty("mail.smtp.host", "smtp.gmail.com"));
            smtpProps.put("mail.smtp.port", config.getProperty("mail.smtp.port", "587"));
            smtpProps.put("mail.smtp.ssl.trust", config.getProperty("mail.smtp.host", "smtp.gmail.com"));

            Session session = Session.getInstance(smtpProps, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(remitente, appPass);
                }
            });

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(remitente, "AAC27- Sistema"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            msg.setSubject("Bienvenido a AAC27- Tus credenciales de acceso");
            msg.setContent(buildHtml(nombreUsuario, rol, contrasena, destinatario), 
                          "text/html; charset=UTF-8");

            Transport.send(msg);
            System.out.println("Correo enviado a: " + destinatario);
            return true;

        } catch (Exception e) {
            System.err.println("Error al enviar correo a " + destinatario + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Carga la configuración desde email.properties.
     * Busca en 3 ubicaciones posibles.
     * @return Properties configuración o null si no existe
     */
    private static Properties cargarConfiguracion() {
        // Intento 1: classpath raíz
        InputStream is = EmailService.class.getClassLoader()
            .getResourceAsStream("email.properties");

        // Intento 2: classpath con barra
        if (is == null) {
            is = EmailService.class.getResourceAsStream("/email.properties");
        }

        // Intento 3: ruta absoluta dentro del WAR
        if (is == null) {
            System.err.println("email.properties no encontrado en classpath.");
            System.err.println("Solución: mueve email.properties a src/main/resources/");
            return null;
        }

        try {
            Properties p = new Properties();
            p.load(is);
            is.close();
            System.out.println("email.properties cargado correctamente.");
            System.out.println("Remitente configurado: " + p.getProperty("mail.from"));
            return p;
        } catch (Exception e) {
            System.err.println("Error leyendo email.properties: " + e.getMessage());
            return null;
        }
    }

    /**
     * Construye el HTML del correo de credenciales.
     */
    private static String buildHtml(String nombre, String rol, String contrasena, String correo) {
        String rolLabel = Character.toUpperCase(rol.charAt(0)) + rol.substring(1).toLowerCase();
        
        String colorRol;
        if ("vendedor".equalsIgnoreCase(rol)) colorRol = "#2980b9";
        else if ("administrador".equalsIgnoreCase(rol)) colorRol = "#e74c3c";
        else if ("cliente".equalsIgnoreCase(rol)) colorRol = "#27ae60";
        else if ("proveedor".equalsIgnoreCase(rol)) colorRol = "#8e44ad";
        else colorRol = "#555555";

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html lang='es'><head><meta charset='UTF-8'></head>");
        sb.append("<body style='margin:0;padding:0;background:#f4f6f8;font-family:Arial,sans-serif;'>");
        sb.append("<table width='100%' cellpadding='0' cellspacing='0' ");
        sb.append("style='background:#f4f6f8;padding:30px 0;'><tr><td align='center'>");
        sb.append("<table width='600' cellpadding='0' cellspacing='0' ");
        sb.append("style='background:#fff;border-radius:10px;");
        sb.append("box-shadow:0 2px 8px rgba(0,0,0,.08);overflow:hidden;'>");

        // Cabecera
        sb.append("<tr><td style='background:#1a1a2e;padding:30px;text-align:center;'>");
        sb.append("<h1 style='color:#fff;margin:0;font-size:28px;letter-spacing:2px;'>AAC27</h1>");
        sb.append("<p style='color:#a0aec0;margin:6px 0 0;font-size:13px;'>Sistema de Gestion Interno</p>");
        sb.append("</td></tr>");

        // Cuerpo
        sb.append("<tr><td style='padding:35px 40px;'>");
        sb.append("<h2 style='color:#1a1a2e;margin:0 0 10px;'>Bienvenido,").append(nombre).append("!</h2>");
        sb.append("<p style='color:#555;font-size:15px;line-height:1.6;'>");
        sb.append("Tu cuenta ha sido creada exitosamente. Aqui estan tus credenciales:</p>");

        // Tarjeta de credenciales
        sb.append("<table width='100%' cellpadding='0' cellspacing='0'");
        sb.append(" style='background:#f8fafc;border:1px solid#e2e8f0;border-radius:8px;margin:25px 0;'>");
        sb.append("<tr><td style='padding:20px 25px;'><table width='100%'>");
        sb.append("<tr><td style='padding:8px 0;color:#718096;font-size:13px;width:140px;'>Usuario:</td>");
        sb.append("<td style='padding:8px 0;color:#1a1a2e;font-weight:bold;'>").append(nombre).append("</td></tr>");

        sb.append("<tr><td style='padding:8px 0;color:#718096;font-size:13px;'>Contrasena:</td>");
        sb.append("<td style='padding:8px 0;'><span style='background:#1a1a2e;color:#f0f0f0;");
        sb.append("padding:5px 14px;border-radius:5px;font-family:monospace;font-size:16px;");
        sb.append("letter-spacing:2px;font-weight:bold;'>").append(contrasena).append("</span></td></tr>");

        sb.append("<tr><td style='padding:8px 0;color:#718096;font-size:13px;'>Rol:</td>");
        sb.append("<td style='padding:8px 0;'><span style='background:").append(colorRol);
        sb.append(";color:#fff;padding:3px 12px;border-radius:20px;font-size:12px;font-weight:bold;'>");
        sb.append(rolLabel).append("</span></td></tr>");

        sb.append("<tr><td style='padding:8px 0;color:#718096;font-size:13px;'>Correo:</td>");
        sb.append("<td style='padding:8px 0;color:#1a1a2e;'>").append(correo).append("</td></tr>");

        sb.append("</table></td></tr></table>");

        // Aviso de seguridad
        sb.append("<table width='100%' cellpadding='0' cellspacing='0' ");
        sb.append("style='background:#fff8e1;border-left:4px solid #f59e0b;border-radius:4px;margin-bottom:20px;'>");
        sb.append("<tr><td style='padding:14px 18px;'>");
        sb.append("<p style='margin:0;color:#92400e;font-size:13px;line-height:1.5;'>");
        sb.append("<strong>Importante:</strong> Esta es una contrasena temporal.");
        sb.append("Te recomendamos cambiarla en tu primer inicio de sesion.</p>");
        sb.append("</td></tr></table>");
        sb.append("</td></tr>");

        // Pie
        sb.append("<tr><td style='background:#f8fafc;padding:20px;text-align:center;border-top:1px solid #e2e8f0;'>");
        sb.append("<p style='margin:0;color:#a0aec0;font-size:12px;'>");
        sb.append("2025 AAC27- Correo generado automaticamente.</p>");
        sb.append("</td></tr></table></td></tr></table></body></html>");
        
        return sb.toString();
    }

    /**
     * Envía un código de 6 dígitos para recuperación de contraseña.
     * RF: Recuperar contraseña
     */
    public static boolean enviarCodigoRecuperacion(String destinatario, String nombreUsuario, String codigo) {
        try {
            Properties config = cargarConfiguracion();
            if (config == null) {
                System.err.println("No se encontró email.properties. Correo de recuperación NO enviado.");
                return false;
            }

            final String remitente = config.getProperty("mail.from");
            final String appPass   = config.getProperty("mail.password");

            Properties smtpProps = new Properties();
            smtpProps.put("mail.smtp.auth", "true");
            smtpProps.put("mail.smtp.starttls.enable", "true");
            smtpProps.put("mail.smtp.host", config.getProperty("mail.smtp.host", "smtp.gmail.com"));
            smtpProps.put("mail.smtp.port", config.getProperty("mail.smtp.port", "587"));
            smtpProps.put("mail.smtp.ssl.trust", config.getProperty("mail.smtp.host", "smtp.gmail.com"));

            Session session = Session.getInstance(smtpProps, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(remitente, appPass);
                }
            });

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(remitente, "AAC27 - Sistema"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            msg.setSubject("AAC27 - Código de recuperación de contraseña");
            msg.setContent(buildHtmlRecuperacion(nombreUsuario, codigo), "text/html; charset=UTF-8");

            Transport.send(msg);
            System.out.println("Correo de recuperación enviado a: " + destinatario);
            return true;

        } catch (Exception e) {
            System.err.println("Error al enviar correo de recuperación a " + destinatario + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static String buildHtmlRecuperacion(String nombre, String codigo) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'></head>");
        sb.append("<body style='margin:0;padding:0;background:#f4f6f8;font-family:Arial,sans-serif;'>");
        sb.append("<table width='100%' cellpadding='0' cellspacing='0' style='background:#f4f6f8;padding:30px 0;'>");
        sb.append("<tr><td align='center'>");
        sb.append("<table width='520' cellpadding='0' cellspacing='0' style='background:#fff;border-radius:12px;");
        sb.append("box-shadow:0 2px 12px rgba(0,0,0,.08);overflow:hidden;'>");

        // Cabecera
        sb.append("<tr><td style='background:linear-gradient(135deg,#9177a8,#c5c2df);padding:30px;text-align:center;'>");
        sb.append("<h1 style='color:#fff;margin:0;font-size:26px;letter-spacing:2px;'>AAC27</h1>");
        sb.append("<p style='color:rgba(255,255,255,0.8);margin:6px 0 0;font-size:13px;'>Recuperación de contraseña</p>");
        sb.append("</td></tr>");

        // Cuerpo
        sb.append("<tr><td style='padding:36px 40px;text-align:center;'>");
        sb.append("<div style='width:64px;height:64px;background:#f3f0ff;border-radius:50%;margin:0 auto 20px;");
        sb.append("display:flex;align-items:center;justify-content:center;font-size:28px;'>🔐</div>");
        sb.append("<h2 style='color:#3d3d3d;margin:0 0 10px;font-size:20px;'>Hola, ").append(nombre != null ? nombre : "usuario").append("</h2>");
        sb.append("<p style='color:#666;font-size:14px;line-height:1.6;margin:0 0 28px;'>");
        sb.append("Recibimos una solicitud para restablecer tu contraseña.<br>Usa el siguiente código:");
        sb.append("</p>");

        // Código grande
        sb.append("<div style='background:#f8f6ff;border:2px dashed #c5c2df;border-radius:12px;");
        sb.append("padding:24px 32px;margin:0 auto 28px;display:inline-block;'>");
        sb.append("<p style='margin:0 0 6px;color:#9177a8;font-size:12px;font-weight:bold;letter-spacing:1px;'>TU CÓDIGO</p>");
        sb.append("<span style='font-size:40px;font-weight:900;letter-spacing:10px;color:#9177a8;font-family:monospace;'>");
        sb.append(codigo).append("</span>");
        sb.append("</div>");

        sb.append("<p style='color:#999;font-size:13px;line-height:1.5;'>");
        sb.append("Este código es válido por <strong>15 minutos</strong>.<br>");
        sb.append("Si no solicitaste este cambio, ignora este mensaje.");
        sb.append("</p></td></tr>");

        // Pie
        sb.append("<tr><td style='background:#f8fafc;padding:18px;text-align:center;border-top:1px solid #e2e8f0;'>");
        sb.append("<p style='margin:0;color:#bbb;font-size:11px;'>© 2025 AAC27 · Correo generado automáticamente</p>");
        sb.append("</td></tr></table></td></tr></table></body></html>");
        return sb.toString();
    }
    /**
     * Envía una consulta de soporte técnico al equipo de desarrollo.
     * @param nombreAdmin nombre del administrador que consulta
     * @param asunto el tema de la consulta
     * @param mensaje el contenido de la duda
     * @return boolean true si se envió correctamente
     */
    public static boolean enviarConsultaSoporte(String nombreAdmin, String asunto, String mensaje) {
        try {
            Properties config = cargarConfiguracion();
            if (config == null) return false;

            final String remitente = config.getProperty("mail.from");
            final String appPass = config.getProperty("mail.password");
            // El destino siempre será el correo de soporte
            final String destinoSoporte = "marlenbe211@gmail.com"; 

            Properties smtpProps = new Properties();
            smtpProps.put("mail.smtp.auth", "true");
            smtpProps.put("mail.smtp.starttls.enable", "true");
            smtpProps.put("mail.smtp.host", config.getProperty("mail.smtp.host", "smtp.gmail.com"));
            smtpProps.put("mail.smtp.port", config.getProperty("mail.smtp.port", "587"));

            Session session = Session.getInstance(smtpProps, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(remitente, appPass);
                }
            });

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(remitente, "AAC27 - Soporte Interno"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinoSoporte));
            
            // Asunto personalizado para que sepas quién escribe
            msg.setSubject("TICKET SOPORTE: " + asunto + " (De: " + nombreAdmin + ")");
            
            // Construimos un HTML sencillo pero profesional para el mensaje
            String htmlContent = buildHtmlSoporte(nombreAdmin, asunto, mensaje);
            msg.setContent(htmlContent, "text/html; charset=UTF-8");

            Transport.send(msg);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private static String buildHtmlSoporte(String nombre, String asunto, String mensaje) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family: Arial, sans-serif; border: 1px solid #c5c2df; border-radius: 8px; overflow: hidden; max-width: 600px;'>");
        sb.append("<div style='background: #1a1a2e; color: white; padding: 20px; text-align: center;'>");
        sb.append("<h1>Nueva Consulta de Soporte</h1></div>");
        sb.append("<div style='padding: 25px; color: #333;'>");
        sb.append("<p><strong>Administrador:</strong> ").append(nombre).append("</p>");
        sb.append("<p><strong>Asunto:</strong> ").append(asunto).append("</p>");
        sb.append("<hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>");
        sb.append("<p style='background: #f9f9f9; padding: 15px; border-radius: 5px;'>").append(mensaje).append("</p>");
        sb.append("</div>");
        sb.append("<div style='background: #f4f6f8; padding: 10px; text-align: center; font-size: 12px; color: #999;'>");
        sb.append("Enviado desde el sistema de ayuda de AAC27</div></div>");
        return sb.toString();
    }
}