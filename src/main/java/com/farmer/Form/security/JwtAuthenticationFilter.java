package com.farmer.Form.security;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtutil;

    public JwtAuthenticationFilter(JwtUtil jwtutil) {
        this.jwtutil = jwtutil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Define a list of public URLs
        String[] publicUrls = { 
            "/api/auth/login", 
            "/api/auth/register", 
            "/api/auth/register-with-role", 
            "/api/auth/register-simple", 
            "/api/auth/send-otp", 
            "/api/auth/verify-otp", 
            "/api/auth/forgot-password", 
            "/api/auth/reset-password/confirm", 
            "/api/auth/forgot-user-id", 
            "/api/auth/captcha", 
            "/api/captcha", 
            "/api/otp", 
            "/api/auth/test", 
            "/api/auth/test-login", 
            "/api/auth/test-registration", 
            "/api/test",
            "/error" 
        };

        // Get the requested URL
        String requestUri = request.getRequestURI();
        log.info("Requested URL: {}", requestUri);

        // Check if the requested URL is public
        boolean isPublicUrl = false;
        for (String url : publicUrls) {
            if (requestUri.equals(url)) {
                isPublicUrl = true;
                break;
            }
        }

        // If it's a public URL, skip token validation
        if (isPublicUrl) {
            log.info("Public URL detected, skipping token validation: {}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }
        
        log.info("Protected URL detected, checking token: {}", requestUri);

        // Get the JWT token from the Authorization header
        String authorizationHeader = request.getHeader("Authorization");
        String jwtToken = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7);
        }

        if (jwtToken == null) {
            log.info("No token found in request");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT Token is missing");
            return;
        }

        try {
            // Validate token
            if (!jwtutil.validateToken(jwtToken)) {
                log.info("Invalid token");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT Token is invalid or expired");
                return;
            }

            // Extract claims
            Claims claims = jwtutil.extractClaims(jwtToken);
            String username = claims.get("sub", String.class);

            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);

            // ✅ Convert roles to Spring Security authorities with ROLE_ prefix
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    username,
                    "",
                    roles.stream()
                         .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                         .collect(Collectors.toList())
            );

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT Token is expired");
            return;
        } catch (Exception e) {
            log.error("JWT processing failed: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT Token is invalid");
            return;
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}
