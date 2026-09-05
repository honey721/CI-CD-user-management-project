package com.usermgmt.usermgmt.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class JwtUtil {
    private final Key key = Keys.hmacShaKeyFor(
        // use 64+ char secret in real prod; here short for example
        "replace-with-very-long-secret-key-of-at-least-256-bits-please-change".getBytes()
    );

    public String generateToken(String email, Set<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(email)
                .claim("roles", roles)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(8, ChronoUnit.HOURS)))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token){
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException ex){
            return false;
        }
    }

    public String getEmailFromToken(String token){
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    @SuppressWarnings("unchecked")
    public Set<String> getRolesFromToken(String token){
        Object claim = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().get("roles");
        if (claim instanceof Collection) {
            return ((Collection<?>)claim).stream().map(Object::toString).collect(Collectors.toSet());
        }
        return Set.of();
    }
}
