package com.javeriana.vigiturno.controllers;

import com.javeriana.vigiturno.exceptions.BusinessException;
import com.javeriana.vigiturno.exceptions.ResourceNotFoundException;
import com.javeriana.vigiturno.models.entities.Incidente;
import com.javeriana.vigiturno.models.entities.Turno;
import com.javeriana.vigiturno.models.entities.Usuario;
import com.javeriana.vigiturno.models.enums.RolNombre;
import com.javeriana.vigiturno.models.enums.SeveridadIncidente;
import com.javeriana.vigiturno.models.enums.TipoIncidente;
import com.javeriana.vigiturno.services.IncidenteService;
import com.javeriana.vigiturno.services.NotificacionService;
import com.javeriana.vigiturno.services.TurnoService;
import com.javeriana.vigiturno.services.UsuarioService;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DocenteController {

    private final TurnoService turnoService;
    private final IncidenteService incidenteService;
    private final NotificacionService notificacionService;
    private final UsuarioService usuarioService;

    public DocenteController(TurnoService turnoService,
                             IncidenteService incidenteService,
                             NotificacionService notificacionService,
                             UsuarioService usuarioService) {
        this.turnoService = turnoService;
        this.incidenteService = incidenteService;
        this.notificacionService = notificacionService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/docente")
    public String dashboardDocente(@RequestParam(value = "usuarioId", required = false) Long usuarioId,
                                   Model model) {

        List<Usuario> docentes = usuarioService.listarTodos()
                .stream()
                .filter(u -> u.getRol() == RolNombre.DOCENTE)
                .toList();

        model.addAttribute("titulo", "Dashboard docente");
        model.addAttribute("docentes", docentes);
        model.addAttribute("usuarioIdSeleccionado", usuarioId);

        if (usuarioId == null) {
            model.addAttribute("modoSeleccion", true);
            return "docente/dashboard";
        }

        Usuario docente = usuarioService.buscarPorId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado con id: " + usuarioId));

        List<Turno> turnos = turnoService.listarPorUsuarioId(usuarioId);
        List<Incidente> incidentes = incidenteService.listarPorUsuarioId(usuarioId);
        var notificaciones = notificacionService.listarPorUsuarioId(usuarioId);

        Optional<Turno> proximoTurno = turnos.stream()
                .sorted(Comparator.comparing(Turno::getFecha)
                        .thenComparing(Turno::getHoraInicio))
                .findFirst();

        model.addAttribute("modoSeleccion", false);
        model.addAttribute("docente", docente);
        model.addAttribute("totalTurnos", turnos.size());
        model.addAttribute("totalIncidentes", incidentes.size());
        model.addAttribute("totalNotificaciones", notificaciones.size());
        model.addAttribute("turnosRecientes", turnos.stream().limit(6).toList());
        model.addAttribute("incidentesRecientes", incidentes.stream().limit(5).toList());
        model.addAttribute("notificacionesRecientes", notificaciones.stream().limit(5).toList());
        model.addAttribute("proximoTurno", proximoTurno.orElse(null));

        return "docente/dashboard";
    }

    @GetMapping("/docente/turno-activo")
    public String turnoActivo(@RequestParam("usuarioId") Long usuarioId, Model model) {
        Usuario docente = usuarioService.buscarPorId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado con id: " + usuarioId));

        var turnos = turnoService.listarPorUsuarioId(usuarioId);

        Turno turnoActivo = turnos.stream()
                .sorted(Comparator.comparing(Turno::getFecha)
                        .thenComparing(Turno::getHoraInicio))
                .findFirst()
                .orElse(null);

        model.addAttribute("titulo", "Turno activo");
        model.addAttribute("docente", docente);
        model.addAttribute("turnoActivo", turnoActivo);
        return "docente/turno-activo";
    }

    @GetMapping("/docente/incidentes")
    public String incidentesDocente(@RequestParam("usuarioId") Long usuarioId, Model model) {
        Usuario docente = usuarioService.buscarPorId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado con id: " + usuarioId));

        var incidentes = incidenteService.listarPorUsuarioId(usuarioId);
        var turnos = turnoService.listarPorUsuarioId(usuarioId);

        model.addAttribute("titulo", "Incidentes docente");
        model.addAttribute("docente", docente);
        model.addAttribute("incidentes", incidentes);
        model.addAttribute("turnos", turnos);
        model.addAttribute("usuarioIdSeleccionado", usuarioId);
        return "docente/incidentes";
    }

    @GetMapping("/docente/incidentes/nuevo")
    public String nuevoIncidenteDocente(@RequestParam("usuarioId") Long usuarioId, Model model) {
        Usuario docente = usuarioService.buscarPorId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado con id: " + usuarioId));

        var turnos = turnoService.listarPorUsuarioId(usuarioId);

        Incidente incidente = new Incidente();
        incidente.setFechaHora(LocalDateTime.now());

        model.addAttribute("titulo", "Nuevo incidente docente");
        model.addAttribute("docente", docente);
        model.addAttribute("incidente", incidente);
        model.addAttribute("turnos", turnos);
        model.addAttribute("tipos", TipoIncidente.values());
        model.addAttribute("severidades", SeveridadIncidente.values());
        model.addAttribute("usuarioIdSeleccionado", usuarioId);

        return "docente/incidente-formulario";
    }

    @PostMapping("/docente/incidentes/guardar")
    public String guardarIncidenteDocente(@RequestParam("usuarioId") Long usuarioId,
                                          Incidente incidente,
                                          @RequestParam("turno") Long turnoId) {

        Usuario docente = usuarioService.buscarPorId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado con id: " + usuarioId));

        Turno turno = turnoService.buscarPorId(turnoId)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + turnoId));

        if (!turno.getUsuario().getId().equals(docente.getId())) {
            throw new BusinessException("El turno no pertenece al docente seleccionado.");
        }

        incidente.setTurno(turno);
        incidente.setZona(turno.getZona());

        incidenteService.guardar(incidente);
        return "redirect:/docente/incidentes?usuarioId=" + usuarioId;
    }

    @GetMapping("/docente/incidentes/editar/{id}")
    public String editarIncidenteDocente(@PathVariable Long id,
                                         @RequestParam("usuarioId") Long usuarioId,
                                         Model model) {
        Usuario docente = usuarioService.buscarPorId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado con id: " + usuarioId));

        Incidente incidente = incidenteService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente no encontrado con id: " + id));

        if (incidente.getTurno() == null || !incidente.getTurno().getUsuario().getId().equals(docente.getId())) {
            throw new BusinessException("El incidente no pertenece al docente seleccionado.");
        }

        var turnos = turnoService.listarPorUsuarioId(usuarioId);

        model.addAttribute("titulo", "Editar incidente docente");
        model.addAttribute("docente", docente);
        model.addAttribute("incidente", incidente);
        model.addAttribute("turnos", turnos);
        model.addAttribute("tipos", TipoIncidente.values());
        model.addAttribute("severidades", SeveridadIncidente.values());
        model.addAttribute("usuarioIdSeleccionado", usuarioId);

        return "docente/incidente-formulario";
    }

    @GetMapping("/docente/incidentes/eliminar/{id}")
    public String eliminarIncidenteDocente(@PathVariable Long id,
                                           @RequestParam("usuarioId") Long usuarioId) {
        Usuario docente = usuarioService.buscarPorId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado con id: " + usuarioId));

        Incidente incidente = incidenteService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente no encontrado con id: " + id));

        if (incidente.getTurno() == null || !incidente.getTurno().getUsuario().getId().equals(docente.getId())) {
            throw new BusinessException("El incidente no pertenece al docente seleccionado.");
        }

        incidenteService.eliminarPorId(id);
        return "redirect:/docente/incidentes?usuarioId=" + usuarioId;
    }

    @GetMapping("/docente/cierre-turno")
    public String cierreTurno(@RequestParam("usuarioId") Long usuarioId, Model model) {
        Usuario docente = usuarioService.buscarPorId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado con id: " + usuarioId));

        var turnos = turnoService.listarPorUsuarioId(usuarioId);

        Turno ultimoTurno = turnos.stream()
                .sorted(Comparator.comparing(Turno::getFecha)
                        .thenComparing(Turno::getHoraInicio))
                .reduce((first, second) -> second)
                .orElse(null);

        model.addAttribute("titulo", "Cierre de turno");
        model.addAttribute("docente", docente);
        model.addAttribute("ultimoTurno", ultimoTurno);
        return "docente/cierre-turno";
    }
}