package com.taller.backend.dto;

public class EmpleadoAdminUpdateRequest {

    private Long rangoId;
    private Boolean activo;
    private Boolean puedeTrabajarComoSeguridad;

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
}
