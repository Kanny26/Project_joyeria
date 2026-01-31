package dao;

import config.ConexionDB;
import model.Administrador;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * DAO encargado de la autenticación de administradores.
 */
public class AdministradorDAO {

    /**
     * Valida credenciales de administrador usando BCrypt.
     */
    public Administrador validar(String nombre, String password) {

        Administrador admin = null;

        String sql = """
            SELECT usuario_id, nombre, pass
            FROM Usuario
            WHERE nombre = ? AND estado = 1
            """;

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String passBD = rs.getString("pass");

                if (BCrypt.checkpw(password, passBD)) {
                    admin = new Administrador();
                    admin.setId(rs.getInt("usuario_id"));
                    admin.setNombre(rs.getString("nombre"));
                    admin.setPass(passBD);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return admin;
    }
}
