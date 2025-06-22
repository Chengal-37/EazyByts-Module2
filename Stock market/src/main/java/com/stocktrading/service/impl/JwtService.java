package com.stocktrading.service.impl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import io.jsonwebtoken.Claims;

@Service
public class JwtService {

    // IMPORTANT: Replace with a strong, securely generated secret key.
    // For production, this should ideally be loaded from environment variables or a secure vault.
    @Value("${jwt.secret.key:yourVerySecretKeyWhichShouldBeAtLeast256BitsLongAndGeneratedSecurely}")
    private String SECRET_KEY; // Base64 encoded key

    // This method will generate the JWT token for a given username
    public String generateToken(String userName) {
        Map<String, Object> claims = new HashMap<>();
        // You can add additional claims here if needed (e.g., user roles)
        return createToken(claims, userName);
    }

    // Helper method to create the token
    private String createToken(Map<String, Object> claims, String userName) {
        return Jwts.builder()
                .setClaims(claims) // Custom claims
                .setSubject(userName) // Subject of the token (username)
                .setIssuedAt(new Date(System.currentTimeMillis())) // When the token was issued
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30)) // Token validity (e.g., 30 minutes)
                .signWith(getSignKey(), SignatureAlgorithm.HS256) // Sign the token with your secret key and algorithm
                .compact(); // Build and compact the token into a string
    }

    // Method to retrieve the signing key from your base64 encoded secret key
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // --- Methods for Token Validation (used by JWT Filter, not directly by AuthService.loginUser) ---

    // Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract a specific claim from the token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims from the token
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Check if the token is expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Validate the token (checks username and expiration)
    public Boolean validateToken(String token, String userDetailsUsername) {
        final String username = extractUsername(token);
        return (username.equals(userDetailsUsername) && !isTokenExpired(token));
    }
}