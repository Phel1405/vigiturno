package com.javeriana.vigiturno.controllers;

import com.javeriana.vigiturno.exceptions.ResourceNotFoundException;
import com.javeriana.vigiturno.models.entities.Zona;
import com.javeriana.vigiturno.services.ZonaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ZonaController {

    private final ZonaService zonaService;

    public ZonaController(ZonaService zonaService) {
        this.zonaService = zonaService;
    }

    @GetMapping("/zonas")
    public String listarZonas(Model model) {
        model.addAttribute("titulo", "Zonas");
        model.addAttribute("zonas", zonaService.listarTodas());
        return "zonas/lista";
    }

    @GetMapping("/zonas/nueva")
    public String mostrarFormularioNuevaZona(Model model) {
        model.addAttribute("titulo", "Nueva zona");
        model.addAttribute("zona", new Zona());
        model.addAttribute("modoEdicion", false);
        return "zonas/formulario";
    }

    @PostMapping("/zonas/guardar")
    public String guardarZona(Zona zona) {
        if (zona.getActiva() == null) {
            zona.setActiva(false);
        }
        zonaService.guardar(zona);
        return "redirect:/zonas";
    }

    @GetMapping("/zonas/editar/{id}")
    public String mostrarFormularioEditarZona(@PathVariable Long id, Model model) {
        Zona zona = zonaService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con id: " + id));

        model.addAttribute("titulo", "Editar zona");
        model.addAttribute("zona", zona);
        model.addAttribute("modoEdicion", true);
        return "zonas/formulario";
    }

    @GetMapping("/zonas/eliminar/{id}")
    public String eliminarZona(@PathVariable Long id) {
        zonaService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con id: " + id));

        zonaService.eliminarPorId(id);
        return "redirect:/zonas";
    }
}