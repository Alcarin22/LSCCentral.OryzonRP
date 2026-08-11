package com.taller.backend.service;

import org.springframework.stereotype.Service;

import com.taller.backend.dto.SesionEmpleadoResponse;
import com.taller.backend.dto.SesionRangoResponse;
import com.taller.backend.entity.Empleado;
import com.taller.backend.repository.EmpleadoRepository;

@Service
public class EmpleadoSesionService {

    private final EmpleadoRepository empleadoRepository;

    public EmpleadoSesionService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    public SesionEmpleadoResponse obtenerEmpleadoSesion(String discordId) {
        Empleado empleado = empleadoRepository.findByDiscordId(discordId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        return mapearEmpleado(empleado);
    }

    private SesionEmpleadoResponse mapearEmpleado(Empleado empleado) {
        SesionEmpleadoResponse response = new SesionEmpleadoResponse();

        response.setId(empleado.getId());
        response.setDiscordId(empleado.getDiscordId());
        response.setNombre(empleado.getNombre());
        response.setActivo(empleado.getActivo());
        response.setPuedeTrabajarComoSeguridad(Boolean.TRUE.equals(empleado.getPuedeTrabajarComoSeguridad()));

        response.setAvatarUrl("");
        response.setNickServidor(empleado.getNombre());

        if (empleado.getRango() != null) {
            SesionRangoResponse rango = new SesionRangoResponse();
            rango.setId(empleado.getRango().getId());
            rango.setNombre(empleado.getRango().getNombre());
            rango.setNivel(empleado.getRango().getNivel());

            response.setRango(rango);
        }

        return response;
    }
}