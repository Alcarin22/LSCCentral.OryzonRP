package com.taller.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taller.backend.dto.AdminPrimaPagadaRequest;
import com.taller.backend.dto.AdminPrimaResponse;
import com.taller.backend.dto.MisPrimasResponse;
import com.taller.backend.dto.PrimaHistorialSemanaResponse;
import com.taller.backend.entity.Empleado;
import com.taller.backend.entity.Factura;
import com.taller.backend.entity.Fichaje;
import com.taller.backend.entity.Prima;
import com.taller.backend.repository.EmpleadoRepository;
import com.taller.backend.repository.FacturaRepository;
import com.taller.backend.repository.FichajeRepository;
import com.taller.backend.repository.PrimaRepository;

@Service
public class PrimasService {

    private static final LocalDate FECHA_INICIO_SEMANA_0 = LocalDate.of(2026, 4, 13);
    private static final int MINUTOS_BASE_SIN_EXTRAS = 7 * 60;
    private static final int IMPORTE_HORA_EXTRA = 1000;
    private static final ZoneId ZONA_MADRID = ZoneId.of("Europe/Madrid");

    private final EmpleadoRepository empleadoRepository;
    private final FichajeRepository fichajeRepository;
    private final FacturaRepository facturaRepository;
    private final PrimaRepository primaRepository;

    public PrimasService(
            EmpleadoRepository empleadoRepository,
            FichajeRepository fichajeRepository,
            FacturaRepository facturaRepository,
            PrimaRepository primaRepository
    ) {
        this.empleadoRepository = empleadoRepository;
        this.fichajeRepository = fichajeRepository;
        this.facturaRepository = facturaRepository;
        this.primaRepository = primaRepository;
    }

    @Transactional
    public MisPrimasResponse getMisPrimas(String discordId, Integer weekOffset) {
        Empleado empleado = empleadoRepository.findByDiscordId(discordId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        int currentWeek = getCurrentWeek();

        if (currentWeek < 0) {
            return crearRespuestaSinSemana(empleado);
        }

        int offset = Math.max(0, weekOffset != null ? weekOffset : 0);
        int selectedWeek = Math.max(0, currentWeek - offset);

        Prima prima = recalcularPrimaEmpleadoSemana(empleado.getId(), selectedWeek);

        MisPrimasResponse response = new MisPrimasResponse();

        response.setNombreEmpleado(empleado.getNombre());
        response.setRango(empleado.getRango().getNombre());
        response.setWeekOffset(offset);
        response.setSemana("Semana " + selectedWeek);
        response.setRangoFechas(formatearRangoFechas(prima.getFechaInicio(), prima.getFechaFin()));

        response.setPrimaEstimada(prima.getTotal().intValue());
        response.setPrimaBase(prima.getPrimaBase().intValue());
        response.setExtraHoras(prima.getExtraHoras().intValue());
        response.setFacturacionSemanal(prima.getFacturado().intValue());
        response.setHorasTrabajadas(formatearHorasDecimal(prima.getHoras()));
        response.setServiciosRealizados(prima.getServicios());
        response.setDiasTrabajados(calcularDiasTrabajados(empleado.getId(), prima.getFechaInicio(), prima.getFechaFin()));
        response.setPorcentajeAplicado(prima.getPorcentajeAplicado());

        response.setRecordPersonalFacturacion(calcularRecordPersonal(empleado.getId()));
        response.setRecordGlobalFacturacion(calcularRecordGlobal());

        response.setHistorico(generarHistoricoDesdeTabla(empleado.getId(), selectedWeek));

        return response;
    }

    @Transactional
    public Prima recalcularPrimaEmpleadoSemana(Long empleadoId, LocalDateTime fechaReferencia) {
        int semana = getWeekFromDate(fechaReferencia.toLocalDate());
        return recalcularPrimaEmpleadoSemana(empleadoId, semana);
    }

    @Transactional
    public Prima recalcularPrimaEmpleadoSemana(Long empleadoId, Integer semana) {
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        LocalDate inicioSemana = FECHA_INICIO_SEMANA_0.plusWeeks(semana);
        LocalDate finSemana = inicioSemana.plusDays(6);

        LocalDateTime inicioDT = inicioSemana.atStartOfDay();
        LocalDateTime finDT = finSemana.atTime(LocalTime.MAX);

        List<Fichaje> fichajes = fichajeRepository
                .findByEmpleadoIdAndFechaHoraEntradaBetween(empleadoId, inicioDT, finDT);

        List<Factura> facturas = facturaRepository
                .findByIdEmpleadoAndFechaBetweenOrderByFechaAsc(empleadoId, inicioDT, finDT);

        int minutos = sumarMinutos(fichajes);

        BigDecimal horas = BigDecimal.valueOf(minutos)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        int servicios = facturas.size();

        BigDecimal facturado = BigDecimal.valueOf(
                facturas.stream()
                        .map(Factura::getTotal)
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue)
                        .sum()
        );

        int porcentaje = getPorcentajePrima(empleado.getRango().getNombre());

        BigDecimal primaBase = facturado
                .multiply(BigDecimal.valueOf(porcentaje))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal extraHoras = BigDecimal.valueOf(calcularExtraHoras(minutos));
        BigDecimal total = primaBase.add(extraHoras);

        Prima prima = primaRepository.findByEmpleadoIdAndSemana(empleadoId, semana)
                .orElseGet(() -> {
                    Prima nueva = new Prima();
                    nueva.setEmpleado(empleado);
                    nueva.setSemana(semana);
                    nueva.setPagada(false);
                    return nueva;
                });

        prima.setFechaInicio(inicioSemana);
        prima.setFechaFin(finSemana);
        prima.setFacturado(facturado);
        prima.setHoras(horas);
        prima.setServicios(servicios);
        prima.setPorcentajeAplicado(porcentaje);
        prima.setPrimaBase(primaBase);
        prima.setExtraHoras(extraHoras);
        prima.setTotal(total);

        return primaRepository.save(prima);
    }

