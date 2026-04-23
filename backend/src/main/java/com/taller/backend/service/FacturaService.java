package com.taller.backend.service;

import com.taller.backend.dto.CreateFacturaRequest;
import com.taller.backend.entity.Empleado;
import com.taller.backend.entity.Factura;
import com.taller.backend.entity.Reparacion;
import com.taller.backend.repository.EmpleadoRepository;
import com.taller.backend.repository.FacturaRepository;
import com.taller.backend.repository.ReparacionRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ReparacionRepository reparacionRepository;

    public FacturaService(
            FacturaRepository facturaRepository,
            EmpleadoRepository empleadoRepository,
            ReparacionRepository reparacionRepository) {
        this.facturaRepository = facturaRepository;
        this.empleadoRepository = empleadoRepository;
        this.reparacionRepository = reparacionRepository;
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
        factura.setConvenio(Boolean.TRUE.equals(request.getConvenio()));

        factura.setModelo(request.getModelo());
        factura.setEstado(request.getEstado());
        factura.setCantidad(request.getCantidad());
        factura.setItem(request.getItem());
        factura.setCategoria(request.getCategoria());
        factura.setGravedad(request.getGravedad());
        factura.setTuneoPlate(request.getTuneoPlate());
        factura.setTuneoSeleccionados(request.getTuneoSeleccionados());

        return facturaRepository.save(factura);
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

            default:
                if (request.getTotal() == null) {
                    throw new RuntimeException("No se pudo calcular el total de la factura");
                }
                totalBase = request.getTotal();
                break;
        }

        if (Boolean.TRUE.equals(request.getConvenio())) {
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

        return reparacion.getPrecio();
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