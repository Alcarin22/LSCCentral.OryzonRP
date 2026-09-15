package com.taller.backend.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.taller.backend.dto.FichajeListadoResponse;
import com.taller.backend.dto.FichajeResponse;
import com.taller.backend.dto.FichajeToggleRequest;
import com.taller.backend.service.FichajeService;

@RestController
@RequestMapping("/api/fichajes")
public class FichajeController {

    private final FichajeService fichajeService;

    public FichajeController(
            FichajeService fichajeService
    ) {
        this.fichajeService = fichajeService;
    }

    @PostMapping("/toggle")
    public FichajeResponse toggleFichaje(
            Authentication authentication,
            @RequestBody(required = false) FichajeToggleRequest request
    ) {

        String discordId =
                obtenerDiscordIdAutenticado(
                        authentication
                );

        Boolean fichajeSeguridad =
                request != null
                        ? request.getFichajeSeguridad()
                        : false;

        return fichajeService.toggleFichaje(
                discordId,
                fichajeSeguridad
        );
    }

    @GetMapping("/estado")
    public FichajeResponse obtenerEstado(
            Authentication authentication
    ) {

        String discordId =
                obtenerDiscordIdAutenticado(
                        authentication
                );

        return fichajeService.obtenerEstadoFichaje(
                discordId
        );
    }

    @GetMapping
    public List<FichajeListadoResponse> listarFichajes(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin
    ) {

        LocalDateTime inicio = null;
        LocalDateTime fin = null;

        if (
                fechaInicio != null
                        && !fechaInicio.isBlank()
                        && fechaFin != null
                        && !fechaFin.isBlank()
        ) {

            inicio =
                    LocalDate
                            .parse(fechaInicio)
                            .atStartOfDay();

            fin =
                    LocalDate
                            .parse(fechaFin)
                            .atTime(
                                    LocalTime.MAX
                            );
        }

        return fichajeService.listarFichajes(
                inicio,
                fin
        );
    }

    private String obtenerDiscordIdAutenticado(
            Authentication authentication
    ) {

        if (
                authentication == null
                        || !authentication.isAuthenticated()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Sesión no autenticada"
            );
        }

        String discordId =
                authentication.getName();

        if (
                discordId == null
                        || discordId.isBlank()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no identificado"
            );
        }

        return discordId;
    }
}