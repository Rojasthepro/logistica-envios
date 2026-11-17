package com.logistica.envios.service;

import com.logistica.envios.dto.PaqueteResumen;
import com.logistica.envios.dto.SolicitudRecoleccion;
import com.logistica.envios.exception.NegocioException;
import com.logistica.envios.model.*;
import com.logistica.envios.repository.MensajeroRepository;
import com.logistica.envios.repository.PaqueteRepository;
import com.logistica.envios.repository.RecoleccionRepository;
import com.logistica.envios.repository.RemitenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Transacción "Registrar recolección". Cada regla de negocio RN-01..RN-08 tiene su propio método.
 */
@Service
public class RecoleccionService {

    static final BigDecimal PESO_MAXIMO_KG = new BigDecimal("30");
    static final int SUMA_DIMENSIONES_MAXIMA_CM = 150;
    static final BigDecimal FACTOR_VOLUMETRICO = new BigDecimal("5000");

    private final RecoleccionRepository recoleccionRepository;
    private final RemitenteRepository remitenteRepository;
    private final PaqueteRepository paqueteRepository;
    private final MensajeroRepository mensajeroRepository;

    public RecoleccionService(RecoleccionRepository recoleccionRepository, RemitenteRepository remitenteRepository,
                              PaqueteRepository paqueteRepository, MensajeroRepository mensajeroRepository) {
        this.recoleccionRepository = recoleccionRepository;
        this.remitenteRepository = remitenteRepository;
        this.paqueteRepository = paqueteRepository;
        this.mensajeroRepository = mensajeroRepository;
    }

    /**
     * Valida las 8 reglas y, si todas se cumplen, crea la recolección REALIZADA, pasa los paquetes
     * a RECOGIDO y el mensajero a EN_RUTA. Si alguna regla falla no se guarda nada.
     */
    @Transactional
    public Recoleccion confirmarRecoleccion(SolicitudRecoleccion solicitud, String usuarioOperador) {
        Remitente remitente = solicitud.getRemitenteId() == null ? null
                : remitenteRepository.findById(solicitud.getRemitenteId()).orElse(null);
        validarRemitenteHabilitado(remitente);                                   // RN-03

        List<Paquete> paquetes = cargarPaquetes(solicitud.getPaqueteIds());
        validarAlMenosUnPaquete(paquetes);                                       // RN-04
        validarPaquetesPendientes(paquetes, remitente);                          // RN-05
        validarPesoYMedidas(paquetes);                                           // RN-01
        validarCoberturaDestinos(paquetes);                                      // RN-02

        Mensajero mensajero = solicitud.getMensajeroId() == null ? null
                : mensajeroRepository.findById(solicitud.getMensajeroId()).orElse(null);
        validarMensajeroAsignable(mensajero, solicitud.getDireccionRecoleccion()); // RN-07

        Recoleccion recoleccion = new Recoleccion();
        recoleccion.setRemitente(remitente);
        recoleccion.setMensajero(mensajero);
        recoleccion.setDireccionRecoleccion(solicitud.getDireccionRecoleccion().trim());
        recoleccion.setFechaHora(LocalDateTime.now());
        recoleccion.setCostoTotal(calcularCostoTotal(paquetes));                 // RN-06
        registrarGuiaYTrazabilidad(recoleccion, usuarioOperador);                // RN-08
        recoleccion.setEstado(EstadoRecoleccion.REALIZADA);
        recoleccionRepository.save(recoleccion);

        for (Paquete paquete : paquetes) {
            paquete.setEstado(EstadoPaquete.RECOGIDO);
            paquete.setRecoleccion(recoleccion);
        }
        recoleccion.setPaquetes(paquetes);
        mensajero.setEstado(EstadoMensajero.EN_RUTA);
        return recoleccion;
    }

    // ------------------------------------------------------------------ reglas

