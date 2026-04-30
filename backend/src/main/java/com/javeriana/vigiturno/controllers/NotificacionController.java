package com.javeriana.vigiturno.controllers;

import com.javeriana.vigiturno.exceptions.ResourceNotFoundException;
import com.javeriana.vigiturno.models.entities.Notificacion;
import com.javeriana.vigiturno.models.enums.TipoNotificacion;
import com.javeriana.vigiturno.services.NotificacionService;
import com.javeriana.vigiturno.services.UsuarioService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final UsuarioService usuarioService;

    public NotificacionController(NotificacionService notificacionService,
                                  UsuarioService usuarioService) {
        this.notificacionService = notificacionService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/notificaciones")
    public String listarNotificaciones(Model model) {
        model.addAttribute("titulo", "Notificaciones");
        model.addAttribute("notificaciones", notificacionService.listarTodas());
        return "notificaciones/lista";
    }

    @GetMapping("/notificaciones/nueva")
    public String mostrarFormularioNuevaNotificacion(Model model) {
        Notificacion notificacion = new Notificacion();
        notificacion.setFechaHora(LocalDateTime.now());
        notificacion.setLeida(false);

        model.addAttribute("titulo", "Nueva notificación");
        model.addAttribute("notificacion", notificacion);
        model.addAttribute("tipos", TipoNotificacion.values());
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("modoEdicion", false);
        return "notificaciones/formulario";
    }

    @PostMapping("/notificaciones/guardar")
    public String guardarNotificacion(Notificacion notificacion,
                                      @RequestParam("usuario") Long usuarioId) {

        notificacion.setUsuario(
                usuarioService.buscarPorId(usuarioId)
                        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId))
        );

        notificacionService.guardar(notificacion);
        return "redirect:/notificaciones";
    }

    @GetMapping("/notificaciones/editar/{id}")
    public String mostrarFormularioEditarNotificacion(@PathVariable Long id, Model model) {
        Notificacion notificacion = notificacionService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con id: " + id));

        model.addAttribute("titulo", "Editar notificación");
        model.addAttribute("notificacion", notificacion);
        model.addAttribute("tipos", TipoNotificacion.values());
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("modoEdicion", true);
        return "notificaciones/formulario";
    }

    @GetMapping("/notificaciones/eliminar/{id}")
    public String eliminarNotificacion(@PathVariable Long id) {
        notificacionService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con id: " + id));

        notificacionService.eliminarPorId(id);
        return "redirect:/notificaciones";
    }
}