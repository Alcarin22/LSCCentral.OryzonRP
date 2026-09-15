package com.taller.backend.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.taller.backend.dto.ConvenioRequest;
import com.taller.backend.dto.ConvenioResponse;
import com.taller.backend.entity.Convenio;
import com.taller.backend.entity.Empleado;
import com.taller.backend.repository.EmpleadoRepository;
import com.taller.backend.service.ConvenioService;

@RestController
@RequestMapping("/api/convenios")
public class ConvenioController {

    private static final int NIVEL_CREAR_CONVENIOS = 3;
    private static final int NIVEL_ADMINISTRAR_CONVENIOS = 4;

    private final ConvenioService convenioService;
    private final EmpleadoRepository empleadoRepository;

    public ConvenioController(
            ConvenioService convenioService,
            EmpleadoRepository empleadoRepository
    ) {
        this.convenioService = convenioService;
        this.empleadoRepository = empleadoRepository;
    }

    @GetMapping
    public List<ConvenioResponse> listar() {
        return convenioService.listar();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ConvenioResponse crear(
            Authentication authentication,
            @RequestPart("datos") ConvenioRequest request,
            @RequestPart(value = "archivo", required = false)
            MultipartFile archivo
    ) {

        exigirNivelMinimo(
                authentication,
                NIVEL_CREAR_CONVENIOS,
                "No tienes permisos para añadir convenios"
        );

        return convenioService.crear(
                request,
                archivo
        );
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ConvenioResponse actualizarPut(
            Authentication authentication,
            @PathVariable Long id,
            @RequestPart("datos") ConvenioRequest request,
            @RequestPart(value = "archivo", required = false)
            MultipartFile archivo
    ) {

        exigirNivelMinimo(
                authentication,
                NIVEL_ADMINISTRAR_CONVENIOS,
                "No tienes permisos para editar convenios"
        );

        return convenioService.actualizar(
                id,
                request,
                archivo
        );
    }

    @PostMapping(
            value = "/{id}/actualizar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ConvenioResponse actualizarPost(
            Authentication authentication,
            @PathVariable Long id,
            @RequestPart("datos") ConvenioRequest request,
            @RequestPart(value = "archivo", required = false)
            MultipartFile archivo
    ) {

        exigirNivelMinimo(
                authentication,
                NIVEL_ADMINISTRAR_CONVENIOS,
                "No tienes permisos para editar convenios"
        );

        return convenioService.actualizar(
                id,
                request,
                archivo
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDelete(
            Authentication authentication,
            @PathVariable Long id
    ) {

        exigirNivelMinimo(
                authentication,
                NIVEL_ADMINISTRAR_CONVENIOS,
                "No tienes permisos para eliminar convenios"
        );

        convenioService.eliminar(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    @PostMapping("/{id}/eliminar")
    public ResponseEntity<Void> eliminarPost(
            Authentication authentication,
            @PathVariable Long id
    ) {

        exigirNivelMinimo(
                authentication,
                NIVEL_ADMINISTRAR_CONVENIOS,
                "No tienes permisos para eliminar convenios"
        );

        convenioService.eliminar(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<byte[]> verArchivo(
            @PathVariable Long id
    ) {

        Convenio convenio =
                convenioService.obtenerEntidad(id);

        byte[] contenido =
                convenioService.obtenerArchivo(id);

        ContentDisposition disposition =
                ContentDisposition
                        .inline()
                        .filename(
                                convenio.getArchivoNombre() != null
                                        ? convenio.getArchivoNombre()
                                        : "convenio.png",
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .body(contenido);
    }

    private void exigirNivelMinimo(
            Authentication authentication,
            int nivelMinimo,
            String mensaje
    ) {

        String discordId =
                obtenerDiscordIdAutenticado(
                        authentication
                );

        Empleado empleado =
                empleadoRepository
                        .findByDiscordId(discordId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Empleado no encontrado"
                                )
                        );

        if (
                empleado.getRango() == null
                        || empleado.getRango().getNivel() == null
                        || empleado.getRango().getNivel()
                                < nivelMinimo
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    mensaje
            );
        }
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