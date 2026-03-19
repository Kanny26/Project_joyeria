package dao;

// Clase para manejar la conexión a la base de datos
import config.ConexionDB;

// Modelo que representa un caso de postventa
import model.CasoPostventa;

// Librerías SQL
import java.sql.*;

// Colecciones
import java.util.ArrayList;
import java.util.List;

/**
 * DAO encargado de gestionar todas las operaciones relacionadas
 * con los casos de postventa en la base de datos.
 */
public class PostventaDAO {

    /**
     * Consulta base reutilizable.
     * 
     * Esta consulta une múltiples tablas para obtener toda la información
     * relevante de un caso de postventa en una sola ejecución:
     * - Caso_Postventa (principal)
     * - Venta
     * - Usuario (vendedor)
     * - Cliente
     * - Producto (mediante subconsulta)
     * - Historial (última observación)
     */
    private static final String SQL_BASE = """
        SELECT 
            cp.caso_id,
            cp.venta_id,
            cp.tipo,
            cp.cantidad,
            cp.motivo,
            cp.fecha,
            cp.estado,

            -- ID del vendedor (usuario que realizó la venta)
            v.usuario_id AS vendedor_id,

            -- Nombre del vendedor
            uv.nombre AS vendedor_nombre,

            -- ID del cliente
            v.cliente_id,

            -- Nombre del cliente
            cl.nombre AS cliente_nombre,

            -- Subconsulta para obtener el ID del producto asociado a la venta
            (SELECT dv2.producto_id FROM Detalle_Venta dv2 WHERE dv2.venta_id = v.venta_id LIMIT 1) AS producto_id,

            -- Subconsulta para obtener el nombre del producto
            (SELECT p2.nombre FROM Detalle_Venta dv2 
             LEFT JOIN Producto p2 ON p2.producto_id = dv2.producto_id
             WHERE dv2.venta_id = v.venta_id LIMIT 1) AS producto_nombre,

            -- Última observación del historial del caso
            h.observacion AS observacion

        FROM Caso_Postventa cp

        -- Relación con la venta
        LEFT JOIN Venta v ON v.venta_id = cp.venta_id

        -- Relación con el usuario (vendedor)
        LEFT JOIN Usuario uv ON uv.usuario_id = v.usuario_id

        -- Relación con el cliente
        LEFT JOIN Cliente cl ON cl.cliente_id = v.cliente_id

        -- Obtener el último registro del historial del caso
        LEFT JOIN Historial_Caso_Postventa h
            ON h.historial_id = (
                SELECT MAX(h2.historial_id)
                FROM Historial_Caso_Postventa h2
                WHERE h2.caso_id = cp.caso_id
            )
        """;

    /**
     * Lista los casos de postventa asociados a un vendedor específico
     */
    public List<CasoPostventa> listarPorVendedor(int vendedorId) throws Exception {

        List<CasoPostventa> lista = new ArrayList<>();

        // Se añade filtro por vendedor y orden descendente
        String sql = SQL_BASE + " WHERE v.usuario_id = ? ORDER BY cp.fecha DESC, cp.caso_id DESC";

        try (Connection con = ConexionDB.getConnection()) {

            PreparedStatement ps = con.prepareStatement(sql);

            // Se asigna el ID del vendedor
            ps.setInt(1, vendedorId);

            try (ResultSet rs = ps.executeQuery()) {

                // Se recorren los resultados y se mapean a objetos
                while (rs.next()) lista.add(mapearCaso(rs));
            }

            ps.close();
        }

        return lista;
    }

    /**
     * Lista todos los casos de postventa del sistema
     */
    public List<CasoPostventa> listarTodos() throws Exception {

        List<CasoPostventa> lista = new ArrayList<>();

        // Orden descendente por fecha e ID
        String sql = SQL_BASE + " ORDER BY cp.fecha DESC, cp.caso_id DESC";

        try (Connection con = ConexionDB.getConnection()) {

            PreparedStatement ps = con.prepareStatement(sql);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) lista.add(mapearCaso(rs));
            }

