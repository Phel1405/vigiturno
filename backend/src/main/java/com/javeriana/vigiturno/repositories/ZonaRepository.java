package com.javeriana.vigiturno.repositories;

import com.javeriana.vigiturno.models.entities.Zona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZonaRepository extends JpaRepository<Zona, Long> {
}