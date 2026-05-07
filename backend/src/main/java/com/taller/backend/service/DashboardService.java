package com.taller.backend.service;

import com.taller.backend.dto.DashboardHoyResponse;
import com.taller.backend.dto.DashboardSemanaResponse;
import com.taller.backend.dto.DashboardMesResponse;
import com.taller.backend.entity.Empleado;
import com.taller.backend.entity.Fichaje;
import com.taller.backend.repository.EmpleadoRepository;
import com.taller.backend.repository.FacturaRepository;
import com.taller.backend.repository.FichajeRepository;

import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@Service
public class DashboardService {

    private final EmpleadoRepository empleadoRepository;
    private final FichajeRepository fichajeRepository;
    private final FacturaRepository facturaRepository;

    public DashboardService(
            EmpleadoRepository empleadoRepository,
            FichajeRepository fichajeRepository,
            FacturaRepository facturaRepository
    ) {
        this.empleadoRepository = empleadoRepository;
        this.fichajeRepository = fichajeRepository;
        this.facturaRepository = facturaRepository;
    }

    public DashboardHoyResponse getResumenHoy(String discordId) {
        Empleado empleado = getEmpleado(discordId);

        LocalDate hoy = LocalDate.now();
        LocalDateTime inicio = hoy.atStartOfDay();
        LocalDateTime fin = hoy.atTime(LocalTime.MAX);

        Fichaje fichajeActivo = fichajeRepository
                .findFirstByEmpleadoIdAndFechaHoraSalidaIsNullOrderByFechaHoraEntradaDesc(empleado.getId())
                .orElse(null);

        Long serviciosHoy = facturaRepository.countByIdEmpleadoAndFechaBetween(
                empleado.getId(),
                inicio,
                fin
        );

        DashboardHoyResponse response = new DashboardHoyResponse();

        response.setHoraEntrada(
                fichajeActivo != null && fichajeActivo.getFechaHoraEntrada() != null
                        ? fichajeActivo.getFechaHoraEntrada().toString()
                        : null
        );

        response.setFichajeActivo(fichajeActivo != null);
        response.setServiciosRealizadosHoy(serviciosHoy != null ? serviciosHoy.intValue() : 0);

        return response;
    }

    public DashboardSemanaResponse getResumenSemana(String discordId) {
        Empleado empleado = getEmpleado(discordId);

        LocalDate hoy = LocalDate.now();
        LocalDate lunes = hoy.with(DayOfWeek.MONDAY);
        LocalDate domingo = hoy.with(DayOfWeek.SUNDAY);

        LocalDateTime inicio = lunes.atStartOfDay();
        LocalDateTime fin = domingo.atTime(LocalTime.MAX);

        List<Fichaje> fichajesSemana = fichajeRepository.findByEmpleadoIdAndFechaHoraEntradaBetween(
                empleado.getId(),
                inicio,
                fin
        );

        int minutosSemana = sumarMinutos(fichajesSemana);

        Long serviciosSemana = facturaRepository.countByIdEmpleadoAndFechaBetween(
                empleado.getId(),
                inicio,
                fin
        );

        Long facturadoSemana = facturaRepository.sumTotalByIdEmpleadoAndFechaBetween(
                empleado.getId(),
                inicio,
                fin
        );

        DashboardSemanaResponse response = new DashboardSemanaResponse();

        response.setHorasRegistradas(formatearMinutos(minutosSemana));
        response.setDiasTrabajados(contarDiasTrabajados(fichajesSemana));
        response.setServiciosCompletados(serviciosSemana != null ? serviciosSemana.intValue() : 0);
        response.setPrimaEstimada("$" + calcularPrimaEstimada(empleado, facturadoSemana != null ? facturadoSemana : 0L));

        return response;
    }

    public DashboardMesResponse getResumenMes(String discordId) {
        Empleado empleado = getEmpleado(discordId);

        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

        LocalDateTime inicio = inicioMes.atStartOfDay();
        LocalDateTime fin = finMes.atTime(LocalTime.MAX);

        List<Fichaje> fichajesMes = fichajeRepository.findByEmpleadoIdAndFechaHoraEntradaBetween(
                empleado.getId(),
                inicio,
                fin
        );

        int minutosMes = sumarMinutos(fichajesMes);

        Long serviciosMes = facturaRepository.countByIdEmpleadoAndFechaBetween(
                empleado.getId(),
                inicio,
                fin
        );

        DashboardMesResponse response = new DashboardMesResponse();

        response.setHorasTotales(formatearMinutos(minutosMes));
        response.setJornadasCompletadas(contarDiasTrabajados(fichajesMes));
        response.setServiciosRealizados(serviciosMes != null ? serviciosMes.intValue() : 0);
        response.setRendimiento(calcularRendimiento(serviciosMes != null ? serviciosMes.intValue() : 0));

        return response;
    }

    private Empleado getEmpleado(String discordId) {
        return empleadoRepository.findByDiscordId(discordId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
    }

    private int sumarMinutos(List<Fichaje> fichajes) {
        return fichajes.stream()
                .map(Fichaje::getMinutosTrabajados)
                .filter(minutos -> minutos != null)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private int contarDiasTrabajados(List<Fichaje> fichajes) {
        return (int) fichajes.stream()
                .filter(f -> f.getFechaHoraEntrada() != null)
                .map(f -> f.getFechaHoraEntrada().toLocalDate())
                .distinct()
                .count();
    }

    private String formatearMinutos(int minutos) {
        int horas = minutos / 60;
        int mins = minutos % 60;

        return horas + "h " + mins + "m";
    }

    private int calcularPrimaEstimada(Empleado empleado, long facturadoSemana) {
        int porcentaje = getPorcentajePrima(
                empleado.getRango() != null ? empleado.getRango().getNombre() : null
        );

        return (int) Math.round(facturadoSemana * (porcentaje / 100.0));
    }

    private int getPorcentajePrima(String rangoNombre) {
        if (rangoNombre == null) return 50;

        String rango = rangoNombre.toLowerCase();

        return switch (rango) {
            case "aprendiz" -> 50;
            case "mecánico", "mecanico" -> 55;
            case "mecánico experimentado", "mecanico experimentado" -> 60;
            case "mecánico experimentado +", "mecanico experimentado +" -> 65;
            case "encargado" -> 70;
            default -> 80;
        };
    }

    private String calcularRendimiento(int serviciosMes) {
        if (serviciosMes >= 80) return "Excelente";
        if (serviciosMes >= 45) return "Bueno";
        if (serviciosMes >= 20) return "Medio";
        return "Bajo";
    }
}