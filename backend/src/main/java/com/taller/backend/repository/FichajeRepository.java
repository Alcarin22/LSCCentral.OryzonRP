package com.taller.backend.repository;

import com.taller.backend.entity.Fichaje;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FichajeRepository extends JpaRepository<Fichaje, Long> {

    Optional<Fichaje> findFirstByEmpleadoIdAndFechaHoraSalidaIsNullOrderByFechaHoraEntradaDesc(
            Long empleadoId
    );

    List<Fichaje> findByEmpleadoIdAndFechaHoraEntradaBetween(
            Long empleadoId,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<Fichaje> findByFechaHoraEntradaBetweenOrderByFechaHoraEntradaDesc(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<Fichaje> findAllByOrderByFechaHoraEntradaDesc();

    List<Fichaje> findAllByEmpleadoId(Long empleadoId);
}