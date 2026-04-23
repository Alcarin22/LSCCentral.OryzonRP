package com.taller.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taller.backend.entity.Reparacion;

public interface ReparacionRepository extends JpaRepository<Reparacion, Long> {
}