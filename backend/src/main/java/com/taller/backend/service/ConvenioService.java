package com.taller.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taller.backend.dto.ConvenioRequest;
import com.taller.backend.dto.ConvenioResponse;
import com.taller.backend.entity.Convenio;
import com.taller.backend.repository.ConvenioRepository;

@Service
public class ConvenioService {

    private final ConvenioRepository convenioRepository;

    public ConvenioService(ConvenioRepository convenioRepository) {
        this.convenioRepository = convenioRepository;
    }

    @Transactional(readOnly = true)
    public List<ConvenioResponse> listar() {
        return convenioRepository.findAllByOrderByCategoriaAscNombreAsc()
                .stream()
                .map(this::mapearResponse)
                .toList();
    }

    @Transactional
    public ConvenioResponse crear(ConvenioRequest request) {
        validarRequest(request);

        Convenio convenio = new Convenio();
        aplicarDatos(convenio, request);

        Convenio guardado = convenioRepository.save(convenio);
        return mapearResponse(guardado);
    }

    @Transactional
    public ConvenioResponse actualizar(Long id, ConvenioRequest request) {
        validarRequest(request);

        Convenio convenio = convenioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Convenio no encontrado"));

        aplicarDatos(convenio, request);

        Convenio actualizado = convenioRepository.save(convenio);
        return mapearResponse(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!convenioRepository.existsById(id)) {
            throw new RuntimeException("Convenio no encontrado");
        }

        convenioRepository.deleteById(id);
    }

    private void aplicarDatos(Convenio convenio, ConvenioRequest request) {
        convenio.setNombre(limpiar(request.getNombre()));
        convenio.setCategoria(limpiar(request.getCategoria()));
        convenio.setEstado(limpiar(request.getEstado()));
        convenio.setDescuento(limpiar(request.getDescuento()));
        convenio.setContacto(limpiar(request.getContacto()));
        convenio.setDescripcion(limpiar(request.getDescripcion()));
        convenio.setDocumentoUrl(limpiar(request.getDocumentoUrl()));

        List<String> condiciones = request.getCondiciones() != null
                ? request.getCondiciones()
                : new ArrayList<>();

        convenio.setCondiciones(
                condiciones.stream()
                        .map(this::limpiar)
                        .filter(valor -> valor != null && !valor.isBlank())
                        .toList()
        );
    }

    private ConvenioResponse mapearResponse(Convenio convenio) {
        ConvenioResponse response = new ConvenioResponse();

        response.setId(convenio.getId());
        response.setNombre(convenio.getNombre());
        response.setCategoria(convenio.getCategoria());
        response.setEstado(convenio.getEstado());
        response.setDescuento(convenio.getDescuento());
        response.setContacto(convenio.getContacto());
        response.setDescripcion(convenio.getDescripcion());
        response.setDocumentoUrl(convenio.getDocumentoUrl());
        response.setCondiciones(convenio.getCondiciones());

        return response;
    }

    private void validarRequest(ConvenioRequest request) {
        if (request == null) {
            throw new RuntimeException("El convenio no puede estar vacío");
        }

        if (esVacio(request.getNombre())) {
            throw new RuntimeException("El nombre del convenio es obligatorio");
        }

        if (esVacio(request.getCategoria())) {
            throw new RuntimeException("La categoría del convenio es obligatoria");
        }

        if (esVacio(request.getEstado())) {
            throw new RuntimeException("El estado del convenio es obligatorio");
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private String limpiar(String valor) {
        if (valor == null) {
            return null;
        }

        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}