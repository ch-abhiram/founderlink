package com.startup_service.Filter;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class GatewayAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String email = request.getHeader("X-User-Email");
        String role = request.getHeader("X-User-Role");
        if (email != null && role != null) {
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority(role)));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(request, response);
    }
}
