package com.javeriana.vigiturno.repositories;

import com.javeriana.vigiturno.models.entities.Incidente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidenteRepository extends JpaRepository<Incidente, Long> {

    List<Incidente> findByTurnoUsuarioIdOrderByFechaHoraDesc(Long usuarioId);
}