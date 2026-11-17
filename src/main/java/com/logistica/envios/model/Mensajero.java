package com.logistica.envios.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mensajero")
public class Mensajero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMensajero;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String documento;

    /** Ciudad en la que opera el mensajero; RN-07 la compara con la ciudad de la recolección. */
    @Column(nullable = false)
    private String zona;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoMensajero estado = EstadoMensajero.DISPONIBLE;

    public Long getIdMensajero() { return idMensajero; }
    public void setIdMensajero(Long idMensajero) { this.idMensajero = idMensajero; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
    public EstadoMensajero getEstado() { return estado; }
    public void setEstado(EstadoMensajero estado) { this.estado = estado; }
}
