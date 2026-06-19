package com.taller.backend.controller;

import com.taller.backend.dto.VehiculoAdminRequest;
import com.taller.backend.dto.VehiculoAdminResponse;
import com.taller.backend.service.VehiculoService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping("/api/vehiculos")
    public List<VehiculoAdminResponse> listarVehiculosActivos() {
        return vehiculoService.listarActivos();
    }

    @GetMapping("/api/admin/vehiculos")
    public List<VehiculoAdminResponse> listarVehiculosAdmin() {
        return vehiculoService.listarTodos();
    }

    @PostMapping("/api/admin/vehiculos")
    public VehiculoAdminResponse crearVehiculo(
            @RequestBody VehiculoAdminRequest request
    ) {
        return vehiculoService.crearVehiculo(request);
    }

    @PutMapping("/api/admin/vehiculos/{id}")
    public VehiculoAdminResponse actualizarVehiculo(
            @PathVariable Long id,
            @RequestBody VehiculoAdminRequest request
    ) {
        return vehiculoService.actualizarVehiculo(id, request);
    }

    @PatchMapping("/api/admin/vehiculos/{id}/activo")
    public VehiculoAdminResponse cambiarActivo(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request
    ) {
        Boolean activo = request.get("activo");
        return vehiculoService.cambiarActivo(id, activo);
    }
}