package main.model;

/**
 * Clase modelo para la tabla Rol
 * Define los roles de usuario en el sistema
 */
public class Rol {
    
    // Enum para los tipos de cargo
    public enum TipoCargo {
        VENDEDOR("vendedor"),
        ADMINISTRADOR("administrador"),
        PROVEEDOR("proveedor"),
        CLIENTE("cliente");
        
        private final String valor;
        
        TipoCargo(String valor) {
            this.valor = valor;
        }
        
        public String getValor() {
            return valor;
        }
        
        public static TipoCargo fromString(String texto) {
            for (TipoCargo tipo : TipoCargo.values()) {
                if (tipo.valor.equalsIgnoreCase(texto)) {
                    return tipo;
                }
            }
            throw new IllegalArgumentException("Cargo no válido: " + texto);
        }
    }
    
    // Atributos
    private int rolId;
    private TipoCargo cargo;
    private int usuarioId;
    private String nombre;
    
    // Constructor vacío
    public Rol() {
    }
    
    // Constructor con campos principales
    public Rol(TipoCargo cargo, int usuarioId, String nombre) {
        this.cargo = cargo;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
    }
    
    // Constructor completo
    public Rol(int rolId, TipoCargo cargo, int usuarioId, String nombre) {
        this.rolId = rolId;
        this.cargo = cargo;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
    }
    
    // Getters y Setters
    public int getRolId() {
        return rolId;
    }
    
    public void setRolId(int rolId) {
        this.rolId = rolId;
    }
    
    public TipoCargo getCargo() {
        return cargo;
    }
    
    public void setCargo(TipoCargo cargo) {
        this.cargo = cargo;
    }
    
    public int getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    // Método toString
    @Override
    public String toString() {
        return "Rol{" +
                "rolId=" + rolId +
                ", cargo=" + cargo +
                ", usuarioId=" + usuarioId +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}