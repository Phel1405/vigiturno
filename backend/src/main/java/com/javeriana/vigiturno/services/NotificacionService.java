package com.javeriana.vigiturno.services;

import com.javeriana.vigiturno.models.entities.Notificacion;
import com.javeriana.vigiturno.repositories.NotificacionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    public List<Notificacion> listarTodas() {
        return notificacionRepository.findAll();
    }

    public List<Notificacion> listarPorUsuarioId(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaHoraDesc(usuarioId);
    }

    public Optional<Notificacion> buscarPorId(Long id) {
        return notificacionRepository.findById(id);
    }

    public Notificacion guardar(Notificacion notificacion) {
        return notificacionRepository.save(notificacion);
    }

    public void eliminarPorId(Long id) {
    notificacionRepository.deleteById(id);
    }
}