    @Transactional
    public List<AdminPrimaResponse> listarPrimasAdmin(Integer semana) {
        int semanaSeleccionada = semana != null ? semana : getCurrentWeek();

        if (semanaSeleccionada < 0) {
            return List.of();
        }

        List<Empleado> empleados = empleadoRepository.findAllByOrderByActivoDescNombreAsc()
                .stream()
                .filter(e -> Boolean.TRUE.equals(e.getActivo()))
                .toList();

        empleados.forEach(empleado ->
                recalcularPrimaEmpleadoSemana(empleado.getId(), semanaSeleccionada)
        );

        return primaRepository.findBySemanaOrderByEmpleadoNombreAsc(semanaSeleccionada)
                .stream()
                .filter(prima -> prima.getEmpleado() != null)
                .filter(prima -> Boolean.TRUE.equals(prima.getEmpleado().getActivo()))
                .map(this::mapearAdminPrima)
                .toList();
    }

    @Transactional
    public AdminPrimaResponse actualizarPagada(Long primaId, AdminPrimaPagadaRequest request) {
        Prima prima = primaRepository.findById(primaId)
                .orElseThrow(() -> new RuntimeException("Prima no encontrada"));

        boolean pagada = request.getPagada() != null && request.getPagada();

        prima.setPagada(pagada);
        prima.setFechaPago(pagada ? LocalDateTime.now(ZONA_MADRID) : null);

        return mapearAdminPrima(primaRepository.save(prima));
    }

    private AdminPrimaResponse mapearAdminPrima(Prima prima) {
        AdminPrimaResponse response = new AdminPrimaResponse();

        response.setId(prima.getId());
        response.setEmpleadoId(prima.getEmpleado().getId());
        response.setNombreEmpleado(prima.getEmpleado().getNombre());
        response.setRango(prima.getEmpleado().getRango().getNombre());
        response.setSemana(prima.getSemana());
        response.setFechaInicio(prima.getFechaInicio());
        response.setFechaFin(prima.getFechaFin());
        response.setFacturado(prima.getFacturado());
        response.setHoras(prima.getHoras());
        response.setServicios(prima.getServicios());
        response.setPorcentajeAplicado(prima.getPorcentajeAplicado());
        response.setPrimaBase(prima.getPrimaBase());
        response.setExtraHoras(prima.getExtraHoras());
        response.setTotal(prima.getTotal());
        response.setPagada(prima.getPagada());
        response.setFechaPago(prima.getFechaPago());

        return response;
    }

