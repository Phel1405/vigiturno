package com.javeriana.vigiturno.controllers;

import com.javeriana.vigiturno.exceptions.ResourceNotFoundException;
import com.javeriana.vigiturno.models.entities.Reasignacion;
import com.javeriana.vigiturno.models.enums.EstadoReasignacion;
import com.javeriana.vigiturno.services.ReasignacionService;
import com.javeriana.vigiturno.services.TurnoService;
import com.javeriana.vigiturno.services.UsuarioService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReasignacionController {

    private final ReasignacionService reasignacionService;
    private final TurnoService turnoService;
    private final UsuarioService usuarioService;

    public ReasignacionController(ReasignacionService reasignacionService,
                                  TurnoService turnoService,
                                  UsuarioService usuarioService) {
        this.reasignacionService = reasignacionService;
        this.turnoService = turnoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/reasignaciones")
    public String listarReasignaciones(Model model) {
        model.addAttribute("titulo", "Reasignaciones");
        model.addAttribute("reasignaciones", reasignacionService.listarTodas());
        return "reasignaciones/lista";
    }

    @GetMapping("/reasignaciones/nueva")
    public String mostrarFormularioNuevaReasignacion(Model model) {
        Reasignacion reasignacion = new Reasignacion();
        reasignacion.setFechaHoraSolicitud(LocalDateTime.now());
        reasignacion.setEstado(EstadoReasignacion.PENDIENTE);

        model.addAttribute("titulo", "Nueva reasignación");
        model.addAttribute("reasignacion", reasignacion);
        model.addAttribute("estados", EstadoReasignacion.values());
        model.addAttribute("turnos", turnoService.listarTodos());
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("modoEdicion", false);
        return "reasignaciones/formulario";
    }

    @PostMapping("/reasignaciones/guardar")
    public String guardarReasignacion(Reasignacion reasignacion,
                                      @RequestParam("turno") Long turnoId,
                                      @RequestParam("docenteOriginal") Long docenteOriginalId,
                                      @RequestParam(value = "docenteReemplazo", required = false) Long docenteReemplazoId) {

        reasignacion.setTurno(
                turnoService.buscarPorId(turnoId)
                        .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + turnoId))
        );

        reasignacion.setDocenteOriginal(
                usuarioService.buscarPorId(docenteOriginalId)
                        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + docenteOriginalId))
        );

        if (docenteReemplazoId != null) {
            reasignacion.setDocenteReemplazo(
                    usuarioService.buscarPorId(docenteReemplazoId)
                            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + docenteReemplazoId))
            );
        } else {
            reasignacion.setDocenteReemplazo(null);
        }

        reasignacionService.guardar(reasignacion);
        return "redirect:/reasignaciones";
    }

    @GetMapping("/reasignaciones/editar/{id}")
    public String mostrarFormularioEditarReasignacion(@PathVariable Long id, Model model) {
        Reasignacion reasignacion = reasignacionService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reasignación no encontrada con id: " + id));

        model.addAttribute("titulo", "Editar reasignación");
        model.addAttribute("reasignacion", reasignacion);
        model.addAttribute("estados", EstadoReasignacion.values());
        model.addAttribute("turnos", turnoService.listarTodos());
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("modoEdicion", true);
        return "reasignaciones/formulario";
    }

    @GetMapping("/reasignaciones/eliminar/{id}")
    public String eliminarReasignacion(@PathVariable Long id) {
        reasignacionService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reasignación no encontrada con id: " + id));

        reasignacionService.eliminarPorId(id);
        return "redirect:/reasignaciones";
    }
}