package joyeria;

import java.sql.*;
import joyeria.database.Conexion;

public class Main {
    public static void main(String[] args) {
        System.out.println(" Leyendo datos reales de la tabla 'usuario'...");

        Connection conn = Conexion.obtenerConexion();
        if (conn == null) {
            System.out.println(" No se pudo conectar a la base de datos.");
            return;
        }

        try {
            String sql = "SELECT usuario_id, nombre, estado, fecha_creacion, documento FROM usuario";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            boolean hayDatos = false;
            while (rs.next()) {
                hayDatos = true;
                int id = rs.getInt("usuario_id");
                String nombre = rs.getString("nombre");
                int estado = rs.getInt("estado"); // 1 = activo, 0 = inactivo
                Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
                String documento = rs.getString("documento"); // Puede ser null

                System.out.println(" ID: " + id + 
                                " | Nombre: " + nombre + 
                                " | Estado: " + (estado == 1 ? "activo" : "inactivo") +
                                " | Creado: " + (fechaCreacion != null ? fechaCreacion.toString() : "N/A") +
                                " | Doc: " + (documento != null ? documento : "Sin documento"));
            }

            if (!hayDatos) {
                System.out.println(" La tabla 'usuario' está vacía.");
                // Insertamos un usuario de prueba
                System.out.println(" Insertando usuario de prueba...");
                insertarUsuarioPrueba(conn);
            } else {
                System.out.println(" ¡Conexión y lectura reales confirmadas!");
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println(" Error al consultar la tabla 'usuario':");
            e.printStackTrace();
        }
    }

    // Método auxiliar para insertar un usuario de prueba (si la tabla está vacía)
    private static void insertarUsuarioPrueba(Connection conn) {
        try {
            String sql = "INSERT INTO usuario (nombre, pass, estado, fecha_creacion) VALUES (?, ?, ?, NOW())";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "Admin Prueba");
            pstmt.setString(2, "123456"); // En producción, ¡usa hashing!
            pstmt.setInt(3, 1); // activo
            pstmt.executeUpdate();
            pstmt.close();
            System.out.println(" Usuario de prueba insertado.");
        } catch (SQLException e) {
            System.err.println(" Error al insertar usuario de prueba:");
            e.printStackTrace();
        }
    }
}