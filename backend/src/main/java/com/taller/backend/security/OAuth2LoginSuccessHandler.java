package com.taller.backend.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller.backend.dto.DiscordLoginResponse;
import com.taller.backend.service.DiscordOAuth2Service;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final DiscordOAuth2Service discordOAuth2Service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OAuth2LoginSuccessHandler(DiscordOAuth2Service discordOAuth2Service) {
        this.discordOAuth2Service = discordOAuth2Service;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauth2User.getAttributes();

        DiscordLoginResponse loginResponse = discordOAuth2Service.buildLoginResponse(attributes);
        String payload = objectMapper.writeValueAsString(loginResponse);

        String html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="UTF-8">
              <title>Login completado</title>
            </head>
            <body>
              <script>
                (function() {
                  const user = %s;
                  if (window.opener) {
                    window.opener.postMessage(user, 'http://localhost:4200');
                  }
                  window.close();
                })();
              </script>
            </body>
            </html>
            """.formatted(payload);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);
        response.getWriter().flush();
    }
}