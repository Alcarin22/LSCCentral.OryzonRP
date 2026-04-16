package com.taller.backend.dto;

public class AuthResponse {

    private Long id;
    private String discordId;
    private String nombre;
    private Boolean activo;
    private String avatarUrl;
    private String nickServidor;
    private RangoDto rango;

    public static class RangoDto {
        private Long id;
        private String nombre;
        private Integer nivel;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public Integer getNivel() {
            return nivel;
        }

        public void setNivel(Integer nivel) {
            this.nivel = nivel;
        }
    }

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

    public RangoDto getRango() {
        return rango;
    }

    public void setRango(RangoDto rango) {
        this.rango = rango;
    }
}