package com.taller.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taller.backend.entity.TasacionPrecio;
import com.taller.backend.repository.TasacionPrecioRepository;

@RestController
@RequestMapping("/api/tasaciones")
public class TasacionPrecioController {

    private final TasacionPrecioRepository tasacionPrecioRepository;

    public TasacionPrecioController(TasacionPrecioRepository tasacionPrecioRepository) {
        this.tasacionPrecioRepository = tasacionPrecioRepository;
    }

    @GetMapping
    public List<TasacionPrecio> listarTasaciones() {
        return tasacionPrecioRepository.findAll();
    }
}