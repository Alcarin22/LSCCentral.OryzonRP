package com.taller.backend.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taller.backend.dto.FichajeListadoResponse;
import com.taller.backend.dto.FichajeResponse;
import com.taller.backend.service.FichajeService;

@RestController
@RequestMapping("/api/fichajes")
public class FichajeController {

    private final FichajeService fichajeService;

    public FichajeController(FichajeService fichajeService) {
        this.fichajeService = fichajeService;
    }

    @PostMapping("/toggle/{discordId}")
    public FichajeResponse toggleFichaje(@PathVariable String discordId) {
        return fichajeService.toggleFichaje(discordId);
    }

    @GetMapping("/estado/{discordId}")
    public FichajeResponse obtenerEstado(@PathVariable String discordId) {
        return fichajeService.obtenerEstadoFichaje(discordId);
    }

    @GetMapping
    public List<FichajeListadoResponse> listarFichajes(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin
    ) {
        LocalDateTime inicio = null;
        LocalDateTime fin = null;

        if (fechaInicio != null && !fechaInicio.isBlank()
                && fechaFin != null && !fechaFin.isBlank()) {
            inicio = LocalDate.parse(fechaInicio).atStartOfDay();
            fin = LocalDate.parse(fechaFin).atTime(LocalTime.MAX);
        }

        return fichajeService.listarFichajes(inicio, fin);
    }
}