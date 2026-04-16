package com.taller.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestParam(defaultValue = "0") Integer weekOffset) {
        return primasService.getMisPrimas(discordId, weekOffset);
    }
}