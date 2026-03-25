package dao;

import config.ConexionDB;
import model.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import services.EmailService;
import services.PasswordGeneratorService;

import java.sql.*;
import java.util.*;

/**
 * DAO para gestión completa de usuarios del sistema.
 * Esta clase centraliza todas las operaciones de acceso a datos relacionadas
 * con la entidad Usuario, incluyendo registro, validación, listado con filtros,
 * edición, auditoría de acciones y consultas estadísticas.
 * Implementa gestión transaccional para operaciones críticas que involucran
 * múltiples tablas (Usuario, Usuario_Rol, Telefono_Usuario, Correo_Usuario)
 * garantizando consistencia de datos. Utiliza BCrypt para encriptación segura
 * de contraseñas, ConexionDB para obtención de conexiones y PreparedStatement
 * para prevenir SQL Injection en todas las consultas.
 */
public class UsuarioDAO {

    /**
     * Verifica si un correo electrónico ya existe registrado en la base de datos.
     * Método de validación utilizado antes de crear nuevos usuarios para prevenir
     * duplicados de contacto que violarían integridad de negocio.
     *
     * Consulta SQL: SELECT COUNT con filtro exacto por email
     * Tabla involucrada: Correo_Usuario (tabla de contactos de usuarios)
     * Condición: WHERE email = ? (búsqueda exacta, case-insensitive por normalización)
     * Pre-procesamiento: toLowerCase().trim() para normalizar entrada antes de comparar
     * Función de agregación: COUNT(*) retorna número de registros que coinciden
     * Lógica de retorno: si COUNT > 0 → true (ya existe), si COUNT = 0 → false (disponible)
     *
     * @param correo dirección de email a validar para unicidad
     * @return {@code true} si el correo ya está registrado en algún usuario,
     *         {@code false} si está disponible para uso
     */
    public boolean existeCorreo(String correo) {
        // SQL: Consulta de conteo para validar unicidad global de correo electrónico
        // Tabla: Correo_Usuario (almacena emails asociados a usuarios)
        // WHERE email = ?: filtro parametrizado para búsqueda exacta y prevención de SQL Injection
        // COUNT(*): retorna 0 si no hay coincidencias, 1 o más si el correo ya existe
        String sql = "SELECT COUNT(*) FROM Correo_Usuario WHERE email = ?";
        // try-with-resources: garantiza cierre automático de Connection, PreparedStatement y ResultSet
        // previniendo fugas de recursos incluso si ocurre excepción durante la ejecución
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Bind parámetro con normalización completa: toLowerCase() + trim()
            // Esto evita falsos negativos por diferencias de mayúsculas/minúsculas o espacios
            ps.setString(1, correo.toLowerCase().trim());
            // Ejecuta consulta de conteo y obtiene ResultSet con una sola fila y columna
            try (ResultSet rs = ps.executeQuery()) {
                // next() avanza a la única fila esperada del resultado de COUNT
                // getInt(1) recupera el valor numérico desde la primera columna
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            // Captura cualquier excepción para diagnóstico sin interrumpir flujo del llamador
            // Imprime stack trace en consola para revisión en logs del servidor
            e.printStackTrace();
        }
        // Retorna false por defecto si no se pudo obtener resultado o hubo error no capturado
        return false;
    }

