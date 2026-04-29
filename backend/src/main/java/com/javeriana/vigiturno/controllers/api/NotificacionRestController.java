package com.javeriana.vigiturno.controllers.api;

import com.javeriana.vigiturno.dtos.api.ApiDtos.NotificacionDto;
import com.javeriana.vigiturno.dtos.api.ApiDtos.NotificacionRequest;
import com.javeriana.vigiturno.dtos.api.ApiMapper;
import com.javeriana.vigiturno.exceptions.ResourceNotFoundException;
import com.javeriana.vigiturno.models.entities.Notificacion;
import com.javeriana.vigiturno.services.NotificacionService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionRestController {

    private final NotificacionService notificacionService;
    private final UsuarioService usuarioService;

    public NotificacionRestController(NotificacionService notificacionService, UsuarioService usuarioService) {
        this.notificacionService = notificacionService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<NotificacionDto> listar(@RequestParam(value = "usuarioId", required = false) Long usuarioId) {
        var notificaciones = usuarioId == null ? notificacionService.listarTodas() : notificacionService.listarPorUsuarioId(usuarioId);
        return notificaciones.stream().map(ApiMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public NotificacionDto buscar(@PathVariable Long id) {
        return notificacionService.buscarPorId(id).map(ApiMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con id: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificacionDto crear(@Valid @RequestBody NotificacionRequest request) {
        Notificacion notificacion = new Notificacion();
        copiar(request, notificacion);
        return ApiMapper.toDto(notificacionService.guardar(notificacion));
    }

    @PutMapping("/{id}")
    public NotificacionDto actualizar(@PathVariable Long id, @Valid @RequestBody NotificacionRequest request) {
        Notificacion notificacion = notificacionService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con id: " + id));
        copiar(request, notificacion);
        return ApiMapper.toDto(notificacionService.guardar(notificacion));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        notificacionService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con id: " + id));
        notificacionService.eliminarPorId(id);
    }

    private void copiar(NotificacionRequest request, Notificacion notificacion) {
        notificacion.setTipo(request.tipo());
        notificacion.setMensaje(request.mensaje());
        notificacion.setFechaHora(request.fechaHora() != null ? request.fechaHora() : LocalDateTime.now());
        notificacion.setLeida(request.leida() != null ? request.leida() : Boolean.FALSE);
        notificacion.setUsuario(usuarioService.buscarPorId(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + request.usuarioId())));
    }
}
