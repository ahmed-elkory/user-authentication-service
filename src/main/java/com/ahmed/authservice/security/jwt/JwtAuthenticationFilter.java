package com.ahmed.authservice.security.jwt;

import com.ahmed.authservice.appuser.AppUserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Custom JWT authentication filter.

 * Intercepts incoming HTTP requests and:
 * - Extracts JWT from Authorization header
 * - Validates token
 * - Sets authentication in SecurityContext

 * This enables stateless authentication.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private  final AppUserService appUserService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Skip if no token provided
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = appUserService.loadUserByUsername(username);

                // Validate token integrity & expiration
                if (!jwtService.isTokenValid(token, userDetails)) {
                    throw new RuntimeException("Invalid or expired token");
                }

                // Set authenticated user in context
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
            filterChain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT authentication failed: {}", e.getMessage());
            // Return 401 response
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter().write(
                    String.format(
                            "{\"message\": \"%s\", \"status\": %d}",
                            "Invalid or expired token",
                            401
                    )
            );
        }
    }
}