package com.taller.backend.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.taller.backend.dto.FichajeResponse;
import com.taller.backend.entity.Empleado;
import com.taller.backend.entity.Fichaje;
import com.taller.backend.repository.EmpleadoRepository;
import com.taller.backend.repository.FichajeRepository;

@Service
public class FichajeService {

    private final FichajeRepository fichajeRepository;
    private final EmpleadoRepository empleadoRepository;

    public FichajeService(FichajeRepository fichajeRepository, EmpleadoRepository empleadoRepository) {
        this.fichajeRepository = fichajeRepository;
        this.empleadoRepository = empleadoRepository;
    }

    public FichajeResponse toggleFichaje(String discordId) {
        Empleado empleado = empleadoRepository.findByDiscordId(discordId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        Fichaje fichajeAbierto = fichajeRepository
                .findFirstByEmpleadoIdAndFechaHoraSalidaIsNullOrderByFechaHoraEntradaDesc(empleado.getId())
                .orElse(null);

        if (fichajeAbierto == null) {
            Fichaje nuevoFichaje = new Fichaje();
            nuevoFichaje.setEmpleado(empleado);
            nuevoFichaje.setFechaHoraEntrada(LocalDateTime.now());
            nuevoFichaje.setFechaHoraSalida(null);
            nuevoFichaje.setMinutosTrabajados(null);

            fichajeRepository.save(nuevoFichaje);

            FichajeResponse response = new FichajeResponse();
            response.setFichajeActivo(true);
            response.setMensaje("Fichaje iniciado correctamente");
            response.setFechaHoraEntrada(nuevoFichaje.getFechaHoraEntrada().toString());
            response.setFechaHoraSalida(null);
            response.setMinutosTrabajados(null);
            return response;
        }

        LocalDateTime ahora = LocalDateTime.now();
        int minutos = (int) Duration.between(fichajeAbierto.getFechaHoraEntrada(), ahora).toMinutes();

        fichajeAbierto.setFechaHoraSalida(ahora);
        fichajeAbierto.setMinutosTrabajados(minutos);

        fichajeRepository.save(fichajeAbierto);

        FichajeResponse response = new FichajeResponse();
        response.setFichajeActivo(false);
        response.setMensaje("Fichaje finalizado correctamente");
        response.setFechaHoraEntrada(fichajeAbierto.getFechaHoraEntrada().toString());
        response.setFechaHoraSalida(fichajeAbierto.getFechaHoraSalida().toString());
        response.setMinutosTrabajados(fichajeAbierto.getMinutosTrabajados());
        return response;
    }

    public FichajeResponse obtenerEstadoFichaje(String discordId) {
        Empleado empleado = empleadoRepository.findByDiscordId(discordId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        Fichaje fichajeAbierto = fichajeRepository
                .findFirstByEmpleadoIdAndFechaHoraSalidaIsNullOrderByFechaHoraEntradaDesc(empleado.getId())
                .orElse(null);

        FichajeResponse response = new FichajeResponse();

        if (fichajeAbierto == null) {
            response.setFichajeActivo(false);
            response.setMensaje("No hay fichaje activo");
            return response;
        }

        response.setFichajeActivo(true);
        response.setMensaje("Hay un fichaje activo");
        response.setFechaHoraEntrada(fichajeAbierto.getFechaHoraEntrada().toString());
        return response;
    }
}