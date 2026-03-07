package dao;

import config.ConexionDB;
import model.CasoPostventa;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostventaDAO {

    // Subconsulta para obtener el primer producto de la venta sin duplicar filas
    private static final String SQL_BASE = """
        SELECT 
            cp.caso_id,
            cp.venta_id,
            cp.tipo,
            cp.cantidad,
            cp.motivo,
            cp.fecha,
            cp.estado,
            v.usuario_id AS vendedor_id,
            uv.nombre AS vendedor_nombre,
            v.cliente_id,
            cl.nombre AS cliente_nombre,
            (SELECT dv2.producto_id FROM Detalle_Venta dv2 WHERE dv2.venta_id = v.venta_id LIMIT 1) AS producto_id,
            (SELECT p2.nombre FROM Detalle_Venta dv2 
             LEFT JOIN Producto p2 ON p2.producto_id = dv2.producto_id
             WHERE dv2.venta_id = v.venta_id LIMIT 1) AS producto_nombre
        FROM Caso_Postventa cp
        LEFT JOIN Venta v ON v.venta_id = cp.venta_id
        LEFT JOIN Usuario uv ON uv.usuario_id = v.usuario_id
        LEFT JOIN Cliente cl ON cl.cliente_id = v.cliente_id
        """;

    public List<CasoPostventa> listarPorVendedor(int vendedorId) throws Exception {
        List<CasoPostventa> lista = new ArrayList<>();
        String sql = SQL_BASE + " WHERE v.usuario_id = ? ORDER BY cp.fecha DESC, cp.caso_id DESC";
        
        try (Connection con = ConexionDB.getConnection()) {
            System.out.println("🔍 PostventaDAO.listarPorVendedor() - Conexión: " + (con != null));
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, vendedorId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCaso(rs));
                }
            }
            ps.close();
        }
        System.out.println("✅ PostventaDAO - Casos recuperados (vendedor): " + lista.size());
        return lista;
    }

    public List<CasoPostventa> listarTodos() throws Exception {
        List<CasoPostventa> lista = new ArrayList<>();
        String sql = SQL_BASE + " ORDER BY cp.fecha DESC, cp.caso_id DESC";
        
        System.out.println("\n" + "🔥".repeat(50));
        System.out.println("🔍 PostventaDAO.listarTodos() - INICIADO");
        
        try (Connection con = ConexionDB.getConnection()) {
            System.out.println("✅ Conexión DB: " + (con != null && !con.isClosed()));
            
            // 🔍 Debug: Contar registros en tabla
            try (Statement stmt = con.createStatement();
                 ResultSet rsCount = stmt.executeQuery("SELECT COUNT(*) FROM Caso_Postventa")) {
                if (rsCount.next()) {
                    System.out.println("📊 Total registros en 'Caso_Postventa': " + rsCount.getInt(1));
                }
            }
            
            PreparedStatement ps = con.prepareStatement(sql);
            try (ResultSet rs = ps.executeQuery()) {
                int contador = 0;
                while (rs.next()) {
                    contador++;
                    System.out.println("📦 Caso #" + contador + 
                        " | ID=" + rs.getInt("caso_id") + 
                        " | Tipo=" + rs.getString("tipo") +
                        " | Estado=" + rs.getString("estado"));
                    lista.add(mapearCaso(rs));
                }
                System.out.println("✅ DAO - Casos recuperados: " + lista.size());
            }
            ps.close();
        } catch (SQLException e) {
            System.err.println("❌ ERROR SQL en listarTodos: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        System.out.println("🔥".repeat(50) + "\n");
        return lista;
    }

    public CasoPostventa obtenerPorId(int casoId) throws Exception {
        String sql = SQL_BASE + " WHERE cp.caso_id = ?";
        
        System.out.println("🔍 DAO - Buscando caso postventa ID: " + casoId);
        
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, casoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("✅ Caso encontrado: ID=" + casoId);
                    return mapearCaso(rs);
                }
            }
        }
        System.out.println("⚠️ Caso NO encontrado: ID=" + casoId);
        return null;
    }

    public int registrar(CasoPostventa caso) throws Exception {
        String sql = "INSERT INTO Caso_Postventa(venta_id, tipo, cantidad, motivo, fecha, estado) VALUES(?, ?, ?, ?, NOW(), 'en_proceso')";
        
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, caso.getVentaId());
            ps.setString(2, caso.getTipo());
            ps.setInt(3, caso.getCantidad());
            ps.setString(4, caso.getMotivo() != null ? caso.getMotivo() : "");
            
            int filasAfectadas = ps.executeUpdate();
            
            if (filasAfectadas > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int nuevoId = generatedKeys.getInt(1);
                        System.out.println("✅ Caso registrado con ID: " + nuevoId);
                        return nuevoId;
                    }
                }
            }
            return -1;
            
        } catch (SQLException e) {
            System.err.println("❌ ERROR al registrar caso postventa: " + e.getMessage());
            throw e;
        }
    }

    public boolean actualizarEstado(int casoId, String nuevoEstado, String observacion, int usuarioId) throws Exception {
        final String sqlUpdate = "UPDATE Caso_Postventa SET estado = ? WHERE caso_id = ?";
        final String sqlHistorial = "INSERT INTO Historial_Caso_Postventa(caso_id, estado, observacion, usuario_id, fecha) VALUES(?,?,?,?,NOW())";
        
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                    ps.setString(1, nuevoEstado);
                    ps.setInt(2, casoId);
                    ps.executeUpdate();
                }
                
                try (PreparedStatement ps = con.prepareStatement(sqlHistorial)) {
                    ps.setInt(1, casoId);
                    ps.setString(2, nuevoEstado);
                    ps.setString(3, observacion != null ? observacion : "");
                    ps.setInt(4, usuarioId);
                    ps.executeUpdate();
                }
                
                // RF31: Si es devolución aprobada, retornar stock
                CasoPostventa caso = obtenerPorId(casoId);
                if (caso != null && "devolucion".equals(caso.getTipo()) && "aprobado".equals(nuevoEstado)) {
                    System.out.println("🔄 Retornando stock por devolución aprobada - Producto ID: " + caso.getProductoId());
                    new VentaDAO().retornarStockDevolucion(
                        caso.getVentaId(), 
                        caso.getProductoId(), 
                        caso.getCantidad(), 
                        usuarioId
                    );
                }
                
                con.commit();
                System.out.println("✅ Estado actualizado para caso #" + casoId + " -> " + nuevoEstado);
                return true;
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    private CasoPostventa mapearCaso(ResultSet rs) throws SQLException {
        CasoPostventa c = new CasoPostventa();
        c.setCasoId(rs.getInt("caso_id"));
        c.setVentaId(rs.getInt("venta_id"));
        c.setTipo(rs.getString("tipo"));
        c.setCantidad(rs.getInt("cantidad"));
        c.setMotivo(rs.getString("motivo"));
        c.setFecha(rs.getDate("fecha"));
        c.setEstado(rs.getString("estado"));
        c.setVendedorNombre(rs.getString("vendedor_nombre"));
        c.setClienteNombre(rs.getString("cliente_nombre"));
        
        // Manejo seguro de valores null para producto
        int productoId = 0;
        if (rs.getObject("producto_id") != null) {
            productoId = rs.getInt("producto_id");
        }
        c.setProductoId(productoId);
        c.setProductoNombre(rs.getString("producto_nombre"));
        return c;
    }
}