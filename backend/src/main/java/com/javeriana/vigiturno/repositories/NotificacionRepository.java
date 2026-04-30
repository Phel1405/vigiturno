package com.javeriana.vigiturno.repositories;

import com.javeriana.vigiturno.models.entities.Notificacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId);
}