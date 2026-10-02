package com.taller.backend.dto;

import java.time.LocalDate;

public class EmpleadoAdminUpdateRequest {

    private Long rangoId;
    private Boolean activo;
    private Boolean puedeTrabajarComoSeguridad;
    private LocalDate fechaContratacion;
    private LocalDate fechaUltimoAscenso;

    public Long getRangoId() {
        return rangoId;
    }

    public void setRangoId(Long rangoId) {
        this.rangoId = rangoId;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
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
