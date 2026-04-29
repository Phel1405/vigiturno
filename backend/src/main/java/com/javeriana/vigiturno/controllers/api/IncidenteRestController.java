package com.javeriana.vigiturno.controllers.api;

import com.javeriana.vigiturno.dtos.api.ApiDtos.IncidenteDto;
import com.javeriana.vigiturno.dtos.api.ApiDtos.IncidenteRequest;
import com.javeriana.vigiturno.dtos.api.ApiMapper;
import com.javeriana.vigiturno.exceptions.ResourceNotFoundException;
import com.javeriana.vigiturno.models.entities.Incidente;
import com.javeriana.vigiturno.services.IncidenteService;
import com.javeriana.vigiturno.services.TurnoService;
import com.javeriana.vigiturno.services.ZonaService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidentes")
public class IncidenteRestController {

    private final IncidenteService incidenteService;
    private final TurnoService turnoService;
    private final ZonaService zonaService;

    public IncidenteRestController(IncidenteService incidenteService, TurnoService turnoService, ZonaService zonaService) {
        this.incidenteService = incidenteService;
        this.turnoService = turnoService;
        this.zonaService = zonaService;
    }

    @GetMapping
    public List<IncidenteDto> listar(@RequestParam(value = "usuarioId", required = false) Long usuarioId) {
        var incidentes = usuarioId == null ? incidenteService.listarTodos() : incidenteService.listarPorUsuarioId(usuarioId);
        return incidentes.stream().map(ApiMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public IncidenteDto buscar(@PathVariable Long id) {
        return incidenteService.buscarPorId(id).map(ApiMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente no encontrado con id: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidenteDto crear(@Valid @RequestBody IncidenteRequest request) {
        Incidente incidente = new Incidente();
        copiar(request, incidente);
        return ApiMapper.toDto(incidenteService.guardar(incidente));
    }

    @PutMapping("/{id}")
    public IncidenteDto actualizar(@PathVariable Long id, @Valid @RequestBody IncidenteRequest request) {
        Incidente incidente = incidenteService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente no encontrado con id: " + id));
        copiar(request, incidente);
        return ApiMapper.toDto(incidenteService.guardar(incidente));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        incidenteService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente no encontrado con id: " + id));
        incidenteService.eliminarPorId(id);
    }

    private void copiar(IncidenteRequest request, Incidente incidente) {
        incidente.setTipo(request.tipo());
        incidente.setSeveridad(request.severidad());
        incidente.setDescripcion(request.descripcion());
        incidente.setFechaHora(request.fechaHora() != null ? request.fechaHora() : LocalDateTime.now());
        incidente.setNombreEstudiante(request.nombreEstudiante());
        incidente.setCursoEstudiante(request.cursoEstudiante());

        if (request.turnoId() != null) {
            var turno = turnoService.buscarPorId(request.turnoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + request.turnoId()));
            incidente.setTurno(turno);
            incidente.setZona(turno.getZona());
        } else {
            incidente.setTurno(null);
            incidente.setZona(zonaService.buscarPorId(request.zonaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con id: " + request.zonaId())));
        }
    }
}
