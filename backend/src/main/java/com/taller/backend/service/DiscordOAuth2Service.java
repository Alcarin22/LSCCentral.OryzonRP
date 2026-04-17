package com.taller.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.taller.backend.dto.AuthResponse;
import com.taller.backend.dto.DiscordGuildMemberData;
import com.taller.backend.entity.Empleado;
import com.taller.backend.entity.Rango;
import com.taller.backend.repository.EmpleadoRepository;
import com.taller.backend.repository.RangoRepository;

@Service
public class DiscordOAuth2Service {

    private final EmpleadoRepository empleadoRepository;
    private final RangoRepository rangoRepository;
    private final DiscordService discordService;

    public DiscordOAuth2Service(
            EmpleadoRepository empleadoRepository,
            RangoRepository rangoRepository,
            DiscordService discordService) {
        this.empleadoRepository = empleadoRepository;
        this.rangoRepository = rangoRepository;
        this.discordService = discordService;
    }

    public AuthResponse buildLoginResponse(String discordId, String nombreGlobal, String avatarUrlGlobal) {
        DiscordGuildMemberData memberData = discordService.obtenerDatosMiembroServidor(discordId);

        List<String> nombresRolesDiscord = memberData.getNombresRoles();
        Rango rangoAsignado = resolverRangoMasAlto(nombresRolesDiscord);

        String nombreFinal = (memberData.getNickServidor() != null && !memberData.getNickServidor().isBlank())
                ? memberData.getNickServidor()
                : nombreGlobal;

        String avatarFinal = (memberData.getAvatarUrlServidor() != null && !memberData.getAvatarUrlServidor().isBlank())
                ? memberData.getAvatarUrlServidor()
                : avatarUrlGlobal;

        Empleado empleado = empleadoRepository.findByDiscordId(discordId)
                .map(existente -> {
                    existente.setNombre(nombreFinal);
                    existente.setActivo(true);
                    existente.setRango(rangoAsignado);
                    return empleadoRepository.save(existente);
                })
                .orElseGet(() -> {
                    Empleado nuevo = new Empleado();
                    nuevo.setDiscordId(discordId);
                    nuevo.setNombre(nombreFinal);
                    nuevo.setActivo(true);
                    nuevo.setRango(rangoAsignado);
                    return empleadoRepository.save(nuevo);
                });

        AuthResponse response = new AuthResponse();
        response.setId(empleado.getId());
        response.setDiscordId(empleado.getDiscordId());
        response.setNombre(empleado.getNombre());
        response.setNickServidor(nombreFinal);
        response.setAvatarUrl(avatarFinal);
        response.setActivo(empleado.getActivo());

        AuthResponse.RangoDto rangoDto = new AuthResponse.RangoDto();
        rangoDto.setId(empleado.getRango().getId());
        rangoDto.setNombre(empleado.getRango().getNombre());
        rangoDto.setNivel(empleado.getRango().getNivel());

        response.setRango(rangoDto);

        return response;
    }

    private Rango resolverRangoMasAlto(List<String> nombresRolesDiscord) {
        List<Rango> rangosCoincidentes = rangoRepository.findByNombreIn(nombresRolesDiscord);

        if (rangosCoincidentes.isEmpty()) {
            throw new RuntimeException("No se encontró ningún rango válido en la base de datos para los roles de Discord: " + nombresRolesDiscord);
        }

        return rangosCoincidentes.stream()
                .max((r1, r2) -> Integer.compare(r1.getNivel(), r2.getNivel()))
                .orElseThrow(() -> new RuntimeException("No se pudo determinar el rango más alto"));
    }
}