package dao;

import config.ConexionDB;
import model.Material;
import model.Proveedor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de proveedores: administra relación comercial con proveedores, sus contactos y materiales asociados.
 * El diseño transaccional asegura que datos maestros y auditoría queden consistentes en cada operación.
 */
public class ProveedorDAO {

    // ==================== CONSULTAS ====================

    /**
     * Devuelve todos los proveedores ordenados por fecha de registro (más recientes primero).
     * Para cada proveedor, también carga sus teléfonos, correos y materiales asociados.
     * Si alguna de estas cargas secundarias falla, se asigna una lista vacía en lugar de
     * detener toda la consulta.
     *
     * @return lista de proveedores (puede estar vacía si hay error crítico al listar)
     */
    public List<Proveedor> listarProveedores() {
        List<Proveedor> lista = new ArrayList<>();
        String sql = """
            SELECT proveedor_id, nombre, documento, fecha_registro, fecha_inicio, estado, minimo_compra 
            FROM Proveedor ORDER BY fecha_registro DESC
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Proveedor p = mapearProveedor(rs);
                int id = p.getProveedorId();
                try { p.setTelefonos(obtenerTelefonos(id)); } catch (Exception e) { p.setTelefonos(new ArrayList<>()); }
                try { p.setCorreos(obtenerCorreos(id)); } catch (Exception e) { p.setCorreos(new ArrayList<>()); }
                try { p.setMateriales(obtenerMateriales(id)); } catch (Exception e) { p.setMateriales(new ArrayList<>()); }
                lista.add(p);
            }
        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO al listar proveedores: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Busca un proveedor por su ID único.
     * Devuelve null si no se encuentra, lo que el servlet usa para redirigir al listado.
     *
     * @param id identificador del proveedor
     * @return el proveedor con datos relacionados o {@code null}
     */
    public Proveedor obtenerPorId(Integer id) {
        String sql = """
            SELECT p.proveedor_id, p.nombre, p.documento, p.fecha_registro, p.fecha_inicio, p.estado, p.minimo_compra 
            FROM Proveedor p WHERE p.proveedor_id = ?
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Proveedor p = mapearProveedor(rs);
                    p.setTelefonos(obtenerTelefonos(p.getProveedorId()));
                    p.setCorreos(obtenerCorreos(p.getProveedorId()));
                    p.setMateriales(obtenerMateriales(p.getProveedorId()));
                    return p;
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener proveedor: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Busca proveedores según un término de búsqueda y un tipo de filtro.
     * El parámetro "filtro" determina en qué campo buscar:
     *   - "nombre"    → busca solo en el nombre del proveedor
     *   - "materiales"→ busca en el nombre de los materiales que provee
     *   - "todos"     → busca en nombre O materiales al mismo tiempo
     *
     * El símbolo % alrededor del término permite buscar coincidencias parciales (LIKE).
     * Si el filtro es "todos", se pasa el mismo parámetro dos veces a la consulta SQL.
     *
     * @param q texto de búsqueda
     * @param filtro {@code nombre}, {@code materiales} u otro valor para “todos”
     * @return lista de proveedores (puede estar vacía)
     * @throws Exception si falla la consulta (en algunos casos se captura internamente y retorna lista vacía)
     */
    public List<Proveedor> buscar(String q, String filtro) throws Exception {
        String sql;
        switch (filtro) {
            case "nombre":
                sql = """
                    SELECT DISTINCT p.proveedor_id, p.nombre, p.documento, p.fecha_registro, 
                           p.fecha_inicio, p.estado, p.minimo_compra 
                    FROM Proveedor p 
                    WHERE p.nombre LIKE ?
                    ORDER BY p.fecha_registro DESC
                    """;
                break;
            case "materiales":
                sql = """
                    SELECT DISTINCT p.proveedor_id, p.nombre, p.documento, p.fecha_registro, 
                           p.fecha_inicio, p.estado, p.minimo_compra 
                    FROM Proveedor p 
                    JOIN Proveedor_Material pm ON p.proveedor_id = pm.proveedor_id 
                    JOIN Material m ON pm.material_id = m.material_id 
                    WHERE m.nombre LIKE ?
                    ORDER BY p.fecha_registro DESC
                    """;
                break;
            default: // todos
                sql = """
                    SELECT DISTINCT p.proveedor_id, p.nombre, p.documento, p.fecha_registro, 
                           p.fecha_inicio, p.estado, p.minimo_compra 
                    FROM Proveedor p 
                    LEFT JOIN Proveedor_Material pm ON p.proveedor_id = pm.proveedor_id 
                    LEFT JOIN Material m ON pm.material_id = m.material_id 
                    WHERE p.nombre LIKE ? OR m.nombre LIKE ?
                    ORDER BY p.fecha_registro DESC
                    """;
                break;
        }

        List<Proveedor> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String param = "%" + q + "%";
            ps.setString(1, param);
            // Solo cuando el filtro es "todos" se necesita un segundo parámetro (para el OR del SQL)
            if ("todos".equals(filtro)) ps.setString(2, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Proveedor p = mapearProveedor(rs);
                    int id = p.getProveedorId();
                    try { p.setTelefonos(obtenerTelefonos(id)); } catch (Exception e) { p.setTelefonos(new ArrayList<>()); }
                    try { p.setCorreos(obtenerCorreos(id)); }    catch (Exception e) { p.setCorreos(new ArrayList<>()); }
                    try { p.setMateriales(obtenerMateriales(id)); } catch (Exception e) { p.setMateriales(new ArrayList<>()); }
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Verifica si ya existe un proveedor con ese documento en la base de datos.
     * Se usa al registrar un nuevo proveedor para evitar duplicados.
     */
    public boolean existeDocumento(String documento) {
        String sql = "SELECT COUNT(*) FROM Proveedor WHERE documento = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, documento);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            System.err.println("Error al verificar existencia de documento: " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica si el documento ya está usado por OTRO proveedor diferente al que se está editando.
     * La condición "proveedor_id <> ?" excluye al proveedor actual de la validación,
     * permitiendo que conserve su propio documento sin error de duplicado.
     *
     * @param documento documento a comprobar
     * @param proveedorIdActual ID del proveedor que se está editando (excluido del conteo)
     * @return {@code true} si otro proveedor distinto ya usa ese documento
     */
    public boolean existeDocumentoParaOtro(String documento, int proveedorIdActual) {
        String sql = "SELECT COUNT(*) FROM Proveedor WHERE documento = ? AND proveedor_id <> ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, documento);
            stmt.setInt(2, proveedorIdActual);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            System.err.println("Error al verificar documento para otro: " + e.getMessage());
        }
        return false;
    }

    // ==================== GUARDAR (INSERT) ====================

    /**
     * Guarda un nuevo proveedor junto con todos sus datos relacionados:
     * teléfonos, correos, materiales y un registro de auditoría.
     *
     * Usa una transacción (setAutoCommit false) para que, si alguna inserción falla,
     * se revierta todo y no queden datos incompletos en la base de datos.
     *
     * RETURN_GENERATED_KEYS permite obtener el ID autoincremental que MySQL asigna
     * al nuevo proveedor, necesario para insertar los registros relacionados.
     *
     * Las listas se insertan en lote (addBatch/executeBatch) para mayor eficiencia.
     * Los correos se convierten a minúsculas para evitar duplicados por capitalización.
     *
     * @param p datos del proveedor a insertar
     * @param telefonos lista de teléfonos (puede ser vacía)
     * @param correos lista de correos
     * @param materialesIds IDs de materiales que suministra
     * @param usuarioId usuario que origina la auditoría
     * @return {@code true} si la transacción terminó bien
     */
    public boolean guardar(Proveedor p, List<String> telefonos, List<String> correos, List<Integer> materialesIds, int usuarioId) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            if (conn == null) return false;
            conn.setAutoCommit(false);

            String sqlProveedor = """
                INSERT INTO Proveedor(nombre, documento, fecha_registro, fecha_inicio, estado, minimo_compra) 
                VALUES(?, ?, CURDATE(), ?, ?, ?)
                """;

            int idGenerado = 0;
            try (PreparedStatement stmt = conn.prepareStatement(sqlProveedor, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, p.getNombre());
                stmt.setString(2, p.getDocumento());
                stmt.setString(3, p.getFechaInicio());
                stmt.setBoolean(4, p.isEstado());
                stmt.setDouble(5, p.getMinimoCompra() != null ? p.getMinimoCompra() : 0.0);
                int filas = stmt.executeUpdate();
                if (filas == 0) throw new SQLException("No se pudo insertar el proveedor.");
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        idGenerado = keys.getInt(1);
                        p.setProveedorId(idGenerado);
                    } else {
                        throw new SQLException("No se obtuvo el ID del proveedor generado.");
                    }
                }
            }

            // Insertar teléfonos — se ignoran los vacíos para no guardar entradas en blanco
            if (telefonos != null && !telefonos.isEmpty()) {
                String sqlTel = "INSERT INTO Telefono_Proveedor(telefono, proveedor_id) VALUES(?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlTel)) {
                    for (String tel : telefonos) {
                        if (tel != null && !tel.trim().isEmpty()) {
                            stmt.setString(1, tel.trim());
                            stmt.setInt(2, idGenerado);
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            }

            // Insertar correos — se normalizan a minúsculas antes de guardar
            if (correos != null && !correos.isEmpty()) {
                String sqlCorreo = "INSERT INTO Correo_Proveedor(email, proveedor_id) VALUES(?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlCorreo)) {
                    for (String correo : correos) {
                        if (correo != null && !correo.trim().isEmpty()) {
                            stmt.setString(1, correo.trim().toLowerCase());
                            stmt.setInt(2, idGenerado);
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            }

            // Insertar materiales que suministra este proveedor
            if (materialesIds != null && !materialesIds.isEmpty()) {
                String sqlMat = "INSERT INTO Proveedor_Material(proveedor_id, material_id) VALUES(?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlMat)) {
                    for (Integer matId : materialesIds) {
                        if (matId != null) {
                            stmt.setInt(1, idGenerado);
                            stmt.setInt(2, matId);
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            }

            // RF38: Registro de auditoría — deja constancia de quién creó el proveedor
            registrarAuditoria(conn, usuarioId, "CREAR", "Proveedor", idGenerado, null, p.getNombre());

            conn.commit();
            System.out.println("Proveedor guardado con éxito. ID: " + idGenerado);
            return true;
        } catch (Exception e) {
            System.err.println("ERROR AL GUARDAR PROVEEDOR: " + e.getMessage());
            e.printStackTrace();
            // Si algo falló, se deshacen todos los cambios para no dejar datos a medias
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // ==================== ACTUALIZAR (UPDATE) ====================

    /**
     * Actualiza los datos editables de un proveedor: estado, mínimo de compra,
     * teléfonos, correos y materiales.
     *
     * IMPORTANTE: nombre, documento y fechaInicio NO se actualizan desde aquí
     * porque son campos inmutables según las reglas del negocio (RF11).
     *
     * La estrategia para teléfonos, correos y materiales es "eliminar todo e insertar de nuevo"
     * (DELETE + INSERT), lo que simplifica el manejo de cambios en las listas.
     * Esto funciona correctamente dentro de la transacción.
     *
     * @param p proveedor con ID y campos editables
     * @param telefonos lista nueva de teléfonos
     * @param correos lista nueva de correos
     * @param materialesIds materiales asociados
     * @param usuarioId usuario para auditoría
     * @return {@code true} si la actualización fue exitosa
     */
    public boolean actualizar(Proveedor p, List<String> telefonos, List<String> correos, List<Integer> materialesIds, int usuarioId) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false);

            // Capturar nombre anterior para dejarlo registrado en la auditoría
            String nombreAnterior = "N/A";
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT nombre FROM Proveedor WHERE proveedor_id = ?")) {
                ps.setInt(1, p.getProveedorId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) nombreAnterior = rs.getString("nombre");
                }
            }

            String sqlProveedor = """
                UPDATE Proveedor 
                SET estado = ?, minimo_compra = ? 
                WHERE proveedor_id = ?
                """;
            try (PreparedStatement stmt = conn.prepareStatement(sqlProveedor)) {
                stmt.setBoolean(1, p.isEstado());
                stmt.setDouble(2, p.getMinimoCompra() != null ? p.getMinimoCompra() : 0.0);
                stmt.setInt(3, p.getProveedorId());
                stmt.executeUpdate();
            }

            // Reemplazar teléfonos: se borran todos y se vuelven a insertar los nuevos
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM Telefono_Proveedor WHERE proveedor_id = ?")) {
                stmt.setInt(1, p.getProveedorId()); stmt.executeUpdate();
            }
            if (telefonos != null && !telefonos.isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO Telefono_Proveedor(telefono, proveedor_id) VALUES(?, ?)")) {
                    for (String tel : telefonos) {
                        if (tel != null && !tel.trim().isEmpty()) {
                            stmt.setString(1, tel.trim());
                            stmt.setInt(2, p.getProveedorId());
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            }

            // Reemplazar correos de la misma forma
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM Correo_Proveedor WHERE proveedor_id = ?")) {
                stmt.setInt(1, p.getProveedorId()); stmt.executeUpdate();
            }
            if (correos != null && !correos.isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO Correo_Proveedor(email, proveedor_id) VALUES(?, ?)")) {
                    for (String correo : correos) {
                        if (correo != null && !correo.trim().isEmpty()) {
                            stmt.setString(1, correo.trim().toLowerCase());
                            stmt.setInt(2, p.getProveedorId());
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            }

            // Reemplazar materiales de la misma forma
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM Proveedor_Material WHERE proveedor_id = ?")) {
                stmt.setInt(1, p.getProveedorId()); stmt.executeUpdate();
            }
            if (materialesIds != null && !materialesIds.isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO Proveedor_Material(proveedor_id, material_id) VALUES(?, ?)")) {
                    for (Integer matId : materialesIds) {
                        if (matId != null) {
                            stmt.setInt(1, p.getProveedorId());
                            stmt.setInt(2, matId);
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            }

            registrarAuditoria(conn, usuarioId, "EDITAR", "Proveedor", p.getProveedorId(), nombreAnterior, p.getNombre());
            conn.commit();
            System.out.println("Proveedor actualizado con éxito. ID: " + p.getProveedorId());
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar proveedor: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            return false;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    /**
     * Verifica si un número de teléfono ya está registrado en CUALQUIER proveedor.
     * Se usa al crear un nuevo proveedor para evitar duplicados globales.
     *
     * @param telefono número a normalizar con {@code trim()} en la consulta
     * @return {@code true} si el teléfono ya existe
     */
    public boolean existeTelefonoProveedor(String telefono) {
        String sql = "SELECT COUNT(*) FROM Telefono_Proveedor WHERE telefono = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, telefono.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Verifica si un teléfono ya lo usa OTRO proveedor distinto al que se está editando.
     * Así el proveedor actual puede conservar su propio teléfono sin error de duplicado.
     *
     * @param telefono número a comprobar
     * @param proveedorId ID del proveedor actual (excluido)
     * @return {@code true} si otro proveedor distinto ya usa ese teléfono
     */
    public boolean existeTelefonoParaOtro(String telefono, int proveedorId) {
        String sql = "SELECT COUNT(*) FROM Telefono_Proveedor WHERE telefono = ? AND proveedor_id != ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, telefono.trim());
            ps.setInt(2, proveedorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Verifica si un correo ya está registrado en CUALQUIER proveedor.
     * El correo se normaliza a minúsculas antes de comparar para evitar falsos negativos.
     *
     * @param correo correo electrónico
     * @return {@code true} si ya existe en algún proveedor
     */
    public boolean existeCorreoProveedor(String correo) {
        String sql = "SELECT COUNT(*) FROM Correo_Proveedor WHERE email = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, correo.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Verifica si un correo ya lo usa OTRO proveedor diferente al que se está editando.
     *
     * @param correo correo a comprobar
     * @param proveedorId ID del proveedor actual (excluido)
     * @return {@code true} si otro proveedor distinto ya usa ese correo
     */
    public boolean existeCorreoParaOtroProveedor(String correo, int proveedorId) {
        String sql = "SELECT COUNT(*) FROM Correo_Proveedor WHERE email = ? AND proveedor_id != ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, correo.trim().toLowerCase());
            ps.setInt(2, proveedorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ==================== ESTADO Y ELIMINACIÓN ====================

    /**
     * Cambia el estado activo/inactivo de un proveedor sin eliminarlo físicamente.
     * Se usa desde el botón toggle de la lista de proveedores.
     *
     * @param id identificador del proveedor
     * @param estado nuevo valor de activo/inactivo
     * @return {@code true} si se actualizó al menos una fila
     */
    public boolean actualizarEstado(Integer id, Boolean estado) {
        String sql = "UPDATE Proveedor SET estado = ? WHERE proveedor_id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, estado);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error al actualizar estado: " + e.getMessage());
            return false;
        }
    }

    /**
     * RF13: Eliminación lógica — marca el proveedor como inactivo (estado = 0).
     * El registro no se borra físicamente de la base de datos, solo se desactiva.
     * Esto permite mantener el historial y reactivar el proveedor si es necesario.
     * Registra la operación en auditoría con la acción "ELIMINAR".
     *
     * @param id {@code proveedor_id}
     * @param usuarioId usuario que origina el registro de auditoría
     * @return {@code true} si la transacción terminó bien
     */
    public boolean eliminar(Integer id, int usuarioId) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false);
            
            // Obtener nombre para dejarlo en el registro de auditoría
            String nombre = "ID:" + id;
            try {
                Proveedor p = obtenerPorId(id);
                if(p != null) nombre = p.getNombre();
            } catch(Exception e) {}

            String sql = "UPDATE Proveedor SET estado = 0 WHERE proveedor_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

            registrarAuditoria(conn, usuarioId, "ELIMINAR", "Proveedor", id, nombre, "ELIMINADO_LOGICO");

            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) {} }
            return false;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {} }
        }
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================

    /**
     * Convierte una fila del ResultSet en un objeto Proveedor.
     * El campo minimo_compra se lee como String primero para controlar el caso
     * en que el valor no sea un número válido (NumberFormatException).
     *
     * @param rs fila actual del {@link ResultSet}
     * @return objeto {@link Proveedor} poblado
     * @throws SQLException si falta alguna columna esperada
     */
    private Proveedor mapearProveedor(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setProveedorId(rs.getInt("proveedor_id"));
        p.setNombre(rs.getString("nombre"));
        p.setDocumento(rs.getString("documento"));
        p.setFechaRegistro(rs.getString("fecha_registro"));
        p.setFechaInicio(rs.getString("fecha_inicio"));
        p.setEstado(rs.getBoolean("estado"));
        String minStr = rs.getString("minimo_compra");
        // Si el valor no es un número válido, se asigna 0.0 como valor seguro
        try { p.setMinimoCompra(minStr != null ? Double.parseDouble(minStr) : 0.0); } 
        catch (NumberFormatException e) { p.setMinimoCompra(0.0); }
        return p;
    }

    /**
     * @param proveedorId ID del proveedor
     * @return lista de teléfonos (puede estar vacía)
     */
    private List<String> obtenerTelefonos(Integer proveedorId) {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT telefono FROM Telefono_Proveedor WHERE proveedor_id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, proveedorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(rs.getString("telefono"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    private List<String> obtenerCorreos(Integer proveedorId) {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT email FROM Correo_Proveedor WHERE proveedor_id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, proveedorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(rs.getString("email"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    /**
     * @param proveedorId ID del proveedor
     * @return materiales vinculados en {@code Proveedor_Material}
     */
    private List<Material> obtenerMateriales(Integer proveedorId) {
        List<Material> lista = new ArrayList<>();
        String sql = """
            SELECT m.material_id, m.nombre FROM Material m
            INNER JOIN Proveedor_Material pm ON m.material_id = pm.material_id
            WHERE pm.proveedor_id = ?
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, proveedorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Material m = new Material();
                    m.setMaterialId(rs.getInt("material_id"));
                    m.setNombre(rs.getString("nombre"));
                    lista.add(m);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    private void eliminarTelefonos(Integer proveedorId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Telefono_Proveedor WHERE proveedor_id = ?")) {
            stmt.setInt(1, proveedorId);
            stmt.executeUpdate();
        }
    }

    private void eliminarCorreos(Integer proveedorId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Correo_Proveedor WHERE proveedor_id = ?")) {
            stmt.setInt(1, proveedorId);
            stmt.executeUpdate();
        }
    }

    private void eliminarMateriales(Integer proveedorId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Proveedor_Material WHERE proveedor_id = ?")) {
            stmt.setInt(1, proveedorId);
            stmt.executeUpdate();
        }
    }

    /**
     * Registra una acción en la tabla de auditoría para trazabilidad.
     * Los datos anteriores y nuevos se guardan en formato JSON simple.
     * Las comillas en los valores se escapan para evitar JSON inválido.
     * Esta función recibe la conexión activa para que el registro quede
     * dentro de la misma transacción que la operación principal.
     *
     * @param conn conexión abierta (misma transacción)
     * @param usuarioId usuario que ejecuta la acción
     * @param accion texto de acción (CREAR, EDITAR, ELIMINAR, etc.)
     * @param entidad nombre lógico de la entidad
     * @param entidadId ID del registro afectado
     * @param datosAnteriores JSON simple con valor anterior o {@code null}
     * @param datosNuevos JSON simple con valor nuevo o {@code null}
     * @throws SQLException si falla el insert
     */
    private void registrarAuditoria(Connection conn, int usuarioId, String accion, String entidad, 
            int entidadId, String datosAnteriores, String datosNuevos) throws SQLException {
			String sql = """
			INSERT INTO Auditoria_Log(usuario_id, accion, entidad, entidad_id, datos_anteriores, datos_nuevos, fecha_hora)
			VALUES(?, ?, ?, ?, ?, ?, NOW())
			""";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, usuarioId);
			stmt.setString(2, accion);
			stmt.setString(3, entidad);
			stmt.setInt(4, entidadId);
			stmt.setString(5, datosAnteriores != null 
			? "{\"valor\": \"" + datosAnteriores.replace("\"", "\\\"") + "\"}" 
			: null);
			stmt.setString(6, datosNuevos != null 
			? "{\"valor\": \"" + datosNuevos.replace("\"", "\\\"") + "\"}" 
			: null);
			stmt.executeUpdate();
			}
}
}
