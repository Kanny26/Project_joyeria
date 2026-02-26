package dao;

import config.ConexionDB;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * DAO unificado de autenticación.
 * Retorna los datos del usuario y su rol sin importar si es admin o vendedor.
 */
public class AuthDAO {

    /**
     * Valida credenciales y retorna un mapa con:
     * - "id"     → usuario_id
     * - "nombre" → nombre
     * - "rol"    → cargo del rol (ej: "administrador", "vendedor")
     * Retorna null si las credenciales son incorrectas.
     */
    public Map<String, Object> validar(String nombre, String password) {
        String sql = """
            SELECT u.usuario_id, u.nombre, u.pass, r.cargo
            FROM Usuario u
            INNER JOIN Rol r ON u.usuario_id = r.usuario_id
            WHERE u.nombre = ? AND u.estado = 1
            """;

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String passBD = rs.getString("pass");

                if (BCrypt.checkpw(password, passBD)) {
                    Map<String, Object> datos = new HashMap<>();
                    datos.put("id",     rs.getInt("usuario_id"));
                    datos.put("nombre", rs.getString("nombre"));
                    datos.put("rol",    rs.getString("cargo"));
                    return datos;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}