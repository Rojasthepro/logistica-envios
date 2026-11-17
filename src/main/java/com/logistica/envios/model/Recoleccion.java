package com.logistica.envios.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recoleccion")
public class Recoleccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRecoleccion;

    @Column(nullable = false, unique = true, length = 20)
    private String numeroGuia;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private String direccionRecoleccion;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal costoTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoRecoleccion estado = EstadoRecoleccion.PROGRAMADA;

    // Remitente 1 — 0..* Recoleccion
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_remitente", nullable = false)
    private Remitente remitente;

    // Mensajero 1 — 0..* Recoleccion
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_mensajero", nullable = false)
    private Mensajero mensajero;

    // Recoleccion 1 — 1..* Paquete (el lado dueño de la FK es Paquete.recoleccion)
    @OneToMany(mappedBy = "recoleccion")
    @OrderBy("idPaquete")
    private List<Paquete> paquetes = new ArrayList<>();

    @Column(nullable = false)
    private String usuarioOperador;

    @Column(nullable = false)
    private LocalDateTime selloTiempo;

    public Long getIdRecoleccion() { return idRecoleccion; }
    public void setIdRecoleccion(Long idRecoleccion) { this.idRecoleccion = idRecoleccion; }
    public String getNumeroGuia() { return numeroGuia; }
    public void setNumeroGuia(String numeroGuia) { this.numeroGuia = numeroGuia; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getDireccionRecoleccion() { return direccionRecoleccion; }
    public void setDireccionRecoleccion(String direccionRecoleccion) { this.direccionRecoleccion = direccionRecoleccion; }
    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }
    public EstadoRecoleccion getEstado() { return estado; }
    public void setEstado(EstadoRecoleccion estado) { this.estado = estado; }
    public Remitente getRemitente() { return remitente; }
    public void setRemitente(Remitente remitente) { this.remitente = remitente; }
    public Mensajero getMensajero() { return mensajero; }
    public void setMensajero(Mensajero mensajero) { this.mensajero = mensajero; }
    public List<Paquete> getPaquetes() { return paquetes; }
    public void setPaquetes(List<Paquete> paquetes) { this.paquetes = paquetes; }
    public String getUsuarioOperador() { return usuarioOperador; }
    public void setUsuarioOperador(String usuarioOperador) { this.usuarioOperador = usuarioOperador; }
    public LocalDateTime getSelloTiempo() { return selloTiempo; }
    public void setSelloTiempo(LocalDateTime selloTiempo) { this.selloTiempo = selloTiempo; }
}
