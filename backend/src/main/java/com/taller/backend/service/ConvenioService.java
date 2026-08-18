package com.taller.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    private static final String MIME_PNG = "image/png";

    private final ConvenioRepository convenioRepository;
    private final Path carpetaConvenios;

    public ConvenioService(ConvenioRepository convenioRepository) {
        this.convenioRepository = convenioRepository;

        String raizUploads = System.getenv("RAILWAY_VOLUME_MOUNT_PATH");

        if (raizUploads == null || raizUploads.isBlank()) {
            raizUploads = "uploads";
        }

        this.carpetaConvenios = Path.of(raizUploads, "convenios")
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.carpetaConvenios);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo crear la carpeta de archivos de convenios: "
                            + this.carpetaConvenios,
                    e
            );
        }
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

        if (archivo != null && !archivo.isEmpty()) {
            aplicarArchivo(convenio, archivo);
        }

        return mapearResponse(
                convenioRepository.save(convenio)
        );
    }

    @Transactional
    public ConvenioResponse actualizar(
            Long id,
            ConvenioRequest request,
            MultipartFile archivo
    ) {
        validarRequest(request);

        Convenio convenio = obtenerEntidad(id);
        aplicarDatos(convenio, request);

        if (archivo != null && !archivo.isEmpty()) {
            String rutaAnterior = convenio.getArchivoRuta();
            aplicarArchivo(convenio, archivo);
            eliminarArchivoFisico(rutaAnterior);
        }

        return mapearResponse(
                convenioRepository.save(convenio)
        );
    }

    @Transactional
    public void eliminar(Long id) {
        Convenio convenio = obtenerEntidad(id);
        String rutaArchivo = convenio.getArchivoRuta();

        convenioRepository.delete(convenio);
        convenioRepository.flush();

        eliminarArchivoFisico(rutaArchivo);
    }

    @Transactional(readOnly = true)
    public Convenio obtenerEntidad(Long id) {
        return convenioRepository
                .findById(id)
                .orElseThrow(
                        () -> new RuntimeException("Convenio no encontrado")
                );
    }

    @Transactional(readOnly = true)
    public byte[] obtenerArchivo(Long id) {
        Convenio convenio = obtenerEntidad(id);

        if (convenio.getArchivoRuta() == null
                || convenio.getArchivoRuta().isBlank()) {
            throw new RuntimeException(
                    "El convenio no tiene ninguna imagen asociada"
            );
        }

        Path ruta = resolverRutaGuardada(convenio.getArchivoRuta());

        if (!Files.exists(ruta) || !Files.isRegularFile(ruta)) {
            throw new RuntimeException(
                    "La imagen del convenio no existe en el almacenamiento"
            );
        }

        try {
            return Files.readAllBytes(ruta);
        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo leer la imagen del convenio",
                    e
            );
        }
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
        validarArchivoPng(archivo);

        String nombreOriginal = limpiarNombreArchivo(
                archivo.getOriginalFilename()
        );

        String nombreFisico = UUID.randomUUID() + ".png";
        Path destino = carpetaConvenios
                .resolve(nombreFisico)
                .normalize();

        if (!destino.startsWith(carpetaConvenios)) {
            throw new RuntimeException("Ruta de archivo no válida");
        }

        try {
            Files.createDirectories(carpetaConvenios);
            Files.copy(
                    archivo.getInputStream(),
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo guardar la imagen del convenio",
                    e
            );
        }

        convenio.setArchivoNombre(nombreOriginal);
        convenio.setArchivoTipoMime(MIME_PNG);
        convenio.setArchivoRuta("convenios/" + nombreFisico);
    }

    private void validarArchivoPng(MultipartFile archivo) {
        if (archivo.getSize() > TAMANO_MAXIMO_ARCHIVO) {
            throw new RuntimeException(
                    "La imagen no puede superar los 10 MB"
            );
        }

        String nombre = archivo.getOriginalFilename();

        boolean extensionPng = nombre != null
                && nombre.toLowerCase().endsWith(".png");

        boolean mimePng = MIME_PNG.equalsIgnoreCase(
                archivo.getContentType()
        );

        if (!extensionPng || !mimePng) {
            throw new RuntimeException(
                    "Solo se permiten imágenes en formato PNG"
            );
        }
    }

    private void eliminarArchivoFisico(String rutaGuardada) {
        if (rutaGuardada == null || rutaGuardada.isBlank()) {
            return;
        }

        Path ruta = resolverRutaGuardada(rutaGuardada);

        try {
            Files.deleteIfExists(ruta);
        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo eliminar la imagen física del convenio",
                    e
            );
        }
    }

    private Path resolverRutaGuardada(String rutaGuardada) {
        String relativa = rutaGuardada
                .replace('\\', '/')
                .trim();

        if (relativa.startsWith("convenios/")) {
            relativa = relativa.substring("convenios/".length());
        }

        Path ruta = carpetaConvenios
                .resolve(relativa)
                .normalize();

        if (!ruta.startsWith(carpetaConvenios)) {
            throw new RuntimeException("Ruta de archivo no válida");
        }

        return ruta;
    }

    private ConvenioResponse mapearResponse(Convenio convenio) {
        ConvenioResponse response = new ConvenioResponse();

        response.setId(convenio.getId());
        response.setLocal(convenio.getLocal());
        response.setCategoria(convenio.getCategoria());
        response.setEstado(convenio.getEstado());
        response.setCondicionesLsc(convenio.getCondicionesLsc());
        response.setCondicionesLocal(convenio.getCondicionesLocal());

        boolean tieneArchivo = convenio.getArchivoRuta() != null
                && !convenio.getArchivoRuta().isBlank();

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
            return "convenio.png";
        }

        String normalizado = nombre
                .replace('\\', '/')
                .trim();

        int ultimaBarra = normalizado.lastIndexOf('/');

        if (ultimaBarra >= 0) {
            normalizado = normalizado.substring(ultimaBarra + 1);
        }

        return normalizado.isBlank()
                ? "convenio.png"
                : normalizado;
    }
}
