package com.javeriana.vigiturno.controllers.api;

import com.javeriana.vigiturno.dtos.api.ApiDtos.TurnoDto;
import com.javeriana.vigiturno.dtos.api.ApiDtos.TurnoRequest;
import com.javeriana.vigiturno.dtos.api.ApiMapper;
import com.javeriana.vigiturno.exceptions.ResourceNotFoundException;
import com.javeriana.vigiturno.models.entities.Turno;
import com.javeriana.vigiturno.models.enums.EstadoTurno;
import com.javeriana.vigiturno.services.TurnoService;
import com.javeriana.vigiturno.services.UsuarioService;
import com.javeriana.vigiturno.services.ZonaService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/turnos")
public class TurnoRestController {

    private final TurnoService turnoService;
    private final UsuarioService usuarioService;
    private final ZonaService zonaService;

    public TurnoRestController(TurnoService turnoService, UsuarioService usuarioService, ZonaService zonaService) {
        this.turnoService = turnoService;
        this.usuarioService = usuarioService;
        this.zonaService = zonaService;
    }

    @GetMapping
    public List<TurnoDto> listar(@RequestParam(value = "usuarioId", required = false) Long usuarioId) {
        var turnos = usuarioId == null ? turnoService.listarTodos() : turnoService.listarPorUsuarioId(usuarioId);
        return turnos.stream().map(ApiMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public TurnoDto buscar(@PathVariable Long id) {
        return turnoService.buscarPorId(id).map(ApiMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TurnoDto crear(@Valid @RequestBody TurnoRequest request) {
        Turno turno = new Turno();
        copiar(request, turno);
        return ApiMapper.toDto(turnoService.guardar(turno));
    }

    @PutMapping("/{id}")
    public TurnoDto actualizar(@PathVariable Long id, @Valid @RequestBody TurnoRequest request) {
        Turno turno = turnoService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + id));
        copiar(request, turno);
        return ApiMapper.toDto(turnoService.guardar(turno));
    }

    @PostMapping("/{id}/check-in")
    public TurnoDto checkIn(@PathVariable Long id) {
        Turno turno = turnoService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + id));
        turno.setEstado(EstadoTurno.EN_CURSO);
        return ApiMapper.toDto(turnoService.guardar(turno));
    }

    @PostMapping("/{id}/cerrar")
    public TurnoDto cerrar(@PathVariable Long id) {
        Turno turno = turnoService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + id));
        turno.setEstado(EstadoTurno.FINALIZADO);
        return ApiMapper.toDto(turnoService.guardar(turno));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        turnoService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + id));
        turnoService.eliminarPorId(id);
    }

    private void copiar(TurnoRequest request, Turno turno) {
        turno.setFecha(request.fecha());
        turno.setHoraInicio(request.horaInicio());
        turno.setHoraFin(request.horaFin());
        turno.setEstado(request.estado() != null ? request.estado() : EstadoTurno.PENDIENTE);
        turno.setUsuario(usuarioService.buscarPorId(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + request.usuarioId())));
        turno.setZona(zonaService.buscarPorId(request.zonaId())
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con id: " + request.zonaId())));
    }
}
