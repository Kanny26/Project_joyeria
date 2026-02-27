package controller;

import dao.DesempenoDAO;
import dao.UsuarioDAO;
import model.Desempeno_Vendedor;
import model.Usuario;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@WebServlet("/UsuarioServlet")
public class UsuarioServlet extends HttpServlet {

    private UsuarioDAO usuarioDAO;
    private DesempenoDAO desempenoDAO;

    @Override
    public void init() {
        usuarioDAO = new UsuarioDAO();
        desempenoDAO = new DesempenoDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String accion = req.getParameter("accion");
        if (accion == null) accion = "listar";

        switch (accion) {
            case "nuevo":
                req.getRequestDispatcher("/Administrador/usuarios/agregar_usuario.jsp")
                        .forward(req, resp);
                break;

            case "editar":
                int id = Integer.parseInt(req.getParameter("id"));
                req.setAttribute("usuario", usuarioDAO.obtenerUsuarioPorId(id));
                req.getRequestDispatcher("/Administrador/usuarios/editar_usuario.jsp")
                        .forward(req, resp);
                break;

            case "historial":
                List<Map<String, Object>> historial =
                        usuarioDAO.obtenerHistorialUsuariosConDesempeno();
                req.setAttribute("historial", historial);
                req.getRequestDispatcher("/Administrador/usuarios/historial.jsp")
                        .forward(req, resp);
                break;

            default:
                req.setAttribute("usuarios", usuarioDAO.listarUsuarios());
                req.setAttribute("totalUsuarios", usuarioDAO.contarUsuarios());
                req.setAttribute("usuariosActivos", usuarioDAO.contarUsuariosActivos());
                req.getRequestDispatcher("/Administrador/usuarios/listar_usuario.jsp")
                        .forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String accion = req.getParameter("accion");

        /* =========================
           AGREGAR USUARIO
           ========================= */
        if ("agregar".equals(accion)) {

            String nombre     = req.getParameter("nombre");
            String correo     = req.getParameter("correo");
            String telefono   = req.getParameter("telefono");
            String documento  = req.getParameter("documento");
            String contrasena = req.getParameter("contrasena");
            String rol        = req.getParameter("rol");

            // ---- VALIDACIONES ----
            if (nombre == null || nombre.trim().isEmpty()
                    || correo == null || correo.trim().isEmpty()
                    || telefono == null || telefono.trim().isEmpty()) {

                req.setAttribute("error", "Todos los campos obligatorios deben completarse");
                req.getRequestDispatcher("/Administrador/usuarios/agregar_usuario.jsp")
                        .forward(req, resp);
                return;
            }

            if (!correo.matches("^[^@]+@[^@]+\\.[^@]+$")) {
                req.setAttribute("error", "El correo no tiene un formato válido");
                req.getRequestDispatcher("/Administrador/usuarios/agregar_usuario.jsp")
                        .forward(req, resp);
                return;
            }

            if (contrasena == null || contrasena.trim().isEmpty()) {
                req.setAttribute("error", "La contraseña es obligatoria");
                req.getRequestDispatcher("/Administrador/usuarios/agregar_usuario.jsp")
                        .forward(req, resp);
                return;
            }
            // ----------------------

            Usuario u = new Usuario();
            u.setNombre(nombre.trim());
            u.setCorreo(correo.trim());
            u.setTelefono(telefono.trim());
            u.setDocumento(documento);
            u.setContrasena(contrasena);
            u.setRol(rol != null && !rol.trim().isEmpty() ? rol.trim() : "vendedor");
            u.setEstado("Activo".equals(req.getParameter("estado")));

            usuarioDAO.agregarUsuario(u);
            resp.sendRedirect("UsuarioServlet");
        }

        /* =========================
           EDITAR USUARIO
           ========================= */
        else if ("editar".equals(accion)) {

            try {
                int usuarioId = Integer.parseInt(req.getParameter("id"));
                String nombre  = req.getParameter("nombre");
                String correo  = req.getParameter("correo");
                String telefono = req.getParameter("telefono");
                String rol     = req.getParameter("rol");
                String observaciones = req.getParameter("observaciones");

                // ---- VALIDACIONES ----
                if (nombre == null || nombre.trim().isEmpty()
                        || correo == null || correo.trim().isEmpty()
                        || rol == null || rol.trim().isEmpty()) {

                    req.setAttribute("error", "Nombre, correo y rol son obligatorios");
                    req.setAttribute("usuario",
                            usuarioDAO.obtenerUsuarioPorId(usuarioId));
                    req.getRequestDispatcher("/Administrador/usuarios/editar_usuario.jsp")
                            .forward(req, resp);
                    return;
                }

                if (!correo.matches("^[^@]+@[^@]+\\.[^@]+$")) {
                    req.setAttribute("error", "Formato de correo inválido");
                    req.setAttribute("usuario",
                            usuarioDAO.obtenerUsuarioPorId(usuarioId));
                    req.getRequestDispatcher("/Administrador/usuarios/editar_usuario.jsp")
                            .forward(req, resp);
                    return;
                }
                // ----------------------

                Usuario u = new Usuario();
                u.setUsuarioId(usuarioId);
                u.setNombre(nombre);
                u.setCorreo(correo);
                u.setTelefono(telefono);
                u.setRol(rol);
                u.setEstado("Activo".equals(req.getParameter("estado")));

                boolean exito = usuarioDAO.editarUsuario(u);

                if (exito) {
                    Desempeno_Vendedor desempeno =
                            desempenoDAO.obtenerUltimoDesempenoPorUsuario(usuarioId);

                    if (desempeno != null) {
                        desempeno.setObservaciones(observaciones);
                        desempenoDAO.actualizarDesempeno(desempeno);
                    } else {
                        desempeno = new Desempeno_Vendedor();
                        desempeno.setUsuarioId(usuarioId);
                        desempeno.setVentasTotales(BigDecimal.ZERO);
                        desempeno.setComisionPorcentaje(BigDecimal.ZERO);
                        desempeno.setComisionGanada(BigDecimal.ZERO);
                        desempeno.setPeriodo(
                                new java.sql.Date(System.currentTimeMillis()));
                        desempeno.setObservaciones(observaciones);

                        desempenoDAO.insertarDesempeno(desempeno);
                    }
                }

                resp.sendRedirect(req.getContextPath() + "/UsuarioServlet");

            } catch (Exception e) {
                e.printStackTrace();
                resp.sendRedirect(req.getContextPath()
                        + "/UsuarioServlet?error=editar");
            }
        }
    }
}