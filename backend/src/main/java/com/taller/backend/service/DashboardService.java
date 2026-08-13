package com.taller.backend.service;

import com.taller.backend.dto.DashboardHoyResponse;
import com.taller.backend.dto.DashboardSemanaResponse;
import com.taller.backend.dto.DashboardMesResponse;
import com.taller.backend.entity.Empleado;
import com.taller.backend.entity.Fichaje;
import com.taller.backend.entity.TipoServicio;
import com.taller.backend.entity.Prima;
import com.taller.backend.repository.EmpleadoRepository;
import com.taller.backend.repository.FacturaRepository;
import com.taller.backend.repository.FichajeRepository;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class DashboardService {

    private static final ZoneId ZONA_MADRID = ZoneId.of("Europe/Madrid");

    private final EmpleadoRepository empleadoRepository;
    private final FichajeRepository fichajeRepository;
    private final FacturaRepository facturaRepository;
    private final PrimasService primasService;

    public DashboardService(
            EmpleadoRepository empleadoRepository,
            FichajeRepository fichajeRepository,
            FacturaRepository facturaRepository,
            PrimasService primasService
    ) {
        this.empleadoRepository = empleadoRepository;
        this.fichajeRepository = fichajeRepository;
        this.facturaRepository = facturaRepository;
        this.primasService = primasService;
    }

    public DashboardHoyResponse getResumenHoy(String discordId) {
        Empleado empleado = getEmpleado(discordId);

        LocalDate hoy = LocalDate.now(ZONA_MADRID);
        LocalDateTime inicio = hoy.atStartOfDay();
        LocalDateTime fin = hoy.atTime(LocalTime.MAX);

        Fichaje fichajeActivo = fichajeRepository
                .findFirstByEmpleadoIdAndFechaHoraSalidaIsNullOrderByFechaHoraEntradaDesc(empleado.getId())
                .orElse(null);

        Fichaje ultimoTurno = fichajeRepository
                .findFirstByEmpleadoIdAndFechaHoraSalidaIsNotNullOrderByFechaHoraEntradaDesc(empleado.getId())
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
        response.setTipoServicio(
                fichajeActivo != null
                        ? (fichajeActivo.getTipoServicio() != null
                            ? fichajeActivo.getTipoServicio().name()
                            : TipoServicio.MECANICA.name())
                        : null
        );
        response.setServiciosRealizadosHoy(serviciosHoy != null ? serviciosHoy.intValue() : 0);

        if (ultimoTurno != null) {
            response.setUltimoTurnoFecha(
                    ultimoTurno.getFechaHoraEntrada() != null
                            ? ultimoTurno.getFechaHoraEntrada().toLocalDate().toString()
                            : null
            );

            response.setUltimoTurnoHoraEntrada(
                    ultimoTurno.getFechaHoraEntrada() != null
                            ? ultimoTurno.getFechaHoraEntrada().toString()
                            : null
            );

            response.setUltimoTurnoHoraSalida(
                    ultimoTurno.getFechaHoraSalida() != null
                            ? ultimoTurno.getFechaHoraSalida().toString()
                            : null
            );

            response.setUltimoTurnoMinutos(ultimoTurno.getMinutosTrabajados());
        } else {
            response.setUltimoTurnoFecha(null);
            response.setUltimoTurnoHoraEntrada(null);
            response.setUltimoTurnoHoraSalida(null);
            response.setUltimoTurnoMinutos(null);
        }

        return response;
    }

    public DashboardSemanaResponse getResumenSemana(String discordId) {
        Empleado empleado = getEmpleado(discordId);

        LocalDate hoy = LocalDate.now(ZONA_MADRID);
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
        Prima primaSemana = primasService.recalcularPrimaEmpleadoSemana(empleado.getId(), inicio);
        response.setPrimaEstimada("$" + primaSemana.getTotal().intValue());

        return response;
    }

    public DashboardMesResponse getResumenMes(String discordId) {
        Empleado empleado = getEmpleado(discordId);

        LocalDate hoy = LocalDate.now(ZONA_MADRID);
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