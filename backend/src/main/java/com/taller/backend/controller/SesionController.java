package com.taller.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.taller.backend.dto.SesionEmpleadoResponse;
import com.taller.backend.service.EmpleadoSesionService;

@RestController
public class SesionController {

    private final EmpleadoSesionService empleadoSesionService;

    public SesionController(EmpleadoSesionService empleadoSesionService) {
        this.empleadoSesionService = empleadoSesionService;
    }

    @GetMapping("/api/session/empleado/{discordId}")
    public SesionEmpleadoResponse obtenerEmpleadoSesion(@PathVariable String discordId) {
        return empleadoSesionService.obtenerEmpleadoSesion(discordId);
    }
}