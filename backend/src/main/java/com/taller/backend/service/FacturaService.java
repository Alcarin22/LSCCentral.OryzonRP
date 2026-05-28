package com.taller.backend.service;

import com.taller.backend.dto.CreateFacturaRequest;
import com.taller.backend.dto.FacturaListadoResponse;
import com.taller.backend.dto.FacturasPageResponse;
import com.taller.backend.entity.Empleado;
import com.taller.backend.entity.Factura;
import com.taller.backend.entity.FullTuning;
import com.taller.backend.entity.Item;
import com.taller.backend.entity.Reparacion;
import com.taller.backend.entity.Tasacion;
import com.taller.backend.entity.TasacionPrecio;
import com.taller.backend.entity.Tuneo;
import com.taller.backend.repository.EmpleadoRepository;
import com.taller.backend.repository.FacturaRepository;
import com.taller.backend.repository.FullTuningRepository;
import com.taller.backend.repository.ItemRepository;
import com.taller.backend.repository.ReparacionRepository;
import com.taller.backend.repository.TasacionPrecioRepository;
import com.taller.backend.repository.TasacionRepository;
import com.taller.backend.repository.TuneoRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private final PrimasService primasService;

    public FacturaService(
            FacturaRepository facturaRepository,
            EmpleadoRepository empleadoRepository,
            ReparacionRepository reparacionRepository,
            ItemRepository itemRepository,
            TasacionPrecioRepository tasacionPrecioRepository,
            TasacionRepository tasacionRepository,
            FullTuningRepository fullTuningRepository,
            TuneoRepository tuneoRepository,
            PrimasService primasService
    ) {
        this.facturaRepository = facturaRepository;
        this.empleadoRepository = empleadoRepository;
        this.reparacionRepository = reparacionRepository;
        this.itemRepository = itemRepository;
        this.tasacionPrecioRepository = tasacionPrecioRepository;
        this.tasacionRepository = tasacionRepository;
        this.fullTuningRepository = fullTuningRepository;
        this.tuneoRepository = tuneoRepository;
        this.primasService = primasService;
    }

    public Factura crearFactura(CreateFacturaRequest request) {
        Empleado empleado = empleadoRepository
                .findByDiscordId(request.getDiscordId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        int totalCalculado = calcularTotal(request);

        Factura factura = new Factura();

        factura.setIdEmpleado(empleado.getId());
        factura.setFecha(LocalDateTime.now());
        factura.setMatricula(request.getMatricula());
        factura.setTipo(request.getTipo());
        factura.setTotal(totalCalculado);

        boolean convenioAplicado =
                Boolean.TRUE.equals(request.getConvenio())
                        && !"Tasación".equals(request.getTipo());

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

        if ("Tasación".equals(request.getTipo())) {
            factura.setEstadoTasacion("Pendiente");
        } else {
            factura.setEstadoTasacion(null);
        }

        Factura guardada = facturaRepository.save(factura);

        if ("Tasación".equals(request.getTipo())) {
            guardarTasacion(guardada, request);
        }

        try {
            primasService.recalcularPrimaEmpleadoSemana(
                    empleado.getId(),
                    guardada.getFecha()
            );
        } catch (Exception e) {
            System.err.println("Error recalculando prima tras crear factura: " + e.getMessage());
        }

        return guardada;
    }

    public FacturaListadoResponse marcarTasacionEnviada(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        if (!"Tasación".equals(factura.getTipo())) {
            throw new RuntimeException("La factura no es una tasación");
        }

        factura.setEstadoTasacion("Enviada");

        Factura guardada = facturaRepository.save(factura);

        Map<Long, String> nombres = new HashMap<>();

        empleadoRepository.findById(guardada.getIdEmpleado())
                .ifPresent(empleado -> nombres.put(empleado.getId(), empleado.getNombre()));

        return mapearFactura(guardada, nombres);
    }

    public void eliminarFactura(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        Long empleadoId = factura.getIdEmpleado();
        LocalDateTime fechaFactura = factura.getFecha();

        facturaRepository.delete(factura);

        try {
            primasService.recalcularPrimaEmpleadoSemana(empleadoId, fechaFactura);
        } catch (Exception e) {
            System.err.println("Error recalculando prima tras eliminar factura: " + e.getMessage());
        }
    }

    private void guardarTasacion(Factura factura, CreateFacturaRequest request) {
        Tasacion tasacion = new Tasacion();

        tasacion.setFacturaId(factura.getId());
        tasacion.setModelo(request.getModelo());
        tasacion.setEstado(request.getEstado());
        tasacion.setOtros(request.getOtros());

        tasacionRepository.save(tasacion);
    }

    public FacturasPageResponse listarFacturas(
            String fechaInicio,
            String fechaFin,
            String tipo,
            Long idEmpleado,
            int page,
            int size
    ) {
        LocalDateTime inicio = null;
        LocalDateTime fin = null;

        if (fechaInicio != null && !fechaInicio.isBlank()) {
            inicio = LocalDate.parse(fechaInicio).atStartOfDay();
        }

        if (fechaFin != null && !fechaFin.isBlank()) {
            fin = LocalDate.parse(fechaFin).atTime(23, 59, 59);
        }

        int safePage = Math.max(page, 0);
        int safeSize = switch (size) {
            case 20, 50 -> size;
            default -> 10;
        };

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "fecha")
        );

        Specification<Factura> spec = crearFiltroFacturas(
                idEmpleado,
                tipo,
                inicio,
                fin
        );

        Page<Factura> pagina = facturaRepository.findAll(spec, pageable);

        List<Factura> facturas = pagina.getContent();

        List<Long> idsEmpleados = facturas
                .stream()
                .map(Factura::getIdEmpleado)
                .distinct()
                .toList();

        Map<Long, String> nombresEmpleados = new HashMap<>();

        empleadoRepository.findAllById(idsEmpleados)
                .forEach(empleado ->
                        nombresEmpleados.put(empleado.getId(), empleado.getNombre())
                );

        List<FacturaListadoResponse> content = facturas
                .stream()
                .map(factura -> mapearFactura(factura, nombresEmpleados))
                .toList();

        long totalFacturado = facturaRepository.findAll(spec)
                .stream()
                .map(Factura::getTotal)
                .filter(total -> total != null)
                .mapToLong(Integer::longValue)
                .sum();

        FacturasPageResponse response = new FacturasPageResponse();

        response.setContent(content);
        response.setTotalElements(pagina.getTotalElements());
        response.setTotalPages(pagina.getTotalPages());
        response.setPage(pagina.getNumber());
        response.setSize(pagina.getSize());
        response.setTotalFacturado(totalFacturado);

        response.setPromedioFactura(
                pagina.getTotalElements() > 0
                        ? totalFacturado / pagina.getTotalElements()
                        : 0
        );

        return response;
    }

    private Specification<Factura> crearFiltroFacturas(
            Long idEmpleado,
            String tipo,
            LocalDateTime inicio,
            LocalDateTime fin
    ) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (idEmpleado != null) {
                predicates = cb.and(
                        predicates,
                        cb.equal(root.get("idEmpleado"), idEmpleado)
                );
            }

            if (tipo != null && !tipo.isBlank()) {
                predicates = cb.and(
                        predicates,
                        cb.equal(root.get("tipo"), tipo)
                );
            }

            if (inicio != null) {
                predicates = cb.and(
                        predicates,
                        cb.greaterThanOrEqualTo(root.get("fecha"), inicio)
                );
            }

            if (fin != null) {
                predicates = cb.and(
                        predicates,
                        cb.lessThanOrEqualTo(root.get("fecha"), fin)
                );
            }

            return predicates;
        };
    }

    private FacturaListadoResponse mapearFactura(
            Factura factura,
            Map<Long, String> nombresEmpleados
    ) {
        FacturaListadoResponse response = new FacturaListadoResponse();

        response.setId(factura.getId());
        response.setIdEmpleado(factura.getIdEmpleado());
        response.setFecha(
                factura.getFecha() != null
                        ? factura.getFecha().toString()
                        : null
        );

        response.setTipo(factura.getTipo());
        response.setTotal(factura.getTotal());
        response.setConvenio(factura.getConvenio());
        response.setMatricula(factura.getMatricula());
        response.setModelo(factura.getModelo());
        response.setEstado(factura.getEstado());
        response.setEstadoTasacion(
                factura.getEstadoTasacion() != null
                        ? factura.getEstadoTasacion()
                        : ("Tasación".equals(factura.getTipo()) ? "Pendiente" : null)
        );
        response.setCantidad(factura.getCantidad());
        response.setItem(factura.getItem());
        response.setCategoria(factura.getCategoria());
        response.setGravedad(factura.getGravedad());
        response.setTuneoPlate(factura.getTuneoPlate());
        response.setTuneoSeleccionados(factura.getTuneoSeleccionados());
        response.setGrua(factura.getGrua());

        response.setNombreEmpleado(
                nombresEmpleados.getOrDefault(
                        factura.getIdEmpleado(),
                        "Desconocido"
                )
        );

        return response;
    }

    private int calcularTotal(CreateFacturaRequest request) {
        int total;

        switch (request.getTipo()) {
            case "Reparación":
                total = calcularReparacion(request);
                break;

            case "Items":
                total = calcularItems(request);
                break;

            case "Tasación":
                total = calcularTasacion(request);
                break;

            case "Full Tuning":
                total = calcularFullTuning(request);
                break;

            case "Tuneo":
                total = calcularTuneo(request);
                break;

            default:
                throw new RuntimeException("Tipo inválido");
        }

        if (
                Boolean.TRUE.equals(request.getConvenio())
                        && !"Tasación".equals(request.getTipo())
        ) {
            total = (int) Math.round(total * 0.8);
        }

        return total;
    }

    private int calcularReparacion(CreateFacturaRequest request) {
        Reparacion reparacion = reparacionRepository
                .findAll()
                .stream()
                .filter(r ->
                        normalizar(r.getTipo())
                                .equals(normalizar(request.getGravedad()))
                )
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Reparación no encontrada"));

        int total = reparacion.getPrecio();

        if (Boolean.TRUE.equals(request.getGrua())) {
            total += PRECIO_GRUA;
        }

        return total;
    }

    private int calcularItems(CreateFacturaRequest request) {
        Item item = itemRepository
                .findAll()
                .stream()
                .filter(i ->
                        normalizar(i.getNombre())
                                .equals(normalizar(request.getItem()))
                )
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        return item.getPrecio()
                .multiply(BigDecimal.valueOf(request.getCantidad()))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    private int calcularTasacion(CreateFacturaRequest request) {
        TasacionPrecio tasacion = tasacionPrecioRepository
                .findAll()
                .stream()
                .filter(t ->
                        normalizar(t.getEstado())
                                .equals(normalizar(request.getEstado()))
                )
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Precio de tasación no encontrado"));

        return tasacion.getPrecio().intValue();
    }

    private int calcularFullTuning(CreateFacturaRequest request) {
        FullTuning fullTuning = fullTuningRepository
                .findAll()
                .stream()
                .filter(ft ->
                        normalizar(ft.getCategoria())
                                .equals(normalizar(request.getCategoria()))
                )
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Categoría de Full Tuning no encontrada"));

        return fullTuning.getPrecio().intValue();
    }

    private int calcularTuneo(CreateFacturaRequest request) {
        if (
                request.getTuneoSeleccionados() == null ||
                request.getTuneoSeleccionados().isBlank()
        ) {
            throw new RuntimeException("No se han seleccionado piezas de tuneo");
        }

        String[] piezas = request.getTuneoSeleccionados().split(",");

        BigDecimal total = BigDecimal.ZERO;

        boolean tieneRendimiento = false;

        for (String pieza : piezas) {
            if (esRendimiento(pieza)) {
                tieneRendimiento = true;
                break;
            }
        }

        FullTuning fullTuning = null;

        if (tieneRendimiento) {
            if (request.getCategoria() == null || request.getCategoria().isBlank()) {
                throw new RuntimeException("La categoría es obligatoria para mejoras de rendimiento");
            }

            fullTuning = fullTuningRepository
                    .findAll()
                    .stream()
                    .filter(ft ->
                            normalizar(ft.getCategoria())
                                    .equals(normalizar(request.getCategoria()))
                    )
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Categoría de tuneo no encontrada"));
        }

        for (String pieza : piezas) {
            if (esRendimiento(pieza)) {
                total = total.add(
                        fullTuning.getPrecio().multiply(PORCENTAJE_RENDIMIENTO)
                );
            } else {
                Tuneo tuneo = tuneoRepository
                        .findAll()
                        .stream()
                        .filter(t ->
                                normalizar(t.getPieza())
                                        .equals(normalizar(getClave(pieza)))
                        )
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Pieza de tuneo no encontrada"));

                total = total.add(tuneo.getPrecio());
            }
        }

        return total.intValue();
    }

    private boolean esRendimiento(String pieza) {
        String valor = normalizar(pieza);

        return valor.equals("motor")
                || valor.equals("frenos")
                || valor.equals("transmision")
                || valor.equals("suspension")
                || valor.equals("blindaje")
                || valor.equals("turbo");
    }

    private String getClave(String pieza) {
        String valor = normalizar(pieza);

        if (valor.equals("pintura")) {
            return "Pintura";
        }

        if (valor.equals("livery")) {
            return "Vinilo";
        }

        if (valor.equals("pintura llantas")) {
            return "Pintura de ruedas";
        }

        return "Parte estetica";
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }

        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}