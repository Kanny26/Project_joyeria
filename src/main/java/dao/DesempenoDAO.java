package dao;
import config.ConexionDB;
import model.Desempeno_Vendedor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DesempenoDAO {

    public Desempeno_Vendedor obtenerUltimoDesempenoPorUsuario(int usuarioId) {
        String sql = """
            SELECT *
            FROM Desempeno_Vendedor
            WHERE usuario_id = ?
            ORDER BY periodo DESC
            LIMIT 1
        """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Desempeno_Vendedor d = new Desempeno_Vendedor();
                d.setDesempenoId(rs.getInt("desempeno_id"));
                d.setUsuarioId(rs.getInt("usuario_id"));
                d.setVentasTotales(rs.getBigDecimal("ventas_totales"));
                d.setComisionPorcentaje(rs.getBigDecimal("comision_porcentaje"));
                d.setComisionGanada(rs.getBigDecimal("comision_ganada"));
                d.setPeriodo(rs.getDate("periodo"));
                d.setObservaciones(rs.getString("observaciones"));
                return d;
            }
        } catch (Exception e) { // ✅
            e.printStackTrace();
        }
        return null;
    }

    public boolean actualizarDesempeno(Desempeno_Vendedor d) {
        String sql = "UPDATE Desempeno_Vendedor SET observaciones = ? WHERE desempeno_id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getObservaciones());
            ps.setInt(2, d.getDesempenoId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { // ✅
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertarDesempeno(Desempeno_Vendedor d) {
        String sql = """
            INSERT INTO Desempeno_Vendedor
            (usuario_id, ventas_totales, comision_porcentaje, comision_ganada, periodo, observaciones)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getUsuarioId());
            ps.setBigDecimal(2, d.getVentasTotales());
            ps.setBigDecimal(3, d.getComisionPorcentaje());
            ps.setBigDecimal(4, d.getComisionGanada());
            ps.setDate(5, new java.sql.Date(d.getPeriodo().getTime()));
            ps.setString(6, d.getObservaciones());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { // ✅
            e.printStackTrace();
            return false;
        }
    }

    public List<Desempeno_Vendedor> obtenerHistorialCompleto() {
        List<Desempeno_Vendedor> historial = new ArrayList<>();
        String sql = """
            SELECT 
                d.desempeno_id,
                d.usuario_id,
                u.nombre AS nombre_vendedor,
                d.ventas_totales,
                d.comision_ganada,
                d.observaciones,
                d.periodo
            FROM Desempeno_Vendedor d
            INNER JOIN Usuario u ON d.usuario_id = u.usuario_id
            ORDER BY d.periodo DESC, d.desempeno_id DESC
        """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Desempeno_Vendedor d = new Desempeno_Vendedor();
                d.setDesempenoId(rs.getInt("desempeno_id"));
                d.setUsuarioId(rs.getInt("usuario_id"));
                d.setVentasTotales(rs.getBigDecimal("ventas_totales"));
                d.setComisionGanada(rs.getBigDecimal("comision_ganada"));
                d.setObservaciones(rs.getString("observaciones"));
                d.setPeriodo(rs.getDate("periodo"));
                historial.add(d);
            }
        } catch (Exception e) { // ✅
            e.printStackTrace();
        }
        return historial;
    }
}