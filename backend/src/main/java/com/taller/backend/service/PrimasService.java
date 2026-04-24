package com.taller.backend.service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.taller.backend.dto.*;
import com.taller.backend.entity.*;
import com.taller.backend.repository.*;

@Service
public class PrimasService {

    private static final LocalDate FECHA_INICIO_SEMANA_0 = LocalDate.of(2026, 4, 13);
    private static final int MINUTOS_BASE_SIN_EXTRAS = 7 * 60;
    private static final int IMPORTE_HORA_EXTRA = 1000;

    private final EmpleadoRepository empleadoRepository;
    private final FichajeRepository fichajeRepository;
    private final FacturaRepository facturaRepository;

    public PrimasService(
            EmpleadoRepository empleadoRepository,
            FichajeRepository fichajeRepository,
            FacturaRepository facturaRepository) {
        this.empleadoRepository = empleadoRepository;
        this.fichajeRepository = fichajeRepository;
        this.facturaRepository = facturaRepository;
    }

    public MisPrimasResponse getMisPrimas(String discordId, Integer weekOffset) {

        Empleado empleado = empleadoRepository.findByDiscordId(discordId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        int currentWeek = getCurrentWeek();

        if (currentWeek < 0) {
            return crearRespuestaSinSemana(empleado);
        }

        int offset = Math.max(0, weekOffset != null ? weekOffset : 0);
        int selectedWeek = Math.max(0, currentWeek - offset);

        SemanaData semana = calcularSemana(
                empleado.getId(),
                selectedWeek,
                empleado.getRango().getNombre()
        );

        LocalDate inicioSemana = FECHA_INICIO_SEMANA_0.plusWeeks(selectedWeek);
        LocalDate finSemana = inicioSemana.plusDays(6);

        MisPrimasResponse response = new MisPrimasResponse();

        response.setNombreEmpleado(empleado.getNombre());
        response.setRango(empleado.getRango().getNombre());
        response.setWeekOffset(offset);
        response.setSemana("Semana " + selectedWeek);
        response.setRangoFechas(semana.rangoFechas);

        response.setPrimaEstimada(semana.prima);
        response.setPrimaBase(semana.primaBase);
        response.setExtraHoras(semana.extraHoras);
        response.setFacturacionSemanal(semana.facturacion);
        response.setHorasTrabajadas(semana.horasTexto);
        response.setServiciosRealizados(semana.servicios);
        response.setDiasTrabajados(semana.diasTrabajados);
        response.setPorcentajeAplicado(semana.porcentaje);

        response.setRecordPersonalFacturacion(calcularRecordPersonal(empleado.getId()));
        response.setRecordGlobalFacturacion(calcularRecordGlobal());

        response.setActividadDiaria(
                generarActividadDiaria(empleado.getId(), inicioSemana, finSemana)
        );

        response.setHistorico(
                generarHistorico(empleado.getId(), selectedWeek)
        );

        return response;
    }

    // ================= CORE =================

    private SemanaData calcularSemana(Long empleadoId, int weekNumber, String rango) {

        LocalDate inicio = FECHA_INICIO_SEMANA_0.plusWeeks(weekNumber);
        LocalDate fin = inicio.plusDays(6);

        LocalDateTime inicioDT = inicio.atStartOfDay();
        LocalDateTime finDT = fin.atTime(LocalTime.MAX);

        List<Fichaje> fichajes = fichajeRepository
                .findByEmpleadoIdAndFechaHoraEntradaBetween(empleadoId, inicioDT, finDT);

        List<Factura> facturas = facturaRepository
                .findByIdEmpleadoAndFechaBetweenOrderByFechaAsc(empleadoId, inicioDT, finDT);

        int minutos = sumarMinutos(fichajes);
        int servicios = facturas.size();

        int facturacion = facturas.stream()
                .map(Factura::getTotal)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int porcentaje = getPorcentajePrima(rango);

        int primaBase = (int) Math.round(facturacion * (porcentaje / 100.0));
        int extraHoras = calcularExtraHoras(minutos);
        int prima = primaBase + extraHoras;

        Set<LocalDate> dias = fichajes.stream()
                .map(f -> f.getFechaHoraEntrada().toLocalDate())
                .collect(Collectors.toSet());

        return new SemanaData(
                prima,
                primaBase,
                extraHoras,
                facturacion,
                servicios,
                dias.size(),
                porcentaje,
                formatearMinutos(minutos),
                formatearRangoFechas(inicio, fin)
        );
    }

    // ================= ACTIVIDAD DIARIA =================

    private List<PrimaActividadDiaResponse> generarActividadDiaria(
            Long empleadoId,
            LocalDate inicioSemana,
            LocalDate finSemana) {

        List<PrimaActividadDiaResponse> actividad = new ArrayList<>();

        for (LocalDate dia = inicioSemana; !dia.isAfter(finSemana); dia = dia.plusDays(1)) {

            LocalDateTime inicioDia = dia.atStartOfDay();
            LocalDateTime finDia = dia.atTime(LocalTime.MAX);

            List<Fichaje> fichajes = fichajeRepository
                    .findByEmpleadoIdAndFechaHoraEntradaBetween(empleadoId, inicioDia, finDia);

            List<Factura> facturas = facturaRepository
                    .findByIdEmpleadoAndFechaBetweenOrderByFechaAsc(empleadoId, inicioDia, finDia);

            int minutos = sumarMinutos(fichajes);

            int facturacion = facturas.stream()
                    .map(Factura::getTotal)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();

            PrimaActividadDiaResponse item = new PrimaActividadDiaResponse();

            item.setFecha(dia.format(DateTimeFormatter.ofPattern("dd/MM")));
            item.setDia(capitalizar(
                    dia.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"))
            ));
            item.setHoras(formatearMinutos(minutos));
            item.setServicios(facturas.size());
            item.setFacturacion(facturacion);

            actividad.add(item);
        }

        return actividad;
    }

    // ================= HISTORICO =================

    private List<PrimaHistorialSemanaResponse> generarHistorico(Long idEmpleado, int selectedWeek) {

        List<PrimaHistorialSemanaResponse> historico = new ArrayList<>();

        int minWeek = Math.max(0, selectedWeek - 3);

        for (int week = selectedWeek; week >= minWeek; week--) {

            SemanaData data = calcularSemana(
                    idEmpleado,
                    week,
                    obtenerRangoEmpleado(idEmpleado)
            );

            PrimaHistorialSemanaResponse semana = new PrimaHistorialSemanaResponse();

            semana.setSemana("Semana " + week);
            semana.setRangoFechas(data.rangoFechas);
            semana.setHoras(data.horasTexto);
            semana.setServicios(data.servicios);
            semana.setFacturacion(data.facturacion);
            semana.setPrima(data.prima);
            semana.setPrimaBase(data.primaBase);
            semana.setExtraHoras(data.extraHoras);

            historico.add(semana);
        }

        return historico;
    }

    // ================= RECORDS =================

    private int calcularRecordPersonal(Long idEmpleado) {

        return facturaRepository.findAllByIdEmpleado(idEmpleado).stream()
                .filter(f -> f.getFecha() != null && f.getTotal() != null)
                .collect(Collectors.groupingBy(
                        f -> (int) ChronoUnit.WEEKS.between(
                                FECHA_INICIO_SEMANA_0,
                                f.getFecha().toLocalDate()
                        ),
                        Collectors.summingInt(Factura::getTotal)
                ))
                .values()
                .stream()
                .max(Integer::compareTo)
                .orElse(0);
    }

    private int calcularRecordGlobal() {

        return facturaRepository.findAll().stream()
                .filter(f -> f.getFecha() != null && f.getTotal() != null)
                .collect(Collectors.groupingBy(
                        f -> f.getIdEmpleado() + "-" +
                                (int) ChronoUnit.WEEKS.between(
                                        FECHA_INICIO_SEMANA_0,
                                        f.getFecha().toLocalDate()
                                ),
                        Collectors.summingInt(Factura::getTotal)
                ))
                .values()
                .stream()
                .max(Integer::compareTo)
                .orElse(0);
    }

    // ================= UTILS =================

    private int getCurrentWeek() {
        LocalDate hoy = LocalDate.now();

        if (hoy.isBefore(FECHA_INICIO_SEMANA_0)) return -1;

        return (int) ChronoUnit.WEEKS.between(FECHA_INICIO_SEMANA_0, hoy);
    }

    private int sumarMinutos(List<Fichaje> fichajes) {
        return fichajes.stream()
                .map(Fichaje::getMinutosTrabajados)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private int calcularExtraHoras(int minutos) {
        if (minutos <= MINUTOS_BASE_SIN_EXTRAS) return 0;

        int extra = minutos - MINUTOS_BASE_SIN_EXTRAS;
        return (extra / 60) * IMPORTE_HORA_EXTRA;
    }

    private int getPorcentajePrima(String rango) {
        if (rango == null) return 50;

        return switch (rango.toLowerCase(Locale.ROOT)) {
            case "aprendiz" -> 50;
            case "mecánico", "mecanico" -> 55;
            case "mecánico experimentado", "mecanico experimentado" -> 60;
            case "mecánico experimentado +" -> 65;
            case "encargado" -> 70;
            default -> 80;
        };
    }

    private String formatearMinutos(int minutos) {
        return (minutos / 60) + "h " + (minutos % 60) + "m";
    }

    private String formatearRangoFechas(LocalDate i, LocalDate f) {
        return i.getDayOfMonth() + "/" + i.getMonthValue() + " - " +
               f.getDayOfMonth() + "/" + f.getMonthValue();
    }

    private String capitalizar(String s) {
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    private String obtenerRangoEmpleado(Long idEmpleado) {
        return empleadoRepository.findById(idEmpleado)
                .map(e -> e.getRango().getNombre())
                .orElse("aprendiz");
    }

    private MisPrimasResponse crearRespuestaSinSemana(Empleado empleado) {
        MisPrimasResponse r = new MisPrimasResponse();
        r.setNombreEmpleado(empleado.getNombre());
        r.setRango(empleado.getRango().getNombre());
        r.setSemana("Sin semana activa");
        r.setRangoFechas("-");
        r.setPrimaEstimada(0);
        r.setHistorico(new ArrayList<>());
        return r;
    }

    private record SemanaData(
            int prima,
            int primaBase,
            int extraHoras,
            int facturacion,
            int servicios,
            int diasTrabajados,
            int porcentaje,
            String horasTexto,
            String rangoFechas
    ) {}
}