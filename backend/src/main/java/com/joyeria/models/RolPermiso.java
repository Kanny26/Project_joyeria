package main.model;

/**
 * Clase modelo para la tabla Rol_Permiso
 * Representa la relación muchos a muchos entre Rol y Permiso
 */
public class RolPermiso {
    
    // Atributos (clave compuesta)
    private int rolId;
    private int permisoId;
    
    // Objetos relacionados (opcional para joins)
    private Rol rol;
    private Permiso permiso;
    
    // Constructor vacío
    public RolPermiso() {
    }
    
    // Constructor con IDs
    public RolPermiso(int rolId, int permisoId) {
        this.rolId = rolId;
        this.permisoId = permisoId;
    }
    
    // Constructor con objetos completos
    public RolPermiso(int rolId, int permisoId, Rol rol, Permiso permiso) {
        this.rolId = rolId;
        this.permisoId = permisoId;
        this.rol = rol;
        this.permiso = permiso;
    }
    
    // Getters y Setters
    public int getRolId() {
        return rolId;
    }
    
    public void setRolId(int rolId) {
        this.rolId = rolId;
    }
    
    public int getPermisoId() {
        return permisoId;
    }
    
    public void setPermisoId(int permisoId) {
        this.permisoId = permisoId;
    }
    
    public Rol getRol() {
        return rol;
    }
    
    public void setRol(Rol rol) {
        this.rol = rol;
    }
    
    public Permiso getPermiso() {
        return permiso;
    }
    
    public void setPermiso(Permiso permiso) {
        this.permiso = permiso;
    }
    
    // Método toString
    @Override
    public String toString() {
        return "RolPermiso{" +
                "rolId=" + rolId +
                ", permisoId=" + permisoId +
                '}';
    }
}