package com.taller.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taller.backend.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
}