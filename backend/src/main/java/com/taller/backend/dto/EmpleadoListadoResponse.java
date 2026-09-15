package com.taller.backend.dto;

public class EmpleadoListadoResponse {

    private Long id;
    private String nombre;
    private Boolean activo;

    public EmpleadoListadoResponse() {
    }

    public EmpleadoListadoResponse(
            Long id,
            String nombre,
            Boolean activo
    ) {
        this.id = id;
        this.nombre = nombre;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}