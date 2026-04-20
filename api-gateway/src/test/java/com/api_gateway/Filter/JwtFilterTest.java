package com.api_gateway.Filter;

import com.api_gateway.Service.RedisService;
import com.api_gateway.Util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisService redisService;

    @Test
    void testFilterMissingAuthHeaderReturnsUnauthorized() {
        JwtFilter filter = new JwtFilter(jwtUtil, redisService);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/users/me").build());

        filter.filter(exchange, ex -> Mono.empty()).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(redisService, never()).isBlacklisted(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void testFilterBlacklistedTokenReturnsUnauthorized() {
        JwtFilter filter = new JwtFilter(jwtUtil, redisService);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/users/me")
                        .header("Authorization", "Bearer token123")
                        .build()
        );
        when(redisService.isBlacklisted("token123")).thenReturn(true);

        filter.filter(exchange, ex -> Mono.empty()).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void testFilterValidTokenMutatesRequestHeaders() {
        JwtFilter filter = new JwtFilter(jwtUtil, redisService);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/users/me")
                        .header("Authorization", "Bearer token123")
                        .build()
        );
        Claims claims = mock(Claims.class);
        when(redisService.isBlacklisted("token123")).thenReturn(false);
        when(jwtUtil.extractAllClaims("token123")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("user@test.com");
        when(claims.get("role", String.class)).thenReturn("ROLE_USER");

        AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            capturedExchange.set(ex);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        ServerWebExchange mutatedExchange = capturedExchange.get();
        assertNotNull(mutatedExchange);
        ServerHttpRequest request = mutatedExchange.getRequest();
        assertEquals("user@test.com", request.getHeaders().getFirst("X-User-Email"));
        assertEquals("ROLE_USER", request.getHeaders().getFirst("X-User-Role"));
    }

    @Test
    void testFilterPublicEndpointBypassesAuth() {
        JwtFilter filter = new JwtFilter(jwtUtil, redisService);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/auth/login").build());

        AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();
        filter.filter(exchange, ex -> {
            capturedExchange.set(ex);
            return Mono.empty();
        }).block();

        assertNotNull(capturedExchange.get());
        verify(redisService, never()).isBlacklisted(org.mockito.ArgumentMatchers.anyString());
    }
}
