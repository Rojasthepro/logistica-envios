package com.logistica.envios.dto;

import java.math.BigDecimal;

/** Paquete pendiente tal como lo consume la pantalla; costoEstimado ya viene calculado con RN-06. */
public record PaqueteResumen(Long idPaquete, String codigo, String descripcion, BigDecimal pesoKg,
                             Integer altoCm, Integer anchoCm, Integer largoCm, int sumaDimensionesCm,
                             BigDecimal pesoFacturable, String destinatario, String ciudadDestino,
                             String zona, boolean zonaCubierta, BigDecimal costoEstimado) {
}
