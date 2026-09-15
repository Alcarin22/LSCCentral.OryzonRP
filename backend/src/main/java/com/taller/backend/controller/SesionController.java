package com.taller.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.taller.backend.dto.SesionEmpleadoResponse;
import com.taller.backend.service.EmpleadoSesionService;

@RestController
public class SesionController {

    private final EmpleadoSesionService empleadoSesionService;

    public SesionController(
            EmpleadoSesionService empleadoSesionService
    ) {
        this.empleadoSesionService = empleadoSesionService;
    }

    @GetMapping("/api/session/me")
    public SesionEmpleadoResponse obtenerMiSesion(
            Authentication authentication
    ) {

        if (
                authentication == null
                        || authentication.getAuthorities()
                                .stream()
                                .noneMatch(authority ->
                                        "ROLE_EMPLEADO".equals(
                                                authority.getAuthority()
                                        )
                                )
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

        return empleadoSesionService
                .obtenerEmpleadoSesion(
                        discordId
                );
    }

    /*
     * Endpoint antiguo.
     *
     * Lo conservamos temporalmente para que una diferencia de
     * despliegue entre Railway y Vercel no rompa la aplicación.
     *
     * Lo eliminaremos cuando comprobemos que /api/session/me
     * funciona correctamente en producción.
     */
    @GetMapping("/api/session/empleado/{discordId}")
    public SesionEmpleadoResponse obtenerEmpleadoSesion(
            @PathVariable String discordId
    ) {
        return empleadoSesionService
                .obtenerEmpleadoSesion(
                        discordId
                );
    }
}