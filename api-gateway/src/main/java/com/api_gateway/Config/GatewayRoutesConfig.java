package com.api_gateway.Config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator founderLinkRoutes(RouteLocatorBuilder routes) {
        return routes.routes()
                .route("auth-service", r -> r.path("/auth/**").uri("lb://AUTH-SERVICE"))
                .route("user-service", r -> r.path("/users/**").uri("lb://USER-SERVICE"))
                .route("admin-service", r -> r.path("/admin/**").uri("lb://USER-SERVICE"))
                .route("startup-service", r -> r.path("/startups/**").uri("lb://STARTUP-SERVICE"))
                .route("investment-service", r -> r.path("/investments/**").uri("lb://INVESTMENT-SERVICE"))
                .route("team-service", r -> r.path("/team/**", "/teams/**")
                        .filters(f -> f.rewritePath("/(team|teams)(?<segment>/.*)", "/team${segment}"))
                        .uri("lb://TEAM-SERVICE"))
                .route("messaging-service", r -> r.path("/messages/**").uri("lb://MESSAGING-SERVICE"))
                .route("notification-service", r -> r.path("/notifications/**").uri("lb://NOTIFICATION-SERVICE"))
                .route("auth-service-docs", r -> r.path("/auth/v3/api-docs", "/auth/v3/api-docs/**")
                        .filters(f -> f.rewritePath("/auth/v3/api-docs(?<segment>/?.*)", "/v3/api-docs${segment}"))
                        .uri("lb://AUTH-SERVICE"))
                .route("user-service-docs", r -> r.path("/users/v3/api-docs", "/users/v3/api-docs/**")
                        .filters(f -> f.rewritePath("/users/v3/api-docs(?<segment>/?.*)", "/v3/api-docs${segment}"))
                        .uri("lb://USER-SERVICE"))
                .route("startup-service-docs", r -> r.path("/startups/v3/api-docs", "/startups/v3/api-docs/**")
                        .filters(f -> f.rewritePath("/startups/v3/api-docs(?<segment>/?.*)", "/v3/api-docs${segment}"))
                        .uri("lb://STARTUP-SERVICE"))
                .route("investment-service-docs", r -> r.path("/investments/v3/api-docs", "/investments/v3/api-docs/**")
                        .filters(f -> f.rewritePath("/investments/v3/api-docs(?<segment>/?.*)", "/v3/api-docs${segment}"))
                        .uri("lb://INVESTMENT-SERVICE"))
                .route("team-service-docs", r -> r.path("/team/v3/api-docs", "/team/v3/api-docs/**", "/teams/v3/api-docs", "/teams/v3/api-docs/**")
                        .filters(f -> f.rewritePath("/(team|teams)/v3/api-docs(?<segment>/?.*)", "/v3/api-docs${segment}"))
                        .uri("lb://TEAM-SERVICE"))
                .route("messaging-service-docs", r -> r.path("/messages/v3/api-docs", "/messages/v3/api-docs/**")
                        .filters(f -> f.rewritePath("/messages/v3/api-docs(?<segment>/?.*)", "/v3/api-docs${segment}"))
                        .uri("lb://MESSAGING-SERVICE"))
                .route("notification-service-docs", r -> r.path("/notifications/v3/api-docs", "/notifications/v3/api-docs/**")
                        .filters(f -> f.rewritePath("/notifications/v3/api-docs(?<segment>/?.*)", "/v3/api-docs${segment}"))
                        .uri("lb://NOTIFICATION-SERVICE"))
                .build();
    }
}
