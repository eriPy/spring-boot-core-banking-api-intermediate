package com.eripy.banking_system.config;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import io.jsonwebtoken.security.Keys;

@Configuration 
public class SecurityConfig {
    @Bean 
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        JwtAuthFilter jwtAuthFilter
    ) throws Exception {
        http
            .csrf(crsf -> crsf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                )
                .permitAll()
                .anyRequest()
                .authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public SecretKey secretKey(@Value("${jwt.secret}") String jwtSecret) {
        return Keys
            .hmacShaKeyFor(jwtSecret
                .getBytes(StandardCharsets.UTF_8)
        );
    }
}
