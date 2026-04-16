package com.taller.backend.controller;

import org.springframework.web.bind.annotation.*;

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
}