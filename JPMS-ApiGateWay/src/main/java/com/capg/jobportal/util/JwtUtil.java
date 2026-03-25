package com.capg.jobportal.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
 
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
	
	@Value("${jwt.secret}")
	private String secret;
	
	
	private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
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
            boolean valid = !claims.getExpiration().before(new Date());
            System.out.println("Token valid: " + valid);
            return valid;
        } catch (Exception e) {
        	System.out.println("Token validation failed: " + e.getMessage());
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
