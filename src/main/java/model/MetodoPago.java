package model;

/**
 * Representa una forma de pago disponible en el sistema.
 * Ejemplos: Efectivo, Tarjeta débito, Transferencia bancaria.
 * Se gestiona desde el tab "Métodos de pago" en org-categorias.jsp.
 */
public class MetodoPago {
    private Integer metodoPagoId;
    private String nombre;

    public MetodoPago() {}

    // Constructor de conveniencia: permite construir el objeto ya inicializado.
    public MetodoPago(Integer metodoPagoId, String nombre) {
        this.metodoPagoId = metodoPagoId;
        this.nombre = nombre;
    }

    // Se usa Integer (objeto) en lugar de int (primitivo) para detectar null
    // antes de que el registro sea persistido en la base de datos.
    public Integer getMetodoPagoId() { return metodoPagoId; }
    public void setMetodoPagoId(Integer metodoPagoId) { this.metodoPagoId = metodoPagoId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
