package com.event.eventservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eventServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Event Service API")
                        .description("REST API documentation for event management operations")
                        .version("v1")
                        .contact(new Contact()
                                .name("Event Service Team")
                                .email("support@eventservice.local"))
                        .license(new License()
                                .name("Internal Use")
                                .url("https://example.local/license")));
    }
}
