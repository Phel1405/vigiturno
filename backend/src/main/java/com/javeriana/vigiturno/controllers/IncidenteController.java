package com.javeriana.vigiturno.controllers;

import com.javeriana.vigiturno.exceptions.ResourceNotFoundException;
import com.javeriana.vigiturno.models.entities.Incidente;
import com.javeriana.vigiturno.models.enums.SeveridadIncidente;
import com.javeriana.vigiturno.models.enums.TipoIncidente;
import com.javeriana.vigiturno.services.IncidenteService;
import com.javeriana.vigiturno.services.TurnoService;
import com.javeriana.vigiturno.services.ZonaService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IncidenteController {

    private final IncidenteService incidenteService;
    private final TurnoService turnoService;
    private final ZonaService zonaService;

    public IncidenteController(IncidenteService incidenteService,
                               TurnoService turnoService,
                               ZonaService zonaService) {
        this.incidenteService = incidenteService;
        this.turnoService = turnoService;
        this.zonaService = zonaService;
    }

    @GetMapping("/incidentes")
    public String listarIncidentes(Model model) {
        model.addAttribute("titulo", "Incidentes");
        model.addAttribute("incidentes", incidenteService.listarTodos());
        return "incidentes/lista";
    }

    @GetMapping("/incidentes/nuevo")
    public String mostrarFormularioNuevoIncidente(Model model) {
        Incidente incidente = new Incidente();
        incidente.setFechaHora(LocalDateTime.now());

        model.addAttribute("titulo", "Nuevo incidente");
        model.addAttribute("incidente", incidente);
        model.addAttribute("tipos", TipoIncidente.values());
        model.addAttribute("severidades", SeveridadIncidente.values());
        model.addAttribute("turnos", turnoService.listarTodos());
        model.addAttribute("zonas", zonaService.listarTodas());
        model.addAttribute("modoEdicion", false);
        return "incidentes/formulario";
    }

    @PostMapping("/incidentes/guardar")
    public String guardarIncidente(Incidente incidente,
                                   @RequestParam(value = "turno", required = false) Long turnoId,
                                   @RequestParam("zona") Long zonaId) {

        if (turnoId != null) {
            incidente.setTurno(
                    turnoService.buscarPorId(turnoId)
                            .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + turnoId))
            );
        } else {
            incidente.setTurno(null);
        }

        incidente.setZona(
                zonaService.buscarPorId(zonaId)
                        .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con id: " + zonaId))
        );

        incidenteService.guardar(incidente);
        return "redirect:/incidentes";
    }

    @GetMapping("/incidentes/editar/{id}")
    public String mostrarFormularioEditarIncidente(@PathVariable Long id, Model model) {
        Incidente incidente = incidenteService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente no encontrado con id: " + id));

        model.addAttribute("titulo", "Editar incidente");
        model.addAttribute("incidente", incidente);
        model.addAttribute("tipos", TipoIncidente.values());
        model.addAttribute("severidades", SeveridadIncidente.values());
        model.addAttribute("turnos", turnoService.listarTodos());
        model.addAttribute("zonas", zonaService.listarTodas());
        model.addAttribute("modoEdicion", true);
        return "incidentes/formulario";
    }

    @GetMapping("/incidentes/eliminar/{id}")
    public String eliminarIncidente(@PathVariable Long id) {
        incidenteService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente no encontrado con id: " + id));

        incidenteService.eliminarPorId(id);
        return "redirect:/incidentes";
    }
}