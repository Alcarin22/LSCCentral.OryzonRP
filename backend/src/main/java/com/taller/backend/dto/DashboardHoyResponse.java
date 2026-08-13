package com.taller.backend.dto;

public class DashboardHoyResponse {

    private String horaEntrada;
    private Boolean fichajeActivo;
    private Integer serviciosRealizadosHoy;
    private String tipoServicio;

    private String ultimoTurnoFecha;
    private String ultimoTurnoHoraEntrada;
    private String ultimoTurnoHoraSalida;
    private Integer ultimoTurnoMinutos;

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

    public String getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(String tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public String getUltimoTurnoFecha() {
        return ultimoTurnoFecha;
    }

    public void setUltimoTurnoFecha(String ultimoTurnoFecha) {
        this.ultimoTurnoFecha = ultimoTurnoFecha;
    }

    public String getUltimoTurnoHoraEntrada() {
        return ultimoTurnoHoraEntrada;
    }

    public void setUltimoTurnoHoraEntrada(String ultimoTurnoHoraEntrada) {
        this.ultimoTurnoHoraEntrada = ultimoTurnoHoraEntrada;
    }

    public String getUltimoTurnoHoraSalida() {
        return ultimoTurnoHoraSalida;
    }

    public void setUltimoTurnoHoraSalida(String ultimoTurnoHoraSalida) {
        this.ultimoTurnoHoraSalida = ultimoTurnoHoraSalida;
    }

    public Integer getUltimoTurnoMinutos() {
        return ultimoTurnoMinutos;
    }

    public void setUltimoTurnoMinutos(Integer ultimoTurnoMinutos) {
        this.ultimoTurnoMinutos = ultimoTurnoMinutos;
    }
}
