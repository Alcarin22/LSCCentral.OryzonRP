package com.taller.backend.service;

import com.taller.backend.dto.CreateFacturaRequest;
import com.taller.backend.entity.Empleado;
import com.taller.backend.entity.Factura;
import com.taller.backend.repository.EmpleadoRepository;
import com.taller.backend.repository.FacturaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final EmpleadoRepository empleadoRepository;

    public FacturaService(FacturaRepository facturaRepository, EmpleadoRepository empleadoRepository) {
        this.facturaRepository = facturaRepository;
        this.empleadoRepository = empleadoRepository;
    }

    public Factura crearFactura(CreateFacturaRequest request) {
        Empleado empleado = empleadoRepository.findByDiscordId(request.getDiscordId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado para ese discordId"));

        Factura factura = new Factura();
        factura.setIdEmpleado(empleado.getId());
        factura.setFecha(LocalDateTime.now());
        factura.setMatricula(request.getMatricula());
        factura.setTipo(request.getTipo());
        factura.setTotal(request.getTotal());
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
}