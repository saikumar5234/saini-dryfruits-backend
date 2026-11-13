
package com.example.demo.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // Keep the same secret to maintain compatibility
    // with existing tokens.
    private static final String SECRET =
            "ADMIN_SECRET_KEY_ADMIN_SECRET_KEY";

    private final SecretKey key =
            Keys.hmacShaKeyFor(SECRET.getBytes());

    // Existing token generation
    public String generateToken(String email, String role) {

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(
                    new Date(System.currentTimeMillis() + 86400000)
                )
                .signWith(key)
                .compact();
    }

    // Validate token and check Superadmin role
    public boolean isSuperAdminToken(String token) {

        try {

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String role = claims.get("role", String.class);

            return "SUPER_ADMIN".equalsIgnoreCase(role);

        } catch (JwtException | IllegalArgumentException ex) {

            return false;
        }
    }
}
