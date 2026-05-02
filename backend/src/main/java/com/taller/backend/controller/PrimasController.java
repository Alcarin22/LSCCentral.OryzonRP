package com.taller.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.taller.backend.dto.AdminPrimaPagadaRequest;
import com.taller.backend.dto.AdminPrimaResponse;
import com.taller.backend.dto.MisPrimasResponse;
import com.taller.backend.service.PrimasService;

@RestController
public class PrimasController {

    private final PrimasService primasService;

    public PrimasController(PrimasService primasService) {
        this.primasService = primasService;
    }

    @GetMapping("/api/primas/{discordId}")
    public MisPrimasResponse getMisPrimas(
            @PathVariable String discordId,
            @RequestParam(defaultValue = "0") Integer weekOffset
    ) {
        return primasService.getMisPrimas(discordId, weekOffset);
    }

    @GetMapping("/api/admin/primas")
    public List<AdminPrimaResponse> listarPrimasAdmin(
            @RequestParam(required = false) Integer semana
    ) {
        return primasService.listarPrimasAdmin(semana);
    }

    @PatchMapping("/api/admin/primas/{primaId}/pagada")
    public AdminPrimaResponse actualizarPagada(
            @PathVariable Long primaId,
            @RequestBody AdminPrimaPagadaRequest request
    ) {
        return primasService.actualizarPagada(primaId, request);
    }
}