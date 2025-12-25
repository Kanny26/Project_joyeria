package joyeria.models;

import java.time.LocalDateTime;

public class Usuario {

    private String nombre;
    private String pass;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    // Constructor
    public Usuario(String nombre, String pass) {
        this.nombre = nombre;
        this.pass = pass;
        this.estado = true;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getPass() {
        return pass;
    }

    public boolean isEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
