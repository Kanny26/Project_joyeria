package main.controllers;

import main.dao.UsuarioDAO;
import main.model.Usuario;
import main.model.Rol;
import main.util.BCryptValidator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet para manejar el proceso de inicio de sesión
 * Valida credenciales y redirige según el rol del usuario
 */
public class LoginServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    
    // DAO para acceso a datos
    private UsuarioDAO usuarioDAO;
    
    /**
     * Inicialización del servlet
     */
    @Override
    public void init() throws ServletException {
        super.init();
        usuarioDAO = new UsuarioDAO();
        System.out.println("LoginServlet inicializado correctamente");
    }
    
    /**
     * Maneja las peticiones GET
     * Redirige al formulario de login
     */
    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        
        // Si alguien accede directamente por GET, redirigir al login
        respuesta.sendRedirect(peticion.getContextPath() + "/frontend/public/Administrador/inicio-sesion.html");
    }
    
    /**
     * Maneja las peticiones POST
     * Procesa el formulario de login
     */
    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        
        // Configurar encoding para caracteres especiales
        peticion.setCharacterEncoding("UTF-8");
        respuesta.setCharacterEncoding("UTF-8");
        
        System.out.println("=== INICIO DE PROCESO DE LOGIN ===");
        
        // 1. OBTENER PARÁMETROS DEL FORMULARIO
        String nombreUsuario = peticion.getParameter("usuario");
        String passwordIngresada = peticion.getParameter("password");
        
        System.out.println("Intento de login para usuario: " + nombreUsuario);
        
        // 2. VALIDAR QUE LOS CAMPOS NO ESTÉN VACÍOS
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty() ||
            passwordIngresada == null || passwordIngresada.trim().isEmpty()) {
            
            System.out.println("Error: Campos vacios");
            manejarErrorLogin(peticion, respuesta, "Por favor complete todos los campos");
            return;
        }
        
        try {
            // 3. BUSCAR EL USUARIO EN LA BASE DE DATOS
            Usuario usuarioEncontrado = usuarioDAO.buscarUsuarioPorNombre(nombreUsuario.trim());
            
            // Verificar si el usuario existe
            if (usuarioEncontrado == null) {
                System.out.println("Error: Usuario no encontrado");
                manejarErrorLogin(peticion, respuesta, "Usuario o contraseña incorrectos");
                return;
            }
            
            // 4. VALIDAR LA CONTRASEÑA CON BCRYPT
            String passwordHasheada = usuarioEncontrado.getPass();
            boolean passwordValida = BCryptValidator.validarPassword(passwordIngresada, passwordHasheada);
            
            if (!passwordValida) {
                System.out.println("Error: Password incorrecta");
                manejarErrorLogin(peticion, respuesta, "Usuario o contraseña incorrectos");
                return;
            }
            
            System.out.println("Password validada correctamente");
            
            // 5. OBTENER EL ROL DEL USUARIO
            Rol rolDelUsuario = usuarioDAO.obtenerRolDeUsuario(usuarioEncontrado.getUsuarioId());
            
            if (rolDelUsuario == null) {
                System.out.println("Error: Usuario sin rol asignado");
                manejarErrorLogin(peticion, respuesta, "Error: Usuario sin permisos asignados");
                return;
            }
            
            System.out.println("Rol del usuario: " + rolDelUsuario.getCargo());
            
            // 6. VERIFICAR QUE SEA ADMINISTRADOR
            if (rolDelUsuario.getCargo() != Rol.TipoCargo.ADMINISTRADOR) {
                System.out.println("Error: Usuario no es administrador");
                manejarErrorLogin(peticion, respuesta, "Acceso denegado: Solo administradores");
                return;
            }
            
            // 7. CREAR SESIÓN Y GUARDAR DATOS DEL USUARIO
            HttpSession sesionUsuario = peticion.getSession(true); // Crear nueva sesión
            
            // Guardar información en la sesión
            sesionUsuario.setAttribute("usuarioId", usuarioEncontrado.getUsuarioId());
            sesionUsuario.setAttribute("nombreUsuario", usuarioEncontrado.getNombre());
            sesionUsuario.setAttribute("rolUsuario", rolDelUsuario.getCargo().getValor());
            sesionUsuario.setAttribute("nombreRol", rolDelUsuario.getNombre());
            sesionUsuario.setAttribute("usuarioLogueado", true);
            
            // Configurar tiempo de expiración de sesión (30 minutos)
            sesionUsuario.setMaxInactiveInterval(30 * 60);
            
            System.out.println("Sesion creada exitosamente para: " + nombreUsuario);
            System.out.println("=== LOGIN EXITOSO ===");
            
            // 8. REDIRIGIR AL DASHBOARD DEL ADMINISTRADOR
            String urlDashboard = peticion.getContextPath() + "/frontend/public/Administrador/admin-principal.html";
            respuesta.sendRedirect(urlDashboard);
            
        } catch (Exception excepcion) {
            // Manejar cualquier error inesperado
            System.err.println("Error inesperado durante el login:");
            excepcion.printStackTrace();
            manejarErrorLogin(peticion, respuesta, "Error del servidor. Intente nuevamente.");
        }
    }
    
    /**
     * Método auxiliar para manejar errores de login
     * Redirige de vuelta al formulario con un mensaje de error
     */
    private void manejarErrorLogin(HttpServletRequest peticion, HttpServletResponse respuesta, 
                                   String mensajeError) throws IOException {
        
        System.out.println("Redirigiendo con error: " + mensajeError);
        
        // Guardar mensaje de error en la sesión para mostrarlo en el HTML
        HttpSession sesion = peticion.getSession(true);
        sesion.setAttribute("mensajeError", mensajeError);
        
        // Redirigir de vuelta al login
        String urlLogin = peticion.getContextPath() + "/frontend/public/Administrador/inicio-sesion.html";
        respuesta.sendRedirect(urlLogin);
    }
    
    /**
     * Método para cerrar sesión (logout)
     * Pueden llamarlo desde otro servlet o página
     */
    public static void cerrarSesion(HttpServletRequest peticion, HttpServletResponse respuesta) 
            throws IOException {
        
        HttpSession sesion = peticion.getSession(false);
        
        if (sesion != null) {
            System.out.println("Cerrando sesion para usuario: " + sesion.getAttribute("nombreUsuario"));
            sesion.invalidate(); // Destruir la sesión
        }
        
        // Redirigir al index principal
        respuesta.sendRedirect(peticion.getContextPath() + "/index.html");
    }
    
}