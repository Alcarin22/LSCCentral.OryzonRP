package com.taller.backend.dto;

import java.time.LocalDate;

public class EmpleadoAdminResponse {

    private Long id;
    private String discordId;
    private String nombre;
    private Boolean activo;
    private Long rangoId;
    private String rangoNombre;
    private Integer rangoNivel;
    private Boolean puedeTrabajarComoSeguridad;
    private LocalDate fechaContratacion;
    private LocalDate fechaUltimoAscenso;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
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

    public Long getRangoId() {
        return rangoId;
    }

    public void setRangoId(Long rangoId) {
        this.rangoId = rangoId;
    }

    public String getRangoNombre() {
        return rangoNombre;
    }

    public void setRangoNombre(String rangoNombre) {
        this.rangoNombre = rangoNombre;
    }

    public Integer getRangoNivel() {
        return rangoNivel;
    }

    public void setRangoNivel(Integer rangoNivel) {
        this.rangoNivel = rangoNivel;
    }
    public Boolean getPuedeTrabajarComoSeguridad() {
        return puedeTrabajarComoSeguridad;
    }

    public void setPuedeTrabajarComoSeguridad(Boolean puedeTrabajarComoSeguridad) {
        this.puedeTrabajarComoSeguridad = puedeTrabajarComoSeguridad;
    }
    public LocalDate getFechaContratacion() { return fechaContratacion; }
    public void setFechaContratacion(LocalDate fechaContratacion) { this.fechaContratacion = fechaContratacion; }
    public LocalDate getFechaUltimoAscenso() { return fechaUltimoAscenso; }
    public void setFechaUltimoAscenso(LocalDate fechaUltimoAscenso) { this.fechaUltimoAscenso = fechaUltimoAscenso; }
}
