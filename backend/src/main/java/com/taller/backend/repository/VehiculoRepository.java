package com.taller.backend.repository;

import com.taller.backend.entity.Vehiculo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    List<Vehiculo> findAllByOrderByMarcaAscModeloAsc();

    List<Vehiculo> findByActivoTrueOrderByMarcaAscModeloAsc();

    boolean existsByMarcaIgnoreCaseAndModeloIgnoreCase(
            String marca,
            String modelo
    );
}