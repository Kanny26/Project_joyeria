package controller;

import dao.UsuarioDAO;
import model.Usuario;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/UsuarioServlet")
public class UsuarioServlet extends HttpServlet {

    private UsuarioDAO usuarioDAO;

    // ==================== PATRONES DE VALIDACIÓN ====================

    // Nombre de usuario: solo letras (con o sin tilde), sin espacios ni números.
    // Máximo 10 caracteres. Se usa como nombre de login, por eso debe ser limpio.
    private static final String REGEX_NOMBRE   = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ]{1,10}$";

    // Correo: formato estándar usuario@dominio.extension.
    // Se permiten puntos, guiones y guion bajo antes del @.
    // El dominio debe tener al menos 2 caracteres de extensión.
    private static final String REGEX_CORREO   = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";

    // Teléfono: solo dígitos, debe iniciar con 3, entre 10 y 15 caracteres.
    private static final String REGEX_TELEFONO = "^3[0-9]{9,14}$";

    // Mensajes de error humanizados para cada validación de backend.
    // Se usan en reenviarConError() para que el JSP los muestre al usuario.
    private static final String MSG_NOMBRE_VACIO    = "El nombre del usuario es obligatorio.";
    private static final String MSG_NOMBRE_FORMATO  = "El usuario solo puede contener letras y máximo 10 caracteres. No se permiten números, espacios ni símbolos.";
    private static final String MSG_CORREO_VACIO    = "El correo electrónico es obligatorio.";
    private static final String MSG_CORREO_FORMATO  = "El correo ingresado no es válido. Debe tener el formato: usuario@dominio.com";
    private static final String MSG_CORREO_DUPLICADO = "Ya existe un usuario registrado con ese correo.";
    private static final String MSG_TELEFONO_VACIO  = "El teléfono es obligatorio.";
    private static final String MSG_TELEFONO_FORMATO = "El teléfono solo puede contener números, debe iniciar con 3 y tener entre 10 y 15 dígitos.";

    @Override
    public void init() {
        usuarioDAO = new UsuarioDAO();
    }

    // ==================== GET ====================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // Seguridad: solo admin y superadmin pueden acceder a este módulo
        HttpSession session = req.getSession(false);
        if (session == null || (session.getAttribute("admin") == null && session.getAttribute("superadmin") == null)) {
            resp.sendRedirect(req.getContextPath() + "/inicio-sesion.jsp");
            return;
        }

        String accion = req.getParameter("accion");
        if (accion == null) accion = "listar";

        switch (accion) {
            case "nuevo" -> req.getRequestDispatcher("/Administrador/usuarios/agregar_usuario.jsp").forward(req, resp);

            case "editar" -> {
                int id = Integer.parseInt(req.getParameter("id"));
                req.setAttribute("usuario", usuarioDAO.obtenerUsuarioPorId(id));
                req.getRequestDispatcher("/Administrador/usuarios/editar_usuario.jsp").forward(req, resp);
            }

            case "historial" -> {
                List<Map<String, Object>> historial = usuarioDAO.obtenerHistorialUsuariosConDesempeno();
                req.setAttribute("historial", historial);
                req.getRequestDispatcher("/Administrador/usuarios/historial.jsp").forward(req, resp);
            }

            // Endpoint AJAX para verificar si un correo ya está registrado
            case "verificarCorreo" -> {
                String correo = req.getParameter("correo");
                String idActualStr = req.getParameter("idActual");
                boolean existe;
                // matches("\\d+") verifica que sea un número antes de parsearlo
                if (idActualStr != null && idActualStr.matches("\\d+")) {
                    existe = usuarioDAO.existeCorreoParaOtro(correo, Integer.parseInt(idActualStr));
                } else {
                    existe = usuarioDAO.existeCorreo(correo);
                }
                resp.setContentType("application/json");
                resp.getWriter().write("{\"existe\": " + existe + "}");
            }

            default -> {
                String filtroRol    = req.getParameter("filtroRol");
                String filtroEstado = req.getParameter("filtroEstado");

                String msg = req.getParameter("msg");
                if (msg != null) req.setAttribute("msg", msg);

                String correoDestino = req.getParameter("correoDestino");
                if (correoDestino != null) req.setAttribute("correoDestino", correoDestino);

                req.setAttribute("usuarios",        usuarioDAO.listarUsuarios(filtroRol, filtroEstado));
                req.setAttribute("totalUsuarios",   usuarioDAO.contarUsuarios());
                req.setAttribute("usuariosActivos", usuarioDAO.contarUsuariosActivos());
                req.getRequestDispatcher("/Administrador/usuarios.jsp").forward(req, resp);
            }
        }
    }

    // ==================== POST ====================
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // Seguridad: verificar sesión antes de procesar cualquier dato
        HttpSession session = req.getSession(false);
        if (session == null || (session.getAttribute("admin") == null && session.getAttribute("superadmin") == null)) {
            resp.sendRedirect(req.getContextPath() + "/inicio-sesion.jsp");
            return;
        }

        // Obtener el ID del administrador que realiza la acción para auditoría
        Object adminObj = session.getAttribute("admin");
        Object superAdminObj = session.getAttribute("superadmin");
        int adminId = -1;
        if (adminObj instanceof model.Administrador) {
            adminId = ((model.Administrador) adminObj).getId();
        } else if (superAdminObj instanceof model.Usuario) {
            adminId = ((model.Usuario) superAdminObj).getUsuarioId();
        }

        String accion = req.getParameter("accion");

        /* ========== AGREGAR ========== */
        if ("agregar".equals(accion)) {
            String nombre     = req.getParameter("nombre");
            String correo     = req.getParameter("correo");
            String telefono   = req.getParameter("telefono");
            String contrasena = req.getParameter("contrasena");
            String rol        = req.getParameter("rol");

            final String VISTA_AGREGAR = "/Administrador/usuarios/agregar_usuario.jsp";

            // Validación 1: nombre obligatorio
            if (nombre == null || nombre.trim().isEmpty()) {
                reenviarConError(req, resp, MSG_NOMBRE_VACIO, VISTA_AGREGAR);
                return;
            }
            // Validación 2: nombre — solo letras, máximo 10 caracteres, sin espacios ni símbolos.
            // matches() compara el valor completo contra el patrón REGEX_NOMBRE.
            if (!nombre.trim().matches(REGEX_NOMBRE)) {
                reenviarConError(req, resp, MSG_NOMBRE_FORMATO, VISTA_AGREGAR);
                return;
            }

            // Validación 3: correo obligatorio
            if (correo == null || correo.trim().isEmpty()) {
                reenviarConError(req, resp, MSG_CORREO_VACIO, VISTA_AGREGAR);
                return;
            }
            // Validación 4: correo con formato estándar (usuario@dominio.ext).
            // El patrón REGEX_CORREO permite puntos, guiones y guion bajo antes del @,
            // y requiere al menos 2 caracteres en la extensión del dominio.
            if (!correo.trim().matches(REGEX_CORREO)) {
                reenviarConError(req, resp, MSG_CORREO_FORMATO, VISTA_AGREGAR);
                return;
            }

            // Validación 5: teléfono obligatorio
            if (telefono == null || telefono.trim().isEmpty()) {
                reenviarConError(req, resp, MSG_TELEFONO_VACIO, VISTA_AGREGAR);
                return;
            }
            // Validación 6: teléfono — solo números, inicia con 3, entre 10 y 15 dígitos.
            if (!telefono.trim().matches(REGEX_TELEFONO)) {
                reenviarConError(req, resp, MSG_TELEFONO_FORMATO, VISTA_AGREGAR);
                return;
            }

            // Validación 7: correo no duplicado en la base de datos
            if (usuarioDAO.existeCorreo(correo)) {
                reenviarConError(req, resp, MSG_CORREO_DUPLICADO, VISTA_AGREGAR);
                return;
            }

            // Validación de jerarquía: solo superadmin puede crear administradores
            String rolNormalizado = rol != null ? rol.trim().toLowerCase() : "vendedor";
            String rolSesion = session.getAttribute("rol") != null ? session.getAttribute("rol").toString().toLowerCase() : "";

            if (rolNormalizado.equals("administrador") && !rolSesion.equals("superadministrador")) {
                reenviarConError(req, resp, "No tienes permiso para crear usuarios con rol de administrador.", VISTA_AGREGAR);
                return;
            }
            if (!rolNormalizado.equals("vendedor") && !rolNormalizado.equals("administrador")) {
                reenviarConError(req, resp, "El rol seleccionado no es válido.", VISTA_AGREGAR);
                return;
            }

            Usuario u = new Usuario();
            u.setNombre(nombre.trim());
            u.setCorreo(correo.trim());
            u.setTelefono(telefono.trim());
            u.setContrasena(contrasena);
            u.setRol(rolNormalizado);
            u.setEstado("Activo".equals(req.getParameter("estado")));

            boolean exito = usuarioDAO.agregarUsuario(u);
            if (exito) {
                resp.sendRedirect(req.getContextPath() + "/UsuarioServlet?msg=creado&correoDestino="
                        + java.net.URLEncoder.encode(correo.trim(), "UTF-8"));
            } else {
                reenviarConError(req, resp, "No se pudo crear el usuario. Verifica que el nombre o correo no estén repetidos.",
                        VISTA_AGREGAR);
            }

        /* ========== EDITAR ========== */
        } else if ("editar".equals(accion)) {
            try {
                int    usuarioId = Integer.parseInt(req.getParameter("id"));
                String nombre    = req.getParameter("nombre");
                String correo    = req.getParameter("correo");
                String telefono  = req.getParameter("telefono");
                String rol       = req.getParameter("rol");
                String estado    = req.getParameter("estado");

                final String VISTA_EDITAR = "/Administrador/usuarios/editar_usuario.jsp";

                Usuario usuarioActual = usuarioDAO.obtenerUsuarioPorId(usuarioId);

                // Bloquear auto-cambio de rol
                if (usuarioActual != null && rol != null && adminId == usuarioId) {
                    reenviarConErrorEditar(req, resp, "No puedes cambiar tu propio rol.", usuarioActual, VISTA_EDITAR);
                    return;
                }

                // Bloquear auto-inactivación
                if (adminId == usuarioId && "Inactivo".equals(estado)) {
                    reenviarConErrorEditar(req, resp, "No puedes inactivarte a ti mismo.", usuarioActual, VISTA_EDITAR);
                    return;
                }

                // Validación: campos obligatorios
                if (nombre == null || nombre.trim().isEmpty()
                        || correo == null || correo.trim().isEmpty()
                        || rol   == null || rol.trim().isEmpty()) {
                    reenviarConErrorEditar(req, resp, "Por favor completa todos los campos obligatorios: nombre, correo y rol.",
                            usuarioDAO.obtenerUsuarioPorId(usuarioId), VISTA_EDITAR);
                    return;
                }

                // Validación: formato del nombre — mismas reglas que al crear
                if (!nombre.trim().matches(REGEX_NOMBRE)) {
                    reenviarConErrorEditar(req, resp, MSG_NOMBRE_FORMATO,
                            usuarioDAO.obtenerUsuarioPorId(usuarioId), VISTA_EDITAR);
                    return;
                }

                // Validación: formato del correo
                if (!correo.trim().matches(REGEX_CORREO)) {
                    reenviarConErrorEditar(req, resp, MSG_CORREO_FORMATO,
                            usuarioDAO.obtenerUsuarioPorId(usuarioId), VISTA_EDITAR);
                    return;
                }

                // Validación: el teléfono es opcional en edición, pero si se ingresa debe ser válido
                if (telefono != null && !telefono.trim().isEmpty() && !telefono.trim().matches(REGEX_TELEFONO)) {
                    reenviarConErrorEditar(req, resp, MSG_TELEFONO_FORMATO,
                            usuarioDAO.obtenerUsuarioPorId(usuarioId), VISTA_EDITAR);
                    return;
                }

                // Validación: el correo no puede estar en uso por otro usuario diferente
                if (usuarioDAO.existeCorreoParaOtro(correo, usuarioId)) {
                    reenviarConErrorEditar(req, resp, "El correo ya está en uso por otro usuario.",
                            usuarioDAO.obtenerUsuarioPorId(usuarioId), VISTA_EDITAR);
                    return;
                }

                // Validación de jerarquía en edición
                String rolNormalizado = rol.trim().toLowerCase();
                String rolSesion = session.getAttribute("rol") != null ? session.getAttribute("rol").toString().toLowerCase() : "";

                if (rolNormalizado.equals("administrador") && !rolSesion.equals("superadministrador")) {
                    reenviarConErrorEditar(req, resp, "No tienes permiso para asignar el rol de administrador.",
                            usuarioDAO.obtenerUsuarioPorId(usuarioId), VISTA_EDITAR);
                    return;
                }

                Usuario u = new Usuario();
                u.setUsuarioId(usuarioId);
                u.setNombre(nombre.trim());
                u.setCorreo(correo.trim().toLowerCase());
                u.setTelefono(telefono);
                u.setRol(rolNormalizado);
                u.setEstado("Activo".equals(estado));

                boolean exito = usuarioDAO.editarUsuario(u, adminId);
                if (exito) {
                    resp.sendRedirect(req.getContextPath() + "/UsuarioServlet?msg=actualizado");
                } else {
                    reenviarConErrorEditar(req, resp, "No se pudo actualizar el usuario. Inténtalo de nuevo.",
                            usuarioDAO.obtenerUsuarioPorId(usuarioId), VISTA_EDITAR);
                }

            } catch (Exception e) {
                e.printStackTrace();
                resp.sendRedirect(req.getContextPath() + "/UsuarioServlet?error=editar");
            }
        }
    }

    // Reenvía a una vista de formulario con un mensaje de error (usado en agregar)
    private void reenviarConError(HttpServletRequest req, HttpServletResponse resp, String mensaje, String vista)
            throws ServletException, IOException {
        req.setAttribute("error", mensaje);
        req.getRequestDispatcher(vista).forward(req, resp);
    }

    // Variante para editar: también requiere pasar el objeto usuario para repoblar el formulario
    private void reenviarConErrorEditar(HttpServletRequest req, HttpServletResponse resp,
                                        String mensaje, Usuario usuario, String vista)
            throws ServletException, IOException {
        req.setAttribute("error", mensaje);
        req.setAttribute("usuario", usuario);
        req.getRequestDispatcher(vista).forward(req, resp);
    }
}
