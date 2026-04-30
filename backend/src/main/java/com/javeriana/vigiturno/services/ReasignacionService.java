package com.javeriana.vigiturno.services;

import com.javeriana.vigiturno.exceptions.BusinessException;
import com.javeriana.vigiturno.models.entities.Reasignacion;
import com.javeriana.vigiturno.models.entities.Turno;
import com.javeriana.vigiturno.models.enums.EstadoReasignacion;
import com.javeriana.vigiturno.models.enums.EstadoTurno;
import com.javeriana.vigiturno.repositories.ReasignacionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReasignacionService {

    private final ReasignacionRepository reasignacionRepository;
    private final TurnoService turnoService;

    public ReasignacionService(ReasignacionRepository reasignacionRepository, TurnoService turnoService) {
        this.reasignacionRepository = reasignacionRepository;
        this.turnoService = turnoService;
    }

    public List<Reasignacion> listarTodas() {
        return reasignacionRepository.findAll();
    }

    public List<Reasignacion> listarPendientesVigentes() {
        return reasignacionRepository.findAll().stream()
                .filter(item -> item.getEstado() == EstadoReasignacion.PENDIENTE)
                .filter(item -> item.getTurno() != null && turnoService.puedeReasignarse(item.getTurno()))
                .toList();
    }

    public Optional<Reasignacion> buscarPorId(Long id) {
        return reasignacionRepository.findById(id);
    }

    @Transactional
    public Reasignacion guardar(Reasignacion reasignacion) {
        validarReasignacion(reasignacion);

        if (reasignacion.getFechaHoraSolicitud() == null) {
            reasignacion.setFechaHoraSolicitud(LocalDateTime.now());
        }
        if (reasignacion.getEstado() == null) {
            reasignacion.setEstado(EstadoReasignacion.PENDIENTE);
        }
        if (reasignacion.getEstado() != EstadoReasignacion.PENDIENTE && reasignacion.getFechaHoraRespuesta() == null) {
            reasignacion.setFechaHoraRespuesta(LocalDateTime.now());
        }

        Reasignacion guardada = reasignacionRepository.save(reasignacion);
        aplicarEfectoEnTurnoSiCorresponde(guardada);
        return guardada;
    }

    public void eliminarPorId(Long id) {
        reasignacionRepository.deleteById(id);
    }

    private void validarReasignacion(Reasignacion reasignacion) {
        Turno turno = reasignacion.getTurno();
        if (turno == null) {
            throw new BusinessException("La reasignación debe estar asociada a un turno.");
        }
        if (!turnoService.puedeReasignarse(turno)) {
            throw new BusinessException("No se puede reasignar un turno que ya finalizó o fue cancelado.");
        }
        if (reasignacion.getDocenteOriginal() == null) {
            throw new BusinessException("La reasignación debe tener docente original.");
        }
        if (turno.getUsuario() != null && !turno.getUsuario().getId().equals(reasignacion.getDocenteOriginal().getId())) {
            throw new BusinessException("El docente original debe coincidir con el docente asignado al turno.");
        }
        if (reasignacion.getDocenteReemplazo() != null
                && reasignacion.getDocenteOriginal().getId().equals(reasignacion.getDocenteReemplazo().getId())) {
            throw new BusinessException("El docente de reemplazo debe ser diferente al docente original.");
        }
        if (reasignacion.getDocenteReemplazo() != null && !turnoService.estaDisponible(reasignacion.getDocenteReemplazo(), turno)) {
            throw new BusinessException("El docente de reemplazo no está disponible en este horario.");
        }
    }

    private void aplicarEfectoEnTurnoSiCorresponde(Reasignacion reasignacion) {
        if (reasignacion.getEstado() == EstadoReasignacion.ACEPTADA && reasignacion.getDocenteReemplazo() != null) {
            Turno turno = reasignacion.getTurno();
            turno.setUsuario(reasignacion.getDocenteReemplazo());
            turno.setEstado(EstadoTurno.REASIGNADO);
            turnoService.guardar(turno);
        }
    }
}
