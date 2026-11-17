package com.logistica.envios.controller;

import com.logistica.envios.dto.PaqueteResumen;
import com.logistica.envios.dto.RemitenteResumen;
import com.logistica.envios.service.RecoleccionService;
import com.logistica.envios.service.RemitenteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final RemitenteService remitenteService;
    private final RecoleccionService recoleccionService;

    public ApiController(RemitenteService remitenteService, RecoleccionService recoleccionService) {
        this.remitenteService = remitenteService;
        this.recoleccionService = recoleccionService;
    }

    @GetMapping("/remitentes")
    public List<RemitenteResumen> buscarRemitentes(@RequestParam(defaultValue = "") String query) {
        return remitenteService.buscar(query);
    }

    @GetMapping("/paquetes")
    public List<PaqueteResumen> paquetesPendientes(@RequestParam Long remitenteId) {
        return recoleccionService.paquetesPendientes(remitenteId);
    }
}
