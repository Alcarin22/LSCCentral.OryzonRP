package com.taller.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.taller.backend.entity.Empleado;
import com.taller.backend.service.EmpleadoService;

@RestController
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @GetMapping("/api/empleados/{discordId}")
    public ResponseEntity<Empleado> getEmpleado(@PathVariable String discordId) {
        return empleadoService.getByDiscordId(discordId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}