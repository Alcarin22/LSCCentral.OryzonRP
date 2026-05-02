package com.taller.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taller.backend.entity.Empleado;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    Optional<Empleado> findByDiscordId(String discordId);

    List<Empleado> findAllByOrderByActivoDescNombreAsc();
}