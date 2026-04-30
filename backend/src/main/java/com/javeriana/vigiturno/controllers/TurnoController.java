package com.javeriana.vigiturno.controllers;

import com.javeriana.vigiturno.exceptions.ResourceNotFoundException;
import com.javeriana.vigiturno.models.entities.Turno;
import com.javeriana.vigiturno.models.enums.EstadoTurno;
import com.javeriana.vigiturno.services.TurnoService;
import com.javeriana.vigiturno.services.UsuarioService;
import com.javeriana.vigiturno.services.ZonaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TurnoController {

    private final TurnoService turnoService;
    private final UsuarioService usuarioService;
    private final ZonaService zonaService;

    public TurnoController(TurnoService turnoService,
                           UsuarioService usuarioService,
                           ZonaService zonaService) {
        this.turnoService = turnoService;
        this.usuarioService = usuarioService;
        this.zonaService = zonaService;
    }

    @GetMapping("/turnos")
    public String listarTurnos(Model model) {
        model.addAttribute("titulo", "Turnos");
        model.addAttribute("turnos", turnoService.listarTodos());
        return "turnos/lista";
    }

    @GetMapping("/turnos/nuevo")
    public String mostrarFormularioNuevoTurno(Model model) {
        model.addAttribute("titulo", "Nuevo turno");
        model.addAttribute("turno", new Turno());
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("zonas", zonaService.listarTodas());
        model.addAttribute("estados", EstadoTurno.values());
        model.addAttribute("modoEdicion", false);
        return "turnos/formulario";
    }

    @PostMapping("/turnos/guardar")
    public String guardarTurno(Turno turno,
                               @RequestParam("usuario") Long usuarioId,
                               @RequestParam("zona") Long zonaId) {

        turno.setUsuario(
                usuarioService.buscarPorId(usuarioId)
                        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId))
        );

        turno.setZona(
                zonaService.buscarPorId(zonaId)
                        .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con id: " + zonaId))
        );

        turnoService.guardar(turno);
        return "redirect:/turnos";
    }

    @GetMapping("/turnos/editar/{id}")
    public String mostrarFormularioEditarTurno(@PathVariable Long id, Model model) {
        Turno turno = turnoService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + id));

        model.addAttribute("titulo", "Editar turno");
        model.addAttribute("turno", turno);
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("zonas", zonaService.listarTodas());
        model.addAttribute("estados", EstadoTurno.values());
        model.addAttribute("modoEdicion", true);
        return "turnos/formulario";
    }

    @GetMapping("/turnos/eliminar/{id}")
    public String eliminarTurno(@PathVariable Long id) {
        turnoService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + id));

        turnoService.eliminarPorId(id);
        return "redirect:/turnos";
    }
}