package com.startup_service.Config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;

// change per service

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Start-Up Service API")
                        .version("1.0")
                        .description("JWT Secured APIs"))
                
                // 🔐 Apply JWT globally
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))

                // 🔐 Define JWT scheme
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }
}
