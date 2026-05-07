package com.taller.backend.repository;

import com.taller.backend.entity.Factura;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FacturaRepository extends JpaRepository<Factura, Long> {

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

    @Query("""
        SELECT f
        FROM Factura f
        WHERE (:idEmpleado IS NULL OR f.idEmpleado = :idEmpleado)
          AND (:tipo IS NULL OR :tipo = '' OR f.tipo = :tipo)
          AND (:inicio IS NULL OR f.fecha >= :inicio)
          AND (:fin IS NULL OR f.fecha <= :fin)
        ORDER BY f.fecha DESC
    """)
    Page<Factura> buscarFacturasFiltradasPaginadas(
            @Param("idEmpleado") Long idEmpleado,
            @Param("tipo") String tipo,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            Pageable pageable
    );

    @Query("""
        SELECT COALESCE(SUM(f.total), 0)
        FROM Factura f
        WHERE (:idEmpleado IS NULL OR f.idEmpleado = :idEmpleado)
          AND (:tipo IS NULL OR :tipo = '' OR f.tipo = :tipo)
          AND (:inicio IS NULL OR f.fecha >= :inicio)
          AND (:fin IS NULL OR f.fecha <= :fin)
    """)
    Long calcularTotalFacturadoFiltrado(
            @Param("idEmpleado") Long idEmpleado,
            @Param("tipo") String tipo,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );
}