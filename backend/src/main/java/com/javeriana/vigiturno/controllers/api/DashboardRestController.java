package com.javeriana.vigiturno.controllers.api;

import com.javeriana.vigiturno.dtos.api.ApiDtos.DashboardDto;
import com.javeriana.vigiturno.dtos.api.ApiDtos.DocenteDashboardDto;
import com.javeriana.vigiturno.dtos.api.ApiDtos.HeatmapZonaDto;
import com.javeriana.vigiturno.dtos.api.ApiMapper;
import com.javeriana.vigiturno.exceptions.ResourceNotFoundException;
import com.javeriana.vigiturno.models.entities.Turno;
import com.javeriana.vigiturno.services.IncidenteService;
import com.javeriana.vigiturno.services.NotificacionService;
import com.javeriana.vigiturno.services.ReasignacionService;
import com.javeriana.vigiturno.services.TurnoService;
import com.javeriana.vigiturno.services.UsuarioService;
import com.javeriana.vigiturno.services.ZonaService;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {

    private final UsuarioService usuarioService;
    private final ZonaService zonaService;
    private final TurnoService turnoService;
    private final IncidenteService incidenteService;
    private final ReasignacionService reasignacionService;
    private final NotificacionService notificacionService;

    public DashboardRestController(UsuarioService usuarioService,
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
    public DashboardDto admin() {
        return resumenGeneral();
    }

    @GetMapping("/coordinador")
    public DashboardDto coordinador() {
        return resumenGeneral();
    }

    @GetMapping("/docente")
    public DocenteDashboardDto docente(@RequestParam Long usuarioId) {
        var docente = usuarioService.buscarPorId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado con id: " + usuarioId));
        var turnos = turnoService.listarPorUsuarioId(usuarioId);
        var incidentes = incidenteService.listarPorUsuarioId(usuarioId);
        var notificaciones = notificacionService.listarPorUsuarioId(usuarioId);
        var proximoTurno = turnos.stream()
                .sorted(Comparator.comparing(Turno::getFecha).thenComparing(Turno::getHoraInicio))
                .findFirst()
                .orElse(null);

        return new DocenteDashboardDto(
                ApiMapper.toDto(docente),
                turnos.size(),
                incidentes.size(),
                notificaciones.size(),
                ApiMapper.toDto(proximoTurno),
                turnos.stream().limit(6).map(ApiMapper::toDto).toList(),
                incidentes.stream().limit(5).map(ApiMapper::toDto).toList(),
                notificaciones.stream().limit(5).map(ApiMapper::toDto).toList()
        );
    }

    @GetMapping("/mapa-calor-zonas")
    public List<HeatmapZonaDto> mapaCalorZonas() {
        var zonas = zonaService.listarTodas();
        var incidentes = incidenteService.listarTodos();
        long total = incidentes.size();

        return zonas.stream().map(zona -> {
            long cantidad = incidentes.stream()
                    .filter(incidente -> incidente.getZona() != null && zona.getId().equals(incidente.getZona().getId()))
                    .count();
            double porcentaje = total == 0 ? 0 : Math.round((cantidad * 10000.0 / total)) / 100.0;
            return new HeatmapZonaDto(zona.getId(), zona.getNombre(), cantidad, porcentaje);
        }).toList();
    }

    private DashboardDto resumenGeneral() {
        return new DashboardDto(
                usuarioService.listarTodos().size(),
                zonaService.listarTodas().size(),
                turnoService.listarTodos().size(),
                incidenteService.listarTodos().size(),
                reasignacionService.listarTodas().size(),
                notificacionService.listarTodas().size()
        );
    }
}
