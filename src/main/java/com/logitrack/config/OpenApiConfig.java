package com.logitrack.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de la documentacion OpenAPI 3 (Swagger UI).
 * Define el esquema de seguridad JWT (bearerAuth) para poder probar los
 * endpoints protegidos desde la interfaz de Swagger.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI logitrackOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LogiTrack API")
                        .description("Sistema de gestion y auditoria de bodegas - LogiTrack S.A. "
                                + "API REST segura con autenticacion JWT.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("LogiTrack S.A.")
                                .email("contacto@logitrack.com"))
                        .license(new License().name("Apache 2.0")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("Ingrese el token JWT con el prefijo 'Bearer '")));
    }
}
