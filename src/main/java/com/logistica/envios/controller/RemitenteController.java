package com.logistica.envios.controller;

import com.logistica.envios.exception.NegocioException;
import com.logistica.envios.model.EstadoRemitente;
import com.logistica.envios.model.Remitente;
import com.logistica.envios.service.RemitenteService;
import jakarta.validation.Valid;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/remitentes")
public class RemitenteController {

    private final RemitenteService remitenteService;

    public RemitenteController(RemitenteService remitenteService) {
        this.remitenteService = remitenteService;
    }

    // Campos vacíos llegan como null (el teléfono es opcional, pero si se digita debe ser válido)
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.setDisallowedFields("idRemitente");
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int pagina,
                         Model model) {
        model.addAttribute("pagina", remitenteService.listar(q, pagina));
        model.addAttribute("q", q);
        return "remitentes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        return formulario(model, new Remitente());
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("remitente") Remitente remitente, BindingResult resultado,
                        Model model, RedirectAttributes flash) {
        if (resultado.hasErrors()) {
            return formulario(model, remitente);
        }
        try {
            remitenteService.crear(remitente);
        } catch (NegocioException e) {
            resultado.rejectValue("documento", "duplicado", mayuscula(e.getDetalle()));
            return formulario(model, remitente);
        }
        flash.addFlashAttribute("exito", "Remitente " + remitente.getNombre() + " creado.");
        return "redirect:/remitentes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Remitente remitente = remitenteService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El remitente no existe"));
        return formulario(model, remitente);
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("remitente") Remitente remitente,
                             BindingResult resultado, Model model, RedirectAttributes flash) {
        remitente.setIdRemitente(id);
        if (resultado.hasErrors()) {
            return formulario(model, remitente);
        }
        try {
            remitenteService.actualizar(id, remitente);
        } catch (NegocioException e) {
            resultado.rejectValue("documento", "duplicado", mayuscula(e.getDetalle()));
            return formulario(model, remitente);
        }
        flash.addFlashAttribute("exito", "Remitente " + remitente.getNombre() + " actualizado.");
        return "redirect:/remitentes";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, @RequestParam(defaultValue = "") String q,
                           @RequestParam(defaultValue = "0") int pagina, RedirectAttributes flash) {
        try {
            remitenteService.eliminar(id);
            flash.addFlashAttribute("exito", "Remitente eliminado.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", mayuscula(e.getDetalle()) + ".");
        }
        flash.addAttribute("q", q);
        flash.addAttribute("pagina", pagina);
        return "redirect:/remitentes";
    }

    private String formulario(Model model, Remitente remitente) {
        model.addAttribute("remitente", remitente);
        model.addAttribute("estados", EstadoRemitente.values());
        return "remitentes/formulario";
    }

    private static String mayuscula(String texto) {
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
