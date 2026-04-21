package com.taller.backend.controller;

import com.taller.backend.dto.CreateFacturaRequest;
import com.taller.backend.entity.Factura;
import com.taller.backend.service.FacturaService;
import org.springframework.web.bind.annotation.*;

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
}