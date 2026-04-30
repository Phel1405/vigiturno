package com.javeriana.vigiturno.controllers;

import com.javeriana.vigiturno.services.IncidenteService;
import com.javeriana.vigiturno.services.ReasignacionService;
import com.javeriana.vigiturno.services.TurnoService;
import com.javeriana.vigiturno.services.ZonaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CoordinadorController {

    private final TurnoService turnoService;
    private final ZonaService zonaService;
    private final IncidenteService incidenteService;
    private final ReasignacionService reasignacionService;

    public CoordinadorController(TurnoService turnoService,
                                 ZonaService zonaService,
                                 IncidenteService incidenteService,
                                 ReasignacionService reasignacionService) {
        this.turnoService = turnoService;
        this.zonaService = zonaService;
        this.incidenteService = incidenteService;
        this.reasignacionService = reasignacionService;
    }

    @GetMapping("/coordinador")
    public String dashboardCoordinador(Model model) {
        var turnos = turnoService.listarTodos();
        var zonas = zonaService.listarTodas();
        var incidentes = incidenteService.listarTodos();
        var reasignaciones = reasignacionService.listarTodas();

        model.addAttribute("titulo", "Dashboard coordinador");
        model.addAttribute("totalTurnos", turnos.size());
        model.addAttribute("totalZonas", zonas.size());
        model.addAttribute("totalIncidentes", incidentes.size());
        model.addAttribute("totalReasignaciones", reasignaciones.size());

        model.addAttribute("zonasRecientes", zonas.stream().limit(5).toList());
        model.addAttribute("turnosRecientes", turnos.stream().limit(5).toList());
        model.addAttribute("incidentesRecientes", incidentes.stream().limit(5).toList());
        model.addAttribute("reasignacionesRecientes", reasignaciones.stream().limit(5).toList());

        return "coordinador/dashboard";
    }

    @GetMapping("/coordinador/cobertura")
    public String cobertura(Model model) {
        var zonas = zonaService.listarTodas();
        var turnos = turnoService.listarTodos();
        var incidentes = incidenteService.listarTodos();

        model.addAttribute("titulo", "Cobertura");
        model.addAttribute("zonas", zonas);
        model.addAttribute("totalZonas", zonas.size());
        model.addAttribute("totalTurnos", turnos.size());
        model.addAttribute("totalIncidentes", incidentes.size());

        return "coordinador/cobertura";
    }

    @GetMapping("/coordinador/alertas")
    public String alertas(Model model) {
        var incidentes = incidenteService.listarTodos();
        var reasignaciones = reasignacionService.listarTodas();
        var turnos = turnoService.listarTodos();

        model.addAttribute("titulo", "Alertas");
        model.addAttribute("incidentes", incidentes.stream().limit(8).toList());
        model.addAttribute("reasignaciones", reasignaciones.stream().limit(8).toList());
        model.addAttribute("turnos", turnos.stream().limit(8).toList());

        model.addAttribute("totalIncidentes", incidentes.size());
        model.addAttribute("totalReasignaciones", reasignaciones.size());
        model.addAttribute("totalTurnos", turnos.size());

        return "coordinador/alertas";
    }

    @GetMapping("/coordinador/reasignaciones")
    public String reasignaciones(Model model) {
        var reasignaciones = reasignacionService.listarTodas();
        var turnos = turnoService.listarTodos();
        var incidentes = incidenteService.listarTodos();

        model.addAttribute("titulo", "Reasignaciones coordinador");
        model.addAttribute("reasignaciones", reasignaciones);
        model.addAttribute("totalReasignaciones", reasignaciones.size());
        model.addAttribute("totalTurnos", turnos.size());
        model.addAttribute("totalIncidentes", incidentes.size());

        return "coordinador/reasignaciones";
    }
}