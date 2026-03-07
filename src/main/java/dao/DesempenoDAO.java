package dao;

import config.ConexionDB;
import model.Desempeno_Vendedor;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DesempenoDAO {

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // OBTENER ÚLTIMO DESEMPEÑO POR USUARIO
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public Desempeno_Vendedor obtenerUltimoDesempenoPorUsuario(int usuarioId) {
        String sql = """
            SELECT * FROM Desempeno_Vendedor 
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // CALCULAR DESEMPEÑO REAL DESDE VENTAS (RF36)
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public Desempeno_Vendedor calcularDesempenoReal(int usuarioId, Date fechaInicio, Date fechaFin) throws Exception {
        String sql = """
            SELECT 
                COALESCE(SUM(pv.monto), 0) AS ventas_totales,
                COUNT(v.venta_id) AS total_ventas
            FROM Venta v
            INNER JOIN Pago_Venta pv ON pv.venta_id = v.venta_id
            WHERE v.usuario_id = ? 
            AND pv.estado = 'confirmado'
            AND v.fecha_emision BETWEEN ? AND ?
            """;
        
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setDate(2, new java.sql.Date(fechaInicio.getTime()));
            ps.setDate(3, new java.sql.Date(fechaFin.getTime()));
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Desempeno_Vendedor d = new Desempeno_Vendedor();
                d.setUsuarioId(usuarioId);
                d.setVentasTotales(rs.getBigDecimal("ventas_totales"));
                
                // ■■ RF36: Comisión calculada automáticamente (ej: 5%) ■■
                BigDecimal comisionPorcentaje = new BigDecimal("5.00");
                d.setComisionPorcentaje(comisionPorcentaje);
                
                // ■■ RF36: comision_ganada = ventas_totales × comision_porcentaje / 100 ■■
                BigDecimal comisionGanada = rs.getBigDecimal("ventas_totales")
                    .multiply(comisionPorcentaje)
                    .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
                d.setComisionGanada(comisionGanada);
                
                d.setPeriodo(fechaInicio);
                d.setObservaciones("Calculado automáticamente desde ventas reales");
                
                return d;
            }
        }
        return null;
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // ACTUALIZAR DESEMPEÑO CON AUDITORÍA (RF38)
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public boolean actualizarDesempeno(Desempeno_Vendedor d, int usuarioIdAuditoria) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false);
            
            // ■■ Capturar estado anterior para auditoría ■■
            Desempeno_Vendedor anterior = obtenerUltimoDesempenoPorUsuario(d.getUsuarioId());
            String datosAnteriores = anterior != null ? 
                "Ventas: " + anterior.getVentasTotales() + ", Comisión: " + anterior.getComisionGanada() : "N/A";
            
            String sql = """
                UPDATE Desempeno_Vendedor 
                SET observaciones = ?, 
                    ventas_totales = ?, 
                    comision_ganada = ?,
                    periodo = ?
                WHERE desempeno_id = ?
                """;
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, d.getObservaciones());
                ps.setBigDecimal(2, d.getVentasTotales());
                ps.setBigDecimal(3, d.getComisionGanada());
                ps.setDate(4, new java.sql.Date(d.getPeriodo().getTime()));
                ps.setInt(5, d.getDesempenoId());
                ps.executeUpdate();
            }
            
            // ■■ RF38: Registro de auditoría ■■
            registrarAuditoria(conn, usuarioIdAuditoria, "EDITAR", "Desempeno_Vendedor", 
                d.getDesempenoId(), datosAnteriores, 
                "Ventas: " + d.getVentasTotales() + ", Comisión: " + d.getComisionGanada());
            
            conn.commit();
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } 
            catch (Exception ignored) {}
        }
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // INSERTAR DESEMPEÑO CON AUDITORÍA (RF38)
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public boolean insertarDesempeno(Desempeno_Vendedor d, int usuarioIdAuditoria) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false);
            
            String sql = """
                INSERT INTO Desempeno_Vendedor 
                (usuario_id, ventas_totales, comision_porcentaje, comision_ganada, periodo, observaciones) 
                VALUES (?, ?, ?, ?, ?, ?)
                """;
            
            int desempenoId = -1;
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, d.getUsuarioId());
                ps.setBigDecimal(2, d.getVentasTotales());
                ps.setBigDecimal(3, d.getComisionPorcentaje());
                ps.setBigDecimal(4, d.getComisionGanada());
                ps.setDate(5, new java.sql.Date(d.getPeriodo().getTime()));
                ps.setString(6, d.getObservaciones());
                ps.executeUpdate();
                
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    desempenoId = rs.getInt(1);
                }
            }
            
            // ■■ RF38: Registro de auditoría ■■
            registrarAuditoria(conn, usuarioIdAuditoria, "CREAR", "Desempeno_Vendedor", 
                desempenoId, null, 
                "Usuario: " + d.getUsuarioId() + ", Ventas: " + d.getVentasTotales() + 
                ", Comisión: " + d.getComisionGanada());
            
            conn.commit();
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } 
            catch (Exception ignored) {}
        }
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // OBTENER HISTORIAL COMPLETO CON FILTROS (RF36)
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public List<Desempeno_Vendedor> obtenerHistorialCompleto(Date fechaInicio, Date fechaFin) {
        List<Desempeno_Vendedor> historial = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT d.desempeno_id, d.usuario_id, u.nombre AS nombre_vendedor, 
                   d.ventas_totales, d.comision_ganada, d.observaciones, d.periodo 
            FROM Desempeno_Vendedor d 
            INNER JOIN Usuario u ON d.usuario_id = u.usuario_id 
            WHERE 1=1
            """);
        
        List<Object> params = new ArrayList<>();
        
        if (fechaInicio != null) {
            sql.append(" AND d.periodo >= ?");
            params.add(fechaInicio);
        }
        if (fechaFin != null) {
            sql.append(" AND d.periodo <= ?");
            params.add(fechaFin);
        }
        
        sql.append(" ORDER BY d.periodo DESC, d.desempeno_id DESC");
        
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof Date) {
                    ps.setDate(i + 1, new java.sql.Date(((Date) params.get(i)).getTime()));
                }
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Desempeno_Vendedor d = new Desempeno_Vendedor();
                d.setDesempenoId(rs.getInt("desempeno_id"));
                d.setUsuarioId(rs.getInt("usuario_id"));
                d.setNombre(rs.getString("nombre_vendedor"));
                d.setVentasTotales(rs.getBigDecimal("ventas_totales"));
                d.setComisionGanada(rs.getBigDecimal("comision_ganada"));
                d.setObservaciones(rs.getString("observaciones"));
                d.setPeriodo(rs.getDate("periodo"));
                historial.add(d);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return historial;
    }

    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // MÉTODO AUXILIAR: AUDITORÍA (RF38)
    // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private void registrarAuditoria(Connection conn, int usuarioId, String accion, String entidad, 
                                    int entidadId, String datosAnteriores, String datosNuevos) throws Exception {
        String sql = """
            INSERT INTO Auditoria_Log 
            (usuario_id, accion, entidad, entidad_id, datos_anteriores, datos_nuevos, fecha_hora) 
            VALUES (?, ?, ?, ?, ?, ?, NOW())
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, accion);
            stmt.setString(3, entidad);
            stmt.setInt(4, entidadId);
            stmt.setString(5, datosAnteriores);
            stmt.setString(6, datosNuevos);
            stmt.executeUpdate();
        }
    }
}