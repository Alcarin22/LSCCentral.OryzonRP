package com.taller.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taller.backend.entity.Convenio;

public interface ConvenioRepository extends JpaRepository<Convenio, Long> {

    List<Convenio> findAllByOrderByCategoriaAscLocalAsc();
}
