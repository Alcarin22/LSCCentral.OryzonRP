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

import org.springframework.web.client.RestTemplate;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class SecurityConfig {

    private final EmpleadoRepository empleadoRepository;
    private final RangoRepository rangoRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${discord.guild-id}")
    private String discordGuildId;

    @Value("${discord.empleado-role-id}")
    private String empleadoRoleId;

    public SecurityConfig(
            EmpleadoRepository empleadoRepository,
            RangoRepository rangoRepository,
            OAuth2AuthorizedClientService authorizedClientService
    ) {
        this.empleadoRepository = empleadoRepository;
        this.rangoRepository = rangoRepository;
        this.authorizedClientService = authorizedClientService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/**",
                                "/oauth2/**",
                                "/login/**",
                                "/error"
                        ).permitAll()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .successHandler((request, response, authentication) -> {

                            OAuth2AuthenticationToken oauthToken =
                                    (OAuth2AuthenticationToken) authentication;

                            OAuth2User oauthUser =
                                    (OAuth2User) authentication.getPrincipal();

                            OAuth2AuthorizedClient client =
                                    authorizedClientService.loadAuthorizedClient(
                                            oauthToken.getAuthorizedClientRegistrationId(),
                                            oauthToken.getName()
                                    );

                            if (client == null || client.getAccessToken() == null) {
                                escribirRespuestaPopup(response, null);
                                return;
                            }

                            String accessToken = client.getAccessToken().getTokenValue();

                            String discordId = oauthUser.getAttribute("id");
                            String username = oauthUser.getAttribute("username");
                            String avatar = oauthUser.getAttribute("avatar");

                            if (discordId == null || discordId.isBlank()) {
                                escribirRespuestaPopup(response, null);
                                return;
                            }

                            boolean tieneRolEmpleado = usuarioTieneRolEmpleado(accessToken);

                            if (!tieneRolEmpleado) {
                                escribirRespuestaPopup(response, null);
                                return;
                            }

                            Empleado empleado = empleadoRepository
                                    .findByDiscordId(discordId)
                                    .orElseGet(() -> crearEmpleadoDesdeDiscord(discordId, username));

                            if (Boolean.FALSE.equals(empleado.getActivo())) {
                                empleado.setActivo(true);
                                empleado = empleadoRepository.save(empleado);
                            }

                            Map<String, Object> user = crearRespuestaUsuario(
                                    empleado,
                                    discordId,
                                    username,
                                    avatar
                            );

                            escribirRespuestaPopup(response, user);
                        })
                );

        return http.build();
    }

    private boolean usuarioTieneRolEmpleado(String accessToken) {
        try {
            String url = "https://discord.com/api/users/@me/guilds/"
                    + discordGuildId
                    + "/member";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );

            JsonNode body = response.getBody();

            if (body == null || !body.has("roles")) {
                return false;
            }

            JsonNode roles = body.get("roles");

            if (!roles.isArray()) {
                return false;
            }

            for (JsonNode role : roles) {
                if (empleadoRoleId.equals(role.asText())) {
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            System.err.println("Error comprobando rol Empleado en Discord: " + e.getMessage());
            return false;
        }
    }

    private Empleado crearEmpleadoDesdeDiscord(String discordId, String username) {
        Rango rangoDefault = rangoRepository.findByNombre("Aprendiz")
                .orElseThrow(() -> new RuntimeException("No existe el rango Aprendiz"));

        Empleado empleado = new Empleado();

        empleado.setDiscordId(discordId);
        empleado.setNombre(username != null && !username.isBlank() ? username : "Empleado");
        empleado.setActivo(true);
        empleado.setRango(rangoDefault);

        return empleadoRepository.save(empleado);
    }

    private Map<String, Object> crearRespuestaUsuario(
            Empleado empleado,
            String discordId,
            String username,
            String avatar
    ) {
        Map<String, Object> rango = new HashMap<>();

        rango.put("id", empleado.getRango().getId());
        rango.put("nombre", empleado.getRango().getNombre());
        rango.put("nivel", empleado.getRango().getNivel());

        String avatarUrl = avatar != null && !avatar.isBlank()
                ? "https://cdn.discordapp.com/avatars/" + discordId + "/" + avatar + ".png"
                : "https://cdn.discordapp.com/embed/avatars/0.png";

        Map<String, Object> user = new HashMap<>();

        user.put("id", empleado.getId());
        user.put("discordId", empleado.getDiscordId());
        user.put("nombre", empleado.getNombre());
        user.put("activo", empleado.getActivo());
        user.put("avatarUrl", avatarUrl);
        user.put("nickServidor", username != null && !username.isBlank() ? username : empleado.getNombre());
        user.put("rango", rango);

        return user;
    }

    private void escribirRespuestaPopup(
            HttpServletResponse response,
            Map<String, Object> user
    ) throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        String json = objectMapper.writeValueAsString(user);

        response.getWriter().write("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Login completado</title>
                </head>
                <body>
                    <script>
                        if (window.opener) {
                            window.opener.postMessage(%s, "*");
                            window.close();
                        } else {
                            document.body.innerText = "Login completado. Puedes cerrar esta ventana.";
                        }
                    </script>
                </body>
                </html>
                """.formatted(json));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:4200",
                "https://*.vercel.app",
                "https://lsccentraloryzonrp-production.up.railway.app"
        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}