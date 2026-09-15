package com.taller.backend.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.taller.backend.entity.Empleado;

@Service
public class JwtService {

    private static final String ISSUER = "lsc-central";

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final long expirationMinutes;

    public JwtService(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration-minutes:720}") long expirationMinutes
    ) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException(
                    "La variable JWT_SECRET es obligatoria"
            );
        }

        this.algorithm = Algorithm.HMAC256(jwtSecret);

        this.verifier = JWT
                .require(algorithm)
                .withIssuer(ISSUER)
                .build();

        this.expirationMinutes = expirationMinutes;
    }

    public String generarToken(Empleado empleado) {

        Instant ahora = Instant.now();

        Instant expiracion = ahora.plus(
                expirationMinutes,
                ChronoUnit.MINUTES
        );

        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(empleado.getDiscordId())
                .withClaim("empleadoId", empleado.getId())
                .withClaim(
                        "nivel",
                        empleado.getRango() != null
                                ? empleado.getRango().getNivel()
                                : 0
                )
                .withIssuedAt(Date.from(ahora))
                .withExpiresAt(Date.from(expiracion))
                .sign(algorithm);
    }

    public DecodedJWT validarToken(String token) {
        return verifier.verify(token);
    }

    public String obtenerDiscordId(String token) {
        return validarToken(token).getSubject();
    }

    public Long obtenerEmpleadoId(String token) {
        return validarToken(token)
                .getClaim("empleadoId")
                .asLong();
    }

    public Integer obtenerNivel(String token) {
        return validarToken(token)
                .getClaim("nivel")
                .asInt();
    }
}