package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo Cliente — mapea la tabla Cliente del SQL.
 * Tabla: Cliente (cliente_id, nombre, documento, fecha_registro, minimo_compra, estado)
 */
public class Cliente {
    private int clienteId;
    private String nombre;
    private String documento;
    private boolean estado;
    private String fechaRegistro;

    // Relaciones (no en tabla principal)
    private String telefonos;   // GROUP_CONCAT de Telefono_Cliente
    private String correos;     // GROUP_CONCAT de Correo_Cliente

    // Para compatibilidad con VentaVendedorServlet que pasa telefono/email sueltos
    private String telefono;
    private String email;

    public Cliente() {}

    public Cliente(String nombre, String telefono, String email) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.estado = true;
    }

    // Getters y Setters
    public int getClienteId()                       { return clienteId; }
    public void setClienteId(int clienteId)         { this.clienteId = clienteId; }

    public String getNombre()                       { return nombre; }
    public void setNombre(String nombre)            { this.nombre = nombre; }

    public String getDocumento()                    { return documento; }
    public void setDocumento(String documento)      { this.documento = documento; }

    public boolean isEstado()                       { return estado; }
    public void setEstado(boolean estado)           { this.estado = estado; }

    public String getFechaRegistro()                { return fechaRegistro; }
    public void setFechaRegistro(String f)          { this.fechaRegistro = f; }

    public String getTelefonos()                    { return telefonos; }
    public void setTelefonos(String telefonos)      { this.telefonos = telefonos; }

    public String getCorreos()                      { return correos; }
    public void setCorreos(String correos)          { this.correos = correos; }

    // Compat: campo simple para insert
    public String getTelefono()                     { return telefono; }
    public void setTelefono(String telefono)        { this.telefono = telefono; }

    public String getEmail()                        { return email; }
    public void setEmail(String email)              { this.email = email; }

    // Campo de compatibilidad con código viejo que usaba isActivo()
    public boolean isActivo()                       { return estado; }
    public void setActivo(boolean activo)           { this.estado = activo; }
}