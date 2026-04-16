package com.taller.backend.dto;

public class PrimaDiaResponse {

    private String dia;
    private String fecha;
    private String horas;
    private Integer servicios;
    private Integer facturacion;
    private Integer prima;

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHoras() {
        return horas;
    }

    public void setHoras(String horas) {
        this.horas = horas;
    }

    public Integer getServicios() {
        return servicios;
    }

    public void setServicios(Integer servicios) {
        this.servicios = servicios;
    }

    public Integer getFacturacion() {
        return facturacion;
    }

    public void setFacturacion(Integer facturacion) {
        this.facturacion = facturacion;
    }

    public Integer getPrima() {
        return prima;
    }

    public void setPrima(Integer prima) {
        this.prima = prima;
    }
}