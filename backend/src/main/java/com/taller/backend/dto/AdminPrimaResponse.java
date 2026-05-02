package com.taller.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AdminPrimaResponse {

    private Long id;
    private Long empleadoId;
    private String nombreEmpleado;
    private String rango;
    private Integer semana;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal facturado;
    private BigDecimal horas;
    private Integer servicios;
    private Integer porcentajeAplicado;
    private BigDecimal primaBase;
    private BigDecimal extraHoras;
    private BigDecimal total;
    private Boolean pagada;
    private LocalDateTime fechaPago;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmpleadoId() {
        return empleadoId;
    }

    public void setEmpleadoId(Long empleadoId) {
        this.empleadoId = empleadoId;
    }

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

    public Integer getSemana() {
        return semana;
    }

    public void setSemana(Integer semana) {
        this.semana = semana;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public BigDecimal getFacturado() {
        return facturado;
    }

    public void setFacturado(BigDecimal facturado) {
        this.facturado = facturado;
    }

    public BigDecimal getHoras() {
        return horas;
    }

    public void setHoras(BigDecimal horas) {
        this.horas = horas;
    }

    public Integer getServicios() {
        return servicios;
    }

    public void setServicios(Integer servicios) {
        this.servicios = servicios;
    }

    public Integer getPorcentajeAplicado() {
        return porcentajeAplicado;
    }

    public void setPorcentajeAplicado(Integer porcentajeAplicado) {
        this.porcentajeAplicado = porcentajeAplicado;
    }

    public BigDecimal getPrimaBase() {
        return primaBase;
    }

    public void setPrimaBase(BigDecimal primaBase) {
        this.primaBase = primaBase;
    }

    public BigDecimal getExtraHoras() {
        return extraHoras;
    }

    public void setExtraHoras(BigDecimal extraHoras) {
        this.extraHoras = extraHoras;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Boolean getPagada() {
        return pagada;
    }

    public void setPagada(Boolean pagada) {
        this.pagada = pagada;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }
}