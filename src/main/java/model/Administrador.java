package model;

/**
 * Clase modelo Administrador
 * Representa la entidad Administrador dentro del sistema AAC27
 * Contiene la información básica necesaria para la autenticación y gestión del administrador
 */
public class Administrador {

    // Identificador único del administrador
    private int id;

    // Nombre de usuario del administrador
    private String nombre;

    // Contraseña del administrador
    private String pass;

    //Constructor vacío necesario para JSP y mapeos automáticos
   
    public Administrador() {
    }

    /**Constructor con todos los atributos
     * 
     * Permite crear un administrador completamente inicializado
     *
     * @param id     Identificador del administrador
     * @param nombre Nombre del administrador
     * @param pass   Contraseña del administrador
     */
    public Administrador(int id, String nombre, String pass) {
        this.id = id;
        this.nombre = nombre;
        this.pass = pass;
    }

    /* ===============================
       GETTERS
       =============================== */

    // Retorna el id del administrador
    public int getId() {
        return id;
    }

    // Retorna el nombre del administrador
    public String getNombre() {
        return nombre;
    }

    // Retorna la contraseña del administrador
    public String getPass() {
        return pass;
    }

    /* ===============================
       SETTERS
       =============================== */

    // Asigna el id del administrador
    public void setId(int id) {
        this.id = id;
    }

    // Asigna el nombre del administrador
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Asigna la contraseña del administrador
    public void setPass(String pass) {
        this.pass = pass;
    }
}
