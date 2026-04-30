package com.javeriana.vigiturno.services;

import com.javeriana.vigiturno.models.entities.Zona;
import com.javeriana.vigiturno.repositories.ZonaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ZonaService {

    private final ZonaRepository zonaRepository;

    public ZonaService(ZonaRepository zonaRepository) {
        this.zonaRepository = zonaRepository;
    }

    public List<Zona> listarTodas() {
        return zonaRepository.findAll();
    }

    public Optional<Zona> buscarPorId(Long id) {
        return zonaRepository.findById(id);
    }

    public Zona guardar(Zona zona) {
        return zonaRepository.save(zona);
    }

    public void eliminarPorId(Long id) {
        zonaRepository.deleteById(id);
    }
}