package com.taller.backend.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.taller.backend.dto.FichajeListadoResponse;
import com.taller.backend.dto.FichajeResponse;
import com.taller.backend.entity.Empleado;
import com.taller.backend.entity.Fichaje;
import com.taller.backend.entity.TipoServicio;
import com.taller.backend.repository.EmpleadoRepository;
import com.taller.backend.repository.FichajeRepository;

@Service
public class FichajeService {

    private static final ZoneId ZONA_MADRID = ZoneId.of("Europe/Madrid");

    private final FichajeRepository fichajeRepository;
    private final EmpleadoRepository empleadoRepository;
    private final PrimasService primasService;

    public FichajeService(
            FichajeRepository fichajeRepository,
            EmpleadoRepository empleadoRepository,
            PrimasService primasService
    ) {
        this.fichajeRepository = fichajeRepository;
        this.empleadoRepository = empleadoRepository;
        this.primasService = primasService;
    }

    public FichajeResponse toggleFichaje(String discordId, Boolean fichajeSeguridadSolicitado) {
        Empleado empleado = empleadoRepository.findByDiscordId(discordId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        Fichaje fichajeAbierto = fichajeRepository
                .findFirstByEmpleadoIdAndFechaHoraSalidaIsNullOrderByFechaHoraEntradaDesc(empleado.getId())
                .orElse(null);

        if (fichajeAbierto == null) {
            TipoServicio tipoServicio = resolverTipoServicio(empleado, fichajeSeguridadSolicitado);
            LocalDateTime ahora = LocalDateTime.now(ZONA_MADRID);

            Fichaje nuevoFichaje = new Fichaje();
            nuevoFichaje.setEmpleado(empleado);
            nuevoFichaje.setFechaHoraEntrada(ahora);
            nuevoFichaje.setFechaHoraSalida(null);
            nuevoFichaje.setMinutosTrabajados(null);
            nuevoFichaje.setTipoServicio(tipoServicio);

            fichajeRepository.save(nuevoFichaje);

            FichajeResponse response = new FichajeResponse();
            response.setFichajeActivo(true);
            response.setMensaje(
                    tipoServicio == TipoServicio.SEGURIDAD
                            ? "Fichaje de seguridad iniciado correctamente"
                            : "Fichaje iniciado correctamente"
            );
            response.setFechaHoraEntrada(nuevoFichaje.getFechaHoraEntrada().toString());
            response.setFechaHoraSalida(null);
            response.setMinutosTrabajados(null);
            response.setTipoServicio(tipoServicio.name());
            return response;
        }

        LocalDateTime ahora = LocalDateTime.now(ZONA_MADRID);
        int minutos = (int) Duration.between(fichajeAbierto.getFechaHoraEntrada(), ahora).toMinutes();

        fichajeAbierto.setFechaHoraSalida(ahora);
        fichajeAbierto.setMinutosTrabajados(Math.max(minutos, 0));
        fichajeRepository.save(fichajeAbierto);

        primasService.recalcularPrimaEmpleadoSemana(
                empleado.getId(),
                fichajeAbierto.getFechaHoraEntrada()
        );

        FichajeResponse response = new FichajeResponse();
        response.setFichajeActivo(false);
        response.setMensaje("Fichaje finalizado correctamente");
        response.setFechaHoraEntrada(fichajeAbierto.getFechaHoraEntrada().toString());
        response.setFechaHoraSalida(fichajeAbierto.getFechaHoraSalida().toString());
        response.setMinutosTrabajados(fichajeAbierto.getMinutosTrabajados());
        response.setTipoServicio(getTipoServicioSeguro(fichajeAbierto).name());
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
        response.setFechaHoraSalida(null);
        response.setMinutosTrabajados(null);
        response.setTipoServicio(getTipoServicioSeguro(fichajeAbierto).name());
        return response;
    }

    public List<FichajeListadoResponse> listarFichajes(LocalDateTime inicio, LocalDateTime fin) {
        List<Fichaje> fichajes;

        if (inicio != null && fin != null) {
            fichajes = fichajeRepository.findByFechaHoraEntradaBetweenOrderByFechaHoraEntradaDesc(inicio, fin);
        } else {
            fichajes = fichajeRepository.findAllByOrderByFechaHoraEntradaDesc();
        }

        return fichajes.stream().map(this::mapearListado).toList();
    }

    private TipoServicio resolverTipoServicio(Empleado empleado, Boolean fichajeSeguridadSolicitado) {
        if (esRangoSeguridad(empleado)) {
            return TipoServicio.SEGURIDAD;
        }

        if (Boolean.TRUE.equals(fichajeSeguridadSolicitado)) {
            if (!Boolean.TRUE.equals(empleado.getPuedeTrabajarComoSeguridad())) {
                throw new RuntimeException("El empleado no está autorizado para realizar fichajes de seguridad");
            }
            return TipoServicio.SEGURIDAD;
        }

        return TipoServicio.MECANICA;
    }

    private boolean esRangoSeguridad(Empleado empleado) {
        if (empleado == null || empleado.getRango() == null || empleado.getRango().getNombre() == null) {
            return false;
        }

        String rango = empleado.getRango().getNombre().trim().toLowerCase(Locale.ROOT);
        return rango.equals("seguridad") || rango.equals("jefe de seguridad");
    }

    private TipoServicio getTipoServicioSeguro(Fichaje fichaje) {
        return fichaje.getTipoServicio() != null ? fichaje.getTipoServicio() : TipoServicio.MECANICA;
    }

    private FichajeListadoResponse mapearListado(Fichaje fichaje) {
        FichajeListadoResponse response = new FichajeListadoResponse();
        response.setId(fichaje.getId());

        if (fichaje.getEmpleado() != null) {
            response.setIdEmpleado(fichaje.getEmpleado().getId());
            response.setNombreEmpleado(fichaje.getEmpleado().getNombre());
        }

        response.setFechaHoraEntrada(
                fichaje.getFechaHoraEntrada() != null ? fichaje.getFechaHoraEntrada().toString() : null
        );
        response.setFechaHoraSalida(
                fichaje.getFechaHoraSalida() != null ? fichaje.getFechaHoraSalida().toString() : null
        );
        response.setMinutosTrabajados(fichaje.getMinutosTrabajados());
        response.setActivo(fichaje.getFechaHoraSalida() == null);
        response.setTipoServicio(getTipoServicioSeguro(fichaje).name());
        return response;
    }
}
