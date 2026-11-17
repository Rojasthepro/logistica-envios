package com.logistica.envios.service;

import com.logistica.envios.dto.SolicitudRecoleccion;
import com.logistica.envios.exception.NegocioException;
import com.logistica.envios.model.*;
import com.logistica.envios.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas de la transacción "Registrar recolección" en formato Given-When-Then.
 * Cada prueba arma sus propios datos y se revierte al terminar (@Transactional).
 */
@SpringBootTest
@Transactional
class RecoleccionServiceTest {

    @Autowired private RecoleccionService recoleccionService;
    @Autowired private RecoleccionRepository recoleccionRepository;
    @Autowired private RemitenteRepository remitenteRepository;
    @Autowired private PaqueteRepository paqueteRepository;
    @Autowired private MensajeroRepository mensajeroRepository;
    @Autowired private DestinatarioRepository destinatarioRepository;
    @Autowired private ZonaCoberturaRepository zonaRepository;

    private ZonaCobertura zonaCubierta;
    private ZonaCobertura zonaSinCobertura;
    private Mensajero mensajeroDisponible;

    @BeforeEach
    void prepararDatosComunes() {
        zonaCubierta = zona("Zona prueba cubierta", true);
        zonaSinCobertura = zona("Zona prueba sin cobertura", false);
        mensajeroDisponible = mensajero("Ciudad Prueba");
    }

    @Test
    @DisplayName("Dado un paquete de 35 kg, cuando se confirma la recolección, entonces se rechaza con RN-01 y no se crea")
    void paqueteDe35Kg_rechazaConRN01_yNoCreaLaRecoleccion() {
        // Given: remitente activo con un paquete de 35 kg hacia una zona cubierta
        Remitente remitente = remitente("9000000001", EstadoRemitente.ACTIVO);
        Paquete pesado = paquete(remitente, destinatario(zonaCubierta), "35.00", 40, 40, 40);
        long recoleccionesAntes = recoleccionRepository.count();

        // When / Then: se rechaza citando RN-01
        assertThatThrownBy(() -> recoleccionService.confirmarRecoleccion(solicitud(remitente, pesado), "tester"))
                .isInstanceOf(NegocioException.class)
                .hasMessageStartingWith("RN-01")
                .hasMessageContaining(pesado.getCodigo() + " pesa 35 kg y el máximo es 30 kg");

        // Then: no se creó la recolección y el paquete sigue pendiente
        assertThat(recoleccionRepository.count()).isEqualTo(recoleccionesAntes);
        assertThat(pesado.getEstado()).isEqualTo(EstadoPaquete.REGISTRADO);
        assertThat(pesado.getRecoleccion()).isNull();
        assertThat(mensajeroDisponible.getEstado()).isEqualTo(EstadoMensajero.DISPONIBLE);
    }

    @Test
    @DisplayName("Dado un destinatario en zona sin cobertura, cuando se confirma la recolección, entonces se rechaza con RN-02")
    void destinatarioEnZonaSinCobertura_rechazaConRN02() {
        // Given: paquete válido en peso y medidas, pero su destinatario está en una zona sin cobertura
        Remitente remitente = remitente("9000000002", EstadoRemitente.ACTIVO);
        Paquete paquete = paquete(remitente, destinatario(zonaSinCobertura), "2.00", 20, 20, 20);

        // When / Then
        assertThatThrownBy(() -> recoleccionService.confirmarRecoleccion(solicitud(remitente, paquete), "tester"))
                .isInstanceOf(NegocioException.class)
                .hasMessageStartingWith("RN-02")
                .hasMessageContaining("no tiene cobertura");
        assertThat(paquete.getEstado()).isEqualTo(EstadoPaquete.REGISTRADO);
    }

    @Test
    @DisplayName("Dado un remitente INACTIVO, cuando se confirma la recolección, entonces se rechaza con RN-03")
    void remitenteInactivo_rechazaConRN03() {
        // Given: remitente INACTIVO con un paquete que por lo demás es válido
        Remitente remitente = remitente("9000000003", EstadoRemitente.INACTIVO);
        Paquete paquete = paquete(remitente, destinatario(zonaCubierta), "2.00", 20, 20, 20);

        // When / Then
        assertThatThrownBy(() -> recoleccionService.confirmarRecoleccion(solicitud(remitente, paquete), "tester"))
                .isInstanceOf(NegocioException.class)
                .hasMessageStartingWith("RN-03")
                .hasMessageContaining("INACTIVO");
        assertThat(paquete.getEstado()).isEqualTo(EstadoPaquete.REGISTRADO);
    }

