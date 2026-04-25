package com.taller.backend.service;

import com.taller.backend.dto.CreateFacturaRequest;
import com.taller.backend.dto.FacturaListadoResponse;
import com.taller.backend.entity.*;
import com.taller.backend.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class FacturaService {

    private static final int PRECIO_GRUA = 600;
    private static final BigDecimal PORCENTAJE_RENDIMIENTO = BigDecimal.valueOf(0.30);

    private final FacturaRepository facturaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ReparacionRepository reparacionRepository;
    private final ItemRepository itemRepository;
    private final TasacionPrecioRepository tasacionPrecioRepository;
    private final TasacionRepository tasacionRepository;
    private final FullTuningRepository fullTuningRepository;
    private final TuneoRepository tuneoRepository;

    public FacturaService(
            FacturaRepository facturaRepository,
            EmpleadoRepository empleadoRepository,
            ReparacionRepository reparacionRepository,
            ItemRepository itemRepository,
            TasacionPrecioRepository tasacionPrecioRepository,
            TasacionRepository tasacionRepository,
            FullTuningRepository fullTuningRepository,
            TuneoRepository tuneoRepository) {
        this.facturaRepository = facturaRepository;
        this.empleadoRepository = empleadoRepository;
        this.reparacionRepository = reparacionRepository;
        this.itemRepository = itemRepository;
        this.tasacionPrecioRepository = tasacionPrecioRepository;
        this.tasacionRepository = tasacionRepository;
        this.fullTuningRepository = fullTuningRepository;
        this.tuneoRepository = tuneoRepository;
    }

    // ============================
    // CREAR FACTURA
    // ============================

    public Factura crearFactura(CreateFacturaRequest request) {
        Empleado empleado = empleadoRepository.findByDiscordId(request.getDiscordId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        int totalCalculado = calcularTotal(request);

        Factura factura = new Factura();
        factura.setIdEmpleado(empleado.getId());
        factura.setFecha(LocalDateTime.now());
        factura.setMatricula(request.getMatricula());
        factura.setTipo(request.getTipo());
        factura.setTotal(totalCalculado);

        boolean convenioAplicado = Boolean.TRUE.equals(request.getConvenio()) && !"Tasación".equals(request.getTipo());
        factura.setConvenio(convenioAplicado);

        factura.setModelo(request.getModelo());
        factura.setEstado(request.getEstado());
        factura.setCantidad(request.getCantidad());
        factura.setItem(request.getItem());
        factura.setCategoria(request.getCategoria());
        factura.setGravedad(request.getGravedad());
        factura.setTuneoPlate(request.getTuneoPlate());
        factura.setTuneoSeleccionados(request.getTuneoSeleccionados());
        factura.setGrua(Boolean.TRUE.equals(request.getGrua()));

        Factura guardada = facturaRepository.save(factura);

        if ("Tasación".equals(request.getTipo())) {
            guardarTasacion(guardada, request);
        }

        return guardada;
    }

    private void guardarTasacion(Factura factura, CreateFacturaRequest request) {
        Tasacion t = new Tasacion();
        t.setFacturaId(factura.getId());
        t.setModelo(request.getModelo());
        t.setEstado(request.getEstado());
        t.setOtros(request.getOtros());
        tasacionRepository.save(t);
    }

    // ============================
    // LISTADO FACTURAS
    // ============================

    public List<FacturaListadoResponse> listarFacturas(
            String fechaInicio,
            String fechaFin,
            String tipo,
            Long idEmpleado) {

        LocalDateTime inicio = null;
        LocalDateTime fin = null;

        if (fechaInicio != null && !fechaInicio.isBlank()) {
            inicio = LocalDate.parse(fechaInicio).atStartOfDay();
        }

        if (fechaFin != null && !fechaFin.isBlank()) {
            fin = LocalDate.parse(fechaFin).atTime(23, 59, 59);
        }

        List<Factura> facturas = facturaRepository.buscarFacturasFiltradas(
                idEmpleado,
                tipo,
                inicio,
                fin
        );

        return facturas.stream()
                .map(this::mapearFactura)
                .toList();
    }

    private FacturaListadoResponse mapearFactura(Factura f) {
        FacturaListadoResponse r = new FacturaListadoResponse();

        r.setId(f.getId());
        r.setIdEmpleado(f.getIdEmpleado());
        r.setFecha(f.getFecha() != null ? f.getFecha().toString() : null);
        r.setTipo(f.getTipo());
        r.setTotal(f.getTotal());
        r.setConvenio(f.getConvenio());
        r.setMatricula(f.getMatricula());
        r.setModelo(f.getModelo());
        r.setEstado(f.getEstado());
        r.setCantidad(f.getCantidad());
        r.setItem(f.getItem());
        r.setCategoria(f.getCategoria());
        r.setGravedad(f.getGravedad());
        r.setTuneoPlate(f.getTuneoPlate());
        r.setTuneoSeleccionados(f.getTuneoSeleccionados());
        r.setGrua(f.getGrua());

        String nombre = empleadoRepository.findById(f.getIdEmpleado())
                .map(Empleado::getNombre)
                .orElse("Desconocido");

        r.setNombreEmpleado(nombre);

        return r;
    }

    // ============================
    // CALCULO TOTAL
    // ============================

    private int calcularTotal(CreateFacturaRequest request) {
        int total;

        switch (request.getTipo()) {
            case "Reparación" -> total = calcularReparacion(request);
            case "Items" -> total = calcularItems(request);
            case "Tasación" -> total = calcularTasacion(request);
            case "Full Tuning" -> total = calcularFullTuning(request);
            case "Tuneo" -> total = calcularTuneo(request);
            default -> throw new RuntimeException("Tipo inválido");
        }

        if (Boolean.TRUE.equals(request.getConvenio()) && !"Tasación".equals(request.getTipo())) {
            total = (int) Math.round(total * 0.8);
        }

        return total;
    }

    private int calcularReparacion(CreateFacturaRequest r) {
        Reparacion rep = reparacionRepository.findAll().stream()
                .filter(x -> normalizar(x.getTipo()).equals(normalizar(r.getGravedad())))
                .findFirst()
                .orElseThrow();

        int total = rep.getPrecio();

        if (Boolean.TRUE.equals(r.getGrua())) {
            total += PRECIO_GRUA;
        }

        return total;
    }

    private int calcularItems(CreateFacturaRequest r) {
        Item item = itemRepository.findAll().stream()
                .filter(x -> normalizar(x.getNombre()).equals(normalizar(r.getItem())))
                .findFirst()
                .orElseThrow();

        return item.getPrecio()
                .multiply(BigDecimal.valueOf(r.getCantidad()))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    private int calcularTasacion(CreateFacturaRequest r) {
        TasacionPrecio t = tasacionPrecioRepository.findAll().stream()
                .filter(x -> normalizar(x.getEstado()).equals(normalizar(r.getEstado())))
                .findFirst()
                .orElseThrow();

        return t.getPrecio().intValue();
    }

    private int calcularFullTuning(CreateFacturaRequest r) {
        FullTuning ft = fullTuningRepository.findAll().stream()
                .filter(x -> normalizar(x.getCategoria()).equals(normalizar(r.getCategoria())))
                .findFirst()
                .orElseThrow();

        return ft.getPrecio().intValue();
    }

    private int calcularTuneo(CreateFacturaRequest r) {
        FullTuning ft = fullTuningRepository.findAll().stream()
                .filter(x -> normalizar(x.getCategoria()).equals(normalizar(r.getCategoria())))
                .findFirst()
                .orElseThrow();

        String[] piezas = r.getTuneoSeleccionados().split(",");

        BigDecimal total = BigDecimal.ZERO;

        for (String p : piezas) {
            if (esRendimiento(p)) {
                total = total.add(ft.getPrecio().multiply(PORCENTAJE_RENDIMIENTO));
            } else {
                Tuneo t = tuneoRepository.findAll().stream()
                        .filter(x -> normalizar(x.getPieza()).equals(normalizar(getClave(p))))
                        .findFirst()
                        .orElseThrow();

                total = total.add(t.getPrecio());
            }
        }

        return total.intValue();
    }

    private boolean esRendimiento(String p) {
        String x = normalizar(p);
        return x.equals("motor") || x.equals("frenos") || x.equals("transmision")
                || x.equals("suspension") || x.equals("blindaje") || x.equals("turbo");
    }

    private String getClave(String pieza) {
        String p = normalizar(pieza);

        if (p.equals("pintura")) return "Pintura";
        if (p.equals("livery")) return "Vinilo";
        if (p.equals("pintura llantas")) return "Pintura de ruedas";

        return "Parte estetica";
    }

    private String normalizar(String v) {
        if (v == null) return "";
        return Normalizer.normalize(v, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}