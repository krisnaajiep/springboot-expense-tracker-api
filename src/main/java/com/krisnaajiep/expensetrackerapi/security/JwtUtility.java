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

import com.krisnaajiep.expensetrackerapi.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
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

    public JwtUtility(JwtConfig jwtConfig) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtConfig.getSecret()));
        this.expiration = jwtConfig.getExpiration();
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

    public Jws<Claims> getClaims(String token) throws Exception {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new Exception("Invalid JWT token");
        }
    }

    public String getSubject(String token) throws Exception {
        return getClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getEmail(String token) throws Exception {
        return getClaims(token)
                .getPayload()
                .get("email", String.class);
    }

    public boolean isTokenExpired(String token) throws Exception {
        Date expirationDate = getClaims(token).getPayload().getExpiration();
        return expirationDate.before(new Date());
    }
}
