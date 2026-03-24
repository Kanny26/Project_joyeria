package model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Clase modelo Desempeno_Vendedor
 * Representa el desempeño de un vendedor en un período determinado
 * Incluye información de ventas, comisión y observaciones administrativas
 */
public class Desempeno_Vendedor {
    
    // Identificador único del registro de desempeño
    private int desempenoId;
    
    // Identificador del usuario/vendedor asociado
    private int usuarioId;
    
    // Nombre del vendedor (para vistas, cargado via JOIN)
    private String nombre;
    
    // Total de ventas realizadas en el período
    private BigDecimal ventasTotales;
    
    // Porcentaje de comisión asignado al vendedor
    private BigDecimal comisionPorcentaje;
    
    // Comisión ganada calculada según ventas y porcentaje
    private BigDecimal comisionGanada;
    
    // Período evaluado (fecha o rango representativo)
    private Date periodo;
    
    // Observaciones adicionales sobre el desempeño
    private String observaciones;

    /**
     * Constructor vacío
     * Requerido para instanciación desde DAOs y JSP
     */
    public Desempeno_Vendedor() { }

    /*=============================== GETTERS ===============================*/
    
    public int getDesempenoId() { return desempenoId; }
    public int getUsuarioId() { return usuarioId; }
    public String getNombre() { return nombre; }
    public BigDecimal getVentasTotales() { return ventasTotales; }
    public BigDecimal getComisionPorcentaje() { return comisionPorcentaje; }
    public BigDecimal getComisionGanada() { return comisionGanada; }
    public Date getPeriodo() { return periodo; }
    public String getObservaciones() { return observaciones; }

    /*=============================== SETTERS ===============================*/
    
    public void setDesempenoId(int desempenoId) { this.desempenoId = desempenoId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setVentasTotales(BigDecimal ventasTotales) { this.ventasTotales = ventasTotales; }
    public void setComisionPorcentaje(BigDecimal comisionPorcentaje) { this.comisionPorcentaje = comisionPorcentaje; }
    public void setComisionGanada(BigDecimal comisionGanada) { this.comisionGanada = comisionGanada; }
    public void setPeriodo(Date periodo) { this.periodo = periodo; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    /*=============================== MÉTODOS AUXILIARES ===============================*/
    
    /**
     * RF36: Calcula automáticamente la comisión ganada
     * basado en ventas_totales × comision_porcentaje / 100
     */
    public void calcularComisionGanada() {
        if (this.ventasTotales != null && this.comisionPorcentaje != null) {
            this.comisionGanada = this.ventasTotales
                .multiply(this.comisionPorcentaje)
                .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
    
    /**
     * RF36: Valida que la comisión no exceda el 20%
     * según política de la empresa
     */
    public boolean validarComision() {
        return this.comisionPorcentaje != null && 
               this.comisionPorcentaje.compareTo(new BigDecimal("20.00")) <= 0;
    }
}