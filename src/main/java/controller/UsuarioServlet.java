package controller;

import dao.UsuarioDAO;
import dao.DesempenoDAO;
import model.Desempeno_Vendedor;
import model.Usuario;
import config.ConexionDB;
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
                req.getRequestDispatcher("/Administrador/usuarios/agregar_usuario.jsp").forward(req, resp);
                break;

            case "editar":
                int id = Integer.parseInt(req.getParameter("id"));
                req.setAttribute("usuario", usuarioDAO.obtenerUsuarioPorId(id));
                req.getRequestDispatcher("/Administrador/usuarios/editar_usuario.jsp").forward(req, resp);
                break;

            case "historial":
                List<Map<String, Object>> historial = usuarioDAO.obtenerHistorialUsuariosConDesempeno();
                req.setAttribute("historial", historial);
                req.getRequestDispatcher("/Administrador/usuarios/historial.jsp").forward(req, resp);
                break;
                
            default:
                req.setAttribute("usuarios", usuarioDAO.listarUsuarios());
                req.setAttribute("totalUsuarios", usuarioDAO.contarUsuarios());
                req.setAttribute("usuariosActivos", usuarioDAO.contarUsuariosActivos());
                req.getRequestDispatcher("/Administrador/usuarios/listar_usuario.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String accion = req.getParameter("accion");

        if ("agregar".equals(accion)) {
            Usuario u = new Usuario();
            u.setNombre(req.getParameter("nombre"));
            u.setCorreo(req.getParameter("correo"));
            u.setTelefono(req.getParameter("telefono"));
            u.setDocumento(req.getParameter("documento"));
            u.setContrasena(req.getParameter("contrasena"));
            u.setEstado("Activo".equals(req.getParameter("estado")));

            usuarioDAO.agregarUsuario(u);
            resp.sendRedirect("UsuarioServlet");

        }else if ("editar".equals(accion)) {
            try {
                int usuarioId = Integer.parseInt(req.getParameter("id"));
                String observaciones = req.getParameter("observaciones");

                // 1. Actualizar datos del usuario (nombre, correo, etc.)
                Usuario u = new Usuario();
                u.setUsuarioId(usuarioId);
                u.setNombre(req.getParameter("nombre"));
                u.setCorreo(req.getParameter("correo"));
                u.setTelefono(req.getParameter("telefono"));
                u.setEstado("Activo".equals(req.getParameter("estado")));

                // --- NUEVO: actualizar rol ---
                String rol = req.getParameter("rol");
                u.setRol(rol);
                // ------------------------------

                boolean exito = usuarioDAO.editarUsuario(u);

                // 2. Actualizar/crear desempeño con observaciones
                if (exito) {
                    Desempeno_Vendedor desempeno = desempenoDAO.obtenerUltimoDesempenoPorUsuario(usuarioId);
                    
                    if (desempeno != null) {
                        desempeno.setObservaciones(observaciones);
                        desempenoDAO.actualizarDesempeno(desempeno);
                    } else {
                        desempeno = new Desempeno_Vendedor();
                        desempeno.setUsuarioId(usuarioId);
                        desempeno.setVentasTotales(BigDecimal.ZERO);
                        desempeno.setComisionPorcentaje(BigDecimal.ZERO);
                        desempeno.setComisionGanada(BigDecimal.ZERO);
                        desempeno.setPeriodo(new java.sql.Date(System.currentTimeMillis()));
                        desempeno.setObservaciones(observaciones);
                        desempenoDAO.insertarDesempeno(desempeno);
                    }
                }

                resp.sendRedirect(req.getContextPath() + "/UsuarioServlet");
            } catch (Exception e) {
                e.printStackTrace();
                resp.sendRedirect(req.getContextPath() + "/UsuarioServlet?error=editar");
            }
        }

    }
}
