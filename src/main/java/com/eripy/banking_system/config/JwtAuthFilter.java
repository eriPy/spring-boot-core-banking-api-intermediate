package com.eripy.banking_system.config;

import java.io.IOException;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.eripy.banking_system.service.account.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component 
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    public JwtAuthFilter(
        JwtService jwtService
    ) {
        this.jwtService = jwtService;
    }

    @Override 
    public void doFilterInternal(
        HttpServletRequest request, 
        HttpServletResponse response, 
        FilterChain chain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String username = jwtService.validateJwt(token);
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authToken);

            } catch (JwtException e) {
                logger.warn("Invalid token: " + e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }
}