    private List<PrimaHistorialSemanaResponse> generarHistoricoDesdeTabla(Long empleadoId, int selectedWeek) {
        return primaRepository.findByEmpleadoIdOrderBySemanaDesc(empleadoId)
                .stream()
                .filter(p -> p.getSemana() <= selectedWeek)
                .limit(4)
                .map(p -> {
                    PrimaHistorialSemanaResponse semana = new PrimaHistorialSemanaResponse();

                    semana.setSemana("Semana " + p.getSemana());
                    semana.setRangoFechas(formatearRangoFechas(p.getFechaInicio(), p.getFechaFin()));
                    semana.setHoras(formatearHorasDecimal(p.getHoras()));
                    semana.setServicios(p.getServicios());
                    semana.setFacturacion(p.getFacturado().intValue());
                    semana.setPrima(p.getTotal().intValue());
                    semana.setPrimaBase(p.getPrimaBase().intValue());
                    semana.setExtraHoras(p.getExtraHoras().intValue());

                    return semana;
                })
                .toList();
    }

    private int getCurrentWeek() {
        LocalDate hoy = LocalDate.now(ZONA_MADRID);
        LocalDate lunesActual = hoy.with(DayOfWeek.MONDAY);

        if (lunesActual.isBefore(FECHA_INICIO_SEMANA_0)) {
            return -1;
        }

        return (int) ChronoUnit.WEEKS.between(FECHA_INICIO_SEMANA_0, lunesActual);
    }

    private int getWeekFromDate(LocalDate fecha) {
        LocalDate lunes = fecha.with(DayOfWeek.MONDAY);

        if (lunes.isBefore(FECHA_INICIO_SEMANA_0)) {
            return 0;
        }

        return (int) ChronoUnit.WEEKS.between(FECHA_INICIO_SEMANA_0, lunes);
    }

    private int sumarMinutos(List<Fichaje> fichajes) {
        return fichajes.stream()
                .map(Fichaje::getMinutosTrabajados)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private int calcularDiasTrabajados(Long empleadoId, LocalDate inicio, LocalDate fin) {
        return (int) fichajeRepository
                .findByEmpleadoIdAndFechaHoraEntradaBetween(
                        empleadoId,
                        inicio.atStartOfDay(),
                        fin.atTime(LocalTime.MAX)
                )
                .stream()
                .map(f -> f.getFechaHoraEntrada().toLocalDate())
                .distinct()
                .count();
    }

    private int calcularExtraHoras(int minutos) {
        if (minutos <= MINUTOS_BASE_SIN_EXTRAS) {
            return 0;
        }

        return ((minutos - MINUTOS_BASE_SIN_EXTRAS) / 60) * IMPORTE_HORA_EXTRA;
    }

    private int getPorcentajePrima(String rangoNombre) {
        if (rangoNombre == null) {
            return 50;
        }

        String r = rangoNombre.toLowerCase();

        return switch (r) {
            case "aprendiz" -> 50;
            case "mecánico", "mecanico" -> 55;
            case "mecánico experimentado", "mecanico experimentado" -> 60;
            case "mecánico experimentado +", "mecanico experimentado +" -> 65;
            case "encargado" -> 70;
            default -> 80;
        };
    }

    private String formatearHorasDecimal(BigDecimal horas) {
        int totalMinutos = horas
                .multiply(BigDecimal.valueOf(60))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        return (totalMinutos / 60) + "h " + (totalMinutos % 60) + "m";
    }

    private String formatearRangoFechas(LocalDate inicio, LocalDate fin) {
        return inicio.getDayOfMonth() + "/" + inicio.getMonthValue()
                + " - " + fin.getDayOfMonth() + "/" + fin.getMonthValue();
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