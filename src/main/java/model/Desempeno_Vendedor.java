// model/DesempenoVendedor.java
package model;

import java.math.BigDecimal;
import java.util.Date;

public class Desempeno_Vendedor {
    private int desempenoId;
    private int usuarioId;
    private String nombre;
    private BigDecimal ventasTotales;        
    private BigDecimal comisionPorcentaje;   
    private BigDecimal comisionGanada;       
    private Date periodo;
    private String observaciones;

    // Getters y setters
    public int getDesempenoId() { return desempenoId; }
    public void setDesempenoId(int desempenoId) { this.desempenoId = desempenoId; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getNombre() {
        return nombre;
    }
    
    public BigDecimal getVentasTotales() { return ventasTotales; }
    public void setVentasTotales(BigDecimal ventasTotales) { this.ventasTotales = ventasTotales; }

    public BigDecimal getComisionPorcentaje() { return comisionPorcentaje; }
    public void setComisionPorcentaje(BigDecimal comisionPorcentaje) { this.comisionPorcentaje = comisionPorcentaje; }

    public BigDecimal getComisionGanada() { return comisionGanada; }
    public void setComisionGanada(BigDecimal comisionGanada) { this.comisionGanada = comisionGanada; }

    public Date getPeriodo() { return periodo; }
    public void setPeriodo(Date periodo) { this.periodo = periodo; }
    
    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
}