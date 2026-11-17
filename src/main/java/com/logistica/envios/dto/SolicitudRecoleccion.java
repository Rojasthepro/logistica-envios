package com.logistica.envios.dto;

import java.util.ArrayList;
import java.util.List;

/** Datos que digita el operador en la pantalla de nueva recolección. */
public class SolicitudRecoleccion {

    private Long remitenteId;
    private List<Long> paqueteIds = new ArrayList<>();
    private Long mensajeroId;
    private String direccionRecoleccion;

    public Long getRemitenteId() { return remitenteId; }
    public void setRemitenteId(Long remitenteId) { this.remitenteId = remitenteId; }
    public List<Long> getPaqueteIds() { return paqueteIds; }
    public void setPaqueteIds(List<Long> paqueteIds) { this.paqueteIds = paqueteIds; }
    public Long getMensajeroId() { return mensajeroId; }
    public void setMensajeroId(Long mensajeroId) { this.mensajeroId = mensajeroId; }
    public String getDireccionRecoleccion() { return direccionRecoleccion; }
    public void setDireccionRecoleccion(String direccionRecoleccion) { this.direccionRecoleccion = direccionRecoleccion; }
}
