package com.javeriana.vigiturno.controllers.api;

import com.javeriana.vigiturno.dtos.api.ApiDtos.UsuarioDto;
import com.javeriana.vigiturno.dtos.api.ApiDtos.UsuarioRequest;
import com.javeriana.vigiturno.dtos.api.ApiMapper;
import com.javeriana.vigiturno.exceptions.ResourceNotFoundException;
import com.javeriana.vigiturno.models.entities.Usuario;
import com.javeriana.vigiturno.services.UsuarioService;
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
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    private final UsuarioService usuarioService;

    public UsuarioRestController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioDto> listar() {
        return usuarioService.listarTodos().stream().map(ApiMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public UsuarioDto buscar(@PathVariable Long id) {
        return usuarioService.buscarPorId(id).map(ApiMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto crear(@Valid @RequestBody UsuarioRequest request) {
        Usuario usuario = new Usuario();
        copiar(request, usuario);
        return ApiMapper.toDto(usuarioService.guardar(usuario));
    }

    @PutMapping("/{id}")
    public UsuarioDto actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        Usuario usuario = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        copiar(request, usuario);
        return ApiMapper.toDto(usuarioService.guardar(usuario));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        usuarioService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        usuarioService.eliminarPorId(id);
    }

    private void copiar(UsuarioRequest request, Usuario usuario) {
        usuario.setNombreCompleto(request.nombreCompleto());
        usuario.setCorreo(request.correo());
        usuario.setPassword(request.password() == null || request.password().isBlank() ? "123456" : request.password());
        usuario.setRol(request.rol());
        usuario.setActivo(request.activo() != null ? request.activo() : Boolean.TRUE);
    }
}
