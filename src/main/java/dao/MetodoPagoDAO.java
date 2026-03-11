package dao;

import config.ConexionDB;
import model.MetodoPago;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MetodoPagoDAO {

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

    public boolean guardar(MetodoPago mp) throws Exception {
        String sql = "INSERT INTO Metodo_Pago (nombre) VALUES (?)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, mp.getNombre().trim());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(MetodoPago mp) throws Exception {
        String sql = "UPDATE Metodo_Pago SET nombre = ? WHERE metodo_pago_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, mp.getNombre().trim());
            ps.setInt(2, mp.getMetodoPagoId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws Exception {
        // Validar si hay ventas/pedidos usando este método (opcional)
        String sql = "DELETE FROM Metodo_Pago WHERE metodo_pago_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}