package com.taller.backend.service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.taller.backend.dto.MisPrimasResponse;
import com.taller.backend.dto.PrimaActividadDiaResponse;
import com.taller.backend.dto.PrimaHistorialSemanaResponse;
import com.taller.backend.entity.Empleado;
import com.taller.backend.entity.Factura;
import com.taller.backend.entity.Fichaje;
import com.taller.backend.repository.EmpleadoRepository;
import com.taller.backend.repository.FacturaRepository;
import com.taller.backend.repository.FichajeRepository;

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

        LocalDate inicioSemana = FECHA_INICIO_SEMANA_0.plusWeeks(selectedWeek);
        LocalDate finSemana = inicioSemana.plusDays(6);

        LocalDateTime inicioDT = inicioSemana.atStartOfDay();
        LocalDateTime finDT = finSemana.atTime(LocalTime.MAX);

        List<Fichaje> fichajes = fichajeRepository
                .findByEmpleadoIdAndFechaHoraEntradaBetween(empleado.getId(), inicioDT, finDT);

        List<Factura> facturas = facturaRepository
                .findByIdEmpleadoAndFechaBetweenOrderByFechaAsc(empleado.getId(), inicioDT, finDT);

        int minutos = sumarMinutos(fichajes);
        int servicios = facturas.size();

        int facturacion = facturas.stream()
                .map(Factura::getTotal)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        Set<LocalDate> diasTrabajados = fichajes.stream()
                .map(f -> f.getFechaHoraEntrada().toLocalDate())
                .collect(Collectors.toSet());

        int porcentaje = getPorcentajePrima(empleado.getRango().getNombre());

        int primaBase = (int) Math.round(facturacion * (porcentaje / 100.0));
        int extraHoras = calcularExtraHoras(minutos);
        int prima = primaBase + extraHoras;

        MisPrimasResponse response = new MisPrimasResponse();

        response.setNombreEmpleado(empleado.getNombre());
        response.setRango(empleado.getRango().getNombre());
        response.setWeekOffset(offset);
        response.setSemana("Semana " + selectedWeek);
        response.setRangoFechas(formatearRangoFechas(inicioSemana, finSemana));

        response.setPrimaEstimada(prima);
        response.setPrimaBase(primaBase);
        response.setExtraHoras(extraHoras);
        response.setFacturacionSemanal(facturacion);
        response.setHorasTrabajadas(formatearMinutos(minutos));
        response.setServiciosRealizados(servicios);
        response.setDiasTrabajados(diasTrabajados.size());
        response.setPorcentajeAplicado(porcentaje);

        response.setRecordPersonalFacturacion(calcularRecordPersonal(empleado.getId()));
        response.setRecordGlobalFacturacion(calcularRecordGlobal());

        response.setActividadDiaria(generarActividadDiaria(empleado.getId(), inicioSemana, finSemana));
        response.setHistorico(generarHistorico(empleado.getId(), selectedWeek, empleado.getRango().getNombre()));

        return response;
    }

    // 🔥 ESTA ES LA CLAVE (CORREGIDA)
    private int getCurrentWeek() {

        ZoneId zonaMadrid = ZoneId.of("Europe/Madrid");

        LocalDate hoy = LocalDate.now(zonaMadrid);

        // Nos aseguramos de coger SIEMPRE el lunes de la semana actual
        LocalDate lunesActual = hoy.with(DayOfWeek.MONDAY);

        if (lunesActual.isBefore(FECHA_INICIO_SEMANA_0)) {
            return -1;
        }

        return (int) ChronoUnit.WEEKS.between(FECHA_INICIO_SEMANA_0, lunesActual);
    }

    private List<PrimaActividadDiaResponse> generarActividadDiaria(
            Long empleadoId,
            LocalDate inicioSemana,
            LocalDate finSemana) {

        List<PrimaActividadDiaResponse> actividad = new ArrayList<>();

        for (LocalDate dia = inicioSemana; !dia.isAfter(finSemana); dia = dia.plusDays(1)) {

            LocalDateTime inicio = dia.atStartOfDay();
            LocalDateTime fin = dia.atTime(LocalTime.MAX);

            List<Fichaje> fichajes = fichajeRepository
                    .findByEmpleadoIdAndFechaHoraEntradaBetween(empleadoId, inicio, fin);

            List<Factura> facturas = facturaRepository
                    .findByIdEmpleadoAndFechaBetweenOrderByFechaAsc(empleadoId, inicio, fin);

            int minutos = sumarMinutos(fichajes);

            int facturacion = facturas.stream()
                    .map(Factura::getTotal)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();

            PrimaActividadDiaResponse item = new PrimaActividadDiaResponse();

            item.setFecha(dia.format(DateTimeFormatter.ofPattern("dd/MM")));
            item.setDia(capitalizar(dia.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"))));
            item.setHoras(formatearMinutos(minutos));
            item.setServicios(facturas.size());
            item.setFacturacion(facturacion);

            actividad.add(item);
        }

        return actividad;
    }

    private List<PrimaHistorialSemanaResponse> generarHistorico(
            Long empleadoId,
            int selectedWeek,
            String rango) {

        List<PrimaHistorialSemanaResponse> historico = new ArrayList<>();

        int minWeek = Math.max(0, selectedWeek - 3);

        for (int week = selectedWeek; week >= minWeek; week--) {

            LocalDate inicio = FECHA_INICIO_SEMANA_0.plusWeeks(week);
            LocalDate fin = inicio.plusDays(6);

            LocalDateTime inicioDT = inicio.atStartOfDay();
            LocalDateTime finDT = fin.atTime(LocalTime.MAX);

            List<Fichaje> fichajes = fichajeRepository
                    .findByEmpleadoIdAndFechaHoraEntradaBetween(empleadoId, inicioDT, finDT);

            List<Factura> facturas = facturaRepository
                    .findByIdEmpleadoAndFechaBetweenOrderByFechaAsc(empleadoId, inicioDT, finDT);

            int minutos = sumarMinutos(fichajes);
            int facturacion = facturas.stream()
                    .map(Factura::getTotal)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();

            int porcentaje = getPorcentajePrima(rango);

            int primaBase = (int) Math.round(facturacion * (porcentaje / 100.0));
            int extraHoras = calcularExtraHoras(minutos);

            PrimaHistorialSemanaResponse semana = new PrimaHistorialSemanaResponse();

            semana.setSemana("Semana " + week);
            semana.setRangoFechas(formatearRangoFechas(inicio, fin));
            semana.setHoras(formatearMinutos(minutos));
            semana.setServicios(facturas.size());
            semana.setFacturacion(facturacion);
            semana.setPrima(primaBase + extraHoras);
            semana.setPrimaBase(primaBase);
            semana.setExtraHoras(extraHoras);

            historico.add(semana);
        }

        return historico;
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
        return ((minutos - MINUTOS_BASE_SIN_EXTRAS) / 60) * IMPORTE_HORA_EXTRA;
    }

    private int getPorcentajePrima(String rangoNombre) {
        if (rangoNombre == null) return 50;

        String r = rangoNombre.toLowerCase();

        return switch (r) {
            case "aprendiz" -> 50;
            case "mecánico", "mecanico" -> 55;
            case "mecánico experimentado" -> 60;
            case "mecánico experimentado +" -> 65;
            case "encargado" -> 70;
            default -> 80;
        };
    }

    private String formatearMinutos(int minutos) {
        return (minutos / 60) + "h " + (minutos % 60) + "m";
    }

    private String formatearRangoFechas(LocalDate inicio, LocalDate fin) {
        return inicio.getDayOfMonth() + "/" + inicio.getMonthValue()
                + " - " + fin.getDayOfMonth() + "/" + fin.getMonthValue();
    }

    private String capitalizar(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private int calcularRecordPersonal(Long idEmpleado) {
        return facturaRepository.findAllByIdEmpleado(idEmpleado).stream()
                .map(Factura::getTotal)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
    }

    private int calcularRecordGlobal() {
        return facturaRepository.findAll().stream()
                .map(Factura::getTotal)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
    }

    private MisPrimasResponse crearRespuestaSinSemana(Empleado e) {
        MisPrimasResponse r = new MisPrimasResponse();
        r.setNombreEmpleado(e.getNombre());
        r.setRango(e.getRango().getNombre());
        r.setSemana("Sin semana activa");
        return r;
    }
}