    /**
     * Verifica si un correo existe para OTRO usuario diferente al que se está editando.
     * La condición "usuario_id <> ?" excluye al usuario actual de la validación,
     * permitiendo que conserve su propio correo sin error de duplicado durante edición.
     *
     * Consulta SQL: SELECT COUNT con dos condiciones
     * Tabla involucrada: Correo_Usuario
     * Condiciones aplicadas:
     *   - WHERE email = ?: busca coincidencias exactas del correo (normalizado)
     *   - AND usuario_id <> ?: excluye el usuario que se está editando
     * Propósito: validar unicidad de correo ignorando el propio registro en contexto de actualización
     *
     * @param correo dirección de email a comprobar para unicidad
     * @param usuarioIdActual ID del usuario que se está editando (excluido del conteo de duplicados)
     * @return {@code true} si otro usuario distinto ya usa ese correo,
     *         {@code false} si está disponible para este usuario
     */
    public boolean existeCorreoParaOtro(String correo, int usuarioIdActual) {
        // SQL: Consulta de conteo para validar unicidad de correo excluyendo usuario actual
        // Tabla: Correo_Usuario
        // WHERE email = ?: filtra por correo específico (normalizado a minúsculas)
        // AND usuario_id <> ?: excluye el registro del usuario que se está editando
        // Esto permite que un usuario mantenga su propio correo al guardar cambios sin falsos positivos
        String sql = "SELECT COUNT(*) FROM Correo_Usuario WHERE email = ? AND usuario_id <> ?";
        // try-with-resources para gestión automática de recursos de base de datos
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Bind parámetros en orden: 1=correo normalizado, 2=ID del usuario actual a excluir
            ps.setString(1, correo.toLowerCase().trim());
            ps.setInt(2, usuarioIdActual);
            // Ejecuta consulta de conteo y evalúa resultado
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            // Registra error en consola para diagnóstico sin propagar excepción
            e.printStackTrace();
        }
        // Retorna false por defecto si no se pudo obtener resultado o hubo error
        return false;
    }

    // ==================== AGREGAR USUARIO ====================

    /**
     * Registra un nuevo usuario en el sistema con gestión transaccional completa.
     * Este método coordina múltiples operaciones atómicas:
     *   1. Validación de unicidad de correo antes de proceder
     *   2. Generación de contraseña segura según rol (o uso de contraseña proporcionada)
     *   3. Encriptación de contraseña con BCrypt antes de persistir
     *   4. Inserción en tabla Usuario y recuperación del usuario_id generado
     *   5. Validación y asignación de rol mediante consulta a tabla Rol
     *   6. Inserción en tabla intermedia Usuario_Rol para relación muchos-a-muchos
     *   7. Inserción de teléfonos múltiples (separados por coma) en Telefono_Usuario
     *   8. Inserción de correo en Correo_Usuario con normalización a minúsculas
     *   9. Commit de transacción solo si todos los pasos completan exitosamente
     *   10. Envío de correo con credenciales al usuario (fuera de transacción)
     *   11. Registro de auditoría con datos de la creación (dentro de try-catch tolerante)
     *
     * Consultas SQL principales:
     *   - INSERT INTO Usuario: crea registro principal con nombre, pass encriptado, estado y fecha
     *   - SELECT rol_id FROM Rol: valida que el cargo exista antes de asignar
     *   - INSERT INTO Usuario_Rol: vincula usuario con su rol mediante FKs
     *   - INSERT INTO Telefono_Usuario: registra cada teléfono en tabla de contactos
     *   - INSERT INTO Correo_Usuario: registra email normalizado en tabla de contactos
     *   - INSERT INTO Auditoria: registra trazabilidad de la acción CREAR
     *
     * Gestión de transacción:
     *   - Desactiva auto-commit al inicio para controlar manualmente la atomicidad
     *   - Ejecuta todas las inserciones en secuencia dentro del mismo bloque try
     *   - Confirma con conn.commit() solo si todos los pasos completan sin excepción
     *   - En caso de error, el bloque catch registra el error y retorna false
     *   - Finalmente se restaura auto-commit implícitamente al cerrar conexión
     *
     * Seguridad:
     *   - BCrypt.hashpw() con BCrypt.gensalt() para encriptación robusta de contraseñas
     *   - PreparedStatement en todas las consultas para prevenir SQL Injection
     *   - Validación explícita de roles permitidos antes de insertar en Usuario_Rol
     *
     * @param usuario objeto Usuario con nombre, rol, teléfono, correo y estado poblados para persistir
     * @return {@code true} si la transacción completó exitosamente todas las inserciones,
     *         {@code false} si falló validación de correo, error de BD o cualquier excepción
     */
    public boolean agregarUsuario(Usuario usuario) {

        // Validación inicial: si se proporciona correo, verifica que no exista ya en el sistema
        // Esto previene duplicados de contacto que violarían reglas de integridad de negocio
        if (usuario.getCorreo() != null && !usuario.getCorreo().trim().isEmpty()) {
            if (existeCorreo(usuario.getCorreo())) {
                System.err.println("ERROR: Correo ya existe: " + usuario.getCorreo());
                return false;
            }
        }

        // SQL 1: INSERT para crear nuevo registro en tabla Usuario (entidad principal)
        // Tabla destino: Usuario (almacena datos de autenticación y perfil de usuarios)
        // Columnas insertadas:
        //   - nombre: nombre completo del usuario para identificación en UI
        //   - pass: contraseña encriptada con BCrypt (NUNCA se almacena en texto plano)
        //   - estado: flag booleano para activación/desactivación de cuenta
        //   - fecha_creacion: NOW() para timestamp automático del servidor de BD
        // Valores parametrizados (?, ?, ?) para seguridad y reutilización de plan de ejecución
        // Configuración RETURN_GENERATED_KEYS: recupera usuario_id autogenerado tras INSERT
        String sqlUsuario = """
            INSERT INTO Usuario(nombre, pass, estado, fecha_creacion) 
            VALUES(?,?,?, NOW())
            """;
        // SQL 2: INSERT para registrar teléfono en tabla de contactos
        // Tabla destino: Telefono_Usuario (relación uno-a-muchos con Usuario)
        // Columnas: telefono (VARCHAR), usuario_id (FK hacia Usuario)
        String sqlTelefono = "INSERT INTO Telefono_Usuario(telefono, usuario_id) VALUES(?,?)";
        // SQL 3: INSERT para registrar correo en tabla de contactos
        // Tabla destino: Correo_Usuario (relación uno-a-muchos con Usuario)
        // Columnas: email (VARCHAR, único en negocio), usuario_id (FK)
        String sqlCorreo   = "INSERT INTO Correo_Usuario(email, usuario_id) VALUES(?,?)";

        // Generación de contraseña: delega en servicio especializado que aplica reglas según rol
        // Si el usuario ya proporcionó contraseña, la usa; si no, genera una segura por defecto
        String contrasenaTextoPlano = PasswordGeneratorService.obtenerContrasena(
            usuario.getRol(),
            usuario.getContrasena()
        );
        // Guarda la contraseña en texto plano temporalmente para envío por email (solo en este flujo)
        usuario.setContrasena(contrasenaTextoPlano);

        // Obtiene conexión manual para controlar explícitamente la transacción atómica
        try (Connection conn = ConexionDB.getConnection()) {
            // Desactiva auto-commit para iniciar transacción manual: o se guardan todos los cambios o ninguno
            conn.setAutoCommit(false);
            int usuarioId;

            // Inserción en tabla Usuario: crea registro principal y recupera ID generado
            // PreparedStatement con configuración para retorno de claves generadas (PK autogenerada)
            try (PreparedStatement ps = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                // Bind parámetros en orden definido en la consulta SQL:
                // 1=nombre, 2=pass encriptado con BCrypt, 3=estado booleano
                ps.setString(1, usuario.getNombre());
                // Encriptación con BCrypt: hashpw() genera hash seguro, gensalt() crea salt único por usuario
                ps.setString(2, BCrypt.hashpw(contrasenaTextoPlano, BCrypt.gensalt()));
                ps.setBoolean(3, usuario.isEstado());
                // Ejecuta INSERT para persistir registro principal de usuario
                ps.executeUpdate();

                // Recupera ResultSet con claves generadas (usuario_id autogenerado por AUTO_INCREMENT)
                ResultSet rs = ps.getGeneratedKeys();
                // next() avanza a la primera fila del resultado de claves generadas
                rs.next();
                // getInt(1) extrae el valor numérico del usuario_id desde la primera columna
                usuarioId = rs.getInt(1);
            }

            // Validación y asignación de rol: normaliza cargo a minúsculas con fallback a "vendedor"
            // Esto garantiza consistencia en nombres de rol independientemente de entrada del usuario
            String cargo = (usuario.getRol() != null && !usuario.getRol().trim().isEmpty())
                ? usuario.getRol().trim().toLowerCase() : "vendedor";

            // Validación explícita de roles permitidos por reglas de negocio
            // Solo se aceptan: superadministrador, administrador o vendedor
            // Cualquier otro valor lanza SQLException para detener transacción
            if (!cargo.equals("superadministrador") && !cargo.equals("administrador") && !cargo.equals("vendedor")) {
                throw new SQLException("Rol no válido: " + cargo);
            }

            int rolId;
            // Consulta auxiliar: obtiene rol_id desde tabla Rol basado en nombre de cargo
            // WHERE cargo=?: filtro parametrizado para búsqueda exacta y prevención de SQL Injection
            try (PreparedStatement psGetRol = conn.prepareStatement("SELECT rol_id FROM Rol WHERE cargo=?")) {
                psGetRol.setString(1, cargo);
                // Ejecuta consulta y obtiene ResultSet con máximo 1 fila (cargo es único en esquema)
                ResultSet rsRol = psGetRol.executeQuery();
                // next() retorna false si no se encuentra el rol, lanzando excepción para rollback
                if (!rsRol.next()) throw new SQLException("Rol no encontrado: " + cargo);
                // getInt() extrae el ID numérico del rol para usar en inserción de relación
                rolId = rsRol.getInt("rol_id");
            }

            // Inserción en tabla intermedia Usuario_Rol: establece relación muchos-a-muchos
            // Esta tabla permite que un usuario tenga múltiples roles en el futuro si el negocio lo requiere
            try (PreparedStatement psRol = conn.prepareStatement("INSERT INTO Usuario_Rol(usuario_id, rol_id) VALUES(?,?)")) {
                // Bind parámetros: 1=usuario_id generado, 2=rol_id obtenido de tabla Rol
                psRol.setInt(1, usuarioId);
                psRol.setInt(2, rolId);
                // Ejecuta INSERT para persistir relación usuario-rol
                psRol.executeUpdate();
            }

            // Inserción de teléfonos múltiples: soporta lista separada por comas en campo teléfono
            // Itera sobre cada teléfono, valida que no esté vacío y ejecuta INSERT individual
            // Nota: podría optimizarse con batch, pero executeUpdate() por ítem es más simple para pocos registros
            if (usuario.getTelefono() != null && !usuario.getTelefono().trim().isEmpty()) {
                try (PreparedStatement psTel = conn.prepareStatement(sqlTelefono)) {
                    // split(",") separa la cadena de entrada en array de teléfonos individuales
                    for (String tel : usuario.getTelefono().split(",")) {
                        if (!tel.trim().isEmpty()) {
                            // Bind parámetros: 1=teléfono con trim() para limpieza, 2=usuario_id
                            psTel.setString(1, tel.trim());
                            psTel.setInt(2, usuarioId);
                            // Ejecuta INSERT para cada teléfono válido
                            psTel.executeUpdate();
                        }
                    }
                }
            }

            // Inserción de correo: normaliza a minúsculas para evitar duplicados por capitalización
            // Ej: "Usuario@Example.com" y "usuario@example.com" se tratan como el mismo email
            if (usuario.getCorreo() != null && !usuario.getCorreo().trim().isEmpty()) {
                try (PreparedStatement psCorreo = conn.prepareStatement(sqlCorreo)) {
                    // Bind parámetros: 1=email normalizado (toLowerCase + trim), 2=usuario_id
                    psCorreo.setString(1, usuario.getCorreo().trim().toLowerCase());
                    psCorreo.setInt(2, usuarioId);
                    // Ejecuta INSERT para persistir registro de correo
                    psCorreo.executeUpdate();
                }
            }

            // Commit final: confirma permanentemente todos los cambios de la transacción
            // Solo se ejecuta si ninguna de las operaciones anteriores lanzó excepción
            conn.commit();

            // Envío de correo con credenciales: se ejecuta FUERA de la transacción principal
            // Si este paso falla, el usuario ya está registrado; el error se maneja en el servicio de email
            // Esto evita rollback innecesario por fallos en infraestructura externa (servidor SMTP)
            if (usuario.getCorreo() != null && !usuario.getCorreo().trim().isEmpty()) {
                EmailService.enviarCredenciales(
                    usuario.getCorreo().trim(),
                    usuario.getNombre(),
                    cargo,
                    contrasenaTextoPlano
                );
            }

            // Registro de auditoría: se ejecuta dentro de try-catch tolerante para no fallar todo el flujo
            // Si la auditoría falla, el usuario ya está creado; el error se registra pero no interrumpe operación
            try {
                // Construye JSON simple con datos nuevos para trazabilidad: nombre y rol del usuario creado
                // org.json.JSONObject.quote() escapa caracteres especiales para mantener JSON válido
                String datosNuevosJson = "{\"nombre\": " + org.json.JSONObject.quote(usuario.getNombre())
                        + ", \"rol\": " + org.json.JSONObject.quote(cargo) + "}";
                // Llama a método helper con conexión activa para que el registro quede en misma transacción
                // Nota: como ya se hizo commit, este INSERT de auditoría usa auto-commit implícito
                registrarAuditoria(conn, usuarioId, "CREAR", "Usuario", usuarioId, null, datosNuevosJson);
            } catch (Exception ignored) {
                // Silenciamos excepción de auditoría intencionalmente: no debe fallar registro de usuario
                // por errores en trazabilidad secundaria
            }

            // Retorna true indicando que la operación principal (registro de usuario) completó exitosamente
            return true;

        } catch (Exception e) {
            // En caso de error en cualquier paso de la transacción, registra stack trace para diagnóstico
            // La conexión se cierra automáticamente por try-with-resources, pero sin commit explícito
            // Esto significa que todos los cambios se revierten implícitamente al cerrar sin commit
            e.printStackTrace();
            // Retorna false indicando que la operación falló y el usuario no fue registrado
            return false;
        }
    }

    // ==================== LISTAR ====================

    /**
     * Lista usuarios con filtros opcionales por rol y estado mediante consulta dinámica.
     * Este método construye una consulta SQL flexible que permite:
     *   - Listar todos los usuarios sin filtros
     *   - Filtrar por rol específico (superadministrador, administrador, vendedor)
     *   - Filtrar por estado (Activo=1, Inactivo=0)
     *   - Combinar ambos filtros con operador AND
     *
     * Consulta SQL base: SELECT con múltiples LEFT JOINs y funciones de agregación
     * Tablas involucradas:
     *   - Usuario (alias 'u'): tabla principal con datos de autenticación y perfil
     *   - Usuario_Rol (alias 'ur'): LEFT JOIN para vincular usuario con su rol
     *   - Rol (alias 'r'): LEFT JOIN para obtener nombre legible del cargo
     *   - Telefono_Usuario (alias 't'): LEFT JOIN para obtener teléfonos asociados
     *   - Correo_Usuario (alias 'c'): LEFT JOIN para obtener correos asociados
     *
     * Funciones de agregación:
     *   - GROUP_CONCAT(DISTINCT t.telefono) AS telefonos: concatena múltiples teléfonos en una cadena
     *   - GROUP_CONCAT(DISTINCT c.email) AS correos: concatena múltiples emails en una cadena
     *   - DISTINCT en ambos casos evita duplicados si hay relaciones repetidas en JOINs
     *
     * Cláusula WHERE dinámica:
     *   - WHERE 1=1: patrón útil para construir condiciones adicionales con AND sin lógica especial
     *   - AND r.cargo = ?: se añade solo si filtroRol es válido y no es "todos"
     *   - AND u.estado = ?: se añade solo si filtroEstado es válido; convierte "Activo" a 1, otro valor a 0
     *
     * Agrupamiento: GROUP BY por campos no agregados para cumplir con SQL estándar
     * Ordenamiento: implícito por orden de inserción (podría añadirse ORDER BY si se requiere)
     *
     * Flujo de mapeo:
     *   - Itera sobre ResultSet creando objeto Usuario por cada fila
     *   - Mapea campos escalares directamente (usuario_id, nombre, estado, fecha_creacion, cargo)
     *   - Mapea campos concatenados (telefonos, correos) como cadenas separadas por coma
     *   - Retorna lista poblada o vacía si no hay resultados o hay error
     *
     * @param filtroRol criterio opcional para filtrar por cargo: "superadministrador", "administrador",
     *                  "vendedor", "todos" o null para sin filtro
     * @param filtroEstado criterio opcional para filtrar por estado: "Activo", "Inactivo",
     *                     "todos" o null para sin filtro
     * @return lista de objetos Usuario con datos principales y contactos concatenados,
     *         o lista vacía si no hay resultados o ocurre error
     */
    public List<Usuario> listarUsuarios(String filtroRol, String filtroEstado) {
        // Lista que almacenará cada usuario recuperado de la consulta
        List<Usuario> lista = new ArrayList<>();

        // Construye consulta SQL base con StringBuilder para permitir modificación dinámica
        // SELECT con LEFT JOINs para recuperar usuario con rol, teléfonos y correos relacionados
        // GROUP_CONCAT con DISTINCT concatena múltiples valores en una sola cadena separada por comas
        // WHERE 1=1: patrón útil para añadir condiciones adicionales con AND sin lógica especial de primer filtro
        StringBuilder sql = new StringBuilder("""
            SELECT u.usuario_id, u.nombre, u.estado, u.fecha_creacion, r.cargo, 
                   GROUP_CONCAT(DISTINCT t.telefono) AS telefonos, 
                   GROUP_CONCAT(DISTINCT c.email) AS correos 
            FROM Usuario u 
            LEFT JOIN Usuario_Rol ur ON ur.usuario_id = u.usuario_id 
            LEFT JOIN Rol r ON r.rol_id = ur.rol_id 
            LEFT JOIN Telefono_Usuario t ON u.usuario_id = t.usuario_id 
            LEFT JOIN Correo_Usuario c ON u.usuario_id = c.usuario_id 
            WHERE 1=1
            """);

        // Lista para almacenar parámetros en orden de aparición en la consulta final
        // Esto permite bind dinámico manteniendo seguridad con PreparedStatement
        List<Object> params = new ArrayList<>();

        // Añade filtro por rol solo si es válido y no es "todos" (que significa sin filtro)
        // El valor se normaliza a minúsculas para coincidencia exacta con esquema de BD
        if (filtroRol != null && !filtroRol.isEmpty() && !"todos".equals(filtroRol)) {
            sql.append(" AND r.cargo = ?");
            params.add(filtroRol.toLowerCase());
        }

        // Añade filtro por estado solo si es válido y no es "todos"
        // Convierte texto "Activo" a 1 (boolean true en BD), cualquier otro valor a 0 (false)
        if (filtroEstado != null && !filtroEstado.isEmpty() && !"todos".equals(filtroEstado)) {
            sql.append(" AND u.estado = ?");
            params.add("Activo".equals(filtroEstado) ? 1 : 0);
        }

        // Añade cláusula GROUP BY para cumplir con SQL estándar: agrupa por campos no agregados
        // Esto permite usar GROUP_CONCAT sin errores de sintaxis en motores SQL estrictos
        sql.append(" GROUP BY u.usuario_id, u.nombre, u.estado, u.fecha_creacion, r.cargo");

        // try-with-resources para gestión automática de Connection y PreparedStatement
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            // Bind dinámico de parámetros: itera sobre lista params asignando cada valor en orden
            // setObject() maneja automáticamente conversión de tipos (String, Integer, etc.) a tipos JDBC
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            // Ejecuta consulta y obtiene ResultSet con usuarios que coinciden con filtros aplicados
            try (ResultSet rs = ps.executeQuery()) {
                // Itera sobre cada fila del resultado para mapearla a objeto Usuario
                while (rs.next()) {
                    // Crea nueva instancia de Usuario para la fila actual
                    Usuario u = new Usuario();
                    // Mapea campos escalares directamente desde ResultSet usando getters tipo-safe
                    u.setUsuarioId(rs.getInt("usuario_id"));
                    u.setNombre(rs.getString("nombre"));
                    u.setEstado(rs.getBoolean("estado"));
                    u.setFechaCreacion(rs.getDate("fecha_creacion"));
                    // Mapea cargo desde tabla Rol (puede ser null si no tiene rol asignado)
                    u.setRol(rs.getString("cargo"));
                    // Mapea teléfonos concatenados por GROUP_CONCAT (cadena separada por comas o null)
                    u.setTelefono(rs.getString("telefonos"));
                    // Mapea correos concatenados por GROUP_CONCAT (cadena separada por comas o null)
                    u.setCorreo(rs.getString("correos"));
                    // Agrega objeto Usuario poblado a lista de retorno
                    lista.add(u);
                }
            }
        } catch (Exception e) {
            // Captura cualquier excepción para diagnóstico sin interrumpir flujo del llamador
            // Imprime stack trace en consola para revisión en logs del servidor
            e.printStackTrace();
        }
        // Retorna lista poblada; si no hay resultados o hay error, retorna lista vacía (nunca null)
        // Esto garantiza que la capa de presentación pueda iterar sin validar null
        return lista;
    }

    // ==================== EDITAR ====================

    /**
     * Edita un usuario existente y registra auditoría de la modificación.
     * Este método actualiza campos editables de un usuario (nombre y estado)
     * manteniendo inmutables otros campos como contraseña y fecha de creación
     * por reglas de seguridad y trazabilidad del negocio.
     *
     * Consulta SQL principal: UPDATE en tabla Usuario con filtro por clave primaria
     * Tabla afectada: Usuario
     * Columnas modificadas:
     *   - nombre: nuevo nombre completo del usuario para identificación en UI
     *   - estado: nuevo flag booleano para activar/desactivar cuenta
     * Condición: WHERE usuario_id = ? (actualiza solo el registro con PK específica)
     * Parámetros en orden: 1=nombre, 2=estado, 3=usuarioId (para el WHERE)
     *
     * Flujo de auditoría:
     *   1. Recupera estado anterior del usuario mediante obtenerUsuarioPorId()
     *   2. Ejecuta UPDATE para aplicar cambios
     *   3. Confirma transacción con conn.commit()
     *   4. (Opcional) Podría registrar auditoría con datos anteriores/nuevos
     *      (actualmente no implementado en este método, pero preparado en estructura)
     *
     * Gestión de transacción:
     *   - Desactiva auto-commit al inicio para controlar manualmente la atomicidad
     *   - Ejecuta UPDATE dentro de bloque try para detectar errores
     *   - Confirma con conn.commit() solo si el UPDATE completa sin excepción
     *   - En caso de error, ejecuta conn.rollback() para revertir cambios
     *   - Finalmente restaura auto-commit implícitamente al cerrar conexión
     *
     * Nota de diseño: este método actualmente solo actualiza nombre y estado.
     * Para actualizar teléfono, correo o rol se requerirían operaciones adicionales
     * en tablas relacionadas (Telefono_Usuario, Correo_Usuario, Usuario_Rol)
     * que podrían implementarse en una versión futura manteniendo atomicidad transaccional.
     *
     * @param usuario objeto Usuario con usuarioId, nombre y estado actualizados para persistir
     * @param usuarioQueEditaId ID del usuario que realiza la edición para futura trazabilidad en auditoría
     * @return {@code true} si se actualizó exitosamente al menos una fila (usuario existente),
     *         {@code false} si ocurrió error de conexión, sintaxis SQL o rollback por excepción
     */
    public boolean editarUsuario(Usuario usuario, int usuarioQueEditaId) {
        // Inicializa referencia a conexión para manejo explícito en bloque finally
        Connection conn = null;
        try {
            // Obtiene conexión manual para controlar explícitamente la transacción
            conn = ConexionDB.getConnection();
            // Desactiva auto-commit para iniciar transacción manual atómica
            conn.setAutoCommit(false);

            // Recupera estado anterior del usuario para posible registro de auditoría
            // Esto permite comparar datos antes/después para trazabilidad de cambios
            Usuario anterior = obtenerUsuarioPorId(usuario.getUsuarioId());

            // SQL: UPDATE para modificar campos editables de usuario existente
            // Tabla destino: Usuario
            // SET nombre = ?, estado = ?: asigna nuevos valores a columnas modificables
            // WHERE usuario_id = ?: filtra por PK para actualizar solo registro específico
            // Parámetros: 1=nombre, 2=estado, 3=usuarioId (bindados en ese orden)
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Usuario SET nombre=?, estado=? WHERE usuario_id=?")) {
                // Bind parámetros en orden definido en la consulta SQL:
                // 1=nuevo nombre, 2=nuevo estado booleano, 3=ID del usuario a modificar
                ps.setString(1, usuario.getNombre());
                ps.setBoolean(2, usuario.isEstado());
                ps.setInt(3, usuario.getUsuarioId());
                // Ejecuta UPDATE y descarta número de filas afectadas (se espera 1 si usuario existe)
                ps.executeUpdate();
            }

            // Confirma la transacción: hace permanentes los cambios si el UPDATE fue exitoso
            conn.commit();

            // Retorna true indicando que la operación completó exitosamente
            return true;

        } catch (Exception e) {
            // En caso de error, intenta revertir cambios para mantener consistencia de datos
            // El bloque try-catch anidado previene que error en rollback oculte excepción original
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            // Retorna false indicando que la operación falló y los cambios se revirtieron
            return false;
        }
    }

    // ==================== AUXILIARES ====================

    /**
     * Obtiene un usuario por su identificador único con información relacionada consolidada.
     * Método utilizado para cargar datos completos de un usuario para edición,
     * visualización de perfil o validaciones de negocio.
     *
     * Consulta SQL: SELECT con múltiples LEFT JOINs y funciones de agregación
     * Tablas involucradas:
     *   - Usuario (alias 'u'): tabla principal con datos de autenticación y perfil
     *   - Usuario_Rol (alias 'ur'): LEFT JOIN para vincular usuario con su rol
     *   - Rol (alias 'r'): LEFT JOIN para obtener nombre legible del cargo
     *   - Telefono_Usuario (alias 't'): LEFT JOIN para obtener teléfonos asociados
     *   - Correo_Usuario (alias 'c'): LEFT JOIN para obtener correos asociados
     *
     * Funciones de agregación:
     *   - GROUP_CONCAT(DISTINCT t.telefono) AS telefonos: concatena múltiples teléfonos en una cadena
     *   - GROUP_CONCAT(DISTINCT c.email) AS correos: concatena múltiples emails en una cadena
     *   - DISTINCT en ambos casos evita duplicados si hay relaciones repetidas en JOINs
     *
     * Condición: WHERE u.usuario_id = ? (búsqueda por PK, eficiente con índice)
     * Agrupamiento: GROUP BY por campos no agregados para cumplir con SQL estándar
     * Parámetro: id (int) → bindado al primer placeholder (?) mediante PreparedStatement
     * Retorno esperado: máximo 1 fila (por clave primaria única)
     *
     * Flujo de mapeo:
     *   - Si rs.next() es true, crea objeto Usuario y mapea todos los campos
     *   - Campos escalares: usuario_id, nombre, estado, fecha_creacion, cargo
     *   - Campos concatenados: telefonos, correos (cadenas separadas por comas o null)
     *   - Retorna objeto completamente poblado; si no hay resultados, retorna null
     *
     * @param id valor de {@code usuario_id} a buscar
     * @return objeto Usuario con datos principales y contactos concatenados,
     *         o {@code null} si no se encuentra el usuario o ocurre error
     */
    public Usuario obtenerUsuarioPorId(int id) {
        // SQL: SELECT con LEFT JOINs y GROUP_CONCAT para recuperar usuario completo con contactos
        // Tabla principal: Usuario (u) → datos de autenticación y perfil
        // LEFT JOIN Usuario_Rol (ur) → vincula usuario con su rol (puede ser NULL si no tiene rol)
        // LEFT JOIN Rol (r) → obtiene nombre legible del cargo desde tabla maestra
        // LEFT JOIN Telefono_Usuario (t) → obtiene teléfonos asociados (puede ser NULL)
        // LEFT JOIN Correo_Usuario (c) → obtiene correos asociados (puede ser NULL)
        // GROUP_CONCAT(DISTINCT ...): concatena múltiples valores en cadena separada por comas
        // WHERE u.usuario_id = ?: filtro por PK para búsqueda eficiente de registro único
        // GROUP BY: agrupa por campos no agregados para cumplir con SQL estándar
        String sql = """
            SELECT u.usuario_id, u.nombre, u.estado, u.fecha_creacion, r.cargo,
                   GROUP_CONCAT(DISTINCT t.telefono) AS telefonos,
                   GROUP_CONCAT(DISTINCT c.email)    AS correos
            FROM Usuario u
            LEFT JOIN Usuario_Rol ur ON ur.usuario_id = u.usuario_id
            LEFT JOIN Rol r          ON r.rol_id = ur.rol_id
            LEFT JOIN Telefono_Usuario t ON u.usuario_id = t.usuario_id
            LEFT JOIN Correo_Usuario c   ON u.usuario_id = c.usuario_id
            WHERE u.usuario_id = ?
            GROUP BY u.usuario_id, u.nombre, u.estado, u.fecha_creacion, r.cargo
            """;

        // try-with-resources para gestión automática de Connection y PreparedStatement
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Bind del parámetro: asigna id al primer placeholder (?) de la consulta
            ps.setInt(1, id);
            // Ejecuta consulta y obtiene ResultSet con máximo 1 fila (por PK única)
            try (ResultSet rs = ps.executeQuery()) {
                // next() retorna true solo si existe al menos una fila en el resultado
                if (rs.next()) {
                    // Crea instancia de Usuario para mapear los datos recuperados
                    Usuario u = new Usuario();
                    // Mapea campos escalares directamente desde ResultSet usando getters tipo-safe
                    u.setUsuarioId(rs.getInt("usuario_id"));
                    u.setNombre(rs.getString("nombre"));
                    u.setEstado(rs.getBoolean("estado"));
                    u.setFechaCreacion(rs.getDate("fecha_creacion"));
                    // Mapea cargo desde tabla Rol (puede ser null si no tiene rol asignado)
                    u.setRol(rs.getString("cargo"));
                    // Mapea teléfonos concatenados por GROUP_CONCAT (cadena separada por comas o null)
                    u.setTelefono(rs.getString("telefonos"));
                    // Mapea correos concatenados por GROUP_CONCAT (cadena separada por comas o null)
                    u.setCorreo(rs.getString("correos"));
                    // Retorna objeto Usuario completamente poblado
                    return u;
                }
            }
        } catch (Exception e) {
            // Captura cualquier excepción para diagnóstico sin propagar excepción
            // Imprime stack trace en consola para revisión en logs del servidor
            e.printStackTrace();
        }
        // Retorna null si no se encontró el usuario o ocurrió error no capturado
        return null;
    }

    /**
     * Cuenta el total de usuarios registrados en el sistema.
     * Método auxiliar para estadísticas y dashboards de administración.
     *
     * Consulta SQL: SELECT COUNT(*) sobre tabla Usuario
     * Tabla involucrada: Usuario
     * Función de agregación: COUNT(*) retorna número total de filas en la tabla
     * Sin condiciones WHERE: cuenta todos los usuarios sin filtrar por estado o rol
     *
     * @return número total de usuarios registrados, o 0 si ocurre error de consulta
     */
    public int contarUsuarios() {
        // SQL: Consulta de conteo para obtener total de usuarios en sistema
        // Tabla: Usuario
        // COUNT(*): función agregada que retorna número de filas en la tabla
        // Sin WHERE: incluye todos los usuarios independientemente de estado o rol
        String sql = "SELECT COUNT(*) FROM Usuario";
        // try-with-resources para gestión automática de recursos de base de datos
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            // next() avanza a la única fila esperada del resultado de COUNT
            // getInt(1) recupera el valor numérico desde la primera columna
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            // Captura excepción para diagnóstico sin interrumpir flujo del llamador
            e.printStackTrace();
        }
        // Retorna 0 por defecto si no se pudo obtener resultado o hubo error
        return 0;
    }

    /**
     * Cuenta el número de usuarios activos en el sistema.
     * Método auxiliar para métricas de engagement y reportes de administración.
     *
     * Consulta SQL: SELECT COUNT con filtro por estado
     * Tabla involucrada: Usuario
     * Condición: WHERE estado = 1 (filtra solo usuarios con flag activo)
     * Función de agregación: COUNT(*) retorna número de filas que cumplen la condición
     *
     * @return número de usuarios con estado activo (estado = 1), o 0 si ocurre error
     */
    public int contarUsuariosActivos() {
        // SQL: Consulta de conteo para obtener total de usuarios activos
        // Tabla: Usuario
        // WHERE estado = 1: filtra solo registros con flag de activación en true
        // COUNT(*): retorna número de filas que cumplen la condición de estado
        String sql = "SELECT COUNT(*) FROM Usuario WHERE estado = 1";
        // try-with-resources para gestión automática de recursos
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            // Ejecuta consulta de conteo y evalúa resultado
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            // Captura excepción para diagnóstico sin propagar error
            e.printStackTrace();
        }
        // Retorna 0 por defecto si no se pudo obtener resultado o hubo error
        return 0;
    }

    /**
     * Registra una acción en la tabla de auditoría para trazabilidad de operaciones.
     * Método helper privado utilizado por métodos públicos para mantener consistencia
     * en el formato y estructura de registros de auditoría del sistema.
     *
     * Consulta SQL: INSERT en tabla Auditoria
     * Tabla destino: Auditoria (bitácora centralizada de eventos del sistema)
     * Columnas insertadas:
     *   - usuario_id: FK hacia Usuario para trazabilidad de quién ejecutó la acción
     *   - accion: tipo de operación ("CREAR", "EDITAR", "ELIMINAR", etc.)
     *   - entidad: nombre lógico de la entidad afectada ("Usuario", "Producto", etc.)
     *   - entidad_id: ID del registro específico modificado para referencia directa
     *   - datos_anteriores: JSON o texto con estado previo de la entidad (nullable)
     *   - datos_nuevos: JSON o texto con estado posterior de la entidad (nullable)
     *   - fecha: NOW() para timestamp automático del servidor de BD
     * Valores parametrizados (?, ?, ?, ?, ?, ?) para seguridad y reutilización de plan
     *
     * Nota de diseño: esta versión recibe la conexión activa para que el registro
     * quede dentro de la misma transacción que la operación principal, garantizando
     * atomicidad: o se registran ambos cambios (operación + auditoría) o ninguno.
     *
     * @param conn conexión abierta dentro de transacción en curso (para consistencia atómica)
     * @param usuarioId ID del usuario que ejecuta la acción para trazabilidad
     * @param accion texto descriptivo de la operación ("CREAR", "EDITAR", "ELIMINAR", etc.)
     * @param entidad nombre lógico de la entidad afectada ("Usuario", "Producto", etc.)
     * @param entidadId ID del registro específico modificado para referencia directa
     * @param datosAnteriores JSON o texto con estado anterior o {@code null} si no aplica
     * @param datosNuevos JSON o texto con estado posterior o {@code null} si no aplica
     * @throws SQLException si falla el INSERT por errores de conexión, constraints o sintaxis SQL
     */
    private void registrarAuditoria(Connection conn, int usuarioId, String accion, String entidad,
                                    int entidadId, String datosAnteriores, String datosNuevos) throws SQLException {
        // SQL: INSERT para registrar evento de auditoría en tabla centralizada
        // Tabla destino: Auditoria (bitácora de trazabilidad de operaciones del sistema)
        // Columnas: usuario_id (FK), accion, entidad, entidad_id, datos_anteriores, datos_nuevos, fecha
        // Valores parametrizados (?, ?, ?, ?, ?, ?) para seguridad y reutilización de plan
        // NOW(): función de BD para timestamp automático de creación del registro
        String sql = """
            INSERT INTO Auditoria(usuario_id, accion, entidad, entidad_id, datos_anteriores, datos_nuevos, fecha)
            VALUES (?, ?, ?, ?, ?, ?, NOW())
            """;
        // PreparedStatement para ejecutar INSERT con parámetros seguros
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Bind parámetros en orden definido en la consulta SQL:
            // 1=usuario_id, 2=accion, 3=entidad, 4=entidad_id, 5=datos_anteriores, 6=datos_nuevos
            ps.setInt(1, usuarioId);
            ps.setString(2, accion);
            ps.setString(3, entidad);
            ps.setInt(4, entidadId);
            // Parámetros 5 y 6 pueden ser null si no hay datos anteriores/nuevos que registrar
            ps.setString(5, datosAnteriores);
            ps.setString(6, datosNuevos);
            // Ejecuta INSERT para persistir registro de auditoría; se espera 1 fila afectada
            ps.executeUpdate();
        }
    }

    /**
     * Valida si un texto representa un JSON bien formado (objeto o array).
     * Método auxiliar privado utilizado para sanitizar datos antes de persistir
     * en campos de auditoría que almacenan estados en formato JSON.
     *
     * Lógica de validación:
     *   1. Verifica que el texto no sea null ni vacío (casos que retornan false)
     *   2. Intenta parsear como JSONObject: si éxito, retorna true
     *   3. Si falla, intenta parsear como JSONArray: si éxito, retorna true
     *   4. Si ambos intentos fallan, retorna false indicando JSON inválido
     *
     * Manejo de excepciones: captura Exception genérica para cualquier error de parseo
     * del org.json library, permitiendo que el método retorne false en lugar de propagar
     * excepción, útil para validaciones tolerantes en flujos de auditoría.
     *
     * @param texto cadena de texto a validar como JSON bien formado
     * @return {@code true} si el texto es JSON válido (objeto o array),
     *         {@code false} si es null, vacío o no puede parsearse como JSON
     */
    private boolean esJsonValido(String texto) {
        // Validación inicial: null o texto vacío no son JSON válidos
        if (texto == null || texto.trim().isEmpty()) return false;
        try {
            // Primer intento: parsear como JSONObject (estructura { "clave": "valor" })
            new org.json.JSONObject(texto);
            return true;
        } catch (Exception e) {
            try {
                // Segundo intento: parsear como JSONArray (estructura [ "valor1", "valor2" ])
                new org.json.JSONArray(texto);
                return true;
            } catch (Exception e2) {
                // Si ambos intentos fallan, el texto no es JSON válido
                return false;
            }
        }
    }

    /**
     * Obtiene historial de usuarios con su desempeño mediante auditoría asociada.
     * Método utilizado para reportes de actividad, análisis de comportamiento
     * y dashboards de administración que requieren trazabilidad de acciones por usuario.
     *
     * Consulta SQL: SELECT con múltiples LEFT JOINs para consolidar datos de usuario y auditoría
     * Tablas involucradas:
     *   - Usuario (alias 'u'): tabla principal con datos de perfil y estado
     *   - Usuario_Rol (alias 'ur'): LEFT JOIN para vincular usuario con su rol
     *   - Rol (alias 'r'): LEFT JOIN para obtener nombre legible del cargo
     *   - Auditoria (alias 'a'): LEFT JOIN para obtener acciones registradas del usuario
     *
     * Tipo de JOIN: LEFT JOIN en todos los casos para conservar usuarios incluso si
     * no tienen rol asignado o no tienen registros de auditoría aún (campos relacionados
     * pueden ser NULL, lo cual es manejado en la capa de presentación).
     *
     * Columnas seleccionadas:
     *   - Datos de usuario: usuario_id, nombre, estado, fecha_creacion
     *   - Datos de rol: cargo (nombre legible desde tabla Rol)
     *   - Datos de auditoría: accion, entidad, fecha_auditoria, datos_anteriores, datos_nuevos
     *
     * Ordenamiento: ORDER BY a.fecha DESC para mostrar acciones más recientes primero
     * Nota: este ordenamiento puede generar múltiples filas por usuario si tiene
     * múltiples registros de auditoría; la agrupación y presentación final se maneja
     * en la capa de negocio o presentación según requerimientos de UI.
     *
     * Flujo de mapeo:
     *   - Itera sobre ResultSet creando Map<String, Object> por cada fila
     *   - LinkedHashMap mantiene orden de inserción para consistencia en presentación
     *   - Mapea cada columna con clave descriptiva para fácil acceso en UI
     *   - Campos de auditoría (datos_anteriores, datos_nuevos) se mantienen como String
     *     para que la capa superior decida si parsear como JSON o mostrar como texto
     *
     * @return lista de mapas con datos consolidados de usuario y sus acciones de auditoría,
     *         ordenados por fecha de auditoría descendente; lista vacía si no hay datos o hay error
     */
    public List<Map<String, Object>> obtenerHistorialUsuariosConDesempeno() {
        // Lista que almacenará cada fila del resultado como mapa de clave-valor
        List<Map<String, Object>> lista = new ArrayList<>();
        // SQL: SELECT con LEFT JOINs para recuperar usuario con su rol y historial de auditoría
        // Tabla principal: Usuario (u) → datos de perfil y estado
        // LEFT JOIN Usuario_Rol (ur) → vincula usuario con su rol (puede ser NULL)
        // LEFT JOIN Rol (r) → obtiene nombre legible del cargo (puede ser NULL)
        // LEFT JOIN Auditoria (a) → obtiene acciones registradas del usuario (puede ser NULL)
        // ORDER BY a.fecha DESC: presenta acciones más recientes primero para mejor UX en reportes
        String sql = """
            SELECT u.usuario_id, u.nombre, u.estado, u.fecha_creacion, r.cargo,
                   a.accion, a.entidad, a.fecha AS fecha_auditoria,
                   a.datos_anteriores, a.datos_nuevos
            FROM Usuario u
            LEFT JOIN Usuario_Rol ur  ON ur.usuario_id = u.usuario_id
            LEFT JOIN Rol r           ON r.rol_id = ur.rol_id
            LEFT JOIN Auditoria a     ON a.usuario_id = u.usuario_id
            ORDER BY a.fecha DESC
            """;
        // try-with-resources para gestión automática de Connection, PreparedStatement y ResultSet
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            // Itera sobre cada fila del resultado para transformarla a Map<String, Object>
            while (rs.next()) {
                // LinkedHashMap mantiene orden de inserción, útil para preservar orden de columnas en UI
                Map<String, Object> fila = new LinkedHashMap<>();
                // Mapea cada columna del ResultSet a una clave descriptiva en el mapa
                fila.put("usuario_id",       rs.getInt("usuario_id"));
                fila.put("nombre",           rs.getString("nombre"));
                fila.put("estado",           rs.getBoolean("estado"));
                fila.put("fecha_creacion",   rs.getDate("fecha_creacion"));
                fila.put("cargo",            rs.getString("cargo"));
                fila.put("accion",           rs.getString("accion"));
                fila.put("entidad",          rs.getString("entidad"));
                // getTimestamp() para fecha_auditoria permite precisión de hora/minuto/segundo
                fila.put("fecha_auditoria",  rs.getTimestamp("fecha_auditoria"));
                // datos_anteriores y datos_nuevos se mantienen como String (JSON o texto)
                // La capa superior decide si parsear como JSON o mostrar como texto plano
                fila.put("datos_anteriores", rs.getString("datos_anteriores"));
                fila.put("datos_nuevos",     rs.getString("datos_nuevos"));
                // Agrega el mapa poblado a la lista de retorno
                lista.add(fila);
            }
        } catch (Exception e) {
            // Captura cualquier excepción para diagnóstico sin interrumpir flujo del llamador
            // Imprime stack trace en consola para revisión en logs del servidor
            e.printStackTrace();
        }
        // Retorna lista poblada; si no hay resultados o hay error, retorna lista vacía (nunca null)
        // Esto garantiza que la capa de presentación pueda iterar sin validar null
        return lista;
    }
}