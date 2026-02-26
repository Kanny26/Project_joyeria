package model;

import java.util.Date;

/**
 * Historial de cambios de estado de un caso postventa.
 * Mapea a Estado_Caso_Cliente en BD.
 */
public class EstadoCasoCliente {

    private int estadoId;
    private int casoId;
    private String estado;       // en_proceso | aprobado | cancelado
    private Date fecha;
    private String observacion;

    public EstadoCasoCliente() {}

    public int getEstadoId()                    { return estadoId; }
    public void setEstadoId(int estadoId)       { this.estadoId = estadoId; }

    public int getCasoId()                      { return casoId; }
    public void setCasoId(int casoId)           { this.casoId = casoId; }

    public String getEstado()                   { return estado; }
    public void setEstado(String estado)        { this.estado = estado; }

    public Date getFecha()                      { return fecha; }
    public void setFecha(Date fecha)            { this.fecha = fecha; }

    public String getObservacion()              { return observacion; }
    public void setObservacion(String obs)      { this.observacion = obs; }
}
