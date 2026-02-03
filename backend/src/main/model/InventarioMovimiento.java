package main.model;

import java.time.LocalDateTime;

/**
 * Clase modelo para la tabla Inventario_Movimiento
 * Registra los movimientos de inventario (entrada, salida, ajuste)
 */
public class InventarioMovimiento {
    
    // Enum para tipos de movimiento
    public enum TipoMovimiento {
        ENTRADA("entrada"),
        SALIDA("salida"),
        AJUSTE("ajuste");
        
        private final String valor;
        
        TipoMovimiento(String valor) {
            this.valor = valor;
        }
        
        public String getValor() {
            return valor;
        }
        
        public static TipoMovimiento fromString(String texto) {
            for (TipoMovimiento tipo : TipoMovimiento.values()) {
                if (tipo.valor.equalsIgnoreCase(texto)) {
                    return tipo;
                }
            }
            throw new IllegalArgumentException("Tipo de movimiento no válido: " + texto);
        }
    }
    
    // Enum para estado del movimiento
    public enum EstadoMovimiento {
        ACTIVO("activo"),
        INACTIVO("inactivo");
        
        private final String valor;
        
        EstadoMovimiento(String valor) {
            this.valor = valor;
        }
        
        public String getValor() {
            return valor;
        }
        
        public static EstadoMovimiento fromString(String texto) {
            for (EstadoMovimiento estado : EstadoMovimiento.values()) {
                if (estado.valor.equalsIgnoreCase(texto)) {
                    return estado;
                }
            }
            throw new IllegalArgumentException("Estado no válido: " + texto);
        }
    }
    
    // Atributos
    private int movimientoId;
    private int productoId;
    private TipoMovimiento tipo;
    private EstadoMovimiento estado;
    private int cantidad;
    private LocalDateTime fecha;
    private String referencia; // Puede ser ID de venta o compra
    
    // Objeto relacionado (para joins)
    private Producto producto;
    
    // Constructor vacío
    public InventarioMovimiento() {
        this.fecha = LocalDateTime.now();
        this.estado = EstadoMovimiento.ACTIVO;
    }
    
    // Constructor con campos principales
    public InventarioMovimiento(int productoId, TipoMovimiento tipo, int cantidad, String referencia) {
        this();
        this.productoId = productoId;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.referencia = referencia;
    }
    
    // Constructor completo
    public InventarioMovimiento(int movimientoId, int productoId, TipoMovimiento tipo,
                               EstadoMovimiento estado, int cantidad, LocalDateTime fecha, String referencia) {
        this.movimientoId = movimientoId;
        this.productoId = productoId;
        this.tipo = tipo;
        this.estado = estado;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.referencia = referencia;
    }
    
    // Getters y Setters
    public int getMovimientoId() {
        return movimientoId;
    }
    
    public void setMovimientoId(int movimientoId) {
        this.movimientoId = movimientoId;
    }
    
    public int getProductoId() {
        return productoId;
    }
    
    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }
    
    public TipoMovimiento getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
    }
    
    public EstadoMovimiento getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoMovimiento estado) {
        this.estado = estado;
    }
    
    public int getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
    
    public LocalDateTime getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    
    public String getReferencia() {
        return referencia;
    }
    
    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
    
    public Producto getProducto() {
        return producto;
    }
    
    public void setProducto(Producto producto) {
        this.producto = producto;
    }
    
    // Método toString
    @Override
    public String toString() {
        return "InventarioMovimiento{" +
                "movimientoId=" + movimientoId +
                ", productoId=" + productoId +
                ", tipo=" + tipo +
                ", cantidad=" + cantidad +
                ", fecha=" + fecha +
                ", referencia='" + referencia + '\'' +
                '}';
    }
}