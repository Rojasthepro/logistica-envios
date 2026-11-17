package com.logistica.envios.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "paquete")
public class Paquete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPaquete;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoKg;

    @Column(nullable = false)
    private Integer altoCm;

    @Column(nullable = false)
    private Integer anchoCm;

    @Column(nullable = false)
    private Integer largoCm;

    @Column(precision = 14, scale = 2)
    private BigDecimal valorDeclarado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoPaquete estado = EstadoPaquete.REGISTRADO;

    // Destinatario 1 — 0..* Paquete
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_destinatario", nullable = false)
    private Destinatario destinatario;

    // Recoleccion 1 — 1..* Paquete; es null mientras el paquete no ha sido recogido
    @ManyToOne
    @JoinColumn(name = "id_recoleccion")
    private Recoleccion recoleccion;

    // Asociación necesaria para listar los paquetes pendientes de cada remitente
    // (Remitente 1 — 0..* Paquete). Ver nota en el README.
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_remitente", nullable = false)
    private Remitente remitente;

    /** Código legible del paquete que se muestra en pantalla y en los mensajes, ej. PKG-7. */
    public String getCodigo() {
        return "PKG-" + idPaquete;
    }

    /** Suma alto + ancho + largo, usada por RN-01. */
    public int getSumaDimensionesCm() {
        return altoCm + anchoCm + largoCm;
    }

    public Long getIdPaquete() { return idPaquete; }
    public void setIdPaquete(Long idPaquete) { this.idPaquete = idPaquete; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPesoKg() { return pesoKg; }
    public void setPesoKg(BigDecimal pesoKg) { this.pesoKg = pesoKg; }
    public Integer getAltoCm() { return altoCm; }
    public void setAltoCm(Integer altoCm) { this.altoCm = altoCm; }
    public Integer getAnchoCm() { return anchoCm; }
    public void setAnchoCm(Integer anchoCm) { this.anchoCm = anchoCm; }
    public Integer getLargoCm() { return largoCm; }
    public void setLargoCm(Integer largoCm) { this.largoCm = largoCm; }
    public BigDecimal getValorDeclarado() { return valorDeclarado; }
    public void setValorDeclarado(BigDecimal valorDeclarado) { this.valorDeclarado = valorDeclarado; }
    public EstadoPaquete getEstado() { return estado; }
    public void setEstado(EstadoPaquete estado) { this.estado = estado; }
    public Destinatario getDestinatario() { return destinatario; }
    public void setDestinatario(Destinatario destinatario) { this.destinatario = destinatario; }
    public Recoleccion getRecoleccion() { return recoleccion; }
    public void setRecoleccion(Recoleccion recoleccion) { this.recoleccion = recoleccion; }
    public Remitente getRemitente() { return remitente; }
    public void setRemitente(Remitente remitente) { this.remitente = remitente; }
}
