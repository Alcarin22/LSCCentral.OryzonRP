package com.taller.backend.dto;

public class EmpleadoAdminUpdateRequest {

    private Long rangoId;
    private Boolean activo;

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
}