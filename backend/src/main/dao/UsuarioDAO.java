package main.dao;

import main.model.Usuario;
import main.model.Rol;
import main.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * DAO para gestionar operaciones de la tabla Usuario
 * Se encarga de consultar y validar usuarios en la base de datos
 */
public class UsuarioDAO {
    
    /**
     * Busca un usuario por su nombre de usuario
     * Incluye información de su rol mediante JOIN
     * 
     * @param nombreUsuario El nombre del usuario a buscar
     * @return Usuario encontrado o null si no existe
     */
    public Usuario buscarUsuarioPorNombre(String nombreUsuario) {
        Usuario usuarioEncontrado = null;
        Connection conexion = null;
        PreparedStatement sentenciaPreparada = null;
        ResultSet resultadoConsulta = null;
        
        // Query SQL con JOIN para obtener usuario y su rol
        String consultaSQL = 
            "SELECT u.usuario_id, u.nombre, u.pass, u.estado, u.fecha_creacion, " +
            "       r.cargo, r.rol_id, r.nombre as nombre_rol " +
            "FROM Usuario u " +
            "LEFT JOIN Rol r ON u.usuario_id = r.usuario_id " +
            "WHERE u.nombre = ? AND u.estado = 1";
        
        try {
            // Obtener conexión a la base de datos
            conexion = DatabaseConfig.getInstance().getConnection();
            
            // Preparar la consulta
            sentenciaPreparada = conexion.prepareStatement(consultaSQL);
            sentenciaPreparada.setString(1, nombreUsuario);
            
            // Ejecutar la consulta
            resultadoConsulta = sentenciaPreparada.executeQuery();
            
            // Si encontró el usuario
            if (resultadoConsulta.next()) {
                // Extraer datos del usuario
                int identificadorUsuario = resultadoConsulta.getInt("usuario_id");
                String nombreDelUsuario = resultadoConsulta.getString("nombre");
                String contraseñaEncriptada = resultadoConsulta.getString("pass");
                boolean estadoUsuario = resultadoConsulta.getBoolean("estado");
                Timestamp fechaCreacionTimestamp = resultadoConsulta.getTimestamp("fecha_creacion");
                
                // Crear objeto Usuario
                usuarioEncontrado = new Usuario();
                usuarioEncontrado.setUsuarioId(identificadorUsuario);
                usuarioEncontrado.setNombre(nombreDelUsuario);
                usuarioEncontrado.setPass(contraseñaEncriptada);
                usuarioEncontrado.setEstado(estadoUsuario);
                
                // Convertir Timestamp a LocalDateTime
                if (fechaCreacionTimestamp != null) {
                    usuarioEncontrado.setFechaCreacion(fechaCreacionTimestamp.toLocalDateTime());
                }
                
                System.out.println("✓ Usuario encontrado: " + nombreDelUsuario);
                
            } else {
                System.out.println("✗ Usuario no encontrado: " + nombreUsuario);
            }
            
        } catch (SQLException excepcionSQL) {
            System.err.println("✗ Error al buscar usuario en la base de datos");
            System.err.println("Detalle del error: " + excepcionSQL.getMessage());
            excepcionSQL.printStackTrace();
            
        } finally {
            // Cerrar recursos en orden inverso
            try {
                if (resultadoConsulta != null) {
                    resultadoConsulta.close();
                }
                if (sentenciaPreparada != null) {
                    sentenciaPreparada.close();
                }
                // NO cerramos la conexión aquí porque es reutilizable (patrón Singleton)
            } catch (SQLException excepcionAlCerrar) {
                System.err.println("✗ Error al cerrar recursos: " + excepcionAlCerrar.getMessage());
            }
        }
        
        return usuarioEncontrado;
    }
    
    /**
     * Obtiene el rol de un usuario específico
     * 
     * @param identificadorUsuario El ID del usuario
     * @return Objeto Rol o null si no tiene rol asignado
     */
    public Rol obtenerRolDeUsuario(int identificadorUsuario) {
        Rol rolDelUsuario = null;
        Connection conexion = null;
        PreparedStatement sentenciaPreparada = null;
        ResultSet resultadoConsulta = null;
        
        String consultaSQL = 
            "SELECT rol_id, cargo, nombre, usuario_id " +
            "FROM Rol " +
            "WHERE usuario_id = ?";
        
        try {
            conexion = DatabaseConfig.getInstance().getConnection();
            sentenciaPreparada = conexion.prepareStatement(consultaSQL);
            sentenciaPreparada.setInt(1, identificadorUsuario);
            
            resultadoConsulta = sentenciaPreparada.executeQuery();
            
            if (resultadoConsulta.next()) {
                int identificadorRol = resultadoConsulta.getInt("rol_id");
                String cargoString = resultadoConsulta.getString("cargo");
                String nombreRol = resultadoConsulta.getString("nombre");
                
                // Convertir el String a enum TipoCargo
                Rol.TipoCargo cargoUsuario = Rol.TipoCargo.fromString(cargoString);
                
                // Crear objeto Rol
                rolDelUsuario = new Rol();
                rolDelUsuario.setRolId(identificadorRol);
                rolDelUsuario.setCargo(cargoUsuario);
                rolDelUsuario.setNombre(nombreRol);
                rolDelUsuario.setUsuarioId(identificadorUsuario);
                
                System.out.println("✓ Rol encontrado: " + cargoUsuario);
            }
            
        } catch (SQLException excepcionSQL) {
            System.err.println("✗ Error al obtener rol del usuario");
            System.err.println("Detalle: " + excepcionSQL.getMessage());
            
        } catch (IllegalArgumentException excepcionCargo) {
            System.err.println("✗ Cargo inválido en la base de datos");
            System.err.println("Detalle: " + excepcionCargo.getMessage());
            
        } finally {
            try {
                if (resultadoConsulta != null) resultadoConsulta.close();
                if (sentenciaPreparada != null) sentenciaPreparada.close();
            } catch (SQLException excepcionAlCerrar) {
                System.err.println("✗ Error al cerrar recursos: " + excepcionAlCerrar.getMessage());
            }
        }
        
        return rolDelUsuario;
    }
    
    /**
     * Verifica si un usuario tiene un cargo específico
     * 
     * @param identificadorUsuario El ID del usuario
     * @param cargoEsperado El cargo a verificar
     * @return true si el usuario tiene ese cargo, false en caso contrario
     */
    public boolean verificarCargoDeUsuario(int identificadorUsuario, Rol.TipoCargo cargoEsperado) {
        Rol rolDelUsuario = obtenerRolDeUsuario(identificadorUsuario);
        
        if (rolDelUsuario != null) {
            return rolDelUsuario.getCargo() == cargoEsperado;
        }
        
        return false;
    }
}