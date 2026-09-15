package com.taller.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.taller.backend.dto.AdminPrimaPagadaRequest;
import com.taller.backend.dto.AdminPrimaResponse;
import com.taller.backend.dto.MisPrimasResponse;
import com.taller.backend.service.PrimasService;

@RestController
public class PrimasController {

    private final PrimasService primasService;

    public PrimasController(
            PrimasService primasService
    ) {
        this.primasService = primasService;
    }

    @GetMapping("/api/primas")
    public MisPrimasResponse getMisPrimas(
            Authentication authentication,
            @RequestParam(defaultValue = "0") Integer weekOffset
    ) {

        String discordId =
                obtenerDiscordIdAutenticado(
                        authentication
                );

        return primasService.getMisPrimas(
                discordId,
                weekOffset
        );
    }

    @GetMapping("/api/admin/primas")
    public List<AdminPrimaResponse> listarPrimasAdmin(
            @RequestParam(required = false) Integer semana
    ) {

        return primasService.listarPrimasAdmin(
                semana
        );
    }

    @PatchMapping("/api/admin/primas/{primaId}/pagada")
    public AdminPrimaResponse actualizarPagada(
            @PathVariable Long primaId,
            @RequestBody AdminPrimaPagadaRequest request
    ) {

        return primasService.actualizarPagada(
                primaId,
                request
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