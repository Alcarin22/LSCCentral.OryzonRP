package com.taller.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taller.backend.entity.FullTuning;

public interface FullTuningRepository extends JpaRepository<FullTuning, Long> {
}