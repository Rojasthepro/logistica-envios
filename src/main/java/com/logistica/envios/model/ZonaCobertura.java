package com.logistica.envios.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "zona_cobertura")
public class ZonaCobertura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idZona;

    @Column(nullable = false)
    private String nombre;

    private boolean cubierta;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tarifaBase;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorPorKg;

    public Long getIdZona() { return idZona; }
    public void setIdZona(Long idZona) { this.idZona = idZona; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public boolean isCubierta() { return cubierta; }
    public void setCubierta(boolean cubierta) { this.cubierta = cubierta; }
    public BigDecimal getTarifaBase() { return tarifaBase; }
    public void setTarifaBase(BigDecimal tarifaBase) { this.tarifaBase = tarifaBase; }
    public BigDecimal getValorPorKg() { return valorPorKg; }
    public void setValorPorKg(BigDecimal valorPorKg) { this.valorPorKg = valorPorKg; }
}
