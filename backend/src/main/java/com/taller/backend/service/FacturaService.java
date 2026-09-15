package com.taller.backend.service;

import com.taller.backend.dto.CreateFacturaRequest;
import com.taller.backend.dto.CreateFacturacionLoteRequest;
import com.taller.backend.dto.CreateFacturacionLoteResponse;
import com.taller.backend.dto.FacturaItemResponse;
import com.taller.backend.dto.FacturaListadoResponse;
import com.taller.backend.dto.FacturasPageResponse;
import com.taller.backend.entity.Empleado;
import com.taller.backend.entity.Factura;
import com.taller.backend.entity.FacturaItem;
import com.taller.backend.entity.FullTuning;
import com.taller.backend.entity.Item;
import com.taller.backend.entity.Reparacion;
import com.taller.backend.entity.Tasacion;
import com.taller.backend.entity.TasacionPrecio;
import com.taller.backend.entity.Tuneo;
import com.taller.backend.repository.EmpleadoRepository;
import com.taller.backend.repository.FacturaRepository;
import com.taller.backend.repository.FacturaItemRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class FacturaService {

    private static final int PRECIO_GRUA = 600;

    private static final BigDecimal PORCENTAJE_RENDIMIENTO =
            BigDecimal.valueOf(0.30);

    private static final ZoneId ZONA_MADRID =
            ZoneId.of("Europe/Madrid");

    private final FacturaRepository facturaRepository;
    private final FacturaItemRepository facturaItemRepository;
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
            FacturaItemRepository facturaItemRepository,
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
        this.facturaItemRepository = facturaItemRepository;
        this.empleadoRepository = empleadoRepository;
        this.reparacionRepository = reparacionRepository;
        this.itemRepository = itemRepository;
        this.tasacionPrecioRepository = tasacionPrecioRepository;
        this.tasacionRepository = tasacionRepository;
        this.fullTuningRepository = fullTuningRepository;
        this.tuneoRepository = tuneoRepository;
        this.primasService = primasService;
    }

    @Transactional
    public Factura crearFactura(
            CreateFacturaRequest request,
            String discordId
    ) {

        if (discordId == null
                || discordId.isBlank()) {

            throw new RuntimeException(
                    "No hay empleado asociado a la facturación"
            );
        }

        Empleado empleado = empleadoRepository
                .findByDiscordId(discordId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Empleado no encontrado"
                        )
                );

        Factura guardada = crearFacturaInterna(
                request,
                empleado
        );

        recalcularPrimaSeguro(
                empleado.getId(),
                guardada.getFecha()
        );

        return guardada;
    }

    @Transactional
    public CreateFacturacionLoteResponse crearFacturacionLote(
            CreateFacturacionLoteRequest request,
            String discordId
    ) {

        if (discordId == null
                || discordId.isBlank()) {

            throw new RuntimeException(
                    "No hay empleado asociado a la facturación"
            );
        }

        if (request == null
                || request.getElementos() == null
                || request.getElementos().isEmpty()) {

            throw new RuntimeException(
                    "No hay elementos para facturar"
            );
        }

        Empleado empleado = empleadoRepository
                .findByDiscordId(discordId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Empleado no encontrado"
                        )
                );

        List<CreateFacturaRequest> items =
                new ArrayList<>();

        List<CreateFacturaRequest> independientes =
                new ArrayList<>();

        for (CreateFacturaRequest elemento : request.getElementos()) {

            if (elemento == null
                    || elemento.getTipo() == null
                    || elemento.getTipo().isBlank()) {

                throw new RuntimeException(
                        "Hay un elemento sin tipo de factura"
                );
            }

            if ("Items".equals(elemento.getTipo())) {
                items.add(elemento);
            } else {
                independientes.add(elemento);
            }
        }

        List<Factura> facturasCreadas =
                new ArrayList<>();

        for (CreateFacturaRequest elemento : independientes) {

            facturasCreadas.add(
                    crearFacturaInterna(
                            elemento,
                            empleado
                    )
            );
        }

        if (!items.isEmpty()) {

            facturasCreadas.add(
                    crearFacturaItemsAgrupada(
                            empleado,
                            items
                    )
            );
        }

        LocalDateTime fechaReferencia = facturasCreadas
                .stream()
                .map(Factura::getFecha)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(
                        LocalDateTime.now(
                                ZONA_MADRID
                        )
                );

        recalcularPrimaSeguro(
                empleado.getId(),
                fechaReferencia
        );

        Map<Long, String> nombres =
                new HashMap<>();

        nombres.put(
                empleado.getId(),
                empleado.getNombre()
        );

        List<FacturaListadoResponse> facturasResponse =
                facturasCreadas
                        .stream()
                        .map(factura ->
                                mapearFactura(
                                        factura,
                                        nombres
                                )
                        )
                        .toList();

        long totalGeneral = facturasCreadas
                .stream()
                .map(Factura::getTotal)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();

        CreateFacturacionLoteResponse response =
                new CreateFacturacionLoteResponse();

        response.setTotalFacturas(
                facturasCreadas.size()
        );

        response.setTotalGeneral(
                totalGeneral
        );

        response.setFacturas(
                facturasResponse
        );

        return response;
    }

    private Factura crearFacturaInterna(
            CreateFacturaRequest request,
            Empleado empleado
    ) {

        int totalCalculado =
                calcularTotal(request);

        Factura factura =
                new Factura();

        factura.setIdEmpleado(
                empleado.getId()
        );

        factura.setFecha(
                LocalDateTime.now(
                        ZONA_MADRID
                )
        );

        factura.setMatricula(
                request.getMatricula()
        );

        factura.setTipo(
                request.getTipo()
        );

        factura.setTotal(
                totalCalculado
        );

        boolean convenioAplicado =
                Boolean.TRUE.equals(
                        request.getConvenio()
                )
                        && !"Tasación".equals(
                                request.getTipo()
                        );

        factura.setConvenio(
                convenioAplicado
        );

        factura.setModelo(
                request.getModelo()
        );

        factura.setEstado(
                request.getEstado()
        );

        factura.setCantidad(
                request.getCantidad()
        );

        factura.setItem(
                request.getItem()
        );

        factura.setCategoria(
                request.getCategoria()
        );

        factura.setGravedad(
                request.getGravedad()
        );

        factura.setTuneoPlate(
                request.getTuneoPlate()
        );

        factura.setTuneoSeleccionados(
                request.getTuneoSeleccionados()
        );

        factura.setGrua(
                Boolean.TRUE.equals(
                        request.getGrua()
                )
        );

        if ("Tasación".equals(
                request.getTipo()
        )) {

            factura.setEstadoTasacion(
                    "Pendiente"
            );

        } else {

            factura.setEstadoTasacion(
                    null
            );
        }

        Factura guardada =
                facturaRepository.save(
                        factura
                );

        if ("Tasación".equals(
                request.getTipo()
        )) {

            guardarTasacion(
                    guardada,
                    request
            );
        }

        if ("Items".equals(
                request.getTipo()
        )) {

            guardarLineaItem(
                    guardada,
                    request
            );
        }

        return guardada;
    }

    private Factura crearFacturaItemsAgrupada(
            Empleado empleado,
            List<CreateFacturaRequest> solicitudes
    ) {

        LinkedHashMap<String, CreateFacturaRequest> agrupados =
                new LinkedHashMap<>();

        for (
                CreateFacturaRequest solicitud :
                solicitudes
        ) {

            if (solicitud.getItem() == null
                    || solicitud.getItem().isBlank()) {

                throw new RuntimeException(
                        "Hay un item sin seleccionar"
                );
            }

            int cantidad =
                    solicitud.getCantidad() != null
                            ? solicitud.getCantidad()
                            : 1;

            if (cantidad <= 0) {

                throw new RuntimeException(
                        "La cantidad del item debe ser mayor que cero"
                );
            }

            String clave =
                    normalizar(
                            solicitud.getItem()
                    )
                            + "|"
                            + Boolean.TRUE.equals(
                                    solicitud.getLspd()
                            );

            CreateFacturaRequest existente =
                    agrupados.get(
                            clave
                    );

            if (existente == null) {

                CreateFacturaRequest copia =
                        new CreateFacturaRequest();

                copia.setTipo(
                        "Items"
                );

                copia.setItem(
                        solicitud.getItem()
                );

                copia.setCantidad(
                        cantidad
                );

                copia.setConvenio(
                        false
                );

                copia.setLspd(
                        Boolean.TRUE.equals(
                                solicitud.getLspd()
                        )
                );

                agrupados.put(
                        clave,
                        copia
                );

            } else {

                existente.setCantidad(
                        existente.getCantidad()
                                + cantidad
                );
            }
        }

        Factura factura =
                new Factura();

        factura.setIdEmpleado(
                empleado.getId()
        );

        factura.setFecha(
                LocalDateTime.now(
                        ZONA_MADRID
                )
        );

        factura.setTipo(
                "Items"
        );

        factura.setTotal(
                0
        );

        factura.setConvenio(
                false
        );

        factura.setGrua(
                false
        );

        factura.setEstadoTasacion(
                null
        );

        Factura guardada =
                facturaRepository.save(
                        factura
                );

        int total = 0;

        for (
                CreateFacturaRequest solicitud :
                agrupados.values()
        ) {

            FacturaItem linea =
                    crearLineaItem(
                            guardada,
                            solicitud
                    );

            facturaItemRepository.save(
                    linea
            );

            total +=
                    linea.getSubtotal();
        }

        guardada.setTotal(
                total
        );

        return facturaRepository.save(
                guardada
        );
    }

    private void guardarLineaItem(
            Factura factura,
            CreateFacturaRequest request
    ) {

        facturaItemRepository.save(
                crearLineaItem(
                        factura,
                        request
                )
        );
    }

    private FacturaItem crearLineaItem(
            Factura factura,
            CreateFacturaRequest request
    ) {

        Item itemCatalogo =
                buscarItem(
                        request.getItem()
                );

        int cantidad =
                request.getCantidad() != null
                        ? request.getCantidad()
                        : 1;

        if (cantidad <= 0) {

            throw new RuntimeException(
                    "La cantidad del item debe ser mayor que cero"
            );
        }

        int precioUnitario =
                itemCatalogo
                        .getPrecio()
                        .setScale(
                                0,
                                RoundingMode.HALF_UP
                        )
                        .intValue();

        int subtotal =
                precioUnitario
                        * cantidad;

        boolean lspd =
                Boolean.TRUE.equals(
                        request.getLspd()
                );

        if (lspd) {

            subtotal =
                    (int) Math.round(
                            subtotal * 0.50
                    );
        }

        FacturaItem linea =
                new FacturaItem();

        linea.setFactura(
                factura
        );

        linea.setItem(
                itemCatalogo.getNombre()
        );

        linea.setCantidad(
                cantidad
        );

        linea.setPrecioUnitario(
                precioUnitario
        );

        linea.setSubtotal(
                subtotal
        );

        linea.setLspd(
                lspd
        );

        return linea;
    }

    private void recalcularPrimaSeguro(
            Long empleadoId,
            LocalDateTime fechaReferencia
    ) {

        try {

            primasService
                    .recalcularPrimaEmpleadoSemana(
                            empleadoId,
                            fechaReferencia
                    );

        } catch (Exception e) {

            System.err.println(
                    "Error recalculando prima tras crear facturación: "
                            + e.getMessage()
            );
        }
    }

    public FacturaListadoResponse marcarTasacionEnviada(
            Long id
    ) {

        Factura factura =
                facturaRepository
                        .findById(
                                id
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Factura no encontrada"
                                )
                        );

        if (!"Tasación".equals(
                factura.getTipo()
        )) {

            throw new RuntimeException(
                    "La factura no es una tasación"
            );
        }

        factura.setEstadoTasacion(
                "Enviada"
        );

        Factura guardada =
                facturaRepository.save(
                        factura
                );

        Map<Long, String> nombres =
                new HashMap<>();

        empleadoRepository
                .findById(
                        guardada.getIdEmpleado()
                )
                .ifPresent(
                        empleado ->
                                nombres.put(
                                        empleado.getId(),
                                        empleado.getNombre()
                                )
                );

        return mapearFactura(
                guardada,
                nombres
        );
    }

    @Transactional
    public void eliminarFactura(
            Long id
    ) {

        Factura factura =
                facturaRepository
                        .findById(
                                id
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Factura no encontrada"
                                )
                        );

        Long empleadoId =
                factura.getIdEmpleado();

        LocalDateTime fechaFactura =
                factura.getFecha();

        facturaItemRepository
                .deleteByFacturaId(
                        id
                );

        facturaRepository.delete(
                factura
        );

        try {

            primasService
                    .recalcularPrimaEmpleadoSemana(
                            empleadoId,
                            fechaFactura
                    );

        } catch (Exception e) {

            System.err.println(
                    "Error recalculando prima tras eliminar factura: "
                            + e.getMessage()
            );
        }
    }

    private void guardarTasacion(
            Factura factura,
            CreateFacturaRequest request
    ) {

        Tasacion tasacion =
                new Tasacion();

        tasacion.setFacturaId(
                factura.getId()
        );

        tasacion.setModelo(
                request.getModelo()
        );

        tasacion.setEstado(
                request.getEstado()
        );

        tasacion.setOtros(
                request.getOtros()
        );

        tasacionRepository.save(
                tasacion
        );
    }

    public FacturasPageResponse listarFacturas(
            String fechaInicio,
            String fechaFin,
            String tipo,
            Long idEmpleado,
            int page,
            int size
    ) {

        LocalDateTime inicio =
                null;

        LocalDateTime fin =
                null;

        if (
                fechaInicio != null
                        && !fechaInicio.isBlank()
        ) {

            inicio =
                    LocalDate
                            .parse(
                                    fechaInicio
                            )
                            .atStartOfDay();
        }

        if (
                fechaFin != null
                        && !fechaFin.isBlank()
        ) {

            fin =
                    LocalDate
                            .parse(
                                    fechaFin
                            )
                            .atTime(
                                    LocalTime.MAX
                            );
        }

        int safePage =
                Math.max(
                        page,
                        0
                );

        int safeSize =
                switch (size) {

                    case 20, 50 ->
                            size;

                    default ->
                            10;
                };

        Pageable pageable =
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(
                                Sort.Direction.DESC,
                                "fecha"
                        )
                );

        Specification<Factura> spec =
                crearFiltroFacturas(
                        idEmpleado,
                        tipo,
                        inicio,
                        fin
                );

        Page<Factura> pagina =
                facturaRepository.findAll(
                        spec,
                        pageable
                );

        List<Factura> facturas =
                pagina.getContent();

        List<Long> idsEmpleados =
                facturas
                        .stream()
                        .map(
                                Factura::getIdEmpleado
                        )
                        .distinct()
                        .toList();

        Map<Long, String> nombresEmpleados =
                new HashMap<>();

        empleadoRepository
                .findAllById(
                        idsEmpleados
                )
                .forEach(
                        empleado ->
                                nombresEmpleados.put(
                                        empleado.getId(),
                                        empleado.getNombre()
                                )
                );

        List<FacturaListadoResponse> content =
                facturas
                        .stream()
                        .map(
                                factura ->
                                        mapearFactura(
                                                factura,
                                                nombresEmpleados
                                        )
                        )
                        .toList();

        long totalFacturado =
                facturaRepository
                        .findAll(
                                spec
                        )
                        .stream()
                        .map(
                                Factura::getTotal
                        )
                        .filter(
                                total ->
                                        total != null
                        )
                        .mapToLong(
                                Integer::longValue
                        )
                        .sum();

        FacturasPageResponse response =
                new FacturasPageResponse();

        response.setContent(
                content
        );

        response.setTotalElements(
                pagina.getTotalElements()
        );

        response.setTotalPages(
                pagina.getTotalPages()
        );

        response.setPage(
                pagina.getNumber()
        );

        response.setSize(
                pagina.getSize()
        );

        response.setTotalFacturado(
                totalFacturado
        );

        response.setPromedioFactura(
                pagina.getTotalElements() > 0
                        ? totalFacturado
                        / pagina.getTotalElements()
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

        return (
                root,
                query,
                cb
        ) -> {

            var predicates =
                    cb.conjunction();

            if (idEmpleado != null) {

                predicates =
                        cb.and(
                                predicates,
                                cb.equal(
                                        root.get(
                                                "idEmpleado"
                                        ),
                                        idEmpleado
                                )
                        );
            }

            if (
                    tipo != null
                            && !tipo.isBlank()
            ) {

                predicates =
                        cb.and(
                                predicates,
                                cb.equal(
                                        root.get(
                                                "tipo"
                                        ),
                                        tipo
                                )
                        );
            }

            if (inicio != null) {

                predicates =
                        cb.and(
                                predicates,
                                cb.greaterThanOrEqualTo(
                                        root.get(
                                                "fecha"
                                        ),
                                        inicio
                                )
                        );
            }

            if (fin != null) {

                predicates =
                        cb.and(
                                predicates,
                                cb.lessThanOrEqualTo(
                                        root.get(
                                                "fecha"
                                        ),
                                        fin
                                )
                        );
            }

            return predicates;
        };
    }

    private FacturaListadoResponse mapearFactura(
            Factura factura,
            Map<Long, String> nombresEmpleados
    ) {

        FacturaListadoResponse response =
                new FacturaListadoResponse();

        response.setId(
                factura.getId()
        );

        response.setIdEmpleado(
                factura.getIdEmpleado()
        );

        /*
         * Se envía como LocalDateTime sin sufijo Z.
         * Representa literalmente la hora de Madrid guardada.
         */
        response.setFecha(
                factura.getFecha() != null
                        ? factura
                                .getFecha()
                                .toString()
                        : null
        );

        response.setTipo(
                factura.getTipo()
        );

        response.setTotal(
                factura.getTotal()
        );

        response.setConvenio(
                factura.getConvenio()
        );

        response.setMatricula(
                factura.getMatricula()
        );

        response.setModelo(
                factura.getModelo()
        );

        response.setEstado(
                factura.getEstado()
        );

        response.setEstadoTasacion(
                factura.getEstadoTasacion() != null
                        ? factura.getEstadoTasacion()
                        : (
                            "Tasación".equals(
                                    factura.getTipo()
                            )
                                    ? "Pendiente"
                                    : null
                        )
        );

        response.setCantidad(
                factura.getCantidad()
        );

        response.setItem(
                factura.getItem()
        );

        response.setCategoria(
                factura.getCategoria()
        );

        response.setGravedad(
                factura.getGravedad()
        );

        response.setTuneoPlate(
                factura.getTuneoPlate()
        );

        response.setTuneoSeleccionados(
                factura.getTuneoSeleccionados()
        );

        response.setGrua(
                factura.getGrua()
        );

        response.setItems(
                facturaItemRepository
                        .findByFacturaIdOrderByIdAsc(
                                factura.getId()
                        )
                        .stream()
                        .map(
                                this::mapearFacturaItem
                        )
                        .toList()
        );

        response.setNombreEmpleado(
                nombresEmpleados
                        .getOrDefault(
                                factura.getIdEmpleado(),
                                "Desconocido"
                        )
        );

        return response;
    }

    private int calcularTotal(
            CreateFacturaRequest request
    ) {

        int total;

        switch (
                request.getTipo()
        ) {

            case "Reparación":
                total =
                        calcularReparacion(
                                request
                        );
                break;

            case "Items":
                total =
                        calcularItems(
                                request
                        );
                break;

            case "Tasación":
                total =
                        calcularTasacion(
                                request
                        );
                break;

            case "Full Tuning":
                total =
                        calcularFullTuning(
                                request
                        );
                break;

            case "Tuneo":
                total =
                        calcularTuneo(
                                request
                        );
                break;

            default:
                throw new RuntimeException(
                        "Tipo inválido"
                );
        }

        if (
                Boolean.TRUE.equals(
                        request.getConvenio()
                )
                        && !"Tasación".equals(
                                request.getTipo()
                        )
        ) {

            total =
                    (int) Math.round(
                            total * 0.8
                    );
        }

        return total;
    }

    private int calcularReparacion(
            CreateFacturaRequest request
    ) {

        Reparacion reparacion =
                reparacionRepository
                        .findAll()
                        .stream()
                        .filter(
                                reparacionCatalogo ->
                                        normalizar(
                                                reparacionCatalogo.getTipo()
                                        ).equals(
                                                normalizar(
                                                        request.getGravedad()
                                                )
                                        )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Reparación no encontrada"
                                )
                        );

        int total =
                reparacion.getPrecio();

        if (
                Boolean.TRUE.equals(
                        request.getGrua()
                )
        ) {

            total +=
                    PRECIO_GRUA;
        }

        return total;
    }

    private int calcularItems(
            CreateFacturaRequest request
    ) {

        Item item =
                buscarItem(
                        request.getItem()
                );

        int cantidad =
                request.getCantidad() != null
                        ? request.getCantidad()
                        : 1;

        if (cantidad <= 0) {

            throw new RuntimeException(
                    "La cantidad del item debe ser mayor que cero"
            );
        }

        int total =
                item
                        .getPrecio()
                        .multiply(
                                BigDecimal.valueOf(
                                        cantidad
                                )
                        )
                        .setScale(
                                0,
                                RoundingMode.HALF_UP
                        )
                        .intValue();

        if (
                Boolean.TRUE.equals(
                        request.getLspd()
                )
        ) {

            total =
                    (int) Math.round(
                            total * 0.90
                    );
        }

        return total;
    }

    private Item buscarItem(
            String nombreItem
    ) {

        if (
                nombreItem == null
                        || nombreItem.isBlank()
        ) {

            throw new RuntimeException(
                    "Item no encontrado"
            );
        }

        return itemRepository
                .findAll()
                .stream()
                .filter(
                        itemCatalogo ->
                                normalizar(
                                        itemCatalogo.getNombre()
                                ).equals(
                                        normalizar(
                                                nombreItem
                                        )
                                )
                )
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "Item no encontrado"
                        )
                );
    }

    private FacturaItemResponse mapearFacturaItem(
            FacturaItem linea
    ) {

        FacturaItemResponse response =
                new FacturaItemResponse();

        response.setId(
                linea.getId()
        );

        response.setItem(
                linea.getItem()
        );

        response.setCantidad(
                linea.getCantidad()
        );

        response.setPrecioUnitario(
                linea.getPrecioUnitario()
        );

        response.setSubtotal(
                linea.getSubtotal()
        );

        response.setLspd(
                linea.getLspd()
        );

        return response;
    }

    private int calcularTasacion(
            CreateFacturaRequest request
    ) {

        TasacionPrecio tasacion =
                tasacionPrecioRepository
                        .findAll()
                        .stream()
                        .filter(
                                precioTasacion ->
                                        normalizar(
                                                precioTasacion.getEstado()
                                        ).equals(
                                                normalizar(
                                                        request.getEstado()
                                                )
                                        )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Precio de tasación no encontrado"
                                )
                        );

        return tasacion
                .getPrecio()
                .intValue();
    }

    private int calcularFullTuning(
            CreateFacturaRequest request
    ) {

        FullTuning fullTuning =
                fullTuningRepository
                        .findAll()
                        .stream()
                        .filter(
                                fullTuningCatalogo ->
                                        normalizar(
                                                fullTuningCatalogo
                                                        .getCategoria()
                                        ).equals(
                                                normalizar(
                                                        request
                                                                .getCategoria()
                                                )
                                        )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Categoría de Full Tuning no encontrada"
                                )
                        );

        return fullTuning
                .getPrecio()
                .intValue();
    }

    private int calcularTuneo(
            CreateFacturaRequest request
    ) {

        if (
                request.getTuneoSeleccionados() == null
                        || request
                                .getTuneoSeleccionados()
                                .isBlank()
        ) {

            throw new RuntimeException(
                    "No se han seleccionado piezas de tuneo"
            );
        }

        String[] piezas =
                request
                        .getTuneoSeleccionados()
                        .split(",");

        BigDecimal total =
                BigDecimal.ZERO;

        boolean tieneRendimiento =
                false;

        for (String pieza : piezas) {

            if (
                    esRendimiento(
                            pieza
                    )
            ) {

                tieneRendimiento =
                        true;

                break;
            }
        }

        FullTuning fullTuning =
                null;

        if (tieneRendimiento) {

            if (
                    request.getCategoria() == null
                            || request
                                    .getCategoria()
                                    .isBlank()
            ) {

                throw new RuntimeException(
                        "La categoría es obligatoria para mejoras de rendimiento"
                );
            }

            fullTuning =
                    fullTuningRepository
                            .findAll()
                            .stream()
                            .filter(
                                    fullTuningCatalogo ->
                                            normalizar(
                                                    fullTuningCatalogo
                                                            .getCategoria()
                                            ).equals(
                                                    normalizar(
                                                            request
                                                                    .getCategoria()
                                                    )
                                            )
                            )
                            .findFirst()
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Categoría de tuneo no encontrada"
                                    )
                            );
        }

        for (String pieza : piezas) {

            if (
                    esRendimiento(
                            pieza
                    )
            ) {

                total =
                        total.add(
                                fullTuning
                                        .getPrecio()
                                        .multiply(
                                                PORCENTAJE_RENDIMIENTO
                                        )
                        );

            } else {

                Tuneo tuneo =
                        tuneoRepository
                                .findAll()
                                .stream()
                                .filter(
                                        tuneoCatalogo ->
                                                normalizar(
                                                        tuneoCatalogo
                                                                .getPieza()
                                                ).equals(
                                                        normalizar(
                                                                getClave(
                                                                        pieza
                                                                )
                                                        )
                                                )
                                )
                                .findFirst()
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Pieza de tuneo no encontrada"
                                        )
                                );

                total =
                        total.add(
                                tuneo.getPrecio()
                        );
            }
        }

        return total.intValue();
    }

    private boolean esRendimiento(
            String pieza
    ) {

        String valor =
                normalizar(
                        pieza
                );

        return valor.equals(
                "motor"
        )
                || valor.equals(
                        "frenos"
                )
                || valor.equals(
                        "transmision"
                )
                || valor.equals(
                        "suspension"
                )
                || valor.equals(
                        "blindaje"
                )
                || valor.equals(
                        "turbo"
                );
    }

    private String getClave(
            String pieza
    ) {

        String valor =
                normalizar(
                        pieza
                );

        if (
                valor.equals(
                        "pintura"
                )
        ) {

            return "Pintura";
        }

        if (
                valor.equals(
                        "livery"
                )
        ) {

            return "Vinilo";
        }

        if (
                valor.equals(
                        "pintura llantas"
                )
        ) {

            return "Pintura de ruedas";
        }

        return "Parte estetica";
    }

    private String normalizar(
            String valor
    ) {

        if (valor == null) {
            return "";
        }

        return Normalizer
                .normalize(
                        valor,
                        Normalizer.Form.NFD
                )
                .replaceAll(
                        "\\p{M}",
                        ""
                )
                .toLowerCase(
                        Locale.ROOT
                )
                .trim();
    }
}