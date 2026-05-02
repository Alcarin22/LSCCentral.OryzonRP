package com.taller.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.taller.backend.dto.EmpleadoAdminResponse;
import com.taller.backend.dto.EmpleadoAdminUpdateRequest;
import com.taller.backend.dto.RangoResponse;
import com.taller.backend.service.AdministracionEmpleadoService;

@RestController
@RequestMapping("/api/admin")
public class AdministracionEmpleadoController {

    private final AdministracionEmpleadoService administracionEmpleadoService;

    public AdministracionEmpleadoController(
            AdministracionEmpleadoService administracionEmpleadoService
    ) {
        this.administracionEmpleadoService = administracionEmpleadoService;
    }

    @GetMapping("/empleados")
    public List<EmpleadoAdminResponse> listarEmpleados() {
        return administracionEmpleadoService.listarEmpleados();
    }

    @GetMapping("/rangos")
    public List<RangoResponse> listarRangos() {
        return administracionEmpleadoService.listarRangos();
    }

    @PutMapping("/empleados/{empleadoId}")
    public EmpleadoAdminResponse actualizarEmpleado(
            @PathVariable Long empleadoId,
            @RequestBody EmpleadoAdminUpdateRequest request
    ) {
        return administracionEmpleadoService.actualizarEmpleado(empleadoId, request);
    }
}