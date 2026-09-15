package com.taller.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.taller.backend.dto.DashboardHoyResponse;
import com.taller.backend.dto.DashboardMesResponse;
import com.taller.backend.dto.DashboardSemanaResponse;
import com.taller.backend.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    /*
     * Endpoints nuevos.
     *
     * La identidad del empleado procede exclusivamente del JWT.
     */

    @GetMapping("/hoy")
    public DashboardHoyResponse getResumenHoy(
            Authentication authentication
    ) {

        String discordId =
                obtenerDiscordIdAutenticado(
                        authentication
                );

        return dashboardService.getResumenHoy(
                discordId
        );
    }

    @GetMapping("/semana")
    public DashboardSemanaResponse getResumenSemana(
            Authentication authentication
    ) {

        String discordId =
                obtenerDiscordIdAutenticado(
                        authentication
                );

        return dashboardService.getResumenSemana(
                discordId
        );
    }

    @GetMapping("/mes")
    public DashboardMesResponse getResumenMes(
            Authentication authentication
    ) {

        String discordId =
                obtenerDiscordIdAutenticado(
                        authentication
                );

        return dashboardService.getResumenMes(
                discordId
        );
    }

    /*
     * Endpoints legacy temporales.
     *
     * Se mantienen únicamente para que el frontend desplegado actualmente
     * continúe funcionando durante la transición.
     *
     * El discordId recibido en la URL se ignora completamente.
     */

    @GetMapping("/hoy/{discordId}")
    public DashboardHoyResponse getResumenHoyLegacy(
            Authentication authentication,
            @PathVariable String discordId
    ) {

        return getResumenHoy(
                authentication
        );
    }

    @GetMapping("/semana/{discordId}")
    public DashboardSemanaResponse getResumenSemanaLegacy(
            Authentication authentication,
            @PathVariable String discordId
    ) {

        return getResumenSemana(
                authentication
        );
    }

    @GetMapping("/mes/{discordId}")
    public DashboardMesResponse getResumenMesLegacy(
            Authentication authentication,
            @PathVariable String discordId
    ) {

        return getResumenMes(
                authentication
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