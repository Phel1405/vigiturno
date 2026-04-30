package com.javeriana.vigiturno.controllers.api;

import com.javeriana.vigiturno.dtos.api.ApiDtos.TurnoDto;
import com.javeriana.vigiturno.dtos.api.ApiDtos.TurnoRequest;
import com.javeriana.vigiturno.dtos.api.ApiDtos.UsuarioDto;
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
        return turnos.stream().map(turno -> ApiMapper.toDto(turno, turnoService)).toList();
    }

    @GetMapping("/hoy")
    public List<TurnoDto> listarHoy() {
        return turnoService.listarDeHoy().stream().map(turno -> ApiMapper.toDto(turno, turnoService)).toList();
    }

    @GetMapping("/proximos")
    public List<TurnoDto> listarProximos(@RequestParam(value = "dias", defaultValue = "7") int dias) {
        return turnoService.listarProximos(dias).stream().map(turno -> ApiMapper.toDto(turno, turnoService)).toList();
    }

    @GetMapping("/activos")
    public List<TurnoDto> listarActivosAhora() {
        return turnoService.listarActivosAhora().stream().map(turno -> ApiMapper.toDto(turno, turnoService)).toList();
    }

    @GetMapping("/sin-cobertura")
    public List<TurnoDto> listarSinCobertura() {
        return turnoService.listarSinCobertura().stream().map(turno -> ApiMapper.toDto(turno, turnoService)).toList();
    }

    @GetMapping("/reasignables")
    public List<TurnoDto> listarReasignables() {
        return turnoService.listarReasignables().stream().map(turno -> ApiMapper.toDto(turno, turnoService)).toList();
    }

    @GetMapping("/{id}")
    public TurnoDto buscar(@PathVariable Long id) {
        return turnoService.buscarPorId(id).map(turno -> ApiMapper.toDto(turno, turnoService))
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + id));
    }

    @GetMapping("/{id}/docentes-disponibles")
    public List<UsuarioDto> docentesDisponibles(@PathVariable Long id) {
        Turno turno = turnoService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + id));
        return turnoService.proponerDocentesDisponibles(turno).stream().map(ApiMapper::toDto).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TurnoDto crear(@Valid @RequestBody TurnoRequest request) {
        Turno turno = new Turno();
        copiar(request, turno);
        return ApiMapper.toDto(turnoService.guardar(turno), turnoService);
    }

    @PutMapping("/{id}")
    public TurnoDto actualizar(@PathVariable Long id, @Valid @RequestBody TurnoRequest request) {
        Turno turno = turnoService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + id));
        copiar(request, turno);
        return ApiMapper.toDto(turnoService.guardar(turno), turnoService);
    }

    @PostMapping("/{id}/check-in")
    public TurnoDto checkIn(@PathVariable Long id, @RequestParam("codigoPin") String codigoPin) {
        Turno turno = turnoService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + id));
        return ApiMapper.toDto(turnoService.registrarCheckIn(turno, codigoPin), turnoService);
    }

    @PostMapping("/{id}/cerrar")
    public TurnoDto cerrar(@PathVariable Long id, @RequestParam("calificacionLimpieza") Integer calificacionLimpieza) {
        Turno turno = turnoService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + id));
        return ApiMapper.toDto(turnoService.cerrarTurno(turno, calificacionLimpieza), turnoService);
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
