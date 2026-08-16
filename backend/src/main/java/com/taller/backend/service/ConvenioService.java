package com.taller.backend.service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.taller.backend.dto.ConvenioRequest;
import com.taller.backend.dto.ConvenioResponse;
import com.taller.backend.entity.Convenio;
import com.taller.backend.repository.ConvenioRepository;

@Service
public class ConvenioService {

    private static final Set<String> CATEGORIAS_VALIDAS = Set.of(
            "Estado",
            "Talleres",
            "Ocio",
            "Alimentación",
            "Otros"
    );

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            "Activo",
            "Inactivo"
    );

    private static final long TAMANO_MAXIMO_ARCHIVO = 10L * 1024L * 1024L;

    private final ConvenioRepository convenioRepository;

    public ConvenioService(ConvenioRepository convenioRepository) {
        this.convenioRepository = convenioRepository;
    }

    @Transactional(readOnly = true)
    public List<ConvenioResponse> listar() {
        return convenioRepository
                .findAllByOrderByCategoriaAscLocalAsc()
                .stream()
                .map(this::mapearResponse)
                .toList();
    }

    @Transactional
    public ConvenioResponse crear(
            ConvenioRequest request,
            MultipartFile archivo
    ) {
        validarRequest(request);

        Convenio convenio = new Convenio();
        aplicarDatos(convenio, request);
        aplicarArchivo(convenio, archivo);

        return mapearResponse(
                convenioRepository.save(convenio)
        );
    }

    @Transactional(readOnly = true)
    public Convenio obtenerEntidad(Long id) {
        return convenioRepository
                .findById(id)
                .orElseThrow(
                        () -> new RuntimeException("Convenio no encontrado")
                );
    }

    private void aplicarDatos(
            Convenio convenio,
            ConvenioRequest request
    ) {
        convenio.setLocal(limpiar(request.getLocal()));
        convenio.setCategoria(limpiar(request.getCategoria()));
        convenio.setEstado(limpiar(request.getEstado()));
        convenio.setCondicionesLsc(limpiar(request.getCondicionesLsc()));
        convenio.setCondicionesLocal(limpiar(request.getCondicionesLocal()));
    }

    private void aplicarArchivo(
            Convenio convenio,
            MultipartFile archivo
    ) {
        if (archivo == null || archivo.isEmpty()) {
            convenio.setArchivoNombre(null);
            convenio.setArchivoTipoMime(null);
            convenio.setArchivoContenido(null);
            return;
        }

        if (archivo.getSize() > TAMANO_MAXIMO_ARCHIVO) {
            throw new RuntimeException(
                    "El archivo no puede superar los 10 MB"
            );
        }

        try {
            convenio.setArchivoNombre(
                    limpiarNombreArchivo(archivo.getOriginalFilename())
            );

            convenio.setArchivoTipoMime(
                    archivo.getContentType() != null
                            ? archivo.getContentType()
                            : "application/octet-stream"
            );

            convenio.setArchivoContenido(
                    archivo.getBytes()
            );
        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo guardar el archivo del convenio",
                    e
            );
        }
    }

    private ConvenioResponse mapearResponse(Convenio convenio) {
        ConvenioResponse response = new ConvenioResponse();

        response.setId(convenio.getId());
        response.setLocal(convenio.getLocal());
        response.setCategoria(convenio.getCategoria());
        response.setEstado(convenio.getEstado());
        response.setCondicionesLsc(convenio.getCondicionesLsc());
        response.setCondicionesLocal(convenio.getCondicionesLocal());

        boolean tieneArchivo =
                convenio.getArchivoContenido() != null
                        && convenio.getArchivoContenido().length > 0;

        response.setTieneArchivo(tieneArchivo);
        response.setArchivoNombre(convenio.getArchivoNombre());
        response.setArchivoTipoMime(convenio.getArchivoTipoMime());

        return response;
    }

    private void validarRequest(ConvenioRequest request) {
        if (request == null) {
            throw new RuntimeException(
                    "Los datos del convenio son obligatorios"
            );
        }

        if (esVacio(request.getLocal())) {
            throw new RuntimeException(
                    "El local es obligatorio"
            );
        }

        if (!CATEGORIAS_VALIDAS.contains(request.getCategoria())) {
            throw new RuntimeException(
                    "La categoría del convenio no es válida"
            );
        }

        if (!ESTADOS_VALIDOS.contains(request.getEstado())) {
            throw new RuntimeException(
                    "El estado del convenio no es válido"
            );
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

    private String limpiarNombreArchivo(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "archivo";
        }

        String normalizado = nombre
                .replace('\\', '/')
                .trim();

        int ultimaBarra = normalizado.lastIndexOf('/');

        if (ultimaBarra >= 0) {
            normalizado = normalizado.substring(ultimaBarra + 1);
        }

        if (normalizado.isBlank()) {
            return "archivo";
        }

        return normalizado.toLowerCase(Locale.ROOT).endsWith(".exe")
                ? "archivo"
                : normalizado;
    }
}
