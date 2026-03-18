package dao;

import config.ConexionDB;
import model.Subcategoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Maneja las operaciones de base de datos para la tabla Subcategoria.
 *
 * CAMBIO PRINCIPAL: tieneProductosActivos() ya NO busca en Producto.subcategoria_id
 * (esa columna fue eliminada). Ahora consulta la tabla Producto_Subcategoria,
 * que es la que relaciona productos con sus subcategorías en la BD nueva.
 */
public class SubcategoriaDAO {

    /**
     * Retorna todas las subcategorías ordenadas por nombre.
     */
    public List<Subcategoria> listarTodas() {
        List<Subcategoria> lista = new ArrayList<>();
        String sql = "SELECT subcategoria_id, nombre FROM Subcategoria ORDER BY nombre ASC";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Subcategoria s = new Subcategoria();
                s.setSubcategoriaId(rs.getInt("subcategoria_id"));
                s.setNombre(rs.getString("nombre"));
                lista.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Busca una subcategoría por su ID.
     */
    public Subcategoria obtenerPorId(int id) {
        Subcategoria s = null;
        String sql = "SELECT subcategoria_id, nombre FROM Subcategoria WHERE subcategoria_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    s = new Subcategoria();
                    s.setSubcategoriaId(rs.getInt("subcategoria_id"));
                    s.setNombre(rs.getString("nombre"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * Verifica si hay productos activos asociados a esta subcategoría.
     *
     * CAMBIO: antes buscaba en Producto.subcategoria_id (columna eliminada).
     * Ahora busca en Producto_Subcategoria haciendo JOIN con Producto
     * para verificar que el producto esté activo (estado = 1).
     */
    public boolean tieneProductosActivos(int subcategoriaId) throws Exception {
        String sql = """
            SELECT COUNT(*)
            FROM Producto_Subcategoria ps
            INNER JOIN Producto p ON p.producto_id = ps.producto_id
            WHERE ps.subcategoria_id = ? AND p.estado = 1
            """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subcategoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Inserta una nueva subcategoría.
     */
    public boolean guardar(Subcategoria s) throws Exception {
        String sql = "INSERT INTO Subcategoria (nombre) VALUES (?)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getNombre().trim());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza el nombre de una subcategoría existente.
     */
    public boolean actualizar(Subcategoria s) throws Exception {
        String sql = "UPDATE Subcategoria SET nombre = ? WHERE subcategoria_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getNombre().trim());
            ps.setInt(2, s.getSubcategoriaId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Elimina una subcategoría solo si no tiene productos activos.
     *
     * CAMBIO: tieneProductosActivos() ahora usa Producto_Subcategoria,
     * por lo que esta validación sigue funcionando correctamente.
     *
     * Al eliminar la subcategoría, MySQL borra automáticamente las filas
     * correspondientes en Producto_Subcategoria gracias al ON DELETE CASCADE
     * definido en esa tabla, sin dejar datos huérfanos.
     */
    public boolean eliminar(int id) throws Exception {
        if (tieneProductosActivos(id)) {
            throw new Exception(
                "No se puede eliminar esta subcategoría porque tiene productos activos asociados. "
                + "Primero reasigna o elimina esos productos."
            );
        }
        String sql = "DELETE FROM Subcategoria WHERE subcategoria_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new Exception(
                "No se puede eliminar esta subcategoría porque está siendo usada en el sistema."
            );
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                throw new Exception(
                    "No se puede eliminar esta subcategoría porque está siendo usada en el sistema."
                );
            }
            throw e;
        }
    }
}