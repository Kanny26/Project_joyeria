package dao;

import config.ConexionDB;
import model.Usuario;
import java.sql.*;

public class ClienteDAO {

    public int registrarOObtenerCliente(String nombre, String telefono, String email) throws Exception {
        Integer existente = buscarPorTelefono(telefono);
        if (existente != null) {
            return existente;
        }

        String sqlUsuario = "INSERT INTO Usuario (nombre, pass, estado, fecha_creacion, fecha_registro) VALUES (?,?,?,?,?)";
        String sqlRol = "INSERT INTO Rol (cargo, usuario_id, nombre) VALUES (?,?,?)";
        String sqlTelefono = "INSERT INTO Telefono_Usuario (telefono, usuario_id) VALUES (?,?)";

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            int nuevoUsuarioId = -1;

            try {
                try (PreparedStatement ps = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, nombre);
                    ps.setString(2, "CLIENTE_TEMP_" + System.currentTimeMillis());
                    ps.setBoolean(3, true);
                    ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                    ps.setDate(5, new java.sql.Date(System.currentTimeMillis()));
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) nuevoUsuarioId = rs.getInt(1);
                    }
                }

                if (nuevoUsuarioId == -1) throw new SQLException("Error al crear usuario cliente");

                try (PreparedStatement ps = con.prepareStatement(sqlRol)) {
                    ps.setString(1, "cliente");
                    ps.setInt(2, nuevoUsuarioId);
                    ps.setString(3, nombre);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(sqlTelefono)) {
                    ps.setString(1, telefono);
                    ps.setInt(2, nuevoUsuarioId);
                    ps.executeUpdate();
                }

                if (email != null && !email.isEmpty()) {
                    String sqlEmail = "INSERT INTO Correo_Usuario (email, usuario_id) VALUES (?,?)";
                    try (PreparedStatement ps = con.prepareStatement(sqlEmail)) {
                        ps.setString(1, email);
                        ps.setInt(2, nuevoUsuarioId);
                        ps.executeUpdate();
                    }
                }

                con.commit();
                return nuevoUsuarioId;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private Integer buscarPorTelefono(String telefono) throws Exception {
        String sql = """
            SELECT u.usuario_id FROM Usuario u
            JOIN Telefono_Usuario tu ON tu.usuario_id = u.usuario_id
            JOIN Rol r ON r.usuario_id = u.usuario_id
            WHERE tu.telefono = ? AND r.cargo = 'cliente' AND u.estado = true
            """;

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, telefono);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("usuario_id");
                }
            }
        }
        return null;
    }
}