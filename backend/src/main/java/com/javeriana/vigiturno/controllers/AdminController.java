package com.javeriana.vigiturno.controllers;

import com.javeriana.vigiturno.services.IncidenteService;
import com.javeriana.vigiturno.services.NotificacionService;
import com.javeriana.vigiturno.services.ReasignacionService;
import com.javeriana.vigiturno.services.TurnoService;
import com.javeriana.vigiturno.services.UsuarioService;
import com.javeriana.vigiturno.services.ZonaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    private final UsuarioService usuarioService;
    private final ZonaService zonaService;
    private final TurnoService turnoService;
    private final IncidenteService incidenteService;
    private final ReasignacionService reasignacionService;
    private final NotificacionService notificacionService;

    public AdminController(UsuarioService usuarioService,
                           ZonaService zonaService,
                           TurnoService turnoService,
                           IncidenteService incidenteService,
                           ReasignacionService reasignacionService,
                           NotificacionService notificacionService) {
        this.usuarioService = usuarioService;
        this.zonaService = zonaService;
        this.turnoService = turnoService;
        this.incidenteService = incidenteService;
        this.reasignacionService = reasignacionService;
        this.notificacionService = notificacionService;
    }

    @GetMapping("/admin")
    public String dashboardAdmin(Model model) {
        var usuarios = usuarioService.listarTodos();
        var zonas = zonaService.listarTodas();
        var turnos = turnoService.listarTodos();
        var incidentes = incidenteService.listarTodos();
        var reasignaciones = reasignacionService.listarTodas();
        var notificaciones = notificacionService.listarTodas();

        model.addAttribute("titulo", "Dashboard administrador");
        model.addAttribute("totalUsuarios", usuarios.size());
        model.addAttribute("totalZonas", zonas.size());
        model.addAttribute("totalTurnos", turnos.size());
        model.addAttribute("totalIncidentes", incidentes.size());
        model.addAttribute("totalReasignaciones", reasignaciones.size());
        model.addAttribute("totalNotificaciones", notificaciones.size());

        model.addAttribute("usuariosRecientes", usuarios.stream().limit(5).toList());
        model.addAttribute("zonasRecientes", zonas.stream().limit(5).toList());
        model.addAttribute("turnosRecientes", turnos.stream().limit(5).toList());
        model.addAttribute("incidentesRecientes", incidentes.stream().limit(5).toList());

        return "admin/dashboard";
    }
}