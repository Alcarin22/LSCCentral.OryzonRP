package com.taller.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
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

        SemanaData semana = calcularSemana(
                empleado.getId(),
                selectedWeek,
                empleado.getRango().getNombre()
        );

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

        response.setActividadDiaria(generarActividadDiaria(empleado.getId(), inicioSemana, finSemana));
        response.setHistorico(generarHistorico(empleado.getId(), selectedWeek, empleado.getRango().getNombre()));

        return response;
    }

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

        Set<LocalDate> diasTrabajados = fichajes.stream()
                .map(f -> f.getFechaHoraEntrada().toLocalDate())
                .collect(Collectors.toSet());

        int porcentaje = getPorcentajePrima(rango);
        int primaBase = (int) Math.round(facturacion * (porcentaje / 100.0));
        int extraHoras = calcularExtraHoras(minutos);
        int prima = primaBase + extraHoras;

        return new SemanaData(
                prima,
                primaBase,
                extraHoras,
                facturacion,
                servicios,
                diasTrabajados.size(),
                porcentaje,
                formatearMinutos(minutos),
                formatearRangoFechas(inicio, fin)
        );
    }

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
            SemanaData data = calcularSemana(empleadoId, week, rango);

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

    private int calcularRecordPersonal(Long idEmpleado) {
        return facturaRepository.findAllByIdEmpleado(idEmpleado).stream()
                .filter(f -> f.getFecha() != null && f.getTotal() != null)
                .filter(f -> !f.getFecha().toLocalDate().isBefore(FECHA_INICIO_SEMANA_0))
                .collect(Collectors.groupingBy(
                        f -> (int) ChronoUnit.WEEKS.between(FECHA_INICIO_SEMANA_0, f.getFecha().toLocalDate()),
                        Collectors.summingInt(Factura::getTotal)
                ))
                .values()
                .stream()
                .max(Integer::compareTo)
                .orElse(0);
    }

    private int calcularRecordGlobal() {
        return facturaRepository.findAll().stream()
                .filter(f -> f.getFecha() != null && f.getTotal() != null && f.getIdEmpleado() != null)
                .filter(f -> !f.getFecha().toLocalDate().isBefore(FECHA_INICIO_SEMANA_0))
                .collect(Collectors.groupingBy(
                        f -> f.getIdEmpleado() + "-" +
                                (int) ChronoUnit.WEEKS.between(FECHA_INICIO_SEMANA_0, f.getFecha().toLocalDate()),
                        Collectors.summingInt(Factura::getTotal)
                ))
                .values()
                .stream()
                .max(Integer::compareTo)
                .orElse(0);
    }

    private int getCurrentWeek() {
        LocalDate hoy = LocalDate.now(ZoneId.of("Europe/Madrid"));

        if (hoy.isBefore(FECHA_INICIO_SEMANA_0)) {
            return -1;
        }

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
        if (minutos <= MINUTOS_BASE_SIN_EXTRAS) {
            return 0;
        }

        int minutosExtra = minutos - MINUTOS_BASE_SIN_EXTRAS;
        int horasExtraCompletas = minutosExtra / 60;

        return horasExtraCompletas * IMPORTE_HORA_EXTRA;
    }

    private int getPorcentajePrima(String rangoNombre) {
        if (rangoNombre == null) {
            return 50;
        }

        String rango = rangoNombre.trim().toLowerCase(Locale.ROOT);

        return switch (rango) {
            case "aprendiz" -> 50;
            case "mecánico", "mecanico" -> 55;
            case "mecánico experimentado", "mecanico experimentado" -> 60;
            case "mecánico experimentado +", "mecanico experimentado +" -> 65;
            case "encargado" -> 70;
            case "jefe mecánico", "jefe mecanico", "jefe seguridad", "dueño", "dueno" -> 80;
            default -> 50;
        };
    }

    private String formatearMinutos(int minutos) {
        int horas = minutos / 60;
        int resto = minutos % 60;
        return horas + "h " + resto + "m";
    }

    private String formatearRangoFechas(LocalDate inicio, LocalDate fin) {
        return inicio.getDayOfMonth() + "/" + inicio.getMonthValue() + " - " +
               fin.getDayOfMonth() + "/" + fin.getMonthValue();
    }

    private String capitalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }

        return valor.substring(0, 1).toUpperCase() + valor.substring(1);
    }

    private MisPrimasResponse crearRespuestaSinSemana(Empleado empleado) {
        MisPrimasResponse response = new MisPrimasResponse();
        response.setNombreEmpleado(empleado.getNombre());
        response.setRango(empleado.getRango().getNombre());
        response.setWeekOffset(0);
        response.setSemana("Sin semana activa");
        response.setRangoFechas("Disponible desde 13/04/2026");
        response.setPrimaEstimada(0);
        response.setPrimaBase(0);
        response.setExtraHoras(0);
        response.setFacturacionSemanal(0);
        response.setHorasTrabajadas("0h 0m");
        response.setServiciosRealizados(0);
        response.setDiasTrabajados(0);
        response.setPorcentajeAplicado(getPorcentajePrima(empleado.getRango().getNombre()));
        response.setRecordPersonalFacturacion(0);
        response.setRecordGlobalFacturacion(0);
        response.setActividadDiaria(new ArrayList<>());
        response.setHistorico(new ArrayList<>());
        return response;
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