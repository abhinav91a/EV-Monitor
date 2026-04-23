package com.evmonitor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean

    public OpenAPI evMonitorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EV Infrastructure Monitor API")
                        .description("""
                    Real-time EV charging station availability API for London.
                    """)
                        .version("v1.0")
                        .contact(new Contact()
                                .name("EV Monitor Team")
                                .email("")
                                .url(""))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("")
                                .description("")
                ));
    }
}