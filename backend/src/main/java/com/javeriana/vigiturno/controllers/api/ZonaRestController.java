package com.javeriana.vigiturno.controllers.api;

import com.javeriana.vigiturno.dtos.api.ApiDtos.ZonaDto;
import com.javeriana.vigiturno.dtos.api.ApiDtos.ZonaRequest;
import com.javeriana.vigiturno.dtos.api.ApiMapper;
import com.javeriana.vigiturno.exceptions.ResourceNotFoundException;
import com.javeriana.vigiturno.models.entities.Zona;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/zonas")
public class ZonaRestController {

    private final ZonaService zonaService;

    public ZonaRestController(ZonaService zonaService) {
        this.zonaService = zonaService;
    }

    @GetMapping
    public List<ZonaDto> listar() {
        return zonaService.listarTodas().stream().map(ApiMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ZonaDto buscar(@PathVariable Long id) {
        return zonaService.buscarPorId(id).map(ApiMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con id: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ZonaDto crear(@Valid @RequestBody ZonaRequest request) {
        Zona zona = new Zona();
        copiar(request, zona);
        return ApiMapper.toDto(zonaService.guardar(zona));
    }

    @PutMapping("/{id}")
    public ZonaDto actualizar(@PathVariable Long id, @Valid @RequestBody ZonaRequest request) {
        Zona zona = zonaService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con id: " + id));
        copiar(request, zona);
        return ApiMapper.toDto(zonaService.guardar(zona));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        zonaService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con id: " + id));
        zonaService.eliminarPorId(id);
    }

    private void copiar(ZonaRequest request, Zona zona) {
        zona.setNombre(request.nombre());
        zona.setDescripcion(request.descripcion());
        zona.setCapacidadMaxima(request.capacidadMaxima());
        zona.setActiva(request.activa() != null ? request.activa() : Boolean.TRUE);
    }
}
