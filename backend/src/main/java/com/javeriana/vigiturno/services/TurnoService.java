package com.javeriana.vigiturno.services;

import com.javeriana.vigiturno.models.entities.Turno;
import com.javeriana.vigiturno.repositories.TurnoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TurnoService {

    private final TurnoRepository turnoRepository;

    public TurnoService(TurnoRepository turnoRepository) {
        this.turnoRepository = turnoRepository;
    }

    public List<Turno> listarTodos() {
        return turnoRepository.findAll();
    }

    public List<Turno> listarPorUsuarioId(Long usuarioId) {
        return turnoRepository.findByUsuarioIdOrderByFechaAscHoraInicioAsc(usuarioId);
    }

    public Optional<Turno> buscarPorId(Long id) {
        return turnoRepository.findById(id);
    }

    public Turno guardar(Turno turno) {
        return turnoRepository.save(turno);
    }

    public void eliminarPorId(Long id) {
        turnoRepository.deleteById(id);
    }
}