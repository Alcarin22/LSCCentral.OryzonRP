package com.taller.backend.dto;

public class DiscordUserResponse {

    private String id;
    private String username;
    private String global_name;
    private String avatar;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGlobal_name() {
        return global_name;
    }

    public void setGlobal_name(String global_name) {
        this.global_name = global_name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}