            ps.close();

        } catch (SQLException e) {

            // Manejo de error
            System.err.println("Error SQL en listarTodos (PostventaDAO): " + e.getMessage());
            throw e;
        }

        return lista;
    }

    /**
     * Lista los casos de postventa asociados a una venta específica
     * 
     * Usa una consulta SQL propia para evitar conflictos de alias
     */
    public List<CasoPostventa> listarPorVenta(int ventaId) throws Exception {

        List<CasoPostventa> lista = new ArrayList<>();

        String sql = """
            SELECT 
                cp.caso_id,
                cp.venta_id,
                cp.tipo,
                cp.cantidad,
                cp.motivo,
                cp.fecha,
                cp.estado,

                -- Nombre del vendedor
                uv.nombre AS vendedor_nombre,

                -- Nombre del cliente
                cl.nombre AS cliente_nombre,

                -- Producto asociado (subconsulta)
                (SELECT dv2.producto_id FROM Detalle_Venta dv2 
                 WHERE dv2.venta_id = cp.venta_id LIMIT 1) AS producto_id,

                -- Nombre del producto
                (SELECT p2.nombre FROM Detalle_Venta dv2 
                 LEFT JOIN Producto p2 ON p2.producto_id = dv2.producto_id
                 WHERE dv2.venta_id = cp.venta_id LIMIT 1) AS producto_nombre,

                -- Última observación del historial
                h.observacion AS observacion

            FROM Caso_Postventa cp

            -- Relación con venta
            LEFT JOIN Venta ven ON ven.venta_id = cp.venta_id

            -- Relación con vendedor
            LEFT JOIN Usuario uv ON uv.usuario_id = ven.usuario_id

            -- Relación con cliente
            LEFT JOIN Cliente cl ON cl.cliente_id = ven.cliente_id

            -- Último historial
            LEFT JOIN Historial_Caso_Postventa h
                ON h.historial_id = (
                    SELECT MAX(h2.historial_id)
                    FROM Historial_Caso_Postventa h2
                    WHERE h2.caso_id = cp.caso_id
                )

            WHERE cp.venta_id = ?

            ORDER BY cp.fecha DESC, cp.caso_id DESC
            """;

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, ventaId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) lista.add(mapearCaso(rs));
            }
        }

        return lista;
    }

    /**
     * Obtiene un caso de postventa por su ID
     */
    public CasoPostventa obtenerPorId(int casoId) throws Exception {

        String sql = SQL_BASE + " WHERE cp.caso_id = ?";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, casoId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) return mapearCaso(rs);
            }
        }

        return null;
    }

    /**
     * Registra un nuevo caso de postventa
     */
    public int registrar(CasoPostventa caso) throws Exception {

        String sql = "INSERT INTO Caso_Postventa(venta_id, tipo, cantidad, motivo, fecha, estado) VALUES(?, ?, ?, ?, NOW(), 'en_proceso')";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, caso.getVentaId());
            ps.setString(2, caso.getTipo());
            ps.setInt(3, caso.getCantidad());
            ps.setString(4, caso.getMotivo() != null ? caso.getMotivo() : "");

            int filas = ps.executeUpdate();

            // Obtener ID generado automáticamente
            if (filas > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }

            return -1;

        } catch (SQLException e) {

            System.err.println("Error al registrar caso postventa: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Actualiza el estado de un caso de postventa y guarda el historial
     */
    public boolean actualizarEstado(int casoId, String nuevoEstado, String observacion, int usuarioId) throws Exception {

        final String sqlUpdate    = "UPDATE Caso_Postventa SET estado = ? WHERE caso_id = ?";
        final String sqlHistorial = "INSERT INTO Historial_Caso_Postventa(caso_id, estado, observacion, usuario_id, fecha) VALUES(?,?,?,?,NOW())";

        try (Connection con = ConexionDB.getConnection()) {

            // Se inicia una transacción
            con.setAutoCommit(false);

            try {

                // Actualizar estado del caso
                try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                    ps.setString(1, nuevoEstado);
                    ps.setInt(2, casoId);
                    ps.executeUpdate();
                }

                // Registrar en historial
                try (PreparedStatement ps = con.prepareStatement(sqlHistorial)) {
                    ps.setInt(1, casoId);
                    ps.setString(2, nuevoEstado);
                    ps.setString(3, observacion != null ? observacion : "");
                    ps.setInt(4, usuarioId);
                    ps.executeUpdate();
                }

                // Verificar si es devolución aprobada
                CasoPostventa caso = obtenerPorId(casoId);

                if (caso != null && "devolucion".equals(caso.getTipo()) && "aprobado".equals(nuevoEstado)) {

                    // Retornar stock al inventario
                    new VentaDAO().retornarStockDevolucion(
                        caso.getVentaId(), caso.getProductoId(), caso.getCantidad(), usuarioId
                    );
                }

                // Confirmar transacción
                con.commit();

                return true;

            } catch (Exception e) {

                // Revertir cambios si ocurre error
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Convierte un ResultSet en un objeto CasoPostventa
     */
    private CasoPostventa mapearCaso(ResultSet rs) throws SQLException {

        CasoPostventa c = new CasoPostventa();

        c.setCasoId(rs.getInt("caso_id"));
        c.setVentaId(rs.getInt("venta_id"));
        c.setTipo(rs.getString("tipo"));
        c.setCantidad(rs.getInt("cantidad"));
        c.setMotivo(rs.getString("motivo"));
        c.setFecha(rs.getDate("fecha"));
        c.setEstado(rs.getString("estado"));

        // Datos adicionales
        c.setVendedorNombre(rs.getString("vendedor_nombre"));
        c.setClienteNombre(rs.getString("cliente_nombre"));
        c.setObservacion(rs.getString("observacion"));

        // Manejo de posible null en producto_id
        int productoId = 0;
        if (rs.getObject("producto_id") != null) productoId = rs.getInt("producto_id");

        c.setProductoId(productoId);
        c.setProductoNombre(rs.getString("producto_nombre"));

        return c;
    }
}