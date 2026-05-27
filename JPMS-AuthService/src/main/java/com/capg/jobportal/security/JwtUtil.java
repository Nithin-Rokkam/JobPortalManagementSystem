package com.capg.jobportal.security;

import java.util.Date;
import java.util.UUID;
import java.security.Key;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {
	
	@Value("${jwt.secret}")
    private String secret;
 
    @Value("${jwt.access-expiry-ms}")
    private long accessExpiryMs;
 
    @Value("${jwt.refresh-expiry-ms}")
    private long refreshExpiryMs;
 
    
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
 
    
    public String generateAccessToken(Long userId, String role) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiryMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
 
    
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }
    
 
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
 
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    
 
    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }
    
 
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }
    
}
