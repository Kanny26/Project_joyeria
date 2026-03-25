package controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Cierra la sesión del usuario de forma segura.
 * Elimina los atributos de sesión uno por uno antes de invalidarla,
 * y configura cabeceras para que el navegador no pueda volver a las páginas
 * protegidas usando el botón "Atrás" después de cerrar sesión.
 */
@WebServlet("/CerrarSesionServlet")
public class CerrarSesionServlet extends HttpServlet {

    /**
     * Maneja las peticiones GET para cerrar la sesión.
     * Obtiene la sesión activa si existe, elimina sus atributos,
     * la invalida y redirige al usuario a la página de inicio.
     * También configura cabeceras anti-caché para evitar acceso a páginas protegidas.
     *
     * @param request objeto HttpServletRequest que contiene la petición del cliente
     * @param response objeto HttpServletResponse para enviar la respuesta al cliente
     * @throws ServletException si ocurre un error en el procesamiento del servlet
     * @throws IOException si ocurre un error de entrada/salida durante el manejo
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false); // false: no crear sesión si no existe
        if (session != null) {
            // Se eliminan los atributos antes de invalidar la sesión
            // para liberar los objetos en memoria de forma explícita.
            session.removeAttribute("admin");
            session.removeAttribute("vendedor");
            session.removeAttribute("rol");
            session.removeAttribute("passTemporal");
            // invalidate() destruye la sesión completamente en el servidor.
            session.invalidate();
        }

        /*
         * Cabeceras anti-caché: impiden que el navegador guarde la última página visitada.
         * Sin esto, el usuario podría presionar "Atrás" y ver una página protegida
         * aunque ya haya cerrado sesión, porque el navegador la mostraría desde caché.
         */
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // Redirige a la página de inicio pública después de cerrar sesión.
        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }

    /**
     * Maneja las peticiones POST para cerrar la sesión.
     * Delega la lógica al método doGet para mantener un comportamiento consistente
     * independientemente del método HTTP utilizado.
     *
     * @param request objeto HttpServletRequest que contiene la petición del cliente
     * @param response objeto HttpServletResponse para enviar la respuesta al cliente
     * @throws ServletException si ocurre un error en el procesamiento del servlet
     * @throws IOException si ocurre un error de entrada/salida durante el manejo
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // El cierre de sesión puede hacerse por GET o POST; ambos ejecutan la misma lógica.
        doGet(request, response);
    }
}