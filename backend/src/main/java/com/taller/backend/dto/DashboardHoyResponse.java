package com.taller.backend.dto;

public class DashboardHoyResponse {

    private String horaEntrada;
    private Boolean fichajeActivo;
    private Integer serviciosRealizadosHoy;

    public String getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(String horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public Boolean getFichajeActivo() {
        return fichajeActivo;
    }

    public void setFichajeActivo(Boolean fichajeActivo) {
        this.fichajeActivo = fichajeActivo;
    }

    public Integer getServiciosRealizadosHoy() {
        return serviciosRealizadosHoy;
    }

    public void setServiciosRealizadosHoy(Integer serviciosRealizadosHoy) {
        this.serviciosRealizadosHoy = serviciosRealizadosHoy;
    }
}