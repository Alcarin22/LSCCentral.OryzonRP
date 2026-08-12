package com.taller.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taller.backend.entity.FacturaItem;

public interface FacturaItemRepository extends JpaRepository<FacturaItem, Long> {

    List<FacturaItem> findByFacturaIdOrderByIdAsc(Long facturaId);

    void deleteByFacturaId(Long facturaId);
}
