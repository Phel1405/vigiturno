package com.javeriana.vigiturno.services;

import com.javeriana.vigiturno.models.entities.Incidente;
import com.javeriana.vigiturno.repositories.IncidenteRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class IncidenteService {

    private final IncidenteRepository incidenteRepository;

    public IncidenteService(IncidenteRepository incidenteRepository) {
        this.incidenteRepository = incidenteRepository;
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
        return incidenteRepository.save(incidente);
    }

    public void eliminarPorId(Long id) {
        incidenteRepository.deleteById(id);
    }
}