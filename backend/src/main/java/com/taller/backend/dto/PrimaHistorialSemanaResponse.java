package com.taller.backend.dto;

public class PrimaHistorialSemanaResponse {

    private String semana;
    private String rangoFechas;
    private String horas;
    private int servicios;
    private int facturacion;
    private int prima;
    private int primaBase;
    private int extraHoras;

    public String getSemana() {
        return semana;
    }

    public void setSemana(String semana) {
        this.semana = semana;
    }

    public String getRangoFechas() {
        return rangoFechas;
    }

    public void setRangoFechas(String rangoFechas) {
        this.rangoFechas = rangoFechas;
    }

    public String getHoras() {
        return horas;
    }

    public void setHoras(String horas) {
        this.horas = horas;
    }

    public int getServicios() {
        return servicios;
    }

    public void setServicios(int servicios) {
        this.servicios = servicios;
    }

    public int getFacturacion() {
        return facturacion;
    }

    public void setFacturacion(int facturacion) {
        this.facturacion = facturacion;
    }

    public int getPrima() {
        return prima;
    }

    public void setPrima(int prima) {
        this.prima = prima;
    }

    public int getPrimaBase() {
        return primaBase;
    }

    public void setPrimaBase(int primaBase) {
        this.primaBase = primaBase;
    }

    public int getExtraHoras() {
        return extraHoras;
    }

    public void setExtraHoras(int extraHoras) {
        this.extraHoras = extraHoras;
    }
}