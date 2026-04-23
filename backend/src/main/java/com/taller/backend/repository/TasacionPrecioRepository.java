package com.taller.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taller.backend.entity.TasacionPrecio;

public interface TasacionPrecioRepository extends JpaRepository<TasacionPrecio, Long> {
}