package com.taller.backend.dto;

import java.util.List;

public class MisPrimasResponse {

    private String nombreEmpleado;
    private String rango;
    private String semana;
    private String rangoFechas;
    private int weekOffset;

    private int primaEstimada;
    private int primaBase;
    private int extraHoras;

    private int facturacionSemanal;
    private String horasTrabajadas;
    private int serviciosRealizados;
    private int diasTrabajados;
    private int porcentajeAplicado;

    private int recordGlobalFacturacion;
    private int recordPersonalFacturacion;

    private List<PrimaDiaResponse> actividadDiaria;
    private List<PrimaHistorialSemanaResponse> historico;

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    public String getRango() {
        return rango;
    }

    public void setRango(String rango) {
        this.rango = rango;
    }

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

    public int getWeekOffset() {
        return weekOffset;
    }

    public void setWeekOffset(int weekOffset) {
        this.weekOffset = weekOffset;
    }

    public int getPrimaEstimada() {
        return primaEstimada;
    }

    public void setPrimaEstimada(int primaEstimada) {
        this.primaEstimada = primaEstimada;
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

    public int getFacturacionSemanal() {
        return facturacionSemanal;
    }

    public void setFacturacionSemanal(int facturacionSemanal) {
        this.facturacionSemanal = facturacionSemanal;
    }

    public String getHorasTrabajadas() {
        return horasTrabajadas;
    }

    public void setHorasTrabajadas(String horasTrabajadas) {
        this.horasTrabajadas = horasTrabajadas;
    }

    public int getServiciosRealizados() {
        return serviciosRealizados;
    }

    public void setServiciosRealizados(int serviciosRealizados) {
        this.serviciosRealizados = serviciosRealizados;
    }

    public int getDiasTrabajados() {
        return diasTrabajados;
    }

    public void setDiasTrabajados(int diasTrabajados) {
        this.diasTrabajados = diasTrabajados;
    }

    public int getPorcentajeAplicado() {
        return porcentajeAplicado;
    }

    public void setPorcentajeAplicado(int porcentajeAplicado) {
        this.porcentajeAplicado = porcentajeAplicado;
    }

    public int getRecordGlobalFacturacion() {
        return recordGlobalFacturacion;
    }

    public void setRecordGlobalFacturacion(int recordGlobalFacturacion) {
        this.recordGlobalFacturacion = recordGlobalFacturacion;
    }

    public int getRecordPersonalFacturacion() {
        return recordPersonalFacturacion;
    }

    public void setRecordPersonalFacturacion(int recordPersonalFacturacion) {
        this.recordPersonalFacturacion = recordPersonalFacturacion;
    }

    public List<PrimaDiaResponse> getActividadDiaria() {
        return actividadDiaria;
    }

    public void setActividadDiaria(List<PrimaDiaResponse> actividadDiaria) {
        this.actividadDiaria = actividadDiaria;
    }

    public List<PrimaHistorialSemanaResponse> getHistorico() {
        return historico;
    }

    public void setHistorico(List<PrimaHistorialSemanaResponse> historico) {
        this.historico = historico;
    }
}