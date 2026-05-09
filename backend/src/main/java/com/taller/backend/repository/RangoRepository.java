package com.taller.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taller.backend.entity.Rango;

public interface RangoRepository extends JpaRepository<Rango, Long> {

    List<Rango> findByNombreIn(List<String> nombres);

    Optional<Rango> findByNombre(String nombre);

    List<Rango> findAllByOrderByNivelAscNombreAsc();
}