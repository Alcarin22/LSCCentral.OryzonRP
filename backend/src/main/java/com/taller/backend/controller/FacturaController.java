package com.taller.backend.controller;

import com.taller.backend.dto.CreateFacturaRequest;
import com.taller.backend.dto.FacturaListadoResponse;
import com.taller.backend.entity.Factura;
import com.taller.backend.service.FacturaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @PostMapping
    public Factura crearFactura(@RequestBody CreateFacturaRequest request) {
        return facturaService.crearFactura(request);
    }

    @GetMapping
    public List<FacturaListadoResponse> listarFacturas(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Long idEmpleado) {
        return facturaService.listarFacturas(fechaInicio, fechaFin, tipo, idEmpleado);
    }
}