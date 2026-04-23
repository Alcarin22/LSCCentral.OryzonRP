package com.taller.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taller.backend.entity.Tasacion;

public interface TasacionRepository extends JpaRepository<Tasacion, Long> {
}