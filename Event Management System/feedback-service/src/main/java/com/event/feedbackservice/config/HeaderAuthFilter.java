package com.event.feedbackservice.config;// ← change package per service

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Reads the trusted X-User-* headers forwarded by the API Gateway
 * and populates the Spring SecurityContext.
 * No JWT validation is done here — the gateway already did it.
 */
@Component
public class HeaderAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(HeaderAuthFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String email  = request.getHeader("X-User-Email");
        String role   = request.getHeader("X-User-Role");
        String path = request.getRequestURI();

        if (email != null && role != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            log.debug("Setting authentication for path: {} | email={}, role={}", path, email, role);
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(email, null, Collections.singletonList(authority));
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        } else if ((email == null || role == null) && SecurityContextHolder.getContext().getAuthentication() == null) {
            log.warn("Missing X-User headers for path: {} | userId={}, email={}, role={}", path, userId, email, role);
        }

        filterChain.doFilter(request, response);
    }
}