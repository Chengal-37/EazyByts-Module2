package com.stocktrading.security; // Assuming this is the correct package for JwtUtils

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders; // Import this for Base64 decoding
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger; // Import for logging
import org.slf4j.LoggerFactory; // Import for logging
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key; // Import java.security.Key
import java.util.Date;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class); // Initialize logger

    // Ensure your application.properties uses these exact property names
    @Value("${stocktrading.app.jwtSecret}")
    private String jwtSecret;

    @Value("${stocktrading.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    // Corrected getSigningKey method to decode the Base64 secret
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret); // Correctly decode Base64
        return Keys.hmacShaKeyFor(keyBytes); // Create SecretKey from decoded bytes
    }

    public String generateJwtToken(Authentication authentication) {
        // Assuming UserDetails is directly your UserDetailsImpl or a compatible class
        // If your UserDetails implementation is specifically UserDetailsImpl, you might cast it:
        // UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                // Using System.currentTimeMillis() is generally more robust and clearer
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage()); // Use logger
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage()); // Use logger
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage()); // Use logger
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage()); // Use logger
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage()); // Use logger
        }
        return false;
    }
}