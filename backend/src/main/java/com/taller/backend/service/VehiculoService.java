package com.taller.backend.service;

import com.taller.backend.dto.VehiculoAdminRequest;
import com.taller.backend.dto.VehiculoAdminResponse;
import com.taller.backend.entity.Vehiculo;
import com.taller.backend.repository.VehiculoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }

    @Transactional(readOnly = true)
    public List<VehiculoAdminResponse> listarTodos() {
        return vehiculoRepository.findAllByOrderByMarcaAscModeloAsc()
                .stream()
                .map(this::mapearVehiculo)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VehiculoAdminResponse> listarActivos() {
        return vehiculoRepository.findByActivoTrueOrderByMarcaAscModeloAsc()
                .stream()
                .map(this::mapearVehiculo)
                .toList();
    }

    @Transactional
    public VehiculoAdminResponse crearVehiculo(VehiculoAdminRequest request) {
        validarRequest(request);

        boolean existe = vehiculoRepository.existsByMarcaIgnoreCaseAndModeloIgnoreCase(
                request.getMarca().trim(),
                request.getModelo().trim()
        );

        if (existe) {
            throw new RuntimeException("Ya existe un vehículo con esa marca y modelo");
        }

        Vehiculo vehiculo = new Vehiculo();

        vehiculo.setMarca(limpiar(request.getMarca()));
        vehiculo.setModelo(limpiar(request.getModelo()));
        vehiculo.setCategoria(limpiar(request.getCategoria()));
        vehiculo.setPrecio(request.getPrecio());
        vehiculo.setImagenUrl(limpiarOpcional(request.getImagenUrl()));
        vehiculo.setActivo(request.getActivo() == null || request.getActivo());

        return mapearVehiculo(vehiculoRepository.save(vehiculo));
    }

    @Transactional
    public VehiculoAdminResponse actualizarVehiculo(Long id, VehiculoAdminRequest request) {
        validarRequest(request);

        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehículo no encontrado"));

        vehiculo.setMarca(limpiar(request.getMarca()));
        vehiculo.setModelo(limpiar(request.getModelo()));
        vehiculo.setCategoria(limpiar(request.getCategoria()));
        vehiculo.setPrecio(request.getPrecio());
        vehiculo.setImagenUrl(limpiarOpcional(request.getImagenUrl()));

        if (request.getActivo() != null) {
            vehiculo.setActivo(request.getActivo());
        }

        return mapearVehiculo(vehiculoRepository.save(vehiculo));
    }

    @Transactional
    public VehiculoAdminResponse cambiarActivo(Long id, Boolean activo) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehículo no encontrado"));

        vehiculo.setActivo(Boolean.TRUE.equals(activo));

        return mapearVehiculo(vehiculoRepository.save(vehiculo));
    }

    private void validarRequest(VehiculoAdminRequest request) {
        if (request == null) {
            throw new RuntimeException("Datos de vehículo inválidos");
        }

        if (request.getMarca() == null || request.getMarca().trim().isBlank()) {
            throw new RuntimeException("La marca es obligatoria");
        }

        if (request.getModelo() == null || request.getModelo().trim().isBlank()) {
            throw new RuntimeException("El modelo es obligatorio");
        }

        if (request.getCategoria() == null || request.getCategoria().trim().isBlank()) {
            throw new RuntimeException("La categoría es obligatoria");
        }

        if (request.getPrecio() == null || request.getPrecio() < 0) {
            throw new RuntimeException("El precio debe ser igual o superior a 0");
        }
    }

    private String limpiar(String value) {
        return value.trim();
    }

    private String limpiarOpcional(String value) {
        if (value == null) {
            return null;
        }

        String limpio = value.trim();

        return limpio.isBlank() ? null : limpio;
    }

    private VehiculoAdminResponse mapearVehiculo(Vehiculo vehiculo) {
        VehiculoAdminResponse response = new VehiculoAdminResponse();

        response.setId(vehiculo.getId());
        response.setMarca(vehiculo.getMarca());
        response.setModelo(vehiculo.getModelo());
        response.setCategoria(vehiculo.getCategoria());
        response.setPrecio(vehiculo.getPrecio());
        response.setImagenUrl(vehiculo.getImagenUrl());
        response.setActivo(vehiculo.getActivo());

        response.setCreatedAt(
                vehiculo.getCreatedAt() != null
                        ? vehiculo.getCreatedAt().toString()
                        : null
        );

        response.setUpdatedAt(
                vehiculo.getUpdatedAt() != null
                        ? vehiculo.getUpdatedAt().toString()
                        : null
        );

        return response;
    }
}