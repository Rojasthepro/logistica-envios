package com.logistica.envios.dto;

import com.logistica.envios.model.Remitente;

public record RemitenteResumen(Long idRemitente, String nombre, String documento, String telefono,
                               String direccion, String ciudad, String estado) {

    public static RemitenteResumen de(Remitente r) {
        return new RemitenteResumen(r.getIdRemitente(), r.getNombre(), r.getDocumento(), r.getTelefono(),
                r.getDireccion(), r.getCiudad(), r.getEstado().name());
    }
}
