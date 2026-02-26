package dao;

import config.ConexionDB;

import java.sql.*;

/**
 * Registra o recupera un cliente (Usuario con rol 'cliente').
 * Si el cliente ya existe por nombre+teléfono, retorna su ID.
 * Si no existe, lo crea como nuevo Usuario + Rol cliente + Teléfono.
 */
public class ClienteDAO {

    public int registrarOObtenerCliente(String nombre, String telefono, String email) throws Exception {
        try (Connection con = ConexionDB.getConnection()) {

            // 1. Buscar por nombre + teléfono
            String sqlBuscar = """
                SELECT u.usuario_id
                FROM Usuario u
                JOIN Telefono_Usuario tu ON tu.usuario_id = u.usuario_id
                JOIN Rol r ON r.usuario_id = u.usuario_id
                WHERE u.nombre = ? AND tu.telefono = ? AND r.cargo = 'cliente'
                LIMIT 1
                """;
            try (PreparedStatement ps = con.prepareStatement(sqlBuscar)) {
                ps.setString(1, nombre);
                ps.setString(2, telefono != null ? telefono : "");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt("usuario_id");
                }
            }

            // 2. Crear nuevo cliente
            con.setAutoCommit(false);
            try {
                int clienteId;
                // Insertar usuario (pass vacío, será cliente sin login)
                String sqlUsuario = "INSERT INTO Usuario (nombre, pass, estado, fecha_creacion) VALUES (?, '', 1, NOW())";
                try (PreparedStatement ps = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, nombre);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("No se generó usuario_id");
                        clienteId = rs.getInt(1);
                    }
                }

                // Rol cliente
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO Rol (cargo, usuario_id) VALUES ('cliente', ?)")) {
                    ps.setInt(1, clienteId);
                    ps.executeUpdate();
                }

                // Teléfono
                if (telefono != null && !telefono.isBlank()) {
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO Telefono_Usuario (telefono, usuario_id) VALUES (?, ?)")) {
                        ps.setString(1, telefono);
                        ps.setInt(2, clienteId);
                        ps.executeUpdate();
                    }
                }

                // Email (opcional)
                if (email != null && !email.isBlank()) {
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO Correo_Usuario (email, usuario_id) VALUES (?, ?)")) {
                        ps.setString(1, email);
                        ps.setInt(2, clienteId);
                        ps.executeUpdate();
                    }
                }

                con.commit();
                return clienteId;

            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }
}
