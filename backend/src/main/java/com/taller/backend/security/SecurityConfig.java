package com.taller.backend.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller.backend.entity.Empleado;
import com.taller.backend.entity.Rango;
import com.taller.backend.repository.EmpleadoRepository;
import com.taller.backend.repository.RangoRepository;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import org.springframework.security.oauth2.core.user.OAuth2User;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
public class SecurityConfig {

    private final EmpleadoRepository empleadoRepository;
    private final RangoRepository rangoRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final JwtService jwtService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${discord.guild-id}")
    private String discordGuildId;

    @Value("${discord.empleado-role-id}")
    private String empleadoRoleId;

    @Value("${discord.bot-token:}")
    private String discordBotToken;

    @Value("${frontend.url}")
    private String frontendUrl;

    public SecurityConfig(
            EmpleadoRepository empleadoRepository,
            RangoRepository rangoRepository,
            OAuth2AuthorizedClientService authorizedClientService,
            JwtService jwtService,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.empleadoRepository = empleadoRepository;
        this.rangoRepository = rangoRepository;
        this.authorizedClientService = authorizedClientService;
        this.jwtService = jwtService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // CORS preflight
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // Endpoints públicos
                        .requestMatchers(
                                "/api/health",
                                "/oauth2/**",
                                "/login/**",
                                "/error"
                        ).permitAll()

                        // Administración: nivel 4 o superior
                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")

                        // Resto de la API: empleado autenticado
                        .requestMatchers(
                                "/api/**"
                        ).hasRole("EMPLEADO")

                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex

                        // Sin JWT válido
                        .authenticationEntryPoint(
                                (request, response, authException) ->
                                        response.sendError(
                                                HttpServletResponse.SC_UNAUTHORIZED
                                        )
                        )

                        // JWT válido, pero sin permisos suficientes
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) ->
                                        response.sendError(
                                                HttpServletResponse.SC_FORBIDDEN
                                        )
                        )
                )
                .oauth2Login(oauth -> oauth
                        .successHandler((request, response, authentication) -> {

                            OAuth2AuthenticationToken oauthToken =
                                    (OAuth2AuthenticationToken) authentication;

                            OAuth2User oauthUser =
                                    (OAuth2User) authentication.getPrincipal();

                            String discordId = oauthUser.getAttribute("id");
                            String username = oauthUser.getAttribute("username");
                            String avatar = oauthUser.getAttribute("avatar");

                            if (discordId == null || discordId.isBlank()) {
                                System.err.println(
                                        "LOGIN DENEGADO: no se pudo obtener discordId"
                                );

                                escribirRespuestaPopup(response, null);
                                return;
                            }

                            OAuth2AuthorizedClient client =
                                    authorizedClientService.loadAuthorizedClient(
                                            oauthToken.getAuthorizedClientRegistrationId(),
                                            oauthToken.getName()
                                    );

                            String accessToken =
                                    client != null && client.getAccessToken() != null
                                            ? client.getAccessToken().getTokenValue()
                                            : null;

                            DiscordMemberData memberData =
                                    obtenerMiembroDiscordConBot(discordId);

                            boolean tieneRolEmpleado =
                                    memberData != null
                                            && memberData.roleIds().contains(
                                                    empleadoRoleId
                                            );

                            if (
                                    !tieneRolEmpleado
                                            && accessToken != null
                                            && !accessToken.isBlank()
                            ) {
                                tieneRolEmpleado =
                                        usuarioTieneRolEmpleadoConOAuth(
                                                accessToken
                                        );
                            }

                            if (!tieneRolEmpleado) {
                                System.err.println(
                                        "LOGIN DENEGADO: usuario sin rol Empleado. Discord ID: "
                                                + discordId
                                );

                                escribirRespuestaPopup(response, null);
                                return;
                            }

                            Empleado empleado =
                                    empleadoRepository
                                            .findByDiscordId(discordId)
                                            .orElseGet(() ->
                                                    crearEmpleadoDesdeDiscord(
                                                            discordId,
                                                            username
                                                    )
                                            );

                            actualizarEmpleadoDesdeDiscord(
                                    empleado,
                                    memberData,
                                    username
                            );

                            Map<String, Object> user =
                                    crearRespuestaUsuario(
                                            empleado,
                                            discordId,
                                            username,
                                            avatar
                                    );

                            escribirRespuestaPopup(
                                    response,
                                    user
                            );
                        })
                );

        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    private DiscordMemberData obtenerMiembroDiscordConBot(
            String discordId
    ) {

        try {

            if (
                    discordBotToken == null
                            || discordBotToken.isBlank()
            ) {
                System.err.println(
                        "DISCORD BOT MEMBER ERROR: discord.bot-token vacío"
                );

                return null;
            }

            String url =
                    "https://discord.com/api/guilds/"
                            + discordGuildId
                            + "/members/"
                            + discordId;

            HttpHeaders headers =
                    new HttpHeaders();

            headers.set(
                    "Authorization",
                    "Bot " + discordBotToken
            );

            ResponseEntity<String> discordResponse =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            String.class
                    );

            JsonNode body =
                    objectMapper.readTree(
                            discordResponse.getBody()
                    );

            String nickServidor = null;

            if (
                    body != null
                            && body.has("nick")
                            && !body.get("nick").isNull()
            ) {
                nickServidor =
                        body.get("nick").asText();
            }

            List<String> roleIds =
                    new ArrayList<>();

            if (
                    body != null
                            && body.has("roles")
                            && body.get("roles").isArray()
            ) {

                for (JsonNode role : body.get("roles")) {
                    roleIds.add(
                            role.asText()
                    );
                }
            }

            return new DiscordMemberData(
                    nickServidor,
                    roleIds
            );

        } catch (HttpClientErrorException e) {

            System.err.println(
                    "DISCORD BOT MEMBER ERROR: "
                            + e.getStatusCode()
                            + " - "
                            + e.getResponseBodyAsString()
            );

            return null;

        } catch (Exception e) {

            System.err.println(
                    "DISCORD BOT MEMBER ERROR: "
                            + e.getMessage()
            );

            return null;
        }
    }

    private boolean usuarioTieneRolEmpleadoConOAuth(
            String accessToken
    ) {

        try {

            String url =
                    "https://discord.com/api/users/@me/guilds/"
                            + discordGuildId
                            + "/member";

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setBearerAuth(
                    accessToken
            );

            ResponseEntity<String> discordResponse =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            String.class
                    );

            JsonNode body =
                    objectMapper.readTree(
                            discordResponse.getBody()
                    );

            return contieneRolEmpleado(
                    body,
                    "OAuth"
            );

        } catch (HttpClientErrorException e) {

            System.err.println(
                    "DISCORD OAUTH CHECK ERROR: "
                            + e.getStatusCode()
                            + " - "
                            + e.getResponseBodyAsString()
            );

            return false;

        } catch (Exception e) {

            System.err.println(
                    "DISCORD OAUTH CHECK ERROR: "
                            + e.getMessage()
            );

            return false;
        }
    }

    private boolean contieneRolEmpleado(
            JsonNode body,
            String origen
    ) {

        if (
                body == null
                        || !body.has("roles")
        ) {

            System.err.println(
                    "DISCORD "
                            + origen
                            + " CHECK: respuesta sin roles"
            );

            return false;
        }

        JsonNode roles =
                body.get("roles");

        if (!roles.isArray()) {

            System.err.println(
                    "DISCORD "
                            + origen
                            + " CHECK: roles no es array"
            );

            return false;
        }

        for (JsonNode role : roles) {

            String roleId =
                    role.asText();

            if (
                    empleadoRoleId.equals(
                            roleId
                    )
            ) {

                System.out.println(
                        "DISCORD "
                                + origen
                                + " CHECK: rol Empleado encontrado"
                );

                return true;
            }
        }

        System.err.println(
                "DISCORD "
                        + origen
                        + " CHECK: rol Empleado NO encontrado"
        );

        return false;
    }

    private void actualizarEmpleadoDesdeDiscord(
            Empleado empleado,
            DiscordMemberData memberData,
            String username
    ) {

        String nombreServidor =
                obtenerNombreServidor(
                        memberData,
                        username
                );

        empleado.setNombre(
                nombreServidor
        );

        empleado.setActivo(
                true
        );

        Optional<Rango> rangoDiscord =
                detectarRangoDesdeRolesDiscord(
                        memberData
                );

        if (rangoDiscord.isPresent()) {

            empleado.setRango(
                    rangoDiscord.get()
            );

        } else if (
                empleado.getRango() == null
        ) {

            Rango rangoDefault =
                    rangoRepository
                            .findByNombre("Aprendiz")
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "No existe el rango Aprendiz"
                                    )
                            );

            empleado.setRango(
                    rangoDefault
            );
        }

        empleadoRepository.save(
                empleado
        );
    }

    private String obtenerNombreServidor(
            DiscordMemberData memberData,
            String username
    ) {

        if (
                memberData != null
                        && memberData.nickServidor() != null
                        && !memberData.nickServidor().isBlank()
        ) {
            return memberData.nickServidor();
        }

        if (
                username != null
                        && !username.isBlank()
        ) {
            return username;
        }

        return "Empleado";
    }

    private Optional<Rango> detectarRangoDesdeRolesDiscord(
            DiscordMemberData memberData
    ) {

        if (
                memberData == null
                        || memberData.roleIds().isEmpty()
        ) {
            return Optional.empty();
        }

        Map<String, String> rolesServidor =
                obtenerRolesServidorConBot();

        if (
                rolesServidor.isEmpty()
        ) {
            return Optional.empty();
        }

        List<String> nombresRolesUsuario =
                memberData
                        .roleIds()
                        .stream()
                        .map(rolesServidor::get)
                        .filter(nombre ->
                                nombre != null
                                        && !nombre.isBlank()
                        )
                        .toList();

        if (
                nombresRolesUsuario.isEmpty()
        ) {
            return Optional.empty();
        }

        List<Rango> rangos =
                rangoRepository.findAll();

        return rangos
                .stream()
                .filter(rango ->
                        nombresRolesUsuario
                                .stream()
                                .anyMatch(nombreRolDiscord ->
                                        normalizar(
                                                nombreRolDiscord
                                        ).equals(
                                                normalizar(
                                                        rango.getNombre()
                                                )
                                        )
                                )
                )
                .max(
                        Comparator.comparingInt(
                                Rango::getNivel
                        )
                );
    }

    private Map<String, String> obtenerRolesServidorConBot() {

        Map<String, String> roles =
                new HashMap<>();

        try {

            if (
                    discordBotToken == null
                            || discordBotToken.isBlank()
            ) {

                System.err.println(
                        "DISCORD BOT ROLES ERROR: discord.bot-token vacío"
                );

                return roles;
            }

            String url =
                    "https://discord.com/api/guilds/"
                            + discordGuildId
                            + "/roles";

            HttpHeaders headers =
                    new HttpHeaders();

            headers.set(
                    "Authorization",
                    "Bot " + discordBotToken
            );

            ResponseEntity<String> discordResponse =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            String.class
                    );

            JsonNode body =
                    objectMapper.readTree(
                            discordResponse.getBody()
                    );

            if (
                    body != null
                            && body.isArray()
            ) {

                for (JsonNode role : body) {

                    if (
                            role.has("id")
                                    && role.has("name")
                    ) {

                        roles.put(
                                role.get("id").asText(),
                                role.get("name").asText()
                        );
                    }
                }
            }

        } catch (HttpClientErrorException e) {

            System.err.println(
                    "DISCORD BOT ROLES ERROR: "
                            + e.getStatusCode()
                            + " - "
                            + e.getResponseBodyAsString()
            );

        } catch (Exception e) {

            System.err.println(
                    "DISCORD BOT ROLES ERROR: "
                            + e.getMessage()
            );
        }

        return roles;
    }

    private Empleado crearEmpleadoDesdeDiscord(
            String discordId,
            String username
    ) {

        Rango rangoDefault =
                rangoRepository
                        .findByNombre("Aprendiz")
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No existe el rango Aprendiz"
                                )
                        );

        Empleado empleado =
                new Empleado();

        empleado.setDiscordId(
                discordId
        );

        empleado.setNombre(
                username != null
                        && !username.isBlank()
                        ? username
                        : "Empleado"
        );

        empleado.setActivo(
                true
        );

        empleado.setRango(
                rangoDefault
        );

        empleado.setPuedeTrabajarComoSeguridad(
                false
        );

        return empleadoRepository.save(
                empleado
        );
    }

    private Map<String, Object> crearRespuestaUsuario(
            Empleado empleado,
            String discordId,
            String username,
            String avatar
    ) {

        Map<String, Object> rango =
                new HashMap<>();

        rango.put(
                "id",
                empleado.getRango().getId()
        );

        rango.put(
                "nombre",
                empleado.getRango().getNombre()
        );

        rango.put(
                "nivel",
                empleado.getRango().getNivel()
        );

        String avatarUrl =
                avatar != null
                        && !avatar.isBlank()
                        ? "https://cdn.discordapp.com/avatars/"
                                + discordId
                                + "/"
                                + avatar
                                + ".png"
                        : "https://cdn.discordapp.com/embed/avatars/0.png";

        Map<String, Object> user =
                new HashMap<>();

        user.put(
                "id",
                empleado.getId()
        );

        user.put(
                "discordId",
                empleado.getDiscordId()
        );

        user.put(
                "nombre",
                empleado.getNombre()
        );

        user.put(
                "activo",
                empleado.getActivo()
        );

        user.put(
                "puedeTrabajarComoSeguridad",
                Boolean.TRUE.equals(
                        empleado.getPuedeTrabajarComoSeguridad()
                )
        );

        user.put(
                "avatarUrl",
                avatarUrl
        );

        user.put(
                "nickServidor",
                empleado.getNombre()
        );

        user.put(
                "rango",
                rango
        );

        user.put(
                "token",
                jwtService.generarToken(
                        empleado
                )
        );

        return user;
    }

    private void escribirRespuestaPopup(
            HttpServletResponse response,
            Map<String, Object> user
    ) throws IOException {

        response.setContentType(
                "text/html;charset=UTF-8"
        );

        String json =
                objectMapper.writeValueAsString(
                        user
                );

        response.getWriter().write(
                """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Login completado</title>
                </head>
                <body>
                    <script>
                        if (window.opener) {
                            window.opener.postMessage(%s, "%s");
                            window.close();
                        } else {
                            document.body.innerText =
                                "Login completado. Puedes cerrar esta ventana.";
                        }
                    </script>
                </body>
                </html>
                """.formatted(
                        json,
                        frontendUrl
                )
        );
    }

    private String normalizar(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return Normalizer
                .normalize(
                        value,
                        Normalizer.Form.NFD
                )
                .replaceAll(
                        "\\p{M}",
                        ""
                )
                .toLowerCase()
                .trim();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOriginPatterns(
                List.of(
                        "http://localhost:4200",
                        frontendUrl,
                        "https://*.vercel.app",
                        "https://lsccentraloryzonrp-production.up.railway.app"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setExposedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(
                true
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    private record DiscordMemberData(
            String nickServidor,
            List<String> roleIds
    ) {
    }
}