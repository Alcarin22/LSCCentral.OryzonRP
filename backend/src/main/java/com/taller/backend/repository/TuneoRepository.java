package com.taller.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taller.backend.entity.Tuneo;

public interface TuneoRepository extends JpaRepository<Tuneo, Long> {
}