    /** RN-01: ningún paquete supera 30 kg ni 150 cm sumando alto + ancho + largo. */
    public void validarPesoYMedidas(List<Paquete> paquetes) {
        for (Paquete p : paquetes) {
            if (p.getPesoKg().compareTo(PESO_MAXIMO_KG) > 0) {
                throw new NegocioException("RN-01", "el paquete " + p.getCodigo() + " pesa "
                        + numero(p.getPesoKg()) + " kg y el máximo es " + numero(PESO_MAXIMO_KG) + " kg");
            }
            if (p.getSumaDimensionesCm() > SUMA_DIMENSIONES_MAXIMA_CM) {
                throw new NegocioException("RN-01", "el paquete " + p.getCodigo() + " mide "
                        + p.getSumaDimensionesCm() + " cm sumando alto + ancho + largo y el máximo es "
                        + SUMA_DIMENSIONES_MAXIMA_CM + " cm");
            }
        }
    }

    /** RN-02: la zona del destinatario de cada paquete debe estar cubierta. */
    public void validarCoberturaDestinos(List<Paquete> paquetes) {
        for (Paquete p : paquetes) {
            ZonaCobertura zona = p.getDestinatario().getZona();
            if (!zona.isCubierta()) {
                throw new NegocioException("RN-02", "el paquete " + p.getCodigo() + " va para "
                        + p.getDestinatario().getNombre() + " en la zona " + zona.getNombre()
                        + ", que no tiene cobertura");
            }
        }
    }

    /** RN-03: el remitente debe estar ACTIVO y tener documento, teléfono y dirección. */
    public void validarRemitenteHabilitado(Remitente remitente) {
        if (remitente == null) {
            throw new NegocioException("RN-03", "debe seleccionar un remitente");
        }
        if (remitente.getEstado() != EstadoRemitente.ACTIVO) {
            throw new NegocioException("RN-03", "el remitente " + remitente.getNombre()
                    + " está " + remitente.getEstado() + " y solo se recoge a remitentes ACTIVO");
        }
        List<String> faltantes = new ArrayList<>();
        if (vacio(remitente.getDocumento())) faltantes.add("documento");
        if (vacio(remitente.getTelefono())) faltantes.add("teléfono");
        if (vacio(remitente.getDireccion())) faltantes.add("dirección");
        if (!faltantes.isEmpty()) {
            throw new NegocioException("RN-03", "al remitente " + remitente.getNombre()
                    + " le falta: " + String.join(", ", faltantes));
        }
    }

    /** RN-04: la recolección debe llevar al menos un paquete. */
    public void validarAlMenosUnPaquete(List<Paquete> paquetes) {
        if (paquetes == null || paquetes.isEmpty()) {
            throw new NegocioException("RN-04", "debe seleccionar al menos un paquete");
        }
    }

    /**
     * RN-05: cada paquete debe estar REGISTRADO y sin recolección asignada.
     * Un paquete de otro remitente tampoco es elegible para esta recolección.
     */
    public void validarPaquetesPendientes(List<Paquete> paquetes, Remitente remitente) {
        for (Paquete p : paquetes) {
            if (p.getEstado() != EstadoPaquete.REGISTRADO || p.getRecoleccion() != null) {
                throw new NegocioException("RN-05", "el paquete " + p.getCodigo() + " está en estado "
                        + p.getEstado() + (p.getRecoleccion() != null
                        ? " y ya pertenece a la guía " + p.getRecoleccion().getNumeroGuia() : "")
                        + "; solo se recogen paquetes REGISTRADO sin recolección");
            }
            if (!p.getRemitente().getIdRemitente().equals(remitente.getIdRemitente())) {
                throw new NegocioException("RN-05", "el paquete " + p.getCodigo()
                        + " no pertenece al remitente " + remitente.getNombre());
            }
        }
    }

