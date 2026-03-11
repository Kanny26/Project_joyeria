package model;

public class MetodoPago {
    private Integer metodoPagoId;
    private String nombre;
    
    public MetodoPago() {}
    
    public MetodoPago(Integer metodoPagoId, String nombre) {
        this.metodoPagoId = metodoPagoId;
        this.nombre = nombre;
    }
    
    public Integer getMetodoPagoId() { return metodoPagoId; }
    public void setMetodoPagoId(Integer metodoPagoId) { this.metodoPagoId = metodoPagoId; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}