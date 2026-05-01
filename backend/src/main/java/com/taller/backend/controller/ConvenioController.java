package com.taller.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taller.backend.dto.ConvenioRequest;
import com.taller.backend.dto.ConvenioResponse;
import com.taller.backend.service.ConvenioService;

@RestController
@RequestMapping("/api/convenios")
public class ConvenioController {

    private final ConvenioService convenioService;

    public ConvenioController(ConvenioService convenioService) {
        this.convenioService = convenioService;
    }

    @GetMapping
    public List<ConvenioResponse> listar() {
        return convenioService.listar();
    }

    @PostMapping
    public ConvenioResponse crear(@RequestBody ConvenioRequest request) {
        return convenioService.crear(request);
    }

    @PutMapping("/{id}")
    public ConvenioResponse actualizar(
            @PathVariable Long id,
            @RequestBody ConvenioRequest request
    ) {
        return convenioService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        convenioService.eliminar(id);
    }
}