package com.javeriana.vigiturno.controllers;

import com.javeriana.vigiturno.exceptions.ResourceNotFoundException;
import com.javeriana.vigiturno.models.entities.Usuario;
import com.javeriana.vigiturno.models.enums.RolNombre;
import com.javeriana.vigiturno.services.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("titulo", "Usuarios");
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/lista";
    }

    @GetMapping("/usuarios/nuevo")
    public String mostrarFormularioNuevoUsuario(Model model) {
        model.addAttribute("titulo", "Nuevo usuario");
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", RolNombre.values());
        model.addAttribute("modoEdicion", false);
        return "usuarios/formulario";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(Usuario usuario) {
        if (usuario.getActivo() == null) {
            usuario.setActivo(false);
        }
        usuarioService.guardar(usuario);
        return "redirect:/usuarios";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String mostrarFormularioEditarUsuario(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        model.addAttribute("titulo", "Editar usuario");
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", RolNombre.values());
        model.addAttribute("modoEdicion", true);
        return "usuarios/formulario";
    }

    @GetMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        usuarioService.eliminarPorId(id);
        return "redirect:/usuarios";
    }
}