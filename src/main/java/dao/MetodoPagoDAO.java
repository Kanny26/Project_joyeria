package dao;

import config.ConexionDB;
import model.MetodoPago;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de métodos de pago: administra las opciones de cobro/abono visibles en ventas y compras
 */
public class MetodoPagoDAO {

    /**
     * Retorna todos los métodos de pago disponibles, ordenados alfabéticamente.
     *
     * @return lista de métodos de pago
     * @throws Exception si falla la consulta
     */
    public List<MetodoPago> listarTodos() throws Exception {
        List<MetodoPago> lista = new ArrayList<>();
        String sql = "SELECT metodo_pago_id, nombre FROM Metodo_Pago ORDER BY nombre ASC";

        // El bloque try-with-resources cierra automáticamente la conexión, el statement
        // y el ResultSet cuando termina, aunque ocurra un error.
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

    /**
     * Busca un método de pago por su ID.
     *
     * @param id {@code metodo_pago_id}
     * @return el registro o {@code null} si no existe
     * @throws Exception si falla la consulta
     */
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

    /**
     * Inserta un nuevo método de pago.
     *
     * @param mp objeto con el nombre a guardar
     * @return {@code true} si se insertó al menos una fila
     * @throws Exception si falla el insert
     */
    public boolean guardar(MetodoPago mp) throws Exception {
        String sql = "INSERT INTO Metodo_Pago (nombre) VALUES (?)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // trim() elimina espacios al inicio y al final del nombre
            ps.setString(1, mp.getNombre().trim());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza el nombre de un método de pago existente.
     *
     * @param mp registro con ID y nombre nuevos
     * @return {@code true} si se actualizó al menos una fila
     * @throws Exception si falla el update
     */
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
     * Elimina un método de pago por su ID.
     * Precaución: si hay ventas que usan este método, puede fallar por restricción
     * de clave foránea en la base de datos.
     *
     * @param id {@code metodo_pago_id}
     * @return {@code true} si se eliminó una fila
     * @throws Exception si falla el delete o hay restricción de integridad
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
