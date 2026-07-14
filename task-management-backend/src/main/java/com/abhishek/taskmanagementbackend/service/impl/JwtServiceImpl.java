package com.abhishek.taskmanagementbackend.service.impl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import com.abhishek.taskmanagementbackend.service.JwtService;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.SecretKey;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generates a signed JWT containing the authenticated
     * user's email as the subject.
     */
    @Override
    public String generateToken(String email) {

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();  //converts to string
    }

    /**
     * Converts the configured secret string into
     * a cryptographic key used to sign JWTs.
     */
    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Parses the JWT and returns all the claims stored in it.
     */
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the authenticated user's email
     * from the JWT subject claim.
     */
    @Override
    public String extractUsername(String token) {

        return extractAllClaims(token).getSubject();
    }

    /**
     * Checks whether the JWT is valid and has not expired.
     */
    @Override
    public boolean isTokenValid(String token) {

        try {

            Claims claims = extractAllClaims(token);

            return claims.getExpiration().after(new Date());

        } catch (Exception ex) {

            return false;

        }
    }
}