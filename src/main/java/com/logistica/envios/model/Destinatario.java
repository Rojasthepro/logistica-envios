package com.logistica.envios.model;

import jakarta.persistence.*;

@Entity
@Table(name = "destinatario")
public class Destinatario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDestinatario;

    @Column(nullable = false)
    private String nombre;

    private String telefono;

    private String direccion;

    private String ciudad;

    // ZonaCobertura 1 — 0..* Destinatario
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_zona", nullable = false)
    private ZonaCobertura zona;

    public Long getIdDestinatario() { return idDestinatario; }
    public void setIdDestinatario(Long idDestinatario) { this.idDestinatario = idDestinatario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public ZonaCobertura getZona() { return zona; }
    public void setZona(ZonaCobertura zona) { this.zona = zona; }
}
