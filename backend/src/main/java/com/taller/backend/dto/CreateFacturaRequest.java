package com.taller.backend.dto;

public class CreateFacturaRequest {

    private String discordId;
    private String matricula;
    private String tipo;
    private Integer total;
    private Boolean convenio;

    private String modelo;
    private String estado;
    private Integer cantidad;
    private String item;
    private String categoria;
    private String gravedad;
    private String tuneoPlate;
    private String tuneoSeleccionados;

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Boolean getConvenio() {
        return convenio;
    }

    public void setConvenio(Boolean convenio) {
        this.convenio = convenio;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getGravedad() {
        return gravedad;
    }

    public void setGravedad(String gravedad) {
        this.gravedad = gravedad;
    }

    public String getTuneoPlate() {
        return tuneoPlate;
    }

    public void setTuneoPlate(String tuneoPlate) {
        this.tuneoPlate = tuneoPlate;
    }

    public String getTuneoSeleccionados() {
        return tuneoSeleccionados;
    }

    public void setTuneoSeleccionados(String tuneoSeleccionados) {
        this.tuneoSeleccionados = tuneoSeleccionados;
    }
}