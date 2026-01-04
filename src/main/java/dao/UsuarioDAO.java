package dao;

import model.Usuario;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UsuarioDAO {
    private Connection conexion;

    public UsuarioDAO(Connection conexion) {
        this.conexion = conexion;
    }

 // Agregar usuario con rol y contraseña segura
    public boolean agregarUsuario(Usuario usuario) {
        String sql = "INSERT INTO Usuario (nombre, correo, pass, estado, fecha_creacion, telefono, rol) VALUES (?, ?, ?, ?, NOW(), ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getCorreo());

            // Encriptar contraseña
            String hashed = BCrypt.hashpw(usuario.getContrasena(), BCrypt.gensalt());
            ps.setString(3, hashed);

            ps.setBoolean(4, usuario.isEstado());
            ps.setString(5, usuario.getTelefono());
            ps.setString(6, usuario.getRol());

            int filas = ps.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Generar contraseña aleatoria de 8 caracteres
    private String generarContrasena() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }
        return sb.toString();
    }


    // Listar usuarios
    public List<Usuario> listarUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT usuario_id, nombre, correo, pass, estado, fecha_creacion, telefono, rol FROM Usuario";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setUsuarioId(rs.getInt("usuario_id"));
                usuario.setNombre(rs.getString("nombre"));
                usuario.setCorreo(rs.getString("correo"));
                usuario.setContrasena(rs.getString("pass"));
                usuario.setEstado(rs.getBoolean("estado"));
                usuario.setTelefono(rs.getString("telefono"));
                usuario.setRol(rs.getString("rol"));
                usuario.setFechaCreacion(rs.getDate("fecha_creacion"));
                lista.add(usuario);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Obtener usuario por ID
    public Usuario obtenerUsuarioPorId(int usuarioId) {
        Usuario usuario = null;
        String sql = "SELECT usuario_id, nombre, correo, pass, estado, fecha_creacion, telefono, rol FROM Usuario WHERE usuario_id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                usuario = new Usuario();
                usuario.setUsuarioId(rs.getInt("usuario_id"));
                usuario.setNombre(rs.getString("nombre"));
                usuario.setCorreo(rs.getString("correo"));
                usuario.setContrasena(rs.getString("pass"));
                usuario.setEstado(rs.getBoolean("estado"));
                usuario.setTelefono(rs.getString("telefono"));
                usuario.setRol(rs.getString("rol"));
                usuario.setFechaCreacion(rs.getDate("fecha_creacion"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuario;
    }

    // Actualizar usuario
    public boolean actualizarUsuario(Usuario usuario) {
        String sql = "UPDATE Usuario SET nombre = ?, correo = ?, pass = ?, estado = ?, telefono = ?, rol = ? WHERE usuario_id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getCorreo());
            ps.setString(3, usuario.getContrasena());
            ps.setBoolean(4, usuario.isEstado());
            ps.setString(5, usuario.getTelefono());
            ps.setString(6, usuario.getRol());
            ps.setInt(7, usuario.getUsuarioId());
            int filas = ps.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Contadores
    public int contarUsuarios() {
        String sql = "SELECT COUNT(*) AS total FROM Usuario";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int contarUsuariosActivos() {
        String sql = "SELECT COUNT(*) AS activos FROM Usuario WHERE estado = 1";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("activos");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}


