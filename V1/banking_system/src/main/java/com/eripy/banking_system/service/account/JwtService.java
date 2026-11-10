package com.eripy.banking_system.service.account;

import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;

@Service 
public class JwtService {
    private final SecretKey secretKey;
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.expiration}")
    private long jwtExpirationTime;

    public JwtService(
        SecretKey secretKey
    ) {
        this.secretKey = secretKey;
    }

    public String createJwt(Long id, long time, SecretKey secretKey) {
        logger.info("Generating Jwt");
        return Jwts.builder()
            .subject(String.valueOf(id))
            .claim("Account id", id)
            .expiration(new Date(System.currentTimeMillis() + time))
            .signWith(secretKey)
            .compact();
    }

    public String validateJwt(String jwt) {
        try {
            logger.info("The token is being verified");
            Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
            return claims.getSubject();

        } catch (ExpiredJwtException e) {
            logger.error("token expired: " + e.getMessage());
            throw new JwtException("Token expired");
        } catch (MalformedJwtException e) {
            logger.error("Invalid token: " + e.getMessage());
            throw new JwtException("Invalid token");
        } catch (JwtException e) {
            logger.error("Failed to validate token: " + e.getMessage());
            throw new JwtException("Failed to validate token");
        }
    }
}
