package com.logistica.envios.exception;

/**
 * Violación de una regla de negocio. El mensaje siempre sale con el código de la regla,
 * por ejemplo "RN-01: el paquete PKG-7 pesa 35 kg y el máximo es 30 kg".
 */
public class NegocioException extends RuntimeException {

    private final String codigoRegla;
    private final String detalle;

    public NegocioException(String codigoRegla, String detalle) {
        super(codigoRegla + ": " + detalle);
        this.codigoRegla = codigoRegla;
        this.detalle = detalle;
    }

    public String getCodigoRegla() {
        return codigoRegla;
    }

    /** El mensaje sin el código, para validaciones que no son reglas RN (ej. documento duplicado). */
    public String getDetalle() {
        return detalle;
    }
}
