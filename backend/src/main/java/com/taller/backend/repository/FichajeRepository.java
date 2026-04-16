package com.taller.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taller.backend.entity.Fichaje;

public interface FichajeRepository extends JpaRepository<Fichaje, Long> {

    Optional<Fichaje> findFirstByEmpleadoIdAndFechaHoraSalidaIsNullOrderByFechaHoraEntradaDesc(Long empleadoId);

    List<Fichaje> findByEmpleadoIdAndFechaHoraEntradaBetween(Long empleadoId, LocalDateTime inicio, LocalDateTime fin);

    List<Fichaje> findAllByEmpleadoId(Long empleadoId);
}