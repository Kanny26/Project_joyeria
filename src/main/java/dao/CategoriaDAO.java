package dao;

import config.ConexionDB;
import model.Categoria;
import model.Subcategoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de categorías para catálogo de joyería: organiza familias de producto y sus subcategorías válidas.
 * Sirve para que formularios y listados trabajen con relaciones coherentes entre categoría principal y
 * opciones derivadas, evitando combinaciones inválidas al registrar productos.
 */
public class CategoriaDAO {

    /**
     * Retorna todas las categorías ordenadas alfabéticamente.
     * CAMBIO: el SELECT ya no incluye subcategoria_id (no existe en la BD nueva).
     *
     * @return lista de categorías (posiblemente vacía si hay error)
     */
    public List<Categoria> listarCategorias() {
        List<Categoria> lista = new ArrayList<>();
        // Consulta de negocio: trae categorías visibles del catálogo y ORDER BY nombre mejora la experiencia
        // del usuario al mostrar listas alfabéticas en formularios.
        String sql = "SELECT categoria_id, nombre, icono FROM Categoria ORDER BY nombre ASC";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categoria c = new Categoria();
                c.setCategoriaId(rs.getInt("categoria_id"));
                c.setNombre(rs.getString("nombre"));
                c.setIcono(rs.getString("icono"));
                lista.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Busca una categoría por ID.
     * CAMBIO: igual que listarCategorias(), sin subcategoria_id.
     *
     * @param id {@code categoria_id}
     * @return categoría o {@code null}
     */
    public Categoria obtenerPorId(int id) {
        Categoria c = null;
        String sql = "SELECT categoria_id, nombre, icono FROM Categoria WHERE categoria_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    c = new Categoria();
                    c.setCategoriaId(rs.getInt("categoria_id"));
                    c.setNombre(rs.getString("nombre"));
                    c.setIcono(rs.getString("icono"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    /**
     * NUEVO: retorna las subcategorías disponibles para una categoría específica,
     * consultando la tabla Categoria_Subcategoria.
     * Se usa en el servlet para poblar el select de subcategorías al registrar
     * o editar un producto, filtrando solo las opciones válidas para esa categoría.
     *
     * @param categoriaId categoría padre
     * @return subcategorías permitidas para esa categoría
     */
    public List<Subcategoria> obtenerSubcategoriasDisponibles(int categoriaId) {
        List<Subcategoria> lista = new ArrayList<>();
        String sql = """
            SELECT s.subcategoria_id, s.nombre
            FROM Subcategoria s
            INNER JOIN Categoria_Subcategoria cs ON cs.subcategoria_id = s.subcategoria_id
            WHERE cs.categoria_id = ?
            ORDER BY s.nombre ASC
            """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Subcategoria s = new Subcategoria();
                    s.setSubcategoriaId(rs.getInt("subcategoria_id"));
                    s.setNombre(rs.getString("nombre"));
                    lista.add(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Verifica si una categoría tiene productos activos antes de eliminarla.
     *
     * @param categoriaId identificador de categoría
     * @return {@code true} si existe al menos un producto activo
     * @throws Exception si falla la consulta
     */
    public boolean tieneProductosActivos(int categoriaId) throws Exception {
        String sql = "SELECT COUNT(*) FROM Producto WHERE categoria_id = ? AND estado = 1";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Inserta una nueva categoría.
     * CAMBIO: el INSERT ya no incluye subcategoria_id.
     *
     * @param c datos de nombre e icono
     * @return {@code true} si se insertó
     * @throws Exception si falla la transacción
     */
    public boolean guardar(Categoria c) throws Exception {
        String sql = "INSERT INTO Categoria (nombre, icono) VALUES (?, ?)";
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, c.getNombre());
                    ps.setString(2, c.getIcono());
                    ps.executeUpdate();
                }
                con.commit();
                return true;
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /**
     * Actualiza nombre e icono de una categoría existente.
     * CAMBIO: el UPDATE ya no incluye subcategoria_id.
     *
     * @param c categoría con ID
     * @return {@code true} si se actualizó
     * @throws Exception si falla la transacción
     */
    public boolean actualizar(Categoria c) throws Exception {
        String sql = "UPDATE Categoria SET nombre = ?, icono = ? WHERE categoria_id = ?";
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, c.getNombre());
                    ps.setString(2, c.getIcono());
                    ps.setInt(3, c.getCategoriaId());
                    ps.executeUpdate();
                }
                con.commit();
                return true;
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /**
     * Elimina una categoría si no tiene productos activos.
     *
     * @param id {@code categoria_id}
     * @return {@code true} si se eliminó
     * @throws Exception si hay productos activos u otro error
     */
    public boolean eliminar(int id) throws Exception {
        if (tieneProductosActivos(id)) {
            throw new Exception("No se puede eliminar: hay productos activos en esta categoría.");
        }
        String sql = "DELETE FROM Categoria WHERE categoria_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}