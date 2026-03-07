package dao;

import config.ConexionDB;
import model.Categoria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public List<Categoria> listarCategorias() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT categoria_id, nombre, icono, subcategoria_id FROM Categoria ORDER BY nombre ASC";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categoria c = new Categoria();
                c.setCategoriaId(rs.getInt("categoria_id"));
                c.setNombre(rs.getString("nombre"));
                c.setIcono(rs.getString("icono"));
                // Manejo seguro de subcategoria_id nullable
                if (rs.getObject("subcategoria_id") != null) {
                    c.setSubcategoriaId(rs.getInt("subcategoria_id"));
                }
                lista.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Categoria obtenerPorId(int id) {
        Categoria c = null;
        String sql = "SELECT categoria_id, nombre, icono, subcategoria_id FROM Categoria WHERE categoria_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    c = new Categoria();
                    c.setCategoriaId(rs.getInt("categoria_id"));
                    c.setNombre(rs.getString("nombre"));
                    c.setIcono(rs.getString("icono"));
                    if (rs.getObject("subcategoria_id") != null) {
                        c.setSubcategoriaId(rs.getInt("subcategoria_id"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    // ■■ RF14: Validar si hay productos activos antes de eliminar ■■
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

    // ■■ RF14: Validar si hay subcategorías dependientes (si aplica lógica inversa) ■■
    // En este schema, Categoria tiene subcategoria_id, así que validamos si esta categoría es padre de otras
    // Nota: Según BD.pdf, Subcategoria es tabla independiente y Categoria tiene FK a Subcategoria.
    // La validación crítica es productos activos.

    public boolean guardar(Categoria c) throws Exception {
        String sql = "INSERT INTO Categoria (nombre, icono, subcategoria_id) VALUES (?, ?, ?)";
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, c.getNombre());
                    ps.setString(2, c.getIcono());
                    if (c.getSubcategoriaId() > 0) {
                        ps.setInt(3, c.getSubcategoriaId());
                    } else {
                        ps.setNull(3, java.sql.Types.INTEGER);
                    }
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

    public boolean actualizar(Categoria c) throws Exception {
        String sql = "UPDATE Categoria SET nombre = ?, icono = ?, subcategoria_id = ? WHERE categoria_id = ?";
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, c.getNombre());
                    ps.setString(2, c.getIcono());
                    if (c.getSubcategoriaId() > 0) {
                        ps.setInt(3, c.getSubcategoriaId());
                    } else {
                        ps.setNull(3, java.sql.Types.INTEGER);
                    }
                    ps.setInt(4, c.getCategoriaId());
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

    // ■■ RF14: Eliminación lógica o validación estricta ■■
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