package com.taller.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.taller.backend.dto.EmpleadoListadoResponse;
import com.taller.backend.entity.Empleado;
import com.taller.backend.repository.EmpleadoRepository;
import com.taller.backend.service.EmpleadoService;

@RestController
public class EmpleadoController {

    private final EmpleadoService empleadoService;
    private final EmpleadoRepository empleadoRepository;

    public EmpleadoController(
            EmpleadoService empleadoService,
            EmpleadoRepository empleadoRepository
    ) {
        this.empleadoService = empleadoService;
        this.empleadoRepository = empleadoRepository;
    }

    @GetMapping("/api/empleados")
    public List<EmpleadoListadoResponse> listarEmpleados() {

        return empleadoRepository
                .findAllByOrderByActivoDescNombreAsc()
                .stream()
                .map(empleado ->
                        new EmpleadoListadoResponse(
                                empleado.getId(),
                                empleado.getNombre(),
                                empleado.getActivo()
                        )
                )
                .toList();
    }

    @GetMapping("/api/empleados/{discordId}")
    public ResponseEntity<Empleado> getEmpleado(
            @PathVariable String discordId
    ) {

        return empleadoService
                .getByDiscordId(discordId)
                .map(ResponseEntity::ok)
                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }
}