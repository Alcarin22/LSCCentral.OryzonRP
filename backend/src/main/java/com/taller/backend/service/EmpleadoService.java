package com.taller.backend.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.taller.backend.entity.Empleado;
import com.taller.backend.repository.EmpleadoRepository;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;

    public EmpleadoService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    public Optional<Empleado> getByDiscordId(String discordId) {
        return empleadoRepository.findByDiscordId(discordId);
    }
}