    @Test
    @DisplayName("Dado un caso válido, cuando se confirma, entonces crea la recolección con guía, paquetes RECOGIDO y mensajero EN_RUTA")
    void casoValido_creaRecoleccionConGuia_paquetesRecogidos_yMensajeroEnRuta() {
        // Given: remitente activo con dos paquetes válidos hacia zona cubierta y un mensajero de su ciudad
        Remitente remitente = remitente("9000000004", EstadoRemitente.ACTIVO);
        Destinatario destinatario = destinatario(zonaCubierta);
        Paquete liviano = paquete(remitente, destinatario, "2.00", 10, 10, 10);   // facturable 2 kg
        Paquete voluminoso = paquete(remitente, destinatario, "1.00", 50, 40, 30); // volumétrico 12 kg
        String guiaEsperada = recoleccionService.generarNumeroGuia();

        // When
        Recoleccion recoleccion = recoleccionService.confirmarRecoleccion(solicitud(remitente, liviano, voluminoso), "tester");

        // Then: la recolección queda REALIZADA, con guía consecutiva, trazabilidad y costo RN-06
        assertThat(recoleccion.getIdRecoleccion()).isNotNull();
        assertThat(recoleccion.getEstado()).isEqualTo(EstadoRecoleccion.REALIZADA);
        assertThat(recoleccion.getNumeroGuia()).matches("GUIA-\\d{6}").isEqualTo(guiaEsperada);
        assertThat(recoleccion.getUsuarioOperador()).isEqualTo("tester");
        assertThat(recoleccion.getSelloTiempo()).isNotNull();
        // (8000 + 2 * 1000) + (8000 + 12 * 1000) = 30000
        assertThat(recoleccion.getCostoTotal()).isEqualByComparingTo("30000");

        // Then: los paquetes quedan RECOGIDO y asociados, y el mensajero EN_RUTA
        List<Paquete> paquetes = paqueteRepository.findAllById(List.of(liviano.getIdPaquete(), voluminoso.getIdPaquete()));
        assertThat(paquetes).allSatisfy(p -> {
            assertThat(p.getEstado()).isEqualTo(EstadoPaquete.RECOGIDO);
            assertThat(p.getRecoleccion()).isEqualTo(recoleccion);
        });
        assertThat(mensajeroRepository.findById(mensajeroDisponible.getIdMensajero()))
                .get().extracting(Mensajero::getEstado).isEqualTo(EstadoMensajero.EN_RUTA);
    }

    // ------------------------------------------------------------ fixtures

    private SolicitudRecoleccion solicitud(Remitente remitente, Paquete... paquetes) {
        SolicitudRecoleccion s = new SolicitudRecoleccion();
        s.setRemitenteId(remitente.getIdRemitente());
        s.setPaqueteIds(java.util.Arrays.stream(paquetes).map(Paquete::getIdPaquete).toList());
        s.setMensajeroId(mensajeroDisponible.getIdMensajero());
        s.setDireccionRecoleccion(remitente.getDireccion() + ", " + remitente.getCiudad());
        return s;
    }

    private ZonaCobertura zona(String nombre, boolean cubierta) {
        ZonaCobertura z = new ZonaCobertura();
        z.setNombre(nombre);
        z.setCubierta(cubierta);
        z.setTarifaBase(new BigDecimal("8000"));
        z.setValorPorKg(new BigDecimal("1000"));
        return zonaRepository.save(z);
    }

    private Mensajero mensajero(String zona) {
        Mensajero m = new Mensajero();
        m.setNombre("Mensajero de prueba");
        m.setDocumento("123456");
        m.setZona(zona);
        m.setEstado(EstadoMensajero.DISPONIBLE);
        return mensajeroRepository.save(m);
    }

    private Remitente remitente(String documento, EstadoRemitente estado) {
        Remitente r = new Remitente();
        r.setNombre("Remitente " + documento);
        r.setDocumento(documento);
        r.setTelefono("3001112233");
        r.setDireccion("Calle 1 # 2-3");
        r.setCiudad("Ciudad Prueba");
        r.setEstado(estado);
        return remitenteRepository.save(r);
    }

    private Destinatario destinatario(ZonaCobertura zona) {
        Destinatario d = new Destinatario();
        d.setNombre("Destinatario en " + zona.getNombre());
        d.setDireccion("Carrera 9 # 8-7");
        d.setCiudad(zona.getNombre());
        d.setZona(zona);
        return destinatarioRepository.save(d);
    }

    private Paquete paquete(Remitente remitente, Destinatario destinatario, String pesoKg, int alto, int ancho, int largo) {
        Paquete p = new Paquete();
        p.setDescripcion("Paquete de prueba");
        p.setPesoKg(new BigDecimal(pesoKg));
        p.setAltoCm(alto);
        p.setAnchoCm(ancho);
        p.setLargoCm(largo);
        p.setValorDeclarado(new BigDecimal("100000"));
        p.setEstado(EstadoPaquete.REGISTRADO);
        p.setDestinatario(destinatario);
        p.setRemitente(remitente);
        return paqueteRepository.save(p);
    }
}
