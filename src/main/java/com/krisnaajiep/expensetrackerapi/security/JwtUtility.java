package com.krisnaajiep.expensetrackerapi.security;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 27/06/25 19.06
@Last Modified 27/06/25 19.06
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.security.config.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtility {
    private final SecretKey secretKey;
    private final long expiration;

    public JwtUtility(AuthProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getJwt().getSecret()));
        this.expiration = properties.getJwt().getExpiration();
    }

    public String generateToken(String subject, String email) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .claim("email", email)
                .signWith(secretKey)
                .compact();
    }

    public Jws<Claims> getClaims(String token) {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
    }

    public String getEmail(String token) {
        return getClaims(token)
                .getPayload()
                .get("email", String.class);
    }

    public boolean isTokenValid(String token) {
        String email = getEmail(token);
        return email != null && !email.isBlank();
    }
}
