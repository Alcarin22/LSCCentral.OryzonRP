package com.taller.backend.dto;

public class DashboardMesResponse {

    private String horasTotales;
    private Integer jornadasCompletadas;
    private Integer serviciosRealizados;
    private String rendimiento;

    public String getHorasTotales() {
        return horasTotales;
    }

    public void setHorasTotales(String horasTotales) {
        this.horasTotales = horasTotales;
    }

    public Integer getJornadasCompletadas() {
        return jornadasCompletadas;
    }

    public void setJornadasCompletadas(Integer jornadasCompletadas) {
        this.jornadasCompletadas = jornadasCompletadas;
    }

    public Integer getServiciosRealizados() {
        return serviciosRealizados;
    }

    public void setServiciosRealizados(Integer serviciosRealizados) {
        this.serviciosRealizados = serviciosRealizados;
    }

    public String getRendimiento() {
        return rendimiento;
    }

    public void setRendimiento(String rendimiento) {
        this.rendimiento = rendimiento;
    }
}