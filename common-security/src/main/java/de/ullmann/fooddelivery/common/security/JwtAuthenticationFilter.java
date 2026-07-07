package de.ullmann.fooddelivery.common.security;

import java.io.IOException;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final List<String> excludedPaths;

    public JwtAuthenticationFilter(
            JwtUtils jwtUtils,
            List<String> excludedPaths) {
        this.jwtUtils = jwtUtils;
        this.excludedPaths = excludedPaths;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return excludedPaths.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                Claims claims = jwtUtils.parseClaims(token);

                String userId = claims.getSubject();
                String role = claims.get("role", String.class);

                var authority = new SimpleGrantedAuthority("ROLE_" + role);

                var authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(authority)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (JwtException e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
