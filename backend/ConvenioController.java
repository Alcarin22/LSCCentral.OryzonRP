package com.taller.backend.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.taller.backend.dto.ConvenioRequest;
import com.taller.backend.dto.ConvenioResponse;
import com.taller.backend.entity.Convenio;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ConvenioResponse crear(
            @RequestPart("datos") ConvenioRequest request,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo
    ) {
        return convenioService.crear(request, archivo);
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ConvenioResponse actualizar(
            @PathVariable Long id,
            @RequestPart("datos") ConvenioRequest request,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo
    ) {
        return convenioService.actualizar(id, request, archivo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {
        convenioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<byte[]> verArchivo(
            @PathVariable Long id
    ) {
        Convenio convenio = convenioService.obtenerEntidad(id);

        byte[] contenido = convenio.getArchivoContenido();

        if (contenido == null || contenido.length == 0) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        if (convenio.getArchivoTipoMime() != null) {
            try {
                mediaType = MediaType.parseMediaType(
                        convenio.getArchivoTipoMime()
                );
            } catch (Exception ignored) {
                // Si el MIME guardado no es válido, se usa octet-stream.
            }
        }

        ContentDisposition disposition = ContentDisposition
                .inline()
                .filename(
                        convenio.getArchivoNombre() != null
                                ? convenio.getArchivoNombre()
                                : "archivo",
                        StandardCharsets.UTF_8
                )
                .build();

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .body(contenido);
    }
}
