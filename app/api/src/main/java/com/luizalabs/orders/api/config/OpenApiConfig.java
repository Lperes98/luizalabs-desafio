package com.luizalabs.orders.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${api.info.title}")
    private String title;

    @Value("${api.info.version}")
    private String version;

    @Value("${api.info.description}")
    private String description;

    @Value("${api.contact.name}")
    private String contactName;

    @Value("${api.contact.email}")
    private String contactEmail;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title(title)
                                .version(version)
                                .description(description)
                                .contact(new Contact().name(contactName).email(contactEmail)));
    }
}
