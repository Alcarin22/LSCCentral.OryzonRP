package com.taller.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.taller.backend.dto.DiscordGuildMemberData;

@Service
public class DiscordService {

    @Value("${discord.bot-token}")
    private String botToken;

    @Value("${discord.guild-id}")
    private String guildId;

    private final RestTemplate restTemplate = new RestTemplate();

    public DiscordGuildMemberData obtenerDatosMiembroServidor(String discordId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bot " + botToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String memberUrl = "https://discord.com/api/v10/guilds/" + guildId + "/members/" + discordId;

            ResponseEntity<Map> memberResponse = restTemplate.exchange(
                    memberUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> memberBody = memberResponse.getBody();

            if (memberBody == null) {
                throw new RuntimeException("No se pudo obtener la información del miembro en Discord");
            }

            @SuppressWarnings("unchecked")
            List<String> roleIds = (List<String>) memberBody.get("roles");

            String nickServidor = (String) memberBody.get("nick");
            String guildAvatar = (String) memberBody.get("avatar");

            String avatarUrlServidor = null;
            if (guildAvatar != null && !guildAvatar.isBlank()) {
                avatarUrlServidor = "https://cdn.discordapp.com/guilds/" + guildId
                        + "/users/" + discordId
                        + "/avatars/" + guildAvatar + ".png?size=256";
            }

            String rolesUrl = "https://discord.com/api/v10/guilds/" + guildId + "/roles";

            ResponseEntity<List> rolesResponse = restTemplate.exchange(
                    rolesUrl,
                    HttpMethod.GET,
                    entity,
                    List.class
            );

            List<?> rolesBody = rolesResponse.getBody();

            if (rolesBody == null) {
                throw new RuntimeException("No se pudieron obtener los roles del servidor en Discord");
            }

            List<String> nombresRoles = new ArrayList<>();

            for (Object rolObj : rolesBody) {
                if (!(rolObj instanceof Map<?, ?> rolMapObj)) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> rol = (Map<String, Object>) rolMapObj;

                Object idObj = rol.get("id");
                Object nombreObj = rol.get("name");

                if (idObj instanceof String roleId && nombreObj instanceof String nombreRol) {
                    if (roleIds.contains(roleId)) {
                        nombresRoles.add(normalizarNombreRol(nombreRol));
                    }
                }
            }

            DiscordGuildMemberData data = new DiscordGuildMemberData();
            data.setNickServidor(nickServidor);
            data.setAvatarUrlServidor(avatarUrlServidor);
            data.setNombresRoles(nombresRoles);

            return data;

        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo datos del miembro en Discord para el usuario " + discordId, e);
        }
    }

    private String normalizarNombreRol(String nombreRolDiscord) {
        return switch (nombreRolDiscord.trim()) {
            case "Mecanico" -> "Mecánico";
            case "Jefe Mecanico" -> "Jefe Mecánico";
            case "Dueno" -> "Dueño";
            case "Owner" -> "Dueño";
            default -> nombreRolDiscord;
        };
    }
}