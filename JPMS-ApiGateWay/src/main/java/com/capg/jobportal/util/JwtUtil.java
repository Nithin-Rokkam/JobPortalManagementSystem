package com.capg.jobportal.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
 
import java.security.Key;
import java.util.Date;



/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: JwtUtil
 * DESCRIPTION:
 * This utility class provides functionality for handling JWT
 * (JSON Web Token) operations such as validation and data extraction.
 *
 * KEY RESPONSIBILITIES:
 * - Parse JWT tokens
 * - Validate token expiration
 * - Extract claims like userId and role
 *
 * SECURITY:
 * - Uses secret key for token signature verification
 * - Ensures token integrity and authenticity
 *
 * PURPOSE:
 * Acts as a central utility for JWT-based authentication in the
 * API Gateway, enabling secure request validation and user
 * identification across microservices.
 * ================================================================
 */
@Component
public class JwtUtil {
	
	@Value("${jwt.secret}")
	private String secret;
	
	/*
	 * ================================================================
	 * METHOD: getSigningKey
	 * DESCRIPTION:
	 * Generates a cryptographic signing key using the secret value
	 * defined in application properties.
	 *
	 * PURPOSE:
	 * This key is used to verify the signature of incoming JWT tokens,
	 * ensuring they are not tampered with.
	 *
	 * RETURNS:
	 * - Key → HMAC SHA-based signing key
	 * ================================================================
	 */
	private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
	
 
	/*
	 * ================================================================
	 * METHOD: extractAllClaims
	 * DESCRIPTION:
	 * Parses the JWT token and extracts all claims (payload data)
	 * present inside it.
	 *
	 * FUNCTIONALITY:
	 * - Validates token signature using signing key
	 * - Decodes token payload into Claims object
	 *
	 * PARAMETERS:
	 * - token → JWT token string
	 *
	 * RETURNS:
	 * - Claims → contains all token data (userId, role, expiry, etc.)
	 * ================================================================
	 */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
 
    
    /*
     * ================================================================
     * METHOD: isTokenValid
     * DESCRIPTION:
     * Validates whether the provided JWT token is still valid.
     *
     * VALIDATION STEPS:
     * - Extracts claims from token
     * - Checks if token is expired
     * - Handles exceptions for invalid tokens
     *
     * PARAMETERS:
     * - token → JWT token string
     *
     * RETURNS:
     * - true  → if token is valid and not expired
     * - false → if token is invalid or expired
     *
     * PURPOSE:
     * Ensures only authenticated and valid users can access protected APIs.
     * ================================================================
     */
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
 
    
    /*
     * ================================================================
     * METHOD: extractUserId
     * DESCRIPTION:
     * Extracts the user ID (subject) from the JWT token.
     *
     * PARAMETERS:
     * - token → JWT token string
     *
     * RETURNS:
     * - String → userId stored in token subject
     *
     * PURPOSE:
     * Used for identifying the user making the request.
     * ================================================================
     */
    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }
 
    
    /*
     * ================================================================
     * METHOD: extractRole
     * DESCRIPTION:
     * Extracts the user role from the JWT token claims.
     *
     * PARAMETERS:
     * - token → JWT token string
     *
     * RETURNS:
     * - String → role of the user (e.g., ADMIN, RECRUITER, JOB_SEEKER)
     *
     * PURPOSE:
     * Used for role-based authorization in downstream services.
     * ================================================================
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }
}
