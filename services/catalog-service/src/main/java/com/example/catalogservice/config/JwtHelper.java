package com.example.catalogservice.config;

import com.example.catalogservice.share.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Component
public class JwtHelper {
    @Value("${jwt.secret}")
    private String secret;

    public String extractUsername(String token) {
        // Claims::getSubject == claims -> claims.getSubject() i pass this function to extractFromClaims and he will apply it
        return extractFromClaims(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractFromClaims(token, claims -> claims.get("role", String.class));
    }

    public Date extractTokenExpirationDate(String token) {
        return extractFromClaims(token, Claims::getExpiration);
    }

    public <T> T extractFromClaims(String token, Function<Claims, T> func) {
        Claims claim = extractClaims(token);
        return func.apply(claim);
    }

    private Claims extractClaims(String token) {
        try {
            return Jwts
                    .parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw CustomException.badRequest("Invalid token");
        }
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token, UserDetails user) {
        String username = extractUsername(token);

        boolean isUserMatch = Objects.equals(username, user.getUsername());
        boolean isDateExpried = isTokenExpired(token);
        return isUserMatch && !isDateExpried;
    }

    public boolean isTokenExpired(String token) {
        Date tokenExpirationDate = extractTokenExpirationDate(token);
        return tokenExpirationDate.before(new Date(System.currentTimeMillis()));
    }

    public String generateToken(UserDetails userDetails, int period) {
        return this.generateToken(new HashMap<>(), userDetails, period);
    }

    public String generateToken(Map<String, Object> claims, UserDetails userDetails, int period) {
        return Jwts
                    .builder()
                    .claims(claims)
                    .subject(userDetails.getUsername())
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + period)) // 10 hours
                    .signWith(getSignInKey())
                    .compact();
    }
}
