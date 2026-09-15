package com.taller.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.taller.backend.dto.CreateFacturaRequest;
import com.taller.backend.dto.CreateFacturacionLoteRequest;
import com.taller.backend.dto.CreateFacturacionLoteResponse;
import com.taller.backend.dto.FacturaListadoResponse;
import com.taller.backend.dto.FacturasPageResponse;
import com.taller.backend.entity.Factura;
import com.taller.backend.service.FacturaService;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(
            FacturaService facturaService
    ) {
        this.facturaService = facturaService;
    }

    @PostMapping
    public Factura crearFactura(
            Authentication authentication,
            @RequestBody CreateFacturaRequest request
    ) {

        String discordId =
                obtenerDiscordIdAutenticado(
                        authentication
                );

        return facturaService.crearFactura(
                request,
                discordId
        );
    }

    @PostMapping("/lote")
    public CreateFacturacionLoteResponse crearFacturacionLote(
            Authentication authentication,
            @RequestBody CreateFacturacionLoteRequest request
    ) {

        String discordId =
                obtenerDiscordIdAutenticado(
                        authentication
                );

        return facturaService.crearFacturacionLote(
                request,
                discordId
        );
    }

    @GetMapping
    public FacturasPageResponse listarFacturas(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Long idEmpleado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return facturaService.listarFacturas(
                fechaInicio,
                fechaFin,
                tipo,
                idEmpleado,
                page,
                size
        );
    }

    @PatchMapping("/{id}/tasacion/enviada")
    public FacturaListadoResponse marcarTasacionEnviada(
            @PathVariable Long id
    ) {

        return facturaService.marcarTasacionEnviada(
                id
        );
    }

    @DeleteMapping("/{id}")
    public void eliminarFactura(
            @PathVariable Long id
    ) {

        facturaService.eliminarFactura(
                id
        );
    }

    private String obtenerDiscordIdAutenticado(
            Authentication authentication
    ) {

        if (
                authentication == null
                        || !authentication.isAuthenticated()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Sesión no autenticada"
            );
        }

        String discordId =
                authentication.getName();

        if (
                discordId == null
                        || discordId.isBlank()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no identificado"
            );
        }

        return discordId;
    }
}