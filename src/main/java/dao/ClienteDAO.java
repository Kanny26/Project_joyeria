package dao;

import config.ConexionDB;
import model.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a Cliente, Telefono_Cliente y Correo_Cliente en AAC27.
 * La usan los controladores al registrar ventas; ConexionDB y el modelo Cliente.
 */
public class ClienteDAO {

    /**
     * Busca por nombre exacto; si no existe, inserta cliente y datos de contacto en transacción.
     *
     * @param nombre nombre del cliente (clave lógica en esta búsqueda)
     * @param telefono teléfono opcional
     * @param email correo opcional
     * @return {@code cliente_id} existente o recién generado
     * @throws Exception si falla la transacción de alta
     */
    public int registrarOObtenerCliente(String nombre, String telefono, String email) throws Exception {
        try (Connection con = ConexionDB.getConnection()) {

            //1. Buscar cliente por nombre: primero buscamos por nombre para no crear dos veces el mismo cliente
            // durante el registro de una venta.
            String sqlBuscar = """
                SELECT c.cliente_id
                FROM Cliente c
                WHERE c.nombre = ?
                LIMIT 1
                """;
            try (PreparedStatement ps = con.prepareStatement(sqlBuscar)) {
                // setString envía el nombre real digitado por el vendedor; PreparedStatement evita
                // inyección SQL al tratarlo como dato y no como fragmento ejecutable.
                ps.setString(1, nombre);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt("cliente_id");
                }
            }

            // 2. Crear nuevo cliente
            con.setAutoCommit(false);
            try {
                int clienteId;
                String sqlCliente = """
                    INSERT INTO Cliente (nombre, fecha_registro, estado)
                    VALUES (?, CURDATE(), 1)
                    """;
                try (PreparedStatement ps = con.prepareStatement(sqlCliente, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, nombre);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("No se generó cliente_id");
                        clienteId = rs.getInt(1);
                    }
                }

                // Teléfono (opcional)
                if (telefono != null && !telefono.isBlank()) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO Telefono_Cliente (telefono, cliente_id) VALUES (?, ?)")) {
                        ps.setString(1, telefono);
                        ps.setInt(2, clienteId);
                        ps.executeUpdate();
                    }
                }

                // Email (opcional)
                if (email != null && !email.isBlank()) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO Correo_Cliente (email, cliente_id) VALUES (?, ?)")) {
                        ps.setString(1, email);
                        ps.setInt(2, clienteId);
                        ps.executeUpdate();
                    }
                }

                con.commit();
                return clienteId;

            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Lista clientes con teléfonos y correos agregados en una sola consulta.
     *
     * @return lista ordenada por nombre; puede estar vacía si hay error
     */
    public List<Cliente> listarClientes() {
        List<Cliente> lista = new ArrayList<>();
        String sql = """
            SELECT c.cliente_id, c.nombre, c.documento, c.estado, c.fecha_registro,
                   GROUP_CONCAT(DISTINCT tc.telefono) AS telefonos,
                   GROUP_CONCAT(DISTINCT cc.email)    AS correos
            FROM Cliente c
            LEFT JOIN Telefono_Cliente tc ON tc.cliente_id = c.cliente_id
            LEFT JOIN Correo_Cliente   cc ON cc.cliente_id = c.cliente_id
            GROUP BY c.cliente_id, c.nombre, c.documento, c.estado, c.fecha_registro
            ORDER BY c.nombre
            """;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Cliente c = new Cliente();
                c.setClienteId(rs.getInt("cliente_id"));
                c.setNombre(rs.getString("nombre"));
                c.setDocumento(rs.getString("documento"));
                c.setEstado(rs.getBoolean("estado"));
                c.setFechaRegistro(rs.getString("fecha_registro"));
                c.setTelefonos(rs.getString("telefonos"));
                c.setCorreos(rs.getString("correos"));
                lista.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}
