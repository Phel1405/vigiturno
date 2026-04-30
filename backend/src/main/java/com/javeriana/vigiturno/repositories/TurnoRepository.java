package com.javeriana.vigiturno.repositories;

import com.javeriana.vigiturno.models.entities.Turno;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findByUsuarioIdOrderByFechaAscHoraInicioAsc(Long usuarioId);

    List<Turno> findByFechaOrderByHoraInicioAsc(LocalDate fecha);

    List<Turno> findByFechaBetweenOrderByFechaAscHoraInicioAsc(LocalDate desde, LocalDate hasta);

    List<Turno> findByUsuarioIdAndFechaAndHoraInicioLessThanAndHoraFinGreaterThan(
            Long usuarioId,
            LocalDate fecha,
            LocalTime horaFinNuevoTurno,
            LocalTime horaInicioNuevoTurno
    );
}
