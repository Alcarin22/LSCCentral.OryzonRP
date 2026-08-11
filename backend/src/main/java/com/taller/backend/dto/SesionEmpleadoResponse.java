package com.taller.backend.dto;

public class SesionEmpleadoResponse {

    private Long id;
    private String discordId;
    private String nombre;
    private Boolean activo;
    private String avatarUrl;
    private String nickServidor;
    private SesionRangoResponse rango;
    private Boolean puedeTrabajarComoSeguridad;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getNickServidor() {
        return nickServidor;
    }

    public void setNickServidor(String nickServidor) {
        this.nickServidor = nickServidor;
    }

    public SesionRangoResponse getRango() {
        return rango;
    }

    public void setRango(SesionRangoResponse rango) {
        this.rango = rango;
    }
    public Boolean getPuedeTrabajarComoSeguridad() {
        return puedeTrabajarComoSeguridad;
    }

    public void setPuedeTrabajarComoSeguridad(Boolean puedeTrabajarComoSeguridad) {
        this.puedeTrabajarComoSeguridad = puedeTrabajarComoSeguridad;
    }
}
