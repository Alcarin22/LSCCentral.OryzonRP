package com.taller.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taller.backend.entity.Prima;

public interface PrimaRepository extends JpaRepository<Prima, Long> {

    Optional<Prima> findByEmpleadoIdAndSemana(Long empleadoId, Integer semana);

    List<Prima> findByEmpleadoIdOrderBySemanaDesc(Long empleadoId);

    List<Prima> findBySemanaOrderByEmpleadoNombreAsc(Integer semana);

    List<Prima> findByFechaInicioAndFechaFinOrderByEmpleadoNombreAsc(LocalDate fechaInicio, LocalDate fechaFin);

    List<Prima> findAllByOrderBySemanaDescEmpleadoNombreAsc();
} 