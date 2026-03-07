package dao;

import config.ConexionDB;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthDAO {
    private static final Logger LOGGER = Logger.getLogger(AuthDAO.class.getName());

    public Map<String, Object> validar(String nombre, String password) {
        String sql = """
            SELECT u.usuario_id, u.nombre, u.pass, r.cargo 
            FROM Usuario u 
            INNER JOIN Usuario_Rol ur ON ur.usuario_id = u.usuario_id 
            INNER JOIN Rol r ON r.rol_id = ur.rol_id 
            WHERE u.nombre = ? AND u.estado = 1
        """;

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String passBD = rs.getString("pass");
                // Verificación segura con BCrypt
                if (BCrypt.checkpw(password, passBD)) {
                    Map<String, Object> datos = new HashMap<>();
                    datos.put("id", rs.getInt("usuario_id"));
                    datos.put("nombre", rs.getString("nombre"));
                    datos.put("rol", rs.getString("cargo"));
                    return datos;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en autenticación", e);
        }
        return null;
    }
}