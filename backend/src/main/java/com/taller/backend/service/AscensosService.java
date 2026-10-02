package com.taller.backend.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.taller.backend.entity.Empleado;
import com.taller.backend.entity.Fichaje;
import com.taller.backend.repository.FichajeRepository;

@Service
public class AscensosService {

    public enum EstadoAscenso { NINGUNO, REQUISITOS_CUMPLIDOS, ASCENSO_PENDIENTE }

    public record EvaluacionAscenso(EstadoAscenso estado, String siguienteRango, LocalDate fechaAscenso, String detalle) {}

    private static final ZoneId ZONA_MADRID = ZoneId.of("Europe/Madrid");
    private final FichajeRepository fichajeRepository;

    public AscensosService(FichajeRepository fichajeRepository) {
        this.fichajeRepository = fichajeRepository;
    }

    public EvaluacionAscenso evaluar(Empleado empleado) {
        if (empleado == null || !Boolean.TRUE.equals(empleado.getActivo()) || empleado.getRango() == null) {
            return ninguno();
        }
        String rango = normalizar(empleado.getRango().getNombre());
        LocalDate hoy = LocalDate.now(ZONA_MADRID);

        if (rango.equals("aprendiz")) return evaluarAprendiz(empleado, hoy);
        if (rango.equals("mecanico")) return evaluarSemanas(empleado, hoy, 10, "Mecánico Experimentado");
        if (rango.equals("mecanico experimentado")) return evaluarSemanas(empleado, hoy, 12, "Mecánico Experimentado +");
        return ninguno();
    }

    private EvaluacionAscenso evaluarAprendiz(Empleado empleado, LocalDate hoy) {
        LocalDate contratacion = empleado.getFechaContratacion();
        if (contratacion == null) return new EvaluacionAscenso(EstadoAscenso.NINGUNO, "Mecánico", null, "Falta fecha de contratación");

        LocalDate fechaMinima = contratacion.plusDays(7);
        LocalDate fechaHoras = fechaEnQueAlcanzaMinutos(empleado.getId(), contratacion, hoy, 7 * 60);
        boolean cumpleHoras = fechaHoras != null;
        boolean cumpleDias = !hoy.isBefore(fechaMinima);
        if (!cumpleDias || !cumpleHoras) return new EvaluacionAscenso(EstadoAscenso.NINGUNO, "Mecánico", null, "Requiere 7 días naturales y 7 h trabajadas");

        LocalDate requisitosCumplidos = fechaHoras.isAfter(fechaMinima) ? fechaHoras : fechaMinima;
        LocalDate lunes = siguienteLunesEstricto(requisitosCumplidos);
        EstadoAscenso estado = !hoy.isBefore(lunes) ? EstadoAscenso.ASCENSO_PENDIENTE : EstadoAscenso.REQUISITOS_CUMPLIDOS;
        return new EvaluacionAscenso(estado, "Mecánico", lunes, "7 días naturales y 7 h completados");
    }

    private EvaluacionAscenso evaluarSemanas(Empleado empleado, LocalDate hoy, int horasMinimas, String siguienteRango) {
        LocalDate ultimoAscenso = empleado.getFechaUltimoAscenso();
        if (ultimoAscenso == null) return new EvaluacionAscenso(EstadoAscenso.NINGUNO, siguienteRango, null, "Falta fecha del último ascenso");

        // Solo se consideran semanas naturales (lunes-domingo) iniciadas después del ascenso.
        LocalDate primerLunes = ultimoAscenso.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate semana = primerLunes;
        LocalDate segundaSemanaValida = null;
        boolean anteriorValida = false;

        while (!semana.isAfter(hoy)) {
            LocalDate fin = semana.plusDays(6);
            LocalDate finEvaluacion = fin.isAfter(hoy) ? hoy : fin;
            int minutos = minutosEntre(empleado.getId(), semana.atStartOfDay(), finEvaluacion.atTime(LocalTime.MAX));
            boolean valida = minutos >= horasMinimas * 60;
            if (valida && anteriorValida) {
                segundaSemanaValida = semana;
                break;
            }
            anteriorValida = valida;
            semana = semana.plusWeeks(1);
        }

        if (segundaSemanaValida == null) {
            return new EvaluacionAscenso(EstadoAscenso.NINGUNO, siguienteRango, null, "Requiere 2 semanas consecutivas de " + horasMinimas + " h");
        }

        LocalDate fechaAscenso = segundaSemanaValida.plusWeeks(1); // lunes siguiente a la segunda semana
        EstadoAscenso estado = !hoy.isBefore(fechaAscenso) ? EstadoAscenso.ASCENSO_PENDIENTE : EstadoAscenso.REQUISITOS_CUMPLIDOS;
        return new EvaluacionAscenso(estado, siguienteRango, fechaAscenso, "2 semanas consecutivas de " + horasMinimas + " h completadas");
    }

    private LocalDate fechaEnQueAlcanzaMinutos(Long empleadoId, LocalDate inicio, LocalDate fin, int objetivoMinutos) {
        int acumulado = 0;
        List<Fichaje> fichajes = fichajeRepository
                .findByEmpleadoIdAndFechaHoraEntradaBetween(empleadoId, inicio.atStartOfDay(), fin.atTime(LocalTime.MAX))
                .stream()
                .filter(f -> f.getMinutosTrabajados() != null)
                .sorted(java.util.Comparator.comparing(Fichaje::getFechaHoraEntrada))
                .toList();
        for (Fichaje fichaje : fichajes) {
            acumulado += fichaje.getMinutosTrabajados();
            if (acumulado >= objetivoMinutos) return fichaje.getFechaHoraEntrada().toLocalDate();
        }
        return null;
    }

    private int minutosEntre(Long empleadoId, LocalDateTime inicio, LocalDateTime fin) {
        List<Fichaje> fichajes = fichajeRepository.findByEmpleadoIdAndFechaHoraEntradaBetween(empleadoId, inicio, fin);
        return fichajes.stream().map(Fichaje::getMinutosTrabajados).filter(m -> m != null).mapToInt(Integer::intValue).sum();
    }

    private LocalDate siguienteLunesEstricto(LocalDate fecha) {
        return fecha.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    private String normalizar(String valor) {
        return java.text.Normalizer.normalize(valor == null ? "" : valor, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim().toLowerCase(Locale.ROOT);
    }

    private EvaluacionAscenso ninguno() { return new EvaluacionAscenso(EstadoAscenso.NINGUNO, null, null, null); }
}
