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

    // Nombre del vendedor
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
    public Desempeno_Vendedor() {
    }

    /* ===============================
       GETTERS
       =============================== */

    // Retorna el id del desempeño
    public int getDesempenoId() {
        return desempenoId;
    }

    // Retorna el id del usuario/vendedor
    public int getUsuarioId() {
        return usuarioId;
    }

    // Retorna el nombre del vendedor
    public String getNombre() {
        return nombre;
    }

    // Retorna el total de ventas del período
    public BigDecimal getVentasTotales() {
        return ventasTotales;
    }

    // Retorna el porcentaje de comisión
    public BigDecimal getComisionPorcentaje() {
        return comisionPorcentaje;
    }

    // Retorna la comisión ganada
    public BigDecimal getComisionGanada() {
        return comisionGanada;
    }

    // Retorna el período evaluado
    public Date getPeriodo() {
        return periodo;
    }

    // Retorna las observaciones del desempeño
    public String getObservaciones() {
        return observaciones;
    }

    /* ===============================
       SETTERS
       =============================== */

    // Asigna el id del desempeño
    public void setDesempenoId(int desempenoId) {
        this.desempenoId = desempenoId;
    }

    // Asigna el id del usuario/vendedor
    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    // Asigna el nombre del vendedor
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Asigna el total de ventas
    public void setVentasTotales(BigDecimal ventasTotales) {
        this.ventasTotales = ventasTotales;
    }

    // Asigna el porcentaje de comisión
    public void setComisionPorcentaje(BigDecimal comisionPorcentaje) {
        this.comisionPorcentaje = comisionPorcentaje;
    }

    // Asigna la comisión ganada
    public void setComisionGanada(BigDecimal comisionGanada) {
        this.comisionGanada = comisionGanada;
    }

    // Asigna el período evaluado
    public void setPeriodo(Date periodo) {
        this.periodo = periodo;
    }

    // Asigna observaciones adicionales
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
