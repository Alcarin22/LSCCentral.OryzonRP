package com.taller.backend.dto;

public class DashboardSemanaResponse {

    private String horasRegistradas;
    private Integer diasTrabajados;
    private Integer serviciosCompletados;
    private String primaEstimada;

    public String getHorasRegistradas() {
        return horasRegistradas;
    }

    public void setHorasRegistradas(String horasRegistradas) {
        this.horasRegistradas = horasRegistradas;
    }

    public Integer getDiasTrabajados() {
        return diasTrabajados;
    }

    public void setDiasTrabajados(Integer diasTrabajados) {
        this.diasTrabajados = diasTrabajados;
    }

    public Integer getServiciosCompletados() {
        return serviciosCompletados;
    }

    public void setServiciosCompletados(Integer serviciosCompletados) {
        this.serviciosCompletados = serviciosCompletados;
    }

    public String getPrimaEstimada() {
        return primaEstimada;
    }

    public void setPrimaEstimada(String primaEstimada) {
        this.primaEstimada = primaEstimada;
    }
}