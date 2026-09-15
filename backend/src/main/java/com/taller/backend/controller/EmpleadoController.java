package com.taller.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taller.backend.dto.EmpleadoListadoResponse;
import com.taller.backend.repository.EmpleadoRepository;

@RestController
public class EmpleadoController {

    private final EmpleadoRepository empleadoRepository;

    public EmpleadoController(
            EmpleadoRepository empleadoRepository
    ) {
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
}