package com.taller.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taller.backend.entity.Reparacion;

public interface ReparacionRepository extends JpaRepository<Reparacion, Long> {
    Optional<Reparacion> findByTipoIgnoreCase(String tipo);
}