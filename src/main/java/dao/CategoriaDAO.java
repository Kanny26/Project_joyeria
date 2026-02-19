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
        String sql = "SELECT categoria_id, nombre, icono FROM Categoria";

        try (
            Connection con = ConexionDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
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

    public Categoria obtenerPorId(int id) {

        Categoria c = null;
        String sql = "SELECT categoria_id, nombre, icono FROM Categoria WHERE categoria_id = ?";

        try (
            Connection con = ConexionDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                c = new Categoria();
                c.setCategoriaId(rs.getInt("categoria_id"));
                c.setNombre(rs.getString("nombre"));
                c.setIcono(rs.getString("icono"));
            }

        } catch (Exception e) { 
            e.printStackTrace();
        }

        return c;
    }

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

        } catch (Exception e) { 
            e.printStackTrace();
        }

        return categoria;
    }
}