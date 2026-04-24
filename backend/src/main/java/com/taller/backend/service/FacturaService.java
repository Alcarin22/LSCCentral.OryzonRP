package com.taller.backend.service;

import com.taller.backend.dto.CreateFacturaRequest;
import com.taller.backend.entity.Empleado;
import com.taller.backend.entity.Factura;
import com.taller.backend.entity.FullTuning;
import com.taller.backend.entity.Item;
import com.taller.backend.entity.Reparacion;
import com.taller.backend.entity.Tasacion;
import com.taller.backend.entity.TasacionPrecio;
import com.taller.backend.repository.EmpleadoRepository;
import com.taller.backend.repository.FacturaRepository;
import com.taller.backend.repository.FullTuningRepository;
import com.taller.backend.repository.ItemRepository;
import com.taller.backend.repository.ReparacionRepository;
import com.taller.backend.repository.TasacionPrecioRepository;
import com.taller.backend.repository.TasacionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class FacturaService {

    private static final int PRECIO_GRUA = 600;
    private static final BigDecimal PORCENTAJE_TUNEO = BigDecimal.valueOf(0.30);

    private final FacturaRepository facturaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ReparacionRepository reparacionRepository;
    private final ItemRepository itemRepository;
    private final TasacionPrecioRepository tasacionPrecioRepository;
    private final TasacionRepository tasacionRepository;
    private final FullTuningRepository fullTuningRepository;

    public FacturaService(
            FacturaRepository facturaRepository,
            EmpleadoRepository empleadoRepository,
            ReparacionRepository reparacionRepository,
            ItemRepository itemRepository,
            TasacionPrecioRepository tasacionPrecioRepository,
            TasacionRepository tasacionRepository,
            FullTuningRepository fullTuningRepository) {
        this.facturaRepository = facturaRepository;
        this.empleadoRepository = empleadoRepository;
        this.reparacionRepository = reparacionRepository;
        this.itemRepository = itemRepository;
        this.tasacionPrecioRepository = tasacionPrecioRepository;
        this.tasacionRepository = tasacionRepository;
        this.fullTuningRepository = fullTuningRepository;
    }

    public Factura crearFactura(CreateFacturaRequest request) {
        Empleado empleado = empleadoRepository.findByDiscordId(request.getDiscordId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado para ese discordId"));

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
        factura.setGrua(Boolean.TRUE.equals(request.getGrua()) && "Reparación".equals(request.getTipo()));

        Factura facturaGuardada = facturaRepository.save(factura);

        if ("Tasación".equals(request.getTipo())) {
            guardarDetalleTasacion(facturaGuardada, request);
        }

        return facturaGuardada;
    }

    private void guardarDetalleTasacion(Factura facturaGuardada, CreateFacturaRequest request) {
        if (request.getModelo() == null || request.getModelo().isBlank()) {
            throw new RuntimeException("El modelo es obligatorio para una tasación");
        }

        if (request.getEstado() == null || request.getEstado().isBlank()) {
            throw new RuntimeException("El estado es obligatorio para una tasación");
        }

        Tasacion tasacion = new Tasacion();
        tasacion.setFacturaId(facturaGuardada.getId());
        tasacion.setModelo(request.getModelo().trim());
        tasacion.setEstado(request.getEstado().trim());
        tasacion.setOtros(request.getOtros());

        tasacionRepository.save(tasacion);
    }

    private int calcularTotal(CreateFacturaRequest request) {
        if (request.getTipo() == null || request.getTipo().isBlank()) {
            throw new RuntimeException("El tipo de factura es obligatorio");
        }

        int totalBase;

        switch (request.getTipo()) {
            case "Reparación":
                totalBase = calcularTotalReparacion(request);
                break;

            case "Items":
                totalBase = calcularTotalItems(request);
                break;

            case "Tasación":
                totalBase = calcularTotalTasacion(request);
                break;

            case "Full Tuning":
                totalBase = calcularTotalFullTuning(request);
                break;

            case "Tuneo":
                totalBase = calcularTotalTuneo(request);
                break;

            default:
                if (request.getTotal() == null) {
                    throw new RuntimeException("No se pudo calcular el total de la factura");
                }
                totalBase = request.getTotal();
                break;
        }

        if (Boolean.TRUE.equals(request.getConvenio()) && !"Tasación".equals(request.getTipo())) {
            totalBase = (int) Math.round(totalBase * 0.8);
        }

        return totalBase;
    }

    private int calcularTotalReparacion(CreateFacturaRequest request) {
        if (request.getGravedad() == null || request.getGravedad().isBlank()) {
            throw new RuntimeException("La gravedad de la reparación es obligatoria");
        }

        String gravedadNormalizada = normalizar(request.getGravedad());

        Reparacion reparacion = reparacionRepository.findAll().stream()
                .filter(r -> normalizar(r.getTipo()).equals(gravedadNormalizada))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No existe una reparación configurada en BD para: " + request.getGravedad()));

        int total = reparacion.getPrecio();

        if (Boolean.TRUE.equals(request.getGrua())) {
            total += PRECIO_GRUA;
        }

        return total;
    }

    private int calcularTotalItems(CreateFacturaRequest request) {
        if (request.getItem() == null || request.getItem().isBlank()) {
            throw new RuntimeException("Debes seleccionar un item");
        }

        if (request.getCantidad() == null || request.getCantidad() <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor que 0");
        }

        String itemNormalizado = normalizar(request.getItem());

        Item item = itemRepository.findAll().stream()
                .filter(i -> normalizar(i.getNombre()).equals(itemNormalizado))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No existe un item configurado en BD para: " + request.getItem()));

        BigDecimal total = item.getPrecio()
                .multiply(BigDecimal.valueOf(request.getCantidad()))
                .setScale(0, RoundingMode.HALF_UP);

        return total.intValue();
    }

    private int calcularTotalTasacion(CreateFacturaRequest request) {
        if (request.getEstado() == null || request.getEstado().isBlank()) {
            throw new RuntimeException("Debes seleccionar un estado para la tasación");
        }

        String estadoNormalizado = normalizar(request.getEstado());

        TasacionPrecio tasacionPrecio = tasacionPrecioRepository.findAll().stream()
                .filter(t -> normalizar(t.getEstado()).equals(estadoNormalizado))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No existe un precio configurado para el estado de tasación: " + request.getEstado()));

        return tasacionPrecio.getPrecio()
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    private int calcularTotalFullTuning(CreateFacturaRequest request) {
        if (request.getCategoria() == null || request.getCategoria().isBlank()) {
            throw new RuntimeException("Debes seleccionar una categoría para Full Tuning");
        }

        String categoriaNormalizada = normalizar(request.getCategoria());

        FullTuning fullTuning = fullTuningRepository.findAll().stream()
                .filter(ft -> normalizar(ft.getCategoria()).equals(categoriaNormalizada))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No existe un precio configurado para la categoría: " + request.getCategoria()));

        return fullTuning.getPrecio()
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    private int calcularTotalTuneo(CreateFacturaRequest request) {
        if (request.getCategoria() == null || request.getCategoria().isBlank()) {
            throw new RuntimeException("Debes seleccionar una categoría para el tuneo");
        }

        if (request.getTuneoSeleccionados() == null || request.getTuneoSeleccionados().isBlank()) {
            throw new RuntimeException("Debes seleccionar al menos una mejora de tuneo");
        }

        String categoriaNormalizada = normalizar(request.getCategoria());

        FullTuning fullTuning = fullTuningRepository.findAll().stream()
                .filter(ft -> normalizar(ft.getCategoria()).equals(categoriaNormalizada))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No existe un precio de Full Tuning para la categoría: " + request.getCategoria()));

        int cantidadMejoras = request.getTuneoSeleccionados().split(",").length;

        BigDecimal total = fullTuning.getPrecio()
                .multiply(PORCENTAJE_TUNEO)
                .multiply(BigDecimal.valueOf(cantidadMejoras));

        return total.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }

        String sinAcentos = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return sinAcentos.trim().toLowerCase(Locale.ROOT);
    }
}