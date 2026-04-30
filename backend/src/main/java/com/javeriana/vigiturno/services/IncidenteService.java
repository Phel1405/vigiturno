package com.javeriana.vigiturno.services;

import com.javeriana.vigiturno.models.entities.Incidente;
import com.javeriana.vigiturno.repositories.IncidenteRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class IncidenteService {

    private final IncidenteRepository incidenteRepository;
    private final TurnoService turnoService;

    public IncidenteService(IncidenteRepository incidenteRepository, TurnoService turnoService) {
        this.incidenteRepository = incidenteRepository;
        this.turnoService = turnoService;
    }

    public List<Incidente> listarTodos() {
        return incidenteRepository.findAll();
    }

    public List<Incidente> listarPorUsuarioId(Long usuarioId) {
        return incidenteRepository.findByTurnoUsuarioIdOrderByFechaHoraDesc(usuarioId);
    }

    public Optional<Incidente> buscarPorId(Long id) {
        return incidenteRepository.findById(id);
    }

    public Incidente guardar(Incidente incidente) {
        if (incidente.getTurno() != null) {
            // Validar que la hora del incidente corresponde a la ventana horaria del turno
            // y que el turno está activo (o al menos no finalizado/cancelado/futuro)
            boolean enVentana = turnoService.estaEnVentanaDelTurno(incidente.getTurno());
            if (!enVentana) {
                throw new com.javeriana.vigiturno.exceptions.BusinessException("La hora del incidente no corresponde a la ventana horaria del turno o el turno no está activo.");
            }
        } else {
            // Podríamos lanzar error si exigimos turno siempre, pero como Admin puede crear incidentes sin turno (por la zona)
            // se deja opcional a menos que la regla sea estricta. El front de Docente ya asocia turno obligatorio.
        }
        return incidenteRepository.save(incidente);
    }

    public void eliminarPorId(Long id) {
        incidenteRepository.deleteById(id);
    }
}