package dao;

import config.ConexionDB;
import model.Material;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Maneja las operaciones de base de datos para la tabla Material.
 * Los materiales se muestran como filtro de búsqueda en categoria.jsp
 * y se gestionan en el tab "Materiales" de org-categorias.jsp.
 *
 * NOTA: Este archivo no estaba incluido en el proyecto original. Fue generado
 * siguiendo la estructura y los métodos que MaterialServlet espera llamar.
 */
public class MaterialDAO {

    /**
     * Retorna todos los materiales ordenados por nombre.
     * En caso de error retorna lista vacía para que el JSP no falle al iterar.
     */
    public List<Material> listarMateriales() {
        List<Material> lista = new ArrayList<>();
        String sql = "SELECT material_id, nombre FROM Material ORDER BY nombre ASC";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Material m = new Material();
                m.setMaterialId(rs.getInt("material_id"));
                m.setNombre(rs.getString("nombre"));
                lista.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Busca un material por su ID.
     * Retorna null si no existe.
     */
    public Material obtenerPorId(int id) {
        Material m = null;
        String sql = "SELECT material_id, nombre FROM Material WHERE material_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m = new Material();
                    m.setMaterialId(rs.getInt("material_id"));
                    m.setNombre(rs.getString("nombre"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return m;
    }

    /**
     * Inserta un nuevo material.
     * Lanza excepción si falla para que el servlet la capture y muestre al usuario.
     */
    public boolean guardar(Material m) throws Exception {
        String sql = "INSERT INTO Material (nombre) VALUES (?)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.getNombre().trim());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza el nombre de un material existente.
     */
    public boolean actualizar(Material m) throws Exception {
        String sql = "UPDATE Material SET nombre = ? WHERE material_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.getNombre().trim());
            ps.setInt(2, m.getMaterialId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Elimina un material por ID.
     * Retorna true si se eliminó, false si el ID no existía.
     *
     * La BD lanza un error de "foreign key constraint" si hay productos que usan este material.
     * Se detecta ese caso específico (código MySQL 1451) y se traduce a un mensaje humano
     * para que el usuario entienda qué está pasando sin ver texto técnico.
     */
    public boolean eliminar(int id) throws Exception {
        String sql = "DELETE FROM Material WHERE material_id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            // Error 1451 en MySQL: se intenta eliminar un registro referenciado por otra tabla.
            // Aquí significa que uno o más productos tienen asignado este material.
            throw new Exception("No se puede eliminar este material porque hay productos que lo usan. Primero reasigna o elimina esos productos.");
        } catch (java.sql.SQLException e) {
            // Fallback por si el driver no lanza SQLIntegrityConstraintViolationException directamente
            if (e.getErrorCode() == 1451) {
                throw new Exception("No se puede eliminar este material porque hay productos que lo usan. Primero reasigna o elimina esos productos.");
            }
            throw e;
        }
    }
}