package com.taller.backend.dto;

public class FichajeResponse {

    private Boolean fichajeActivo;
    private String mensaje;
    private String fechaHoraEntrada;
    private String fechaHoraSalida;
    private Integer minutosTrabajados;
    private String tipoServicio;

    public Boolean getFichajeActivo() {
        return fichajeActivo;
    }

    public void setFichajeActivo(Boolean fichajeActivo) {
        this.fichajeActivo = fichajeActivo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getFechaHoraEntrada() {
        return fechaHoraEntrada;
    }

    public void setFechaHoraEntrada(String fechaHoraEntrada) {
        this.fechaHoraEntrada = fechaHoraEntrada;
    }

    public String getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    public void setFechaHoraSalida(String fechaHoraSalida) {
        this.fechaHoraSalida = fechaHoraSalida;
    }

    public Integer getMinutosTrabajados() {
        return minutosTrabajados;
    }

    public void setMinutosTrabajados(Integer minutosTrabajados) {
        this.minutosTrabajados = minutosTrabajados;
    }
    public String getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(String tipoServicio) {
        this.tipoServicio = tipoServicio;
    }
}
