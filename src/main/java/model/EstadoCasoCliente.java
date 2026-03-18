package model;

import java.util.Date;

/**
 * Representa un registro en el historial de cambios de estado de un caso postventa.
 * Cada vez que el administrador cambia el estado de un caso (por ejemplo,
 * de "en_proceso" a "aprobado"), se guarda una entrada en la tabla
 * Estado_Caso_Cliente con la fecha, el estado nuevo y una observación.
 */
public class EstadoCasoCliente {

    private int estadoId;
    private int casoId;

    // Valores posibles: "en_proceso", "aprobado", "cancelado"
    private String estado;

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
