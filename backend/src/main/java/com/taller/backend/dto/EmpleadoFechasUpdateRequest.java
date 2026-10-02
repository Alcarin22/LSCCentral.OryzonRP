package com.taller.backend.dto;

import java.time.LocalDate;

public class EmpleadoFechasUpdateRequest {
    private LocalDate fechaContratacion;
    private LocalDate fechaUltimoAscenso;
    public LocalDate getFechaContratacion() { return fechaContratacion; }
    public void setFechaContratacion(LocalDate fechaContratacion) { this.fechaContratacion = fechaContratacion; }
    public LocalDate getFechaUltimoAscenso() { return fechaUltimoAscenso; }
    public void setFechaUltimoAscenso(LocalDate fechaUltimoAscenso) { this.fechaUltimoAscenso = fechaUltimoAscenso; }
}
