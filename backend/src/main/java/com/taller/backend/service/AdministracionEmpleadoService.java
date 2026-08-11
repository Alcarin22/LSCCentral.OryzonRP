package com.taller.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taller.backend.dto.EmpleadoAdminResponse;
import com.taller.backend.dto.EmpleadoAdminUpdateRequest;
import com.taller.backend.dto.RangoResponse;
import com.taller.backend.entity.Empleado;
import com.taller.backend.entity.Rango;
import com.taller.backend.repository.EmpleadoRepository;
import com.taller.backend.repository.RangoRepository;

@Service
public class AdministracionEmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final RangoRepository rangoRepository;

    public AdministracionEmpleadoService(
            EmpleadoRepository empleadoRepository,
            RangoRepository rangoRepository
    ) {
        this.empleadoRepository = empleadoRepository;
        this.rangoRepository = rangoRepository;
    }

    @Transactional(readOnly = true)
    public List<EmpleadoAdminResponse> listarEmpleados() {
        return empleadoRepository.findAllByOrderByActivoDescNombreAsc()
                .stream()
                .map(this::mapearEmpleado)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RangoResponse> listarRangos() {
        return rangoRepository.findAllByOrderByNivelAscNombreAsc()
                .stream()
                .map(this::mapearRango)
                .toList();
    }

    @Transactional
    public EmpleadoAdminResponse actualizarEmpleado(Long empleadoId, EmpleadoAdminUpdateRequest request) {
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        if (request.getRangoId() != null) {
            Rango rango = rangoRepository.findById(request.getRangoId())
                    .orElseThrow(() -> new RuntimeException("Rango no encontrado"));

            empleado.setRango(rango);
        }

        if (request.getActivo() != null) {
            empleado.setActivo(request.getActivo());
        }

        if (request.getPuedeTrabajarComoSeguridad() != null) {
            empleado.setPuedeTrabajarComoSeguridad(request.getPuedeTrabajarComoSeguridad());
        }

        Empleado actualizado = empleadoRepository.save(empleado);

        return mapearEmpleado(actualizado);
    }

    private EmpleadoAdminResponse mapearEmpleado(Empleado empleado) {
        EmpleadoAdminResponse response = new EmpleadoAdminResponse();

        response.setId(empleado.getId());
        response.setDiscordId(empleado.getDiscordId());
        response.setNombre(empleado.getNombre());
        response.setActivo(empleado.getActivo());
        response.setPuedeTrabajarComoSeguridad(Boolean.TRUE.equals(empleado.getPuedeTrabajarComoSeguridad()));

        if (empleado.getRango() != null) {
            response.setRangoId(empleado.getRango().getId());
            response.setRangoNombre(empleado.getRango().getNombre());
            response.setRangoNivel(empleado.getRango().getNivel());
        }

        return response;
    }

    private RangoResponse mapearRango(Rango rango) {
        RangoResponse response = new RangoResponse();

        response.setId(rango.getId());
        response.setNombre(rango.getNombre());
        response.setNivel(rango.getNivel());

        return response;
    }
}