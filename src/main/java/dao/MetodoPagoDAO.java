package dao;
import config.ConexionDB;
import model.MetodoPago;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Maneja todas las operaciones de base de datos para los métodos de pago
 * (tabla Metodo_Pago).
 */
public class MetodoPagoDAO {

    /** Retorna todos los métodos de pago disponibles, ordenados alfabéticamente. */
    public List<MetodoPago> listarTodos() throws Exception {
        List<MetodoPago> lista = new ArrayList<>();
        String sql = "SELECT metodo_pago_id, nombre FROM Metodo_Pago ORDER BY nombre ASC";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                MetodoPago mp = new MetodoPago();
                mp.setMetodoPagoId(rs.getInt("metodo_pago_id"));
                mp.setNombre(rs.getString("nombre"));
                lista.add(mp);
            }
        }
        return lista;
    }

    /** Busca un método de pago por su ID. Retorna null si no existe. */
    public MetodoPago obtenerPorId(int id) throws Exception {
        MetodoPago mp = null;
        String sql = "SELECT metodo_pago_id, nombre FROM Metodo_Pago WHERE metodo_pago_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    mp = new MetodoPago();
                    mp.setMetodoPagoId(rs.getInt("metodo_pago_id"));
                    mp.setNombre(rs.getString("nombre"));
                }
            }
        }
        return mp;
    }

    /** Inserta un nuevo método de pago. Retorna true si se guardó correctamente. */
    public boolean guardar(MetodoPago mp) throws Exception {
        String sql = "INSERT INTO Metodo_Pago (nombre) VALUES (?)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, mp.getNombre().trim());
            return ps.executeUpdate() > 0;
        }
    }

    /** Actualiza el nombre de un método de pago existente. */
    public boolean actualizar(MetodoPago mp) throws Exception {
        String sql = "UPDATE Metodo_Pago SET nombre = ? WHERE metodo_pago_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, mp.getNombre().trim());
            ps.setInt(2, mp.getMetodoPagoId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Verifica si un método de pago está referenciado en abono_credito.
     * Retorna true si tiene abonos asociados y NO puede eliminarse de forma segura.
     */
    public boolean tieneAbonos(int id) throws Exception {
        String sql = "SELECT COUNT(*) FROM abono_credito WHERE metodo_pago_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Elimina un método de pago por su ID.
     * Usar siempre después de verificar con tieneAbonos() para evitar
     * errores de clave foránea.
     */
    public boolean eliminar(int id) throws Exception {
        String sql = "DELETE FROM Metodo_Pago WHERE metodo_pago_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}