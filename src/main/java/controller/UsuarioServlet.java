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

    @Override
    public void init() {
        usuarioDAO = new UsuarioDAO();
    }

    // ==================== GET ====================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        // ✅ PERMITIR ACCESO A SUPERADMINISTRADOR Y ADMINISTRADOR
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

            case "verificarDocumento" -> {
                String doc = req.getParameter("documento");
                String idActualStr = req.getParameter("idActual");
                boolean existe;
                if (idActualStr != null && idActualStr.matches("\\d+")) {
                    existe = usuarioDAO.existeDocumentoParaOtro(doc, Integer.parseInt(idActualStr));
                } else {
                    existe = usuarioDAO.existeDocumento(doc);
                }
                resp.setContentType("application/json");
                resp.getWriter().write("{\"existe\": " + existe + "}");
            }

            case "verificarCorreo" -> {
                String correo = req.getParameter("correo");
                String idActualStr = req.getParameter("idActual");
                boolean existe;
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

        HttpSession session = req.getSession(false);
        // ✅ PERMITIR ACCESO A SUPERADMINISTRADOR Y ADMINISTRADOR
        if (session == null || (session.getAttribute("admin") == null && session.getAttribute("superadmin") == null)) {
            resp.sendRedirect(req.getContextPath() + "/inicio-sesion.jsp");
            return;
        }

        // Obtener ID del usuario que realiza la acción (puede ser admin o superadmin)
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
            String documento  = req.getParameter("documento");
            String contrasena = req.getParameter("contrasena");
            String rol        = req.getParameter("rol");

            if (nombre == null || nombre.trim().isEmpty()) {
                reenviarConError(req, resp, "El nombre es obligatorio.", "/Administrador/usuarios/agregar_usuario.jsp");
                return;
            }
            if (correo == null || correo.trim().isEmpty()) {
                reenviarConError(req, resp, "El correo es obligatorio.", "/Administrador/usuarios/agregar_usuario.jsp");
                return;
            }
            if (!correo.matches("^[^@]+@[^@]+\\.[^@]+$")) {
                reenviarConError(req, resp, "El correo no tiene un formato válido.", "/Administrador/usuarios/agregar_usuario.jsp");
                return;
            }
            if (telefono == null || telefono.trim().isEmpty()) {
                reenviarConError(req, resp, "El teléfono es obligatorio.", "/Administrador/usuarios/agregar_usuario.jsp");
                return;
            }
            if (documento != null && !documento.trim().isEmpty() && usuarioDAO.existeDocumento(documento)) {
                reenviarConError(req, resp, "Ya existe un usuario con ese documento.", "/Administrador/usuarios/agregar_usuario.jsp");
                return;
            }
            if (usuarioDAO.existeCorreo(correo)) {
                reenviarConError(req, resp, "Ya existe un usuario con ese correo.", "/Administrador/usuarios/agregar_usuario.jsp");
                return;
            }

            // ✅ VALIDACIÓN DE JERARQUÍA: Solo superadmin puede crear admins
            String rolNormalizado = rol != null ? rol.trim().toLowerCase() : "vendedor";
            String rolSesion = session.getAttribute("rol") != null ? session.getAttribute("rol").toString().toLowerCase() : "";
            
            if (rolNormalizado.equals("administrador") && !rolSesion.equals("superadministrador")) {
                reenviarConError(req, resp, "No tienes permiso para crear usuarios con rol de administrador.", 
                        "/Administrador/usuarios/agregar_usuario.jsp");
                return;
            }
            if (!rolNormalizado.equals("vendedor") && !rolNormalizado.equals("administrador")) {
                reenviarConError(req, resp, "Rol no válido.", "/Administrador/usuarios/agregar_usuario.jsp");
                return;
            }

            Usuario u = new Usuario();
            u.setNombre(nombre.trim());
            u.setCorreo(correo.trim());
            u.setTelefono(telefono.trim());
            u.setDocumento(documento);
            u.setContrasena(contrasena);
            u.setRol(rolNormalizado);
            u.setEstado("Activo".equals(req.getParameter("estado")));

            boolean exito = usuarioDAO.agregarUsuario(u);
            if (exito) {
                resp.sendRedirect(req.getContextPath() + "/UsuarioServlet?msg=creado&correoDestino="
                        + java.net.URLEncoder.encode(correo.trim(), "UTF-8"));
            } else {
                reenviarConError(req, resp, "No se pudo crear el usuario. Verifica que el nombre, documento o correo no estén repetidos.",
                        "/Administrador/usuarios/agregar_usuario.jsp");
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

                Usuario usuarioActual = usuarioDAO.obtenerUsuarioPorId(usuarioId);

                // ■■ Bloquear auto-cambio de rol ■■
                if (usuarioActual != null && rol != null && adminId == usuarioId) {
                    req.setAttribute("error", "No puedes cambiar tu propio rol.");
                    req.setAttribute("usuario", usuarioActual);
                    req.getRequestDispatcher("/Administrador/usuarios/editar_usuario.jsp").forward(req, resp);
                    return;
                }

                // ■■ Bloquear auto-inactivación ■■
                if (adminId == usuarioId && "Inactivo".equals(estado)) {
                    req.setAttribute("error", "No puedes inactivarte a ti mismo.");
                    req.setAttribute("usuario", usuarioActual);
                    req.getRequestDispatcher("/Administrador/usuarios/editar_usuario.jsp").forward(req, resp);
                    return;
                }

                // ■■ Validaciones obligatorias ■■
                if (nombre == null || nombre.trim().isEmpty()
                        || correo == null || correo.trim().isEmpty()
                        || rol   == null || rol.trim().isEmpty()) {
                    req.setAttribute("error", "Nombre, correo y rol son obligatorios.");
                    req.setAttribute("usuario", usuarioDAO.obtenerUsuarioPorId(usuarioId));
                    req.getRequestDispatcher("/Administrador/usuarios/editar_usuario.jsp").forward(req, resp);
                    return;
                }
                if (!correo.matches("^[^@]+@[^@]+\\.[^@]+$")) {
                    req.setAttribute("error", "Formato de correo inválido.");
                    req.setAttribute("usuario", usuarioDAO.obtenerUsuarioPorId(usuarioId));
                    req.getRequestDispatcher("/Administrador/usuarios/editar_usuario.jsp").forward(req, resp);
                    return;
                }
                if (usuarioDAO.existeCorreoParaOtro(correo, usuarioId)) {
                    req.setAttribute("error", "El correo ya está en uso por otro usuario.");
                    req.setAttribute("usuario", usuarioDAO.obtenerUsuarioPorId(usuarioId));
                    req.getRequestDispatcher("/Administrador/usuarios/editar_usuario.jsp").forward(req, resp);
                    return;
                }

                // ✅ VALIDACIÓN DE JERARQUÍA EN EDICIÓN
                String rolNormalizado = rol.trim().toLowerCase();
                String rolSesion = session.getAttribute("rol") != null ? session.getAttribute("rol").toString().toLowerCase() : "";
                
                if (rolNormalizado.equals("administrador") && !rolSesion.equals("superadministrador")) {
                    req.setAttribute("error", "No tienes permiso para asignar rol de administrador.");
                    req.setAttribute("usuario", usuarioDAO.obtenerUsuarioPorId(usuarioId));
                    req.getRequestDispatcher("/Administrador/usuarios/editar_usuario.jsp").forward(req, resp);
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
                    req.setAttribute("error", "No se pudo actualizar el usuario.");
                    req.setAttribute("usuario", usuarioDAO.obtenerUsuarioPorId(usuarioId));
                    req.getRequestDispatcher("/Administrador/usuarios/editar_usuario.jsp").forward(req, resp);
                }

            } catch (Exception e) {
                e.printStackTrace();
                resp.sendRedirect(req.getContextPath() + "/UsuarioServlet?error=editar");
            }
        }
    }

    private void reenviarConError(HttpServletRequest req, HttpServletResponse resp, String mensaje, String vista)
            throws ServletException, IOException {
        req.setAttribute("error", mensaje);
        req.getRequestDispatcher(vista).forward(req, resp);
    }
}