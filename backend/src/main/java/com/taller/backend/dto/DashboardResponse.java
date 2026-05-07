package com.taller.backend.dto;

public class DashboardResponse {

    private String nombre;

    private String horasHoy;

    private Integer facturasHoy;

    private Integer facturasSemana;

    private Integer facturacionSemana;

    public DashboardResponse() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getHorasHoy() {
        return horasHoy;
    }

    public void setHorasHoy(String horasHoy) {
        this.horasHoy = horasHoy;
    }

    public Integer getFacturasHoy() {
        return facturasHoy;
    }

    public void setFacturasHoy(Integer facturasHoy) {
        this.facturasHoy = facturasHoy;
    }

    public Integer getFacturasSemana() {
        return facturasSemana;
    }

    public void setFacturasSemana(Integer facturasSemana) {
        this.facturasSemana = facturasSemana;
    }

    public Integer getFacturacionSemana() {
        return facturacionSemana;
    }

    public void setFacturacionSemana(Integer facturacionSemana) {
        this.facturacionSemana = facturacionSemana;
    }
}