    /** RN-06: costoTotal = Σ (tarifaBase de la zona + pesoFacturable × valorPorKg). */
    public BigDecimal calcularCostoTotal(List<Paquete> paquetes) {
        return paquetes.stream()
                .map(this::calcularCostoPaquete)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** RN-06: costo de un paquete = tarifaBase + pesoFacturable × valorPorKg. */
    public BigDecimal calcularCostoPaquete(Paquete paquete) {
        ZonaCobertura zona = paquete.getDestinatario().getZona();
        return zona.getTarifaBase()
                .add(calcularPesoFacturable(paquete).multiply(zona.getValorPorKg()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** RN-06: pesoFacturable = max(pesoKg, alto × ancho × largo / 5000). */
    public BigDecimal calcularPesoFacturable(Paquete paquete) {
        BigDecimal volumen = BigDecimal.valueOf((long) paquete.getAltoCm() * paquete.getAnchoCm() * paquete.getLargoCm());
        BigDecimal pesoVolumetrico = volumen.divide(FACTOR_VOLUMETRICO, 2, RoundingMode.HALF_UP);
        return paquete.getPesoKg().max(pesoVolumetrico);
    }

    /** RN-07: el mensajero debe estar DISPONIBLE y su zona debe ser la ciudad de la dirección de recolección. */
    public void validarMensajeroAsignable(Mensajero mensajero, String direccionRecoleccion) {
        if (mensajero == null) {
            throw new NegocioException("RN-07", "debe seleccionar un mensajero");
        }
        if (mensajero.getEstado() != EstadoMensajero.DISPONIBLE) {
            throw new NegocioException("RN-07", "el mensajero " + mensajero.getNombre() + " está "
                    + mensajero.getEstado() + " y debe estar DISPONIBLE");
        }
        String ciudad = extraerCiudad(direccionRecoleccion);
        if (ciudad == null) {
            throw new NegocioException("RN-07", "la dirección de recolección debe terminar con la ciudad"
                    + " separada por coma, por ejemplo \"Calle 45 # 12-30, Bogotá\"");
        }
        if (!normalizar(ciudad).equals(normalizar(mensajero.getZona()))) {
            throw new NegocioException("RN-07", "el mensajero " + mensajero.getNombre() + " cubre la zona "
                    + mensajero.getZona() + " y la recolección es en " + ciudad);
        }
    }

    /** RN-08: genera la guía consecutiva GUIA-000001 y registra usuarioOperador y selloTiempo. */
    public void registrarGuiaYTrazabilidad(Recoleccion recoleccion, String usuarioOperador) {
        recoleccion.setNumeroGuia(generarNumeroGuia());
        recoleccion.setUsuarioOperador(usuarioOperador);
        recoleccion.setSelloTiempo(LocalDateTime.now());
    }

    /** RN-08: siguiente número de guía a partir del último registrado. */
    public String generarNumeroGuia() {
        String ultima = recoleccionRepository.findUltimoNumeroGuia();
        long siguiente = ultima == null ? 1 : Long.parseLong(ultima.substring("GUIA-".length())) + 1;
        return String.format("GUIA-%06d", siguiente);
    }

    // ------------------------------------------------------------- consultas

    @Transactional(readOnly = true)
    public List<PaqueteResumen> paquetesPendientes(Long remitenteId) {
        return paqueteRepository
                .findByRemitenteIdRemitenteAndEstadoAndRecoleccionIsNullOrderByIdPaqueteAsc(remitenteId, EstadoPaquete.REGISTRADO)
                .stream()
                .map(p -> new PaqueteResumen(p.getIdPaquete(), p.getCodigo(), p.getDescripcion(), p.getPesoKg(),
                        p.getAltoCm(), p.getAnchoCm(), p.getLargoCm(), p.getSumaDimensionesCm(),
                        calcularPesoFacturable(p), p.getDestinatario().getNombre(), p.getDestinatario().getCiudad(),
                        p.getDestinatario().getZona().getNombre(), p.getDestinatario().getZona().isCubierta(),
                        calcularCostoPaquete(p)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Mensajero> mensajerosDisponibles() {
        return mensajeroRepository.findByEstadoOrderByNombreAsc(EstadoMensajero.DISPONIBLE);
    }

    @Transactional(readOnly = true)
    public Optional<Recoleccion> buscarPorId(Long id) {
        return recoleccionRepository.findById(id);
    }

    // ------------------------------------------------------------- utilidades

    private List<Paquete> cargarPaquetes(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Paquete> paquetes = new ArrayList<>(paqueteRepository.findAllById(new LinkedHashSet<>(ids)));
        paquetes.sort(Comparator.comparing(Paquete::getIdPaquete));
        return paquetes;
    }

    /** Toma la ciudad como el texto después de la última coma de la dirección. */
    static String extraerCiudad(String direccion) {
        if (direccion == null) return null;
        int coma = direccion.lastIndexOf(',');
        if (coma < 0) return null;
        String ciudad = direccion.substring(coma + 1).trim();
        return ciudad.isEmpty() ? null : ciudad;
    }

    private static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT);
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private static String numero(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }
}
