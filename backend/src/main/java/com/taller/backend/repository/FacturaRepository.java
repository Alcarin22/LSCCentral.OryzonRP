package com.taller.backend.repository;

import com.taller.backend.entity.Factura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FacturaRepository extends JpaRepository<Factura, Long>, JpaSpecificationExecutor<Factura> {

    List<Factura> findByIdEmpleadoAndFechaBetweenOrderByFechaAsc(
            Long idEmpleado,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<Factura> findAllByIdEmpleado(Long idEmpleado);

    Long countByIdEmpleadoAndFechaBetween(
            Long idEmpleado,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    @Query("""
        SELECT COALESCE(SUM(f.total), 0)
        FROM Factura f
        WHERE f.idEmpleado = :idEmpleado
          AND f.fecha BETWEEN :inicio AND :fin
    """)
    Long sumTotalByIdEmpleadoAndFechaBetween(
            @Param("idEmpleado") Long idEmpleado,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );
}