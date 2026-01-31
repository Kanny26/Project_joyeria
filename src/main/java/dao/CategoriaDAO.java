package dao;

import config.ConexionDB;
import model.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) encargado de gestionar
 * las operaciones de acceso a datos relacionadas con la entidad Categoria.
 *
 * Se comunica directamente con la base de datos mediante JDBC
 * y transforma los resultados en objetos del modelo Categoria.
 *
 * ⚠️ Este DAO trabaja sobre la tabla:
 *     Categoria (categoria_id, nombre, icono)
 */
public class CategoriaDAO {

    /**
     * Obtiene la lista completa de categorías registradas en la base de datos.
     *
     * @return Lista de objetos Categoria con id, nombre e icono
     */
    public List<Categoria> listarCategorias() {

        List<Categoria> lista = new ArrayList<>();

        String sql = "SELECT categoria_id, nombre, icono FROM Categoria";

        try (
            Connection con = ConexionDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            // Recorre el resultado y construye la lista de categorías
            while (rs.next()) {
                Categoria c = new Categoria();
                c.setCategoriaId(rs.getInt("categoria_id"));
                c.setNombre(rs.getString("nombre"));
                c.setIcono(rs.getString("icono"));

                lista.add(c);
            }

        } catch (SQLException e) {
            // Manejo básico de errores de base de datos
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * Obtiene una categoría específica a partir de su ID.
     *
     * Método principal recomendado para búsquedas por ID.
     *
     * @param id Identificador de la categoría
     * @return Objeto Categoria si existe, de lo contrario null
     */
    public Categoria obtenerPorId(int id) {

        Categoria c = null;
        String sql = "SELECT categoria_id, nombre, icono FROM Categoria WHERE categoria_id = ?";

        try (
            Connection con = ConexionDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            // Asigna el ID al parámetro de la consulta
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            // Si el registro existe, se construye el objeto
            if (rs.next()) {
                c = new Categoria();
                c.setCategoriaId(rs.getInt("categoria_id"));
                c.setNombre(rs.getString("nombre"));
                c.setIcono(rs.getString("icono"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return c;
    }

    /**
     * Busca una categoría por su ID.
     *
     * ⚠️ Método redundante respecto a {@link #obtenerPorId(int)}.
     * Se mantiene por compatibilidad con código existente.
     *
     * Internamente utiliza la MISMA estructura de tabla y columnas.
     *
     * @param id Identificador de la categoría
     * @return Objeto Categoria si existe, de lo contrario null
     */
    public Categoria buscarPorId(int id) {

        Categoria categoria = null;
        String sql = "SELECT categoria_id, nombre, icono FROM Categoria WHERE categoria_id = ?";

        try (
            Connection con = ConexionDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                categoria = new Categoria();
                categoria.setCategoriaId(rs.getInt("categoria_id"));
                categoria.setNombre(rs.getString("nombre"));
                categoria.setIcono(rs.getString("icono"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categoria;
    }
}

