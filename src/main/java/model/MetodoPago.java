package model;

public class MetodoPago {

    private int    metodoPagoId;
    private String nombre;

    public MetodoPago() {}

    public int getMetodoPagoId()                  { return metodoPagoId; }
    public void setMetodoPagoId(int metodoPagoId) { this.metodoPagoId = metodoPagoId; }

    public String getNombre()               { return nombre; }
    public void setNombre(String nombre)    { this.nombre = nombre; }
}