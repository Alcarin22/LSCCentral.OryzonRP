package com.taller.backend.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.taller.backend.dto.DiscordLoginResponse;
import com.taller.backend.entity.Empleado;
import com.taller.backend.entity.Rango;
import com.taller.backend.repository.EmpleadoRepository;

@Service
public class DiscordOAuth2Service {

    private final EmpleadoRepository empleadoRepository;

    public DiscordOAuth2Service(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    public DiscordLoginResponse buildLoginResponse(Map<String, Object> attributes) {
        String discordId = getAsString(attributes.get("id"));
        String username = getAsString(attributes.get("username"));
        String globalName = getAsString(attributes.get("global_name"));
        String avatar = getAsString(attributes.get("avatar"));

        Optional<Empleado> empleadoOpt = empleadoRepository.findByDiscordId(discordId);

        Empleado empleado = empleadoOpt.orElseGet(() -> {
            Empleado nuevo = new Empleado();
            nuevo.setDiscordId(discordId);
            nuevo.setNombre(globalName != null && !globalName.isBlank() ? globalName : username);
            nuevo.setActivo(false);
            return empleadoRepository.save(nuevo);
        });

        if (empleado.getNombre() == null || empleado.getNombre().isBlank()) {
            empleado.setNombre(globalName != null && !globalName.isBlank() ? globalName : username);
            empleado = empleadoRepository.save(empleado);
        }

        DiscordLoginResponse response = new DiscordLoginResponse();
        response.setId(empleado.getId());
        response.setDiscordId(discordId);
        response.setNombre(empleado.getNombre());
        response.setNickServidor(empleado.getNombre());
        response.setActivo(Boolean.TRUE.equals(empleado.getActivo()));
        response.setAvatarUrl(buildAvatarUrl(discordId, avatar));

        if (empleado.getRango() != null) {
            response.setRango(toDto(empleado.getRango()));
        }

        return response;
    }

    private DiscordLoginResponse.RangoDto toDto(Rango rango) {
        DiscordLoginResponse.RangoDto dto = new DiscordLoginResponse.RangoDto();
        dto.setId(rango.getId());
        dto.setNombre(rango.getNombre());
        dto.setNivel(rango.getNivel());
        return dto;
    }

    private String buildAvatarUrl(String discordId, String avatar) {
        if (discordId == null || avatar == null || avatar.isBlank()) {
            return "https://cdn.discordapp.com/embed/avatars/0.png";
        }
        return "https://cdn.discordapp.com/avatars/" + discordId + "/" + avatar + ".png?size=256";
    }

    private String getAsString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }
}