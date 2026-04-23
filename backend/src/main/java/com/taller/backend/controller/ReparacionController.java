package com.taller.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taller.backend.entity.Reparacion;
import com.taller.backend.repository.ReparacionRepository;

@RestController
@RequestMapping("/api/reparaciones")
public class ReparacionController {

    private final ReparacionRepository reparacionRepository;

    public ReparacionController(ReparacionRepository reparacionRepository) {
        this.reparacionRepository = reparacionRepository;
    }

    @GetMapping
    public List<Reparacion> listarReparaciones() {
        return reparacionRepository.findAll();
    }
}