package com.taller.backend.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;

import com.taller.backend.entity.Empleado;
import com.taller.backend.repository.EmpleadoRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final EmpleadoRepository empleadoRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            EmpleadoRepository empleadoRepository
    ) {
        this.jwtService = jwtService;
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(
                        HttpHeaders.AUTHORIZATION
                );

        if (
                authorizationHeader == null
                        || !authorizationHeader.startsWith("Bearer ")
        ) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                authorizationHeader
                        .substring(7)
                        .trim();

        if (
                token.isBlank()
        ) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        try {

            String discordId =
                    jwtService.obtenerDiscordId(
                            token
                    );

            if (
                    discordId != null
                            && !discordId.isBlank()
                            && SecurityContextHolder
                                    .getContext()
                                    .getAuthentication() == null
            ) {

                empleadoRepository
                        .findByDiscordId(
                                discordId
                        )
                        .filter(empleado ->
                                Boolean.TRUE.equals(
                                        empleado.getActivo()
                                )
                        )
                        .ifPresent(empleado ->
                                autenticarEmpleado(
                                        request,
                                        empleado
                                )
                        );
            }

        } catch (JWTVerificationException e) {

            SecurityContextHolder
                    .clearContext();

        } catch (Exception e) {

            SecurityContextHolder
                    .clearContext();

            System.err.println(
                    "JWT AUTH ERROR: "
                            + e.getMessage()
            );
        }

        filterChain.doFilter(
                request,
                response
        );
    }

    private void autenticarEmpleado(
            HttpServletRequest request,
            Empleado empleado
    ) {

        List<GrantedAuthority> authorities =
                new ArrayList<>();

        authorities.add(
                new SimpleGrantedAuthority(
                        "ROLE_EMPLEADO"
                )
        );

        if (
                empleado.getRango() != null
                        && empleado.getRango().getNivel() >= 4
        ) {

            authorities.add(
                    new SimpleGrantedAuthority(
                            "ROLE_ADMIN"
                    )
            );
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        empleado.getDiscordId(),
                        null,
                        authorities
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(
                                request
                        )
        );

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        authentication
                );
    }
}