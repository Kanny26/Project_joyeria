package controller;

import dao.AuthDAO;
import model.Administrador;
import model.Usuario;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * Maneja el inicio de sesión unificado para todos los roles del sistema.
 *
 * Atiende peticiones POST desde el formulario de /inicio-sesion.jsp.
 * Después de validar las credenciales, crea la sesión y redirige al panel
 * correspondiente según el rol (administrador, superadministrador o vendedor).
 */
@WebServlet("/loginUnificado")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nombre = request.getParameter("usuario");
        String pass   = request.getParameter("password");

        // Validación mínima: ninguno de los dos campos puede llegar vacío o nulo.
        // isBlank() detecta cadenas null, vacías o que solo contienen espacios.
        // Si faltan, se redirige al login con el parámetro ?error=campos para mostrar el aviso.
        if (nombre == null || nombre.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/inicio-sesion.jsp?error=campos");
            return;
        }

        AuthDAO authDAO = new AuthDAO();
        // validar() compara las credenciales contra la BD usando BCrypt.
        // Retorna null si el usuario no existe, está inactivo o la contraseña es incorrecta.
        Map<String, Object> datos = authDAO.validar(nombre, pass);

        if (datos == null) {
            // forward (no redirect) para que el JSP pueda leer el atributo "error"
            // del request actual. Con sendRedirect se perdería ese atributo.
            request.setAttribute("error", "Usuario o contraseña incorrectos");
            request.getRequestDispatcher("/inicio-sesion.jsp").forward(request, response);
            return;
        }

        /*
         * Prevención de fijación de sesión (Session Fixation Attack):
         * Se invalida la sesión anterior antes de crear una nueva.
         * Sin esto, un atacante podría forzar un ID de sesión conocido y
         * luego reutilizarlo después de que la víctima se autentique.
         */
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = request.getSession(true); // true: crear sesión nueva
        session.setMaxInactiveInterval(900); // Expira tras 15 minutos de inactividad (900 segundos)

        // Cabeceras para que el navegador no guarde en caché las páginas autenticadas.
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        String rol = (String) datos.get("rol");

        /*
         * Según el rol autenticado, se configura la sesión con el objeto correspondiente
         * y se redirige al panel adecuado.
         *
         * sendRedirect: envía al navegador a una nueva URL (cambia la URL en la barra del navegador).
         * forward: transfiere la petición al JSP sin cambiar la URL (ya se usó arriba para errores).
         */
        switch (rol) {
            case "superadministrador":
            case "administrador": {
                // Ambos roles comparten el mismo panel y el mismo tipo de objeto en sesión.
                // El rol exacto se guarda aparte para diferenciar permisos dentro del sistema.
                Administrador admin = new Administrador();
                admin.setId((int) datos.get("id"));
                admin.setNombre((String) datos.get("nombre"));

                session.setAttribute("admin", admin);
                session.setAttribute("rol", rol); // Guardado para verificar si es superadministrador en ciertos módulos

                response.sendRedirect(request.getContextPath() + "/Administrador/admin-principal.jsp");
                break;
            }
            case "vendedor": {
                Usuario vendedor = new Usuario();
                vendedor.setUsuarioId((int) datos.get("id"));
                vendedor.setNombre((String) datos.get("nombre"));
                vendedor.setRol(rol);

                session.setAttribute("vendedor", vendedor);
                session.setAttribute("rol", "vendedor");

                response.sendRedirect(request.getContextPath() + "/vendedor/vendedor_principal.jsp");
                break;
            }
            default:
                // Si el rol en BD es uno desconocido, se rechaza el acceso para no exponer el sistema.
                request.setAttribute("error", "Rol no autorizado");
                request.getRequestDispatcher("/inicio-sesion.jsp").forward(request, response);
        }
    }
}
