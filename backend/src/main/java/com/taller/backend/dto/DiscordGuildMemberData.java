package com.taller.backend.dto;

import java.util.List;

public class DiscordGuildMemberData {

    private String nickServidor;
    private String avatarUrlServidor;
    private List<String> nombresRoles;

    public String getNickServidor() {
        return nickServidor;
    }

    public void setNickServidor(String nickServidor) {
        this.nickServidor = nickServidor;
    }

    public String getAvatarUrlServidor() {
        return avatarUrlServidor;
    }

    public void setAvatarUrlServidor(String avatarUrlServidor) {
        this.avatarUrlServidor = avatarUrlServidor;
    }

    public List<String> getNombresRoles() {
        return nombresRoles;
    }

    public void setNombresRoles(List<String> nombresRoles) {
        this.nombresRoles = nombresRoles;
    }
}