package com.javeriana.vigiturno.controllers.api;

import com.javeriana.vigiturno.dtos.api.ApiDtos.ReasignacionDto;
import com.javeriana.vigiturno.dtos.api.ApiDtos.ReasignacionRequest;
import com.javeriana.vigiturno.dtos.api.ApiMapper;
import com.javeriana.vigiturno.exceptions.ResourceNotFoundException;
import com.javeriana.vigiturno.models.entities.Reasignacion;
import com.javeriana.vigiturno.models.enums.EstadoReasignacion;
import com.javeriana.vigiturno.services.ReasignacionService;
import com.javeriana.vigiturno.services.TurnoService;
import com.javeriana.vigiturno.services.UsuarioService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reasignaciones")
public class ReasignacionRestController {

    private final ReasignacionService reasignacionService;
    private final TurnoService turnoService;
    private final UsuarioService usuarioService;

    public ReasignacionRestController(ReasignacionService reasignacionService, TurnoService turnoService, UsuarioService usuarioService) {
        this.reasignacionService = reasignacionService;
        this.turnoService = turnoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<ReasignacionDto> listar() {
        return reasignacionService.listarTodas().stream().map(ApiMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ReasignacionDto buscar(@PathVariable Long id) {
        return reasignacionService.buscarPorId(id).map(ApiMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Reasignación no encontrada con id: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReasignacionDto crear(@Valid @RequestBody ReasignacionRequest request) {
        Reasignacion reasignacion = new Reasignacion();
        copiar(request, reasignacion);
        return ApiMapper.toDto(reasignacionService.guardar(reasignacion));
    }

    @PutMapping("/{id}")
    public ReasignacionDto actualizar(@PathVariable Long id, @Valid @RequestBody ReasignacionRequest request) {
        Reasignacion reasignacion = reasignacionService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reasignación no encontrada con id: " + id));
        copiar(request, reasignacion);
        return ApiMapper.toDto(reasignacionService.guardar(reasignacion));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        reasignacionService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reasignación no encontrada con id: " + id));
        reasignacionService.eliminarPorId(id);
    }

    private void copiar(ReasignacionRequest request, Reasignacion reasignacion) {
        reasignacion.setMotivo(request.motivo());
        reasignacion.setFechaHoraSolicitud(request.fechaHoraSolicitud() != null ? request.fechaHoraSolicitud() : LocalDateTime.now());
        reasignacion.setFechaHoraRespuesta(request.fechaHoraRespuesta());
        reasignacion.setEstado(request.estado() != null ? request.estado() : EstadoReasignacion.PENDIENTE);
        reasignacion.setTurno(turnoService.buscarPorId(request.turnoId())
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + request.turnoId())));
        reasignacion.setDocenteOriginal(usuarioService.buscarPorId(request.docenteOriginalId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + request.docenteOriginalId())));
        if (request.docenteReemplazoId() != null) {
            reasignacion.setDocenteReemplazo(usuarioService.buscarPorId(request.docenteReemplazoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + request.docenteReemplazoId())));
        } else {
            reasignacion.setDocenteReemplazo(null);
        }
    }
}
