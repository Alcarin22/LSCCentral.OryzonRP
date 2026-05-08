package com.taller.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller.backend.entity.Empleado;
import com.taller.backend.repository.EmpleadoRepository;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

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
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SecurityConfig(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
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
                            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

                            String discordId = oauthUser.getAttribute("id");
                            String username = oauthUser.getAttribute("username");
                            String avatar = oauthUser.getAttribute("avatar");

                            Empleado empleado = empleadoRepository.findByDiscordId(discordId)
                                    .orElse(null);

                            if (empleado == null || Boolean.FALSE.equals(empleado.getActivo())) {
                                escribirRespuestaPopup(response, null);
                                return;
                            }

                            Map<String, Object> rango = new HashMap<>();
                            rango.put("id", empleado.getRango().getId());
                            rango.put("nombre", empleado.getRango().getNombre());
                            rango.put("nivel", empleado.getRango().getNivel());

                            String avatarUrl = avatar != null
                                    ? "https://cdn.discordapp.com/avatars/" + discordId + "/" + avatar + ".png"
                                    : "https://cdn.discordapp.com/embed/avatars/0.png";

                            Map<String, Object> user = new HashMap<>();
                            user.put("id", empleado.getId());
                            user.put("discordId", empleado.getDiscordId());
                            user.put("nombre", empleado.getNombre());
                            user.put("activo", empleado.getActivo());
                            user.put("avatarUrl", avatarUrl);
                            user.put("nickServidor", username != null ? username : empleado.getNombre());
                            user.put("rango", rango);

                            escribirRespuestaPopup(response, user);
                        })
                );

        return http.build();
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