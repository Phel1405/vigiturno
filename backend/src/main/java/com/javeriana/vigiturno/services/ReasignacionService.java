package com.javeriana.vigiturno.services;

import com.javeriana.vigiturno.models.entities.Reasignacion;
import com.javeriana.vigiturno.repositories.ReasignacionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ReasignacionService {

    private final ReasignacionRepository reasignacionRepository;

    public ReasignacionService(ReasignacionRepository reasignacionRepository) {
        this.reasignacionRepository = reasignacionRepository;
    }

    public List<Reasignacion> listarTodas() {
        return reasignacionRepository.findAll();
    }

    public Optional<Reasignacion> buscarPorId(Long id) {
        return reasignacionRepository.findById(id);
    }

    public Reasignacion guardar(Reasignacion reasignacion) {
        return reasignacionRepository.save(reasignacion);
    }

    public void eliminarPorId(Long id) {
        reasignacionRepository.deleteById(id);
    }
}