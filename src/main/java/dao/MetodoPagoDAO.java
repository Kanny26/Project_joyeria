package dao;

import model.MetodoPago;
import config.ConexionDB;

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
}