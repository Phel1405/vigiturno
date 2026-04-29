package com.javeriana.vigiturno.repositories;

import com.javeriana.vigiturno.models.entities.Turno;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findByUsuarioIdOrderByFechaAscHoraInicioAsc(Long usuarioId);
}