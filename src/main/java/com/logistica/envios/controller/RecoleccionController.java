package com.logistica.envios.controller;

import com.logistica.envios.dto.RemitenteResumen;
import com.logistica.envios.dto.SolicitudRecoleccion;
import com.logistica.envios.exception.NegocioException;
import com.logistica.envios.model.Paquete;
import com.logistica.envios.model.Recoleccion;
import com.logistica.envios.service.RecoleccionService;
import com.logistica.envios.service.RemitenteService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/recolecciones")
public class RecoleccionController {

    private final RecoleccionService recoleccionService;
    private final RemitenteService remitenteService;
    private final String usuarioOperador;

    public RecoleccionController(RecoleccionService recoleccionService, RemitenteService remitenteService,
                                 @Value("${logistica.usuario-operador}") String usuarioOperador) {
        this.recoleccionService = recoleccionService;
        this.remitenteService = remitenteService;
        this.usuarioOperador = usuarioOperador;
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Long remitenteId, Model model) {
        SolicitudRecoleccion solicitud = new SolicitudRecoleccion();
        solicitud.setRemitenteId(remitenteId);
        return pantalla(model, solicitud);
    }

    @PostMapping
    public String confirmar(@ModelAttribute("solicitud") SolicitudRecoleccion solicitud, Model model) {
        try {
            Recoleccion recoleccion = recoleccionService.confirmarRecoleccion(solicitud, usuarioOperador);
            return "redirect:/recolecciones/" + recoleccion.getIdRecoleccion();
        } catch (NegocioException e) {
            // Se vuelve a pintar la pantalla con lo digitado y el error de la regla
            model.addAttribute("error", e.getMessage());
            return pantalla(model, solicitud);
        }
    }

    @GetMapping("/{id}")
    public String comprobante(@PathVariable Long id, Model model) {
        Recoleccion recoleccion = recoleccionService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La recolección no existe"));
        model.addAttribute("recoleccion", recoleccion);
        // Desglose por paquete de RN-06 para el comprobante
        Map<Long, BigDecimal> pesosFacturables = new HashMap<>();
        Map<Long, BigDecimal> costos = new HashMap<>();
        for (Paquete p : recoleccion.getPaquetes()) {
            pesosFacturables.put(p.getIdPaquete(), recoleccionService.calcularPesoFacturable(p));
            costos.put(p.getIdPaquete(), recoleccionService.calcularCostoPaquete(p));
        }
        model.addAttribute("pesosFacturables", pesosFacturables);
        model.addAttribute("costos", costos);
        return "recolecciones/comprobante";
    }

    private String pantalla(Model model, SolicitudRecoleccion solicitud) {
        model.addAttribute("solicitud", solicitud);
        model.addAttribute("mensajeros", recoleccionService.mensajerosDisponibles());
        RemitenteResumen remitente = solicitud.getRemitenteId() == null ? null
                : remitenteService.buscarPorId(solicitud.getRemitenteId()).map(RemitenteResumen::de).orElse(null);
        model.addAttribute("remitenteSeleccionado", remitente);
        return "recolecciones/nueva";
    }
}
